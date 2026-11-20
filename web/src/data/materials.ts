export interface MaterialPreset {
  name: string
  tag: string
  /** g/cm³ — used only for the marketing cards; pricing uses the API density. */
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
  ABS: {
    name: 'ABS',
    tag: 'Tough · heat-ready',
    density: 1.04,
    colors: ['#2a2a2a', '#b91c1c', '#e8e8e8', '#3b82f6'],
  },
}

export const LANDING_MATERIAL_ORDER = ['PLA', 'PETG', 'TPU', 'ABS'] as const

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
