# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Full stack (Docker)
```
docker-compose up --build
```
- Client: http://localhost:4200
- Server: http://localhost:7070
- MariaDB: localhost:3306 (user/pass: `scapelike/scapelike`)

### Backend only (Docker) + frontend local dev
```
docker-compose -f docker-compose.dev.yml up --build -d
cd client && ng serve
```
- Client (hot reload): http://localhost:4200
- Server: http://localhost:7070

### Server (local dev — requires local MariaDB)
```
cd server
mvn package -DskipTests
java -jar target/scapelike-server.jar
```

### Client (local dev — connects to ws://localhost:7070)
```
cd client
npm install
npm start        # dev server on :4200
ng test          # Karma/Jasmine unit tests
```

## Architecture

Three-process system wired together in `Main.java`:

```
Database → TickEngine → GameServer
```

- **`Database`** — HikariCP pool; config via env vars `DB_HOST/PORT/NAME/USER/PASS` (fallback: localhost/scapelike). Schema migrations run inline in `Database.migrate()` — no Flyway/Liquibase.
- **`TickEngine`** — single-thread `ScheduledExecutorService` firing every 600 ms (`TICK_MS`). Game state processing is wired here (currently `TODO`).
- **`GameServer`** — Javalin 6 app. Exposes `GET /health` and `WS /ws/game`. Keeps a `ConcurrentHashMap<sessionId, WsContext>` for broadcasting. Message routing is `TODO`.

Client uses a single `GameService` (Angular injectable) that wraps a raw `WebSocket`. The root `App` component connects on init and accumulates all incoming messages. `WS_URL` is hardcoded in `app.ts` — update it when the dev/prod split is needed.

## Key details

- Java 21, Javalin 6.3, HikariCP 5, MariaDB driver 3.4
- Angular 20 standalone components (no NgModules)
- Prettier config lives in `client/package.json` (`printWidth: 100`, `singleQuote: true`)
- Server fat-jar built by `maven-shade-plugin`, output: `server/target/scapelike-server.jar`
- Phaser.js is planned for game rendering but not yet added

## Conventions

See [`styling_conventions.md`](./styling_conventions.md) — authoritative style reference for both the Java backend and Angular frontend. Use it for implementation and code review.

## Git

- Commit format: `type(scope): subject` — Conventional Commits, subject ≤ 50 chars, imperative mood
- No `Co-Authored-By` or any watermark in commit messages
- Split commits by logical concern; don't bundle unrelated changes
- **Never push without explicit user instruction**
