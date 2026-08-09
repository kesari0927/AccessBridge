# AccessBridge product vision

This document is the source of truth for AccessBridge product decisions and future development. It distinguishes the working initial scaffold from planned assist-mode capabilities so that prototype copy and implementation status remain accurate.

## Problem

People who are blind or visually impaired often rely on Android TalkBack, but applications such as Grab can contain graphical maps and rapidly changing visual information that TalkBack may not explain clearly. TalkBack may announce surrounding controls and exposed text while leaving important visual context unclear—for example, where a driver marker appears, which visible road it is near, whether it appears to be approaching the pickup point, or what other important information is visible on the screen.

## Product idea

AccessBridge is an Android accessibility companion. It complements TalkBack rather than replacing it, and it is not affiliated with Grab.

The planned assist-mode workflow is explicitly initiated by the user:

1. While using an application such as Grab, the user activates AccessBridge through Android's accessibility shortcut or accessibility button.
2. Only after that activation, AccessBridge captures a snapshot of the current screen.
3. AccessBridge opens a temporary, TalkBack-friendly accessibility panel over the current app.
4. The activity asks, “What do you want to know?”

The menu should offer:

- Where is my driver?
- Arrival time and distance
- Driver and vehicle details
- Where is my pickup point?
- Read the entire screen
- Refresh current information
- Close AccessBridge

The user-invoked panel is an accessibility overlay that exists only for the current request. It is not a persistent floating bubble, and closing it leaves the underlying application in place. Manual screenshot sharing remains available as a fallback.

## Analysis approach

AccessBridge is intended to combine three sources of information:

1. Accessibility-tree information already exposed by the active Android application.
2. Bundled, on-device ML Kit OCR for visible text such as ETA, road names, pickup details, and vehicle information.
3. Optional Featherless multimodal vision analysis for graphical information such as vehicle markers, pickup markers, visible road labels, and their approximate relationship.

Full-image vision analysis is separate from the existing text-only Featherless summary and requires its own explicit consent.

## Answer style

Answers should be short, focused, and easy for TalkBack to read. AccessBridge should let TalkBack announce the result instead of adding competing automatic speech.

An example answer is:

> Closest visible road: Jalan Ampang. The vehicle marker appears near the pickup point. Estimated arrival shown: three minutes. This describes one screenshot and may be inaccurate.

Every answer must distinguish visible or inferred snapshot information from guaranteed live data.

## Important limitations

- AccessBridge does not have access to Grab's private driver coordinates, internal APIs, or private account data.
- It must not claim to provide guaranteed live GPS tracking.
- It analyses a user-requested snapshot of the visible screen. “Refresh” means capturing and analysing another snapshot.
- A street name may be reported only when it is visible or can be identified with reasonable confidence.
- Uncertainty must be communicated clearly.
- AccessBridge must never invent coordinates, distances, directions, street names, vehicle positions, or other details.
- Results may be incomplete or inaccurate because they describe a single visible snapshot.

## Accessibility requirements

- The question-selection menu must work properly with TalkBack.
- Use standard Jetpack Compose controls and large touch targets.
- Maintain a logical focus and reading order with clear accessibility labels.
- Announce loading and result changes through accessible UI state.
- Let TalkBack read results instead of adding separate automatic speech.
- Closing AccessBridge should remove the temporary panel without changing the underlying application.
- Keep manual screenshot sharing as a fallback.
- Use only a temporary, user-invoked accessibility overlay; do not add a persistent floating overlay.
- Perform real-device TalkBack testing before claiming assist mode is accessible or complete.

## Privacy requirements

- Analysis begins only after an explicit user request.
- Prefer on-device accessibility-tree data and OCR.
- Provide an on-device-only option.
- Full-image vision analysis requires separate, explicit consent.
- Before vision analysis, explain that screenshots may contain location, driver, vehicle, and other personal information.
- Do not permanently store screenshots.
- Do not commit API keys or other credentials.
- Clearly identify what data will leave the device before it is sent.

## Current implementation status

The initial scaffold currently includes:

- A React landing page.
- An initial Kotlin and Jetpack Compose Android application.
- Manual screenshot sharing into AccessBridge through the Android share sheet.
- Bundled ML Kit on-device OCR.
- Local rule-based text summaries.
- Optional, consent-gated Featherless text summaries that send recognized text rather than the screenshot.
- An initial Assist Mode foundation with a manually enabled `AccessibilityService`.
- Accessibility shortcut/button activation, accessibility-tree text extraction, and one-shot screenshots on Android 11 and newer.
- A temporary accessibility panel for summarizing, reading visible text, refreshing, or closing without leaving the current app.
- In-memory-only screenshot handoff that combines accessibility text with bundled ML Kit OCR.
- Architecture and accessibility-testing documentation.

The following capabilities are planned and are **not yet implemented**:

- Featherless image or multimodal vision analysis.
- Map-marker interpretation.
- Ride selection, automatic clicking or booking, persistent overlays, and continuous monitoring.

The Android project completes its Gradle build and local unit tests, but has not completed a real-device TalkBack test. Documentation, demos, and presentations must not claim Assist Mode is accessibility-verified until that testing is complete.

## Development order

1. Compile and run the existing Android application.
2. Test screenshot sharing, OCR, and TalkBack.
3. Fix critical problems.
4. Merge the initial scaffold.
5. Create a separate `feature/assist-mode` branch.
6. Add the `AccessibilityService` and user-triggered screen capture.
7. Add the accessible question-selection menu.
8. Add accessibility-tree extraction.
9. Add optional Featherless vision analysis with separate consent.
10. Perform real-device TalkBack testing.
11. Prepare a controlled hackathon demonstration.

## Initial Prototype

The intial AccessBridge prototype successfully works as follows:

1. **Take a screenshot**  
   The user takes a screenshot of content they want to understand, such as a map, settings screen, webpage, or another app.

2. **Share the screenshot to AccessBridge**  
   The screenshot is shared to AccessBridge using Android's standard Share menu. AccessBridge is registered as a share target and receives the image.

3. **On-device text recognition (OCR)**  
   AccessBridge analyzes the screenshot locally and extracts visible text without needing to upload the screenshot to an external service.

4. **Accessible results**  
   The recognized information is organized into an accessible result. Rather than requiring the user to inspect the original image, AccessBridge presents the detected information as readable text under sections such as **Screen information** and **More details**.

5. **TalkBack support**  
   The result works with Android TalkBack. When TalkBack is enabled, a blind or low-vision user can navigate to the recognized-information section and have the extracted content read aloud.

### Example

The prototype has been tested with a Google Maps directions screenshot. AccessBridge successfully extracted information including locations, journey duration, distance, route information, and other visible text.

TalkBack was then able to read the resulting information aloud.

### Current Workflow

**Screenshot → Share to AccessBridge → On-device OCR → Accessible text → TalkBack**
