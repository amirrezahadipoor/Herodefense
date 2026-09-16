# Hero Defense — Critical Review (2026-09-13, main @ post-VFX)

> **Status added 2026-09-16: historical snapshot, superseded.** This review was written on 2026-09-13 for
> the then-current `main` (post-Phase 16.5). Its grades and counts describe that commit, not today's tree.
> The independent audit that supersedes it is [`docs/audit/AUDIT_2026-09-16.md`](audit/AUDIT_2026-09-16.md)
> (550/1000, granular criteria listed per category).
>
> Numbers in this file that have since been re-measured, with the current value and where it comes from:
>
> | Claim here (2026-09-13) | Measured 2026-09-16 | Produced by |
> |---|---|---|
> | "189 core tests" | 531 tests in 150 classes, 0 failures/errors/skipped | `:core:test` run recorded in `docs/ROADMAP_TO_1000.md` (Phase 83 entry) |
> | "116 files ... largest file 863 lines" | 329 Java files under `core/src` and `android/src`; largest `HeroDefenseGame.java` 1,519 lines | `find` + `wc -l`; tracked as roadmap R2.2 |
> | "13 MB assets ... 236 MB RGBA if all resident ... no measurement exists" | 14 MB payload on disk; decoded catalog **361,279,488 bytes (344.5 MiB)**, live combat set **76.8 MiB** against a 100 MiB budget | `android/assets/generated/asset_manifest.json`, gated by `RuntimeResidencyTest` |
> | "reviewed contact sheets ... style guide enforced by tests" | the HD enlargement had been produced by resampling, not by rendering; it was reverted and the provenance measured per sheet | `docs/art_reviews/INTEGRITY_RECOVERY_2026-09-16.md`, `docs/art_reviews/MASTER_TIER_PROVENANCE.md` |
>
> Findings that are **still open** are mapped to roadmap items, not repeated here: no tutorial or stat
> tooltips (R7.1/R7.2), Android Back unhandled (R7.4), optimiser imbalance and the flat mid-curve
> (R4.1/R4.2), four boss identities across twenty encounters (R3.2), no meta-progression (R3.3), thin audio
> (R6.1/R6.2), renderer coverage that only grepped source strings (R1.6), accessibility and localisation
> (R7.5/R7.3). The "source-string only" test criticism is partly closed: the validator now reports its
> measured gates separately from its config-presence checks.

Scope: everything in the repository as shipped on `main` after Phase 16.5. Grades are deliberately
strict (10 = polished commercial mobile title). Measurements were taken with the real game systems
(`BalanceSimulator` plus a "passive player" harness), the committed assets, and the last accepted
emulator captures. No physical device was available; anything device-only is flagged.

## Scorecard

| Area | Grade /10 | One-line verdict |
|---|---:|---|
| Architecture & code quality | 8.0 | Clean split (gameplay / render / input / polish), 116 files, 0 TODOs, largest file 863 lines. `HeroDefenseGame` is the one god-object. |
| Test discipline | 8.5 | 189 core tests, deterministic sims, evidence hash-binding, touch-only emulator journeys. Coverage of renderers is "source-string" only. |
| Combat feel (moment-to-moment) | 6.5 | Auto-attack, no player agency inside a wave beyond speed/potions. VFX/shake/hit-stop now sell impacts, but the loop is watch-and-manage. |
| Balance & difficulty curve | 5.5 | Optimizer wins 40/40 seeds with ≤10 % HP loss per wave; a passive player dies wave 16–42. Curve is flat in the middle (waves 25–80 nearly identical damage fractions). |
| Progression & economy | 6.0 | 5 stats, 40 items, 6 potions, 20 boss cards is enough depth, but there is no meta-progression, no unlocks, no run-to-run reason to replay. |
| Boss design | 6.0 | 4 mechanically distinct specials, but they are instant damage-events with no telegraph the player can react to; 20 boss fights reuse 4 identities. |
| Onboarding / teaching | 3.0 | No tutorial, no first-run hints, no stat tooltips; the shop/inventory value math is opaque to a new player. |
| UI / UX surfaces | 8.0 | Premium-v2 panels, consistent typography, ≥96 px targets, accepted emulator evidence for every surface. Android Back is not handled. |
| Art direction & consistency | 8.0 | Coherent forest-sanctuary palette, reviewed contact sheets, style guide enforced by tests. Some sprite sheets are oversized for their on-screen size. |
| VFX | 7.5 | Layered, budgeted, deterministic. Still ShapeRenderer polygons rather than authored textures; ambient is subtle to the point of near-invisibility at 1080p. |
| Audio | 5.0 | One music loop (1.2 MB) + 80 KB of SFX; frozen by roadmap. No boss theme, no wave-clear sting variety. |
| Performance & memory | 6.5 | 13 MB assets, but equipment sheets are 1920×768 each → 236 MB RGBA if all resident (lazy-loaded, so real residency is bounded; no measurement exists). No frame-pacing data. |
| Persistence & robustness | 8.0 | Lenient JSON, `validateAndRepair`, background save, 1-HP defeat path tested. Save format has no version field. |
| Release readiness (Cafe Bazaar) | 7.0 | Release workflow + lint exist; `versionName 0.1.0`, `minifyEnabled false`, `allowBackup=true` un-reviewed, no physical-device run. |
| Accessibility & localisation | 3.5 | English only in a Persian-market build, no text scaling, no colour-blind consideration for rarity glows. |
| **Overall** | **6.5** | Technically disciplined vertical slice with premium presentation; the *game* underneath is thin and easy for anyone who touches the shop. |

## Evidence behind the low grades

### Balance (5.5)
- `BalanceSimulator` (balanced talents + shop + best-item equip): **40/40 seeds reach wave 100**; mean
  damage-fraction per wave 0.03–0.10; wave 100 clears in ~32 s. There is no late-game tension.
- Passive harness (real systems, first reward card, no shop):
  - spends nothing → dies wave 16–42 (median 20);
  - spends talents only → 20/30 win;
  - talents + auto-equip → 23/30 win.
  So the entire difficulty lives in "did you open the shop"; once you do, waves 25–100 are a formality.
- Damage fraction is *lower* at wave 50 (0.03) than at wave 5 (0.10). The curve does not escalate.
- Crit chance is a flat 5 % with no stat that scales it; Luck only affects drop rate.
- Legendary drop rate 0.15 % base — over a 100-wave run most players will never see one.

### Combat agency (6.5)
- Hero is anchored at arena centre (`anchorHeroAtArenaCenter()` every frame). Inputs during a wave:
  speed toggle, open shop/inventory, quaff potion (auto). No dodge, no target, no ability.
- Boss specials trigger by range/cooldown and apply damage on the same frame
  (`BossSpecialAttackSystem.execute`) — the 0.5 s `specialAnimationSeconds` plays *after* the hit.
  Nothing is readable or counter-playable.

### Onboarding (3.0)
- grep for tutorial/hint/onboard in `core/src/main`: zero hits. First-time users get "NEW GAME" and a
  wave timer.

### Performance (6.5)
- Equipment attachment sheets: 40 × 1920×768 PNG (4.7 MB on disk, 5.9 MB RGBA *each*). Six can be
  resident at once (≈35 MB) — acceptable, but the hero is 160 px on screen; sheets are ~2× oversampled.
- Boss sheets 2048×1024; 11 sprite atlases = 69 MB RGBA if all loaded; inactive boss atlases are
  disposed, regular enemies are not.
- No startup-time, frame-time, or texture-residency measurement exists anywhere in the repo.

### Release hygiene (7.0)
- `allowBackup="true"` with an unversioned save JSON — a restored save from a future build has no
  migration hook beyond `ignoreUnknownFields`.
- `minifyEnabled false` on release; APK size and method count are un-shrunk.
- Android Back button/gesture: unhandled → on API 33+ predictive back will exit the game from any
  overlay.
- `supportsRtl` set but no RTL/Persian strings; label hardcoded "Hero Defense".

## What is genuinely strong
- Deterministic RNG threaded through combat, drops, and cards → every balance claim is reproducible.
- Evidence pipeline: every art batch has a hash-bound review, touch-only emulator captures, and a
  test that fails if a PNG changes silently. Few indie repos do this.
- Save robustness path (1-HP organic defeat, repair-on-load) is tested end-to-end.
- Visual style guide is enforced by tests, not by convention.

## Prioritised recommendations (if work continues)
1. **Difficulty**: add a late-game escalator (e.g. enemy damage `1.012^w` after wave 40, elite affixes
   every 10 waves) and re-tune until the optimiser loses ~15 % of seeds and passive players die
   before wave 30.
2. **Boss telegraphs**: move damage to the *end* of `specialAnimationSeconds` and draw a ground
   warning ring so Dodge/potion timing matters.
3. **First-run teaching**: three contextual one-line hints (first drop, first level-up, first boss).
4. **Back handling**: `Gdx.input.setCatchKey(BACK)` → close overlay / pause instead of exiting.
5. **Save versioning**: add `schemaVersion` to `GameState` and a migration switch.
6. **Texture diet**: downsample equipment sheets to 1280×512 (hero is 160 px) — ~55 % less residency.
7. **Localisation**: extract strings; a Persian pass is the obvious market fit for Cafe Bazaar.
8. **Measure**: one instrumented run recording startup ms, 1 % low frame time, and `Texture` count at
   wave 50 with a boss — record it next to the art reviews.

Physical-device caveat: all readability and performance statements derive from the API 15 emulator
(1080×2220) and static analysis; no real handset was tested.
