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
     * One policy's frozen shape. Measured on the shipped curve (three seeds, two hundred waves):
     * the optimiser spikes to 0.30, slides at most 4 waves, finds 13 valleys and never stalls; the naive
     * player spikes to 1.09 (a wave can cost a passive hero almost their whole bar -- and B1's pity is what
     * keeps that from being the last wave), slides at most 13, finds 3 valleys and stalls on ~1.5% of waves.
     */
    private record Bands(float maxSpike, int maxDecline, int minBreathers, float maxStall) {
    }

    private static final Bands OPTIMISER_BANDS = new Bands(0.35f, 6, 10, 0f);
    private static final Bands NAIVE_BANDS = new Bands(1.25f, 16, 2, 0.03f);

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

    @Test
    void skillBuysAMeasurableMargin() {
        float skilled = 0f;
        float unskilled = 0f;
        for (long seed : SEEDS) {
            skilled += new BalanceSimulator().runWithPolicy(seed, BalanceSimulator.Policy.OPTIMISER)
                .averageDamageFraction();
            unskilled += new BalanceSimulator().runWithPolicy(seed, BalanceSimulator.Policy.NAIVE)
                .averageDamageFraction();
        }
        assertTrue(unskilled > skilled,
            "the same curve must cost the passive player more than the skilled one: naive averaged "
                + unskilled / SEEDS.length + ", optimiser " + skilled / SEEDS.length);
    }
}
