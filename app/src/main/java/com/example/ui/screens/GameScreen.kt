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
import com.example.engine.BackgammonRules
import com.example.model.BoardThemes
import com.example.model.GameState
import com.example.model.PlayerColor
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
    onExitGame: () -> Unit
) {
    val theme = BoardThemes.getThemeById(equippedBoardId)
    val view = LocalView.current
    var showChatDialog by remember { mutableStateOf(false) }
    var showResignDialog by remember { mutableStateOf(false) }

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

        // 1. Cinematic VIP Dragon Lounge Background Art
        Image(
            painter = painterResource(id = R.drawable.img_dragon_vip_lounge_1790546607385),
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
                            Color(0xFF0B0E14).copy(alpha = 0.70f),
                            Color(0xFF040608).copy(alpha = 0.94f)
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
                onOpenResign = { showResignDialog = true }
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
                onOpenResign = { showResignDialog = true }
            )
        }
    }

    // Dialogs...
    if (showResignDialog) {
        AlertDialog(
            onDismissRequest = { showResignDialog = false },
            containerColor = Color(0xFF141722),
            shape = RoundedCornerShape(16.dp),
            title = { Text("تسلیم شدن از بازی؟", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = { Text("در صورت تسلیم، سکه‌های شرط (${state.matchBet}) کسر خواهند شد.", color = Color.LightGray, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = { showResignDialog = false; onExitGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D))
                ) {
                    Text("تسلیم", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResignDialog = false }) { Text("ادامه بازی", color = Color.Gray) }
            }
        )
    }

    if (state.isGameOver) {
        val isWin = (state.winner == PlayerColor.WHITE)
        val wonCoins = if (isWin) (state.matchBet * 1.95).toLong() else 0L

        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF0F131C),
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = if (isWin) "🏆 پیروزی شکوهمند!" else "💔 شکست در مسابقه",
                    color = if (isWin) Color(0xFFFFD700) else Color(0xFFFF5252),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (isWin) {
                        Text("جایزه شما: 🪙 +$wonCoins سکه", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("سکه‌های کسر شده: -${state.matchBet}", color = Color(0xFFFF8A80), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onExitGame,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("بازگشت به لابی", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            }
        )
    }

    if (showChatDialog) {
        MatchChatDialog(onDismiss = { showChatDialog = false }, onSendMessage = onSendChat)
    }
}

// =========================================================================
// PORTRAIT LAYOUT (Vertical Mobile Screen - Matching User's Design Image)
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
    onOpenResign: () -> Unit
) {
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP: Opponent HUD (Player 2 - Black / Imperial Gold)
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

            // Stake & Doubling Cube Badge
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

        // CENTER: 3D Backgammon Board & Glowing Turn Banner
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
                    .fillMaxSize()
                    .aspectRatio(1.36f, matchHeightConstraintsFirst = false)
            )

            // Dynamic 3D Dice Tumble Overlay
            if (state.dice != null || state.openingRoll != null) {
                BoardDiceOverlay(
                    diceRoll = state.dice,
                    openingRoll = state.openingRoll,
                    isMyTurn = isMyTurn,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Floating Glowing Turn Banner (Matching user reference image!)
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

            // AI Commentary if available
            if (commentary.isNotBlank()) {
                Surface(
                    color = Color(0xFF0C101A).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "💡 $commentary",
                        color = Color(0xFF80D8FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // BOTTOM: Player 1 HUD & Cyber-Luxury Action Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F131D).copy(alpha = 0.95f))
                .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Player info row
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

                // Match Bet Chips
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

            // Turn Timer Progress
            if (isMyTurn) {
                val turnProgress = (state.turnTimerSeconds / 30f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { turnProgress },
                    color = if (state.turnTimerSeconds <= 5) Color(0xFFFF5252) else Color(0xFF00E5FF),
                    trackColor = Color(0xFF1E2638),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                )
            }

            // PRIMARY ACTION CONTROLS (Matching user reference image!)
            if (state.dice == null && (isMyTurn || state.isRollingForTurn)) {
                // Roll Dice Button with glowing pulse
                GlowingRollDiceButton(
                    isRollingForTurn = state.isRollingForTurn,
                    onClick = onRollClick
                )
            } else if (isMyTurn && state.dice != null && !state.isRollingForTurn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Undo Move Button
                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onUndoClick()
                        },
                        enabled = canUndo,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A2338),
                            disabledContainerColor = Color(0xFF101420)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (canUndo) Color.White else Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text("برگشت", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (canUndo) Color.White else Color.Gray)
                    }

                    // Confirm Turn Button
                    val pulseAnim = rememberInfiniteTransition(label = "pulse_confirm")
                    val confirmScale by pulseAnim.animateFloat(
                        initialValue = 0.98f,
                        targetValue = 1.03f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(650, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onConfirmTurn()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .then(if (allMovesFinished) Modifier.scale(confirmScale) else Modifier),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allMovesFinished) Color(0xFF00E676) else Color(0xFF00B0FF)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(Modifier.width(4.dp))
                        Text("تایید حرکت", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }

                    // Doubling Offer Button (if available)
                    if (!state.doublingOffered) {
                        Button(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                onDoubleOffer()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
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
// LANDSCAPE LAYOUT (Horizontal Screen)
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
    onOpenResign: () -> Unit
) {
    val view = LocalView.current

    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT SIDEBAR: Player 1 (You - Cyan Dragon)
        Column(
            modifier = Modifier
                .width(170.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlayerProfileCard(
                    name = state.whitePlayerName,
                    pipCount = state.whitePipCount,
                    avatarEmoji = "👑",
                    isTurn = state.currentTurn == PlayerColor.WHITE,
                    timerSeconds = state.turnTimerSeconds,
                    bankSeconds = state.whiteBankSeconds,
                    isUsingBankTime = state.isUsingBankTime && state.currentTurn == PlayerColor.WHITE,
                    isWhite = true
                )

                if (state.currentTurn == PlayerColor.WHITE || state.isRollingForTurn) {
                    SidebarDiceWidget(
                        diceRoll = state.dice,
                        isMyTurn = true,
                        isRollingForTurn = state.isRollingForTurn,
                        onRollClick = onRollClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp)
                    )
                }

                if (isMyTurn && state.dice != null && !state.isRollingForTurn) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF141926))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onUndoClick()
                            },
                            enabled = canUndo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F2B44),
                                disabledContainerColor = Color(0xFF141926)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (canUndo) Color.White else Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text("برگشت مهره", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (canUndo) Color.White else Color.Gray)
                        }

                        val pulseAnim = rememberInfiniteTransition(label = "pulse_confirm")
                        val confirmScale by pulseAnim.animateFloat(
                            initialValue = 0.98f,
                            targetValue = 1.04f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(650, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )

                        Button(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                onConfirmTurn()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .then(if (allMovesFinished) Modifier.scale(confirmScale) else Modifier),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (allMovesFinished) Color(0xFF00E676) else Color(0xFF00B0FF)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(17.dp), tint = Color.Black)
                            Spacer(Modifier.width(4.dp))
                            Text("تایید حرکت", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AutoActionToggle(title = "تاس خودکار", icon = Icons.Default.Refresh, isEnabled = autoRoll, onToggle = onToggleAutoRoll)
                AutoActionToggle(title = "خروج خودکار", icon = Icons.Default.FastForward, isEnabled = autoBearOff, onToggle = onToggleAutoBearOff)
            }
        }

        // CENTER: 3D Board
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            BoardCanvas(
                state = state,
                theme = theme,
                onPointClick = onPointClick,
                onBarClick = onBarClick,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1.36f, matchHeightConstraintsFirst = false)
            )

            if (state.dice != null || state.openingRoll != null) {
                BoardDiceOverlay(
                    diceRoll = state.dice,
                    openingRoll = state.openingRoll,
                    isMyTurn = isMyTurn,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Floating Glowing Turn Banner
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
            ) {
                FloatingTurnBanner(
                    isMyTurn = isMyTurn,
                    timerSeconds = state.turnTimerSeconds,
                    isUsingBank = state.isUsingBankTime
                )
            }

            if (commentary.isNotBlank()) {
                Surface(
                    color = Color(0xFF0E131E).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "💡 $commentary",
                        color = Color(0xFF80D8FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // RIGHT SIDEBAR: Player 2 (Opponent - Imperial Gold Dragon)
        Column(
            modifier = Modifier
                .width(170.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PlayerProfileCard(
                    name = state.blackPlayerName,
                    pipCount = state.blackPipCount,
                    avatarEmoji = "🤖",
                    isTurn = state.currentTurn == PlayerColor.BLACK,
                    timerSeconds = state.turnTimerSeconds,
                    bankSeconds = state.blackBankSeconds,
                    isUsingBankTime = state.isUsingBankTime && state.currentTurn == PlayerColor.BLACK,
                    isWhite = false
                )

                if (state.currentTurn == PlayerColor.BLACK) {
                    SidebarDiceWidget(
                        diceRoll = state.dice,
                        isMyTurn = false,
                        onRollClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MatchStakeCard(bet = state.matchBet, doubling = state.doublingCubeValue)

                if (isMyTurn && state.dice == null && !state.doublingOffered) {
                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onDoubleOffer()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("پیشنهاد دوبل (2X)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenChat,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242A38)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                    }
                    Button(
                        onClick = onOpenResign,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381C22)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = "Resign", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    }
                }
            }
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

@Composable
private fun PlayerProfileCard(
    name: String,
    pipCount: Int,
    avatarEmoji: String,
    isTurn: Boolean,
    timerSeconds: Int,
    bankSeconds: Int,
    isUsingBankTime: Boolean,
    isWhite: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121622))
            .border(
                1.5.dp,
                if (isTurn) {
                    if (isUsingBankTime) Color(0xFFFF5252) else (if (isWhite) Color(0xFF00E5FF) else Color(0xFFFFD700))
                } else Color(0xFF232A3D),
                RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isWhite) Color(0xFF0D253A) else Color(0xFF281F0E))
                    .border(1.2.dp, if (isWhite) Color(0xFF00E5FF) else Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(avatarEmoji, fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("PIP: $pipCount", color = if (isWhite) Color(0xFF00E5FF) else Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("بانک ذخیره:", color = Color.Gray, fontSize = 10.sp)
            Text(
                text = "${bankSeconds}s",
                color = if (isUsingBankTime) Color(0xFFFF5252) else Color(0xFF90CAF9),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isTurn) {
            Spacer(modifier = Modifier.height(6.dp))

            if (isUsingBankTime) {
                val bankProgress = (bankSeconds / 30f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { bankProgress },
                    color = Color(0xFFFF3D00),
                    trackColor = Color(0xFF4A1515),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = "⚠️ زمان اضافه: ${bankSeconds}s",
                    color = Color(0xFFFF5252),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    textAlign = TextAlign.End
                )
            } else {
                val turnProgress = (timerSeconds / 30f).coerceIn(0f, 1f)
                val timerColor = if (timerSeconds <= 5) Color(0xFFFF5252) else (if (isWhite) Color(0xFF00E5FF) else Color(0xFFFFD700))
                LinearProgressIndicator(
                    progress = { turnProgress },
                    color = timerColor,
                    trackColor = Color(0xFF222B3D),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = "زمان نوبت: ${timerSeconds}s",
                    color = timerColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun MatchStakeCard(bet: Long, doubling: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF151924))
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text("جایزه مسابقه", color = Color.Gray, fontSize = 9.sp)
            Text("🪙 ${bet * 2 * doubling}", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
        DoublingCubeBadge(value = doubling)
    }
}

@Composable
private fun AutoActionToggle(title: String, icon: ImageVector, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isEnabled) Color(0xFF004D40) else Color(0xFF141926))
            .clickable { onToggle(!isEnabled) }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isEnabled) Color(0xFF00E676) else Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, color = if (isEnabled) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            modifier = Modifier
                .scale(0.6f)
                .padding(end = 0.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00E676),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF222B3D),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
