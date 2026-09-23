package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Dread waves (P6b longer waves): a wave with a living boss or a standing elite reads as dread --
 * the drum darkens and the frame's edge reddens -- until the last of them falls. Pure queries over
 * the run state, so the beat, the lens and the tests all read the same dread.
 */
public final class DreadWaves {
    private DreadWaves() {
    }

    /** True while the wave's dread stands: a living boss, or an elite still in the field. */
    public static boolean dreadStands(GameState state) {
        if (state == null || !state.waveActive) {
            return false;
        }
        for (Boss boss : state.aliveBosses) {
            if (boss != null && boss.alive) {
                return true;
            }
        }
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy != null && enemy.alive && enemy.eliteAffix != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * The frame's dread level: 1 while dread stands, 0 anywhere else. The composite eases toward
     * it, so dread breathes in and out instead of popping.
     */
    public static float dreadLevel(GameState state) {
        return dreadStands(state) ? 1f : 0f;
    }
}
