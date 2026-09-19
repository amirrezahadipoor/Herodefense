package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/** Deeds pay once, only when earned, and never for a state too fresh to hold a ledger. */
class DeedsTest {

    @Test
    void waveDeedsPayOnceAtEachMilestone() {
        GameState state = new GameState();
        state.codexUnlocked = new java.util.LinkedHashMap<>();
        int coinsBefore = 120;
        state.coins = coinsBefore;
        state.waveNumber = 9;
        assertTrue(Deeds.completeNewlyEarned(state).isEmpty());
        assertEquals(coinsBefore, state.coins);

        state.waveNumber = 10;
        java.util.List<Deeds> paid = Deeds.completeNewlyEarned(state);
        assertEquals(1, paid.size());
        assertEquals(Deeds.WAVE_10, paid.get(0));
        assertEquals(coinsBefore + Deeds.WAVE_10.reward(), state.coins);
        assertTrue(Deeds.completeNewlyEarned(state).isEmpty());
        assertEquals(coinsBefore + Deeds.WAVE_10.reward(), state.coins);
    }

    @Test
    void cleanDeedsDemandAnUnfallenDryRun() {
        GameState state = new GameState();
        state.codexUnlocked = new java.util.LinkedHashMap<>();
        state.waveNumber = 25;
        state.heroDiedThisRun = false;
        state.potionsUsedThisRun = 0;
        java.util.List<Deeds> paid = Deeds.completeNewlyEarned(state);
        assertTrue(paid.contains(Deeds.WAVE_10));
        assertTrue(paid.contains(Deeds.WAVE_25));
        assertTrue(paid.contains(Deeds.CLEAN_25));

        GameState fallen = new GameState();
        fallen.codexUnlocked = new java.util.LinkedHashMap<>();
        fallen.waveNumber = 25;
        fallen.heroDiedThisRun = true;
        java.util.List<Deeds> fallenPaid = Deeds.completeNewlyEarned(fallen);
        assertFalse(fallenPaid.contains(Deeds.CLEAN_25));
    }

    @Test
    void codexDeedCountsOnlyCodexPagesNotItsOwnPayouts() {
        GameState state = new GameState();
        state.codexUnlocked = new java.util.LinkedHashMap<>();
        state.codexUnlocked.put("codex_01", Boolean.TRUE);
        state.codexUnlocked.put("codex_02", Boolean.TRUE);
        state.waveNumber = 10;
        Deeds.completeNewlyEarned(state);
        for (int index = 0; index < 9; index++) {
            state.codexUnlocked.put("codex_" + (index + 3), Boolean.TRUE);
        }
        assertTrue(Deeds.completeNewlyEarned(state).contains(Deeds.CODEX_10));
    }

    @Test
    void nullLedgerPaysNothingAndDoesNotCrash() {
        GameState state = new GameState();
        state.codexUnlocked = null;
        state.waveNumber = 100;
        assertTrue(Deeds.completeNewlyEarned(state).isEmpty());
    }
}
