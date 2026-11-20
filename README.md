# SliceQuote

> Backend service for a 3D-print cost estimation site. Pick a material, upload an STL, get a priced estimate back.

## API

See [docs/api.md](docs/api.md) for the HTTP contract (endpoints, error shape, upload lifecycle).

## Web client

The React frontend lives in [`web/`](web/) (Vite + React + TypeScript) and
implements the Acid/Forest quotation design. It talks to this API for signup /
login, materials, STL upload, slicing status, and price breakdowns.

```bash
cd web
npm install
npm run dev      # http://localhost:5173 (CORS default)
npm run build    # type-check + production bundle
npm run preview  # http://localhost:4173
```

The API URL defaults to `http://localhost:8080`; override it with
`VITE_API_URL` in `web/.env.local`. See [`web/README.md`](web/README.md).

---


## Stack

- **Java 21 + Spring Boot** 
- **React 19 + Vite + TypeScript** (`web/`)
- **Postgres 16**
- **JWT** 
- **PrusaSlicer CLI**
- **Docker** 
- **GitHub Actions**
- Deployed on **Home Server** 

## Architecture

A layer only talks to the layer below it.

| Layer | Package | Job |
|---|---|---|
| Controller | `controller/` | HTTP only: JSON in, JSON + status out |
| Service | `service/` | Business rules (pricing, ownership, lifecycle) |
| Worker | `worker/` | Async slice job, status transitions |
| Slicer | `slicer/` | The **only** class touching `ProcessBuilder` |
| Repository | `repository/` | DB access (Spring generates the impls) |
| Entity | `data/` | Stored rows (JPA lives in `data`, not `entity`) |
| DTO | `dto/` | Request/response shapes at the API boundary |
| Config | `config/` | Beans, security rules, `@ControllerAdvice` |
