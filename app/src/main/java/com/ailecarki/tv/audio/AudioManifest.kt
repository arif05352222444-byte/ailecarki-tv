package com.ailecarki.tv.audio

/**
 * TEK MERKEZİ SES EŞLEME DOSYASI.
 *
 * Oyun kodu yalnızca [SoundId] (ör. SoundId.VOICE_WELCOME) kullanır; dosya adı hiçbir yerde yazılmaz.
 * Final ses paketi (ör. ElevenLabs "AileCarki_AudioPack_v1") numaralı isimlerle gelirse
 * (01_hosgeldiniz, 03_carki_cevir, 09_iflas…) sadece aşağıdaki [overrides] tablosunu doldurmak yeterli.
 *
 * Arama sırası (ilk bulunan kullanılır, uzantı: ogg/mp3/wav/m4a):
 *   1) overrides[id] içindeki adlar
 *   2) SoundId.fileBase (ör. "welcome")
 * Klasörler: assets/audio/<kategori>/  ve  assets/audio/  (paket düz klasör halinde de atılabilir)
 * Hiçbiri yoksa ses sessizce atlanır; oyun durmaz.
 */
object AudioManifest {
    /** Final paket geldiğinde burayı doldurun. Örnek satırlar yorumda. */
    val overrides: Map<SoundId, List<String>> = mapOf(
        // SoundId.VOICE_WELCOME to listOf("01_hosgeldiniz"),
        // SoundId.VOICE_SPIN_PROMPT to listOf("03_carki_cevir"),
        // SoundId.VOICE_BANKRUPT to listOf("09_iflas"),
    )

    fun candidates(id: SoundId): List<String> = overrides[id].orEmpty() + id.fileBase

    val extensions = listOf("ogg", "mp3", "wav", "m4a")
}
