package com.ailecarki.tv.data

import android.content.Context
import android.util.Log
import com.ailecarki.tv.domain.engine.AnswerNormalizer
import com.ailecarki.tv.domain.model.Puzzle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class PuzzleDto(val id: String, val category: String, val answer: String)

/** assets/puzzles/*.json altındaki tüm dosyaları okur. Yeni kategori için yeni JSON eklemek yeterli. */
class PuzzleRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cache: List<Puzzle>? = null

    suspend fun load(): List<Puzzle> = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) { readAll() }.also { cache = it }
    }

    private fun readAll(): List<Puzzle> {
        val files = context.assets.list(DIR).orEmpty().filter { it.endsWith(".json") }
        val result = files.flatMap { file ->
            runCatching {
                val text = context.assets.open("$DIR/$file").bufferedReader(Charsets.UTF_8).use { it.readText() }
                json.decodeFromString(ListSerializer(PuzzleDto.serializer()), text)
            }.onFailure { Log.e(TAG, "Kelime dosyası okunamadı: $file", it) }.getOrDefault(emptyList())
        }
            .map { Puzzle(it.id, it.category.trim(), AnswerNormalizer.normalize(it.answer)) }
            .filter { it.answer.isNotBlank() }
            .distinctBy { it.id }
        Log.i(TAG, "${result.size} soru yüklendi")
        return result
    }

    companion object {
        private const val DIR = "puzzles"
        private const val TAG = "AileCarki/Puzzles"
    }
}
