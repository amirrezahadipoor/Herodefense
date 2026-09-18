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
      the pixels it rendered before. Exactly one captured screen did change, and it changed because of the coach
      rather than the movement: six steps draw six pips where five drew five, and the first Android run of this
      change measured `live-hud-premium-v2.png` at mean 47.09 / lit 0.9306 against its stored reference
      46.81 / 0.9340 (run 35301961953's own brightness-measurements.txt), inside the tolerance — so no reference
      moved, and the frame in that run shows the six-pip banner over a wave the Hero has not stepped in yet.
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
- [x] **A5 (no deduction; found while building A1) A boss's special lands wherever the Hero is standing when the
      telegraph ends.** `BossSpecialAttackSystem.executeOnce` applies all four identities' hits through the damage
      pipeline with no position test, and `VOID_KNIGHT`'s charge re-places the boss at melee range of the Hero at
      telegraph time. That was invisible while the Hero could not move; now that it can, a player who learns to
      step will try to step out of a telegraph and be hit anyway, and the telegraph contract in `docs/BALANCE.md`
      promises a readable warning rather than an avoidable one. Decide it: either give specials a landed-position
      test (which re-opens every boss-fight measurement in this file's Phase 91 sections and the 40-encounter
      table), or state plainly in the coaching and the codex that a special is not dodged and is only braced for.
      Nothing may claim the step is a dodge until that decision is made.
      **Decided: the words, not the mechanic.** A landed-position test would re-open every boss measurement for
      a verb the brace already answers, and a dodge that only works against bosses would make the shield the
      wrong button exactly when the game is loudest. So the contract is now stated in both places the item named:
      the vigil grew a fourth step -- BRACE, "a boss's great blow finds you anywhere: tap the Hero to brace and
      shield it", advanced only when `BraceSystem.tryBrace` actually raises the shield, and paid for out of the
      passive steps' budgets (ULTIMATE 12→10, LOOT 8→6, CARD 12→10, SHOP 8→6) so the lesson still fits its
      pinned sixty seconds at seven steps -- and codex entry nine tells the first boss's killer that the arm
      "falls where you stood when the wind rose. Set your shield there, not your boots." (entry and
      `docs/STORY_CONTENT.md` stay verbatim twins, inside the 300-character body budget). BALANCE.md's two
      A5-flagged paragraphs now carry the decision.
- [x] **A2 (−8) Nothing is actively cast.** All five skills are passive upgrade cards; the only player verbs were
      marking a target and the ultimate. The verb that landed is the **brace**: tap the Hero's own body and the
      shield goes up for three seconds -- everything that hurts, melee swings, boss specials and the rot nobody
      dodges, hurts 0.4 as much through it -- and in exchange the bow fires nothing new and the feet stay planted,
      with twelve seconds raise to raise. `docs/BALANCE.md` carries the table and the trade's arithmetic.
      *Why this gesture.* It is the one tap in the arena that meant nothing: a tap marks the enemy under the
      finger and on empty ground releases the mark, but on the Hero there was no enemy and nothing to release, so
      the verb costs no existing input, no new HUD button, no new art and no new sound. The sfx roster is exactly
      what F2 measures, and borrowing a cue for the shield would deepen the reuse that finding counts; the
      feedback is the sprite's cold tint, the shield's own bar under the feet and the haptic the router already
      owns. Both bars are absent at rest, so no captured screen changes until a player chooses the verb.
      *Why a trade and not a button.* A shield that is always right to press is a second health bar; this one
      spends a quarter of the bow's uptime to buy its windows, which is a decision per wave -- the thing the
      audit asked for -- rather than a buff. `BraceSystemTest` measures each clause against the real pipeline:
      ten damage unbraced is four braced through `HeroDamageSystem`, the bow's update returns zero shots while
      arrows in flight still land, a live step order is voided and new ones refused, and a loaded save cannot
      carry a shield longer than the game's own.
      *What it is not.* It is not a dodge. Boss specials still land regardless of position (A5); the brace answers
      that from the other side, and the coaching, the codex and the Hero's javadoc all say so rather than letting
      a player discover otherwise. The published bands did not move: `BalanceSimulator` never names
      `BraceSystem`, a test pins that, and another pins that the shield's clocks age before the bow and the
      damage resolve inside `CombatSystem.update`.
- [x] **A3 (−8) Eight enemy types carry 200 waves.** The second half scaled numbers rather than behaviour, which
      is what the open playtest finding about the plateau was describing before it was marked fixed by curve
      changes alone. First half of the answer, landed: two **roles**. From wave 121 a living HUSK_WARDEN projects
      a ward -- every living enemy within 90 units, itself included, takes ×0.96 damage -- so the shielded
      mid-weight becomes a body the wave is organised around, and the counter is a verb the game already teaches
      (mark the warden; the ward dies with it in the same tick). From wave 141 a FUNGAL_BRUTE below 30% health
      latches into a berserk -- closing ×1.25, swing interval ×0.80 -- so the heaviest regular body stops being
      a sponge and becomes a clock, with the brace from A2 as one of the answers. Both gates sit far past the brief
      vigil's thirtieth wave, so the first session keeps exactly the roster its evidence was measured on.
      The ward enters damage through one function every enemy-damaging call site passes through, and a source
      scan fails the build if a fourth call site skips it; the berserk latches once at the transition, because
      nothing heals a regular enemy. The simulator wires its systems by hand, so the roles tick in its loop
      explicitly (a ratchet raise of 3 lines, documented) and the balance gate re-measured every band against
      them -- A3 is the one deduction whose resolution cannot be asserted, only measured. It was measured: the
      first landing (ward ×0.75 at 150 units from wave 101, berserk ×1.4/×0.6 below 40% from wave 121) broke
      four gates -- naked-run average 0.1564 against the 0.15 ceiling, trial-pair median spikes up to 0.4863
      against 0.40 -- and the curve answered the way this entry promised: lighter role numbers, gates back to
      121/141, re-measured green, with the before/after table in `docs/BALANCE.md`.
      Second half, landed and measured on the same seeds: from wave 161 a non-elite BRAMBLE_THRALL at 50%
      health splits into two rootling fragments carrying a hair under its remaining budget (2×24% of max
      against the 50% ratio), its speed, reach and swing interval, and half its damage each, swings staggered
      half an interval apart -- the same contact damage per second in two bodies that land their hits on
      different frames, kill reward handed to the fragments, drop lineage rolled once on the corpse, so the
      split changes the shape of the fight, mints no economy and costs marginally less to finish. From wave 181 a SAP_HOUND fences: a still 0.45 s
      windup, a 0.55 s dash at ×3.2, a 0.9 s self-stun punishment window, then 3 s of walking -- over a cycle it
      covers less ground than it would walking, so the lunge adds tells, not pressure. Both are
      pressure-neutral by construction because the trial-spike ceiling had no room left after the ward and the
      berserk were measured in -- and the gate proved the point twice more, catching a median reshuffle onto a
      0.4037 spike at wave 173 until the fragments were staggered and the ward lightened to ×0.96 across 90
      units. The gate has spoken three times: it rejected the first landing, rejected the un-staggered split,
      and accepted the shipped four. A3 closes: the second half now scales behaviour, wave block by wave block.
- [x] **A4 (−4) Five wave modifiers, one of which is `NONE`.** Four real modifiers across 200 waves -- and now
      six. The pool was one stat twist per multiplier, each of them making the wave dearer in exactly one way
      for exactly 1.25× the coin; the two new omens open the axes that were missing. **GILDED / زراندود**
      (health ×1.18, coin ×2.5) is the economy omen: the wave that pays for itself twice over and asks a
      tougher hide for it, a decision about the build rather than a tax. **WARBAND / برگزیدگان** is SWARM
      inverted (count ×0.70, health ×1.50, damage ×1.30, coin ×1.50): a third fewer bodies, each half again as
      heavy and hitting a third harder -- total wave health within 5% of ordinary, total contact damage 9%
      lower, total pay about even, and what it actually asks is mark discipline and burst timing instead of
      area throughput. The first draft of the second omen (DEADMARCH, speed ×0.85) was rejected by the kiting
      invariant in `HeroMovementSystemTest` -- nothing may slow the field below its base speeds, because the
      head-start bound is arithmetic about the slowest base speed in the game -- and the rejection is the
      ratchets working: the shipped omens honour it. Omens remain trial-gated (HOLLOW_OMENS), so the default
      run is untouched and every balance gate stayed green on its fixed seeds with no generated block moving
      (`BalanceDocumentTest` passes without regeneration). Evidence: `WaveOmenTest` grows to nine tests -- all
      six omens reachable on the one fixed seed, GILDED paying ×2.5 through the real reward system, WARBAND
      inverting the body count and holding its total-health, total-damage and total-pay bounds.

## B — depth and progression (66/100, 34 points deducted)

- [x] **B1 (−12) Open ledger finding: the economy cannot move until the trial ceiling has headroom.**
      `finding-economy-cannot-move-until-the-trial-ceiling-has-headroom` was `open` in
      `docs/playtests/findings.json` -- an open finding the owner has read is a debt with a name, and this was
      that one. The finding's own evidence held the key: three versions of R4.3's pity rule were measured and
      each broke a ceiling, and the blocker was not the gift's size but a trial ceiling with no room to absorb
      any economy change. A3 bought the room (worst pair 0.3815 against 0.40) and B1 spent it: the guaranteed
      common after thirty dry kills ships (`0d992b3`), armed from wave 31 only, because the first measured
      version's one untunable failure was the brief vigil's floor. The streak is counted inside the roll that
      already happens -- a miss still spends exactly one draw -- lives in `GameState` like every run counter,
      and survives saves. Re-measured on the fixed seeds with every band green: riskiest pair unchanged,
      quarter means 0.0593/0.0970/0.1170/0.1417, and the sweep average's floor raised 0.0816 -> 0.0949 -- the
      unlucky runs the rule exists for are measurably no longer the weakest runs. `ItemDropPityTest` pins the
      four promises; the ledger finding is `fixed` citing the implementation commit, and BALANCE.md's bad-luck
      section now carries the shipped rule beside the three measured refusals.
- [x] **B2 (−10) Build space is narrow:** 5 skills, 10 evolutions, 15 affixes. Two runs at the same tier can and
      do converge on the same build.
      **B2a landed (affix axis):** the pool is twenty — Elite Damage (+10% against elites, the arrow formula's
      elite branch mirroring the boss lane), Thorns (landed melee swings reflect 20% of themselves through the
      same ward-wrapped path arrows use; the ward scan now pins four damage call sites), Potion Find (+15% drop
      rate, one draw, bit-identical without the affix), Focus Gain (+12% fill rate, the single multiplier every
      charge passes through) and Fortitude (-6% damage taken on both incoming-hit paths, stacked pieces floored
      at half). The five expansion lanes roll on **Legendaries from wave 101 onward only**: the naive variant
      (every Rare diluting across twenty lanes) was measured and refused — it remapped every Legendary loadout,
      flipped the balanced run's middle-third shape gate and pushed the FAMISHED_EARTH pairs to `0.4536` against
      the `0.40` ceiling — while the gated variant leaves every measured band bit-identical to B1's close and
      puts the wider table exactly where runs used to converge. `AffixExpansionWiringTest` fires all five lanes
      end to end; 881 unit tests green, balance suite green. **B2b (skill axis):** a new `SkillId` needs a
      reviewed premium-v2 medallion in the asset manifest (`SkillIconContractTest`), which needs the Blender art
      pipeline and is blocked in-sandbox; evolutions carry no icon contract, so a third fork per skill is the
      remaining in-reach lever.
      **B2b landed (evolution axis):** every skill now forks three ways -- Overcharge (chain arcs +25% damage),
      Sure Strike (secondary arrows always crit; the crit roll is still consumed so the combat stream never
      shifts), Nerve Strike (+8% stun chance), Overload (crits charge Focus x3 instead of x2) and Horizon (+3%
      damage per 100 units of distance, sharing the LONG_RANGE distance lane with Deadeye). Ten evolutions
      become fifteen with no icon contract involved; the fork row renders three labelled options (LEFT/MID/RIGHT
      on three lines of the row) and the row's touch splits into thirds. The simulator's `simPick` deliberately
      keeps buying the same five evolutions it always did, so every measured band stays bit-identical across
      the fork widening; each new fork is pinned end to end in `SkillEvolutionWiringTest` instead, and the
      presentation/touch contracts moved with the layout (`PremiumShopPresentationTest`,
      `StatShopTouchLayoutTest`, `SkillEvolutionTest` now pin three options and fifteen ids).
      **The skill axis stays open on purpose:** 5 skills is what the reviewed premium-v2 icon pipeline can
      ship; adding a sixth `SkillId` without a medallion fails `SkillIconContractTest`, and the Blender art
      review loop lives outside this sandbox. B2 closes with the two axes that are code: 15 evolutions, 20
      affixes, and a documented art-pipeline dependency for the third.
- [x] **B3 (−8) No choice of hero, class or playstyle.** One defender, one weapon, one 50-hour line.
      Closed with the axis that is code: a hero-path choice as the first phase of the existing pre-run draft —
      four cards in the trial cards' own grammar (icon, name, green bend, red price), trials dealt only after a
      path binds. The four paths are zero-sum on purpose — UNBOUND bends nothing (the classic numbers), ROOT
      trades −10% damage for +25% max health, WIND trades −10% max health for +15% attack speed, STAR trades
      −10% attack speed for +25% focus gain — so there is no strongest path, only different runs, and no power
      creep reaches the frozen balance baseline: the simulator builds states that never choose a path, a null
      path decodes to identity multipliers, and `HeroPathWiringTest` pins both bit-for-bit. The bound path is
      run state like the draft picks: encoded, re-dealt by `prepareOffer`, repaired on decode (old saves and
      garbage strings decode to unchosen, never crash), and a mid-draft reload replays the path phase exactly
      as it replays the trial phase (`draftPending()` holds across both — the offer is already dealt). The card
      icons reuse four reviewed manifest keys (`hero`, the aegis, the wind boots, the starfall bow), so no art
      contract moved. Still open by necessity: the hero model and the weapon themselves live in the Blender
      art pipeline outside this sandbox; the path is the playstyle axis code can carry alone.
- [ ] **B4 (−4) Ascension was inverted for non-optimiser players.** Fixed (`NonOptimiserBandTest` now guards the
      band), but it was found by a simulated policy, not by a player — re-validate against C1 when a human
      session exists.

## C — balance and fairness (66/100, 34 points deducted)

- [ ] **C1 (−20) No human has ever played this game, and the repository says so.** All three sessions in
      `docs/playtests/sessions.json` have `player: "balance simulator"`; the finding
      `finding-first-run-has-no-recorded-human-session` is `open`. Everything in `docs/PLAYTEST_PROTOCOL.md`
      exists for this item and has never been used. This needs the owner's hands on a device — it is the one
      deduction no amount of code can close.
- [x] **C2 (−8) Fun is unmeasured.** Twelve balance test classes validate curves against simulated policies.
      Nothing measures whether a wave feels fair; C1 is the instrument. The machine half now exists:
      `balance/FunMetrics` computes the shape of a run from the wave record the simulator already keeps --
      worst neighbour-wave difficulty jump (ambush), longest health-decline streak (death spiral), breather
      waves and longest breather streak (valleys, and walls of nothing), stalled waves, and run-end tension --
      and `FunInstrumentTest` freezes all of it on three fixed seeds for both shipped policies, with the caps
      per policy because the optimiser's numbers describe the curve while the naive player's numbers describe
      what the same curve costs somebody who never shops (a 1.09 spike, a thirteen-wave slide, ~1.4% stalls:
      measured, not assumed; B1's pity is what keeps those survivable). The measured bands and the reasoning
      live in `docs/BALANCE.md` under "The fun instrument". The human half stays open under C1: the protocol's
      sessions are what recalibrate these caps against a player who can feel unfairness rather than count it.
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
- [x] **D2 (−12) Eight enemy identities and three elite affixes.** The affix half is closed in code: the pool
      doubles to six -- hollowmolt splits into two husks on death, gravemoss regrows its own health (stun is
      the window that stops it), cinderhalo burns whoever stands inside its halo on a half-second rhythm --
      each with its own glowing outline (three new `ELITE_*` visual rarities) and its own two-part
      "Whispering Wounds" thread in `docs/STORY_CONTENT.md` §4, verbatim-locked by `EliteFragmentsTest`.
      The pool opens at **wave 101 only** (`EnemyWaveSpawner.LATE_AFFIX_WAVE`), the B2a playbook: below the
      gate the draw size stays 3, so every early and mid roll is bit-identical and the frozen baseline keeps
      its meaning; `EliteAffixPoolTest` pins both sides. The pressure it adds was measured, not assumed: the
      first cut tripped the trial-pair spike gate (0.4151 at wave 119 against the 0.40 ceiling), the tuning
      loop softened the three constants until the riskiest pair landed at 0.3590, and `docs/BALANCE.md`
      carries the regenerated numbers plus the one shallow quarter dip (-2.69%, inside the 5% allowance) that
      the deep pool put back on the board. The identity half stays open by necessity: new enemy bodies are
      sprite-sheet art, and the reviewed Blender pipeline that makes them lives outside this sandbox -- the
      same dependency B2 documented for the sixth skill.
- [ ] **D3 (−8) Four boss identities across forty encounters.** The encounter table itself is good work — eight
      fight scripts on a shifting permutation, deterministic against the save file — but only four of them have
      a body.
- [x] **D4 (−8) One fragment shader and one vertex shader in the whole project,** which caps how different the
      arena, the bosses and the weather can look from each other. The count is now three and three, and the
      two new pairs do exactly the differentiation the entry names — with one honest correction: the game
      has no weather system, so there was no weather to differentiate; inventing one under a shader item
      would have been scope theatre, and the audit's third noun is recorded here as a fact that was wrong.
      `shaders/arena-veil` is the air itself: two mist bands drifting against each other, a broad light
      shaft breathing from the upper right, tinted per wave by the stage's own `StageGrade` and faded out
      toward the bottom of the frame so the fight stays crisp, clamped to a whisper of alpha.
      `shaders/boss-aura` is the ground a boss stands on: a soft torus of that boss's own telegraph
      identity colour — the same colour its special-attack warnings use, so the aura and the telegraphs
      read as one voice — quarter-sunk around the sprite's feet line, which is
      `CombatEntityRenderer.BOSS_FEET_RATIO` of the 240-unit sprite below the anchor, read from the
      renderer rather than guessed. Both passes are procedural: one generated white pixel stretched over
      a quad, everything computed in the fragment stage, sampling no texture, so they cannot moire
      against the reviewed backdrop art. `ArenaAtmosphereRenderer` owns both, compiles both in pedantic
      mode at construction (a shader that does not compile throws at startup, and the CI emulator smoke
      run is what proves they compile where it counts), and is owned in turn by
      `ArenaEnvironmentRenderer` — drawn over the finished arena, under the actors — which kept every
      frozen file untouched: no ratchet entry moved. Both effects are motion, so both answer to the G3a
      gate: the composer hands the arena call the reduced-motion flag it already computed, a suppressed
      frame gets a frozen clock and a zeroed breath multiplier, and the effects hold on a calm still
      instead of disappearing. Because a mistyped uniform fails silently at the driver level — the exact
      dead-asset class of mistake `InternalAssetReferences` was born from — `ArenaAtmosphereShaderTest`
      pins the vocabulary in both directions by reading the sources: every fragment uniform is set by the
      renderer, every set name is declared, the vertex stages speak the SpriteBatch contract verbatim,
      the clamps and the reduced-motion multiplier live in the GLSL itself, and the aura/feet/tint
      arithmetic is held by pure-helper tests. 929/929 green locally, PMD and SpotBugs clean.
      **Correction of record, found by the CI emulator the same day:** the two procedural fragments crashed
      every in-run frame on a real GL stack — `SpriteBatch.setupMatrices` sets `u_texture` on whatever shader
      is current and libGDX throws when the program has no such uniform, and shaders that sample nothing
      declare no sampler. The unit tests could not catch it (no GL context), the shaders compiled fine, and
      the smoke run died on the first frame with `No uniform with name 'u_texture'`. Both fragments now
      declare the batch sampler and read it — declaring alone is not enough, because the GLSL optimizer
      strips an unread uniform and restores the crash — and
      `everyFragmentShaderInTheDirectoryDeclaresAndUsesTheBatchSampler` now walks the whole shader directory
      pinning the contract for every frag that ever ships, so this crash class is closed locally after all.

## E — visuals and presentation (70/100, 30 points deducted)

- [x] **E1 (−12) No post-processing.** No bloom, no AA, no depth, no vignette: the frame is the sprites and the
      clear colour. It is not anymore. `render/PostProcessRenderer` — the name is a deliberate resurrection of
      the dead stub `InternalAssetReferences` was born from, and the guard test that pinned the class's absence
      now pins the stronger property: it may exist only because the composer calls it and its shaders ship. The
      in-run world (arena, atmosphere, actors, particles, floating text, ceremony whispers) renders into a scene
      target; a bright pass keeps only the light above a luma threshold that sits above the deliberately dark
      arena art, so only hits, fire, gold and spell-light ever bleed; a separable nine-texel Gaussian blurs that
      light at half resolution in two five-tap passes; and a composite returns the frame with the bloom added
      where it was born and a radial vignette darkening the corners. The HUD and every overlay are drawn after
      the composite, straight to the screen — interface text never passes through a blur. Two of the audit's
      four nouns are recorded as considered rejections rather than silently dropped: no depth pass, because a
      defocused background would defocus the telegraphs the game is played on, and no AA pass, because the bloom
      already softens the aliasing sprite edges and an FXAA smear over one-pixel telegraph rings would cost more
      readability than it returns. Failure is a fallback, not a crash: a device that cannot give the chain its
      buffers disables it for good and the world draws exactly as it did before E1. The buffers cost a scene
      target at device resolution plus two half-resolution bloom targets, comfortably inside the wave-50 memory
      budget the CI emulator measures against. `PostProcessShaderTest` pins the uniform vocabulary in both
      directions (the silent-failure class), the SpriteBatch vertex contract, the restraint ceilings and the
      fallback arithmetic; the chain's real compilation happens where it matters — the CI emulator's GL stack,
      in pedantic mode, at construction. 933/933 green locally, PMD and SpotBugs clean.
      **Correction of record, measured by the CI emulator the same day:** the first run of the chain rendered
      every in-run screen near-black — the bare arena at a mean luma of 1.35 against the smoke suite's floor of
      30 — while the menu and overlay-only screens passed, which is the fingerprint of a composite returning
      darkness under healthy overlays. The cause was one GL call: the composite binds the bloom texture to unit
      1, and `Texture.bind(1)` leaves `GL_TEXTURE1` active; SpriteBatch flushes by binding the drawn texture to
      whatever unit is active, so the scene landed on unit 1 and `u_texture` sampled the near-black bloom as the
      frame. The fix is the two-word restore — `glActiveTexture(GL_TEXTURE0)` between the bind and the draw —
      and the class is pinned locally by a source-order test
      (`theMultiTexturePassRestoresTheActiveUnitBeforeTheBatchDraws`), because no unit test can see a texture
      unit and no emulator run should have to.
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

- [x] **F1 (−18) Four static music tracks for a 2.2-hour run.** No adaptive or layered music, so hour two sounds
      exactly like minute two. The vigil now breathes with the run: `vigil_tension.ogg` is a fifth generated
      track -- same key, tempo, bar count and chord progression as the vigil bed, no bass (the bed owns the
      low end), double-time percussion and a low counter-lead -- authored as a spec in
      `tools/audio/generate_music.py` like every other note in the game. `MusicSelectionPolicy.tensionFor`
      maps the run to 0..1 in the difficulty curve's own tiers (silent to wave 40, 0.35 to 100, 0.70 to 150,
      full after; a hero below 30% health adds 0.30 of urgency wherever the run is), and `MusicDeck` fades
      the layer under the bed at that tension, ducked and levelled with it. Phase discipline is structural,
      not hopeful: the layer opens on the same frame -- the same sample -- as the bed it rides, and any bed
      change closes it outright rather than resuming it against a playhead it never started on. Boss fights
      need no layer because the whole bed already changes to the march. The chain of evidence held: the level
      gate re-measured all 30 files (the stem decodes at peak 0.838 -- Vorbis overshoots the 0.72 render
      target, which is precisely why the gate measures bytes and not intentions; the commit message that
      shipped the stem quoted the render target instead of the decode, and this line is the correction of
      record, in the spirit of H4), the ledger carries the stem's SHA-256, and the policy's curve, the
      red-health bonus and the one-bed-one-layer contract are all pinned in `MusicSelectionPolicyTest`.
- [x] **F2 (−10) Twenty SFX** cover twelve enemy/boss identities plus every skill, which means audible reuse.
      The identity axis now has voices: five new cues, all generated in-repo by `tools/audio/generate_sfx.py`
      (an effect that is code is an effect whose licence, length and loudness are reviewable). Light bodies
      (rootling, gloom wolf, bark stalker, sap hound) die with a quick breathy yelp; heavy bodies (stonekin,
      fungal brute, husk warden, bramble thrall) collapse under a sub thud and gravel; and three of the four
      boss bodies announce themselves -- the Ancient Golem in falling stone, the Ember Wyrm in a two-part
      shriek, the Void Knight as a hollow fifth collapsing inward -- while the Thorn Matriarch keeps the
      shipped horn. `audio/IdentityCues` owns the mapping and falls back to a shipped cue for any state it
      cannot read, so nothing that played before F2 can go silent. The whole contract chain held: the level
      gate re-measured every file (`LEVELS.md` regenerated, 29 files, loudest peak 0.900 against the 0.94
      ceiling), the license ledger carries each new hash, and `AudioContractTest` pins the cue count at 24.
      `WaveDirectorTest` now accepts any of the four entrance voices, because wave 20's boss has a body and
      the body has a voice.
- [ ] **F3 (−6) No voice or narration,** including for the 31 lore entries and the boss title cards that are
      written as if they were being read aloud.
- [x] **F4 (−4) Haptics are the `VIBRATE` permission** and a couple of trigger points. The hands now speak
      the run's punctuation, not just the UI's: `HapticFeedback` grew five words -- a hit, a boss falling, a
      level, a wave rolling over, the ultimate going out -- as defaults, so every fake that predates the
      vocabulary still compiles and a host without hands is a silence rather than a crash.
      `presentation/HapticRunWatcher` fires them by diffing frame to frame, which keeps every gameplay
      system free of the device (the same reason the music follows the flow from the presentation side), and
      two rules keep it honest: the first frame after the run screen is entered only remembers, so a load or
      a pause never pulses for damage nobody watched, and the hit pulse is rate-limited because a swarm is
      one event, not a jackhammer. The gate is the effects toggle: a silenced phone is also a stilled one.
      `GdxHapticFeedback` says each word inside what libGDX 1.13 actually offers -- duration plus amplitude
      (API 26+, full-amplitude fallback), no patterns -- and the difference is honest: 55 ms full-strength `code:main/java/com/amirrezahadipoor/herodefense/input/GdxHapticFeedback.java`
      for a hit, the longest heaviest 140 ms the hands ever say for a boss `code:main/java/com/amirrezahadipoor/herodefense/input/GdxHapticFeedback.java`, a medium lift for a
      level, an 18 ms soft tick for a wave `code:main/java/com/amirrezahadipoor/herodefense/input/GdxHapticFeedback.java`, one long pulse for the ultimate. Seven watcher tests pin
      the vocabulary, the cooldown, the rebaseline and the silences.

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
  - [x] **G3-layout The settings screen is full.** Six rows is the ceiling of a non-scrolling screen; a seventh
        needs a scrolling list, and G3b and G3c each want a row. This is the blocker for both, not a nicety.
        Fixed by turning the settings viewport into a drag-scrollable list with `VISIBLE_ROWS = 6` visible slots
        between 270f and 1090f (`SettingsTouchLayout.slotY(slot)` and `visibleSlotAt(y)`). `SettingsTouchController`
        maintains `firstVisibleIndex` and `accumulatedDrag` with `ROW_DRAG_THRESHOLD = 55f` matching Codex and
        Inventory, resetting overshoot at boundaries so reverse scrolling responds instantly. `SettingsOverlayRenderer`
        draws the visible window dynamically and displays a subtle golden scroll indicator on the trailing edge
        when rows exceed 6, while keeping 100% pixel-identical output at scroll offset 0. `ScreenTouchRouter`
        routes drag gestures to settings, `ScreenStateComposer` passes the active scroll index to the renderer,
        and `SettingsScrollLayoutTest` locks slot geometry, drag clamping, and scrolled hit-dispatch.
        `HeroDefenseGame` shrank to 775 lines (ceiling 779), strictly respecting the architecture ratchet.
  - [x] **G3b Text size.** Three scales (0.85/1.0/1.18) as `display.textSize` persisted via `LocalSettingsRepository`,
        cycled by a seventh settings row (`TEXT_SIZE`). `GameFonts` now owns a `textScale` (default 1.0, clamped
        0.5..2.0) applied in `worldSizeFor(role, metrics, scale)` and rebuilds its atlases when the scale changes
        (`setTextScale` / `applyTextScale`). The controller cycles the scale and applies it live via reflection
        so headless unit tests without a GL context still pass, while the repository applies it on load. The
        scroll viewport from G3-layout shows the new row after one drag (TOTAL_ROWS now 8 with G3c, VISIBLE_ROWS=6).
        Text-fit guard extended to include the new subtitle (≤30 chars). Local compile green; full suite to be verified
        by CI (test-core workflow) due to sandbox memory limits.
  - [x] **G3c Colour-blind-safe rarity colours.** Rarity was previously carried by a private `rarityColor(String)`
        inside `InventoryOverlayRenderer` and tier art. Centralised the five item rarity tiers (`COMMON`,
        `UNCOMMON`, `RARE`, `LEGENDARY`, `MYTHIC`) into `VisualRarity` with dedicated dual palette support:
        the default historical palette (`#E7D8B1`, `#74C365`, `#6FADEB`, `#F2B84B`, `#C77DFF`) and an accessible
        Okabe-Ito / Wong high-contrast palette (`#D9D2C9`, `#56B4E9`, `#0072B2`, `#E69F00`, `#CC79A7`) designed
        to preserve high chroma and luminance separation across protanopia, deuteranopia, and tritanopia.
        Pairwise Euclidean RGB distance between every tier in the accessible palette is mathematically asserted
        to be >= 0.20 in `VisualRarityTest` (ranging 0.37..1.15). Wired `colourBlindRarity` into `GameSettings`,
        persisted via `display.colourBlindRarity` in `LocalSettingsRepository`, and integrated as the 8th row in
        the scrollable settings viewport (`SettingsTouchLayout.Action.TOGGLE_COLOUR_BLIND_RARITY`), complete with
        bilingual English/Persian translations ("ACCESSIBLE RARITY" / "رنگ‌های دسترس‌پذیر") gated under 30 characters
        in `SettingsTextFitTest`. `InventoryOverlayRenderer` delegates all item badges, list accents, and detail panels
        to `VisualRarity.colorForTier(tier, colourBlind)`. Added `ColourBlindRaritySettingsTest` and expanded
        `DropRarityVisualTest` and `SettingsScrollLayoutTest`. TOTAL_ROWS now 8 (6 visible, 2 scroll steps).
  - [ ] **G3d Screen-reader support — recorded, not started.** This is one libGDX surface, so TalkBack has
        nothing to read: real support means an accessibility delegate publishing virtual views for the HUD, the
        menus and the card choices, and keeping them in step with a renderer that redraws every frame. That is
        an Android-platform project of its own and it is not honest to tick it off as a settings row. Written
        here so the −14 is accounted for rather than quietly redefined to mean the three things that were
        cheaper.
- [x] **G4 (−4) The HUD does not mirror in RTL,** a documented and deliberate asymmetry, still an asymmetry a
      Persian player meets every wave. The asymmetry is gone, and it is gone in the only way `UiMirror`'s
      contract allows: the drawing and the hit-testing mirror together or not at all. `HudTouchLayout` kept
      its frozen design-grid constants and grew five accessors — `speedX()` through `ultimateX()` — that
      return the constant in English and the mirrored edge in Persian; the five `*At` hit tests read the same
      accessors, so a Persian player presses the pause button where the pause button is drawn, which in
      Persian is where speed used to be. The utility row reverses with it: ultimate, shop, inventory from the
      leading edge. `HudRenderer` routes every horizontal position through the mirror — the two info panels,
      the six icons, the trial stack, the button frames — and its two text helpers do the rest: left-aligned
      labels mirror by their shaped measured width, centred ones about the screen's centre, and the EXP
      caption moved from `drawRightAligned` to `drawTrailing`, which is the same pixel in English and the
      correct edge in Persian. The bars were the one genuinely new shape: the tracks mirror, and a new
      `barFillX` helper starts every fill at the leading edge, so health and EXP drain toward the trailing
      edge in both languages instead of left-to-right in a right-to-left screen. One field died for this:
      `CombatEntityRenderer.DROP_TARGET_X` was a `static final` that captured the English inventory position
      at class-load, before any locale existed, and drops would have flown at a button that is not there in
      Persian — it is `dropTargetX()` now, and the ratchet moved 656→658 with that reason recorded at the
      entry. English is pixel-frozen by construction: every mirror call is the identity when the locale is
      English, which is what kept this item safe to ship between device runs. Pinned by the RTL test in
      `HudTouchLayoutTest` (mirrored edges, reversed row order, hits inside the mirrored boxes and outside
      the English ones) and the new `HudRendererMirrorTest` (fill arithmetic both directions, full-bar
      identity, drop target following the button, and no locale residue after the switch). 920/920 green
      locally, PMD and SpotBugs clean.
- [x] **G5 (−2) Overlay text density** (inventory, codex, tooltips) leaves little room for a thumb. The
      codebase already had a thumb standard — `HudTouchLayoutTest` has always held the in-run HUD to
      96-unit targets — the browsing overlays just never answered to it. They do now. The codex was the
      worst offender: rows 74 tall on an 84 stride, ten units of gap between two lore entries; they are 96
      on 108, and the band between the tab strip and the details panel fits five such rows instead of six
      cramped ones, which is the trade this item exists to make — scrolling is cheap, mis-taps are not. The
      codex tabs grew with the rows (74→96) and the two shelves, which used to start 32 units apart for a
      reason nobody could reconstruct, now share one geometry under one strip. The inventory: loadout slots
      and backpack rows to 96 (rows on a 104 stride), auto-sell chips 80→96, the slot column dropped six
      units at the top to keep the backpack label clear, and the set-bonus line and feedback toast moved
      down with the taller fourth row. The stat shop's tabs and root button were 68 — the rows there were
      already generous at 136 — and are 96 now, the strip lowered so the tabs never reach the close button.
      Every interior text and icon offset recentred with its box. Pinned twice over: the existing geometry
      tests updated to the new coordinates, and a new `OverlayTouchDensityTest` holding the floor itself —
      every target ≥96, every inter-row gap ≥8, and the clearances that keep taller geometry from sliding
      under a neighbour (codex rows above the details panel, inventory rows above the set-bonus strip,
      slots above the backpack list, chips under the header band, shop rows above the help panel). 924/924
      green locally, PMD and SpotBugs clean.
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
- [x] **H3 (−6) HUD and numbers stay untranslated by design.** The design did not survive contact with the
      audit: the HUD chrome was already localized — `HudStrings` and `GameLocale.number` have covered the
      panels since R7.3 — but the combat pop-up layer spoke English to everyone. Three surfaces, three
      fixes. Damage numbers were ASCII with a Latin "k" tail in both languages; `GameNumbers.compact` now
      owns that shape, English frozen at the exact strings the renderer always drew and Persian getting its
      own digits, CLDR's U+066B decimal separator and «ه» for هزار — a compact number whose tail is a Latin
      letter is the mixed-script word this class exists to prevent, and U+066B is a glyph no Persian string
      ever asked for, so the `GameFonts` derived character set grew by one codepoint and the coverage gate
      proves the face carries it. The STUN tag became `HudStrings.STUN_TAG` ("STUN" / «مات»). The auto-sell
      pop-up became `ItemStrings.FLOATING_COIN_AUTOSELL` with its English side frozen at the old
      concatenation byte-for-byte — two coin pop-ups, two frozen strings, because unifying them onto the
      pickup's "$ +n" would have moved pixels the device captures gate. English surfaces are all
      pixel-identical, so no capture reference moves. Pinned by a codepoint-level compact test in
      `GameNumbersTest` and a Persian-language emit test in `FloatingDamageTextSystemTest`; local suite
      green and the static gates clean. This commit also pays the R8.5 debt from the haptics close: the
      durations quoted there now carry their
      `code:main/java/com/amirrezahadipoor/herodefense/input/GdxHapticFeedback.java` citations, and
      `check_perf_provenance.py` passes again.
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
