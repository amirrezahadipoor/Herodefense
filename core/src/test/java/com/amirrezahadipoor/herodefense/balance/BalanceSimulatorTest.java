package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.balance.BalanceReport;
import com.amirrezahadipoor.herodefense.balance.WaveSample;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.List;
import org.junit.jupiter.api.Test;

final class BalanceSimulatorTest {
    private static final long BASELINE_SEED = 0x4845524F444546L;
    // Re-based by audit item 1. The old 0.05-0.15 band described a run whose every hit was worth 0.27% of the bar
    // and which no policy could lose (WavePressureCurveTest documents the measurement). Damage is a share of the
    // bar now, so the run averages 0.27-0.36 of a bar per wave and a single wave can cost a bar and a tenth; the
    // band below is drawn around the post-B1 measurement, with the floor kept as the "not a walkover" guard.
    private static final float MINIMUM_AVERAGE_DAMAGE_FRACTION = 0.15f;
    private static final float MAXIMUM_AVERAGE_DAMAGE_FRACTION = 0.55f;
    // H1 hard era: 1.10 -> 1.30 (measured 1.1418 at wave 180), matching WavePressureCurveTest.
    private static final float MAXIMUM_SINGLE_WAVE_DAMAGE_FRACTION = 1.30f;
    private static final float MAXIMUM_CLEAR_TIME_SECONDS = 120f;
    private static final int GATE_WAVE = GameState.FINAL_WAVE;

    @Test
    void balancedRunPassesAcceptanceGateAndLogsEveryWave() {
        BalanceReport report = new BalanceSimulator().run(BASELINE_SEED);

        assertTrue(report.reachedFinalWave(), "Balanced baseline must complete the continuous run");
        assertEquals(GameState.FINAL_WAVE, report.waves().size());
        assertEquals(1, report.waves().get(0).wave());
        // Gate scope: the full 1..GATE_WAVE run (Phase 18.4 rebalance).
        List<WaveSample> gated = report.waves().stream()
            .filter(sample -> sample.wave() <= GATE_WAVE)
            .toList();
        float averageDamage = 0f;
        for (WaveSample sample : gated) averageDamage += sample.damageFraction();
        averageDamage /= gated.size();
        assertTrue(
            averageDamage >= MINIMUM_AVERAGE_DAMAGE_FRACTION
                && averageDamage <= MAXIMUM_AVERAGE_DAMAGE_FRACTION,
            "Average gross damage fraction was " + averageDamage
        );
        // Phase 25.3b: the middle third rises end to end (first vs last quarter). P6a: it now holds
        // within a shallow valley instead (measured 0.175 -> 0.148). Longer waves press the weaker
        // early window harder (exposure) and ease the stronger late window (sequential pulses), so
        // the gate allows a dip to 0.05 and still catches a collapse.
        float earlyMiddle = 0f;
        float lateMiddle = 0f;
        for (WaveSample sample : gated) {
            if (sample.wave() >= 25 && sample.wave() <= 38) earlyMiddle += sample.damageFraction();
            if (sample.wave() >= 67 && sample.wave() <= 80) lateMiddle += sample.damageFraction();
        }
        earlyMiddle /= 14f;
        lateMiddle /= 14f;
        assertTrue(
            lateMiddle > earlyMiddle - 0.05f,
            "Middle-third quarters were " + earlyMiddle + " -> " + lateMiddle
        );
        for (WaveSample sample : gated) {
            assertTrue(Float.isFinite(sample.remainingHealth()));
            assertTrue(Float.isFinite(sample.dpsToEnemyHpRatio()));
            assertTrue(Float.isFinite(sample.clearTimeSeconds()));
            assertTrue(sample.clearTimeSeconds() > 0f);
            assertTrue(
                sample.damageFraction() <= MAXIMUM_SINGLE_WAVE_DAMAGE_FRACTION,
                "Wave " + sample.wave() + " damage spike was " + sample.damageFraction()
            );
            assertTrue(
                sample.clearTimeSeconds() <= MAXIMUM_CLEAR_TIME_SECONDS,
                "Wave " + sample.wave() + " clear-time spike was " + sample.clearTimeSeconds()
            );
            assertFalse(sample.timedOut(), "Wave " + sample.wave() + " timed out");
        }
        String csv = report.toCsv();
        assertTrue(csv.startsWith("wave,start_hp,start_max_hp,remaining_hp"));
        assertEquals(report.waves().size() + 1L, csv.lines().count());
        System.out.print(csv);
    }
}
