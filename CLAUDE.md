# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DaRoomate is a roommate management application with a Spring Boot backend, Create React App frontend, and Docker-based deployment. The application handles room creation, member management, chores, utilities, and invitations with JWT-cookie authentication.

## Development Commands

### Backend (Spring Boot with Maven)
```bash
cd backend

# Run locally (requires Postgres + Redis)
mvn spring-boot:run

# Run with local Docker stack (Postgres on 5433, Redis, backend on 8085)
docker compose up

# Build
mvn clean package

# Run tests (uses H2 + test profile)
mvn test

# Run specific test
mvn test -Dtest=UserServiceUnitTest
mvn test -Dtest=RoomE2ETest
```

### Frontend (Create React App)
```bash
cd frontend

# Development server (proxies to localhost:8085)
npm start

# Production build (requires REACT_APP_BASE_API_URL at build time)
npm run build

# Tests
npm test
```

### Full Stack Local Testing
```bash
# From deploy directory
cd deploy
cp env.example .env
# Edit .env with values
docker compose -f docker-compose.production.yml up --build
```

## Architecture

### Backend Structure
- **Layering**: `controller -> service -> repository -> entities`
- **Entry point**: `backend/src/main/java/com/roomate/app/StartOneApplication.java`
- **Security config**: `backend/src/main/java/com/roomate/app/config/security/SecurityConfig.java`
  - `/user/**` endpoints: auth/public routes
  - `/api/**` endpoints: authenticated routes

### Core Domain: Rooms
- **Entities**: `RoomEntity` + `RoomMemberEntity` in `backend/src/main/java/com/roomate/app/entities/room/`
- **Service orchestration**: `RoomServiceImplt` at `backend/src/main/java/com/roomate/app/service/implementation/RoomServiceImplt.java`
- **Business rules** (enforced server-side):
  - Max 3 rooms per user
  - Max 6 members per room
  - Role-gated membership actions

### Authentication Flow
- Login endpoint `/user/login` (in `AuthController`) writes:
  - Response token body
  - HttpOnly `jwt` cookie
- `JwtAuthenticationFilter` accepts either:
  - `Authorization: Bearer <token>` header
  - `jwt` cookie
- Frontend auth check: `/user/status` → `/api/get-user` → `/api/profile-status`
- Location: `backend/src/main/java/com/roomate/app/config/security/JwtAuthenticationFilter.java`

### Frontend Architecture
- **State management**: Context-heavy in `frontend/src/App.jsx`
  - Auth, current user, and shared app data fetched once and cached
  - Feature pages mutate cache helpers instead of refetching
- **HTTP client**: Use `frontend/src/apiClient.js` (`withCredentials: true`, respects `REACT_APP_BASE_API_URL`)
- **Role constants**: `frontend/src/constants/roles.jsx` (prefer these over string literals)
- **AppData context**: Distinguishes eager loads (rooms/chores/utilities) from lazy events

## Key Project Conventions

### Backend Patterns
- **Service naming**: Not uniform (`*Implt`, `*Impl`, `UserServiceImplementation`) — match existing class names, don't normalize during feature work
- **Error handling**: Controllers return `ResponseEntity` with broad try/catch, map domain errors via `UserApiError`
- **API contracts**: DTO-first in `backend/src/main/java/com/roomate/app/dto/` — keep entities out of external contracts unless endpoint already returns entity types

### Database & Schema
- **JPA mode**: `ddl-auto: update` in `backend/src/main/resources/application.yml`
- **Flyway**: Currently disabled — schema drift possible across environments
- **Data seeding**: `DataSeeder` inserts test user/room on startup if not present (`backend/src/main/java/com/roomate/app/config/DataSeeder.java`)

### Testing Strategy
- **Test profiles**: Uses `test` profile + H2 in-memory DB (`backend/src/test/resources/application-test.yml`)
- **Test organization**: `backend/src/test/java/com/roomate/app/` contains unit/integration/E2E tests
- **Examples**:
  - Unit: `UserServiceUnitTest.java`
  - Integration: `UserServiceIntegrationTest.java`
  - E2E: `UserE2ETest.java`, `RoomE2ETest.java`

## Infrastructure & Integrations

### Rate Limiting
- **Stack**: Bucket4j + Redis
- **Fail-open behavior**: Intentionally passes requests when Redis unavailable
- **Config**: `RateLimitingFilter` and `RedisRateLimitConfig` in `backend/src/main/java/com/roomate/app/config/`

### Email
- **Requirements**: SMTP vars (`EMAIL_HOST`, `EMAIL_PORT`, `EMAIL_ID`, `EMAIL_PASSWORD`)
- **Usage**: Room invites + email verification
- **Senders**: `RoomInviteMailSender`, `UserServiceImplementation`

### WebSocket (Inactive)
- Chat feature is scaffolded but not active
- Backend config fully commented in `backend/src/main/java/com/roomate/app/websocket/WebSocketConfig.java`
- Frontend `Message` page is placeholder at `frontend/src/webpages/Message.jsx`

### Mixed Auth Context
- `useProfileCompletionRedirect` uses Auth0 hooks (`frontend/src/component/userProfileRedirection.jsx`)
- Backend auth is JWT-cookie based
- Treat this area as mixed/legacy behavior

## Environment Variables

### Backend Required
- `POSTGRESQL_HOST`, `POSTGRESQL_PORT`, `POSTGRESQL_DATABASE`, `POSTGRESQL_USERNAME`, `POSTGRESQL_PASSWORD`
- `JWT_KEY` (min 32 characters)
- `EMAIL_HOST`, `EMAIL_PORT`, `EMAIL_ID`, `EMAIL_PASSWORD`

### Backend Optional
- `ACTIVE_PROFILE` (default: none, use `prod` or `dev`)
- `CONTAINER_PORT` (default: 8085)

### Frontend Required (Build-time)
- `REACT_APP_BASE_API_URL` — must be set during `npm run build` for Docker deployments

## Deployment

### CI/CD
- **Backend CI**: `.github/workflows/maven.yml` runs with JDK 21 and `mvn -B package`
- **Target platforms**: Railway (current), AWS (planned via ECS/Beanstalk)

### Deployment Guide
- See `deploy/README.md` for Railway setup and AWS migration paths
- Use `deploy/env.example` as environment variable template
- Production compose file: `deploy/docker-compose.production.yml`

## Technologies

### Backend
- Spring Boot 3.4.4 with JDK 21
- PostgreSQL (production) / H2 (tests)
- Redis (rate limiting)
- JWT (jjwt 0.12.6)
- Lombok
- Bucket4j for rate limiting
- Spring Security + Mail + WebSocket

### Frontend
- React 19.1 (Create React App)
- React Router 7.6
- Axios for HTTP
- Firebase 11.8 (likely for future features)
- STOMP.js (WebSocket, inactive)
- Lucide React (icons)
