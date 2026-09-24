package com.ailecarki.tv.util

import android.util.Log
import com.ailecarki.tv.domain.model.GameState

object GameStateLogger {
    private const val TAG = "AileCarki/State"
    var enabled = true

    fun log(old: GameState?, new: GameState) {
        if (!enabled) return
        if (old?.phase != new.phase || old.currentPlayerIndex != new.currentPlayerIndex || old.eventCounter != new.eventCounter) {
            Log.d(
                TAG,
                "tur=${new.round}/${new.totalRounds} faz=${new.phase} sıra=${new.currentPlayer.name} " +
                    "skorlar=${new.players.joinToString { "${it.name}:${it.score}" }} " +
                    "harfler=${new.usedLetters.joinToString("")} olay=${new.lastEvent}",
            )
        }
    }
}
