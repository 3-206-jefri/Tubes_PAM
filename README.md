<div align="center">
  <img src=![Logo_PocketGuard.png](composeApp/src/commonMain/composeResources/drawable/Logo_PocketGuard.png) alt="PocketGuard Logo" width="100">

  <h1>🛡️ PocketGuard</h1>
  <p><b>Personal Finance Tracker & Smart Budgeting</b></p>

[![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin&logoColor=white)](#)
[![Compose](https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=android&logoColor=white)](#)
[![Status](https://img.shields.io/badge/Status-Release_Ready-success)](#)
</div>

---

## 📖 Tentang Proyek
**PocketGuard** adalah aplikasi manajemen keuangan pribadi berbasis **Kotlin Multiplatform (KMP)**. Dibangun dengan fokus pada kecepatan dan privasi (*offline-first*), aplikasi ini membantu pengguna melacak arus kas harian dan mengelola target pengeluaran bulanan melalui antarmuka yang bersih dan responsif.

[cite_start]Proyek ini dikembangkan sebagai pemenuhan Tugas Besar mata kuliah Pengembangan Aplikasi Mobile (PAM) di Institut Teknologi Sumatera[cite: 6, 7, 23].

### 🎬 Demo & Showcase
[cite_start]Lihat bagaimana PocketGuard bekerja secara langsung[cite: 205]:
* **Final Demo Release:** [Tonton di YouTube](#) *(Masukkan link)*
* **UI & Unit Test Coverage:** [Tonton di YouTube](#) *(Masukkan link)*

---

## ✨ Fitur Utama & Pratinjau Layar

| Dashboard & Riwayat | Smart Budgeting | Pencatatan Cepat |
| :---: | :---: | :---: |
| <img src="https://via.placeholder.com/200x400?text=Dashboard" width="200"/> | <img src="https://via.placeholder.com/200x400?text=Budget+Limit" width="200"/> | <img src="https://via.placeholder.com/200x400?text=Add+Transaction" width="200"/> |

* 💸 **Dynamic Transaction:** Pisahkan pemasukan dan pengeluaran dengan kategori visual yang intuitif (Makanan, Transport, Tagihan, Gaji).
* 🎯 **Visual Budget Tracker:** Tetapkan batas pengeluaran bulanan. *Progress bar* akan beradaptasi secara visual untuk mencegah *over-budget*.
* 🔍 **Advanced Filtering:** Temukan transaksi spesifik menggunakan pencarian *real-time* atau penyaringan berdasarkan riwayat bulan.
* 🛡️ **Offline-First Vault:** Semua riwayat keuangan dienkripsi dan disimpan secara lokal di memori perangkat pengguna, memastikan privasi total.

---

## 🛠️ Arsitektur & Teknologi

[cite_start]PocketGuard mengimplementasikan **Clean Architecture** (Domain, Data, Presentation) yang digabungkan dengan pola **MVVM** untuk memastikan skalabilitas kode[cite: 225].

**Core Stack:**
* **UI Toolkit:** Jetpack Compose Multiplatform
* [cite_start]**Database Layer:** SQLDelight (Native C-Interop) [cite: 223]
* [cite_start]**Dependency Injection:** Koin [cite: 223]
* [cite_start]**Networking API:** Ktor HTTP Client [cite: 223]
* **Local Preferences:** Jetpack DataStore
* **Asynchronous:** Kotlin Coroutines & StateFlow

> [cite_start]**Catatan Penilai:** *[Tambahkan gambar diagram arsitektur Anda di sini untuk memenuhi rubrik]* [cite: 226]

---

## 🧪 Strategi Pengujian (Testing)
Stabilitas aplikasi divalidasi melalui pengujian di berbagai lapisan:

1. **Presentation Layer:** Menggunakan **Robolectric** dan Compose UI Test untuk memvalidasi interaksi komponen UI. Skenario mencakup pengujian `EmptyState` pada layar kosong, validasi error pada fitur pencarian, dan keakuratan *formatter* mata uang pada kartu ringkasan saldo.
2. **Domain & Data Layer:** Memanfaatkan *Manual Fakes* untuk mereplikasi Repository secara aman di ekosistem KMP. Ini memastikan logika kalkulasi *budget* dan operasi CRUD database berjalan akurat melalui evaluasi *StateFlow* dengan library Turbine.

---

## 🚀 Panduan Instalasi (Getting Started)

1. [cite_start]**Persiapan Sistem:** Pastikan Android Studio terbaru (Jellyfish/Koala) dan JDK 17 telah terinstal[cite: 230].
2. **Kloning Repositori:**
   ```bash
   git clone [https://github.com/3-206-jefri/Tubes_PAM.git](https://github.com/3-206-jefri/Tubes_PAM.git)