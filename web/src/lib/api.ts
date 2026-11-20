export interface ApiMaterial {
  id: number
  name: string
  costPerGram: number
}

export interface AuthResponse {
  token: string
}

export type UploadStatus = 'PENDING' | 'PROCESSING' | 'DONE' | 'FAILED'

export interface PriceLine {
  label: string
  amount: number
}

export interface PriceBreakdown {
  total: number
  lines: PriceLine[]
}

export interface UploadJob {
  id: number
  originalFilename: string
  materialId: number
  status: UploadStatus
  estimatedPrice: number | null
  errorMessage: string | null
  breakdown: PriceBreakdown | null
  createdAt: string
  printSeconds: number | null
  filamentCm3: number | null
}

interface ApiErrorBody {
  status?: number
  error?: string
  message?: string
  field?: string
}

const TOKEN_KEY = 'sq-token'
const DEFAULT_API_URL = 'http://localhost:8080'

const configuredUrl = import.meta.env.VITE_API_URL
export const API_URL = (configuredUrl === undefined ? DEFAULT_API_URL : configuredUrl).replace(/\/$/, '')

export class ApiError extends Error {
  readonly status: number
  readonly field?: string

  constructor(message: string, status = 0, field?: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.field = field
  }
}

let unauthorizedHandler: (() => void) | null = null

export function setUnauthorizedHandler(handler: (() => void) | null) {
  unauthorizedHandler = handler
}

export function getStoredToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function storeToken(token: string) {
  try {
    localStorage.setItem(TOKEN_KEY, token)
  } catch {
    /* storage disabled */
  }
}

export function clearStoredToken() {
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {
    /* storage disabled */
  }
}

function decodeTokenPayload(token: string): Record<string, unknown> | null {
  try {
    const part = token.split('.')[1]
    if (!part) return null
    const base64 = part.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
    return JSON.parse(atob(padded)) as Record<string, unknown>
  } catch {
    return null
  }
}

export function isTokenExpired(token: string): boolean {
  const payload = decodeTokenPayload(token)
  if (!payload || typeof payload.exp !== 'number') return true
  return payload.exp * 1000 <= Date.now()
}

export function tokenEmail(token: string): string | null {
  const payload = decodeTokenPayload(token)
  return typeof payload?.sub === 'string' ? payload.sub : null
}

interface RequestOptions {
  method?: 'GET' | 'POST'
  body?: Record<string, unknown> | FormData
  auth?: boolean
  signal?: AbortSignal
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  let body: BodyInit | undefined

  if (options.body instanceof FormData) {
    body = options.body
  } else if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
    body = JSON.stringify(options.body)
  }

  const token = getStoredToken()
  if (options.auth !== false && token) {
    headers.Authorization = `Bearer ${token}`
  }

  let response: Response
  try {
    response = await fetch(`${API_URL}${path}`, {
      method: options.method ?? (body === undefined ? 'GET' : 'POST'),
      headers,
      body,
      signal: options.signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw new ApiError('Can’t reach the SliceQuote server. Is it running?')
  }

  if (!response.ok) {
    let payload: ApiErrorBody = {}
    try {
      payload = (await response.json()) as ApiErrorBody
    } catch {
      /* non-JSON error body */
    }
    const message = payload.message ?? `Request failed (${response.status})`
    const apiError = new ApiError(message, response.status, payload.field)

    if (response.status === 401 && options.auth !== false) {
      clearStoredToken()
      unauthorizedHandler?.()
    }
    throw apiError
  }

  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

export const api = {
  login(email: string, password: string) {
    return request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: { email, password },
      auth: false,
    })
  },

  signup(email: string, password: string) {
    return request<AuthResponse>('/auth/signup', {
      method: 'POST',
      body: { email, password },
      auth: false,
    })
  },

  materials(signal?: AbortSignal) {
    return request<ApiMaterial[]>('/materials', { signal })
  },

  createUpload(file: File, materialId: number) {
    const body = new FormData()
    body.append('file', file)
    body.append('materialId', String(materialId))
    return request<UploadJob>('/uploads', { method: 'POST', body })
  },

  getUpload(id: number, signal?: AbortSignal) {
    return request<UploadJob>(`/uploads/${id}`, { signal })
  },
}
