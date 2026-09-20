package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.UserEntity
import com.example.engine.BackgammonAI
import com.example.engine.BackgammonRules
import com.example.engine.GeminiAssistant
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = GameRepository(application)

    val userProfile: StateFlow<UserEntity> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserEntity()
    )

    val matchHistory = repository.matchHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val friendsList = repository.friendsList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _aiCommentary = MutableStateFlow<String>("🎲 Welcome to Takhte Nard! Roll the dice to begin.")
    val aiCommentary: StateFlow<String> = _aiCommentary.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val chatMessages: StateFlow<List<Pair<String, String>>> = _chatMessages.asStateFlow()

    private var aiTurnJob: Job? = null
    private var timerJob: Job? = null
    private val undoStack = mutableListOf<GameState>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty() && _gameState.value?.currentTurn == PlayerColor.WHITE && _gameState.value?.isGameOver == false

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
        }
    }

    fun startNewGame(
        bet: Long,
        mode: GameMode,
        opponentName: String = "AI Grandmaster"
    ) {
        undoStack.clear()
        val currentUser = userProfile.value
        val initial = BackgammonRules.createInitialState(
            bet = bet,
            whiteName = currentUser.username,
            blackName = opponentName,
            mode = mode
        )
        _gameState.value = initial.copy(
            isRollingForTurn = true,
            openingRoll = null
        )
        _chatMessages.value = emptyList()
        _aiCommentary.value = "🎲 برای تعیین شروع‌کننده بازی، تاس بریزید!"

        startTurnTimer()
    }

    fun rollDice() {
        val state = _gameState.value ?: return
        if (state.dice != null || state.isGameOver) return

        // Case 1: Opening Roll to determine who goes first
        if (state.isRollingForTurn) {
            if (state.openingRoll != null) return // currently in animation
            viewModelScope.launch {
                val whiteDie = (1..6).random()
                val blackDie = (1..6).random()
                _gameState.value = state.copy(openingRoll = Pair(whiteDie, blackDie))
                
                delay(1200) // let the opening dice roll animation complete on the board

                if (whiteDie > blackDie) {
                    _aiCommentary.value = "👑 شما برنده شدید ($whiteDie به $blackDie)! بازی با نوبت شما آغاز می‌شود."
                    val newState = _gameState.value?.copy(
                        isRollingForTurn = false,
                        currentTurn = PlayerColor.WHITE,
                        dice = DiceRoll(whiteDie, blackDie),
                        openingRoll = null,
                        turnTimerSeconds = 20,
                        isUsingBankTime = false
                    ) ?: return@launch
                    _gameState.value = newState
                    startTurnTimer()
                    val legalMoves = BackgammonRules.getLegalMoves(newState)
                    if (legalMoves.isEmpty()) {
                        delay(1200)
                        confirmTurn()
                    }
                } else if (blackDie > whiteDie) {
                    _aiCommentary.value = "🤖 حریف برنده شد ($blackDie به $whiteDie)! بازی با نوبت حریف آغاز می‌شود."
                    val newState = _gameState.value?.copy(
                        isRollingForTurn = false,
                        currentTurn = PlayerColor.BLACK,
                        dice = DiceRoll(blackDie, whiteDie),
                        openingRoll = null,
                        turnTimerSeconds = 20,
                        isUsingBankTime = false
                    ) ?: return@launch
                    _gameState.value = newState
                    startTurnTimer()
                    checkTurnAndTriggerAI()
                } else {
                    _aiCommentary.value = "🤝 هر دو تاس $whiteDie آمدند! تاس مساوی است، لطفاً دوباره پرتاب کنید."
                    delay(800)
                    _gameState.value = _gameState.value?.copy(openingRoll = null)
                }
            }
            return
        }

        // Case 2: Regular turn roll
        undoStack.clear()
        val rolled = BackgammonRules.rollDice()
        val newState = state.copy(
            dice = rolled,
            turnTimerSeconds = 20,
            isUsingBankTime = false
        )
        _gameState.value = newState

        startTurnTimer()

        // Check legal moves
        val legalMoves = BackgammonRules.getLegalMoves(newState)
        if (legalMoves.isEmpty()) {
            viewModelScope.launch {
                _aiCommentary.value = "❌ هیچ حرکتی با این تاس‌ها ممکن نیست! نوبت واگذار می‌شود..."
                delay(1400)
                confirmTurn()
            }
        } else {
            triggerCommentaryUpdate(newState)
        }
    }

    fun undoLastMove() {
        val current = _gameState.value ?: return
        if (undoStack.isNotEmpty() && !current.isGameOver) {
            val previousState = undoStack.removeAt(undoStack.size - 1)
            _gameState.value = previousState.copy(
                selectedPoint = null,
                highlightedMoves = emptyList(),
                turnTimerSeconds = current.turnTimerSeconds,
                whiteBankSeconds = current.whiteBankSeconds,
                blackBankSeconds = current.blackBankSeconds,
                isUsingBankTime = current.isUsingBankTime
            )
            _aiCommentary.value = "↺ حرکت بازگردانده شد. حرکت خود را انتخاب کنید."
        }
    }

    fun confirmTurn() {
        val state = _gameState.value ?: return
        if (state.isGameOver || state.isRollingForTurn) return

        undoStack.clear()
        val switched = BackgammonRules.switchTurn(state)
        _gameState.value = switched.copy(
            turnTimerSeconds = 20,
            isUsingBankTime = false
        )
        _aiCommentary.value = "نوبت به بازیکن ${if (switched.currentTurn == PlayerColor.WHITE) "شما (سفید)" else "حریف (مشکی)"} واگذار شد."
        startTurnTimer()
        checkTurnAndTriggerAI()
    }

    private fun startTurnTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // Phase 1: Standard 20 seconds turn timer
            var time = 20
            while (time > 0) {
                val state = _gameState.value ?: break
                if (state.isGameOver) break
                _gameState.value = state.copy(turnTimerSeconds = time, isUsingBankTime = false)
                delay(1000)
                time--
            }

            val stateAfter20s = _gameState.value ?: return@launch
            if (stateAfter20s.isGameOver) return@launch

            // If player has already played all moves or has no legal moves left, auto confirm turn without consuming bank time!
            val canMove = BackgammonRules.canMakeAnyMove(stateAfter20s)
            val hasDice = stateAfter20s.dice != null

            if (hasDice && !canMove) {
                confirmTurn()
                return@launch
            }

            // Phase 2: Bank Time (30 seconds cumulative extra reserve across the match)
            val activePlayer = stateAfter20s.currentTurn
            var bankRemaining = if (activePlayer == PlayerColor.WHITE) {
                stateAfter20s.whiteBankSeconds
            } else {
                stateAfter20s.blackBankSeconds
            }

            _aiCommentary.value = "⏳ مهلت ۲۰ ثانیه به پایان رسید! بانک زمان ۳۰ ثانیه‌ای فعال شد."

            while (bankRemaining > 0) {
                val curState = _gameState.value ?: break
                if (curState.isGameOver) break

                // If during bank time all moves were made:
                if (curState.dice != null && !BackgammonRules.canMakeAnyMove(curState)) {
                    confirmTurn()
                    return@launch
                }

                val updatedState = if (curState.currentTurn == PlayerColor.WHITE) {
                    curState.copy(
                        turnTimerSeconds = 0,
                        isUsingBankTime = true,
                        whiteBankSeconds = bankRemaining
                    )
                } else {
                    curState.copy(
                        turnTimerSeconds = 0,
                        isUsingBankTime = true,
                        blackBankSeconds = bankRemaining
                    )
                }
                _gameState.value = updatedState
                delay(1000)
                bankRemaining--
            }

            val finalState = _gameState.value ?: return@launch
            if (!finalState.isGameOver) {
                onTurnTimerExpired(finalState)
            }
        }
    }

    private fun onTurnTimerExpired(state: GameState) {
        if (state.isGameOver) return
        timerJob?.cancel()

        val winnerColor = if (state.currentTurn == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE
        
        val finalState = state.copy(
            isGameOver = true,
            winner = winnerColor,
            turnTimerSeconds = 0,
            isUsingBankTime = false,
            whiteBankSeconds = if (state.currentTurn == PlayerColor.WHITE) 0 else state.whiteBankSeconds,
            blackBankSeconds = if (state.currentTurn == PlayerColor.BLACK) 0 else state.blackBankSeconds
        )
        
        _gameState.value = finalState
        
        _aiCommentary.value = "⏰ اتمام کل زمان! ${if (state.currentTurn == PlayerColor.WHITE) "شما" else "حریف"} به دلیل اتمام زمان بازنده شدید."
        
        handleGameOver(finalState)
    }

    fun onPointClicked(pointIndex: Int) {
        val state = _gameState.value ?: return
        if (state.dice == null || state.isGameOver) return

        val player = state.currentTurn
        val selected = state.selectedPoint

        if (selected == null) {
            val pt = if (pointIndex in 1..24) state.points[pointIndex] else PointState()
            if (pt.color == player && pt.count > 0) {
                val legalForPoint = BackgammonRules.getLegalMoves(state).filter { it.from == pointIndex }
                if (legalForPoint.size == 1) {
                    executeMove(legalForPoint.first())
                } else if (legalForPoint.isNotEmpty()) {
                    _gameState.value = state.copy(
                        selectedPoint = pointIndex,
                        highlightedMoves = legalForPoint
                    )
                }
            }
        } else {
            val matchingMove = state.highlightedMoves.firstOrNull { move ->
                move.to == pointIndex ||
                (move.isBearOff && (pointIndex == 0 || pointIndex == 25 || pointIndex == selected))
            }

            if (matchingMove != null) {
                executeMove(matchingMove)
            } else {
                val pt = if (pointIndex in 1..24) state.points[pointIndex] else PointState()
                if (pt.color == player && pt.count > 0) {
                    val legalForPoint = BackgammonRules.getLegalMoves(state).filter { it.from == pointIndex }
                    if (legalForPoint.size == 1) {
                        executeMove(legalForPoint.first())
                    } else if (legalForPoint.isNotEmpty()) {
                        _gameState.value = state.copy(
                            selectedPoint = pointIndex,
                            highlightedMoves = legalForPoint
                        )
                    } else {
                        _gameState.value = state.copy(selectedPoint = null, highlightedMoves = emptyList())
                    }
                } else {
                    _gameState.value = state.copy(selectedPoint = null, highlightedMoves = emptyList())
                }
            }
        }
    }

    fun onBarClicked(player: PlayerColor) {
        val state = _gameState.value ?: return
        if (state.currentTurn != player) return

        val barIndex = if (player == PlayerColor.WHITE) BackgammonRules.BAR_WHITE_INDEX else BackgammonRules.BAR_BLACK_INDEX
        val barCount = if (player == PlayerColor.WHITE) state.barWhite else state.barBlack

        if (barCount > 0) {
            val legalFromBar = BackgammonRules.getLegalMoves(state).filter { it.from == barIndex }
            if (legalFromBar.size == 1) {
                executeMove(legalFromBar.first())
            } else if (legalFromBar.isNotEmpty()) {
                _gameState.value = state.copy(
                    selectedPoint = barIndex,
                    highlightedMoves = legalFromBar
                )
            }
        }
    }

    private fun executeMove(move: Move) {
        val currentState = _gameState.value ?: return
        undoStack.add(currentState)

        val nextState = BackgammonRules.applyMove(currentState, move)
        _gameState.value = nextState

        if (nextState.isGameOver) {
            timerJob?.cancel()
            handleGameOver(nextState)
        } else {
            val canStillMove = BackgammonRules.canMakeAnyMove(nextState)
            if (!canStillMove) {
                _aiCommentary.value = "تمام حرکت‌ها انجام شد. دکمه تایید حرکت را بزنید یا حرکت را بازگردانید."
            }
        }
    }

    private fun checkTurnAndTriggerAI() {
        val state = _gameState.value ?: return
        if (state.isGameOver) return

        val isAiTurn = (state.currentTurn == PlayerColor.BLACK && state.gameMode in listOf(GameMode.AI_EASY, GameMode.AI_MEDIUM, GameMode.AI_HARD))

        if (isAiTurn) {
            aiTurnJob?.cancel()
            aiTurnJob = viewModelScope.launch {
                delay(800)
                val curState = _gameState.value ?: return@launch

                // 1. Roll AI Dice if not rolled
                if (curState.dice == null) {
                    rollDice()
                    delay(1000)
                }

                // 2. Play AI Moves in sequence
                var activeState = _gameState.value ?: return@launch
                while (activeState.currentTurn == PlayerColor.BLACK && !activeState.isGameOver && activeState.dice != null) {
                    val aiMove = BackgammonAI.chooseBestMove(activeState)
                    if (aiMove != null) {
                        activeState = BackgammonRules.applyMove(activeState, aiMove)
                        _gameState.value = activeState
                        delay(700)
                    } else {
                        break
                    }
                }

                if (activeState.isGameOver) {
                    handleGameOver(activeState)
                } else {
                    delay(500)
                    confirmTurn()
                }
            }
        }
    }

    private fun handleGameOver(finalState: GameState) {
        viewModelScope.launch {
            val isWhiteWin = (finalState.winner == PlayerColor.WHITE)
            val currentTheme = BoardThemes.getThemeById(userProfile.value.selectedBoardId)

            repository.recordMatchResult(
                opponentName = finalState.blackPlayerName,
                bet = finalState.matchBet,
                isWin = isWhiteWin,
                winType = finalState.winType.name,
                movesCount = finalState.moveHistory.size,
                boardName = currentTheme.name
            )

            _aiCommentary.value = if (isWhiteWin) {
                "🏆 VICTORY! You won ${finalState.matchBet} coins with a ${finalState.winType} win!"
            } else {
                "💔 MATCH DEFEAT! Better luck next roll!"
            }
        }
    }

    fun offerDouble() {
        val state = _gameState.value ?: return
        _gameState.value = BackgammonRules.doubleCube(state)

        // If AI opponent, respond automatically
        if (state.gameMode in listOf(GameMode.AI_EASY, GameMode.AI_MEDIUM, GameMode.AI_HARD)) {
            viewModelScope.launch {
                delay(1200)
                val accepted = (Math.random() < 0.75)
                val updated = BackgammonRules.respondToDouble(_gameState.value!!, accepted)
                _gameState.value = updated
                _aiCommentary.value = if (accepted) "🔥 AI Accepted the 2X Double!" else "🏳️ AI Forfeited the match!"
                if (updated.isGameOver) {
                    handleGameOver(updated)
                }
            }
        }
    }

    fun sendChatMessage(msg: String) {
        val list = _chatMessages.value.toMutableList()
        list.add(Pair(userProfile.value.username, msg))
        _chatMessages.value = list
    }

    fun claimDailyCoins(onResult: (Boolean, Long) -> Unit) {
        viewModelScope.launch {
            val res = repository.claimDailyReward()
            onResult(res.first, res.second)
        }
    }

    fun updateProfile(name: String, avatarId: Int) {
        viewModelScope.launch {
            repository.updateUsername(name, avatarId)
        }
    }

    fun selectBoardTheme(boardId: Int) {
        viewModelScope.launch {
            repository.selectBoard(boardId)
        }
    }

    fun addCoinsPackage(amount: Long) {
        viewModelScope.launch {
            repository.addCoins(amount)
        }
    }

    private fun triggerCommentaryUpdate(state: GameState) {
        viewModelScope.launch {
            _aiCommentary.value = GeminiAssistant.getMatchCommentary(state)
        }
    }

    fun exitGame() {
        _gameState.value = null
    }
}
