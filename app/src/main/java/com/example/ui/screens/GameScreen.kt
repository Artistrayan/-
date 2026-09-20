package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            delay(600) // Brief pause to feel natural
            onRollClick()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E17)) // Deep luxury casino background
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ==========================================
        // LEFT SIDEBAR (Player 1 - You)
        // ==========================================
        Column(
            modifier = Modifier
                .width(170.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                // Render Dice or Roll Button if it's our turn or rolling for turn
                if (state.currentTurn == PlayerColor.WHITE || state.isRollingForTurn) {
                    SidebarDiceWidget(
                        diceRoll = state.dice,
                        isMyTurn = true,
                        isRollingForTurn = state.isRollingForTurn,
                        onRollClick = onRollClick,
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )
                }

                // Move actions: Undo & Confirm Turn
                if (isMyTurn && state.dice != null && !state.isRollingForTurn) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1B192A))
                            .border(1.dp, Color(0xFF322E4A), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Undo Button
                        Button(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onUndoClick()
                            },
                            enabled = canUndo,
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF253354),
                                disabledContainerColor = Color(0xFF1C2230)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (canUndo) Color.White else Color.Gray)
                            Spacer(Modifier.width(6.dp))
                            Text("برگشت مهره", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (canUndo) Color.White else Color.Gray)
                        }

                        // Confirm Turn Button
                        val pulseAnim = rememberInfiniteTransition(label = "confirm_pulse")
                        val confirmGlow by pulseAnim.animateFloat(
                            initialValue = 0.85f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(700, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "confirm_glow"
                        )

                        Button(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                onConfirmTurn()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .then(if (allMovesFinished) Modifier.scale(confirmGlow) else Modifier),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (allMovesFinished) Color(0xFF10B981) else Color(0xFF047857)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(17.dp), tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text("تایید حرکت", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AutoActionToggle(
                    title = "تاس خودکار",
                    icon = Icons.Default.Refresh,
                    isEnabled = autoRoll,
                    onToggle = { autoRoll = it }
                )
                AutoActionToggle(
                    title = "خروج خودکار",
                    icon = Icons.Default.FastForward,
                    isEnabled = autoBearOff,
                    onToggle = { autoBearOff = it }
                )
            }
        }

        // ==========================================
        // CENTER: 3D STANDARD SIZED BOARD
        // ==========================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Fixed Aspect Ratio ensures the board is strictly proportioned and never stretched!
            BoardCanvas(
                state = state,
                theme = theme,
                onPointClick = onPointClick,
                onBarClick = onBarClick,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1.35f, matchHeightConstraintsFirst = false) // Standard Backgammon ratio
            )

            // Dynamic 3D Dice Tumble Overlay on Board (falls in Left or Right field, never on center divider)
            if (state.dice != null || state.openingRoll != null) {
                BoardDiceOverlay(
                    diceRoll = state.dice,
                    openingRoll = state.openingRoll,
                    isMyTurn = isMyTurn,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Commentary Overlay inside board area
            if (commentary.isNotBlank()) {
                Surface(
                    color = Color(0xFF141120).copy(alpha = 0.90f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "💡 $commentary",
                        color = Color(0xFFFFE082),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // ==========================================
        // RIGHT SIDEBAR (Player 2 - Opponent)
        // ==========================================
        Column(
            modifier = Modifier
                .width(170.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                // Render Dice if it's opponent's turn
                if (state.currentTurn == PlayerColor.BLACK) {
                    SidebarDiceWidget(
                        diceRoll = state.dice,
                        isMyTurn = false,
                        onRollClick = {},
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Match Stake & Double Cube
                MatchStakeCard(bet = state.matchBet, doubling = state.doublingCubeValue)

                if (isMyTurn && state.dice == null && !state.doublingOffered) {
                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onDoubleOffer()
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("پیشنهاد دوبل (2X)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showChatDialog = true },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E3A)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                    }
                    Button(
                        onClick = { showResignDialog = true },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A1E1E)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = "Resign", tint = Color(0xFFFF4D4D), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // Dialogs...
    if (showResignDialog) {
        AlertDialog(
            onDismissRequest = { showResignDialog = false },
            containerColor = Color(0xFF181525),
            title = { Text("تسلیم شدن؟", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = { Text("سکه‌های شرط (${state.matchBet}) کسر خواهند شد.", color = Color.LightGray, fontSize = 13.sp) },
            confirmButton = {
                Button(onClick = { showResignDialog = false; onExitGame() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D))) {
                    Text("تسلیم", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResignDialog = false }) { Text("ادامه", color = Color.Gray) }
            }
        )
    }

    if (state.isGameOver) {
        val isWin = (state.winner == PlayerColor.WHITE)
        val wonCoins = if (isWin) (state.matchBet * 1.95).toLong() else 0L

        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF141324),
            shape = RoundedCornerShape(22.dp),
            title = { Text(if (isWin) "🏆 پیروزی!" else "💔 شکست", color = if (isWin) Color(0xFFFFD700) else Color(0xFFFF5252), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (isWin) {
                        Text("جایزه: 🪙 +$wonCoins سکه", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("سکه‌های کسر شده: -${state.matchBet}", color = Color(0xFFFF8A80), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(onClick = onExitGame, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)), modifier = Modifier.fillMaxWidth()) {
                    Text("بازگشت به لابی", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            }
        )
    }

    if (showChatDialog) {
        MatchChatDialog(onDismiss = { showChatDialog = false }, onSendMessage = onSendChat)
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
            .background(Color(0xFF161422))
            .border(
                1.5.dp,
                if (isTurn) {
                    if (isUsingBankTime) Color(0xFFFF5252) else Color(0xFFFFD700)
                } else Color(0xFF2A2A3A),
                RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isWhite) Color(0xFFFFF8DC) else Color(0xFF282834)),
                contentAlignment = Alignment.Center
            ) {
                Text(avatarEmoji, fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("PIP: $pipCount", color = if (isTurn) Color(0xFFFFD700) else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Persistent Bank Time Display
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
                // Bank Timer Running
                val bankProgress = (bankSeconds / 30f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { bankProgress },
                    color = Color(0xFFFF3D00),
                    trackColor = Color(0xFF4A1515),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                )
                Text(
                    text = "⚠️ زمان اضافه: ${bankSeconds}s",
                    color = Color(0xFFFF5252),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    textAlign = TextAlign.End
                )
            } else {
                // 20s Standard Turn Timer Running
                val turnProgress = (timerSeconds / 20f).coerceIn(0f, 1f)
                val timerColor = if (timerSeconds <= 5) Color(0xFFFF5252) else Color(0xFFFFD700)
                LinearProgressIndicator(
                    progress = { turnProgress },
                    color = timerColor,
                    trackColor = Color(0xFF333344),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                )
                Text(
                    text = "زمان نوبت: ${timerSeconds}s",
                    color = timerColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
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
            .background(Color(0xFF241C10))
            .border(1.dp, Color(0xFFB8860B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text("جایزه مسابقه", color = Color.Gray, fontSize = 9.sp)
            Text("🪙 ${bet * 2 * doubling}", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            color = Color(0xFFFFD700),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                "${doubling}X", 
                color = Color.Black, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Black, 
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun AutoActionToggle(title: String, icon: ImageVector, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isEnabled) Color(0xFF1B4332) else Color(0xFF1C1A29))
            .clickable { onToggle(!isEnabled) }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isEnabled) Color(0xFF4ADE80) else Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, color = if (isEnabled) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            modifier = Modifier.scale(0.6f).padding(end = 0.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White, 
                checkedTrackColor = Color(0xFF4ADE80),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF2A2A3A),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
