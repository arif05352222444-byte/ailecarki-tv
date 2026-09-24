package com.ailecarki.tv.domain.engine

import com.ailecarki.tv.domain.model.WheelSegment
import kotlin.random.Random

/**
 * Çark geometrisi. Dilim i, dönme açısı 0 iken [-90 + i*sweep, -90 + (i+1)*sweep] aralığındadır
 * (Canvas açıları: 0 = saat 3, saat yönünde artar). Pointer sabit olarak en üstte (-90°).
 * Çark saat yönünde [rotation] kadar döndüğünde pointer altındaki dilim [segmentIndexAt] ile bulunur.
 */
class WheelEngine(
    val segments: List<WheelSegment>,
    private val random: Random = Random.Default,
) {
    init { require(segments.isNotEmpty()) }

    val sweep: Float get() = 360f / segments.size

    fun pickSegmentIndex(): Int = random.nextInt(segments.size)

    fun segmentIndexAt(rotation: Float): Int {
        val offset = positiveMod(-rotation, 360f)
        return (offset / sweep).toInt().coerceIn(0, segments.size - 1)
    }

    /**
     * [current] açısından başlayıp [extraTurns] tam tur attıktan sonra [index] dilimi pointer altında
     * duracak hedef açıyı döndürür. [jitter] -0.5..0.5 arası; dilim içinde doğal duruş noktası.
     */
    fun targetRotation(current: Float, index: Int, extraTurns: Int, jitter: Float = 0f): Float {
        val j = jitter.coerceIn(-0.4f, 0.4f)
        val desired = -(index + 0.5f + j) * sweep
        val delta = positiveMod(desired - current, 360f)
        return current + extraTurns * 360f + delta
    }

    fun randomJitter(): Float = (random.nextFloat() - 0.5f) * 0.7f
    fun randomExtraTurns(): Int = 4 + random.nextInt(3)
    fun randomDurationMs(): Int = 3000 + random.nextInt(2001)

    companion object {
        fun positiveMod(a: Float, b: Float): Float = ((a % b) + b) % b
    }
}
