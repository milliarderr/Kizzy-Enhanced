<div align="center">
<h1>Kizzy Fork</h1>
<h4>A customized fork of <a href="https://github.com/dead8309/Kizzy">Kizzy</a> — a Discord Rich Presence manager for Android, written in Kotlin.</h4>
</div>

<div align="center">
<img src="https://img.shields.io/badge/Minimum%20SDK-27-%23?&style=flat-square&color=5b5ef7">
<img src="https://img.shields.io/badge/kotlin-5b5ef7.svg?logo=kotlin&logoColor=white&style=flat-square">
<img src="https://img.shields.io/badge/package-io.kizzy.fork-5b5ef7?style=flat-square">
</div>

> [!NOTE]
> This is a personal fork. The customizations described below were implemented by **Claude** (Anthropic's Claude Code) at the request of the repository owner. The original app is by [dead8309](https://github.com/dead8309/Kizzy).

## What's new in this fork

All additions live in the **Experimental RPC** screen and on the home screen.

### Platform spoofer
- Choose which platform your presence connects from — **Desktop, Web, Android, iOS, Xbox, Playstation, VR** (ported from the platforms used by Equicord's platformSpoofer).
- Works at the gateway `IDENTIFY` level (client status), so it is correctly picked up by plugins like **platformindicators**.
- Shows the readable platform name (e.g. "Android") and applies **live** — the service restarts itself, no manual toggle needed.

### Custom buttons
- Add up to two clickable buttons with any **text** and **URL**.
- *(Note: Discord only renders RPC buttons to other users viewing your profile, not to yourself.)*

### Custom Application ID (own icon)
- Selector **Built-in / Custom**. Built-in keeps the default (Kizzy) icon; Custom reveals a field where you paste your own Discord **Application ID**, so the activity shows **your** icon and name.

### Extra template placeholders
In addition to the original ones, with autocomplete:
- `{{media_album}}`, `{{media_album_artist}}`, `{{media_duration}}`, `{{media_writer}}`, `{{media_composer}}`
- `{{battery}}` — battery percentage
- `{{playback}}` — playback state (▶ / ⏸)

### Timestamp mode selector
- **Media / App (default)** — timestamps from the current track/app.
- **Same as your current time (not reset after 24h)** — timer anchored to when you picked it; survives service restarts and does not reset after 24h.
- **Custom** — type raw **Start / End Timestamp (in milliseconds)** yourself.

### Home screen redesign
- Feature cards are now a vertical stack of full-width cards.
- **On/off toggle switches on every card**, so Media / Custom / Experimental RPC can be started and stopped directly from the home screen.

### Fixes
- Fixed crash on **"Share log"** (FileProvider authority now follows the package id).
- Fixed crashes when switching RPC features from the home screen (empty custom config, and Experimental RPC without notification access).

## System Requirements
- OS: Android 8.1 through 14 *(Android 14 may have some bugs with experimental features)*
- RAM: 3GB minimum

## Download
Grab the latest signed `app-release.apk` from the [**Releases**](../../releases) page.

The fork uses the package id **`io.kizzy.fork`**, so it installs alongside the original Kizzy if you have it.

> [!WARNING]
> This app uses the Discord Gateway connection. Use it at your own risk.

## Build
> Prerequisites: Android Studio, and familiarity with Gradle, Kotlin, and Jetpack Compose.

```console
git clone https://github.com/milliarderr/Kizzy.git
```
Open the project in Android Studio, import it, then Build and Run. Release builds are produced automatically by the **Build Release Apk** GitHub Actions workflow.

## Credits
This fork's customizations were built by **Claude** ([Claude Code](https://claude.com/claude-code) by Anthropic).

Original app and all upstream work by [dead8309](https://github.com/dead8309/Kizzy) and contributors:

✨ [Read You](https://github.com/Ashinch/ReadYou) and [Seal](https://github.com/JunkFood02/Seal) for UI Components

✨ [Material Color Utilities](https://github.com/material-foundation/material-color-utilities)

✨ [Rich-Presence-U](https://github.com/ninstar/Rich-Presence-U) for Nintendo and Wii U games data

✨ [Logra](https://github.com/wingio/Logra) for logs ui

✨ [Xbox-Rich-Presence-Discord](https://github.com/MrCoolAndroid/Xbox-Rich-Presence-Discord) for Xbox games data

✨ [Monet](https://github.com/Kyant0/Monet) for Material3 palettes

## Licence
**Kizzy** is an open source project under the GNU GPL 3.0 Open Source License, which allows you to use, reference, and modify the source code for free, but does not allow the modified and derived code to be distributed and sold as closed-source commercial software.
