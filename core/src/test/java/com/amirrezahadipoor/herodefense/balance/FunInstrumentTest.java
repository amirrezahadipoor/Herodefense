package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The fun instrument's bands (roadmap C2). Every gate reads {@link FunMetrics} over full simulated runs of
 * both shipped policies, and the caps are per policy on purpose: the optimiser's numbers describe the CURVE
 * (the same one every other balance gate measures), while the naive player's numbers describe what the curve
 * costs somebody who never shops, never reforges and takes the first card offered. Those two experiences are
 * allowed to differ -- B1's pity and C3's brief-vigil floor exist so the naive run survives -- but neither may
 * leave the shape this instrument froze: no ambush jumps, no endless slides, valleys that exist but never run
 * together into a wall of nothing, and no wave the run stalls on.
 *
 * <p>Every cap below was measured from the shipped curve and then given margin; a cap that trips means the
 * curve changed shape or the instrument found what absolute bands cannot see. The human instrument (roadmap
 * C1) is what eventually recalibrates these against a player who can feel unfairness rather than count it.
 */
final class FunInstrumentTest {
    private static final long[] SEEDS = {0x5EED01L, 0x5EED02L, 0x5EED03L};

    /**
     * One policy's frozen shape, re-measured after audit item 1 re-anchored damage to the bar (three seeds, two
     * hundred waves): the optimiser's worst neighbour-wave jump is 0.42-0.55 against 0.30 before, slides at most
     * five waves, finds five to twelve valleys and never stalls; the naive player jumps 0.42-0.54, slides at most
     * five, finds three or four valleys and never stalls either -- it dies at wave 21, 37 and 46 instead, which is
     * the trade this instrument exists to record: the same curve that stopped being a walkover stopped leaving
     * free waves in it, and a passive player is now caught by wave 15 to 50 rather than chipping through thirty.
     */
    private record Bands(float maxSpike, int maxDecline, int minBreathers, float maxStall) {
    }

    private static final Bands OPTIMISER_BANDS = new Bands(0.65f, 6, 4, 0f);
    private static final Bands NAIVE_BANDS = new Bands(0.65f, 6, 2, 0.03f);

    /** Valleys running together longer than this is a wall of nothing -- boring, not safe. Measured at 4 for both policies on the shipped curve; same cap for both. */
    private static final int MAX_BREATHER_STREAK = 6;

    private static Bands bandsOf(BalanceSimulator.Policy policy) {
        return policy == BalanceSimulator.Policy.NAIVE ? NAIVE_BANDS : OPTIMISER_BANDS;
    }

    private static List<FunMetrics> measure(BalanceSimulator.Policy policy) {
        List<FunMetrics> all = new ArrayList<>();
        for (long seed : SEEDS) {
            all.add(FunMetrics.from(new BalanceSimulator().runWithPolicy(seed, policy)));
        }
        return all;
    }

    private static String report(List<FunMetrics> all) {
        StringBuilder text = new StringBuilder();
        for (FunMetrics metrics : all) {
            text.append(String.format(
                "%n  spike=%.4f decline=%d breathers=%d wall=%d stall=%.3f final=%.3f",
                metrics.worstSpike(), metrics.longestDeclineStreak(), metrics.breatherWaves(),
                metrics.longestBreatherStreak(), metrics.stallFraction(), metrics.finalHealthFraction()));
        }
        return text.toString();
    }

    @Test
    void difficultyNeverJumpsLikeAnAmbush() {
        for (BalanceSimulator.Policy policy : BalanceSimulator.Policy.values()) {
            Bands bands = bandsOf(policy);
            List<FunMetrics> all = measure(policy);
            for (FunMetrics metrics : all) {
                assertTrue(metrics.worstSpike() <= bands.maxSpike(),
                    policy + " hit a neighbour-wave difficulty jump of " + metrics.worstSpike()
                        + " (cap " + bands.maxSpike() + ")" + report(all));
            }
        }
    }

    @Test
    void pressureAlwaysLeavesARecoveryWindow() {
        for (BalanceSimulator.Policy policy : BalanceSimulator.Policy.values()) {
            Bands bands = bandsOf(policy);
            List<FunMetrics> all = measure(policy);
            for (FunMetrics metrics : all) {
                assertTrue(metrics.longestDeclineStreak() <= bands.maxDecline(),
                    policy + " slid " + metrics.longestDeclineStreak() + " waves with no recovery (cap "
                        + bands.maxDecline() + ")" + report(all));
            }
        }
    }

    @Test
    void thePacingHasValleysButNeverAWall() {
        for (BalanceSimulator.Policy policy : BalanceSimulator.Policy.values()) {
            Bands bands = bandsOf(policy);
            List<FunMetrics> all = measure(policy);
            for (FunMetrics metrics : all) {
                assertTrue(metrics.breatherWaves() >= bands.minBreathers(),
                    policy + " found only " + metrics.breatherWaves() + " breather waves (floor "
                        + bands.minBreathers() + ")" + report(all));
                assertTrue(metrics.longestBreatherStreak() <= MAX_BREATHER_STREAK,
                    policy + " ran " + metrics.longestBreatherStreak() + " empty waves together (cap "
                        + MAX_BREATHER_STREAK + ")" + report(all));
            }
        }
    }

    @Test
    void nobodyStallsOnAWaveTheyCannotKill() {
        for (BalanceSimulator.Policy policy : BalanceSimulator.Policy.values()) {
            Bands bands = bandsOf(policy);
            List<FunMetrics> all = measure(policy);
            for (FunMetrics metrics : all) {
                assertTrue(metrics.stallFraction() <= bands.maxStall(),
                    policy + " stalled on " + metrics.stallFraction() + " of waves (cap "
                        + bands.maxStall() + ")" + report(all));
            }
        }
    }

    /**
     * Skill buys a margin, and after B1 the margin is measured in waves rather than in pressure.
     *
     * <p>The old version of this test compared the two policies' average damage per wave and required the passive
     * player's to be larger. That comparison stopped meaning anything the moment damage was anchored to the bar: a
     * landed hit costs both policies the same share of the bar, so a policy's average pressure now measures the
     * curve, not the player -- measured after B1, the optimiser averaged 0.244-0.322 and the naive 0.235-0.333,
     * interleaved. What skill actually buys is <em>survival</em>: on these three seeds the passive player is caught
     * at wave 21, 37 and 46 while the skilled one finishes two hundred, and the gap is the margin this test pins.
     */
    @Test
    void skillBuysAMeasurableMargin() {
        int skilledReach = 0;
        int unskilledReach = 0;
        for (long seed : SEEDS) {
            skilledReach += new BalanceSimulator().runWithPolicy(seed, BalanceSimulator.Policy.OPTIMISER)
                .waves().size();
            unskilledReach += new BalanceSimulator().runWithPolicy(seed, BalanceSimulator.Policy.NAIVE)
                .waves().size();
        }
        assertTrue(unskilledReach * 2 < skilledReach,
            "the same curve must cost the passive player the run: naive reached " + unskilledReach / 2
                + " waves on average against " + skilledReach / 2 + " for the skilled one");
        for (long seed : SEEDS) {
            assertTrue(new BalanceSimulator().runWithPolicy(seed, BalanceSimulator.Policy.OPTIMISER)
                    .reachedFinalWave(),
                "seed " + seed + " must stay winnable by the policy that plays the game");
            assertTrue(!new BalanceSimulator().runWithPolicy(seed, BalanceSimulator.Policy.NAIVE)
                    .reachedFinalWave(),
                "seed " + seed + " must still catch the policy that ignores it");
        }
    }
}
