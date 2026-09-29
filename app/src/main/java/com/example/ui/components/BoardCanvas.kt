package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * AAA Master Ultra-Realistic 3D Backgammon Board Canvas
 * Features:
 * - Left Quad: Luminous Cyan Electric Blue Dragon Engraving
 * - Right Quad: Radiant 24K Imperial Gold Dragon Engraving
 * - Inlaid glowing neon triangular points (Cyan & Gold)
 * - 3D Checkers with embossed dragon crests (Obsidian Cyan & Polished 24K Gold)
 * - Center Bar with Golden Dragon Spine Hinges & 3D Glowing "64" Doubling Cube
 * - Full responsive touch detection and animated glowing highlights
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun BoardCanvas(
    state: GameState,
    theme: BoardTheme,
    onPointClick: (Int) -> Unit,
    onBarClick: (PlayerColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val textMeasurer = rememberTextMeasurer()

    // Pulse animation for landing target highlights & dragon glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val highlightAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val dragonAuraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragonAura"
    )

    val highlightGlowRadius by infiniteTransition.animateFloat(
        initialValue = 2.5f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRad"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state) {
                detectTapGestures { offset ->
                    if (size.width > 20 && size.height > 20) {
                        val clickedPoint = calculateClickedPoint(offset, size.width.toFloat(), size.height.toFloat())
                        if (clickedPoint != null) {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            if (clickedPoint == 0) {
                                onBarClick(PlayerColor.WHITE)
                            } else if (clickedPoint == 25) {
                                onBarClick(PlayerColor.BLACK)
                            } else {
                                onPointClick(clickedPoint)
                            }
                        }
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        if (w < 50f || h < 50f) return@Canvas

        // ==========================================================
        // 1. BOARD FRAME & PLAYFIELD GEOMETRY
        // ==========================================================
        val frameMarginX = w * 0.022f
        val frameMarginY = h * 0.028f
        val frameW = w - (frameMarginX * 2)
        val frameH = h - (frameMarginY * 2)

        val frameBorderThickness = min(frameW * 0.038f, frameH * 0.068f)
        val innerPlayX = frameMarginX + frameBorderThickness
        val innerPlayY = frameMarginY + frameBorderThickness
        val innerPlayW = frameW - (frameBorderThickness * 2)
        val innerPlayH = frameH - (frameBorderThickness * 2)

        // Side Bear-off Tray width on right
        val bearOffTrayW = innerPlayW * 0.075f
        val playableAreaW = innerPlayW - bearOffTrayW
        val barW = playableAreaW * 0.082f
        val halfPlayW = (playableAreaW - barW) / 2f
        val pointW = halfPlayW / 6f
        val pointH = innerPlayH * 0.425f

        val leftQuadX = innerPlayX
        val barX = leftQuadX + halfPlayW
        val rightQuadX = barX + barW
        val bearOffX = rightQuadX + halfPlayW

        val topY = innerPlayY
        val bottomY = innerPlayY + innerPlayH

        // ==========================================================
        // 2. LUXURY OBSIDIAN / CARBON CASING WITH GOLD TRIM
        // ==========================================================
        drawObsidianDragonCasing(
            x = frameMarginX,
            y = frameMarginY,
            width = frameW,
            height = frameH,
            borderThickness = frameBorderThickness
        )

        // ==========================================================
        // 3. INLAID PLAYFIELDS & MYTHIC DRAGON ENGRAVINGS
        // ==========================================================
        drawDragonPlayfields(
            leftX = leftQuadX,
            rightX = rightQuadX,
            topY = topY,
            halfW = halfPlayW,
            height = innerPlayH,
            barX = barX,
            barW = barW,
            bearOffX = bearOffX,
            bearOffW = bearOffTrayW,
            dragonAuraAlpha = dragonAuraAlpha
        )

        // ==========================================================
        // 4. PRECISION INLAID TRIANGLES (POINTS 1..24)
        // ==========================================================
        val validTargetPoints = state.highlightedMoves.map { it.to }.toSet()
        val glowCyan = Color(0xFF00E5FF)
        val glowGold = Color(0xFFFFD700)

        // Top Row: Points 13..18 (Left) & 19..24 (Right)
        val topPoints = listOf(13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24)
        for (i in topPoints.indices) {
            val ptIdx = topPoints[i]
            val xStart = if (i < 6) leftQuadX + (i * pointW) else rightQuadX + ((i - 6) * pointW)
            val isDark = (ptIdx % 2 == 1)
            val isTarget = validTargetPoints.contains(ptIdx)
            val isSource = (state.selectedPoint == ptIdx)

            drawInlaidDragonTriangle(
                xStart = xStart,
                yStart = topY,
                width = pointW,
                height = pointH,
                isTop = true,
                isCyanNeon = isDark
            )

            if (isTarget || isSource) {
                drawDynamicPointHighlight(
                    xStart = xStart,
                    yStart = topY,
                    width = pointW,
                    height = pointH,
                    isTop = true,
                    glowColor = if (isSource) glowCyan else glowGold,
                    alpha = highlightAlpha,
                    glowRadius = highlightGlowRadius
                )
            }

            // Elegant Glowing Point Number
            drawText(
                textMeasurer = textMeasurer,
                text = "$ptIdx",
                style = TextStyle(
                    color = if (isTarget) glowGold else (if (isDark) glowCyan.copy(alpha = 0.65f) else glowGold.copy(alpha = 0.65f)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                ),
                topLeft = Offset(xStart + (pointW / 2f) - 6f, topY + 3f)
            )
        }

        // Bottom Row: Points 12..7 (Left) & 6..1 (Right)
        val bottomPoints = listOf(12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)
        for (i in bottomPoints.indices) {
            val ptIdx = bottomPoints[i]
            val xStart = if (i < 6) leftQuadX + (i * pointW) else rightQuadX + ((i - 6) * pointW)
            val isDark = (ptIdx % 2 == 0)
            val isTarget = validTargetPoints.contains(ptIdx)
            val isSource = (state.selectedPoint == ptIdx)

            drawInlaidDragonTriangle(
                xStart = xStart,
                yStart = bottomY,
                width = pointW,
                height = pointH,
                isTop = false,
                isCyanNeon = isDark
            )

            if (isTarget || isSource) {
                drawDynamicPointHighlight(
                    xStart = xStart,
                    yStart = bottomY,
                    width = pointW,
                    height = pointH,
                    isTop = false,
                    glowColor = if (isSource) glowCyan else glowGold,
                    alpha = highlightAlpha,
                    glowRadius = highlightGlowRadius
                )
            }

            // Elegant Glowing Point Number
            drawText(
                textMeasurer = textMeasurer,
                text = "$ptIdx",
                style = TextStyle(
                    color = if (isTarget) glowGold else (if (isDark) glowCyan.copy(alpha = 0.65f) else glowGold.copy(alpha = 0.65f)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                ),
                topLeft = Offset(xStart + (pointW / 2f) - 6f, bottomY - 16f)
            )
        }

        // ==========================================================
        // 5. CENTER WOODEN BAR & GOLDEN DRAGON SPINE HINGES
        // ==========================================================
        drawCenterBarAndDragonHinges(
            barX = barX,
            barY = topY,
            barW = barW,
            barH = innerPlayH,
            doublingValue = state.doublingCubeValue
        )

        // ==========================================================
        // 6. GOLDEN DRAGON CORNER BRACKETS
        // ==========================================================
        drawDragonCornerBrackets(
            x = frameMarginX,
            y = frameMarginY,
            w = frameW,
            h = frameH,
            bracketSize = frameBorderThickness * 1.55f
        )

        // ==========================================================
        // 7. 3D DRAGON CHECKERS (OBSIDIAN CYAN & 24K POLISHED GOLD)
        // ==========================================================
        val checkerRadius = max(6f, min(pointW * 0.45f, (pointH / 5.2f) * 0.48f))

        for (ptIdx in 1..24) {
            val ptState = state.points[ptIdx]
            if (ptState.count == 0 || ptState.color == null) continue

            val isTopRow = (ptIdx in 13..24)
            val colIndex = when (ptIdx) {
                in 13..18 -> ptIdx - 13
                in 19..24 -> ptIdx - 19 + 6
                in 7..12 -> 12 - ptIdx
                else -> 6 - ptIdx + 6
            }

            val xCenter = if (colIndex < 6) leftQuadX + (colIndex * pointW) + (pointW / 2f) else rightQuadX + ((colIndex - 6) * pointW) + (pointW / 2f)
            val countToDraw = min(ptState.count, 5)
            val isSelectedPoint = (state.selectedPoint == ptIdx)

            for (c in 0 until countToDraw) {
                val yOffset = c * (checkerRadius * 1.82f)
                val yCenter = if (isTopRow) {
                    topY + checkerRadius + 16f + yOffset
                } else {
                    bottomY - checkerRadius - 16f - yOffset
                }

                drawMasterDragonChecker(
                    center = Offset(xCenter, yCenter),
                    radius = checkerRadius,
                    playerColor = ptState.color,
                    isSelected = isSelectedPoint && (c == countToDraw - 1),
                    glowColor = glowCyan,
                    highlightAlpha = highlightAlpha
                )
            }

            // Stack count badge if > 5 checkers
            if (ptState.count > 5) {
                val lastY = if (isTopRow) topY + 16f + (5 * checkerRadius * 1.82f) else bottomY - 16f - (5 * checkerRadius * 1.82f)
                drawCircle(
                    color = Color(0xFF0A0C14).copy(alpha = 0.95f),
                    radius = max(2f, checkerRadius * 0.70f),
                    center = Offset(xCenter, lastY)
                )
                drawCircle(
                    color = glowGold,
                    radius = max(2f, checkerRadius * 0.70f),
                    center = Offset(xCenter, lastY),
                    style = Stroke(width = 1.8f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "+${ptState.count - 5}",
                    style = TextStyle(color = glowGold, fontSize = 10.sp, fontWeight = FontWeight.Black),
                    topLeft = Offset(xCenter - 8f, lastY - 7f)
                )
            }
        }

        // ==========================================================
        // 8. BAR CHECKERS (Hit Pieces on Center Divider)
        // ==========================================================
        val barXCenter = barX + (barW / 2f)

        // White Bar (Bottom)
        if (state.barWhite > 0) {
            val isBarSelected = (state.selectedPoint == 25)
            for (c in 0 until min(state.barWhite, 4)) {
                val yC = (h / 2f) + (checkerRadius * 1.55f) + (c * checkerRadius * 1.75f)
                drawMasterDragonChecker(
                    center = Offset(barXCenter, yC),
                    radius = checkerRadius,
                    playerColor = PlayerColor.WHITE,
                    isSelected = isBarSelected,
                    glowColor = glowCyan,
                    highlightAlpha = highlightAlpha
                )
            }
            if (state.barWhite > 1) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "${state.barWhite}",
                    style = TextStyle(color = glowCyan, fontSize = 11.sp, fontWeight = FontWeight.Black),
                    topLeft = Offset(barXCenter - 4f, (h / 2f) + (checkerRadius * 0.6f))
                )
            }
        }

        // Black Bar (Top)
        if (state.barBlack > 0) {
            val isBarSelected = (state.selectedPoint == 0)
            for (c in 0 until min(state.barBlack, 4)) {
                val yC = (h / 2f) - (checkerRadius * 1.55f) - (c * checkerRadius * 1.75f)
                drawMasterDragonChecker(
                    center = Offset(barXCenter, yC),
                    radius = checkerRadius,
                    playerColor = PlayerColor.BLACK,
                    isSelected = isBarSelected,
                    glowColor = glowCyan,
                    highlightAlpha = highlightAlpha
                )
            }
            if (state.barBlack > 1) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "${state.barBlack}",
                    style = TextStyle(color = glowGold, fontSize = 11.sp, fontWeight = FontWeight.Black),
                    topLeft = Offset(barXCenter - 4f, (h / 2f) - (checkerRadius * 2.5f))
                )
            }
        }

        // ==========================================================
        // 9. BEAR-OFF TRAYS (Collected Checkers on Right Tray)
        // ==========================================================
        val isBearOffWhite = validTargetPoints.contains(0)
        val isBearOffBlack = validTargetPoints.contains(25)
        val trayXCenter = bearOffX + (bearOffTrayW / 2f)

        // White Bear-off (Bottom Right)
        val whiteTrayY = bottomY - (innerPlayH * 0.24f)
        if (isBearOffWhite) {
            drawRoundRect(
                color = glowCyan.copy(alpha = highlightAlpha * 0.35f),
                topLeft = Offset(bearOffX + 4f, bottomY - (innerPlayH * 0.45f)),
                size = Size(bearOffTrayW - 8f, innerPlayH * 0.42f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = glowCyan.copy(alpha = highlightAlpha),
                topLeft = Offset(bearOffX + 4f, bottomY - (innerPlayH * 0.45f)),
                size = Size(bearOffTrayW - 8f, innerPlayH * 0.42f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2.5f)
            )
        }
        if (state.offWhite > 0) {
            drawMasterDragonChecker(
                center = Offset(trayXCenter, whiteTrayY),
                radius = max(3f, checkerRadius * 0.85f),
                playerColor = PlayerColor.WHITE,
                isSelected = isBearOffWhite,
                glowColor = glowCyan,
                highlightAlpha = highlightAlpha
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "${state.offWhite} خروج",
                style = TextStyle(color = glowCyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                topLeft = Offset(trayXCenter - 14f, whiteTrayY + (checkerRadius * 1.05f))
            )
        }

        // Black Bear-off (Top Right)
        val blackTrayY = topY + (innerPlayH * 0.24f)
        if (isBearOffBlack) {
            drawRoundRect(
                color = glowGold.copy(alpha = highlightAlpha * 0.35f),
                topLeft = Offset(bearOffX + 4f, topY + 8f),
                size = Size(bearOffTrayW - 8f, innerPlayH * 0.42f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = glowGold.copy(alpha = highlightAlpha),
                topLeft = Offset(bearOffX + 4f, topY + 8f),
                size = Size(bearOffTrayW - 8f, innerPlayH * 0.42f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2.5f)
            )
        }
        if (state.offBlack > 0) {
            drawMasterDragonChecker(
                center = Offset(trayXCenter, blackTrayY),
                radius = max(3f, checkerRadius * 0.85f),
                playerColor = PlayerColor.BLACK,
                isSelected = isBearOffBlack,
                glowColor = glowGold,
                highlightAlpha = highlightAlpha
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "${state.offBlack} خروج",
                style = TextStyle(color = glowGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                topLeft = Offset(trayXCenter - 14f, blackTrayY - (checkerRadius * 1.6f))
            )
        }
    }
}

// -------------------------------------------------------------
// DRAWING HELPERS: 3D OBSIDIAN CASING, DRAGONS, INLAYS & CHECKERS
// -------------------------------------------------------------

private fun DrawScope.drawObsidianDragonCasing(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    borderThickness: Float
) {
    // 1. Deep Cast Drop Shadow behind the Board
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.90f),
        topLeft = Offset(x + 8f, y + 10f),
        size = Size(width, height),
        cornerRadius = CornerRadius(18f, 18f)
    )

    // 2. Midnight Obsidian / Carbon Fiber Outer Frame
    val obsidianGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1E222D),
            Color(0xFF10131A),
            Color(0xFF1A1F2B),
            Color(0xFF090B0F)
        ),
        start = Offset(x, y),
        end = Offset(x + width, y + height)
    )

    drawRoundRect(
        brush = obsidianGradient,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(16f, 16f)
    )

    // 3. 3D Outer Bevel / Chamfer (Cyan & Gold ambient neon edges)
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.45f), Color(0xFFFFD700).copy(alpha = 0.45f)),
            startX = x,
            endX = x + width
        ),
        topLeft = Offset(x + 1.5f, y + 1.5f),
        size = Size(width - 3f, height - 3f),
        cornerRadius = CornerRadius(15f, 15f),
        style = Stroke(width = 2.2f)
    )

    drawRoundRect(
        color = Color(0xFF06070A),
        topLeft = Offset(x + borderThickness - 2f, y + borderThickness - 2f),
        size = Size(width - (borderThickness * 2) + 4f, height - (borderThickness * 2) + 4f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 3.5f)
    )

    // 4. Regal 24K Gold Inlay Groove along Frame
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFD700), Color(0xFFB8860B), Color(0xFFFFECB3), Color(0xFF996515)),
            start = Offset(x, y),
            end = Offset(x + width, y + height)
        ),
        topLeft = Offset(x + (borderThickness * 0.45f), y + (borderThickness * 0.45f)),
        size = Size(width - (borderThickness * 0.9f), height - (borderThickness * 0.9f)),
        cornerRadius = CornerRadius(12f, 12f),
        style = Stroke(width = 1.6f)
    )
}

private fun DrawScope.drawDragonPlayfields(
    leftX: Float,
    rightX: Float,
    topY: Float,
    halfW: Float,
    height: Float,
    barX: Float,
    barW: Float,
    bearOffX: Float,
    bearOffW: Float,
    dragonAuraAlpha: Float
) {
    // Left Quad: Deep midnight blue velvet with cyan undertone
    val leftGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFF0B1928),
            Color(0xFF070F19),
            Color(0xFF04080F)
        ),
        center = Offset(leftX + (halfW / 2f), topY + (height / 2f)),
        radius = halfW * 0.95f
    )
    drawRect(brush = leftGradient, topLeft = Offset(leftX, topY), size = Size(halfW, height))

    // Right Quad: Deep midnight obsidian with warm amber undertone
    val rightGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFF1E160C),
            Color(0xFF120D07),
            Color(0xFF0A0704)
        ),
        center = Offset(rightX + (halfW / 2f), topY + (height / 2f)),
        radius = halfW * 0.95f
    )
    drawRect(brush = rightGradient, topLeft = Offset(rightX, topY), size = Size(halfW, height))

    // Bear-off Tray (Dark obsidian tray with gold glow)
    val trayGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF0A0B10), Color(0xFF131520), Color(0xFF08090C)),
        startX = bearOffX,
        endX = bearOffX + bearOffW
    )
    drawRect(brush = trayGradient, topLeft = Offset(bearOffX, topY), size = Size(bearOffW, height))
    drawRect(
        color = Color(0xFFFFD700).copy(alpha = 0.25f),
        topLeft = Offset(bearOffX, topY),
        size = Size(bearOffW, height),
        style = Stroke(width = 1.2f)
    )

    // Inner shadow border on playfields
    drawRect(
        color = Color.Black.copy(alpha = 0.60f),
        topLeft = Offset(leftX, topY),
        size = Size(halfW, height),
        style = Stroke(width = 4f)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.60f),
        topLeft = Offset(rightX, topY),
        size = Size(halfW, height),
        style = Stroke(width = 4f)
    )

    // =======================================================
    // 🐉 LEFT QUAD: GLOWING CYAN ELECTRIC DRAGON ENGRAVING
    // =======================================================
    val leftCenterX = leftX + (halfW / 2f)
    val centerY = topY + (height / 2f)
    val dragonRadius = min(halfW * 0.44f, height * 0.32f)

    drawCyanDragonEmblem(
        cx = leftCenterX,
        cy = centerY,
        radius = dragonRadius,
        auraAlpha = dragonAuraAlpha
    )

    // =======================================================
    // 🐉 RIGHT QUAD: RADIANT 24K IMPERIAL GOLD DRAGON ENGRAVING
    // =======================================================
    val rightCenterX = rightX + (halfW / 2f)
    drawGoldDragonEmblem(
        cx = rightCenterX,
        cy = centerY,
        radius = dragonRadius,
        auraAlpha = dragonAuraAlpha
    )
}

/**
 * Procedural Mythic Cyan Dragon Emblem etched onto the left quadrant
 */
private fun DrawScope.drawCyanDragonEmblem(
    cx: Float,
    cy: Float,
    radius: Float,
    auraAlpha: Float
) {
    val cyanGlow = Color(0xFF00E5FF)
    val cyanDeep = Color(0xFF00B0FF)
    val cyanLight = Color(0xFF80D8FF)

    // 1. Ambient Celestial Rune Rings
    drawCircle(
        color = cyanGlow.copy(alpha = auraAlpha * 0.22f),
        radius = radius * 1.15f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )
    drawCircle(
        color = cyanDeep.copy(alpha = auraAlpha * 0.35f),
        radius = radius * 0.95f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.8f)
    )
    drawCircle(
        color = cyanLight.copy(alpha = auraAlpha * 0.15f),
        radius = radius * 0.75f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.2f)
    )

    // 2. Serpentine Dragon Coils (Bezier Paths)
    val spinePath = Path().apply {
        // Upper dragon body arch
        moveTo(cx - (radius * 0.70f), cy + (radius * 0.20f))
        cubicTo(
            cx - (radius * 0.85f), cy - (radius * 0.75f),
            cx - (radius * 0.15f), cy - (radius * 0.90f),
            cx + (radius * 0.35f), cy - (radius * 0.45f)
        )
        // Mid body twist
        cubicTo(
            cx + (radius * 0.70f), cy - (radius * 0.10f),
            cx + (radius * 0.40f), cy + (radius * 0.65f),
            cx - (radius * 0.10f), cy + (radius * 0.55f)
        )
        // Tail curl
        cubicTo(
            cx - (radius * 0.45f), cy + (radius * 0.50f),
            cx - (radius * 0.55f), cy + (radius * 0.15f),
            cx - (radius * 0.25f), cy + (radius * 0.05f)
        )
    }

    // Outer glow of dragon body
    drawPath(path = spinePath, color = cyanDeep.copy(alpha = auraAlpha * 0.45f), style = Stroke(width = 8f))
    // Core body stroke
    drawPath(path = spinePath, color = cyanGlow.copy(alpha = 0.85f), style = Stroke(width = 3.5f))
    drawPath(path = spinePath, color = Color.White.copy(alpha = 0.90f), style = Stroke(width = 1.5f))

    // 3. Dragon Head (Top-Right / Center)
    val headX = cx + (radius * 0.35f)
    val headY = cy - (radius * 0.45f)

    val headPath = Path().apply {
        moveTo(headX, headY)
        lineTo(headX + (radius * 0.32f), headY - (radius * 0.12f)) // Snout
        lineTo(headX + (radius * 0.28f), headY + (radius * 0.08f)) // Lower jaw
        lineTo(headX + (radius * 0.10f), headY + (radius * 0.12f))
        close()
    }
    drawPath(path = headPath, color = cyanDeep.copy(alpha = 0.75f), style = Fill)
    drawPath(path = headPath, color = cyanLight, style = Stroke(width = 1.8f))

    // Piercing Dragon Eye
    drawCircle(color = Color.White, radius = 3.2f, center = Offset(headX + (radius * 0.16f), headY - (radius * 0.02f)))
    drawCircle(color = cyanGlow, radius = 5.5f, center = Offset(headX + (radius * 0.16f), headY - (radius * 0.02f)), style = Stroke(width = 1.2f))

    // Dragon Horns
    val hornPath = Path().apply {
        moveTo(headX, headY)
        cubicTo(
            headX - (radius * 0.15f), headY - (radius * 0.35f),
            headX + (radius * 0.05f), headY - (radius * 0.50f),
            headX + (radius * 0.15f), headY - (radius * 0.42f)
        )
    }
    drawPath(path = hornPath, color = cyanGlow, style = Stroke(width = 2.5f))

    // 4. Mystical Flaming Pearl / Dragon Orb in Center
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, cyanGlow, Color.Transparent),
            center = Offset(cx, cy),
            radius = radius * 0.32f
        ),
        radius = radius * 0.32f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = cyanLight,
        radius = radius * 0.16f,
        center = Offset(cx, cy),
        style = Stroke(width = 2.2f)
    )

    // Dragon Claws clutching the orb
    for (ang in listOf(-45.0, 45.0, 135.0, 225.0)) {
        val rad = Math.toRadians(ang)
        val clawStartX = cx + (cos(rad) * radius * 0.16f).toFloat()
        val clawStartY = cy + (sin(rad) * radius * 0.16f).toFloat()
        val clawEndX = cx + (cos(rad) * radius * 0.34f).toFloat()
        val clawEndY = cy + (sin(rad) * radius * 0.34f).toFloat()
        drawLine(color = cyanGlow, start = Offset(clawStartX, clawStartY), end = Offset(clawEndX, clawEndY), strokeWidth = 2.2f)
    }
}

/**
 * Procedural Imperial 24K Gold Dragon Emblem etched onto the right quadrant
 */
private fun DrawScope.drawGoldDragonEmblem(
    cx: Float,
    cy: Float,
    radius: Float,
    auraAlpha: Float
) {
    val goldGlow = Color(0xFFFFD700)
    val goldDeep = Color(0xFFFFA000)
    val goldLight = Color(0xFFFFF176)

    // 1. Ambient Celestial Solar Rings
    drawCircle(
        color = goldGlow.copy(alpha = auraAlpha * 0.22f),
        radius = radius * 1.15f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )
    drawCircle(
        color = goldDeep.copy(alpha = auraAlpha * 0.35f),
        radius = radius * 0.95f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.8f)
    )
    drawCircle(
        color = goldLight.copy(alpha = auraAlpha * 0.15f),
        radius = radius * 0.75f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.2f)
    )

    // 2. Serpentine Dragon Coils (Mirror of left)
    val spinePath = Path().apply {
        moveTo(cx + (radius * 0.70f), cy + (radius * 0.20f))
        cubicTo(
            cx + (radius * 0.85f), cy - (radius * 0.75f),
            cx + (radius * 0.15f), cy - (radius * 0.90f),
            cx - (radius * 0.35f), cy - (radius * 0.45f)
        )
        cubicTo(
            cx - (radius * 0.70f), cy - (radius * 0.10f),
            cx - (radius * 0.40f), cy + (radius * 0.65f),
            cx + (radius * 0.10f), cy + (radius * 0.55f)
        )
        cubicTo(
            cx + (radius * 0.45f), cy + (radius * 0.50f),
            cx + (radius * 0.55f), cy + (radius * 0.15f),
            cx + (radius * 0.25f), cy + (radius * 0.05f)
        )
    }

    drawPath(path = spinePath, color = goldDeep.copy(alpha = auraAlpha * 0.45f), style = Stroke(width = 8f))
    drawPath(path = spinePath, color = goldGlow.copy(alpha = 0.85f), style = Stroke(width = 3.5f))
    drawPath(path = spinePath, color = Color.White.copy(alpha = 0.90f), style = Stroke(width = 1.5f))

    // 3. Golden Dragon Head (Top-Left / Center)
    val headX = cx - (radius * 0.35f)
    val headY = cy - (radius * 0.45f)

    val headPath = Path().apply {
        moveTo(headX, headY)
        lineTo(headX - (radius * 0.32f), headY - (radius * 0.12f))
        lineTo(headX - (radius * 0.28f), headY + (radius * 0.08f))
        lineTo(headX - (radius * 0.10f), headY + (radius * 0.12f))
        close()
    }
    drawPath(path = headPath, color = goldDeep.copy(alpha = 0.75f), style = Fill)
    drawPath(path = headPath, color = goldLight, style = Stroke(width = 1.8f))

    // Piercing Dragon Eye
    drawCircle(color = Color.White, radius = 3.2f, center = Offset(headX - (radius * 0.16f), headY - (radius * 0.02f)))
    drawCircle(color = goldGlow, radius = 5.5f, center = Offset(headX - (radius * 0.16f), headY - (radius * 0.02f)), style = Stroke(width = 1.2f))

    // Dragon Horns
    val hornPath = Path().apply {
        moveTo(headX, headY)
        cubicTo(
            headX + (radius * 0.15f), headY - (radius * 0.35f),
            headX - (radius * 0.05f), headY - (radius * 0.50f),
            headX - (radius * 0.15f), headY - (radius * 0.42f)
        )
    }
    drawPath(path = hornPath, color = goldGlow, style = Stroke(width = 2.5f))

    // 4. Flaming Golden Sunburst Pearl in Center
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, goldGlow, Color.Transparent),
            center = Offset(cx, cy),
            radius = radius * 0.32f
        ),
        radius = radius * 0.32f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = goldLight,
        radius = radius * 0.16f,
        center = Offset(cx, cy),
        style = Stroke(width = 2.2f)
    )

    // Dragon Claws clutching the orb
    for (ang in listOf(-45.0, 45.0, 135.0, 225.0)) {
        val rad = Math.toRadians(ang)
        val clawStartX = cx + (cos(rad) * radius * 0.16f).toFloat()
        val clawStartY = cy + (sin(rad) * radius * 0.16f).toFloat()
        val clawEndX = cx + (cos(rad) * radius * 0.34f).toFloat()
        val clawEndY = cy + (sin(rad) * radius * 0.34f).toFloat()
        drawLine(color = goldGlow, start = Offset(clawStartX, clawStartY), end = Offset(clawEndX, clawEndY), strokeWidth = 2.2f)
    }
}

private fun DrawScope.drawInlaidDragonTriangle(
    xStart: Float,
    yStart: Float,
    width: Float,
    height: Float,
    isTop: Boolean,
    isCyanNeon: Boolean
) {
    val tipX = xStart + (width / 2f)
    val tipY = if (isTop) yStart + height else yStart - height

    val path = Path().apply {
        moveTo(xStart, yStart)
        lineTo(xStart + width, yStart)
        lineTo(tipX, tipY)
        close()
    }

    // Inlaid obsidian body with subtle gradient
    val baseGradient = if (isCyanNeon) {
        Brush.verticalGradient(
            colors = if (isTop) listOf(Color(0xFF0F1E2E), Color(0xFF07101B)) else listOf(Color(0xFF07101B), Color(0xFF0F1E2E)),
            startY = if (isTop) yStart else tipY,
            endY = if (isTop) tipY else yStart
        )
    } else {
        Brush.verticalGradient(
            colors = if (isTop) listOf(Color(0xFF261D12), Color(0xFF130E08)) else listOf(Color(0xFF130E08), Color(0xFF261D12)),
            startY = if (isTop) yStart else tipY,
            endY = if (isTop) tipY else yStart
        )
    }
    drawPath(path = path, brush = baseGradient)

    // Glowing Neon Edge Lines (Cyan vs Gold)
    val neonEdgeColor = if (isCyanNeon) Color(0xFF00E5FF).copy(alpha = 0.65f) else Color(0xFFFFD700).copy(alpha = 0.65f)
    drawPath(path = path, color = neonEdgeColor, style = Stroke(width = 1.4f))

    // Sharp 3D needle tip highlight
    val tipRadius = 2.5f
    drawCircle(
        color = if (isCyanNeon) Color(0xFF80D8FF) else Color(0xFFFFF59D),
        radius = tipRadius,
        center = Offset(tipX, tipY)
    )
}

private fun DrawScope.drawDynamicPointHighlight(
    xStart: Float,
    yStart: Float,
    width: Float,
    height: Float,
    isTop: Boolean,
    glowColor: Color,
    alpha: Float,
    glowRadius: Float
) {
    val tipX = xStart + (width / 2f)
    val tipY = if (isTop) yStart + height else yStart - height

    val path = Path().apply {
        moveTo(xStart, yStart)
        lineTo(xStart + width, yStart)
        lineTo(tipX, tipY)
        close()
    }

    // Radiant Glowing Cone
    drawPath(
        path = path,
        color = glowColor.copy(alpha = alpha * 0.40f)
    )

    // Pulsing Neon Border
    drawPath(
        path = path,
        color = glowColor.copy(alpha = alpha),
        style = Stroke(width = 2.6f)
    )

    // Target Diamond Beacon at needle tip
    val beaconY = if (isTop) tipY - 8f else tipY + 8f
    val diamondPath = Path().apply {
        moveTo(tipX, beaconY - 6f)
        lineTo(tipX + 5f, beaconY)
        lineTo(tipX, beaconY + 6f)
        lineTo(tipX - 5f, beaconY)
        close()
    }
    drawPath(path = diamondPath, color = glowColor.copy(alpha = alpha))
    drawPath(path = diamondPath, color = Color.White, style = Stroke(width = 1.4f))
}

private fun DrawScope.drawCenterBarAndDragonHinges(
    barX: Float,
    barY: Float,
    barW: Float,
    barH: Float,
    doublingValue: Int
) {
    // 1. Center Obsidian Bar
    val barGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF0D0F14),
            Color(0xFF1B202C),
            Color(0xFF222938),
            Color(0xFF0D0F14)
        ),
        startX = barX,
        endX = barX + barW
    )
    drawRect(brush = barGradient, topLeft = Offset(barX, barY), size = Size(barW, barH))

    // Bar Bevel Borders
    drawLine(color = Color(0xFF00E5FF).copy(alpha = 0.5f), start = Offset(barX, barY), end = Offset(barX, barY + barH), strokeWidth = 2f)
    drawLine(color = Color(0xFFFFD700).copy(alpha = 0.5f), start = Offset(barX + barW, barY), end = Offset(barX + barW, barY + barH), strokeWidth = 2f)

    // 2. Golden Dragon Spine Hinges (Top & Bottom)
    val hingeYPositions = listOf(
        barY + (barH * 0.14f),
        barY + (barH * 0.86f)
    )

    for (hy in hingeYPositions) {
        val hingeW = barW * 0.78f
        val hingeH = barH * 0.052f
        val hx = barX + ((barW - hingeW) / 2f)

        val brassGradient = Brush.linearGradient(
            colors = listOf(Color(0xFFFFF0B2), Color(0xFFFFD700), Color(0xFFB8860B), Color(0xFFFFF59D)),
            start = Offset(hx, hy),
            end = Offset(hx + hingeW, hy + hingeH)
        )
        drawRoundRect(
            brush = brassGradient,
            topLeft = Offset(hx, hy),
            size = Size(hingeW, hingeH),
            cornerRadius = CornerRadius(5f, 5f)
        )
        drawRoundRect(
            color = Color(0xFF59430A),
            topLeft = Offset(hx, hy),
            size = Size(hingeW, hingeH),
            cornerRadius = CornerRadius(5f, 5f),
            style = Stroke(width = 1.2f)
        )

        // Brass Screws
        drawCircle(color = Color(0xFF3E2D07), radius = 2.2f, center = Offset(hx + (hingeW * 0.25f), hy + (hingeH / 2f)))
        drawCircle(color = Color(0xFF3E2D07), radius = 2.2f, center = Offset(hx + (hingeW * 0.75f), hy + (hingeH / 2f)))
    }

    // 3. Glowing Doubling Cube in the exact center of the bar! (Matching user screenshot: Glowing Blue "64" Cube)
    val cubeCenterY = barY + (barH / 2f)
    val cubeCenterX = barX + (barW / 2f)
    val cubeSize = min(barW * 0.85f, 32f)

    draw3DGlowingDoublingCube(
        cx = cubeCenterX,
        cy = cubeCenterY,
        size = cubeSize,
        value = doublingValue
    )
}

/**
 * 3D Glowing Blue Doubling Cube displayed on the center divider
 */
private fun DrawScope.draw3DGlowingDoublingCube(
    cx: Float,
    cy: Float,
    size: Float,
    value: Int
) {
    val half = size / 2f
    val cubeRect = Rect(cx - half, cy - half, cx + half, cy + half)

    // Soft Blue Neon Ambient Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.55f), Color.Transparent),
            center = Offset(cx, cy),
            radius = size * 1.35f
        ),
        radius = size * 1.35f,
        center = Offset(cx, cy)
    )

    // Cube Body: Electric Cyan-Blue with metallic depth
    val cubeBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF00B0FF), Color(0xFF0D47A1), Color(0xFF012B6B)),
        start = Offset(cubeRect.left, cubeRect.top),
        end = Offset(cubeRect.right, cubeRect.bottom)
    )
    drawRoundRect(
        brush = cubeBrush,
        topLeft = Offset(cubeRect.left, cubeRect.top),
        size = Size(size, size),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Glowing Neon Cyan Border
    drawRoundRect(
        color = Color(0xFF00E5FF),
        topLeft = Offset(cubeRect.left, cubeRect.top),
        size = Size(size, size),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 1.8f)
    )

    // Chamfer highlights
    drawLine(color = Color.White.copy(alpha = 0.85f), start = Offset(cubeRect.left + 2f, cubeRect.top + 2f), end = Offset(cubeRect.right - 2f, cubeRect.top + 2f), strokeWidth = 1.2f)
}

private fun DrawScope.drawDragonCornerBrackets(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    bracketSize: Float
) {
    val brassGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFECB3), Color(0xFFFFD700), Color(0xFFB8860B), Color(0xFFFFF9C4)),
        start = Offset(x, y),
        end = Offset(x + bracketSize, y + bracketSize)
    )

    val corners = listOf(
        Offset(x, y),
        Offset(x + w, y),
        Offset(x, y + h),
        Offset(x + w, y + h)
    )

    for (c in corners) {
        val path = Path().apply {
            val signX = if (c.x > x + (w / 2f)) -1f else 1f
            val signY = if (c.y > y + (h / 2f)) -1f else 1f

            moveTo(c.x, c.y)
            lineTo(c.x + (signX * bracketSize), c.y)
            lineTo(c.x + (signX * bracketSize * 0.65f), c.y + (signY * bracketSize * 0.35f))
            lineTo(c.x + (signX * bracketSize * 0.35f), c.y + (signY * bracketSize * 0.65f))
            lineTo(c.x, c.y + (signY * bracketSize))
            close()
        }

        drawPath(path = path, brush = brassGradient)
        drawPath(path = path, color = Color(0xFF523E08), style = Stroke(width = 1.2f))

        // Corner Gold Rivet
        val rivetCenter = Offset(
            c.x + (if (c.x > x + (w / 2f)) -bracketSize * 0.35f else bracketSize * 0.35f),
            c.y + (if (c.y > y + (h / 2f)) -bracketSize * 0.35f else bracketSize * 0.35f)
        )
        drawCircle(color = Color(0xFF3B2C04), radius = 3f, center = rivetCenter)
        drawCircle(color = Color(0xFFFFECB3), radius = 1.5f, center = rivetCenter - Offset(0.8f, 0.8f))
    }
}

// -------------------------------------------------------------
// MASTER 3D DRAGON CHECKER PIECES (OBSIDIAN CYAN & 24K GOLD)
// -------------------------------------------------------------
private fun DrawScope.drawMasterDragonChecker(
    center: Offset,
    radius: Float,
    playerColor: PlayerColor,
    isSelected: Boolean,
    glowColor: Color,
    highlightAlpha: Float
) {
    val safeRadius = max(4f, radius)
    val isCyanPiece = (playerColor == PlayerColor.WHITE)

    // 1. Soft Realistic Drop Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.70f),
        radius = safeRadius * 1.08f,
        center = center + Offset(2.5f, 4f)
    )

    // 2. Base 3D Body Gradient
    if (isCyanPiece) {
        // Metallic Obsidian Blue/Black body
        val obsidianGradient = Brush.radialGradient(
            colors = listOf(
                Color(0xFF2C394F),
                Color(0xFF192233),
                Color(0xFF0F1522),
                Color(0xFF080C14),
                Color(0xFF030508)
            ),
            center = center - Offset(safeRadius * 0.32f, safeRadius * 0.32f),
            radius = max(1f, safeRadius * 1.35f)
        )
        drawCircle(brush = obsidianGradient, radius = safeRadius, center = center)

        // Glowing Electric Cyan Outer Rim
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = safeRadius - 0.8f,
            center = center,
            style = Stroke(width = 2.0f)
        )

        // Inner Concentric Ridge (Polished Silver/Cyan)
        drawCircle(
            color = Color(0xFF80D8FF).copy(alpha = 0.75f),
            radius = max(1f, safeRadius * 0.65f),
            center = center,
            style = Stroke(width = 1.8f)
        )

        // Inset Center Core
        val coreBrush = Brush.radialGradient(
            colors = listOf(Color(0xFF142033), Color(0xFF080E18)),
            center = center,
            radius = max(1f, safeRadius * 0.45f)
        )
        drawCircle(brush = coreBrush, radius = max(1f, safeRadius * 0.45f), center = center)

        // Embossed Cyan Dragon Crest in center!
        drawCheckerDragonCrest(center = center, size = safeRadius * 0.50f, color = Color(0xFF00E5FF))

        // Polished Specular Highlight
        val specular = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.65f), Color.Transparent),
            center = center - Offset(safeRadius * 0.42f, safeRadius * 0.42f),
            radius = max(1f, safeRadius * 0.65f)
        )
        drawCircle(brush = specular, radius = max(1f, safeRadius * 0.75f), center = center)

    } else {
        // Polished 24K Pure Gold body
        val goldGradient = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF9C4),
                Color(0xFFFFEE58),
                Color(0xFFFFD700),
                Color(0xFFD4AF37),
                Color(0xFF8C6D1F),
                Color(0xFF5A440A)
            ),
            center = center - Offset(safeRadius * 0.32f, safeRadius * 0.32f),
            radius = max(1f, safeRadius * 1.35f)
        )
        drawCircle(brush = goldGradient, radius = safeRadius, center = center)

        // Radiant 24K Golden Outer Rim
        drawCircle(
            color = Color(0xFFFFF176),
            radius = safeRadius - 0.8f,
            center = center,
            style = Stroke(width = 2.0f)
        )

        // Inner Concentric Ridge (Deep Dark Gold)
        drawCircle(
            color = Color(0xFF996515),
            radius = max(1f, safeRadius * 0.65f),
            center = center,
            style = Stroke(width = 2.0f)
        )

        // Inset Center Core
        val coreBrush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFD700), Color(0xFFB8860B)),
            center = center,
            radius = max(1f, safeRadius * 0.45f)
        )
        drawCircle(brush = coreBrush, radius = max(1f, safeRadius * 0.45f), center = center)

        // Embossed Imperial Dragon Crest in center!
        drawCheckerDragonCrest(center = center, size = safeRadius * 0.50f, color = Color(0xFF4A3403))

        // Polished Specular Highlight
        val specular = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.75f), Color.Transparent),
            center = center - Offset(safeRadius * 0.42f, safeRadius * 0.42f),
            radius = max(1f, safeRadius * 0.65f)
        )
        drawCircle(brush = specular, radius = max(1f, safeRadius * 0.75f), center = center)
    }

    // Selected Glowing Ring Effect
    if (isSelected) {
        val selectGlow = if (isCyanPiece) Color(0xFF00E5FF) else Color(0xFFFFD700)
        drawCircle(
            color = selectGlow.copy(alpha = highlightAlpha),
            radius = safeRadius * 1.30f,
            center = center,
            style = Stroke(width = 3.5f)
        )
        drawCircle(
            color = Color.White,
            radius = safeRadius * 1.15f,
            center = center,
            style = Stroke(width = 1.6f)
        )
    }
}

/**
 * Draws a detailed miniature embossed Dragon Crest in the center of the checker piece
 */
private fun DrawScope.drawCheckerDragonCrest(
    center: Offset,
    size: Float,
    color: Color
) {
    val half = size / 2f
    val crestPath = Path().apply {
        // Stylized Dragon Head & Wing silhouette
        moveTo(center.x, center.y - half)
        cubicTo(center.x + (half * 0.7f), center.y - (half * 0.4f), center.x + half, center.y + (half * 0.2f), center.x + (half * 0.3f), center.y + half)
        lineTo(center.x, center.y + (half * 0.5f))
        lineTo(center.x - (half * 0.3f), center.y + half)
        cubicTo(center.x - half, center.y + (half * 0.2f), center.x - (half * 0.7f), center.y - (half * 0.4f), center.x, center.y - half)
        close()
    }
    drawPath(path = crestPath, color = color.copy(alpha = 0.85f), style = Stroke(width = 1.4f))
    drawCircle(color = color, radius = 1.5f, center = center)
}

// -------------------------------------------------------------
// TAP DETECTOR GEOMETRY
// -------------------------------------------------------------
private fun calculateClickedPoint(offset: Offset, w: Float, h: Float): Int? {
    val frameMarginX = w * 0.022f
    val frameMarginY = h * 0.028f
    val frameW = w - (frameMarginX * 2)
    val frameH = h - (frameMarginY * 2)

    val frameBorderThickness = min(frameW * 0.038f, frameH * 0.068f)
    val innerPlayX = frameMarginX + frameBorderThickness
    val innerPlayY = frameMarginY + frameBorderThickness
    val innerPlayW = frameW - (frameBorderThickness * 2)
    val innerPlayH = frameH - (frameBorderThickness * 2)

    val bearOffTrayW = innerPlayW * 0.075f
    val playableAreaW = innerPlayW - bearOffTrayW
    val barW = playableAreaW * 0.082f
    val halfPlayW = (playableAreaW - barW) / 2f
    val pointW = halfPlayW / 6f

    val leftQuadX = innerPlayX
    val barX = leftQuadX + halfPlayW
    val rightQuadX = barX + barW
    val bearOffX = rightQuadX + halfPlayW

    val x = offset.x
    val y = offset.y

    // Bar Click
    if (x >= barX && x <= barX + barW) {
        return if (y > h / 2f) 25 else 0
    }

    // Right Bear-off Click
    if (x >= bearOffX) {
        return if (y > h / 2f) 0 else 25
    }

    val isTop = (y <= h / 2f)

    if (x >= leftQuadX && x < barX) {
        val col = ((x - leftQuadX) / pointW).toInt().coerceIn(0, 5)
        return if (isTop) 13 + col else 12 - col
    } else if (x >= rightQuadX && x < bearOffX) {
        val col = ((x - rightQuadX) / pointW).toInt().coerceIn(0, 5)
        return if (isTop) 19 + col else 6 - col
    }

    return null
}
