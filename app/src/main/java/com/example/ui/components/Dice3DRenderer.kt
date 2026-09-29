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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiceRoll
import kotlinx.coroutines.delay
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
    val view = LocalView.current

    val animProgress = remember { Animatable(1f) }
    var rollingDisplay1 by remember { mutableIntStateOf(1) }
    var rollingDisplay2 by remember { mutableIntStateOf(2) }

    val restTiltX1 = remember { -6f + Random.nextFloat() * 12f }
    val restTiltY1 = remember { -6f + Random.nextFloat() * 12f }
    val restTiltZ1 = remember { -12f + Random.nextFloat() * 24f }

    val restTiltX2 = remember { -6f + Random.nextFloat() * 12f }
    val restTiltY2 = remember { -6f + Random.nextFloat() * 12f }
    val restTiltZ2 = remember { -12f + Random.nextFloat() * 24f }

    var impactTriggered1 by remember { mutableStateOf(false) }
    var impactTriggered2 by remember { mutableStateOf(false) }

    val isDoubles = (diceRoll != null && diceRoll.die1 == diceRoll.die2)

    LaunchedEffect(diceRoll, openingRoll) {
        if (openingRoll != null || diceRoll != null) {
            animProgress.snapTo(0f)
            impactTriggered1 = false
            impactTriggered2 = false

            val rollJob = launch {
                while (animProgress.value < 0.85f) {
                    rollingDisplay1 = Random.nextInt(1, 7)
                    rollingDisplay2 = Random.nextInt(1, 7)
                    delay(45)
                }
                if (openingRoll != null) {
                    rollingDisplay1 = openingRoll.first
                    rollingDisplay2 = openingRoll.second
                } else if (diceRoll != null) {
                    rollingDisplay1 = diceRoll.die1
                    rollingDisplay2 = diceRoll.die2
                }
            }

            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 850, easing = LinearEasing)
            )
            rollJob.join()
        }
    }

    val progress = animProgress.value

    LaunchedEffect(progress) {
        if (progress in 0.52f..0.58f && !impactTriggered1) {
            impactTriggered1 = true
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } else if (progress in 0.80f..0.86f && !impactTriggered2) {
            impactTriggered2 = true
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    if (openingRoll != null || diceRoll != null) {
        val finalVal1 = openingRoll?.first ?: (diceRoll?.die1 ?: 1)
        val finalVal2 = openingRoll?.second ?: (diceRoll?.die2 ?: 2)

        val showVal1 = if (progress >= 0.85f) finalVal1 else rollingDisplay1
        val showVal2 = if (progress >= 0.85f) finalVal2 else rollingDisplay2

        val startX1: Float
        val startY1: Float
        val targetX1: Float
        val targetY1: Float

        val startX2: Float
        val startY2: Float
        val targetX2: Float
        val targetY2: Float

        if (openingRoll != null) {
            startX1 = -380f
            startY1 = 120f
            targetX1 = -140f
            targetY1 = 0f

            startX2 = 380f
            startY2 = -120f
            targetX2 = 140f
            targetY2 = 0f
        } else {
            val baseStartX = if (isMyTurn) -380f else 380f
            val baseTargetX = if (isMyTurn) 140f else -140f

            startX1 = baseStartX
            startY1 = 60f
            targetX1 = baseTargetX - 26f
            targetY1 = -10f

            startX2 = baseStartX
            startY2 = -60f
            targetX2 = baseTargetX + 26f
            targetY2 = 12f
        }

        val interp = cubicEaseOut(progress)
        val curX1 = startX1 + (targetX1 - startX1) * interp
        val curY1 = startY1 + (targetY1 - startY1) * interp
        val curX2 = startX2 + (targetX2 - startX2) * interp
        val curY2 = startY2 + (targetY2 - startY2) * interp

        val height1 = computeParabolicBounceHeight(progress, 85f, 30f, 8f)
        val height2 = computeParabolicBounceHeight(progress, 75f, 26f, 7f)

        val rotX1 = (1f - progress) * 1080f + restTiltX1
        val rotY1 = (1f - progress) * 720f + restTiltY1
        val rotZ1 = (1f - progress) * 1440f + restTiltZ1

        val rotX2 = (1f - progress) * 900f + restTiltX2
        val rotY2 = (1f - progress) * 1080f + restTiltY2
        val rotZ2 = (1f - progress) * 1260f + restTiltZ2

        val die1Available = diceRoll?.remainingMoves?.contains(diceRoll.die1) ?: true
        val die2Available = diceRoll?.remainingMoves?.contains(diceRoll.die2) ?: true

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (progress in 0.50f..0.98f) {
                val burstProgress = ((progress - 0.50f) / 0.48f).coerceIn(0f, 1f)
                DiceSparkleBurst(
                    progress = burstProgress,
                    centerOffset = Offset(curX1, curY1)
                )
                DiceSparkleBurst(
                    progress = burstProgress,
                    centerOffset = Offset(curX2, curY2)
                )
            }

            if (isDoubles && progress >= 0.82f) {
                val auraPulse = rememberInfiniteTransition(label = "aura")
                val auraScale by auraPulse.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "auraScale"
                )
                Canvas(modifier = Modifier.size(160.dp).offset(x = curX1.dp, y = curY1.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.45f),
                                Color(0xFFFF8F00).copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * 0.5f * auraScale
                    )
                }
            }

            Realistic3DDieActor(
                value = showVal1,
                offsetX = curX1,
                offsetY = curY1,
                heightZ = height1,
                rotX = rotX1,
                rotY = rotY1,
                rotZ = rotZ1,
                isUsed = !die1Available,
                size = 40.dp
            )

            Realistic3DDieActor(
                value = showVal2,
                offsetX = curX2,
                offsetY = curY2,
                heightZ = height2,
                rotX = rotX2,
                rotY = rotY2,
                rotZ = rotZ2,
                isUsed = !die2Available,
                size = 40.dp
            )
        }
    }
}

private fun cubicEaseOut(t: Float): Float {
    val f = t - 1.0f
    return f * f * f + 1.0f
}

private fun computeParabolicBounceHeight(t: Float, h1: Float, h2: Float, h3: Float): Float {
    return when {
        t < 0.54f -> {
            val p = t / 0.54f
            4f * h1 * p * (1f - p)
        }
        t < 0.82f -> {
            val p = (t - 0.54f) / 0.28f
            4f * h2 * p * (1f - p)
        }
        t < 1.0f -> {
            val p = (t - 0.82f) / 0.18f
            4f * h3 * p * (1f - p)
        }
        else -> 0f
    }
}

@Composable
fun Realistic3DDieActor(
    value: Int,
    offsetX: Float,
    offsetY: Float,
    heightZ: Float,
    rotX: Float,
    rotY: Float,
    rotZ: Float,
    isUsed: Boolean,
    size: Dp
) {
    val shadowScale = (1f + (heightZ / 80f) * 0.7f).coerceIn(1f, 1.8f)
    val shadowAlpha = ((1f - (heightZ / 80f) * 0.75f) * 0.85f).coerceIn(0.15f, 0.85f)
    val shadowOffsetY = offsetY + (heightZ * 0.35f) + 12f

    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = shadowOffsetY.dp)
            .size(width = size * 1.15f * shadowScale, height = size * 0.55f * shadowScale)
            .graphicsLayer {
                alpha = if (isUsed) shadowAlpha * 0.4f else shadowAlpha
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.90f),
                        Color.Black.copy(alpha = 0.50f),
                        Color.Transparent
                    )
                )
            )
    )

    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = (offsetY - heightZ).dp)
            .graphicsLayer {
                rotationX = rotX
                rotationY = rotY
                rotationZ = rotZ
                cameraDistance = 16f * density
                scaleX = 1f + (heightZ / 100f) * 0.15f
                scaleY = 1f + (heightZ / 100f) * 0.15f
            }
    ) {
        Single3DDieCube(
            value = value,
            isUsed = isUsed,
            size = size
        )
    }
}

@Composable
fun Single3DDieCube(
    value: Int,
    isUsed: Boolean,
    size: Dp = 40.dp
) {
    val alpha = if (isUsed) 0.35f else 1.0f
    val neonColor = Color(0xFF00E5FF)

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = if (isUsed) 2.dp else 12.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = Color.Black,
                spotColor = neonColor
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isUsed) {
                    Brush.linearGradient(listOf(Color(0xFF1E212B), Color(0xFF111319)))
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF262E3E),
                            Color(0xFF171B26),
                            Color(0xFF0C0E14)
                        )
                    )
                }
            )
            .border(
                width = 1.6.dp,
                brush = Brush.linearGradient(
                    listOf(
                        neonColor.copy(alpha = alpha),
                        Color(0xFF0D47A1).copy(alpha = alpha * 0.7f),
                        neonColor.copy(alpha = alpha)
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            val highlightPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w - 6f, 6f)
                lineTo(6f, 6f)
                lineTo(6f, h - 6f)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = highlightPath,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = if (isUsed) 0.05f else 0.55f),
                        Color.Transparent
                    )
                ),
                style = Fill
            )

            val shadowPath = Path().apply {
                moveTo(w, 0f)
                lineTo(w, h)
                lineTo(0f, h)
                lineTo(6f, h - 6f)
                lineTo(w - 6f, h - 6f)
                lineTo(w - 6f, 6f)
                close()
            }
            drawPath(
                path = shadowPath,
                brush = Brush.linearGradient(
                    listOf(
                        Color.Black.copy(alpha = if (isUsed) 0.15f else 0.45f),
                        Color.Transparent
                    )
                ),
                style = Fill
            )
        }

        DieDotsLayout(
            value = value,
            dotColor = if (isUsed) Color.Gray else neonColor
        )
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
                DieDot(dotColor, dotSize * 1.25f, Alignment.Center)
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
    size: Dp,
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
                        Color.White,
                        color,
                        color.copy(alpha = 0.85f),
                        Color.Black.copy(alpha = 0.5f)
                    )
                )
            )
            .border(0.6.dp, color.copy(alpha = 0.7f), CircleShape)
    )
}

@Composable
fun DiceSparkleBurst(
    progress: Float,
    centerOffset: Offset = Offset.Zero
) {
    Canvas(
        modifier = Modifier
            .size(150.dp)
            .offset(x = centerOffset.x.dp, y = centerOffset.y.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val particleCount = 18

        val ringRadius = progress * (size.width * 0.48f)
        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = ((1f - progress) * 0.4f).coerceIn(0f, 0.4f)),
            radius = ringRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        for (i in 0 until particleCount) {
            val angle = (i * (360f / particleCount)) * (PI / 180f).toFloat()
            val distance = progress * (size.width * 0.50f)
            val px = center.x + cos(angle) * distance
            val py = center.y + sin(angle) * distance
            val alpha = ((1f - progress) * 0.95f).coerceIn(0f, 1f)
            val radius = ((1f - progress) * 5f + 1.5f).coerceAtLeast(0.5f)

            drawCircle(
                color = if (i % 2 == 0) Color(0xFFFFD700).copy(alpha = alpha) else Color(0xFFFF6D00).copy(alpha = alpha),
                radius = radius,
                center = Offset(px, py)
            )
        }
    }
}

private const val PI = 3.141592653589793
