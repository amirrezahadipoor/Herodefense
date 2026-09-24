package com.amirrezahadipoor.herodefense.balance;

import com.amirrezahadipoor.herodefense.rewards.RewardCardId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Temporary H1 probe: HP trajectory of glassy vs EHP boss-1 picks. Delete before merging. */
@Tag("balance")
final class TempFloorProbeTest {
    private static final long SEED = 0x4341524453494DL;

    @Test
    void probeBoss1Floor() {
        for (RewardCardId card : new RewardCardId[]{RewardCardId.LUCK, RewardCardId.HEALTH}) {
            BalanceReport report = new BalanceSimulator().runWithForcedCard(SEED, card, 1);
            System.out.println("PROBE-" + card + " waves=" + report.waves().size());
            for (WaveSample w : report.waves()) {
                if (w.wave() > 15) break;
                System.out.println("PROBE-" + card + " w=" + w.wave()
                    + " start=" + String.format("%.3f", w.startingHealth())
                    + " end=" + String.format("%.3f", w.remainingHealth())
                    + " max=" + String.format("%.1f", w.maximumHealth())
                    + " share=" + String.format("%.4f", w.damageFraction()));
            }
        }
    }
}
