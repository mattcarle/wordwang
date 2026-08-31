# WordWang

A real-time word-scramble party game. The server picks a random 8-letter word, scrambles it, and players race the clock to find as many valid words as they can from its letters — solo or with friends.

## How to play

1. An 8-letter word is scrambled and shown to everyone as tiles.
2. You have **2 minutes** to find as many words as you can using those letters — including, if you spot it, the full 8-letter word.
3. Click tiles or type on your keyboard. You can only use each letter as many times as it appears in the scrambled word.
4. Longer words score more points:

   | Word length | Points |
   |---|---|
   | 1–2 letters | 0 |
   | 3 letters | 1 |
   | 4 letters | 3 |
   | 5 letters | 7 |
   | 6 letters | 10 |
   | 7 letters | 15 |
   | 8 letters | 20 |

5. Play alone against the clock, or start a game and invite friends via a shareable link or a 5-digit code — everyone sees live scores as they play.
6. Whoever has the highest score when the timer runs out wins. Scores are recorded to a persistent high-score table.

## Tech stack

- **Backend**: Java 21, Spring Boot (WebMVC + WebSocket/STOMP + Data JPA), H2 (file-based)
- **Frontend**: React 19, TypeScript, Vite, React Router, `@stomp/stompjs` over SockJS
- **Dictionary**: [ENABLE1](https://github.com/dolph/dictionary) word list (public domain), bundled at `src/main/resources/dictionary/enable1.txt`

## Running locally

You need two processes running at once: the Spring Boot backend and the Vite dev server.

**Backend** (from the project root):

```bash
./mvnw spring-boot:run
```

Starts on `http://localhost:8081`. Uses a file-based H2 database under `./data/` (created automatically) and exposes the H2 console at `/h2-console`.

**Frontend** (from `frontend/`):

```bash
npm install
npm run dev
```

Starts on `http://localhost:5174/wordwang/` — served from that path prefix rather than the site root even locally, matching how it's deployed in production (see "Deploying with Docker" below), so dev and prod behave the same way. The dev server proxies `<prefix>/api` and `<prefix>/ws` requests to the backend on port 8081, and binds to all network interfaces (`host: true` in `vite.config.ts`), so it's also reachable from other devices on your LAN at `http://<your-lan-ip>:5174/wordwang/`.

Open `http://localhost:5174/wordwang/` in a browser and click **New Game** to start playing.

## Testing

**Backend** — unit tests plus a STOMP integration test exercising the real REST + WebSocket flow end to end:

```bash
./mvnw test
```

**Frontend** — typecheck and production build:

```bash
cd frontend
npm run build
```

## Deploying with Docker

This app shares its host and domain (`carle7.com`) with other apps behind a single reverse proxy
— see the separate [`carle7-edge`](https://github.com/mattcarle/carle7-edge) repo, which is the
only thing on the host that binds ports 80/443 or terminates TLS. The included
`docker-compose.yml` here runs just this app's own two containers, on a private network plus the
`carle7-edge` network that proxy reaches them on:

- **`app`** — the Spring Boot backend, built by the root `Dockerfile`, running on plain HTTP
  internally (port 8080, not published to the host). Its H2 database file lives in the `h2-data`
  named volume, so it survives container rebuilds/restarts. The H2 console is disabled in this
  config.
- **`caddy`** — built by `frontend/Dockerfile`, serves the built static files and reverse-proxies
  `/wordwang/api/*` and `/wordwang/ws` to `app`. It speaks plain HTTP on the Docker network only
  (no TLS, no published ports) — `carle7-edge` forwards it everything under `/wordwang/*`
  unmodified, still carrying that prefix.

### First-time deployment

1. Bring up [`carle7-edge`](https://github.com/mattcarle/carle7-edge) first if it isn't already
   running — it creates the `carle7-edge` Docker network this app's `caddy` service attaches to.
   `docker compose up` here fails until that network exists.
2. Build and start both containers:

   ```bash
   docker compose up -d --build
   ```
3. Open `https://<SITE_ADDRESS>/wordwang/` (`SITE_ADDRESS` is configured in `carle7-edge`, not
   here).

Check container status/logs with:

```bash
docker compose ps
docker compose logs -f app       # backend logs
docker compose logs -f caddy     # reverse proxy logs
```

### Updating after code or config changes

```bash
# Backend code (src/), pom.xml, or Dockerfile changes
docker compose build app && docker compose up -d app

# Frontend code (frontend/src/), frontend/Dockerfile, or frontend/Caddyfile changes
docker compose build caddy && docker compose up -d caddy
```

The `h2-data` volume is untouched by rebuilds or `docker compose down`, so high scores persist
across updates. Only `docker compose down -v` (or manually removing the volume) deletes it.

## Project structure

```
src/main/java/com/wordwang/
  dictionary/    — word list loading, validity checks, scrambling
  game/model/    — in-memory Game/Player domain objects
  game/service/  — game lifecycle, scoring, guess validation, scheduled finalization
  game/web/      — REST controller + STOMP message handler
  game/dto/      — request/response and STOMP event records
  highscore/     — persisted high-score entity/repository/service/controller
  config/        — WebSocket (STOMP/SockJS) and scheduling config

frontend/src/
  pages/         — HomePage, HowToPlayPage, GamePage, HighScoresPage
  components/    — lobby, in-game (tiles/timer/scoreboard), results views
  hooks/         — useGameSocket (STOMP), useGameState (snapshot + live events)
  api/           — REST client
  types/         — TypeScript mirrors of backend DTOs/events

docs/
  original-spec.md      — the original game specification
  implementation-plan.md — the implementation plan the build followed
```

## Game mechanics notes

- Game state (lobby, scores, timer) lives in memory on the server; only high scores are persisted to the database.
- The 2-minute round timer is server-authoritative — the server schedules game finalization independently of any client, so the game resolves correctly even in solo play or if players disconnect. The organiser can also end the round early with the "Quit Game" button.
- The server re-validates every guess (length, available letters, dictionary membership, duplicates) — the client-side tile input is a UX convenience, not the source of truth.
