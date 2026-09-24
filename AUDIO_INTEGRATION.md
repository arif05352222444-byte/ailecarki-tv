# Ses Paketi Entegrasyonu

Oyun kodu dosya adı bilmez; yalnızca `SoundId` kullanır (`audio/SoundId.kt`).
Dosya adları tek yerde eşlenir: `app/src/main/java/com/ailecarki/tv/audio/AudioManifest.kt`.

## Final paket geldiğinde
1. Dosyaları kopyalayın:
   - `app/src/main/assets/audio/voice/`, `effect/`, `music/`, `ui/`  **veya**
   - hepsini düz olarak `app/src/main/assets/audio/` içine
2. `AudioManifest.overrides` tablosunu doldurun, örn:
   ```kotlin
   SoundId.VOICE_WELCOME to listOf("01_hosgeldiniz"),
   SoundId.VOICE_SPIN_PROMPT to listOf("03_carki_cevir"),
   SoundId.VOICE_BANKRUPT to listOf("09_iflas"),
   ```
3. Uzantı yazılmaz; `.ogg / .mp3 / .wav / .m4a` otomatik denenir.

Düz klasörde aynı ada sahip iki farklı ses (ör. voice ve effect için "correct") çakışabilir.
Numaralı isimler kullanıldığında bu sorun olmaz.

## Güvenlik
- Dosya yoksa ses sessizce atlanır; build ve oyun etkilenmez.
- Sunucu sesi çalarken müzik otomatik kısılır (ducking).
- Beklenen varsayılan dosya adları: `assets/audio/README.txt`
- Dinamik (isimli) sunucu cümleleri: `DynamicSpeechService`. Şu an `MockDynamicSpeechService` kullanılıyor.
  ElevenLabs bir backend üzerinden bağlanacak; API anahtarı APK'ya konmaz.
