package com.ailecarki.tv.domain.model

/**
 * Oyunun tüm durumu. Değişmez (immutable); GameEngine her aksiyonda yeni bir kopya üretir.
 */
data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int = 0,
    val round: Int = 1,
    val totalRounds: Int,
    val puzzle: Puzzle,
    val revealedLetters: Set<Char> = emptySet(),
    val usedLetters: Set<Char> = emptySet(),
    val phase: GamePhase = GamePhase.PLAYER_TURN,
    /** Son çevirmede seçilen dilim. */
    val spinSegmentIndex: Int? = null,
    /** Her çevirmede artar; UI animasyon anahtarı. */
    val spinCount: Int = 0,
    val doubleActive: Boolean = false,
    val lastEvent: GameEvent? = null,
    val eventCounter: Int = 0,
    val lastRevealedLetter: Char? = null,
    val roundWinnerIndex: Int? = null,
    val usedPuzzleIds: Set<String> = emptySet(),
    val isFinal: Boolean = false,
    val finalistIndex: Int? = null,
    val finalPicks: List<Char> = emptyList(),
    val finalWon: Boolean? = null,
) {
    val currentPlayer: Player get() = players[currentPlayerIndex]
}
