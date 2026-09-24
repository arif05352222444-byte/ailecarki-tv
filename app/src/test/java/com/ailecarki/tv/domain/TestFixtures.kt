package com.ailecarki.tv.domain

import com.ailecarki.tv.domain.engine.GameEngine
import com.ailecarki.tv.domain.engine.WheelEngine
import com.ailecarki.tv.domain.model.GameState
import com.ailecarki.tv.domain.model.Player
import com.ailecarki.tv.domain.model.Puzzle
import com.ailecarki.tv.domain.model.SegmentType
import com.ailecarki.tv.domain.model.WheelSegment
import com.ailecarki.tv.domain.rules.GameRules
import kotlin.random.Random

object TestFixtures {
    /** 3 tane S içerir. */
    val ASSOS = Puzzle("t_assos", "ŞEHİR", "ASSOS")
    val PUZZLES = listOf(
        ASSOS,
        Puzzle("t2", "ŞEHİR", "İSTANBUL"),
        Puzzle("t3", "HAYVAN", "KUTUP AYISI"),
        Puzzle("t4", "YEMEK", "KARNIYARIK"),
        Puzzle("t5", "EŞYA", "ÇAYDANLIK"),
    )

    fun points(v: Int) = WheelSegment(SegmentType.POINTS, v, v.toString(), 0)
    fun special(t: SegmentType) = WheelSegment(t, 0, t.name, 0)

    /** Tek dilimli çark → her çevirmede aynı sonuç. */
    fun engine(segment: WheelSegment, rules: GameRules = GameRules()) =
        GameEngine(rules, WheelEngine(listOf(segment), Random(1)), Random(1))

    fun state(puzzle: Puzzle = ASSOS, scores: List<Int> = listOf(0, 0, 0)) = GameState(
        players = scores.mapIndexed { i, s -> Player("P$i", score = s) },
        totalRounds = 3,
        puzzle = puzzle,
        usedPuzzleIds = setOf(puzzle.id),
    )

    /** Çarkı çevirip sonucu uygular (animasyonsuz). */
    fun GameEngine.spinAndResolve(s: GameState): GameState =
        resolveWheelResult(onSpinFinished(startSpin(s)))
}
