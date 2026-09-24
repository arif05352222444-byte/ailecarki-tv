package com.ailecarki.tv.domain.engine

import com.ailecarki.tv.domain.model.GameEvent
import com.ailecarki.tv.domain.model.GamePhase
import com.ailecarki.tv.domain.model.GameState
import com.ailecarki.tv.domain.model.Player
import com.ailecarki.tv.domain.model.Puzzle
import com.ailecarki.tv.domain.model.SegmentType
import com.ailecarki.tv.domain.rules.GameRules
import kotlin.random.Random

/**
 * Oyunun kalbi. Saf Kotlin, Android bağımlılığı yok → unit test edilebilir.
 * Her fonksiyon mevcut state'i alır, yeni state döndürür. Geçersiz çağrılarda state değişmeden döner.
 */
class GameEngine(
    val rules: GameRules,
    val wheel: WheelEngine,
    private val random: Random = Random.Default,
) {

    // ---------------------------------------------------------------- oyun kurulumu

    fun newGame(names: List<String>, puzzles: List<Puzzle>): GameState {
        require(names.size >= rules.minPlayers) { "En az ${rules.minPlayers} oyuncu gerekli" }
        val puzzle = PuzzleEngine.pickPuzzle(puzzles, emptySet(), random)
        return GameState(
            players = names.map { Player(it.trim()) },
            totalRounds = rules.normalRounds,
            puzzle = puzzle,
            usedPuzzleIds = setOf(puzzle.id),
        )
    }

    // ---------------------------------------------------------------- sorgular

    private val actionPhases = setOf(GamePhase.PLAYER_TURN, GamePhase.PLAYER_ACTION)

    fun canSpin(s: GameState): Boolean =
        s.phase in actionPhases &&
            PuzzleEngine.remainingConsonants(s.puzzle.answer, s.revealedLetters).isNotEmpty()

    fun canBuyVowel(s: GameState): Boolean =
        s.phase in actionPhases &&
            ScoreEngine.canBuyVowel(s.currentPlayer.score, rules.vowelCost) &&
            TurkishAlphabet.VOWELS.any { it !in s.usedLetters }

    fun canSolve(s: GameState): Boolean = s.phase in actionPhases

    fun canSelectLetter(s: GameState, letter: Char): Boolean {
        if (letter in s.usedLetters) return false
        return when (s.phase) {
            GamePhase.LETTER_SELECTION -> TurkishAlphabet.isConsonant(letter)
            GamePhase.VOWEL_SELECTION -> TurkishAlphabet.isVowel(letter)
            GamePhase.FINAL_LETTER_SELECTION -> canPickFinal(s, letter)
            else -> false
        }
    }

    fun currentSegment(s: GameState) = s.spinSegmentIndex?.let { wheel.segments[it] }

    // ---------------------------------------------------------------- çark

    fun startSpin(s: GameState): GameState {
        if (!canSpin(s)) return s
        return s.copy(
            phase = GamePhase.WHEEL_SPINNING,
            spinSegmentIndex = wheel.pickSegmentIndex(),
            spinCount = s.spinCount + 1,
        )
    }

    fun onSpinFinished(s: GameState): GameState =
        if (s.phase == GamePhase.WHEEL_SPINNING) s.copy(phase = GamePhase.WHEEL_RESULT) else s

    /** Sonuç ekranda gösterildikten sonra çağrılır; dilimin etkisini uygular. */
    fun resolveWheelResult(s: GameState): GameState {
        if (s.phase != GamePhase.WHEEL_RESULT) return s
        val segment = currentSegment(s) ?: return s
        return when (segment.type) {
            SegmentType.POINTS, SegmentType.JOKER -> s.copy(phase = GamePhase.LETTER_SELECTION)
            SegmentType.BANKRUPT -> {
                val players = s.players.updated(s.currentPlayerIndex) {
                    ScoreEngine.applyBankrupt(it, rules.bankruptPolicy)
                }
                advanceTurn(s.copy(players = players), GameEvent.Bankrupt)
            }
            SegmentType.LOSE_TURN -> advanceTurn(s, GameEvent.LoseTurn)
            SegmentType.DOUBLE -> s.copy(phase = GamePhase.PLAYER_TURN, doubleActive = true)
                .withEvent(GameEvent.DoubleActivated)
        }
    }

    // ---------------------------------------------------------------- harfler

    fun chooseConsonant(s: GameState, letter: Char): GameState {
        if (s.phase != GamePhase.LETTER_SELECTION || !canSelectLetter(s, letter)) return s
        val segment = currentSegment(s) ?: return s
        val count = PuzzleEngine.letterCount(s.puzzle.answer, letter)
        val used = s.usedLetters + letter
        if (count == 0) {
            return advanceTurn(s.copy(usedLetters = used), GameEvent.LetterMissing(letter))
        }
        val isJoker = segment.type == SegmentType.JOKER
        val points = if (isJoker) {
            ScoreEngine.jokerPoints(count, rules.jokerPoints)
        } else {
            val multiplier = if (s.doubleActive) rules.doubleMultiplier else 1
            ScoreEngine.consonantPoints(segment.value, count, multiplier)
        }
        return s.copy(
            players = s.players.updated(s.currentPlayerIndex) { ScoreEngine.addPoints(it, points) },
            usedLetters = used,
            revealedLetters = s.revealedLetters + letter,
            lastRevealedLetter = letter,
            // 2X, sonraki BAŞARILI ünsüz seçiminde tüketilir — Joker üzerinden olsa bile.
            // Joker puanı sabit kalır (2X ile katlanmaz).
            doubleActive = false,
            phase = GamePhase.LETTER_REVEAL,
        ).withEvent(GameEvent.LetterFound(letter, count, points))
    }

    fun startBuyVowel(s: GameState): GameState =
        if (canBuyVowel(s)) s.copy(phase = GamePhase.VOWEL_SELECTION) else s

    fun cancelVowel(s: GameState): GameState =
        if (s.phase == GamePhase.VOWEL_SELECTION) s.copy(phase = GamePhase.PLAYER_ACTION) else s

    fun chooseVowel(s: GameState, letter: Char): GameState {
        if (s.phase != GamePhase.VOWEL_SELECTION || !canSelectLetter(s, letter)) return s
        if (!ScoreEngine.canBuyVowel(s.currentPlayer.score, rules.vowelCost)) return s
        val count = PuzzleEngine.letterCount(s.puzzle.answer, letter)
        val players = s.players.updated(s.currentPlayerIndex) { ScoreEngine.addPoints(it, -rules.vowelCost) }
        val base = s.copy(players = players, usedLetters = s.usedLetters + letter)
        val event = GameEvent.VowelBought(letter, count, rules.vowelCost)
        return if (count > 0) {
            base.copy(
                revealedLetters = s.revealedLetters + letter,
                lastRevealedLetter = letter,
                phase = GamePhase.LETTER_REVEAL,
            ).withEvent(event)
        } else {
            // Kural: sesli harf alındıktan sonra sıra aynı oyuncuda kalır.
            base.copy(phase = GamePhase.PLAYER_ACTION).withEvent(event)
        }
    }

    /** Harf açılma animasyonu bittiğinde çağrılır. */
    fun finishReveal(s: GameState): GameState {
        if (s.phase != GamePhase.LETTER_REVEAL) return s
        return if (PuzzleEngine.isFullyRevealed(s.puzzle.answer, s.revealedLetters)) {
            completeRound(s, s.currentPlayerIndex, event = null)
        } else {
            s.copy(phase = GamePhase.PLAYER_ACTION)
        }
    }

    // ---------------------------------------------------------------- çözme

    fun startSolve(s: GameState): GameState =
        if (canSolve(s)) s.copy(phase = GamePhase.SOLVING) else s

    fun cancelSolve(s: GameState): GameState =
        if (s.phase == GamePhase.SOLVING) s.copy(phase = GamePhase.PLAYER_ACTION) else s

    fun submitSolve(s: GameState, text: String): GameState {
        if (s.phase != GamePhase.SOLVING) return s
        return if (AnswerNormalizer.matches(text, s.puzzle.answer, rules.lenientAnswerMatching)) {
            completeRound(s, s.currentPlayerIndex, GameEvent.CorrectAnswer(s.currentPlayerIndex))
        } else {
            advanceTurn(s, GameEvent.WrongAnswer(s.currentPlayerIndex))
        }
    }

    // ---------------------------------------------------------------- tur geçişi

    private fun completeRound(s: GameState, winner: Int, event: GameEvent?): GameState {
        val players = s.players.updated(winner) {
            ScoreEngine.addPoints(it, rules.roundWinBonus).copy(roundsWon = it.roundsWon + 1)
        }
        val next = s.copy(
            players = players,
            revealedLetters = s.revealedLetters + PuzzleEngine.lettersIn(s.puzzle.answer),
            phase = GamePhase.ROUND_COMPLETE,
            roundWinnerIndex = winner,
            doubleActive = false,
        )
        return if (event != null) next.withEvent(event) else next
    }

    fun isLastNormalRound(s: GameState): Boolean = s.round >= s.totalRounds

    fun nextRound(s: GameState, puzzles: List<Puzzle>): GameState {
        if (s.phase != GamePhase.ROUND_COMPLETE) return s
        if (isLastNormalRound(s)) return startFinal(s, puzzles)
        val puzzle = PuzzleEngine.pickPuzzle(puzzles, s.usedPuzzleIds, random)
        return s.copy(
            players = s.players.map { it.copy(roundScore = 0) },
            round = s.round + 1,
            currentPlayerIndex = s.round % s.players.size, // her tur farklı oyuncu başlar
            puzzle = puzzle,
            usedPuzzleIds = s.usedPuzzleIds + puzzle.id,
            revealedLetters = emptySet(),
            usedLetters = emptySet(),
            lastRevealedLetter = null,
            spinSegmentIndex = null,
            doubleActive = false,
            roundWinnerIndex = null,
            phase = GamePhase.PLAYER_TURN,
        )
    }

    // ---------------------------------------------------------------- final

    fun startFinal(s: GameState, puzzles: List<Puzzle>): GameState {
        val finalist = TieBreaker.pickFinalist(s.players)
        val given = rules.finalGivenLetters
        val puzzle = PuzzleEngine.pickPuzzle(puzzles, s.usedPuzzleIds, random) {
            PuzzleEngine.hiddenTileCount(it.answer, given) >= rules.finalMinHiddenTiles
        }
        return s.copy(
            players = s.players.map { it.copy(roundScore = 0) },
            round = s.totalRounds + 1,
            isFinal = true,
            finalistIndex = finalist,
            currentPlayerIndex = finalist,
            puzzle = puzzle,
            usedPuzzleIds = s.usedPuzzleIds + puzzle.id,
            revealedLetters = given,
            usedLetters = given,
            lastRevealedLetter = null,
            finalPicks = emptyList(),
            spinSegmentIndex = null,
            doubleActive = false,
            roundWinnerIndex = null,
            phase = GamePhase.FINAL_LETTER_SELECTION,
        )
    }

    fun finalConsonantsPicked(s: GameState) = s.finalPicks.count { TurkishAlphabet.isConsonant(it) }
    fun finalVowelsPicked(s: GameState) = s.finalPicks.count { TurkishAlphabet.isVowel(it) }

    fun canPickFinal(s: GameState, letter: Char): Boolean {
        if (s.phase != GamePhase.FINAL_LETTER_SELECTION || letter in s.usedLetters) return false
        return when {
            TurkishAlphabet.isConsonant(letter) -> finalConsonantsPicked(s) < rules.finalConsonantPicks
            TurkishAlphabet.isVowel(letter) -> finalVowelsPicked(s) < rules.finalVowelPicks
            else -> false
        }
    }

    fun pickFinalLetter(s: GameState, letter: Char): GameState {
        if (!canPickFinal(s, letter)) return s
        val picks = s.finalPicks + letter
        val next = s.copy(finalPicks = picks, usedLetters = s.usedLetters + letter)
        val done = picks.size >= rules.finalConsonantPicks + rules.finalVowelPicks
        return if (done) {
            next.copy(
                revealedLetters = next.revealedLetters + picks,
                lastRevealedLetter = null,
                phase = GamePhase.FINAL_REVEAL,
            )
        } else next
    }

    fun finishFinalReveal(s: GameState): GameState =
        if (s.phase == GamePhase.FINAL_REVEAL) s.copy(phase = GamePhase.FINAL_SOLVING) else s

    fun submitFinal(s: GameState, text: String): GameState {
        if (s.phase != GamePhase.FINAL_SOLVING) return s
        val won = AnswerNormalizer.matches(text, s.puzzle.answer, rules.lenientAnswerMatching)
        return endFinal(s, won)
    }

    fun finalTimeout(s: GameState): GameState =
        if (s.phase == GamePhase.FINAL_SOLVING) endFinal(s, false) else s

    private fun endFinal(s: GameState, won: Boolean): GameState = s.copy(
        revealedLetters = s.revealedLetters + PuzzleEngine.lettersIn(s.puzzle.answer),
        finalWon = won,
        phase = GamePhase.GAME_COMPLETE,
    ).withEvent(GameEvent.FinalResult(won))

    /** Genel kazanan: en yüksek puan (TieBreaker kuralları). */
    fun overallWinner(s: GameState): Int = TieBreaker.pickFinalist(s.players)

    // ---------------------------------------------------------------- kayıttan devam

    /** Kaydedilmiş bir oyunu animasyon ortasında kalmış fazlardan güvenli bir faza taşır. */
    fun sanitizeForResume(s: GameState): GameState {
        val base = s.copy(lastEvent = null)
        return when (base.phase) {
            GamePhase.WHEEL_SPINNING -> base.copy(phase = GamePhase.WHEEL_RESULT)
            GamePhase.LETTER_REVEAL -> finishReveal(base)
            GamePhase.FINAL_REVEAL -> finishFinalReveal(base)
            GamePhase.SOLVING, GamePhase.VOWEL_SELECTION -> base.copy(phase = GamePhase.PLAYER_ACTION)
            else -> base
        }
    }

    // ---------------------------------------------------------------- yardımcılar

    private fun advanceTurn(s: GameState, event: GameEvent): GameState = s.copy(
        currentPlayerIndex = (s.currentPlayerIndex + 1) % s.players.size,
        phase = GamePhase.PLAYER_TURN,
        doubleActive = false,
        spinSegmentIndex = null,
    ).withEvent(event)

    private fun GameState.withEvent(e: GameEvent) = copy(lastEvent = e, eventCounter = eventCounter + 1)

    private inline fun List<Player>.updated(index: Int, f: (Player) -> Player): List<Player> =
        mapIndexed { i, p -> if (i == index) f(p) else p }
}
