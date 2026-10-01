## TheRoommate

Being with roommates can be hard, especially when you're handling everything. Reminding your roommates to do chores or pay utilities month after month can get tiring.

**TheRoommate** solves these problems and more. Set up meetings, send automatic email reminders, assign chores, and track utilities — all in one app.

This app also comes with landlords in mind: communicate with roommates in real time, track all properties, and in future releases manage financial records and store documents per property.

---

### Current Production Architecture

<img width="1256" height="770" alt="Screenshot 2026-05-24 at 16 11 02" src="https://github.com/user-attachments/assets/d9679cb7-8556-4426-b420-0461e754e27d" />

---

### n8n Receipt Parser

The n8n receipt parser automatically reads images of receipts and updates the personal budgeting page with the line items. It prompts the user to split with roommates or log as a personal expense.

<img width="1396" height="453" alt="Screenshot 2026-06-18 at 08 57 47" src="https://github.com/user-attachments/assets/d4abf3f8-0f03-4f19-a83c-c5a4643cec42" />

---

### Feature Overview

<img width="2676" height="1838" alt="image" src="https://github.com/user-attachments/assets/21fae405-5186-492b-8624-e2644fe818bc" />

---

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21 — Spring Boot 3.4 |
| Frontend | React 19 (CRA), served by nginx |
| Primary DB | PostgreSQL 17 (`daroomate`) |
| Budget DB | PostgreSQL 17 (`daroomate_budget`) — shared with n8n |
| Caching / Rate limiting | Redis 7 + Spring Cache (Bucket4j) |
| Auth | JWT (JJWT 0.12), HttpOnly cookie, stateless |
| Email | Spring Mail + SMTP |
| Hosting | Railway (backend + frontend as separate services) |
| Automation | n8n (receipt-parsing AI workflow) |

---

### Running Locally

See **[DEPLOY.md](DEPLOY.md)** for full instructions including:

- Bare-metal setup (Java + Node without Docker)
- Docker Compose stack (`docker compose up --build` in `backend/`)
- Railway production deployment
- All environment variables
- Health check endpoints

Quick start with Docker:

```bash
cd backend
docker compose up --build   # first run
# API: http://localhost:8085

cd ../frontend
npm install && npm start    # in a separate terminal
# App: http://localhost:3000
```

---

### Repository Layout

```
TheRoommate/
├── backend/          Spring Boot API
│   ├── src/          Java source (controller → service → repository → entities)
│   ├── Dockerfile    Production multi-stage build (eclipse-temurin:21-jre-alpine)
│   ├── Dockerfile.dev Dev image with DevTools + source volume mount
│   ├── docker-compose.yml  Local stack (API + 2× Postgres + Redis)
│   └── railway.json  Railway deployment config
├── frontend/         React SPA
│   ├── src/          JSX source, components, pages
│   ├── Dockerfile    Production build + nginx
│   ├── nginx.conf    SPA routing, gzip, cache headers
│   └── railway.json  Railway deployment config
├── DEPLOY.md         Full deployment guide ← start here
├── SECURITY.md       Security policy and CI scan details
└── AGENTS.md         AI coding guidance (canonical source of truth)
```
