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

========================================
STEP BY STEP CARA MENJALANKAN (WINDOWS)
========================================

1) Buka PowerShell di folder project `BizManager`.

2) Jalankan full pipeline build/test dengan JDK 17 dari Docker:
PowerShell
.\run.ps1 all

Yang dilakukan command ini:
- reset container docker (down + up dari awal)
- cek versi Java di container (harus JDK 17)
- jalankan test
- jalankan clean build
- output build tersimpan ke folder host `build\`

3) (Opsional) Jalankan aplikasi desktop di host Windows:
PowerShell
.\run.ps1 run-local

Catatan: command ini memakai JDK host (bukan Docker), jadi pastikan host sudah install JDK 17.

4) Generate installer Windows (.exe/.msi):
PowerShell
.\run.ps1 win-installer

Hasil installer ada di:
- `build\compose\binaries\main-release\exe\`
- `build\compose\binaries\main-release\msi\`

5) Jika ingin membersihkan environment Docker:
PowerShell
.\run.ps1 down

Troubleshooting cepat:
- Jika error `gradlew` terkait CRLF, jalankan lagi `.\run.ps1 all` (script sudah auto-normalisasi).
- Jika `win-installer` gagal karena Java, install/set JDK 17 di host lalu ulangi.
