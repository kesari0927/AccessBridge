# AccessBridge privacy policy

_Last updated: [DATE]_

AccessBridge is an Android accessibility tool that helps blind and low-vision people understand screens that TalkBack cannot fully describe. This policy explains what the app reads, what it does with that information, and what leaves your device.

## Summary

- AccessBridge reads the screen **only when you ask it to**.
- Screen content is processed **on your device** and is **never stored**.
- AccessBridge has no accounts, no advertising, no analytics of its own, and no servers.

## What AccessBridge reads

**Screenshots you share.** When you share an image to AccessBridge from another app, AccessBridge reads the visible text in that image using on-device text recognition.

**Assist Mode (Accessibility Service).** If you turn on AccessBridge Assist Mode in Android's accessibility settings, AccessBridge uses Android's AccessibilityService API. It acts only when you press the accessibility button or shortcut. At that moment it:

1. reads the text and content descriptions that the app on screen exposes to accessibility services, and
2. on Android 11 and newer, takes one screenshot of the current screen.

AccessBridge does not monitor your activity in the background, record which apps you use, or tap, type, or perform any action inside other apps.

## How the information is used

The text and screenshot are used only to produce the spoken summary or the "read all visible text" result you asked for. The screenshot is kept in memory only while text recognition runs, then discarded. Nothing is written to storage, and the result is cleared when you close AccessBridge.

## What leaves your device

AccessBridge itself sends no screen content off your device.

Text recognition uses Google ML Kit, which runs on your device. Google ML Kit may send limited, non-content diagnostic and performance information to Google (for example, device model, OS version, and API performance metrics). It does not send your screenshots or the recognized text. See Google's ML Kit data disclosure: https://developers.google.com/ml-kit/android-data-disclosure

## Data sharing and sale

AccessBridge does not sell, share, or transfer your data to third parties, and does not use it for advertising.

## Children

AccessBridge is not directed at children under 13.

## Changes

If a future version adds a feature that sends any data off your device (for example, optional AI summaries), it will be off by default, will ask for your explicit consent first, and this policy will be updated before that version is released.

## Contact

Questions about this policy: [CONTACT EMAIL]
