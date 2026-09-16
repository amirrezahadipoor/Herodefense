# نقشه راه تا امتیاز ۱۰۰۰ واقعی — Roadmap to a real 1000

**تاریخ:** ۱۶ سپتامبر ۲۰۲۶ · **مخزن:** `amirrezahadipoor/Herodefense` · **کامیت شروع:** `49fa799`

## خلاصه فارسی (برای صاحب پروژه)

- امتیاز فعلی پروژه بر اساس بررسی مستقل **۵۵۰ از ۱۰۰۰** است. این نقشه راه فقط برای بستن کسرها
  نوشته شده، نه برای «عددسازی».
- قانون اصلی: هر تیک فقط وقتی زده می‌شود که شاهد قابل بازتولید در مخزن وجود داشته باشد
  (دستور، فایل تولیدشده، اجرای CI). هیچ ادعای متنی پذیرفته نیست.
- قانون دوم: برای سبز شدن هیچ تستی، تست ضعیف نمی‌شود. حذف assertion، `assertTrue(true)`،
  `|| true`، `@Disabled` و کم‌کردن آستانه **ممنوع** است.
- بخش «آماده‌سازی انتشار» بنابر نظر شما از این نقشه راه **حذف** شده؛ بنابراین سقف این نقشه
  راه ۹۲۰ از ۱۰۰۰ است (۸۰ امتیاز آن دسته بیرون از محدوده).
- کارهای انجام‌شده (تیک‌خورده) در انتهای همین فایل در «گزارش پیشرفت» با شاهد آمده‌اند.

## English summary

This file is the working plan that answers the independent audit of 2026-09-16
([`docs/audit/AUDIT_2026-09-16.md`](audit/AUDIT_2026-09-16.md), **550/1000**). It is organised against
the audit's own category table so progress is measurable instead of rhetorical. Release preparation is
out of scope by owner direction, which puts the in-scope ceiling at **920/1000**.

Rules of the game:

1. **No fake evidence.** An item is ticked only when the repository contains something a stranger can
   re-run: a command, a generated artifact, a CI run, a screenshot with a fixed seed.
2. **No test may be weakened.** If an assertion must change, it becomes stricter or equally strict
   about the real contract, and the commit message explains why.
3. **Documentation numbers are generated**, never typed by hand.
4. **One item, one commit, one push.** The box is ticked in the commit that carries the evidence.
5. **Failure is recorded** as `[!]` with its reason, never quietly deleted.

Status legend: `[x]` verified done · `[~]` in progress · `[ ]` not started · `[!]` stopped, with reason.

---

## 0. Where the 450 missing points are

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

**In-scope target: 920/1000.** Current state: 550/1000 plus the recovery work recorded at the bottom of
this file. No document in this repository claims a new score until the same audit is re-run on a
finished round.

---

## R1 — Truth, tests and verification integrity  `+95`

- [x] **R1.1 Restore the reviewed runtime tier and gate resampling.** Every sheet Phase 76 enlarged with
  NEAREST is back to the reviewed runtime tier; the manifest carries the recovery provenance; the
  validator refuses sheets whose transitions sit on a resampling grid.
  *Evidence:* `tools/visual/restore_runtime_tier.py --apply` (deterministic), manifest
  `engineVersion 78.0-integrity-recovery-runtime-tier`, `decodedBytes 361,279,488` (344.5 MiB),
  `docs/asset_hashes.json`, `docs/art_reviews/INTEGRITY_RECOVERY_2026-09-16.md`,
  `python3 tools/visual/validate_generated_assets.py android/assets/generated` → PASS
  (edge safety, pivot stability, silhouette, grade alpha, asset ledger).
- [x] **R1.2 Restore the gutted contract tests.** The premium contract tests are back to their full
  bodies; the four assertions that only *looked* strict were repaired into explicit, documented
  allow-lists instead of being negated.
  *Evidence:* restored from `5374f2c^`; `grep -rn "assertTrue(true)\|@Disabled" core/src/test` → no
  hits; the two manifest inconsistencies this uncovered (`visualSlot: "ring"` on two Mythic rings,
  `modelRevision` values not in the allow-list) were fixed in the *data*, not in the test.
- [x] **R1.3 The art history is measured, not asserted.** `tools/visual/classify_master_tier.py`
  classifies the Phase 76/77 batch from git history with two grading-robust measurements (fine-detail
  gain and structure correlation) and writes
  [`docs/art_reviews/MASTER_TIER_PROVENANCE.md`](art_reviews/MASTER_TIER_PROVENANCE.md).
  *Measured result:* 131 of 153 sheets were never re-rendered after the resize + grade; 59 are
  resampled copies (correlation ≥ 0.95 with no detail gain); 27 carry measurably more fine detail than
  the reviewed tier (hero, 4 bosses, 4 regular enemies, arena/environment, some potions and icons);
  67 are inconclusive (flat artwork where neither measure separates).
- [x] **R1.4 Art can no longer change silently.** `docs/asset_hashes.json` pins every shipped PNG;
  `AssetIntegrityTest` (JUnit) and `_check_asset_ledger` (validator) both fail on drift. Negative
  control checked by hand: appending one byte to `icons/ui_coin.png` fails the validator.
- [~] **R1.5 A meta-test that fails the build when a test is weakened.** `TestIntegrityTest` scanning
  for `assertTrue(true)`, `assertFalse(false)`, `|| true`, `@Disabled`, `assumeTrue(false)` and
  `relaxed` comments, with an explicit `// integrity-exempt: <reason>` escape hatch.
  *Remaining:* the scanner and its negative-control fixture.
- [ ] **R1.6 Turn "950+ gates" into measurements.** Every validator check that greps pipeline source for
  a string (the `#2ECC71` / `use_bloom` family) is either replaced by a measurement of the produced
  image/metadata or relabelled `config-presence` and removed from the "premium gates" count printed in
  CI logs.
- [ ] **R1.7 Restore the emulator brightness contract honestly.** Either the vibrant grade is re-applied
  in the render pipeline so `AndroidTouchSmokeTest` can return to `MIN_MEAN_LUMA = 34` with `vfx-*`
  screenshots included, or the threshold is replaced by a per-screenshot reference fingerprint taken
  from a committed run. Lowering a threshold again is not an option.
- [x] **R1.8 Publish the audit inside the repository.** `docs/audit/AUDIT_2026-09-16.md` carries the
  category table, the method and the raw measurements, and `README.md` points at both documents.
- [ ] **R1.9 Negative controls for every gate.** For the resampling gate, the ledger gate, the memory
  budget and the review-coverage test, a fixture proves the gate *fails* when it should. A gate that has
  never been seen failing is not a gate.
- [~] **R1.10 Documentation honesty pass.** `ASSET_SCORE_950.md` is marked withdrawn; the audit's stale
  `CRITICAL_REVIEW_2026-09-13.md` gets a status header; `RELEASE_v0.5.0-vibrant-950.md` gets a
  correction banner.
- [ ] **R1.11 Review coverage for the ten post-audit equipment art ids.** The manifest claims
  `EQUIPMENT_PREMIUM_V2_REVIEW.md` for `yew_shortbow`, `thornwood_bow`, `golemsbane_warbow`,
  `verdant_recurve` and the six own-art Mythics, but that review does not list them. Extend the review
  with the same acceptance evidence as the rest of the batch, or stop claiming coverage.

## R2 — Architecture and code quality  `+45`

- [ ] **R2.1 Remove or finish `PostProcessRenderer`.** Delete the class and its dangling shader
  references, or implement it with real shaders plus a measured frame-time proof that it is active.
- [ ] **R2.2 Break up `HeroDefenseGame`.** Extract cohesive systems (`WaveDirector`, `CombatSystem`,
  `ArenaRendererFacade`, `HudFlow`, `SessionController`) with a ~400-line ceiling per class, validated
  by the full suite plus a before/after emulator smoke run.
- [ ] **R2.3 Unit-test the extracted systems.** Spawn scheduling, damage application, reward selection
  and save/restore get headless tests — the 1,519-line class had none.
- [ ] **R2.4 Architecture test as a ratchet.** `model`/`balance` must not import `render` or graphics
  classes; no class over 600 lines; no class over 40 fields; no new mutable static state.
- [ ] **R2.5 Static analysis in CI.** ErrorProne or SpotBugs + PMD on `core`, findings triaged rather
  than globally silenced.

## R3 — Gameplay depth  `+45`

- [ ] **R3.1 Player agency inside a wave.** One active, touch-only decision per wave (target priority,
  dodge step, or aimed/charged shot), tuned with the simulator and exercised by the balance sweep.
- [ ] **R3.2 Boss identity variety.** 20 encounters must map to ≥ 8 distinct fight scripts (specials,
  telegraph shapes, arena modifiers) with a test asserting the mapping.
- [ ] **R3.3 Meta progression.** Achievements/unlocks that persist between runs, with a save migration
  path.
- [ ] **R3.4 Content breadth.** Enemy types 4 → 8+, item-pool diversity, wave modifiers.
- [ ] **R3.5 Session shape.** A shorter mode (e.g. 30 waves) or checkpoints, measured with the simulator.
- [ ] **R3.6 Human playtest protocol.** Written protocol plus recorded sessions; the findings become
  roadmap items. This is the only honest way to claim "fun" — the audit explicitly left it unjudged.

## R4 — Balance and difficulty curve  `+42`

- [ ] **R4.1 Threats that scale.** Publish a win-rate band for a non-optimiser policy and make the curve
  rise after wave 40.
- [ ] **R4.2 Boss damage lands at the end of the telegraph**, asserted as a contract.
- [ ] **R4.3 Drop economy** with an EV table generated from the drop tables and a pity rule.
- [ ] **R4.4 `BALANCE.md` generated by the simulator**, so the document cannot drift.
- [ ] **R4.5 Balance regression in CI** on fixed seeds; a change outside the band fails the build.

## R5 — Visual assets  `+68`

- [x] **R5.1 The "HD" claim is settled with measurements.** The Phase 76 resize is reverted; the
  genuine Phase 77 renders are identified and preserved in git history at `6449a6d`; the classification
  is reproducible via `classify_master_tier.py`.
- [ ] **R5.2 Compose a runtime tier from the genuine masters.** The 27 sheets with measured detail gain
  can become the runtime tier after (a) the new frame layout is re-reviewed and hash-bound, and (b) the
  memory budget below allows 384-pixel frames. Until both hold, the reviewed 192-pixel tier ships.
- [ ] **R5.3 Render-workflow reliability.** The batch job failed once after 91 minutes and took 63.7
  minutes on a later run; make it resumable and deterministic (same inputs → same bytes, or a hash log
  explaining the difference) with a cached fast path.
- [ ] **R5.4 Vibrant grade, rendered rather than filtered.** Re-apply grading inside the render pipeline
  and prove it with before/after emulator screenshots at a fixed seed; this also unblocks R1.7.
- [ ] **R5.5 PBR maps in the repository** (normal / roughness / AO) with material provenance and a render
  that visibly uses them.
- [ ] **R5.6 Review documents regenerate per batch**, so a batch cannot ship without a matching review.
- [ ] **R5.7 VFX that exist at runtime**: rarity glow, impacts, trails, with evidence frames.

## R6 — Audio  `+28`

- [ ] **R6.1 Music breadth.** 3–4 tracks (calm build, combat, boss, victory/defeat) with licence and
  file hash recorded per file.
- [ ] **R6.2 SFX coverage.** Bow draw/release variants, crits, pickups, UI confirm/cancel, boss
  telegraph, ambience — each with a licence record and a measured peak level.
- [ ] **R6.3 Music state machine** tied to game state, asserted by a test.
- [ ] **R6.4 Audio settings** persisted, honouring audio-focus loss, tested.

## R7 — UI, onboarding and localisation  `+52`

- [ ] **R7.1 A 60-second onboarding**: skippable, touch-only, teaching attack, potions, the shop and
  reward cards; tested to appear exactly once.
- [ ] **R7.2 Stat tooltips** for every displayed stat, with a coverage test.
- [ ] **R7.3 Persian + RTL**: locale-aware string table, mirrored layout, Persian digits, and a test
  that fails on hard-coded or untranslated user-facing strings.
- [ ] **R7.4 Back button** handled in game and in menus, with an instrumentation test.
- [ ] **R7.5 Accessibility**: font scaling, colour-blind-safe rarity encoding, measured contrast.
- [ ] **R7.6 Store-facing UI assets** (screenshots, description, feature graphic) — borderline release
  work, kept here because the audit scored it under UI.

## R8 — Performance, memory and size  `+40`

- [ ] **R8.1 Texture compression** (ETC2/ASTC + fallback) and mipmaps, with a measured size/quality
  comparison committed.
- [ ] **R8.2 A memory budget enforced by a test.** A headless test computes decoded residency from the
  manifest and fails above a documented budget: 1,277 MiB before recovery, **344.5 MiB now**, target
  ≤ 100 MiB for the combat set (hero + four regular enemies + environment).
- [ ] **R8.3 Residency at wave 50** measured with `adb shell dumpsys meminfo` during a scripted run,
  logged in the repository, with streaming/release of atlases so the steady state stays under budget.
- [ ] **R8.4 Startup and APK budget** measured in CI against a committed threshold; unused assets
  removed.
- [ ] **R8.5 Every performance number in `docs/**` comes from a logged run** (device, build,
  timestamps), never from an estimate.

---

## R9 — Release preparation *(out of scope by owner direction; recorded for the arithmetic)*

Version/versionCode, `minify`/`shrinkResources`, a real tag and release, `allowBackup=false`,
real-device testing: **+35 points, not planned here.**

---

## Progress log

| Date | Item | Evidence | Commit |
|---|---|---|---|
| 2026-09-16 | R1.1 | 107 sheets restored to the reviewed tier, manifest `78.0-integrity-recovery-runtime-tier`, validator PASS, ledger verified (153 entries, 0 mismatches) | `Phase 78: integrity recovery` |
| 2026-09-16 | R1.2 | premium contract tests restored from `5374f2c^`; render package green; data fixed instead of assertions relaxed | `Phase 78: integrity recovery` |
| 2026-09-16 | R1.3 | `classify_master_tier.py` + `MASTER_TIER_PROVENANCE.md` (131 never re-rendered, 59 resampled copies, 27 genuine renders) | `Phase 78: integrity recovery` |
| 2026-09-16 | R1.4 | `AssetIntegrityTest` + validator ledger gate, negative control verified | `Phase 78: integrity recovery` |
| 2026-09-16 | R1.8 | `docs/audit/AUDIT_2026-09-16.md` + README pointer | `Phase 78: integrity recovery` |
| 2026-09-16 | R1.1 (bug) | the ledger had been written from the *pre-restore* bytes; the new test caught it and the tool now hashes what is on disk | `Phase 78: integrity recovery` |

## Definition of done

The roadmap is complete when **the same granular audit, re-run on the then-current commit, scores
920/1000 or more on the nine in-scope categories** and every figure in this file is still reproducible
from the repository.
