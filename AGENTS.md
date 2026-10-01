# AGENTS

Canonical AI coding guidance for this repository. Keep this file as the source of truth; `CLAUDE.md` points here.

## Scope
- Monorepo with Spring Boot backend (`backend`), CRA frontend (`frontend`), and deployment assets (`deploy`). See `DEPLOY.md` for full deployment instructions.
- API boundary: `/user/**` (auth/public-ish) vs `/api/**` (authenticated) in `backend/src/main/java/com/roomate/app/config/security/SecurityConfig.java`.

## Big-Picture Architecture
- Backend layering is stable: `controller -> service -> repository -> entities`; app entry is `backend/src/main/java/com/roomate/app/StartOneApplication.java`.
- Core room domain is orchestrated in `backend/src/main/java/com/roomate/app/service/implementation/RoomServiceImplt.java` with `RoomEntity` and `RoomMemberEntity`.
- Room rules are enforced server-side (not UI): max 3 rooms/user, max 6 members/room, role-gated membership actions.
- Frontend state is centralized in `frontend/src/App.jsx`; pages are expected to mutate cached app data helpers rather than refetching broadly.
- Use `frontend/src/apiClient.js` for HTTP (`withCredentials: true`, base URL from `REACT_APP_BASE_API_URL`).

## Auth and Request Flow
- `/user/login` (`AuthController`) returns token and sets HttpOnly `jwt` cookie.
- `JwtAuthenticationFilter` validates JWT from `jwt` cookie only (stateless, no bearer fallback).
- **CSRF Status (⚠️ currently disabled)**: `SecurityConfig` has `csrf.disable()` active. The intended approach
  is a double-submit cookie pattern (`CookieCsrfTokenRepository`): token in public `XSRF-TOKEN` cookie,
  sent via `X-CSRF-TOKEN` header on state-changing requests. See `CSRF_PROTECTION.md` for the plan.
  Re-enable before production hardening.
  - Excluded endpoints (when re-enabled): `/user/login`, `/user/register`, `/user/logout`, `/user/status`, `/user/verify`.
- Frontend boot path is `/user/status` -> `/api/get-user` -> `/api/profile-status` (see `frontend/src/App.jsx`).
  - `/user/status` GET would trigger CSRF token generation on client once re-enabled.
- `apiClient.js` is wired to add the CSRF token to POST/PUT/DELETE/PATCH requests via interceptor (no-op today while CSRF is off).
- `frontend/src/component/userProfileRedirection.jsx` still uses Auth0 hooks; treat this as mixed/legacy auth context.

## Developer Workflows
- Backend local: `cd backend && mvn spring-boot:run` (requires Postgres + Redis).
- Backend docker stack: `cd backend && docker compose up` (API 8085, primary Postgres→5433, budget Postgres→5434, Redis 6379).
- Backend tests: `cd backend && mvn test` (H2 + `test` profile from `backend/src/test/resources/application-test.yml`).
- Frontend local: `cd frontend && npm start`; build: `npm run build`; tests: `npm test`.
- Production: deployed via Railway using `frontend/railway.json` and `backend/railway.json` (each pointing to their own Dockerfile).

## Project-Specific Conventions
- Service implementation names are intentionally inconsistent (`*Implt`, `*Impl`, `UserServiceImplementation`); match existing naming in touched area.
- Controllers commonly use broad `try/catch` and return `ResponseEntity`; domain errors are often mapped via `UserApiError`.
- Prefer DTO-first contracts in `backend/src/main/java/com/roomate/app/dto`; avoid exposing entities unless an endpoint already does.
- Frontend role checks should use `frontend/src/constants/roles.jsx` constants, not raw role strings.
- Preserve eager vs lazy app-data loading behavior in `frontend/src/App.jsx` when adding UI data fetches.

## Integrations and Operational Notes
- **Security**: CSRF is currently disabled (see Auth section above); rate limiting is Bucket4j + Redis (fail-open when Redis down).
- Rate limiting is `RateLimitingFilter`, `RedisRateLimitConfig`; intentionally fail-open when Redis is down.
- Redis is also used for Spring Cache (`CacheConfig`): `roomChores`/`roomUtilities` (2 min TTL), `userChores`/`userUtilities` (1 min TTL).
- Email invite/verification relies on SMTP env vars (`EMAIL_*`) via `RoomInviteMailSender` and `UserServiceImplementation`.
- **Budget DB**: a second Postgres (`daroomate_budget`) is managed by `BudgetDataSourceConfig` and is **shared with n8n** for the receipt-parsing workflow. Both systems read/write it; keep schemas in sync manually until Flyway is enabled.
- **n8n**: `N8N_WEBHOOK_URL` and `N8N_WEBHOOK_TOKEN` env vars wire the receipt-parser flow. Frontend uses `REACT_APP_N8N_WEBHOOK_URL` at build time.
- WebSocket chat is scaffolded but inactive (`backend/src/main/java/com/roomate/app/websocket/WebSocketConfig.java` is commented; `frontend/src/webpages/Message.jsx` is placeholder).
- `DataSeeder` may auto-insert local test user/room data; account for this when debugging duplicate-looking data.
- JPA is `ddl-auto: update` and Flyway is disabled, so schema drift between environments is possible.

## Environment Variables (high-impact)
- Backend required: `POSTGRESQL_*` (host/port/database/username/password), `BUDGET_DB_*` (host/port/name/username/password), `JWT_KEY` (>=32 chars), `REDIS_URL`.
- Backend email (optional, needed for invites): `EMAIL_HOST`, `EMAIL_PORT`, `EMAIL_ID`, `EMAIL_PASSWORD`.
- Backend optional: `ACTIVE_PROFILE` (`prod`/`dev`), `CONTAINER_PORT` (default `8085`), `N8N_WEBHOOK_URL`, `N8N_WEBHOOK_TOKEN`, `CORS_ALLOWED_ORIGINS`.
- Frontend build-time: `REACT_APP_BASE_API_URL` (required), `REACT_APP_N8N_WEBHOOK_URL` (optional).
- Full variable table: see `DEPLOY.md`.

