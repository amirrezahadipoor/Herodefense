# Roadmap to a real 1000

> **خلاصه فارسی (۳ خط):** امتیاز مرجع این مخزن ۵۵۰ از ۱۰۰۰ است. این فایل برنامه‌ی بستن کسرهاست؛ هر تیک فقط با شاهد قابل بازتولید زده می‌شود و هیچ تستی برای سبز شدن شل نمی‌شود. دسته‌ی «آماده‌سازی انتشار» بنابر نظر صاحب پروژه بیرون از محدوده است، پس سقف این نقشه راه **۹۲۰ از ۱۰۰۰** است (۸۰ امتیاز آن دسته). ریزمترها و معیار پذیرش هر فاز، پایین‌تر در بخش‌های `R1`–`R8` آمده است.

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

Status legend: `[x]` verified · `[~]` in progress · `[ ]` not started · `[!]` stopped, with reason.

## 2. Phase plan (numbered, English)

`Phase 78` is complete and pushed. `Phase 79` onward is the remaining work, one phase per commit.

| Phase | Title | Roadmap items | Status |
|---|---|---|---|
| **78** | Integrity recovery: reviewed runtime tier restored, contract tests un-gutted, hash ledger + resampling/ledger gates | R1.1 · R1.2 · R1.3 · R1.4 · R1.8 | `[x]` |
| **79** | Test-integrity meta-test: weakening a test fails the build | R1.5 | `[x]` |
| **80** | Gates that measure output, not script text | R1.6 | `[x]` |
| **81** | Emulator brightness contract restored honestly | R1.7 | `[ ]` |
| **82** | Negative control for every gate (fixtures that must fail) | R1.9 · R8.2 | `[x]` |
| **83** | Review coverage for the ten post-audit art ids | R1.11 | `[x]` |
| **84** | Documentation honesty sweep (stale and inflated docs) | R1.10 | `[~]` |
| **85** | Dead code out, architecture ratchet in | R2.1 · R2.4 · R2.5 | `[ ]` |
| **86** | Break up `HeroDefenseGame` into systems, with tests | R2.2 · R2.3 | `[ ]` |
| **87** | Gameplay: one real player decision inside a wave | R3.1 | `[ ]` |
| **88** | Boss identity variety across the 20 encounters | R3.2 | `[ ]` |
| **89** | Meta progression, content breadth, run shape | R3.3 · R3.4 · R3.5 | `[ ]` |
| **90** | Human playtest protocol and recorded findings | R3.6 | `[ ]` |
| **91** | Balance program: scaling threats, telegraph contract, drop economy, generated docs, CI band | R4.1 – R4.5 | `[ ]` |
| **92** | Runtime tier composed from the genuine master renders | R5.1 · R5.2 | `[~]` |
| **93** | Render pipeline: reliability, rendered grading, PBR maps, per-batch reviews, real VFX | R5.3 – R5.7 | `[ ]` |
| **94** | Audio program: music breadth, SFX coverage, state machine, settings | R6.1 – R6.4 | `[ ]` |
| **95** | Onboarding, tooltips, Persian + RTL, Back button, accessibility, store UI | R7.1 – R7.6 | `[ ]` |
| **96** | Memory and performance program: compression, budgets, wave-50 residency, startup/APK | R8.1 – R8.5 | `[ ]` |
| **97** | Re-audit with the same granular method and publish the new score | Definition of done | `[ ]` |

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

## R1 — Truth, tests and verification integrity  `+95`

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
- [ ] **R1.7 Emulator brightness contract.** Either the vibrant grade is re-applied in the render
  pipeline so `AndroidTouchSmokeTest` returns to `MIN_MEAN_LUMA = 34` with `vfx-*` screenshots included,
  or the threshold is replaced by a per-screenshot reference fingerprint.
- [x] **R1.8 Audit published inside the repository.** `docs/audit/AUDIT_2026-09-16.md` + README pointer.
- [x] **R1.9 Negative control for every gate.** Fixtures that must fail exist for the resampling gate
  (NEAREST 2× and 3×), for hash-ledger drift, for the test-integrity scanner, and now for the residency
  budget (an over-budget set and a sheet that was quadrupled both throw). The review-coverage gate gets
  its control together with R1.11, when the gate exists.
- [~] **R1.10 Documentation honesty sweep.** Withdrawn: `ASSET_SCORE_950.md`; correction banner:
  `RELEASE_v0.5.0-vibrant-950.md`; annotated: `ROADMAP.md` Phase 54–77 claims and the "Standing Rules
  for HD". Remaining: a status header on the stale `CRITICAL_REVIEW_2026-09-13.md` and a final grep for
  numbers that no artifact produces.
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

- [ ] **R2.1 Remove or finish `PostProcessRenderer`.** Delete the class and its dangling shader
  references, or implement it with real shaders plus a measured frame-time proof.
- [ ] **R2.2 Break up `HeroDefenseGame`** into `WaveDirector`, `CombatSystem`, `ArenaRendererFacade`,
  `HudFlow`, `SessionController` with a ~400-line ceiling per class, validated by the full suite plus a
  before/after emulator smoke run.
- [ ] **R2.3 Unit-test the extracted systems** (spawn scheduling, damage, reward selection, save/restore).
- [ ] **R2.4 Architecture ratchet test.** `model`/`balance` must not import `render` or graphics classes;
  no class over 600 lines; no class over 40 fields; no new mutable static state. Existing offenders are
  listed explicitly and the list may only shrink.
- [ ] **R2.5 Static analysis in CI** (ErrorProne or SpotBugs + PMD), findings triaged not silenced.

## R3 — Gameplay depth  `+45`

- [ ] **R3.1 Player agency inside a wave.** One active, touch-only decision per wave (target priority,
  dodge step, or aimed/charged shot), tuned with the simulator and exercised by the balance sweep.
- [ ] **R3.2 Boss identity variety.** 20 encounters map to ≥ 8 distinct fight scripts (specials,
  telegraph shapes, arena modifiers) with a test asserting the mapping.
- [ ] **R3.3 Meta progression.** Achievements/unlocks persisting between runs, with a save migration.
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

## R9 — Release preparation *(out of scope by owner direction; recorded for the arithmetic)*

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
| 2026-09-16 | 83 | R1.11 | ten post-audit art ids moved to their own contract record; generated revision-label blocks in 10 review documents; `ReviewLabelBinding` enforced in core and in the validator | *(this commit)* |
| 2026-09-16 | 82 | R1.9 · R8.2 | `RuntimeResidency` + `RuntimeResidencyTest` (catalog 361,279,488 bytes; combat set 76.8 MiB vs a 100 MiB budget) with negative controls; budget recorded in the restore tool | *(this commit)* |

## Definition of done

The roadmap is complete when the same granular audit, re-run on the then-current commit (Phase 97),
scores **920/1000 or more** on the nine in-scope categories and every figure in this file is still
reproducible from the repository.
