Walkthrough: BizManager
BizManager adalah aplikasi Windows Desktop pengelolaan Invoice dan Piutang offline (stand-alone) yang dibangun menggunakan Jetbrains Compose for Desktop (Kotlin) dan SQLite. Seluruh fitur dari blueprint telah diimplementasikan.

Features Implemented:
Dashboard Ringkasan Omzet, Piutang Berjalan, dan Laba Bersih untuk bulan ini.
Customer & Product Management
Entri data dengan validasi, filter Aktif/Nonaktif.
Histori penjualan disematkan di detail Customer.
Invoice Processing
Memastikan data base bisa dicoret-coret dan diduplikasi secara safety tanpa bergantung pada Cloud.
Restore menyalin database lama jadi Safeguard AutoBackup terlebih dahulu sebelum override.
Prerequisites & Compilation Instructions
Aplikasi ini dibangun menggunakan framework JetBrains Compose for Desktop (Kotlin). Pada environment ini, dideteksi bahwa mesin belum menginstall Java Runtime Environment (JRE/JDK).

Untuk menjalankan aplikasi ini secara Native di Windows Desktop layaknya aplikasi standar:

Install Java JDK 17+ (Rekomendasi) Download dan pasang Adoptium Temurin (JDK 17) atau Oracle JDK dari situs resminya.
Build the Application Buka terminal di folder project (BizManager), lalu jalankan:
bash
./gradlew run
Membangun Installer (.exe/.msi) Sekalinya UI/Logic sudah dites dengan run, jalankan perintah berikut untuk menghasilkan distribusi Native Windows Setup:
bash
./gradlew packageDistributionForCurrentOS
(File eksekusi akan berada di /build/compose/binaries/main/app)