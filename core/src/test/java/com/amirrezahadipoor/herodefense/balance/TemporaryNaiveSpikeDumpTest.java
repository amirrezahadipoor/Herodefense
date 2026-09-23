package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.gameplay.EnemyWaveSpawner;
import com.amirrezahadipoor.herodefense.gameplay.WaveOmens;
import com.amirrezahadipoor.herodefense.model.WaveModifier;
import java.util.List;
import org.junit.jupiter.api.Test;

/** TEMPORARY P6a debug: dumps per-wave pressure to locate NAIVE seed 3's spike. DELETE AFTER. */
final class TemporaryNaiveSpikeDumpTest {
    @Test
    void dumpNaiveWaves() {
        StringBuilder dump = new StringBuilder();
        for (long seed : new long[] {0x5EED01L, 0x5EED02L, 0x5EED03L}) {
            BalanceReport report = new BalanceSimulator()
                .runWithPolicy(seed, BalanceSimulator.Policy.NAIVE);
            dump.append("\nseed=").append(Long.toHexString(seed)
                ).append(" reachedFinal=").append(report.reachedFinalWave());
            List<WaveSample> waves = report.waves();
            for (int index = 0; index < waves.size(); index++) {
                WaveSample wave = waves.get(index);
                float jump = index == 0 ? 0f
                    : Math.abs(wave.damageFraction() - waves.get(index - 1).damageFraction());
                WaveModifier hint = WaveOmens.of(seed, wave.wave(), 0, true);
                dump.append("\nw").append(wave.wave())
                    .append(" f").append(String.format("%.3f", wave.damageFraction()))
                    .append(" c").append(String.format("%.0f", wave.clearTimeSeconds()))
                    .append(" j").append(String.format("%.3f", jump))
                    .append(wave.wave() % 5 == 0 ? " B" : "")
                    .append(EnemyWaveSpawner.isEliteWave(wave.wave(), 0) ? " E" : "")
                    .append(hint.isOmen() ? " O" + hint : "");
            }
        }
        assertTrue(false, dump.toString());
    }
}
