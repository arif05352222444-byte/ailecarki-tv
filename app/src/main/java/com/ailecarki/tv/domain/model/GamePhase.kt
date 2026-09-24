package com.ailecarki.tv.domain.model

/**
 * UI davranışı tamamen bu faz üzerinden yönetilir.
 */
enum class GamePhase {
    /** Sıra yeni başladı: ÇARKI ÇEVİR / SESLİ HARF AL / ÇÖZ */
    PLAYER_TURN,
    WHEEL_SPINNING,
    /** Çark durdu, sonuç büyük gösteriliyor. */
    WHEEL_RESULT,
    /** Puan/Joker sonrası ünsüz seçimi. */
    LETTER_SELECTION,
    /** Sesli harf satın alma kartelası. */
    VOWEL_SELECTION,
    /** Harfler panoda tek tek açılıyor. */
    LETTER_REVEAL,
    /** Doğru harf sonrası oyuncu devam ediyor. */
    PLAYER_ACTION,
    SOLVING,
    ROUND_COMPLETE,
    FINAL_LETTER_SELECTION,
    FINAL_REVEAL,
    FINAL_SOLVING,
    GAME_COMPLETE,
}
