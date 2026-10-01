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
                
                delay(350) // Fast snappy opening roll animation

                if (whiteDie > blackDie) {
                    _aiCommentary.value = "👑 شما برنده شدید ($whiteDie به $blackDie)! بازی با نوبت شما آغاز می‌شود."
                    val newState = _gameState.value?.copy(
                        isRollingForTurn = false,
                        currentTurn = PlayerColor.WHITE,
                        dice = DiceRoll(whiteDie, blackDie),
                        openingRoll = null,
                        turnTimerSeconds = 30,
                        isUsingBankTime = false
                    ) ?: return@launch
                    _gameState.value = newState
                    startTurnTimer()
                    val legalMoves = BackgammonRules.getLegalMoves(newState)
                    if (legalMoves.isEmpty()) {
                        delay(300)
                        confirmTurn()
                    }
                } else if (blackDie > whiteDie) {
                    _aiCommentary.value = "🤖 حریف برنده شد ($blackDie به $whiteDie)! بازی با نوبت حریف آغاز می‌شود."
                    val newState = _gameState.value?.copy(
                        isRollingForTurn = false,
                        currentTurn = PlayerColor.BLACK,
                        dice = DiceRoll(blackDie, whiteDie),
                        openingRoll = null,
                        turnTimerSeconds = 30,
                        isUsingBankTime = false
                    ) ?: return@launch
                    _gameState.value = newState
                    startTurnTimer()
                    checkTurnAndTriggerAI()
                } else {
                    _aiCommentary.value = "🤝 هر دو تاس $whiteDie آمدند! تاس مساوی است، لطفاً دوباره پرتاب کنید."
                    delay(250)
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
            turnTimerSeconds = 30,
            isUsingBankTime = false
        )
        _gameState.value = newState

        startTurnTimer()

        // Check legal moves
        val legalMoves = BackgammonRules.getLegalMoves(newState)
        if (legalMoves.isEmpty()) {
            viewModelScope.launch {
                _aiCommentary.value = "❌ هیچ حرکتی با این تاس‌ها ممکن نیست! نوبت واگذار می‌شود..."
                delay(400)
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
            turnTimerSeconds = 30,
            isUsingBankTime = false,
            selectedPoint = null,
            highlightedMoves = emptyList()
        )
        _aiCommentary.value = "نوبت به بازیکن ${if (switched.currentTurn == PlayerColor.WHITE) "شما (سفید)" else "حریف (مشکی)"} واگذار شد."
        startTurnTimer()
        checkTurnAndTriggerAI()
    }

    private fun startTurnTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // Phase 1: Standard 30 seconds turn timer
            var time = 30
            while (time > 0) {
                val state = _gameState.value ?: break
                if (state.isGameOver) break
                _gameState.value = state.copy(turnTimerSeconds = time, isUsingBankTime = false)
                delay(1000)
                time--
            }

            val stateAfter30s = _gameState.value ?: return@launch
            if (stateAfter30s.isGameOver) return@launch

            // If player has already played all moves or has no legal moves left, auto confirm turn without consuming bank time!
            val canMove = BackgammonRules.canMakeAnyMove(stateAfter30s)
            val hasDice = stateAfter30s.dice != null

            if (hasDice && !canMove) {
                confirmTurn()
                return@launch
            }

            // Phase 2: Bank Time (30 seconds cumulative extra reserve across the match)
            val activePlayer = stateAfter30s.currentTurn
            var bankRemaining = if (activePlayer == PlayerColor.WHITE) {
                stateAfter30s.whiteBankSeconds
            } else {
                stateAfter30s.blackBankSeconds
            }

            _aiCommentary.value = "⏳ مهلت ۳۰ ثانیه به پایان رسید! بانک زمان فعال شد."

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
            // First tap: Select checker and highlight valid destination points
            val pt = if (pointIndex in 1..24) state.points[pointIndex] else PointState()
            if (pt.color == player && pt.count > 0) {
                val allLegal = BackgammonRules.getLegalMoves(state)
                val legalForPoint = allLegal.filter { it.from == pointIndex }
                if (legalForPoint.isNotEmpty()) {
                    _gameState.value = state.copy(
                        selectedPoint = pointIndex,
                        highlightedMoves = legalForPoint
                    )
                }
            }
        } else {
            // If tapping the currently selected point again -> Deselect
            if (pointIndex == selected) {
                _gameState.value = state.copy(selectedPoint = null, highlightedMoves = emptyList())
                return
            }

            // Check if tapping a highlighted target destination or bear off
            val matchingMove = state.highlightedMoves.firstOrNull { move ->
                move.to == pointIndex ||
                (move.isBearOff && (pointIndex == 0 || pointIndex == 25 || pointIndex == selected))
            }

            if (matchingMove != null) {
                executeMove(matchingMove)
            } else {
                // Tapping another own checker -> Switch selection
                val pt = if (pointIndex in 1..24) state.points[pointIndex] else PointState()
                if (pt.color == player && pt.count > 0) {
                    val allLegal = BackgammonRules.getLegalMoves(state)
                    val legalForPoint = allLegal.filter { it.from == pointIndex }
                    if (legalForPoint.isNotEmpty()) {
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
        if (state.currentTurn != player || state.isGameOver || state.dice == null) return

        val barIndex = if (player == PlayerColor.WHITE) BackgammonRules.BAR_WHITE_INDEX else BackgammonRules.BAR_BLACK_INDEX
        val barCount = if (player == PlayerColor.WHITE) state.barWhite else state.barBlack

        if (barCount > 0) {
            val legalFromBar = BackgammonRules.getLegalMoves(state).filter { it.from == barIndex }
            if (legalFromBar.isNotEmpty()) {
                _gameState.value = state.copy(
                    selectedPoint = barIndex,
                    highlightedMoves = legalFromBar
                )
            }
        }
    }

    private fun executeMove(move: Move) {
        val currentState = _gameState.value ?: return
        if (currentState.isGameOver || currentState.isMatchOver) return
        undoStack.add(currentState)

        val nextState = BackgammonRules.applyMove(currentState, move)
        if (nextState.isGameOver) {
            timerJob?.cancel()
            val scoreUpdated = BackgammonRules.updateMatchScoreAfterGame(nextState)
            _gameState.value = scoreUpdated
            handleGameOver(scoreUpdated)
        } else {
            _gameState.value = nextState
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
                delay(120)
                val curState = _gameState.value ?: return@launch

                // 1. Roll AI Dice if not rolled
                if (curState.dice == null) {
                    rollDice()
                    delay(200)
                }

                // 2. Play AI Moves in sequence
                var activeState = _gameState.value ?: return@launch
                while (activeState.currentTurn == PlayerColor.BLACK && !activeState.isGameOver && activeState.dice != null) {
                    val aiMove = BackgammonAI.chooseBestMove(activeState)
                    if (aiMove != null) {
                        activeState = BackgammonRules.applyMove(activeState, aiMove)
                        _gameState.value = activeState
                        delay(160) // Fast, smooth animation
                    } else {
                        break
                    }
                }

                if (activeState.isGameOver) {
                    handleGameOver(activeState)
                } else {
                    delay(100)
                    confirmTurn()
                }
            }
        }
    }

    private fun handleGameOver(finalState: GameState) {
        viewModelScope.launch {
            val stateWithScore = if (finalState.whiteMatchScore == 0 && finalState.blackMatchScore == 0 && finalState.winner != null) {
                BackgammonRules.updateMatchScoreAfterGame(finalState)
            } else {
                finalState
            }
            _gameState.value = stateWithScore

            val isWhiteWin = (stateWithScore.winner == PlayerColor.WHITE)
            val currentTheme = BoardThemes.getThemeById(userProfile.value.selectedBoardId)
            val wonPoints = BackgammonRules.calculateWonPoints(stateWithScore)
            val totalWonCoins = stateWithScore.matchBet * wonPoints

            repository.recordMatchResult(
                opponentName = stateWithScore.blackPlayerName,
                bet = if (isWhiteWin) totalWonCoins else stateWithScore.matchBet,
                isWin = isWhiteWin,
                winType = stateWithScore.winType.name,
                movesCount = stateWithScore.moveHistory.size,
                boardName = currentTheme.name
            )

            val persianWinType = when (stateWithScore.winType) {
                WinType.NORMAL -> "برد معمولی (Single - ۱ امتیاز)"
                WinType.GAMMON -> "برد مارسی (Gammon - ۲ امتیاز)"
                WinType.BACKGAMMON -> "برد مارسی دوبل (Backgammon - ۳ امتیاز)"
            }

            if (stateWithScore.isMatchOver) {
                val matchWon = stateWithScore.matchWinner == PlayerColor.WHITE
                _aiCommentary.value = if (matchWon) {
                    "🏆 تبریک! شما قهرمان مسابقه شدید! نتیجه نهایی: ${stateWithScore.whiteMatchScore} بر ${stateWithScore.blackMatchScore}"
                } else {
                    "💔 مسابقه به پایان رسید و حریف با نتیجه ${stateWithScore.blackMatchScore} بر ${stateWithScore.whiteMatchScore} پیروز شد."
                }
            } else {
                _aiCommentary.value = if (isWhiteWin) {
                    "🏆 پیروزی در این دست! $persianWinType با کیوب ${stateWithScore.doublingCubeValue}X ($wonPoints امتیاز مسابقه)."
                } else {
                    "💔 شکست در این دست! حریف $wonPoints امتیاز مسابقه کسب کرد."
                }
            }
        }
    }

    fun offerDouble() {
        val state = _gameState.value ?: return
        if (!BackgammonRules.canPlayerDouble(state, state.currentTurn)) {
            _aiCommentary.value = "⚠️ در این نوبت امکان پیشنهاد دوبل وجود ندارد."
            return
        }

        val updated = BackgammonRules.doubleCube(state)
        _gameState.value = updated
        _aiCommentary.value = "🎲 شما پیشنهاد دوبل (۲ برابر کردن امتیاز) دادید..."

        // If AI opponent, evaluate offer
        if (state.gameMode in listOf(GameMode.AI_EASY, GameMode.AI_MEDIUM, GameMode.AI_HARD)) {
            viewModelScope.launch {
                delay(1200)
                val cur = _gameState.value ?: return@launch
                if (!cur.doublingOffered) return@launch

                val (wPip, bPip) = BackgammonRules.calculatePipCount(cur.points, cur.barWhite, cur.barBlack)
                val accepted = (bPip <= wPip + 22)
                val finalDoubleState = BackgammonRules.respondToDouble(cur, accepted)
                _gameState.value = finalDoubleState
                _aiCommentary.value = if (accepted) {
                    "🔥 حریف پیشنهاد دوبل را پذیرفت (Take)! ارزش مکعب: ${finalDoubleState.doublingCubeValue}X"
                } else {
                    "🏳️ حریف بازی را واگذار کرد (Pass)!"
                }
                if (finalDoubleState.isGameOver) {
                    handleGameOver(finalDoubleState)
                }
            }
        }
    }

    fun respondToDouble(accepted: Boolean) {
        val state = _gameState.value ?: return
        if (!state.doublingOffered) return
        val updated = BackgammonRules.respondToDouble(state, accepted)
        _gameState.value = updated
        _aiCommentary.value = if (accepted) {
            "🔥 شما پیشنهاد دوبل را پذیرفتید (Take)! ارزش مکعب: ${updated.doublingCubeValue}X"
        } else {
            "🏳️ شما دست را واگذار کردید (Pass)."
        }
        if (updated.isGameOver) {
            handleGameOver(updated)
        }
    }

    fun offerResignation(type: WinType) {
        val state = _gameState.value ?: return
        if (state.isGameOver || state.isMatchOver) return

        val updated = BackgammonRules.offerResignation(state, PlayerColor.WHITE, type)
        _gameState.value = updated

        val typeLabel = when (type) {
            WinType.NORMAL -> "معمولی (Single)"
            WinType.GAMMON -> "مارس (Gammon)"
            WinType.BACKGAMMON -> "مارس دوبل (Backgammon)"
        }
        _aiCommentary.value = "🏳️ شما پیشنهاد تسلیم به صورت $typeLabel ارسال کردید..."

        // AI opponent evaluation
        if (state.gameMode in listOf(GameMode.AI_EASY, GameMode.AI_MEDIUM, GameMode.AI_HARD)) {
            viewModelScope.launch {
                delay(1000)
                val cur = _gameState.value ?: return@launch
                if (cur.resignationOfferedBy != PlayerColor.WHITE) return@launch

                val aiAccepts = when (type) {
                    WinType.BACKGAMMON, WinType.GAMMON -> true
                    WinType.NORMAL -> {
                        // AI only declines single if it has a dominating gammon chance
                        !(cur.offWhite == 0 && cur.offBlack >= 8)
                    }
                }

                val result = BackgammonRules.respondToResignation(cur, aiAccepts)
                _gameState.value = result

                if (aiAccepts) {
                    _aiCommentary.value = "🏳️ حریف پیشنهاد تسلیم $typeLabel شما را پذیرفت."
                    handleGameOver(result)
                } else {
                    _aiCommentary.value = "⚔️ حریف تسلیم معمولی شما را رد کرد و برای برد مارس ادامه می‌دهد!"
                }
            }
        }
    }

    fun respondToResignation(accepted: Boolean) {
        val state = _gameState.value ?: return
        if (state.resignationOfferedBy == null) return
        val result = BackgammonRules.respondToResignation(state, accepted)
        _gameState.value = result
        if (accepted && result.isGameOver) {
            handleGameOver(result)
        }
    }

    fun startNextGame() {
        val state = _gameState.value ?: return
        if (state.isMatchOver) return
        undoStack.clear()
        val next = BackgammonRules.startNextGameInMatch(state)
        _gameState.value = next
        _aiCommentary.value = "🎲 بازی شماره ${next.gameNumber} مسابقه آغاز شد! برای تعیین شروع‌کننده تاس بریزید."
        startTurnTimer()
    }

    fun resetMatch() {
        val state = _gameState.value ?: return
        undoStack.clear()
        val reset = BackgammonRules.resetMatch(state)
        _gameState.value = reset
        _aiCommentary.value = "🔄 مسابقه جدید آغاز شد! برای تعیین شروع‌کننده تاس بریزید."
        startTurnTimer()
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

    fun addGemsPackage(amount: Long) {
        viewModelScope.launch {
            repository.addGems(amount)
        }
    }

    fun buyVipPass() {
        viewModelScope.launch {
            repository.purchaseVipPass()
        }
    }

    fun selectDiceStyle(style: String) {
        viewModelScope.launch {
            repository.selectDiceStyle(style)
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
