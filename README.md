<p align="center">
  <img src="docs/slicequote-lockup.svg" width="400" alt="SliceQuote">
</p>

<p align="center">
  Pick a material, upload an STL, get a priced estimate back.
</p>


## Intro
SliceQuote is a self-hostable quoting service for 3D printing. It slices every
upload with PrusaSlicer, turns the result into material and machine costs, and
returns an itemized estimate.


## Visuals
| Landing page | Quotation page |
| :---: | :---: |
| <img src="docs/screenshots/landing.png" width="400" alt="SliceQuote landing page"> | <img src="docs/screenshots/quote.png" width="400" alt="SliceQuote quotation page"> |


## Architecture

```mermaid
flowchart TB
 classDef client fill:#1e40af,stroke:#60a5fa,stroke-width:1.5px,color:#eff6ff
 classDef infra fill:#334155,stroke:#94a3b8,stroke-width:1.5px,color:#f8fafc
 classDef api fill:#0f766e,stroke:#2dd4bf,stroke-width:2px,color:#f0fdfa
 classDef worker fill:#6d28d9,stroke:#a78bfa,stroke-width:1.5px,color:#f5f3ff
 classDef slicer fill:#b45309,stroke:#fbbf24,stroke-width:1.5px,color:#fffbeb
 classDef pricing fill:#15803d,stroke:#4ade80,stroke-width:1.5px,color:#f0fdf4
 classDef store fill:#0f172a,stroke:#475569,stroke-width:1.5px,color:#e2e8f0

 B["React SPA<br/>Vite + TypeScript"]:::client -->|HTTPS| N["nginx<br/>static + /api"]:::infra
 N --> API["Spring Boot API"]:::api

 API -->|save STL| FS[("uploads volume")]:::store
 API -->|insert PENDING| DB[("Postgres 17")]:::store
 API -.->|async| W["Slice worker<br/>4-8 threads"]:::worker

 W -->|minimal.ini| S["PrusaSlicer CLI<br/>max 2 concurrent"]:::slicer
 S -->|print time + filament cm3| W
 W --> P["Pricing Logic"]:::pricing
 W -->|DONE + price lines| DB

 B -.->|poll GET /uploads/:id| API

 linkStyle default stroke:#94a3b8,stroke-width:1.3px
```


## Download

```bash
git clone https://github.com/AbdulkaderBaghaffar/slicequote.git
cd slicequote
docker compose up -d
```

Open <http://localhost:3000>. 




## Tech Stack

| **Component** | **Technology** | **Description** |
|---|---|---|
| **Frontend** | <img src="https://skillicons.dev/icons?i=react,ts,vite,threejs" valign="middle" /> | React 19, TypeScript, Vite, and three.js power the quote flow, the UI, and the live 3D STL preview. |
| **API** | <img src="https://skillicons.dev/icons?i=java,spring" valign="middle" /> | Java 21 and Spring Boot 4.1 expose auth, materials, uploads, repricing, and quote history over REST. |
| **Database** | <img src="https://skillicons.dev/icons?i=postgres" valign="middle" /> | PostgreSQL 17 stores materials, rates, uploads, and quote lines. |
| **Delivery** | <img src="https://skillicons.dev/icons?i=docker,nginx,githubactions" valign="middle" /> | Docker Compose runs the stack, nginx proxies `/api`, GitHub Actions publishes images to GHCR. |
