package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.rewards.BossRewardCardSystem;
import com.amirrezahadipoor.herodefense.trials.TrialId;
import org.junit.jupiter.api.Test;

final class WaveLifecycleSystemTest {
    private final EnemyWaveSpawner spawner = new EnemyWaveSpawner(new EnemyFactory());
    private final WaveLifecycleSystem lifecycle = new WaveLifecycleSystem(spawner, new ContinuousWaveRun());

    @Test
    void fullyClearedWaveAutomaticallyAdvancesAndSpawnsTheNextWave() {
        GameState state = GameState.newRun(1L);
        assertTrue(lifecycle.startCurrentWave(state));
        assertTrue(state.waveActive);
        assertTrue(state.livingEnemyCount() > 0);
        assertEquals(4, state.wavePlannedEnemies);
        assertEquals(1, state.tricklePulse);
        assertEquals(4, state.aliveEnemies.size());
        for (Enemy enemy : state.aliveEnemies) enemy.receiveDamage(Float.MAX_VALUE);

        assertEquals(WaveCompletion.NEXT_WAVE, lifecycle.updateAfterCombat(state));
        assertEquals(2, state.waveNumber);
        assertTrue(state.waveActive);
        assertTrue(state.livingEnemyCount() > 0);
    }

    @Test
    void doesNotAdvanceUntilEveryEnemyIsDead() {
        GameState state = GameState.newRun(2L);
        lifecycle.startCurrentWave(state);
        state.aliveEnemies.get(0).receiveDamage(Float.MAX_VALUE);
        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state));
        assertEquals(1, state.waveNumber);
        assertEquals(1, state.tricklePulse, "a four-body skirmish walks in whole");
    }

    @Test
    void clearingWaveOneHundredPausesForThreeCardsBeforeCompletingRun() {
        GameState state = GameState.newRun(3L);
        state.waveNumber = GameState.FINAL_WAVE;
        lifecycle.startCurrentWave(state);
        assertTrue(lifecycle.completeBossIntro(state));
        assertEquals(6, state.aliveEnemies.size(), "half the twelve-body escort walks in first");
        assertEquals(1, state.escortWave);
        state.aliveBosses.get(0).receiveDamage(Float.MAX_VALUE);

        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state),
            "the boss's death-cry calls the rest of the escort");
        assertEquals(2, state.escortWave);
        assertEquals(12, state.aliveEnemies.size());
        for (Enemy enemy : state.aliveEnemies) enemy.receiveDamage(Float.MAX_VALUE);

        assertEquals(WaveCompletion.BOSS_REWARD, lifecycle.updateAfterCombat(state));
        assertTrue(state.awaitingBossReward);
        assertEquals(3, state.pendingRewardCards.size());
        assertFalse(state.waveActive);

        state.awaitingBossReward = false;
        state.pendingRewardCards.clear();
        assertEquals(WaveCompletion.RUN_COMPLETED, lifecycle.continueAfterBossReward(state));
        assertTrue(state.runComplete);
        assertEquals(0, state.livingEnemyCount());
    }

    @Test
    void clearingWaveOneHundredBossFreezesCombatUntilTheCeremonyCompletes() {
        GameState state = GameState.newRun(100L);
        state.waveNumber = GameState.PLANTING_WAVE;
        assertTrue(lifecycle.startCurrentWave(state));
        assertTrue(state.bossIntroPending);
        assertTrue(state.aliveBosses.isEmpty());
        assertTrue(lifecycle.completeBossIntro(state));
        assertFalse(state.aliveBosses.isEmpty());
        for (Boss boss : state.aliveBosses) boss.receiveDamage(Float.MAX_VALUE);
        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state),
            "the vengeance pulse arrives before the reward");
        for (Enemy enemy : state.aliveEnemies) enemy.receiveDamage(Float.MAX_VALUE);
        assertEquals(WaveCompletion.BOSS_REWARD, lifecycle.updateAfterCombat(state));
        assertTrue(new BossRewardCardSystem().chooseCard(state, 0));

        assertEquals(WaveCompletion.PLANTING_CEREMONY, lifecycle.continueAfterBossReward(state));
        assertTrue(state.ceremonyPending);
        assertFalse(state.secondTreePlanted);
        assertEquals(GameState.PLANTING_WAVE + 1, state.waveNumber);
        assertFalse(state.waveActive);
        assertEquals(0, state.livingEnemyCount());
        assertFalse(lifecycle.startCurrentWave(state));

        assertTrue(lifecycle.completePlantingCeremony(state));
        assertFalse(state.ceremonyPending);
        assertTrue(state.secondTreePlanted);
        assertTrue(state.waveActive);
        assertTrue(state.livingEnemyCount() > 0);
        assertFalse(lifecycle.completePlantingCeremony(state));
    }

    @Test
    void reloadedSaveKeepsTheCeremonyPendingAndRepairsWaveActive() {
        GameState state = GameState.newRun(101L);
        state.waveNumber = GameState.PLANTING_WAVE + 1;
        state.ceremonyPending = true;
        state.waveActive = true;
        state.validateAndRepair();
        assertTrue(state.ceremonyPending);
        assertFalse(state.waveActive);

        GameState early = GameState.newRun(102L);
        early.waveNumber = 40;
        early.ceremonyPending = true;
        early.secondTreePlanted = true;
        early.validateAndRepair();
        assertFalse(early.ceremonyPending);
        assertFalse(early.secondTreePlanted);
        assertEquals(0, early.plantedTreesCount);
    }

    @Test
    void bossWaveClearRecordsTheWaveTimerBeforeTheCardChoice() {
        GameState state = GameState.newRun(103L);
        state.waveNumber = 5;
        lifecycle.startCurrentWave(state);
        lifecycle.completeBossIntro(state);
        state.waveElapsedSeconds = 25f;
        state.aliveBosses.get(0).receiveDamage(Float.MAX_VALUE);

        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state),
            "the vengeance pulse arrives before the reward");
        for (Enemy enemy : state.aliveEnemies) enemy.receiveDamage(Float.MAX_VALUE);

        assertEquals(WaveCompletion.BOSS_REWARD, lifecycle.updateAfterCombat(state));
        assertEquals(25f, state.fastestWaveClearSeconds);
        assertEquals(0f, state.waveElapsedSeconds);
    }

    @Test
    void clearingAMilestoneBossWaveEarnsPipsBreather() {
        GameState state = GameState.newRun(112L);
        state.waveNumber = 25;
        lifecycle.startCurrentWave(state);
        assertTrue(lifecycle.completeBossIntro(state));
        for (Boss boss : state.aliveBosses) boss.receiveDamage(Float.MAX_VALUE);
        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state));
        for (Enemy enemy : state.aliveEnemies) enemy.receiveDamage(Float.MAX_VALUE);
        assertEquals(WaveCompletion.BOSS_REWARD, lifecycle.updateAfterCombat(state));

        state.awaitingBossReward = false;
        state.pendingRewardCards.clear();
        assertEquals(WaveCompletion.BREATHER, lifecycle.continueAfterBossReward(state));
        assertEquals(26, state.waveNumber);
        assertTrue(state.breatherPending);
        assertEquals(25, state.breatherWave);
        assertFalse(state.waveActive);
        assertFalse(lifecycle.startCurrentWave(state), "the next wave waits on Pip's beat");

        assertTrue(lifecycle.completeBreather(state));
        assertFalse(state.breatherPending);
        assertTrue(state.waveActive);
        assertTrue(state.livingEnemyCount() > 0);
    }

    @Test
    void completingWithoutAPendingBreatherDoesNothing() {
        GameState state = GameState.newRun(113L);
        state.waveNumber = 26;

        assertFalse(lifecycle.completeBreather(state));
        assertFalse(lifecycle.completeBreather(null));
        assertFalse(state.waveActive);
    }

    @Test
    void reloadRepairsWaveActiveWhileBreathing() {
        GameState state = GameState.newRun(114L);
        state.waveNumber = 26;
        state.breatherPending = true;
        state.breatherWave = 25;
        state.waveActive = true;

        state.validateAndRepair();

        assertTrue(state.breatherPending);
        assertFalse(state.waveActive, "the beat replays; the wave has not started");
    }

    @Test
    void waveClearsAroundALoneSilentWatcherAndDespawnsIt() {
        GameState state = GameState.newRun(104L);
        state.waveNumber = 6;
        assertTrue(lifecycle.startCurrentWave(state));
        Enemy watcher = new EnemyFactory().create(state, EnemyType.ROOTLING, 200f, 820f, 0);
        watcher.silentWatcher = true;
        state.aliveEnemies.add(watcher);
        long watcherId = watcher.id;
        for (Enemy enemy : state.aliveEnemies) {
            if (!enemy.silentWatcher) enemy.receiveDamage(Float.MAX_VALUE);
        }

        assertEquals(WaveCompletion.NEXT_WAVE, lifecycle.updateAfterCombat(state));
        assertEquals(7, state.waveNumber);
        for (Enemy enemy : state.aliveEnemies) assertFalse(enemy.id == watcherId);
    }

    @Test
    void laterPulsesArriveAtHalfAndQuarterStrength() {
        GameState state = GameState.newRun(105L);
        state.waveNumber = 12;
        assertTrue(lifecycle.startCurrentWave(state));
        assertEquals(10, state.wavePlannedEnemies);
        assertEquals(5, state.aliveEnemies.size());

        fellToLiving(state, 4);
        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state));
        assertEquals(2, state.tricklePulse);
        assertEquals(8, state.aliveEnemies.size());

        fellToLiving(state, 3);
        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state));
        assertEquals(2, state.tricklePulse, "three living of ten planned is above quarter strength");

        fellToLiving(state, 2);
        assertEquals(WaveCompletion.NO_CHANGE, lifecycle.updateAfterCombat(state));
        assertEquals(3, state.tricklePulse);
        assertEquals(10, state.aliveEnemies.size());

        for (Enemy enemy : state.aliveEnemies) enemy.receiveDamage(Float.MAX_VALUE);
        assertEquals(WaveCompletion.NEXT_WAVE, lifecycle.updateAfterCombat(state));
        assertEquals(13, state.waveNumber);
    }

    /** Kills living fighters until exactly {@code living} remain; silent watchers never count. */
    private static void fellToLiving(GameState state, int living) {
        while (state.livingEnemyCount() > living) {
            for (Enemy enemy : state.aliveEnemies) {
                if (enemy.alive && !enemy.silentWatcher) {
                    enemy.receiveDamage(Float.MAX_VALUE);
                    break;
                }
            }
        }
    }

    @Test
    void aWaveStartedWholeClearsWholeWithoutPhantomPulses() {
        GameState state = GameState.newRun(107L);
        state.waveNumber = 8;
        state.waveActive = true;
        spawner.spawnRegularEnemies(state, 8, spawner.regularCountForWave(8));
        assertEquals(0, state.tricklePulse);
        for (Enemy enemy : state.aliveEnemies) enemy.receiveDamage(Float.MAX_VALUE);

        assertEquals(WaveCompletion.NEXT_WAVE, lifecycle.updateAfterCombat(state));
        assertEquals(9, state.waveNumber);
    }

    @Test
    void theRaisedCapBindsPastWaveOneTwenty() {
        GameState state = GameState.newRun(108L);
        state.waveNumber = 121;
        assertTrue(lifecycle.startCurrentWave(state));
        assertEquals(25, state.wavePlannedEnemies);
        assertEquals(15, state.aliveEnemies.size());

        GameState capped = GameState.newRun(109L);
        capped.waveNumber = 121;
        capped.activeTrials.add(TrialId.IRON_TIDE.name());
        assertTrue(lifecycle.startCurrentWave(capped));
        assertEquals(25, capped.wavePlannedEnemies);

        GameState deep = GameState.newRun(111L);
        deep.waveNumber = 199;
        assertTrue(lifecycle.startCurrentWave(deep));
        assertEquals(28, deep.wavePlannedEnemies);
        assertEquals(17, deep.aliveEnemies.size());

        GameState early = GameState.newRun(110L);
        early.waveNumber = 39;
        early.activeTrials.add(TrialId.IRON_TIDE.name());
        assertTrue(lifecycle.startCurrentWave(early));
        assertEquals(24, early.wavePlannedEnemies);
    }
}
