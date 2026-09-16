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
| **85** | Dead code out, architecture ratchet in | R2.1 · R2.4 · R2.5 | `[~]` |
| **86** | Break up `HeroDefenseGame` into systems, with tests | R2.2 · R2.3 | `[~]` |
| **87** | Gameplay: one real player decision inside a wave | R3.1 | `[x]` |
| **88** | Boss identity variety across the 20 encounters | R3.2 | `[x]` |
| **89** | Meta progression, content breadth, run shape | R3.3 · R3.4 · R3.5 | `[~]` |
| **90** | Human playtest protocol and recorded findings | R3.6 | `[ ]` |
| **91** | Balance program: scaling threats, telegraph contract, drop economy, generated docs, CI band | R4.1 – R4.5 | `[ ]` |
| **92** | Runtime tier composed from the genuine master renders | R5.1 · R5.2 | `[~]` |
| **93** | Render pipeline: reliability, rendered grading, PBR maps, per-batch reviews, real VFX | R5.3 – R5.7 | `[ ]` |
| **94** | Audio program: music breadth, SFX coverage, state machine, settings | R6.1 – R6.4 | `[ ]` |
| **95** | Onboarding, tooltips, Persian + RTL, Back button, accessibility, store UI | R7.1 – R7.6 | `[ ]` |
| **96** | Memory and performance program: compression, budgets, wave-50 residency, startup/APK | R8.1 – R8.5 | `[ ]` |
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
- [ ] **R2.5 Static analysis in CI** (ErrorProne or SpotBugs + PMD), findings triaged not silenced.
- [x] **R2.6 A test budget: the suite's cost is measured, and the local loop is not a second CI.**
  *Measured 2026-09-16 (157 classes, 579 tests, `./gradlew :core:test`):* three suites spend **311 of the
  suite's 322 seconds of test time** — `TrialSimulationTest` 109 s, `RewardCardSimulationTest` 106 s,
  `AscensionGateTest` 96 s — six cases between them. The other 154 classes cost about ten seconds
  together. Two changes follow from that number, neither of which lowers a ceiling:
  *Parallel forks.* `core/build.gradle` now runs two test JVMs when the machine has the cores (opt out with
  `-PserialTests`). The classes are independent and every gate seeds itself, so this is scheduling only:
  the full suite went from **7 m 29 s to 3 m 45 s** with the same 579 tests and the same numbers.
  *A local fast loop.* `./gradlew :core:test -PfastTests` leaves out those three sweeping suites and runs
  the rest in **21 seconds**. CI never passes the flag, so the suite that judges a push is still the
  complete one, and the sweeps still run locally at every phase boundary — the flag removes the duplication
  of running the same two-minute sweep locally *and* on the runner for every one-line change.
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
- [ ] **R3.4 Content breadth.** Enemy types 4 → 8+, item-pool diversity, wave modifiers.
- [ ] **R3.5 Session shape.** A shorter mode (e.g. 30 waves) or checkpoints, measured with the simulator.
- [ ] **R3.6 Human playtest protocol** plus recorded sessions; findings become roadmap items.

## R4 — Balance and difficulty curve  `+42`

- [ ] **R4.1 Threats that scale.** Publish a win-rate band for a non-optimiser policy; make the curve
  rise after wave 40.
- [ ] **R4.2 Boss damage lands at the end of the telegraph**, asserted as a contract.
- [ ] **R4.3 Drop economy** with an EV table generated from the drop tables and a pity rule.
- [ ] **R4.4 `BALANCE.md` generated by the simulator**, so the document cannot drift.
- [ ] **R4.5 Balance regression in CI** on fixed seeds; a change outside the band fails the build.

## R5 — Visual assets  `+68`

- [x] **R5.1 The "HD" claim settled with measurements** (Phase 78 evidence above).
- [ ] **R5.2 Compose a runtime tier from the genuine masters.** The 27 sheets with measured detail gain
  become the runtime tier once (a) the new frame layout has its own accepted, hash-bound review and
  (b) the memory budget allows 384-pixel frames.
- [ ] **R5.3 Render-workflow reliability**: resumable and deterministic, with a hash log for differences.
- [ ] **R5.4 Vibrant grade rendered, not filtered**, proven with before/after emulator screenshots
  (unblocks R1.7).
- [ ] **R5.5 PBR maps in the repository** (normal / roughness / AO) with material provenance.
- [ ] **R5.6 Review documents regenerate per batch.**
- [ ] **R5.7 VFX that exist at runtime**: rarity glow, impacts, trails, with evidence frames.

## R6 — Audio  `+28`

- [ ] **R6.1 Music breadth** (3–4 tracks) with licence and file hash per file.
- [ ] **R6.2 SFX coverage** (bow draw/release variants, crits, pickups, UI, telegraph, ambience) with a
  measured peak level each.
- [ ] **R6.3 Music state machine** tied to game state, asserted by a test.
- [ ] **R6.4 Audio settings** persisted, honouring audio-focus loss, tested.

## R7 — UI, onboarding and localisation  `+52`

- [ ] **R7.1 A 60-second onboarding**, skippable, touch-only, tested to appear exactly once.
- [ ] **R7.2 Stat tooltips** for every displayed stat, with a coverage test.
- [ ] **R7.3 Persian + RTL**: locale-aware string table, mirrored layout, and a test that fails on
  hard-coded or untranslated user-facing strings.
- [ ] **R7.4 Back button** handled in game and menus, with an instrumentation test.
- [ ] **R7.5 Accessibility**: font scaling, colour-blind-safe rarity encoding, measured contrast.
- [ ] **R7.6 Store-facing UI assets** (screenshots, description, feature graphic).

## R8 — Performance, memory and size  `+40`

- [ ] **R8.1 Texture compression** (ETC2/ASTC + fallback) and mipmaps with a measured comparison.
- [~] **R8.2 A memory budget enforced by a test.** `RuntimeResidency` computes residency from the
  manifest; `RuntimeResidencyTest` checks the catalog against `decodedCatalogBudgetBytes` (370 MB) and the
  live combat set against `decodedCombatResidencyBudgetBytes`, now a deliberate **100 MiB**
  (was 150 MB). Measured: catalog 361,279,488 bytes, combat set **76.8 MiB** — inside budget, and the
  test fails if that changes. Remaining: compression (R8.1) to bring the catalog itself down.
- [ ] **R8.3 Residency at wave 50** measured with `adb shell dumpsys meminfo` during a scripted run,
  logged in the repository, with streaming/release of atlases.
- [ ] **R8.4 Startup and APK budget** measured in CI against a committed threshold.
- [ ] **R8.5 Every performance number in `docs/**` comes from a logged run.**

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

| 2026-09-16 | 89 | R3.3 | twelve persistent trophies with a counter, a target and a progress reading; announced at the save point (haptic + chime + one HUD line that yields to a story line); Codex gained a LORE/TROPHIES tab strip with its own shelf and details panel; `TrophyLedger` survives `resetForNewRun` and `TrophyBook.migrate` back-fills a pre-trophy save from the progress it already had; 20 new test cases | `_PENDING_` |

## Definition of done

Two gates, both measured with the granular method of the 2026-09-16 audit and both reproducible from the
repository:

1. **Repo rubric (Phase 97):** the nine in-scope categories score **≥ 900 of 920** — the release category
   stays out of scope by owner direction — and every figure in this file is still reproducible.
2. **Experience rubric (Phase 144):** **≥ 900 of 1,000**, with no category below 80 % of its weight.

Only then may this repository describe the game as a "1000-level" title, and the sentence has to name both
scores and the commit they were measured on. Until then every claim stays per-item, with its evidence, and
the remaining weaknesses are listed instead of hidden.
