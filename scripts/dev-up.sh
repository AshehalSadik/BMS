#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$ROOT_DIR/.env.example" "$ENV_FILE"
  echo "Created .env from .env.example"
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required but not found in PATH." >&2
  exit 1
fi

if docker compose version >/dev/null 2>&1; then
  docker compose -f "$ROOT_DIR/docker-compose.yml" up -d
elif command -v docker-compose >/dev/null 2>&1; then
  docker-compose -f "$ROOT_DIR/docker-compose.yml" up -d
else
  echo "Docker Compose is required but not found." >&2
  exit 1
fi

cd "$ROOT_DIR"
./mvnw spring-boot:run

