package com.ailecarki.tv.domain

import com.ailecarki.tv.domain.TestFixtures.spinAndResolve
import com.ailecarki.tv.domain.model.GameEvent
import com.ailecarki.tv.domain.model.GamePhase
import com.ailecarki.tv.domain.model.SegmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test fun correctConsonantAddsPointsAndKeepsTurn() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        var s = e.spinAndResolve(TestFixtures.state())
        assertEquals(GamePhase.LETTER_SELECTION, s.phase)
        s = e.chooseConsonant(s, 'S')
        assertEquals(1500, s.players[0].score)
        assertEquals(GamePhase.LETTER_REVEAL, s.phase)
        assertEquals(GameEvent.LetterFound('S', 3, 1500), s.lastEvent)
        s = e.finishReveal(s)
        assertEquals(GamePhase.PLAYER_ACTION, s.phase)
        assertEquals(0, s.currentPlayerIndex)
    }

    @Test fun wrongConsonantAdvancesTurnWithoutPoints() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        val s = e.chooseConsonant(e.spinAndResolve(TestFixtures.state()), 'K')
        assertEquals(0, s.players[0].score)
        assertEquals(1, s.currentPlayerIndex)
        assertEquals(GamePhase.PLAYER_TURN, s.phase)
        assertTrue('K' in s.usedLetters)
    }

    @Test fun repeatedLetterCannotBeSelected() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        var s = e.finishReveal(e.chooseConsonant(e.spinAndResolve(TestFixtures.state()), 'S'))
        s = e.spinAndResolve(s)
        assertFalse(e.canSelectLetter(s, 'S'))
        assertSame(s, e.chooseConsonant(s, 'S'))
    }

    @Test fun vowelNotAllowedInConsonantSelection() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        val s = e.spinAndResolve(TestFixtures.state())
        assertFalse(e.canSelectLetter(s, 'A'))
        assertSame(s, e.chooseConsonant(s, 'A'))
    }

    @Test fun doubleSegmentDoublesNextCorrectConsonant() {
        val e2x = TestFixtures.engine(TestFixtures.special(SegmentType.DOUBLE))
        var s = e2x.spinAndResolve(TestFixtures.state())
        assertTrue(s.doubleActive)
        assertEquals(0, s.currentPlayerIndex)
        val e500 = TestFixtures.engine(TestFixtures.points(500))
        s = e500.chooseConsonant(e500.spinAndResolve(s), 'S')
        assertEquals(3000, s.players[0].score)
        assertFalse(s.doubleActive)
    }

    @Test fun jokerGivesFixedPoints() {
        val e = TestFixtures.engine(TestFixtures.special(SegmentType.JOKER))
        val s = e.chooseConsonant(e.spinAndResolve(TestFixtures.state()), 'S')
        assertEquals(1000, s.players[0].score)
    }

    @Test fun buyingVowelCosts250AndKeepsTurn() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        var s = TestFixtures.state(scores = listOf(1000, 0, 0))
        s = e.startBuyVowel(s)
        assertEquals(GamePhase.VOWEL_SELECTION, s.phase)
        s = e.chooseVowel(s, 'O')
        assertEquals(750, s.players[0].score)
        assertEquals(0, s.currentPlayerIndex)
        s = e.chooseVowel(e.startBuyVowel(e.finishReveal(s)), 'E') // yok ama puan yine gider
        assertEquals(500, s.players[0].score)
        assertEquals(0, s.currentPlayerIndex)
    }

    @Test fun cannotBuyVowelWithLessThanCost() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        val s = TestFixtures.state(scores = listOf(200, 0, 0))
        assertFalse(e.canBuyVowel(s))
        assertSame(s, e.startBuyVowel(s))
    }

    @Test fun bankruptZeroesScoreAndAdvances() {
        val e = TestFixtures.engine(TestFixtures.special(SegmentType.BANKRUPT))
        val s = e.spinAndResolve(TestFixtures.state(scores = listOf(2500, 0, 0)))
        assertEquals(0, s.players[0].score)
        assertEquals(1, s.currentPlayerIndex)
        assertEquals(GameEvent.Bankrupt, s.lastEvent)
    }

    @Test fun loseTurnKeepsScore() {
        val e = TestFixtures.engine(TestFixtures.special(SegmentType.LOSE_TURN))
        val s = e.spinAndResolve(TestFixtures.state(scores = listOf(2500, 0, 0)))
        assertEquals(2500, s.players[0].score)
        assertEquals(1, s.currentPlayerIndex)
    }

    @Test fun correctSolveCompletesRound() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        val s = e.submitSolve(e.startSolve(TestFixtures.state()), "assos")
        assertEquals(GamePhase.ROUND_COMPLETE, s.phase)
        assertEquals(0, s.roundWinnerIndex)
        assertEquals(1, s.players[0].roundsWon)
    }

    @Test fun wrongSolveAdvancesTurn() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        val s = e.submitSolve(e.startSolve(TestFixtures.state()), "efes")
        assertEquals(1, s.currentPlayerIndex)
        assertEquals(GamePhase.PLAYER_TURN, s.phase)
    }

    @Test fun lastLetterRevealCompletesRound() {
        val e = TestFixtures.engine(TestFixtures.points(100))
        var s = TestFixtures.state(scores = listOf(1000, 0, 0)).copy(revealedLetters = setOf('A', 'O'))
        s = e.finishReveal(e.chooseConsonant(e.spinAndResolve(s), 'S'))
        assertEquals(GamePhase.ROUND_COMPLETE, s.phase)
    }

    @Test fun threeRoundsThenFinalForLeader() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        var s = e.newGame(listOf("ARİF", "DEMİR", "ANNE"), TestFixtures.PUZZLES)
        repeat(3) { r ->
            assertEquals(r + 1, s.round)
            s = e.submitSolve(e.startSolve(s), s.puzzle.answer)
            s = e.nextRound(s, TestFixtures.PUZZLES)
        }
        assertTrue(s.isFinal)
        assertEquals(GamePhase.FINAL_LETTER_SELECTION, s.phase)
        assertEquals(4, s.usedPuzzleIds.size) // hiç tekrar yok
    }

    @Test fun tieBreakerUsesRoundsWon() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        var s = TestFixtures.state(scores = listOf(1000, 1000, 500))
        s = s.copy(players = s.players.mapIndexed { i, p -> if (i == 1) p.copy(roundsWon = 2) else p },
            phase = GamePhase.ROUND_COMPLETE, round = 3)
        s = e.nextRound(s, TestFixtures.PUZZLES)
        assertEquals(1, s.finalistIndex)
    }

    @Test fun finalPicksThreeConsonantsOneVowel() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        var s = TestFixtures.state().copy(phase = GamePhase.ROUND_COMPLETE, round = 3)
        s = e.nextRound(s, TestFixtures.PUZZLES)
        val consonants = com.ailecarki.tv.domain.engine.TurkishAlphabet.CONSONANTS.filter { it !in s.usedLetters }
        s = e.pickFinalLetter(s, consonants[0])
        s = e.pickFinalLetter(s, consonants[1])
        s = e.pickFinalLetter(s, consonants[2])
        assertFalse(e.canPickFinal(s, consonants[3]))
        s = e.pickFinalLetter(s, 'A')
        assertEquals(GamePhase.FINAL_REVEAL, s.phase)
        s = e.finishFinalReveal(s)
        val won = e.submitFinal(s, s.puzzle.answer.lowercase())
        assertEquals(true, won.finalWon)
        assertEquals(false, e.finalTimeout(s).finalWon)
    }

    @Test fun resumeFromSpinningGoesToResult() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        val spinning = e.startSpin(TestFixtures.state())
        assertEquals(GamePhase.WHEEL_RESULT, e.sanitizeForResume(spinning).phase)
    }

    @Test fun doubleThenJokerConsumesDoubleButKeepsFixedJokerPoints() {
        val e2x = TestFixtures.engine(TestFixtures.special(SegmentType.DOUBLE))
        var s = e2x.spinAndResolve(TestFixtures.state())
        assertTrue(s.doubleActive)
        val joker = TestFixtures.engine(TestFixtures.special(SegmentType.JOKER))
        s = joker.chooseConsonant(joker.spinAndResolve(s), 'S')
        assertEquals(1000, s.players[0].score) // 2000 değil
        assertFalse(s.doubleActive)            // 2X tüketildi
        // Sonraki normal harf artık 2X almaz
        val e500 = TestFixtures.engine(TestFixtures.points(500))
        s = e500.finishReveal(s)
        val before = s.players[0].score
        val puz = TestFixtures.state(puzzle = com.ailecarki.tv.domain.model.Puzzle("x", "T", "KAKAO"))
        s = s.copy(puzzle = puz.puzzle, revealedLetters = emptySet(), usedLetters = emptySet())
        s = e500.chooseConsonant(e500.spinAndResolve(s), 'K')
        assertEquals(before + 1000, s.players[0].score) // 500 x 2 harf, çarpansız
    }

    @Test fun jokerWithoutDoubleIsFixed1000() {
        val e = TestFixtures.engine(TestFixtures.special(SegmentType.JOKER))
        val s = e.chooseConsonant(e.spinAndResolve(TestFixtures.state()), 'S') // 3 adet S
        assertEquals(1000, s.players[0].score)
    }

    @Test fun wrongLetterAfterDoubleClearsDoubleAndPassesTurn() {
        val e2x = TestFixtures.engine(TestFixtures.special(SegmentType.DOUBLE))
        var s = e2x.spinAndResolve(TestFixtures.state())
        val e500 = TestFixtures.engine(TestFixtures.points(500))
        s = e500.chooseConsonant(e500.spinAndResolve(s), 'K')
        assertFalse(s.doubleActive)
        assertEquals(1, s.currentPlayerIndex)
    }

    @Test fun finalRejectsGivenAndRepeatedLetters() {
        val e = TestFixtures.engine(TestFixtures.points(500))
        var s = e.nextRound(TestFixtures.state().copy(phase = GamePhase.ROUND_COMPLETE, round = 3), TestFixtures.PUZZLES)
        assertFalse(e.canPickFinal(s, 'R')) // verilen harf
        assertFalse(e.canPickFinal(s, 'E'))
        s = e.pickFinalLetter(s, 'K')
        assertFalse(e.canPickFinal(s, 'K')) // tekrar
        s = e.pickFinalLetter(s, 'A')
        assertFalse(e.canPickFinal(s, 'O')) // ikinci sesli yok
        assertEquals(listOf('K', 'A'), s.finalPicks)
    }

    @Test fun noActionPhaseIsDeadEnd() {
        // Tüm ünsüzler açık, puan yetersiz → yine de ÇÖZ her zaman mümkün
        val e = TestFixtures.engine(TestFixtures.points(500))
        val s = TestFixtures.state(scores = listOf(0, 0, 0)).copy(revealedLetters = setOf('S'), usedLetters = setOf('S'))
        assertFalse(e.canSpin(s))
        assertFalse(e.canBuyVowel(s))
        assertTrue(e.canSolve(s))
    }
}
