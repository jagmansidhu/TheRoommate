#!/usr/bin/env bash
# Start an isolated TheRoommate stack for verification.
# Containers, volumes, and the frontend process belong to this run only.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILL_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ROOT="$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel)"
RUN_DIR="$SKILL_DIR/run"
STATE="$RUN_DIR/state.env"
COMPOSE=(docker compose -p theroommate-verify
  -f "$ROOT/backend/docker-compose.yml"
  -f "$SKILL_DIR/docker-compose.verify.yml")
API_URL="http://localhost:18085"
WEB_URL="http://localhost:3100"
SEED_USER="test1@example.com"
SEED_PASSWORD='Test_123!'

port_busy() {
  local port="$1"
  lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

wait_for_docker() {
  if docker info >/dev/null 2>&1; then
    return 0
  fi
  echo "Docker daemon is down. Opening Docker Desktop."
  open -a Docker
  local i
  for i in $(seq 1 60); do
    if docker info >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  echo "Docker daemon did not come up." >&2
  exit 1
}

web_html_ok() {
  curl -sf "$WEB_URL/login" | grep -q "<title>TheRoommate</title>"
}

api_is_ours() {
  local running logs
  running="$(docker inspect -f '{{.State.Running}}' theroommate-verify-api 2>/dev/null || true)"
  [[ "$running" == "true" ]] || return 1
  curl -sf "$API_URL/actuator/health" >/dev/null 2>&1 || return 1
  logs="$(docker logs theroommate-verify-api 2>&1 || true)"
  grep -q -e "TEST DATA SEEDING COMPLETE" -e "Test user already exists" <<<"$logs"
}

if [[ -f "$STATE" ]] && api_is_ours && web_html_ok; then
  echo "Verification stack already usable at $WEB_URL (api $API_URL)."
  exit 0
fi

wait_for_docker
mkdir -p "$RUN_DIR"

if api_is_ours; then
  echo "API already usable at $API_URL."
else
  for port in 18085 15433 15434 16379; do
    if port_busy "$port"; then
      echo "Port $port is already in use. Refusing to start a second stack." >&2
      exit 1
    fi
  done
  echo "Building and starting theroommate-verify."
  "${COMPOSE[@]}" up -d --build

  echo "Waiting for DataSeeder and actuator health."
  ready=0
  for _ in $(seq 1 180); do
    logs="$(docker logs theroommate-verify-api 2>&1 || true)"
    if grep -q "APPLICATION FAILED TO START" <<<"$logs"; then
      echo "$logs" | tail -n 80 >&2
      echo "API failed to start." >&2
      exit 1
    fi
    if grep -q -e "TEST DATA SEEDING COMPLETE" -e "Test user already exists" <<<"$logs" \
      && curl -sf "$API_URL/actuator/health" >/dev/null 2>&1; then
      ready=1
      break
    fi
    sleep 5
  done
  if [[ "$ready" -ne 1 ]]; then
    echo "API did not become usable. Last logs:" >&2
    docker logs --tail 80 theroommate-verify-api >&2 || true
    exit 1
  fi
fi

if port_busy 3100; then
  echo "Port 3100 is already in use. Refusing to start a second frontend." >&2
  exit 1
fi

echo "Starting frontend on $WEB_URL."
: >"$RUN_DIR/frontend.log"
# Double-fork into a new session so the dev server survives this script exiting.
# A background job of this shell is killed with the shell.
(
  cd "$ROOT/frontend"
  FRONTEND_DIR="$ROOT/frontend" python3 - "$RUN_DIR/frontend.pid" "$RUN_DIR/frontend.log" "$API_URL" <<'PY'
import os, sys
pidfile, logfile, api = sys.argv[1:]
if os.fork() > 0:
    os.wait()
    raise SystemExit(0)
os.setsid()
log = open(logfile, "ab", buffering=0)
os.dup2(log.fileno(), 1)
os.dup2(log.fileno(), 2)
child = os.fork()
if child > 0:
    with open(pidfile, "w") as fh:
        fh.write(str(child))
    os._exit(0)
os.environ["PORT"] = "3100"
os.environ["BROWSER"] = "none"
os.environ["REACT_APP_BASE_API_URL"] = api
os.chdir(os.environ["FRONTEND_DIR"])
os.execvp("npm", ["npm", "start"])
PY
)

web_ready=0
for _ in $(seq 1 60); do
  # The login heading is rendered by React. Curl only sees index.html.
  if web_html_ok && grep -q -e "Compiled successfully" -e "webpack compiled successfully" "$RUN_DIR/frontend.log"; then
    web_ready=1
    break
  fi
  sleep 2
done
if [[ "$web_ready" -ne 1 ]]; then
  echo "Frontend did not serve index.html. See $RUN_DIR/frontend.log" >&2
  exit 1
fi

cat >"$STATE" <<EOF
API_URL=$API_URL
WEB_URL=$WEB_URL
COMPOSE_PROJECT=theroommate-verify
FRONTEND_PID=$(cat "$RUN_DIR/frontend.pid")
SEED_USER=$SEED_USER
SEED_PASSWORD=$SEED_PASSWORD
EOF

echo "Ready. Web $WEB_URL  API $API_URL  user $SEED_USER"
