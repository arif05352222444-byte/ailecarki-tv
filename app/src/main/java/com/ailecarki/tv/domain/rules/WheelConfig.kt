package com.ailecarki.tv.domain.rules

import com.ailecarki.tv.domain.model.SegmentType
import com.ailecarki.tv.domain.model.WheelSegment

/** Çark dilimleri buradan düzenlenir. Sıra, çarkta saat yönündeki sıradır (0 = başlangıçta en üstte). */
object WheelConfig {
    const val LABEL_BANKRUPT = "İFLAS"
    const val LABEL_LOSE_TURN = "SIRA GEÇ"
    const val LABEL_DOUBLE = "2X"
    const val LABEL_JOKER = "JOKER"

    private fun p(value: Int, color: Int) = WheelSegment(SegmentType.POINTS, value, value.toString(), color)
    private val bankrupt = WheelSegment(SegmentType.BANKRUPT, 0, LABEL_BANKRUPT, 8)
    private val loseTurn = WheelSegment(SegmentType.LOSE_TURN, 0, LABEL_LOSE_TURN, 9)
    private val double = WheelSegment(SegmentType.DOUBLE, 0, LABEL_DOUBLE, 10)
    private val joker = WheelSegment(SegmentType.JOKER, 0, LABEL_JOKER, 11)

    val DEFAULT: List<WheelSegment> = listOf(
        p(500, 0), p(100, 1), p(300, 2), bankrupt,
        p(750, 3), p(250, 4), double, p(400, 5),
        p(1000, 6), p(200, 7), loseTurn, p(500, 1),
        p(1500, 2), p(300, 0), joker, p(250, 3),
        p(750, 4), bankrupt, p(400, 6), p(2000, 5),
        p(200, 2), loseTurn, p(500, 7), p(1000, 3),
    )
}
