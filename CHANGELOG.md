# Changelog

## [1.1.7] - 2026-10-20

### Fixed
- **Reliable update prompts:** Bypass cached update metadata, compare all available metadata sources, and allow users to defer the update prompt without permanently hiding it.

## [1.1.6] - 2026-10-20

### Added
- **Motion-sensitive poster tilt:** The full-screen player poster responds smoothly to device movement, with a persistent Settings toggle.
- **Followed-artist Discover feed:** Discover recommends tracks by followed artists whose genres match the user's listening history and refreshes when followed artists change.

## [1.1.5] - 2026-10-20

### Changed
- **Feed and Search UI refresh:** Redesigned both screens while retaining existing music data, search, navigation, and playback behavior.
- **Play Next queue behavior:** Adding a song to the queue now places it immediately after the currently playing song.
- **Update checks:** GitHub-hosted version metadata is checked before the website copy so releases published to the repository are detected promptly.

## [1.1.4] - 2026-10-15

### Added
- **Custom Profile Avatars & Mini Doodles:** Users can customize their profile picture by picking images from their gallery or choosing between 10 colorful mini doodles across Login, Onboarding, and Settings.
- **Quick Settings Navigation:** Tapping the profile avatar on the Home screen header takes the user directly to About & System in Settings.
- **What's New Dialog:** In-app version changelog popup notifying users about newly introduced features and bug fixes upon launch.
- **Liquid Frequency Glass Styling:** Sleek charcoal and translucent frosted glass styling with tactile spring interactions.

### Changed & Fixed
- **Clean Full-Screen Playback Experience:** Removed Dynamic Island overlay to eliminate background display conflicts and provide a clean, distraction-free UI.
- **Privacy Improvements:** Replaced personalized demo name with generic "XYZ" placeholder across onboarding and greeting headers.
- **Notification Polish:** Refined foreground playback notification behavior and actions.


### Fixed
- **Duplicate/overlapping "now playing" notification on Android 10 (One UI 2.x).**
  `buildNotification()` used `Notification.DecoratedMediaCustomViewStyle`
  with a `MediaSession` attached, alongside a fully custom `RemoteViews`
  player (own artwork, title, artist, transport buttons). On Android 10 +
  Samsung One UI 2.x, SystemUI's older media-notification renderer drew
  its own full media chrome as a second layer instead of just framing the
  custom view, producing two overlapping players in the notification
  shade/quick controls.

  Now version-gated: Android 11+ keeps `DecoratedMediaCustomViewStyle`
  with the session attached as before; Android 10 and below uses
  `Notification.DecoratedCustomViewStyle` (no session tag on the
  notification itself). Lock screen controls, Bluetooth, Android Auto,
  and the in-app widget are unaffected, since they all read from
  `mediaSession` directly rather than this notification's `Style` object.

### Added
- **Offline playback priority across all screens (Fixes #31).**
  `MusicPlayer.resolveTrackAudioStream()` now checks the local Room database (`DownloadedTrackDao`) and verifies file presence on disk before attempting remote network resolution (Lossless / YouTube Music / InnerTube). When a track has already been downloaded, LastWave plays the local media file directly without making network calls, enabling seamless offline playback across Home, Search, Playlists, Album, and Artist screens and saving cellular data when online.

  Files changed:
  `app/src/main/java/com/listenit/music/playback/MusicPlayer.kt`

- **Download state awareness and duplicate download prevention.**
  - Added `DownloadedTrackDao.findByTrackKey` and deduplication checks in `TrackDownloadManager.downloadTrack` to prevent duplicate download jobs, redundant network requests, and duplicate files (e.g. `(1).flac`) in MediaStore.
  - The 3-dot context menu sheet (`TrackContextMenuSheet`) now dynamically reflects the track's status (`Downloaded` with check icon, `Downloading…`, or `Download (Max Quality)`), giving instant visual feedback and preventing accidental re-downloads.

  Files changed:
  `app/src/main/java/com/listenit/music/data/local/db/DownloadedTrackDao.kt`
  `app/src/main/java/com/listenit/music/data/download/TrackDownloadManager.kt`
  `app/src/main/java/com/listenit/music/ui/common/TrackContextMenuSheet.kt`

- **Direct navigation from download notifications to the Downloads screen.**
  `TrackDownloadManager` now fires pending intents with `ACTION_VIEW_DOWNLOADS` targeting `DownloadsScreen` (`AppRoute.Downloads`). Integrated an `AppRouteNavigator` singleton and `AppRouteNavBridge` into `NavGraph` and `MainActivity` so tapping download notifications opens the Downloads screen directly instead of only bringing the app to the foreground.

  Files changed:
  `app/src/main/java/com/listenit/music/data/download/TrackDownloadManager.kt`
  `app/src/main/java/com/listenit/music/MainActivity.kt`
  `app/src/main/java/com/listenit/music/ui/navigation/AppRouteNavigator.kt`
  `app/src/main/java/com/listenit/music/ui/navigation/NavGraph.kt`

## 2026-09-02 — musaibbhat120605

### Fixed
- **WebM/Opus downloads not showing metadata in external players/file managers.**
  `AudioTagWriter.embedIntoWebm()` previously appended the `Tags`/`Attachments`
  elements at the very end of the file, after all audio `Cluster` data. This
  produced structurally valid EBML, but many players and file managers only
  scan the header region of a WebM/Matroska file (stopping once they reach
  audio data) instead of reading the whole file, so the tags were effectively
  invisible outside the app.

  Metadata is now spliced in right before the first `Cluster`, matching where
  real muxers place it:
  - Any existing `SeekHead` is dropped instead of left with stale offsets —
    compliant readers fall back to a normal sequential scan when it's absent.
  - Any existing `Tags`/`Attachments` elements are removed so duplicates
    aren't left behind.
  - The `Segment` size field is patched to match the new layout.
  - If a `Cues` index is present (rare for YouTube's DASH audio, but possible
    for other muxed sources), the old end-of-file append is used instead,
    since rewriting `Cues` byte offsets safely is out of scope for this fix.

  Files changed: `app/src/main/java/com/listenit/music/data/download/AudioTagWriter.kt`

### Fixed
- **Home screen (Last.fm) lag / stutter.**
  `HomeUiState.visibleRows()` (filters, day-groups, sorts, and dedupes the
  full track history) was being recomputed inline inside a Compose
  `remember` block, which runs on the UI thread. Last.fm's now-playing and
  recent-tracks polling ticks every 12–30 seconds, so every time a track
  scrobbled in, this fairly expensive rebuild ran right on the frame meant
  to update the screen, causing a visible stutter tied directly to
  scrobbling. On top of that, the "Recent" track list was never capped, so
  it kept growing (and getting more expensive to rebuild) the longer Home
  stayed open in a session.

  - `HomeViewModel` now recomputes the row list on a background dispatcher
    (`Dispatchers.Default`) and exposes it as its own `StateFlow`, so the UI
    thread just collects a finished list instead of building it.
  - The 30-second recent-tracks poll now caps the merged track history at
    500 entries instead of growing it indefinitely.

  Files changed:
  `app/src/main/java/com/listenit/music/ui/home/HomeViewModel.kt`,
  `app/src/main/java/com/listenit/music/ui/home/HomeScreen.kt`

### Fixed
- **Downloaded tracks appearing twice in the Downloads list.**
  `TrackDownloadManager.downloadTrack()` had no check against tracks that
  were already downloaded — it only guarded against the *same* download
  running twice concurrently (`activeKeys`), which is cleared as soon as a
  download finishes. Re-downloading a track you already had correctly
  overwrote the file on disk, but `downloadedTrackDao.insert()` always
  created a brand-new row: `DownloadedTrackEntity.id` is an
  autoincrement primary key with no other unique constraint, so
  `OnConflictStrategy.REPLACE` never had anything to actually collide
  with.

  - Added a normalized `trackKey` column (`"${artist}_${title}"`,
    lowercased/trimmed) with a **unique index**, so the database itself
    can no longer hold two rows for the same track.
  - Migration `10 → 11` backfills `trackKey` for existing rows, deletes
    any duplicate rows already present (keeping the most recently
    downloaded copy of each), then creates the unique index.
  - `downloadTrack()` now checks the database first; if the track is
    already downloaded and its file still exists, it skips re-downloading
    entirely instead of re-fetching and duplicating. If the file was
    removed outside the app, it falls through and re-downloads, and the
    unique index makes that insert safely `REPLACE` the stale row instead
    of duplicating it.

  Files changed:
  `app/src/main/java/com/listenit/music/data/local/db/DownloadedTrackEntity.kt`,
  `app/src/main/java/com/listenit/music/di/DatabaseModule.kt`,
  `app/src/main/java/com/listenit/music/data/download/TrackDownloadManager.kt`

### Fixed
- **Skipping to the next track did nothing when playing from Downloads.**
  `DownloadsViewModel.playTrack()` started playback via `MusicPlayer.play()`,
  which always builds a single-track queue (`listOf(track)`) regardless of
  how many tracks are downloaded. So the moment you played anything from the
  Downloads screen, the player's queue had exactly one item — there was
  never a "next" track to advance to, so `next()` correctly found no
  following item and silently did nothing.

  `playTrack()` now builds the full queue from every currently downloaded
  track (in the same order shown on screen), starting at the tapped
  track's position, via `MusicPlayer.playQueue()` instead of `play()`.
  Next/previous now move through the rest of your downloads normally.

  Files changed:
  `app/src/main/java/com/listenit/music/ui/settings/DownloadsViewModel.kt`
