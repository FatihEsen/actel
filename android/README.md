# ACTel Android - Native Jetpack Compose Dashboard & Button Box

Assetto Corsa için geliştirilmiş, yüksek performanslı, düşük gecikmeli **Native Android (Kotlin + Jetpack Compose)** telemetri ve buton kutusu uygulaması.

---

## 🏎️ Özellikler

1. **Jetpack Compose 60/120Hz Arayüz**: Donanım hızlandırmalı, pürüzsüz devir/hız animasyonları.
2. **Otomatik PC Keşfi (Zero-Config UDP Discovery)**: IP adresi yazmaya gerek kalmadan ağdaki Assetto Corsa PC sunucusunu otomatik bulur ve bağlanır.
3. **Gerçek Haptik Geri Bildirim**:
   - Button Box tuşlarına basıldığında mekanik tıklama hissi.
   - Devir kesiciye (Redline / Shift Light) ulaşıldığında ritmik titreşim uyarısı.
4. **Yarış Modu Ekranı**:
   - `FLAG_KEEP_SCREEN_ON`: Yarış boyunca ekran asla kararmaz veya kilitlenmez.
   - `Immersive Sticky Mode`: Sistem bildirim ve gezinme çubukları gizlenir, tam ekran çalışır.
   - Yatay (Landscape) yönlendirme kilidi.
5. **Ekranlar**:
   - **GÖSTERGE (Dashboard)**: 16 LED dinamik Shift Light bar, büyük vites kutusu, hız, RPM, tur zamanlama & delta, gaz/fren dikey barları, 2D G-Force dairesi.
   - **LASTİKLER (Tyres)**: 4 tekerlek IMO (İç/Orta/Dış) ısı haritası, lastik basınçları (PSI), fren disk sıcaklıkları ve aşınma oranları.
   - **BUTTON BOX**: TC +/-, ABS +/-, Fren Dengesi +/-, Pit Limiter, DRS, Farlar, Selektör, Silecek, Korna, Pite Dön, VR Sıfırla.
   - **AYARLAR**: Otomatik/Manuel sunucu seçimi, haptik titreşim ayarı ve demo modları.

---

## 🛠️ Nasıl Çalıştırılır ve Derlenir?

### Yöntem 1: Android Studio ile (Önerilen)
1. **Android Studio**'yu açın.
2. **Open** seçeneği ile `/mnt/Documents/Agv/actel/android` klasörünü seçin.
3. Gradle senkronizasyonunun tamamlanmasını bekleyin.
4. Android cihazınızı USB ile bağlayın (USB Hata Ayıklama açık olmalıdır).
5. **Run ('app')** butonuna tıklayın.

### Yöntem 2: PC Sunucusu
PC tarafında telemetri köprüsünü başlatmak için:
```bash
node server.js
```
Aynı yerel ağda (Wi-Fi) telefon ve PC olduğunda telefon sunucuyu otomatik olarak keşfedecektir.
