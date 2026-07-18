<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
<img src="https://img.shields.io/badge/Min_SDK-29-1F5C2E?style=for-the-badge"/>
<img src="https://img.shields.io/badge/License-MIT-C8E6C9?style=for-the-badge"/>

# 🏃 FitRoute

**Aplikasi olahraga outdoor Android berbasis multi-sensor smartphone**
untuk tracking rute, elevasi, dan kalori secara real-time.

> Dikembangkan sebagai proyek penelitian oleh **Dwiki Dzaki Yudi Putra**
> Program Studi Teknik Informatika · 2026

</div>

---

## 📋 Daftar Isi

- [Tentang Proyek](#-tentang-proyek)
- [Fitur Utama](#-fitur-utama)
- [Sensor yang Digunakan](#-sensor-yang-digunakan)
- [Arsitektur](#-arsitektur)
- [Teknologi](#-teknologi)
- [Struktur Proyek](#-struktur-proyek)
- [Persyaratan](#-persyaratan)
- [Instalasi & Menjalankan Proyek](#-instalasi--menjalankan-proyek)
- [Skema Database](#-skema-database)
- [Alur Aplikasi](#-alur-aplikasi)
- [Pengujian](#-pengujian)
- [Kontribusi](#-kontribusi)
- [Lisensi](#-lisensi)

---

## 🌿 Tentang Proyek

FitRoute adalah aplikasi mobile Android yang memanfaatkan sensor-sensor bawaan smartphone secara sinergis untuk memantau aktivitas olahraga outdoor tanpa memerlukan perangkat wearable tambahan. Aplikasi ini dirancang sebagai jawaban atas kesenjangan di pasar aplikasi kebugaran Indonesia, yaitu minimnya aplikasi yang mengoptimalkan sensor **barometer** untuk profiling elevasi akurat sekaligus menggabungkannya dalam algoritma estimasi kalori yang adaptif dan personal.

### Latar Belakang Penelitian

Penelitian ini mengacu pada metodologi **R&D (Research and Development)** dengan pendekatan Agile-Scrum, mengintegrasikan empat sensor utama smartphone melalui teknik **Sensor Fusion berbasis Kalman Filter** untuk menghasilkan data performa olahraga yang akurat, stabil, dan dapat diakses seluruh lapisan masyarakat.

---

## ✨ Fitur Utama

| Fitur | Deskripsi |
|---|---|
| 🗺️ **GPS Route Tracking** | Pelacakan rute real-time dengan peta interaktif, jarak kumulatif, dan kecepatan sesaat |
| ⛰️ **Elevation Profiling** | Grafik profil ketinggian menggunakan data barometer + koreksi GPS via Kalman Filter |
| 🔥 **Adaptive Calorie Counter** | Estimasi kalori berbasis MET dinamis mempertimbangkan kecepatan, elevasi, berat, dan usia |
| 🤖 **Activity Detection** | Deteksi otomatis jenis aktivitas (lari/sepeda/hiking) menggunakan accelerometer + ML on-device |
| 📊 **Workout History & Analytics** | Riwayat sesi latihan, grafik progres mingguan/bulanan, dan personal record otomatis |
| 📶 **Offline Mode** | Fungsionalitas penuh tanpa internet; sinkronisasi cloud otomatis via WorkManager saat online |
| 🔐 **Biometric Authentication** | Login sidik jari via Android Biometric API + enkripsi RSA-2048 & AES-256 |

---

## 📡 Sensor yang Digunakan

```
┌─────────────────────────────────────────────────────┐
│              SENSOR FUSION ENGINE                   │
│                                                     │
│  📍 GPS          →  Rute, jarak, kecepatan          │
│  🌡️ Barometer    →  Elevasi real-time (±0.1 m)      │
│  📐 Accelerometer →  Deteksi langkah & aktivitas    │
│  🌀 Gyroscope    →  Orientasi & deteksi cycling     │
│  👆 Fingerprint  →  Autentikasi biometrik           │
└─────────────────────────────────────────────────────┘
         ↓ Kalman Filter (noise reduction)
    Data performa olahraga yang akurat & stabil
```

### Formula Estimasi Kalori Adaptif

```
Kalori (kcal) = MET_adaptif × Berat (kg) × Durasi (jam)

MET_adaptif  = MET_base × SpeedFactor × ElevationFactor
SpeedFactor  = 1.0 + (speed - 8.0) × 0.05   // jika speed > 8 km/h
ElevFactor   = 1.0 + (elevGain / 100) × 0.5  // per 100m elevasi/jam
```

---

## 🏗️ Arsitektur

FitRoute menggunakan **Clean Architecture** dengan pola **MVVM (Model-View-ViewModel)**:

```
┌──────────────────────────────────────────────┐
│           PRESENTATION LAYER                 │
│  Fragment / Activity + ViewModel (LiveData)  │
├──────────────────────────────────────────────┤
│             DOMAIN LAYER                     │
│  Use Cases: LoginUseCase, CalorieUseCase,    │
│             PersonalRecordUseCase, ...       │
├──────────────────────────────────────────────┤
│              DATA LAYER                      │
│  Repository Pattern                          │
│  ├── SensorDataSource  (Hardware sensor)     │
│  ├── LocalDataSource   (Room Database)       │
│  └── RemoteDataSource  (Retrofit REST API)   │
└──────────────────────────────────────────────┘
```

---

## 🛠️ Teknologi

### Android (Kotlin)

| Komponen | Library | Versi |
|---|---|---|
| UI & Navigation | AndroidX Navigation Fragment | 2.7.6 |
| Database Lokal | Room Database + KTX | 2.6.1 |
| Lokasi & GPS | Google Play Services Location | 21.2.0 |
| Keamanan | AndroidX Security Crypto | 1.1.0-alpha06 |
| Biometrik | AndroidX Biometric | 1.2.0-alpha05 |
| Background Sync | WorkManager KTX | 2.9.1 |
| HTTP Client | Retrofit 2 + OkHttp | 2.9.0 |
| Async | Kotlin Coroutines | 1.7.3 |
| Peta | OpenStreetMap + MapLibre GL | — |
| Arsitektur | ViewModel + LiveData + StateFlow | 2.7.0 |

### Backend (Node.js)

| Komponen | Teknologi |
|---|---|
| Runtime | Node.js + Express.js |
| Database | PostgreSQL + PostGIS (geospasial) |
| Keamanan | RSA-2048 + AES-256 |

---

## 📁 Struktur Proyek

```
FitRoute/
├── app/
│   └── src/main/
│       ├── java/com/fitroute/
│       │   ├── ui/
│       │   │   ├── auth/          # Splash, Login, Register, Biometric
│       │   │   ├── dashboard/     # Home dashboard
│       │   │   ├── tracking/      # Live tracking & pilih aktivitas
│       │   │   ├── summary/       # Ringkasan sesi
│       │   │   ├── history/       # Riwayat & analytics
│       │   │   └── profile/       # Profil & pengaturan
│       │   ├── domain/
│       │   │   └── usecase/       # LoginUseCase, CalorieUseCase, ...
│       │   ├── data/
│       │   │   ├── local/         # Room Database, DAO, Entity
│       │   │   ├── remote/        # Retrofit API service
│       │   │   └── repository/    # AuthRepo, SessionRepo, ...
│       │   ├── service/
│       │   │   └── TrackingService.kt   # Foreground service GPS
│       │   ├── sensor/
│       │   │   ├── SensorFusionManager.kt
│       │   │   ├── KalmanFilter1D.kt
│       │   │   └── ActivityDetector.kt
│       │   ├── worker/
│       │   │   └── SyncWorker.kt  # WorkManager cloud sync
│       │   └── util/
│       │       ├── BiometricHelper.kt
│       │       ├── AppSettings.kt # EncryptedSharedPreferences
│       │       └── ShareImageGenerator.kt
│       └── res/
│           ├── layout/            # XML layout setiap layar
│           └── navigation/        # nav_auth.xml, nav_main.xml
├── build.gradle.kts
├── gradle.properties
└── settings.gradle.kts
```

---

## ✅ Persyaratan

- **Android Studio** Jellyfish (2023.3.1) atau lebih baru
- **JDK** 17
- **Android SDK** API Level 29+ (Android 10)
- **Device/Emulator** dengan sensor GPS dan barometer
- **Google Play Services** terinstall di device

---

## 🚀 Instalasi & Menjalankan Proyek

### 1. Clone repository

```bash
git clone https://github.com/dwikidyp/FitRoute.git
cd FitRoute
```

### 2. Buka di Android Studio

```
File → Open → Pilih folder FitRoute
```

Tunggu proses **Gradle sync** selesai secara otomatis.

### 3. Konfigurasi

Buat file `local.properties` di root proyek dan tambahkan:

```properties
sdk.dir=/path/to/your/Android/sdk
BASE_URL="https://your-api-server.com/api/"
```

### 4. Build & Run

```bash
# Via terminal
./gradlew assembleDebug

# Via Android Studio
Run → Run 'app' (Shift+F10)
```

### 5. Izin yang diminta saat runtime

Saat pertama kali dijalankan, aplikasi akan meminta izin:
- **Lokasi presisi tinggi** (`ACCESS_FINE_LOCATION`) — untuk GPS tracking
- **Lokasi background** (`ACCESS_BACKGROUND_LOCATION`) — agar tracking berjalan saat layar terkunci
- **Pengenalan aktivitas** (`ACTIVITY_RECOGNITION`) — untuk deteksi jenis olahraga otomatis

---

## 🗄️ Skema Database

FitRoute menggunakan **Room Database** (SQLite) dengan 6 tabel utama:

```
USERS ──────────────────┬──────────────── BIOMETRIC_AUTH
  │                     │
  ├── WORKOUT_SESSIONS ──┤
  │     │               │
  │     ├── SENSOR_DATA │
  │     └── ELEVATION_POINTS
  │
  └── NOTIFICATIONS
```

| Tabel | Fungsi |
|---|---|
| `users` | Profil pengguna & data antropometri |
| `biometric_auth` | Status fingerprint & public key RSA |
| `workout_sessions` | Data agregat setiap sesi latihan |
| `sensor_data` | Raw data GPS/barometer/accelerometer/gyroscope per interval |
| `elevation_points` | Titik profil elevasi per sesi (sumber: barometer vs GPS) |
| `notifications` | Notifikasi in-app (PR, streak, sinkronisasi, laporan) |

---

## 🔄 Alur Aplikasi

```
Splash Screen
    ├── Sesi aktif → Dashboard
    └── Tidak ada sesi → Login
                            ├── Email & Password
                            ├── Fingerprint
                            └── Register → Setup Biometrik → Dashboard

Dashboard
    └── Mulai Latihan → Pilih Aktivitas → Live Tracking
                                              ├── Pause ↔ Resume
                                              ├── Profil Elevasi (real-time)
                                              └── Stop → Ringkasan Sesi
                                                            ├── Bagikan
                                                            └── Simpan → Riwayat
                                                                            ├── Filter (Lari/Sepeda/Hiking)
                                                                            ├── Detail Sesi
                                                                            └── Analytics
```

---

## 🧪 Pengujian

### Unit Test

```bash
./gradlew test
```

### Instrumented Test (membutuhkan device/emulator)

```bash
./gradlew connectedAndroidTest
```

### Checklist pengujian manual

- [ ] Login email/password dan biometrik berhasil
- [ ] Foreground service tetap berjalan saat layar terkunci
- [ ] Data GPS terekam dan tergambar sebagai polyline di peta
- [ ] Kalman Filter menghasilkan elevasi stabil tanpa noise
- [ ] Kalori bertambah lebih cepat saat kecepatan tinggi atau menanjak
- [ ] Personal record terdeteksi otomatis dan memicu notifikasi
- [ ] WorkManager sinkronisasi saat koneksi tersedia meski app ditutup
- [ ] `EncryptedSharedPreferences` tidak dapat dibaca aplikasi lain

### Target pengujian akademik

| Pengujian | Metode | Target |
|---|---|---|
| Akurasi GPS | Bandingkan dengan Garmin GPSMAP 67 | MAE ≤ 5m |
| Akurasi elevasi barometer | Bandingkan dengan altimeter kalibrasi | RMSE ≤ 3m |
| Estimasi kalori | Bandingkan dengan COSMED K5 metabolic analyzer | Akurasi ≥ 85% |
| Usability | System Usability Scale (SUS), n=30 | Skor ≥ 80 |

---

## 🔐 Keamanan

- **Enkripsi data transmisi** — RSA-2048 untuk pertukaran kunci, AES-256 untuk enkripsi payload
- **Penyimpanan lokal aman** — `EncryptedSharedPreferences` dengan AES-256-GCM
- **Autentikasi biometrik** — Android Biometric API, kunci disimpan di Android Keystore
- **Identifikasi perangkat** — Device UID (`ANDROID_ID`) untuk binding akun ke perangkat

---

## 🤝 Kontribusi

Proyek ini merupakan bagian dari penelitian akademik. Kontribusi sangat diterima:

1. Fork repository ini
2. Buat branch fitur baru (`git checkout -b feat/nama-fitur`)
3. Commit perubahan (`git commit -m 'feat: tambah fitur X'`)
4. Push ke branch (`git push origin feat/nama-fitur`)
5. Buat Pull Request

---

## 📄 Lisensi

Didistribusikan di bawah **Lisensi MIT**. Lihat [`LICENSE`](LICENSE) untuk informasi lebih lanjut.

---

## 👤 Penulis

**Dwiki Dzaki Yudi Putra**

- GitHub: [@dwikidyp](https://github.com/dwikidyp)
- Proyek: [github.com/dwikidyp/FitRoute](https://github.com/dwikidyp/FitRoute)

---

<div align="center">
  <sub>Dibuat dengan ❤️ sebagai proyek penelitian Teknik Informatika · 2026</sub>
</div>
