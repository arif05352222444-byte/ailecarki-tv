package com.ailecarki.tv.domain.engine

import java.util.Locale

object TurkishAlphabet {
    val LOCALE: Locale = Locale.forLanguageTag("tr-TR")
    val LETTERS: List<Char> = "ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ".toList()
    private val LETTER_SET: Set<Char> = LETTERS.toSet()
    val VOWELS: Set<Char> = "AEIİOÖUÜ".toSet()
    val CONSONANTS: Set<Char> = LETTERS.filter { it !in VOWELS }.toSet()

    fun isLetter(c: Char): Boolean = c in LETTER_SET
    fun isVowel(c: Char): Boolean = c in VOWELS
    fun isConsonant(c: Char): Boolean = c in CONSONANTS
    fun upper(text: String): String = text.uppercase(LOCALE)
}
