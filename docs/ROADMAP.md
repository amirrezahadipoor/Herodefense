# Roadmap

Every point the 2026-09-18 audit deducted from the game, as a work item. Nothing here is a wish list: each line
started as a number taken off a score, and the number is written next to it so the item can be judged against
what it was worth.

The score this file came from: **626 / 1000**, measured on commit `bae7238` by reading the tree, the assets, the
tests, the CI logs and `docs/playtests/findings.json`. The audit could not play the game — no human ever has
(item C1) — so every deduction below is traceable to something in the repository, not to an impression.

The table is the audit's baseline, frozen, not a live scoreboard: items done since (G1, H1, I3, G3a, G5a and the
E3 correction) change six of the ten areas and the running total, and re-measuring the whole game after a batch
is cheaper and more honest than editing ten numbers after every commit. The next full re-measure happens when
the current order of work reaches its first checkpoint, and it will be presented with the list of what moved it.

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
| E | visuals and presentation | 70/100 at audit, 72/100 after the E3 correction |
| F | audio | 62/100 |
| G | UI, UX and readability | 54/100 |
| H | localisation and Persian quality | 74/100 |
| I | engineering, tests and integrity | 80/100 |
| M | the measuring tools themselves | (unscored — they grade the rest) |
| P | release and store | 34/100 — **parked** |

Status marks are the ones `docs/RULES.md` documents: `[x]` done and evidenced, `[~]` partly done, `[ ]` not
started, `[!]` attempted and failed, with the failure written down.

## A — moment-to-moment gameplay (62/100, 38 points deducted)

- [x] **A1 (−18) The Hero cannot move.** Not one assignment to `hero.x` or `hero.y` existed anywhere in
      `core/src/main`. Decided deliberately, and built: the Hero steps. `OnboardingClaimsTest` failed on purpose
      the day the position started being written, which is what it was written to do, and it now guards the new
      answer instead of the old one.
      *The gesture.* A drag, not a tap, because a tap was already spoken for — it marks a target for the bow — and
      because a drag during a wave previously did nothing at all beyond moving a press marker. The walkable band
      (`WorldLayout.HERO_WALK_*`, x 72–648 / y 168–700 of the 720×1280 frame) is what excludes the HUD: it clears
      both button rows at every panel shift, and `HeroMovementSystemTest` walks a grid over the band asserting no
      HUD hit test fires inside it, so a finger on a button walks nobody. Releasing the drag stops the walk where
      the finger left it.
      *The bound.* Movement is priced per wave, not per second: 260 units at 165 units/s, a 1.58-second burst,
      refilled by `startCurrentWave`. A speed limit cannot be both dodge-capable and kite-proof — the slowest body
      in the game walks at 28 units/s, so a Hero slow enough never to be kited with is too slow to be worth a
      gesture, and one fast enough to matter outruns half the roster forever. A whole budget buys 9.29 s of
      distance on the slowest foe and 2.60 s on the fastest, computed in the test from the shipped `EnemyType`,
      `BossType`, `BossFightScript`, `WaveModifier` and `TrialEffects` tables rather than restated from a comment.
      `docs/BALANCE.md` carries the table and the argument.
      *The evidence that did not have to be re-run.* `BalanceSimulator` still anchors the Hero every tick and no
      simulated policy steps, so every published band still measures a rooted Hero — the floor of player skill —
      and stepping can only improve on it by less than ten seconds of distance a wave. `HeroMovementSystemTest`
      pins that too, by asserting the simulator still calls the anchor and never names the movement system.
      *What stepping buys, measured.* `EnemyMeleeAttackSystem` checks its range against the Hero's live position,
      so the test plays a swing that lands at the centre and then lands nowhere after a 100-unit step north. The
      meter under the Hero's feet is drawn only once some budget is spent, which is why an untouched wave renders
      the pixels it rendered before and the device journeys' reference brightness needed no re-capture — their one
      swipe is inside the inventory, not the arena.
      *The coaching.* The drag lesson came back as step 2 of 6 in both languages ("Drag on the ground and the Hero
      steps there" / «انگشت را روی زمین بکشید تا قهرمان گام بردارد»), reported only when
      `HeroMovementSystem.orderStepTo` actually took the order, so the lesson cannot complete on a drag that moved
      nothing. Six budgets still total exactly sixty seconds, which meant shortening the three lessons whose
      gesture a player either performs at once or never performs.
      *Cost.* `HeroDefenseGame` grew four lines and no fields (one import, three of comment where the per-frame
      anchor used to be) and the ratchet records why; everything else landed in files the ratchet does not hold.
      `GameState` stayed at 635 lines because `Hero.keepAt` — the anchor — now voids a pending step order itself,
      which is also the correct semantics: a ceremony or a save repair puts the Hero somewhere the player did not
      ask for, and a stale order should not resume afterwards.
- [ ] **A5 (no deduction; found while building A1) A boss's special lands wherever the Hero is standing when the
      telegraph ends.** `BossSpecialAttackSystem.executeOnce` applies all four identities' hits through the damage
      pipeline with no position test, and `VOID_KNIGHT`'s charge re-places the boss at melee range of the Hero at
      telegraph time. That was invisible while the Hero could not move; now that it can, a player who learns to
      step will try to step out of a telegraph and be hit anyway, and the telegraph contract in `docs/BALANCE.md`
      promises a readable warning rather than an avoidable one. Decide it: either give specials a landed-position
      test (which re-opens every boss-fight measurement in this file's Phase 91 sections and the 40-encounter
      table), or state plainly in the coaching and the codex that a special is not dodged and is only braced for.
      Nothing may claim the step is a dodge until that decision is made.
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
- [x] **C3 (−6) `finding-brief-vigil-has-no-teeth` was closed as `accepted` — and the acceptance was right about
      survival and silent about the economy.** Its evidence shows the thirty-wave run cannot be lost even by a
      policy with no skill, and accepts that as the feature: the first session must not punish a player for not
      knowing a talent tree exists. That is a floor, and the floor stays. What the acceptance never priced is
      that at the old formula a flawless brief clear paid 26 heartwood for 30 waves while a flawless long clear
      paid 110 for 200 — 0.87 a wave against 0.55 — so the mode that cannot kill you was the efficient earner,
      and the player it served best was a veteran who had stopped being challenged. Neither "give it teeth" nor
      "cut it" was the right instruction; the floor and the farm were the same code path, so the fix splits
      them: the brief vigil's formula is halved (0.43 a wave flawless at tier zero, under the long run's 0.55),
      the menu row says so in both languages ("Thirty waves | same vigil, half the heartwood"), the death
      screen's preview and the award both pass their mode to the formula so the two numbers cannot disagree in
      front of the player, and `docs/PROGRESSION_HOURS.md` carries the halving inside its income bullet because
      an equation that omits a mode is an equation that lies by completeness. `BriefVigilEconomyTest` holds all
      four edges: the long run pays exactly what it always did, the brief run pays half of the same formula
      including the tier bonus, the dangerous run is the better earner per wave, and both call sites still name
      their mode. The finding itself stays `accepted` — its rationale was about a new player's first hour and
      that rationale still holds.

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
- [ ] **E3 (−6, corrected to −4 below) No human has looked at the running game.** The audit's original wording
      said no recorded review of it existed either, and that half was wrong: `AndroidTouchSmokeTest` captures
      thirty frames of the running game per emulator run, gates each against a measured mean-luma and
      lit-fraction reference, and uploads the frames as artifacts. What does not exist is a person looking at
      them -- the audit found G5a's three text collisions only because a brightness failure made it open the
      artifact, and nothing before that had. The deduction stays, smaller: a machine that notices a screen went
      dark is not a reviewer who notices a subtitle ran into a hint, but it is not nothing either.
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
- [~] **G3 (−14) There were no accessibility options at all.** Split into the four things that word covered,
      because they are four different sizes of work and one of them is not this project's to finish alone:
  - [x] **G3a Reduced motion.** `GameSettings.reducedMotion`, persisted as `display.reducedMotion`, toggled by
        a sixth settings row, and honoured where the motion is drawn: the camera stops reading the shake
        offsets and the ambient spore drift is not drawn. Both go through one gate, `polish/ReducedMotion`, so a
        third source of motion cannot forget the preference exists. Hit particles, floating damage numbers and
        the hit-stop pause are deliberately left alone: those carry information, and an accessibility setting is
        not a licence to make the game harder to read. `ReducedMotionSettingsTest` holds all four links — the
        tap, the relaunch, the gate's meaning, and the composer still consulting it — and asserts the shake it
        suppresses is a shake that actually moves the camera, so the row cannot become a placebo unnoticed.
        The sixth row did not fit the screen as it was: rows went from 150f on a 150f pitch to 130f on a 140f
        pitch, which is what the surrounding constraints allow (96f minimum target, clear the footer panel at
        260f, clear the close button at 1120f, clear the header band at 1096f).
  - [ ] **G3-layout The settings screen is full.** Six rows is the ceiling of a non-scrolling screen; a seventh
        needs a scrolling list, and G3b and G3c each want a row. This is the blocker for both, not a nicety.
  - [ ] **G3b Text size.** `GameFonts` already rebuilds its atlases from `DisplayMetrics`, so three named steps
        are plausible the way the volume levels are; what has to be checked first is every surface's text
        bounds, because a larger face in a row that was laid out for a smaller one clips, and clipping is worse
        than small.
  - [ ] **G3c Colour-blind-safe rarity colours.** Rarity is currently carried by `rarityColor(String)` inside
        `InventoryOverlayRenderer` and by tier art, so this starts with centralising the palette before it can
        offer an alternative to it. `VisualRarityTest` and `DropRarityVisualTest` are the tests that will say
        whether the alternative still distinguishes five tiers.
  - [ ] **G3d Screen-reader support — recorded, not started.** This is one libGDX surface, so TalkBack has
        nothing to read: real support means an accessibility delegate publishing virtual views for the HUD, the
        menus and the card choices, and keeping them in step with a renderer that redraws every frame. That is
        an Android-platform project of its own and it is not honest to tick it off as a settings row. Written
        here so the −14 is accounted for rather than quietly redefined to mean the three things that were
        cheaper.
- [ ] **G4 (−4) The HUD does not mirror in RTL,** a documented and deliberate asymmetry, still an asymmetry a
      Persian player meets every wave.
- [ ] **G5 (−2) Overlay text density** (inventory, codex, tooltips) leaves little room for a thumb.
- [x] **G5a The settings screen's own text collided with itself, and only a screenshot could show it.** The CI
      capture of the settings screen showed three subtitles ending under their rows' right-aligned tap hints
      ("boss entrances" under "tap to step", "direction" under "tap to switch", the new row's "spores" under
      "tap to enable") and the footer hint running out of the panel drawn around it. Two of the three
      collisions predate G3a and had shipped in every build; nothing but a drawing could have caught them.
      Fixed by shortening the three subtitles in both languages and stepping the footer hint down one text
      size, and held by `SettingsTextFitTest`, which caps the six row subtitles and the footer lines by length
      and says in its own javadoc that length is a proxy for width and the real gate is the CI screenshot.
      This item exists because E3 was wrong in one direction too: the running game *is* captured in CI, thirty
      frames of it, and the audit had not looked at those frames until a brightness failure made it.

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

- [x] **I1 (−8) No coverage measurement.** 198 test files and 828 `@Test` methods, and no number for what they
      touch. Now there is one: `:core:jacocoTestReport` runs as its own CI step after the unit loop, publishes a
      line-coverage sentence to the step summary through `tools/audit/coverage_summary.py` (a script rather than
      a shell heredoc, because a heredoc inside a YAML block scalar cannot end at column zero and an indented
      terminator is a broken heredoc), and uploads the HTML/XML report as an artifact. Not gated, and the
      build.gradle comment says why in the same words the roadmap uses: a gate set before anyone has seen the
      number is a gate set to a guess, and a blanket percentage over a suite full of source-scanning tests would
      measure how much of a test helper ran. The first gate gets its own commit and its own measured baseline,
      with the classes it excludes named. Measured baseline, from the first CI run of this change:
      **core line coverage 61.0% (6,852 of 11,229 lines)** -- that is the number a first gate would be set
      against, and it is recorded here rather than in a chat message so the next person sees it without a CI run.
      Three tests hold the sentence to the report: the LINE counter is the
      one reported, a report with no LINE counter exits non-zero instead of reading as zero, and the summary
      file carries the same sentence and the same "reported, not gated" heading the log does.
- [ ] **I2 (−6) Device evidence is a headless x86 emulator.** Real Adreno/Mali GPUs, real touch latency and
      thermal behaviour are untested. `docs/perf/` runs are emulator runs and should say so wherever they are
      quoted.
- [x] **I3 (−4) Three or-true suffixes in `.github/workflows/generate-visual-assets.yml` swallowed failures.**
      One hid a broken render-resume, which then looked identical to "this batch owes nothing" and quietly
      re-rendered the whole batch; two hid the render hash log and the batch's review evidence failing to be
      written, after which the workflow stayed green, the artifacts uploaded empty with `if-no-files-found:
      warn`, and nothing anywhere said which of the two had happened. All three now fail their step with a
      `::error::` line naming what is missing, and the resume fallback announces itself in the step summary
      instead of being inferred from an empty variable. The integrity metric needed the same fix from the other
      side: `workflow_always_true` counted the pattern anywhere in the file, so the three comments explaining
      what had been removed would have kept reporting three swallowed failures in a workflow with none. It now
      ignores comment lines, is a function of its own so it can be tested, and reads 0. Rule 2 is about tests,
      but a step that cannot fail is the same shape of problem, and so is a metric that cannot tell a failure
      from a sentence about one. This workflow only runs on `workflow_dispatch`, so nothing in CI exercised the
      change: the YAML parses and all ten `run` blocks pass `bash -n`, which is checked and stated rather than
      left implied.
- [ ] **I4 (−2) The core suite was not executed locally today.** CI is green on both jobs, and the last full
      local execution predates the current tree by two days. Small, and it stays small only while the sandbox
      keeps losing its JDK; `scripts/gradle.sh` plus a pinned toolchain in CI already covers it.

## M — the measuring tools (unscored)

- [x] **M1 `measure_round.py` reported features it had not seen.** Its five content flags were keyword regexes
      over whole files, javadoc included, so `accessibility: true` came from one word in a `GameSettings`
      javadoc line and no accessibility feature at all. A tool that over-reports is worse than no tool, because
      it is trusted. Fixed by searching code with comments blanked out (line numbers preserved, so an evidence
      line still points where it says), and by reporting `flagEvidence` — the `file:line` list each flag rests
      on — so a reader can check a claim instead of trusting it. `accessibility` is now `false` with an empty
      evidence list, which is a truer sentence than the old one; `backButton`, `persian`, `rightToLeft` and
      `haptics` stay `true` and now name the lines that make them true. Six tests in
      `tools/audit/tests/test_measure_round.py` hold it: a feature named only in a comment is not reported, the
      same feature in code is reported with its line, comment stripping keeps the numbering, `accessibility` is
      false until G3 makes it true, every flag equals its own evidence and that evidence still exists in the
      tree at a line the file actually has, and the four real features keep being detected.

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

## Corrections to the audit itself

Three so far. Each is a wrong fact in the audit rather than a wrong fact in the game, and each is recorded here
rather than silently edited, because a reader of this file has to be able to tell the audit from its
corrections.

- **H4: two languages, not three.** `GameLanguage` is `ENGLISH` and `PERSIAN`.
- **E3: the running game is recorded in CI.** Thirty captures per emulator run with per-screen brightness
  references, see above. The deduction narrows from 6 to 4; the missing half is a human, and that is C1.
- **A citation in commit `39ce834` names the wrong CI run.** Its message says the settings brightness reference
  came from run 35297444725; the run that measured `mean=44.40 lit=0.9553` is **35296081592**, which is what the
  code comment in `AndroidTouchSmokeTest` cites. The message was written before the run id was fetched and not
  checked afterwards. Left as pushed and corrected here rather than rewriting history over a number, because
  the wrong number is in a public commit and the correction belongs next to it.

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
