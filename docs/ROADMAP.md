# Roadmap

Every point the 2026-09-18 audit deducted from the game, as a work item. Nothing here is a wish list: each line
started as a number taken off a score, and the number is written next to it so the item can be judged against
what it was worth.

The score this file came from: **626 / 1000**, measured on commit `bae7238` by reading the tree, the assets, the
tests, the CI logs and `docs/playtests/findings.json`. The audit could not play the game — no human ever has
(item C1) — so every deduction below is traceable to something in the repository, not to an impression.

This file is governed by `docs/RULES.md`, including the two rules that shape it most: one item per commit,
pushed immediately (rule 4), and a failure recorded as `[!]` rather than quietly dropped (rule 5). Numbers about
performance and size are **not** copied here; they live in `docs/perf/` where a logged run stands behind them
(rule 3).

## Ids

`A1`, `G3`, `P2` and so on. These are **not** the `R`-ids (`R2.4`, `R8.2`) that source comments still cite: that
scheme belonged to the roadmap deleted on 2026-09-18, it is frozen, and `docs/RULES.md` says nothing may be
added to it. Letters here match the audit's ten scored areas:

| Letter | Area | Score |
|---|---|---|
| A | moment-to-moment gameplay | 62/100 |
| B | depth and progression | 66/100 |
| C | balance and fairness | 66/100 |
| D | content volume and variety | 58/100 |
| E | visuals and presentation | 70/100 |
| F | audio | 62/100 |
| G | UI, UX and readability | 54/100 |
| H | localisation and Persian quality | 74/100 |
| I | engineering, tests and integrity | 80/100 |
| M | the measuring tools themselves | (unscored — they grade the rest) |
| P | release and store | 34/100 — **parked** |

Status marks are the ones `docs/RULES.md` documents: `[x]` done and evidenced, `[~]` partly done, `[ ]` not
started, `[!]` attempted and failed, with the failure written down.

## A — moment-to-moment gameplay (62/100, 38 points deducted)

- [ ] **A1 (−18) The Hero cannot move.** Not one assignment to `hero.x` or `hero.y` exists anywhere in
      `core/src/main`. There is an `EnemyMovementSystem` and no hero equivalent, so the whole spatial layer of
      the game — positioning, kiting, stepping out of a telegraph — is absent. Decide it deliberately: either
      build tap-to-move (path, collision, camera, and a re-balance of every melee range that currently assumes a
      fixed target), or accept a stationary defender and remove the last traces of movement from the coaching,
      the lore and this file. `OnboardingClaimsTest` now pins the current answer and fails the day it changes.
- [ ] **A2 (−8) Nothing is actively cast.** All five skills are passive upgrade cards; the only player verbs are
      marking a target and the ultimate. One aimed or timed active ability would add a decision per wave without
      adding content.
- [ ] **A3 (−8) Eight enemy types carry 200 waves.** The second half scales numbers rather than behaviour, which
      is what the open playtest finding about the plateau was describing before it was marked fixed by curve
      changes alone.
- [ ] **A4 (−4) Five wave modifiers, one of which is `NONE`.** Four real modifiers across 200 waves.

## B — depth and progression (66/100, 34 points deducted)

- [ ] **B1 (−12) Open ledger finding: the economy cannot move until the trial ceiling has headroom.**
      `finding-economy-cannot-move-until-the-trial-ceiling-has-headroom` is still `open` in
      `docs/playtests/findings.json`. An open finding the owner has read is a debt with a name; this is that one.
- [ ] **B2 (−10) Build space is narrow:** 5 skills, 10 evolutions, 15 affixes. Two runs at the same tier can and
      do converge on the same build.
- [ ] **B3 (−8) No choice of hero, class or playstyle.** One defender, one weapon, one 50-hour line.
- [ ] **B4 (−4) Ascension was inverted for non-optimiser players.** Fixed (`NonOptimiserBandTest` now guards the
      band), but it was found by a simulated policy, not by a player — re-validate against C1 when a human
      session exists.

## C — balance and fairness (66/100, 34 points deducted)

- [ ] **C1 (−20) No human has ever played this game, and the repository says so.** All three sessions in
      `docs/playtests/sessions.json` have `player: "balance simulator"`; the finding
      `finding-first-run-has-no-recorded-human-session` is `open`. Everything in `docs/PLAYTEST_PROTOCOL.md`
      exists for this item and has never been used. This needs the owner's hands on a device — it is the one
      deduction no amount of code can close.
- [ ] **C2 (−8) Fun is unmeasured.** Twelve balance test classes validate curves against simulated policies.
      Nothing measures whether a wave feels fair; C1 is the instrument.
- [ ] **C3 (−6) `finding-brief-vigil-has-no-teeth` was closed as `accepted`.** A known-toothless mode shipped
      rather than being fixed or cut. Either give brief mode a reason to be played or remove it.

## D — content volume and variety (58/100, 42 points deducted)

- [ ] **D1 (−14) One arena.** The manifest holds a single `arena_backdrop` and `ground_tile_0..2`. 200 waves,
      2.2 hours, one room. A second backdrop plus a ground set is mostly an art-pipeline task, and the pipeline
      (Blender tools, render workflow, hash-bound reviews) already exists.
- [ ] **D2 (−12) Eight enemy identities and three elite affixes.**
- [ ] **D3 (−8) Four boss identities across forty encounters.** The encounter table itself is good work — eight
      fight scripts on a shifting permutation, deterministic against the save file — but only four of them have
      a body.
- [ ] **D4 (−8) One fragment shader and one vertex shader in the whole project,** which caps how different the
      arena, the bosses and the weather can look from each other.

## E — visuals and presentation (70/100, 30 points deducted)

- [ ] **E1 (−12) No post-processing.** No bloom, no AA, no depth, no vignette: the frame is the sprites and the
      clear colour.
- [ ] **E2 (−8) Sprite animation at the frame rate and frame counts named in
      `code:main/java/com/amirrezahadipoor/herodefense/gameplay/HeroAnimationController.java`** (idle, attack and
      hit clips). Correct and consistent, but it is the ceiling of the presentation, not a step towards
      something else.
- [ ] **E3 (−6) No recorded review of the running game on a device screen.** CI validates assets (edge safety,
      pivot, silhouette, grade) and runs a headless emulator smoke test; nobody has looked at the game playing.
      `docs/art_reviews/` is offline render evidence and stays as it is.
- [ ] **E4 (−4) The interface is text-heavy** for a game played on a phone at arm's length.

## F — audio (62/100, 38 points deducted)

- [ ] **F1 (−18) Four static music tracks for a 2.2-hour run.** No adaptive or layered music, so hour two sounds
      exactly like minute two.
- [ ] **F2 (−10) Twenty SFX** cover twelve enemy/boss identities plus every skill, which means audible reuse.
- [ ] **F3 (−6) No voice or narration,** including for the 31 lore entries and the boss title cards that are
      written as if they were being read aloud.
- [ ] **F4 (−4) Haptics are the `VIBRATE` permission** and a couple of trigger points.

## G — UI, UX and readability (54/100, 46 points deducted)

- [x] **G1 (−22, −4, −6) The first-run coach taught three mechanics that do not exist.** Step 1 said "Tap empty
      ground and the Hero walks there" while nothing in the tree can move the Hero; step 2 said "Hold and drag to
      aim — the bow fires while you hold" while a drag during play only moves a press marker; step 3 said drops
      come to you "when you walk near them" while `DropPickupSystem` auto-collects them after a short delay.
      Each step also *completed* on the gesture it described, so the player was told a lie and then marked as
      having learned it. Fixed by re-wording all three lines in both languages to the shipped behaviour,
      deleting the `DRAG_FIRE` action nothing can honestly claim, and adding `OnboardingClaimsTest`, which fails
      if a coached line ever claims movement or aiming again and fails if anyone makes the Hero move without
      coming back to the coach. Worth 32 points across G and H; see H1 for the Persian half.
- [ ] **G3 (−14) There are no accessibility options.** No colour-blind palette, no screen-reader labels, no font
      size, no reduced-motion switch. `GameSettings` holds audio, auto-sell and a tutorial flag; that is all.
- [ ] **G4 (−4) The HUD does not mirror in RTL,** a documented and deliberate asymmetry, still an asymmetry a
      Persian player meets every wave.
- [ ] **G5 (−2) Overlay text density** (inventory, codex, tooltips) leaves little room for a thumb.

## H — localisation and Persian quality (74/100, 26 points deducted)

- [x] **H1 (−10) The false coaching claims shipped in Persian too** — the translation was faithful, which is
      exactly the problem: a correct translation of a wrong sentence. Fixed inside G1, both languages at once,
      and `OnboardingClaimsTest` checks the Persian side's vocabulary as well as the English side's.
- [ ] **H2 (−10) No native-proofread record.** `TranslationTableTest` proves both sides exist, are non-blank,
      agree on placeholders and are actually Persian script. Nothing proves a Persian reader judged the wording.
- [ ] **H3 (−6) HUD and numbers stay untranslated by design.**
- [ ] **H4 (correction, no deduction) The audit said three languages; there are two.** `GameLanguage` is
      `ENGLISH` and `PERSIAN`. Recorded here because a wrong fact in an audit is the same kind of debt as a
      wrong claim in a game, and rule 1 does not switch off for documents I wrote an hour ago.

## I — engineering, tests and integrity (80/100, 20 points deducted)

- [ ] **I1 (−8) No coverage measurement.** 198 test files and 828 `@Test` methods, and no number for what they
      touch. Jacoco or an equivalent, reported in CI, not gated at first.
- [ ] **I2 (−6) Device evidence is a headless x86 emulator.** Real Adreno/Mali GPUs, real touch latency and
      thermal behaviour are untested. `docs/perf/` runs are emulator runs and should say so wherever they are
      quoted.
- [ ] **I3 (−4) Three `|| true` in `.github/workflows/generate-visual-assets.yml`** can swallow a failure in the
      render-resume path. Rule 2 is about tests, but a step that cannot fail is the same shape of problem.
- [ ] **I4 (−2) The core suite was not executed locally today.** CI is green on both jobs, and the last full
      local execution predates the current tree by two days. Small, and it stays small only while the sandbox
      keeps losing its JDK; `scripts/gradle.sh` plus a pinned toolchain in CI already covers it.

## M — the measuring tools (unscored)

- [ ] **M1 `measure_round.py` reports features it has not seen.** Its content flags are keyword regexes, so
      `accessibility: true` came from one javadoc word in `GameSettings` and `haptics: true` from a manifest
      permission. A tool that over-reports is worse than no tool, because it is trusted. Either make each flag
      detect structure (a settings field, a code path) or rename the flags to say they count mentions.

## P — release and store (34/100, 66 points deducted) — PARKED

Parked on 2026-09-18 by the owner's instruction ("فعلا انتشار رو کاری نداشته باش"). Listed so the 66 points are
not mistaken for forgotten ones. Nothing here may be started without the owner lifting the park.

- [ ] **P1 (−18) Store visuals:** one 512px icon in `store-assets/cafe-bazaar/`, no screenshots, no feature
      graphic, no video.
- [ ] **P2 (−14) The first-run experience is unvalidated by a human** — the release-side consequence of C1.
- [ ] **P3 (−12) No crash reporting and no ANR watchdog.** Nothing in the tree references Firebase, Crashlytics,
      Sentry or an uncaught-exception handler. Note the tension with rule 7 (nothing designed around revenue):
      crash reporting is not revenue, but it is telemetry, so the choice belongs to the owner and should be
      written down either way.
- [ ] **P4 (−10) No store listing copy.**
- [ ] **P5 (−8) No privacy policy and no content rating.**
- [ ] **P6 (−4) `versionName "0.1.0"`** with no public changelog.

Already in place, and not deducted: the signed Cafe Bazaar APK workflow with its four secrets and `always()`
cleanup, the APK measured against a committed budget in CI, cold start measured from logcat, wave-50 residency
checked, packaged natives verified per ABI.

## Order of work

Cheapest point first, and nothing that needs the owner's hands before things that do not:

1. **G1 + H1** — done (`OnboardingClaimsTest`).
2. **M1** — the tool that grades everything else is over-reporting.
3. **I3** — three characters per line, removes a way for a failure to hide.
4. **G3** — accessibility settings: real, testable, no art dependency.
5. **C3** — brief mode: fix or cut, decided by simulation that already exists.
6. **I1** — coverage reporting, no gate.
7. **B1** — the open economy finding.
8. **D1** — second arena, through the existing render pipeline.
9. **A2 / A3 / A4** — verbs and variety in the second half.
10. **E1 / F1** — post-processing and adaptive music.
11. **A1** — the movement decision. Biggest single deduction, biggest design consequence, so it goes after the
    cheap honesty work and before the content that would have to be rebuilt around it.
12. **C1** — whenever the owner has a device and an hour. It gates C2, B4 and P2.
