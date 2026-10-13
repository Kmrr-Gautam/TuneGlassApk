<div align="center">

<img src="tuneglass_logo.png" alt="TuneGlass Logo" width="120" height="120" style="border-radius: 50%;" />

# TuneGlass v1.0.0

**High-Resolution Lossless Music Streaming & Player with Real-Time Synced Lyrics & Smart Discovery for Android, built with Material 3 Expressive design.**

<p align="center">
  <a href="#">
    <img src="https://img.shields.io/badge/Audio-Hi--Res%20Lossless%20FLAC-00E5FF?style=for-the-badge&logo=flac&logoColor=white&labelColor=012226" alt="Hi-Res Lossless FLAC" />
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/Version-1.0.0--native-C6F100?style=for-the-badge&labelColor=012226" alt="Version 1.0.0-native" />
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=2d2d2d" alt="Platform" />
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge&labelColor=2d2d2d" alt="GPL 3.0 License" />
  </a>
</p>

</div>

<br/>

## <img src="https://api.iconify.design/lucide:sparkles.svg?color=%23C6F100" width="20" height="20" align="center" /> Overview

**TuneGlass** is a native Android music streaming app engineered for audiophiles and music lovers. It delivers studio-quality **Hi-Res Lossless FLAC (up to 24-bit/192kHz)** and Opus playback, offline downloads with full embedded metadata, kinetic real-time synchronized lyrics, smart algorithmic playlist generation, and automatic scrobbling across all your devices.

Designed from the ground up around **Material 3 Expressive**, TuneGlass pairs an ultra-smooth visual experience with uncompromising audio fidelity.

---

## <img src="https://api.iconify.design/lucide:headphones.svg?color=%23C6F100" width="20" height="20" align="center" /> Lossless Audio Streaming & Backend

TuneGlass features a lossless streaming and download engine powered directly by the **TuneGlass Audio** backend:

- <img src="https://api.iconify.design/lucide:disc.svg?color=%2300E5FF" width="16" height="16" align="center" /> **Studio Master Quality:** Stream and download bit-perfect FLAC audio up to **24-bit / 192kHz** directly via **TuneGlass Lossless Engine**.
- <img src="https://api.iconify.design/lucide:zap.svg?color=%23C6F100" width="16" height="16" align="center" /> **Opus & High-Efficiency Audio:** Seamless dual-engine fallback streaming via YouTube Music Opus for exhaustive global catalog coverage.
- <img src="https://api.iconify.design/lucide:download.svg?color=%23C6F100" width="16" height="16" align="center" /> **Full-Fidelity Offline Downloads:** One-tap downloads saved directly to your local storage (`Music/TuneGlass`), fully tagged with high-res cover art, release tags, and synchronized LRCLIB `.lrc` lyrics.
- <img src="https://api.iconify.design/lucide:sliders.svg?color=%23C6F100" width="16" height="16" align="center" /> **Zero-Gap Playback:** Powered by AndroidX Media3 ExoPlayer with foreground audio playback and lockscreen media session controls.

---

## <img src="https://api.iconify.design/lucide:layers.svg?color=%23C6F100" width="20" height="20" align="center" /> Key Features

| Icon | Feature | Highlight |
|:---:|:---|:---|
| <img src="https://api.iconify.design/lucide:music.svg?color=%2300E5FF" width="20" height="20" /> | **Lossless Streaming** | True Hi-Res 24-bit FLAC & Opus streaming with lossless bitstream output. |
| <img src="https://api.iconify.design/lucide:arrow-down-to-line.svg?color=%23C6F100" width="20" height="20" /> | **Offline Downloader** | Download albums, mixes, and individual songs with embedded album art and synced lyrics. |
| <img src="https://api.iconify.design/lucide:mic.svg?color=%23FFB4A2" width="20" height="20" /> | **Real-Time Synced Lyrics** | Millisecond-accurate animated karaoke lyrics powered by LRCLIB with 8 customizable physics motion styles (Apple Fluid, Karaoke Pulse, Kinetic Slide, etc.). |
| <img src="https://api.iconify.design/lucide:compass.svg?color=%23C6F100" width="20" height="20" /> | **Smart Discovery Engine** | Tailored recommendation feed built from your taste profile, similar seeds, loved tracks, and live charts. |
| <img src="https://api.iconify.design/lucide:disc.svg?color=%2300E5FF" width="20" height="20" /> | **Genre DNA & Explorer** | Dynamic breakdown of your favorite genres with instant "Start Mix" and one-tap "Discover More" recommendations. |
| <img src="https://api.iconify.design/lucide:wand-2.svg?color=%23C6F100" width="20" height="20" /> | **Taste Mixes & Playlists** | Generate unique 30–35 track mood mixes, regenerate fresh variations, manage custom playlists, and sync with YouTube Music. |
| <img src="https://api.iconify.design/lucide:users.svg?color=%23FFB4A2" width="20" height="20" /> | **Friends & Social Feed** | Browse friends' listening habits, explore their top tracks, and play their taste profiles. |
| <img src="https://api.iconify.design/lucide:radio.svg?color=%2300E5FF" width="20" height="20" /> | **Integrated Scrobbler** | Automatic background scrobbler watching your active media sessions across any Android music app with zero battery drain. |
| <img src="https://api.iconify.design/lucide:palette.svg?color=%23C6F100" width="20" height="20" /> | **Material 3 Expressive** | Dynamic wallpaper colors, custom HSL color picker, dynamic album art palette, fluid morphing cards, and tactile haptics. |
| <img src="https://api.iconify.design/lucide:cast.svg?color=%2300E5FF" width="20" height="20" /> | **Chromecast & Android Auto** | Stream to Google Cast receivers and browse music securely in your car with Android Auto. |

---

## <img src="https://api.iconify.design/lucide:cpu.svg?color=%23C6F100" width="20" height="20" align="center" /> Tech Stack & Architecture

- **Language & Framework:** 100% Kotlin + Jetpack Compose (Material 3 Expressive)
- **Lossless Audio Backend:** **[TuneGlass Audio](https://github.com/Kmrr-Gautam/TuneGlassApk)** by [Gautam Kumar (Kmrr-Gautam)](https://github.com/Kmrr-Gautam)
- **Audio Engine:** AndroidX Media3 ExoPlayer + MediaSessionService + Low-Latency Oboe Native Engine
- **Lyrics Engine:** [LRCLIB](https://lrclib.net) API + LyricsPlus + BetterLyrics
- **Data & Intelligence:** Last.fm API, Room DB, Jetpack DataStore, Dagger Hilt

---

## <img src="https://api.iconify.design/lucide:terminal.svg?color=%23C6F100" width="20" height="20" align="center" /> Building from Source

```bash
git clone https://github.com/Kmrr-Gautam/TuneGlassApk.git
cd TuneGlassApk
./gradlew assembleRelease
```

---

## <img src="https://api.iconify.design/lucide:info.svg?color=%2300E5FF" width="20" height="20" align="center" /> Attribution & License

TuneGlass is licensed under the [GNU General Public License v3.0](LICENSE).  
Developed and maintained by **[Gautam Kumar (Kmrr-Gautam)](https://github.com/Kmrr-Gautam)**.
