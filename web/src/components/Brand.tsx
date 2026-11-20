import { Link, useLocation } from 'react-router-dom'
import { LayersMark } from './Icons'

interface BrandProps {
  className?: string
}

export function Brand({ className = 'brand' }: BrandProps) {
  const { pathname } = useLocation()

  return (
    <Link
      className={className}
      to="/"
      onClick={() => {
        if (pathname === '/') window.scrollTo({ top: 0, behavior: 'smooth' })
      }}
    >
      <span className="brand-mark" aria-hidden="true">
        <LayersMark />
      </span>
      SliceQuote
    </Link>
  )
}
