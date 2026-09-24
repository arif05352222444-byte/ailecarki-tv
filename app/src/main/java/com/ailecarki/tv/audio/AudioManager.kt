package com.ailecarki.tv.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log

/**
 * Sesin tek giriş noktası. Dosya yoksa sessizce devam eder (derleme/çalışma hatası yok).
 * Sunucu (VOICE) konuşurken müzik otomatik kısılır (ducking).
 */
class AudioManager(private val context: Context) {
    private val attrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attrs).build()
    private val poolIds = HashMap<SoundId, Int>()
    private val paths = HashMap<SoundId, String>()

    private var musicPlayer: MediaPlayer? = null
    private var currentMusic: SoundId? = null
    private var voicePlayer: MediaPlayer? = null
    private var ducked = false

    private var musicVolume = 0.6f
    private var voiceVolume = 1f
    private var effectsVolume = 0.8f
    private var voiceEnabled = true
    private var uiEnabled = true

    init {
        indexAssets()
        paths.filterKeys { it.category == AudioCategory.EFFECT || it.category == AudioCategory.UI }
            .forEach { (id, path) ->
                runCatching { context.assets.openFd(path).use { poolIds[id] = soundPool.load(it, 1) } }
                    .onFailure { Log.w(TAG, "Yüklenemedi: $path", it) }
            }
        Log.i(TAG, "Ses paketi: ${paths.size}/${SoundId.entries.size} dosya bulundu")
    }

    private fun indexAssets() {
        fun list(dir: String) = runCatching { context.assets.list(dir).orEmpty().toSet() }.getOrDefault(emptySet())
        val rootFiles = list(ROOT)
        for (category in AudioCategory.entries) {
            val dir = "$ROOT/${category.folder}"
            val files = list(dir)
            for (id in SoundId.entries.filter { it.category == category }) {
                val names = AudioManifest.candidates(id).flatMap { base -> AudioManifest.extensions.map { "$base.$it" } }
                val path = names.firstOrNull { it in files }?.let { "$dir/$it" }
                    ?: names.firstOrNull { it in rootFiles }?.let { "$ROOT/$it" }
                if (path != null) paths[id] = path
            }
        }
    }

    fun has(id: SoundId): Boolean = paths.containsKey(id)

    fun play(id: SoundId) = when (id.category) {
        AudioCategory.VOICE -> playVoice(id)
        AudioCategory.MUSIC -> playMusic(id)
        else -> playEffect(id)
    }

    fun playEffect(id: SoundId) {
        if (id.category == AudioCategory.UI && !uiEnabled) return
        val sid = poolIds[id] ?: return
        val v = effectsVolume
        soundPool.play(sid, v, v, 1, 0, 1f)
    }

    fun playVoice(id: SoundId) {
        if (!voiceEnabled) return
        val path = paths[id] ?: return
        releaseVoice()
        voicePlayer = createPlayer(path, loop = false, volume = voiceVolume)?.apply {
            setOnCompletionListener { releaseVoice() }
            duck(true)
            start()
        }
    }

    fun playMusic(id: SoundId, loop: Boolean = true) {
        if (currentMusic == id && musicPlayer != null) return
        stopMusic()
        currentMusic = id
        val path = paths[id] ?: return
        musicPlayer = createPlayer(path, loop, effectiveMusicVolume())?.apply { start() }
    }

    fun stopMusic() {
        musicPlayer?.runCatching { stop(); release() }
        musicPlayer = null
        currentMusic = null
    }

    fun pauseAll() {
        musicPlayer?.runCatching { if (isPlaying) pause() }
        releaseVoice()
    }

    fun resumeAll() {
        musicPlayer?.runCatching { start() }
    }

    fun setMusicVolume(v: Float) { musicVolume = v.coerceIn(0f, 1f); applyMusicVolume() }
    fun setVoiceVolume(v: Float) { voiceVolume = v.coerceIn(0f, 1f) }
    fun setEffectsVolume(v: Float) { effectsVolume = v.coerceIn(0f, 1f) }
    fun setVoiceEnabled(enabled: Boolean) { voiceEnabled = enabled; if (!enabled) releaseVoice() }
    fun setUiSoundsEnabled(enabled: Boolean) { uiEnabled = enabled }

    fun release() {
        stopMusic(); releaseVoice(); soundPool.release()
    }

    private fun duck(on: Boolean) { ducked = on; applyMusicVolume() }
    private fun effectiveMusicVolume() = if (ducked) musicVolume * DUCK_FACTOR else musicVolume
    private fun applyMusicVolume() {
        val v = effectiveMusicVolume()
        musicPlayer?.runCatching { setVolume(v, v) }
    }

    private fun releaseVoice() {
        voicePlayer?.runCatching { stop(); release() }
        voicePlayer = null
        if (ducked) duck(false)
    }

    private fun createPlayer(path: String, loop: Boolean, volume: Float): MediaPlayer? = runCatching {
        MediaPlayer().apply {
            context.assets.openFd(path).use { setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            setAudioAttributes(attrs)
            isLooping = loop
            setVolume(volume, volume)
            prepare()
        }
    }.onFailure { Log.w(TAG, "Oynatılamadı: $path", it) }.getOrNull()

    companion object {
        private const val TAG = "AileCarki/Audio"
        private const val ROOT = "audio"
        private const val DUCK_FACTOR = 0.25f
    }
}
