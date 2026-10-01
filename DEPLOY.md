# Deployment Guide

Covers every way to run TheRoommate — local bare-metal, local Docker stack, and production on Railway.

---

## Architecture overview

```
Browser / SPA (React + nginx)
        │  HTTPS
        ▼
Spring Boot API (port 8085)
   ├── JWT cookie auth  ──────►  Primary PostgreSQL DB  (daroomate)
   ├── Redis            ──────►  Rate limiting + caching (Bucket4j / Spring Cache)
   ├── SMTP             ──────►  Email invites / verification
   └── Budget DB calls  ──────►  Budget PostgreSQL DB    (daroomate_budget)
                                         ▲
                                     n8n webhook
                               (receipt-parser AI flow)
```

**Railway services in production**

| Service | Image | Notes |
|---------|-------|-------|
| `backend` | `backend/Dockerfile` | Spring Boot fat-JAR, Java 21 |
| `frontend` | `frontend/Dockerfile` | React CRA build served by nginx |
| `postgres` (primary) | Railway managed PostgreSQL | DB name `daroomate` |
| `postgres` (budget) | Railway managed PostgreSQL | DB name `daroomate_budget`, shared with n8n |
| `redis` | Railway managed Redis | Rate limiting + Spring Cache |
| `n8n` | Railway managed n8n | Receipt-parsing AI workflow |

---

## 1 — Local: bare-metal (fastest iteration)

### Prerequisites
- Java 21 (temurin recommended)
- Node.js >= 20
- PostgreSQL 17 running locally (two databases: `daroomate` and `daroomate_budget`)
- Redis running locally on port 6379

### Backend

```bash
cd backend

export POSTGRESQL_HOST=localhost
export POSTGRESQL_PORT=5432
export POSTGRESQL_DATABASE=daroomate
export POSTGRESQL_USERNAME=postgres
export POSTGRESQL_PASSWORD=password
export BUDGET_DB_HOST=localhost
export BUDGET_DB_PORT=5432
export BUDGET_DB_NAME=daroomate_budget
export BUDGET_DB_USERNAME=postgres
export BUDGET_DB_PASSWORD=password
export JWT_KEY=your-super-secret-key-at-least-32-chars
export REDIS_URL=redis://localhost:6379
export CORS_ALLOWED_ORIGINS=http://localhost:3000

mvn spring-boot:run
# API available at http://localhost:8085
```

### Frontend

```bash
cd frontend

echo "REACT_APP_BASE_API_URL=http://localhost:8085" > .env

npm install
npm start
# App available at http://localhost:3000
```

---

## 2 — Local: Docker stack (mirrors production)

The `backend/docker-compose.yml` spins up the API, two Postgres instances, and Redis.
Source code is **volume-mounted** so Spring Boot DevTools auto-restarts on `.java` changes (~3–5 s, no image rebuild).

```bash
cd backend

# First run or after pom.xml changes
docker compose up --build

# Subsequent runs (fast — no rebuild)
docker compose up

# Tear down (data volumes preserved)
docker compose down

# Tear down AND wipe DB volumes
docker compose down -v
```

| Port (host) | Service |
|-------------|---------|
| `8085` | Spring Boot API |
| `5433` | Primary PostgreSQL (daroomate) |
| `5434` | Budget PostgreSQL (daroomate_budget) |
| `6379` | Redis |

Frontend is **not** part of the compose file; run it separately with `npm start` in `frontend/`.

> **Email:** SMTP variables (`EMAIL_HOST`, `EMAIL_PORT`, `EMAIL_ID`, `EMAIL_PASSWORD`) default to empty in docker-compose. Email features will silently fail until set.

---

## 3 — Production: Railway

Both services have a `railway.json` that points Railway at their respective `Dockerfile`.
Deploy is triggered by pushing to the linked Git branch (typically `main`).

### Backend deployment

1. Create a Railway project and add a **GitHub** source pointing to `backend/`.
2. Add a **PostgreSQL** plugin — this becomes the primary DB.
3. Add a second **PostgreSQL** plugin — this becomes the budget DB (shared with n8n if used).
4. Add a **Redis** plugin.
5. Set all required environment variables (see table below).
6. Railway reads `backend/railway.json` automatically and builds with `backend/Dockerfile`.
7. Health check: `GET /actuator/health/liveness` (timeout 60 s).

### Frontend deployment

1. Add a second Railway service pointing to `frontend/`.
2. Set build-time vars (they must be present **at build time**, not just runtime):
   - `REACT_APP_BASE_API_URL` — the Railway-provided URL of the backend service.
   - `REACT_APP_N8N_WEBHOOK_URL` — n8n webhook URL (optional; required for receipt parsing).
3. Railway reads `frontend/railway.json` and builds with `frontend/Dockerfile`.
4. Health check: `GET /health` (returns `200 OK` via nginx).

---

## Environment variable reference

### Backend (required)

| Variable | Example | Description |
|----------|---------|-------------|
| `POSTGRESQL_HOST` | `postgres.railway.internal` | Primary DB host |
| `POSTGRESQL_PORT` | `5432` | Primary DB port |
| `POSTGRESQL_DATABASE` | `daroomate` | Primary DB name |
| `POSTGRESQL_USERNAME` | `postgres` | Primary DB user |
| `POSTGRESQL_PASSWORD` | `...` | Primary DB password |
| `BUDGET_DB_HOST` | `budget-postgres.railway.internal` | Budget DB host |
| `BUDGET_DB_PORT` | `5432` | Budget DB port |
| `BUDGET_DB_NAME` | `daroomate_budget` | Budget DB name |
| `BUDGET_DB_USERNAME` | `postgres` | Budget DB user |
| `BUDGET_DB_PASSWORD` | `...` | Budget DB password |
| `JWT_KEY` | `<>=32 random chars>` | JWT signing secret — never reuse across envs |
| `REDIS_URL` | `redis://redis.railway.internal:6379` | Redis connection URL |
| `CORS_ALLOWED_ORIGINS` | `https://yourfrontend.up.railway.app` | Comma-separated allowed origins |

### Backend (optional)

| Variable | Default | Description |
|----------|---------|-------------|
| `ACTIVE_PROFILE` | `dev` | Spring profile (`dev` / `prod`) |
| `CONTAINER_PORT` | `8085` | HTTP port the JVM listens on |
| `EMAIL_HOST` | *(empty)* | SMTP host |
| `EMAIL_PORT` | `25` | SMTP port |
| `EMAIL_ID` | *(empty)* | SMTP sender address |
| `EMAIL_PASSWORD` | *(empty)* | SMTP password |
| `N8N_WEBHOOK_URL` | *(empty)* | n8n webhook base URL |
| `N8N_WEBHOOK_TOKEN` | *(empty)* | n8n webhook auth token |

### Frontend (build-time, required for production)

| Variable | Description |
|----------|-------------|
| `REACT_APP_BASE_API_URL` | Full URL of the backend API |
| `REACT_APP_N8N_WEBHOOK_URL` | n8n webhook URL for receipt parsing (optional) |

> **Important:** React bakes `REACT_APP_*` variables into the static bundle at build time.
> Changing them requires a **redeploy**, not just a restart.

---

## Running tests

### Backend

```bash
cd backend
mvn test
# Uses H2 in-memory DB via src/test/resources/application-test.yml
# No external Postgres or Redis needed
```

### Frontend

```bash
cd frontend
npm test
```

---

## Health checks

| Endpoint | Description |
|----------|-------------|
| `GET /actuator/health` | Spring Boot health (all sub-indicators) |
| `GET /actuator/health/liveness` | Liveness probe (used by Railway) |
| `GET /actuator/health/readiness` | Readiness probe |
| `GET /health` | Frontend nginx — returns `200 OK` |

---

## CI / GitHub Actions

Workflows in `.github/workflows/`:

| Workflow | Trigger | What it does |
|----------|---------|-------------|
| `maven.yml` | push/PR | Compiles + runs unit tests |
| `config-safety-check.yml` | push/PR | Blocks `ddl-auto: drop`, detects hardcoded secrets |
| `dependency-security.yml` | push/PR | OWASP Dependency-Check (Maven) + `npm audit` |
| `code-security-scan.yml` | push/PR | Semgrep OWASP Top 10, SpotBugs, TruffleHog |
| `docker-security.yml` | push/PR | Hadolint + Trivy container scanning |
| `database-safety.yml` | push/PR | Blocks DROP/TRUNCATE in migrations |

---

## Known limitations / gotchas

- **Schema management:** Flyway is disabled; Hibernate uses `ddl-auto: update`.
  Schema drift between environments is possible. Consider enabling Flyway for production.
- **CSRF:** Currently `csrf.disable()` is active in `SecurityConfig`. The double-submit cookie
  pattern described in `SECURITY.md` / `AGENTS.md` is the intended target but not yet
  live in the filter chain. Re-enable before going to production.
- **WebSocket chat:** `WebSocketConfig.java` is commented out; `Message.jsx` is a placeholder.
  Chat is scaffolded but not functional.
- **Budget DB shared with n8n:** Both the Spring Boot backend and n8n read/write
  `daroomate_budget`. Keep schemas in sync manually until a migration tool is added.
- **DataSeeder:** May auto-insert test data in `dev` profile environments.
  Check for duplicate-looking entries when debugging.
