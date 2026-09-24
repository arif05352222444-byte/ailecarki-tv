# DEVELOPER NOTES — Aile Çarkı

## CURRENT STATUS
Tüm ekranlar ve oyun akışı yazıldı. Oyun motoru (domain) Kotlin 2.0.21 ile derlendi, 48/48 unit test geçti.
BUILD: yapılamadı (ortamda Android SDK yok). Tested on: not tested.
Android/Compose katmanı geliştirme ortamında derlenemedi (Android SDK ve Google Maven erişimi yoktu);
ilk derleme GitHub Actions üzerinde yapılacak. İlk CI derlemesinde küçük derleme hataları çıkabilir.

## WORKING
- [x] Gradle projesi (AGP 8.5.2, Kotlin 2.0.21, Compose BOM 2024.09.03), GitHub Actions APK iş akışı
- [x] Manifest: Leanback launcher, banner, landscape, touchscreen gerekmez
- [x] GameEngine: tur, sıra, ünsüz/sesli, İFLAS, SIRA GEÇ, 2X, JOKER, çözme, tur sonu, final, kayıttan devam
- [x] WheelEngine: sonuç önceden seçilir, animasyon hedef açısı matematiksel hesaplanır (testli)
- [x] Türkçe normalizasyon (İSTANBUL = istanbul) + Türkçe karaktersiz klavye toleransı
- [x] 156 soruluk offline kelime bankası, oturumda tekrar yok
- [x] Ana menü, oyuncu ekranı (2–6 oyuncu, rastgele isim, boş/tekrar isim uyarısı), ayarlar
- [x] Oyun ekranı: büyük çark YOK; çark sadece çevirirken overlay olarak gelir
- [x] Alttan kayan 29 harfli kartela, harflerin tek tek açılması, olay bannerları, konfeti
- [x] Final ekranı (geri sayım) ve oyun sonu ekranı
- [x] AudioManager (VOICE/EFFECT/MUSIC/UI, ducking, dosya yoksa sessiz)
- [x] DynamicSpeechService + Mock
- [x] Ayarlar ve kayıtlı oyun DataStore'da
- [x] Unit testler (48): WheelEngine (çizim↔pointer, wrap-around), ScoreEngine, PuzzleEngine, normalizasyon, GameEngine (2X+Joker dahil)
- [x] Merkezi ses eşleme: audio/AudioManifest.kt (bkz. AUDIO_INTEGRATION.md)

## KNOWN ISSUES
- Compose/Android katmanı henüz hiç derlenmedi; ilk Gradle derlemesinde hata çıkabilir.
- TV/emulator testi yapılmadı.
- Disabled buton ve harfler bilinçli olarak odaklanabilir (OK çalışmaz, gri görünür). Böylece odak
  kaybolmuyor, örneğin 6. oyuncu eklenince odaktaki buton pasif olsa bile odak yerinde kalıyor.
- Kayıttan devam edilirse final sayacı baştan başlar.

## TODO
- [ ] İlk GitHub Actions derlemesi ve varsa derleme hatalarının düzeltilmesi
- [ ] Gerçek TV / TV Box üzerinde kumanda testi
- [ ] AileCarki_AudioPack_v1 ses dosyalarının eklenmesi (`assets/audio/README.txt`)
- [ ] ElevenLabsDynamicSpeechService (backend/proxy üzerinden)
- [ ] Kelime bankasını büyütmek
- [ ] İsteğe bağlı: androidx.tv:tv-material bileşenlerine geçiş

## NEXT STEP
Projeyi GitHub'a yükle, Actions'tan APK'yı al, TV'de dene. Derleme hatası olursa log'u paylaş.

## Nereden ne değişir
- Kurallar: `domain/rules/GameRules.kt` (sesli bedeli, iflas politikası, joker puanı, final harfleri/süresi)
- Çark dilimleri: `domain/rules/WheelConfig.kt`
- Kelimeler: `assets/puzzles/*.json` — klasördeki tüm JSON dosyaları okunur. Format:
  `{"id":"city_014","category":"ŞEHİR","answer":"KONYA"}` — yalnızca Türk alfabesi harfleri ve boşluk.
- Sesler: `assets/audio/<voice|effect|music|ui>/<ad>.ogg`, isimler `audio/SoundId.kt`
- ElevenLabs: `AppContainer.speech` içindeki `MockDynamicSpeechService` yerine backend'e bağlanan bir
  sınıf yazılır. API anahtarı ASLA APK'ya konmaz.

## Mimari notlar
- UI tamamen `GameState.phase` ile yönetilir; tüm kural mantığı `GameEngine` içindedir (saf fonksiyonlar).
- Çark sonucu önce engine'de seçilir, sonra UI o dilimde duracak açıya döndürür.
- Performans: çark tek sefer çizilir, sadece `graphicsLayer.rotationZ` döner; blur yok; konfeti süreli.
- Odak: disabled buton/harfler odaklanabilir kalır ama çalışmaz; faz değişince odak otomatik verilir.
- Diyaloglar ayrı pencere (`Dialog`) → odak arkadaki butonlara kaçmaz.
- Loglar: `adb logcat -s AileCarki/State AileCarki/Audio AileCarki/Puzzles`
