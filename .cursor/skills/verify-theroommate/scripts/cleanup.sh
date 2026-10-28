#!/usr/bin/env bash
# Stop the verification stack this skill started. Leaves artifacts/ in place.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILL_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ROOT="$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel)"
RUN_DIR="$SKILL_DIR/run"
COMPOSE=(docker compose -p theroommate-verify
  -f "$ROOT/backend/docker-compose.yml"
  -f "$SKILL_DIR/docker-compose.verify.yml")

kill_tree() {
  local p="$1"
  local child
  for child in $(pgrep -P "$p" 2>/dev/null || true); do
    kill_tree "$child"
  done
  kill "$p" 2>/dev/null || true
}

if [[ -f "$RUN_DIR/frontend.pid" ]]; then
  pid="$(cat "$RUN_DIR/frontend.pid")"
  if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
    cmd="$(ps -p "$pid" -o command= 2>/dev/null || true)"
    if grep -q -e "react-scripts" -e "npm" <<<"$cmd"; then
      kill_tree "$pid"
    else
      echo "Refusing to kill pid $pid ($cmd)." >&2
    fi
  fi
fi

if docker info >/dev/null 2>&1; then
  "${COMPOSE[@]}" down -v --remove-orphans || true
fi

rm -rf "$RUN_DIR"
echo "Cleanup done. Artifacts remain in $SKILL_DIR/artifacts"
