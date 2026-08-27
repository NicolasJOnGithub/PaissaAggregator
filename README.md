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
  small/medium/large breakdown. Click a card to jump to the leaderboard filtered to that
  datacenter.
- **Leaderboard** (`/leaderboard`) — worlds ranked by open plots. Use the ownership tabs
  (FC-only / Individual-only / Unrestricted) and the size dropdown to change what's ranked;
  unrestricted plots always count on every tab, since anyone can buy them. Click a world row to
  see its actual plots.
- **World plots** (`/worlds/:id`) — every open plot for that world, grouped by district, with
  in-game ward/plot numbers, price, and ownership. Same ownership/size filters as the leaderboard.

### Triggering a manual refresh

Instead of waiting for the 30-minute schedule, trigger a sync on demand from the "Refresh now"
control in the header — it'll prompt for the `REFRESH_KEY` you set in `.env`. Or call the API
directly:

```bash
curl -X POST http://localhost:8080/api/refresh -H "X-Refresh-Key: <your REFRESH_KEY>"
```

Returns `202` if a sync just started, `409` if one's already running, `401` if the key is wrong.

## Local development (without Docker)

Backend (needs a local Postgres — see `docker-compose.yml` for the expected db/user/password, or
just run `docker compose up postgres` and point at it):

```bash
cd backend
DB_URL=jdbc:postgresql://localhost:5432/paissa REFRESH_KEY=dev mvn spring-boot:run
```

Frontend (proxies `/api` to `localhost:8080` in dev — see `vite.config.ts`):

```bash
cd frontend
npm install
npm run dev
```
