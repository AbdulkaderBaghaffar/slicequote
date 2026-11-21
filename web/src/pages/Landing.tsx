import { useNavigate } from 'react-router-dom'
import { Brand } from '../components/Brand'
import { ThemeToggle } from '../components/ThemeToggle'
import { ArrowRightIcon } from '../components/Icons'
import { LANDING_MATERIAL_ORDER, MATERIAL_PRESETS } from '../data/materials'
import { useAuth } from '../theme/AuthProvider'

export function Landing() {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const quotePath = isAuthenticated ? '/quote' : '/login'

  return (
    <section className="screen" aria-labelledby="landing-title">
      <div className="landing-hero">
        <header className="container nav">
          <Brand />
          <nav className="nav-links" aria-label="Primary">
            <a href="#how">How it works</a>
            <a href="#materials">Materials</a>
            <a href="#pricing">Pricing</a>
          </nav>
          <div className="nav-cta">
            <ThemeToggle />
            <a className="btn btn-dark-ghost" href="#how">
              Learn more
            </a>
            <button className="btn btn-on-dark" type="button" onClick={() => navigate(quotePath)}>
              Get a quote
            </button>
          </div>
        </header>

        <div className="container hero">
          <div>
            <span className="hero-eyebrow">
              <span className="dot" aria-hidden="true" />
              Instant additive manufacturing quotes
            </span>
            <h1 id="landing-title">
              Price your part <span className="accent">before you print it.</span>
            </h1>
            <p className="hero-sub">
              Upload a spec, pick a material, and watch a live price form in seconds. No
              back-and-forth, no waiting on a human estimate.
            </p>
            <div className="hero-actions">
              <button className="btn btn-primary" type="button" onClick={() => navigate(quotePath)}>
                Get a quote
                <ArrowRightIcon />
              </button>
              <a className="btn btn-dark-ghost" href="#how">
                See how it works
              </a>
            </div>
            <p className="hero-note">Free to preview · pricing is a live demo estimate</p>
          </div>

          <div className="hero-visual" aria-hidden="true">
            <div className="part-stage-hero">
              <img
                className="hero-benchy"
                src="/models/benchy-hero.webp"
                width="915"
                height="868"
                alt=""
              />
              <span className="spec-tag t1">
                <b>PLA</b> · lime
              </span>
              <span className="spec-tag t2">
                <b>60 × 31 × 48</b> mm
              </span>
            </div>
          </div>
        </div>
      </div>

      <div className="trust-strip">
        <div className="container trust-grid">
          <div className="trust-item">
            <span className="num">4</span>
            <span className="lbl">Materials available</span>
          </div>
          <div className="trust-item">
            <span className="num">48h</span>
            <span className="lbl">Typical turnaround</span>
          </div>
          <div className="trust-item">
            <span className="num">±0.2mm</span>
            <span className="lbl">Dimensional tolerance</span>
          </div>
        </div>
      </div>

      <section className="container section" id="how">
        <div className="section-head">
          <span className="kicker">How it works</span>
          <h2>From part to price in three steps</h2>
        </div>
        <div className="steps">
          <div className="step">
            <span className="step-num">01</span>
            <h3>Specify your part</h3>
            <p>Set the bounding dimensions, infill, and quantity for the geometry you need printed.</p>
          </div>
          <div className="step">
            <span className="step-num">02</span>
            <h3>Choose a material</h3>
            <p>Pick from PLA, PETG, TPU, or ABS and a finish color that matches your use case.</p>
          </div>
          <div className="step">
            <span className="step-num">03</span>
            <h3>Get your price</h3>
            <p>Watch material cost, weight, and print time recompute instantly — then send the quote.</p>
          </div>
        </div>
      </section>

      <section className="container section" id="materials">
        <div className="section-head">
          <span className="kicker">Materials</span>
          <h2>Engineered for the job</h2>
        </div>
        <div className="materials" id="materialsGrid">
          {LANDING_MATERIAL_ORDER.map((key) => {
            const material = MATERIAL_PRESETS[key]
            return (
              <div className="mat-card" key={key}>
                <span
                  className="mat-swatch"
                  style={{ background: material.colors[0] }}
                  aria-hidden="true"
                />
                <div>
                  <h3>{material.name}</h3>
                  <p className="mat-desc">{material.tag}</p>
                </div>
                <span className="mat-meta">{material.density} g/cm³</span>
              </div>
            )
          })}
        </div>
      </section>

      <section className="cta-band" id="pricing">
        <div className="container cta-inner">
          <div>
            <h2>Ready to see your number?</h2>
            <p>Sign in and build a quote in under a minute.</p>
          </div>
          <button className="btn btn-primary" type="button" onClick={() => navigate(quotePath)}>
            Start a quote
            <ArrowRightIcon />
          </button>
        </div>
      </section>

      <footer className="footer">
        <div className="container footer-inner">
          <Brand />
          <small>Demo prototype — quotes are illustrative, not a binding offer.</small>
        </div>
      </footer>
    </section>
  )
}
