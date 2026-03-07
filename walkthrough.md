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
Aplikasi ini dibangun menggunakan JetBrains Compose for Desktop (Kotlin) dengan JDK 17.

Untuk menjalankan aplikasi di Windows:

1) Build/test via Docker (opsional, aman dari konflik JDK host):
PowerShell
.\run.ps1 all

2) Jalankan UI lokal di Windows host:
PowerShell
.\run.ps1 run-local

3) Build installer Windows native (.exe/.msi):
PowerShell
.\run.ps1 win-installer

Output installer:
- build\compose\binaries\main-release\exe\
- build\compose\binaries\main-release\msi\

Catatan:
- Output .exe/.msi tidak dihasilkan dari container Linux.
- Installer Windows harus dipackage dari environment Windows host.
