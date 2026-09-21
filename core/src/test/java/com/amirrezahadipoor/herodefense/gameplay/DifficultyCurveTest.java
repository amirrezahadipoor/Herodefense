package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

final class DifficultyCurveTest {
    private final DifficultyCurve curve = new DifficultyCurve();

    @Test
    void regularHealthUsesTheSimulationTunedWaveFormulaAndTypeMultiplier() {
        assertEquals(
            DifficultyCurve.BASE_ENEMY_HEALTH * DifficultyCurve.ENEMY_HEALTH_GROWTH,
            curve.baselineRegularHealth(1),
            0.0001f
        );
        assertEquals(
            curve.baselineRegularHealth(25) * EnemyType.STONEKIN.baseHealth() / 20f,
            curve.regularHealth(EnemyType.STONEKIN, 25),
            0.001f
        );
        assertTrue(curve.baselineRegularHealth(100) > curve.baselineRegularHealth(50));
    }

    /**
     * The property the deleted 28% clamp used to protect, kept as a test instead of a branch (audit item 1).
     *
     * <p>The clamp never fired once in two hundred waves -- the damage it was guarding against was 0.095% of the
     * bar, not 28% -- so it was dead code pretending to be a safety rail. What matters is the live property: at
     * every wave, every archetype's hit is a bounded share of the bar the hero is expected to be carrying, and the
     * reference archetype's share is exactly the published curve.
     */
    @Test
    void everyHitIsABoundedShareOfTheExpectedBar() {
        for (int wave = 1; wave <= GameState.FINAL_WAVE; wave++) {
            for (EnemyType type : EnemyType.values()) {
                float share = curve.regularDamage(type, wave) / DifficultyCurve.expectedHeroMaxHealth(wave);
                assertTrue(share <= 0.08f,
                    type + " hits for " + (share * 100f) + "% of the bar on wave " + wave);
                assertTrue(share > 0f, "and the hit is never nothing");
            }
            float reference = curve.regularDamage(EnemyType.ROOTLING, wave)
                / DifficultyCurve.expectedHeroMaxHealth(wave);
            assertEquals(DifficultyCurve.damageShareOfExpectedBar(wave), reference, 0.0001f,
                "the reference archetype carries the published share on wave " + wave);
        }
    }

    @Test
    void theAnchorTablesAreWellFormedAndTheShareFallsAcrossTheRun() {
        assertEquals(DifficultyCurve.EXPECTED_BAR_ANCHOR_WAVES.length,
            DifficultyCurve.EXPECTED_BAR_ANCHOR_VALUES.length);
        assertEquals(DifficultyCurve.DAMAGE_SHARE_ANCHOR_WAVES.length,
            DifficultyCurve.DAMAGE_SHARE_ANCHOR_VALUES.length);
        for (int index = 1; index < DifficultyCurve.EXPECTED_BAR_ANCHOR_WAVES.length; index++) {
            assertTrue(DifficultyCurve.EXPECTED_BAR_ANCHOR_WAVES[index]
                > DifficultyCurve.EXPECTED_BAR_ANCHOR_WAVES[index - 1], "anchor waves ascend");
            assertTrue(DifficultyCurve.EXPECTED_BAR_ANCHOR_VALUES[index]
                > DifficultyCurve.EXPECTED_BAR_ANCHOR_VALUES[index - 1], "and so does the bar");
        }
        for (int index = 1; index < DifficultyCurve.DAMAGE_SHARE_ANCHOR_WAVES.length; index++) {
            assertTrue(DifficultyCurve.DAMAGE_SHARE_ANCHOR_WAVES[index]
                > DifficultyCurve.DAMAGE_SHARE_ANCHOR_WAVES[index - 1], "anchor waves ascend");
            assertTrue(DifficultyCurve.DAMAGE_SHARE_ANCHOR_VALUES[index]
                < DifficultyCurve.DAMAGE_SHARE_ANCHOR_VALUES[index - 1],
                "the share falls: a late wave lands more hits, so each one may be worth less of the bar");
        }
        // Interpolation, endpoints and the flat tail the ramp-in clamps to.
        assertEquals(100f, DifficultyCurve.expectedHeroMaxHealth(1), 0.001f);
        assertEquals(863f, DifficultyCurve.expectedHeroMaxHealth(GameState.FINAL_WAVE), 0.01f);
        assertEquals(DifficultyCurve.expectedHeroMaxHealth(1), DifficultyCurve.expectedHeroMaxHealth(0), 0.001f);
        assertEquals(DifficultyCurve.expectedHeroMaxHealth(GameState.FINAL_WAVE),
            DifficultyCurve.expectedHeroMaxHealth(500), 0.001f);
        assertEquals(166f, DifficultyCurve.expectedHeroMaxHealth(10), 0.01f);
    }

    @Test
    void theTierChargeOnDamageIsOneAtTierZeroAndBitesDeeperWithEveryTier() {
        float previous = 0f;
        for (int tier : new int[] {0, 1, 3, 6, 10}) {
            float charge = DifficultyCurve.ascensionDamageCharge(GameState.FINAL_WAVE, tier);
            assertTrue(charge >= previous, "tier " + tier + " is not lighter than the tier below it");
            previous = charge;
        }
        assertEquals(1f, DifficultyCurve.ascensionDamageCharge(1, 10), 0.000001f,
            "the charge compounds from wave one, so wave one pays nothing");
        assertEquals(1f, DifficultyCurve.ascensionDamageCharge(GameState.FINAL_WAVE, 0), 0f);
        assertTrue(curve.baselineRegularDamage(GameState.FINAL_WAVE, 10)
            > curve.baselineRegularDamage(GameState.FINAL_WAVE, 0) * 1.5f,
            "and a ten-tier ladder is more than half again as hard by the last wave");
    }

    @Test
    void bossesUseFifteenTimesBaselineHpAndThreeTimesBaselineDamage() {
        int wave = 50;
        assertEquals(
            curve.baselineRegularHealth(wave) * 15f,
            curve.bossHealth(wave),
            0.001f
        );
        assertEquals(
            curve.baselineRegularDamage(wave) * 3f,
            curve.bossDamage(wave),
            0.001f
        );
        assertTrue(curve.bossHealth(wave) > curve.regularHealth(EnemyType.FUNGAL_BRUTE, wave));
        assertTrue(curve.bossDamage(wave) > curve.regularDamage(EnemyType.FUNGAL_BRUTE, wave));
    }

    /**
     * The one telegraphed hit in the game, priced as a share of the bar (audit item 1's other half).
     *
     * <p>A special used to be "three times a regular hit", which made the loudest warning in the game worth 0.46% of
     * the bar by wave 200 -- a telegraph the player learns to ignore. Anchored to the bar instead, the golem's 1.6x
     * special takes 7.2% of it on wave one and 12% on wave two hundred, and every boss's special is worth at least
     * four regular hits of the same wave, because a hit the game warns about has to be worth reading. The ceiling is
     * what keeps a telegraph from being a death sentence: even the golem leaves seven eighths of the bar standing,
     * and that is before the brace, which multiplies it by 0.4 like any other hit.
     */
    @Test
    void theTelegraphedSpecialIsWorthReadingAndNeverUnpayable() {
        for (int wave : new int[] {1, 5, 40, 100, 200}) {
            float bar = DifficultyCurve.expectedHeroMaxHealth(wave);
            float special = curve.bossSpecialDamage(wave);
            assertTrue(special <= 0.10f * bar,
                "the special's base is " + (special / bar * 100f) + "% of the bar on wave " + wave);
            assertTrue(special * 1.6f <= 0.15f * bar,
                "the heaviest encounter multiplier takes " + (special * 1.6f / bar * 100f)
                    + "% of the bar on wave " + wave);
            assertTrue(special >= 4f * curve.baselineRegularDamage(wave),
                "a telegraphed special is worth more than four contact hits on wave " + wave);
            assertTrue(special * 1.6f > curve.baselineRegularDamage(wave) * 8f,
                "and the golem's is worth more than eight, so the encounter the player meets first is the loudest");
        }
        assertTrue(curve.bossSpecialDamage(GameState.FINAL_WAVE) > curve.bossSpecialDamage(100),
            "the telegraphed hit grows in points across the run");
        assertEquals(curve.bossSpecialDamage(50), curve.bossSpecialDamage(50, 0), 0f,
            "tier zero is bit-identical, so every tier-0 gate keeps measuring the shipped game");
        assertTrue(curve.bossSpecialDamage(50, 10) > curve.bossSpecialDamage(50, 0),
            "and a deeper ladder charges for it in points as well");
    }

    @Test
    void ascensionScheduleIsBitIdenticalAtTierZero() {
        assertEquals(DifficultyCurve.ENEMY_HEALTH_GROWTH, DifficultyCurve.healthGrowthForTier(0), 0.0f);
        assertEquals(
            DifficultyCurve.SECOND_HALF_HEALTH_GROWTH, DifficultyCurve.secondHalfHealthGrowthForTier(0), 0.0f);
        assertEquals(
            DifficultyCurve.FINAL_QUARTER_HEALTH_GROWTH, DifficultyCurve.finalQuarterHealthGrowthForTier(0), 0.0f);
    }

    @Test
    void ascensionBumpsScaleMultiplicativelyAndMonotonically() {
        assertEquals(
            DifficultyCurve.ENEMY_HEALTH_GROWTH * (1f + DifficultyCurve.ASCENSION_HEALTH_BUMP_PER_TIER * 10),
            DifficultyCurve.healthGrowthForTier(10),
            0.000001f
        );
        int[] tiers = {0, 1, 3, 6, 10};
        for (int i = 1; i < tiers.length; i++) {
            assertTrue(DifficultyCurve.healthGrowthForTier(tiers[i])
                > DifficultyCurve.healthGrowthForTier(tiers[i - 1]));
            assertTrue(DifficultyCurve.ascensionDamageCharge(GameState.FINAL_WAVE, tiers[i])
                > DifficultyCurve.ascensionDamageCharge(GameState.FINAL_WAVE, tiers[i - 1]));
        }
        assertEquals(
            DifficultyCurve.healthGrowthForTier(0), DifficultyCurve.healthGrowthForTier(-4), 0.0f);
        assertEquals(
            DifficultyCurve.ascensionDamageCharge(GameState.FINAL_WAVE, 0),
            DifficultyCurve.ascensionDamageCharge(GameState.FINAL_WAVE, -4), 0.0f);
    }

    /**
     * R4.7's charge, pinned as arithmetic rather than as a vibe: tier 0 is bit-identical (so every tier-0 gate in
     * the repository keeps measuring the shipped game), the charge is heaviest on the first wave of the run and fades
     * to nothing by the end of its span, and it lands on the baseline rather than on a growth rate -- which is what
     * makes it pay for the tier's flat starting power in the waves where that power is worth the most.
     */
    @Test
    void theAscensionBaseChargeIsBitIdenticalAtTierZeroAndFadesAcrossItsSpan() {
        int span = DifficultyCurve.ASCENSION_BASE_CHARGE_SPAN_WAVES;
        assertTrue(span > 1, "the charge has to have a span to fade across");
        for (int wave : new int[] {1, 25, span / 2, span, GameState.FINAL_WAVE}) {
            assertEquals(1f, DifficultyCurve.baseScaleForTier(wave, 0), 0.0f, "tier 0 pays no base charge");
            assertEquals(1f, DifficultyCurve.baseDamageScaleForTier(wave, 0), 0.0f);
        }
        assertEquals(1f + DifficultyCurve.ASCENSION_BASE_HEALTH_BUMP_PER_TIER * 10f,
            DifficultyCurve.baseScaleForTier(1, 10), 0.000001f, "tier 10 pays full price on wave one");
        assertEquals(1f + DifficultyCurve.ASCENSION_BASE_DAMAGE_BUMP_PER_TIER * 10f,
            DifficultyCurve.baseDamageScaleForTier(1, 10), 0.000001f);
        assertEquals(1f + DifficultyCurve.ASCENSION_BASE_HEALTH_BUMP_PER_TIER * 10f / span,
            DifficultyCurve.baseScaleForTier(span, 10), 0.000001f,
            "the fade is linear: the last wave of the span carries one span'th of the charge");
        assertEquals(1f + DifficultyCurve.ASCENSION_BASE_DAMAGE_BUMP_PER_TIER * 10f / span,
            DifficultyCurve.baseDamageScaleForTier(span, 10), 0.000001f);
        assertEquals(1f, DifficultyCurve.baseScaleForTier(span + 1, 10), 0.0f, "and then it is gone exactly");
        assertEquals(1f, DifficultyCurve.baseScaleForTier(GameState.FINAL_WAVE, 10), 0.0f);
        float previous = Float.MAX_VALUE;
        for (int wave = 1; wave <= GameState.FINAL_WAVE; wave++) {
            float scale = DifficultyCurve.baseScaleForTier(wave, 10);
            assertTrue(scale <= previous, "the fade is monotone: wave " + wave + " charged more than the last");
            previous = scale;
        }
        DifficultyCurve curve = new DifficultyCurve();
        float endRatio = curve.baselineRegularHealth(GameState.FINAL_WAVE, 10)
            / curve.baselineRegularHealth(GameState.FINAL_WAVE, 0);
        assertEquals((float) Math.pow(1f + DifficultyCurve.ASCENSION_HEALTH_BUMP_PER_TIER * 10f,
                GameState.FINAL_WAVE), endRatio, 0.01f,
            "the last wave keeps only the per-wave growth bump: the base charge has faded out entirely");
        assertTrue(curve.baselineRegularHealth(1, 10) > curve.baselineRegularHealth(1, 0) * 3f,
            "and the first wave is more than three times the tier-0 enemy -- the charge is on the baseline the game reads");
        assertTrue(curve.baselineRegularDamage(1, 10) > curve.baselineRegularDamage(1, 0) * 2f,
            "damage pays part of it too");
    }

    @Test
    void bothSecondHalfSpansKeepTheSameRelativeBump() {
        for (int tier : new int[] {1, 3, 6, 10}) {
            assertEquals(
                DifficultyCurve.healthGrowthForTier(tier) / DifficultyCurve.healthGrowthForTier(0),
                DifficultyCurve.secondHalfHealthGrowthForTier(tier)
                    / DifficultyCurve.secondHalfHealthGrowthForTier(0),
                0.000001f
            );
            assertEquals(
                DifficultyCurve.healthGrowthForTier(tier) / DifficultyCurve.healthGrowthForTier(0),
                DifficultyCurve.finalQuarterHealthGrowthForTier(tier)
                    / DifficultyCurve.finalQuarterHealthGrowthForTier(0),
                0.000001f
            );
        }
    }

    @Test
    void theOpeningWavesKeepTheBaseHealthFormulaAndCarryTheAnchoredHit() {
        assertEquals(
            DifficultyCurve.BASE_ENEMY_HEALTH * Math.pow(DifficultyCurve.ENEMY_HEALTH_GROWTH, 24),
            curve.baselineRegularHealth(24),
            0.001f
        );
        assertEquals(
            DifficultyCurve.BASE_ENEMY_HEALTH * DifficultyCurve.ENEMY_HEALTH_GROWTH,
            curve.baselineRegularHealth(1),
            0.0001f
        );
        // The B1 anchor, pinned the same way the health formula is: wave 24's bar is 244.4 and its share of that
        // bar is 0.00258286, so the hit is 0.63125 points. Recomputed from the tables, never carried over.
        assertEquals(244.4f, DifficultyCurve.expectedHeroMaxHealth(24), 0.01f);
        assertEquals(0.00258286f, DifficultyCurve.damageShareOfExpectedBar(24), 0.00000001f);
        assertEquals(0.63125f, curve.baselineRegularDamage(24), 0.0001f);
        assertEquals(0.75f, curve.baselineRegularDamage(1), 0.0001f);
    }

    @Test
    void middleSegmentCompoundsItsOwnRateAcrossWavesTwentyFiveToEighty() {
        assertEquals(
            Math.pow(DifficultyCurve.MIDDLE_HEALTH_GROWTH, 56),
            curve.baselineRegularHealth(80) / curve.baselineRegularHealth(24),
            0.0001f
        );
        assertTrue(curve.baselineRegularDamage(80) / curve.baselineRegularDamage(24) > 1.2f,
            "the middle span is still where hit points climb fastest");
        assertTrue(curve.baselineRegularHealth(25) > curve.baselineRegularHealth(24));
        assertTrue(curve.baselineRegularHealth(80) > curve.baselineRegularHealth(79));
        assertTrue(curve.baselineRegularDamage(25) > curve.baselineRegularDamage(24));
        assertTrue(curve.baselineRegularDamage(80) > curve.baselineRegularDamage(79));
    }

    @Test
    void lateFirstHalfResumesTheBaseRateFromTheHotterWaveEightyValue() {
        assertEquals(
            Math.pow(DifficultyCurve.ENEMY_HEALTH_GROWTH, 20),
            curve.baselineRegularHealth(100) / curve.baselineRegularHealth(80),
            0.0001f
        );
        assertTrue(curve.baselineRegularDamage(100) > curve.baselineRegularDamage(80),
            "and hit points keep climbing on the base rate");
        assertTrue(curve.baselineRegularHealth(81) > curve.baselineRegularHealth(80));
        assertTrue(curve.baselineRegularDamage(81) > curve.baselineRegularDamage(80));
    }

    @Test
    void middleSegmentTakesTheSameRelativeAscensionBumpAsTheBaseRate() {
        assertEquals(
            DifficultyCurve.MIDDLE_HEALTH_GROWTH, DifficultyCurve.middleHealthGrowthForTier(0), 0.0f);
        assertEquals(
            DifficultyCurve.MIDDLE_HEALTH_GROWTH
                * (1f + DifficultyCurve.ASCENSION_HEALTH_BUMP_PER_TIER * 10),
            DifficultyCurve.middleHealthGrowthForTier(10),
            0.000001f
        );
        assertEquals(
            DifficultyCurve.healthGrowthForTier(6) / DifficultyCurve.healthGrowthForTier(0),
            DifficultyCurve.middleHealthGrowthForTier(6) / DifficultyCurve.middleHealthGrowthForTier(0),
            0.000001f
        );
    }

    @Test
    void waveSpawnerAppliesCurrentWaveStats() {
        GameState state = GameState.newRun(77L);
        EnemyWaveSpawner spawner = new EnemyWaveSpawner(new EnemyFactory());
        spawner.spawnRegularEnemies(state, 40, 3);
        Enemy first = state.aliveEnemies.get(0);
        EnemyType type = first.type();
        assertEquals(curve.regularHealth(type, 40), first.maxHealth, 0.001f);
        assertEquals(curve.regularDamage(type, 40), first.damage, 0.001f);
    }
}
