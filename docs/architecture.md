# Architecture

AccessBridge keeps the public website and Android prototype independent so each can be built and shipped without coupling to a backend.

## Current Android flow

The implemented scaffold uses manual screenshot sharing as its input and keeps the image on-device:

```text
Android share intent
        ↓
Shared image URI
        ↓
Bundled ML Kit text recognition (on-device)
        ↓
Local structured summary
        ↓ optional, explicit opt-in
Recognized text only → Featherless AI
```

The Android source follows a small clean-architecture split:

- `presentation`: Compose UI and screen state
- `domain`: models, repository contracts, and use cases
- `data`: ML Kit OCR and local/optional remote summary implementations

The current scaffold contains no `AccessibilityService`, active-window capture, accessibility-tree extraction, question-selection menu, image analysis, or map-marker interpretation. These are implementation-status boundaries, not permanent exclusions from the product vision.

## Planned assist-mode flow

The planned workflow remains explicitly user-triggered:

```text
Accessibility shortcut or button
        ↓ user requests analysis
Accessibility-tree snapshot + current-screen snapshot
        ↓
On-device accessibility data and bundled ML Kit OCR
        ↓
TalkBack-friendly question menu and focused answer
        ↓ optional, separate explicit image consent
Current screenshot → Featherless multimodal vision analysis
```

The full-image path must explain that a screenshot may contain location, driver, vehicle, or other personal information. An on-device-only path must remain available. AccessBridge will describe one snapshot at a time; refresh means requesting another snapshot, not starting continuous tracking.

There is intentionally no authentication, persistent screenshot storage, AccessBridge database, application backend, Grab control, private Grab integration, or guaranteed live GPS tracking.

## Web flow

The landing page is a static Vite build. It contains no API requests, cookies, analytics, authentication, or runtime data store. Examples and ride details are labelled as simulated data, and product copy must distinguish implemented screenshot sharing from planned assist-mode features.
