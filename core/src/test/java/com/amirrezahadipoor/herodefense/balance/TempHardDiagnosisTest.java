package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.amirrezahadipoor.herodefense.model.GameMode;
import com.amirrezahadipoor.herodefense.rewards.RewardCardId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * TEMPORARY H1 diagnostic, deleted before the gate goes green: prints the wave-by-wave record of the
 * runs the gate loses, so the fix tunes the disease instead of the thermometer. Read it in the
 * {@code core-balance-results} artifact's XML standard-output.
 */
@Tag("balance")
final class TempHardDiagnosisTest {
    @Test
    void printTheRunsTheGateLoses() {
        BalanceReport forced = new BalanceSimulator()
            .runWithForcedCard(0x4341524453494DL, RewardCardId.STRENGTH, 1);
        assertNotNull(forced);
        System.out.println("FORCED-STRENGTH-BOSS1 waves=" + forced.waves().size());
        System.out.print(forced.toCsv());
        BalanceReport naive = new BalanceSimulator()
            .runWithPolicy(0x4E414956453035L, BalanceSimulator.Policy.NAIVE, 0, GameMode.BRIEF);
        assertNotNull(naive);
        System.out.println("NAIVE05-BRIEF waves=" + naive.waves().size());
        System.out.print(naive.toCsv());
        BalanceReport naive07 = new BalanceSimulator()
            .runWithPolicy(0x4E414956453037L, BalanceSimulator.Policy.OPTIMISER, 0, GameMode.STANDARD);
        assertNotNull(naive07);
        System.out.println("NAIVE07-LONG-OPTIMISER waves=" + naive07.waves().size());
        System.out.print(naive07.toCsv());
        BalanceReport tiered = new BalanceSimulator().runWithAscensionTier(0x4845524F444546L, 3);
        assertNotNull(tiered);
        System.out.println("TIER3-NAKED-BASELINE waves=" + tiered.waves().size());
        System.out.print(tiered.toCsv());
    }
}
