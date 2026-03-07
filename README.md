# BizManager

Aplikasi desktop manajemen bisnis berbasis Kotlin + Compose Desktop.

## Tech Stack
- Kotlin JVM `1.9.22`
- JetBrains Compose Desktop `1.6.0`
- Gradle Wrapper `9.5.0-milestone-4`
- SQLite + Exposed ORM

## Prasyarat
- JDK `17` (disarankan Temurin 17)
- Docker + Docker Compose (opsional, jika ingin run via container)

## Struktur File Penting
- `build.gradle.kts`: konfigurasi build Gradle/Compose
- `dockerfile.yml`: image Docker berbasis JDK 17
- `dockercompose.yml`: service untuk menjalankan Gradle di container
- `run.sh`: helper script untuk build/test/run via Docker
- `run.ps1`: helper script untuk Windows (PowerShell)

## Menjalankan Secara Lokal (tanpa Docker)
Pastikan `JAVA_HOME` mengarah ke JDK 17.

```bash
./gradlew run
```

Build artifact:

```bash
./gradlew clean build
```

## Menjalankan dengan Docker
Script `run.sh`/`run.ps1` akan memakai `dockercompose.yml`.

Lihat bantuan:

```bash
./run.sh --help
```

Perintah yang tersedia:

```bash
./run.sh all      # jalankan semua tahap (default)
./run.sh up       # build image + start container background
./run.sh jdk      # cek Java version di container (JDK 17)
./run.sh test     # jalankan unit test
./run.sh package  # clean build
./run.sh run      # petunjuk run UI Compose Desktop
./run.sh run-local # jalankan UI di host
./run.sh shell    # masuk shell container
./run.sh down     # stop + hapus resource compose
```

Windows (PowerShell):

```powershell
.\run.ps1 all
.\run.ps1 jdk
.\run.ps1 package
```

## Catatan Build Installer
- `packageVersion` sudah diset ke format valid (`1.0.0`) untuk native distribution.
- Build installer Windows (`.msi/.exe`) harus dilakukan di environment **Windows** (native atau CI windows runner).
- Menjalankan build di macOS/Linux tidak menghasilkan installer Windows native.

## Git Ignore
File yang tidak perlu dipush sudah diatur di `.gitignore` (cache/build/IDE/local env/artifact installer).

## Author
- Nama: [Indirokan Fadhilah](https://github.com/okanfdlh)
- Email: indirokan.fadhilah@gmail.com
- Website: [indirokanfadhilah.vercel.app](https://indirokanfadhilah.vercel.app)
