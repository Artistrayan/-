package com.example.model

enum class PlayerColor {
    WHITE, BLACK;

    fun opposite(): PlayerColor = if (this == WHITE) BLACK else WHITE
}

enum class GameMode {
    AI_EASY,
    AI_MEDIUM,
    AI_HARD,
    ONLINE_QUICK,
    PRIVATE_ROOM,
    PASS_AND_PLAY,
    TOURNAMENT
}

enum class WinType {
    NORMAL, // 1x points
    GAMMON, // 2x points (opponent didn't bear off any checker)
    BACKGAMMON // 3x points (opponent didn't bear off any checker AND has a checker on bar/in home)
}

data class PointState(
    val color: PlayerColor? = null,
    val count: Int = 0
)

data class Move(
    val from: Int, // 1..24, or 0 for BAR_WHITE (25 for BAR_BLACK)
    val to: Int,   // 1..24, or 0 for OFF_WHITE (25 for OFF_BLACK)
    val dieValue: Int,
    val isHit: Boolean = false,
    val isBearOff: Boolean = false
)

data class DiceRoll(
    val die1: Int,
    val die2: Int,
    val isDouble: Boolean = die1 == die2,
    val remainingMoves: List<Int> = if (die1 == die2) listOf(die1, die1, die1, die1) else listOf(die1, die2)
)

data class GameState(
    val points: List<PointState> = List(25) { PointState() }, // 1..24 used
    val barWhite: Int = 0,
    val barBlack: Int = 0,
    val offWhite: Int = 0,
    val offBlack: Int = 0,
    val currentTurn: PlayerColor = PlayerColor.WHITE,
    val dice: DiceRoll? = null,
    val doublingCubeValue: Int = 1,
    val doublingCubeOwner: PlayerColor? = null, // null means unowned
    val doublingOffered: Boolean = false,
    val selectedPoint: Int? = null, // Selected point index (0..25, 0 = White Bar, 25 = Black Bar)
    val highlightedMoves: List<Move> = emptyList(),
    val moveHistory: List<Move> = emptyList(),
    val isGameOver: Boolean = false,
    val winner: PlayerColor? = null,
    val winType: WinType = WinType.NORMAL,
    val matchBet: Long = 100,
    val whitePlayerName: String = "You",
    val blackPlayerName: String = "Opponent",
    val whiteAvatarRes: Int = 1,
    val blackAvatarRes: Int = 2,
    val gameMode: GameMode = GameMode.AI_MEDIUM,
    val whitePipCount: Int = 167,
    val blackPipCount: Int = 167,
    val lastHitPoint: Int? = null,
    val turnTimerSeconds: Int = 20,
    val whiteBankSeconds: Int = 30,
    val blackBankSeconds: Int = 30,
    val isUsingBankTime: Boolean = false,
    val isRollingForTurn: Boolean = false,
    val openingRoll: Pair<Int, Int>? = null // (White die, Black die) for turn decider
)

data class BoardTheme(
    val id: Int,
    val name: String,
    val unlockLevel: Int,
    val description: String,
    val boardBackgroundColors: List<Long>, // ARGB Longs for Gradient
    val woodBorderColor: Long,
    val pointColorLight: Long,
    val pointColorDark: Long,
    val checkerWhiteColors: List<Long>,
    val checkerBlackColors: List<Long>,
    val accentGlowColor: Long,
    val particleColor: Long,
    val iconEmoji: String = "🎲"
)
