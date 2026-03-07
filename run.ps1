param(
  [string]$Command = "all"
)

$ErrorActionPreference = "Stop"

$ComposeFile = "dockercompose.yml"
$Service = "bizmanager"

function Show-Usage {
  Write-Host "Usage: .\run.ps1 [command]"
  Write-Host ""
  Write-Host "Commands:"
  Write-Host "  all       Run full pipeline in Docker JDK 17 (output stays on host) (default)"
  Write-Host "  up        Build image and start container in background"
  Write-Host "  jdk       Show Java version used inside container"
  Write-Host "  test      Run gradle test in container (JDK 17)"
  Write-Host "  package   Run gradle clean build in container"
  Write-Host "  win-installer Build Windows installer (.exe/.msi) on host"
  Write-Host "  run       Show how to run Compose Desktop app (UI) correctly"
  Write-Host "  run-local Run gradlew run on host (requires host JDK 17)"
  Write-Host "  shell     Open interactive shell in running container"
  Write-Host "  down      Stop and remove compose resources"
}

function Ensure-Up {
  docker compose -f $ComposeFile down -v --remove-orphans
  docker compose -f $ComposeFile up -d --build $Service
}

function Exec-InContainer([string]$Cmd) {
  docker compose -f $ComposeFile exec -T $Service bash -lc "sed -i 's/\r`$//' ./gradlew && chmod +x ./gradlew && $Cmd"
}

function Ensure-HostJdk17 {
  $versionOut = & .\gradlew.bat -version 2>&1
  if ($LASTEXITCODE -ne 0) {
    throw "Gagal cek versi Gradle/JDK host. Pastikan JDK 17 terpasang dan JAVA_HOME benar."
  }

  $joined = ($versionOut | Out-String)
  if ($joined -notmatch "JVM:\s+17(\.| )") {
    Write-Host "JDK host terdeteksi bukan 17."
    Write-Host "Silakan set JDK 17 lalu jalankan ulang."
    Write-Host "Contoh sementara (PowerShell):"
    Write-Host '$env:JAVA_HOME="C:\Program Files\Java\jdk-17"'
    Write-Host '$env:Path="$env:JAVA_HOME\bin;$env:Path"'
    throw "Host JDK harus versi 17 untuk run-local/win-installer."
  }
}

function Exec-HostGradle {
  param(
    [string[]]$Args
  )
  & .\gradlew.bat @Args
  if ($LASTEXITCODE -ne 0) {
    throw "Gradle command failed: gradlew.bat $($Args -join ' ')"
  }
}

switch ($Command) {
  "all" {
    Ensure-Up
    Exec-InContainer 'echo "JAVA_HOME=$JAVA_HOME" && java -version'
    Exec-InContainer 'bash ./gradlew test'
    Exec-InContainer 'bash ./gradlew clean build'
    Write-Host "Docker build selesai. Output ada di folder host: build\"
    Write-Host "Untuk installer Windows (.exe/.msi), jalankan: .\run.ps1 win-installer"
  }
  "up" {
    Ensure-Up
  }
  "jdk" {
    Ensure-Up
    Exec-InContainer 'echo "JAVA_HOME=$JAVA_HOME" && java -version'
  }
  "test" {
    Ensure-Up
    Exec-InContainer 'bash ./gradlew test'
  }
  "package" {
    Ensure-Up
    Exec-InContainer 'bash ./gradlew clean build'
  }
  "run" {
    Write-Host "Compose Desktop app butuh GUI/OpenGL, jadi tidak cocok dijalankan interaktif di container headless."
    Write-Host "Gunakan .\run.ps1 all untuk build/test Docker, lalu .\run.ps1 run-local untuk jalankan UI di host."
    Write-Host "Untuk output installer Windows, jalankan: .\run.ps1 win-installer"
  }
  "win-installer" {
    Ensure-HostJdk17
    Write-Host "Installer Windows dijalankan di host (butuh JDK 17 di host)."
    Exec-HostGradle @("packageExe", "packageMsi")
    Write-Host "Output:"
    Write-Host "  build\compose\binaries\main\exe\"
    Write-Host "  build\compose\binaries\main\msi\"
  }
  "run-local" {
    Ensure-HostJdk17
    Exec-HostGradle @("run")
  }
  "shell" {
    Ensure-Up
    docker compose -f $ComposeFile exec $Service bash
  }
  "down" {
    docker compose -f $ComposeFile down -v
  }
  "help" {
    Show-Usage
  }
  default {
    Write-Error "Unknown command: $Command"
    Show-Usage
    exit 1
  }
}
