import { useCallback, useEffect, useState } from 'react'
import { FileIcon } from './Icons'
import { ApiError, api, type UploadJob, type UploadStatus } from '../lib/api'
import { fmtDate, fmtMoney, fmtTime } from '../lib/format'

interface HistoryPanelProps {
  onBackToQuote: () => void
}

function isWorking(status: UploadStatus): boolean {
  return status === 'PENDING' || status === 'PROCESSING'
}

function statusLabel(status: UploadStatus): string {
  switch (status) {
    case 'DONE':
      return 'Ready'
    case 'FAILED':
      return 'Failed'
    case 'PROCESSING':
      return 'Slicing'
    case 'PENDING':
      return 'Pending'
  }
}

function statusTone(status: UploadStatus): string {
  if (status === 'DONE') return 'done'
  if (status === 'FAILED') return 'failed'
  return 'working'
}

export function HistoryPanel({ onBackToQuote }: HistoryPanelProps) {
  const [uploads, setUploads] = useState<UploadJob[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [expandedId, setExpandedId] = useState<number | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  const load = useCallback(async (signal?: AbortSignal, silent = false) => {
    if (!silent) setLoading(true)
    try {
      const list = await api.uploads(signal)
      setUploads(list)
      setError(null)
    } catch (caught) {
      if (caught instanceof DOMException && caught.name === 'AbortError') return
      if (caught instanceof ApiError && caught.status === 401) return
      setError(
        caught instanceof ApiError
          ? caught.message
          : 'Could not load your quote history.',
      )
    } finally {
      if (!silent) setLoading(false)
    }
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    void load(controller.signal)
    return () => controller.abort()
  }, [load, reloadKey])

  const hasWorking = uploads.some((upload) => isWorking(upload.status))
  useEffect(() => {
    if (!hasWorking) return
    const timer = window.setInterval(() => {
      void load(undefined, true)
    }, 3000)
    return () => window.clearInterval(timer)
  }, [hasWorking, load])

  if (loading) {
    return (
      <div className="history-loading" role="status">
        <span className="spinner" />
        Loading quotes…
      </div>
    )
  }

  if (error) {
    return (
      <div className="history-empty">
        <h2>Couldn't load history</h2>
        <p>{error}</p>
        <button
          className="btn btn-ghost"
          type="button"
          onClick={() => setReloadKey((value) => value + 1)}
        >
          Try again
        </button>
      </div>
    )
  }

  if (uploads.length === 0) {
    return (
      <div className="history-empty">
        <h2>No quotes yet</h2>
        <p>Price your first model and it will show up here.</p>
        <button className="btn btn-primary" type="button" onClick={onBackToQuote}>
          Start a quote
        </button>
      </div>
    )
  }

  return (
    <div className="history-panel">
      <div className="history-toolbar">
        <span className="history-count">
          {uploads.length} {uploads.length === 1 ? 'quote' : 'quotes'}
        </span>
        <button
          className="btn-link"
          type="button"
          onClick={() => setReloadKey((value) => value + 1)}
        >
          Refresh
        </button>
      </div>

      <div className="history-list">
        {uploads.map((job) => {
          const expanded = expandedId === job.id
          const ready = job.status === 'DONE'
          const total =
            job.estimatedPrice != null ? fmtMoney(Number(job.estimatedPrice)) : '—'

          return (
            <article className="history-card" key={job.id}>
              <div className="history-main">
                <div className="history-file">
                  <span className="file-icon" aria-hidden="true">
                    <FileIcon />
                  </span>
                  <div className="history-name">
                    <h3 title={job.originalFilename}>{job.originalFilename}</h3>
                    <p>
                      {job.materialName ?? `Material #${job.materialId}`}
                      {' · '}
                      {job.quantity} {job.quantity === 1 ? 'part' : 'parts'}
                      {' · '}
                      {fmtDate(job.createdAt)}
                    </p>
                  </div>
                </div>

                <div className="history-meta">
                  <span className={`history-status ${statusTone(job.status)}`}>
                    {statusLabel(job.status)}
                  </span>
                  <span className="history-price">{ready ? total : '—'}</span>
                  <button
                    className="btn-link"
                    type="button"
                    aria-expanded={expanded}
                    onClick={() => setExpandedId(expanded ? null : job.id)}
                  >
                    {expanded ? 'Hide details' : 'Details'}
                  </button>
                </div>
              </div>

              {expanded && (
                <div className="history-details">
                  <div className="history-stats">
                    <div className="history-stat">
                      <span>Filament / part</span>
                      <b>
                        {job.filamentCm3 != null
                          ? `${Number(job.filamentCm3).toFixed(2)} cm³`
                          : '—'}
                      </b>
                    </div>
                    <div className="history-stat">
                      <span>Print time / part</span>
                      <b>
                        {job.printSeconds != null
                          ? fmtTime(Number(job.printSeconds) / 60)
                          : '—'}
                      </b>
                    </div>
                    <div className="history-stat">
                      <span>Instances</span>
                      <b>{job.quantity}</b>
                    </div>
                  </div>

                  {ready && job.breakdown && (
                    <div className="history-breakdown">
                      {job.breakdown.lines.map((line, index) => (
                        <div className="history-line" key={`${line.label}-${index}`}>
                          <span>{line.label}</span>
                          <span>{fmtMoney(Number(line.amount))}</span>
                        </div>
                      ))}
                      <div className="history-line total">
                        <span>Estimated total</span>
                        <span>{total}</span>
                      </div>
                      <p className="history-note">
                        For {job.quantity} {job.quantity === 1 ? 'part' : 'parts'} ·
                        estimated, not binding
                      </p>
                    </div>
                  )}

                  {isWorking(job.status) && (
                    <p className="history-note">
                      Still calculating. This tab refreshes automatically.
                    </p>
                  )}

                  {job.status === 'FAILED' && (
                    <p className="history-error">
                      {job.errorMessage ?? 'Slicing failed.'}
                    </p>
                  )}
                </div>
              )}
            </article>
          )
        })}
      </div>
    </div>
  )
}
