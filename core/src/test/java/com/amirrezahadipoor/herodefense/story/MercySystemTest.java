package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/** Three greetings spare a watcher; nothing else does, and a spared watcher pays nothing. */
class MercySystemTest {

    private GameState stateWithWatcher() {
        GameState state = new GameState();
        Enemy watcher = new Enemy(1L, "ROOTLING", 100f, 100f);
        watcher.silentWatcher = true;
        watcher.killRewardsGranted = false;
        state.aliveEnemies.add(watcher);
        return state;
    }

    @Test
    void threeGreetingsSpareTheWatcherWithoutReward() {
        GameState state = stateWithWatcher();
        assertEquals(MercySystem.Result.GREETED, MercySystem.greet(state, 100f, 100f));
        assertEquals(MercySystem.Result.GREETED, MercySystem.greet(state, 100f, 100f));
        assertEquals(MercySystem.Result.SPARED, MercySystem.greet(state, 100f, 100f));
        assertTrue(state.aliveEnemies.isEmpty());
    }

    @Test
    void progressResetsAreNotPersistedBecauseWatcherLeavesOnSpare() {
        GameState state = stateWithWatcher();
        MercySystem.greet(state, 100f, 100f);
        MercySystem.greet(state, 100f, 100f);
        assertEquals(2, state.aliveEnemies.get(0).spareTouches);
        assertEquals(MercySystem.Result.SPARED, MercySystem.greet(state, 100f, 100f));
    }

    @Test
    void fightingEnemiesAndEmptyGroundAreNeverMercyTargets() {
        GameState state = stateWithWatcher();
        Enemy fighter = new Enemy(2L, "ROOTLING", 300f, 300f);
        state.aliveEnemies.add(fighter);
        assertEquals(MercySystem.Result.NONE, MercySystem.greet(state, 300f, 300f));
        assertEquals(MercySystem.Result.NONE, MercySystem.greet(state, 500f, 500f));
        assertTrue(fighter.alive);
        assertFalse(state.aliveEnemies.isEmpty());
    }

    @Test
    void deadWatchersAreIgnored() {
        GameState state = stateWithWatcher();
        state.aliveEnemies.get(0).alive = false;
        assertEquals(MercySystem.Result.NONE, MercySystem.greet(state, 100f, 100f));
    }

    @Test
    void nanCoordinatesGreetNothing() {
        GameState state = stateWithWatcher();
        assertEquals(MercySystem.Result.NONE, MercySystem.greet(state, Float.NaN, 100f));
        assertNull(MercySystem.watcherAt(null, 0f, 0f));
    }
}
