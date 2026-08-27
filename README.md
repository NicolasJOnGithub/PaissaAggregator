# PaissaAggregator

Aggregates FF XIV housing plot availability from [PAISSADB](https://paissadb.zhu.codes) into a
Postgres-backed leaderboard: which datacenters and worlds currently have the most open plots,
filterable by size (small/medium/large) and ownership type (FC-only/individual-only/unrestricted).

## Stack

- **Backend**: Java 21, Spring Boot 3, Postgres, Flyway
- **Frontend**: React 19, TypeScript, Vite, Tailwind CSS
- **DevOps**: Docker Compose (postgres + backend + frontend)

## Running it

Requires Docker (Docker Desktop, Colima, etc.) with Compose v2.

```bash
cp .env.example .env
```

Edit `.env` and set `REFRESH_KEY` to a key of your choosing (this gates the manual refresh
endpoint — see below). The other defaults are fine for local use.

```bash
docker compose up --build
```

This starts three containers:

| Service    | URL                     | Notes                                   |
| ---------- | ----------------------- | ---------------------------------------- |
| frontend   | http://localhost:3000   | nginx serving the built app, proxies `/api/*` to backend |
| backend    | http://localhost:8080   | REST API                                 |
| postgres   | (internal only)         | data persisted in the `postgres_data` volume |

On first boot the backend automatically kicks off a full sync from PAISSADB (no need to trigger
anything manually). **A full sync takes roughly 15 minutes** — PAISSADB rate-limits to about one
request per 10 seconds, and the backend syncs all 85 worlds one at a time to respect that. The app
is usable while this runs; pages will just show partial data until it completes. After the first
sync, it re-syncs automatically every 30 minutes.

Check sync progress any time:

```bash
curl http://localhost:8080/api/refresh/status
```

`worldsSynced` climbs toward 85 as the sync progresses; `inProgress` flips to `false` when it's done.

## Using it

Open http://localhost:3000.

- **Datacenters** (`/`) — one card per datacenter with its total open-plot count and a
  small/medium/large breakdown. Filter to one region (Japan / North America / Europe / Oceania)
  with the region dropdown. Click a card to jump to the leaderboard filtered to that datacenter.
- **Leaderboard** (`/leaderboard`) — worlds ranked by open plots. Use the ownership tabs
  (FC-only / Individual-only / Unrestricted, single-select) plus the size and district dropdowns
  (both multi-select — e.g. check both Mist and The Lavender Beds, or both Small and Medium, to
  combine them) and the region dropdown to change what's ranked. Picking multiple sizes ranks by
  their combined total; unrestricted plots always count on every ownership tab, since anyone can
  buy them. Click a world row to see its actual plots.
- **World plots** (`/worlds/:id`) — every open plot for that world, grouped by district, with
  in-game ward/plot numbers, price, and ownership. Same ownership/size/district filters as the
  leaderboard (size and district are multi-select here too).

### Triggering a manual refresh

Instead of waiting for the 30-minute schedule, trigger a sync on demand from the "Refresh now"
control in the header — it'll prompt for the `REFRESH_KEY` you set in `.env`. Or call the API
directly:

```bash
curl -X POST http://localhost:8080/api/refresh -H "X-Refresh-Key: <your REFRESH_KEY>"
```

Returns `202` if a sync just started, `409` if one's already running, `401` if the key is wrong.

## Local development (backend/frontend running directly, not in Docker)

This runs the backend as a plain `mvn spring-boot:run` process and the frontend as a Vite dev
server, both against a Postgres started via Compose. Useful for faster edit/reload loops than
rebuilding Docker images each time.

Requires JDK 21 and Maven locally (`java --version`, `mvn --version`). If your default JDK is
older, point `JAVA_HOME` at a 21 install for the `mvn` command below.

**1. Start just Postgres**, exposed to the host so a locally-run backend can reach it:

```bash
cp .env.example .env   # if you haven't already
docker compose up -d postgres
```

This publishes Postgres on `localhost:${POSTGRES_PORT:-5432}` (only `postgres` has this host port
mapping — `backend`/`frontend` are still meant to run via `docker compose up`, not this flow).

**2. Run the backend:**

```bash
cd backend
DB_URL=jdbc:postgresql://localhost:5432/paissa DB_USER=paissa DB_PASSWORD=paissa REFRESH_KEY=dev \
  mvn spring-boot:run
```

Adjust the port/user/password if you changed them in `.env`. On startup it runs Flyway migrations
against the same database docker-compose uses, then starts syncing from PAISSADB — same ~15 minute
first sync as above. It listens on `localhost:8080`.

**3. Run the frontend:**

```bash
cd frontend
npm install
npm run dev
```

Vite's dev server (`localhost:5173`) proxies `/api/*` to `localhost:8080` — see the `proxy` block in
`vite.config.ts` if you need to point it elsewhere.
