package com.ailecarki.tv.domain

import com.ailecarki.tv.domain.engine.ScoreEngine
import com.ailecarki.tv.domain.model.Player
import com.ailecarki.tv.domain.rules.BankruptPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreEngineTest {
    @Test fun fiveHundredTimesThreeLetters() = assertEquals(1500, ScoreEngine.consonantPoints(500, 3))
    @Test fun doubleMultiplier() = assertEquals(3000, ScoreEngine.consonantPoints(500, 3, 2))
    @Test fun jokerFixed() {
        assertEquals(1000, ScoreEngine.jokerPoints(4, 1000))
        assertEquals(0, ScoreEngine.jokerPoints(0, 1000))
    }
    @Test fun vowelAffordability() {
        assertTrue(ScoreEngine.canBuyVowel(250, 250))
        assertFalse(ScoreEngine.canBuyVowel(249, 250))
    }
    @Test fun bankruptResetsTotal() {
        val p = ScoreEngine.applyBankrupt(Player("A", 4000, 1500), BankruptPolicy.RESET_TOTAL)
        assertEquals(0, p.score)
    }
    @Test fun bankruptRoundPolicyKeepsEarlierRounds() {
        val p = ScoreEngine.applyBankrupt(Player("A", 4000, 1500), BankruptPolicy.RESET_ROUND_EARNINGS)
        assertEquals(2500, p.score)
    }
}
