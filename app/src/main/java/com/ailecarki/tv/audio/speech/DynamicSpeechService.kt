package com.ailecarki.tv.audio.speech

import android.util.Log
import java.io.File

sealed interface AudioResult {
    data class Ready(val file: File) : AudioResult
    data object Unavailable : AudioResult
    data class Error(val message: String) : AudioResult
}

/**
 * Oyuncu isimli canlı sunucu cümleleri için soyutlama.
 * Üretimde ElevenLabsDynamicSpeechService bir backend/proxy üzerinden çalışacak;
 * API anahtarı ASLA APK içine konmaz.
 */
interface DynamicSpeechService {
    suspend fun speak(text: String): AudioResult
}

class MockDynamicSpeechService : DynamicSpeechService {
    override suspend fun speak(text: String): AudioResult {
        Log.d("AileCarki/Speech", "[mock] $text")
        return AudioResult.Unavailable
    }
}

object SpeechTemplates {
    const val YOUR_TURN = "{player}, sıra sende. Çarkı çevir!"
    const val CORRECT = "Tebrikler {player}!"
    const val FINALIST = "{player}, finale hoş geldin!"

    fun format(template: String, player: String) = template.replace("{player}", player)
}
