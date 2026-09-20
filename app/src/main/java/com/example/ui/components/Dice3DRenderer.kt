package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiceRoll
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SidebarDiceWidget(
    diceRoll: DiceRoll?,
    isMyTurn: Boolean,
    isRollingForTurn: Boolean = false,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (diceRoll == null) {
            if (isMyTurn || isRollingForTurn) {
                // Large Glowing "ROLL DICE" Button in Center
                val pulseAnim = rememberInfiniteTransition(label = "pulse")
                val glowAlpha by pulseAnim.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = Color(0xFFFFD700)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                            )
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onRollClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🎲", fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isRollingForTurn) "پرتاب تاس شروع" else "پرتاب تاس",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else {
                Surface(
                    color = Color(0xFF161524).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF))
                        )
                        Text(
                            text = "نوبت حریف...",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            // Display Static Numbers / Cubes
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val die1Available = diceRoll.remainingMoves.contains(diceRoll.die1)
                val die2Available = diceRoll.remainingMoves.contains(diceRoll.die2)

                Single3DDieCube(value = diceRoll.die1, isUsed = !die1Available, size = 42.dp)
                Single3DDieCube(value = diceRoll.die2, isUsed = !die2Available, size = 42.dp)
            }
        }
    }
}

@Composable
fun BoardDiceOverlay(
    diceRoll: DiceRoll?,
    openingRoll: Pair<Int, Int>?,
    isMyTurn: Boolean,
    modifier: Modifier = Modifier
) {
    var isRollingAnimation by remember { mutableStateOf(false) }

    // Multi-axis rotation and dynamic scale for realistic 3D tumble
    val rotationAnim1 = remember { Animatable(0f) }
    val rotationAnim2 = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }
    val particleProgress = remember { Animatable(0f) }
    
    // Throw trajectory from player side directly into Left or Right playfield
    val offsetXAnim1 = remember { Animatable(0f) }
    val offsetYAnim1 = remember { Animatable(0f) }
    val offsetXAnim2 = remember { Animatable(0f) }
    val offsetYAnim2 = remember { Animatable(0f) }

    // Natural final landing resting tilt angle
    val restAngle1 = remember { -8f + Random.nextInt(16).toFloat() }
    val restAngle2 = remember { 6f + Random.nextInt(16).toFloat() }

    LaunchedEffect(diceRoll, openingRoll) {
        if (openingRoll != null) {
            // Opening roll: White die lands on Left quadrant (-135dp), Black die on Right quadrant (+135dp)
            isRollingAnimation = true
            rotationAnim1.snapTo(0f)
            rotationAnim2.snapTo(0f)
            scaleAnim.snapTo(0.3f)
            particleProgress.snapTo(0f)

            offsetXAnim1.snapTo(-350f)
            offsetYAnim1.snapTo(100f)
            offsetXAnim2.snapTo(350f)
            offsetYAnim2.snapTo(-100f)

            launch {
                offsetXAnim1.animateTo(-135f, tween(650, easing = FastOutSlowInEasing))
            }
            launch {
                offsetYAnim1.animateTo(0f, tween(650, easing = FastOutSlowInEasing))
            }
            launch {
                offsetXAnim2.animateTo(135f, tween(650, easing = FastOutSlowInEasing))
            }
            launch {
                offsetYAnim2.animateTo(0f, tween(650, easing = FastOutSlowInEasing))
            }

            scaleAnim.animateTo(1.2f, spring(stiffness = Spring.StiffnessLow))
            launch {
                rotationAnim1.animateTo(1080f + restAngle1, tween(700, easing = FastOutSlowInEasing))
            }
            launch {
                rotationAnim2.animateTo(1080f + restAngle2, tween(700, easing = FastOutSlowInEasing))
            }
            particleProgress.animateTo(1f, tween(700, easing = LinearOutSlowInEasing))
            scaleAnim.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

            isRollingAnimation = false
        } else if (diceRoll != null) {
            // Regular roll:
            // White rolls to Right quadrant (+135dp), Black rolls to Left quadrant (-135dp)
            isRollingAnimation = true
            rotationAnim1.snapTo(0f)
            rotationAnim2.snapTo(0f)
            scaleAnim.snapTo(0.3f)
            particleProgress.snapTo(0f)

            val startX = if (isMyTurn) -350f else 350f
            val targetBaseX = if (isMyTurn) 135f else -135f

            offsetXAnim1.snapTo(startX)
            offsetYAnim1.snapTo(50f)
            offsetXAnim2.snapTo(startX)
            offsetYAnim2.snapTo(-50f)

            // Throw animation: curved trajectory to middle of target field (left or right, never center)
            launch {
                offsetXAnim1.animateTo(targetBaseX - 24f, tween(600, easing = FastOutSlowInEasing))
            }
            launch {
                offsetYAnim1.animateTo(-8f + Random.nextInt(16).toFloat(), tween(600, easing = FastOutSlowInEasing))
            }
            launch {
                offsetXAnim2.animateTo(targetBaseX + 24f, tween(600, easing = FastOutSlowInEasing))
            }
            launch {
                offsetYAnim2.animateTo(8f + Random.nextInt(16).toFloat(), tween(600, easing = FastOutSlowInEasing))
            }

            // Tumble and bounce
            scaleAnim.animateTo(1.2f, spring(stiffness = Spring.StiffnessLow))
            launch {
                rotationAnim1.animateTo(1440f + restAngle1, tween(700, easing = FastOutSlowInEasing))
            }
            launch {
                rotationAnim2.animateTo(1440f + restAngle2, tween(700, easing = FastOutSlowInEasing))
            }
            particleProgress.animateTo(1f, tween(700, easing = LinearOutSlowInEasing))
            scaleAnim.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

            isRollingAnimation = false
        }
    }

    if (openingRoll != null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (particleProgress.value in 0.01f..0.99f) {
                DiceSparkleBurst(progress = particleProgress.value)
            }

            // White Player Die on Left Quadrant
            Box(
                modifier = Modifier
                    .offset(x = offsetXAnim1.value.dp, y = offsetYAnim1.value.dp)
                    .scale(scaleAnim.value)
                    .rotate(if (isRollingAnimation) rotationAnim1.value else restAngle1)
            ) {
                Single3DDieCube(value = openingRoll.first, isUsed = false, size = 38.dp)
            }

            // Black Player Die on Right Quadrant
            Box(
                modifier = Modifier
                    .offset(x = offsetXAnim2.value.dp, y = offsetYAnim2.value.dp)
                    .scale(scaleAnim.value)
                    .rotate(if (isRollingAnimation) rotationAnim2.value else restAngle2)
            ) {
                Single3DDieCube(value = openingRoll.second, isUsed = false, size = 38.dp)
            }
        }
    } else if (diceRoll != null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Particle Burst behind dice during roll
            if (particleProgress.value in 0.01f..0.99f) {
                DiceSparkleBurst(progress = particleProgress.value)
            }

            // Die 1
            Box(
                modifier = Modifier
                    .offset(x = offsetXAnim1.value.dp, y = offsetYAnim1.value.dp)
                    .scale(scaleAnim.value)
                    .rotate(if (isRollingAnimation) rotationAnim1.value else restAngle1)
            ) {
                Single3DDieCube(value = diceRoll.die1, isUsed = false, size = 38.dp)
            }

            // Die 2
            Box(
                modifier = Modifier
                    .offset(x = offsetXAnim2.value.dp, y = offsetYAnim2.value.dp)
                    .scale(scaleAnim.value)
                    .rotate(if (isRollingAnimation) rotationAnim2.value else restAngle2)
            ) {
                Single3DDieCube(value = diceRoll.die2, isUsed = false, size = 38.dp)
            }
        }
    }
}

@Composable
fun Single3DDieCube(
    value: Int,
    isUsed: Boolean,
    size: androidx.compose.ui.unit.Dp = 38.dp
) {
    val alpha = if (isUsed) 0.35f else 1.0f

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = if (isUsed) 2.dp else 10.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = Color.Black,
                spotColor = Color(0xFFFFD700)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isUsed) {
                    Brush.linearGradient(listOf(Color(0xFF2C2C34), Color(0xFF1E1E24)))
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFFF9E6),
                            Color(0xFFF0DEB4),
                            Color(0xFFD6BA84)
                        )
                    )
                }
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFFD700).copy(alpha = alpha),
                        Color(0xFFB8860B).copy(alpha = alpha * 0.7f),
                        Color(0xFFFFE082).copy(alpha = alpha)
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Top-left Specular Bevel Highlight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isUsed) 0.05f else 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(8f, 8f),
                        radius = 35f
                    )
                )
        )

        // Die Face Dots (Pip Positions)
        DieDotsLayout(value = value, dotColor = if (isUsed) Color.Gray else Color(0xFF8B0000))
    }
}

@Composable
fun DieDotsLayout(value: Int, dotColor: Color) {
    val dotSize = 8.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        when (value) {
            1 -> {
                DieDot(dotColor, dotSize, Alignment.Center)
            }
            2 -> {
                DieDot(dotColor, dotSize, Alignment.TopStart)
                DieDot(dotColor, dotSize, Alignment.BottomEnd)
            }
            3 -> {
                DieDot(dotColor, dotSize, Alignment.TopStart)
                DieDot(dotColor, dotSize, Alignment.Center)
                DieDot(dotColor, dotSize, Alignment.BottomEnd)
            }
            4 -> {
                DieDot(dotColor, dotSize, Alignment.TopStart)
                DieDot(dotColor, dotSize, Alignment.TopEnd)
                DieDot(dotColor, dotSize, Alignment.BottomStart)
                DieDot(dotColor, dotSize, Alignment.BottomEnd)
            }
            5 -> {
                DieDot(dotColor, dotSize, Alignment.TopStart)
                DieDot(dotColor, dotSize, Alignment.TopEnd)
                DieDot(dotColor, dotSize, Alignment.Center)
                DieDot(dotColor, dotSize, Alignment.BottomStart)
                DieDot(dotColor, dotSize, Alignment.BottomEnd)
            }
            6 -> {
                DieDot(dotColor, dotSize, Alignment.TopStart)
                DieDot(dotColor, dotSize, Alignment.TopEnd)
                DieDot(dotColor, dotSize, Alignment.CenterStart)
                DieDot(dotColor, dotSize, Alignment.CenterEnd)
                DieDot(dotColor, dotSize, Alignment.BottomStart)
                DieDot(dotColor, dotSize, Alignment.BottomEnd)
            }
        }
    }
}

@Composable
fun BoxScope.DieDot(
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    alignment: Alignment
) {
    Box(
        modifier = Modifier
            .size(size)
            .align(alignment)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0.85f),
                        Color.Black.copy(alpha = 0.6f)
                    )
                )
            )
            .border(0.5.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
    )
}

@Composable
fun DiceSparkleBurst(progress: Float) {
    Canvas(
        modifier = Modifier
            .size(140.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val particleCount = 16

        for (i in 0 until particleCount) {
            val angle = (i * (360f / particleCount)) * (PI / 180f).toFloat()
            val distance = progress * (size.width * 0.45f)
            val px = center.x + cos(angle) * distance
            val py = center.y + sin(angle) * distance
            val alpha = ((1f - progress) * 0.9f).coerceIn(0f, 1f)
            val radius = (1f - progress) * 4f + 2f

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alpha),
                radius = radius,
                center = Offset(px, py)
            )
        }
    }
}
private const val PI = 3.141592653589793
