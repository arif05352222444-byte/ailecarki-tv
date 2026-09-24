# CHANGELOG

## 0.1.1 — Final build hazırlık turu
### Düzeltildi
- **2X + JOKER kuralı:** 2X aktifken Joker'den gelen doğru ünsüz artık 2X hakkını tüketiyor.
  Joker puanı sabit 1000 kalıyor (2X ile katlanmıyor). Eski kodda 2X hakkı Joker sonrası da kalıyordu.
- **Odak:** Çıkış diyaloğu kapanınca odak ÇARKI ÇEVİR / kartelaya yeniden veriliyor.
- **Odak:** Oyuncu ismi diyaloğu kapanınca odak düzenlenen satıra dönüyor.
- **Final sayacı:** Oyun ekranından çıkılınca (dispose) sayaç temizleniyor.

### Eklendi
- `audio/AudioManifest.kt`: tek merkezi ses eşleme dosyası (SoundId → dosya adı). Numaralı final paket
  (01_hosgeldiniz…) buradan bağlanır. Paket `assets/audio/` altına düz de atılabilir.
- Unit testler (+10): 2X+Joker, Joker sabit 1000, 2X sonrası yanlış harf, final harf limitleri
  (verilen/tekrar/ikinci sesli), dead-end yok, çark çizim açısı ↔ pointer eşleşmesi (500 tekrar),
  0/360 wrap-around, 500 dilimi, İngilizce varsayılan locale'de İ/I, 29 harfli klavye.
- CHANGELOG.md, AUDIO_INTEGRATION.md

### Değişmedi
- Dependency / Gradle / SDK sürümleri değiştirilmedi.
- Mimari, oyun motoru ve tasarım korunarak sadece hedefli değişiklik yapıldı.

## 0.1.0 — İlk sürüm
- Proje, oyun motoru, tüm ekranlar, 156 soruluk kelime bankası, ses altyapısı, 38 unit test.
