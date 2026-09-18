# Hero Defense Balance Contract

These coefficients are centralized in renderer-independent Java so the Phase 14 simulator can tune them without changing game-flow code.

## Regular enemy growth

For wave `w` clamped to 1–200 (Phase 18.4 extended the run; waves 1–100 keep the Phase 17 curve unchanged):

- The required starting candidate was `20 × 1.045^w`; Phase 14 simulation tuned it to `1.035`, and the Phase 17 lifesteal-and-skills rebalance raised it to the shipped `20 × 1.037^w` (Wave 100 enemies carry 21% more HP than before).
- Baseline damage growth is `0.27 × 1.003^(w−1)` (Phase 17 raised it from `1.002`), before archetype scaling and before the middle segment's hotter `1.006`.
- The shipped curve itself, wave by wave, is generated below from `DifficultyCurve` rather than copied: the numbers this section used to publish were two curve revisions out of date by the time anyone checked.

<!-- balance:generated growth-checkpoints -->
| Wave | Baseline HP | Baseline damage |
|---:|---:|---:|
| 1 | 20.74 | 0.2700 |
| 25 | 49.79 | 0.2910 |
| 50 | 135.97 | 0.3379 |
| 75 | 371.28 | 0.3924 |
| 100 | 938.72 | 0.4293 |
| 125 | 1783.28 | 0.5305 |
| 150 | 3387.69 | 0.6555 |
| 175 | 5291.74 | 0.7708 |
| 200 | 8265.95 | 0.9063 |
<!-- balance:end growth-checkpoints -->
- **Second half (waves 101–200, after the planting ceremony):** the half climbs in **two spans** (Phase 91, R4.6). Waves 101–150 — the entry — continue from their Wave 100 values at `HP × 1.026^(w−100)` and `damage × 1.0085^(w−100)`; waves 151–200 — the final quarter — climb at `1.018` and `1.0065`. Measured HP checkpoints on the shipped curve: Wave 100 `938.67`, Wave 125 `1783.34`, Wave 150 `3387.71`, Wave 175 `5291.71`, Wave 200 `8266.02`; baseline damage reaches `0.9063` at Wave 200. Phase 18.4 shipped `1.021 / 1.006` against a simulator that ignored the Anvil; once the simulated player reforges equipped Rare/Legendary items (Phase 19.3) the second half fell to 1–3% pressure per wave, so the curve was tightened one notch. `1.024/1.008`, `1.024/1.010`, `1.025/1.010` and `1.0235/1.008` were rejected because their worst single wave exceeded the 35% ceiling (36–43%) or clears passed 80 s. A single `1.023/1.008` rate covering the whole half is what Phase 91 replaced: see the R4.6 section below for why the half is split and what the split cost.
- A regular hit is capped at 28% of the max HP of a reference Hero who invests one of every five earned points in Health.
- Archetype HP multipliers, relative to the 20-HP Rootling: Rootling `1.00`, Stonekin `1.70`, Gloom Wolf `0.85`, Fungal Brute `2.30`.
- Archetype damage multipliers, relative to the authored 5-damage Rootling: Rootling `1.00`, Stonekin `1.40`, Gloom Wolf `1.20`, Fungal Brute `2.00`.
- Regular populations grow from four and cap at 24 so late waves remain a readable melee defense rather than an unbounded swarm.
- Movement speed, melee reach, and attack interval remain archetype properties rather than wave-scaled values.

## Boss growth

- Boss HP on milestone wave `w`: baseline regular HP at `w` × `15`.
- Boss contact damage on milestone wave `w`: baseline regular damage at `w` × `3`.
- Boss movement, reach, interval, and special behavior remain identity-specific.

## Hero stat gains

- Strength: `+2` base damage per point from a `10`-damage baseline.
- Agility: `+0.03` attacks per second per point from `1.00`; interval is the reciprocal.
- Luck: multiplies item-drop rates by `1.02` per point.
- Dodge: `+0.5` percentage points per point, capped at `60%`.
- Health: `+10` maximum HP per point from a `100`-HP baseline.

## Equipment tier power

| Tier | Relative power target | Whole-stat budget |
|---|---:|---:|
| Common | +5% | 1 point |
| Uncommon | +12% | 2 points |
| Rare | +25% | 4 points |
| Legendary | +45% | 7 points |

The relative targets express intended contemporary-run impact. Every authored item spends its tier's entire whole-stat budget across one or two of the five Hero stats.

## Anvil (item reforging, Phase 18.3)

Only Rare and Legendary catalog items can be reforged, up to `+5`. Each step adds `+1` to every stat bonus on the item and raises its sell price by half the step's cost. Step costs are `base × 1.6^level`, rounded to 5 coins: Rare `150, 240, 385, 615, 985` (total `2 375`), Legendary `350, 560, 895, 1 435, 2 295` (total `5 535`). A fully reforged Legendary therefore carries `+17` whole stat points (7 authored + 10 forged), which is why the Anvil is priced like ~2–3 late stat levels per step rather than as a cheap sink.

## Economy audit (Phase 19.3)

The simulator's spending policy models a thrifty player: talent points go to the lowest base stat, coins always buy the cheapest affordable stat or skill level, spare drops are sold, and the Anvil is used on an equipped item whenever its next step is no dearer than the cheapest shop purchase (`BalanceSimulator.forgeEquippedItems`). `BalanceSimulator.lastLedger()` exposes the resulting coin flow. Baseline seed over 200 waves:

<!-- balance:generated economy-audit -->
| Flow | Coins | Count |
|---|---:|---:|
| Kill income | 91177 | |
| Item sales | 24579 | |
| Stat shop | 58990 | 135 levels |
| Skill shop | 39580 | 44 levels |
| Anvil | 15980 | 28 steps |

Across the 9 gate seeds the split is stable: stats 51-57%, skills 28-35%, Anvil 13-15% of spend.
<!-- balance:end economy-audit -->

Notes on the flows: kill income scales `×(1 + 0.025·wave)` with bosses worth `50 + 20·n`; item sales are the
second largest source of income and auto-sell is equivalent for the economy; the Anvil's end state is every
equipped Rare or Legendary at +4/+5. The table and the stability line under it are read from
`BalanceSimulator.lastLedger()`, so they are the simulator's own books rather than a transcript. Item sales
matter: without them a run would lose roughly two stat levels per ten waves, which is why sell prices stay at
`12 / 30 / 75 / 180` and forged items sell for more.

## The second half's two spans (Phase 91, R4.6)

`WavePressureCurveTest` measures the shape of the run in four fifty-wave quarters and found the shallowest step in the curve where the second half begins: waves 101–150 were only **4.7%** heavier than waves 51–100, against **71%** for the step before them, because the whole half ran at the curve's coolest rate (`1.023 / 1.008`) against the base `1.037` and the middle `1.041`. Two repairs from wave 121 (`1.025/1.0085` and `1.024/1.008`) were measured and rejected on the ceilings — trial pairs 0.41–0.47 and tier-10 STRENGTH median 0.677 against their 0.40 and 0.60.

Both rejections had the same cause, and it is the reason the half is now split instead of simply raised. **The second half's heat is the run's spike carrier.** Every spike the gates catch late in a run sits on an **elite wave** (the elite schedule is every seventh wave at tier 0: 126, 133, 154, 182 and 196 are all elite waves), and an elite's contact damage carried a flat `×1.5` on top of a damage baseline that had already climbed for a hundred more waves. Measured over the five trial seeds, a softer elite multiplier of `1.2` in the second half buys **0.01–0.03** of spike headroom on the worst pairs and moves average pressure by **less than 0.001**, because an elite's damage does not change how long its wave takes. That purchase is what pays for the hotter entry.

Shipped shape, all measured on the five fixed seeds of `WavePressureCurveTest` and the five of `TrialSimulationTest`:

For the record, the state the split replaced, measured the same way: quarter means `0.0576 / 0.0986 / 0.1032 /
0.1229`, steps `×1.712 / ×1.047 / ×1.191`, sweep average range `0.081–0.107`, elite multiplier `×1.5` in both halves,
worst trial-pair median spike `0.381`, reward-card spike `0.262`. Everything below is generated on the shipped curve,
so the comparison cannot rot:

<!-- balance:generated second-half -->
| Quantity | Measured now | Where it comes from |
|---|---:|---|
| Quarter means (fixed sweep) | `0.0576 / 0.0986 / 0.1142 / 0.1298` | `WavePressureCurveTest`'s five seeds |
| Quarter steps | `x1.712 / x1.158 / x1.136` | the same sweep |
| Sweep average range | `0.0748 - 0.1193` | the same sweep, inside the 0.05-0.15 band |
| Deepest single-seed quarter dip | `4.97%` against the `5.00%` allowance | the same sweep |
| Elite contact multiplier, first half / second half | `x1.5 / x1.2` | `EnemyWaveSpawner` |
| Riskiest trial pairs, median spike | `0.3729 / 0.3816 / 0.3515` | `TrialSimulationTest`'s five seeds, against the 0.40 ceiling |

The three pairs are the ones this gate has caught above 0.38, in the order of the row: `BOSS_BOUNTY + FAMISHED_EARTH`, `BOSS_BOUNTY + BLOOD_PRICE`, `MISERS_PACT + BLOOD_PRICE` (the other eleven pairs of the matrix run in the gate, not here).
| Reward-card spike, AGILITY forced at boss 1 | `0.38992` | `RewardCardSimulationTest`'s seed, against the 0.40 ceiling |
<!-- balance:end second-half -->

The step into the second half more than doubled, the wave-200 enemy is 9% lighter in health and 5% lighter in damage than the old single rate left it, and the price is carried in the final quarter, which is now the coolest span of the curve. Two honest caveats, both of them visible in the generated table above rather than buried: the deepest single-seed quarter dip spends most of the five percent the gate allows, and the reward-card matrix keeps very little headroom against its ceiling — so a later change that adds pressure to the *first* hundred waves has almost nothing to spend.

**It is not enough for the blocked bad-luck rule.** R4.3's pity rule needed about `0.06` of trial headroom (its candidates moved pairs to `0.4146` and `0.4386` against `0.40`) and R4.6 returned `0.01–0.03`, leaving the shipped worst pair at `0.382`. The rule stays deferred; see the economy section above.

## The ascension ladder in growth rates (Phase 91)

A tier charges twice, and the two charges have deliberately different shapes. The **growth bump** multiplies every
growth rate by its own factor — `0.0004` health and `0.00025` damage per tier — which is a fifth of a percent of a
wave in the opening and a multiple of the run by wave 200: heavy exactly where the hero is already strong, and it is
the reason a tier's charge used to be invisible. The **base charge** (R4.7) scales the enemy's baseline health by
22% and damage by 30% per tier at wave 1 and fades linearly to nothing by wave 141: heaviest exactly where a tier's
flat starting power is worth the most. That is what closed the inversion R4.7 measured — for a player who ignores
every system, tier 10's mean brief-vigil pressure was `0.0019` against tier 0's `0.0918`, and its long-vigil reach
was `102.3` waves against `69.7`; on the same six seeds it now measures `0.0507` and `77.3` waves. The cost is a
slower run and it is priced rather than hidden: the optimiser's tier-10 session grows **32%** against the **60%** the
session gate allows (tier 2 is the tightest, at 84% of its own budget), and the average-pressure ceiling in
`AscensionGateTest` is tier-indexed, because a harder ladder is what a tier *is*. The search behind the two numbers,
including the two mixes and one fade span that were measured and rejected, is in the R4.7 roadmap entry.

<!-- balance:generated ascension-bumps -->
| Tier | Health growth per wave | Damage growth per wave | Base health charge at wave 1 | Base damage charge at wave 1 | Second-half entry | Final quarter |
|---:|---:|---:|---:|---:|---:|---:|
| 0 | 1.0370 | 1.00300 | x1.00 | x1.00 | 1.0260 | 1.0180 |
| 3 | 1.0382 | 1.00375 | x1.66 | x1.90 | 1.0272 | 1.0192 |
| 6 | 1.0395 | 1.00450 | x2.32 | x2.80 | 1.0285 | 1.0204 |
| 10 | 1.0411 | 1.00551 | x3.20 | x4.00 | 1.0301 | 1.0221 |

The base charge (R4.7) is health `+22%` and damage `+30%` per tier at wave 1, fading linearly to `+0%` by wave 141.

Elite waves arrive every 7 / 6 / 4 / 4 waves at tiers 0 / 3 / 6 / 10, and never on the 5-wave boss lap (R4.8: the cadence used to step through that lap and left tiers 6-8 with no elites at all).
<!-- balance:end ascension-bumps -->

## How the balance gate runs (Phase 91, roadmap R4.5)

The gates in this document are not prose: `:core:balanceGate` runs them, and CI runs that task as its own job on
every push, with its own timeout and its own report. The suites that sweep whole runs carry a `balance` tag —
`TrialSimulationTest`, `RewardCardSimulationTest`, `AscensionGateTest`, `NonOptimiserBandTest` — and are excluded
from `:core:test`, which is the seconds-long unit loop; `:core:check` depends on both, so the aggregate gate a
contributor runs locally is still the complete one. Measured on 2026-09-17: unit loop **31 s**, balance gate
**4 m 47 s**, no suite running twice.

The sweeps are fixed, because a gate that picks fresh seeds every push measures noise instead of the change:

| Gate | Seeds |
|---|---|
| `WavePressureCurveTest`, `BalanceSimulatorTest`, `EliteDamageAccountingTest` | `0x4845524F444546` + 0, 1, 2 then `0x747269616C7331` then `0x4341524453494D` |
| `TrialSimulationTest` | `0x747269616C7331` + 0…4, judged on the median run of the five |
| `RewardCardSimulationTest` | `0x4341524453494D`, every card forced at every boss 1…39 |
| `AscensionGateTest` | 8 cards × tiers {0, 3, 6, 10} × 5 seeds |
| `NonOptimiserBandTest` | 12 seeds: the two sweeps above plus `0x4E414956453031`…`37` |

## Critical hits

- Every Hero projectile has a deterministic `5%` critical chance and deals `1.75×` damage on success; Critical Mastery raises both (see Skill shop).
- A confirmed critical impact freezes only combat simulation for `CRITICAL_HIT_STOP_SECONDS = 0.045` s
  (`code:main/java/com/amirrezahadipoor/herodefense/polish/HitStopSystem.java`); UI and rendering continue.

## Direct stat shop

- Strength, Agility, Luck, Dodge, and Health can each be purchased up to 20 times with earned coins only.
- Base prices are `55`, `60`, `50`, `50`, and `65` coins respectively; purchase level `n` adds `20n` coins. This linear schedule tracks the 20 boss milestones without an unaffordable late-run exponential.
- The shop is entered and operated exclusively through touch targets from the paused run.
- For boss index `b` (1–20), the boss grants `50 + 20b` coins while a stat's contemporary purchase level `b−1` costs `base + 20(b−1)`. The reward alone therefore buys one upgrade in every case and is no more than 1.40× its price.
- Equipment resale is supplemental rather than the primary income source: Common `12`, Uncommon `30`, Rare `75`, and Legendary `180` coins.

## Skill shop (Phase 17)

Five coin-only skills, each with ten levels. Level `n` (0-based) costs `round5(base × 1.32^n)`; bases are Chain Lightning `260`, Multi Shot `300`, Stunning Arrows `220`, Critical Mastery `240`, Eagle Range `180`, so maxing one skill costs roughly 6–10k coins and all five ≈ 38k: a genuine late-run sink rather than an early spike.

- Chain Lightning: `10% + 5%/level` chance per primary hit to arc `55%` of the arrow's damage to the nearest `1 + (level−1)/3` foes within `210 px`. Secondary (Multi Shot) arrows never chain.
- Multi Shot: `+0.30` extra arrows per level (fractional part rolled), capped at 4, each at `70%` damage and spread across other foes in range.
- Stunning Arrows: `2%/level` chance to stun for `0.5 s + 0.05 s/level`; bosses take half duration. Stunned foes neither move, swing, nor cast specials.
- Critical Mastery: crit chance `5% + 0.5%/level` (10% at max), multiplier `1.75 + 0.075/level` (2.5× at max).
- Eagle Range: `+22 px/level` on the 420 px bow.
- Lifesteal (reward card, +2%/pick) also heals from chain arcs, which is why the Phase 17 enemy curve was raised.

## Kill rewards

- Regular kill coins use each enemy archetype's base reward times `1 + 0.025 × wave`.
- Boss `n` grants `50 + 20n` coins; the permanent coin-income card multiplies both reward sources.
- Regular XP uses archetype values; boss `n` grants `100 + 30n` XP. Rewards are claimed once before dead entities are removed.

## Equipment drops

- Per defeated enemy before Luck: Common `6%`, Uncommon `3%`, Rare `0.8%`, Legendary `0.15%`.
- Each effective Luck point multiplies every band by `1.02`; at most one item drops from a kill.
- The single cumulative roll checks Legendary first, then Rare, Uncommon, and Common.

## Health potions

- Tier heals are `15%`, `25%`, `40%`, `60%`, `80%`, and `100%` of current maximum HP.
- Healing is capped at max HP, full-health use is rejected, and a successful use consumes exactly one potion.
- Each defeated enemy has an independent `8%` potion chance.
- Tiers unlock progressively; within the unlocked set, tier `n` receives weight `n`, shifting drops toward contemporary potions without removing weaker stock.

## Reward-card budget

- Boss `b` (1–40) uses multiplier `1 + 0.05 × (b−1)`, rising smoothly from `1.00` to `1.95` at boss 20 and on to `2.95` at boss 40 (`RewardPowerBudget.MAX_BOSS = 40`).
- Percentage effects multiply their base magnitude by that budget.
- Base-stat cards award the rounded budget in whole stat points: one point early and two points late.
- Every displayed description is generated from the same budget object used to apply the effect.

The card regression runs all eight card identities as the forced choice at every boss with future combat (Bosses 1–39), for 312 complete 200-wave simulations. Each remaining segment must retain at least 5% average gross damage, 25 seconds average clear time, and pressure on at least 90% of waves, while still respecting the 35% damage and 120-second spike ceilings. The calibrated scenarios retained at least `6.8017%` average damage and `33.822624 s` average clear time; their worst single wave was `30.708814%` damage and `100.86547 s`. Boss 40 is omitted because no wave remains after its reward. After the Phase 18.4 extension the 312 scenarios retained at least `6.22%` average damage and `33.58 s` average clear time; their worst single wave was `29.60%` damage and `82.90 s`.

## Renderer-independent simulation gate

`BalanceSimulator` advances the real movement, attacks, projectiles, enemy and boss behavior, progression, drops, potions, equipment, shop, reward-card, and wave-lifecycle systems at 30 Hz. Its balanced automated policy distributes talent points across all five stats, always buys the cheapest affordable stat or skill level in the shop, equips upgrades, sells spare gear, and chooses rewards by a fixed survival/power priority. The fixed baseline seed emits one CSV row per wave with HP, gross incoming damage, DPS-to-enemy-HP ratio, clear time, and timeout state.

The deterministic regression gate requires all of the following:

- Complete exactly 200 waves with the Hero alive (the simulator plants the grove trees instantly at the 50/100/150 ceremonies).
- Average gross incoming damage from enemy attacks, divided by contemporary maximum HP, must be 5%–15% across the run. Gross damage is measured before potion and lifesteal recovery so healing cannot hide pressure.
- No single wave may exceed 35% gross damage or 120 seconds to clear.
- Every metric must be finite and no wave may hit the simulator's timeout.

After the Phase 17 rebalance, baseline seed `0x4845524F444546` and eight further seeds all completed 100/100 waves; across those nine runs the average gross damage was `9.3%`, the worst single wave `28.4%`, and the longest clear `68.2 s`. Every forced-card scenario (all cards at all 19 bosses) also stays under the 35% / 120 s spikes (worst `29.6%`, `71.2 s`). Candidates `1.038–1.040` HP growth were rejected: they pushed single-wave damage past 35% under the forced Dodge/Lifesteal card scenarios. This automated gate is reproducible balance evidence; the remaining multi-seed and manual checkpoints still have to validate resource starvation and subjective play feel.

### Phase 19.3 result (waves 1–200, Anvil-aware simulator)

With the second-half curve `1.023 / 1.008` and the Anvil policy enabled, baseline seed `0x4845524F444546` plus eight (SimProbe) and fourteen (extended) further seeds all completed 200/200 waves; across the nine gate runs the average gross damage was `8.6%`, the worst single wave `29.6%` (seed 2, wave 32 — a first-half spike unchanged from Phase 17), and the longest clear `69.0 s`. The baseline's second half sits at 3–6% gross damage per wave with 22–49 s clears and a DPS-to-HP ratio falling from `0.030` at Wave 100 to `0.010` at Wave 200.

### Phase 18.4 result (waves 1–200, historical)

With the second-half curve `1.021 / 1.006`, baseline seed `0x4845524F444546` and eight further seeds all completed 200/200 waves; across those nine runs the average gross damage was `10.0%`, the worst single wave `28.8%`, and the longest clear `72.3 s`. The baseline's second half sits at 2–6% gross damage per wave with 24–43 s clears and a slowly falling DPS-to-HP ratio (`0.027` at Wave 100 → `0.011` at Wave 200), so the run keeps tightening without a cliff. The acceptance gate (`GATE_WAVE = FINAL_WAVE`) and the forced-card regression (Bosses 1–39) now cover the full run.

### Phase 25.3a result (ascension schedule search, baseline seed `0x4845524F444546`)

The ascension schedule multiplies every growth constant by `(1 + bump × tier)`: health
`0.0005`/tier, damage `0.0002`/tier, with the same relative bump on the second-half constants
(so wave-200 stats compound the bump twice). Tier 0 is bit-identical to the untiered curve.
The roadmap's starting form (`0.015` / `0.008`) died at waves 24–52 on tiers 3–10 with
57–77% average damage — roughly 50–100× too hot for the fixed-power sim hero — so the
search below re-tuned the coefficients while keeping the specified multiplicative form:

| health / damage bump | t3 avg/max/clear | t6 avg/max/clear | t10 avg/max/clear |
|---|---|---|---|
| 0.015 / 0.008 (start) | DIED w52 | DIED w36 | DIED w24 |
| 0.001 / 0.0005 | 6.5 / 27.2 / 61.4 ✓ | 11.6 / 63.3✗ / 103.0 | 20.7✗ / 162.5✗ / 205.5✗ |
| 0.0004 / 0.0002 | 5.0 / 22.0 / 56.5 | 5.2 / 23.0 / 66.5 | 4.3✗ / 25.0 / 72.7 |
| 0.0006 / 0.0003 | 8.3 / 33.6 / 79.7 | 5.3 / 28.2 / 60.0 | 9.6 / 62.1✗ / 136.3✗ |
| **0.0005 / 0.0002 (shipped)** | **6.41 / 25.77 / 70.77** | **5.61 / 27.71 / 79.70** | **5.05 / 32.87 / 105.47** |
| 0.0006 / 0.00015 | 7.85 / 31.07 / 79.67 | 4.70✗ / 24.02 / 60.03 | 7.69 / 46.66✗ / 136.27✗ |
| 0.0003 / 0.0003 | 5.38 / 19.13 / 61.43 | 3.76✗ / 17.09 / 49.70 | 4.54✗ / 32.75 / 98.63 |
| 0.0004 / 0.00025 | 5.10 / 22.61 / 56.47 | 5.46 / 24.27 / 66.47 | 4.66✗ / 27.51 / 72.73 |

(Averages in % gross damage; max = worst single wave %; clear = worst clear seconds.
Bands: avg 5–15%, max ≤ 35%, clear ≤ 120 s.)

The shipped point is the only all-green baseline row, confirmed on three extra seeds
(`+1`, `+2`, `0x123456789`): tier averages stayed in band on 11/12 cells (5.05–7.51%),
but the single-wave max — one wave in 200, mostly boss-adjacent late waves — is seed
noise (±7pp at t10: 32.9–46.2%) and breached 35% on 4/12 cells. Two structural notes:
the elite interval `7 → 6 → 5 → 4` collides with boss waves at interval 5, so tiers 6–8
spawn no elites (every multiple of 5 is a boss wave; test-locked at the time). R4.8 removed the collision — the
cadence steps `7 → 6 → 4`, every tier has elites again, and the assertion was turned around so that it checks the
outcome rather than the arithmetic; the note stays because it was correct when it was written, and elite loot
overcompensates the stat bumps — at t10, forty elite waves' double coins plus talent
materials snowball the hero, so difficulty is non-monotonic (baseline t10 avg 5.05% sits
below t3's 6.41%). Flags for the 26.1 gate: the shipped t10 baseline average has only
0.05pp of floor margin, and the 5% average floor already fails cross-seed at tier 0
itself (seed `0x123456789`: 4.69%), so the 9-seed gate needs tier-relative, averaged, or
ceiling-only-cross-seed bands rather than the naive per-seed 5–15%.

### Phase 25.3b result (middle-third segment, baseline seed `0x4845524F444546`)

Waves 25–80 grow at their own slightly hotter first-half rate (`1.040` health /
`1.004` damage instead of `1.037` / `1.003`); waves 81–100 resume the base rate from
the hotter wave-80 value, so the second half is rebased but not reshaped. The shipped
run averages `10.14%` gross damage with a `25.17%` worst wave (178) and an `80.30 s`
longest clear — still inside the 5–15% / 35% / 120 s gate. Middle-third quarters rise
end to end: `7.75% → 8.95% → 10.77% → 11.21%` (previously flat at ~8% with zero
fitted slope). The rise is locked by a `Q4 > Q1 + 1pp` assertion on the baseline seed.

### Phase 26.1a result (ascension gate, tiers 0/3/6/10)

The tier-0 bands do not transfer to tiers: across 9 seeds the naked tier-10 average
sits at 5.03–9.26% (no room to cool) while its worst single wave reaches 37–55%
(single-wave spikes are seed noise even at tier 0, which breaches 35% on one seed),
so the naive per-cell 5–15% / 35% / 120 s window is empty above tier 0. The committed
`AscensionGateTest` therefore re-derives the bands instead of re-running them: every
cell must finish 200/200; naked and forced-card averages stay strict at 5–15% (all
cells green); single-wave maxima and max clears gate per-tier MEDIANS (one wave in
200 is seed noise — a tier-6 seed threw 63.8% against a 40.1% next-worst), with an
absolute 100% backstop at every cell; naked/card median ceilings index 40% + 2pp per
tier with clears at 120 s + 3 s per tier; trial-pair median spikes index 40% + 4pp
per tier; and the trial-pair pressured floor steps down 175 → 160 → 145 → 120 waves
(root bonuses plus elite loot let empowered builds trivialize wave counts faster
than they blunt spikes). The gate covers a naked 9-seed matrix (36 runs), every
forced card at boss 20 on 3-seed medians (96 runs), and power/damage/horde trial
pairs on 3-seed medians (36 runs); the full 66-pair and 312-scenario matrices stay
tier-0 (their spike ceiling re-derived 35% -> 40% in 26.1c, below).

### Phase 26.1b result (Elite damage accounting)

No main-code change: the Elite-affix-aware accounting path already exists — the
spawner marks Elites inside simulated runs, and Elite melee, blightburst blasts, and
weeping rot all land inside the gross-damage HP-delta window. `EliteDamageAccountingTest`
locks the inclusion: the baseline run spawns all 23 Elite waves, every one lands
pressured damage (1.74–24.14%), and Elite waves contribute +0.62pp to the reported
10.14% run average.

### Phase 26.1c result (Ultimate + Evolution policies, ult-era re-derivation)

The simulator now fires the Ultimate the moment Focus fills and buys one focused
Evolution (closest skill to level 10, higher-DPS fork via `simPick` — DEADEYE on the
baseline — funded by a quarter of each visit's coins once the fork opens, so power
keeps flowing instead of stalling behind full hoarding). Fire-on-cooldown at the
shipped 24.1 budget (4x damage, 2 focus/hit, 549 fires/run) trivialized the game
(naked average 1.66%), so the gate set the power budget: shipped 2x damage with 0.1
focus/hit paces rare nukes (~120 fires/run, one per 1.6 waves) and lands the naked
baseline at 9.91% mid-band. Ultimates flattened the 25.3b middle (+0.01pp), so the
segment re-steepened to 1.041/1.006 (quarters 7.23 → 9.52 → 10.76 → 9.29%,
rise +0.0207 against the +1pp bar). Empowered spike ceilings re-derived for the
ult-era middle: tier-0 trial/card maxima 35% -> 40% (worst 37.67%, HEAVY_CROWNS +
FAMISHED_EARTH), trial-spot median ceiling to 40% + 4pp per tier (BLOOD_PRICE +
HOLLOW_CALLING tier-3 median 49.5%). Naked averages hold per-cell strict 5–15% at
every tier (36 cells: 6.08–10.8%).

## Manual Balance Guidance (Phase 26.2)

### 26.2a manual checkpoints at tiers 0, 5, and 10 (14.6-style)

Protocol (extends the 14.6 wave list `{1, 5, 25, 50, 75, 100}` with `{150, 200}`
for the endless second half): play each checkpoint wave at tiers 0, 5, and 10 on
the baseline seed and record felt difficulty — incoming pressure, clear time,
potion urgency, and any moment that felt unfair rather than hard. No human
playtest has been run yet (same waiver as 14.6); the table below is the
simulator's felt-difficulty proxy evidence on the baseline seed, to be confirmed
or corrected by owner playtests:

| wave | tier 0 dmg / clear | tier 5 dmg / clear | tier 10 dmg / clear |
|---|---|---|---|
| 1 | 0.0% / 10 s | 0.0% / 5 s | 0.0% / 3 s |
| 5 (boss) | 11.9% / 24 s | 0.8% / 9 s | 0.0% / 4 s |
| 25 (boss) | 2.8% / 16 s | 0.7% / 9 s | 0.0% / 6 s |
| 50 (boss) | 3.5% / 25 s | 1.7% / 20 s | 1.2% / 18 s |
| 75 (boss) | 3.2% / 23 s | 3.2% / 25 s | 1.6% / 22 s |
| 100 (boss) | 5.1% / 28 s | 5.2% / 31 s | 3.2% / 23 s |
| 150 (boss) | 2.6% / 39 s | 4.8% / 50 s | 2.0% / 39 s |
| 200 (boss) | 8.5% / 41 s | 10.6% / 54 s | 11.5% / 61 s |
| run avg / max | 9.91% / 30.0% | 11.84% / 49.0% | 7.35% / 50.0% |
| close calls (>=25%) | 3 waves | 29 waves | 10 waves |
| pressured waves | 197 | 174 | 159 |
| run clear time | 8409 s (2.34 h) | 9746 s (2.71 h) | 7884 s (2.19 h) |

Felt read (sim-derived, pending human confirmation): tier 0 ramps steadily with a
spicy first boss (11.9% at wave 5 — the wake-up call). Tier 5 is the hottest
checkpoint (11.84% average, 29 close calls): interval-6 Elite waves with only
mid-ladder root power make the middle tiers the meat grinder, not the top.
Tier 10 stomps the early game (0% through wave 25 on root power — expect
boredom, not fear, before wave 50) and turns knife's-edge late (50% worst wave,
61 s final boss). Difficulty over tiers is non-monotonic with a mid-ladder peak.
Note: the tier-5 baseline run bought no Evolution (fork plus fund never aligned
there); the policy is conditional per-run, locked firing on the tier-0 baseline.

### 26.2b target session model (one Wave 1–200 run)

Pace definition: engaged (Ultimate on cooldown, focused Evolution, greedy-optimal
build), shopping (every wave and level-up), no idle time (back-to-back waves,
instant planting ceremony). That is exactly the simulator's pace, so the model is
the simulator's median run clear time: tier 0 clears 200 waves in **7970 s
(2.21 h)** (3-seed median; seeds span 7625–8409 s). Every ascension tier must land
within roughly ±20% of that time (committed as `sessionTimesStayWithinTwentyPercentOfTierZero`
on 3-seed medians — single-seed totals swing ±20% inside one tier, so cells cannot
gate it). Measured medians: t1 8005 (+0.4%), t2 7232 (−9.3%), t3 7672 (−3.7%),
t4 7588 (−4.8%), t5 7259 (−8.9%), t6 8437 (+5.9%), t7 8565 (+7.5%), t8 8796
(+10.3%), t9 8681 (+8.9%), t10 7884 (−1.1%) — all inside ±10.3%, so added challenge
comes from build precision (pressure, spikes, close calls), not padded wave count.

Phase 32 Living Grove adds grove plantings at waves 50/100/150 (short 4.45 s at 50/150, full 7.2 s at 100) reusing the sapling atlas; simulator ceremonies are instant, so the 3-seed median stays **7970 s (2.21 h)** and the tier deltas above re-verified green on 2026-09-15 (t0 7970 s, t1 8005 +0.4%, t2 7231 −9.3%, t10 7884 −1.1%, all within ±10.3% and ±20% gate). Extra grove HP is post-death siege only and does not move the DPS-to-HP pressure curve, so second-half checkpoints and growth constants are unchanged.


### Phase 89 result (enemy roster 4 -> 8, ascension gate held)

R3.4 doubles the regular roster to eight roles. The spawner cycles types uniformly, so the
additions are authored with the per-type means held exactly (health 117, damage 28,
experience 69, coins 19, speed 242, reach 172, interval 5.10 over eight roles as over
four); waves 1-10 stay on the four field creatures and only deep waves draw from all
eight, interleaved with a coprime stride. The change is deliberately variance, not weight.

The ascension gate is what proved it. With the first draft of the additions the
forced-card sweep failed one cell of 160: a forced Dodge build at tier 6 on seed
`20342418142676295` averaged **15.907%** of the hero's health per wave against the 15%
ceiling (its four sister seeds: 6.90-10.55%). A probe across eight cards x four tiers x
five seeds, plus a per-wave breakdown of the offending cell, located the cause in wave
duration rather than in the card: late waves took 100-146 s to close instead of 50-70 s,
so each wave spawned on top of the last one and the seed that fell behind never recovered
(30+ waves above 35% damage in its final sixty). A build with the least offence outlived
its own waves; the tier curve was stretching waves rather than hardening them.

Three repairs were measured and two were rejected on evidence:

* **Roster walk-in floor** — speed five points from the fastest walker to the slowest
  (Bark Stalker 74 -> 69, Bramble Thrall 28 -> 33), total untouched. Fixes the Dodge cell
  (worst tier-6 seed 10.559%), but adds tier-0 pressure that the tier-0 gates reject:
  `TrialSimulationTest` median spike 0.4083 against the 0.40 ceiling on BOSS_BOUNTY +
  FAMISHED_EARTH, and `BalanceSimulatorTest` caught a bare-run spike of 36.4% against 35%.
  Rejected: the roster is authored to leave tier 0 alone.
* **Ascension health only** — per-tier health bump 0.0005 -> 0.0004. Fixes the Dodge cell,
  but the tier-10 naked average fell to 4.938% against the 5% floor: tier 10 already has no
  room to cool, exactly as Phase 26.1a recorded. Rejected on its own.
* **Ascension schedule, both bumps** (the shipped repair) — health bump 0.0005 -> 0.0004
  and damage bump 0.0002 -> 0.00025: late waves close again, and the pressure lands as hit
  weight instead of wave length. Every ascension constant multiplies by `max(0, tier)`, so
  tier 0 is bit-identical and every tier-0 gate in the repository is untouched. Green:
  `AscensionGateTest` 4/4 (naked matrix, forced cards, trial pairs, session drift) and
  `DifficultyCurveTest` 11/11, with no band, ceiling or floor moved.

Run `./scripts/balance-check.sh` immediately after every coefficient change and as a mandatory precondition to any manual playtest. The script forces a fresh run rather than accepting Gradle's prior task output. `BalanceSimulatorTest` also remains part of the complete `:core:test` suite executed by the core GitHub Actions workflow on every push and pull request.

## Late-run pressure: the curve after wave 40 (Phase 91, roadmap R4.1)

Measured by `WavePressureCurveTest`, which runs the fixed five-seed sweep
(`0x4845524F444546`, `+1`, `+2`, `0x747269616C7331`, `0x4341524453494D`) through the whole
two-hundred-wave run. Pressure is the share of the hero's health a wave took — the same
measurement every other gate in this repository uses. The windows are fifty-wave quarters,
because a forty-wave window straddles the curve's segment boundaries and then measures the
window instead of the curve.

Mean pressure per quarter, shipped curve (each seed's own quarters are in the gate's failure
message when it fails; these are the sweep means):

| Quarter | waves | mean pressure | step |
|---|---|---|---|
| Q1 | 1-50 | 0.0576 | — |
| Q2 | 51-100 | 0.0986 | x1.71 |
| Q3 | 101-150 | 0.1032 | **x1.047** |
| Q4 | 151-200 | 0.1229 | x1.19 |

The run rises after wave 40 and ends at roughly twice the pressure it opened with (last twenty
waves 0.132-0.159 against the first twenty at 0.022-0.041 on the five seeds). Per seed, no
quarter is more than 1.4% lighter than the one before it (Q3 dips by 1.2-1.4% on three of the
five seeds). Run average 0.081-0.107, peak wave 0.22-0.30, inside the 0.05-0.15 and 0.40 bands.

**The shallow step is real and is now an item.** Waves 101-150 are only 4.7% heavier than waves
51-100, against 71% for the step before them, because the second half is the coolest stretch of
the curve: `SECOND_HALF_HEALTH_GROWTH` 1.023 and `SECOND_HALF_DAMAGE_GROWTH` 1.008 against the
base 1.037/1.003 and the middle 1.041/1.006. That is roadmap item **R4.6**, and it is open
because both repairs that were measured spent headroom the shipped ceilings do not have:

| candidate | change | measured effect | why it was rejected |
|---|---|---|---|
| late segment A | from wave 121, health 1.025, damage 1.0085 | Q-mean steps x1.711 / x1.092 / x1.491, run average 0.0960 -> 0.1061 | `BalanceSimulatorTest` bare-run spike 0.3540 against the 0.35 ceiling; trial pairs 0.4002-0.5241 against 0.40; tier-10 STRENGTH median 0.6765 against 0.60 |
| late segment B | from wave 121, health 1.024, damage 1.008 | Q3 0.1032 -> 0.1088 (step x1.104), Q4 0.1558, bare-run spikes inside the ceiling | trial pairs 0.4071-0.4732 against 0.40 (`BLOOD_PRICE + HOLLOW_CALLING` at tier 0 median 0.4071) |

B is the informative rejection: raising only the late health rate — no damage change at all —
still broke trial-pair ceilings, and one of its failures landed at wave 119, before the changed
segment, i.e. the simulation's random stream diverged rather than the ceiling being approached
from one direction. The trial ceilings sit at their margins in the shipped build (a passing pair
can be within one percent of 0.40), so R4.6 has to buy its headroom somewhere — a slower late
damage rate, a wider trial ceiling with the reason written down, or an economy-side change —
rather than spend it.

Both candidates were run through their gates and reverted; nothing about them is in the shipped
build except this table.

## The non-optimiser band (Phase 91, roadmap R4.1, second half)

Every other gate in this repository measures one player: the optimiser policy of `BalanceSimulator` — it scores the
boss reward cards, keeps both shop tabs moving with the cheapest purchase it can afford, funds a skill evolution and
reforges its gear. Nobody plays that way on the first evening, so the item asked for the other measurement and a
*published band*, and `NonOptimiserBandTest` is both. The non-optimiser takes the first reward card it is offered,
never opens the shop, spreads its talent points evenly down the list and never reforges; it still equips a better
item and sells what it replaces, because the game's own opening asks that of anyone. Both policies run the same
combat, spawner, economy and curve code — the policy changes only those choices.

Measured in the brief vigil (30 waves, the mode's own ending, twelve seeds):

| tier | brief vigil wins | mean pressure | mean reach in the long vigil |
|---|---|---|---|
| 0 | **12/12** | 0.0918 | 69.7 waves |
| 3 | 12/12 | 0.0199 | 85.6 waves |
| 6 | 12/12 | 0.0089 | 94.3 waves |
| 10 | 12/12 | **0.0019** | **102.3 waves** |

The optimiser, on the same twelve seeds at tier 0, finishes every brief run at a mean pressure of 0.051 and every
two-hundred-wave run as well.

What the band says, and what it found:

* **The brief vigil is a floor, not a range.** A player who ignores every system still has to be able to finish the
  opening of the game; a tutorial that punishes you for not knowing the systems yet is a tutorial that loses
  players. The gate demands a **90% win rate** there and measures 100%.
* **The long vigil is the range.** The same player has to be caught by the curve somewhere — a run that cannot be
  lost is not a run — and has to get far enough to see the game. The gate demands an average reach between **55 and
  90 waves** and measures 69.7, with the run ending badly on all twelve seeds and the optimiser untouched at 200.
* **The ladder is inverted for this player, and that is now an item (R4.7).** Ascension is supposed to make the game
  harder; for a player who does not optimise it does the opposite at every step. Mean pressure in the brief vigil
  falls from 0.0918 at tier 0 to 0.0019 at tier 10 — a hundredfold — and the long-run reach *rises* from 69.7 to
  102.3 waves. The reason is that the tier's hero-side bonuses (the heartwood and root-network power a real player
  earns between runs, which the simulator grants at run start) are flat and large, while the per-tier enemy growth
  is a 0.0004 health and 0.00025 damage bump per tier compounding over the wave count. At tier 10 the hero starts
  with +10 strength, +20 health, +500 coins, +5 talent points and a wider Focus pool, and the enemies have grown
  ~4% in health and ~2.5% in damage: the ladder hands out the reward and charges almost nothing for it. The fix has
  to be found without spending the spike headroom the trial and tier ceilings need (see R4.6), which is why R4.7 is
  an item with numbers rather than a paragraph of intent.

## The telegraph contract (Phase 91, roadmap R4.2)

A boss special is the only attack in this game that announces itself, so what the announcement promises has to
hold for every encounter rather than for the first golem. `BossTelegraphContractTest` writes that promise down as
five rules and checks them over all four identities, the forty encounters of a run, ascension tiers 0/3/6/10 and
both vigils — 224 combinations for the first rule alone — plus the stun, the stutter and the double strike.

| the promise | how it is checked | what it measures |
|---|---|---|
| every boss warns for the reference window | the window is compared against `BossFightScript.REFERENCE_TELEGRAPH_SECONDS` and the encounter's own script, at every tier and in both vigils | **0.50 s** everywhere |
| the special lands when the window closes, never before | the window is walked in twenty steps of 0.05 s and the hero's health is read after each one | untouched for the first nineteen, hit by the twentieth |
| one warning carries exactly the hits its script authorises | the landed damage is compared against boss damage x the identity's authored per-strike figure x the script's multiplier x its hit count | one strike, or two for `TWIN_TELEGRAPH` — never a third |
| the ladder buys power and never reaction time | tier 0 against tier 10 on the same identity and encounter | the same window, a strictly harder hit |
| both vigils promise the same thing | the whole sweep runs in `STANDARD` and in `BRIEF` | identical windows |

The roster behind it, read out of `BossSpecialAttackSystem` and measured with a probe over the forty encounters:

| identity | reach | cycle | per-strike damage | strikes per warning |
|---|---|---|---|---|
| `ANCIENT_GOLEM` | 145 | 5.5 s | 1.60x boss damage | 1 |
| `THORN_MATRIARCH` | 210 | 5.0 s | 0.50x | 1 |
| `EMBER_WYRM` | 250 | 4.5 s | 0.55x twice inside one strike (1.10x) | 1 |
| `VOID_KNIGHT` | 480 | 4.0 s | 1.25x | 1 |

Three details the sweep had to get right, all of them measured rather than assumed:

* **The dodge die is rolled when the warning starts.** The roll is drawn at trigger and spent at detonation, so the
  outcome cannot change while the player is reading the tell, and a stun cannot re-roll it.
* **A stun holds the warning instead of cancelling it.** A stunned boss's window stops counting; when the stun ends
  the same warning finishes and lands once. It is never re-rolled and never thrown away.
* **A big health pool hides small differences.** These measurements hold the hero at 1000 health, because a float at
  100000 has a spacing of 1/128: a 1.31-health slam would land as 1.3125 and the tier-10 bonus at the first
  encounters — about one percent of the boss's damage, `+0.00025` per tier compounding — would vanish into the
  rounding. That is also why the earliest bosses make the ladder's damage bump invisible in play (R4.7).

## The drop economy (Phase 91, roadmap R4.3)

Two tables, both generated: the rates below are computed from the same constants the game rolls, and the document
is only allowed to contain what the generator computed — a number that no longer matches `ItemDropSystem`,
`EquipmentCatalog` or `EquipmentDefinition` fails `BalanceDocumentTest` instead of being copied and forgotten
(roadmap R4.4: the block markers, not the prose around them, are what the build checks).

<!-- balance:generated drop-economy -->
| tier | rate per kill | share of the item budget | pool | sell price | coins per kill |
|---|---|---|---|---|---|
| `COMMON` | 6.0000% | 60.29% | 14 | 12 | 0.7200 |
| `UNCOMMON` | 3.0000% | 30.15% | 12 | 30 | 0.9000 |
| `RARE` | 0.8000% | 8.04% | 9 | 75 | 0.6000 |
| `LEGENDARY` | 0.1500% | 1.51% | 5 | 180 | 0.2700 |
| `MYTHIC` | 0.0015% | 0.02% | 6 | 400 | 0.0060 |
| **total** | 9.9515% | 100% | 46 | | **2.4960** |
<!-- balance:end drop-economy -->

Read as an economy: a kill is worth about a tenth of an item and about two and a half coins, a wave is worth roughly
that much times its enemy count, and twenty kills are worth two items. Luck multiplies every band, so the same roll
buys a longer stretch of the table rather than a different table; elites and (with `BOSS_BOUNTY`) bosses floor the
tier at rare, which is the only way a roll that lands on nothing still drops something.

### Bad-luck protection: three rules measured, none shipped

Roadmap R4.3 asks for a pity rule and the honest state today is that the game has none, because the balance sweep's
ceilings sit within a percent of their limits and every version of the rule was measured moving one of them. All
three are recorded here with their prices, in the order they were tried:

* **A guaranteed common after thirty kills with no item.** Fires about five times per run. Broke three gates: the
  brief vigil's average pressure fell to **0.0346** against its 0.035 floor, `BOSS_BOUNTY + FAMISHED_EARTH` spiked to
  **0.4196** at wave 182 against the 0.40 ceiling, and one sweep seed's third quarter came in **6.5%** below its
  second against the 5% allowance.
* **A guaranteed legendary drop every twenty boss kills.** Two per run. Broke them differently: the same trial pair
  reached **0.4574** at wave 196, one sweep seed's third quarter fell **5.9%** below its second, and tier 6 sessions
  ran **20.3%** faster than tier 0 against a 20% allowance.
* **A promotion instead of a gift** — the next boss drop after a twenty-kill drought becomes a legendary, so the run
  spawns the same number of items and spends exactly the same draws. This is the cheapest version the rule can have
  and it still broke the trial gate: `BOSS_BOUNTY + FAMISHED_EARTH` at **0.4386** and `MISERS_PACT + BLOOD_PRICE` at
  **0.4146**, both at wave 196, against 0.40.

The third line is the useful one. It says the blocker is not the size of the gift and not the extra randomness: a
trial ceiling that a passing pair already sits within one percent of cannot absorb *any* economy change, which is the
same missing headroom `R4.6` describes from the curve's side. The rule returns when that headroom exists; the table
above is what it will be priced against.
