package com.ailecarki.tv

import android.content.Context
import com.ailecarki.tv.audio.AudioManager
import com.ailecarki.tv.audio.speech.DynamicSpeechService
import com.ailecarki.tv.audio.speech.MockDynamicSpeechService
import com.ailecarki.tv.data.AppSettings
import com.ailecarki.tv.data.PuzzleRepository
import com.ailecarki.tv.data.SavedGameRepository
import com.ailecarki.tv.data.SettingsRepository
import com.ailecarki.tv.data.appDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Basit bağımlılık kabı (DI kütüphanesi kullanmadan). */
class AppContainer(context: Context) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val puzzles = PuzzleRepository(context)
    val settings = SettingsRepository(context.appDataStore)
    val savedGames = SavedGameRepository(context.appDataStore)
    val audio = AudioManager(context)
    /** İleride ElevenLabsDynamicSpeechService ile değiştirilecek. */
    val speech: DynamicSpeechService = MockDynamicSpeechService()

    init {
        appScope.launch {
            settings.settings.collect { applyAudio(it) }
        }
    }

    private fun applyAudio(s: AppSettings) {
        audio.setMusicVolume(s.musicLevel / AppSettings.MAX_LEVEL.toFloat())
        audio.setEffectsVolume(s.effectsLevel / AppSettings.MAX_LEVEL.toFloat())
        audio.setVoiceEnabled(s.hostVoice)
        audio.setUiSoundsEnabled(s.uiSounds)
    }
}
