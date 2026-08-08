# AccessBridge product vision

This document is the source of truth for AccessBridge product decisions and future development. It distinguishes the working initial scaffold from planned assist-mode capabilities so that prototype copy and implementation status remain accurate.

## Problem

People who are blind or visually impaired often rely on Android TalkBack, but applications such as Grab can contain graphical maps and rapidly changing visual information that TalkBack may not explain clearly. TalkBack may announce surrounding controls and exposed text while leaving important visual context unclear—for example, where a driver marker appears, which visible road it is near, whether it appears to be approaching the pickup point, or what other important information is visible on the screen.

## Product idea

AccessBridge is an Android accessibility companion. It complements TalkBack rather than replacing it, and it is not affiliated with Grab.

The planned assist-mode workflow is explicitly initiated by the user:

1. While using an application such as Grab, the user activates AccessBridge through Android's accessibility shortcut or accessibility button.
2. Only after that activation, AccessBridge captures a snapshot of the current screen.
3. AccessBridge opens a simple, full-screen, TalkBack-friendly Compose activity.
4. The activity asks, “What do you want to know?”

The menu should offer:

- Where is my driver?
- Arrival time and distance
- Driver and vehicle details
- Where is my pickup point?
- Read the entire screen
- Refresh current information
- Close AccessBridge

For the hackathon prototype, a normal full-screen activity is preferred to a floating overlay. Manual screenshot sharing remains available as a fallback.

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
- Closing AccessBridge should return the user to the previous application.
- Keep manual screenshot sharing as a fallback.
- Prefer a normal full-screen Compose activity to a floating overlay for the hackathon prototype.
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
- Architecture and accessibility-testing documentation.

The following capabilities are planned and are **not yet implemented**:

- Android `AccessibilityService`.
- Accessibility shortcut or accessibility-button activation.
- Reading the active application's accessibility tree.
- Capturing the active window through the accessibility service.
- The question-selection menu.
- Featherless image or multimodal vision analysis.
- Map-marker interpretation.
- Refresh and return-to-previous-application workflow.

The Android project has not yet completed a full Android Studio build or real-device TalkBack test. Documentation, demos, and presentations must not claim that any planned capability already works.

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
