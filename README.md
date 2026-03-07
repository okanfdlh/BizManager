# BizManager

Aplikasi desktop manajemen bisnis berbasis Kotlin + Compose Desktop.

## Tech Stack
- Kotlin JVM `1.9.22`
- JetBrains Compose Desktop `1.6.0`
- Gradle Wrapper `9.5.0-milestone-4`
- SQLite + Exposed ORM

## Prasyarat
- JDK `21` (disarankan Temurin 21)
- Docker + Docker Compose (opsional, jika ingin run via container)

## Struktur File Penting
- `build.gradle.kts`: konfigurasi build Gradle/Compose
- `dockerfile.yml`: image Docker berbasis JDK 21
- `dockercompose.yml`: service untuk menjalankan Gradle di container
- `run.sh`: helper script untuk build/test/run via Docker

## Menjalankan Secara Lokal (tanpa Docker)
Pastikan `JAVA_HOME` mengarah ke JDK 21.

```bash
./gradlew run
```

Build artifact:

```bash
./gradlew clean build
```

## Menjalankan dengan Docker
Script `run.sh` akan memakai `dockercompose.yml`.

Lihat bantuan:

```bash
./run.sh --help
```

Perintah yang tersedia:

```bash
./run.sh up       # build image + start container background
./run.sh jdk      # cek Java version di container (JDK 21)
./run.sh test     # jalankan unit test
./run.sh package  # clean build (default)
./run.sh run      # petunjuk run UI Compose Desktop
./run.sh run-local # jalankan UI di host
./run.sh shell    # masuk shell container
./run.sh down     # stop + hapus resource compose
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
