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
  Write-Host "  all       Run full pipeline: up -> jdk -> test -> package (default)"
  Write-Host "  up        Build image and start container in background"
  Write-Host "  jdk       Show Java version used inside container"
  Write-Host "  test      Run gradle test in container (JDK 17)"
  Write-Host "  package   Run gradle clean build in container"
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
  docker compose -f $ComposeFile exec -T $Service bash -lc $Cmd
}

switch ($Command) {
  "all" {
    Ensure-Up
    Exec-InContainer 'echo "JAVA_HOME=$JAVA_HOME" && java -version'
    Exec-InContainer 'bash ./gradlew test'
    Exec-InContainer 'bash ./gradlew clean build'
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
  }
  "run-local" {
    .\gradlew.bat run
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
