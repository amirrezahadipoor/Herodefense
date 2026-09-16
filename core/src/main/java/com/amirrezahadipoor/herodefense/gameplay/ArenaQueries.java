package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Read-only questions the game loop asks about the arena, kept out of the game class itself (roadmap R2.2).
 *
 * <p>{@code livingBossCount} and {@code totalEnemyHealth} are sampled before and after an update to detect that
 * something happened, and {@code untouchedFirstWave} tells the opening cinematic whether this is a fresh run.
 */
public final class ArenaQueries {

    private ArenaQueries() {
    }

    /** Living bosses right now; used as before/after snapshots around an update. */
    public static int livingBossCount(GameState state) {
        int count = 0;
        if (state == null) return 0;
        for (Boss boss : state.aliveBosses) {
            if (boss != null && boss.alive) count++;
        }
        return count;
    }

    /** Total health still standing on the field, for both regular foes and bosses. */
    public static float totalEnemyHealth(GameState state) {
        if (state == null) return 0f;
        float total = 0f;
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy != null) total += Math.max(0f, enemy.health);
        }
        for (Boss boss : state.aliveBosses) {
            if (boss != null) total += Math.max(0f, boss.health);
        }
        return total;
    }

    /** True while a run has not yet begun wave 1 (the save written right after New Game). */
    public static boolean untouchedFirstWave(GameState state) {
        return state != null && state.waveNumber == 1 && !state.waveActive && state.totalKills == 0
            && state.aliveEnemies.isEmpty() && state.aliveBosses.isEmpty();
    }

    /** Living regular foes, excluding bosses. */
    public static int livingEnemyCount(GameState state) {
        int count = 0;
        if (state == null) return 0;
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy != null && enemy.alive) count++;
        }
        return count;
    }
}
