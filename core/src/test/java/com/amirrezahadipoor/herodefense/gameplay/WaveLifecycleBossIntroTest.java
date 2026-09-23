package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/**
 * Boss waves defer for their watch-only intro: the advance parks the wave number and raises the
 * flag, and the fight only spawns when the intro hands off (or instantly for the simulator).
 */
final class WaveLifecycleBossIntroTest {

    private final WaveLifecycleSystem lifecycle =
        new WaveLifecycleSystem(new EnemyWaveSpawner(new EnemyFactory()), new ContinuousWaveRun());

    @Test
    void startingABossWaveDefersForItsIntro() {
        GameState state = GameState.newRun(21L);
        state.waveNumber = 5;

        assertTrue(lifecycle.startCurrentWave(state));
        assertTrue(state.bossIntroPending);
        assertEquals(5, state.bossIntroWave);
        assertFalse(state.waveActive, "nothing spawns until the intro hands off");
        assertTrue(state.aliveBosses.isEmpty());
    }

    @Test
    void regularWavesStillSpawnAtOnce() {
        GameState state = GameState.newRun(22L);
        state.waveNumber = 6;

        assertTrue(lifecycle.startCurrentWave(state));
        assertFalse(state.bossIntroPending);
        assertTrue(state.waveActive);
        assertTrue(state.livingEnemyCount() > 0);
    }

    @Test
    void startingTwiceWhilePendingStaysPut() {
        GameState state = GameState.newRun(23L);
        state.waveNumber = 5;

        assertTrue(lifecycle.startCurrentWave(state));
        assertFalse(lifecycle.startCurrentWave(state));
        assertTrue(state.bossIntroPending);
    }

    @Test
    void completingTheIntroSpawnsTheFight() {
        GameState state = GameState.newRun(24L);
        state.waveNumber = 5;
        lifecycle.startCurrentWave(state);

        assertTrue(lifecycle.completeBossIntro(state));
        assertFalse(state.bossIntroPending);
        assertTrue(state.waveActive);
        assertEquals(1, state.aliveBosses.size());
        assertEquals("ANCIENT_GOLEM", state.aliveBosses.get(0).bossType);
    }

    @Test
    void completingWithoutAPendingIntroDoesNothing() {
        GameState state = GameState.newRun(25L);
        state.waveNumber = 5;

        assertFalse(lifecycle.completeBossIntro(state));
        assertFalse(lifecycle.completeBossIntro(null));
        assertFalse(state.waveActive);
        assertTrue(state.aliveBosses.isEmpty());
    }

    @Test
    void clearingIntoABossWaveReportsBossIntro() {
        GameState state = GameState.newRun(26L);
        state.waveNumber = 4;
        lifecycle.startCurrentWave(state);
        for (Enemy enemy : state.aliveEnemies) enemy.receiveDamage(Float.MAX_VALUE);

        assertEquals(WaveCompletion.BOSS_INTRO, lifecycle.updateAfterCombat(state));
        assertEquals(5, state.waveNumber);
        assertTrue(state.bossIntroPending);
        assertFalse(state.waveActive);
    }

    @Test
    void clearingIntoARegularWaveStillReportsNextWave() {
        GameState state = GameState.newRun(27L);
        state.waveNumber = 6;
        lifecycle.startCurrentWave(state);
        for (Enemy enemy : state.aliveEnemies) enemy.receiveDamage(Float.MAX_VALUE);

        assertEquals(WaveCompletion.NEXT_WAVE, lifecycle.updateAfterCombat(state));
        assertEquals(7, state.waveNumber);
        assertTrue(state.waveActive);
        assertFalse(state.bossIntroPending);
    }

    @Test
    void rewardChoiceIntoABossWaveReportsBossIntro() {
        GameState state = GameState.newRun(28L);
        state.waveNumber = 9;
        state.awaitingBossReward = false;
        state.pendingRewardCards.clear();

        assertEquals(WaveCompletion.BOSS_INTRO, lifecycle.continueAfterBossReward(state));
        assertEquals(10, state.waveNumber);
        assertTrue(state.bossIntroPending);
    }

    @Test
    void introPropSpawnsAtItsLanesEdge() {
        GameState state = GameState.newRun(29L);
        state.waveNumber = 5;
        lifecycle.startCurrentWave(state);

        Boss prop = lifecycle.spawnBossIntroProp(state);

        assertNotNull(prop);
        assertEquals("ANCIENT_GOLEM", prop.bossType);
        assertEquals(-EnemyWaveSpawner.EDGE_OFFSET, prop.x, 0.001f);
        assertEquals(1, state.aliveBosses.size());
    }

    @Test
    void introPropRespawnsFreshAfterAReload() {
        GameState state = GameState.newRun(30L);
        state.waveNumber = 5;
        lifecycle.startCurrentWave(state);
        Boss stale = lifecycle.spawnBossIntroProp(state);
        stale.x = 123f;

        Boss fresh = lifecycle.spawnBossIntroProp(state);

        assertNotNull(fresh);
        assertEquals(1, state.aliveBosses.size(), "the stale twin is gone, never doubled");
        assertEquals(-EnemyWaveSpawner.EDGE_OFFSET, fresh.x, 0.001f);
    }

    @Test
    void propSpawnRefusesWithoutAPendingIntro() {
        GameState state = GameState.newRun(31L);
        state.waveNumber = 5;

        assertNull(lifecycle.spawnBossIntroProp(state));
        assertNull(lifecycle.spawnBossIntroProp(null));
    }

    @Test
    void propSpawnRefusesACorruptWave() {
        GameState state = GameState.newRun(32L);
        state.waveNumber = 5;
        lifecycle.startCurrentWave(state);
        state.bossIntroWave = 6;

        assertNull(lifecycle.spawnBossIntroProp(state));
        assertTrue(state.aliveBosses.isEmpty());
    }

    @Test
    void halfTheEscortWalksInWithTheBoss() {
        GameState state = GameState.newRun(34L);
        state.waveNumber = 5;
        lifecycle.startCurrentWave(state);

        assertTrue(lifecycle.completeBossIntro(state));
        assertEquals(1, state.escortWave);
        assertEquals(1, state.aliveBosses.size());
        assertEquals(2, state.aliveEnemies.size(), "four escorts, half up front");
    }

    @Test
    void theBloodiedBossCallsTheRestMidFight() {
        GameState state = GameState.newRun(35L);
        state.waveNumber = 5;
        lifecycle.startCurrentWave(state);
        lifecycle.completeBossIntro(state);
        Boss boss = state.aliveBosses.get(0);
        boss.health = boss.maxHealth * 0.9f;

        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state));
        assertEquals(1, state.escortWave, "a scratched boss keeps its escort waiting");
        assertEquals(2, state.aliveEnemies.size());

        boss.health = boss.maxHealth / 2f;
        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state));
        assertEquals(2, state.escortWave);
        assertEquals(4, state.aliveEnemies.size());
    }

    @Test
    void theFallenBossIsAvengedBeforeTheReward() {
        GameState state = GameState.newRun(36L);
        state.waveNumber = 10;
        lifecycle.startCurrentWave(state);
        lifecycle.completeBossIntro(state);
        state.aliveBosses.get(0).receiveDamage(Float.MAX_VALUE);

        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state));
        assertEquals(2, state.escortWave);
        assertEquals(4, state.aliveEnemies.size());

        for (Enemy enemy : state.aliveEnemies) enemy.receiveDamage(Float.MAX_VALUE);
        assertEquals(WaveCompletion.BOSS_REWARD, lifecycle.updateAfterCombat(state));
    }

    @Test
    void theIntroPropWalksAlone() {
        GameState state = GameState.newRun(37L);
        state.waveNumber = 5;
        lifecycle.startCurrentWave(state);

        assertNotNull(lifecycle.spawnBossIntroProp(state));
        assertTrue(state.aliveEnemies.isEmpty(), "the trash-talk gets the stage alone");
        assertEquals(0, state.escortWave);
    }

    @Test
    void reloadRepairsWaveActiveWhilePending() {
        GameState state = GameState.newRun(33L);
        state.waveNumber = 5;
        state.bossIntroPending = true;
        state.bossIntroWave = 5;
        state.waveActive = true;

        state.validateAndRepair();

        assertTrue(state.bossIntroPending);
        assertFalse(state.waveActive, "the intro replays; the wave has not started");
    }
}
