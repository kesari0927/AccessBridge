# Architecture

AccessBridge keeps the public website and Android prototype independent so each can be built and shipped without coupling to a backend.

## Android flow

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

There is intentionally no persistence, authentication, automatic screen control, map integration, or application backend.

## Web flow

The landing page is a static Vite build. It contains no API requests, cookies, analytics, authentication, or runtime data store. Examples and ride details are labelled as simulated data.
