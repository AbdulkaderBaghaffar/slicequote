#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

port="${WEB_PORT:-3000}"
url="http://localhost:${port}"

docker compose pull
docker compose up -d

printf 'Waiting for SliceQuote'
for _ in $(seq 1 90); do
  if curl -fsS "${url}/healthz" >/dev/null 2>&1; then
    printf '\nSliceQuote is up: %s\n' "$url"
    exit 0
  fi
  printf '.'
  sleep 2
done

printf '\nTimed out waiting for %s\nCheck: docker compose logs app web\n' "$url" >&2
exit 1
