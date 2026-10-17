# ASTRALDLC Overlay Template — Android Java

A starter template for a separate launcher app with a local demo key and a user-authorized Android overlay.
It launches Standoff 2 if installed and displays an ASTRALDLC menu overlay. It does **not** inject code into
the game and does not include aimbot, wallhack, memory reading, anti-cheat bypass, or game modification.

## Included
- Injector-style launch screen: `KEY` + `LOAD`.
- Demo key: `ASTRAL-152` (hardcoded only for demo; not a secure licensing system).
- Launches package `com.axlebolt.standoff2` if installed.
- If overlay permission has not been granted, explains how to grant it in Android Settings.
- Overlay watermark at top-right labelled `Astraldlc | astral.cc`; tapping it toggles the menu.
- Menu panel can be dragged; watermark stays fixed.
- HOME / AIMBOT (safe placeholder) / VISUALS / OPTIMIZE / CONFIGS / SETTINGS / ABOUT.
- Local settings and JSON configs (save/load/rename/delete/reset).
- Potato Graphics and visual toggles are app-side demo preferences; they do not alter game graphics.
- Root status is a common-path check only; no root commands run.

## Build
Use Android Studio / compatible Gradle IDE with JDK 17, Android Gradle Plugin 8.7.3, Gradle 8.9, SDK 35.
Run `gradle :app:assembleDebug`. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

Phone-only cloud build: upload this project to GitHub (include `.github`) and run Actions workflow
`Build ASTRALDLC overlay debug APK`. Download artifact `ASTRALDLC-overlay-debug-apk`.

## Permissions and Android behavior
Android requires the user to explicitly grant “Display over other apps” special access. The app opens
the system settings page; it cannot silently grant that permission. Some manufacturers restrict overlays
over games or secure screens. The overlay is a normal Android window, not an in-game modification.

## Validation status
ZIP structure/integrity checked. This environment did not compile the APK or test it on a real device.
Do not describe it as bug-free or fully tested until built and tested on the target Android version.
