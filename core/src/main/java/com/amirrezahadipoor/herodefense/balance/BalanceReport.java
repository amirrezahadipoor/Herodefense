package com.amirrezahadipoor.herodefense.balance;

import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.List;
import java.util.Locale;

/** What one simulated run measured: a wave-by-wave record, and whether the Hero finished the run. */
public record BalanceReport(List<WaveSample> waves, boolean reachedFinalWave) {
    public BalanceReport {
        waves = List.copyOf(waves);
    }

    /** True once the run cleared the planting wave (the tuned first half). */
    public boolean reachedWave100() {
        return reachedFinalWave || waves.size() > GameState.PLANTING_WAVE;
    }

    public float averageDamageFraction() {
        if (waves.isEmpty()) return 0f;
        float total = 0f;
        for (WaveSample wave : waves) total += wave.damageFraction();
        return total / waves.size();
    }

    public String toCsv() {
        StringBuilder result = new StringBuilder(
            "wave,start_hp,start_max_hp,remaining_hp,max_hp,damage_taken,damage_fraction,dps_to_hp,clear_seconds,timed_out\n"
        );
        for (WaveSample wave : waves) {
            result.append(String.format(
                Locale.ROOT,
                "%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.6f,%.6f,%.3f,%s%n",
                wave.wave(),
                wave.startingHealth(),
                wave.startingMaxHealth(),
                wave.remainingHealth(),
                wave.maximumHealth(),
                wave.grossDamageTaken(),
                wave.damageFraction(),
                wave.dpsToEnemyHpRatio(),
                wave.clearTimeSeconds(),
                wave.timedOut()
            ));
        }
        return result.toString();
    }
}
