package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.config.CustomGameThemeConfig
import com.example.engine.BackgammonRules
import com.example.model.BoardThemes
import com.example.model.GameState
import com.example.model.PlayerColor
import com.example.model.WinType
import com.example.ui.components.*
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    state: GameState,
    equippedBoardId: Int,
    commentary: String,
    chatMessages: List<Pair<String, String>>,
    canUndo: Boolean,
    onRollClick: () -> Unit,
    onPointClick: (Int) -> Unit,
    onBarClick: (PlayerColor) -> Unit,
    onDoubleOffer: () -> Unit,
    onUndoClick: () -> Unit,
    onConfirmTurn: () -> Unit,
    onSendChat: (String) -> Unit,
    onOfferResign: (WinType) -> Unit = {},
    onRespondResign: (Boolean) -> Unit = {},
    onRespondDouble: (Boolean) -> Unit = {},
    onStartNextGame: () -> Unit = {},
    onResetMatch: () -> Unit = {},
    onExitGame: () -> Unit
) {
    val theme = BoardThemes.getThemeById(equippedBoardId)
    val view = LocalView.current
    var showChatDialog by remember { mutableStateOf(false) }
    var showResignDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    var autoRoll by remember { mutableStateOf(false) }
    var autoBearOff by remember { mutableStateOf(false) }

    val isMyTurn = (state.currentTurn == PlayerColor.WHITE)
    val canMakeMove = BackgammonRules.canMakeAnyMove(state)
    val allMovesFinished = isMyTurn && state.dice != null && !canMakeMove

    // Auto-roll logic
    LaunchedEffect(state.currentTurn, state.dice, state.isRollingForTurn, state.openingRoll, autoRoll) {
        if (autoRoll && (isMyTurn || state.isRollingForTurn) && state.dice == null && state.openingRoll == null) {
            delay(600)
            onRollClick()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080C))
    ) {
        val isPortrait = maxHeight > maxWidth

        // 1. Cinematic VIP Background Art
        Image(
            painter = painterResource(id = CustomGameThemeConfig.gameBackgroundDrawable),
            contentDescription = "Casino VIP Lounge",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Dark Atmospheric Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0B0E14).copy(alpha = (CustomGameThemeConfig.backgroundVignetteDarkness * 0.75f).coerceIn(0f, 1f)),
                            Color(0xFF040608).copy(alpha = CustomGameThemeConfig.backgroundVignetteDarkness.coerceIn(0f, 1f))
                        )
                    )
                )
        )

        // 3. Adaptive Screen Layout (Portrait vs Landscape)
        if (isPortrait) {
            PortraitGameLayout(
                state = state,
                theme = theme,
                isMyTurn = isMyTurn,
                allMovesFinished = allMovesFinished,
                canUndo = canUndo,
                autoRoll = autoRoll,
                autoBearOff = autoBearOff,
                commentary = commentary,
                onRollClick = onRollClick,
                onPointClick = onPointClick,
                onBarClick = onBarClick,
                onDoubleOffer = onDoubleOffer,
                onUndoClick = onUndoClick,
                onConfirmTurn = onConfirmTurn,
                onToggleAutoRoll = { autoRoll = it },
                onToggleAutoBearOff = { autoBearOff = it },
                onOpenChat = { showChatDialog = true },
                onOpenResign = { showResignDialog = true },
                onOpenHelp = { showHelpDialog = true }
            )
        } else {
            LandscapeGameLayout(
                state = state,
                theme = theme,
                isMyTurn = isMyTurn,
                allMovesFinished = allMovesFinished,
                canUndo = canUndo,
                autoRoll = autoRoll,
                autoBearOff = autoBearOff,
                commentary = commentary,
                onRollClick = onRollClick,
                onPointClick = onPointClick,
                onBarClick = onBarClick,
                onDoubleOffer = onDoubleOffer,
                onUndoClick = onUndoClick,
                onConfirmTurn = onConfirmTurn,
                onToggleAutoRoll = { autoRoll = it },
                onToggleAutoBearOff = { autoBearOff = it },
                onOpenChat = { showChatDialog = true },
                onOpenResign = { showResignDialog = true },
                onOpenHelp = { showHelpDialog = true },
                onExitGame = onExitGame
            )
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = Color(0xFF141722),
            shape = RoundedCornerShape(16.dp),
            title = { Text("راهنمای بازی تخته‌نرد", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• برای پرتاب تاس روی دکمه «رول تاس» کلیک کنید.", color = Color.White, fontSize = 12.sp)
                    Text("• مهره‌ها با کلیک روی ستون مبدأ و ستون مقصد جابه‌جا می‌شوند.", color = Color.White, fontSize = 12.sp)
                    Text("• با دکمه «تایید حرکت» نوبت خود را ثبت کنید.", color = Color.White, fontSize = 12.sp)
                    Text("• در صورت نیاز از مکعب دوبل (2X) برای افزایش جایزه استفاده کنید.", color = Color.White, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))) {
                    Text("متوجه شدم", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 1. Multi-tier Resignation Dialog (Rule 27)
    if (showResignDialog) {
        var selectedResignType by remember { mutableStateOf(WinType.NORMAL) }
        val wonPtsNormal = 1 * state.doublingCubeValue
        val wonPtsGammon = 2 * state.doublingCubeValue
        val wonPtsBackgammon = 3 * state.doublingCubeValue

        AlertDialog(
            onDismissRequest = { showResignDialog = false },
            containerColor = Color(0xFF141722),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "پیشنهاد تسلیم (Resignation)",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "نوع تسلیم پیشنهادی خود را انتخاب کنید. حریف می‌تواند این پیشنهاد را قبول یا رد کند:",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    // Option 1: Single
                    ResignOptionCard(
                        title = "تسلیم معمولی (Single)",
                        subtitle = "۱ × کیوب = $wonPtsNormal امتیاز برای حریف",
                        isSelected = selectedResignType == WinType.NORMAL,
                        onClick = { selectedResignType = WinType.NORMAL }
                    )

                    // Option 2: Gammon
                    ResignOptionCard(
                        title = "تسلیم مارس (Gammon)",
                        subtitle = "۲ × کیوب = $wonPtsGammon امتیاز برای حریف",
                        isSelected = selectedResignType == WinType.GAMMON,
                        onClick = { selectedResignType = WinType.GAMMON }
                    )

                    // Option 3: Backgammon
                    ResignOptionCard(
                        title = "تسلیم مارس دوبل (Backgammon)",
                        subtitle = "۳ × کیوب = $wonPtsBackgammon امتیاز برای حریف",
                        isSelected = selectedResignType == WinType.BACKGAMMON,
                        onClick = { selectedResignType = WinType.BACKGAMMON }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResignDialog = false
                        onOfferResign(selectedResignType)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D))
                ) {
                    Text("ارسال پیشنهاد تسلیم", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResignDialog = false }) {
                    Text("انصراف و ادامه بازی", color = Color.Gray)
                }
            }
        )
    }

    // 2. Incoming Resignation Dialog from Opponent
    if (state.resignationOfferedBy != null && state.resignationOfferedBy != PlayerColor.WHITE && !state.isGameOver) {
        val type = state.resignationType ?: WinType.NORMAL
        val pts = when (type) {
            WinType.NORMAL -> 1
            WinType.GAMMON -> 2
            WinType.BACKGAMMON -> 3
        } * state.doublingCubeValue
        val typeName = when (type) {
            WinType.NORMAL -> "تسلیم معمولی (Single)"
            WinType.GAMMON -> "تسلیم مارس (Gammon)"
            WinType.BACKGAMMON -> "تسلیم مارس دوبل (Backgammon)"
        }

        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF141722),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("درخواست تسلیم از سوی حریف!", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Text(
                    "حریف درخواست $typeName به ارزش $pts امتیاز مسابقه داده است. در صورت پذیرش، شما برنده این دست خواهید شد.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { onRespondResign(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("قبول تسلیم (برد دست)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onRespondResign(false) }) {
                    Text("رد و ادامه بازی", color = Color.LightGray)
                }
            }
        )
    }

    // 3. Incoming Doubling Offer Dialog from Opponent
    if (state.doublingOffered && state.currentTurn == PlayerColor.BLACK && !state.isGameOver) {
        val newCube = state.doublingCubeValue * 2
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF141722),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("🎲 پیشنهاد دوبل از سوی حریف!", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Text(
                    "حریف پیشنهاد داد ارزش بازی به ${newCube}X افزایش یابد. در صورت انصراف (Pass)، حریف برنده دست فعلی با ارزش ${state.doublingCubeValue}X خواهد بود.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { onRespondDouble(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("قبول دوبل (Take)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { onRespondDouble(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D))
                ) {
                    Text("واگذاری دست (Pass)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 4. Game Over & Match Over Dialog (Rules 25, 28, 29)
    if (state.isGameOver) {
        val isWin = (state.winner == PlayerColor.WHITE)
        val wonPts = BackgammonRules.calculateWonPoints(state)
        val wonCoins = if (isWin) (state.matchBet * wonPts) else state.matchBet

        val winTypeName = when (state.winType) {
            WinType.NORMAL -> "برد معمولی (Single)"
            WinType.GAMMON -> "برد مارس (Gammon)"
            WinType.BACKGAMMON -> "برد مارس دوبل (Backgammon)"
        }

        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF0F131C),
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = if (state.isMatchOver) {
                        if (state.matchWinner == PlayerColor.WHITE) "🏆 قهرمان مسابقه شدید!" else "💔 مسابقه به پایان رسید"
                    } else {
                        if (isWin) "🏆 پیروزی در این دست!" else "💔 شکست در این دست"
                    },
                    color = if (isWin) Color(0xFFFFD700) else Color(0xFFFF5252),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "$winTypeName • کیوب: ${state.doublingCubeValue}X",
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "امتیاز کسب شده: $wonPts امتیاز مسابقه",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    // Match Score Banner
                    Surface(
                        color = Color(0xFF181F2E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.whitePlayerName, color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${state.whiteMatchScore}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                            Text("هدف: ${state.matchTargetScore}", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.blackPlayerName, color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${state.blackMatchScore}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    if (isWin) {
                        Text("جایزه سکه: 🪙 +$wonCoins سکه", color = Color(0xFFFFD700), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                if (state.isMatchOver) {
                    Button(
                        onClick = onResetMatch,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("شروع مجدد مسابقه (0 - 0)", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Button(
                        onClick = onStartNextGame,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("دست بعدی (بازی ${state.gameNumber + 1})", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onExitGame, modifier = Modifier.fillMaxWidth()) {
                    Text("بازگشت به منوی اصلی", color = Color.Gray, fontSize = 12.sp)
                }
            }
        )
    }

    if (showChatDialog) {
        MatchChatDialog(onDismiss = { showChatDialog = false }, onSendMessage = onSendChat)
    }
}

@Composable
private fun ResignOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFF2C1920) else Color(0xFF181C26),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) Color(0xFFFF4D4D) else Color(0xFF2A3347)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFFFFB4AB), fontSize = 11.sp)
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF4D4D))
            )
        }
    }
}

// =========================================================================
// PORTRAIT LAYOUT
// =========================================================================
@Composable
private fun PortraitGameLayout(
    state: GameState,
    theme: com.example.model.BoardTheme,
    isMyTurn: Boolean,
    allMovesFinished: Boolean,
    canUndo: Boolean,
    autoRoll: Boolean,
    autoBearOff: Boolean,
    commentary: String,
    onRollClick: () -> Unit,
    onPointClick: (Int) -> Unit,
    onBarClick: (PlayerColor) -> Unit,
    onDoubleOffer: () -> Unit,
    onUndoClick: () -> Unit,
    onConfirmTurn: () -> Unit,
    onToggleAutoRoll: (Boolean) -> Unit,
    onToggleAutoBearOff: (Boolean) -> Unit,
    onOpenChat: () -> Unit,
    onOpenResign: () -> Unit,
    onOpenHelp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF10131D).copy(alpha = 0.90f))
                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.40f), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F180B))
                        .border(1.5.dp, Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 20.sp)
                }
                Column {
                    Text(state.blackPlayerName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("PIP: ${state.blackPipCount}", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DoublingCubeBadge(value = state.doublingCubeValue)
                IconButton(onClick = onOpenChat, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onOpenResign, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Flag, contentDescription = "Resign", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                }
            }
        }

        // CENTER BOARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            BoardCanvas(
                state = state,
                theme = theme,
                onPointClick = onPointClick,
                onBarClick = onBarClick,
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .aspectRatio(1.45f)
            )

            if (state.dice != null || state.openingRoll != null) {
                BoardDiceOverlay(
                    diceRoll = state.dice,
                    openingRoll = state.openingRoll,
                    isMyTurn = isMyTurn,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-6).dp)
            ) {
                FloatingTurnBanner(
                    isMyTurn = isMyTurn,
                    timerSeconds = state.turnTimerSeconds,
                    isUsingBank = state.isUsingBankTime
                )
            }
        }

        // BOTTOM CONTROLS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F131D).copy(alpha = 0.95f))
                .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A192F))
                            .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 18.sp)
                    }
                    Column {
                        Text(state.whitePlayerName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("PIP: ${state.whitePipCount}", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = Color(0xFF1A1F2C),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "🪙 ${state.matchBet * 2 * state.doublingCubeValue}",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (state.dice == null && (isMyTurn || state.isRollingForTurn)) {
                GlowingRollDiceButton(
                    isRollingForTurn = state.isRollingForTurn,
                    onClick = onRollClick
                )
            } else if (isMyTurn && state.dice != null && !state.isRollingForTurn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onUndoClick,
                        enabled = canUndo,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2338)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("برگشت", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (canUndo) Color.White else Color.Gray)
                    }

                    Button(
                        onClick = onConfirmTurn,
                        modifier = Modifier.weight(1.5f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (allMovesFinished) Color(0xFF00E676) else Color(0xFF00B0FF)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("تایید حرکت", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }

                    if (!state.doublingOffered) {
                        Button(
                            onClick = onDoubleOffer,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("دوبل 2X", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// LANDSCAPE LAYOUT (Exact Match to User Reference Image 3)
// =========================================================================
@Composable
private fun LandscapeGameLayout(
    state: GameState,
    theme: com.example.model.BoardTheme,
    isMyTurn: Boolean,
    allMovesFinished: Boolean,
    canUndo: Boolean,
    autoRoll: Boolean,
    autoBearOff: Boolean,
    commentary: String,
    onRollClick: () -> Unit,
    onPointClick: (Int) -> Unit,
    onBarClick: (PlayerColor) -> Unit,
    onDoubleOffer: () -> Unit,
    onUndoClick: () -> Unit,
    onConfirmTurn: () -> Unit,
    onToggleAutoRoll: (Boolean) -> Unit,
    onToggleAutoBearOff: (Boolean) -> Unit,
    onOpenChat: () -> Unit,
    onOpenResign: () -> Unit,
    onOpenHelp: () -> Unit,
    onExitGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HEADER (Matching Image 3: Left Player, Center Ornate Logo, Right Player)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player 1 (Left)
            PlayerHudPill(
                name = state.whitePlayerName,
                score = state.whiteMatchScore,
                rating = "1520",
                avatar = "👑",
                color = Color(0xFF00E5FF)
            )

            // Center Ornate Logo & Tournament Match Status
            val canDouble = BackgammonRules.canPlayerDouble(state, state.currentTurn)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1C170C).copy(alpha = 0.95f), Color(0xFF0D0F17).copy(alpha = 0.95f))
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFD700), Color(0xFF00E5FF), Color(0xFFFFD700))
                            ),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("👑", fontSize = 13.sp)
                            Text(
                                text = "تخته نرد",
                                color = Color(0xFFFFD700),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black, blurRadius = 8f)
                                )
                            )
                            Text("🎲", fontSize = 13.sp)
                        }
                        Text(
                            text = if (state.isCrawfordGame) "قانون کرافورد (کوب غیرفعال) • تا ${state.matchTargetScore}" else "دست ${state.gameNumber} • مسابقه تا ${state.matchTargetScore}",
                            color = if (state.isCrawfordGame) Color(0xFFFF4081) else Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Doubling Cube Indicator Pill
                Surface(
                    color = if (canDouble) Color(0xFF261D0A) else Color(0xFF141722),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        if (canDouble) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.clickable(enabled = canDouble, onClick = onDoubleOffer)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🎲", fontSize = 14.sp)
                        Text(
                            text = "${state.doublingCubeValue}X",
                            color = if (canDouble) Color(0xFFFFD700) else Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (canDouble) {
                            Text("دوبل", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Player 2 (Right)
            PlayerHudPill(
                name = state.blackPlayerName,
                score = state.blackMatchScore,
                rating = "1487",
                avatar = "🐉",
                color = Color(0xFFFF5252)
            )
        }

        // CENTER: 3D Backgammon Board & Turn Banner (Maximized size)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            BoardCanvas(
                state = state,
                theme = theme,
                onPointClick = onPointClick,
                onBarClick = onBarClick,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1.58f, matchHeightConstraintsFirst = true)
            )

            if (state.dice != null || state.openingRoll != null) {
                BoardDiceOverlay(
                    diceRoll = state.dice,
                    openingRoll = state.openingRoll,
                    isMyTurn = isMyTurn,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Floating Glowing Turn Banner («نوبت شماست»)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp)
            ) {
                FloatingTurnBanner(
                    isMyTurn = isMyTurn,
                    timerSeconds = state.turnTimerSeconds,
                    isUsingBank = state.isUsingBankTime
                )
            }
        }

        // BOTTOM ACTION BAR (Menü, Undo, Double, Roll, Help, Resign)
        val canDouble = BackgammonRules.canPlayerDouble(state, state.currentTurn)
        GameBottomActionBar(
            isMyTurn = isMyTurn,
            dice = state.dice,
            isRollingForTurn = state.isRollingForTurn,
            canUndo = canUndo,
            canDouble = canDouble,
            cubeMultiplier = state.doublingCubeValue,
            onRollClick = onRollClick,
            onUndoClick = onUndoClick,
            onDoubleOffer = onDoubleOffer,
            onOpenMenu = onExitGame,
            onOpenHelp = onOpenHelp,
            onOpenResign = onOpenResign
        )
    }
}

@Composable
private fun PlayerHudPill(
    name: String,
    score: Int,
    rating: String,
    avatar: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF10131D).copy(alpha = 0.90f))
            .border(1.2.dp, color.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(avatar, fontSize = 14.sp)
        }
        Column(verticalArrangement = Arrangement.Center) {
            Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("امتیاز: $score", color = color, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun GameBottomActionBar(
    isMyTurn: Boolean,
    dice: com.example.model.DiceRoll?,
    isRollingForTurn: Boolean,
    canUndo: Boolean,
    canDouble: Boolean = false,
    cubeMultiplier: Int = 1,
    onRollClick: () -> Unit,
    onUndoClick: () -> Unit,
    onDoubleOffer: () -> Unit = {},
    onOpenMenu: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenResign: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF141926).copy(alpha = 0.95f), Color(0xFF0D101A).copy(alpha = 0.95f))
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFD700).copy(alpha = 0.6f), Color(0xFF00E5FF).copy(alpha = 0.6f))
                ),
                RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Menu
        GameActionButton(icon = "☰", label = "منو", onClick = onOpenMenu)
        // 2. Undo
        GameActionButton(icon = "↩", label = "بازگشت", enabled = canUndo, onClick = onUndoClick)
        // 3. Doubling Cube
        GameActionButton(
            icon = "🎲",
            label = "دوبل (${cubeMultiplier * 2}X)",
            enabled = canDouble,
            color = Color(0xFFFFD700),
            onClick = onDoubleOffer
        )
        // 4. Roll Dice (Central Glowing Button)
        GlowingRollDiceButtonCompact(isRolling = isRollingForTurn, onClick = onRollClick)
        // 5. Help
        GameActionButton(icon = "💡", label = "کمک", onClick = onOpenHelp)
        // 6. Resign
        GameActionButton(icon = "🏳", label = "تسلیم", color = Color(0xFFFF5252), onClick = onOpenResign)
    }
}

@Composable
private fun GameActionButton(
    icon: String,
    label: String,
    enabled: Boolean = true,
    color: Color = Color(0xFFFFD700),
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 18.sp, color = if (enabled) color else Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color.Gray)
    }
}

@Composable
private fun GlowingRollDiceButtonCompact(
    isRolling: Boolean,
    onClick: () -> Unit
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse_compact_roll")
    val scale by pulseAnim.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .height(44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFD700), Color(0xFFFF9100))
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("🎲", fontSize = 16.sp)
            Text(
                text = "رول تاس",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

// =========================================================================
// FLOATING GLOWING TURN BANNER (MATCHING USER REFERENCE IMAGE)
// =========================================================================
@Composable
private fun FloatingTurnBanner(
    isMyTurn: Boolean,
    timerSeconds: Int,
    isUsingBank: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "turn_glow")
    val bannerGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val borderColor = if (isMyTurn) Color(0xFF00E5FF) else Color(0xFFFFD700)
    val bgColors = if (isMyTurn) {
        listOf(Color(0xFF0A2239), Color(0xFF051322))
    } else {
        listOf(Color(0xFF2E1C0A), Color(0xFF170E04))
    }

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = borderColor.copy(alpha = bannerGlowAlpha)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(bgColors))
            .border(1.6.dp, borderColor.copy(alpha = bannerGlowAlpha), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(if (isMyTurn) "🐉" else "🤖", fontSize = 15.sp)
            Text(
                text = if (isMyTurn) "نوبت شماست • YOUR TURN" else "نوبت حریف • OPPONENT'S TURN",
                color = if (isMyTurn) Color(0xFF80D8FF) else Color(0xFFFFE082),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Surface(
                color = if (isUsingBank) Color(0xFFFF5252) else borderColor,
                shape = CircleShape
            ) {
                Text(
                    text = "${timerSeconds}s",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun GlowingRollDiceButton(
    isRollingForTurn: Boolean,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val pulseAnim = rememberInfiniteTransition(label = "pulse_roll")
    val rollScale by pulseAnim.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(rollScale)
            .shadow(12.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFFFFD700))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFD700), Color(0xFFFF9100), Color(0xFFFFD700))
                )
            )
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("🎲", fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isRollingForTurn) "پرتاب تاس شروع بازی" else "پرتاب تاس (ROLL DICE)",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun DoublingCubeBadge(value: Int) {
    Surface(
        color = Color(0xFF0D47A1),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00E5FF))
    ) {
        Text(
            text = "64 [${value}X]",
            color = Color(0xFF80D8FF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
