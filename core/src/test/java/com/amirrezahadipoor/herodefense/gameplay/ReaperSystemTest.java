package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.audio.IdentityCues;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/**
 * The reaper's contract (roadmap B4).
 *
 * <p>The leak this closes was measured, not suspected: a two-hundred-wave run ended with 3,512 bodies in
 * {@code aliveEnemies}, every one of them drawn each frame and every one of them serialized into a 3.07 MB save.
 * These tests pin the three promises the fix makes -- a corpse is presentation and lives for its presentation, the
 * list is bounded no matter what the field does, and nothing that reads a corpse is beaten to it by the reaper.
 */
final class ReaperSystemTest {

    private final EnemyWaveSpawner spawner = new EnemyWaveSpawner(new EnemyFactory());

    @Test
    void aDeadBodyLeavesTheStateOnceItsPresentationIsDoneAndNotBefore() {
        GameState state = GameState.newRun(7L);
        spawner.spawnRegularEnemies(state, 1, 4);
        Enemy corpse = state.aliveEnemies.get(0);
        corpse.receiveDamage(corpse.maxHealth * 4f);
        assertFalse(corpse.alive, "the body is dead");

        ReaperSystem.update(state, ReaperSystem.CORPSE_LINGER_SECONDS * 0.5f);
        assertTrue(state.aliveEnemies.contains(corpse), "half the window is not the whole window");

        ReaperSystem.update(state, ReaperSystem.CORPSE_LINGER_SECONDS);
        assertFalse(state.aliveEnemies.contains(corpse), "the corpse is gone once its window has passed");
        assertEquals(3, ReaperSystem.bodyCount(state), "and the living are untouched by it");
    }

    @Test
    void aLivingBodyIsNeverTouchedHoweverLongTheRunGoesOn() {
        GameState state = GameState.newRun(11L);
        spawner.spawnRegularEnemies(state, 40, spawner.regularCountForWave(40));
        int living = state.aliveEnemies.size();
        for (int tick = 0; tick < 600; tick++) {
            ReaperSystem.update(state, 1f / 30f);
        }
        assertEquals(living, ReaperSystem.bodyCount(state), "twenty seconds of ticks do not reap the living");
        assertEquals(0, ReaperSystem.corpseCount(state));
    }

    @Test
    void theListIsBoundedEvenWhenEveryBodyDiesInTheSameTick() {
        // Forty waves, every body of every wave killed in the same tick, and the reaper called once per frame
        // exactly as CombatSystem calls it. Without the cap the 560 bodies below would all still be there: the
        // linger alone cannot keep up with a wave that dies at once, which is what the cap is for.
        GameState state = GameState.newRun(13L);
        for (int wave = 1; wave <= 40; wave++) {
            spawner.spawnRegularEnemies(state, wave, spawner.regularCountForWave(wave));
            for (Enemy enemy : state.aliveEnemies) {
                enemy.receiveDamage(enemy.maxHealth * 4f);
            }
            ReaperSystem.update(state, 1f / 30f);
        }
        assertTrue(state.aliveEnemies.size() <= ReaperSystem.MAX_BODIES_IN_STATE,
            "aliveEnemies holds " + state.aliveEnemies.size() + " bodies, over the cap of "
                + ReaperSystem.MAX_BODIES_IN_STATE);
        // The measurement that matters: what the state costs to serialize is now a constant of the design.
        assertTrue(ReaperSystem.corpseCount(state) <= ReaperSystem.MAX_BODIES_IN_STATE);
    }

    @Test
    void clearingAtAWaveBoundaryLeavesOnlyTheSurvivors() {
        GameState state = GameState.newRun(17L);
        spawner.spawnRegularEnemies(state, 30, 8);
        Enemy survivor = state.aliveEnemies.get(1);
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy != survivor) {
                enemy.receiveDamage(enemy.maxHealth * 4f);
            }
        }
        ReaperSystem.clear(state);
        assertEquals(1, state.aliveEnemies.size(), "one body walked out of the wave");
        assertEquals(survivor, state.aliveEnemies.get(0));
    }

    @Test
    void theDeathCueStillFindsItsCorpseOnTheFrameItDied() {
        // The reaper runs last in CombatSystem.update precisely so this holds: the audio identity of a death is
        // read from the freshest corpse, and a reaper that ran first would silence every death in the game.
        GameState state = GameState.newRun(19L);
        spawner.spawnRegularEnemies(state, 12, 3);
        Enemy last = state.aliveEnemies.get(state.aliveEnemies.size() - 1);
        last.enemyType = EnemyType.FUNGAL_BRUTE.name();
        last.receiveDamage(last.maxHealth * 4f);
        assertNotNull(IdentityCues.newestCorpseType(state), "the corpse is readable on the frame it fell");
        assertEquals(EnemyType.FUNGAL_BRUTE, IdentityCues.newestCorpseType(state));

        for (int tick = 0; tick < 30; tick++) {
            ReaperSystem.update(state, 1f / 30f);
        }
        assertTrue(ReaperSystem.corpseCount(state) == 0, "and gone a second later, as designed");
    }

    @Test
    void nullAndEmptyStatesAreNotACrash() {
        ReaperSystem.update(null, 1f);
        ReaperSystem.clear(null);
        assertEquals(0, ReaperSystem.bodyCount(null));
        assertEquals(0, ReaperSystem.corpseCount(null));
        GameState empty = GameState.newRun(23L);
        ReaperSystem.update(empty, 1f);
        ReaperSystem.clear(empty);
        assertEquals(0, ReaperSystem.bodyCount(empty));
    }
}
