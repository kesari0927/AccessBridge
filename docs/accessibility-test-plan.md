# Accessibility test plan

AccessBridge targets WCAG 2.2 Level AA. This checklist separates verifiable implementation checks from tests that require a real browser, screen reader, emulator, or Android device.

## Implementation checks

- [x] Exactly one page-level `h1` with logical `h2` and `h3` descendants
- [x] Header, navigation, main, section, and footer landmarks
- [x] A keyboard-visible “Skip to main content” link
- [x] Descriptive GitHub links and buttons
- [x] Minimum 48×48px interactive targets
- [x] Strong `:focus-visible` indicator that is not colour-only
- [x] Brand text colours selected for at least 4.5:1 contrast against their backgrounds
- [x] No hover-only content, autoplay, carousel, slider, or timed interaction
- [x] No information represented by colour alone
- [x] `prefers-reduced-motion` and Windows forced-colour handling
- [x] Mobile-first layouts with DOM order matching the visual order
- [x] Text remains live HTML rather than being embedded in images
- [x] Simulated ride information is explicitly labelled as simulated

## Desktop browser checks

Run these checks at 320px, 768px, and a wide desktop viewport, then repeat at 200% browser zoom.

1. Press `Tab` from the top of the page. Confirm the skip link becomes visible first.
2. Activate the skip link and confirm focus moves to the main content.
3. Continue through all navigation and call-to-action links. Confirm every focus indicator is visible and no element is obscured by the sticky header.
4. Confirm there is no horizontal page scrolling at 320px or 200% zoom. A contained navigation row may scroll horizontally on narrow screens.
5. In VoiceOver with Safari or Chrome, or NVDA with Chrome or Firefox, navigate by landmarks and headings. Confirm labels and section order match the visual page.
6. Confirm the CSS bridge/sound-wave marks are ignored and do not create meaningless screen-reader output.

## Android and TalkBack checks

1. Enable TalkBack and launch AccessBridge in Chrome’s Android environment or on a physical Android device.
2. From a screenshot-capable app, share one screenshot to AccessBridge.
3. Confirm TalkBack announces processing status, then the summary headline, source, section labels, and content in that order.
4. Confirm the full “AI text sharing” row toggles the switch and has a single switch role.
5. With AI off, confirm no remote request is made and the on-device summary remains available.
6. With a test Featherless key, turn AI on and confirm the disclosure is announced before requesting a summary.
7. Increase Android font size and display size to their largest settings. Confirm all content remains reachable by scrolling and controls remain usable.

## Current environment status

- Production TypeScript/Vite build: passed
- Chrome interactive test: pending; no connected Chrome browser session was available
- VoiceOver/NVDA test: pending; requires an assistive-technology session
- TalkBack share-flow test: pending; requires an Android SDK/emulator or physical device
