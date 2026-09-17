# Roadmap to a real 1000

> **خلاصه فارسی (۳ خط):** امتیاز مرجع این مخزن ۵۵۰ از ۱۰۰۰ است. عدد اصلی گزارش، «نمرهٔ تجربهٔ بازی» است (بخش ۴): نه فقط کد، بلکه دارایی‌ها، حجم محتوا، زمان بازی، نوآوری، جذابیت، رمز و راز و روایت. کارِ انتشار/درآمدزایی بیرون از محدوده است. این فایل برنامه‌ی بستن کسرهاست؛ هر تیک فقط با شاهد قابل بازتولید زده می‌شود و هیچ تستی برای سبز شدن شل نمی‌شود. دسته‌ی «آماده‌سازی انتشار» بنابر نظر صاحب پروژه بیرون از محدوده است، پس سقف این نقشه راه **۹۲۰ از ۱۰۰۰** است (۸۰ امتیاز آن دسته). ریزمترها و معیار پذیرش هر فاز، پایین‌تر در بخش‌های `R1`–`R8` آمده است.

**Repository:** `amirrezahadipoor/Herodefense` · **Baseline commit:** `49fa799` · **Audit:** [docs/audit/AUDIT_2026-09-16.md](audit/AUDIT_2026-09-16.md) (550/1000)

Everything below the Persian summary is in English on purpose: the roadmap is the repository's working
document, and the repository's language is English.

---

## 1. How this file is used

1. **No fake evidence.** A box is ticked only when the repository contains something a stranger can
   re-run: a command, a generated artifact, a CI run, a screenshot at a fixed seed. Prose is not
   evidence.
2. **No test may be weakened.** Removing an assertion, adding `assertTrue(true)`, `|| true`, `@Disabled`,
   or lowering a threshold is never the fix. If an assertion must change it becomes *stricter or equally
   strict about the real contract*, and the commit message says why.
3. **Documentation numbers are generated** by a tool in the repository, never typed by hand.
4. **One item, one commit, one push — immediately.** Local batches are not allowed to grow large; work
   is pushed as soon as it is verified, so neither the workspace nor the review can lose it.
5. **Failure is recorded**, never deleted: an abandoned item becomes `[!]` with its reason.

6. **Two rubrics, one headline.** The *repo rubric* (the 2026-09-16 audit, 1,000 points, release category
   excluded by owner direction) gates the code. The *experience rubric* in section 4 is the headline number:
   it scores the game as something a person plays. A completion claim needs both, and neither may be
   supported by prose alone.
7. **Nothing here is designed around revenue.** Monetisation, store pages and release preparation stay out of
   scope by owner direction; no system may depend on timers, paywalls or engagement metrics that exist to
   sell something.

Status legend: `[x]` verified · `[~]` in progress · `[ ]` not started · `[!]` stopped, with reason.

## 2. Phase plan (numbered, English)

`Phase 78` is complete and pushed. `Phase 79` onward is the remaining work, one phase per commit.

**Table A — repo rubric (the code and the assets it ships).**

| Phase | Title | Roadmap items | Status |
|---|---|---|---|
| **78** | Integrity recovery: reviewed runtime tier restored, contract tests un-gutted, hash ledger + resampling/ledger gates | R1.1 · R1.2 · R1.3 · R1.4 · R1.8 | `[x]` |
| **79** | Test-integrity meta-test: weakening a test fails the build | R1.5 | `[x]` |
| **80** | Gates that measure output, not script text | R1.6 | `[x]` |
| **81** | Emulator brightness contract restored honestly | R1.7 | `[x]` |
| **82** | Negative control for every gate (fixtures that must fail) | R1.9 · R8.2 | `[x]` |
| **83** | Review coverage for the ten post-audit art ids | R1.11 | `[x]` |
| **84** | Documentation honesty sweep (stale and inflated docs) | R1.10 | `[x]` |
| **85** | Dead code out, architecture ratchet in | R2.1 · R2.4 · R2.5 | `[x]` |
| **86** | Break up `HeroDefenseGame` into systems, with tests | R2.2 · R2.3 | `[~]` |
| **87** | Gameplay: one real player decision inside a wave | R3.1 | `[x]` |
| **88** | Boss identity variety across the 20 encounters | R3.2 | `[x]` |
| **89** | Meta progression, content breadth, run shape | R3.3 · R3.4 · R3.5 | `[x]` |
| **90** | Human playtest protocol and recorded findings | R3.6 | `[x]` |
| **91** | Balance program: scaling threats, telegraph contract, drop economy, generated docs, CI band | R4.1 – R4.5 | `[~]` |
| **92** | Runtime tier composed from the genuine master renders | R5.1 · R5.2 | `[~]` |
| **93** | Render pipeline: reliability, rendered grading, PBR maps, per-batch reviews, real VFX | R5.3 – R5.7 | `[~]` |
| **94** | Audio program: music breadth, SFX coverage, state machine, settings | R6.1 – R6.4 | `[x]` |
| **95** | Onboarding, tooltips, Persian + RTL, Back button, accessibility, store UI | R7.1 – R7.6 | `[~]` |
| **96** | Memory and performance program: compression, budgets, wave-50 residency, startup/APK | R8.1 – R8.5 | `[~]` |
| **97** | Re-audit with the same granular method and publish the repo-rubric score | Gate 1 of the definition of done | `[ ]` |

**Table B — experience phases (added 2026-09-16 at the owner's direction: assets, content, playtime,
human-feel innovation, engagement, secrets, narrative, and a 2026 benchmark).** These phases start from the
premise that a 1000-level game is not a codebase with a high score; it is a game that a person wants to keep
playing, and every item below has to end in something measurable.

| Phase | Title | Roadmap items | Status |
|---|---|---|---|
| **98** | Asset quality audit: every asset's texel density, contrast and framing measured | R9.1 | `[ ]` |
| **99** | Art upgrade batches for every category, delivered by real renders and reviewed per batch | R9.2 | `[ ]` |
| **100** | Shading pass: normal/roughness/AO maps for hero, bosses and creatures | R9.3 | `[ ]` |
| **101** | Palette, value range and gameplay-contrast pass with measured ratios | R9.4 | `[ ]` |
| **102** | Animation pass: real clip counts and frame coverage per entity | R9.5 | `[ ]` |
| **103** | Content register: machine-readable inventory of every game asset with an owner phase | R10.1 | `[ ]` |
| **104** | Creature families: new enemy archetypes with distinct mechanics and telegraphs | R10.2 | `[ ]` |
| **105** | Boss roster and elite variants across the act structure | R10.3 | `[ ]` |
| **106** | Biomes and arenas: four visually and mechanically distinct theatres | R10.4 | `[ ]` |
| **107** | Equipment, consumables, affixes and set bonuses | R10.5 | `[ ]` |
| **108** | Authored VFX library replacing shape primitives | R10.6 | `[ ]` |
| **109** | Playtime model: target sessions, measured against automated play | R11.1 | `[ ]` |
| **110** | Act structure: three acts of twelve waves with intermissions and mini-bosses | R11.2 | `[ ]` |
| **111** | Mirror/endless mode with scaling modifiers and local records | R11.3 | `[ ]` |
| **112** | Seeded daily trial, offline and deterministic | R11.4 | `[ ]` |
| **113** | Achievements and bounties (60+) that unlock cosmetics and codex pages | R11.5 | `[ ]` |
| **114** | Ascension tiers / new-game-plus modifiers | R11.6 | `[ ]` |
| **115** | Living arena: allies, critters, weather and a day/night cycle that changes waves | R12.1 | `[ ]` |
| **116** | Physicality: weight, knockback, hit-stop measured in milliseconds | R12.2 | `[ ]` |
| **117** | Expressive hero: mood state, contextual lines, near-death presentation | R12.3 | `[ ]` |
| **118** | Scripted beats: hand-authored moments inside runs | R12.4 | `[ ]` |
| **119** | Adaptive pressure with a documented policy and a visible indicator | R12.5 | `[ ]` |
| **120** | Accessibility and comfort: one-hand mode, palettes, text scaling, reduced flash | R12.6 | `[ ]` |
| **121** | Touch feel: haptic vocabulary, precision assist, mis-tap forgiveness | R12.7 | `[ ]` |
| **122** | The 30-second loop: input-to-feedback latency budget on the emulator | R13.1 | `[ ]` |
| **123** | Reward cadence: unlocks and drops per minute, verified by the simulator | R13.2 | `[ ]` |
| **124** | One-more-run hooks: visible seeds, personal bests, streaks | R13.3 | `[ ]` |
| **125** | Mastery curve and measurable skill gap | R13.4 | `[ ]` |
| **126** | Session boundaries: save anywhere, resume and first-wave budgets | R13.5 | `[ ]` |
| **127** | No dark patterns: checklist enforced by a test | R13.6 | `[ ]` |
| **128** | Secret registry: data-driven mysteries with deterministic triggers | R14.1 | `[ ]` |
| **129** | Environmental mysteries inside the arenas | R14.2 | `[ ]` |
| **130** | Lore fragments and a connected codex graph | R14.3 | `[ ]` |
| **131** | Rare run anomalies with seeds a test can replay | R14.4 | `[ ]` |
| **132** | A meta-mystery that spans save files | R14.5 | `[ ]` |
| **133** | Spoiler discipline: hidden content stays hidden outside the game | R14.6 | `[ ]` |
| **134** | Narrative bible, beat sheet and delivery map | R15.1 | `[ ]` |
| **135** | Cinematic timeline runtime (camera, letterbox, typewriter, skippable) | R15.2 | `[ ]` |
| **136** | Opening prologue animation | R15.3 | `[ ]` |
| **137** | Interludes for act transitions and boss beats | R15.4 | `[ ]` |
| **138** | Dialogue system with portraits and localisation-ready text | R15.5 | `[ ]` |
| **139** | In-world storytelling: props, epitaphs, arena state that changes | R15.6 | `[ ]` |
| **140** | Narrator stingers and per-act audio motifs | R15.7 | `[ ]` |
| **141** | 2026 benchmark rubric: reference titles and comparable criteria | R16.1 | `[ ]` |
| **142** | `docs/audit/BENCHMARK_2026.md` measured on our build | R16.2 | `[ ]` |
| **143** | Close the benchmark gaps, or record them as accepted trade-offs | R16.3 | `[ ]` |
| **144** | Experience re-audit with the granular method, both scores published | R16.4 | `[ ]` |
| **145** | Final claim gate: both rubrics at target, no open `[!]` | R16.5 | `[ ]` |

## 3. Baseline: where the 450 missing points are

| # | Category | Weight | Audit | Missing | Main reason | In scope |
|---|---|---|---|---|---|---|
| 1 | Architecture & code quality | 150 | 105 | −45 | `HeroDefenseGame.java` god class (1,519 lines / 49 methods / 150+ fields); dead `PostProcessRenderer`; no unit test on the riskiest file | yes |
| 2 | Tests, CI & trustworthiness | 150 | 85 | −65 | 10 neutralised contract tests, hash checks commented out, "gates" that only grep script source, luma threshold 34 → 20, `vfx-` screenshots excluded | yes |
| 3 | Gameplay depth & content | 120 | 75 | −45 | hero locked to the arena centre during a wave; 4 boss identities across 20 encounters; no tutorial; 2.2 h runs | yes |
| 4 | Balance & difficulty curve | 80 | 38 | −42 | project docs admit a flat mid-curve; optimiser policy wins 40/40 seeds | yes |
| 5 | Visual assets | 130 | 62 | −68 | the "HD" batch was a NEAREST resize of the reviewed sheets; no PBR maps in the repository | yes |
| 6 | Audio | 50 | 22 | −28 | 1 music loop + 11 SFX (~1.3 MB) | yes |
| 7 | UI, onboarding, localisation | 100 | 48 | −52 | no tutorial code, English-only, Back button unhandled | yes |
| 8 | Performance, memory & size | 80 | 40 | −40 | 1,277 MiB decoded texture residency, ~230 MiB estimated at wave 50, no compression, no budget test | yes |
| 9 | Release preparation | 80 | 45 | −35 | version 0.1.0, `minify` off, no tag/release | **no — excluded** |
| 10 | Documentation & honesty | 60 | 30 | −30 | `ASSET_SCORE_950.md` / `RELEASE_v0.5.0-vibrant-950.md` were built on resized art | yes |

**In-scope target: 920/1000.** No document in this repository claims a new score until the same audit is
re-run on a finished round (Phase 97).

---

## 4. The experience rubric (the headline number)

The repo rubric asks "is the code and are the assets honest and well built". This one asks the question the
owner actually cares about: **is this a game someone would rather play than the 2026 titles it competes
with?** It is scored out of 1,000, every category needs evidence of the kind described, and a category cannot
pass on prose.

| # | Category | Weight | What earns the points | How it is measured |
|---|---|---|---|---|
| 1 | Asset quality | 150 | every shipped asset is drawn at or above its on-screen texel density, reads at 1080p, has authored shading detail, and matches its category's style guide | texel-density audit per asset (R9.1), contrast ratios measured from screenshots (R9.4), animation coverage per entity (R9.5) |
| 2 | Content volume and variety | 130 | creature families with distinct mechanics, boss roster with identities, four biomes, equipment and affix breadth, authored VFX | counts and variety matrix from the content register (R10.1), each entry traced to a reviewed render or authored file |
| 3 | Playtime and pacing | 110 | 8–12 minute runs, a 3-act arc worth 20–30 hours to full completion, escalation that stays legible, no filler | playtime model against automated play (R11.1) and the act structure (R11.2) |
| 4 | Systems depth and mastery | 120 | decisions inside a wave, build-defining equipment, meaningful combos, a skill ceiling an expert can climb | simulator policy gap (R13.4) plus unit-tested systems |
| 5 | Human-feel innovation | 110 | living arena, physicality, an expressive hero, hand-authored beats, adaptive pressure that is documented rather than hidden | each item has a dedicated test or a measured capture (R12.x) |
| 6 | Engagement and reward cadence | 110 | feedback under 100 ms, unlocks paced across a session, one-more-run hooks, respect for the player's time | latency budget on the emulator (R13.1), cadence from the simulator (R13.2), the no-dark-patterns checklist (R13.6) |
| 7 | Secrets and mysteries | 80 | discoveries that are findable, not random; lore that connects; rare events worth retelling | deterministic trigger tests (R14.1/R14.4), codex graph connectivity (R14.3), spoiler gate (R14.6) |
| 8 | Narrative and cinematics | 100 | a prologue worth watching, interludes that land the story beats, in-world storytelling, a skippable timeline runtime | narrative delivery map (R15.1), timeline player tests (R15.2), recordings of the prologue and one interlude |
| 9 | Presentation feel | 90 | audio identity, UI that reads at a glance, haptics, transitions and ceremony that feel authored | audio asset register with licences, screenshot set with brightness contract, haptic vocabulary list |

**Gate 1 (repo rubric):** ≥ 900 of 920 in-scope points at Phase 97.
**Gate 2 (experience rubric):** ≥ 900 of 1,000 at Phase 144, with no category below 80 % of its weight.
**Claim rule:** only when both gates pass may this repository describe the game as "1000-level", and the
sentence has to name the two scores and the commit they were measured on.

## R1 — Truth, tests and verification integrity  `+95` — complete

- [x] **R1.1 Restore the reviewed runtime tier and gate resampling.** Every sheet Phase 76 enlarged with
  NEAREST is back to the reviewed runtime tier; the validator refuses sheets whose transitions sit on a
  resampling grid.
  *Evidence:* `tools/visual/restore_runtime_tier.py --apply`, manifest
  `engineVersion 78.0-integrity-recovery-runtime-tier`, `decodedBytes 361,279,488` (344.5 MiB),
  `docs/asset_hashes.json`, `docs/art_reviews/INTEGRITY_RECOVERY_2026-09-16.md`, validator PASS.
- [x] **R1.2 Restore the gutted contract tests.** Premium contract tests back to full bodies; the four
  assertions that only *looked* strict were repaired into explicit, documented allow-lists.
  *Evidence:* restored from `5374f2c^`; `grep -rn "assertTrue(true)\|@Disabled" core/src/test` → no hits;
  the two manifest inconsistencies it uncovered were fixed in the data, not in the tests.
- [x] **R1.3 Art history measured, not asserted.** `tools/visual/classify_master_tier.py` +
  `docs/art_reviews/MASTER_TIER_PROVENANCE.md`: 131/153 sheets never re-rendered after the resize+grade,
  59 measured resampled copies, 27 with genuine detail gain, 67 inconclusive on flat art.
- [x] **R1.4 Art can no longer change silently.** `docs/asset_hashes.json` pins every shipped PNG;
  `AssetIntegrityTest` and the validator's ledger gate both fail on drift (negative control verified).
- [x] **R1.5 Meta-test: weakening a test fails the build.** `TestIntegrityTest` scans
  `core/src/test/java` and `android/src/androidTest/java` for vacuous assertions, `|| true`,
  `@Disabled`/`@Ignore` and softened-in-prose checks, and fails the build on a hit. Exemptions must be
  written on the line as `// integrity-exempt: <reason>` (minimum reason length enforced).
  *Evidence:* the scanner found its own first version's leftover comments and now passes;
  `theScannerDetectsAWeakenedTestAndRejectsAnEmptyExemption` proves it fires on a vacuous assertion, a
  softened comment and a disabled test, rejects an empty exemption and accepts a documented one.
  The last two `relaxed` comments in the premium arena test were replaced by a real check against the
  hash ledger instead of being kept, and the emulator luma line now carries an explicit
  `integrity-exempt` pointing at R1.7.
- [x] **R1.6 Gates that measure output.** The validator now reports
  `Measured gates: 7 (asset hash ledger, grade alpha round-trip, pivot stability, edge safety,
  silhouette, no resampling signature, reviewed-tier pinning) | config-presence checks: 3 (palette
  strings, bloom/GTAO flags, outline colours)`, so a string search is never counted as a quality gate.
  The wildcard tier set was replaced by an exact reviewed-tier pin, and the divergence between
  `config.py` (which Phase 54-55 raised to 3/32 and 4/48) and the art that was actually rendered is now
  printed: *config asks for more than the art was rendered at*. Tests in
  `tools/visual/tests/test_art_gates.py::ReviewedTierPinningTest` keep the mapping explicit.
- [x] **R1.7 Emulator brightness contract.** The blanket floor is gone, replaced by per-screenshot
  references measured on the CI emulator: `AndroidTouchSmokeTest` compares every capture — **including the
  `vfx-*` frames that used to be skipped** — against the mean luma and lit-pixel fraction recorded in
  `SCREEN_REFERENCE`, with ±8 luma tolerance, a lit-fraction tolerance of 0.10, and an absolute floor of 30
  so no screen can be OLED-black. The old threshold of 20 could not tell a dark screen from a black one and
  let the vfx frames pass unmeasured.
  *Evidence:* run [`35078435013`](https://github.com/amirrezahadipoor/Herodefense/actions/runs/35078435013)
  (commit `0ccc92c`, both workflows green) uploaded `brightness-measurements.txt` with all 28 captures:
  minimum mean luma **34.17** (`opening-line-three`), maximum **48.30** (`vfx-combat-0`), minimum lit
  fraction **0.9141** (`tree-siege`). Those 28 values are the table; a new capture is reported as
  UNREFERENCED until its measurement is added in the same commit.
- [x] **R1.8 Audit published inside the repository.** `docs/audit/AUDIT_2026-09-16.md` + README pointer.
- [x] **R1.9 Negative control for every gate.** Fixtures that must fail exist for the resampling gate
  (NEAREST 2× and 3×), for hash-ledger drift, for the test-integrity scanner, and now for the residency
  budget (an over-budget set and a sheet that was quadrupled both throw). The review-coverage gate gets
  its control together with R1.11, when the gate exists.
- [x] **R1.10 Documentation honesty sweep.** Withdrawn: `ASSET_SCORE_950.md`; correction banner:
  `RELEASE_v0.5.0-vibrant-950.md`; annotated: `ROADMAP.md` Phase 54–77 claims and the "Standing Rules
  for HD". Closed with:
  a status header on `CRITICAL_REVIEW_2026-09-13.md` that names the four numbers which changed and the
  measurements that replaced them (531 tests / 150 classes, 329 Java files, 1,519-line god class, decoded
  catalog 361,279,488 bytes, combat set 76.8 MiB) and maps every still-open finding to a roadmap item;
  a "budgets are targets, not delivered facts" note in `VISUAL_STYLE_GUIDE.md`; the stale budget and
  contract values in `ASSET_ENGINE.md` (`decodedCatalog ≤ 335 MB`, `visualQuality == premium-v2`,
  `engineVersion == 28.6`, none of which the shipped manifest satisfies) replaced by references to the
  manifest and the core test that enforces them; and a batch-record banner on 21 `docs/art_reviews/*`
  documents so their accepted-run numbers are not read as today's values.
  *Evidence:* a scan for count-like claims across `docs/**/*.md` leaves 8 documents, all of them either
  bannered batch records or value tables that now defer to the manifest (command and result recorded in the
  progress log below).
- [x] **R1.11 Review coverage for the ten post-audit art ids.** The claim is now true and enforced.
  `docs/art_reviews/POST_BATCH_EQUIPMENT_CONTRACT.md` states exactly what is verified for those ten ids
  (runtime contract, hash ledger, no resampling signature, reviewed tier) and what is *not* claimed (no
  rendered acceptance pass) instead of pointing at the premium-v2 review that never saw them;
  `restore_runtime_tier.py` writes the same pointers, so a regeneration cannot drift back.
  Revision labels are bound to documents: `tools/visual/write_review_labels.py` generates a label block in
  every review document from the manifest, `ReviewLabelBinding` + `ReviewLabelBindingTest` in core and the
  validator gate *review label binding* fail if a label is not listed in the document that claims to cover
  it, and the equipment contract test now expects a different document for the premium batch than for the
  post-batch ids instead of one document for all 46.
  *Evidence:* validator `Measured gates: 8 (... review label binding ...)`; full suite 150 classes /
  531 tests / 0 failures; `write_review_labels.py --check` current in 10 documents.

## R2 — Architecture and code quality  `+45`

- [x] **R2.1 Remove or finish `PostProcessRenderer`.** Deleted: it was a 26-line stub whose `begin`/`end`
  were empty and which loaded `shaders/post-process.vert`/`.frag`, two files that have never existed in
  `android/assets`. The deletion is guarded by a packaging test instead of a comment:
  `InternalAssetReferences` extracts every literal `Gdx.files.internal("...")` from `core/src/main/java`
  and `android/src/main/java` (comments and javadoc stripped first) and `InternalAssetReferenceTest` fails
  when a literal has no file under `android/assets`, with a negative control and a count of the computed
  paths that cannot be checked statically.
  *Evidence:* `internal asset references: 5 literal paths checked, 15 computed path(s) not checkable
  statically`; `theDeadPostProcessRendererIsGone` asserts the stub stays gone.
- [~] **R2.2 Break up `HeroDefenseGame`** into `WaveDirector`, `CombatSystem`, `ArenaRendererFacade`,
  `HudFlow`, `SessionController` with a ~400-line ceiling per class, validated by the full suite plus a
  before/after emulator smoke run.
  *Slice 1 (done):* `presentation/RunPresentationSystem` now owns the seven presentation methods that grew
  every time an effect was added — defeat bursts, boss entrance, elite fragments, wave reflection lines,
  collection sparkles and pending pickups — behind a two-method `BeatSink` (`showBeat`, `save`) so the game
  class stays the only writer of the story line. `HeroDefenseGame` went from **1,519 to 1,447 lines** and the
  ratchet freeze was lowered to the measured size in the same commit, so the exception cannot be used as
  headroom. The layered-event test was strengthened rather than moved: it now scans the game class *and* the
  presentation system and requires each layered effect to be bound **exactly once** (twice for tree
  destruction, which has a world-tree and a grove-tree binding).
  *Slice 2 (done):* the whole per-screen touch chain moved out of the game class into
  `input/ScreenTouchRouter`, which implements `TouchInputController.Listener` and reaches the game only
  through a nested `Host` port (29 readers, 5 setters, 7 commands, and one counter for the handled-touch-up
  statistic). The chain was moved **verbatim** — a whitespace-insensitive comparison of the 8,184-character
  normalised body against the pre-extraction file is byte-identical — so no behaviour changed while
  `HeroDefenseGame` went from **1,447 to 1,201 lines**. The game keeps the `setInputProcessor` registration
  and the `viewport` it needs, the shop-tab field it renders, and the story line it writes. The ratchet freeze
  dropped to 1,201 / 91 in the same commit, and a new guard test (`theTouchChainLivesBehindTheRouter`) fails
  if any touch layout or touch controller is referenced from the game class again.
  *Slice 3 (done):* the per-state frame build (the audit's `ArenaRendererFacade` and `HudFlow` items) moved
  into `presentation/ScreenStateComposer`. It draws the arena, actors, effects, floating numbers, the HUD and
  all twelve overlays through a 51-getter `Host` port and receives only the camera and the sprite batch it is
  allowed to drive; the 5,492-character normalised body is byte-identical to the pre-extraction method, so the
  frame the emulator captures is built by the same code. `HeroDefenseGame` went from **1,201 to 1,072 lines**
  (29 % smaller than the 1,519-line god class the audit measured) and the ratchet freeze dropped to
  1,072 / 92. The guard test now also fails if any per-state draw call returns to the game class.
  *Slice 4 (done):* the wave lifecycle that ended `updatePlaying` (98 lines: hero death with the tree-fall burst
  and the epilogue choice, the level-up pause, the wave advance with its boss-entrance beat and reflection line,
  the reward-card offer, the planting ceremony and the completed run) moved into `gameplay/WaveDirector`. The
  director reaches the game only through a five-method `Host` (`gameState`, `transitionTo`, `saveNow`,
  `showWaveReflection`, `beginPlantingCeremony`), so screen transitions and saving stay in one place and the
  per-frame call site is a single line: `waveDirector.afterCombat(gameOver, killRewards.levelsGained() > 0)`.
  The move is statement-for-statement identical to the removed block — the two `waveCompletion != NO_CHANGE`
  guards collapsed into one early `return`, and `simulationSeconds += simulationDelta;` stayed in the game
  class because it is not wave flow. `HeroDefenseGame` went from **1,067 to 1,056 lines**; the director handle
  is one new field (92 → 93), recorded in the ratchet freeze instead of hidden. The layered-event guard was
  extended rather than relaxed: it now scans four files (game, `RunPresentationSystem`, `ScreenStateComposer`,
  `WaveDirector`) and still requires each layered effect exactly once.
  *Slice 5 (done):* the combat frame moved into `gameplay/CombatSystem` — the hero's arrows with every effect they
  cause (damage text, hit sparks, chain arcs, stun sparks, muzzle flash, the critical hit-stop, the kill shake),
  the hero taking melee back with its hit feedback, the auto-potion rule, and the payout a kill owes: item and
  potion drops, kill rewards with the coin text and the level-up jingle, the elite affix tick, the codex unlocks
  and the pickup sweep with auto-sell. It is handed its sixteen collaborators at construction instead of pulling
  them from the game, it takes `settings` per call because auto-sell rules can change mid-run, and it returns one
  record, `CombatSystem.Frame(gameOver, leveledUp)`, which is exactly what the wave director needs. The class is
  76 lines shorter (1,056 → **980 lines**, 35 % below the 1,519-line god class the audit measured) with one field
  for the handle (93 → 94), recorded in the ratchet freeze in the same commit.
  *Slice 6 (done):* the run session moved into `gameplay/SessionController` — a fresh run, the death screen's
  "again" (new seed, same tier), ascension (heartwood plus one tier), and picking a saved run back up on the
  screen its save was interrupted at (draft, boss reward, ceremony, talent points, replayed prologue, or the
  next wave). The three ways to start a run used to carry three copies of the same fifteen-line reset, which is
  how a god class drifts: change the reset once and two paths keep the old behaviour. They now share one
  `prepareFreshRun()`, in the same order as before. `HeroDefenseGame` 980 → **954 lines** (37 % below the 1,519
  the audit measured), fields 94 → 95, ratchet freeze lowered in the same commit.
  *Slice 7 (done):* the two ceremonies moved into `gameplay/CinematicFlow` — the prologue that opens a run
  (which snapshots the opening tier so a save replays the right lines) and the grove planting every fifty waves
  (which tree is being planted, the water the can drops, and the wave hand-off, including the boss that walks in
  at that moment). The layer ticks that run in every state stay in the game; the flow owns only what a ceremony
  decides. Before this the state machine, the water-drop accumulator and the hand-off were inline in `render`
  and `updateCinematic`, three hundred lines apart from the ceremony objects that drove them. `HeroDefenseGame`
  954 → **927 lines** (39 % below the 1,519 the audit measured), fields 95 → 94, ratchet freeze lowered in the
  same commit.
  *Slice 8 (done):* renderer ownership moved into `render/RenderStack` — the twenty-seven renderers and the
  sprite batch are now declared, created and closed in one place (the audit's `ArenaRendererFacade` item). Three
  blocks used to have to be edited in step in the game class, and a forgotten close call was a silent leak; the
  stack's `close()` mirrors the original teardown statement for statement, in the same order, renderer by
  renderer and then the batch. Nothing draws differently: the frame still asks the same objects for the same
  calls, and the twenty-seven `ComposerHost` getters now read from the stack. `HeroDefenseGame` 927 → **797
  lines** (47 % below the 1,519 the audit measured; fourteen fields became one), fields 94 → 68, ratchet freeze
  lowered in the same commit. This is the first slice shipped on the roadmap's new fast test loop: 573 tests in
  26 seconds instead of a seven-minute wait.
  *Slice 9 (done):* the frame moved into `presentation/FrameDriver` — the frame order (audio, the per-frame
  system ticks, the decision between simulation and ceremony, drawing), the wall-clock pause record behind the
  Long Pause secret, the two timed story lines (a wave reflection, an idle whisper), the ambient clock the arena
  reads, and the game-over presentation timer with its ten-second cap. The logic moved, it was not re-decided: a
  whisper still holds the simulation while it is up and a story line still does not. Two new ports made the frame
  testable (`audio/AudioFrame` for the two calls a frame makes, and an injected `NanoClock` so a five-minute pause
  can be asserted in microseconds), and `FrameDriverTest` covers the six behaviours above with real systems
  behind them. `HeroDefenseGame` 797 → **760 lines** (half of the audited 1,519), fields 68 → 61.
  *Remaining:* the host adapters that keep the game the single owner of its state, the before/after emulator
  smoke comparison, and the 400-line ceiling — with the honest note that the last 200 lines are the public
  surface plus five ~20-line adapters, so reaching 400 would mean trading cohesion for a line count (recorded
  as a follow-up for phase 96, which measures memory and startup, rather than pretended).
- [~] **R2.3 Unit-test the extracted systems** (spawn scheduling, damage, reward selection, save/restore).
  *Started with the first two slices:* `WaveDirectorTest` (five cases: hero death fells every tree on the epilogue
  screen and persists it, a level-up opens before the wave advances, an idle frame only offers the reflection and
  writes nothing, clearing wave 19 announces the wave-20 boss and rolls the run forward, and wave 200 offers the
  final card before the run may end) and `SessionControllerTest` (seven cases: a fresh run discards the save and
  opens the draft, a same-tier restart keeps tier and heartwood, an ascension climbs exactly one tier and pays,
  continuing without an offer does nothing, a save closed mid-draft resumes at the draft, `canContinue` refuses a
  dead hero and a completed run, and the opening tier comes from the snapshot when there is one).
  *Testability that made it possible:* the extracted systems used to name `GameAudioManager` and
  `LocalSaveRepository` directly, and both open libGDX resources in their constructors, so no core test could
  instantiate them. Two one-method-wide interfaces now sit in between — `audio/AudioPlayback` and
  `save/RunSaveRepository` — implemented by those classes; `WaveDirector`, `CombatSystem`, `CinematicFlow` and
  `SessionController` depend on the interfaces, and the tests record cues and saves in fakes. The remaining
  extracted systems get their tests when their slice lands.
- [x] **R2.4 Architecture ratchet test.** `ArchitectureRatchet` + `ArchitectureRatchetTest`: files under
  `model/` and `balance/` may not import `render`/graphics types (currently 0 violations), no class over
  600 lines or 40 instance fields, and the four pre-existing offenders are frozen **at their measured
  size** — `HeroDefenseGame` (760 lines / 61 fields after roadmap phase 86 slice 9),
  `CombatEntityRenderer` (681 / 13 after roadmap phase 87 and 88),
  `BalanceSimulator` (639 / 23), `model/GameState` (592 / 80). The freeze may only shrink: the ratchet fails
  if a frozen class grows, and it fails if an entry is left behind after the class comes inside the limits,
  so the exception list is self-cleaning. Negative controls cover a rendering import in `model`, an
  700-line class, a 45-field class, and a frozen offender that grows.
- [x] **R2.5 Static analysis in CI** (ErrorProne or SpotBugs + PMD), findings triaged not silenced.
  *Two analysers, because they see different things.* **SpotBugs 4.8.6** (Gradle plugin 6.0.26, effort MAX,
  confidence LOW) reads the compiled bytecode of main **and** test sources: null dereferences, ignored return
  values, exposure of mutable state, dead stores. **PMD 7.7.0** reads the source: unused or shadowed declarations,
  missing braces, resource leaks, collections that should be the interface or an `EnumSet`. Neither is wired into
  `test`, so the fast loop and the full suite keep their pace; `./gradlew :core:ciStaticAnalysis` runs all four
  reports and is what CI executes next to the tests, uploading both HTML reports as an artifact on failure.
  *The triage is the item.* First pass: **306 PMD findings** and **171 SpotBugs findings** in main, **3,198** and
  **7** in test. Around fifty of them were real and are fixed in code: a dead `nodesToApply` budget and a dead
  distance store (`UnusedAssignment` / `DLS_DEAD_LOCAL_STORE`), a `keySet()` + `get()` loop
  (`WMI_WRONG_MAP_ITERATOR`), `% 2 == 1` for odd (`IM_BAD_CHECK_FOR_ODD`), an integer division cast to float
  (`ICAST_IDIV_CAST_TO_DOUBLE`), a `volatile long` incremented from the input callback (`VO_VOLATILE_INCREMENT`),
  `toUpperCase()` without a locale (`DM_CONVERT_CASE`), a hand-rounded `6.2831f` circle
  (`CNT_ROUGH_CONSTANT_VALUE`), ten `catch (NullPointerException)` parses that now check for `null` explicitly
  (`DCN_NULLPOINTER_EXCEPTION`), two uncalled private methods, a write-only field and its only caller's parameter,
  a duplicated `secondTreePlanted` sync, four repeated string literals that became constants, two identical switch
  branches, `EnumSet`/`TreeSet` in signatures that became `Set`, a `continue`-as-last-statement loop, an untested
  resource in `UiFrameRendererTest` (now try-with-resources), two float loop indices, and a test helper that
  returned `null` for an unreadable PNG header instead of an empty array. Every remaining finding class is
  **excluded with a written reason** — 16 rule classes, each reasoned where it is excluded: the accessor naming
  convention (145), inline gameplay thresholds (99), reference releases (15), record compact constructors (2),
  deliberate identity comparisons (7), per-frame renderer allocations (6), and on the SpotBugs side the public
  simulation data model (95 + 2), constructor injection (27), live-object accessors (3), exact float tie-breakers
  (2), fields read from tests or the save format (5), and the single-threaded `GameFonts.shared` lifecycle (4).
  Test sources get their own ruleset: the same rules minus four that describe test-writing style rather than
  defects (`UnitTestAssertionsShouldIncludeMessage` 2,505, `UnitTestContainsTooManyAsserts` 553,
  `SimplifiableTestAssertion` 28, `SystemPrintln` 25 — the sweeping gates print their CSV tables into the CI log on
  purpose). The audit trail is `docs/STATIC_ANALYSIS.md`, which lists every fix and every exclusion with its
  reason and its count; the configs sit in `core/config/pmd/` and `core/config/spotbugs/`. After triage the task is
  green on an untriaged-free report, so a new finding shows up as a failing step rather than as noise in 300
  standing warnings.
- [x] **R2.6 A test budget: the suite's cost is measured, and the local loop is not a second CI.**
  *Measured 2026-09-16 (157 classes, 579 tests, `./gradlew :core:test`):* three suites spend **311 of the
  suite's 322 seconds of test time** — `TrialSimulationTest` 109 s, `RewardCardSimulationTest` 106 s,
  `AscensionGateTest` 96 s — six cases between them. The other 154 classes cost about ten seconds
  together. Two changes follow from that number, neither of which lowers a ceiling:
  *Parallel forks.* `core/build.gradle` now runs two test JVMs when the machine has the cores (opt out with
  `-PserialTests`). The classes are independent and every gate seeds itself, so this is scheduling only:
  the full suite went from **7 m 29 s to 3 m 45 s** with the same 579 tests and the same numbers.
  *A local fast loop.* The three sweeping suites were taken out of the unit loop and run in **21 seconds**.
  R4.5 finished the job with a `balance` tag instead of a flag: `:core:test` is the unit loop (**31 s** measured,
  four heavy suites excluded), `:core:balanceGate` is the sweeps (**4 m 47 s**), `:core:check` is both, and CI runs
  the two as separate jobs — so a balance break fails a job called *balance gate* rather than one line inside a
  650-test run, and no suite runs twice.
  *What was checked before assuming anything:* the unit layer has no filler to trim — 529 `@Test` methods
  were compared by normalised body, **zero** are duplicates of another case, and only five are one-liners
  (each asserting a real edge: a null text, an empty selection, a zero-velocity rotation, an empty arena
  tap, the application adapter type). The cost is in six cases that sweep whole runs, not in careless
  cases. **Rule from here on: gates are never trimmed for speed; duplicated work is.**

## R3 — Gameplay depth  `+45`

- [x] **R3.1 Player agency inside a wave — tap-to-focus.** The bow used to pick the nearest foe and the only
  in-run choice happened between waves, which is exactly the "hero is locked to the arena centre" finding of
  the audit. Tapping an enemy now marks it for **6 s**: the bow switches to the marked target while it is
  inside reach and every arrow into it carries **×1.2** damage, a second tap refreshes the window, and a tap on
  empty ground releases the mark, so a bad pick is always undoable. The choice is touch-only and readable —
  corner brackets pulse around the marked enemy and fade as the window runs out, and the tap answers with the
  usual ripple and haptic.
  *Implementation:* `gameplay/FocusFireSystem` (deterministic, platform independent: window, tap radius,
  damage multiplier as constants), `Enemy.focusMarkSeconds` (kept separate from the Crown mythic mark so the
  two never stack by accident), target selection plus damage in `HeroAutoAttackSystem`, corner brackets in
  `render/FocusMarkRenderer`, and the routing rule in `ScreenTouchRouter` — any tap inside a running wave that
  no HUD element claimed marks or releases.
  *Evidence:* `FocusFireSystemTest` (8 cases: mark switches the bow off the nearer foe, empty-ground release,
  expiry returning to the nearest target, dead/silent enemies never marked, bosses markable and dropping out
  on death, nearest-candidate tie broken on the lower id, the 56 px radius as a hard limit, degenerate input
  ignored) and three new cases in `HeroAutoAttackSystemTest` (marked target overrides proximity, damage is
  exactly ×1.2, an expired mark falls back to the nearest foe). The balance sweep is untouched because a run
  without taps behaves exactly as before.
  *Cost:* `HeroDefenseGame` stayed inside its ratchet freeze (1,072 → 1,067 lines after three arena queries
  moved to `gameplay/ArenaQueries`); `CombatEntityRenderer` gained 13 lines and one field for the shared
  sprite-box helper, recorded in the freeze ledger instead of being hidden.
- [x] **R3.2 Boss identity variety — eight fight scripts over forty encounters.** The four authored boss
  identities used to fight identically every time, so the fortieth boss was the first one with a bigger health
  bar. A **fight script** now rides on top of the identity: `MEASURED` (the authored fight, the reference),
  `CRYPTIC_TELL` (the tell drawn at 0.85×, the hardest in the roster to read), `SNAPPING_TELL` (0.95×),
  `ENRAGED_HEART` (its tell shrinks below 40 % health), `PATIENT_WARDEN` (a 1.25× tell), `TWIN_TELEGRAPH` (every
  special lands twice for half the damage each — two dodge chances on one warning), `BULWARK` (the biggest tell,
  1.35×) and `ASSASSIN` (a small tell behind two half hits, shrinking further when wounded).
  `BossEncounterTable` maps encounter number to script with pure arithmetic — a fixed permutation that shifts
  every lap — so no encounter repeats the script before it, all eight appear inside any twenty consecutive
  encounters, and a save file, the balance sweep and the player always agree on what is about to walk in.
  *What four drafts measured, and why the shipped roster is narrower than the plan.* Every axis that moves the
  combat simulation was tried and then dropped with numbers attached: scaled per-hit damage (up to 1.30×), a
  variable special cycle (0.70–1.30×), script-specific legs and reach, and finally the warning **length** itself
  (0.28–0.85 s). Each moved a single-wave spike cell in `TrialSimulationTest` / `AscensionGateTest` by 15–45 %
  (worst: `MISERS_PACT + FAMISHED_EARTH` 0.32 → 0.44 against a 0.40 ceiling, `LIFESTEAL` forced at Boss 26
  0.295 → 0.403) — not because the average pressure changed, but because the sweep reads one deterministic
  two-hundred-wave stream, and changing *when* a boss warns reshuffles every draw after it. Pinning the warning
  length at the reference 0.50 s made four of the six cells the drafts broke reproduce the phase-87 numbers
  **bit for bit** (0.3348112 / 0.31112048 / 0.29544255 / 0.33877918, identical to the pre-phase build). The
  shipped roster therefore pins physics and timing — one cycle, one warning length, the identity's own legs and
  reach — and varies only what the simulation cannot feel: tell size, hits per warning, and the enrage tell.
  `BossFightScriptTest` asserts those pins, so a later phase that wants warning-length or tempo variety has to
  unpin them on purpose and re-run the sweep. One cycle still lands exactly the reference damage
  (`specialDamageMultiplier() = cycleMultiplier / hits()`).
  *The gates themselves got fixed too.* Phase-88's measurements showed the spike statistic was a coin flip: one
  build, one cell, nine seeds spanning 0.27–0.46 — and the phase-87 build's own worst seed sits at 0.64, so the
  median of three was the only thing passing it. The spike matrices in `TrialSimulationTest` and
  `AscensionGateTest` now sample **five seeds instead of three**, with every ceiling left exactly as Phase 26.1c
  derived it. `SimulatorPolicyTest`'s Evolution check likewise became a five-seed matrix (at most one focused
  Evolution per run, and the mechanism must fire somewhere in the matrix), because the fork only opens in a run
  whose economy reaches level 10 *and* the price — measured 4 of 9 seeds on the shipped build, 7 of 9 before.
  *Evidence:* `BossEncounterTableTest` (five cases over all forty encounters: the encounter-to-script mapping,
  the twenty-window property, the no-consecutive-repeat property and the out-of-range fallback);
  `BossFightScriptTest` (eight cases: eight scripts, no two parameter sets alike, the sweep-safe axes pinned,
  values inside sane bounds, the roster varying tell size, hit count and reaction, per-cycle damage parity, no
  hit above the reference, unknown names falling back to the measured fight, and the enrage shrinking the tell
  while leaving the warning and the cycle alone); three new `BossSpecialAttackSystemTest` cases that assert the
  *fight* changed, not just the table — every encounter warns for the reference window and lands exactly when it
  ends, the twin strike deals both half hits inside one warning, and an enraged boss warns and arms exactly like
  a healthy one while its tell shrinks; `BossFactoryTest` locks what the factory applies to the stats.
  *Deferred on purpose:* warning-length and tempo variety are recorded as a revisit for a later phase, with the
  measurements above; the four boss **visual** identities also stay as they are, because new art for extra
  identities is phase 99/105 work.
- [x] **R3.3 Meta progression — twelve trophies that outlive the run, with a save migration.**
  The game already kept heartwood, ascensions, the codex and a few counters, but none of it was ever *said*: a run
  ended, the numbers moved nobody could see, and the codex was about the Tree rather than about the player. Now
  every save point evaluates twelve trophy rules (`progression/Trophy`, `TrophyBook`) and announces whatever is new
  — a haptic tap, the level-up chime, and one line on the HUD, which yields to the Tree whenever a story line is
  already on screen so a trophy can never wipe out the game's own voice.
  *Design that keeps it honest.* Each trophy is a counter with a target read from state the game already
  maintains — the trophy ledger's own wave counter, the heartwood bank, the ascension tier, codex entries written,
  elite kills, boss identities met, potionless runs — so `TrophyBook.progress` can show "7 / 100" instead of a
  locked box, and `evaluate` is idempotent: it can run at every save point without any system having to thread an
  achievement event through it.
  *The ledger and the migration.* `GameState.trophies` is one field that `resetForNewRun` hands to the fresh run
  instead of clearing, so a trophy is earned once, ever. `TrophyLedger.ledgerVersion` is what makes an old save
  work: a save written before trophies existed has no ledger at all, and `TrophyBook.migrate` reads that save's
  heartwood, ascensions, best wave, codex and boss kills once and converts them into what it already earned —
  without it, a veteran would open the new build to an empty case and no way to know the game had forgotten.
  *Where the trophies live in the UI.* The Codex grew a second shelf: the same overlay now has a LORE / TROPHIES
  tab strip (`CodexTouchLayout.Tab`), the trophy shelf shows `[*] Title` with `n / target` under it, and the
  details panel explains what earns the selected one. Tapping a shelf clears the other shelf's selection, and the
  trophy list scrolls to its own end rather than to the codex's length — both asserted.
  *Evidence:* `TrophyBookTest` (ten cases: an empty case on a fresh run, a trophy arriving exactly at its target
  and only once, progress capping at the target, a potionless run earning BARE_HANDS while the ledger still keeps
  the best run, three trees earning GARDENER, the meta trophies reading existing progress, the case surviving
  `resetForNewRun`, a veteran save migrating instead of being forgotten, a broken ledger being repaired, and all
  twelve ids/titles being distinct); `CodexTrophyShelfTest` (six cases: opening on LORE, switching shelves,
  tapping the showing shelf doing nothing, a trophy selection never becoming a lore selection, the trophy list
  stopping at its own end, and both shelves clearing the tab strip); `TrophyPresenterTest` (four cases: silence
  with no trophy, tap-chime-name in order, a story line never overwritten, and three-at-once being counted rather
  than listed).
  *Recorded cost:* the ratchet freeze for `HeroDefenseGame` moved 760 → 771 lines (the save point evaluates and
  announces; the presentation itself lives in the feature) and `model/GameState` 592 → 600 lines / 80 → 81 fields
  (the ledger field). Both are noted in the ratchet test with the reason, not hidden.
- [x] **R3.4 Content breadth.** Enemy types 4 → 8, item-pool diversity, wave modifiers.
  **Shipped: wave omens, as the thirteenth drafted trial.** Two hundred waves differ by their count and their place
  on the curve, but nothing ever surprised a player twice: wave 43 and wave 143 were the same wave with bigger
  numbers. A regular wave can now carry an **omen** (`model/WaveModifier`): `SWARM` (a quarter more of them),
  `IRON_HIDE` (15% harder to fell), `BLOODRUSH` (12% heavier blows) or `QUICKSTEP` (10% faster), and an omen wave
  pays 25% more coins — so it is a decision, not a punishment. The omen is derived, never stored: it is a pure
  function of the run seed and the wave number, so a save that resumes mid-run resumes the same omen, and nothing had
  to be encoded or repaired.
  *Where it lives is the design decision.* Omens ship as a drafted trial (`TrialId.HOLLOW_OMENS`, "Hollow Omens /
  Every sixth wave carries an omen / +25% coins on omen waves") rather than as a rule of every run, and the reason is
  measurement: the untrialled run is the run every balance gate has ever measured, and adding a feature must not move
  those numbers. Every lever already exists — `trials/TrialEffects.omensEnabled` is read by the spawner, the enemy
  factory, the kill-reward system and the HUD — so a player who wants the wood to answer drafts it, and the trial's
  own band runs through the trial gate with the other twelve instead of being asserted here. The simulator needed no
  new API at all: the counterfactual is simply the same seed with the trial and without it.
  *Where an omen may land* is one line of design, in `gameplay/WaveOmens`: never two twists at once. A boss wave
  already has a script and an elite wave already carries triple-health enemies, so stacking an omen on either shows up
  as exactly the single-wave spike the balance gates exist to catch (measured on the forced-card sweep: refusing the
  elite collision moved the worst-case spike from 40.9% back to 37.2%, which is what kept the card gate green without
  touching its ceiling). Twenty-two of the two hundred waves carry an omen once the boss and elite waves are excluded,
  and the HUD prints the omen's name under the wave counter for as long as the wave runs.
  *Measured,* trial against the same seed without it: all ten runs finish the vigil, and on the five seeds this was
  measured on the omen trial's spike runs 35.9% / 27.7% / 31.7% / 30.3% / 39.9% against the plain run's 30.0% / 30.3%
  / 26.4% / 32.1% / 24.0%, and the average pressure is up on four seeds. Of the twenty-two omen waves per run, 17, 15,
  17, 10 and 18 pressed harder than the same wave without the trial — the twist is felt, not just labelled. One honest
  caveat worth writing down: the worst of those seeds (39.9%) sits one seed away from the trial gate's own 40% ceiling,
  so the omen trial is the tightest thing in the band today; widening that headroom is exactly what the balance program
  (R4.1-R4.5) is scheduled to do.
  **Shipped: the item pool written down, and its guard.** `ItemDropSystem` now picks through `chooseFor` over a
  `tierPool` that cannot be empty: a tier with no pieces of its own borrows the nearest non-empty one instead of
  indexing an empty list, which is what the old one-liner would have done the day someone emptied a tier while editing
  content. The pool's shape is now asserted instead of assumed, and writing it down was the useful part — 46 pieces
  (14 common / 12 uncommon / 9 rare / 5 legendary / 6 mythic), every slot carrying seven or more pieces and its own
  mythic, and the whole legendary tier being five pieces: one per slot except the first ring, which stops at rare, so
  a legendary ring can only ever land in the second ring slot. Four thousand rare-floor kills in a test draw 30+
  distinct pieces out of the 46, and the pick spends exactly one combat float, so drops sit where they always sat in
  the random stream.
  **Measured and deliberately not shipped: the ownership-aware pick.** The obvious way to widen a pool is to stop
  handing back what the hero already owns, and to prefer a piece that would fill a slot nothing occupies yet. It was
  written and tested — prefer unowned-in-an-empty-slot, else unowned, else anything — and it works as a pool feature.
  It also moves the trajectory of every run that drops equipment, and that was enough to push two gates past ceilings
  that sit at 40%: the ascension gate's LIFESTEAL tier-0 scenario to a 40.24% spike and the trial gate's
  BOSS_BOUNTY + FAMISHED_EARTH pair to 43.33% at wave 196. Those ceilings belong to the balance program, not to this
  item, so the pick stays uniform; the recipe and both numbers are recorded here so the next attempt starts from the
  measurement instead of repeating it. That fragility is itself the finding: several scenarios now sit within a
  fraction of a percent of their ceiling, which is what R4.1-R4.5 exists to fix.
  **Shipped: the roster doubles, four enemies at a time.** The roster is eight. Four additions — Bark Stalker (wiry
  flanker, 26 hp), Sap Hound (fastest role, 22 hp), Husk Warden (shielded mid-weight, 30 hp) and Bramble Thrall
  (slow thorn mass, 39 hp) — each with its own model revision, silhouette, material story and four-clip motion
  language, authored in `models.py` and `rig.py` and rendered where the pipeline can run: the GitHub Actions render
  workflow has root and installs Blender's X libraries, this sandbox does not. The batch is rendered as
  **384 px masters at 3x/32** and published onto the reviewed runtime tier (**192 px frames, 2x/28**) by
  `tools/visual/publish_runtime_tier.py`, the same `compose_runtime_sheet` LANCZOS LOD that recovered the shipped
  tier in Phase 78; each manifest entry keeps the master render next to the shipped claim as `masterRender`, so the
  tier says both what ships and where its pixels came from.
  *The gameplay half is one enum, and it deliberately does not move a balance band.* The spawner cycles types
  uniformly, so the roster's mean per-type numbers are the wave's weight: the four additions are authored so that
  health 117, damage 28, experience 69, coins 19, speed 242, reach 172 and interval 5.10 all hold over eight roles
  exactly as they held over four. What changes is variance — a wave now mixes a 17-health wolf with a 46-health brute
  across eight roles — which is the point of the item. And the additions do not enter the cycle at wave one: the
  spawner keeps the first ten waves on the four field creatures and only then draws from all eight, because every
  accepted balance measurement was taken on those openings; deep waves interleave the roster with a coprime stride so
  no wave opens with a run of heavy bodies. `EnemyFactoryTest` asserts the means, `EnemyWaveSpawnerTest` the stagger.
  *One gate went red on the roster change, and the repair is a re-derived tier schedule rather than a wider band.*
  With all eight roles in the deep mix, the ascension gate's forced-card sweep failed exactly one cell of 160: a forced
  Dodge build at tier 6 on seed `20342418142676295` averaged 15.907% of the hero's health per wave against the 15%
  ceiling, where its four sister seeds sat at 6.9-10.6%. A probe (eight cards x four tiers x five seeds, plus a
  per-wave breakdown of that cell) showed the card was not the cause — the build with the least offence outlived its
  own waves: late waves took 100-146 s to close, each spawn landed on top of the last one, and the seed that fell
  behind never recovered (30+ waves above 35% damage in its last sixty). Three repairs were measured; two were
  rejected on evidence. Raising the roster's walk-in floor (the slowest role's speed) does fix the Dodge cell, but it
  adds pressure everywhere, and the tier-0 gates caught it immediately — `BOSS_BOUNTY + FAMISHED_EARTH` spiked to
  0.4083 against its 0.40 ceiling and the bare run spiked 36.4% against its 35% one. Rebalancing the ascension schedule
  does not touch tier 0 at all, because every ascension constant multiplies by `max(0, tier)`: the schedule now trades
  wave length for hit weight, with the per-tier health bump down from 0.0005 to 0.0004 (late waves close again, which
  is exactly what a low-offence build needs) and the per-tier damage bump up from 0.0002 to 0.00025 to keep the
  pressure where it was. The gate's four scenarios are green with no band, ceiling or floor moved, and every tier-0
  measurement in the repository is bit-identical to before the change. The probe that found the cause lived in
  `core/src/test` for the search only.
  *Two real defects the gates caught, and the fixes are in the art.* The first batch put opaque pixels on a cell
  border in `bramble_thrall/attack[6]` and `sap_hound/attack[3]` (authored extremes, trimmed in the source, verified
  on the shipped tier at 2-3 px margins); the second had a flat idle — the stalker keyed its crouch twice, so three
  of six frames rendered identically against a floor of five unique (the pose now settles into a coil before it
  loops, and a source-level guard makes a repeated consecutive key a test failure). Neither gate was relaxed; both
  found art that needed to change.
  *One follow-up the first re-dispatch forced:* the workflow now installs Pillow before the publisher runs (the
  publisher resamples with LANCZOS, and the runner's Python does not ship Pillow), which the run `35148572358`
  failed on one step before the validator — the fix is in the same commit as this paragraph.
  *Evidence, not summaries:* the accepted run is `35137859000` / artifact `10463764236`, the audit
  `docs/art_reviews/regular_enemies_premium_v2/regular_enemies_audit.json` hashes all 17 review sheets, 224 frames,
  every candidate sheet/atlas/metadata and the baseline state, and `docs/art_reviews/ENEMIES_PREMIUM_V2_REVIEW.md`
  carries the addendum with the per-frame border measurements. Measured cost: the combat set is 104,087,552
  bytes -- 99.3 MiB of the 100 MiB budget (`perf:2026-09-17-residency`; the 95.9 MiB this paragraph first
  carried was the pre-alignment arithmetic) -- and the catalog 384,872,448 bytes of a 390,000,000 budget — both raised deliberately and in the same
  commit as the content, with `RuntimeResidencyTest`, `PremiumAssetContractTest` and the asset validator agreeing on
  one definition of the live set.
  **Shipped: item-pool diversity.** See the pool paragraph above.
- [x] **R3.5 Session shape — a second run length, measured with the simulator.**
  The game had exactly one session shape: two hundred waves, and a player who has forty minutes may not start one.
  A run now carries a **mode** (`model/GameMode`): `STANDARD` (The Long Vigil, 200 waves) and `BRIEF` (A Brief
  Vigil, 30 waves — the sixth boss is the last one). Nothing about how a wave plays changes: the same spawner, the
  same economy, the same difficulty curve, the same boss scripting. Only the ending moves, which is exactly what
  the measurement below proves.
  *Reachable in the menu.* The main menu had five rows at 120 px; a sixth needed room, so the stack is now a table
  (`MainMenuTouchLayout.rowBottom(row)`), six rows at 96 px with a 22 px gap, laid out from 812 down to 222 — the
  drawn button and the tappable button read the same numbers, so they cannot drift apart, and a test asserts the
  rows never overlap, every row clears the title panel and the footer line, and no target is smaller than a finger.
  The game-over overlay also now shows this run's length rather than the constant 200.
  *Measured with the simulator,* which is what the item asked for. `BalanceSimulator.runBrief(seed)` sets the mode
  on the state before the run; the old signature is a one-line delegate to the standard mode, so every existing
  sweep is unchanged **by construction** (and the full suite confirms it). On seed `0x4845524F444546` the brief
  vigil runs 30 waves in 68 ms and the long vigil 200 waves in 1.65 s, and the first thirty waves of the short run
  are **bit-identical** to the first thirty waves of the long one — same damage fraction, same clear time, wave by
  wave. The short mode's own band, over three seeds: spike 0.1229 against the same 0.40 ceiling, average pressure
  0.0468 against the same 0.035 floor, and its last ten waves press harder than its first ten.
  *The one deliberate difference, recorded rather than hidden:* the long run's gate demands a minimum clear time of
  24 s. A thirty-wave run legitimately opens with waves that resolve in 10 s, so the short mode gets its own bounded
  version of that rule (no wave under 4 s, none over 120 s, and the slowest at least twice the fastest) instead of a
  copy of a rule written for a two-hundred-wave curve.
- [x] **R3.6 Human playtest protocol** plus recorded sessions; findings become roadmap items.
  *The protocol is written, the sessions are recorded, and the gate that keeps them honest is in CI.*
  `docs/PLAYTEST_PROTOCOL.md` defines what a session is, the two ways one gets recorded, and — in a section that is
  not decoration — what a session cannot prove. **The game records it itself.** `playtest/RunRecord` is the format
  (`herodefense.run-record/1`) and `playtest/RunRecordStore` writes it beside the save, keeping the newest twelve:
  build, platform, mode, ascension tier, seed, waves cleared, how the run ended, the trials, and the counters the
  state already keeps. A record carries nothing about the player — no name, no device id, no free text — so the
  evidence is a build and its numbers, not a person. Both endings write it, and that was worth a lesson: the first
  draft wired the death path and missed the completion path, because *both modes end on a boss wave*, so a finished
  run leaves through the reward card in the touch router rather than through the wave director.
  `SessionRecordIntegrityTest` now reads the sources and fails the build when a `transitionTo(GameScreenState.
  GAME_OVER)` is not followed by a `recordRunEnd()`, which is the difference between fixing a bug and testing one.
  *The ledger is a tool, not a document.* `tools/playtests/promote_run_record.py` is its only writer: it files the
  raw record as evidence under `docs/playtests/records/`, fills the session from the record rather than from the
  caller, and refuses a file that is not a run record. `tools/playtests/validate_playtest_ledger.py` reads the real
  roadmap and enforces the three rules that make findings more than a wish list: a finding's `roadmapItem` must
  exist in this file, a `fixed` finding must name the commit in its evidence, and an `accepted` finding must say in
  a sentence that starts with "accepted:" why it was accepted instead of fixed. Sessions must name the build they
  ran as a git hash and carry at least one line of notes, and a session whose filed record disagrees with it is
  rejected. Sixteen Python cases (eleven for the gate, six for promotion) and nine Java cases cover the path,
  including every negative rule; both commands run in CI next to the test suite.
  *The first sessions, and what they found.* The ledger opens with a real run of this build: seed
  `20342418142676294`, mode `BRIEF`, 30/30 waves cleared, `heroDied` false, average damage fraction **0.0511**, peak
  **0.1756**, fastest wave 10.1 s, slowest 37.1 s — captured by `SimulatorSessionCaptureTest` and promoted rather
  than typed. Two findings came out of it and both point at roadmap items: **R4.1** (a run at tier 0 that ends with
  82 percent of its health untouched is not a difficulty promise; the win-rate band for a non-optimiser policy is
  the item that fixes that) and **R3.6 itself** (every session so far was played by a script — the device path that
  pulls a human session off the app is implemented and unit-tested, but the owner's first real session is what
  turns the human half of this protocol from designed into proven). The ledger marks that gap instead of hiding it.

## R4 — Balance and difficulty curve  `+42`

- [x] **R4.1 Threats that scale.** Publish a win-rate band for a non-optimiser policy; make the curve
  rise after wave 40. *Both halves done and gated (2026-09-17), with the gap they found filed as R4.6 and R4.7:
  the rise is a measurement now (`WavePressureCurveTest`, quarters 0.0576 / 0.0986 / 0.1032 / 0.1229 on the fixed
  sweep, every quarter heavier than the one before it, no seed dipping more than five percent, the run inside the
  0.05-0.15 average band and under the 0.40 spike ceiling), and the band for a player who does not optimise is
  published and gated (`NonOptimiserBandTest`: the brief vigil is a floor at a 90% win rate, measured 100%; the long
  vigil has to catch that player between 55 and 90 waves of average reach, measured 69.7, with the optimiser still
  finishing all twelve runs at 200 waves). The band needed a second policy in the simulator — the non-optimiser
  takes the first reward card, never opens the shop, spreads talent points evenly and never reforges — and the two
  policies are proven to be the same code with different choices, with the default policy bit-identical to the
  optimiser the other gates measure.* the rise is now a measurement, not a claim —
  `WavePressureCurveTest` runs the fixed five-seed sweep and holds the curve to two rules: across the sweep every
  fifty-wave quarter must be heavier than the quarter before it, and no single seed may dip more than five percent
  below its own previous quarter, with the run average inside the shipped 0.05-0.15 band and the peak under 0.40.
  Measured on the shipped curve: quarters 0.0576 / 0.0986 / 0.1032 / 0.1229, last twenty waves 0.132-0.159 against
  the first twenty at 0.022-0.041. The win-rate half needs the non-optimiser policy and is next.
- [x] **R4.2 Boss damage lands at the end of the telegraph**, asserted as a contract. The behaviour was already right — the special
  is queued at trigger and lands when the window closes — and what was missing was the assertion that makes it a
  contract rather than a habit: `BossTelegraphContractTest` checks five promises over all four identities, the forty
  encounters of a run, ascension tiers 0/3/6/10 and both vigils. Every boss warns for the reference 0.50 s whatever the
  tier or the run length; the window is walked in twenty steps of 0.05 s and the hero's health may only move on the
  step that ends it; one warning carries exactly the strikes its script authorises (one, or two for `TWIN_TELEGRAPH`);
  ten tiers buy power and never reaction time, with the same window and a strictly harder hit; and a stun holds the
  warning instead of cancelling or re-rolling it, while a hero already killed by the first half of the wyrm's sweep is never
  hit a second time. The roster table, the per-strike figures and the reason these measurements hold the hero at 1000
  health (a float at 100000 has a 1/128 spacing that hides a one-percent tier difference) are in `docs/BALANCE.md`.
- [ ] **R4.3 Drop economy** with an EV table generated from the drop tables and a pity rule. *First half done and
  gated (2026-09-17):* the table is generated by `DropEconomyTableTest` from the same constants the game rolls,
  and `docs/BALANCE.md` is only allowed to contain what the test computed — a rate, a pool size or a sell price
  that drifts fails the build. Measured: **9.9515%** of kills drop an item (6% common, 3% uncommon, 0.8% rare,
  0.15% legendary, 0.0015% mythic), **2.496** coins per kill, 46 pieces across the five bands, and Luck
  multiplies every band rather than changing the table. *The pity rule is measured and blocked:* all three
  versions of it moved a shipped ceiling — a guaranteed common after thirty dry kills (brief vigil 0.0346
  against its 0.035 floor, trial spike 0.4196 against 0.40, one seed's third quarter 6.5% below its second), a
  guaranteed legendary drop every twenty boss kills (trial spike 0.4574, quarter dip 5.9%, tier-6 sessions
  20.3% faster than tier 0 against 20%), and even a promotion that spawns no item and spends no draw
  (`BOSS_BOUNTY + FAMISHED_EARTH` 0.4386 and `MISERS_PACT + BLOOD_PRICE` 0.4146, both at wave 196 against
  0.40). The third line is the finding rather than the failure: a trial ceiling that a passing pair already
  sits within one percent of cannot absorb *any* economy change, which is the same missing headroom R4.6
  describes from the curve's side. *R4.6 then bought some of it and measured how much:* the softer second-half
  elite multiplier and the cooler final quarter returned 0.01-0.03 of spike headroom on the worst pairs, which
  leaves the shipped worst trial pair at 0.382 against 0.40 — still short of the ~0.06 the rule needs. The rule
  ships when that headroom exists; until then the code has none and `ItemDropSystem` says so in its own comment.
- [x] **R4.4 `BALANCE.md` generated by the simulator, so the document cannot drift.** The doc's numbers used to be
  typed from a terminal, and the drift was already there to prove it: the wave-100 health checkpoint this file
  published was two curve revisions out of date, and every number in the R4.6 table had been copied by hand. The
  numeric blocks now live between `<!-- balance:generated … -->` markers and are built by `BalanceDocument` from the
  shipped constants and from the simulator's own books — the curve's checkpoints, the R4.6 measurements (quarter
  means, steps, average range, deepest single-seed dip, elite multipliers, the three riskiest trial pairs, the
  reward-card spike), the drop table, the economy audit with its spend-split stability line, and the ascension
  ladder's growth rates per tier. `BalanceDocumentTest` fails when the file disagrees with the code, names the block,
  prints what the code now says and tells the reader to run `:core:regenerateBalanceDoc`; that task is a `JavaExec`
  with the document declared as its input, because the first version of the gate could be called up to date after an
  edit to the very file it guards. Prose stays authored, and the markers are what draw that line — the second test
  refuses a marker the code does not own. The gate has teeth in both directions, checked by hand: a one-digit edit
  inside the checkpoint block fails it, and the generator puts it back. This also retired the drop table's own copy
  of the same idea in `DropEconomyTableTest`, which now holds only the economy's arithmetic.
- [x] **R4.5 Balance regression in CI, as a job you can point at — `50bde24`+ on fixed seeds.** The gates already
  ran on every push, but they ran as 650 anonymous tests inside one job: a curve that left its band, a trial pair
  that spiked, or a tier that stopped charging for itself produced a red step called *unit tests*. The suites that
  sweep whole runs now carry a `balance` tag, `:core:balanceGate` runs exactly those, and CI runs them as their own
  job with its own 25-minute budget and its own uploaded report, next to the unit job. The split is a *move*, not a
  copy — the tagged suites are excluded from `:core:test`, so nothing runs twice — and it made the unit loop fast
  enough to be worth watching again: **31 s** against the 4 m 50 s it took when the sweeps lived in it, with the
  gate's **4 m 47 s** running in parallel on the other job. `:core:check` depends on both, so the aggregate gate a
  contributor runs locally is still the complete one, and the fixed seeds every sweep uses are named in
  `docs/BALANCE.md`.
- [x] **R4.7 The ascension ladder is inverted for a non-optimiser — closed by charging every tier from its first
  wave.** Measured for the player who takes the first card and then ignores every system, the ladder ran *backwards*:
  0.0918 mean pressure in the brief vigil at tier 0 against **0.0019** at tier 10, and 69.7 waves of reach in the
  long vigil against **102.3**. The cause was structural rather than numeric — a tier's reward (heartwood and root
  power, +10 strength and +20 health and 500 coins at tier 10) is granted at run start, while its price was a growth
  bump of 0.0004 health and 0.00025 damage per tier *per wave*, which is a fifth of a percent of a wave in the
  opening and four percent of the run by wave 200. The reward landed on wave one and the charge landed on wave 150.
  A tier now scales the enemy's **baseline** health (+22%) and damage (+30%) instead, fading linearly over 140
  waves, so the charge lands in the waves where flat starting power is worth anything.
  The search is part of the item. **Health is what the naive advantage is made of** — a damaging charge alone
  cannot open the ladder at all, because the early waves die to one or two hits either way (damage-only, twelve
  seeds: 0.0039 brief pressure at tier 10, still inverted) and a large damaging charge without health wrecks the
  optimiser's late game (0.2128 average, final quarter 0.572). But **health is also what makes a wave take
  longer**, and a tier pays its charge in seconds as well as in blood: the health-heavy mix (0.30 / 0.15 per tier)
  held the same ladder numbers at a **63%** longer tier-10 session, against **35%** for the shipped 0.22 / 0.30.
  The fade's span was measured the same way: 60 waves fixed the brief (0.0370) and left the long vigil inverted
  (103.8 waves), because the reach is decided in the middle of the run rather than the opening.
  Shipped, on six seeds per tier: tier-10 mean brief pressure **0.0507** against 0.0901 at tier 0 (56% of the
  tier-0 value where the finding measured 2%), long-vigil reach **77.3** waves against 71.3, the optimiser finishes
  every run with an average rising **0.1035 → 0.1750**, and the seven-second-per-wave ladder is gone. Two gates were
  rewritten to price that honestly rather than to accommodate it: the average-pressure **ceiling** in
  `AscensionGateTest` is tier-indexed (`0.15 + 0.02·tier`, the same slope the spike ceiling already used) because a
  tier is a harder game chosen by a player who finished the last one, and the session-time rule became
  `+20% + 4% per tier on the mean of the seeds` — the old `±20% of tier 0` was written when tiers cost nothing —
  with the padding it was originally guarding against asserted directly instead: **the wave count is exactly 200 at
  every tier**, so no tier can buy difficulty with length. The charge's arithmetic (tier 0 bit-identical, the fade
  linear and monotone, the baseline the game reads) is pinned in `DifficultyCurveTest`; the tier-0 gates in the
  repository are untouched because with tier 0 the charge is exactly `1.0`.
- [x] **R4.6 The second half's shallow step — closed by splitting the half and paying for the split.** The defect
  was measured: waves 101-150 were only **4.7%** heavier than waves 51-100 against **71%** for the step before them,
  because the whole half ran at the curve's coolest rate (`1.023 / 1.008`). Two repairs were tried first and both
  were rejected on the shipped ceilings (`1.025/1.0085`: bare-run spike 0.3540 against 0.35, tier-10 STRENGTH median
  0.6765 against 0.60; `1.024/1.008`: trial pairs 0.4071-0.4732 against 0.40) — and finding out *why* is what the
  item actually delivered: **the second half's heat is the run's spike carrier, and the spikes sit on elite waves**
  (the schedule is every seventh wave at tier 0, so 126, 133, 154, 182 and 196 are all elite waves, and every late
  spike the gates have ever caught sits on one). Headroom was therefore bought before it was spent:
  `ELITE_SECOND_HALF_DAMAGE_MULT` **1.2** against the flat 1.5, measured over the five trial seeds at **0.01-0.03**
  of spike headroom for **less than 0.001** of average-pressure movement, because an elite's damage does not change
  how long its wave takes. The curve then splits into two spans — waves 101-150 at `1.026 / 1.0085` and waves
  151-200 at `1.018 / 1.0065` — so the heat moved out of the quarter that carried the spikes and into the entry that
  carries the step. Measured on the fixed sweep: quarters `0.0576 / 0.0986 / 0.1142 / 0.1298`, steps
  **x1.712 / x1.158 / x1.136** (the step into the second half more than doubles), every seed's average inside
  `0.05-0.15`, the worst trial-pair median spike `0.382` against `0.40` where the shipped curve already stood, and
  the full core suite green. Two caveats are written into `docs/BALANCE.md` rather than hidden: one sweep seed
  spends 4.9% of its 5% quarter-dip allowance, the reward-card matrix keeps 2.5% of headroom, and the purchase was
  **not** big enough for R4.3's pity rule (it needs ~0.06, R4.6 returned 0.01-0.03), which stays deferred.
- [x] **R4.8 The ladder's elite escalation never reached tiers 6, 7 and 8.** The cadence tightens one wave per
  three tiers, and the arithmetic step from seven landed on **five** — which is the boss lap, and a boss wave may
  not carry elites — so those three tiers spawned **zero** elites in an entire two-hundred-wave run while this
  ladder (and `docs/BALANCE.md`) claimed to escalate them. It was not an undiscovered bug, it was a *pinned* one:
  `EnemyWaveSpawnerTest` asserted the absence wave by wave and the balance document recorded the collision as a
  structural note, which is what a defect looks like when the test checks the arithmetic instead of the outcome.
  R4.7 found it by accident — the new elite-charge contract asked for a tier-6 elite wave and there was none to
  find — and the repair is the smallest one that changes the thing being measured rather than the numbers
  around it: the cadence now steps `7 → 6 → 4`, skipping the interval that is the boss lap, and the assertion is
  turned around so that **every tier from 0 to 10 has to find elites in a run**, with tier 0 keeping its shipped
  seven-wave cadence (so every brief vigil and every tier-0 gate stays bit-identical). Tiers 6, 7 and 8 now have
  elites, which makes them harder, so the gate was re-measured rather than assumed: the full `:core:balanceGate`
  is green on the new schedule, tier 6's mean session drift is +0.245 against a 0.44 budget, and the generated
  block in `docs/BALANCE.md` publishes the cadence itself so the claim cannot drift back.

## R5 — Visual assets  `+68`

- [x] **R5.1 The "HD" claim settled with measurements** (Phase 78 evidence above).
- [ ] **R5.2 Compose a runtime tier from the genuine masters.** The 27 sheets with measured detail gain
  become the runtime tier once (a) the new frame layout has its own accepted, hash-bound review and
  (b) the memory budget allows 384-pixel frames.
- [x] **R5.3 Render-workflow reliability: resumable, deterministic, comparable.** A Blender batch was
  all-or-nothing inside a hundred-and-twenty-minute job: a timeout threw away every sheet it had packed, a
  re-run re-rendered byte-identical sheets, and nothing recorded what a render had produced.
  `tools/render/render_hash_log.py` now logs every `.png`, `.atlas` and `.json` a render writes — hash and
  size per file, plus the asset keys the batch's manifest declares — and it deliberately records **no**
  timestamp, **no** commit and **no** absolute path, because two logs of the same bytes have to be
  byte-identical for a comparison to mean anything; the manifest itself is logged as keys rather than bytes
  for the same reason, since it carries `generatedAt`. On the shipped tree the log covers **321 files across
  111 assets** and a self-diff is clean. Three commands carry the item: `write` (what the render produced),
  `diff` (added / changed / removed files and keys, with `--fail-on-change` so "this render is the reviewed
  one" can be a gate rather than a commit message) and `resume` (the keys an interrupted render still owes,
  from the manifest plus the log, split into *never finished* — a file absent, empty or absent from the log —
  and *rendered differently*). Resume was rehearsed on the shipped tree with a three-asset fixture: two
  interrupted keys were named with their reasons (`sprites/rootling.png is empty`,
  `sprites/stonekin.atlas missing`) and `--keys` printed exactly the two keys the generator needs.
  The workflow uses all three: it restores the masters from `actions/cache` under a key that includes
  `hashFiles('tools/blender/**', 'tools/render/**', ...)` — so a resumed render is always a render of the
  *same code* — asks the log what the batch owes, renders only that, writes the log again, prints the diff
  against the restored one into the job summary, uploads the log even when a later step fails (a partial
  render is exactly the thing that has to be diffable) and caches the directory only when the render
  succeeded. What resume does **not** cover is written down in `tools/render/README.md` rather than implied:
  a batch interrupted before its first manifest write renders from the start, because the manifest is what
  maps keys to files. Ten new tests run in the unit job (`tools/render/tests`), and the README documents the
  three commands with their real output.
- [ ] **R5.4 Vibrant grade rendered, not filtered**, proven with before/after emulator screenshots
  (unblocks R1.7).
- [x] **R5.5 PBR maps in the repository** (normal / roughness / AO) with material provenance. 39 maps -- normal,
  roughness and ambient occlusion for the hero, the four bosses and the eight creatures -- under `docs/materials/`,
  derived from the rendered masters that ship by a recipe that is in the repository rather than in somebody's
  memory: `tools/visual/generate_material_maps.py` reads the master's luminance as a height field, differentiates it
  into a tangent-space normal, reads luma variance as roughness and local contrast as occlusion, and writes a
  provenance table (`docs/art_reviews/MATERIAL_MAPS_PROVENANCE.md`) that names, per map, the master it came from,
  the master's SHA-256, the map's SHA-256 and its size. `tools/visual/validate_material_maps.py` re-derives every
  map and fails on any disagreement -- a one-pixel edit is caught, which `tools/visual/tests` asserts as a negative
  control and then asserts again after the map is restored -- and that step runs in the core job next to the shipped
  asset validator. Two decisions are written down rather than implied: the maps are a quarter of the master's
  resolution because a material definition, not a fourth texture set, is what this item owes, and they live under
  `docs/` rather than in `android/assets/generated/` because a texture the runtime does not read yet is a texture
  the measured APK budget should not pay for. Consuming them in a shading pass is R9.3's job, and it now has real
  files to consume.
- [x] **R5.6 Review documents regenerate per batch.** `tools/visual/regenerate_batch_review.py` names every batch the
  render workflow accepts -- twelve of them, from `enemies` to `world-tree` -- and the review generator that owns that
  batch's evidence, so the mapping is a table in the repository instead of a habit in somebody's shell history. The
  workflow runs it after the render, with the committed tree as the baseline and the tree this run just rendered as
  the candidate, under `if: always()` because a batch that failed validation is exactly the batch a person has to look
  at, and uploads the contact sheets and the audit JSON as an artifact named for the batch. A batch with no generator
  is an error rather than a silent skip: a render nobody can review is not an accepted batch, and that failure belongs
  where the batch is named. Two tests hold both halves -- every named batch's generator exists on disk, and the
  workflow calls the tool with the batch it was actually asked to render.
- [ ] **R5.7 VFX that exist at runtime**: rarity glow, impacts, trails, with evidence frames.

## R6 — Audio  `+28`

- [x] **R6.1 Music breadth** (3–4 tracks) with licence and file hash per file. There are four beds -- `vigil` for
  the run, `hollow_march` for a boss on the field, `heartwood_dawn` for the menus, the codex and the root
  network, `quiet_after` for the end of a run -- and none of them is a download. `tools/audio/generate_music.py`
  renders each one from a seed: oscillators, an envelope per voice, a wrapped reverb tail and a wrapped head, so
  the loop has no seam, in one key so a crossfade lands. That is a stronger provenance than a licence because
  it is the source, and the per-file hashes are in `AUDIO_LICENSES.md` with a test that fails if a bed
  is missing from the table. The imported 1.2 MB loop this replaces is retired in the ledger rather than
  deleted from it, and the APK is ~900 KB lighter for it.
- [x] **R6.2 SFX coverage** (bow draw/release variants, crits, pickups, UI, telegraph, ambience) with a
  measured peak level each. Eight cues join the eleven, and every one of them hangs on a real event: the bow
  release (and a lighter one for the extra arrows of a volley), the ultimate, coins from a kill, a wave that
  ends, the warning before a boss special (`BossSpecialAttackSystem` now reports telegraphs started, read
  once per frame so a frame cannot play it twice), menu and overlay taps, closing an overlay, and eight
  seconds of wind looping under a run. They are authored by `tools/audio/generate_sfx.py`, so the source is
  the repository. *Measured* is the literal part: `tools/audio/check_audio_levels.py` decodes every
  committed file and writes `docs/audio/LEVELS.md`, and it immediately found what listening had not -- five
  of the imported CC0 cues decoded above full scale (`boss_entrance` 1.122, `hit` 1.058, `purchase` 1.022,
  `death` 0.986, `multi_shot` 0.948) and one of the new beds touched 1.000. The imported files were
  corrected by `tools/audio/normalize_levels.py`, which re-encodes and re-measures because Vorbis adds its
  own overshoot, and the applied gains are recorded in the licence ledger; the generated ones were
  re-rendered with more headroom at their source. The gate runs in CI and fails above 0.94 (-0.5 dBFS) or
  below the audibility floor.
- [x] **R6.3 Music state machine** tied to game state, asserted by a test. `MusicSelectionPolicy` maps every
  member of the game's own `GameScreenState` to a bed and a gain, and a boss on the field outranks the screen it
  was found on. The policy is pure, so the test asserts the table itself over all thirteen screens -- including
  that a new screen added later cannot slip through with the menu theme playing over a fight. `Crossfade` owns the
  hand-over: two decoders for 0.8 s, gains that always sum to one (asserted, because two beds that add up
  would be louder than either), no overshoot on a stalled frame and no rewind on negative time. The frame
  asks for the bed once per frame through `AudioFrame.guideMusic`, which is recorded and asserted in `FrameDriverTest` --
  menus, the run, and the level-up wall that keeps the track and lowers it instead of switching.
- [x] **R6.4 Audio settings** persisted, honouring audio-focus loss, tested. The settings surface grew two
  rows: effect level and music level, three named steps each (QUIET / NORMAL / FULL, 0.45 / 0.75 / 1.0)
  because a touch-only screen has no drag handles. They are persisted with the rest, applied per cue and
  per bed, and a hand-edited or older preference file is snapped to the nearest named step on load rather
  than drawn as a level the screen cannot label. Audio focus is a core decision rather than an Android
  detail: `AudioFocusState` says that a real loss silences everything until focus returns, while a
  transient loss ducks music to a quarter and keeps the fight audible, because a game that goes silent for
  a notification looks broken. `AndroidLauncher` only translates Android's codes -- through a
  `AudioFocusRequest` on modern APIs, requesting on resume and abandoning on pause -- and the tests assert
  the policy over the events, including that the player's own toggles still win.

## R7 — UI, onboarding and localisation  `+52`

- [x] **R7.1 A sixty-second onboarding, skippable, touch-only, and shown exactly once.** The 2026-09-13 review's
  sharpest onboarding line was a measurement: *"grep for tutorial/hint/onboard in `core/src/main`: zero
  hits"*. There is now a first vigil — five coached steps, one minute, each waiting for the thing it
  describes rather than for a timer. The player is taught by doing: tap empty ground and the Hero walks,
  hold and drag to aim and fire, loot homes to you, a level-up offers cards, coins buy things in the shop.
  Two rules make it safe to ship, and both are pinned by tests rather than by hope. **A step ends on its own
  action or on its own budget**: `OnboardingCoachTest` performs every step's action *and* every other action
  as well, and asserts that the wrong ones change nothing — a player who taps the ground early does not tick
  off the card lesson — while a step whose budget runs out is left behind, so a player who ignores the coach
  entirely is never trapped by it and the whole lesson still ends inside its own sixty seconds
  (`OnboardingStep.totalSeconds() == 60.0`, budgeted 12 / 12 / 10 / 14 / 12). **Exactly once is a property of
  the save file**: finishing *or* skipping writes `onboarding.tutorialSeen` through
  `LocalSettingsRepository`, and the test walks the whole path — first run teaches, a second run in the same
  session is silent, the reloaded settings are silent, the run after that is silent. A Continue never opens
  the lesson, because the player resuming a session at wave 40 is the last person who needs to be told to tap
  the ground. The banner is anchored above the utility row and its **Skip target is a 150-by-100 world-unit
  button**, the same minimum every other phone-HUD target uses; `OnboardingTouchLayoutTest` asserts the
  geometry that matters (the banner never covers the utility row or the status row, never reaches the arena
  floor or the Hero, every point of the Skip target is drawn inside the banner) and
  `OnboardingSystemTest` asserts that Skip swallows its tap — the one target that must never double as play
  input — while a tap one button-width to the left still walks the Hero. Touch-only by construction, which
  `TouchOnlyInputPolicyTest` enforces for the whole repository: every completion action is something a finger
  produces, and the vocabulary has no orphans (each action in `OnboardingAction` is taught by exactly one
  step). The one lesson the touch layer cannot see — loot arriving — is read from the run's own books
  (`OnboardingSystem.observe`: coins arriving or items entering the backpack), which is why the coach is ticked
  once a frame by the composer instead of by the input layer. 21 new tests; the step texts are constants so
  the locale work of R7.3 has one place to read them from.
- [x] **R7.2 Stat tooltips for every displayed stat, with a coverage test — and the coverage test found the
  screens were explaining the wrong numbers.** `tooltips/StatTooltips` is now the single source for what the
  talent screens say: `shortLine(stat)` is the row text the stat shop draws (it used to be written inside the
  shop renderer) and `tooltip(stat)` is the long explanation, shown by the shop's help panel for the row the
  player last touched (`StatShopSystem.noteTouch`, 4.5 s, drawn by the renderer with the panel's own line
  budget). Ten readouts the game puts on screen outside the five talents — health, damage, attacks per second,
  crit, dodge, drop rate, coins, wave, kills, heartwood — each have a label and an explanation too.
  The old contract this replaces was **passing while it was wrong**: `PremiumShopPresentationTest` asserted
  that every row line starts with `"+1 "` and is at least ten characters long, and three of the five lines
  named a unit the code does not have — `DODGE` read "+1 dodge rating" against a real 0.5% per point, `LUCK`
  read "+1 critical-chance rating" against a real 2% drop multiplier, `AGILITY` read "+1 attack-speed rating"
  against a real 0.03 attacks per second. A length check cannot catch a sentence that describes the wrong
  number, which is what the 2026-09-13 review meant by "the value math is opaque". The lines now state the
  per-point gain with the constant interpolated from `HeroStats`, and the test asserts *that* instead: the row
  line has one source, starts with a plus, contains a digit and fits its button.
  The coverage mechanism is a keyword table in `StatTooltipsTest` keyed by `HeroStat`: a new talent fails the
  test because the table has no row for it, and its tooltip has to name the mechanic it moves (damage, speed,
  drop, chance, HP are the keywords the shipped five are held to). Length is enforced as the layout budget it
  is — `noteLines()` wraps a tooltip onto the panel's two lines of 62 characters, the test asserts every
  tooltip needs no third line, and that wrapping loses no word (`"wrapping may not lose or reorder a word"`).
  The numbers in the sentences are checked against the constants that produce them: the dodge tooltip must
  state the cap the code enforces, the luck tooltip the multiplier the code applies.
- [ ] **R7.3 Persian + RTL**: locale-aware string table, mirrored layout, and a test that fails on
  hard-coded or untranslated user-facing strings.
- [ ] **R7.4 Back button** handled in game and menus, with an instrumentation test.
- [ ] **R7.5 Accessibility**: font scaling, colour-blind-safe rarity encoding, measured contrast.
- [ ] **R7.6 Store-facing UI assets** (screenshots, description, feature graphic).
- [x] **R7.7 The menu's rows are one table, and the device journey asks it instead of copying it.** R3.5 re-tabled
  this menu from five rows of 120 px to six of 96 px and two readers of the old table stayed behind. `MainMenuRenderer`
  resolved each button's *pressed* state against the old 780 / 620 / 460 / 300 / 140 rectangles, so pressing a drawn
  button lit whichever button was nearest its stale rectangle, and Brief Vigil lit New Game because the two shared a
  state. And eight taps in `AndroidTouchSmokeTest` asked for Continue at y=680 and Settings at y=200, which the new
  table puts outside both rows (Continue is 576-672, Settings is 222-318) -- that is the reason the Android job has
  been red since `b9027b7`: nine of its eleven journeys time out behind a tap that lands on nothing while the
  remaining two, which never tap Continue, pass. Both now derive from `MainMenuTouchLayout.rowBottom(row)`: the
  renderer with its own state per row, the journey by asking the layout which row carries an action, so a re-ordered
  or resized menu still resolves and an action with no row fails at the tap instead of in a timeout. Verified by
  `MainMenuRendererTest`, `MainMenuAndSettingsTouchTest` and `UiFrameRendererTest` in core, and by the Android job on
  this commit.

## R8 — Performance, memory and size  `+40`

- [~] **R8.1 Texture compression** (ETC2/ASTC + fallback) and mipmaps with a measured comparison. The
  measured half is in: `TextureFormat` carries each format's real arithmetic (ETC1 half a byte per pixel
  *and no alpha*, so an alpha page needs a companion sheet and the plan is charged for it; ETC2 one byte;
  ASTC 6x6 sixteen bytes per thirty-six pixels; RGBA8888 the four the device decodes to today) and the mip
  chain costs the third that `1 + 1/4 + 1/16 ...` says it costs. `:core:residencyReport` now prints the
  shipped catalog in every one of those formats -- 384,872,448 bytes decoded, 96,218,112 as ETC2, 42,763,608
  as ASTC 6x6, and the live combat set 80,740,352 -> 20,185,088 -> 8,971,152 -- and the whole projection is
  a logged run (`perf:2026-09-17-format-projection`), not a typed table. The RGBA8888 row is not a
  projection at all: a test asserts it equals the decoded catalog the residency gate already enforces, which
  is what makes the compressed rows worth quoting. **The remaining half is the payload**: the encoded
  containers, the loader that picks a format per device with a PNG fallback, and the device evidence that
  the fallback works. A first-party ETC2 encoder now exists in this repository, so what is left is evidence
  rather than tooling.
  **The payload half is in the repository and measured, and nothing ships yet -- by the tool's own rule.** A
  first-party ETC2 encoder (`tools/texture/etc2.py`) implements both colour modes against the published bit
  layout and is held to Google's `etc1` implementation -- the encoder/decoder pair libGDX ships as JNI -- by
  decoding four reference blocks (gradient, flat, hard edge, noisy) pixel for pixel in
  `tools/texture/tests/test_etc2_encoder.py`; `tools/texture/ktx.py` writes and reads the KTX v1 containers
  libGDX's `KTXTextureData` already knows how to upload, and `tools/texture/encode_textures.py` measures the
  three numbers that decide a sheet: decoded bytes, the share of alpha that is already a mask, and the PSNR of
  the round trip. Running it over the combat sheets answered the question rather than assuming it: the shipped
  sheets are **not** masks (0.94-0.99 of their pixels are 0 or 255, so 0.6-6 % would be hardened by the
  punchthrough format, and ETC2_RGBA8's base-and-modifier alpha cannot represent a mask at all), and the
  encoder's colour quality measures **28.88 dB** against the **33.57 dB** the reference encoder reaches on the
  same pixels -- below the 32 dB bar this item sets for itself, so the tool declines every sheet and the PNG
  stays the shipped payload. The per-device half is in and tested (`DeviceTextureSupport` reads the format out of
  the GL version, `TexturePayloadPolicy` chooses the container per sheet and falls back to the reviewed PNG,
  six cases in `TexturePayloadPolicyTest`), so a payload that does clear the bar switches on without a second
  decision at runtime. What is still open is written down rather than implied: closing the five-decibel gap
  against the reference encoder (or pinning that encoder and its licence), wiring containers into the
  atlas-page loading path, and the device evidence that a compressed palette uploads and renders on the
  emulator. That evidence is now a test rather than a promise: `CompressedTextureDeviceTest` opens its own
  GLES3 pbuffer through `EGL14` (the game asks libGDX for a GLES2 context, which could never load an ETC2
  page), uploads a 64x64 punchthrough container built from a real shipped sheet by
  `tools/texture/make_device_fixture.py`, reads the frame back with `glReadPixels` and asserts the GPU agrees
  channel by channel with this repository's own decode of the same bytes -- with the fixture rebuilt in the
  unit job, so a device cannot be asked to decode bytes the encoder no longer produces. What that run has not
  done yet is happen: the row stays `[~]` until the emulator job reports it green.
- [x] **R8.2 A memory budget enforced by a test.** `RuntimeResidency` computes residency from the
  manifest; `RuntimeResidencyTest` checks the catalog against `decodedCatalogBudgetBytes` (390 MB) and the
  live combat set against `decodedCombatResidencyBudgetBytes`, a deliberate **100 MiB** (was 150 MB), and the
  measured values are a logged run rather than a sentence: `perf:2026-09-17-residency` records the catalog at
  384,872,448 bytes of the 390,000,000 budget and the live combat set at 104,087,552 bytes -- **99.3 MiB** of
  the 100 MiB budget. (This row used to say 95.9 MiB; that was the pre-alignment arithmetic, and the logged
  run is what the file says now.) The test fails if either number moves, and `PremiumAssetContractTest`
  measures the same set through the same arithmetic instead of its own wider superset, so the two cannot
  drift apart. The device half of the same budget is filed too: `perf:2026-09-17-wave50-residency` measures
  148.3 MiB of the 400 MiB wave-50 ceiling on the CI emulator. What the budget does *not* leave is room for a
  ninth enemy at the current frame size: with roughly one enemy-sized sheet of headroom, a ninth role needs
  smaller frames, a shared page, or the compression R8.1 is still working on.
- [x] **R8.3 Residency at wave 50** measured with `adb shell dumpsys meminfo` during a scripted run,
  logged in the repository, with streaming/release of atlases. Two of the three halves are in and the third is
  only waiting on a green emulator job. **Streaming/release**: `AtlasResidencyPolicy` (`b32ad6e`) makes residency a
  rule rather than a hope -- a page is released when its sheet family has been off screen for a stated interval and
  a re-entry is served by a bounded reload, with `AtlasResidencyPolicyTest` covering the release, the reload and the
  budget the policy is allowed to work inside; `ResidencyReport` prints what the rule does to the shipped catalog.
  **The measurement**: the instrumented `WaveFiftyMemoryTest` loads a wave-50 save, taps through the menu, waits
  four seconds inside the wave and logs one stable line,
  `HERODEFENSE_PERF wave=50 totalPssKb=... totalRssKb=... graphicsKb=... budgetKb=... source=...`
  (`0e5c70a`), which `tools/perf/check_wave50_memory.py` reads out of the logcat capture and compares against the
  committed `docs/perf/wave50_memory_budget.json` (409,600 KiB total PSS, 163,840 KiB graphics), exiting 2 when the
  log carries no measurement at all. The first CI run of this test measured nothing -- the shell dump was read once
  and came back truncated before its App Summary -- and the fix (`8a2e05f`, plus a compile error CI caught and
  `3b7c580` fixed) makes the process's own `Debug.MemoryInfo` the primary source, merges the shell dump field by
  field, reads it to the end, and keeps it in the logcat as evidence. **What is still missing is the number**:
  **The number is in.** CI run `35196492084` (commit `7329f4b`, Android job `105120923106`) went green on
  2026-09-17 and filed the measurement as `perf:2026-09-17-wave50-residency`: **148.3 MiB total PSS
  (151,837 KiB) of the 409,600 KiB budget**, 267.2 MiB RSS. The graphics field reads 0 because neither the
  process's own `Debug.MemoryInfo` nor the shell dump's App Summary exposes a GL heap on the software stack
  this emulator runs, and that is written into the logged run rather than papered over: the budget the gate
  enforces is the total, and the per-field evidence stays in the logcat the job uploads.
- [x] **R8.4 Startup and APK budget** measured in CI against a committed threshold. The instruments are in and
  the ordering bug the first run found is fixed. `scripts/android-touch-test.sh` now installs the debug APK after
  the instrumentation run uninstalls it, resolves the launcher component, and measures the cold start with
  `am start -W` (`6f90222`); `tools/perf/parse_startup.py` turns that line into a number, `tools/perf/log_run.py`
  files it as a logged run and `tools/perf/render_performance_doc.py` regenerates `docs/perf/PERFORMANCE.md` from
  those runs; the APK-size and cold-start budgets are committed thresholds that the Android job compares against
  and the numbers only count when the job is green. That sentence was written the same day the ordering bug was
  **Both budgets have their number.** The same green run files `perf:2026-09-17-cold-start` -- `LaunchState
  COLD`, `TotalTime 882` ms against the committed 6000 ms threshold -- and `perf:2026-09-17-apk-size`, the
  20,872,599-byte debug APK against the 120,000,000-byte ceiling. `docs/perf/PERFORMANCE.md` regenerates from
  those runs, so the page and the CI job quote one number rather than two.
- [x] **R8.5 Every performance number in `docs/**` comes from a logged run.** `docs/perf/` is the home: `runs/*.json`
  are the logged runs (command, commit, date, metrics), `PERFORMANCE.md` is *generated* from them, and
  `tools/perf/check_perf_provenance.py` fails the build on any number in the documentation with no run behind
  it. A citation names a logged run (`perf:` and the run's id), a committed threshold (`budget:` and the file),
  a design constant (`code:` and a path that must exist) or an environment fact (`env:` and a key listed in
  `docs/perf/ENVIRONMENT.md`), and a citation that resolves to nothing fails too, because it looks like
  provenance without being it.
  The rule found nine uncited numbers and all nine were fixed rather than exempted: the 390 MB catalog budget,
  the 45 ms hit stop and the 12 fps playback baseline (now named constants in named files), the APK size
  claim in the release document (now the committed budget), and the runner's disk facts (now `env:` keys).
  Three documents are exempt with their reasons written into the checker: the roadmap (the plan and the work
  log), the 2026-09-13 review (a dated document whose numbers are the finding) and the audit/art-review
  records. The first two logged runs also caught a stale number in the repository: R8.2's note says the live
  combat set is 95.9 MiB, and `:core:residencyReport` measures 104,087,552 bytes — 99.3 MiB of a 100 MiB
  budget, 750 KiB of headroom. The note is corrected and the measurement is what the file says now.

---

## R9 — Asset quality: improve every shipped asset  `+150 experience`

Deliverable per item: a measurement plus either a better asset or a documented reason it stays.

- [ ] **R9.1 Texel-density audit for every asset.** For each asset: sheet frame size, drawn size in world
  units, device pixels at 720p and 1080p, and the ratio. Produces `docs/art_reviews/ASSET_TEXEL_AUDIT.md`
  from a tool, and a gate that fails when a gameplay-critical asset is more than 1.35× undersampled at 1080p
  or more than 4× oversampled.
- [ ] **R9.2 Art upgrade batches, per category.** Hero, bosses, creatures, world tree, environment and props,
  equipment, icons, UI frames, ceremony. Each batch is produced by the render workflow, shipped with a
  per-batch review document, contact sheet and ledger hashes; the resampling and ledger gates from R1.1/R1.4
  must stay green, so an upgrade can never again be a resize.
- [ ] **R9.3 Shading pass.** Authored normal, roughness and ambient-occlusion maps for hero, bosses and
  creatures, used by the runtime shader path, with before/after captures at a fixed seed.
- [ ] **R9.4 Palette, value range and gameplay contrast.** Hero, enemies, projectiles, pickups and telegraphs
  must hold at least 4.5:1 contrast against the arena behind them, measured from real captures rather than
  asserted in a style guide.
- [ ] **R9.5 Animation pass.** Every creature and boss gets idle, wind-up, impact and defeat clips with real
  frame counts; a test asserts the clip inventory per entity and that no clip is empty or duplicated.

## R10 — Asset expansion  `+130 experience`

- [ ] **R10.1 Content register.** One machine-readable file listing every gameplay asset with its category,
  owner phase, review document and licence. A test fails when a shipped asset has no register entry.
- [ ] **R10.2 Creature families.** New enemy archetypes with mechanics the current four cannot express:
  burrower, flyer, shield-bearer, healer, splitter, ranged caster, swarm-mother, corpse-bloomer, mirror
  duplicate, frost-warden, ember-hound, root-tender — each with a telegraph, a counter, and its own review
  coverage.
- [ ] **R10.3 Boss roster and elites.** Distinct boss identities with multi-phase fights, elite variants of
  regular enemies, and an act-aware boss schedule.
- [ ] **R10.4 Biomes and arenas.** Four theatres (sanctum, ashfall ridge, frost hollow, corrupted rootway)
  with their own tilesets, backdrops, weather, ambient VFX and audio hooks, each with a review document.
- [ ] **R10.5 Equipment, consumables, affixes, sets.** More items per slot, affixes that change how the hero
  plays, set bonuses, and consumables with meaningful trade-offs; icons and overlays in the same batch style.
- [ ] **R10.6 Authored VFX library.** Impact, cast, aura, pickup, level-up, weather and death effects as
  authored textures and shaders instead of shape primitives, with a budget per effect and a capture sheet.
- [ ] **R10.7 Story art kit.** Portraits, panel backgrounds, props and title art for the narrative phases.

## R11 — Content volume and playtime  `+110 experience`

- [ ] **R11.1 Playtime model.** Targets: 8–12 minute runs, an act arc of roughly 40–60 minutes, 20–30 hours
  to full completion. Measured against automated play plus recorded smoke timings, not estimated.
- [ ] **R11.2 Act structure.** Three acts of twelve waves with intermissions, mini-bosses at 4/8/12, escalating
  modifiers and a legible threat curve between waves 25 and 80 (the flat middle the audit measured).
- [ ] **R11.3 Mirror mode.** Endless run that mirrors the player's own build back at them with scaling
  modifiers, ending in a score and a local record board.
- [ ] **R11.4 Seeded daily trial.** One generated seed per day, offline, fixed loadout, comparable score.
- [ ] **R11.5 Achievements and bounties.** 60+ entries that unlock cosmetics, codex pages and titles; each
  entry has a testable trigger.
- [ ] **R11.6 Ascension tiers.** New-game-plus with modifiers that change how the game is played, not only how
  hard it hits.
- [ ] **R11.7 Playtime telemetry, local.** Run durations, wave reached, deaths and builds recorded in the save
  file so the playtime model can be checked against real sessions; no network.

## R12 — Human-feel innovation  `+110 experience`

- [ ] **R12.1 Living arena.** Critters and ambient life, weather that changes wave behaviour, a day/night cycle
  that alters enemy and plant behaviour, occasional allied spirits.
- [ ] **R12.2 Physicality.** Weight in movement, knockback that reads, hit-stop measured in milliseconds,
  screen-space impacts that never obscure gameplay-critical information.
- [ ] **R12.3 Expressive hero.** A mood state that shows in posture and lines, near-death presentation
  (heartbeat, vignette, slowed music), reactions to the tree, to boss kills and to the first defeat.
- [ ] **R12.4 Scripted beats.** Hand-authored moments inside runs: the tree speaks, a wolf refuses to attack, a
  wandering merchant appears mid-wave, a dying enemy drops a note.
- [ ] **R12.5 Adaptive pressure.** A documented, testable policy with a visible indicator, never a hidden
  difficulty rubber band.
- [ ] **R12.6 Accessibility and comfort.** One-hand mode, colour-blind palettes, text scaling, reduced flash,
  pause anywhere, hold-to-confirm for destructive actions.
- [ ] **R12.7 Touch feel.** Haptic vocabulary per event, precision assist for drag targets, a mis-tap
  forgiveness window, all covered by the touch smoke journeys.

## R13 — Engagement without monetisation  `+110 experience`

- [ ] **R13.1 The 30-second loop.** Tap to visible feedback under 100 ms, measured on the emulator; the first
  wave starts within 5 seconds of launch.
- [ ] **R13.2 Reward cadence.** Unlocks, drops and discoveries per minute of play defined as a schedule and
  verified against the drop tables and the simulator.
- [ ] **R13.3 One-more-run hooks.** Visible run seed, personal bests, streaks, a "next run" panel that shows
  what changed.
- [ ] **R13.4 Mastery curve.** Skill-expressive systems (focus targeting, ability timing, positioning) with a
  measured gap between an optimiser policy and a casual policy.
- [ ] **R13.5 Session boundaries.** Save anywhere, resume in under 3 seconds, no lost progress on process
  death; each budget has a test.
- [ ] **R13.6 No dark patterns.** A checklist in the repository — no energy timers, no loot boxes, no
  artificial grind gates, no notification pressure — enforced by a test over the systems that could grow one.

## R14 — Mysteries and secrets  `+80 experience`

- [ ] **R14.1 Secret registry.** Data-driven definitions with trigger conditions, discovery states, hinting
  and rewards; deterministic tests prove each trigger fires exactly when intended.
- [ ] **R14.2 Environmental mysteries.** Hidden interactions in the arenas: a hollow that answers three taps,
  a reflection that only appears at night, a crystal that hums a sequence, a tile that remembers a seed.
- [ ] **R14.3 Lore fragments.** 40+ collectible fragments that cross-reference each other; a test asserts the
  graph is connected and no fragment is orphaned.
- [ ] **R14.4 Rare run anomalies.** 1-in-N events with seeds a test can replay: red moon, a stranger at the
  gate, a wounded beast that can be spared, a wave that arrives from the wrong direction.
- [ ] **R14.5 Meta-mystery.** A puzzle spanning save files whose solution path is documented in the repository
  but never stated in the game's UI.
- [ ] **R14.6 Spoiler discipline.** Hidden content names, art and text must not appear in boot flow, menu
  hints, tutorial text or the store-facing strings; enforced by a test.

## R15 — Narrative and cinematics  `+100 experience`

- [ ] **R15.1 Narrative bible and delivery map.** Characters, acts, beats and, for every beat, the exact
  in-game delivery (cinematic, dialogue, environmental, audio) tracked like an asset.
- [ ] **R15.2 Cinematic timeline runtime.** A data-driven timeline for camera, letterbox, focus, shake,
  typewriter text, fades and audio cues; deterministic, skippable, unit-tested, and usable by the smoke suite.
- [ ] **R15.3 Opening prologue.** A 45–60 second staged prologue animation replacing the text-only opening,
  skippable, replayable from the menu, recorded in CI as screenshots.
- [ ] **R15.4 Interludes.** 10–20 second panels at act transitions, first boss kill, first defeat, tree
  ceremonies and the ending; each act dressed differently.
- [ ] **R15.5 Dialogue system.** Portraits, speaker names, typewriter pacing, localisation-ready strings,
  with a test that every line exists in every supported language.
- [ ] **R15.6 In-world storytelling.** Props, epitaphs, tree carvings and an arena whose state changes as the
  story progresses.
- [ ] **R15.7 Narrator stingers.** Short narration motifs per act and per major beat, produced with the audio
  phases and registered like every other audio asset.

## R16 — 2026 benchmark and the experience re-audit  `gate`

- [ ] **R16.1 Benchmark rubric.** Six reference titles of the genre and roughly forty comparable criteria
  (menu-to-game time, input latency, content volume, presentation polish, session design, meta depth,
  accessibility, secrets, story delivery, offline-first design), each with a measurement method.
- [ ] **R16.2 `docs/audit/BENCHMARK_2026.md`.** Our build measured on every criterion, generated by a tool
  where a tool can do it and by recorded captures where it cannot.
- [ ] **R16.3 Gap closure.** Every criterion below the reference either fixed or recorded as an accepted
  trade-off with the reason; nothing quietly dropped.
- [ ] **R16.4 Experience re-audit.** Score the experience rubric with the same granular method, publish it
  next to the repo-rubric score, and list the remaining weaknesses plainly.
- [ ] **R16.5 Final claim gate.** Both gates at target, no open `[!]`, then and only then the "1000-level"
  sentence is allowed.

## RX — Release preparation *(out of scope by owner direction; recorded for the arithmetic)*

Version/versionCode, `minify`/`shrinkResources`, a real tag and release, `allowBackup=false`,
real-device testing: **+35 points, not planned here.**

---

## Progress log

| Date | Phase | Item | Evidence | Commit |
|---|---|---|---|---|
| 2026-09-16 | 78 | R1.1 | 107 sheets restored to the reviewed tier, manifest `78.0-integrity-recovery-runtime-tier`, validator PASS, ledger 153 entries / 0 mismatches | `9d31f70` |
| 2026-09-16 | 78 | R1.2 | premium contract tests restored from `5374f2c^`; render package green; data fixed instead of assertions relaxed | `9d31f70` |
| 2026-09-16 | 78 | R1.3 | `classify_master_tier.py` + `MASTER_TIER_PROVENANCE.md` | `9d31f70` |
| 2026-09-16 | 78 | R1.4 | `AssetIntegrityTest` + validator ledger gate; negative control verified by hand | `9d31f70` |
| 2026-09-16 | 78 | R1.8 | `docs/audit/AUDIT_2026-09-16.md` + README pointer | `9d31f70` |
| 2026-09-16 | 78 | R1.1 (bug) | the ledger had been written from pre-restore bytes; the new test caught it | `9d31f70` |
| 2026-09-16 | 78 | R1.9 (partial) | gate negative controls (NEAREST 2×/3× rejected, honest sheet passes, drift detectable) | `b855e97` |
| 2026-09-16 | 78 | CI failure | first push turned `Test core logic` red (numpy not installed); fixtures rewritten with Pillow only | `b855e97` |
| 2026-09-16 | — | Phase plan | remaining work expressed as numbered English phases (79–97) | `492e057` |
| 2026-09-16 | 79 | R1.5 | `TestIntegrityTest` ratchet + negative controls; arena test's last softened comments replaced by a ledger check | `71bab7d` |
| 2026-09-16 | 80 | R1.6 | validator reports 7 measured gates vs 3 config-presence checks, exact reviewed-tier pin, config-vs-art divergence printed; 3 tests added | `651d56a` |
| 2026-09-16 | 85b | R2.4 | architecture ratchet enforced: layer imports, 600-line and 40-field ceilings, four frozen offenders that may only shrink, with negative controls | `0ccc92c` |
| 2026-09-16 | 85a | R2.1 | dead `PostProcessRenderer` deleted; new packaging test fails on any `Gdx.files.internal` literal without a shipped file (5 literals checked, 15 computed paths reported) | `16bac49` |
| 2026-09-16 | 84 | R1.10 | CRITICAL_REVIEW status header with re-measured numbers, style-guide budget note, ASSET_ENGINE values pointed at the manifest, batch-record banners on 21 review docs; count-like claim scan: 8 documents left, all accounted for | `e406773` |
| 2026-09-16 | 81 | R1.7 step 1 | emulator smoke test measures every captured frame and publishes `brightness-measurements.txt` in the CI artifact; assertions unchanged in this step | `9089e1b` |
| 2026-09-16 | 81 | R1.7 step 2 | per-screenshot brightness references enforced (28 captures, vfx included), ±8 luma, lit fraction −0.10, absolute floor 30; measured min 34.17 / max 48.30 | `772fb45` |
| 2026-09-16 | — | CI | run [`35078435013`](https://github.com/amirrezahadipoor/Herodefense/actions/runs/35078435013) at `0ccc92c`: both workflows green (covers phases 81 step 1, 83, 84, 85a, 85b) | `0ccc92c` |
| 2026-09-16 | — | CI | run [`35079208076`](https://github.com/amirrezahadipoor/Herodefense/actions/runs/35079208076) at `772fb45`: both workflows green with the per-screenshot brightness contract active; 28/28 captures referenced, drift ≤ 0.07 luma except the animated collapse frame (7.14), which is why that entry carries a ±12 band | `772fb45` |
| 2026-09-16 | 83 | R1.11 | ten post-audit art ids moved to their own contract record; generated revision-label blocks in 10 review documents; the manifest is the single source of truth for the four revision labels, and 36+6+4 assets were re-stamped to match their real provenance | `37b6aec` |
| 2026-09-16 | 86 | R2.2 slice 1 | `RunPresentationSystem` extracted from the god class; `HeroDefenseGame` 1,519 → 1,447 lines; ratchet freeze lowered to 1,447 / 91; layered-event test strengthened to scan both files and require exactly one binding per effect | `49923b3` |
| 2026-09-16 | — | Roadmap v3 | experience phases added at the owner's direction: asset quality and expansion, playtime and content volume, human-feel innovation, engagement without monetisation, secrets and mysteries, narrative and cinematics, and a 2026 benchmark; experience rubric defined as the headline number | `442687b` |
| 2026-09-16 | 88 | R3.2 | eight encounter scripts over forty encounters (tell size 0.85×–1.35×, one or two half hits per warning, an enrage that shrinks the tell) with a deterministic encounter table; four wider drafts were measured and dropped because they moved the sweep's worst-wave cells by 15–45 %, so physics and warning timing are pinned by test and the pinned roster reproduces the phase-87 cells bit for bit; 17 new or extended test cases, and the two spike matrices now sample five seeds instead of three with every ceiling unchanged | `4af8101` |
| 2026-09-16 | 87 | R3.1 | tap-to-focus: the player can now point the bow at any enemy for 6 s (×1.2 damage), release it with a tap on empty ground, and see the window fade out; 11 new test cases; the wave is no longer a spectator sport | `499da95` |
| 2026-09-16 | 86 | R2.2 slice 3 | per-state frame build extracted into `ScreenStateComposer` (arena, actors, effects, HUD, all overlays) behind a 51-getter port; `HeroDefenseGame` 1,201 → 1,072 lines; ratchet freeze lowered; guard test extended to the draw calls | `177aabe` |
| 2026-09-16 | — | CI honesty | `937e8b2` turned `Test core logic` red: the pipeline's Python UI source test still looked for the touch lifecycle inside the game class that slice 2 had just emptied. Fixed in `c8c2b27`, which reads the router instead, so the check follows the code rather than a file location | `c8c2b27` |
| 2026-09-16 | 86 | R2.2 slice 2 | per-screen touch chain extracted verbatim into `ScreenTouchRouter` behind a `Host` port; `HeroDefenseGame` 1,447 → 1,201 lines; ratchet freeze lowered; new guard test fails if a touch layout returns to the game class; prior progress-log rows back-filled with their real commit hashes | `937e8b2` |
| 2026-09-16 | 82 | R1.9 · R8.2 | `RuntimeResidency` + `RuntimeResidencyTest` (catalog 361,279,488 bytes; combat set 76.8 MiB vs a 100 MiB budget) with negative controls; budget recorded in the restore tool | `317728c` |

| 2026-09-16 | 86 | R2.2 slice 4 | wave director (death and the tree falling, level-up pause, wave advance with the boss-entrance beat and reflection line, reward-card offer, planting ceremony, completed run) extracted into `gameplay/WaveDirector` behind a five-method `Host`; `HeroDefenseGame` 1,067 → 1,056 lines, fields 92 → 93 for the director handle and the ratchet records both; layered-event guard now scans four files | `b9b089a` |

| 2026-09-16 | 86 | R2.2 slice 5 | combat frame (arrows and all their effects, melee feedback, auto-potion, item and potion drops, kill rewards, elite affixes, codex unlocks, pickups with auto-sell) extracted into `gameplay/CombatSystem` with sixteen collaborators injected and one `Frame(gameOver, leveledUp)` result; `HeroDefenseGame` 1,056 → 980 lines (35 percent smaller than the audited 1,519-line god class), fields 93 → 94, ratchet freeze lowered in the same commit | `e9d1840` |

| 2026-09-16 | 86 | R2.2 slice 6 | run session (fresh run, restart at the same tier, ascension, and continue-on-the-right-screen) extracted into `gameplay/SessionController`; the three duplicated fifteen-line resets collapsed into one `prepareFreshRun()`; `HeroDefenseGame` 980 → 954 lines (37 percent smaller than the audited 1,519-line god class), fields 94 → 95, ratchet freeze lowered in the same commit | `eab4fcc` |

| 2026-09-16 | 86 | R2.2 slice 7 | the prologue and the grove-planting ceremonies extracted into `gameplay/CinematicFlow` (tier snapshot, planted tree, water drops, wave hand-off and the boss entrance at it); `HeroDefenseGame` 954 → 927 lines (39 percent smaller than the audited 1,519-line god class), fields 95 → 94, ratchet freeze lowered in the same commit | `7d1b7da` |

| 2026-09-16 | 86 | R2.2/R2.3 | `WaveDirectorTest` (5 cases) and `SessionControllerTest` (7 cases) make the two extracted flows testable without libGDX: `audio/AudioPlayback` and `save/RunSaveRepository` now sit between the gameplay systems and the libGDX-backed classes that open resources in their constructors | `44285d4` |

| 2026-09-16 | 86 | R2.6 | test-cost measurement (3 suites = 311 of 322 s of test time, 157 classes / 579 tests) plus two speed changes that keep every gate: two parallel test JVMs (`-PserialTests` to opt out) took the full suite from 7 m 29 s to 3 m 45 s, and `-PfastTests` gives a 21-second local loop that skips only the three sweeping suites, which CI still runs on every push | `705a3a3` |

| 2026-09-16 | 86 | R2.2 slice 8 | renderer ownership (27 renderers + the batch) extracted into `render/RenderStack`, with the teardown kept statement for statement; `HeroDefenseGame` 927 → 797 lines (47 percent smaller than the audited 1,519-line god class), fields 94 → 68; first slice verified on the new fast loop (573 tests in 26 s) | `8af0ed1` |

| 2026-09-16 | 86 | tests | full suite on the shipped tree at slice 8: 157 classes / 579 tests / 0 failures in 4 m 07 s (two forks, was 7 m 29 s serial) | `c2b6933` |

| 2026-09-16 | 86 | R2.2 slice 9 | the frame extracted into `presentation/FrameDriver` (frame order, pause record, the two timed story lines, ambient clock, game-over timer) plus two ports that make it testable (`audio/AudioFrame`, injected `NanoClock`); `HeroDefenseGame` 797 → 760 lines (half of the audited 1,519), fields 68 → 61; `FrameDriverTest` 6 cases | `c055436` |

| 2026-09-16 | 89 | R3.3 | twelve persistent trophies with a counter, a target and a progress reading; announced at the save point (haptic + chime + one HUD line that yields to a story line); Codex gained a LORE/TROPHIES tab strip with its own shelf and details panel; `TrophyLedger` survives `resetForNewRun` and `TrophyBook.migrate` back-fills a pre-trophy save from the progress it already had; 20 new test cases | `7323587` |

| 2026-09-16 | 89 | R3.5 | a second run length: `GameMode.STANDARD` (200) / `BRIEF` (30) with `GameState.runLengthWaves()`, a brief-vigil row in the re-tabled six-row main menu, the game-over overlay showing this run's length, and `BalanceSimulator.runBrief`; measured: the brief run's first 30 waves are bit-identical to the long run's first 30 on the same seed, spike 0.1229 vs the 0.40 ceiling, its own bounded clear-time rule recorded as the one deliberate difference; 5 new simulation cases | `b9027b7` |

| 2026-09-16 | 89 | R3.4a | wave omens as the thirteenth drafted trial: `model/WaveModifier` (SWARM / IRON_HIDE / BLOODRUSH / QUICKSTEP, +25% coins on the wave), `gameplay/WaveOmens` policy (never on a boss or elite wave), a HUD line, and `TrialEffects.omensEnabled` read by the spawner, the factory, kill rewards and the HUD; the untrialled run is unchanged by construction, so the counterfactual is the same seed without the trial | `af92ff1` |

| 2026-09-16 | 89 | R3.4c | enemy roster 4 → 8: four authored enemies (Bark Stalker, Sap Hound, Husk Warden, Bramble Thrall) with per-type means held exactly (health 117 / damage 28 / exp 69 / coins 19 / speed 242 / reach 172 / interval 5.10 over eight roles), a staggered spawner that keeps waves 1-10 on the field roster and interleaves deep waves, 384 px masters rendered in CI and published onto the reviewed 192 px tier with `masterRender` provenance, two art defects found and fixed by the gates (frame-border extremes, a flat idle), 17 review sheets + audit hashes, catalog budget 370 → 390 MB, combat set 95.9 MiB of 100 MiB, and the ascension gate's one red cell (forced Dodge, tier 6, seed 20342418142676295 at 15.907% vs a 15% ceiling) repaired by re-deriving the per-tier health/damage bumps (0.0005/0.0002 → 0.0004/0.00025), a change tier 0 cannot see | `b186c42` · `0e32e8a` · `621bec6` |

| 2026-09-16 | 89 | R2.5 | static analysis in CI: SpotBugs 4.8.6 (bytecode, main + test) and PMD 7.7.0 (source, main + test) run as `:core:ciStaticAnalysis` next to the tests, reports uploaded on failure; first pass found 306 + 171 findings in main and 3,198 + 7 in test, ~50 fixed in code (dead stores, a `keySet()`+`get()` loop, `%2==1`, a locale-free `toUpperCase`, ten `catch (NullPointerException)` parses, uncalled private methods, duplicated literals, a leaked renderer in a test, two float loop indices) and 16 rule classes excluded **with a written reason each** in `docs/STATIC_ANALYSIS.md` | `0a987ae` |

| 2026-09-17 | 90 | R3.6 | playtest protocol and ledger: the game writes a `herodefense.run-record/1` JSON beside the save when a run ends (both endings, newest twelve kept, nothing about the player), `tools/playtests/promote_run_record.py` files the raw record as evidence and fills the session from it, `tools/playtests/validate_playtest_ledger.py` checks every finding against the real roadmap (item must exist, `fixed` must name a commit, `accepted` must say why) and runs in CI with 16 Python cases; first automated session promoted (seed 20342418142676294, BRIEF, 30/30, avg 0.0511, peak 0.1756) and two findings opened against R4.1 and R3.6 | `ef55da9` |

| 2026-09-16 | 89 | R3.4b | item pool: `ItemDropSystem.chooseFor` + a `tierPool` fallback that cannot index an empty tier, the pool composition asserted (46 pieces, 14/12/9/5/6, one mythic per slot, the first ring slot stops at rare) and the ownership-aware pick measured and held back with its gate numbers (ascension LIFESTEAL tier-0 40.24%, trial BOSS_BOUNTY+FAMISHED_EARTH 43.33%) | `5b78835` |
| 2026-09-17 | 91 | R7.7 | the main menu's press states and the device journey's menu taps both still read the pre-R3.5 row table; both now derive from `MainMenuTouchLayout.rowBottom(row)`, and the journey finds a row by asking for its action -- the nine Android journeys that have been timing out behind a missed tap are verified green on the device by the Android job on `05d28cc` (run 35156565880, all eleven journeys) | `398e149` |
| 2026-09-17 | 91 | R4.2 | the boss telegraph asserted as a contract: five promises over four identities x forty encounters x tiers 0/3/6/10 x both vigils, 6 cases in 0.06 s, with the roster table and the per-strike damage published | `67bdd26` |
| 2026-09-17 | 91 | R4.3 | the drop economy's table generated from the code and drift-gated against `docs/BALANCE.md`, plus three measured pity rules recorded with the ceilings each one broke | `06b2676` |
| 2026-09-17 | 91 | ratchet | `BalanceSimulator` stood at 715 lines against its 661 freeze after R4.1's policy switch; `WaveSample` and `BalanceReport` moved to their own files in the same package (a published shape with its own reason to exist), leaving the simulator at 660 and every balance gate green | `bfeff15` |
| 2026-09-17 | 91 | balance | the second half was split into two spans and the second half's elite contact multiplier softened to 1.2, so the step into the second half rises from x1.047 to x1.158 and the late spikes land on a curve that paid for them; full core suite green | `50bde24` |
| 2026-09-17 | 91 | ci | the balance sweeps became a tagged `:core:balanceGate` task and a second CI job with its own timeout and report (unit loop 31 s, gate 4 m 47 s, no suite running twice) | `15804e1` |
| 2026-09-17 | 91 | docs | `docs/BALANCE.md`'s numbers became generated blocks with markers and a gate that names the block that drifted (`BalanceDocumentTest`, `:core:regenerateBalanceDoc`); the hand-typed checkpoints and the R4.6 table are both machine-checked now | `6424fb1` |
| 2026-09-17 | 91 | ladder | every tier now charges from its first wave: the enemy baseline carries the tier's health and damage charge, fading over 140 waves, so tier 10's brief pressure is 0.0507 against tier 0's 0.0901 and its reach 77.3 waves against 71.3, at the price of a 32% longer tier-10 session that the session gate now budgets for | `af70976` |
| 2026-09-17 | 91 | elites | the elite cadence skipped the boss lap, so tiers 6-8 had no elites at all in a 200-wave run and the test that pinned it checked the interval arithmetic rather than the outcome; the cadence is `7 → 6 → 4` and the assertion now requires elites at every tier | `75e932b` |
| 2026-09-17 | 91 | render | the render pipeline became resumable, comparable and deterministic to compare: a hash log of every sheet, atlas and descriptor it writes (no timestamps, no commits, no absolute paths, so two logs of the same bytes are equal), a diff that classifies added / changed / removed with `--fail-on-change` for promotion, and a resume list the workflow feeds straight back into the generator's `--only`; 321 files across 111 assets on the shipped tree, ten new tests in the unit job | `57e21ac` |
| 2026-09-17 | 95 | R7.1 | the first vigil: five coached steps in one minute, each waiting for its own touch action or its own budget, skippable through a 150-by-100 HUD-sized target that swallows the tap, and taught exactly once per device through the settings the run already writes; 21 tests cover the rules, the geometry and the seen-once path | `3b58f4e` |
| 2026-09-17 | 95 | R7.2 | every displayed stat now says what it is: one catalog for the five talents and ten HUD readouts, the shop's help panel explains the row the player touched, and the old "+1 rating" lines — three of which named units the code does not have — were replaced by the interpolated constants; 12 tests cover the keyword table, the line budget, the wrapping and the drift between text and numbers | `03d7166` |
| 2026-09-17 | 96 | R8.5 | every documented performance number now traces to a logged run, a committed budget, a named constant or a recorded environment fact; nine uncited numbers fixed, a stale residency figure corrected by measurement, and the gate runs in CI with its own negative controls | `cb457b8` · `2783f05` |
| 2026-09-17 | 96 | R8.3 (rule half) | residency bounded by `AtlasResidencyPolicy` instead of by luck: a page is released after its sheet family has been off screen for a stated interval, a re-entry reloads inside the budget, `ResidencyReport` prints what the rule costs, and `AtlasResidencyPolicyTest` covers release, reload and the ceiling | `b32ad6e` |
| 2026-09-17 | 96 | R8.3 (measurement half) | the wave-50 instrumented test logs one stable line, `tools/perf/check_wave50_memory.py` reads it against the committed 409,600 KiB / 163,840 KiB budget and exits 2 when no measurement was taken, with 20 Python cases including the negative control that the diagnosis line is not mistaken for a measurement | `0e5c70a` |
| 2026-09-17 | 96 | R8.4 | the cold start is measured after the instrumentation run: the script reinstalls the debug APK, resolves the launcher component and times `am start -W`, and `tools/perf/parse_startup.py` + `log_run.py` + `render_performance_doc.py` turn that into a filed run and a generated document | `6f90222` · `cb457b8` |
| 2026-09-17 | 96 | CI | the first emulator run of the wave-50 test measured nothing (a single truncated shell read) and the cold start ran against an uninstalled package; both diagnosed from the job log, fixed in the script and the test, and a compile error CI found in the new stat helper fixed by parsing `MemoryInfo`'s string stats | `8a2e05f` · `3b7c580` |
| 2026-09-17 | 93 | R5.5 · R5.6 | 39 material maps (normal / roughness / AO) derived from the shipped masters with a provenance table and a gate that re-derives every one of them, and a review-evidence step that regenerates a rendered batch's contact sheets and audit JSON from the batch itself | `05e2f79` |
| 2026-09-17 | 96 | R8.1 (payload) | a first-party ETC2 encoder held to Google's reference implementation by four committed blocks, KTX v1 containers the runtime can already upload, a per-device source policy with six cases, and an encode tool that measures decoded bytes, mask share and round-trip PSNR; measured 28.88 dB against the reference encoder's 33.57 dB, below the 32 dB bar, so no sheet ships compressed yet and the PNGs stay the payload | `bd598a9` |
| 2026-09-17 | 96 | R8.2 · R8.3 · R8.4 | the first green emulator run files the numbers three rows were waiting on: wave-50 residency 151,837 KiB PSS of a 409,600 KiB budget (source=debug.MemoryInfo), the cold start 882 ms of 6000 ms and the debug APK 20,872,599 bytes of 120,000,000 -- each a logged run the performance page regenerates from -- and the stale 95.9 MiB combat-set note is corrected to the gate's 99.3 MiB in the same commit | `8784207` |

## Definition of done

Two gates, both measured with the granular method of the 2026-09-16 audit and both reproducible from the
repository:

1. **Repo rubric (Phase 97):** the nine in-scope categories score **≥ 900 of 920** — the release category
   stays out of scope by owner direction — and every figure in this file is still reproducible.
2. **Experience rubric (Phase 144):** **≥ 900 of 1,000**, with no category below 80 % of its weight.

Only then may this repository describe the game as a "1000-level" title, and the sentence has to name both
scores and the commit they were measured on. Until then every claim stays per-item, with its evidence, and
the remaining weaknesses are listed instead of hidden.
