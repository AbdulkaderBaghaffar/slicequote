import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  api,
  clearStoredToken,
  getStoredToken,
  isTokenExpired,
  setUnauthorizedHandler,
  storeToken,
  tokenEmail,
} from '../lib/api'

interface AuthContextValue {
  token: string | null
  email: string | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  signup: (email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

function readUsableToken(): string | null {
  const token = getStoredToken()
  if (!token) return null
  if (isTokenExpired(token)) {
    clearStoredToken()
    return null
  }
  return token
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(readUsableToken)

  const logout = useCallback(() => {
    clearStoredToken()
    setToken(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(logout)
    return () => setUnauthorizedHandler(null)
  }, [logout])

  const login = useCallback(async (email: string, password: string) => {
    const response = await api.login(email, password)
    storeToken(response.token)
    setToken(response.token)
  }, [])

  const signup = useCallback(async (email: string, password: string) => {
    const response = await api.signup(email, password)
    storeToken(response.token)
    setToken(response.token)
  }, [])

  const email = useMemo(() => (token ? tokenEmail(token) : null), [token])

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      email,
      isAuthenticated: token !== null,
      login,
      signup,
      logout,
    }),
    [token, email, login, signup, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
