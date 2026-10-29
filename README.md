# 🏎️ ACTel - Assetto Corsa Telemetry & Button Box System

[![GitHub Release](https://img.shields.io/github/v/release/FatihEsen/actel?style=flat-square&logo=github&color=blue)](https://github.com/FatihEsen/actel/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Android-4285F4?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Node.js](https://img.shields.io/badge/Node.js-18%2B-339933?style=flat-square&logo=node.js&logoColor=white)](https://nodejs.org/)
[![Assetto Corsa](https://img.shields.io/badge/Assetto%20Corsa-CSP%20Compatible-E21B23?style=flat-square)](https://assettocorsa.gg/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

**ACTel**, Assetto Corsa yarış simülasyonu için özel olarak geliştirilmiş; ultra düşük gecikmeli (50Hz), donanım hızlandırmalı ve gelişmiş haptik titreşim geri bildirimli profesyonel bir **Native Android Telemetri Göstergesi (Dashboard)** ve **Sanal Düğme Kutusu (Button Box)** ekosistemidir.

Telefon ve tabletleri **Kotlin & Jetpack Compose** gücüyle yarış kokpitinizin ayrılmaz bir parçasına dönüştürür.

---

## 📸 Ekran Görüntüleri (Screenshots)

### 1. Canlı Yarış Göstergesi (Dashboard)
16 LED kademeli Shift Light devir barı, Sidekick tarzı akıllı Turbo Basınç ve ERS/KERS/DRS telemetri barları, simetrik kokpit kanatları (Sol: Sinyal, Far, Flaşör, DRS, ABS / Sağ: Sinyal, Dörtlü ⚠️, PIT, TC, Fren Dağılımı), otantik 7-segment dijital vites göstergesi, büyütülmüş Hız (KM/H) ve RPM, canlı tur zamanı (pist dışına çıkışta kırmızı uyarı), dinamik ghost farkı ve trend okları (kazançta yeşil, kayıpta kırmızı), debriyaj / fren / gaz pedalları, 2D G-Kuvveti dairesi ve 4 tekerlek lastik telemetrisi (PSI, °C ve aşınma).

![ACTel Android Dashboard](docs/screenshots/dashboard.png)

### 2. Sanal Buton Kutusu (Button Box)
5x6 tam ekran dokunsal buton matrisi (30 fonksiyonel buton), üst anlık telemetri şeridi ve Content Manager & CSP fabrika standart tuşları: TC, ABS, Fren Dağılımı, Farlar, Selektör, Silecek, Korna, Start (`Ctrl+S`), Duraklat (`ESC`), Pit Limiter, DRS, KERS, Sinyaller, Dörtlü Flaşör ve F1 Kamera / Bakış açıları.

![ACTel Android Buton Kutusu](docs/screenshots/button_box.png)

### 3. Ayarlar & Haptik Titreşim Kontrolü (Settings)
Sıfır yapılandırmalı yerel ağ PC radar keşfi (Zero-Config UDP radar), ayarlanabilir devir kesici titreşimi slider'ı (%20 - %100), bordür / kasis / zemin darbe titreşim anahtarı ve akıllı çalışma modları (AUTO / LIVE / DEMO).

![ACTel Android Ayarlar Ekranı](docs/screenshots/settings.png)

---

## 📦 Hazır APK İndirme (GitHub Releases)

Derleme yapmadan doğrudan telefonunuza kurmak için:
1. Sağ taraftaki **[Releases](https://github.com/FatihEsen/actel/releases)** bölümünden en güncel `app-debug.apk` dosyasını telefonunuza indirin.
2. Telefonunuzda APK dosyasını açıp kurulumu onaylayın.
3. PC sunucusunu başlattıktan sonra uygulamayı açtığınızda telefon PC'yi otomatik keşfedip bağlanacaktır.

---

## 🌟 Temel Özellikler

### 📱 1. Native Android Uygulaması (Kotlin + Jetpack Compose)
* **60 / 120Hz Donanım Hızlandırma**: Kusursuz akıcılıkta profesyonel yarış gösterge arayüzü.
* **Gelişmiş Haptik Titreşim Motoru**:
  * **Kesintisiz Devir Kesici (Redline)**: Araç son %5'lik devir dilimine (`rpms >= maxRpm * 0.95`) girdiğinde sürücü vites yükseltene kadar ritmik kesici hissiyle titrer.
  * **Çarpışma & Darbe Titreşimi**: Kaza veya sert bordür/bariyer çarpmalarında telefonun vibratörü güçlü darbe ritmiyle anlık uyarır.
  * **Titreşim Şiddeti Kaydırıcısı (Slider)**: %20 ile %100 arasında ayarlanabilir, kaydırıldığında anlık dokunsal geri bildirim önizlemesi verir.
  * **Bordür (Kerb), Kasis ve Zemin Titreşimi**: Süspansiyon darbe ivmesi ve dikey G kuvveti hesaplanarak kerblerde ve çakılda direksiyon/yol titreşimi simüle edilir.
* **Yarış Modu Ekran Yönetimi**:
  * `FLAG_KEEP_SCREEN_ON`: Yarış esnasında ekran asla kararmaz / kapanmaz.
  * `Immersive Fullscreen`: Sistem gezinti çubukları gizlenir, tam ekran çalışır.
* **Sıfır Yapılandırmalı Otomatik Bağlantı (UDP Beacon)**: PC IP adresini elle girmeye gerek kalmadan yerel ağdaki Assetto Corsa sunucusunu 1 saniyede otomatik bulur.
* **5x6 Buton Kutusu Dokunsal Geri Bildirimi**: Tuşlara dokunulduğunda hafif haptik tıklama hissi ve bas-çek fonksiyonları (KERS / DRS).

### 🖥️ 2. PC Telemetri & Kontrol Sunucusu (Node.js)
* **Sıfır Gecikmeli Shared Memory**: Windows `kernel32.dll` API'si üzerinden Assetto Corsa'nın paylaşılan bellek sayfalarını (`acpmf_physics`, `acpmf_graphics`, `acpmf_static`) C-struct hızında doğrudan okur.
* **Otomatik Dosya Yolu ve Kontrol Doğrulama (`acPathDetector`)**: Assetto Corsa kurulum ve Belgeler klasörlerini otomatik tespit eder, `controls.ini` dosyasında eşzamanlı klavye-direksiyon desteğini (`COMBINE_WITH_KEYBOARD_CONTROL=1`) otomatik aktif eder.
* **Araca Özel Dinamik Veri Okuma**: Aracın `.ini` dosyalarını (`car.ini`, `engine.ini`) tarayarak doğru devir kesici (limiter) ve ideal vites yükseltme (shift light) devirlerini otomatik tespit eder.
* **Content Manager & CSP Entegrasyonu**: Mobil arayüzden gönderilen eylemleri oyunun pencere odağını bozmadan Windows API (`keybd_event`) ve Custom Shaders Patch üzerinden simülasyona aktarır.
* **Akıllı Mod Yönetimi (AUTO / LIVE / DEMO)**: Oyun açık olmadığında otomatik olarak gerçekçi simüle telemetri üretir, oyun başladığında anında canlı moda geçer.

---

## 🎮 Buton Kutusu Eşleşmeleri (Keybindings)

Tüm tuş eşleşmeleri **orijinal Assetto Corsa ve Custom Shaders Patch (CSP)** fabrika varsayılanlarına göre optimize edilmiştir. Kullanıcının manuel ayar yapmasına gerek kalmadan doğrudan çalışır:

| Buton | İşlev | Gönderilen Standart Tuş / Eylem |
| :--- | :--- | :--- |
| **START** | Content Manager Başlat / Menüden Çık / Marş | `Ctrl + S` (CSP Start) + `ESC` + `Space` + `I` (Marş) *(1sn sonra Dash'e döner)* |
| **DURAKLAT** | Oyunu Duraklat / Menüyü Aç | `ESC` (Orijinal AC Varsayılanı) |
| **TC + / -** | Çekiş Kontrolü Seviyesi | `Ctrl + T` / `Ctrl + Shift + T` (Orijinal AC Varsayılanı) |
| **ABS + / -** | Kilitlenme Karşıtı Fren Seviyesi | `Ctrl + A` / `Ctrl + Shift + A` (Orijinal AC Varsayılanı) |
| **BIAS + / -** | Fren Ön/Arka Denge Dağılımı | `Ctrl + B` / `Ctrl + Shift + B` (Orijinal AC Varsayılanı) |
| **PIT LIMITER**| Pit Hız Sınırlayıcı Aç/Kapat | `Alt + L` (CSP Pit Limiter Standardı) |
| **DRS** | Arka Kanat DRS Aç/Kapat | `D` (Orijinal AC Varsayılanı) |
| **KERS** | Hibrit / MGU-K Güç Boşaltma | `K` (Orijinal AC Varsayılanı) |
| **FARLAR** | Far Aç / Kapat | `L` (Orijinal AC Varsayılanı) |
| **SELEKTÖR** | Flaşör / Selektör Yak | `G` (Orijinal AC Varsayılanı) |
| **İÇ IŞIK** | Kabin Aydınlatması (Extra A) | `Alt + Numpad 7` (CSP Standardı) |
| **SİS FARI** | Sis Farı / Uzunlar (Extra B) | `Alt + Numpad 8` (CSP Standardı) |
| **SİLECEK** | Cam Sileceği Aç / Hız Değiştir | `Alt + W` (CSP Standardı) |
| **KORNA** | Korna Çal | `H` (Orijinal AC Varsayılanı) |
| **PİTE DÖN** | Aracı Pite Işınla / Teleport | `Ctrl + P` (Orijinal AC / CM Varsayılanı) |
| **KAMERA** | Sürüş Kamerası Değiştir | `F1` / `C` (Orijinal AC Varsayılanı) |
| **SİNYALLER** | Sol / Sağ Dönüş Sinyalleri | `Alt + Sol Ok` / `Alt + Sağ Ok` (CSP Standardı) |
| **DÖRTLÜ** | Dörtlü Flaşörler (Hazard) | `Alt + Aşağı Ok` (CSP Standardı) |
| **BAKIŞLAR** | Sola / Arkaya / Sağa Bak | `Q` / `W` / `E` (Orijinal AC Varsayılanı) |
| **YENİDEN BAŞLAT** | Oturumu Baştan Başlat | `Ctrl + O` (Orijinal AC / CM Varsayılanı) |

---

## 🚀 Hızlı Başlangıç & Kurulum

### 1. Gereksinimler
* **PC**: Windows 10/11, [Node.js](https://nodejs.org/) (v18 veya üzeri), Assetto Corsa (Custom Shaders Patch önerilir).
* **Mobil**: Android 8.0 (Oreo) veya üzeri telefon ya da tablet.

### 2. PC Sunucusunun Çalıştırılması
1. Projeyi bilgisayarınıza indirin veya klonlayın:
   ```cmd
   git clone https://github.com/FatihEsen/actel.git
   cd actel
   ```
2. Bağımlılıkları yükleyin:
   ```cmd
   npm install
   ```
3. Sunucuyu başlatın:
   * Doğrudan **`start.bat`** dosyasına çift tıklayın **veya**:
   ```cmd
   npm start
   ```

### 3. Mobil Uygulamanın Kullanımı
* **Yöntem A (En Kolay):** [Releases](https://github.com/FatihEsen/actel/releases) sayfasından `app-debug.apk` dosyasını indirip telefonunuza yükleyin.
* **Yöntem B (Geliştirici / Android Studio):** `android/` klasörünü Android Studio ile açıp `Run 'app'` deyin veya terminalden derleyin:
  ```cmd
  npm run build:apk
  ```
* Telefonunuzu ve PC'nizi **aynı Wi-Fi ağına** bağlayın. Uygulama açıldığında PC sunucusunu otomatik olarak bulacaktır.

#### ⚡ Ultra Düşük Gecikmeli USB Bağlantısı (İsteğe Bağlı)
Wi-Fi olmayan ortamlarda veya sıfır gecikme isteniyorsa, telefon PC'ye USB kablosuyla bağlıyken tek bir komutla USB köprüsü kurulabilir:
```cmd
adb reverse tcp:8080 tcp:8080
```
Bu sayede uygulama yerel `127.0.0.1:8080` portu üzerinden kablolu bağlantı hızında çalışır.

---

## 📁 Proje Mimarisi

```
actel/
├── server.js               # Ana Node.js HTTP, WebSocket ve UDP Beacon sunucusu
├── start.bat               # Windows tek tıkla başlatma betiği
├── package.json            # Node.js bağımlılıkları ve çalıştırma komutları
├── .gitignore              # Git yok sayma kuralları (*.apk, build/, .gradle/)
│
├── docs/
│   └── screenshots/        # Güncel 2340x1080 Android arayüz ekran görüntüleri
│
├── lib/
│   ├── acReader.js         # Windows Shared Memory okuyucusu, kerb ve darbe dedektörü
│   ├── acController.js     # Windows API klavye simülatörü & Content Manager eylemleri
│   ├── acPathDetector.js   # AC dizinleri ve controls.ini otomatik yapılandırıcısı
│   ├── carInfoReader.js    # AC araç .ini dosyaları limiter/shift tespiti
│   └── mockTelemetry.js    # AC kapalıyken çalışan gerçekçi telemetri motoru
│
├── ac_plugin/              # Assetto Corsa içi Python köprü eklentisi
│   └── actel_bridge/       # Shared Memory köprü modülü
│
└── android/                # Native Jetpack Compose mobil uygulaması
    ├── app/src/main/java/com/actel/dashboard/
    │   ├── MainActivity.kt                  # Tam ekran, haptik titreşim ve çarpışma motoru
    │   ├── viewmodel/MainViewModel.kt       # StateFlow telemetri & ayarlar yönetimi
    │   ├── data/
    │   │   ├── model/TelemetryModels.kt     # JSON veri modelleri
    │   │   └── network/                     # WebSocket istemcisi & UDP radar servisi
    │   └── ui/
    │       ├── components/                  # ShiftLights, GearSpeedDisplay, Tyres, Pedals
    │       └── screens/                     # DashboardScreen, ButtonBoxScreen, SettingsScreen
    └── build.gradle.kts
```

---

## 📄 Lisans

Bu proje [MIT Lisansı](LICENSE) kapsamında lisanslanmıştır. Dilediğiniz gibi geliştirebilir, özelleştirebilir ve kullanabilirsiniz.
