package com.amirrezahadipoor.herodefense.balance;

import java.util.List;

/**
 * The machine-side fun instrument (roadmap C2): what a run FELT like, computed from the wave record the
 * simulator already keeps. Twelve balance gates ask whether the curve lands inside its bands; this asks the
 * questions a band cannot see -- did difficulty ever jump without warning, did the hero ever slide for wave
 * after wave with no window to recover, did the pacing ever offer a valley, did any wave stall, and how much
 * breath was left in the hero when the tension ended.
 *
 * <p>It is a record of measurements, not of verdicts: {@code FunInstrumentTest} owns the bands, and the human
 * instrument (roadmap C1, {@code docs/PLAYTEST_PROTOCOL.md}) is what eventually calibrates them against a
 * player who can feel unfairness rather than only count it.
 */
public record FunMetrics(
    float worstSpike,
    int longestDeclineStreak,
    int breatherWaves,
    int longestBreatherStreak,
    float stallFraction,
    float finalHealthFraction
) {
    /** A wave that costs the hero less than this fraction of max health counts as a pacing valley. */
    public static final float BREATHER_FRACTION = 0.02f;

    /** A health-fraction change smaller than this between waves is flat, not a decline. */
    private static final float DECLINE_EPSILON = 1e-4f;

    public static FunMetrics from(BalanceReport report) {
        List<WaveSample> waves = report.waves();
        if (waves.isEmpty()) {
            return new FunMetrics(0f, 0, 0, 0, 0f, 0f);
        }
        float worstSpike = 0f;
        for (int index = 1; index < waves.size(); index++) {
            float jump = Math.abs(waves.get(index).damageFraction() - waves.get(index - 1).damageFraction());
            worstSpike = Math.max(worstSpike, jump);
        }
        int longestDecline = 0;
        int currentDecline = 0;
        int breathers = 0;
        int longestBreathers = 0;
        int currentBreathers = 0;
        int stalls = 0;
        for (int index = 0; index < waves.size(); index++) {
            WaveSample wave = waves.get(index);
            if (index > 0 && healthFraction(wave) < healthFraction(waves.get(index - 1)) - DECLINE_EPSILON) {
                currentDecline++;
                longestDecline = Math.max(longestDecline, currentDecline);
            } else {
                currentDecline = 0;
            }
            if (wave.damageFraction() < BREATHER_FRACTION) {
                breathers++;
                currentBreathers++;
                longestBreathers = Math.max(longestBreathers, currentBreathers);
            } else {
                currentBreathers = 0;
            }
            if (wave.timedOut()) {
                stalls++;
            }
        }
        WaveSample last = waves.get(waves.size() - 1);
        return new FunMetrics(
            worstSpike,
            longestDecline,
            breathers,
            longestBreathers,
            (float) stalls / waves.size(),
            healthFraction(last));
    }

    private static float healthFraction(WaveSample wave) {
        return wave.maximumHealth() > 0f ? wave.remainingHealth() / wave.maximumHealth() : 0f;
    }
}
