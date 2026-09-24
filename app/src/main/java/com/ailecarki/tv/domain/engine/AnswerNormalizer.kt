package com.ailecarki.tv.domain.engine

/** Türkçe locale kurallarıyla cevap karşılaştırma (i→İ, ı→I). */
object AnswerNormalizer {
    private val WHITESPACE = Regex("\\s+")

    fun normalize(input: String): String =
        TurkishAlphabet.upper(input.trim())
            .filter { it.isLetterOrDigit() || it.isWhitespace() }
            .split(WHITESPACE)
            .filter { it.isNotEmpty() }
            .joinToString(" ")

    /** Türkçe karakterleri Latin karşılığına indirger (hoşgörülü eşleşme için). */
    fun fold(text: String): String = buildString(text.length) {
        for (c in text) append(
            when (c) {
                'Ç' -> 'C'; 'Ğ' -> 'G'; 'İ' -> 'I'; 'Ö' -> 'O'; 'Ş' -> 'S'; 'Ü' -> 'U'
                else -> c
            }
        )
    }

    fun matches(input: String, answer: String, lenient: Boolean): Boolean {
        val a = normalize(input)
        val b = normalize(answer)
        if (a.isEmpty()) return false
        if (a == b) return true
        return lenient && fold(a) == fold(b)
    }
}
