import {
  lazy,
  Suspense,
  useEffect,
  useMemo,
  useRef,
  useState,
  type DragEvent,
} from 'react'
import { useNavigate } from 'react-router-dom'
import { AppBar } from '../components/AppBar'
import { CloseIcon, FileIcon, ResetViewIcon, UploadIcon } from '../components/Icons'
import { useToast } from '../components/Toast'
import { presetFor, sortMaterials } from '../data/materials'
import { fmtMoney, fmtTime } from '../lib/format'
import { ApiError, api, type ApiMaterial, type UploadJob } from '../lib/api'
import { parseStl, type ParsedModel } from '../lib/stl'

const StlViewer = lazy(() =>
  import('../components/StlViewer').then((module) => ({ default: module.StlViewer })),
)

type QuotePhase = 'idle' | 'uploading' | 'processing' | 'done' | 'failed'

const POLL_INTERVAL_MS = 1500
const MAX_STL_BYTES = 100 * 1024 * 1024
const MAX_INSTANCES = 24

function delay(ms: number) {
  return new Promise<void>((resolve) => window.setTimeout(resolve, ms))
}

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function Quote() {
  const showToast = useToast()
  const navigate = useNavigate()

  const [materials, setMaterials] = useState<ApiMaterial[]>([])
  const [materialsLoading, setMaterialsLoading] = useState(true)
  const [materialsError, setMaterialsError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)
  const [materialId, setMaterialId] = useState<number | null>(null)

  const [color, setColor] = useState('#a3e635')
  const [file, setFile] = useState<File | null>(null)
  const [sampleFile, setSampleFile] = useState<File | null>(null)
  const [model, setModel] = useState<ParsedModel | null>(null)
  const [placeholderModel, setPlaceholderModel] = useState<ParsedModel | null>(null)
  const [sampleLoading, setSampleLoading] = useState(true)
  const [modelError, setModelError] = useState<string | null>(null)
  const [parsing, setParsing] = useState(false)
  const [dragging, setDragging] = useState(false)

  const [phase, setPhase] = useState<QuotePhase>('idle')
  const [job, setJob] = useState<UploadJob | null>(null)
  const [jobError, setJobError] = useState<string | null>(null)
  const [repricing, setRepricing] = useState(false)

  const [resetViewKey, setResetViewKey] = useState(0)
  const [instances, setInstances] = useState(1)

  const inputRef = useRef<HTMLInputElement>(null)
  const modelRef = useRef<ParsedModel | null>(null)
  const placeholderRef = useRef<ParsedModel | null>(null)
  const runRef = useRef(0)
  const fileReadRef = useRef(0)
  const instancesRef = useRef(1)
  const repricingRef = useRef(false)
  const pendingRepriceRef = useRef<number | null>(null)

  useEffect(() => {
    instancesRef.current = instances
  }, [instances])

  useEffect(() => {
    const controller = new AbortController()
    setMaterialsLoading(true)
    setMaterialsError(null)

    api
      .materials(controller.signal)
      .then((list) => {
        const sorted = sortMaterials(list)
        setMaterials(sorted)
        setMaterialId((current) =>
          current !== null && sorted.some((material) => material.id === current)
            ? current
            : (sorted[0]?.id ?? null),
        )
        setMaterialsLoading(false)
      })
      .catch((error) => {
        if (error instanceof DOMException && error.name === 'AbortError') return
        setMaterialsError(
          error instanceof ApiError ? error.message : 'Could not load materials.',
        )
        setMaterialsLoading(false)
      })

    return () => controller.abort()
  }, [reloadKey])

  useEffect(() => {
    if (!model) return
    modelRef.current = model
  }, [model])

  useEffect(() => {
    let cancelled = false
    setSampleLoading(true)

    fetch('/models/benchy.stl')
      .then((response) => {
        if (!response.ok) throw new Error('Sample model not available')
        return response.arrayBuffer()
      })
      .then(async (buffer) => {
        const parsed = await parseStl(buffer, { smooth: true })
        const sample = new File([buffer], '3DBenchy.stl', { type: 'model/stl' })
        return { parsed, sample }
      })
      .then(({ parsed, sample }) => {
        if (cancelled) {
          parsed.geometry.dispose()
          return
        }
        placeholderRef.current?.geometry.dispose()
        placeholderRef.current = parsed
        setPlaceholderModel(parsed)
        setSampleFile(sample)
        setSampleLoading(false)
      })
      .catch(() => {
        if (!cancelled) setSampleLoading(false)
      })

    return () => {
      cancelled = true
      placeholderRef.current?.geometry.dispose()
      placeholderRef.current = null
    }
  }, [])

  useEffect(() => {
    return () => {
      runRef.current += 1
      fileReadRef.current += 1
      modelRef.current?.geometry.dispose()
      modelRef.current = null
    }
  }, [])

  const selectedMaterial = useMemo(
    () => materials.find((material) => material.id === materialId) ?? null,
    [materials, materialId],
  )

  const selectedName = selectedMaterial?.name ?? null
  useEffect(() => {
    if (!selectedName) return
    setColor(presetFor(selectedName).colors[0])
  }, [selectedName])

  const preset = presetFor(selectedName ?? 'PLA')
  const busy = phase === 'uploading' || phase === 'processing' || repricing
  const priceFile = file ?? sampleFile
  const canPrice = Boolean(priceFile) && materialId !== null && !busy && !parsing
  const pricingSample = file === null && sampleFile !== null

  const statusLabel = repricing
    ? 'Pricing'
    : phase === 'uploading'
      ? 'Uploading'
      : phase === 'processing'
        ? 'Slicing'
        : phase === 'done'
          ? 'Ready'
          : phase === 'failed'
            ? 'Failed'
            : 'Not priced'

  const statusTone = repricing
    ? 'working'
    : phase === 'done'
      ? 'done'
      : phase === 'failed'
        ? 'failed'
        : busy
          ? 'working'
          : 'idle'

  const priceButtonLabel = repricing
    ? 'Pricing…'
    : phase === 'uploading'
      ? 'Uploading…'
      : phase === 'processing'
        ? 'Slicing…'
        : phase === 'done'
          ? 'Price again'
          : phase === 'failed'
            ? 'Try again'
            : pricingSample
              ? 'Price sample'
              : 'Get price'

  function clearJob() {
    runRef.current += 1
    repricingRef.current = false
    pendingRepriceRef.current = null
    setRepricing(false)
    setJob(null)
    setJobError(null)
    setPhase('idle')
  }

  function replaceModel(next: ParsedModel | null) {
    const previous = modelRef.current
    modelRef.current = next
    setModel(next)
    previous?.geometry.dispose()
  }

  async function acceptFile(nextFile: File) {
    setModelError(null)

    if (!/\.stl$/i.test(nextFile.name)) {
      setModelError('Choose an STL file (.stl).')
      return
    }
    if (nextFile.size > MAX_STL_BYTES) {
      setModelError('STL files must be 100 MB or smaller.')
      return
    }

    const requestId = ++fileReadRef.current
    setParsing(true)
    clearJob()

    try {
      const buffer = await nextFile.arrayBuffer()
      if (requestId !== fileReadRef.current) return

      const parsed = await parseStl(buffer)
      if (requestId !== fileReadRef.current) {
        parsed.geometry.dispose()
        return
      }

      replaceModel(parsed)
      setFile(nextFile)
    } catch {
      if (requestId === fileReadRef.current) {
        replaceModel(null)
        setFile(null)
        setModelError('Could not read this STL file. It may be corrupt.')
      }
    } finally {
      if (requestId === fileReadRef.current) setParsing(false)
    }
  }

  function clearFile() {
    fileReadRef.current += 1
    clearJob()
    replaceModel(null)
    setFile(null)
    setModelError(null)
    setParsing(false)
    if (inputRef.current) inputRef.current.value = ''
  }

  function selectMaterial(id: number) {
    if (id === materialId) return
    setMaterialId(id)
    clearJob()
  }

  function handleDrop(event: DragEvent<HTMLButtonElement>) {
    event.preventDefault()
    setDragging(false)
    const dropped = event.dataTransfer.files[0]
    if (dropped) void acceptFile(dropped)
  }

  function resetOptions() {
    clearFile()
    const first = materials[0]
    setMaterialId(first?.id ?? null)
    setColor(first ? presetFor(first.name).colors[0] : '#a3e635')
    setInstances(1)
    setResetViewKey((value) => value + 1)
    showToast('Quote reset.')
  }

  async function getPrice() {
    if (!priceFile || materialId === null || busy) return

    const runId = ++runRef.current
    setPhase('uploading')
    setJob(null)
    setJobError(null)

    try {
      let current = await api.createUpload(priceFile, materialId, instances)
      if (runId !== runRef.current) return

      setJob(current)
      setInstances(current.quantity)

      if (current.status === 'DONE') {
        setPhase('done')
        showToast('Quote ready.')
        return
      }
      if (current.status === 'FAILED') {
        setPhase('failed')
        setJobError(current.errorMessage ?? 'Slicing failed.')
        return
      }

      setPhase('processing')
      while (current.status === 'PENDING' || current.status === 'PROCESSING') {
        await delay(POLL_INTERVAL_MS)
        if (runId !== runRef.current) return

        current = await api.getUpload(current.id)
        if (runId !== runRef.current) return

        setJob(current)
        if (current.status === 'DONE') {
          setPhase('done')
          showToast('Quote ready.')
          break
        }
        if (current.status === 'FAILED') {
          setPhase('failed')
          setJobError(current.errorMessage ?? 'Slicing failed.')
          break
        }
      }
    } catch (error) {
      if (runId !== runRef.current) return
      if (error instanceof DOMException && error.name === 'AbortError') return
      if (error instanceof ApiError && error.status === 401) return

      setPhase('failed')
      setJobError(
        error instanceof ApiError
          ? error.message
          : 'Something went wrong while pricing this model.',
      )
    }
  }

  async function reprice(quantity: number) {
    if (!job || job.status !== 'DONE') return
    if (repricingRef.current) {
      pendingRepriceRef.current = quantity
      return
    }

    repricingRef.current = true
    setRepricing(true)
    setJobError(null)

    const runId = ++runRef.current

    try {
      const updated = await api.reprice(job.id, quantity)
      if (runId !== runRef.current) return
      setJob(updated)
      if (pendingRepriceRef.current === null) {
        setInstances(updated.quantity)
      }
      setPhase('done')
    } catch (error) {
      if (runId !== runRef.current) return
      if (error instanceof DOMException && error.name === 'AbortError') return
      if (error instanceof ApiError && error.status === 401) return

      if (pendingRepriceRef.current === null) {
        instancesRef.current = job.quantity
        setInstances(job.quantity)
      }
      setPhase('done')
      showToast(
        error instanceof ApiError
          ? error.message
          : 'Could not update the quantity. Please try again.',
      )
    } finally {
      if (runId === runRef.current) {
        repricingRef.current = false
        setRepricing(false)

        const pending = pendingRepriceRef.current
        pendingRepriceRef.current = null
        if (pending !== null && pending !== quantity) {
          void reprice(pending)
        }
      }
    }
  }

  function adjustInstances(delta: number) {
    const next = Math.max(1, Math.min(MAX_INSTANCES, instancesRef.current + delta))
    if (next === instancesRef.current) return

    instancesRef.current = next
    setInstances(next)

    if (phase === 'done' && job?.status === 'DONE') {
      void reprice(next)
    } else if (phase === 'failed') {
      clearJob()
    }
  }

  const previewModel = model ?? placeholderModel
  const isPlaceholder = model === null && placeholderModel !== null

  const boundsLabel = previewModel
    ? `${previewModel.size.x.toFixed(1)} × ${previewModel.size.y.toFixed(1)} × ${previewModel.size.z.toFixed(1)} mm`
    : '—'

  const filamentLabel =
    job?.filamentCm3 != null ? `${Number(job.filamentCm3).toFixed(2)} cm³` : '—'
  const printTimeLabel =
    job?.printSeconds != null ? fmtTime(Number(job.printSeconds) / 60) : '—'
  const totalLabel =
    job?.estimatedPrice != null ? fmtMoney(Number(job.estimatedPrice)) : '—'
  const breakdownLines = job?.breakdown?.lines ?? []

  return (
    <section className="screen" aria-labelledby="quote-title">
      <AppBar backTo="/" backLabel="Back to landing" showSignOut />

      <main className="container quote-wrap">
        <div className="quote-head">
          <div>
            <h1 id="quote-title">Build your quote</h1>
            <p className="sub">Upload an STL, pick a material, and get a server-side price.</p>
          </div>
          <button
            className="btn btn-ghost"
            type="button"
            onClick={() => navigate('/history')}
          >
            History
          </button>
        </div>

        <div className="quote-grid">
          <div className="vp">
            <div className="vp-top">
              <span className="vp-title">LIVE 3D PREVIEW</span>
              <div className="vp-actions">
                <button
                  className="btn-icon"
                  type="button"
                  id="resetView"
                  aria-label="Reset view"
                  title="Reset view"
                  style={{ color: 'var(--on-dark-muted)' }}
                  onClick={() => setResetViewKey((value) => value + 1)}
                >
                  <ResetViewIcon />
                </button>
              </div>
            </div>
            <div className="vp-stage">
              <div className="vp-gridline" aria-hidden="true" />
              {previewModel ? (
                <>
                  <div className="vp-shadow" aria-hidden="true" />
                  <Suspense
                    fallback={
                      <div className="vp-parse" role="status">
                        <span className="spinner" />
                        Preparing viewer…
                      </div>
                    }
                  >
                    <StlViewer
                      geometry={previewModel.geometry}
                      color={color}
                      instances={instances}
                      resetKey={resetViewKey}
                      flatShading={!isPlaceholder}
                      label={
                        isPlaceholder
                          ? '3D preview of the 3DBenchy sample model'
                          : `3D preview of ${file?.name ?? 'uploaded model'}`
                      }
                    />
                  </Suspense>
                </>
              ) : (
                <div className="vp-empty" role="status">
                  {sampleLoading ? (
                    <>
                      <span className="spinner" />
                      <span>Loading 3D preview…</span>
                    </>
                  ) : (
                    <>
                      <strong>3D preview unavailable</strong>
                      <span>Upload an STL to preview your model.</span>
                    </>
                  )}
                </div>
              )}
              {parsing && (
                <div className="vp-parse" role="status">
                  <span className="spinner" />
                  Reading model…
                </div>
              )}
            </div>
            <div className="vp-hint">
              <span className="hint-mouse">Drag to orbit</span>
              <span className="hint-mouse">Scroll to zoom</span>
              <span className="hint-mouse">Right-drag to pan</span>
              <span className="hint-touch">Drag to orbit</span>
              <span className="hint-touch">Pinch to zoom</span>
              <span className="hint-touch">Two-finger drag to pan</span>
            </div>
          </div>

          <div className="options" role="group" aria-label="Customization options">
            <div className="opt-section">
              <div className="opt-label">
                <span className="t">Material</span>
                <span className="v">
                  {materialsLoading
                    ? 'loading…'
                    : materialsError
                      ? 'unavailable'
                      : `${materials.length} options`}
                </span>
              </div>
              <div className="mat-list" id="matList">
                {materialsLoading && (
                  <div className="mat-state">
                    <span className="spinner" />
                    Loading materials…
                  </div>
                )}
                {!materialsLoading && materialsError && (
                  <div className="mat-state mat-state-error">
                    <span>{materialsError}</span>
                    <button
                      className="btn-link"
                      type="button"
                      onClick={() => setReloadKey((value) => value + 1)}
                    >
                      Retry
                    </button>
                  </div>
                )}
                {!materialsLoading && !materialsError && materials.length === 0 && (
                  <div className="mat-state">No materials are configured on the server.</div>
                )}
                {!materialsLoading &&
                  !materialsError &&
                  materials.map((material) => {
                    const materialPreset = presetFor(material.name)
                    return (
                      <button
                        key={material.id}
                        type="button"
                        className="mat-opt"
                        data-mat={material.name}
                        aria-pressed={material.id === materialId}
                        onClick={() => selectMaterial(material.id)}
                      >
                        <span className="n">{material.name}</span>
                        <span className="d">{materialPreset.tag}</span>
                        <span className="c">
                          ${Number(material.costPerGram).toFixed(2)}/g
                        </span>
                      </button>
                    )
                  })}
              </div>
            </div>

            <div className="opt-section">
              <div className="opt-label">
                <span className="t">Preview color</span>
                <span className="v" id="colorName">{color.toUpperCase()}</span>
              </div>
              <div className="swatches" id="swatches">
                {preset.colors.map((swatch) => (
                  <button
                    key={swatch}
                    type="button"
                    className="swatch"
                    data-color={swatch}
                    aria-pressed={swatch === color}
                    aria-label={`Preview color ${swatch}`}
                    style={{ background: swatch }}
                    onClick={() => setColor(swatch)}
                  />
                ))}
              </div>
            </div>

            <div className="opt-section">
              <div className="opt-label">
                <span className="t">Model</span>
                <span className="v">STL · 100 MB max</span>
              </div>
              <input
                ref={inputRef}
                id="stlFile"
                className="sr-only"
                type="file"
                accept=".stl,model/stl,application/sla"
                aria-label="Upload STL model"
                onChange={(event) => {
                  const selected = event.target.files?.[0]
                  event.target.value = ''
                  if (selected) void acceptFile(selected)
                }}
              />
              {file ? (
                <div className="file-chip">
                  <span className="file-icon" aria-hidden="true">
                    <FileIcon />
                  </span>
                  <span className="file-meta">
                    <b title={file.name}>{file.name}</b>
                    <small>
                      {formatBytes(file.size)}
                      {model ? ' · STL' : ''}
                    </small>
                  </span>
                  <span className="file-actions">
                    <button
                      className="btn-link"
                      type="button"
                      onClick={() => inputRef.current?.click()}
                    >
                      Replace
                    </button>
                    <button
                      className="btn-icon file-remove"
                      type="button"
                      aria-label="Remove model"
                      onClick={clearFile}
                    >
                      <CloseIcon />
                    </button>
                  </span>
                </div>
              ) : (
                <button
                  type="button"
                  className={`dropzone${dragging ? ' is-dragover' : ''}`}
                  onClick={() => inputRef.current?.click()}
                  onDragEnter={(event) => {
                    event.preventDefault()
                    setDragging(true)
                  }}
                  onDragOver={(event) => {
                    event.preventDefault()
                    setDragging(true)
                  }}
                  onDragLeave={() => setDragging(false)}
                  onDrop={handleDrop}
                >
                  <UploadIcon className="ic dz-icon" size={26} />
                  <span>
                    <b>Drop an STL</b> or click to browse
                  </span>
                  <small>Binary or ASCII · max 100 MB</small>
                </button>
              )}
              {modelError && (
                <p className="field-error" role="alert">{modelError}</p>
              )}
            </div>

            <div className="opt-section">
              <div className="opt-label">
                <span className="t">Bounding box</span>
                <span className="v" id="sizeVal">{boundsLabel}</span>
              </div>
              <div className="slider-row">
                <div className="dim-grid">
                  <div className="dim-cell">
                    <span className="dim-key">X mm</span>
                    <span className="val" id="dimXOut">
                      {previewModel ? previewModel.size.x.toFixed(1) : '—'}
                    </span>
                  </div>
                  <div className="dim-cell">
                    <span className="dim-key">Y mm</span>
                    <span className="val" id="dimYOut">
                      {previewModel ? previewModel.size.y.toFixed(1) : '—'}
                    </span>
                  </div>
                  <div className="dim-cell">
                    <span className="dim-key">Z mm</span>
                    <span className="val" id="dimZOut">
                      {previewModel ? previewModel.size.z.toFixed(1) : '—'}
                    </span>
                  </div>
                </div>
                {model && (
                  <p className="field-help">
                    Read from your STL. Units are assumed to be millimetres.
                  </p>
                )}
              </div>
            </div>

            <div className="opt-section">
              <div className="opt-label">
                <span className="t">Instances</span>
                <span className="v" id="instanceLabel">
                  {instances} {instances === 1 ? 'part' : 'parts'}
                </span>
              </div>
              <div className="qty">
                <button
                  type="button"
                  id="instanceMinus"
                  aria-label="Decrease instances"
                  disabled={busy || instances <= 1}
                  onClick={() => adjustInstances(-1)}
                >
                  −
                </button>
                <span className="n" id="instanceVal">{instances}</span>
                <button
                  type="button"
                  id="instancePlus"
                  aria-label="Increase instances"
                  disabled={busy || instances >= MAX_INSTANCES}
                  onClick={() => adjustInstances(1)}
                >
                  +
                </button>
              </div>
              <p className="field-help">
                The 3D preview and server price update with this count (max {MAX_INSTANCES}).
              </p>
            </div>

            {phase !== 'idle' && (
              <div className="summary" aria-live="polite" aria-busy={busy}>
                <div className="summary-head">
                  <h3>Estimate</h3>
                  <span className={`status-pill ${statusTone}`}>{statusLabel}</span>
                </div>
                <div className="sum-row">
                  <span className="k">Material</span>
                  <span className="v">{selectedMaterial?.name ?? '—'}</span>
                </div>
                <div className="sum-row">
                  <span className="k">Filament / part</span>
                  <span className="v" id="sumVolume">{filamentLabel}</span>
                </div>
                <div className="sum-row">
                  <span className="k">Print time / part</span>
                  <span className="v" id="sumTime">{printTimeLabel}</span>
                </div>
                <div className="sum-row">
                  <span className="k">Instances</span>
                  <span className="v" id="sumInstances">{instances}</span>
                </div>
                {breakdownLines.map((line, index) => (
                  <div className="sum-row" key={`${line.label}-${index}`}>
                    <span className="k">{line.label}</span>
                    <span className="v">{fmtMoney(Number(line.amount))}</span>
                  </div>
                ))}
                {instances > 1 && (
                  <p className="summary-note">
                    Server total for {instances} parts; base fee and minimum order
                    apply once.
                  </p>
                )}
                <div className="sum-row total">
                  <span className="k">Estimated total</span>
                  <span>
                    <span className="v" id="sumTotal">{totalLabel}</span>
                    <span className="per" id="sumPer">not binding</span>
                  </span>
                </div>
                {(phase === 'uploading' || phase === 'processing') && (
                  <p className="summary-note summary-note-row">
                    <span className="spinner" />
                    {phase === 'uploading' ? 'Uploading model…' : 'Slicing on the server…'}
                  </p>
                )}
                {repricing && (
                  <p className="summary-note summary-note-row">
                    <span className="spinner" />
                    Updating quantity…
                  </p>
                )}
                {phase === 'failed' && jobError && (
                  <p className="quote-error" role="alert">{jobError}</p>
                )}
              </div>
            )}

            <div className="quote-actions">
              <button className="btn btn-ghost" type="button" id="resetBtn" onClick={resetOptions}>
                Reset
              </button>
              <button
                className="btn btn-primary"
                type="button"
                id="priceBtn"
                disabled={!canPrice}
                onClick={() => void getPrice()}
              >
                {busy && <span className="spinner" />}
                {priceButtonLabel}
              </button>
            </div>
          </div>
        </div>
      </main>
    </section>
  )
}
