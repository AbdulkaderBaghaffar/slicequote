function hexToRgb(hex: string): [number, number, number] {
  let h = hex.replace('#', '')
  if (h.length === 3) {
    h = h
      .split('')
      .map((c) => c + c)
      .join('')
  }
  return [
    parseInt(h.slice(0, 2), 16),
    parseInt(h.slice(2, 4), 16),
    parseInt(h.slice(4, 6), 16),
  ]
}

function rgbToHex(r: number, g: number, b: number): string {
  return (
    '#' +
    [r, g, b]
      .map((v) =>
        Math.max(0, Math.min(255, Math.round(v)))
          .toString(16)
          .padStart(2, '0'),
      )
      .join('')
  )
}

/**
 * Derive an isometric face color from the selected swatch.
 * f < 1 darkens toward black; f > 1 lifts toward white.
 */
export function shade(hex: string, f: number): string {
  const [r, g, b] = hexToRgb(hex)
  if (f < 1) return rgbToHex(r * f, g * f, b * f)
  return rgbToHex(
    r + (255 - r) * (f - 1),
    g + (255 - g) * (f - 1),
    b + (255 - b) * (f - 1),
  )
}
