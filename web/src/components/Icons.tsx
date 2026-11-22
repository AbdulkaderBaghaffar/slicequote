import type { ReactNode, SVGProps } from 'react'

type IconProps = SVGProps<SVGSVGElement> & {
  size?: number
  children?: ReactNode
}

function Icon({ size = 20, className, ...props }: IconProps) {
  return (
    <svg
      className={className}
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      {...props}
    />
  )
}

export function LayersMark({ size = 18, className = 'ic' }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <path d="M12 3 3 8l9 5 9-5-9-5Z" />
      <path d="M3 12l9 5 9-5" />
      <path d="M3 16l9 5 9-5" />
    </Icon>
  )
}

export function MoonIcon({ size = 20, className }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z" />
    </Icon>
  )
}

export function SunIcon({ size = 20, className }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
    </Icon>
  )
}

export function ArrowRightIcon({ size = 16, className = 'ic' }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <path d="M5 12h14M13 6l6 6-6 6" />
    </Icon>
  )
}

export function ArrowLeftIcon({ size = 22, className = 'ic' }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <path d="M19 12H5M11 18l-6-6 6-6" />
    </Icon>
  )
}

export function ResetViewIcon({ size = 18, className = 'ic' }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <path d="M3 12a9 9 0 1 0 3-6.7" />
      <path d="M3 4v5h5" />
    </Icon>
  )
}

export function EyeIcon({ size = 20, className = 'ic' }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" />
      <circle cx="12" cy="12" r="3" />
    </Icon>
  )
}

export function EyeOffIcon({ size = 20, className = 'ic' }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <path d="M3 3l18 18" />
      <path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" />
      <path d="M9.9 5.2A9.8 9.8 0 0 1 12 5c6.5 0 10 7 10 7a17.6 17.6 0 0 1-2.4 3.1" />
      <path d="M6.2 6.2C3.6 7.9 2 12 2 12a17.6 17.6 0 0 0 4.1 4.9" />
    </Icon>
  )
}

export function UploadIcon({ size = 24, className = 'ic' }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <path d="M12 16V4" />
      <path d="M7 9l5-5 5 5" />
      <path d="M4 16v3a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-3" />
    </Icon>
  )
}

export function FileIcon({ size = 20, className = 'ic' }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <path d="M14 3H6a1 1 0 0 0-1 1v16a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V8Z" />
      <path d="M14 3v5h5" />
    </Icon>
  )
}

export function CloseIcon({ size = 16, className = 'ic' }: { size?: number; className?: string }) {
  return (
    <Icon size={size} className={className}>
      <path d="M6 6l12 12M18 6 6 18" />
    </Icon>
  )
}
