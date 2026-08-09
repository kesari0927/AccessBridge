# Accessibility test plan

AccessBridge targets WCAG 2.2 Level AA. This checklist separates the current landing page and screenshot-sharing prototype from planned assist-mode coverage. Planned checks remain unchecked until their corresponding features exist and can be tested with TalkBack.

## Landing-page implementation checks

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

## Current Android screenshot-sharing checks

1. Enable TalkBack on an Android emulator or physical Android device and launch AccessBridge.
2. From a screenshot-capable app, share one screenshot to AccessBridge.
3. Confirm TalkBack announces processing status, then the summary headline, source, section labels, and content in that order.
4. Confirm the full “AI text sharing” row toggles the switch and has a single switch role.
5. With AI off, confirm no remote request is made and the on-device summary remains available.
6. With a test Featherless key, turn AI on and confirm the disclosure is announced before requesting a summary.
7. Increase Android font size and display size to their largest settings. Confirm all content remains reachable by scrolling and controls remain usable.

## Assist-mode device checks

These checks require an emulator or physical device:

- [ ] Confirm analysis begins only after the user invokes the accessibility shortcut or button.
- [ ] Confirm AccessBridge opens a temporary accessibility panel over the current app with standard controls, large touch targets, meaningful labels, and logical focus order.
- [ ] Confirm TalkBack reads every question option, including refresh and close, without competing automatic speech.
- [ ] Confirm loading and result changes are announced once and at an appropriate priority.
- [ ] Confirm accessibility-tree information and OCR results are represented as snapshot information rather than guaranteed live data.
- [ ] Confirm “Refresh current information” captures and analyses a new snapshot.
- [ ] Confirm “Close AccessBridge” removes the panel and leaves the underlying application unchanged.
- [ ] Confirm the on-device-only option performs no image upload.
- [ ] Confirm full-image vision analysis has separate, affirmative consent explaining that screenshots may contain location, driver, vehicle, and other personal information.
- [ ] Confirm declining or revoking vision consent preserves the on-device flow.
- [ ] Confirm uncertain roads, markers, distances, and directions are described with uncertainty and never invented.
- [ ] Repeat assist-mode testing on a physical device with TalkBack and large font/display settings.

## Current environment status

- Production TypeScript/Vite build: passed
- Chrome interactive test: pending; no connected Chrome browser session was available
- VoiceOver/NVDA test: pending; requires an assistive-technology session
- TalkBack share-flow test: pending; requires an Android SDK/emulator or physical device
- Assist-mode automated coverage currently includes extracted-text combination, local summary prioritization, and AI prompt constraints. Real-device overlay and TalkBack interaction testing remains required.
