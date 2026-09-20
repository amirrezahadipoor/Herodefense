package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/** When a rootward elite raises its ward, the two nearest regulars catch a shorter copy of it. */
class RootwardShareTest {  // shared copy = a decaying damage ward, never an immunity

    @Test
    void raisingTheEliteShieldSharesItWithNearbyRegulars() {
        GameState state = GameState.newRun(50L);
        Enemy elite = new Enemy(state.allocateEntityId(), "ROOTLING", 100f, 100f);
        elite.eliteAffix = "rootward_ward";
        Enemy near = new Enemy(state.allocateEntityId(), "ROOTLING", 130f, 100f);
        Enemy far = new Enemy(state.allocateEntityId(), "ROOTLING", 400f, 400f);
        state.aliveEnemies.add(elite);
        state.aliveEnemies.add(near);
        state.aliveEnemies.add(far);

        EliteAffixSystem system = new EliteAffixSystem(null);
        system.update(state, EliteAffixSystem.ROOTWARD_SHIELD_PERIOD);

        assertTrue(elite.affixShieldRemainingSeconds > 0f, "the elite's own ward raised");
        assertEquals(EliteAffixSystem.ROOTWARD_SHARE_DURATION, near.affixWardRemainingSeconds, 0.01f);
        assertEquals(0f, far.affixWardRemainingSeconds, 0.0001f);
    }

    @Test
    void elitesAndWatchersNeverReceiveTheShare() {
        GameState state = GameState.newRun(51L);
        Enemy elite = new Enemy(state.allocateEntityId(), "ROOTLING", 100f, 100f);
        elite.eliteAffix = "rootward_ward";
        Enemy fellowElite = new Enemy(state.allocateEntityId(), "ROOTLING", 110f, 100f);
        fellowElite.eliteAffix = "gravemoss";
        Enemy watcher = new Enemy(state.allocateEntityId(), "ROOTLING", 120f, 100f);
        watcher.silentWatcher = true;
        state.aliveEnemies.add(elite);
        state.aliveEnemies.add(fellowElite);
        state.aliveEnemies.add(watcher);

        new EliteAffixSystem(null).update(state, EliteAffixSystem.ROOTWARD_SHIELD_PERIOD);

        assertEquals(0f, fellowElite.affixWardRemainingSeconds, 0.0001f);
        assertEquals(0f, watcher.affixWardRemainingSeconds, 0.0001f);
    }

    @Test
    void theSharedWardBendsDamageWithoutEverBlockingIt() {
        assertWardBendsAndNeverBlocks();
    }

    @Test
    void theSharedWardAlwaysDecays() {
        GameState state = GameState.newRun(54L);
        Enemy ally = new Enemy(state.allocateEntityId(), "ROOTLING", 0f, 0f);
        ally.affixWardRemainingSeconds = EliteAffixSystem.ROOTWARD_SHARE_DURATION;
        state.aliveEnemies.add(ally);
        new EliteAffixSystem(null).update(state, EliteAffixSystem.ROOTWARD_SHARE_DURATION + 1f);
        assertEquals(0f, ally.affixWardRemainingSeconds, 0.0001f);
    }

    @Test
    void surgeScheduleNeverLandsOnABossLap() {
        boolean onlyTheScheduledLateWavesSurge =
            !EnemyWaveSpawner.isSurgeWave(100) && !EnemyWaveSpawner.isSurgeWave(110)
            && EnemyWaveSpawner.isSurgeWave(113) && EnemyWaveSpawner.isSurgeWave(193)
            && !EnemyWaveSpawner.isSurgeWave(150) && !EnemyWaveSpawner.isSurgeWave(200)
            && !EnemyWaveSpawner.isSurgeWave(205);
        assertTrue(onlyTheScheduledLateWavesSurge);
    }

    private void assertWardBendsAndNeverBlocks() {
        GameState state = GameState.newRun(53L);
        Enemy warded = new Enemy(state.allocateEntityId(), "ROOTLING", 0f, 0f);
        warded.health = 1000f;
        warded.maxHealth = 1000f;
        warded.affixWardRemainingSeconds = 1f;
        warded.receiveDamage(100f);
        assertEquals(1000f - 100f * com.amirrezahadipoor.herodefense.model.Enemy.SHARED_WARD_MULTIPLIER,
            warded.health, 0.0001f);

        Enemy decayed = new Enemy(state.allocateEntityId(), "ROOTLING", 0f, 0f);
        decayed.health = 1000f;
        decayed.maxHealth = 1000f;
        decayed.affixWardRemainingSeconds = 0f;
        decayed.receiveDamage(100f);
        assertEquals(900f, decayed.health, 0.0001f);
    }
}
