const githubUrl = 'https://github.com/kesari0927/AccessBridge'

const steps = [
  {
    number: '01',
    title: 'Share a screenshot',
    description:
      'Choose AccessBridge from Android’s share sheet. No account or app connection is needed.',
  },
  {
    number: '02',
    title: 'Extract text on-device',
    description:
      'Bundled ML Kit text recognition reads visible words without uploading the screenshot.',
  },
  {
    number: '03',
    title: 'Receive a clear summary',
    description:
      'AccessBridge organizes the result into structured text designed to work well with TalkBack.',
  },
]

const technologies = [
  'Android',
  'Kotlin',
  'Jetpack Compose',
  'ML Kit',
  'Featherless AI',
  'DevSwarm',
  'OpenAI Codex',
]

function BrandMark() {
  return (
    <span className="brand-mark" aria-hidden="true">
      <span className="bridge-deck" />
      <span className="bridge-point bridge-point-left" />
      <span className="bridge-point bridge-point-right" />
      <span className="sound-wave sound-wave-one" />
      <span className="sound-wave sound-wave-two" />
    </span>
  )
}

function SectionHeading({
  eyebrow,
  title,
  description,
}: {
  eyebrow: string
  title: string
  description?: string
}) {
  return (
    <div className="section-heading">
      <p className="eyebrow">{eyebrow}</p>
      <h2>{title}</h2>
      {description ? <p>{description}</p> : null}
    </div>
  )
}

function App() {
  return (
    <>
      <a className="skip-link" href="#main-content">
        Skip to main content
      </a>

      <header className="site-header">
        <div className="page-shell header-inner">
          <a className="brand-link" href="#top" aria-label="AccessBridge home">
            <BrandMark />
            <span>AccessBridge</span>
          </a>
          <nav aria-label="Primary navigation">
            <ul className="nav-list">
              <li>
                <a href="#how-it-works">How it works</a>
              </li>
              <li>
                <a href="#demo">Example</a>
              </li>
              <li>
                <a href="#privacy">Privacy</a>
              </li>
              <li>
                <a className="nav-github" href={githubUrl}>
                  View on GitHub
                </a>
              </li>
            </ul>
          </nav>
        </div>
      </header>

      <main id="main-content">
        <section className="hero" id="top" aria-labelledby="hero-title">
          <div className="page-shell hero-grid">
            <div className="hero-copy">
              <p className="eyebrow">A clearer path through visual interfaces</p>
              <h1 id="hero-title">When apps go silent, AccessBridge speaks.</h1>
              <p className="hero-lede">
                The current prototype turns a screenshot you choose to share into calm,
                structured text that TalkBack can communicate clearly.
              </p>
              <div className="hero-actions">
                <a className="button button-primary" href={githubUrl}>
                  View AccessBridge on GitHub
                </a>
                <a className="button button-secondary" href="#how-it-works">
                  Learn how it works
                </a>
              </div>
              <p className="prototype-note">
                Current prototype: manual sharing · OCR works on-device · No account needed
              </p>
            </div>

            <figure className="phone-figure">
              <div className="phone-frame">
                <div className="phone-speaker" aria-hidden="true" />
                <div className="phone-content">
                  <div className="phone-topline">
                    <BrandMark />
                    <strong>AccessBridge</strong>
                  </div>
                  <p className="phone-label">SCREEN SUMMARY</p>
                  <h2>Your ride is arriving soon</h2>
                  <dl className="phone-details">
                    <div>
                      <dt>Status</dt>
                      <dd>Driver approaching</dd>
                    </div>
                    <div>
                      <dt>Estimated arrival</dt>
                      <dd>3 minutes</dd>
                    </div>
                    <div>
                      <dt>Pickup</dt>
                      <dd>North entrance, Gate B</dd>
                    </div>
                  </dl>
                  <div className="spoken-status">
                    <span aria-hidden="true">)))</span>
                    <span>Ready for TalkBack</span>
                  </div>
                </div>
              </div>
              <figcaption>
                Simulated placeholder preview of an organized AccessBridge summary.
              </figcaption>
            </figure>
          </div>
        </section>

        <section className="problem-section" aria-labelledby="problem-title">
          <div className="page-shell narrow-grid">
            <div>
              <p className="eyebrow">The problem</p>
              <h2 id="problem-title">Important information can be visible, but not announced.</h2>
            </div>
            <div className="problem-copy">
              <p>
                Graphical layouts, map-heavy screens, and missing accessibility labels can
                leave TalkBack with too little context—or nothing useful to say.
              </p>
              <p>
                The current fallback offers another route: the person shares a screenshot,
                then receives visible text in a practical reading order they can use.
              </p>
            </div>
          </div>
        </section>

        <section className="section" id="how-it-works" aria-labelledby="how-title">
          <div className="page-shell">
            <SectionHeading
              eyebrow="How it works"
              title="Current prototype: three deliberate steps."
              description="Today, AccessBridge works from a screenshot you manually share. Planned assist mode will use user-activated screen snapshots and accessibility data; those features are not implemented yet."
            />
            <ol className="steps-grid">
              {steps.map((step) => (
                <li className="step-card" key={step.number}>
                  <span className="step-number" aria-hidden="true">
                    {step.number}
                  </span>
                  <h3>{step.title}</h3>
                  <p>{step.description}</p>
                </li>
              ))}
            </ol>
          </div>
        </section>

        <section className="section section-muted" id="demo" aria-labelledby="demo-title">
          <div className="page-shell">
            <SectionHeading
              eyebrow="From clutter to clarity"
              title="The same information, organized for listening."
              description="This example uses fictional screen text to demonstrate the transformation."
            />
            <div className="transform-grid">
              <article className="text-panel text-panel-messy">
                <div className="panel-header">
                  <span className="status-chip status-chip-neutral">Before</span>
                  <h3>Raw recognized text</h3>
                </div>
                <p className="messy-text">
                  3 min 1.2 km Suresh KA 05 MQ 4821 Silver Swift Pickup Gate B
                  driver arriving call chat promo safety route
                </p>
              </article>
              <div className="transform-arrow" aria-hidden="true">
                <span>→</span>
              </div>
              <article className="text-panel text-panel-clear">
                <div className="panel-header">
                  <span className="status-chip">After</span>
                  <h3>AccessBridge summary</h3>
                </div>
                <dl className="summary-list">
                  <div>
                    <dt>Status</dt>
                    <dd>Your driver is arriving in about 3 minutes.</dd>
                  </div>
                  <div>
                    <dt>Pickup point</dt>
                    <dd>Gate B.</dd>
                  </div>
                  <div>
                    <dt>Vehicle</dt>
                    <dd>Silver Swift, registration KA 05 MQ 4821.</dd>
                  </div>
                </dl>
              </article>
            </div>
          </div>
        </section>

        <section className="section" aria-labelledby="ride-title">
          <div className="page-shell ride-grid">
            <div>
              <p className="eyebrow">Accessible ride-status demonstration</p>
              <h2 id="ride-title">Ride details that follow a useful reading order.</h2>
              <p className="ride-intro">
                The demonstration groups time-sensitive details first, then location and
                vehicle information. It never presents colour as the only indicator.
              </p>
              <p className="simulation-label">
                <strong>Simulated data:</strong> this is a fictional example, not a live ride.
              </p>
            </div>
            <article className="ride-card" aria-label="Simulated accessible ride status">
              <div className="ride-status">
                <span className="status-symbol" aria-hidden="true">✓</span>
                <div>
                  <span>Status</span>
                  <strong>Driver approaching pickup</strong>
                </div>
              </div>
              <dl className="ride-details">
                <div>
                  <dt>Estimated arrival</dt>
                  <dd>3 minutes</dd>
                </div>
                <div>
                  <dt>Distance</dt>
                  <dd>1.2 kilometres away</dd>
                </div>
                <div>
                  <dt>Pickup point</dt>
                  <dd>North entrance, Gate B</dd>
                </div>
                <div>
                  <dt>Driver</dt>
                  <dd>Suresh</dd>
                </div>
                <div>
                  <dt>Vehicle</dt>
                  <dd>Silver Swift</dd>
                </div>
                <div>
                  <dt>Registration</dt>
                  <dd>KA 05 MQ 4821</dd>
                </div>
              </dl>
            </article>
          </div>
        </section>

        <section className="section trust-section" id="privacy" aria-labelledby="privacy-title">
          <div className="page-shell trust-grid">
            <article className="trust-card trust-card-positive">
              <p className="eyebrow">Privacy by default</p>
              <h2 id="privacy-title">Current text analysis stays on-device.</h2>
              <ul className="check-list">
                <li>Bundled ML Kit performs screenshot OCR on-device.</li>
                <li>The current optional AI text summary is off until you choose it.</li>
                <li>In today's flow, only recognized text is sent—not the image.</li>
                <li>Planned image analysis is not implemented and will require separate consent.</li>
                <li>No AccessBridge account, database, or usage profile is created.</li>
              </ul>
            </article>
            <article className="trust-card">
              <p className="eyebrow">Clear limitations</p>
              <h2>Useful assistance, honest boundaries.</h2>
              <ul className="boundary-list">
                <li>Cannot provide guaranteed live GPS tracking.</li>
                <li>Cannot control Grab or perform actions inside another app.</li>
                <li>Planned marker analysis will describe one user-requested snapshot.</li>
                <li>Cannot access private Grab account or ride data.</li>
              </ul>
            </article>
          </div>
        </section>

        <section className="section" aria-labelledby="technology-title">
          <div className="page-shell technology-grid">
            <div>
              <p className="eyebrow">Technology and sponsor tools</p>
              <h2 id="technology-title">A focused scaffold with a clear next step.</h2>
              <p>
                Manual sharing and text summaries work in the current scaffold. Assist-mode
                capture, its question menu, accessibility-tree reading, and image analysis
                remain planned and must be tested before a hackathon demonstration.
              </p>
            </div>
            <ul className="technology-list" aria-label="Technologies used">
              {technologies.map((technology) => (
                <li key={technology}>{technology}</li>
              ))}
            </ul>
          </div>
        </section>

        <section className="section team-section" aria-labelledby="team-title">
          <div className="page-shell team-inner">
            <div>
              <p className="eyebrow">Team AccessBridge</p>
              <h2 id="team-title">Designed with access, agency, and usefulness in mind.</h2>
            </div>
            <p>
              AccessBridge is a hackathon collaboration exploring how familiar Android
              tools can make visually complex screens easier to understand. We welcome
              practical feedback from screen-reader users and accessibility practitioners.
            </p>
          </div>
        </section>

        <section className="final-cta" aria-labelledby="cta-title">
          <div className="page-shell cta-inner">
            <BrandMark />
            <p className="eyebrow">Help build a clearer bridge</p>
            <h2 id="cta-title">Explore the prototype and follow the demo.</h2>
            <p>
              Review the source, try the workflow, and share what would make AccessBridge
              more useful in everyday navigation.
            </p>
            <a className="button button-light" href={githubUrl}>
              Open the AccessBridge GitHub repository
            </a>
          </div>
        </section>
      </main>

      <footer className="site-footer">
        <div className="page-shell footer-inner">
          <p>© 2026 AccessBridge</p>
          <p>Hackathon prototype—not affiliated with Grab.</p>
        </div>
      </footer>
    </>
  )
}

export default App
