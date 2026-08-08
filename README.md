# AccessBridge

> When apps go silent, AccessBridge speaks.

AccessBridge is a hackathon prototype that turns screenshots of visually complex or poorly labelled app screens into structured text that screen readers can communicate clearly.

## Repository layout

```text
AccessBridge/
├── android/        Kotlin and Jetpack Compose Android app
├── web/            Vite, React, TypeScript, and Tailwind landing page
└── docs/           Product and architecture notes
```

## Landing page

The website is a static single-page application with no backend or persistent data.

```bash
cd web
bun install
bun run dev
```

Run `bun run build` to create a production build in `web/dist`.

## Android prototype

Open the `android` directory in Android Studio. The app targets Android 8.0+ and accepts shared screenshots through the Android share sheet. OCR uses ML Kit's bundled on-device text recognizer.

The optional Featherless AI summary is disabled unless the user opts in. For local development, expose an API key and optional model before building:

```bash
export FEATHERLESS_API_KEY="your-key"
export FEATHERLESS_MODEL="your-model-id"
```

Only recognized text—not the screenshot—is sent when AI summaries are enabled. Build-time API keys are suitable for a prototype only and must not be used for a production release.

## Prototype boundaries

AccessBridge does not control other apps, track vehicles, access private Grab data, or guarantee accurate interpretation of every map or screenshot. It has no authentication, database, or backend and is not affiliated with Grab.

## Accessibility verification

The implementation targets WCAG 2.2 AA and includes semantic landmarks, a skip link, visible focus states, 48×48px minimum controls, reduced-motion handling, and a source order that matches the visual order. See [`docs/accessibility-test-plan.md`](docs/accessibility-test-plan.md) for the manual keyboard and assistive-technology test matrix.
