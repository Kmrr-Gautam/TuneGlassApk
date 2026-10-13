import os
import re

def main():
    root_dir = "app/src/main"
    
    # 1. Update Shape.kt to make UI sharper
    shape_path = os.path.join(root_dir, "java/com/listenit/music/ui/theme/Shape.kt")
    if os.path.exists(shape_path):
        with open(shape_path, "r", encoding="utf-8") as f:
            content = f.read()
        sharp_shapes = """package com.listenit.music.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Crisp, sharp, modern geometric design system for TuneGlass */
val ListenItShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

val ExpressiveHeroShape = RoundedCornerShape(14.dp)
val ExpressivePillShape = RoundedCornerShape(8.dp)
val HeroInnerShape = RoundedCornerShape(10.dp)
val StatPillShape = RoundedCornerShape(8.dp)
val ListContainerShape = RoundedCornerShape(12.dp)
val BadgePillShape = RoundedCornerShape(6.dp)
val NowPlayingCardShape = RoundedCornerShape(12.dp)
val TrackRowShape = RoundedCornerShape(8.dp)
val ArtworkShape = RoundedCornerShape(8.dp)
"""
        with open(shape_path, "w", encoding="utf-8") as f:
            f.write(sharp_shapes)
        print("Updated Shape.kt with sharp, clean geometry")

    # 2. Update OnboardingScreen.kt
    onboarding_path = os.path.join(root_dir, "java/com/listenit/music/ui/onboarding/OnboardingScreen.kt")
    if os.path.exists(onboarding_path):
        with open(onboarding_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = content.replace("Welcome to ListenIt", "Welcome to TuneGlass")
        content = content.replace('"ListenIt Logo"', '"TuneGlass Logo"')
        content = content.replace('"e.g. Gautam"', '"e.g. abc"')
        content = content.replace("placeholder = { Text(\"e.g. Gautam\",", "placeholder = { Text(\"e.g. abc\",")
        # Sharper shapes
        content = content.replace("RoundedCornerShape(22.dp)", "RoundedCornerShape(14.dp)")
        content = content.replace("RoundedCornerShape(18.dp)", "RoundedCornerShape(12.dp)")
        content = content.replace("RoundedCornerShape(16.dp)", "RoundedCornerShape(10.dp)")
        content = content.replace("RoundedCornerShape(14.dp)", "RoundedCornerShape(8.dp)")
        with open(onboarding_path, "w", encoding="utf-8") as f:
            f.write(content)
        print("Updated OnboardingScreen.kt for TuneGlass & e.g. abc")

    # 3. Update SettingsScreen.kt
    settings_path = os.path.join(root_dir, "java/com/listenit/music/ui/settings/SettingsScreen.kt")
    if os.path.exists(settings_path):
        with open(settings_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = content.replace('title = "About ListenIt"', 'title = "About TuneGlass"')
        content = content.replace('"ListenIt v${appVersionName(context)}', '"TuneGlass v${appVersionName(context)}')
        content = content.replace('"ListenIt Music Player v${appVersionName(context)}"', '"TuneGlass Music Player v${appVersionName(context)}"')
        content = content.replace('Text("ListenIt"', 'Text("TuneGlass"')
        content = content.replace('contentDescription = "ListenIt"', 'contentDescription = "TuneGlass"')
        content = content.replace('SectionLabel("YouTube Music")', 'SectionLabel("Cloud Streaming & Sync")')
        content = content.replace('title = "Connect YouTube Music"', 'title = "Connect Cloud Stream Account"')
        content = content.replace('title = "Import from YouTube Music"', 'title = "Import Online Playlists"')
        content = content.replace('title = "Sync Playback to YouTube Music History"', 'title = "Sync Playback to Cloud History"')
        content = content.replace('appear in your YouTube Music history', 'appear in your cloud streaming history')
        content = content.replace('stays out of your YouTube Music history', 'stays out of your cloud streaming history')
        content = content.replace('"YouTube Music (AAC / Opus)"', '"Standard Stream (AAC / Opus)"')
        content = content.replace('"YouTube Music • Native stream"', '"Standard Stream • Native audio"')
        content = content.replace('• YouTube Music fallback', '• Standard fallback')
        content = content.replace('falls back to YouTube Music', 'falls back to Standard Stream')
        content = content.replace('"Disconnect YouTube Music?"', '"Disconnect Cloud Account?"')
        content = content.replace('"YouTube Music Account"', '"Cloud Stream Account"')
        content = content.replace('"YouTube Music"', '"Cloud Stream"')
        content = content.replace('RoundedCornerShape(28.dp)', 'RoundedCornerShape(14.dp)')
        content = content.replace('RoundedCornerShape(24.dp)', 'RoundedCornerShape(12.dp)')
        content = content.replace('RoundedCornerShape(20.dp)', 'RoundedCornerShape(10.dp)')
        with open(settings_path, "w", encoding="utf-8") as f:
            f.write(content)
        print("Updated SettingsScreen.kt for TuneGlass & removed YouTube Music mentions")

    # 4. Update SearchScreen.kt
    search_path = os.path.join(root_dir, "java/com/listenit/music/ui/search/SearchScreen.kt")
    if os.path.exists(search_path):
        with open(search_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = content.replace('Search YouTube Music…', 'Search songs, artists, albums…')
        content = content.replace('Search YouTube Music', 'Search songs, artists, albums')
        content = content.replace('Search YouTube Music\\u2026', 'Search songs, artists, albums\\u2026')
        with open(search_path, "w", encoding="utf-8") as f:
            f.write(content)
        print("Updated SearchScreen.kt search placeholder")

    # 5. Update YouTubePlaylistImportScreen.kt
    yt_import_path = os.path.join(root_dir, "java/com/listenit/music/ui/settings/YouTubePlaylistImportScreen.kt")
    if os.path.exists(yt_import_path):
        with open(yt_import_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = content.replace('Search YouTube Music playlists...', 'Search online playlists...')
        content = content.replace('Searching YouTube Music playlists...', 'Searching online playlists...')
        content = content.replace('YouTube Music, Links', 'Online Playlists, Links')
        content = content.replace('Paste any YouTube or YouTube Music playlist URL. ListenIt resolves every track', 'Paste any playlist URL. TuneGlass resolves every track')
        content = content.replace('Create a playlist in YouTube Music (or refresh) and it will show up here.', 'Create an online playlist (or refresh) and it will show up here.')
        with open(yt_import_path, "w", encoding="utf-8") as f:
            f.write(content)
        print("Updated YouTubePlaylistImportScreen.kt")

    # 6. Update Playlist screens
    pl_path = os.path.join(root_dir, "java/com/listenit/music/ui/playlist/PlaylistDetailScreen.kt")
    if os.path.exists(pl_path):
        with open(pl_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = content.replace('"Sync to YouTube Music"', '"Sync to Cloud"')
        content = content.replace('"Syncing to YouTube Music"', '"Syncing to Cloud"')
        content = content.replace('• YouTube Music', '• Cloud Stream')
        with open(pl_path, "w", encoding="utf-8") as f:
            f.write(content)
        print("Updated PlaylistDetailScreen.kt")

    # 7. Update MainShell.kt
    shell_path = os.path.join(root_dir, "java/com/listenit/music/ui/shell/MainShell.kt")
    if os.path.exists(shell_path):
        with open(shell_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = content.replace('RoundedCornerShape(32.dp)', 'RoundedCornerShape(14.dp)')
        content = content.replace('RoundedCornerShape(20.dp)', 'RoundedCornerShape(10.dp)')
        content = content.replace('RoundedCornerShape(14.dp)', 'RoundedCornerShape(8.dp)')
        with open(shell_path, "w", encoding="utf-8") as f:
            f.write(content)
        print("Updated MainShell.kt for sharper dock and cards")

    # 8. Update notification layout
    notif_path = os.path.join(root_dir, "res/layout/notification_player_expanded.xml")
    if os.path.exists(notif_path):
        with open(notif_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = content.replace("LISTENIT", "TUNEGLASS")
        with open(notif_path, "w", encoding="utf-8") as f:
            f.write(content)
        print("Updated notification layout to TUNEGLASS")

    # 9. Update README.md
    readme_path = "README.md"
    if os.path.exists(readme_path):
        with open(readme_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = content.replace("ListenIt", "TuneGlass")
        content = content.replace("LISTENIT", "TUNEGLASS")
        content = content.replace("listenit", "tuneglass")
        content = content.replace("ListenIt v1.0.0", "TuneGlass v1.0.0")
        with open(readme_path, "w", encoding="utf-8") as f:
            f.write(content)
        print("Updated README.md to TuneGlass")

if __name__ == "__main__":
    main()
