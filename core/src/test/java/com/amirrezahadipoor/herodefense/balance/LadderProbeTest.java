package com.amirrezahadipoor.herodefense.balance;

import com.amirrezahadipoor.herodefense.gameplay.DifficultyCurve;
import com.amirrezahadipoor.herodefense.model.GameMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/** TEMPORARY: the R4.7 ladder measurement. Prints and passes. Delete before committing. */
class LadderProbeTest {
    private static final long[] SEEDS = {
        0x4845524F444546L, 0x4845524F444546L + 1, 0x4845524F444546L + 2, 0x747269616C7331L, 0x4341524453494DL,
        0x4E414956453031L};

    private static void ladderRow(int tier) {
        BalanceSimulator simulator = new BalanceSimulator();
        float brief = 0f;
        float reach = 0f;
        float optimiser = 0f;
        for (long seed : SEEDS) {
            brief += simulator.runWithPolicy(seed, BalanceSimulator.Policy.NAIVE, tier, GameMode.BRIEF)
                .averageDamageFraction() / SEEDS.length;
            reach += simulator.runWithPolicy(seed, BalanceSimulator.Policy.NAIVE, tier, GameMode.STANDARD)
                .waves().size() / (float) SEEDS.length;
            optimiser += simulator.runWithAscensionTier(seed, tier).averageDamageFraction() / SEEDS.length;
        }
        BalanceReport last = simulator.runWithAscensionTier(SEEDS[0], tier);
        float[] quarters = new float[4];
        for (WaveSample wave : last.waves()) {
            quarters[Math.min(3, (wave.wave() - 1) / 50)] += wave.damageFraction() / 50f;
        }
        List<String> ratios = new ArrayList<>();
        for (long seed : SEEDS) {
            float hp = new DifficultyCurve().baselineRegularHealth(30, tier)
                / new DifficultyCurve().baselineRegularHealth(30, 0);
            ratios.add(String.format(Locale.ROOT, "%.2f", hp));
        }
        System.out.println(String.format(Locale.ROOT,
            "LADDER tier %d naiveBrief %.4f naiveReach %.1f optimiserAvg %.4f wave30hp %s quarters %.3f/%.3f/%.3f/%.3f",
            tier, brief, reach, optimiser, ratios.get(0), quarters[0], quarters[1], quarters[2], quarters[3]));
    }

    @Test
    void printTheLadder() {
        for (int tier : new int[] {0, 3, 6, 10}) {
            ladderRow(tier);
        }
    }
}
