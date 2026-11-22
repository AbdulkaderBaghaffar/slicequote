import { useRef, useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { AppBar } from '../components/AppBar'
import { EyeIcon, EyeOffIcon } from '../components/Icons'
import { ApiError } from '../lib/api'
import { useAuth } from '../theme/AuthProvider'

type Mode = 'login' | 'signup'

interface FieldErrors {
  email?: string
  password?: string
}

interface SummaryError {
  id: 'email' | 'password'
  message: string
}

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const { isAuthenticated, login, signup } = useAuth()

  const [mode, setMode] = useState<Mode>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [errors, setErrors] = useState<FieldErrors>({})
  const [summaryErrors, setSummaryErrors] = useState<SummaryError[]>([])
  const [formError, setFormError] = useState<string | null>(null)
  const [credentialError, setCredentialError] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const emailRef = useRef<HTMLInputElement>(null)
  const passwordRef = useRef<HTMLInputElement>(null)
  const summaryRef = useRef<HTMLDivElement>(null)

  const from = (location.state as { from?: string } | null)?.from ?? '/quote'

  if (isAuthenticated) {
    return <Navigate to={from} replace />
  }

  function focusSummary() {
    window.requestAnimationFrame(() => summaryRef.current?.focus())
  }

  function validateEmail(): boolean {
    const value = email.trim()
    let message = ''
    if (!EMAIL_RE.test(value)) {
      message = 'Enter a valid email address, like name@company.com.'
    } else if (value.length > 255) {
      message = 'Email is too long.'
    }
    setErrors((current) => ({ ...current, email: message || undefined }))
    return !message
  }

  function validatePassword(): boolean {
    let message = ''
    if (password.length < 8) {
      message = 'Use at least 8 characters.'
    } else if (password.length > 72) {
      message = 'Use 8–72 characters.'
    }
    setErrors((current) => ({ ...current, password: message || undefined }))
    return !message
  }

  function switchMode(nextMode: Mode) {
    setMode(nextMode)
    setErrors({})
    setSummaryErrors([])
    setFormError(null)
    setCredentialError(false)
    setPassword('')
    setShowPassword(false)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFormError(null)
    setCredentialError(false)
    setSummaryErrors([])

    const emailOk = validateEmail()
    const passwordOk = validatePassword()

    if (!emailOk || !passwordOk) {
      const nextErrors: SummaryError[] = []
      if (!emailOk) {
        nextErrors.push({ id: 'email', message: 'Enter a valid email address.' })
      }
      if (!passwordOk) {
        nextErrors.push({ id: 'password', message: 'Use a password of 8–72 characters.' })
      }
      setSummaryErrors(nextErrors)
      focusSummary()
      return
    }

    setSubmitting(true)
    try {
      if (mode === 'login') {
        await login(email.trim(), password)
      } else {
        await signup(email.trim(), password)
      }
      navigate(from, { replace: true })
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.field === 'email') {
          setErrors((current) => ({ ...current, email: error.message }))
          setSummaryErrors([{ id: 'email', message: error.message }])
        } else if (error.status === 401) {
          setCredentialError(true)
          window.requestAnimationFrame(() => passwordRef.current?.focus())
        } else {
          setFormError(error.message)
        }
      } else {
        setFormError('Something went wrong. Please try again.')
      }
      focusSummary()
    } finally {
      setSubmitting(false)
    }
  }

  const hasErrors = formError !== null || summaryErrors.length > 0

  return (
    <section className="screen" aria-labelledby="login-title">
      <AppBar backTo="/" backLabel="Back to landing" />

      <main className="container auth-wrap">
        <form className="auth-card" id="loginForm" noValidate onSubmit={handleSubmit}>
          <h1 id="login-title">
            {mode === 'login' ? 'Sign in to get your quote' : 'Create your SliceQuote account'}
          </h1>
          <p className="sub">
            {mode === 'login'
              ? 'Continue to the quotation tool to price your part.'
              : 'Upload a model and get a live price from the slicing service.'}
          </p>

          <div
            className="error-summary"
            id="loginSummary"
            role="alert"
            tabIndex={-1}
            ref={summaryRef}
            style={hasErrors ? { display: 'block' } : undefined}
          >
            {formError && <p>{formError}</p>}
            {summaryErrors.length > 0 && (
              <>
                <strong>Please fix the following:</strong>
                <ul>
                  {summaryErrors.map((error) => (
                    <li key={error.id}>
                      <a
                        href={`#${error.id}`}
                        onClick={(event) => {
                          event.preventDefault()
                          if (error.id === 'email') emailRef.current?.focus()
                          else passwordRef.current?.focus()
                        }}
                      >
                        {error.message}
                      </a>
                    </li>
                  ))}
                </ul>
              </>
            )}
          </div>

          <div className="auth-divider" aria-hidden="true" />

          <div className="field">
            <label htmlFor="email">
              Email <span className="req" aria-hidden="true">*</span>
            </label>
            <div className="input-wrap">
              <input
                className="input"
                type="email"
                id="email"
                name="email"
                autoComplete="email"
                inputMode="email"
                maxLength={255}
                placeholder="you@company.com"
                aria-describedby="emailErr emailHelp"
                aria-invalid={Boolean(errors.email)}
                aria-required="true"
                ref={emailRef}
                value={email}
                onChange={(event) => {
                  setEmail(event.target.value)
                  if (errors.email) setErrors((current) => ({ ...current, email: undefined }))
                  if (formError) setFormError(null)
                  if (credentialError) setCredentialError(false)
                }}
                onBlur={validateEmail}
              />
            </div>
            <p className="field-error" id="emailErr">{errors.email ?? ''}</p>
            <p className="field-help" id="emailHelp">We’ll only use this to send your quote.</p>
          </div>

          <div className="field">
            <label htmlFor="password">
              Password <span className="req" aria-hidden="true">*</span>
            </label>
            <div className="input-wrap">
              <input
                className="input"
                type={showPassword ? 'text' : 'password'}
                id="password"
                name="password"
                autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                maxLength={72}
                placeholder="8+ characters"
                aria-describedby="passwordErr"
                aria-invalid={Boolean(errors.password)}
                aria-required="true"
                ref={passwordRef}
                value={password}
                onChange={(event) => {
                  setPassword(event.target.value)
                  if (errors.password) {
                    setErrors((current) => ({ ...current, password: undefined }))
                  }
                  if (formError) setFormError(null)
                  if (credentialError) setCredentialError(false)
                }}
                onBlur={validatePassword}
              />
              <button
                className="pw-toggle"
                type="button"
                id="pwToggle"
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                aria-pressed={showPassword}
                onClick={() => setShowPassword((current) => !current)}
              >
                {showPassword ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            <p className="field-error" id="passwordErr">{errors.password ?? ''}</p>
          </div>

          <div className="auth-meta">
            {credentialError && (
              <span className="auth-credential-error" role="alert">
                Invalid email or password.
              </span>
            )}
            <span className="auth-switch-inline">
              {mode === 'login' ? 'New to SliceQuote?' : 'Already have an account?'}{' '}
              <button
                className="btn-link"
                type="button"
                onClick={() => switchMode(mode === 'login' ? 'signup' : 'login')}
              >
                {mode === 'login' ? 'Create an account' : 'Sign in'}
              </button>
            </span>
          </div>

          <button className="btn btn-primary btn-block" type="submit" id="loginBtn" disabled={submitting}>
            {!submitting && (
              <span id="loginBtnLabel">
                {mode === 'login' ? 'Sign in' : 'Create account'}
              </span>
            )}
            {submitting && <span className="spinner" id="loginSpinner" />}
          </button>

          <div className="auth-guest">
            <button
              className="btn btn-ghost btn-block"
              type="button"
              onClick={() => navigate('/quote')}
            >
              Continue as guest
            </button>
          </div>

          <div className="auth-row">
            <button className="btn-link" type="button" onClick={() => navigate('/')}>
              Back to home
            </button>
          </div>
        </form>
      </main>
    </section>
  )
}
