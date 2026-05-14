# AGENTS

## Scope
- Monorepo with Spring Boot backend (`backend`), CRA React frontend (`frontend`), and deployment assets (`deploy`).
- Primary API boundary is backend REST under `/user/**` (auth/public-ish) and `/api/**` (authenticated) in `backend/src/main/java/com/roomate/app/config/security/SecurityConfig.java`.

## Architecture Snapshot
- Backend layering is consistent: `controller -> service -> repository -> entities`; start at `backend/src/main/java/com/roomate/app/StartOneApplication.java`.
- Core room domain uses `RoomEntity` + `RoomMemberEntity` (`backend/src/main/java/com/roomate/app/entities/room`) and `RoomServiceImplt` orchestration (`backend/src/main/java/com/roomate/app/service/implementation/RoomServiceImplt.java`).
- Room lifecycle constraints are coded server-side: max 3 rooms per user, max 6 members per room, role-gated membership actions (`RoomServiceImplt`).
- Frontend app state is context-heavy in `frontend/src/App.jsx`: auth, current user, and shared app data are fetched once and cached; feature pages are expected to mutate cache helpers instead of refetching indiscriminately.
- HTTP calls should go through `frontend/src/apiClient.js` (`withCredentials: true`, `REACT_APP_BASE_API_URL`), matching cookie-based JWT behavior.

## Auth + Request Flow
- Login writes both response token and `jwt` HttpOnly cookie (`/user/login` in `backend/src/main/java/com/roomate/app/controller/AuthController.java`).
- `JwtAuthenticationFilter` accepts either `Authorization: Bearer ...` or `jwt` cookie (`backend/src/main/java/com/roomate/app/config/security/JwtAuthenticationFilter.java`).
- Frontend boot auth check is `/user/status`; user profile fetch is `/api/get-user`; profile completion check is `/api/profile-status` (`frontend/src/App.jsx`).
- `useProfileCompletionRedirect` currently uses Auth0 hooks (`frontend/src/component/userProfileRedirection.jsx`), while backend auth is JWT-cookie based; treat this area as mixed/legacy behavior.

## Developer Workflows
- Backend CI runs in `backend` with JDK 21 and `mvn -B package` (`.github/workflows/maven.yml`).
- Local backend stack with Postgres + Redis is defined in `backend/docker-compose.yml` (API on `8085`, DB mapped to host `5433`).
- Production-like compose is `deploy/docker-compose.production.yml` with env contract in `deploy/env.example`; deployment guide is `deploy/README.md`.
- Frontend is Create React App (`frontend/package.json` scripts: `start`, `build`, `test`); API base URL must be set at build time for Docker (`frontend/Dockerfile`).
- Backend tests use profile `test` + H2 (`backend/src/test/resources/application-test.yml`) and are organized as unit/integration/E2E under `backend/src/test/java/com/roomate/app`.

## Project-Specific Conventions
- Service implementation naming is not fully uniform (`*Implt`, `*Impl`, `UserServiceImplementation`); match existing class names instead of normalizing during feature work.
- Controllers often return `ResponseEntity` with broad `try/catch` and map domain errors via `UserApiError` (example: `RoomController`).
- DTO-first API payloads are standard in controllers (`backend/src/main/java/com/roomate/app/dto`); keep entities out of external contracts unless an endpoint already returns entity types.
- Frontend role checks use shared constants in `frontend/src/constants/roles.jsx`; prefer these over string literals.
- AppData context in `frontend/src/App.jsx` distinguishes eager loads (rooms/chores/utilities) from lazy events; preserve this pattern when adding pages.

## Integrations and Operational Notes
- Rate limiting uses Bucket4j + Redis and intentionally fails open when Redis is unavailable (`backend/src/main/java/com/roomate/app/config/security/RateLimitingFilter.java`, `backend/src/main/java/com/roomate/app/config/RedisRateLimitConfig.java`).
- Email flows require SMTP vars (`EMAIL_*`) and are used for room invites + verification (`RoomInviteMailSender`, `UserServiceImplementation`).
- WebSocket chat is scaffolded but currently inactive: backend config is fully commented (`backend/src/main/java/com/roomate/app/websocket/WebSocketConfig.java`), frontend `Message` page is placeholder (`frontend/src/webpages/Message.jsx`).
- Data seeding (`backend/src/main/java/com/roomate/app/config/DataSeeder.java`) inserts a test user/room on startup when not already present; account for this when debugging duplicated local data.
- JPA is currently `ddl-auto: update` in main config (`backend/src/main/resources/application.yml`) with Flyway disabled; schema drift is possible across environments.

