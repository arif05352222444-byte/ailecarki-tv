package com.ailecarki.tv.domain

import com.ailecarki.tv.domain.engine.PuzzleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PuzzleEngineTest {
    @Test fun countsLetters() {
        assertEquals(3, PuzzleEngine.letterCount("ASSOS", 'S'))
        assertEquals(0, PuzzleEngine.letterCount("ASSOS", 'K'))
    }

    @Test fun dottedAndDotlessIAreDifferentLetters() {
        assertEquals(2, PuzzleEngine.letterCount("KUTUP AYISI", 'I'))
        assertEquals(0, PuzzleEngine.letterCount("KUTUP AYISI", 'İ'))
    }

    @Test fun spacesDoNotBlockFullReveal() {
        val all = "KUTUP AYISI".filter { it != ' ' }.toSet()
        assertTrue(PuzzleEngine.isFullyRevealed("KUTUP AYISI", all))
        assertFalse(PuzzleEngine.isFullyRevealed("KUTUP AYISI", all - 'K'))
    }

    @Test fun neverRepeatsWithinSession() {
        val used = HashSet<String>()
        repeat(TestFixtures.PUZZLES.size) {
            val p = PuzzleEngine.pickPuzzle(TestFixtures.PUZZLES, used, Random(it))
            assertFalse(p.id in used)
            used += p.id
        }
    }
}
