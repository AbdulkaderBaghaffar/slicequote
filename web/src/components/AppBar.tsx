import { useNavigate } from 'react-router-dom'
import { Brand } from './Brand'
import { ThemeToggle } from './ThemeToggle'
import { ArrowLeftIcon } from './Icons'
import { useAuth } from '../theme/AuthProvider'

interface AppBarProps {
  backTo: '/' | '/login'
  backLabel: string
  showSignOut?: boolean
}

export function AppBar({ backTo, backLabel, showSignOut = false }: AppBarProps) {
  const navigate = useNavigate()
  const { email, logout } = useAuth()

  function signOut() {
    logout()
    navigate('/login')
  }

  return (
    <header className="app-bar">
      <div className="container app-bar-inner">
        <div className="app-bar-left">
          <button
            className="btn-icon"
            type="button"
            onClick={() => navigate(backTo)}
            aria-label={backLabel}
          >
            <ArrowLeftIcon />
          </button>
          <Brand />
        </div>
        <div className="app-bar-right">
          {showSignOut && email && (
            <span className="app-user" title={email}>
              {email}
            </span>
          )}
          <ThemeToggle />
          {showSignOut && (
            <button className="btn-link" type="button" onClick={signOut}>
              Sign out
            </button>
          )}
        </div>
      </div>
    </header>
  )
}
