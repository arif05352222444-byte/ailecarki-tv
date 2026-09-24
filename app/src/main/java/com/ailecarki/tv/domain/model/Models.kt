package com.ailecarki.tv.domain.model

/** Oyuncu. [roundScore] yalnızca içinde bulunulan turdaki net kazancı tutar. */
data class Player(
    val name: String,
    val score: Int = 0,
    val roundScore: Int = 0,
    val roundsWon: Int = 0,
)

/** Kelime bankasındaki tek bir soru. [answer] büyük harfli Türkçe metindir. */
data class Puzzle(
    val id: String,
    val category: String,
    val answer: String,
)

enum class SegmentType { POINTS, BANKRUPT, LOSE_TURN, DOUBLE, JOKER }

/** Çarkın tek dilimi. [colorIndex] UI paletindeki renk sırasıdır. */
data class WheelSegment(
    val type: SegmentType,
    val value: Int,
    val label: String,
    val colorIndex: Int,
)
