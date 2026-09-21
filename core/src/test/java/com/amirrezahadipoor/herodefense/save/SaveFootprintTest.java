package com.amirrezahadipoor.herodefense.save;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.gameplay.EnemyFactory;
import com.amirrezahadipoor.herodefense.gameplay.EnemyWaveSpawner;
import com.amirrezahadipoor.herodefense.gameplay.ReaperSystem;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/**
 * The save is a deliverable with a budget, measured like the APK (roadmap B4).
 *
 * <p>Before the reaper, this measurement read 3.07 MB at wave 200 and grew every wave, because
 * {@code GameStateCodec} serializes the whole {@code GameState} and the state carried every body the run had ever
 * spawned. The budget below is deliberately loose against the shipped behaviour -- a wave's worth of bodies
 * pretty-printed is tens of kilobytes -- and deliberately tight against the leak, because a regression that lets
 * the list grow again fails here on the push that introduces it rather than at release time on a device.
 */
final class SaveFootprintTest {

    /** The loose ceiling: a whole wave's bodies plus the run's own state. */
    private static final int SAVE_BUDGET_BYTES = 220 * 1024;

    /** How many waves the measurement sweeps. The old leak was already 46 KB at wave 10 and 1.4 MB at wave 100. */
    private static final int WAVES = 200;

    @Test
    void theSaveStaysInsideItsBudgetForAWholeRun() {
        long seed = 20342418142676294L;
        GameState state = GameState.newRun(seed);
        EnemyWaveSpawner spawner = new EnemyWaveSpawner(new EnemyFactory());
        GameStateCodec codec = new GameStateCodec();
        int worstBytes = 0;
        int worstWave = 0;

        for (int wave = 1; wave <= WAVES; wave++) {
            state.waveNumber = wave;
            if (wave % EnemyWaveSpawner.BOSS_WAVE_INTERVAL != 0) {
                spawner.spawnRegularEnemies(state, wave, spawner.regularCountForWave(wave));
            }
            // The wave is fought and lost by its monsters, one tick at a time, with the reaper in the loop exactly
            // as CombatSystem calls it.
            for (Enemy enemy : state.aliveEnemies) {
                if (enemy != null) {
                    enemy.receiveDamage(enemy.maxHealth * 4f);
                }
            }
            ReaperSystem.update(state, 1f / 30f);
            ReaperSystem.clear(state);

            String json = codec.encode(state);
            if (json.length() > worstBytes) {
                worstBytes = json.length();
                worstWave = wave;
            }
        }

        assertTrue(worstBytes <= SAVE_BUDGET_BYTES,
            "the largest save in a " + WAVES + "-wave run was " + worstBytes + " bytes at wave " + worstWave
                + ", over the budget of " + SAVE_BUDGET_BYTES + " bytes");
        assertTrue(ReaperSystem.bodyCount(state) <= ReaperSystem.MAX_BODIES_IN_STATE,
            "the state carries " + ReaperSystem.bodyCount(state) + " bodies after the run");
    }

    @Test
    void aRunThatNeverReapsIsTheThingTheBudgetIsGuarding() {
        // The counterfactual, kept as evidence: the same sweep without the reaper is what produced the 3.07 MB
        // measurement in the first place. If this ever stops being true, the budget above has stopped measuring
        // anything and should be re-derived from the new numbers rather than left in place.
        GameState state = GameState.newRun(20342418142676294L);
        EnemyWaveSpawner spawner = new EnemyWaveSpawner(new EnemyFactory());
        for (int wave = 1; wave <= 60; wave++) {
            state.waveNumber = wave;
            if (wave % EnemyWaveSpawner.BOSS_WAVE_INTERVAL != 0) {
                spawner.spawnRegularEnemies(state, wave, spawner.regularCountForWave(wave));
            }
            for (Enemy enemy : state.aliveEnemies) {
                if (enemy != null) {
                    enemy.receiveDamage(enemy.maxHealth * 4f);
                }
            }
        }
        int leakedBodies = ReaperSystem.bodyCount(state);
        assertTrue(leakedBodies > 400,
            "an unreaped run piles up bodies; measured " + leakedBodies + " by wave 60");
        assertTrue(ReaperSystem.corpseCount(state) == leakedBodies, "all of them are corpses");
    }
}
