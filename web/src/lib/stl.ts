import type { BufferGeometry } from 'three'

export interface ModelSize {
  x: number
  y: number
  z: number
}

export interface ParsedModel {
  geometry: BufferGeometry
  size: ModelSize
}

export interface ParseStlOptions {
  /** Smooth shallow angles (used for the decimated sample model). */
  smooth?: boolean
}

/**
 * Parse an STL in the browser so the preview and bounding box appear before
 * the server finishes slicing. Units are assumed to be millimetres.
 *
 * three.js is loaded on demand so the landing and login screens stay light.
 */
export async function parseStl(
  buffer: ArrayBuffer,
  options: ParseStlOptions = {},
): Promise<ParsedModel> {
  const { STLLoader } = await import('three/addons/loaders/STLLoader.js')
  let geometry = new STLLoader().parse(buffer)
  geometry.computeBoundingBox()

  const box = geometry.boundingBox
  if (!box) throw new Error('STL has no geometry')

  const size = {
    x: box.max.x - box.min.x,
    y: box.max.y - box.min.y,
    z: box.max.z - box.min.z,
  }

  if (!Number.isFinite(size.x) || !Number.isFinite(size.y) || !Number.isFinite(size.z)) {
    geometry.dispose()
    throw new Error('STL has invalid geometry')
  }

  geometry.center()

  if (options.smooth) {
    const { mergeVertices, toCreasedNormals } = await import(
      'three/addons/utils/BufferGeometryUtils.js'
    )
    geometry.deleteAttribute('normal')
    const welded = mergeVertices(geometry, 1e-4)
    geometry.dispose()
    geometry = toCreasedNormals(welded, Math.PI / 3)
    if (geometry !== welded) welded.dispose()
  } else {
    geometry.computeVertexNormals()
  }

  return { geometry, size }
}
