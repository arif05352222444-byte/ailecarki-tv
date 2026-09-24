package com.ailecarki.tv.domain

import com.ailecarki.tv.domain.engine.WheelEngine
import com.ailecarki.tv.domain.rules.WheelConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WheelEngineTest {
    private val wheel = WheelEngine(WheelConfig.DEFAULT, Random(42))

    @Test
    fun defaultWheelHasBalancedSegmentCount() {
        assertTrue(WheelConfig.DEFAULT.size in 20..24)
    }

    @Test
    fun targetRotationStopsExactlyOnChosenSegment() {
        val starts = listOf(0f, 13f, 179.5f, 359f, -45f, 1234.5f)
        val jitters = listOf(-0.35f, 0f, 0.35f)
        for (index in WheelConfig.DEFAULT.indices) for (start in starts) for (j in jitters) {
            val target = wheel.targetRotation(start, index, extraTurns = 5, jitter = j)
            assertEquals(index, wheel.segmentIndexAt(target))
            assertTrue(target - start >= 5 * 360f)
            assertTrue(target - start < 6 * 360f)
        }
    }

    @Test
    fun zeroRotationPointsAtFirstSegment() {
        assertEquals(0, wheel.segmentIndexAt(0f))
        assertEquals(WheelConfig.DEFAULT.size - 1, wheel.segmentIndexAt(1f))
    }

    @Test
    fun pickedIndexesCoverWholeWheel() {
        val seen = HashSet<Int>()
        repeat(2000) { seen += wheel.pickSegmentIndex() }
        assertEquals(WheelConfig.DEFAULT.size, seen.size)
    }

    @Test
    fun durationIsBetweenThreeAndFiveSeconds() {
        repeat(200) { assertTrue(wheel.randomDurationMs() in 3000..5000) }
    }

    /** UI çizimiyle aynı formül: dilim i, [-90 + i*sweep + rotation] açısından başlar; pointer -90°'de. */
    private fun segmentUnderPointerAsDrawn(rotation: Float): Int {
        val sweep = 360f / WheelConfig.DEFAULT.size
        for (i in WheelConfig.DEFAULT.indices) {
            val start = WheelEngine.positiveMod(-90f + i * sweep + rotation, 360f)
            val pointer = WheelEngine.positiveMod(-90f, 360f)
            val rel = WheelEngine.positiveMod(pointer - start, 360f)
            if (rel < sweep) return i
        }
        error("pointer hiçbir dilimde değil")
    }

    @Test
    fun drawnSegmentUnderPointerMatchesEngineResult() {
        var rotation = 0f
        repeat(500) {
            val idx = wheel.pickSegmentIndex()
            val start = WheelEngine.positiveMod(rotation, 360f) // UI her çevirmede 0..360'a normalize eder
            rotation = wheel.targetRotation(start, idx, wheel.randomExtraTurns(), wheel.randomJitter())
            assertEquals(idx, segmentUnderPointerAsDrawn(rotation))
            assertEquals(idx, wheel.segmentIndexAt(rotation))
        }
    }

    @Test
    fun wrapAroundAtZeroAndThreeSixty() {
        val last = WheelConfig.DEFAULT.size - 1
        for (start in listOf(359.99f, 0f, 360f, 720f, -0.01f)) {
            assertEquals(0, wheel.segmentIndexAt(wheel.targetRotation(start, 0, 4)))
            assertEquals(last, wheel.segmentIndexAt(wheel.targetRotation(start, last, 4)))
        }
    }

    @Test
    fun fiveHundredSegmentLandsOnFiveHundred() {
        val idx = WheelConfig.DEFAULT.indexOfFirst { it.value == 500 }
        val r = wheel.targetRotation(123f, idx, 5, 0.3f)
        assertEquals(500, WheelConfig.DEFAULT[segmentUnderPointerAsDrawn(r)].value)
    }
}
