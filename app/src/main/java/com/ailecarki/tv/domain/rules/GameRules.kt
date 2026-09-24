package com.ailecarki.tv.domain.rules

enum class BankruptPolicy {
    /** Oyuncunun toplam puanı sıfırlanır (varsayılan). */
    RESET_TOTAL,
    /** Yalnızca bu turdaki kazancı silinir. */
    RESET_ROUND_EARNINGS,
}

/** Oyunun tüm ayarlanabilir kuralları. Sabit değerleri kod içine gömmek yerine buradan değiştirin. */
data class GameRules(
    val normalRounds: Int = 3,
    val vowelCost: Int = 250,
    val bankruptPolicy: BankruptPolicy = BankruptPolicy.RESET_TOTAL,
    val doubleMultiplier: Int = 2,
    val jokerPoints: Int = 1000,
    val roundWinBonus: Int = 0,
    val finalSeconds: Int = 20,
    val finalGivenLetters: Set<Char> = setOf('R', 'S', 'T', 'L', 'N', 'E'),
    val finalConsonantPicks: Int = 3,
    val finalVowelPicks: Int = 1,
    /** Final kelimesinde verilen harflerden sonra en az bu kadar kapalı kutu kalsın. */
    val finalMinHiddenTiles: Int = 3,
    val minPlayers: Int = 2,
    val maxPlayers: Int = 6,
    val defaultPlayers: Int = 3,
    val maxNameLength: Int = 12,
    /** Türkçe karakteri olmayan klavyelerde "ISTANBUL" gibi yazımları da kabul et. */
    val lenientAnswerMatching: Boolean = true,
)
