# 🏎️ ACTel - Assetto Corsa Telemetry & Button Box System

[![Node.js](https://img.shields.io/badge/Node.js-18%2B-339933?style=flat-square&logo=node.js&logoColor=white)](https://nodejs.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Android-4285F4?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Assetto Corsa](https://img.shields.io/badge/Assetto%20Corsa-CSP%20Compatible-E21B23?style=flat-square)](https://assettocorsa.gg/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

**ACTel**, Assetto Corsa yarış simülasyonu için özel olarak geliştirilmiş; ultra düşük gecikmeli (50Hz), donanım hızlandırmalı ve haptik titreşim geri bildirimli profesyonel bir **Telemetri Göstergesi** ve **Sanal Düğme Kutusu (Button Box)** ekosistemidir.

---

## 📸 Ekran Görüntüleri (Screenshots)

### 1. Canlı Yarış Göstergesi (Dashboard)
16 LED kademeli Shift Light devir barı, büyük vites ve dijital hız göstergesi, canlı tur zamanı ve delta farkı, debriyaj / fren / gaz pedalları, 2D G-Kuvveti dairesi ve 4 tekerlek lastik telemetrisi (PSI, °C ve aşınma).

![ACTel Gösterge Ekranı](docs/screenshots/dashboard.png)

### 2. Sanal Buton Kutusu (Button Box)
Oyun odaktan çıkmadan araç ve oturum kontrolleri: TC, ABS, Fren Dağılımı, Farlar, Selektör, Silecek, Korna, CSP Start (`Ctrl+S`), Duraklat (`ESC`), Pit Limiter, DRS, Sinyaller, Dörtlü Flaşör ve Kamera / Bakış açıları.

![ACTel Buton Kutusu](docs/screenshots/button_box.png)

### 3. Ayarlar & Haptik Titreşim Kontrolü (Settings)
Otomatik yerel ağ PC keşfi (Zero-Config UDP radar), kesintisiz devir kesici titreşim şiddeti slider'ı (%20 - %100), bordür / kasis / zemin titreşim anahtarı ve çalışma modları (AUTO / LIVE / DEMO).

![ACTel Ayarlar Ekranı](docs/screenshots/settings.png)

---

## 🌟 Temel Özellikler

### 📱 1. Native Android Uygulaması (Kotlin + Jetpack Compose)
* **60 / 120Hz Donanım Hızlandırma**: Kusursuz akıcılıkta yarış arayüzü.
* **Gelişmiş Haptik Titreşim Motoru**:
  * **Kesintisiz Devir Kesici (Redline)**: Araç son %5'lik devir dilimine (`rpms >= maxRpm * 0.95`) girdiğinde sürücü vites yükseltene kadar ritmik kesici hissiyle titrer.
  * **Titreşim Şiddeti Kaydırıcısı (Slider)**: %20 ile %100 arasında ayarlanabilir, kaydırıldığında anlık dokunsal geri bildirim önizlemesi verir.
  * **Bordür (Kerb), Kasis ve Zemin Titreşimi**: Süspansiyon darbe ivmesi ve dikey G kuvveti hesaplanarak kerblerde ve çakılda direksiyon/yol titreşimi simüle edilir.
* **Yarış Modu Ekran Yönetimi**:
  * `FLAG_KEEP_SCREEN_ON`: Yarış esnasında ekran asla kararmaz / kapanmaz.
  * `Immersive Fullscreen`: Sistem gezinti çubukları gizlenir, tam ekran çalışır.
* **Sıfır Yapılandırmalı Otomatik Bağlantı (UDP Beacon)**: PC IP adresini elle girmeye gerek kalmadan yerel ağdaki Assetto Corsa sunucusunu 1 saniyede otomatik bulur.

### 🖥️ 2. PC Telemetri & Kontrol Sunucusu (Node.js)
* **Sıfır Gecikmeli Shared Memory**: Windows `kernel32.dll` API'si üzerinden Assetto Corsa'nın paylaşılan bellek sayfalarını (`acpmf_physics`, `acpmf_graphics`, `acpmf_static`) C-struct hızında doğrudan okur.
* **Araca Özel Dinamik Veri Okuma**: Aracın `.ini` dosyalarını (`car.ini`, `engine.ini`) tarayarak doğru devir kesici (limiter) ve ideal vites yükseltme (shift light) devirlerini otomatik tespit eder.
* **Çift Yönlü Buton Entegrasyonu**: Mobil arayüzden gönderilen buton eylemlerini oyunun pencere odağını bozmadan Windows API (`SendInput` / `PostMessage`) ve CSP (`controls.ini`) üzerinden simülasyona aktarır.
* **Akıllı Mod Yönetimi (AUTO / LIVE / DEMO)**: Oyun açık olmadığında otomatik olarak gerçekçi simüle telemetri üretir, oyun başladığında anında canlı moda geçer.
* **Web Dashboard**: Mobil cihaz olmasa dahi herhangi bir tarayıcıdan (`http://localhost:8080`) erişilebilen web arayüzü.

---

## 🎮 Buton Kutusu Eşleşmeleri (Keybindings)

| Buton | İşlev | Gönderilen Tuş / Eylem |
| :--- | :--- | :--- |
| **START** | CSP Yarışı Başlat / Menüden Çık / Marş | `Ctrl + S` (CSP Start) + `ESC` + `I` (900ms Marş) |
| **DURAKLAT** | Oyunu Duraklat / Menüyü Aç | `ESC` |
| **TC + / -** | Çekiş Kontrolü Seviyesi | `Ctrl + T` / `Ctrl + Shift + T` |
| **ABS + / -** | Kilitlenme Karşıtı Fren Seviyesi | `Ctrl + A` / `Ctrl + Shift + A` |
| **BIAS + / -** | Fren Ön/Arka Denge Dağılımı | `Ctrl + B` / `Ctrl + Shift + B` |
| **PIT LIMITER**| Pit Hız Sınırlayıcı Aç/Kapat | `Alt + L` (CSP Pit Limiter) |
| **DRS** | Arka Kanat DRS Aç/Kapat | `Space` |
| **FARLAR** | Far Aç / Kapat | `Ctrl + P` (CSP Headlights) |
| **SELEKTÖR** | Flaşör / Selektör Yak | `Ctrl + F` (CSP Flasher) |
| **SİLECEK** | Cam Sileceği Aç / Hız Değiştir | `Alt + 1` (CSP Wipers) |
| **KORNA** | Korna Çal | `H` |
| **PİTE DÖN** | Aracı Pite Işınla / Teleport | `Ctrl + O` (Ideal Line çakışması önlenmiştir) |
| **KAMERA** | Sürüş Kamerası Değiştir | `F1` |
| **SİNYALLER** | Sol / Sağ Dönüş Sinyalleri | `Alt + Sol Ok` / `Alt + Sağ Ok` (CSP) |
| **DÖRTLÜ** | Dörtlü Flaşörler (Hazard) | `Alt + Aşağı Ok` (CSP Hazard) |
| **BAKIŞLAR** | Sola / Arkaya / Sağa Bak | `Numpad 1` / `F6` / `Numpad 3` |
| **YENİDEN BAŞLAT** | Oturumu Baştan Başlat | `Ctrl + R` |

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
   * Doğrudan `start.bat` dosyasına çift tıklayın **veya**:
   ```cmd
   node server.js
   ```

### 3. Mobil Uygulamanın Çalıştırılması
1. Android uygulamasını Android Studio ile açın:
   * `android/` klasörünü Android Studio'da açıp cihazınıza yükleyin (`Run 'app'`).
   * Veya komut satırından derleyin:
     ```cmd
     cd android
     ./gradlew assembleDebug
     ```
2. Telefonunuzu ve PC'nizi **aynı Wi-Fi ağına** bağlayın. Uygulama açıldığında PC sunucusunu otomatik olarak bulacaktır.

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
├── package.json            # Node.js bağımlılıkları (express, ws, koffi)
├── .gitignore              # Git yok sayma kuralları
│
├── docs/
│   └── screenshots/        # README arayüz ekran görüntüleri
│
├── lib/
│   ├── acReader.js         # Windows Shared Memory C-struct okuyucusu & kerb analizi
│   ├── acController.js     # Windows API klavye simülatörü & CSP tuş yöneticisi
│   ├── carInfoReader.js    # AC araç .ini dosyaları limiter/shift tespiti
│   └── mockTelemetry.js    # AC kapalıyken çalışan gerçekçi telemetri motoru
│
├── ac_plugin/              # Assetto Corsa içi Python köprü eklentisi
│   └── actel_bridge/       # Shared Memory köprü modülü
│
├── public/                 # Web tabanlı gösterge arayüzü (HTML/CSS/JS)
│
└── android/                # Native Jetpack Compose mobil uygulaması
    ├── app/src/main/java/com/actel/dashboard/
    │   ├── MainActivity.kt                  # Tam ekran, haptik titreşim ve sekme yönetimi
    │   ├── viewmodel/MainViewModel.kt       # StateFlow telemetri & ayarlar yönetimi
    │   ├── data/
    │   │   ├── model/TelemetryModels.kt     # JSON veri modelleri
    │   │   └── network/                     # WebSocket istemcisi & UDP radar servisi
    │   └── ui/
    │       ├── components/                  # ShiftLights, Tyres, Pedals, G-Force
    │       └── screens/                     # DashboardScreen, ButtonBoxScreen, SettingsScreen
    └── build.gradle.kts
```

---

## 📄 Lisans

Bu proje [MIT Lisansı](LICENSE) kapsamında lisanslanmıştır. Dilediğiniz gibi geliştirebilir, özelleştirebilir ve kullanabilirsiniz.
