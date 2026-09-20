package com.example.engine

import com.example.model.*

object BackgammonRules {

    const val BAR_WHITE_INDEX = 25
    const val BAR_BLACK_INDEX = 0

    fun createInitialState(
        bet: Long = 100,
        whiteName: String = "You",
        blackName: String = "Opponent",
        mode: GameMode = GameMode.AI_MEDIUM
    ): GameState {
        val pts = MutableList(25) { PointState() }

        // Standard Backgammon Starting Position:
        // White moves 24 -> 1 (Home: 1..6)
        // Black moves 1 -> 24 (Home: 19..24)

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
            gameMode = mode
        )

        return updatePipCounts(state)
    }

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

    fun rollDice(): DiceRoll {
        val d1 = (1..6).random()
        val d2 = (1..6).random()
        return DiceRoll(d1, d2)
    }

    fun canPlayerBearOff(state: GameState, player: PlayerColor): Boolean {
        if (player == PlayerColor.WHITE) {
            if (state.barWhite > 0) return false
            // Check if any White checker is outside points 1..6
            for (i in 7..24) {
                if (state.points[i].color == PlayerColor.WHITE && state.points[i].count > 0) {
                    return false
                }
            }
            return true
        } else {
            if (state.barBlack > 0) return false
            // Check if any Black checker is outside points 19..24
            for (i in 1..18) {
                if (state.points[i].color == PlayerColor.BLACK && state.points[i].count > 0) {
                    return false
                }
            }
            return true
        }
    }

    fun getLegalMoves(state: GameState): List<Move> {
        if (state.dice == null || state.dice.remainingMoves.isEmpty() || state.isGameOver) {
            return emptyList()
        }

        val player = state.currentTurn
        val remainingDice = state.dice.remainingMoves.distinct()
        val moves = mutableListOf<Move>()

        val hasBarCheckers = if (player == PlayerColor.WHITE) state.barWhite > 0 else state.barBlack > 0

        if (hasBarCheckers) {
            // Must re-enter from Bar first
            val fromIndex = if (player == PlayerColor.WHITE) BAR_WHITE_INDEX else BAR_BLACK_INDEX
            for (die in remainingDice) {
                val targetIndex = if (player == PlayerColor.WHITE) BAR_WHITE_INDEX - die else BAR_BLACK_INDEX + die
                if (isValidTarget(state, player, targetIndex, isBearOffAllowed = false)) {
                    val isHit = isOpponentBlot(state, player, targetIndex)
                    moves.add(Move(from = fromIndex, to = targetIndex, dieValue = die, isHit = isHit))
                }
            }
            return moves
        }

        // Standard point moves
        val canBearOff = canPlayerBearOff(state, player)

        for (fromPt in 1..24) {
            val pt = state.points[fromPt]
            if (pt.color != player || pt.count <= 0) continue

            for (die in remainingDice) {
                val targetIndex = if (player == PlayerColor.WHITE) fromPt - die else fromPt + die

                if (player == PlayerColor.WHITE) {
                    if (targetIndex > 0) {
                        // Standard move
                        if (isValidTarget(state, player, targetIndex, canBearOff)) {
                            val isHit = isOpponentBlot(state, player, targetIndex)
                            moves.add(Move(from = fromPt, to = targetIndex, dieValue = die, isHit = isHit))
                        }
                    } else if (canBearOff) {
                        // Bear off
                        if (targetIndex == 0) {
                            moves.add(Move(from = fromPt, to = 0, dieValue = die, isBearOff = true))
                        } else {
                            // Higher die roll than point position. Allowed ONLY IF no checkers exist on higher points (fromPt+1..6)
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
                        // Standard move
                        if (isValidTarget(state, player, targetIndex, canBearOff)) {
                            val isHit = isOpponentBlot(state, player, targetIndex)
                            moves.add(Move(from = fromPt, to = targetIndex, dieValue = die, isHit = isHit))
                        }
                    } else if (canBearOff) {
                        // Bear off
                        if (targetIndex == 25) {
                            moves.add(Move(from = fromPt, to = 25, dieValue = die, isBearOff = true))
                        } else {
                            // Higher die roll than point position. Allowed ONLY IF no checkers exist on lower points (19..fromPt-1)
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

    private fun isValidTarget(
        state: GameState,
        player: PlayerColor,
        targetIndex: Int,
        isBearOffAllowed: Boolean
    ): Boolean {
        if (targetIndex < 1 || targetIndex > 24) return false
        val pt = state.points[targetIndex]
        if (pt.count == 0 || pt.color == player) return true
        // Opponent occupies target
        return pt.count == 1 // Only 1 opponent checker (blot) can be hit
    }

    private fun isOpponentBlot(state: GameState, player: PlayerColor, targetIndex: Int): Boolean {
        if (targetIndex < 1 || targetIndex > 24) return false
        val pt = state.points[targetIndex]
        return pt.color == player.opposite() && pt.count == 1
    }

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
                // Hitting opponent blot!
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

        // 4. Check Win Condition
        var gameOver = false
        var gameWinner: PlayerColor? = null
        var winType = WinType.NORMAL

        if (offW == 15) {
            gameOver = true
            gameWinner = PlayerColor.WHITE
            winType = determineWinType(offB, barB, newPts, PlayerColor.WHITE)
        } else if (offB == 15) {
            gameOver = true
            gameWinner = PlayerColor.BLACK
            winType = determineWinType(offW, barW, newPts, PlayerColor.BLACK)
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

    fun canMakeAnyMove(state: GameState): Boolean {
        if (state.dice == null || state.dice.remainingMoves.isEmpty() || state.isGameOver) return false
        return getLegalMoves(state).isNotEmpty()
    }

    private fun determineWinType(
        loserOff: Int,
        loserBar: Int,
        points: List<PointState>,
        winner: PlayerColor
    ): WinType {
        if (loserOff > 0) return WinType.NORMAL

        // Loser hasn't borne off any checkers -> At least GAMMON!
        // Check for BACKGAMMON: Loser has checker on bar OR in Winner's Home Board
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

        return if (loserHasOnBar || loserHasInWinnerHome) WinType.BACKGAMMON else WinType.GAMMON
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

    fun doubleCube(state: GameState): GameState {
        if (state.doublingOffered || state.isGameOver) return state
        return state.copy(doublingOffered = true)
    }

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
            // Forfeit match
            state.copy(
                isGameOver = true,
                winner = offeringPlayer,
                winType = WinType.NORMAL,
                doublingOffered = false
            )
        }
    }
}
