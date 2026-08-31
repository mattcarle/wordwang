# Implementation Plan (as approved)

This is Claude's implementation plan for the initial WordWang build, saved here for future reference. It was written during plan mode (internally named `wondrous-floating-flame`) and approved before implementation began. See `docs/original-spec.md` for the prompt it was written against.

Note: the plan below still says "30-second timed round" throughout — that was later changed, first to 3 minutes and then to 2 minutes (see git history / `GameService.ROUND_DURATION`), so treat timing details here as historical rather than current.

Note: the plan also assumes the backend/frontend dev ports are 8080/5173 (Spring Boot's and Vite's defaults). Those were later changed to 8081/5174 — see `application.properties` and `frontend/vite.config.ts`.

Note: an organiser-only "Quit Game" button (ends the round early for everyone) was added after this plan; it isn't described below.

---

# WordWang Game — Implementation Plan

## Context

WordWang currently has only a bare Spring Boot skeleton (STOMP/SockJS config, H2+JPA wired but unused, empty entry point) and a placeholder Vite/React/TS frontend. Nothing else exists. This plan implements the full game described by the user: a real-time, multiplayer (or solo) word-scramble game with a lobby/invite flow, a 30-second timed round, server-validated scoring, live scoreboards, and a persistent high-score table.

Because the codebase is greenfield, this plan defines the whole vertical slice — dictionary sourcing, backend domain/services/API, real-time protocol, and the frontend pages/components — rather than adapting an existing pattern.

## Assumptions / decisions (confirm or override at review)

These resolve ambiguities the spec didn't cover. Defaults chosen for simplicity; flag any you want changed:

1. **5-digit code *is* the game id** — used directly in REST paths, the STOMP topic, and the frontend route. No separate internal id.
2. **Server drives game-end**, not the client's timer. On `start`, the server schedules a one-shot task at `endsAt` that finalizes the game (computes winner, writes high scores, broadcasts reveal) regardless of client activity — required so solo games and games where players vanish still resolve.
3. **Tie-break**: players sharing the top score are all listed as co-winners; no arbitrary tiebreak.
4. **High-score table records every player's score** at game end (not winner-only) — gives solo play a leaderboard and captures near-misses in multiplayer.
5. **Word list**: bundle **ENABLE1** (public-domain Scrabble-style word list, ~172k words) as a classpath resource, used as-is for v1 (no frequency-based curation of obscure 8-letter words — flagged as a possible v1.1 polish, not a blocker).
6. **Duplicate guess** (already found by this player this game): 0 points, message `"You've already found <WORD>!"`.
7. **Server-side-only invalid case** (letters not available — defense against a bypassed client, shouldn't normally be reachable since the input control prevents it): message `"<WORD> can't be made from these letters!"`.
8. **Duplicate player name within one lobby**: rejected (400) with a clear error; the joiner must pick a different name.
9. **Feedback broadcast**: all guess feedback broadcasts to everyone in the game (tagged with the target `playerId`, filtered client-side) rather than wiring a real per-user STOMP destination — simplest option for small casual games; means a technically-inclined player could inspect raw WS frames to see others' guess attempts. Low severity, acceptable for v1.
10. **Organiser disconnects before starting**: no reassignment logic in v1; the lobby just times out via the abandoned-game sweep.
11. **No production packaging work in this pass** — frontend keeps running via `npm run dev` with a Vite proxy to the backend; bundling the built frontend into the Spring Boot jar is a separate future step.

## Word list

Source **ENABLE1** (`enable1.txt`, public domain, standard for word-game software) into `src/main/resources/dictionary/enable1.txt`, plus a short `NOTICE.md` noting provenance. Loaded once at startup into an uppercased `Set<String>` (validity checks) and a filtered `List<String>` of exactly-8-letter words (random selection pool).

## Backend

New Java under `src/main/java/com/wordwang/`, alongside the existing `config/WebSocketConfig.java` (unchanged) and `WordwangApplication.java`.

**`dictionary/`**
- `DictionaryService` — `@PostConstruct` loads the word list; `isValidWord(String)`, `randomEightLetterWord()`.
- `ScrambleUtil` — static `scramble(String)`, Fisher–Yates shuffle, re-rolls if result equals input.

**`game/model/`** (plain mutable POJOs, not JPA — game state is short-lived and in-memory)
- `GameStatus` enum: `LOBBY`, `IN_PROGRESS`, `FINISHED`.
- `Player`: id (UUID), name, score, foundWords, joinedAt.
- `Game`: id (5-digit code), organiserId, status, solutionWord, scrambledWord, startedAt, endsAt, players (LinkedHashMap, join order), createdAt.

**`game/service/`**
- `GameCodeGenerator` — random 5-digit code, retries against a "taken" check.
- `ScoringService` — pure function implementing the point table (§ Scoring below).
- `GuessValidator` — stateless, runs the ordered checks (length → letter-multiset subset → dictionary → duplicate) and returns an outcome + message.
- `GameService` — `@Service`, `ConcurrentHashMap<String, Game>`; `createGame`, `joinGame`, `startGame` (organiser-only, schedules finalization), `submitGuess`, `finalizeGame` (winner computation + triggers high-score writes), `getSummary`/`getSnapshot`. Each mutating method synchronizes on the fetched `Game` instance. `@Scheduled` sweep evicts stale `LOBBY` (>30min) and `FINISHED` (>10min) games. Stays broadcast-agnostic — returns data; controllers own `SimpMessagingTemplate` broadcasting.
- `GameFinalizerScheduler` — wraps a Spring `TaskScheduler`; schedules the one-shot end-of-game task per started game.

**`game/web/`**
- `GameController` (`@RestController`, `/api/games`) — REST endpoints below; broadcasts `PLAYER_JOINED`/`GAME_STARTED` after successful mutations.
- `GameSocketController` (`@Controller`, `@MessageMapping("/game/{gameId}/guess")`) — calls `GameService.submitGuess`, broadcasts `GUESS_FEEDBACK` and (on scoring guesses) `SCORE_UPDATE`.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) — maps not-found/forbidden/invalid-state to 404/403/409 with `{ "error": "..." }`.

**`game/dto/`** — Java records: `CreateGameRequest`, `JoinGameRequest`, `StartGameRequest`, `PlayerView`, `GameSummaryResponse`, `GameSnapshotResponse`, and STOMP event records `PlayerJoinedEvent`, `GameStartedEvent`, `ScoreUpdateEvent`, `GuessFeedbackEvent`, `GameEndedEvent`.

**`highscore/`**
- `HighScoreEntry` `@Entity` (id, playerName, score, playedAt, gameId).
- `HighScoreRepository extends JpaRepository<...>` — top-N by score desc.
- `HighScoreService.recordScore(...)`, `getTopScores(limit)`.
- `HighScoreController` — `GET /api/highscores?limit=20`.

**`config/`** — add `SchedulingConfig` (`@EnableScheduling` + `TaskScheduler` bean), needed for the cleanup sweep and per-game finalization.

### REST contract (base `/api`)

- `POST /games` `{organiserName}` → `201 {gameId, organiserId, organiserName, status}`
- `GET /games/{gameId}` → summary (no identity needed — used by invite-link visitors before joining)
- `POST /games/{gameId}/join` `{playerName}` → `{gameId, playerId, status, players[]}` (404 unknown game, 409 not in LOBBY, 400 blank/duplicate name)
- `POST /games/{gameId}/start` `{playerId}` → `{gameId, status, scrambledWord, endsAt}` (403 not organiser, 409 not in LOBBY)
- `GET /games/{gameId}/snapshot?playerId={uuid}` → full state for refresh/late load (`solutionWord` null until FINISHED, includes `yourFoundWords`)
- `GET /highscores?limit=20` → `[{playerName, score, playedAt}]`

Guesses are **STOMP-only**, no REST endpoint for them.

### STOMP contract

- Subscribe: `/topic/game/{gameId}` (anyone, joined or not)
- Publish: `/app/game/{gameId}/guess` `{playerId, word}`
- Broadcast envelope discriminated by `type`: `PLAYER_JOINED`, `GAME_STARTED {scrambledWord, endsAt}`, `GUESS_FEEDBACK {playerId, word, outcome, points, message}`, `SCORE_UPDATE {players[]}`, `GAME_ENDED {solutionWord, winners[], players[]}`.
- `outcome` values: `VALID`, `TOO_SHORT`, `NOT_A_WORD`, `INVALID_LETTERS`, `ALREADY_FOUND`.

### Scoring & validation (exact)

```
length < 3        → 0 points   ("<WORD> is too short!")
3 letters         → 1 point
4 letters         → 3 points
5 letters         → 7 points
6 letters         → 10 points
7 letters         → 15 points
8 letters         → 20 points
```

`GuessValidator` order (normalize: trim + uppercase): length ≥ 3 → letters are a sub-multiset of the scrambled word → in dictionary → not already found by this player → success (`"+<N> points!"`). Server re-checks the letter-multiset constraint even though the client input control enforces it — never trust client input.

## Frontend

New deps: `react-router-dom`, `@stomp/stompjs`, `sockjs-client` (+ `@types/sockjs-client`). `vite.config.ts` gets a dev proxy (`/api`, `/ws` → `http://localhost:8080`, `ws: true` for `/ws`) so no backend CORS config is needed.

```
frontend/src/
  main.tsx                 — wrap App in BrowserRouter
  App.tsx                  — routes: /, /how-to-play, /game/:gameId, /highscores
  types/game.ts             — TS mirrors of backend DTOs/events
  api/{client,games,highscores}.ts
  utils/cookies.ts          — wordwang_name cookie (1yr), get/set
  hooks/useGameSocket.ts    — STOMP connect/subscribe/submitGuess for a gameId
  hooks/useGameState.ts     — reducer merging REST snapshot + live STOMP events
  pages/HomePage.tsx        — New Game / Join Game / How to Play, cookie-prefilled name
  pages/HowToPlayPage.tsx   — static instructions
  pages/GamePage.tsx        — loads snapshot by :gameId, renders Lobby|Play|Results by status
  pages/HighScoresPage.tsx
  components/shared/NameEntryForm.tsx
  components/lobby/LobbyView.tsx      — player list (live), Invite (WhatsApp wa.me link + copy), Start Game (organiser-gated), "Waiting..." for others
  components/game/PlayView.tsx        — scrambled tiles, timer, input, scoreboard, feedback toast
  components/game/TileInput.tsx       — click-to-add + physical keyboard entry, multiset-aware tile disable
  components/game/CountdownTimer.tsx  — remaining = endsAt - Date.now(), recomputed each tick
  components/game/Scoreboard.tsx      — live, sorted desc by score
  components/results/ResultsView.tsx  — solution reveal, winner(s), final scores, link to /highscores
  styles/tiles.css                    — keyboard-key/Scrabble-tile CSS (box-shadow, radius, slab/monospace font)
```

Flow mapping: Home→New Game prompts name (cookie-prefilled) → `POST /games` → `/game/:id` renders `LobbyView` (this browser is organiser). Home→Join Game or an invite link → `GET /games/{id}` for an immediate read, `NameEntryForm` → `POST /games/{id}/join`. Only the organiser's browser (`playerId === organiserId`) renders "Start Game". `GAME_STARTED` flips everyone to `PlayView`; `TileInput` enforces the multiset client-side and publishes guesses; `GUESS_FEEDBACK` filtered to `playerId === me` drives the toast; `SCORE_UPDATE` refreshes the board. `GAME_ENDED` (server-scheduled) flips to `ResultsView`.

## Tests

- Backend unit: `ScoringServiceTest` (all length boundaries), `GuessValidatorTest` (every outcome incl. multiset edge cases), `DictionaryServiceTest` (resource loads, 8-letter pool sanity), `ScrambleUtilTest` (output is a permutation), `GameCodeGeneratorTest`, `GameServiceTest` (full lifecycle, organiser-only start, state-guarded join/start, no double-scoring on duplicate guesses, concurrent-join race check).
- One `@SpringBootTest(webEnvironment = RANDOM_PORT)` STOMP integration test using `WebSocketStompClient`: create/join/start via REST, submit a guess via STOMP, assert `GUESS_FEEDBACK`/`SCORE_UPDATE` arrive correctly — cheap given the test starters are already in `pom.xml`, and the only place REST+STOMP+in-memory-state interact end-to-end.

## Implementation sequence

1. Backend domain + services (dictionary, model, `GameService`, `ScoringService`, `GuessValidator`) with unit tests — no web layer yet.
2. Backend web layer (`GameController`, `GameSocketController`, DTOs, exception handling) + the STOMP integration test.
3. High-score entity/repository/service/controller.
4. Frontend scaffolding: router, types, API client, cookie util, STOMP hook.
5. Frontend pages/components in flow order: Home → How to Play → Lobby → Play (tiles/timer/input/scoreboard) → Results → High Scores.
6. Tile/keyboard visual styling pass.
7. End-to-end manual verification (below).

## Verification

- `mvn test` (or `./mvnw test`) for all backend unit + the STOMP integration test.
- `cd frontend && npm run build` for typecheck/build sanity.
- Manual: run backend (`./mvnw spring-boot:run`) + `npm run dev`; open two browser windows — create a game in one, join via the 5-digit code in the other, start, submit valid/short/invalid/duplicate/repeat guesses in both, confirm scores, feedback text, and sorted scoreboard update live in both windows, let the timer expire and confirm both windows reveal the word/winner simultaneously, then check `/highscores` shows both players' rows.
