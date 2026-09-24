package com.ailecarki.tv.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ailecarki.tv.domain.model.GamePhase
import com.ailecarki.tv.domain.model.GameState
import com.ailecarki.tv.domain.model.Player
import com.ailecarki.tv.domain.model.Puzzle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SavedPlayer(val name: String, val score: Int, val roundScore: Int, val roundsWon: Int)

@Serializable
data class SavedGame(
    val version: Int = 1,
    val players: List<SavedPlayer>,
    val currentPlayerIndex: Int,
    val round: Int,
    val totalRounds: Int,
    val puzzleId: String,
    val puzzleCategory: String,
    val puzzleAnswer: String,
    val revealed: String,
    val used: String,
    val phase: String,
    val spinSegmentIndex: Int? = null,
    val spinCount: Int = 0,
    val doubleActive: Boolean = false,
    val roundWinnerIndex: Int? = null,
    val usedPuzzleIds: List<String> = emptyList(),
    val isFinal: Boolean = false,
    val finalistIndex: Int? = null,
    val finalPicks: String = "",
    val finalWon: Boolean? = null,
)

/** Aktif oyunu JSON olarak DataStore'a yazar; DEVAM ET buradan çalışır. */
class SavedGameRepository(private val store: DataStore<Preferences>) {
    private val key = stringPreferencesKey("saved_game_v1")
    private val json = Json { ignoreUnknownKeys = true }

    val hasSavedGame: Flow<Boolean> = store.data.map { !it[key].isNullOrBlank() }

    suspend fun save(state: GameState) {
        val text = json.encodeToString(SavedGame.serializer(), state.toSaved())
        store.edit { it[key] = text }
    }

    suspend fun load(): GameState? {
        val text = store.data.first()[key] ?: return null
        return runCatching { json.decodeFromString(SavedGame.serializer(), text).toState() }
            .onFailure { Log.w("AileCarki/Save", "Kayıt okunamadı, siliniyor", it); clear() }
            .getOrNull()
    }

    suspend fun clear() {
        store.edit { it.remove(key) }
    }

    private fun GameState.toSaved() = SavedGame(
        players = players.map { SavedPlayer(it.name, it.score, it.roundScore, it.roundsWon) },
        currentPlayerIndex = currentPlayerIndex,
        round = round,
        totalRounds = totalRounds,
        puzzleId = puzzle.id,
        puzzleCategory = puzzle.category,
        puzzleAnswer = puzzle.answer,
        revealed = revealedLetters.joinToString(""),
        used = usedLetters.joinToString(""),
        phase = phase.name,
        spinSegmentIndex = spinSegmentIndex,
        spinCount = spinCount,
        doubleActive = doubleActive,
        roundWinnerIndex = roundWinnerIndex,
        usedPuzzleIds = usedPuzzleIds.toList(),
        isFinal = isFinal,
        finalistIndex = finalistIndex,
        finalPicks = finalPicks.joinToString(""),
        finalWon = finalWon,
    )

    private fun SavedGame.toState() = GameState(
        players = players.map { Player(it.name, it.score, it.roundScore, it.roundsWon) },
        currentPlayerIndex = currentPlayerIndex,
        round = round,
        totalRounds = totalRounds,
        puzzle = Puzzle(puzzleId, puzzleCategory, puzzleAnswer),
        revealedLetters = revealed.toSet(),
        usedLetters = used.toSet(),
        phase = GamePhase.valueOf(phase),
        spinSegmentIndex = spinSegmentIndex,
        spinCount = spinCount,
        doubleActive = doubleActive,
        roundWinnerIndex = roundWinnerIndex,
        usedPuzzleIds = usedPuzzleIds.toSet(),
        isFinal = isFinal,
        finalistIndex = finalistIndex,
        finalPicks = finalPicks.toList(),
        finalWon = finalWon,
    )
}
