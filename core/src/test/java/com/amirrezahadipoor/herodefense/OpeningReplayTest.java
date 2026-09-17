package com.amirrezahadipoor.herodefense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.gameplay.ArenaQueries;
import com.amirrezahadipoor.herodefense.gameplay.ContinuousWaveRun;
import com.amirrezahadipoor.herodefense.gameplay.EnemyFactory;
import com.amirrezahadipoor.herodefense.gameplay.EnemyWaveSpawner;
import com.amirrezahadipoor.herodefense.gameplay.SessionController;
import com.amirrezahadipoor.herodefense.gameplay.WaveLifecycleSystem;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/** Continue on the save written right after New Game must replay the opening, never skip it. */
class OpeningReplayTest {

    @Test
    void freshRunSaveIsRecognisedAsUntouched() {
        GameState state = GameState.newRun(7L);
        assertTrue(ArenaQueries.untouchedFirstWave(state));
    }

    @Test
    void startedFirstWaveIsNotUntouched() {
        GameState state = GameState.newRun(7L);
        new WaveLifecycleSystem(new EnemyWaveSpawner(new EnemyFactory()), new ContinuousWaveRun())
            .startCurrentWave(state);
        assertFalse(ArenaQueries.untouchedFirstWave(state));
    }

    @Test
    void laterWaveBetweenSpawnsIsNotUntouched() {
        GameState state = GameState.newRun(7L);
        state.waveNumber = 2;
        assertFalse(ArenaQueries.untouchedFirstWave(state));
    }

    // These two asked `HeroDefenseGame.openingTierFor`, a five-line delegate to the class that owns the rule.
    // R7.4 needed the lines in the game class more than the delegate did, so the assertions now name the owner
    // and the delegate is gone; `SessionControllerTest` asserts the same two cases beside the save it drives.
    @Test
    void continueReplaysSnapshotTierNotLiveTier() {
        GameState state = GameState.newRun(7L);
        state.ascensionTier = 3;
        state.openingTier = 1;
        assertEquals(1, SessionController.openingTierFor(state));
    }

    @Test
    void missingSnapshotFallsBackToLiveTier() {
        GameState state = GameState.newRun(7L);
        state.ascensionTier = 2;
        assertEquals(2, SessionController.openingTierFor(state));
    }
}
