package com.amirrezahadipoor.herodefense.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * The trophy ledger and its rules (roadmap R3.3): a trophy appears exactly at its target, never early, never
 * twice, survives a new run, and a save written before trophies existed still gets credit for what it did.
 */
final class TrophyBookTest {

    @Test
    void nothingIsEarnedOnAFreshRun() {
        GameState state = GameState.newRun(77L);

        assertTrue(TrophyBook.evaluate(state).isEmpty(), "a new Warden starts with an empty case");
        assertEquals(0, state.trophies.earnedCount());
        for (Trophy trophy : Trophy.values()) {
            assertEquals(0, TrophyBook.progress(state, trophy), trophy.id());
        }
    }

    @Test
    void aTrophyArrivesExactlyAtItsTargetAndOnlyOnce() {
        GameState state = GameState.newRun(77L);

        for (int wave = 1; wave <= 99; wave++) {
            state.trophies.recordWaveCleared();
        }
        assertTrue(TrophyBook.evaluate(state).isEmpty(), "ninety-nine waves is not a hundred");
        assertEquals(99, TrophyBook.progress(state, Trophy.STEADY_HAND));

        state.trophies.recordWaveCleared();
        assertEquals(List.of(Trophy.STEADY_HAND), TrophyBook.evaluate(state), "the hundredth wave pays");
        assertTrue(TrophyBook.evaluate(state).isEmpty(), "and it only pays once");
        assertTrue(state.trophies.isEarned(Trophy.STEADY_HAND));
    }

    @Test
    void progressNeverOvershootsTheTarget() {
        GameState state = GameState.newRun(77L);
        for (int wave = 0; wave < 6000; wave++) {
            state.trophies.recordWaveCleared();
        }

        assertEquals(100, TrophyBook.progress(state, Trophy.STEADY_HAND));
        assertEquals(5000, TrophyBook.progress(state, Trophy.ENDLESS_PATIENCE), "a bar reads full, not 6000/5000");
        List<Trophy> earned = TrophyBook.evaluate(state);
        assertTrue(earned.contains(Trophy.LONG_HOLD) && earned.contains(Trophy.ENDLESS_PATIENCE));
    }

    @Test
    void aRunThatNeverReachesForAPotionEarnsBareHands() {
        GameState state = GameState.newRun(77L);

        state.trophies.recordRunEnd(40, false);
        assertFalse(state.trophies.isEarned(Trophy.BARE_HANDS), "a potion run does not earn it");

        state.trophies.recordRunEnd(12, true);
        assertTrue(TrophyBook.evaluate(state).contains(Trophy.BARE_HANDS));
        assertEquals(40, state.trophies.bestWave, "and the ledger keeps the best run regardless");
    }

    @Test
    void threeTreesInOneRunEarnTheGardener() {
        GameState state = GameState.newRun(77L);

        state.trophies.recordTreePlanted(1);
        state.trophies.recordTreePlanted(2);
        assertFalse(state.trophies.isEarned(Trophy.GARDENER), "two grove trees is not the whole grove");

        state.trophies.recordTreePlanted(3);
        assertTrue(TrophyBook.evaluate(state).contains(Trophy.GARDENER));
        assertEquals(3, state.trophies.mostTreesInOneRun);
    }

    @Test
    void theMetaTrophiesReadTheProgressTheGameAlreadyKeeps() {
        GameState state = GameState.newRun(77L);
        state.heartwood = 1000;
        state.ascensionTier = 12;
        state.wave200ReachedCount = 1;
        state.eliteKillCounts.put("blightburst", 60);
        state.eliteKillCounts.put("weeping_rot", 41);
        for (int id = 1; id <= 20; id++) {
            state.codexUnlocked.put("lore_" + id, Boolean.TRUE);
        }
        for (BossType type : BossType.values()) {
            state.firstBossKills.put(type.name(), Boolean.TRUE);
        }

        List<Trophy> earned = TrophyBook.evaluate(state);

        assertTrue(earned.containsAll(List.of(
            Trophy.DEEP_ROOTED, Trophy.TWELFTH_DESCENT, Trophy.HOLLOW_ANSWERED, Trophy.THORN_COLLECTOR,
            Trophy.LOREKEEPER, Trophy.WITNESS
        )), "earned: " + earned);
        assertEquals(100, TrophyBook.progress(state, Trophy.THORN_COLLECTOR),
            "the run's own elite counters (60 + 41) are the source, read through the ledger's rule");
    }

    @Test
    void theEarnedCaseSurvivesANewRun() {
        GameState state = GameState.newRun(77L);
        for (int wave = 0; wave < 100; wave++) {
            state.trophies.recordWaveCleared();
        }
        TrophyBook.evaluate(state);
        int wavesBefore = state.trophies.wavesCleared;

        state.resetForNewRun(99L);

        assertTrue(state.trophies.isEarned(Trophy.STEADY_HAND), "a new run does not take a trophy away");
        assertEquals(wavesBefore, state.trophies.wavesCleared, "and the counters keep counting");
        assertEquals(1, state.waveNumber, "while the run itself is fresh");
    }

    @Test
    void anOldSaveIsMigratedInsteadOfForgotten() {
        GameState veteran = GameState.newRun(77L);
        // This is what a save from before the ledger looked like: progress, and no ledger at all.
        veteran.heartwood = 2500;
        veteran.ascensionTier = 4;
        veteran.wave200ReachedCount = 1;
        veteran.eliteKillCounts.put("blightburst", 120);
        for (int id = 1; id <= 21; id++) {
            veteran.codexUnlocked.put("lore_" + id, Boolean.TRUE);
        }
        veteran.trophies = null;

        List<Trophy> backfilled = TrophyBook.migrate(veteran);

        assertTrue(backfilled.containsAll(List.of(
            Trophy.DEEP_ROOTED, Trophy.HOLLOW_ANSWERED, Trophy.THORN_COLLECTOR, Trophy.LOREKEEPER
        )), "backfilled: " + backfilled);
        assertTrue(backfilled.contains(Trophy.FIRST_VIGIL), "a run that reached wave 200 is a finished run");
        assertEquals(TrophyLedger.CURRENT_LEDGER_VERSION, veteran.trophies.ledgerVersion);
        assertTrue(TrophyBook.migrate(veteran).isEmpty(), "a migrated save is not re-read on every load");
    }

    @Test
    void aBrokenLedgerIsRepairedRatherThanTrusted() {
        GameState state = GameState.newRun(77L);
        state.trophies.wavesCleared = -5;
        state.trophies.earned = null;
        state.trophies.repair();
        state.validateAndRepair();

        assertEquals(0, state.trophies.wavesCleared);
        assertFalse(state.trophies.isEarned(Trophy.STEADY_HAND));
        assertEquals(0, TrophyBook.progress(state, Trophy.STEADY_HAND));
    }

    @Test
    void everyTrophyIsReachableAndDistinct() {
        Set<String> titles = new HashSet<>();
        Set<String> ids = new HashSet<>();
        for (Trophy trophy : Trophy.values()) {
            assertTrue(trophy.target() > 0, trophy.id());
            assertFalse(trophy.title().isBlank() || trophy.hint().isBlank(), trophy.id());
            assertTrue(titles.add(trophy.title()), "duplicate title: " + trophy.title());
            assertTrue(ids.add(trophy.id()), "duplicate id: " + trophy.id());
        }
        assertEquals(12, Trophy.values().length);
        assertEquals(BossType.values().length, Trophy.WITNESS.target(),
            "WITNESS counts the identities the game actually ships");
    }
}
