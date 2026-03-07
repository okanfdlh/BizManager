#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="dockercompose.yml"
SERVICE="bizmanager"

usage() {
  cat <<USAGE
Usage: ./run.sh [command]

Commands:
  all      Run full pipeline: up -> jdk -> test -> package (default)
  up       Build image and start container in background
  jdk      Show Java version used inside container
  test     Run ./gradlew test in container (JDK 17)
  package  Run ./gradlew clean build in container
  run      Show how to run Compose Desktop app (UI) correctly
  run-local Run ./gradlew run on host (requires host JDK 17)
  shell    Open interactive shell in running container
  down     Stop and remove compose resources

Examples:
  ./run.sh all
  ./run.sh up
  ./run.sh jdk
  ./run.sh package
  ./run.sh test
  ./run.sh run
  ./run.sh run-local
USAGE
}

ensure_up() {
  docker compose -f "$COMPOSE_FILE" up -d --build "$SERVICE"
}

exec_in_container() {
  local cmd="$1"
  docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" bash -lc "$cmd"
}

cmd="${1:-all}"

case "$cmd" in
  all)
    ensure_up
    exec_in_container 'echo "JAVA_HOME=$JAVA_HOME" && java -version'
    exec_in_container './gradlew test'
    exec_in_container './gradlew clean build'
    ;;

  up)
    ensure_up
    ;;

  jdk)
    ensure_up
    exec_in_container 'echo "JAVA_HOME=$JAVA_HOME" && java -version'
    ;;

  test)
    ensure_up
    exec_in_container './gradlew test'
    ;;

  package)
    ensure_up
    exec_in_container './gradlew clean build'
    ;;

  run)
    cat <<MSG
Compose Desktop app membutuhkan GUI/OpenGL dan tidak cocok dijalankan interaktif di container headless.

Yang direkomendasikan:
1) Build/test tetap lewat Docker JDK 17:
   ./run.sh all
2) Jalankan UI di host:
   ./run.sh run-local
MSG
    ;;

  run-local)
    ./gradlew run
    ;;

  shell)
    ensure_up
    docker compose -f "$COMPOSE_FILE" exec "$SERVICE" bash
    ;;

  down)
    docker compose -f "$COMPOSE_FILE" down -v
    ;;

  -h|--help|help)
    usage
    ;;

  *)
    echo "Unknown command: $cmd"
    usage
    exit 1
    ;;
esac
