package com.ailecarki.tv.domain.engine

import com.ailecarki.tv.domain.model.Player

/**
 * Finalist seçimi (rastgele değil):
 * 1) en yüksek toplam puan, 2) eşitse en çok tur kazanan, 3) hâlâ eşitse oyuncu sırasında önce gelen.
 */
object TieBreaker {
    fun pickFinalist(players: List<Player>): Int {
        require(players.isNotEmpty())
        var best = 0
        for (i in 1 until players.size) {
            val a = players[i]
            val b = players[best]
            if (a.score > b.score || (a.score == b.score && a.roundsWon > b.roundsWon)) best = i
        }
        return best
    }
}
