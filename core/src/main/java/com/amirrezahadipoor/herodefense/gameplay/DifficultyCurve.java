package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;

/** Central wave-number coefficients; renderer-independent for later simulations. */
public final class DifficultyCurve {
    public static final float BASE_ENEMY_HEALTH = 20f;
    public static final float ENEMY_HEALTH_GROWTH = 1.037f;
    public static final float ENEMY_TYPE_REFERENCE_DAMAGE = 5f;
    // The second half climbs in two spans (roadmap R4.6). Waves 101-150 -- the entry -- climb at
    // SECOND_HALF_*_GROWTH, hotter than the 1.023/1.008 the whole half used to run at, because the
    // step into the second half was the shallowest in the curve (x1.047 against the x1.71 before it).
    // Waves 151-200 -- the final quarter -- climb at FINAL_QUARTER_*_GROWTH, cooler than the old
    // tail, which is where the run's heaviest single waves sat. Together the two spans end the run
    // 9% lighter in health and 5% lighter in damage than the old single rate did, while the measured
    // quarter step rises to x1.165. Waves 1-100 read neither span, so the first half -- and every
    // brief-vigil and tier-0 gate -- stays bit-identical to the shipped curve.
    public static final float SECOND_HALF_HEALTH_GROWTH = 1.026f;
    /** Last wave of the second half's hotter entry span; the final quarter owns the rest of the run. */
    public static final int SECOND_HALF_ENTRY_LAST_WAVE = 150;
    public static final float FINAL_QUARTER_HEALTH_GROWTH = 1.018f;
    // Middle-third segment (Phase 25.3b): waves 25-80 grow slightly hotter than the
    // base first-half rate so pressure rises end to end instead of plateauing.
    // Tuned against the 5-15% / 35% / 120 s gate (see docs/BALANCE.md).
    public static final int MIDDLE_SEGMENT_FIRST_WAVE = 25;
    public static final int MIDDLE_SEGMENT_LAST_WAVE = 80;
    public static final float MIDDLE_HEALTH_GROWTH = 1.041f;
    public static final float BOSS_HEALTH_MULTIPLIER = 15f;
    // H1 hard era: boss contact hits at 3.25x the baseline, not 3x. Boss health is untouched --
    // hard mode is hit weight, never hit points -- and the first bosses stay fair because the
    // wave-5 damage share falls at the same time (see below): wave 5's boss costs the same bar
    // it always did, while wave 200's hits a third harder.
    public static final float BOSS_DAMAGE_MULTIPLIER = 3.25f;

    // --- The damage anchor (audit item 1) -------------------------------------------------------------------
    // Enemy damage is no longer a growth chain of its own. It was one -- `0.27 x 1.003^(w-1)` -- and the audit
    // that closed B1 measured what that produced: a monster's hit at wave 200 was 0.82 points on a bar of 863,
    // or 0.095% of it, and the only wave in a whole run that cost the hero more than 29% of the bar spent it in
    // 63 seconds. Health grew x398 across the run and damage x3, so the ratio between "how long this takes to
    // kill" and "how hard it can hit me" diverged by a factor of 131. The clamp that was supposed to catch this
    // -- MAX_REASONABLE_HEALTH_FRACTION_PER_HIT -- never fired once in two hundred waves.
    //
    // The replacement is an anchor rather than a rate: damage is a *share of the bar the hero is expected to be
    // carrying on that wave*. Two tables, both measured, both linear between their anchors:
    //
    //   * EXPECTED_BAR_* is the bar the shipped progression actually produces, read off the simulator's own
    //     wave-by-wave record (100 at wave 1, 382 at wave 50, 601 at wave 100, 863 at wave 200). It is the
    //     *expectation*, not the player's bar: a player who invests in health does not make monsters hit
    //     harder, which is the failure mode of scaling damage to the live hero.
    //   * DAMAGE_SHARE_* is the share of that bar one regular hit takes, and it falls across the run because
    //     the number of hits a wave lands rises with it (four bodies become twenty-four and a wave goes from
    //     ten seconds to a minute). What has to stay in band is the *total* a wave costs, and the band is the
    //     gate's 15-35% (docs/BALANCE.md, and PressureBandTest enforces it).
    //
    // The consequence for everything else in the game is the point of the change: a boss special is now 24% of
    // the bar instead of 0.9% (boss contact damage is x3 the baseline, a special x1.6 that), an elite is x1.5,
    // and the brace, the step, the dodge and the telegraph all finally multiply, avoid or prevent something that
    // matters. DifficultyCurveTest pins the arithmetic: no type's hit exceeds its share of the expected bar at
    // any wave, and the share falls monotonically.
    static final float[] EXPECTED_BAR_ANCHOR_WAVES = {1f, 10f, 25f, 50f, 80f, 100f, 150f, 200f};
    static final float[] EXPECTED_BAR_ANCHOR_VALUES = {100f, 166f, 250f, 382f, 500f, 601f, 790f, 863f};
    static final float[] DAMAGE_SHARE_ANCHOR_WAVES = {1f, 5f, 40f, 100f, 200f};

    /**
     * What one landed contact hit takes out of the bar the hero is expected to be carrying (audit item 1).
     *
     * <p><b>Why the number falls instead of holding or rising.</b> A hit's size is not a free parameter: a wave
     * deals damage <em>hits x share</em>, and a wave's budget is the bar the hero walks into it with. The
     * simulator says how many times a body gets to swing before the bow deletes it, and that count is the part of
     * the game that is broken, not the multiplier: measured on the fixed sweep, waves 1-4 land 0-7 contact hits,
     * the first boss wave lands 151, and waves 100-200 land 110-200 each, because a late wave spawns the cap of
     * twenty-four bodies and they all reach the hero. So a share that holds a visible 5% of the bar from wave one
     * to wave two hundred would cost 130 hits x 5% = six and a half bars in a single late wave. The share has to
     * fall, and it falls because of a number that was measured rather than a difficulty rate that was asserted:
     * this is the honest shape the old curve only pretended to have.
     *
     * <p>The anchors are set so that a wave spends a budget a player can read: waves 1-4 land so few hits that a
     * hit can be worth about a percent of the bar and still leave the wave under a tenth of it, the first boss
     * wave is the first real spike (about 60% of the bar, up from 1.9% before this change), the middle of the run
     * sits near a quarter of the bar per wave, and the last quarter holds a quarter of it. The raw hit grows
     * across the run (0.65 damage on a 100 health bar at wave one, 1.90 on an 863 health bar at wave two hundred),
     * so damage still grows with health as the audit demanded -- it just stops outgrowing the wave it arrives in.
     *
     * <p><b>What would have to change for 5% a hit.</b> A 5% contact hit is a 5% hit only in a game that lands
     * twenty hits a wave. Making contact hits rare and loud -- fewer swings, longer wind-ups, readable telegraphs,
     * a swing that a step can beat -- is how this table gets flattened back towards a constant share, and that is
     * item 6 (two attack axes) and item 4 (wave forms), not a number that can be typed in here. Until then, the
     * honest statement of this curve is: a hit is worth between a tenth and a third of a percent of the bar in the
     * late game, and a wave is worth a quarter of it.
     */
    // H1 hard era: the opening softens a breath (waves 1-5), the middle heats (+4% at wave 40) and the
    // late run climbs (+20% at wave 100, +22% at wave 200). The table stays strictly falling -- a late
    // wave lands far more hits, so the share still falls -- but nearly flat past wave 5, which is what
    // makes the tail brutal: the same share times twice the hits. Boss health, spawn counts and the
    // expected bar are untouched: this era's hardness is hit weight, never bloat.
    static final float[] DAMAGE_SHARE_ANCHOR_VALUES = {0.0065f, 0.0026f, 0.0025f, 0.0024f, 0.0022f};

    /**
     * The share of the bar a boss special takes before the encounter's own multiplier (audit item 1 + A5).
     *
     * <p>A special is the one attack in the game that is <em>telegraphed</em>, and the audit's complaint about the
     * old numbers was that the telegraph warned about 0.46% of the bar at the end of the run. This table makes the
     * warning worth reading: the golem's 1.6x special lands at 7.2% of the bar at wave one and 12% at wave two
     * hundred, the wyrm's double 0.55x at 2.5% twice and 4.1% twice, the matriarch's 0.5x at 2.3% and 3.8%. The
     * per-boss multipliers are untouched -- they are the encounter's identity -- and only their *base* moved from
     * "three times the baseline regular hit" to "a share of the bar", because a special and a melee swing are not
     * the same kind of event and should not be priced as multiples of one another.
     *
     * <p><b>Why it is not 25% yet, measured.</b> A boss lands about five and a half specials per boss wave on the
     * shipped cadence (one every five to seven seconds of a twenty-five to forty second fight), and the boss wave
     * also eats about half a bar in contact damage, so the share above already costs a boss wave roughly 0.7 bars.
     * Set the special to a quarter of the bar and the fifth wave -- the first boss, the one the player meets with
     * tier-1 potions and no gear -- is a guaranteed death: a 25% special is only a decision when the telegraph has
     * an inside and an outside (roadmap A5, item 2), which is the next commit. The brace already multiplies this
     * by 0.4 like any other hit.
     */
    static final float[] BOSS_SPECIAL_SHARE_ANCHOR_WAVES = {1f, 5f, 40f, 100f, 200f};
    // H1 hard era: the first bosses' specials are untouched (waves 1-5 stay fair), the late ones climb
    // (+7% at wave 40, +12% at wave 100, +17% at wave 200). Still under the 10% base / 15% heaviest
    // ceilings DifficultyCurveTest pins: telegraphed hits stay payable, just worth noticeably more dread.
    static final float[] BOSS_SPECIAL_SHARE_ANCHOR_VALUES = {0.045f, 0.050f, 0.062f, 0.074f, 0.088f};

    // Ascension schedule (Phase 25.3): relative per-tier bumps on every growth constant,
    // tuned against the simulator at tiers 0/3/6/10 (search in docs/BALANCE.md). Phase 89
    // re-derived the pair against the eight-role roster: the deeper roster let a tier-6 forced
    // Dodge build -- the build with the least offence -- outlive its waves, and the per-tier
    // health bump was stretching every late wave instead of hardening it (15.907% average
    // against a 15% ceiling). Health comes down 0.0005 -> 0.0004 so late waves close, and
    // damage rises 0.0002 -> 0.00025 so the pressure lands as hit weight instead of as wave
    // length. Both constants multiply by max(0, tier), so tier 0 -- and every tier-0 gate in
    // this repo -- stays bit-identical.
    public static final float ASCENSION_HEALTH_BUMP_PER_TIER = 0.0004f;
    public static final float ASCENSION_DAMAGE_BUMP_PER_TIER = 0.00025f;

    // The ladder's base charge (roadmap R4.7). The per-wave bumps above only pay off late: at tier 10 they are a
    // fifth of a percent of a wave in the opening and a multiple of it by wave 200, so a tier handed the hero flat
    // power at run start and asked almost nothing for it in return. Measured for the player who ignores every
    // system, that made the ladder run *backwards*: 0.0918 mean pressure in the brief vigil at tier 0 against
    // 0.0019 at tier 10, and 69.7 waves of average reach against 102.3 -- a tier's starting strength is three
    // times a first-wave bow's damage, so the player who never buys anything simply killed the opening faster.
    //
    // These two constants scale the enemy's *baseline*, so the charge lands from the first wave. The size and the
    // mix were searched, not guessed, and the search is the reason the numbers look the way they do. Health alone
    // fixes the opening (it is what the naive advantage is made of -- the early waves cannot be made longer by
    // damage, because they die to one or two hits either way) but health is also what makes a wave *take longer*,
    // and the ladder pays for its reward in seconds as well as in blood: at 0.30 health / 0.15 damage the optimiser's
    // tier-10 session grew 63%, against 35% for the shipped 0.22 / 0.30, for the same ladder numbers. Damage alone
    // was measured and rejected twice -- on its own it cannot open the ladder at all (0.0039 brief pressure at tier
    // 10, still inverted) and pushed without health it wrecks the optimiser's late game (0.2128 average, final
    // quarter 0.572). So the charge is damage-heavy but not damage-only. The fade spans most of the run for the
    // same reason the mix moved: a 60-wave fade fixed the brief (0.0370) and left the long vigil inverted (103.8
    // waves against 71.3), because the reach is decided in the middle of the run, not the opening.
    //
    // Shipped, on six seeds per tier: brief pressure at tier 10 is 0.0507 against 0.0901 at tier 0 (56% of the
    // tier-0 value where the finding measured 2%), long-vigil reach is 77.3 waves against 71.3, and the optimiser
    // finishes every run with an average that rises 0.1035 -> 0.1750 -- the cost of a harder ladder, which is where
    // the tier-indexed ceilings in AscensionGateTest come from.
    public static final float ASCENSION_BASE_HEALTH_BUMP_PER_TIER = 0.22f;
    public static final float ASCENSION_BASE_DAMAGE_BUMP_PER_TIER = 0.30f;

    /**
     * The ladder's charge is counter-cyclical on purpose: it is heaviest in the waves where the tier's flat reward
     * is worth the most. A tier hands the hero starting stats, and starting stats are a multiplier in the opening --
     * ten points of strength is three times the damage of a first-wave bow -- and a rounding error by wave 150. So
     * the base charge fades over the first {@link #ASCENSION_BASE_CHARGE_SPAN_WAVES} waves instead of scaling the
     * whole run, and it is linear rather than a cliff so that no wave of the ramp is visibly the one where the tier
     * stops charging.
     */
    public static final int ASCENSION_BASE_CHARGE_SPAN_WAVES = 140;

    public static float baseScaleForTier(int waveNumber, int tier) {
        return 1f + ASCENSION_BASE_HEALTH_BUMP_PER_TIER * Math.max(0, tier) * baseChargeFade(waveNumber);
    }

    public static float baseDamageScaleForTier(int waveNumber, int tier) {
        return 1f + ASCENSION_BASE_DAMAGE_BUMP_PER_TIER * Math.max(0, tier) * baseChargeFade(waveNumber);
    }

    private static float baseChargeFade(int waveNumber) {
        if (ASCENSION_BASE_CHARGE_SPAN_WAVES <= 0) {
            return 1f;
        }
        float elapsed = Math.max(0, waveNumber - 1);
        return Math.max(0f, 1f - elapsed / ASCENSION_BASE_CHARGE_SPAN_WAVES);
    }

    public static float healthGrowthForTier(int tier) {
        return ENEMY_HEALTH_GROWTH * (1f + ASCENSION_HEALTH_BUMP_PER_TIER * Math.max(0, tier));
    }


    public static float finalQuarterHealthGrowthForTier(int tier) {
        return FINAL_QUARTER_HEALTH_GROWTH * (1f + ASCENSION_HEALTH_BUMP_PER_TIER * Math.max(0, tier));
    }


    /** Waves the second half spends at its entry rate before the final quarter's cooler climb. */
    public static int secondHalfEntryWaves() {
        return SECOND_HALF_ENTRY_LAST_WAVE - GameState.PLANTING_WAVE;
    }

    public static float secondHalfHealthGrowthForTier(int tier) {
        return SECOND_HALF_HEALTH_GROWTH * (1f + ASCENSION_HEALTH_BUMP_PER_TIER * Math.max(0, tier));
    }


    public static float middleHealthGrowthForTier(int tier) {
        return MIDDLE_HEALTH_GROWTH * (1f + ASCENSION_HEALTH_BUMP_PER_TIER * Math.max(0, tier));
    }


    public float baselineRegularHealth(int waveNumber) {
        return baselineRegularHealth(waveNumber, 0);
    }

    public float baselineRegularHealth(int waveNumber, int ascensionTier) {
        int wave = clampWave(waveNumber);
        int firstHalf = Math.min(wave, GameState.PLANTING_WAVE);
        int secondHalf = Math.max(0, wave - GameState.PLANTING_WAVE);
        int early = Math.min(firstHalf, MIDDLE_SEGMENT_FIRST_WAVE - 1);
        int middle = Math.min(
            Math.max(0, firstHalf - early), MIDDLE_SEGMENT_LAST_WAVE - MIDDLE_SEGMENT_FIRST_WAVE + 1);
        int late = Math.max(0, firstHalf - early - middle);
        int entry = Math.min(secondHalf, secondHalfEntryWaves());
        int finalQuarter = Math.max(0, secondHalf - entry);
        return BASE_ENEMY_HEALTH
            * baseScaleForTier(wave, ascensionTier)
            * (float) Math.pow(healthGrowthForTier(ascensionTier), early + late)
            * (float) Math.pow(middleHealthGrowthForTier(ascensionTier), middle)
            * (float) Math.pow(secondHalfHealthGrowthForTier(ascensionTier), entry)
            * (float) Math.pow(finalQuarterHealthGrowthForTier(ascensionTier), finalQuarter);
    }

    public float regularHealth(EnemyType type, int waveNumber) {
        return regularHealth(type, waveNumber, 0);
    }

    public float regularHealth(EnemyType type, int waveNumber, int ascensionTier) {
        return baselineRegularHealth(waveNumber, ascensionTier) * type.baseHealth() / BASE_ENEMY_HEALTH;
    }

    public float baselineRegularDamage(int waveNumber) {
        return baselineRegularDamage(waveNumber, 0);
    }

    /**
     * The bar a hero is expected to be carrying on a given wave: measured, not guessed.
     *
     * <p>The anchors are the simulator's own wave-by-wave record of {@code startingMaxHealth} for the sustained
     * balance policy, rounded to the nearest whole point and read back with linear interpolation. The point of
     * naming this curve rather than hard-coding a second growth rate is that every damage number in the game is
     * derived from it, so a future change to hero progression moves the monsters with it instead of silently
     * changing how hard the game is -- which is exactly what happened when damage was a rate of its own.
     */
    public static float expectedHeroMaxHealth(int waveNumber) {
        return interpolate(EXPECTED_BAR_ANCHOR_WAVES, EXPECTED_BAR_ANCHOR_VALUES, clampWave(waveNumber));
    }

    /** The share of {@link #expectedHeroMaxHealth} one regular hit of the reference archetype takes on this wave. */
    public static float damageShareOfExpectedBar(int waveNumber) {
        return interpolate(DAMAGE_SHARE_ANCHOR_WAVES, DAMAGE_SHARE_ANCHOR_VALUES, clampWave(waveNumber));
    }

    /**
     * The per-tier charge on damage. The old curve carried it as a bump on the per-wave growth rate, which is the
     * same thing as this compounding factor: a ten-tier ladder is `1.0025^199` harder at wave 200, so the deepest
     * tier pays about 1.65x the baseline in the last quarter and nothing on wave one.
     */
    public static float ascensionDamageCharge(int waveNumber, int ascensionTier) {
        int tier = Math.max(0, ascensionTier);
        if (tier == 0) {
            return 1f;
        }
        return (float) Math.pow(1f + ASCENSION_DAMAGE_BUMP_PER_TIER * tier, Math.max(0, clampWave(waveNumber) - 1));
    }

    public float baselineRegularDamage(int waveNumber, int ascensionTier) {
        int wave = clampWave(waveNumber);
        return expectedHeroMaxHealth(wave)
            * damageShareOfExpectedBar(wave)
            * baseDamageScaleForTier(wave, ascensionTier)
            * ascensionDamageCharge(wave, ascensionTier);
    }

    public float regularDamage(EnemyType type, int waveNumber) {
        return regularDamage(type, waveNumber, 0);
    }

    /**
     * The damage one hit of {@code type} deals on {@code waveNumber}: its share of the expected bar, scaled by the
     * archetype's authored weight. There is no clamp any more (audit item 1): a clamp was the old curve's emergency
     * exit for damage that had drifted away from the bar, and the anchor above makes the exit unnecessary by
     * construction. {@code DifficultyCurveTest} keeps the property the clamp was there to protect.
     */
    public float regularDamage(EnemyType type, int waveNumber, int ascensionTier) {
        return baselineRegularDamage(waveNumber, ascensionTier)
            * type.baseDamage() / ENEMY_TYPE_REFERENCE_DAMAGE;
    }

    /** Linear interpolation between two tables of anchors, used by both damage curves above. */
    private static float interpolate(float[] waveAnchors, float[] valueAnchors, float wave) {
        if (wave <= waveAnchors[0]) {
            return valueAnchors[0];
        }
        for (int index = 1; index < waveAnchors.length; index++) {
            if (wave <= waveAnchors[index]) {
                float span = waveAnchors[index] - waveAnchors[index - 1];
                float position = span <= 0f ? 0f : (wave - waveAnchors[index - 1]) / span;
                return valueAnchors[index - 1] + position * (valueAnchors[index] - valueAnchors[index - 1]);
            }
        }
        return valueAnchors[valueAnchors.length - 1];
    }

    public float bossHealth(int waveNumber) {
        return bossHealth(waveNumber, 0);
    }

    public float bossHealth(int waveNumber, int ascensionTier) {
        return baselineRegularHealth(waveNumber, ascensionTier) * BOSS_HEALTH_MULTIPLIER;
    }

    public float bossDamage(int waveNumber) {
        return bossDamage(waveNumber, 0);
    }

    public float bossDamage(int waveNumber, int ascensionTier) {
        return baselineRegularDamage(waveNumber, ascensionTier) * BOSS_DAMAGE_MULTIPLIER;
    }

    /** The damage one boss special carries on this wave, before the encounter's own multiplier. */
    public float bossSpecialDamage(int waveNumber) {
        return bossSpecialDamage(waveNumber, 0);
    }

    /** The damage one boss special carries on this wave, before the encounter's own multiplier. */
    public float bossSpecialDamage(int waveNumber, int ascensionTier) {
        int wave = clampWave(waveNumber);
        return expectedHeroMaxHealth(wave)
            * interpolate(BOSS_SPECIAL_SHARE_ANCHOR_WAVES, BOSS_SPECIAL_SHARE_ANCHOR_VALUES, wave)
            * baseDamageScaleForTier(wave, ascensionTier)
            * ascensionDamageCharge(wave, ascensionTier);
    }

    public void applyToRegularEnemy(Enemy enemy, EnemyType type, int waveNumber) {
        applyToRegularEnemy(enemy, type, waveNumber, 0);
    }

    public void applyToRegularEnemy(Enemy enemy, EnemyType type, int waveNumber, int ascensionTier) {
        float health = regularHealth(type, waveNumber, ascensionTier);
        enemy.health = health;
        enemy.maxHealth = health;
        enemy.damage = regularDamage(type, waveNumber, ascensionTier);
    }

    public void applyToBoss(Enemy boss, int waveNumber) {
        applyToBoss(boss, waveNumber, 0);
    }

    public void applyToBoss(Enemy boss, int waveNumber, int ascensionTier) {
        boss.health = bossHealth(waveNumber, ascensionTier);
        boss.maxHealth = boss.health;
        boss.damage = bossDamage(waveNumber, ascensionTier);
    }

    private static int clampWave(int waveNumber) {
        return Math.max(1, Math.min(GameState.FINAL_WAVE, waveNumber));
    }
}
