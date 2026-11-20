interface PartSvgProps {
  className?: string
}

/**
 * Isometric placeholder bracket, matching the OpenDesign prototype.
 * Swap for a real STL/OBJ/3MF viewer when model upload lands.
 */
export function PartSvg({ className = 'part-svg' }: PartSvgProps) {
  const frontLines: number[] = []
  const rightLines: number[] = []
  for (let y = 70; y <= 210; y += 20) frontLines.push(y)
  for (let y = 125; y <= 205; y += 20) rightLines.push(y)

  return (
    <svg
      className={className}
      viewBox="0 0 260 235"
      role="img"
      aria-label="Isometric render of a 3D-printed bracket with a raised boss"
    >
      <polygon className="part-side-r" points="138.66,110 216.6,65 216.6,175 138.66,220" />
      <polygon className="part-side-l" points="138.66,110 43.4,55 43.4,165 138.66,220" />
      <polygon className="part-top" points="138.66,110 216.6,65 121.34,10 43.4,55" />
      {frontLines.map((y) => (
        <line key={`front-${y}`} className="part-line" x1="43.4" y1={y} x2="138.66" y2={y} />
      ))}
      {rightLines.map((y) => (
        <line key={`right-${y}`} className="part-line" x1="138.66" y1={y} x2="216.6" y2={y} />
      ))}
      <polygon className="part-side-r" points="135.2,82 173.3,60 173.3,78 135.2,100" />
      <polygon className="part-side-l" points="135.2,82 86.7,54 86.7,72 135.2,100" />
      <polygon className="part-top" points="135.2,82 173.3,60 124.8,32 86.7,54" />
      <ellipse className="part-hole" cx="130" cy="75" rx="11" ry="6.4" />
    </svg>
  )
}
