package com.ailecarki.tv.domain.engine

import com.ailecarki.tv.domain.model.Puzzle
import kotlin.random.Random

object PuzzleEngine {
    fun letterCount(answer: String, letter: Char): Int = answer.count { it == letter }

    fun isFullyRevealed(answer: String, revealed: Set<Char>): Boolean =
        answer.all { !TurkishAlphabet.isLetter(it) || it in revealed }

    fun hiddenTileCount(answer: String, revealed: Set<Char>): Int =
        answer.count { TurkishAlphabet.isLetter(it) && it !in revealed }

    fun remainingConsonants(answer: String, revealed: Set<Char>): Set<Char> =
        answer.filter { TurkishAlphabet.isConsonant(it) && it !in revealed }.toSet()

    fun remainingVowels(answer: String, revealed: Set<Char>): Set<Char> =
        answer.filter { TurkishAlphabet.isVowel(it) && it !in revealed }.toSet()

    fun lettersIn(answer: String): Set<Char> = answer.filter { TurkishAlphabet.isLetter(it) }.toSet()

    /** Aynı oturumda tekrar vermemek için kullanılmamış bir soru seçer. */
    fun pickPuzzle(
        all: List<Puzzle>,
        used: Set<String>,
        random: Random,
        filter: (Puzzle) -> Boolean = { true },
    ): Puzzle {
        require(all.isNotEmpty()) { "Kelime bankası boş" }
        val fresh = all.filter { it.id !in used }
        val preferred = fresh.filter(filter)
        val pool = preferred.ifEmpty { fresh }.ifEmpty { all }
        return pool.random(random)
    }
}
