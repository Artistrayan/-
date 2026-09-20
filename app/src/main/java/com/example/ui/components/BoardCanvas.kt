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
import androidx.compose.ui.geometry.RoundRect
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
import kotlin.math.max
import kotlin.math.min

/**
 * AAA Master Ultra-Realistic 3D Backgammon Board Canvas
 * Procedurally rendered with luxury mahogany/walnut wood grains,
 * precision-inlaid ivory/ebony triangles, polished brass hinges,
 * and high-gloss 3D beveled checkers.
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

    // Pulse animation for landing target highlights
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val highlightAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val highlightGlowRadius by infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 6f,
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

        val frameBorderThickness = min(frameW * 0.035f, frameH * 0.065f)
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
        // 2. LUXURY MAHOGANY / WALNUT OUTER WOOD CASING
        // ==========================================================
        drawLuxuryWoodCasing(
            x = frameMarginX,
            y = frameMarginY,
            width = frameW,
            height = frameH,
            borderThickness = frameBorderThickness
        )

        // ==========================================================
        // 3. INLAID PLAYFIELDS (LEFT QUAD & RIGHT QUAD)
        // ==========================================================
        drawPlayfields(
            leftX = leftQuadX,
            rightX = rightQuadX,
            topY = topY,
            halfW = halfPlayW,
            height = innerPlayH,
            barX = barX,
            barW = barW,
            bearOffX = bearOffX,
            bearOffW = bearOffTrayW
        )

        // ==========================================================
        // 4. PRECISION INLAID TRIANGLES (POINTS 1..24)
        // ==========================================================
        val validTargetPoints = state.highlightedMoves.map { it.to }.toSet()
        val glowGold = Color(0xFFFFD700)
        val glowCyan = Color(0xFF00E5FF)

        // Palette for Inlaid Triangles
        val ivoryColor1 = Color(0xFFFFF7E6)
        val ivoryColor2 = Color(0xFFEADBBE)
        val darkWoodColor1 = Color(0xFF5A2A18)
        val darkWoodColor2 = Color(0xFF38150A)

        // Top Row: Points 13..18 (Left) & 19..24 (Right)
        val topPoints = listOf(13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24)
        for (i in topPoints.indices) {
            val ptIdx = topPoints[i]
            val xStart = if (i < 6) leftQuadX + (i * pointW) else rightQuadX + ((i - 6) * pointW)
            val isDark = (ptIdx % 2 == 1)
            val isTarget = validTargetPoints.contains(ptIdx)
            val isSource = (state.selectedPoint == ptIdx)

            drawInlaidTriangle(
                xStart = xStart,
                yStart = topY,
                width = pointW,
                height = pointH,
                isTop = true,
                colorTop = if (isDark) darkWoodColor1 else ivoryColor1,
                colorBottom = if (isDark) darkWoodColor2 else ivoryColor2,
                isDark = isDark
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

            // Elegant Point Number
            drawText(
                textMeasurer = textMeasurer,
                text = "$ptIdx",
                style = TextStyle(
                    color = if (isTarget) glowGold else Color(0xFFD4AF37).copy(alpha = 0.55f),
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

            drawInlaidTriangle(
                xStart = xStart,
                yStart = bottomY,
                width = pointW,
                height = pointH,
                isTop = false,
                colorTop = if (isDark) darkWoodColor1 else ivoryColor1,
                colorBottom = if (isDark) darkWoodColor2 else ivoryColor2,
                isDark = isDark
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

            // Elegant Point Number
            drawText(
                textMeasurer = textMeasurer,
                text = "$ptIdx",
                style = TextStyle(
                    color = if (isTarget) glowGold else Color(0xFFD4AF37).copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                ),
                topLeft = Offset(xStart + (pointW / 2f) - 6f, bottomY - 16f)
            )
        }

        // ==========================================================
        // 5. CENTER WOODEN BAR & SOLID BRASS HINGES
        // ==========================================================
        drawCenterBarAndBrassHinges(
            barX = barX,
            barY = topY,
            barW = barW,
            barH = innerPlayH
        )

        // ==========================================================
        // 6. SOLID BRASS CORNER BRACKETS (ALL 4 CORNERS)
        // ==========================================================
        drawBrassCornerBrackets(
            x = frameMarginX,
            y = frameMarginY,
            w = frameW,
            h = frameH,
            bracketSize = frameBorderThickness * 1.5f
        )

        // ==========================================================
        // 7. 3D REALISTIC CHECKERS (PEARL IVORY & OBSIDIAN EBONY)
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

                drawMaster3DChecker(
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
                    color = Color(0xFF100E17).copy(alpha = 0.90f),
                    radius = max(2f, checkerRadius * 0.68f),
                    center = Offset(xCenter, lastY)
                )
                drawCircle(
                    color = glowGold,
                    radius = max(2f, checkerRadius * 0.68f),
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
        // 8. BAR CHECKERS (Hit Pieces on Center Wooden Divider)
        // ==========================================================
        val barXCenter = barX + (barW / 2f)

        // White Bar (Bottom)
        if (state.barWhite > 0) {
            val isBarSelected = (state.selectedPoint == 25)
            for (c in 0 until min(state.barWhite, 4)) {
                val yC = (h / 2f) + (checkerRadius * 1.5f) + (c * checkerRadius * 1.75f)
                drawMaster3DChecker(
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
                    style = TextStyle(color = glowGold, fontSize = 11.sp, fontWeight = FontWeight.Black),
                    topLeft = Offset(barXCenter - 4f, (h / 2f) + (checkerRadius * 0.6f))
                )
            }
        }

        // Black Bar (Top)
        if (state.barBlack > 0) {
            val isBarSelected = (state.selectedPoint == 0)
            for (c in 0 until min(state.barBlack, 4)) {
                val yC = (h / 2f) - (checkerRadius * 1.5f) - (c * checkerRadius * 1.75f)
                drawMaster3DChecker(
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
                color = glowGold.copy(alpha = highlightAlpha * 0.35f),
                topLeft = Offset(bearOffX + 4f, bottomY - (innerPlayH * 0.45f)),
                size = Size(bearOffTrayW - 8f, innerPlayH * 0.42f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = glowGold.copy(alpha = highlightAlpha),
                topLeft = Offset(bearOffX + 4f, bottomY - (innerPlayH * 0.45f)),
                size = Size(bearOffTrayW - 8f, innerPlayH * 0.42f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2.5f)
            )
        }
        if (state.offWhite > 0) {
            drawMaster3DChecker(
                center = Offset(trayXCenter, whiteTrayY),
                radius = max(3f, checkerRadius * 0.85f),
                playerColor = PlayerColor.WHITE,
                isSelected = isBearOffWhite,
                glowColor = glowGold,
                highlightAlpha = highlightAlpha
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "${state.offWhite} خروج",
                style = TextStyle(color = glowGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
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
            drawMaster3DChecker(
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
                style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                topLeft = Offset(trayXCenter - 14f, blackTrayY - (checkerRadius * 1.6f))
            )
        }
    }
}

// -------------------------------------------------------------
// DRAWING HELPERS: 3D WOOD CASING, INLAYS, HINGES & CHECKERS
// -------------------------------------------------------------

private fun DrawScope.drawLuxuryWoodCasing(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    borderThickness: Float
) {
    // 1. Deep Cast Drop Shadow behind the Board
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.85f),
        topLeft = Offset(x + 6f, y + 8f),
        size = Size(width, height),
        cornerRadius = CornerRadius(16f, 16f)
    )

    // 2. Rich Multi-tone Mahogany Wood Frame
    val woodGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4A1E11), // Deep Mahogany
            Color(0xFF2C0F08), // Dark Walnut
            Color(0xFF5E2716), // Warm Teak highlight
            Color(0xFF2A0D07)  // Deep shadow edge
        ),
        start = Offset(x, y),
        end = Offset(x + width, y + height)
    )

    drawRoundRect(
        brush = woodGradient,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // 3. 3D Outer Bevel / Chamfer (Top-Left Highlight & Bottom-Right Shadow)
    drawRoundRect(
        color = Color(0xFF9E5336).copy(alpha = 0.75f),
        topLeft = Offset(x + 1.5f, y + 1.5f),
        size = Size(width - 3f, height - 3f),
        cornerRadius = CornerRadius(13f, 13f),
        style = Stroke(width = 2.5f)
    )

    drawRoundRect(
        color = Color(0xFF120503).copy(alpha = 0.90f),
        topLeft = Offset(x + borderThickness - 2f, y + borderThickness - 2f),
        size = Size(width - (borderThickness * 2) + 4f, height - (borderThickness * 2) + 4f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 3.5f)
    )

    // 4. Gold Trim Inlay Line along Frame
    drawRoundRect(
        color = Color(0xFFD4AF37).copy(alpha = 0.45f),
        topLeft = Offset(x + (borderThickness * 0.45f), y + (borderThickness * 0.45f)),
        size = Size(width - (borderThickness * 0.9f), height - (borderThickness * 0.9f)),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 1.4f)
    )
}

private fun DrawScope.drawPlayfields(
    leftX: Float,
    rightX: Float,
    topY: Float,
    halfW: Float,
    height: Float,
    barX: Float,
    barW: Float,
    bearOffX: Float,
    bearOffW: Float
) {
    val playfieldGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF231109),
            Color(0xFF381C0E),
            Color(0xFF2E170B),
            Color(0xFF1F0D07)
        ),
        startY = topY,
        endY = topY + height
    )

    // Left Quad
    drawRect(brush = playfieldGradient, topLeft = Offset(leftX, topY), size = Size(halfW, height))

    // Right Quad
    drawRect(brush = playfieldGradient, topLeft = Offset(rightX, topY), size = Size(halfW, height))

    // Bear-off Tray (Dark grooved felt tray)
    val trayGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF180A05), Color(0xFF2A140B), Color(0xFF140804)),
        startX = bearOffX,
        endX = bearOffX + bearOffW
    )
    drawRect(brush = trayGradient, topLeft = Offset(bearOffX, topY), size = Size(bearOffW, height))

    // Inner shadow on playfields
    drawRect(
        color = Color.Black.copy(alpha = 0.40f),
        topLeft = Offset(leftX, topY),
        size = Size(halfW, height),
        style = Stroke(width = 4f)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.40f),
        topLeft = Offset(rightX, topY),
        size = Size(halfW, height),
        style = Stroke(width = 4f)
    )
}

private fun DrawScope.drawInlaidTriangle(
    xStart: Float,
    yStart: Float,
    width: Float,
    height: Float,
    isTop: Boolean,
    colorTop: Color,
    colorBottom: Color,
    isDark: Boolean
) {
    val tipX = xStart + (width / 2f)
    val tipY = if (isTop) yStart + height else yStart - height

    val path = Path().apply {
        moveTo(xStart, yStart)
        lineTo(xStart + width, yStart)
        lineTo(tipX, tipY)
        close()
    }

    // Triangle Gradient
    val triangleBrush = Brush.verticalGradient(
        colors = if (isTop) listOf(colorTop, colorBottom) else listOf(colorBottom, colorTop),
        startY = if (isTop) yStart else tipY,
        endY = if (isTop) tipY else yStart
    )
    drawPath(path = path, brush = triangleBrush)

    // Inlaid Wood Border / Bevel
    val borderColor = if (isDark) Color(0xFF7A3B22).copy(alpha = 0.6f) else Color(0xFFC7B18E).copy(alpha = 0.7f)
    drawPath(path = path, color = borderColor, style = Stroke(width = 1.2f))

    // Subtle 3D Edge Shading (Left side shadow, Right side light)
    val shadePath = Path().apply {
        moveTo(xStart, yStart)
        lineTo(tipX, tipY)
    }
    drawPath(path = shadePath, color = Color.Black.copy(alpha = 0.35f), style = Stroke(width = 1.5f))
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
        color = glowColor.copy(alpha = alpha * 0.38f)
    )

    // Pulsing Neon Border
    drawPath(
        path = path,
        color = glowColor.copy(alpha = alpha),
        style = Stroke(width = 2.5f)
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
    drawPath(path = diamondPath, color = Color.White, style = Stroke(width = 1.2f))
}

private fun DrawScope.drawCenterBarAndBrassHinges(
    barX: Float,
    barY: Float,
    barW: Float,
    barH: Float
) {
    // 1. Center Wooden Bar
    val barGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF260D07),
            Color(0xFF4C1E11),
            Color(0xFF5D2716),
            Color(0xFF2A0D07)
        ),
        startX = barX,
        endX = barX + barW
    )
    drawRect(brush = barGradient, topLeft = Offset(barX, barY), size = Size(barW, barH))

    // Bar 3D Bevel Borders
    drawLine(color = Color(0xFF8B4513).copy(alpha = 0.6f), start = Offset(barX, barY), end = Offset(barX, barY + barH), strokeWidth = 2f)
    drawLine(color = Color(0xFF140503).copy(alpha = 0.9f), start = Offset(barX + barW, barY), end = Offset(barX + barW, barY + barH), strokeWidth = 2f)

    // 2. Brass Hinges (Top, Middle, Bottom)
    val hingeYPositions = listOf(
        barY + (barH * 0.12f),
        barY + (barH * 0.50f),
        barY + (barH * 0.88f)
    )

    for (hy in hingeYPositions) {
        val hingeW = barW * 0.75f
        val hingeH = barH * 0.055f
        val hx = barX + ((barW - hingeW) / 2f)

        // Hinge Brass Gradient
        val brassGradient = Brush.linearGradient(
            colors = listOf(Color(0xFFF9E498), Color(0xFFD4AF37), Color(0xFFAA8012), Color(0xFFF7DE8B)),
            start = Offset(hx, hy),
            end = Offset(hx + hingeW, hy + hingeH)
        )
        drawRoundRect(
            brush = brassGradient,
            topLeft = Offset(hx, hy),
            size = Size(hingeW, hingeH),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color(0xFF59430A),
            topLeft = Offset(hx, hy),
            size = Size(hingeW, hingeH),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = 1.2f)
        )

        // Brass Screws
        drawCircle(color = Color(0xFF4A380A), radius = 2f, center = Offset(hx + (hingeW * 0.25f), hy + (hingeH / 2f)))
        drawCircle(color = Color(0xFF4A380A), radius = 2f, center = Offset(hx + (hingeW * 0.75f), hy + (hingeH / 2f)))
    }
}

private fun DrawScope.drawBrassCornerBrackets(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    bracketSize: Float
) {
    val brassGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFE082), Color(0xFFD4AF37), Color(0xFF8C6D1F), Color(0xFFFFF0B3)),
        start = Offset(x, y),
        end = Offset(x + bracketSize, y + bracketSize)
    )

    val corners = listOf(
        Offset(x, y), // Top-Left
        Offset(x + w, y), // Top-Right
        Offset(x, y + h), // Bottom-Left
        Offset(x + w, y + h) // Bottom-Right
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

        // Corner Brass Rivet
        val rivetCenter = Offset(
            c.x + (if (c.x > x + (w / 2f)) -bracketSize * 0.35f else bracketSize * 0.35f),
            c.y + (if (c.y > y + (h / 2f)) -bracketSize * 0.35f else bracketSize * 0.35f)
        )
        drawCircle(color = Color(0xFF3B2C04), radius = 3f, center = rivetCenter)
        drawCircle(color = Color(0xFFFFECB3), radius = 1.5f, center = rivetCenter - Offset(0.8f, 0.8f))
    }
}

// -------------------------------------------------------------
// MASTER 3D CHECKER PIECES (IVORY PEARL & OBSIDIAN EBONY)
// -------------------------------------------------------------
private fun DrawScope.drawMaster3DChecker(
    center: Offset,
    radius: Float,
    playerColor: PlayerColor,
    isSelected: Boolean,
    glowColor: Color,
    highlightAlpha: Float
) {
    val safeRadius = max(4f, radius)
    val isWhitePiece = (playerColor == PlayerColor.WHITE)

    // 1. Soft Realistic Drop Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.60f),
        radius = safeRadius * 1.08f,
        center = center + Offset(2.5f, 3.8f)
    )

    // 2. Base 3D Body Gradient
    val baseGradient = if (isWhitePiece) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFFFF9EE),
                Color(0xFFF3E5CB),
                Color(0xFFDCBE92),
                Color(0xFFB89868)
            ),
            center = center - Offset(safeRadius * 0.35f, safeRadius * 0.35f),
            radius = max(1f, safeRadius * 1.35f)
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF4A4A57),
                Color(0xFF282833),
                Color(0xFF171720),
                Color(0xFF0C0C12),
                Color(0xFF000000)
            ),
            center = center - Offset(safeRadius * 0.35f, safeRadius * 0.35f),
            radius = max(1f, safeRadius * 1.35f)
        )
    }
    drawCircle(brush = baseGradient, radius = safeRadius, center = center)

    // 3. Outer Rim Ring (Gold Rim on White, Dark Chrome Rim on Black)
    val outerRimColor = if (isWhitePiece) Color(0xFFD4AF37).copy(alpha = 0.75f) else Color(0xFF6B6B7F).copy(alpha = 0.55f)
    drawCircle(
        color = outerRimColor,
        radius = safeRadius - 0.8f,
        center = center,
        style = Stroke(width = 1.8f)
    )

    // 4. Concentric Engraved Ring (The Signature Backgammon Checker Ridge)
    val innerRidgeColor = if (isWhitePiece) Color(0xFFC7A267).copy(alpha = 0.80f) else Color(0xFF333342)
    drawCircle(
        color = innerRidgeColor,
        radius = max(1f, safeRadius * 0.66f),
        center = center,
        style = Stroke(width = 2.2f)
    )

    // 5. Inset Center Core
    val centerCoreBrush = if (isWhitePiece) {
        Brush.radialGradient(
            colors = listOf(Color(0xFFFFFDF8), Color(0xFFEADBBE)),
            center = center,
            radius = max(1f, safeRadius * 0.45f)
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Color(0xFF2A2A35), Color(0xFF0F0F16)),
            center = center,
            radius = max(1f, safeRadius * 0.45f)
        )
    }
    drawCircle(brush = centerCoreBrush, radius = max(1f, safeRadius * 0.45f), center = center)

    // 6. Polished Specular Highlight (High-End Gloss Reflection)
    val specular = Brush.radialGradient(
        colors = listOf(Color.White.copy(alpha = if (isWhitePiece) 0.70f else 0.45f), Color.Transparent),
        center = center - Offset(safeRadius * 0.42f, safeRadius * 0.42f),
        radius = max(1f, safeRadius * 0.65f)
    )
    drawCircle(brush = specular, radius = max(1f, safeRadius * 0.75f), center = center)

    // 7. Selected Glowing Ring Effect
    if (isSelected) {
        drawCircle(
            color = glowColor.copy(alpha = highlightAlpha),
            radius = safeRadius * 1.28f,
            center = center,
            style = Stroke(width = 3.5f)
        )
        drawCircle(
            color = Color.White,
            radius = safeRadius * 1.12f,
            center = center,
            style = Stroke(width = 1.6f)
        )
    }
}

// -------------------------------------------------------------
// TAP DETECTOR GEOMETRY
// -------------------------------------------------------------
private fun calculateClickedPoint(offset: Offset, w: Float, h: Float): Int? {
    val frameMarginX = w * 0.022f
    val frameMarginY = h * 0.028f
    val frameW = w - (frameMarginX * 2)
    val frameH = h - (frameMarginY * 2)

    val frameBorderThickness = min(frameW * 0.035f, frameH * 0.065f)
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
