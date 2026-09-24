package com.ailecarki.tv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.ailecarki.tv.ui.theme.AppColors

/** Statik sahne arka planı: gradyan, ışık hüzmeleri, soyut şehir silueti, sahne zemini. Animasyonsuz. */
@Composable
fun StageBackground(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            Brush.verticalGradient(
                0f to AppColors.Navy,
                0.45f to AppColors.DeepBlue,
                0.75f to Color(0xFF2A1A6E),
                1f to AppColors.Navy,
            ),
        )
        // Işık hüzmeleri
        val beams = listOf(0.12f, 0.3f, 0.7f, 0.88f)
        beams.forEachIndexed { i, x ->
            val path = Path().apply {
                moveTo(w * x - w * 0.012f, 0f)
                lineTo(w * x + w * 0.012f, 0f)
                val spread = if (i < 2) 0.10f else -0.10f
                lineTo(w * (x + spread) + w * 0.08f, h * 0.9f)
                lineTo(w * (x + spread) - w * 0.08f, h * 0.9f)
                close()
            }
            drawPath(path, Brush.verticalGradient(listOf(Color(0x33B8C8FF), Color.Transparent), 0f, h * 0.9f))
        }
        // Tavan spotları
        for (i in 0..9) {
            val cx = w * (0.05f + i * 0.1f)
            drawCircle(
                Brush.radialGradient(listOf(Color(0x88FFF2C0), Color.Transparent), Offset(cx, h * 0.015f), h * 0.05f),
                radius = h * 0.05f,
                center = Offset(cx, h * 0.015f),
            )
        }
        // Soyut şehir silueti
        val baseY = h * 0.66f
        val sil = Color(0x2A0A0F35)
        val blocks = listOf(0.22f to 0.06f, 0.29f to 0.10f, 0.35f to 0.05f, 0.62f to 0.08f, 0.69f to 0.12f, 0.76f to 0.06f)
        blocks.forEach { (x, hh) ->
            drawRect(sil, Offset(w * x, baseY - h * hh), Size(w * 0.05f, h * hh))
        }
        // Kubbe + minareler
        drawArc(sil, 180f, 180f, true, Offset(w * 0.44f, baseY - h * 0.1f), Size(w * 0.12f, h * 0.2f))
        drawRect(sil, Offset(w * 0.425f, baseY - h * 0.2f), Size(w * 0.006f, h * 0.2f))
        drawRect(sil, Offset(w * 0.57f, baseY - h * 0.2f), Size(w * 0.006f, h * 0.2f))
        // Sahne zemini
        drawRect(
            Brush.verticalGradient(listOf(Color(0x001B2A7A), Color(0xAA0A0F35)), baseY, h),
            Offset(0f, baseY),
            Size(w, h - baseY),
        )
        drawLine(Color(0x66FFC52E), Offset(0f, baseY), Offset(w, baseY), strokeWidth = h * 0.004f)
        for (i in 0..40) {
            drawCircle(Color(0x88FFD66B), radius = h * 0.004f, center = Offset(w * i / 40f, baseY + h * 0.012f))
        }
    }
}
