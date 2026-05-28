# AGENTS

Canonical AI coding guidance for this repository. Keep this file as the source of truth; `CLAUDE.md` points here.

## Scope
- Monorepo with Spring Boot backend (`backend`), CRA frontend (`frontend`), and deployment assets (`deploy`).
- API boundary: `/user/**` (auth/public-ish) vs `/api/**` (authenticated) in `backend/src/main/java/com/roomate/app/config/security/SecurityConfig.java`.

## Big-Picture Architecture
- Backend layering is stable: `controller -> service -> repository -> entities`; app entry is `backend/src/main/java/com/roomate/app/StartOneApplication.java`.
- Core room domain is orchestrated in `backend/src/main/java/com/roomate/app/service/implementation/RoomServiceImplt.java` with `RoomEntity` and `RoomMemberEntity`.
- Room rules are enforced server-side (not UI): max 3 rooms/user, max 6 members/room, role-gated membership actions.
- Frontend state is centralized in `frontend/src/App.jsx`; pages are expected to mutate cached app data helpers rather than refetching broadly.
- Use `frontend/src/apiClient.js` for HTTP (`withCredentials: true`, base URL from `REACT_APP_BASE_API_URL`).

## Auth and Request Flow
- `/user/login` (`AuthController`) returns token and sets HttpOnly `jwt` cookie.
- `JwtAuthenticationFilter` accepts either `Authorization: Bearer <token>` or `jwt` cookie.
- Frontend boot path is `/user/status` -> `/api/get-user` -> `/api/profile-status` (see `frontend/src/App.jsx`).
- `frontend/src/component/userProfileRedirection.jsx` still uses Auth0 hooks; treat this as mixed/legacy auth context.

## Developer Workflows
- Backend local: `cd backend && mvn spring-boot:run` (requires Postgres + Redis).
- Backend docker stack: `cd backend && docker compose up` (API 8085, Postgres mapped to 5433).
- Backend tests: `cd backend && mvn test` (H2 + `test` profile from `backend/src/test/resources/application-test.yml`).
- Frontend local: `cd frontend && npm start`; build: `npm run build`; tests: `npm test`.
- Production-like local run: `cd deploy && cp env.example .env && docker compose -f docker-compose.production.yml up --build`.

## Project-Specific Conventions
- Service implementation names are intentionally inconsistent (`*Implt`, `*Impl`, `UserServiceImplementation`); match existing naming in touched area.
- Controllers commonly use broad `try/catch` and return `ResponseEntity`; domain errors are often mapped via `UserApiError`.
- Prefer DTO-first contracts in `backend/src/main/java/com/roomate/app/dto`; avoid exposing entities unless an endpoint already does.
- Frontend role checks should use `frontend/src/constants/roles.jsx` constants, not raw role strings.
- Preserve eager vs lazy app-data loading behavior in `frontend/src/App.jsx` when adding UI data fetches.

## Integrations and Operational Notes
- Rate limiting is Bucket4j + Redis and intentionally fail-open when Redis is down (`RateLimitingFilter`, `RedisRateLimitConfig`).
- Email invite/verification relies on SMTP env vars (`EMAIL_*`) via `RoomInviteMailSender` and `UserServiceImplementation`.
- WebSocket chat is scaffolded but inactive (`backend/src/main/java/com/roomate/app/websocket/WebSocketConfig.java` is commented; `frontend/src/webpages/Message.jsx` is placeholder).
- `DataSeeder` may auto-insert local test user/room data; account for this when debugging duplicate-looking data.
- JPA is `ddl-auto: update` and Flyway is disabled, so schema drift between environments is possible.

## Environment Variables (high-impact)
- Backend required: `POSTGRESQL_*`, `JWT_KEY` (>=32 chars), `EMAIL_HOST`, `EMAIL_PORT`, `EMAIL_ID`, `EMAIL_PASSWORD`.
- Backend optional: `ACTIVE_PROFILE` (`prod`/`dev`), `CONTAINER_PORT` (default `8085`).
- Frontend build-time: `REACT_APP_BASE_API_URL`.

