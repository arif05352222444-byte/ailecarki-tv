package com.ailecarki.tv.domain

import com.ailecarki.tv.domain.engine.AnswerNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerNormalizationTest {
    @Test fun istanbulLowercaseMatches() =
        assertTrue(AnswerNormalizer.matches("istanbul", "İSTANBUL", lenient = false))

    @Test fun trimsAndCollapsesSpaces() =
        assertTrue(AnswerNormalizer.matches("   kutup    ayısı  ", "KUTUP AYISI", lenient = false))

    @Test fun turkishUppercaseRules() {
        assertEquals("İSTANBUL", AnswerNormalizer.normalize("istanbul"))
        assertEquals("ISPARTA", AnswerNormalizer.normalize("ısparta"))
    }

    @Test fun strictModeKeepsIAndDottedIDistinct() =
        assertFalse(AnswerNormalizer.matches("ISTANBUL", "İSTANBUL", lenient = false))

    @Test fun lenientModeAcceptsKeyboardsWithoutTurkishLetters() {
        assertTrue(AnswerNormalizer.matches("ISTANBUL", "İSTANBUL", lenient = true))
        assertTrue(AnswerNormalizer.matches("caydanlik", "ÇAYDANLIK", lenient = true))
    }

    @Test fun wrongAnswerFails() {
        assertFalse(AnswerNormalizer.matches("ankara", "İSTANBUL", lenient = true))
        assertFalse(AnswerNormalizer.matches("   ", "İSTANBUL", lenient = true))
    }

    @Test fun englishDefaultLocaleDoesNotBreakI() {
        val old = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale.ENGLISH)
            assertTrue(AnswerNormalizer.matches("istanbul", "İSTANBUL", lenient = false))
            assertTrue(AnswerNormalizer.matches("kutup ayısı", "KUTUP AYISI", lenient = false))
        } finally {
            java.util.Locale.setDefault(old)
        }
    }

    @Test fun keyboardHasAll29TurkishLetters() {
        val letters = com.ailecarki.tv.domain.engine.TurkishAlphabet.LETTERS.joinToString("")
        assertEquals("ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ", letters)
    }
}
