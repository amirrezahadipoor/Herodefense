package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.gameplay.DifficultyCurve;
import com.amirrezahadipoor.herodefense.model.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * "Threats that scale" as a measured claim, not a slogan (roadmap R4.1).
 *
 * <p>The item asked for two things: a curve that keeps rising after wave 40, and a published band for a policy
 * that is not an optimiser. This test is the first half. It runs the fixed sweep of five seeds through the whole
 * two-hundred-wave run and looks at the shape of the pressure it produces, where pressure is the share of the
 * hero's health a wave took — the same measurement every other balance gate in this repository uses.
 *
 * <p><b>Why four quarters and not five buckets of forty.</b> The wave director has three segments (the base rate,
 * the hotter middle of waves 25-80, and the second half), and a forty-wave window sometimes straddles a segment
 * boundary, which makes the measurement an artifact of the window rather than of the curve. Fifty-wave quarters are
 * long enough that every window contains a whole segment or more, and they still describe the shape a player feels
 * as the run goes on.
 *
 * <p><b>What it found, before it was a gate.</b> The shipped curve does rise after wave 40 — the mean pressure of
 * the four quarters is 0.0576, 0.0986, 0.1032 and 0.1229 on the fixed sweep, so the run ends at roughly twice the
 * pressure it opened with. It also showed the shallowest step in the curve: waves 101-150 are only 4.7% heavier
 * than waves 51-100, against 71% for the step before them, because the second half drops to the coolest growth
 * rate in the whole curve (`SECOND_HALF_HEALTH_GROWTH` 1.023 against the base 1.037 and the middle 1.041). Closing
 * that step is worth doing and was tried — two candidates, `1.025/1.0085` and `1.024/1.008` from wave 121, both
 * lifted the third quarter above the 5% mark — but each of them pushed late-game spikes through the shipped
 * ceilings (`BalanceSimulatorTest` bare run 0.354 against 0.35, trial pairs 0.41-0.47 against 0.40, tier 10
 * STRENGTH median 0.677 against 0.60). Both candidates are recorded with their numbers in `docs/BALANCE.md` and
 * the plateau is an open roadmap item (R4.6) rather than a hidden one.
 *
 * <p><b>The two rules this gate enforces.</b> Across the seeds, every quarter must be heavier than the quarter
 * before it — the "the curve rises after wave 40" claim, measured on the mean of the fixed sweep so one seed's
 * luck cannot carry it. Per seed, no quarter may be more than five percent lighter than the one before it, which
 * is the honest allowance for simulation noise: a single seed may dip a hair, the curve may not.
 */
final class WavePressureCurveTest {

    /** The fixed sweep the balance gates use, so a curve change is judged on the same seeds everywhere. */
    private static final long[] SEEDS = {
        0x4845524F444546L, 0x4845524F444546L + 1, 0x4845524F444546L + 2, 0x747269616C7331L, 0x4341524453494DL};

    private static final int QUARTERS = 4;
    private static final int WAVES_PER_QUARTER = GameState.FINAL_WAVE / QUARTERS;

    /**
     * The rise the curve has to show: every quarter heavier than the one before it, in the mean of the sweep. The
     * shipped curve clears this with the shallowest step at 1.047 (waves 101-150, the plateau of R4.6), so the gate
     * is what keeps a later change from flattening the run rather than a threshold chosen to make today pass by a
     * hair.
     */
    private static final float MINIMUM_QUARTER_STEP = 1.0f;

    /** What a single seed is allowed to do: no more than this much lighter than the quarter before. */
    private static final float SEED_DIP_ALLOWANCE = 0.95f;

    /** The band every other gate uses: a run must not be a walkover and must not be a wall. */
    private static final float MINIMUM_AVERAGE = 0.05f;
    private static final float MAXIMUM_AVERAGE = 0.15f;
    private static final float MAXIMUM_SPIKE = 0.40f;

    private static float quarterAverage(List<WaveSample> waves, int quarter) {
        float total = 0f;
        int count = 0;
        for (int wave = quarter * WAVES_PER_QUARTER; wave < (quarter + 1) * WAVES_PER_QUARTER; wave++) {
            total += waves.get(wave).damageFraction();
            count++;
        }
        return total / count;
    }

    @Test
    void theCurveKeepsRisingAfterWaveForty() {
        float[][] quarters = new float[SEEDS.length][QUARTERS];
        List<String> readings = new ArrayList<>();
        for (int index = 0; index < SEEDS.length; index++) {
            BalanceReport report = new BalanceSimulator().run(SEEDS[index]);
            assertTrue(report.reachedFinalWave(), "seed " + Long.toHexString(SEEDS[index])
                + " has to finish the run for its shape to mean anything");
            for (int quarter = 0; quarter < QUARTERS; quarter++) {
                quarters[index][quarter] = quarterAverage(report.waves(), quarter);
            }
            readings.add(String.format(Locale.ROOT, "%s: %.4f %.4f %.4f %.4f",
                Long.toHexString(SEEDS[index]), quarters[index][0], quarters[index][1],
                quarters[index][2], quarters[index][3]));
        }

        for (int index = 0; index < SEEDS.length; index++) {
            for (int quarter = 1; quarter < QUARTERS; quarter++) {
                assertTrue(quarters[index][quarter] >= quarters[index][quarter - 1] * SEED_DIP_ALLOWANCE,
                    "seed " + Long.toHexString(SEEDS[index]) + " quarter " + (quarter + 1)
                        + " is more than five percent lighter than quarter " + quarter
                        + " (" + quarters[index][quarter] + " against " + quarters[index][quarter - 1]
                        + "); readings: " + readings);
            }
        }

        for (int quarter = 1; quarter < QUARTERS; quarter++) {
            float previous = 0f;
            float current = 0f;
            for (float[] perSeed : quarters) {
                previous += perSeed[quarter - 1];
                current += perSeed[quarter];
            }
            assertTrue(current >= previous * MINIMUM_QUARTER_STEP,
                "the curve must keep rising after wave 40: quarter " + (quarter + 1) + " averages "
                    + current / SEEDS.length + " against " + previous / SEEDS.length + " for quarter " + quarter
                    + ", which is less than the " + MINIMUM_QUARTER_STEP + " the band demands; readings: "
                    + readings);
        }
    }

    @Test
    void theLateRunStaysInsideTheShippedPressureBand() {
        for (long seed : SEEDS) {
            BalanceReport report = new BalanceSimulator().run(seed);
            float average = report.averageDamageFraction();
            float peak = 0f;
            for (WaveSample wave : report.waves()) {
                peak = Math.max(peak, wave.damageFraction());
            }
            String where = "seed " + Long.toHexString(seed);
            assertTrue(average >= MINIMUM_AVERAGE, where + " averaged only " + average + " over the two hundred "
                + "waves; a run in this game is not a walkover");
            assertTrue(average <= MAXIMUM_AVERAGE, where + " averaged " + average + ", over the "
                + MAXIMUM_AVERAGE + " ceiling the difficulty band allows");
            assertTrue(peak <= MAXIMUM_SPIKE, where + " spiked to " + peak + " in a single wave, over the "
                + MAXIMUM_SPIKE + " ceiling");
        }
    }

    /**
     * The shape of the shipped curve, so a later reader can see why the run looks the way it does: the middle
     * segment (waves 25-80) is hotter than the base rate, and the second half runs cooler than both. The second
     * half is where the shallow step lives — the measured +4.7% between the second and third quarter, against
     * +71% before it — which is recorded as an open roadmap item (R4.6) rather than hidden here, because raising
     * that rate broke the spike ceilings on two tried candidates (see docs/BALANCE.md).
     */
    @Test
    void theShippedCurveIsHotterInTheMiddleAndCoolerInTheSecondHalf() {
        DifficultyCurve curve = new DifficultyCurve();
        assertTrue(DifficultyCurve.MIDDLE_HEALTH_GROWTH > DifficultyCurve.ENEMY_HEALTH_GROWTH,
            "waves 25-80 compound a hotter health rate than the base");
        assertTrue(DifficultyCurve.MIDDLE_DAMAGE_GROWTH > DifficultyCurve.ENEMY_DAMAGE_GROWTH,
            "and a hotter damage rate");
        assertTrue(DifficultyCurve.SECOND_HALF_HEALTH_GROWTH < DifficultyCurve.ENEMY_HEALTH_GROWTH,
            "the second half is the coolest stretch of the curve, which is why its step is the shallowest");
        assertTrue(curve.baselineRegularHealth(GameState.FINAL_WAVE) > curve.baselineRegularHealth(120),
            "wave 200 is still heavier than wave 120");
        assertTrue(curve.baselineRegularDamage(GameState.FINAL_WAVE) > curve.baselineRegularDamage(120),
            "with heavier hits as well");
    }
}
