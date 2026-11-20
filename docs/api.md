# API contract

Base URL: `http://localhost:8080` locally. All paths are rooted there.

Protected endpoints expect `Authorization: Bearer <token>`.

## Errors

Every non-2xx response uses this body:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "not a valid STL",
  "field": "file"
}
```

`field` is only present when the error maps to a request field (e.g. `email`,
`password`, `file`). The frontend can show `message` verbatim and use `field`
to attach the error to an input.

Common statuses:

| Status | Meaning |
|---|---|
| `400` | invalid input, malformed JSON, missing part/parameter |
| `401` | missing, invalid or expired token; bad credentials |
| `403` | request forbidden by CORS or authorization |
| `404` | upload not found or not owned by the caller |
| `409` | duplicate email / data conflict |
| `413` | upload exceeds the size limit |
| `500` | unexpected server error |

## Auth

### `POST /auth/signup`

Public. Creates the account and returns a token (one-step signup).

```json
{ "email": "user@example.com", "password": "password123" }
```

`201 Created`:

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

Rules: valid email, password 8–72 characters, email unique (case-insensitive).
Duplicate email returns `409` with `field: "email"`.

### `POST /auth/login`

Public. Returns a token for an existing account.

```json
{ "email": "user@example.com", "password": "password123" }
```

`200 OK`:

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

Wrong email or password returns `401` with `"Invalid email or password"`.

Tokens expire after 1 hour. Treat any `401` as "clear the token and go to
`/login`".

## Health

### `GET /actuator/health`

Public. `200 OK` with `{"status":"UP"}`.

## Materials

### `GET /materials`

Requires a token. Returns the **active** materials available for selection.

```json
[
  { "id": 1, "name": "PLA", "costPerGram": 0.03, "density": 1.24, "active": true }
]
```

`density` is in g/cm³ and is used together with `costPerGram` for material
pricing.

## Uploads

### `POST /uploads`

Requires a token. `multipart/form-data` with:

| Part | Type | Notes |
|---|---|---|
| `file` | file | STL, max 10MB |
| `materialId` | number | from `GET /materials` |
| `quantity` | number | optional, default `1`, range `1–24` |

Do not set `Content-Type` manually; the browser must add the multipart boundary.

`201 Created` with an `UploadResponse` whose `status` is `PENDING`:

```json
{
  "id": 42,
  "originalFilename": "cube.stl",
  "materialId": 1,
  "quantity": 1,
  "status": "PENDING",
  "estimatedPrice": null,
  "errorMessage": null,
  "breakdown": null,
  "createdAt": "2026-11-19T09:57:55Z",
  "printSeconds": null,
  "filamentCm3": null
}
```

### `GET /uploads`

Requires a token. Returns the caller's uploads, newest first.

### `GET /uploads/{id}`

Requires a token. Returns one upload owned by the caller, or `404`.

### `POST /uploads/{id}/reprice`

Requires a token. Recalculates a completed upload for a new quantity without
re-slicing. Query parameter:

| Param | Type | Notes |
|---|---|---|
| `quantity` | number | default `1`, range `1–24` |

`200 OK` with the updated `UploadResponse`. Returns `409` if the upload is not
`DONE`.

## Upload status

`PENDING` → `PROCESSING` → `DONE` or `FAILED`.

Poll `GET /uploads/{id}` until `DONE` or `FAILED`. Do not render a price for
`PENDING`/`PROCESSING`; show "calculating" instead.

When `DONE`:

```json
{
  "id": 42,
  "originalFilename": "cube.stl",
  "materialId": 1,
  "quantity": 3,
  "status": "DONE",
  "estimatedPrice": 1.28,
  "errorMessage": null,
  "breakdown": {
    "total": 1.28,
    "lines": [
      { "label": "Machine time", "amount": 1.11 },
      { "label": "Material cost", "amount": 0.12 },
      { "label": "Base fee", "amount": 0.05 }
    ]
  },
  "createdAt": "2026-11-19T09:57:55Z",
  "printSeconds": 834,
  "filamentCm3": 3.59
}
```

`estimatedPrice` and `breakdown.total` are for the requested `quantity`.
`printSeconds` and `filamentCm3` remain per part. `breakdown.total` always
equals the sum of `breakdown.lines[].amount` and equals `estimatedPrice`. Render
`lines[]` generically; new labels such as `Minimum order` may appear without a
frontend change.

Material cost is calculated as:

```text
materialCost = filamentCm3 × quantity × density × costPerGram
```

where `density` is the material row's g/cm³ value.

When `FAILED`, `errorMessage` is set and both `breakdown` and
`estimatedPrice` are `null`.

## CORS

Allowed origins come from `CORS_ALLOWED_ORIGINS` (comma-separated).
Local defaults: `http://localhost:5173` (Vite dev) and
`http://localhost:4173` (Vite preview). `Authorization`, `Content-Type` and
`Accept` request headers are allowed.

## Limits

- STL upload: 10MB (request 12MB)
- Quantity: 1–24
- Status polling: keep it to roughly one request per 1–2 seconds
