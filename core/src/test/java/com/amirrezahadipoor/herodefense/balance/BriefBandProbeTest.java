package com.amirrezahadipoor.herodefense.balance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.junit.jupiter.api.Test;

final class BriefBandProbeTest {
    @Test
    void probe() throws Exception {
        StringBuilder out = new StringBuilder();
        float minAvg = 9f, maxSpike = 0f;
        for (int i = 0; i < 10; i++) {
            long seed = 0x4845524F444546L + i;
            BalanceSimulator.BalanceReport brief = new BalanceSimulator().runBrief(seed);
            BalanceSimulator.BalanceReport full = new BalanceSimulator().run(seed);
            float briefAvg = 0f, briefSpike = 0f, fullAvg = 0f, fullSpike = 0f;
            for (BalanceSimulator.WaveSample w : brief.waves()) {
                briefAvg += w.damageFraction();
                briefSpike = Math.max(briefSpike, w.damageFraction());
            }
            // the full run's opening 30 waves, for the like-for-like comparison
            for (int index = 0; index < 30 && index < full.waves().size(); index++) {
                fullAvg += full.waves().get(index).damageFraction();
                fullSpike = Math.max(fullSpike, full.waves().get(index).damageFraction());
            }
            briefAvg /= brief.waves().size();
            fullAvg /= 30f;
            minAvg = Math.min(minAvg, briefAvg);
            maxSpike = Math.max(maxSpike, briefSpike);
            out.append(String.format(Locale.ROOT,
                "seed+%d brief waves=%d spike=%.4f avg=%.4f | plain-first-30 avg=%.4f spike=%.4f finished=%s%n",
                i, brief.waves().size(), briefSpike, briefAvg, fullAvg, fullSpike, brief.reachedFinalWave()));
        }
        out.append(String.format(Locale.ROOT, "min brief avg=%.4f max brief spike=%.4f%n", minAvg, maxSpike));
        Files.writeString(Path.of("/tmp/brief_band.txt"), out.toString());
    }
}
