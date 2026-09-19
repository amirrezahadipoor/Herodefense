package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;

/** Central wave-number coefficients; renderer-independent for later simulations. */
public final class DifficultyCurve {
    public static final float BASE_ENEMY_HEALTH = 20f;
    public static final float ENEMY_HEALTH_GROWTH = 1.037f;
    public static final float BASE_ENEMY_DAMAGE = 0.27f;
    public static final float ENEMY_TYPE_REFERENCE_DAMAGE = 5f;
    public static final float ENEMY_DAMAGE_GROWTH = 1.003f;
    // The second half climbs in two spans (roadmap R4.6). Waves 101-150 -- the entry -- climb at
    // SECOND_HALF_*_GROWTH, hotter than the 1.023/1.008 the whole half used to run at, because the
    // step into the second half was the shallowest in the curve (x1.047 against the x1.71 before it).
    // Waves 151-200 -- the final quarter -- climb at FINAL_QUARTER_*_GROWTH, cooler than the old
    // tail, which is where the run's heaviest single waves sat. Together the two spans end the run
    // 9% lighter in health and 5% lighter in damage than the old single rate did, while the measured
    // quarter step rises to x1.165. Waves 1-100 read neither span, so the first half -- and every
    // brief-vigil and tier-0 gate -- stays bit-identical to the shipped curve.
    public static final float SECOND_HALF_HEALTH_GROWTH = 1.026f;
    public static final float SECOND_HALF_DAMAGE_GROWTH = 1.0085f;
    /** Last wave of the second half's hotter entry span; the final quarter owns the rest of the run. */
    public static final int SECOND_HALF_ENTRY_LAST_WAVE = 150;
    public static final float FINAL_QUARTER_HEALTH_GROWTH = 1.018f;
    public static final float FINAL_QUARTER_DAMAGE_GROWTH = 1.0045f;
    // Middle-third segment (Phase 25.3b): waves 25-80 grow slightly hotter than the
    // base first-half rate so pressure rises end to end instead of plateauing.
    // Tuned against the 5-15% / 35% / 120 s gate (see docs/BALANCE.md).
    public static final int MIDDLE_SEGMENT_FIRST_WAVE = 25;
    public static final int MIDDLE_SEGMENT_LAST_WAVE = 80;
    public static final float MIDDLE_HEALTH_GROWTH = 1.041f;
    public static final float MIDDLE_DAMAGE_GROWTH = 1.006f;
    public static final float BOSS_HEALTH_MULTIPLIER = 15f;
    public static final float BOSS_DAMAGE_MULTIPLIER = 3f;
    public static final float MAX_REASONABLE_HEALTH_FRACTION_PER_HIT = 0.28f;

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

    public static float damageGrowthForTier(int tier) {
        return ENEMY_DAMAGE_GROWTH * (1f + ASCENSION_DAMAGE_BUMP_PER_TIER * Math.max(0, tier));
    }

    public static float finalQuarterHealthGrowthForTier(int tier) {
        return FINAL_QUARTER_HEALTH_GROWTH * (1f + ASCENSION_HEALTH_BUMP_PER_TIER * Math.max(0, tier));
    }

    public static float finalQuarterDamageGrowthForTier(int tier) {
        return FINAL_QUARTER_DAMAGE_GROWTH * (1f + ASCENSION_DAMAGE_BUMP_PER_TIER * Math.max(0, tier));
    }

    /** Waves the second half spends at its entry rate before the final quarter's cooler climb. */
    public static int secondHalfEntryWaves() {
        return SECOND_HALF_ENTRY_LAST_WAVE - GameState.PLANTING_WAVE;
    }

    public static float secondHalfHealthGrowthForTier(int tier) {
        return SECOND_HALF_HEALTH_GROWTH * (1f + ASCENSION_HEALTH_BUMP_PER_TIER * Math.max(0, tier));
    }

    public static float secondHalfDamageGrowthForTier(int tier) {
        return SECOND_HALF_DAMAGE_GROWTH * (1f + ASCENSION_DAMAGE_BUMP_PER_TIER * Math.max(0, tier));
    }

    public static float middleHealthGrowthForTier(int tier) {
        return MIDDLE_HEALTH_GROWTH * (1f + ASCENSION_HEALTH_BUMP_PER_TIER * Math.max(0, tier));
    }

    public static float middleDamageGrowthForTier(int tier) {
        return MIDDLE_DAMAGE_GROWTH * (1f + ASCENSION_DAMAGE_BUMP_PER_TIER * Math.max(0, tier));
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

    public float baselineRegularDamage(int waveNumber, int ascensionTier) {
        int wave = clampWave(waveNumber);
        int firstHalf = Math.max(0, Math.min(wave, GameState.PLANTING_WAVE) - 1);
        int secondHalf = Math.max(0, wave - GameState.PLANTING_WAVE);
        // Damage grows as g^(wave-1): waves 1-24 contribute 23 base-rate units, so
        // the middle span (waves 25-80) owns damage units 24-79.
        int early = Math.min(firstHalf, MIDDLE_SEGMENT_FIRST_WAVE - 2);
        int middle = Math.min(
            Math.max(0, firstHalf - early), MIDDLE_SEGMENT_LAST_WAVE - MIDDLE_SEGMENT_FIRST_WAVE + 1);
        int late = Math.max(0, firstHalf - early - middle);
        int entry = Math.min(secondHalf, secondHalfEntryWaves());
        int finalQuarter = Math.max(0, secondHalf - entry);
        return BASE_ENEMY_DAMAGE
            * baseDamageScaleForTier(wave, ascensionTier)
            * (float) Math.pow(damageGrowthForTier(ascensionTier), early + late)
            * (float) Math.pow(middleDamageGrowthForTier(ascensionTier), middle)
            * (float) Math.pow(secondHalfDamageGrowthForTier(ascensionTier), entry)
            * (float) Math.pow(finalQuarterDamageGrowthForTier(ascensionTier), finalQuarter);
    }

    public float uncappedRegularDamage(EnemyType type, int waveNumber) {
        return uncappedRegularDamage(type, waveNumber, 0);
    }

    public float uncappedRegularDamage(EnemyType type, int waveNumber, int ascensionTier) {
        return baselineRegularDamage(waveNumber, ascensionTier)
            * type.baseDamage() / ENEMY_TYPE_REFERENCE_DAMAGE;
    }

    public float regularDamage(EnemyType type, int waveNumber) {
        return regularDamage(type, waveNumber, 0);
    }

    public float regularDamage(EnemyType type, int waveNumber, int ascensionTier) {
        return Math.min(
            uncappedRegularDamage(type, waveNumber, ascensionTier),
            reasonableHeroMaxHealth(waveNumber) * MAX_REASONABLE_HEALTH_FRACTION_PER_HIT
        );
    }

    public float reasonableHeroMaxHealth(int waveNumber) {
        int expectedHealthPoints = Math.max(0, clampWave(waveNumber) - 1) / 5;
        return 100f + expectedHealthPoints * 10f;
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
