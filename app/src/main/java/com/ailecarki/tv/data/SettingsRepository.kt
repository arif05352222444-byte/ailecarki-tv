package com.ailecarki.tv.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val hostVoice: Boolean = true,
    val musicLevel: Int = 6,
    val effectsLevel: Int = 8,
    val uiSounds: Boolean = true,
    val rounds: Int = 3,
    val vowelCost: Int = 250,
    val finalSeconds: Int = 20,
) {
    companion object {
        const val MAX_LEVEL = 10
        val ROUND_OPTIONS = listOf(3, 5)
        val VOWEL_COST_OPTIONS = listOf(100, 250, 500)
        val FINAL_SECONDS_OPTIONS = listOf(20, 30)
    }
}

class SettingsRepository(private val store: DataStore<Preferences>) {
    private object Keys {
        val HOST = booleanPreferencesKey("host_voice")
        val MUSIC = intPreferencesKey("music_level")
        val EFFECTS = intPreferencesKey("effects_level")
        val UI = booleanPreferencesKey("ui_sounds")
        val ROUNDS = intPreferencesKey("rounds")
        val VOWEL = intPreferencesKey("vowel_cost")
        val FINAL = intPreferencesKey("final_seconds")
    }

    val settings: Flow<AppSettings> = store.data.map { p ->
        val d = AppSettings()
        AppSettings(
            hostVoice = p[Keys.HOST] ?: d.hostVoice,
            musicLevel = p[Keys.MUSIC] ?: d.musicLevel,
            effectsLevel = p[Keys.EFFECTS] ?: d.effectsLevel,
            uiSounds = p[Keys.UI] ?: d.uiSounds,
            rounds = p[Keys.ROUNDS] ?: d.rounds,
            vowelCost = p[Keys.VOWEL] ?: d.vowelCost,
            finalSeconds = p[Keys.FINAL] ?: d.finalSeconds,
        )
    }

    suspend fun update(s: AppSettings) {
        store.edit { p ->
            p[Keys.HOST] = s.hostVoice
            p[Keys.MUSIC] = s.musicLevel
            p[Keys.EFFECTS] = s.effectsLevel
            p[Keys.UI] = s.uiSounds
            p[Keys.ROUNDS] = s.rounds
            p[Keys.VOWEL] = s.vowelCost
            p[Keys.FINAL] = s.finalSeconds
        }
    }
}
