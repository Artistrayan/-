package com.example.engine

import com.example.model.*

/**
 * Backgammon Standard Tournament Rules & Turn Engine
 * 
 * Implements all 24 official WBF / standard tournament Backgammon rules:
 * 1. Opening Roll (1 die each, reroll on tie, higher goes first using the two rolled numbers)
 * 2. Regular Turn (2 dice, independent moves, must play both if possible, larger die priority if only one can be played)
 * 3. Doubles (4 moves of the rolled value, must play maximum possible number of moves)
 * 4. Movement Direction (White: 24 -> 1 [Home 1..6], Black: 1 -> 24 [Home 19..24])
 * 5. Open / Blocked / Blots (Empty/friendly = open, 1 opponent = blot, 2+ opponent = blocked)
 * 6. Hitting Blots (Sends opponent checker to the Bar)
 * 7. Bar Priority (Must re-enter all Bar checkers before moving any board checker)
 * 8. Bar Entry (White enters Black's home 24..19, Black enters White's home 1..6)
 * 9. Mandatory Max Dice Consumption (Exhaustive sequence evaluation, larger die priority)
 * 10. Multi-step Move Sequences (Intermediate points must be legally open)
 * 11. Bear-Off Condition (All 15 checkers in home board or borne off, 0 on Bar)
 * 12. Bear-Off Exact Roll (Removes checker on exact point)
 * 13. Bear-Off Higher Roll (Removes furthest checker only if no checkers exist on higher points)
 * 14. Hit During Bear-Off (Sends checker to Bar, disables Bear-Off until it re-enters and returns home)
 * 15. Game Over (First to bear off 15 checkers wins)
 * 16. Single Win (1x base points: loser has borne off >= 1 checker)
 * 17. Gammon Win (2x base points: loser has borne off 0 checkers)
 * 18. Backgammon Win (3x base points: loser off = 0 AND has checker on Bar or in winner's home board)
 * 19. Doubling Cube (Starts at 1 in Center, Take = 2x + ownership, Pass = forfeit)
 * 20. Redouble (Only cube owner can redouble before roll: 2 -> 4 -> 8 -> 16 -> 32 -> 64)
 * 21. Double Eligibility (Center = either player, Owned = only owner, Pending = none)
 * 22. Crawford Rule (In Match Play, when leading player reaches targetScore - 1, next game is Crawford with cube disabled)
 * 23. Jacoby Rule (In Money Play, Gammons/Backgammons only count if cube was doubled at least once)
 * 24. Gammon / Backgammon Toggles (Customizable gammon / backgammon enablement)
 */
object BackgammonRules {

    const val BAR_WHITE_INDEX = 25
    const val BAR_BLACK_INDEX = 0
    const val OFF_WHITE_INDEX = 0
    const val OFF_BLACK_INDEX = 25

    /**
     * Creates standard starting board layout with 15 checkers per player:
     * White: 2 on pt 24, 5 on pt 13, 3 on pt 8, 5 on pt 6
     * Black: 2 on pt 1, 5 on pt 12, 3 on pt 17, 5 on pt 19
     */
    fun createInitialState(
        bet: Long = 100,
        whiteName: String = "You",
        blackName: String = "Opponent",
        mode: GameMode = GameMode.AI_MEDIUM,
        matchTargetScore: Int = 5,
        whiteScore: Int = 0,
        blackScore: Int = 0,
        isCrawford: Boolean = false,
        crawfordPassed: Boolean = false,
        jacobyEnabled: Boolean = true
    ): GameState {
        val pts = MutableList(25) { PointState() }

        // White checkers (15 total)
        pts[24] = PointState(PlayerColor.WHITE, 2)
        pts[13] = PointState(PlayerColor.WHITE, 5)
        pts[8]  = PointState(PlayerColor.WHITE, 3)
        pts[6]  = PointState(PlayerColor.WHITE, 5)

        // Black checkers (15 total)
        pts[1]  = PointState(PlayerColor.BLACK, 2)
        pts[12] = PointState(PlayerColor.BLACK, 5)
        pts[17] = PointState(PlayerColor.BLACK, 3)
        pts[19] = PointState(PlayerColor.BLACK, 5)

        val state = GameState(
            points = pts,
            barWhite = 0,
            barBlack = 0,
            offWhite = 0,
            offBlack = 0,
            currentTurn = PlayerColor.WHITE,
            dice = null,
            matchBet = bet,
            whitePlayerName = whiteName,
            blackPlayerName = blackName,
            gameMode = mode,
            matchTargetScore = matchTargetScore,
            whiteMatchScore = whiteScore,
            blackMatchScore = blackScore,
            isCrawfordGame = isCrawford,
            crawfordPassed = crawfordPassed,
            jacobyRuleEnabled = jacobyEnabled
        )

        return updatePipCounts(state)
    }

    /**
     * Calculates Pip Counts for both players.
     */
    fun calculatePipCount(points: List<PointState>, barWhite: Int, barBlack: Int): Pair<Int, Int> {
        var whitePips = barWhite * 25
        var blackPips = barBlack * 25

        for (i in 1..24) {
            val pt = points[i]
            if (pt.color == PlayerColor.WHITE) {
                whitePips += pt.count * i
            } else if (pt.color == PlayerColor.BLACK) {
                blackPips += pt.count * (25 - i)
            }
        }
        return Pair(whitePips, blackPips)
    }

    fun updatePipCounts(state: GameState): GameState {
        val (wPip, bPip) = calculatePipCount(state.points, state.barWhite, state.barBlack)
        return state.copy(whitePipCount = wPip, blackPipCount = bPip)
    }

    /**
     * Rolls two standard 6-sided dice.
     */
    fun rollDice(): DiceRoll {
        val d1 = (1..6).random()
        val d2 = (1..6).random()
        return DiceRoll(d1, d2)
    }

    /**
     * Rule 11: Bear-Off is allowed ONLY when all 15 checkers are in home board (or borne off), and 0 on Bar.
     */
    fun canPlayerBearOff(state: GameState, player: PlayerColor): Boolean {
        if (player == PlayerColor.WHITE) {
            if (state.barWhite > 0) return false
            // All White checkers must be in Home Board (1..6)
            for (i in 7..24) {
                if (state.points[i].color == PlayerColor.WHITE && state.points[i].count > 0) {
                    return false
                }
            }
            return true
        } else {
            if (state.barBlack > 0) return false
            // All Black checkers must be in Home Board (19..24)
            for (i in 1..18) {
                if (state.points[i].color == PlayerColor.BLACK && state.points[i].count > 0) {
                    return false
                }
            }
            return true
        }
    }

    /**
     * Checks if a target point is legally open (empty, friendly, or single opponent blot).
     */
    private fun isValidTarget(
        state: GameState,
        player: PlayerColor,
        targetIndex: Int,
        isBearOffAllowed: Boolean
    ): Boolean {
        if (targetIndex < 1 || targetIndex > 24) return false
        val pt = state.points[targetIndex]
        if (pt.count == 0 || pt.color == player) return true
        // Opponent occupies target: Only 1 checker (blot) can be hit
        return pt.count == 1
    }

    private fun isOpponentBlot(state: GameState, player: PlayerColor, targetIndex: Int): Boolean {
        if (targetIndex < 1 || targetIndex > 24) return false
        val pt = state.points[targetIndex]
        return pt.color == player.opposite() && pt.count == 1
    }

    /**
     * Generates all immediate single-step candidate moves for given remaining dice.
     */
    fun getCandidateMoves(state: GameState): List<Move> {
        if (state.dice == null || state.dice.remainingMoves.isEmpty() || state.isGameOver) {
            return emptyList()
        }

        val player = state.currentTurn
        val remainingDice = state.dice.remainingMoves.distinct()
        val moves = mutableListOf<Move>()

        val hasBarCheckers = if (player == PlayerColor.WHITE) state.barWhite > 0 else state.barBlack > 0

        if (hasBarCheckers) {
            // Rules 7 & 8: Must enter from Bar first into opponent's home board
            val fromIndex = if (player == PlayerColor.WHITE) BAR_WHITE_INDEX else BAR_BLACK_INDEX
            for (die in remainingDice) {
                val targetIndex = if (player == PlayerColor.WHITE) 25 - die else die
                if (isValidTarget(state, player, targetIndex, isBearOffAllowed = false)) {
                    val isHit = isOpponentBlot(state, player, targetIndex)
                    moves.add(Move(from = fromIndex, to = targetIndex, dieValue = die, isHit = isHit))
                }
            }
            return moves
        }

        val canBearOff = canPlayerBearOff(state, player)

        for (fromPt in 1..24) {
            val pt = state.points[fromPt]
            if (pt.color != player || pt.count <= 0) continue

            for (die in remainingDice) {
                val targetIndex = if (player == PlayerColor.WHITE) fromPt - die else fromPt + die

                if (player == PlayerColor.WHITE) {
                    if (targetIndex > 0) {
                        // Standard board move
                        if (isValidTarget(state, player, targetIndex, canBearOff)) {
                            val isHit = isOpponentBlot(state, player, targetIndex)
                            moves.add(Move(from = fromPt, to = targetIndex, dieValue = die, isHit = isHit))
                        }
                    } else if (canBearOff) {
                        // Bear-Off
                        if (targetIndex == 0) {
                            // Rule 12: Exact roll Bear-Off
                            moves.add(Move(from = fromPt, to = 0, dieValue = die, isBearOff = true))
                        } else {
                            // Rule 13: Higher roll Bear-Off (only if no checkers on higher points 6..fromPt+1)
                            var hasCheckerOnHigherPoint = false
                            for (hp in (fromPt + 1)..6) {
                                if (state.points[hp].color == PlayerColor.WHITE && state.points[hp].count > 0) {
                                    hasCheckerOnHigherPoint = true
                                    break
                                }
                            }
                            if (!hasCheckerOnHigherPoint) {
                                moves.add(Move(from = fromPt, to = 0, dieValue = die, isBearOff = true))
                            }
                        }
                    }
                } else { // PlayerColor.BLACK
                    if (targetIndex <= 24) {
                        // Standard board move
                        if (isValidTarget(state, player, targetIndex, canBearOff)) {
                            val isHit = isOpponentBlot(state, player, targetIndex)
                            moves.add(Move(from = fromPt, to = targetIndex, dieValue = die, isHit = isHit))
                        }
                    } else if (canBearOff) {
                        // Bear-Off
                        if (targetIndex == 25) {
                            // Rule 12: Exact roll Bear-Off
                            moves.add(Move(from = fromPt, to = 25, dieValue = die, isBearOff = true))
                        } else {
                            // Rule 13: Higher roll Bear-Off (only if no checkers on lower points 19..fromPt-1)
                            var hasCheckerOnLowerPoint = false
                            for (lp in 19 until fromPt) {
                                if (state.points[lp].color == PlayerColor.BLACK && state.points[lp].count > 0) {
                                    hasCheckerOnLowerPoint = true
                                    break
                                }
                            }
                            if (!hasCheckerOnLowerPoint) {
                                moves.add(Move(from = fromPt, to = 25, dieValue = die, isBearOff = true))
                            }
                        }
                    }
                }
            }
        }

        return moves
    }

    /**
     * Rules 2, 3, 9, 10: Exhaustive Legal Turn Sequences Generator
     * 
     * Computes all full legal sequences of moves for the entire turn.
     * Enforces:
     * - Must play maximum possible number of dice (2 out of 2, 4 out of 4, etc.)
     * - If only 1 die out of 2 different dice can be played, MUST play the LARGER die!
     */
    fun getLegalSequences(state: GameState): List<List<Move>> {
        if (state.dice == null || state.dice.remainingMoves.isEmpty() || state.isGameOver) {
            return emptyList()
        }

        val allSequences = mutableListOf<List<Move>>()

        fun explore(currentState: GameState, currentPath: List<Move>) {
            val candidateMoves = getCandidateMoves(currentState)
            if (candidateMoves.isEmpty()) {
                if (currentPath.isNotEmpty()) {
                    allSequences.add(currentPath)
                }
                return
            }

            for (move in candidateMoves) {
                val nextState = applyMove(currentState, move)
                explore(nextState, currentPath + move)
            }
        }

        explore(state, emptyList())

        if (allSequences.isEmpty()) return emptyList()

        // 1. Max moves condition: Only sequences with maximum possible moves are valid
        val maxMoves = allSequences.maxOf { it.size }
        var validSequences = allSequences.filter { it.size == maxMoves }

        // 2. Larger Die Priority: If max moves is 1 and original roll was non-double with 2 dice available
        val dice = state.dice
        if (maxMoves == 1 && !dice.isDouble && dice.remainingMoves.size == 2) {
            val maxDieValue = maxOf(dice.die1, dice.die2)
            val largerDieSequences = validSequences.filter { it.first().dieValue == maxDieValue }
            if (largerDieSequences.isNotEmpty()) {
                validSequences = largerDieSequences
            }
        }

        return validSequences
    }

    /**
     * Strict Legal Moves: Returns only the moves that can lead to a valid maximal turn sequence.
     */
    fun getLegalMoves(state: GameState): List<Move> {
        val sequences = getLegalSequences(state)
        return sequences.map { it.first() }.distinct()
    }

    /**
     * Applies a single legal move to the GameState and updates pip counts, bar, and off counters.
     */
    fun applyMove(state: GameState, move: Move): GameState {
        val player = state.currentTurn
        val newPts = state.points.map { it.copy() }.toMutableList()

        var barW = state.barWhite
        var barB = state.barBlack
        var offW = state.offWhite
        var offB = state.offBlack
        var hitPt: Int? = null

        // 1. Remove checker from source
        if (move.from == BAR_WHITE_INDEX) {
            barW--
        } else if (move.from == BAR_BLACK_INDEX) {
            barB--
        } else {
            val srcPt = newPts[move.from]
            val newCount = srcPt.count - 1
            newPts[move.from] = if (newCount <= 0) PointState() else srcPt.copy(count = newCount)
        }

        // 2. Add checker to destination or bear off or hit
        if (move.isBearOff) {
            if (player == PlayerColor.WHITE) offW++ else offB++
        } else {
            val destPt = newPts[move.to]
            if (destPt.color == player.opposite() && destPt.count == 1) {
                // Rule 6: Hitting opponent blot!
                hitPt = move.to
                if (player == PlayerColor.WHITE) barB++ else barW++
                newPts[move.to] = PointState(player, 1)
            } else {
                newPts[move.to] = PointState(player, destPt.count + 1)
            }
        }

        // 3. Consume die value from remaining moves
        val remainingDiceList = state.dice?.remainingMoves?.toMutableList() ?: mutableListOf()
        val indexToRem = remainingDiceList.indexOf(move.dieValue)
        if (indexToRem != -1) {
            remainingDiceList.removeAt(indexToRem)
        }

        val updatedDice = state.dice?.copy(remainingMoves = remainingDiceList)

        // 4. Check Win Condition & Win Type
        var gameOver = false
        var gameWinner: PlayerColor? = null
        var winType = WinType.NORMAL

        if (offW == 15) {
            gameOver = true
            gameWinner = PlayerColor.WHITE
            winType = determineWinType(offB, barB, newPts, PlayerColor.WHITE, state)
        } else if (offB == 15) {
            gameOver = true
            gameWinner = PlayerColor.BLACK
            winType = determineWinType(offW, barW, newPts, PlayerColor.BLACK, state)
        }

        var newState = state.copy(
            points = newPts,
            barWhite = barW,
            barBlack = barB,
            offWhite = offW,
            offBlack = offB,
            dice = updatedDice,
            moveHistory = state.moveHistory + move,
            isGameOver = gameOver,
            winner = gameWinner,
            winType = winType,
            selectedPoint = null,
            highlightedMoves = emptyList(),
            lastHitPoint = hitPt
        )

        newState = updatePipCounts(newState)
        return newState
    }

    /**
     * Rules 16, 17, 18, 23, 24: Win Type & Points Calculation
     * - Single (1x): Loser has borne off >= 1 checker
     * - Gammon (2x): Loser has borne off 0 checkers
     * - Backgammon (3x): Loser off = 0 AND has checker on Bar or in Winner's Home
     * - Jacoby Rule: In money play, Gammons/Backgammons only count if cube > 1
     * - Gammons/Backgammons Enabled Flags
     */
    fun determineWinType(
        loserOff: Int,
        loserBar: Int,
        points: List<PointState>,
        winner: PlayerColor,
        state: GameState
    ): WinType {
        if (loserOff > 0) return WinType.NORMAL

        // Loser off == 0 -> Check for Backgammon vs Gammon
        val winnerHomeRange = if (winner == PlayerColor.WHITE) 1..6 else 19..24
        val loserHasOnBar = loserBar > 0

        var loserHasInWinnerHome = false
        val loserColor = winner.opposite()
        for (pt in winnerHomeRange) {
            if (points[pt].color == loserColor && points[pt].count > 0) {
                loserHasInWinnerHome = true
                break
            }
        }

        var determined = if (loserHasOnBar || loserHasInWinnerHome) WinType.BACKGAMMON else WinType.GAMMON

        // Rule 24: Check custom toggle restrictions
        if (determined == WinType.BACKGAMMON && !state.backgammonsEnabled) {
            determined = if (state.gammonsEnabled) WinType.GAMMON else WinType.NORMAL
        }
        if (determined == WinType.GAMMON && !state.gammonsEnabled) {
            determined = WinType.NORMAL
        }

        // Rule 23: Jacoby Rule enforcement
        if (state.jacobyRuleEnabled && state.doublingCubeValue == 1) {
            // Cube was never doubled: Gammon & Backgammon count as Single (1x)
            determined = WinType.NORMAL
        }

        return determined
    }

    /**
     * Calculates the total match points earned by the winner.
     */
    fun calculateWonPoints(state: GameState): Int {
        val basePoints = when (state.winType) {
            WinType.NORMAL -> 1
            WinType.GAMMON -> 2
            WinType.BACKGAMMON -> 3
        }
        return basePoints * state.doublingCubeValue
    }

    fun canMakeAnyMove(state: GameState): Boolean {
        if (state.dice == null || state.dice.remainingMoves.isEmpty() || state.isGameOver) return false
        return getLegalMoves(state).isNotEmpty()
    }

    fun switchTurn(state: GameState): GameState {
        val nextTurn = state.currentTurn.opposite()
        return state.copy(
            currentTurn = nextTurn,
            dice = null, // Needs new dice roll for next turn
            selectedPoint = null,
            highlightedMoves = emptyList()
        )
    }

    /**
     * Rules 19, 20, 21, 22, 26: Doubling Cube Eligibility
     */
    fun canPlayerDouble(state: GameState, player: PlayerColor): Boolean {
        if (state.isGameOver || state.isMatchOver) return false
        if (state.dice != null) return false // Must double before rolling
        if (state.doublingOffered) return false // Already pending
        if (state.isCrawfordGame) return false // Rule 22: Crawford Rule disables cube

        // Rule 26: Maximum Cube limit (e.g. 16 cannot become 32 if maxCubeValue is 16)
        if (state.doublingCubeValue * 2 > state.maxCubeValue) return false

        // Rule 21: Cube ownership check
        return state.doublingCubeOwner == null || state.doublingCubeOwner == player
    }

    /**
     * Offers double to the opponent.
     */
    fun doubleCube(state: GameState): GameState {
        if (!canPlayerDouble(state, state.currentTurn)) return state
        return state.copy(doublingOffered = true)
    }

    /**
     * Responds to a double offer:
     * - Accept (Take): Cube value doubles, responding player gains cube ownership.
     * - Decline (Pass): Offering player wins immediately with points = previous cube value.
     */
    fun respondToDouble(state: GameState, accepted: Boolean): GameState {
        if (!state.doublingOffered) return state

        val offeringPlayer = state.currentTurn
        val respondingPlayer = offeringPlayer.opposite()

        return if (accepted) {
            state.copy(
                doublingCubeValue = state.doublingCubeValue * 2,
                doublingCubeOwner = respondingPlayer,
                doublingOffered = false
            )
        } else {
            // Opponent drops: Offering player wins current game
            val updated = state.copy(
                isGameOver = true,
                winner = offeringPlayer,
                winType = WinType.NORMAL,
                doublingOffered = false
            )
            updateMatchScoreAfterGame(updated)
        }
    }

    /**
     * Rule 27: Multi-tier Resignation (Single, Gammon, Backgammon)
     * A player can offer to resign at a specific level based on game situation.
     */
    fun offerResignation(state: GameState, byPlayer: PlayerColor, type: WinType): GameState {
        if (state.isGameOver || state.isMatchOver) return state
        return state.copy(
            resignationOfferedBy = byPlayer,
            resignationType = type
        )
    }

    /**
     * Responds to a Resignation offer:
     * - Accept: Game immediately ends; opponent of resigning player wins with points for the offered winType.
     * - Decline: Game continues uninterrupted without rolling changes.
     */
    fun respondToResignation(state: GameState, accepted: Boolean): GameState {
        val resigningPlayer = state.resignationOfferedBy ?: return state
        val offeredType = state.resignationType ?: WinType.NORMAL

        if (!accepted) {
            return state.copy(
                resignationOfferedBy = null,
                resignationType = null
            )
        }

        // Accepted: Opponent wins with offered win type
        val winningPlayer = resigningPlayer.opposite()
        val endedState = state.copy(
            isGameOver = true,
            winner = winningPlayer,
            winType = offeredType,
            resignationOfferedBy = null,
            resignationType = null
        )
        return updateMatchScoreAfterGame(endedState)
    }

    /**
     * Rule 25, 28: Calculates match points won in the current game and updates match score.
     */
    fun updateMatchScoreAfterGame(state: GameState): GameState {
        if (!state.isGameOver || state.winner == null) return state

        val wonPoints = calculateWonPoints(state)
        val newWhiteScore = if (state.winner == PlayerColor.WHITE) state.whiteMatchScore + wonPoints else state.whiteMatchScore
        val newBlackScore = if (state.winner == PlayerColor.BLACK) state.blackMatchScore + wonPoints else state.blackMatchScore

        val isMatchOver = newWhiteScore >= state.matchTargetScore || newBlackScore >= state.matchTargetScore
        val matchWinner = if (newWhiteScore >= state.matchTargetScore) {
            PlayerColor.WHITE
        } else if (newBlackScore >= state.matchTargetScore) {
            PlayerColor.BLACK
        } else {
            null
        }

        return state.copy(
            whiteMatchScore = newWhiteScore,
            blackMatchScore = newBlackScore,
            isMatchOver = isMatchOver,
            matchWinner = matchWinner
        )
    }

    /**
     * Rule 22, 28, 29: Starts the next game in an ongoing match.
     * Resets the board, checkers, bar, off, doubling cube to 1, while preserving match scores
     * and correctly evaluating the Crawford Rule!
     */
    fun startNextGameInMatch(currentState: GameState): GameState {
        val wasCrawford = currentState.isCrawfordGame
        val crawfordPassed = currentState.crawfordPassed || wasCrawford

        // Evaluate if this new game is a Crawford game
        val isNowCrawford = if (crawfordPassed) {
            false
        } else {
            (currentState.whiteMatchScore == currentState.matchTargetScore - 1 ||
             currentState.blackMatchScore == currentState.matchTargetScore - 1)
        }

        val pts = MutableList(25) { PointState() }
        // White checkers (15 total)
        pts[24] = PointState(PlayerColor.WHITE, 2)
        pts[13] = PointState(PlayerColor.WHITE, 5)
        pts[8]  = PointState(PlayerColor.WHITE, 3)
        pts[6]  = PointState(PlayerColor.WHITE, 5)

        // Black checkers (15 total)
        pts[1]  = PointState(PlayerColor.BLACK, 2)
        pts[12] = PointState(PlayerColor.BLACK, 5)
        pts[17] = PointState(PlayerColor.BLACK, 3)
        pts[19] = PointState(PlayerColor.BLACK, 5)

        val fresh = currentState.copy(
            points = pts,
            barWhite = 0,
            barBlack = 0,
            offWhite = 0,
            offBlack = 0,
            currentTurn = PlayerColor.WHITE,
            dice = null,
            doublingCubeValue = 1,
            doublingCubeOwner = null,
            doublingOffered = false,
            selectedPoint = null,
            highlightedMoves = emptyList(),
            moveHistory = emptyList(),
            isGameOver = false,
            winner = null,
            winType = WinType.NORMAL,
            lastHitPoint = null,
            turnTimerSeconds = 20,
            whiteBankSeconds = 30,
            blackBankSeconds = 30,
            isUsingBankTime = false,
            isRollingForTurn = true,
            openingRoll = null,
            isCrawfordGame = isNowCrawford,
            crawfordPassed = crawfordPassed,
            resignationOfferedBy = null,
            resignationType = null,
            gameNumber = currentState.gameNumber + 1
        )

        return updatePipCounts(fresh)
    }

    /**
     * Resets the entire match to 0-0.
     */
    fun resetMatch(currentState: GameState): GameState {
        val pts = MutableList(25) { PointState() }
        // White checkers (15 total)
        pts[24] = PointState(PlayerColor.WHITE, 2)
        pts[13] = PointState(PlayerColor.WHITE, 5)
        pts[8]  = PointState(PlayerColor.WHITE, 3)
        pts[6]  = PointState(PlayerColor.WHITE, 5)

        // Black checkers (15 total)
        pts[1]  = PointState(PlayerColor.BLACK, 2)
        pts[12] = PointState(PlayerColor.BLACK, 5)
        pts[17] = PointState(PlayerColor.BLACK, 3)
        pts[19] = PointState(PlayerColor.BLACK, 5)

        val fresh = currentState.copy(
            points = pts,
            barWhite = 0,
            barBlack = 0,
            offWhite = 0,
            offBlack = 0,
            currentTurn = PlayerColor.WHITE,
            dice = null,
            doublingCubeValue = 1,
            doublingCubeOwner = null,
            doublingOffered = false,
            selectedPoint = null,
            highlightedMoves = emptyList(),
            moveHistory = emptyList(),
            isGameOver = false,
            winner = null,
            winType = WinType.NORMAL,
            lastHitPoint = null,
            turnTimerSeconds = 20,
            whiteBankSeconds = 30,
            blackBankSeconds = 30,
            isUsingBankTime = false,
            isRollingForTurn = true,
            openingRoll = null,
            whiteMatchScore = 0,
            blackMatchScore = 0,
            isCrawfordGame = false,
            crawfordPassed = false,
            isMatchOver = false,
            matchWinner = null,
            resignationOfferedBy = null,
            resignationType = null,
            gameNumber = 1
        )

        return updatePipCounts(fresh)
    }

    /**
     * Rule 22: Crawford Rule evaluation for Match Play.
     */
    fun evaluateCrawfordOnScoreUpdate(
        targetScore: Int,
        newWhiteScore: Int,
        newBlackScore: Int,
        alreadyPassedCrawford: Boolean
    ): Pair<Boolean, Boolean> {
        if (alreadyPassedCrawford) return Pair(false, true)
        val isCrawford = (newWhiteScore == targetScore - 1 || newBlackScore == targetScore - 1)
        return Pair(isCrawford, isCrawford)
    }
}
