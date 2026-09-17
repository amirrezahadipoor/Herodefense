# Audio Source Selection

Selected on 2026-09-12 before any audio asset was committed.

| Game use | Candidate file | Source collection | Published license |
|---|---|---|---|
| Seamless background music | `Heavenly Loop.ogg` | [Heavenly Loop by isaiah658](https://opengameart.org/content/heavenly-loop) | CC0 1.0 |
| Enemy/Hero hit | `Audio/chop.ogg` | [Kenney RPG Audio](https://kenney.nl/assets/rpg-audio) | CC0 1.0 |
| Enemy death | `Audio/dropLeather.ogg` | [Kenney RPG Audio](https://kenney.nl/assets/rpg-audio) | CC0 1.0 |
| Item drop/pickup | `Audio/drop_002.ogg` | [Kenney Interface Sounds](https://kenney.nl/assets/interface-sounds) | CC0 1.0 |
| Level up | `Audio/confirmation_004.ogg` | [Kenney Interface Sounds](https://kenney.nl/assets/interface-sounds) | CC0 1.0 |
| Boss entrance | `Audio/doorOpen_2.ogg` | [Kenney RPG Audio](https://kenney.nl/assets/rpg-audio) | CC0 1.0 |

The source pages identify all three collections as free CC0 content. Exact archive license text, imported-file hashes, format checks, and final filenames are recorded separately at import time in `AUDIO_LICENSES.md`. Full source archives are temporary review inputs and are not committed.

## Phase 18 additions (selected 2026-09-14)

| Game use | Candidate file | Source collection | Published license |
|---|---|---|---|
| Critical arrow | `Audio/impactMetal_heavy_003.ogg` | [Kenney Impact Sounds](https://kenney.nl/assets/impact-sounds) | CC0 1.0 |
| Enemy kill thump | `Audio/impactPunch_heavy_002.ogg` | [Kenney Impact Sounds](https://kenney.nl/assets/impact-sounds) | CC0 1.0 |
| Chain-lightning arc | `Audio/impactGlass_light_002.ogg` | [Kenney Impact Sounds](https://kenney.nl/assets/impact-sounds) | CC0 1.0 |
| Stun | `Audio/impactBell_heavy_004.ogg` | [Kenney Impact Sounds](https://kenney.nl/assets/impact-sounds) | CC0 1.0 |
| Multi Shot bowstring | `Audio/pluck_002.ogg` | [Kenney Interface Sounds](https://kenney.nl/assets/interface-sounds) | CC0 1.0 |
| Shop purchase | `Audio/handleCoins.ogg` | [Kenney RPG Audio](https://kenney.nl/assets/rpg-audio) | CC0 1.0 |

## Generated in-repo (added 2026-09-17, roadmap R6.1)

The four music beds (`vigil`, `hollow_march`, `heartwood_dawn`, `quiet_after`) have no source page because
they have no source: they are rendered by `tools/audio/generate_music.py`, which is committed next to them.
The tool is the provenance, the per-file hashes are in `AUDIO_LICENSES.md`, and `MusicSelectionPolicyTest`
fails if a bed is missing from that ledger. The one imported loop that used to be the whole soundtrack
(Heavenly Loop, CC0) is retired: its ledger row stays, the file leaves the APK.

## Generated in-repo effects (added 2026-09-17, roadmap R6.2)

The nine effects added for R6.2 (`bow_release`, `bow_release_light`, `bow_release_heavy`, `coin_pickup`,
`telegraph_warning`, `ui_tap`, `ui_close`, `wave_clear`, `ambience_vigil`) come from
`tools/audio/generate_sfx.py` for the same reason. Their measured peaks are in `LEVELS.md`; the imported cues
that measurement found above full scale were re-encoded by `tools/audio/normalize_levels.py` and the applied
gains are recorded in `AUDIO_LICENSES.md`.
