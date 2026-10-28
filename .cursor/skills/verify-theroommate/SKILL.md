---
name: verify-theroommate
description: Drive the TheRoommate web app (React on port 3100, Spring API on port 18085) through sign-in, the dashboard, rooms, and room creation, and capture proof. Use when a change touches the frontend, login, rooms, or the local Docker stack and the user path has to be shown working.
---

# Verify TheRoommate

TheRoommate's primary surface is the React app in `frontend/`. It talks to the Spring API in `backend/`. Secondary surfaces are that API (cookie session) and the email-verification link; chat is a placeholder and is not a verification target.

One verification instance can run beside a normal `docker compose up` because this skill uses other ports and other container names. Two verification instances cannot. The ports below are fixed. If one of them is taken, stop. Do not point this skill at a stack you did not start.

| Piece | Where |
|---|---|
| Web | `http://localhost:3100` |
| API | `http://localhost:18085` |
| Compose project | `theroommate-verify` |
| Containers | `theroommate-verify-api`, `theroommate-verify-db`, `theroommate-verify-budget-db`, `theroommate-verify-redis` |

Seeded sign-in, written by `DataSeeder` on an empty database: `test1@example.com` / `Test_123!`. The seeder has no `@Profile` gate. The email is the constant in that class, not `TEST_DATA_EMAIL`. The seeded room is `University House`. That user is `HEAD_ROOMMATE` and is the only membership, so `/rooms` redirects into that room.

## Launch

From the repo root:

```bash
.cursor/skills/verify-theroommate/scripts/launch.sh
```

The script opens Docker Desktop if the daemon is down, builds `backend/docker-compose.yml` with `.cursor/skills/verify-theroommate/docker-compose.verify.yml`, and waits until both of these are true: the API log contains `TEST DATA SEEDING COMPLETE` or `Test user already exists`, and `http://localhost:18085/actuator/health` returns success. It then starts `npm start` in `frontend/` with `PORT=3100` and `REACT_APP_BASE_API_URL=http://localhost:18085`, and double-forks that process into a new session so it survives the script. Ready means the dev-server log contains `Compiled successfully` and `http://localhost:3100/login` returns `index.html` with `<title>TheRoommate</title>`. The heading `Welcome back` is client-rendered. Curl cannot see it. The browser drive is what proves that heading.

The dev image's healthcheck calls `wget`, and the image does not install `wget`. Ignore Docker's unhealthy mark. Readiness is the seed line plus the host health curl.

Teardown is `scripts/cleanup.sh`. Run it after a failed launch too, so a half-started stack does not keep the ports.

## Doctor

```bash
.cursor/skills/verify-theroommate/scripts/doctor.sh
```

Run this first whenever something looks off. It is read-only except for one login of the seeded user. It checks API health, the seed log line, that the recorded frontend pid is alive and is `npm` or `react-scripts`, that `/login` returns the HTML shell titled `TheRoommate`, and that `GET /api/rooms` with the login cookie includes `University House`. `doctor: ok` is the only passing line that matters.

## Drive

Use the Cursor IDE browser tools against `http://localhost:3100`. Prefer the accessible name (`Email address`, `Sign in`, `Dashboard`, `My Rooms`, `University House`) over coordinates. The feature files in `features/` are the recipes. Read `features/README.md` before driving, then follow one feature file completely.

A shell proof of the same session, without a browser, is not a substitute for the click path. Use it as the second view of stored state:

```bash
curl -s -c /tmp/theroommate-verify.cookies -H 'Content-Type: application/json' \
  -d '{"email":"test1@example.com","password":"Test_123!"}' \
  http://localhost:18085/user/login -o /dev/null
curl -s -b /tmp/theroommate-verify.cookies http://localhost:18085/api/rooms
```

Do not archive the login response body or the cookie jar. `AuthDto` includes the JWT.

## Evidence

Write proof under `.cursor/skills/verify-theroommate/artifacts/<feature-id>/`. Capture the action and the state after it: an accessibility snapshot and a screenshot of the page that shows the app name `TheRoommate` and the result you claim. For a mutation, also keep a second view (reopen the screen, or the rooms JSON with the token redacted). Name the feature id and the entry point in a `notes.txt` next to those files.

Exercise the real UI. Do not call a service method, flip `localStorage.appAuth`, or hit a test-only endpoint and call that the proof. `login()` writes `appAuth` before the cookie is known to stick, so a dashboard shell with no room data is not proof. Onboarding (`Welcome to DaRoommate`) means the rooms request came back empty.

## Cleanup

```bash
.cursor/skills/verify-theroommate/scripts/cleanup.sh
```

This kills the frontend pid recorded in `run/frontend.pid` (and its children) only when that process is `npm` or `react-scripts`, then runs `docker compose -p theroommate-verify down -v` on the two compose files. It deletes `run/`. It does not delete `artifacts/`. After cleanup, the screenshot and snapshot must still be on disk.

## Helpers

- `.cursor/skills/verify-theroommate/scripts/launch.sh`
- `.cursor/skills/verify-theroommate/scripts/doctor.sh`
- `.cursor/skills/verify-theroommate/scripts/cleanup.sh`

All three are executable. `docker-compose.verify.yml` is the port and container-name overlay. It stays after cleanup.
