# SliceQuote — web client

React implementation of the LayerForge quotation design produced in the
OpenDesign project `b45df71d-cdad-4614-ab2a-4e098d9cda8b`, now connected to the
SliceQuote Spring Boot API.

- **Acid** — light mode, the default.
- **Forest** — dark mode.
- `html[data-theme="light" | "dark"]`, persisted in `localStorage["lf-theme"]`.
- Newsreader / Space Grotesk / Spline Sans Mono type stack.
- Landing → Login → Quotation routes (`/`, `/login`, `/quote`).
- JWT auth, live materials, STL upload, server-side slicing, status polling,
  and the backend's price breakdown.

The brand name is **SliceQuote**; the original design used the placeholder name
LayerForge.

## Run the backend first

```bash
cd ..                 # repository root
docker compose up -d db app
curl http://localhost:8080/actuator/health
```

The compose stack serves the API at <http://localhost:8080> and seeds the `PLA`
material. `prusa-slicer` runs inside the app image.

## Run the frontend

```bash
cd web
npm install
npm run dev
```

Vite serves <http://localhost:5173/> — exactly the origin allowed by the
backend's default `CORS_ALLOWED_ORIGINS`. Open it through `localhost`, not
`127.0.0.1`, unless you add that origin to the backend config.

The API base URL defaults to `http://localhost:8080`. Override it with
`web/.env.local`:

```bash
VITE_API_URL=https://your-api.example.com
```

## Build and preview

```bash
npm run build
npm run preview
```

`npm run preview` serves the production build at <http://localhost:4173/>.
For a real static host, configure an SPA fallback so `/login` and `/quote`
resolve to `index.html`.

## Verify

```bash
npm run typecheck
npm run build
```

With the backend running, the end-to-end flow is:

1. Open `/quote` while signed out → redirected to `/login`.
2. Create an account → JWT stored under `localStorage["sq-token"]` → `/quote`.
3. Materials load from `GET /materials`.
4. Drop an STL → parsed in the browser → 3D preview + bounding box.
5. **Get price** → `POST /uploads`, then poll `GET /uploads/{id}` until
   `DONE`/`FAILED`.
6. The summary renders `filamentCm3`, `printSeconds`, `breakdown.lines`, and
   `estimatedPrice` from the server.
7. Sign out clears the token; an invalid/expired token returns you to `/login`.

## Backend wiring

| UI action | API call |
|---|---|
| Sign in | `POST /auth/login` |
| Create account | `POST /auth/signup` |
| Load materials | `GET /materials` (Bearer token) |
| Get price | `POST /uploads` multipart `file` + `materialId` |
| Poll status | `GET /uploads/{id}` every 1.5 s |
| Sign out / 401 | Clear token, redirect to `/login` |

The API contract lives in [`../docs/api.md`](../docs/api.md).

## Notes and limits

- The **Estimate** panel is hidden until **Get price** is pressed. It then shows
  upload/slicing status and the returned server-side quote.
- The **Preview color** swatches and the 3D mesh color are cosmetic; the backend
  does not accept a color.
- The 3D viewport uses Three.js `OrbitControls`: drag/touch to orbit,
  wheel/pinch to zoom, and right-drag/two-finger drag to pan. The reset-view
  button restores the camera.
- The quote viewport opens with a web-optimized **3DBenchy** sample (19k
  triangles, CC0) until you upload an STL. **Price sample** sends that STL
  through the real upload/slicing flow, so you can test a quote without
  uploading a file. Source and license details are in
  [`public/models/README.md`](public/models/README.md).
- The quote is for one uploaded model. Quantity, infill, and dimensions are not
  client inputs anymore: the server reads them from the STL and its slicer
  profile.
- The bounding box is computed client-side from the STL and assumes millimetres.
- The landing page's four material cards are static marketing content. The live
  material list on `/quote` comes from the server and may contain fewer items.
- JWTs are stored in `localStorage` because the API returns Bearer tokens. Use
  an HttpOnly cookie/session if you need stronger XSS protection.
- There is no order/checkout endpoint yet; the estimate is not a binding offer.

## Structure

```text
src/
  components/   AppBar, Brand, PartSvg, StlViewer, ThemeToggle, Toast, Icons
  data/         Marketing material presets + presentation colors
  lib/          API client, STL parser, color shading, formatting
  pages/        Landing, Login, Quote
  styles/       Design tokens + component CSS
  theme/        Acid/Forest and auth providers
```
