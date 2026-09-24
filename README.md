# Aile Çarkı (Android TV)

**BUILD STATUS: NOT BUILT** — geliştirme ortamında Android SDK / Google Maven erişimi yok, APK henüz üretilmedi.
Domain unit testleri: 48/48 geçti (kotlinc ile). APK için GitHub Actions kullanın (aşağıda).
**Tested on: not tested** (Android TV emulator bu ortamda kurulamadı)

Kumandayla oynanan, çarklı Türkçe kelime tahmin oyunu. Kotlin + Jetpack Compose, tamamen offline.

- Paket: `com.ailecarki.tv` · minSdk 21 · targetSdk 34
- Yatay ekran, dokunmatik gerekmez, Android TV ana ekranında (Leanback) görünür.

## APK nasıl alınır (GitHub Actions)
1. Bu klasörün içeriğini bir GitHub reposuna yükle (`.github` klasörü dahil).
2. **Actions** sekmesinde "Build APK" iş akışı otomatik başlar (ya da "Run workflow").
3. Bitince **Artifacts → AileCarki-debug-apk** indir → `app-debug.apk`.
4. Hata olursa log'u paylaş.

Bilgisayarda: Android Studio (JDK 17) ile aç → `./gradlew assembleDebug`
Çıktı: `app/build/outputs/apk/debug/app-debug.apk`

Testler: `./gradlew testDebugUnitTest`

## Kumanda
- Yön tuşları: gezinme · OK: seç · Geri: menü/iptal
- Oyunda geri → "Oyundan çık?" (oyun otomatik kaydedilir, ana menüde DEVAM ET).

## Oyun akışı
Tur başı → ÇARKI ÇEVİR → çark ortaya gelir, 3–5 sn döner → sonuç (ör. 500 PUAN) →
harf kartelası alttan gelir → ünsüz seç → harfler tek tek açılır, puan eklenir.
Sesli harf 250 puan (ayarlanabilir), sıra devam eder. ÇÖZ ile cevap yazılır.
3 tur (veya 5) + final: lider oyuncu, R S T L N E verilir, 3 ünsüz + 1 sesli seçer, 20 sn süre.

## Klasör yapısı
```
app/src/main/java/com/ailecarki/tv/
  domain/   → saf Kotlin oyun mantığı (Android'siz, test edilebilir)
    model/  GameState, GamePhase, GameEvent, Player, Puzzle, WheelSegment
    rules/  GameRules (kurallar), WheelConfig (çark dilimleri)
    engine/ GameEngine, WheelEngine, ScoreEngine, PuzzleEngine, AnswerNormalizer, TieBreaker
  data/     → kelime bankası (assets JSON), ayarlar & kayıt (DataStore)
  audio/    → AudioManager, SoundId, speech/DynamicSpeechService
  ui/       → Compose ekranları, bileşenler, ViewModel'ler
app/src/main/assets/puzzles/word_bank.json  → 156 soru, 12 kategori
app/src/main/assets/audio/                  → ses paketi (README.txt içinde dosya listesi)
```
