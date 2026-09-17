package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.balance.BalanceReport;
import com.amirrezahadipoor.herodefense.balance.WaveSample;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.rewards.RewardCardId;
import com.amirrezahadipoor.herodefense.trials.TrialId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 26.1a: the ascension regression gate. The tier-0 bands do not transfer
 * to tiers (measured: the 5-15% / 35% / 120 s window is empty above tier 0 —
 * cooling spikes below 35% pushes tier-10 averages under the 5% floor), so 26.1
 * re-derives every band from the full game instead of naively re-running them:
 * averages stay strict (5-15% naked, 3.5%+ empowered) while spike ceilings and
 * pressured floors index by tier. Full-pair and full-card matrices stay tier-0;
 * this gate guards the tier axes with a naked 9-seed matrix, a forced-card spot
 * (every card at boss 20), and a trial-pair spot (power, damage, and horde
 * pairs) at tiers 0/3/6/10.
 */
/**
 * Tagged {@code balance}: this suite sweeps whole runs, so it is the {@code :core:balanceGate} task's work and not
 * part of the fast unit loop (roadmap R4.5). The gate is what CI runs on every push; the tag only decides which
 * task pays for it, never whether it runs.
 */
@Tag("balance")
final class AscensionGateTest {
    private static final long BASELINE_SEED = 0x4845524F444546L;
    private static final long TRIAL_SEED = 0x747269616C7331L;
    private static final int[] TIERS = {0, 3, 6, 10};
    private static final float MINIMUM_NAKED_AVERAGE = 0.05f;
    /**
     * The average-pressure ceiling is tier-indexed (roadmap R4.7), the way the spike and clear ceilings above it
     * already were, and for the same reason: a tier is a harder game chosen by a player who has finished the last
     * one. The flat 0.15 was written when the ladder charged almost nothing -- before R4.7 the optimiser's average
     * at tier 10 was 0.088, and the ceiling only ever had to catch a walkover's opposite. Now that the ladder pays
     * for its reward (a counter-cyclical base charge on enemy health and damage), the shipped tier-10 average is
     * 0.1643: the ceiling moves to 0.15 + 0.02 per tier, which is the same slope the spike ceiling uses, while the
     * floor stays at 0.05 for every tier and the run still has to finish every seed -- the wall this ceiling exists
     * to catch is `reachedFinalWave`, and it is asserted separately.
     */
    private static float nakedAverageCeiling(int tier) {
        return 0.15f + 0.02f * tier;
    }
    private static final float MINIMUM_EMPOWERED_AVERAGE = 0.035f;
    private static final float MINIMUM_EMPOWERED_CLEAR_SECONDS = 24f;
    /**
     * Absolute single-wave backstop at every tier: spikes are seed noise (one wave
     * in 200), so tiers gate their median spike, but no wave may ever exceed gross
     * damage equal to the whole health pool.
     */
    private static final float MAXIMUM_SPIKE_ANY_SEED = 1.00f;

    private static float nakedMaxDamageCeiling(int tier) {
        return 0.40f + 0.02f * tier;
    }

    private static float clearCeiling(int tier) {
        return 120f + 3f * tier;
    }

    private static float trialMaxDamageCeiling(int tier) {
        return 0.40f + 0.04f * tier;
    }

    private static int trialPressuredFloor(int tier) {
        return switch (tier) {
            case 0 -> 175;
            case 3 -> 160;
            case 6 -> 145;
            case 10 -> 120;
            default -> throw new IllegalArgumentException("Ungated tier " + tier);
        };
    }

    @Test
    void nakedRunsStayInBandAtEveryGatedTier() {
        long[] seeds = {
            BASELINE_SEED, BASELINE_SEED + 1, BASELINE_SEED + 2, BASELINE_SEED + 3,
            BASELINE_SEED + 4, BASELINE_SEED + 5, BASELINE_SEED + 6, BASELINE_SEED + 7,
            0x123456789L,
        };
        System.out.println("seed,tier,finished,avg,max,maxclr");
        System.out.println("tier,median_max,median_maxclr");
        for (int tier : TIERS) {
            List<Float> maxima = new ArrayList<>();
            List<Float> maxClears = new ArrayList<>();
            for (long seed : seeds) {
                BalanceReport report = new BalanceSimulator().runWithAscensionTier(seed, tier);
                Summary summary = summarize(report.waves());
                System.out.println(seed + "," + tier + "," + report.reachedFinalWave() + ","
                    + summary.averageDamage + "," + summary.maximumDamage + ","
                    + summary.maximumClear);
                String cell = "seed " + seed + " tier " + tier;
                assertTrue(report.reachedFinalWave(), cell + " must finish");
                assertTrue(summary.averageDamage >= MINIMUM_NAKED_AVERAGE
                    && summary.averageDamage <= nakedAverageCeiling(tier),
                    cell + " average was " + summary.averageDamage);
                assertTrue(summary.maximumDamage <= MAXIMUM_SPIKE_ANY_SEED,
                    cell + " max spike was " + summary.maximumDamage);
                maxima.add(summary.maximumDamage);
                maxClears.add(summary.maximumClear);
            }
            float medianMax = medianFloat(maxima.stream().sorted().toList());
            float medianMaxClear = medianFloat(maxClears.stream().sorted().toList());
            System.out.println(tier + "," + medianMax + "," + medianMaxClear);
            assertTrue(medianMax <= nakedMaxDamageCeiling(tier),
                "tier " + tier + " median max spike was " + medianMax);
            assertTrue(medianMaxClear <= clearCeiling(tier),
                "tier " + tier + " median max clear was " + medianMaxClear);
        }
    }

    @Test
    void forcedCardsStayInBandAtEveryGatedTier() {
        // Single-seed maxima are noise (one wave in 200), so the card spot gates
        // 3-seed medians like the naked and trial spots, with the same backstop.
        System.out.println("card,tier,seed,finished,avg,max,maxclr");
        System.out.println("card,tier,median_max,median_maxclr");
        for (RewardCardId card : RewardCardId.values()) {
            for (int tier : TIERS) {
                List<Float> maxima = new ArrayList<>();
                List<Float> maxClears = new ArrayList<>();
                for (long s = 0; s < 5; s++) {
                    BalanceReport report = new BalanceSimulator()
                        .runWithForcedCardAndTier(BASELINE_SEED + s, card, 20, tier);
                    Summary summary = summarize(report.waves());
                    System.out.println(card + "," + tier + "," + (BASELINE_SEED + s) + ","
                        + report.reachedFinalWave() + "," + summary.averageDamage + ","
                        + summary.maximumDamage + "," + summary.maximumClear);
                    String cell = card + " tier " + tier + " seed " + (BASELINE_SEED + s);
                    assertTrue(report.reachedFinalWave(), cell + " must finish");
                    assertTrue(summary.averageDamage >= MINIMUM_NAKED_AVERAGE
                        && summary.averageDamage <= nakedAverageCeiling(tier),
                        cell + " average was " + summary.averageDamage);
                    assertTrue(summary.maximumDamage <= MAXIMUM_SPIKE_ANY_SEED,
                        cell + " max spike was " + summary.maximumDamage);
                    maxima.add(summary.maximumDamage);
                    maxClears.add(summary.maximumClear);
                }
                float medianMax = medianFloat(maxima.stream().sorted().toList());
                float medianMaxClear = medianFloat(maxClears.stream().sorted().toList());
                System.out.println(card + "," + tier + "," + medianMax + "," + medianMaxClear);
                String cell = card + " tier " + tier;
                assertTrue(medianMax <= nakedMaxDamageCeiling(tier),
                    cell + " median max spike was " + medianMax);
                assertTrue(medianMaxClear <= clearCeiling(tier),
                    cell + " median max clear was " + medianMaxClear);
            }
        }
    }

    @Test
    void trialPairsStayInBandAtEveryGatedTier() {
        TrialId[][] pairs = {
            {TrialId.DRY_VEINS, TrialId.HEAVY_CROWNS},
            {TrialId.BLOOD_PRICE, TrialId.HOLLOW_CALLING},
            {TrialId.IRON_TIDE, TrialId.STONE_SKIN},
        };
        System.out.println("pair,tier,finished_runs,median_avg,median_clear,"
            + "median_pressured,median_max,median_maxclr");
        List<String> failures = new ArrayList<>();
        for (TrialId[] pair : pairs) {
            for (int tier : TIERS) {
                List<Summary> finished = new ArrayList<>();
                // Five seeds, not three: see the note in TrialSimulationTest. The naked matrix above has always
                // sampled nine; three was the coin flip this phase measured.
                for (long s = 0; s < 5; s++) {
                    BalanceReport report = new BalanceSimulator()
                        .runWithTrialsAndTier(TRIAL_SEED + s, pair[0], pair[1], tier);
                    if (report.reachedFinalWave()
                        && report.waves().size() == GameState.FINAL_WAVE) {
                        finished.add(summarize(report.waves()));
                    }
                }
                String cell = pair[0] + "+" + pair[1] + " tier " + tier;
                if (finished.size() < 2) {
                    failures.add(cell + " finished only " + finished.size() + "/3 seeds");
                    continue;
                }
                Summary median = medians(finished);
                System.out.println(pair[0] + "+" + pair[1] + "," + tier + "," + finished.size()
                    + "/3," + median.averageDamage + "," + median.averageClear + ","
                    + median.pressured + "," + median.maximumDamage + "," + median.maximumClear);
                check(failures, median.averageDamage >= MINIMUM_EMPOWERED_AVERAGE,
                    cell + " trivialized median pressure: " + median.averageDamage);
                check(failures, median.averageClear >= MINIMUM_EMPOWERED_CLEAR_SECONDS,
                    cell + " trivialized median clear time: " + median.averageClear);
                check(failures, median.pressured >= trialPressuredFloor(tier),
                    cell + " left too few pressured waves: " + median.pressured);
                check(failures, median.maximumDamage <= trialMaxDamageCeiling(tier),
                    cell + " caused a damage spike: " + median.maximumDamage);
                check(failures, median.maximumClear <= clearCeiling(tier),
                    cell + " caused a clear-time spike: " + median.maximumClear);
            }
        }
        assertTrue(failures.isEmpty(), "Breaking tier cells:\n" + String.join("\n", failures));
    }

    /**
     * A tier may not buy its difficulty with padded length, and since R4.7 it may not be free either.
     *
     * The rule this replaces was written in Phase 26.2b, when a tier's only cost was a per-wave growth bump a fifth
     * of a percent wide: "added challenge must come from build precision, not padded wave count -- every tier's
     * median run clears within +-20% of tier 0's". The ladder now charges for its reward in the waves it rewards
     * (see DifficultyCurve's base charge), and that charge is paid in seconds as well as in blood, because enemy
     * health is what makes a wave take longer. So the budget is tier-indexed the way every other ceiling in this
     * gate already is, and the literal thing the old rule was guarding is asserted directly instead: the wave count
     * is {@link GameState#FINAL_WAVE} for every tier, so no tier can pad its difficulty with more waves at all. The
     * number below is a measurement, not a taste: on the three seeds the gate uses, the mean session total drifts
     * from tier 0's by +0.11 at tier 1, +0.24 at tier 2, +0.15 at tier 3, +0.25 at tier 6, +0.28 at tier 8 and
     * +0.32 at tier 10, against a budget that reaches 0.24 / 0.28 / 0.32 / 0.44 / 0.52 and 0.60. The tightest tier
     * in the shipped table is tier 2, which spends 84% of its budget; the charge costs about three percent of a
     * session per tier and is allowed to.
     *
     * The mean of the three seeds is what is compared, because with three samples the median is simply the middle
     * seed: tier-10 sessions spread from +12% to +65% inside one tier, and the middle of three such numbers is not
     * an estimate of anything.
     */
    @Test
    void sessionTimesStayWithinTheBudgetTheLadderPaysFor() {
        System.out.println("tier,seed,total_s");
        System.out.println("tier,mean_total_s,delta_fraction,budget");
        float tierZeroMean = 0f;
        for (int tier = 0; tier <= 10; tier++) {
            List<Float> totals = new ArrayList<>();
            for (long s = 0; s < 3; s++) {
                BalanceReport report = new BalanceSimulator()
                    .runWithAscensionTier(BASELINE_SEED + s, tier);
                assertTrue(report.reachedFinalWave(),
                    "tier " + tier + " seed " + (BASELINE_SEED + s) + " must finish");
                assertEquals(GameState.FINAL_WAVE, report.waves().size(),
                    "a tier's difficulty is charged inside the run, never added to it: tier " + tier
                        + " seed " + (BASELINE_SEED + s) + " ran a different number of waves");
                float total = 0f;
                for (WaveSample sample : report.waves()) total += sample.clearTimeSeconds();
                System.out.println(tier + "," + (BASELINE_SEED + s) + "," + total);
                totals.add(total);
            }
            float mean = 0f;
            for (float total : totals) mean += total / totals.size();
            if (tier == 0) tierZeroMean = mean;
            float delta = (mean - tierZeroMean) / tierZeroMean;
            float budget = sessionBudget(tier);
            System.out.println(tier + "," + mean + "," + delta + "," + budget);
            assertTrue(delta <= budget,
                "tier " + tier + " mean session " + mean + "s grew " + delta
                    + " over tier 0's " + tierZeroMean + "s, past its " + budget + " budget");
            assertTrue(delta >= -0.20f,
                "tier " + tier + " mean session " + mean + "s is " + delta + " of tier 0's " + tierZeroMean
                    + "s: a tier that is *shorter* than tier 0 is a tier that charges nothing");
        }
    }

    /** The seconds a tier may spend on its charge: 20% of a session, plus 4% per tier of ladder. */
    private static float sessionBudget(int tier) {
        return 0.20f + 0.04f * tier;
    }

    private static void check(List<String> failures, boolean condition, String message) {
        if (!condition) failures.add(message);
    }

    private record Summary(
        float averageDamage,
        float averageClear,
        long pressured,
        float maximumDamage,
        float maximumClear
    ) {
    }

    private static Summary summarize(List<WaveSample> waves) {
        float averageDamage = 0f;
        float averageClear = 0f;
        long pressured = 0;
        float maximumDamage = 0f;
        float maximumClear = 0f;
        for (WaveSample sample : waves) {
            averageDamage += sample.damageFraction();
            averageClear += sample.clearTimeSeconds();
            if (sample.damageFraction() >= 0.01f) pressured++;
            maximumDamage = Math.max(maximumDamage, sample.damageFraction());
            maximumClear = Math.max(maximumClear, sample.clearTimeSeconds());
        }
        return new Summary(
            averageDamage / waves.size(), averageClear / waves.size(), pressured,
            maximumDamage, maximumClear);
    }

    private static Summary medians(List<Summary> runs) {
        return new Summary(
            medianFloat(runs.stream().map(Summary::averageDamage).sorted().toList()),
            medianFloat(runs.stream().map(Summary::averageClear).sorted().toList()),
            medianLong(runs.stream().map(Summary::pressured).sorted().toList()),
            medianFloat(runs.stream().map(Summary::maximumDamage).sorted().toList()),
            medianFloat(runs.stream().map(Summary::maximumClear).sorted().toList()));
    }

    private static float medianFloat(List<Float> sorted) {
        return sorted.get(sorted.size() / 2);
    }

    private static long medianLong(List<Long> sorted) {
        return sorted.get(sorted.size() / 2);
    }
}
