# Publishing AccessBridge on Google Play

A step-by-step checklist from source code to a live Play Store listing. Do the steps in order.

## 0. Before anything else: rotate the Featherless key

Earlier debug APKs embedded the Featherless API key, and anyone holding one of those APK files can extract it. Revoke that key in the Featherless dashboard and create a new one. Put the new key only in `android/local.properties` (gitignored). Release builds no longer include any key — see `android/app/build.gradle.kts`.

## 1. One-time setup

1. **Android SDK.** Open the `android/` folder in Android Studio and accept the SDK setup prompts. Install **Android SDK Platform 36** from *Settings → Languages & Frameworks → Android SDK*.
2. **Play Console account.** Register at https://play.google.com/console ($25 one-time fee, plus identity verification).
3. **Testing requirement for new personal accounts.** Personal developer accounts must run a **closed test with at least 12 testers opted in for 14 continuous days** before they can apply for production access. Start recruiting testers early. Blind and low-vision TalkBack users are the best testers for this app.

## 2. Create your upload key (once, keep it forever)

Play App Signing holds the real app-signing key; you sign uploads with an *upload key*. From the `android/` folder:

```bash
keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Then create `android/keystore.properties` (gitignored — never commit it):

```properties
storeFile=upload-keystore.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=upload
keyPassword=YOUR_KEY_PASSWORD
```

Back up the `.jks` file and both passwords somewhere safe, such as a password manager. Losing them means requesting an upload-key reset from Google.

## 3. Build the release bundle

Google Play takes an Android App Bundle (`.aab`), not an APK. Play splits it per device, so the 55 MB debug APK becomes a much smaller download (most of that size is ML Kit's native library for four CPU types).

```bash
./gradlew bundleRelease
```

Output: `android/app/build/outputs/bundle/release/app-release.aab`

For each new upload, increase `versionCode` (and usually `versionName`) in `android/app/build.gradle.kts`.

## 4. Create the app in Play Console

*Create app* → name **AccessBridge**, type **App**, **Free**.

### App content (Policy section)

| Item | What to enter |
| --- | --- |
| Privacy policy | Public URL of `docs/privacy-policy.md` (for example, its GitHub page once the repo is public, or a page on the landing site). Fill in the date and contact email first. |
| App access | All functionality available without login. Add instructions: "Enable AccessBridge Assist Mode in Settings → Accessibility, then press the accessibility button." |
| Ads | No ads |
| Content rating | Complete the questionnaire. Category: Utility/Productivity. No violence, user-generated content, or similar. |
| Target audience | 13+ or 18+. Not designed for children. |
| Data safety | See below |
| **Accessibility API declaration** | See below. This is the most important review item. |

### Data safety form

AccessBridge's own code collects and shares nothing: screen text and screenshots are processed on-device and never transmitted (release builds have no AI feature). Google ML Kit, however, sends some diagnostic data to Google. Answer the ML Kit portion using Google's guidance: https://developers.google.com/ml-kit/android-data-disclosure

- Data encrypted in transit: Yes (ML Kit diagnostics)
- Users can request deletion: Not applicable (no data stored)

### Accessibility API declaration

Play reviews every app that uses `AccessibilityService`. AccessBridge declares `android:isAccessibilityTool="true"` because it is designed for people with disabilities. Suggested answers:

**Is your app an accessibility tool?** Yes.

**Describe how the app uses the AccessibilityService API:**

> AccessBridge helps blind and low-vision TalkBack users understand screens that are visually complex or poorly labelled, such as maps and ride-hailing apps. When the user presses the Android accessibility button or shortcut, AccessBridge reads the active window's text and content descriptions and, on Android 11 and newer, takes a single screenshot. It runs on-device text recognition on that screenshot, then shows an accessibility overlay that summarizes the screen or reads all visible text through TalkBack. AccessBridge only acts on explicit user activation. It ignores accessibility events, performs no actions or gestures in other apps, does not monitor activity, and never stores or transmits screen content.

**Video:** Reviewers may ask for a short screen recording. Record one showing: enabling the service in Settings → Accessibility, opening another app, pressing the accessibility button, the overlay appearing, *Summarize* and *Read all*, then *Close*. Leave TalkBack on so reviewers can hear the accessibility value.

## 5. Store listing

**App name:** AccessBridge

**Short description (80 characters max):**

> Understand screens TalkBack can't read: on-device screen text for blind users.

**Full description:**

> AccessBridge is an accessibility companion for blind and low-vision people who use TalkBack.
>
> Some apps — maps, ride-hailing, delivery tracking — show important information that TalkBack cannot fully describe. AccessBridge reads what is visible on screen and turns it into short, structured text that TalkBack can speak clearly.
>
> HOW IT WORKS
> • Assist Mode: press Android's accessibility button or shortcut in any app. AccessBridge reads that screen and opens a simple panel: Summarize, Read all visible text, Refresh, Close.
> • Share a screenshot: share any image to AccessBridge to hear its text organized for TalkBack.
>
> PRIVATE BY DESIGN
> • Works only when you ask. It never monitors you in the background.
> • Text recognition runs on your device. Screenshots are never saved or uploaded.
> • No account, no ads.
>
> LIMITS
> AccessBridge describes a single snapshot of what is visible on screen. It cannot see private app data or live GPS, and results may be incomplete. AccessBridge is independent and not affiliated with the apps it reads.

**Graphics required:**

- App icon: 512 × 512 PNG
- Feature graphic: 1024 × 500 PNG/JPG
- At least 2 phone screenshots (for example: home screen, Assist Mode overlay over another app, a summary result)

Don't use other companies' logos or names (for example, Grab) in screenshots or the title in a way that implies endorsement.

**Category:** Tools. **Tags:** accessibility.

## 6. Release tracks

1. **Internal testing** — upload the `.aab`, install from the Play link on your own phone, and run the device checklist below.
2. **Closed testing** — 12+ testers for 14 days (personal accounts).
3. **Apply for production** — answer Google's questions about the closed test, then submit for review. Accessibility-service apps often take longer to review than typical apps.

## 7. Device checklist before each release

Test on a real phone with TalkBack on, using the Play-installed build (not a debug build):

- [ ] Status bar and header text do not overlap (edge-to-edge on Android 15/16).
- [ ] Assist Mode overlay is fully visible above the navigation bar and fully reachable with TalkBack swipes.
- [ ] Summarize, Read all, Refresh, and Close all work, and Close leaves the underlying app unchanged.
- [ ] A protected screen (for example, a banking app) shows the "Protected screen" message instead of failing silently.
- [ ] The share-a-screenshot flow works from Photos/Gallery.
- [ ] No AI option appears anywhere in the release build.
- [ ] Largest font size and display size: all content remains reachable.
