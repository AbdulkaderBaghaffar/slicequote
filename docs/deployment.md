# Production deployment

## Architecture

```text
Internet
   │ HTTPS
   ▼
Cloudflare
   │ Tunnel
   ▼
cloudflared
   │
   ▼
web (nginx :80)
   ├─ /              → React build (dist)
   ├─ /login, /quote → index.html (SPA fallback)
   ├─ /api/*         → app:8080
   └─ /healthz       → 200 ok
          │
          ▼
app (Spring Boot :8080)
   ├─ Postgres
   ├─ PrusaSlicer
   └─ pricing service
```

Only `web` is reachable through the tunnel. `app` and `db` stay on the
internal Compose network.

## Services

| Service | Public? | Notes |
|---|---|---|
| `db` | No | Postgres, `pgdata` volume |
| `app` | Loopback only (`127.0.0.1:8080`) | Spring Boot + PrusaSlicer |
| `web` | Loopback `:3000` + tunnel | nginx + React build |
| `cloudflared` | Outbound | Opt-in: `--profile tunnel` |

The backend and web host ports are bound to loopback for local testing. The
tunnel reaches `web:80` over the Compose network, not through the host port.

## Run locally

```bash
docker compose up -d
```

Open <http://localhost:3000>. No `.env` is needed: Compose supplies local
defaults for the database, JWT secret, and CORS. The first boot takes about a
minute while Postgres initialises and Flyway seeds the data.

Build from source instead of pulling the published images:

```bash
docker compose -f docker-compose.yml -f docker-compose.build.yml up -d --build
```

The API is same-origin:

```text
Browser → http://localhost:3000/api/uploads
nginx   → http://app:8080/uploads
```

Cloudflare is not part of this local check.

## Cloudflare tunnel

In the Cloudflare Zero Trust dashboard, the tunnel's public hostname must route
to:

```text
http://web:80
```

If it currently points at `http://app:8080`, change it. `web` serves React and
proxies `/api` to the backend.

The Compose service uses:

```text
command: tunnel --no-autoupdate run --token ${CLOUDFLARED_TOKEN}
```

Keep the token in `.env` and out of git.

## Environment

Local runs need no `.env`: `docker-compose.yml` has working defaults for every
value. For production, create `.env` next to the compose file — Compose loads it
automatically for `${...}` interpolation.

```dotenv
DB_NAME=slicequote
DB_USER=postgres
DB_PASSWORD=...
JWT_SECRET=...              # 32+ bytes
CORS_ALLOWED_ORIGINS=https://your-domain.example
CLOUDFLARED_TOKEN=...
```

`DB_URL` is derived from `DB_NAME`. Override `DB_URL` only if Postgres runs
outside this Compose project.

Behind nginx the browser is same-origin, so CORS normally does not run. Adding
the public HTTPS origin is still recommended if the API is ever called directly.

## Uploads

Uploads live in the `uploads` Docker volume, not `./data/uploads`. Back them up
with:

```bash
docker compose exec -T app tar czf - -C /app/uploads . > uploads-backup.tgz
```

Migrating existing files from the old bind mount (one time):

```bash
docker compose stop app
docker run --rm \
  -v slicequote_uploads:/to \
  -v "$PWD/data/uploads:/from:ro" \
  alpine sh -c 'cp -a /from/. /to/ && chown -R 1001:1001 /to'
docker compose up -d
```

The volume is named `<compose-project>_uploads`. If your checkout directory is
not `slicequote`, find it with `docker volume ls | grep uploads` and use that
name above. A bind mount is still possible with a production override file; in
that case keep the old `user: "1000:1000"` so the container can write to the
host directory.

## API path mapping

The frontend production image is built with:

```text
VITE_API_URL=/api
```

Nginx strips the prefix:

```text
/api/auth/login          → app:8080/auth/login
/api/materials           → app:8080/materials
/api/uploads             → app:8080/uploads
/api/uploads/42          → app:8080/uploads/42
/api/uploads/42/reprice  → app:8080/uploads/42/reprice
```

Uploads are limited to 102 MB at nginx, matching Spring's 100 MB multipart limit.

## Deployment

Pin image tags in production. CI publishes both services as tags of a single
package:

```text
ghcr.io/abdulkaderbaghaffar/slicequote:latest   # API
ghcr.io/abdulkaderbaghaffar/slicequote:web      # web frontend
```

Typical rollout:

```bash
docker compose pull
docker compose up -d db app web
docker compose --profile tunnel up -d
```

The tunnel service was renamed from the misspelled `slicequote-cloudlfared` to
`cloudflared`. Remove a leftover container once with
`docker rm -f slicequote-slicequote-cloudlfared-1` if it still exists.

Flyway runs on app startup (V1–V12). Back up the database before a deployment
that includes migrations.

Health checks:

```bash
curl http://127.0.0.1:3000/healthz
curl http://127.0.0.1:8080/actuator/health
```

## Backups

Database:

```bash
docker compose exec -T db sh -c \
  'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' > backup.sql
```

Uploads:

```bash
docker compose exec -T app tar czf - -C /app/uploads . > uploads-backup.tgz
```

Store backups off the host and test restores.

## Security checklist

- Do not publish `5432` or `8080` publicly.
- Keep the backend and web ports loopback-only.
- Use a strong `JWT_SECRET` and database password.
- Keep `.env` out of git.
- Keep the 100 MB STL / 102 MB request limit.
- Consider Cloudflare rate limiting for `/api/auth/login`.
- Add security headers at nginx (already set for content type, referrer, frame).
- Review whether JWT-in-localStorage is acceptable for your threat model.
- Keep images pinned to a release tag or digest.
- Run dependency and container scans in CI.

## Troubleshooting

| Symptom | Check |
|---|---|
| `port is already allocated` | Set `WEB_PORT=3001` (and/or `API_PORT=8099`) when starting |
| Upload fails with a permission error | Use the `uploads` volume; a root-owned bind mount is not writable by the app user |
| `502` from nginx | Is `app` healthy? `docker compose logs app` |
| `403` on login through nginx | Host / forwarded headers; set the public origin in `CORS_ALLOWED_ORIGINS` |
| `413` on upload | nginx `client_max_body_size 102m` |
| `/login` 404 on refresh | nginx `try_files ... /index.html` |
| Tunnel shows backend pages | Tunnel target is still `app:8080`; change to `web:80` |
| API calls go to `localhost:8080` in production | The web image was built without `VITE_API_URL=/api` |
| nginx cannot resolve `app` after app recreation | Restart `web`; the container uses Docker DNS with a 10s cache |
