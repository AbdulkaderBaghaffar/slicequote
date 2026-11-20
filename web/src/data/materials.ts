export interface MaterialPreset {
  name: string
  tag: string
  /** g/cm³ — used only for the marketing cards, not for server pricing. */
  density: number
  colors: string[]
}

export const MATERIAL_PRESETS: Record<string, MaterialPreset> = {
  PLA: {
    name: 'PLA',
    tag: 'Entry · rigid',
    density: 1.24,
    colors: ['#a3e635', '#1c2308', '#f4f7e6', '#4d7c0f'],
  },
  ABS: {
    name: 'ABS',
    tag: 'Tough · heat-ready',
    density: 1.04,
    colors: ['#2a2a2a', '#b91c1c', '#e8e8e8', '#3b82f6'],
  },
  PETG: {
    name: 'PETG',
    tag: 'Strong · flexible',
    density: 1.27,
    colors: ['#0f766e', '#1c2308', '#f4f7e6', '#4d7c0f'],
  },
  TPU: {
    name: 'TPU',
    tag: 'Flexible · abrasion',
    density: 1.21,
    colors: ['#2563eb', '#1c2308', '#f4f7e6', '#60a5fa'],
  },
  ASA: {
    name: 'ASA',
    tag: 'Outdoor · UV-stable',
    density: 1.07,
    colors: ['#f97316', '#1c2308', '#e8e8e8', '#c2410c'],
  },
  NYLON: {
    name: 'Nylon',
    tag: 'Engineering · durable',
    density: 1.14,
    colors: ['#111827', '#e8e8e8', '#6b7280', '#f4f7e6'],
  },
  PC: {
    name: 'PC',
    tag: 'High-strength · heat',
    density: 1.2,
    colors: ['#475569', '#0f172a', '#e2e8f0', '#94a3b8'],
  },
  RESIN: {
    name: 'Resin',
    tag: 'SLA · high detail',
    density: 1.1,
    colors: ['#6b7280', '#2a2a2a', '#e8e8e8'],
  },
}

export const LANDING_MATERIAL_ORDER = [
  'PLA',
  'ABS',
  'PETG',
  'TPU',
  'ASA',
  'NYLON',
  'PC',
  'RESIN',
] as const

export function presetFor(name: string): MaterialPreset {
  const key = name.trim().toUpperCase()
  return (
    MATERIAL_PRESETS[key] ?? {
      name: name.trim() || 'Material',
      tag: 'Production material',
      density: 1.2,
      colors: ['#a3e635', '#1c2308', '#f4f7e6', '#4d7c0f'],
    }
  )
}

const MATERIAL_RANK = new Map<string, number>(
  LANDING_MATERIAL_ORDER.map((key, index) => [key, index]),
)

export function sortMaterials<T extends { name: string }>(materials: T[]): T[] {
  return [...materials].sort((a, b) => {
    const rankA = MATERIAL_RANK.get(a.name.trim().toUpperCase()) ?? Number.MAX_SAFE_INTEGER
    const rankB = MATERIAL_RANK.get(b.name.trim().toUpperCase()) ?? Number.MAX_SAFE_INTEGER
    return rankA - rankB || a.name.localeCompare(b.name)
  })
}
