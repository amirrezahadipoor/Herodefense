package com.amirrezahadipoor.herodefense.balance;

import com.amirrezahadipoor.herodefense.gameplay.EnemyWaveSpawner;
import com.amirrezahadipoor.herodefense.gameplay.WaveOmens;
import com.amirrezahadipoor.herodefense.model.WaveModifier;
import java.util.List;
import org.junit.jupiter.api.Test;

/** TEMPORARY P6a debug: dumps per-wave pressure to locate NAIVE seed 3's spike. DELETE AFTER. */
final class TemporaryNaiveSpikeDumpTest {
    @Test
    void dumpNaiveWaves() {
        for (long seed : new long[] {0x5EED01L, 0x5EED02L, 0x5EED03L}) {
            BalanceReport report = new BalanceSimulator()
                .runWithPolicy(seed, BalanceSimulator.Policy.NAIVE);
            System.out.println("SPIKEDUMP seed=" + Long.toHexString(seed)
                + " reachedFinal=" + report.reachedFinalWave()
                + " waves=" + report.waves().size());
            List<WaveSample> waves = report.waves();
            for (int index = 0; index < waves.size(); index++) {
                WaveSample wave = waves.get(index);
                float jump = index == 0 ? 0f
                    : Math.abs(wave.damageFraction() - waves.get(index - 1).damageFraction());
                WaveModifier hint = WaveOmens.of(seed, wave.wave(), 0, true);
                System.out.println("SPIKEDUMP " + Long.toHexString(seed) + " wave=" + wave.wave()
                    + " frac=" + wave.damageFraction() + " clear=" + wave.clearTimeSeconds()
                    + " jump=" + jump
                    + (wave.wave() % 5 == 0 ? " BOSS" : "")
                    + (EnemyWaveSpawner.isEliteWave(wave.wave(), 0) ? " ELITE" : "")
                    + (hint.isOmen() ? " OMENHINT-" + hint : ""));
            }
        }
    }
}
