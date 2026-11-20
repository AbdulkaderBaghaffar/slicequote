import { useEffect, useRef } from 'react'
import * as THREE from 'three'
import type { BufferGeometry } from 'three'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'

interface StlViewerProps {
  geometry: BufferGeometry
  color: string
  label: string
  instances?: number
  resetKey?: number
  flatShading?: boolean
}

interface ViewerState {
  renderer: THREE.WebGLRenderer
  material: THREE.MeshStandardMaterial
  controls: OrbitControls
  layoutGroup: THREE.Group
  render: () => void
  reset: () => void
}

const MAX_INSTANCES = 24

function clearInstances(group: THREE.Group) {
  for (const child of [...group.children]) {
    group.remove(child)
    if (child instanceof THREE.InstancedMesh) child.dispose()
  }
}

export function StlViewer({
  geometry,
  color,
  label,
  instances = 1,
  resetKey = 0,
  flatShading = true,
}: StlViewerProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const stateRef = useRef<ViewerState | null>(null)

  useEffect(() => {
    const container = containerRef.current
    if (!container) return

    const scene = new THREE.Scene()
    const camera = new THREE.PerspectiveCamera(30, 1, 0.1, 100)
    camera.position.set(5.2, 3.4, 6.4)

    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
    renderer.outputColorSpace = THREE.SRGBColorSpace
    renderer.toneMapping = THREE.ACESFilmicToneMapping
    renderer.toneMappingExposure = 1.1
    container.appendChild(renderer.domElement)

    const material = new THREE.MeshStandardMaterial({
      color: new THREE.Color(color),
      roughness: 0.5,
      metalness: 0.06,
      flatShading,
      side: THREE.DoubleSide,
    })

    const layoutGroup = new THREE.Group()
    scene.add(layoutGroup)

    scene.add(new THREE.AmbientLight(0xffffff, 1.1))

    const keyLight = new THREE.DirectionalLight(0xffffff, 2.8)
    keyLight.position.set(5, 8, 9)
    scene.add(keyLight)

    const fillLight = new THREE.DirectionalLight(0xffffff, 0.7)
    fillLight.position.set(-7, 3, 5)
    scene.add(fillLight)

    const rimLight = new THREE.DirectionalLight(0xffffff, 0.9)
    rimLight.position.set(0, -6, -7)
    scene.add(rimLight)

    const controls = new OrbitControls(camera, renderer.domElement)
    controls.target.set(0, 0, 0)
    controls.enableDamping = true
    controls.dampingFactor = 0.08
    controls.rotateSpeed = 0.9
    controls.zoomSpeed = 0.9
    controls.panSpeed = 0.8
    controls.screenSpacePanning = true
    controls.zoomToCursor = true
    controls.minDistance = 3
    controls.maxDistance = 18
    controls.update()
    controls.saveState()

    const render = () => renderer.render(scene, camera)

    const resize = () => {
      const width = container.clientWidth
      const height = container.clientHeight
      if (!width || !height) return
      renderer.setSize(width, height, false)
      camera.aspect = width / height
      camera.updateProjectionMatrix()
      render()
    }

    const observer = new ResizeObserver(resize)
    observer.observe(container)
    resize()

    let frameId = 0
    const animate = () => {
      frameId = requestAnimationFrame(animate)
      if (controls.update()) render()
    }
    animate()

    stateRef.current = {
      renderer,
      material,
      controls,
      layoutGroup,
      render,
      reset: () => {
        const damping = controls.enableDamping
        controls.enableDamping = false
        controls.reset()
        controls.update()
        controls.enableDamping = damping
        render()
      },
    }

    return () => {
      cancelAnimationFrame(frameId)
      observer.disconnect()
      stateRef.current = null
      clearInstances(layoutGroup)
      controls.dispose()
      material.dispose()
      renderer.dispose()
      if (renderer.domElement.parentNode === container) {
        container.removeChild(renderer.domElement)
      }
    }
  }, [geometry, flatShading])

  useEffect(() => {
    const state = stateRef.current
    if (!state) return

    const group = state.layoutGroup
    clearInstances(group)

    geometry.computeBoundingBox()
    const size = new THREE.Vector3()
    geometry.boundingBox?.getSize(size)
    if (!geometry.boundingBox || size.x <= 0 || size.y <= 0 || size.z <= 0) return

    const count = Math.max(1, Math.min(MAX_INSTANCES, Math.floor(instances)))
    const columns = Math.ceil(Math.sqrt(count))
    const rows = Math.ceil(count / columns)

    // STL is Z-up; after the -90° X rotation the bed footprint is X × Y.
    const maxDimension = Math.max(size.x, size.y, size.z)
    const gap = maxDimension * 0.12
    const cellX = size.x + gap
    const cellZ = size.y + gap
    const width = (columns - 1) * cellX + size.x
    const depth = (rows - 1) * cellZ + size.y
    const targetSize = count === 1 ? 2.45 : 3
    const fitScale = targetSize / Math.max(width, depth, size.z, 1)

    const mesh = new THREE.InstancedMesh(geometry, state.material, count)
    mesh.frustumCulled = false

    const matrix = new THREE.Matrix4()
    const quaternion = new THREE.Quaternion().setFromEuler(
      new THREE.Euler(-Math.PI / 2, 0, 0),
    )
    const scale = new THREE.Vector3(fitScale, fitScale, fitScale)
    const position = new THREE.Vector3()

    for (let index = 0; index < count; index++) {
      const column = index % columns
      const row = Math.floor(index / columns)
      position.set(
        (column - (columns - 1) / 2) * cellX * fitScale,
        0,
        (row - (rows - 1) / 2) * cellZ * fitScale,
      )
      matrix.compose(position, quaternion, scale)
      mesh.setMatrixAt(index, matrix)
    }

    mesh.instanceMatrix.needsUpdate = true
    group.add(mesh)
    state.render()
  }, [geometry, instances, flatShading])

  useEffect(() => {
    const state = stateRef.current
    if (!state) return
    state.material.color.set(color)
    state.render()
  }, [color])

  useEffect(() => {
    stateRef.current?.reset()
  }, [resetKey])

  return <div className="stl-viewer" ref={containerRef} role="img" aria-label={label} />
}
