# Committed Audio License Ledger

Every source and license below was checked on 2026-09-12 **before** the corresponding file was added to the repository. All files are unmodified Ogg Vorbis source bytes renamed for their game role. No full source archive is committed.

## OpenGameArt — Heavenly Loop

- Author: `isaiah658`
- Source page: https://opengameart.org/content/heavenly-loop
- Direct file: https://opengameart.org/sites/default/files/Heavenly%20Loop_0.ogg
- License displayed on source page: Creative Commons Zero 1.0 (`CC0-1.0`)
- License URL: https://creativecommons.org/publicdomain/zero/1.0/
- Source notes: advertised as a seamless ambient loop; attribution is appreciated but not required.
- Download SHA-256: `a842e9e054019132cacc8fd352e7b31c000ebb51e0b227a2511e1bccb4eb166e`
- Preserved provenance: `licenses/heavenly-loop-provenance.txt`

## Kenney — RPG Audio 1.0

- Creator/distributor: Kenney Vleugels (`Kenney.nl`)
- Source page: https://kenney.nl/assets/rpg-audio
- Download archive: https://kenney.nl/media/pages/assets/rpg-audio/8e99002d76-1677590336/kenney_rpg-audio.zip
- License: Creative Commons Zero 1.0 (`CC0-1.0`), including commercial use; attribution optional.
- Archive SHA-256 at review: `6dbeaf8544da958d8f2adcb4a4a4b76c1ade34a05f8ab9edccd327da7375f38b`
- Exact bundled license: `licenses/kenney-rpg-audio-license.txt`

## Kenney — Interface Sounds 1.0

- Creator/distributor: Kenney (`Kenney.nl`)
- Source page: https://kenney.nl/assets/interface-sounds
- Download archive: https://kenney.nl/media/pages/assets/interface-sounds/fa43c1dd4d-1677589452/kenney_interface-sounds.zip
- License: Creative Commons Zero 1.0 (`CC0-1.0`), including personal, educational, and commercial use; attribution optional.
- Archive SHA-256 at review: `f2193d072726d6758a5f7871b2dcc54dcce0d5c35c6f0a62f92549b327c81232`
- Exact bundled license: `licenses/kenney-interface-sounds-license.txt`

## Kenney — Impact Sounds 1.0 (added 2026-09-14, Phase 18)

- Creator/distributor: Kenney (`Kenney.nl`)
- Source page: https://kenney.nl/assets/impact-sounds
- Download archive: https://kenney.nl/media/pages/assets/impact-sounds/87b4ddecda-1677589768/kenney_impact-sounds.zip
- License: Creative Commons Zero 1.0 (`CC0-1.0`); bundled `License.txt` states "Creative Commons Zero, CC0" and permits commercial use without attribution.
- Archive SHA-256 at review: `029d734af1582474edf3a694d1b0cebc97c1c152f2f39fa34d4c2bafc5de77f8`
- Exact bundled license: `licenses/kenney-impact-sounds-license.txt`
- The RPG Audio and Interface Sounds archives were re-downloaded on 2026-09-14 and matched the SHA-256 values recorded above before the two new files were taken from them.

## Generated in-repo (added 2026-09-17, roadmap R6.1)

These four beds are not downloads. They are authored by `tools/audio/generate_music.py`, which is committed, so
the provenance is stronger than a licence: it is the source. Each is rendered from a seed -- oscillators, an
envelope per voice, a wrapped reverb tail and a wrapped head so the loop has no seam -- and every note is in the
same key, which is what lets a fight crossfade into a menu without a jolt. The tool prints a per-file size and
tempo; `MusicSelectionPolicyTest` fails if any of these files or hashes is missing from this table.

Rights: nothing here is derived from a third-party recording, sample pack or composition, so there is no
attribution obligation and no licence to honour -- the files are the output of a generator, in the same sense
as the 111 PNG sprites the art pipeline renders. The one imported loop this replaces
(`audio/music/world_tree_vigil.ogg`, CC0, Heavenly Loop by isaiah658) is retired in the table above rather than
deleted from the ledger, and its row keeps the hash it shipped with.

| Committed file | Original file | Source | Use | SHA-256 |
|---|---|---|---|---|
| `audio/music/vigil.ogg` | *(none)* | generated in-repo | Music bed | `731bc05cead8d719ccd53866f92bf4e0f832aff2648d2680101f92f64f59e852` |
| `audio/music/hollow_march.ogg` | *(none)* | generated in-repo | Music bed | `74b97655dc3470c5b16aadc2546a4ac9c131b8ac4bb19b54a009742d9791689f` |
| `audio/music/heartwood_dawn.ogg` | *(none)* | generated in-repo | Music bed | `939f87b202fd98944ac69915a65d93a9c282edadd668e7852182e718c4da6fe9` |
| `audio/music/quiet_after.ogg` | *(none)* | generated in-repo | Music bed | `4a2bba3c4927c72b06453135fec1ccc7dd6e43f27f26a11e6b55365604cf45a9` |

## Level normalisation (2026-09-17, roadmap R6.2)

Measuring the committed bytes found what listening had not: five imported cues decoded above full scale --
`boss_entrance` at 1.122, `hit` at 1.058, `purchase` at 1.022, `death` at 0.986, `multi_shot` at 0.948 -- and one
generated bed touched 1.000. They are corrected here, in the repository, by a tool rather than by hand:
`tools/audio/normalize_levels.py` decodes each file, applies the gain that puts the peak on the ceiling,
re-encodes, and re-measures, because Vorbis adds its own overshoot on the way out (about 7% in these files).
Applied gains, one line per file, oldest first: `boss_entrance` x0.802 then x0.934, `hit` x0.851 then x0.857,
`death` x0.913 then x0.962, `purchase` x0.880, `multi_shot` x0.949, `level_up` x0.9996. The generated beds were
re-rendered with more headroom at their source instead (the generator's target is 0.72).

The hashes in the per-file table below are the hashes of the files as they are committed now, after
normalisation; `docs/audio/LEVELS.md` is the measurement of every one of them, regenerated by
`tools/audio/check_audio_levels.py`.

## Generated in-repo effects (added 2026-09-17, roadmap R6.2)

Nine more effects, authored by `tools/audio/generate_sfx.py` for the same reason as the beds: the source is the
repository, so there is no licence to honour. The 2026-09-13 review named exactly the holes they fill -- the bow
itself, the loot, the UI, the warning a boss gives before a special, the moment a wave ends, and a place for the
vigil to sound like a place. Measured peaks are in `LEVELS.md`, generated from these bytes.

| Committed file | Original file | Source | Use | SHA-256 |
|---|---|---|---|---|
| `audio/sfx/ambience_vigil.ogg` | *(none)* | generated in-repo | Wind under a run (ambience loop) | `1a979610ad4d07a69ef5de21e4219ebab76cc4d2abac4c1d0571f16f04768c3f` |
| `audio/sfx/bow_release.ogg` | *(none)* | generated in-repo | Release of a single arrow | `7e5ff569b80718b301b7e889435d6ac6e95e2671d4109c35440c01b41e929a28` |
| `audio/sfx/bow_release_heavy.ogg` | *(none)* | generated in-repo | Ultimate release | `be43271a0ffd385aa34d44051ca0ef11a635acd0099042cd48eb94b9829f6da9` |
| `audio/sfx/bow_release_light.ogg` | *(none)* | generated in-repo | Extra arrows of a volley | `d99055f589d9ccda67534d33345f510c7393f25701ba0443e494c619730e5761` |
| `audio/sfx/coin_pickup.ogg` | *(none)* | generated in-repo | Coins from a kill | `15ab667e920c595aaa2f12a65e7e8c11dc6ff023ba6b8d192f2aecb35e8ccbae` |
| `audio/sfx/telegraph_warning.ogg` | *(none)* | generated in-repo | A boss special is coming | `332ec7dc42f71bc180fabd78fe38b4b987d78ccc679463c028e4b848778fd663` |
| `audio/sfx/ui_close.ogg` | *(none)* | generated in-repo | Closing an overlay | `756eb67c92e06f9f08b7504e7d395c1f9f1e0450e9f4893f06095aa269da940a` |
| `audio/sfx/ui_tap.ogg` | *(none)* | generated in-repo | Menu and overlay button | `87637004ecad74dffe8abca58fb4613800caa2b3c230f7ee48704c7dd420d955` |
| `audio/sfx/wave_clear.ogg` | *(none)* | generated in-repo | A wave is behind the player | `f70785f43898c5db01b71a1a369956b2a9c8dc839ef550e19224b139b89b3044` |

## Per-file ledger

| Committed file | Original file | Source | Use | SHA-256 |
|---|---|---|---|---|
| `audio/music/world_tree_vigil.ogg` | `Heavenly Loop.ogg` | OpenGameArt / isaiah658 | Seamless background loop | `a842e9e054019132cacc8fd352e7b31c000ebb51e0b227a2511e1bccb4eb166e` | *retired 2026-09-17 (R6.1)*
| `audio/sfx/hit.ogg` | `Audio/chop.ogg` | Kenney RPG Audio | Hit | `93012e6c830238df3f155c304bf72f55139cc6f5836550dd42e812af341ff4da` |
| `audio/sfx/death.ogg` | `Audio/dropLeather.ogg` | Kenney RPG Audio | Death | `9ba2d4ae6aca12963f5f17e56e85123b4f711d04db730deb017d8fdf896c312e` |
| `audio/sfx/item_drop.ogg` | `Audio/drop_002.ogg` | Kenney Interface Sounds | Item drop/pickup | `4ac4d1cef7e936965cbf795852ca2020300b9e2ba7daa59f2bf4f1f7bf416218` |
| `audio/sfx/level_up.ogg` | `Audio/confirmation_004.ogg` | Kenney Interface Sounds | Level up | `96cddc814c098b3d2998455dae811fe456492dd88ff72a093330b34f34c41477` |
| `audio/sfx/boss_entrance.ogg` | `Audio/doorOpen_2.ogg` | Kenney RPG Audio | Boss entrance | `44fcd4be5fef22c10d32856784ab566413bda2b6ef9c3ae759cfde60d47311f9` |
| `audio/sfx/critical.ogg` | `Audio/impactMetal_heavy_003.ogg` | Kenney Impact Sounds | Critical arrow | `b0f2ba4dabde9a87eb9c188a19d31e0c2300fd321adeba08d3b9b8aa011d7037` |
| `audio/sfx/kill.ogg` | `Audio/impactPunch_heavy_002.ogg` | Kenney Impact Sounds | Enemy kill thump (layered under Death) | `7993dd4c156b9979ad69f17be5ebe31850b16039a3857f8477141be54dfee1b3` |
| `audio/sfx/chain_lightning.ogg` | `Audio/impactGlass_light_002.ogg` | Kenney Impact Sounds | Chain-lightning arc | `710db7451bb7857b9140dfe6afe4621ea2188ad65eda3919c569d1bb285b933d` |
| `audio/sfx/stun.ogg` | `Audio/impactBell_heavy_004.ogg` | Kenney Impact Sounds | Stun | `49fe4fafa2001bd0d312976796824571ca8429851f285997e1327d17bb34fd00` |
| `audio/sfx/multi_shot.ogg` | `Audio/pluck_002.ogg` | Kenney Interface Sounds | Multi Shot volley bowstring | `ee00f93e38d572731827b6546c3d9c9b355f4818bf12c6b4c372298e6fca6960` |
| `audio/sfx/purchase.ogg` | `Audio/handleCoins.ogg` | Kenney RPG Audio | Shop purchase | `7d2cfc55c2e24327c4c609835a87ef35b4138bae6d3057aa46e6c23c48510f2b` |

## Technical validation

All files decoded successfully during import. The seven Phase 18 effects were decoded with `soundfile` on 2026-09-14: 0.16–0.85 s, peaks 0.88–0.95, non-silent; per-cue playback is rate-limited in `AudioThrottle` so volleys cannot stack identical samples. The music is stereo 44.1 kHz and 33.652 seconds long. Effects are mono/stereo 44.1–48 kHz and 0.191–1.413 seconds long. Every file has non-empty, non-silent decoded samples and is directly supported by libGDX's Android audio backend.
