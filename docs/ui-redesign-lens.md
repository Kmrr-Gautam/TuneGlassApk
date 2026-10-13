# "Lens" UI redesign

Front-end only. No ViewModel, repository, player, playback, or data code was changed.

## Identity
- Ink + warm paper palette; artwork is the only loud colour (`Md3SchemeBuilder`). Accent hue, AMOLED and
  dynamic colour still work exactly as before.
- Fraunces serif (display/headline/titleLarge) + Google Sans Flex with roundness off (everything else).
- Hairline rings for secondary buttons (`LensRingButton`), bare glyphs for transport (`LensGlyphButton`),
  and one solid control: the play disc, whose outer ring is the track-progress arc.
- Text-only floating dock (no icons, no pill).
- Now Playing: full-bleed cover that dissolves into its own deepest colour. Header ring button toggles
  "fill screen" vs "show whole cover" (not persisted).
- Seek bar: hairline with a ring thumb; single gentle ripple while playing. "Wavy seekbar" off = straight.

## Fonts
`res/font/fraunces.ttf` is Fraunces (c) The Fraunces Project Authors, SIL Open Font License 1.1
(https://openfontlicense.org). Instanced (WONK off, SOFT 30) and subset to Latin.

## Files
New: `ui/common/LensComponents.kt`, `res/font/fraunces.ttf`
Edited: `ui/theme/{Md3SchemeBuilder,Shape,Type}.kt`, `ui/common/{ExpressiveHeader,ExpressiveGroup}.kt`,
`ui/shell/MainShell.kt`, `ui/player/{PlayerHost,WavySeekBar}.kt`, `res/values/colors.xml`
