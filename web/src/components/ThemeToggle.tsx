import { MoonIcon, SunIcon } from './Icons'
import { useTheme } from '../theme/ThemeProvider'

export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme()

  return (
    <button
      className="theme-toggle"
      type="button"
      onClick={toggleTheme}
      aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
    >
      <MoonIcon className="ic ic-moon" />
      <SunIcon className="ic ic-sun" />
    </button>
  )
}
