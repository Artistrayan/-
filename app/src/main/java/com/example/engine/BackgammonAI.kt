package com.example.engine

import com.example.model.*

object BackgammonAI {

    fun chooseBestMove(state: GameState): Move? {
        val legalMoves = BackgammonRules.getLegalMoves(state)
        if (legalMoves.isEmpty()) return null

        val mode = state.gameMode
        val aiColor = state.currentTurn

        // Novice: 70% random, 30% heuristic
        if (mode == GameMode.AI_EASY) {
            return if (Math.random() < 0.7) {
                legalMoves.random()
            } else {
                legalMoves.maxByOrNull { move ->
                    val nextState = BackgammonRules.applyMove(state, move)
                    evaluateBoardPosition(nextState, aiColor) + evaluateMoveBonus(move)
                } ?: legalMoves.random()
            }
        }

        // Medium / Hard: Evaluate moves with positional heuristics
        val scoredMoves = legalMoves.map { move ->
            val nextState = BackgammonRules.applyMove(state, move)
            val score = evaluateBoardPosition(nextState, aiColor) + evaluateMoveBonus(move)
            Pair(move, score)
        }

        // Hard: Select absolute best score; Medium: slight variance
        return if (mode == GameMode.AI_HARD) {
            scoredMoves.maxByOrNull { it.second }?.first ?: legalMoves.first()
        } else {
            val top3 = scoredMoves.sortedByDescending { it.second }.take(3)
            top3.random().first
        }
    }

    private fun evaluateMoveBonus(move: Move): Double {
        var bonus = 0.0
        if (move.isHit) bonus += 25.0 // High priority to hit opponent blot
        if (move.isBearOff) bonus += 30.0 // High priority to bear off
        return bonus
    }

    private fun evaluateBoardPosition(state: GameState, aiColor: PlayerColor): Double {
        var score = 0.0

        val (wPip, bPip) = BackgammonRules.calculatePipCount(state.points, state.barWhite, state.barBlack)
        val myPip = if (aiColor == PlayerColor.WHITE) wPip else bPip
        val oppPip = if (aiColor == PlayerColor.WHITE) bPip else wPip

        // 1. Pip count difference (lower myPip is better)
        score += (oppPip - myPip) * 1.5

        // 2. Bar penalty for opponent / reward for hitting
        val oppBar = if (aiColor == PlayerColor.WHITE) state.barBlack else state.barWhite
        val myBar = if (aiColor == PlayerColor.WHITE) state.barWhite else state.barBlack

        score += oppBar * 35.0
        score -= myBar * 45.0

        // 3. Off count reward
        val myOff = if (aiColor == PlayerColor.WHITE) state.offWhite else state.offBlack
        val oppOff = if (aiColor == PlayerColor.WHITE) state.offBlack else state.offWhite
        score += myOff * 40.0
        score -= oppOff * 40.0

        // 4. Made points (2+ checkers) vs Exposed Blots (1 checker)
        var myBlots = 0
        var myMadePoints = 0

        for (i in 1..24) {
            val pt = state.points[i]
            if (pt.color == aiColor) {
                if (pt.count == 1) myBlots++
                if (pt.count >= 2) myMadePoints++
            }
        }

        score -= myBlots * 12.0
        score += myMadePoints * 8.0

        return score
    }
}
