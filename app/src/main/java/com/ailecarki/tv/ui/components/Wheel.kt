package com.ailecarki.tv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailecarki.tv.domain.model.SegmentType
import com.ailecarki.tv.domain.model.WheelSegment
import com.ailecarki.tv.ui.theme.AppColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Çark. Dilimler Canvas'ta gerçek açılarla bir kez çizilir; dönüş yalnızca graphicsLayer.rotationZ ile
 * yapılır → her karede yeniden çizim / recomposition olmaz (TV Box dostu).
 * Açı kuralı domain/engine/WheelEngine ile birebir aynıdır: dilim i, [-90 + i*sweep] açısından başlar.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun WheelView(
    segments: List<WheelSegment>,
    rotation: () -> Float,
    modifier: Modifier = Modifier,
    size: Dp = 420.dp,
) {
    val measurer = rememberTextMeasurer()
    val sweep = 360f / segments.size
    val k = size.value / 420f
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        // Dönen disk
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation() },
        ) {
            val r = this.size.minDimension / 2f
            val c = center
            val diskR = r * 0.9f
            val topLeft = Offset(c.x - diskR, c.y - diskR)
            val arcSize = Size(diskR * 2, diskR * 2)
            segments.forEachIndexed { i, seg ->
                val start = -90f + i * sweep
                drawArc(AppColors.Wheel[seg.colorIndex % AppColors.Wheel.size], start, sweep, true, topLeft, arcSize)
                drawArc(Color(0x55000000), start, sweep, true, topLeft, arcSize, style = Stroke(1.5.dp.toPx()))
            }
            segments.forEachIndexed { i, seg ->
                val mid = -90f + (i + 0.5f) * sweep
                val label = seg.label
                val fontSize = when {
                    label.length >= 6 -> (13 * k).sp
                    label.length >= 4 -> (17 * k).sp
                    else -> (20 * k).sp
                }
                val textColor = if (seg.type == SegmentType.JOKER || seg.colorIndex == 5) AppColors.Navy else Color.White
                val layout = measurer.measure(
                    label,
                    TextStyle(color = textColor, fontSize = fontSize, fontWeight = FontWeight.Black),
                )
                // Yazı merkezden dışa doğru, dilim ekseni boyunca okunur.
                rotate(mid, c) {
                    val x = c.x + diskR * 0.93f - layout.size.width
                    drawText(layout, topLeft = Offset(x, c.y - layout.size.height / 2f))
                }
            }
        }
        // Sabit çerçeve, ampuller, göbek ve pointer
        Canvas(Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2f
            val c = center
            drawCircle(AppColors.Gold, r * 0.95f, c, style = Stroke(r * 0.07f))
            drawCircle(AppColors.GoldDark, r * 0.985f, c, style = Stroke(r * 0.02f))
            val bulbs = 24
            for (i in 0 until bulbs) {
                val a = Math.toRadians((i * 360.0 / bulbs) + 7.5)
                drawCircle(
                    if (i % 2 == 0) AppColors.GoldLight else Color.White,
                    r * 0.022f,
                    Offset(c.x + (r * 0.95f) * cos(a).toFloat(), c.y + (r * 0.95f) * sin(a).toFloat()),
                )
            }
            drawCircle(AppColors.GoldDark, r * 0.16f, c)
            drawCircle(AppColors.Gold, r * 0.135f, c)
            drawPath(starPath(c, r * 0.1f), AppColors.GoldLight)
            // Pointer (üstte, aşağıyı gösterir)
            val p = Path().apply {
                moveTo(c.x - r * 0.07f, c.y - r * 1.0f)
                lineTo(c.x + r * 0.07f, c.y - r * 1.0f)
                lineTo(c.x, c.y - r * 0.78f)
                close()
            }
            drawPath(p, AppColors.GoldLight)
            drawPath(p, AppColors.GoldDark, style = Stroke(2.dp.toPx()))
        }
    }
}

private fun starPath(c: Offset, scale: Float): Path = Path().apply {
    for (i in 0 until 10) {
        val a = Math.toRadians(-90.0 + i * 36.0)
        val rr = if (i % 2 == 0) scale else scale * 0.45f
        val x = c.x + (rr * cos(a)).toFloat()
        val y = c.y + (rr * sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
