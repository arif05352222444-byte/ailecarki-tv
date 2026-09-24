package com.ailecarki.tv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ailecarki.tv.AileCarkiApp
import com.ailecarki.tv.audio.SoundId
import com.ailecarki.tv.data.AppSettings
import com.ailecarki.tv.domain.engine.GameEngine
import com.ailecarki.tv.domain.engine.TurkishAlphabet
import com.ailecarki.tv.domain.engine.WheelEngine
import com.ailecarki.tv.domain.model.GameEvent
import com.ailecarki.tv.domain.model.GamePhase
import com.ailecarki.tv.domain.model.GameState
import com.ailecarki.tv.domain.model.Puzzle
import com.ailecarki.tv.domain.rules.GameRules
import com.ailecarki.tv.domain.rules.WheelConfig
import com.ailecarki.tv.util.GameStateLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SpinPlan(val targetIndex: Int, val extraTurns: Int, val jitter: Float, val durationMs: Int)

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val container = (app as AileCarkiApp).container
    private val audio get() = container.audio

    private var engine = GameEngine(GameRules(), WheelEngine(WheelConfig.DEFAULT))
    private var puzzles: List<Puzzle> = emptyList()

    private val _state = MutableStateFlow<GameState?>(null)
    val state: StateFlow<GameState?> = _state.asStateFlow()

    private val _names = MutableStateFlow(List(GameRules().defaultPlayers) { "" })
    val names: StateFlow<List<String>> = _names.asStateFlow()

    private val _finalSecondsLeft = MutableStateFlow(0)
    val finalSecondsLeft: StateFlow<Int> = _finalSecondsLeft.asStateFlow()

    val hasSavedGame: StateFlow<Boolean> =
        container.savedGames.hasSavedGame.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private var timerJob: Job? = null

    val rules: GameRules get() = engine.rules
    val wheel: WheelEngine get() = engine.wheel

    init {
        viewModelScope.launch { puzzles = container.puzzles.load() }
    }

    // ------------------------------------------------------------ oyuncu kurulumu

    fun setName(index: Int, name: String) {
        val clean = TurkishAlphabet.upper(name.trim()).take(rules.maxNameLength)
        _names.value = _names.value.toMutableList().also { if (index in it.indices) it[index] = clean }
    }

    fun addPlayer() {
        if (_names.value.size < rules.maxPlayers) _names.value = _names.value + ""
    }

    fun removePlayer() {
        if (_names.value.size > rules.minPlayers) _names.value = _names.value.dropLast(1)
    }

    fun fillRandomNames() {
        val taken = _names.value.toMutableSet()
        val pool = RANDOM_NAMES.filter { it !in taken }.shuffled().iterator()
        _names.value = _names.value.map { if (it.isBlank() && pool.hasNext()) pool.next() else it }
    }

    fun namesValid(): Boolean = _names.value.size >= rules.minPlayers && _names.value.all { it.isNotBlank() }
    fun hasDuplicateNames(): Boolean = _names.value.filter { it.isNotBlank() }.let { it.size != it.toSet().size }

    // ------------------------------------------------------------ başlat / devam et

    private suspend fun buildEngine() {
        val s = container.settings.settings.first()
        engine = GameEngine(rules = s.toRules(), wheel = WheelEngine(WheelConfig.DEFAULT))
        if (puzzles.isEmpty()) puzzles = container.puzzles.load()
    }

    fun startNewGame(onReady: () -> Unit) {
        if (!namesValid()) return
        viewModelScope.launch {
            buildEngine()
            timerJob?.cancel()
            _state.value = null
            update(engine.newGame(_names.value, puzzles))
            audio.playVoice(SoundId.VOICE_WELCOME)
            onReady()
        }
    }

    fun resumeGame(onReady: (Boolean) -> Unit) {
        viewModelScope.launch {
            buildEngine()
            val saved = container.savedGames.load()
            if (saved == null) { onReady(false); return@launch }
            _names.value = saved.players.map { it.name }
            _state.value = null
            update(engine.sanitizeForResume(saved))
            onReady(true)
        }
    }

    fun leaveGame() {
        timerJob?.cancel()
    }

    // ------------------------------------------------------------ oyun aksiyonları

    private inline fun act(f: (GameState) -> GameState) {
        val s = _state.value ?: return
        val n = f(s)
        if (n !== s) update(n)
    }

    fun canSpin() = _state.value?.let(engine::canSpin) ?: false
    fun canBuyVowel() = _state.value?.let(engine::canBuyVowel) ?: false
    fun canSelectLetter(c: Char) = _state.value?.let { engine.canSelectLetter(it, c) } ?: false

    fun spin() = act(engine::startSpin)

    fun spinPlan(): SpinPlan? {
        val s = _state.value ?: return null
        val idx = s.spinSegmentIndex ?: return null
        return SpinPlan(idx, wheel.randomExtraTurns(), wheel.randomJitter(), wheel.randomDurationMs())
    }

    fun onSpinFinished() {
        audio.playEffect(SoundId.FX_WHEEL_STOP)
        act(engine::onSpinFinished)
    }

    fun resolveWheelResult() = act(engine::resolveWheelResult)

    fun chooseLetter(c: Char) = act { s ->
        when (s.phase) {
            GamePhase.LETTER_SELECTION -> engine.chooseConsonant(s, c)
            GamePhase.VOWEL_SELECTION -> engine.chooseVowel(s, c)
            GamePhase.FINAL_LETTER_SELECTION -> engine.pickFinalLetter(s, c)
            else -> s
        }
    }

    fun finishReveal() = act(engine::finishReveal)
    fun buyVowel() = act(engine::startBuyVowel)
    fun cancelVowel() = act(engine::cancelVowel)
    fun startSolve() = act(engine::startSolve)
    fun cancelSolve() = act(engine::cancelSolve)
    fun submitSolve(text: String) = act { engine.submitSolve(it, text) }
    fun nextRound() = act { engine.nextRound(it, puzzles) }
    fun finishFinalReveal() = act(engine::finishFinalReveal)

    fun submitFinal(text: String) {
        timerJob?.cancel()
        act { engine.submitFinal(it, text) }
    }

    fun isLastNormalRound(): Boolean = _state.value?.let(engine::isLastNormalRound) ?: false
    fun overallWinner(): Int = _state.value?.let(engine::overallWinner) ?: 0
    fun finalConsonantsPicked() = _state.value?.let(engine::finalConsonantsPicked) ?: 0
    fun finalVowelsPicked() = _state.value?.let(engine::finalVowelsPicked) ?: 0

    // ------------------------------------------------------------ iç akış

    private fun update(new: GameState) {
        val old = _state.value
        _state.value = new
        GameStateLogger.log(old, new)
        if (old == null || old.eventCounter != new.eventCounter) new.lastEvent?.let(::playEventSound)
        onPhaseChanged(old, new)
        persist(new)
    }

    private fun onPhaseChanged(old: GameState?, new: GameState) {
        if (old?.phase == new.phase) return
        when (new.phase) {
            GamePhase.WHEEL_SPINNING -> audio.playEffect(SoundId.FX_WHEEL_SPIN)
            GamePhase.ROUND_COMPLETE -> audio.playVoice(SoundId.VOICE_ROUND_COMPLETE)
            GamePhase.FINAL_LETTER_SELECTION -> {
                audio.playMusic(SoundId.MUSIC_FINAL)
                audio.playVoice(SoundId.VOICE_FINAL_INTRO)
            }
            GamePhase.FINAL_SOLVING -> startFinalTimer()
            GamePhase.GAME_COMPLETE -> timerJob?.cancel()
            else -> Unit
        }
    }

    private fun playEventSound(e: GameEvent) {
        when (e) {
            is GameEvent.LetterFound -> audio.playEffect(SoundId.FX_LETTER_REVEAL)
            is GameEvent.VowelBought ->
                audio.playEffect(if (e.count > 0) SoundId.FX_LETTER_REVEAL else SoundId.FX_LETTER_WRONG)
            is GameEvent.LetterMissing -> audio.playEffect(SoundId.FX_LETTER_WRONG)
            GameEvent.Bankrupt -> { audio.playEffect(SoundId.FX_BANKRUPT); audio.playVoice(SoundId.VOICE_BANKRUPT) }
            GameEvent.LoseTurn -> audio.playEffect(SoundId.FX_LOSE_TURN)
            GameEvent.DoubleActivated -> audio.playEffect(SoundId.FX_DOUBLE)
            is GameEvent.CorrectAnswer -> { audio.playEffect(SoundId.FX_CORRECT); audio.playVoice(SoundId.VOICE_CORRECT) }
            is GameEvent.WrongAnswer -> { audio.playEffect(SoundId.FX_WRONG); audio.playVoice(SoundId.VOICE_WRONG) }
            is GameEvent.FinalResult -> {
                audio.playEffect(if (e.won) SoundId.FX_CELEBRATION else SoundId.FX_TIMEOUT)
                audio.playVoice(if (e.won) SoundId.VOICE_FINAL_WIN else SoundId.VOICE_FINAL_LOSE)
            }
        }
    }

    private fun startFinalTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (sec in rules.finalSeconds downTo 1) {
                _finalSecondsLeft.value = sec
                if (sec <= 5) audio.playEffect(SoundId.FX_TIMER_TICK)
                delay(1000)
            }
            _finalSecondsLeft.value = 0
            act(engine::finalTimeout)
        }
    }

    private fun persist(s: GameState) {
        viewModelScope.launch {
            runCatching {
                if (s.phase == GamePhase.GAME_COMPLETE) container.savedGames.clear()
                else container.savedGames.save(s)
            }
        }
    }

    companion object {
        val RANDOM_NAMES = listOf(
            "ANNE", "BABA", "DEDE", "NİNE", "TEYZE", "DAYI", "AMCA", "HALA",
            "ABLA", "ABİ", "KUZEN", "ENİŞTE", "YENGE", "MİNİK",
        )
    }
}

fun AppSettings.toRules() = GameRules(normalRounds = rounds, vowelCost = vowelCost, finalSeconds = finalSeconds)
