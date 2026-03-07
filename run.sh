#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="dockercompose.yml"
SERVICE="bizmanager"

usage() {
  cat <<USAGE
Usage: ./run.sh [command]

Commands:
  build    Build Docker image only
  test     Run Gradle tests in container
  package  Build app artifacts in container (default)
  run      Run Gradle desktop app task in container
  shell    Open interactive shell in container
  down     Stop and remove compose resources
USAGE
}

cmd="${1:-package}"

case "$cmd" in
  build)
    docker compose -f "$COMPOSE_FILE" build
    ;;

  test)
    docker compose -f "$COMPOSE_FILE" run --rm "$SERVICE" bash -lc "./gradlew test"
    ;;

  package)
    docker compose -f "$COMPOSE_FILE" run --rm "$SERVICE" bash -lc "./gradlew clean build"
    ;;

  run)
    docker compose -f "$COMPOSE_FILE" run --rm "$SERVICE" bash -lc "./gradlew run"
    ;;

  shell)
    docker compose -f "$COMPOSE_FILE" run --rm "$SERVICE" bash
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
