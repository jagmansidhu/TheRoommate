#!/usr/bin/env bash
# Read-only: is the verification instance worth driving?
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILL_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
STATE="$SKILL_DIR/run/state.env"

if [[ ! -f "$STATE" ]]; then
  echo "No verification state file. Launch first." >&2
  exit 1
fi
# shellcheck disable=SC1090
source "$STATE"

fail() {
  echo "doctor: $1" >&2
  exit 1
}

curl -sf "$API_URL/actuator/health" >/dev/null || fail "API health failed at $API_URL/actuator/health"

logs="$(docker logs theroommate-verify-api 2>&1 || true)"
grep -q -e "TEST DATA SEEDING COMPLETE" -e "Test user already exists" <<<"$logs" \
  || fail "DataSeeder has not finished"

pid="${FRONTEND_PID:-}"
[[ -n "$pid" ]] || fail "frontend pid missing"
kill -0 "$pid" 2>/dev/null || fail "frontend pid $pid is not running"
cmd="$(ps -p "$pid" -o command= 2>/dev/null || true)"
grep -q -e "react-scripts" -e "npm" <<<"$cmd" || fail "pid $pid is not the verification frontend ($cmd)"

curl -sf "$WEB_URL/login" | grep -q "<title>TheRoommate</title>" \
  || fail "login shell is not at $WEB_URL/login (curl sees index.html, not the React heading)"

cookie="$SKILL_DIR/run/doctor.cookies"
rm -f "$cookie"
code="$(curl -s -o /dev/null -w "%{http_code}" \
  -c "$cookie" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$SEED_USER\",\"password\":\"$SEED_PASSWORD\"}" \
  "$API_URL/user/login")"
[[ "$code" == "200" ]] || fail "seed login returned HTTP $code"
rooms="$(curl -s -b "$cookie" "$API_URL/api/rooms")"
grep -q "University House" <<<"$rooms" || fail "seeded rooms response has no University House"
rm -f "$cookie"

echo "doctor: ok"
echo "web $WEB_URL"
echo "api $API_URL"
echo "frontend pid $pid"
echo "seed user $SEED_USER"
