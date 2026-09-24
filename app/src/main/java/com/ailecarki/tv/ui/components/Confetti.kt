package com.ailecarki.tv.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.ailecarki.tv.ui.theme.AppColors
import kotlin.math.sin
import kotlin.random.Random

private data class Piece(val x: Float, val speed: Float, val sway: Float, val phase: Float, val color: Int, val size: Float)

/** Sınırlı süreli, az parçacıklı kutlama (sonsuz animasyon yok). */
@Composable
fun Confetti(trigger: Any?, modifier: Modifier = Modifier, durationMs: Int = 3500, count: Int = 70) {
    val progress = remember(trigger) { Animatable(0f) }
    val pieces = remember(trigger) {
        val r = Random(trigger.hashCode())
        List(count) {
            Piece(r.nextFloat(), 0.6f + r.nextFloat() * 0.8f, 0.02f + r.nextFloat() * 0.04f, r.nextFloat() * 6f,
                r.nextInt(AppColors.Wheel.size), 6f + r.nextFloat() * 8f)
        }
    }
    LaunchedEffect(trigger) { progress.animateTo(1f, tween(durationMs, easing = LinearEasing)) }
    Canvas(modifier.fillMaxSize()) {
        val p = progress.value
        if (p <= 0f || p >= 1f) return@Canvas
        pieces.forEach { piece ->
            val y = (-0.1f + p * piece.speed * 1.3f) * size.height
            val x = (piece.x + sin(p * 12f + piece.phase) * piece.sway) * size.width
            val s = piece.size * density / 2f
            drawRect(AppColors.Wheel[piece.color], Offset(x, y), Size(s, s * 1.6f), alpha = 1f - p * 0.5f)
        }
    }
}
