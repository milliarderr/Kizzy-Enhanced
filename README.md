<div align="center">
<h1>Kizzy Enhanced</h1>
<h4>An enhanced fork of <a href="https://github.com/dead8309/Kizzy">Kizzy</a> — a Discord Rich Presence manager for Android, written in Kotlin.</h4>
</div>

<div align="center">
<img src="https://img.shields.io/badge/Minimum%20SDK-27-%23?&style=flat-square&color=5b5ef7">
<img src="https://img.shields.io/badge/kotlin-5b5ef7.svg?logo=kotlin&logoColor=white&style=flat-square">
<img src="https://img.shields.io/badge/package-io.kizzy.fork-5b5ef7?style=flat-square">
<img src="https://img.shields.io/badge/name-Kizzy%20Enhanced-5b5ef7?style=flat-square">
</div>

<div align="center">

[![README на русском](https://img.shields.io/badge/README-на%20русском%20языке-blue?style=for-the-badge)](README_RU.md)

</div>

> [!NOTE]
> **Kizzy Enhanced** is a personal fork of [Kizzy](https://github.com/dead8309/Kizzy) by dead8309. Fork-specific additions are marked with ⭐ below.

## Features

- [x] Clickable buttons
- [x] Detects current Running app
- [x] Detects Current Playing media
- [x] Optional timestamps
- [x] Custom Status
- [x] Save/Load presence configs
- [x] Material You theme
- [x] Translations
- [x] Easy Setup
- [x] 300+ Predefined presets
- [x] Create custom configs with your own images and links
- [x] Preview RPC in the app itself
- [x] Runs in background even when screen is off
- [x] Gif support
- [x] External Url support (meaning you can give a url which points to an image on the web and discord will show it!)
- [x] Use Images from Gallery
- [x] ⭐ Platform spoofer (Desktop / Web / Android / iOS / Xbox / Playstation / VR)
- [x] ⭐ Custom Application ID — use your own Discord app icon and name
- [x] ⭐ Extra template placeholders (`{{media_album}}`, `{{battery}}`, `{{playback}}` and more)
- [x] ⭐ Timestamp mode selector (Media/App default · Current time of day · Custom ms)
- [x] ⭐ Home screen redesign — full-width cards with on/off toggles
- [x] ⭐ Auto-reconnect gateway with exponential backoff (no more dying after 1–2 h)
- [x] ⭐ Unbreakable detection loop — network errors no longer stop app/game detection
- [x] ⭐ 2-second app-switch polling (was 5 s) for snappier game detection

## What's new in this fork

All additions live in the **Experimental RPC** screen and on the home screen.

### ⭐ Platform spoofer
- Choose which platform your presence connects from — **Desktop, Web, Android, iOS, Xbox, Playstation, VR** (ported from the platforms used by Equicord's platformSpoofer).
- Works at the gateway `IDENTIFY` level (client status), so it is correctly picked up by plugins like **platformindicators**.
- Applies **live** — the service restarts itself, no manual toggle needed.

### ⭐ Custom Application ID (own icon)
- Selector **Built-in / Custom**. Built-in keeps the default icon; Custom reveals a field where you paste your own Discord **Application ID**, so the activity shows **your** icon and name.

### ⭐ Extra template placeholders
In addition to the original ones, with autocomplete:
- `{{media_album}}`, `{{media_album_artist}}`, `{{media_duration}}`, `{{media_writer}}`, `{{media_composer}}`
- `{{battery}}` — battery percentage
- `{{playback}}` — playback state (▶ / ⏸)

### ⭐ Timestamp mode selector
- **Media / App (default)** — timestamps from the current track/app.
- **Same as current time of day** — Discord shows the current time as elapsed (e.g. at 15:30 → shows "15:30 elapsed"). Same logic as Vencord's customRPC.
- **Custom** — type raw **Start / End Timestamp (in milliseconds)** yourself.

### ⭐ Home screen redesign
- Feature cards are now a vertical stack of full-width cards.
- **On/off toggle switches on every card**, so Media / Custom / Experimental RPC can be started and stopped directly from the home screen.

### ⭐ Gateway resilience (ported from [YuuzuTH/Kizzy](https://github.com/YuuzuTH/Kizzy))
- Auto-reconnect with exponential backoff on any unexpected disconnect.
- Resumes the session when possible; falls back to fresh identify after 3 failed attempts.
- Fatal close codes (4004, 4010–4014) still stop permanently.
- 60-second cap on presence-update waits (was an infinite loop).
- Detection loop is now wrapped in try/catch — a single network error no longer kills app-switch detection until manual restart.
- App-switch polling: **2 s** (was 5 s).
- Fix: switching between two enabled apps now updates the presence immediately.

### Fixes
- Fixed crash on **"Share log"** (FileProvider authority now follows the package id).
- Fixed crashes when switching RPC features from the home screen.

## System Requirements
- OS: Android 8.1 through 14 *(Android 14 may have some bugs with experimental features)*
- RAM: 3 GB minimum

## Download
Grab the latest signed `app-release.apk` from the [**Releases**](../../releases/latest) page.

**Kizzy Enhanced** uses the package id **`io.kizzy.fork`**, so it installs alongside the original Kizzy if you have it.

> [!WARNING]
> This app uses the Discord Gateway connection. Use it at your own risk.

## Build
> Prerequisites: Android Studio, and familiarity with Gradle, Kotlin, and Jetpack Compose.

```console
git clone https://github.com/milliarderr/Kizzy.git
```
Open the project in Android Studio, import it, then Build and Run. Release builds are produced automatically by the **Build Release Apk** GitHub Actions workflow.

## Credits
Original app and all upstream work by [dead8309](https://github.com/dead8309/Kizzy) and contributors.

Gateway resilience fixes ported from [YuuzuTH/Kizzy](https://github.com/YuuzuTH/Kizzy).

✨ [Read You](https://github.com/Ashinch/ReadYou) and [Seal](https://github.com/JunkFood02/Seal) for UI Components

✨ [Material Color Utilities](https://github.com/material-foundation/material-color-utilities)

✨ [Rich-Presence-U](https://github.com/ninstar/Rich-Presence-U) for Nintendo and Wii U games data

✨ [Logra](https://github.com/wingio/Logra) for logs ui

✨ [Xbox-Rich-Presence-Discord](https://github.com/MrCoolAndroid/Xbox-Rich-Presence-Discord) for Xbox games data

✨ [Monet](https://github.com/Kyant0/Monet) for Material3 palettes

## Licence
**Kizzy** is an open source project under the GNU GPL 3.0 Open Source License, which allows you to use, reference, and modify the source code for free, but does not allow the modified and derived code to be distributed and sold as closed-source commercial software.
