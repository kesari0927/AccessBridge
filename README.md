# AccessBridge

> When apps go silent, AccessBridge speaks.

AccessBridge is an Android accessibility-companion prototype. The current application turns manually shared screenshots of visually complex or poorly labelled screens into structured text that TalkBack can communicate clearly. The planned assist mode will add user-triggered screen analysis through Android accessibility features.

[`docs/product-vision.md`](docs/product-vision.md) is the source of truth for the product direction, privacy boundaries, implementation status, and development order.

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

## Current Android prototype

Open the `android` directory in Android Studio. The app targets Android 8.0+ and accepts manually shared screenshots through the Android share sheet. This share flow is the current implementation and will remain available as a fallback. OCR uses ML Kit's bundled on-device text recognizer.

The optional Featherless AI summary is disabled unless the user opts in. For local development, expose an API key and optional model before building:

```bash
export FEATHERLESS_API_KEY="your-key"
export FEATHERLESS_MODEL="your-model-id"
```

In the current text-summary flow, only recognized text—not the screenshot—is sent when AI summaries are enabled. Build-time API keys are suitable for a prototype only and must not be used for a production release.

## Planned assist mode

The planned assist mode will be activated explicitly through Android's accessibility shortcut or accessibility button. It is intended to combine accessibility-tree information, on-device OCR, and optional full-image Featherless vision analysis. Image analysis is not implemented and will require separate consent that explains screenshots may contain location, driver, vehicle, and other personal information.

The `AccessibilityService`, user-triggered active-window capture, question-selection menu, accessibility-tree extraction, marker interpretation, refresh, and return-to-previous-app workflow are all planned. They must not be presented as working features.

## Prototype boundaries

AccessBridge does not control Grab, access private Grab data, or provide guaranteed live GPS tracking. Planned assist mode will describe a user-requested snapshot using information visible on screen or exposed through the accessibility tree; it will not gain access to private coordinates or internal APIs. AccessBridge has no authentication, database, or backend and is not affiliated with Grab.

## Accessibility verification

The landing page targets WCAG 2.2 AA and includes semantic landmarks, a skip link, visible focus states, 48×48px minimum controls, reduced-motion handling, and a source order that matches the visual order. The Android interface uses standard Compose controls, but its full build and real-device TalkBack test are still pending. See [`docs/accessibility-test-plan.md`](docs/accessibility-test-plan.md) for current and planned test coverage.
