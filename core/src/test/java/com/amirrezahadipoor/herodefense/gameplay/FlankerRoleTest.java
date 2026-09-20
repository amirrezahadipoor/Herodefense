package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/** A flanker spits bark-acid at a tree it passes -- one bite, scarred but never felled. */
class FlankerRoleTest {

    private GameState groveState(int wave, long seed) {
        GameState state = GameState.newRun(seed);
        state.waveNumber = wave;
        state.plantedTreesCount = 1;
        state.plantedTreeHealth.clear();
        state.plantedTreeMaxHealth.clear();
        state.plantedTreeHealth.add(1000f);
        state.plantedTreeMaxHealth.add(1000f);
        return state;
    }

    /** A body id whose deterministic hash makes it a flanker of this exact run. */
    private long flankingId(long seed) {
        for (long id = 1L; id < 10_000L; id++) {
            if (EnemyRoleSystem.isFlankerAssignment(seed, id)) {
                return id;
            }
        }
        throw new IllegalStateException("no flanker id in range");
    }

    private Enemy flankerAt(GameState state, float x, float y) {
        Enemy enemy = new Enemy(flankingId(state.runSeed), "ROOTLING", x, y);
        state.aliveEnemies.add(enemy);
        return enemy;
    }

    @Test
    void assignmentIsDeterministicAndBounded() {
        boolean any = false;
        boolean none = false;
        for (long id = 1L; id <= 400L; id++) {
            boolean first = EnemyRoleSystem.isFlankerAssignment(77L, id);
            assertEquals(first, EnemyRoleSystem.isFlankerAssignment(77L, id));
            any |= first;
            none |= !first;
        }
        assertTrue(any && none, "both kinds must occur across the roster");
    }

    @Test
    void aFlankerBitesATreeItPassesExactlyOnce() {
        GameState state = groveState(191, 5L);
        Enemy enemy = flankerAt(state, WorldLayout.groveTreeX(0), WorldLayout.groveTreeY(0) + 30f);
        EnemyRoleSystem.update(state, 0.016f);
        assertEquals(1000f - 1000f * EnemyRoleSystem.FLANK_BITE_SHARE, state.getTreeHealth(1), 0.0001f);
        assertTrue(enemy.flanker, "the raider wears its bite");
        for (int tick = 0; tick < 20; tick++) {
            EnemyRoleSystem.update(state, 0.016f);
        }
        assertEquals(1000f - 1000f * EnemyRoleSystem.FLANK_BITE_SHARE,
            state.getTreeHealth(1), 0.0001f);
    }

    @Test
    void aBiteNeverFellsTheTree() {
        GameState state = groveState(191, 6L);
        state.setTreeHealth(1, EnemyRoleSystem.TREE_BITE_HEALTH_FLOOR);
        flankerAt(state, WorldLayout.groveTreeX(0), WorldLayout.groveTreeY(0));
        EnemyRoleSystem.update(state, 0.016f);
        assertEquals(EnemyRoleSystem.TREE_BITE_HEALTH_FLOOR, state.getTreeHealth(1), 0.0001f);
    }

    @Test
    void aFlankerFarFromEveryTreeKeepsMarching() {
        GameState state = groveState(191, 7L);
        Enemy enemy = flankerAt(state, WorldLayout.groveTreeX(0) + 400f, 100f);
        EnemyRoleSystem.update(state, 0.016f);
        assertFalse(enemy.flanker);
        assertEquals(1000f, state.getTreeHealth(1), 0.0001f);
    }

    @Test
    void belowTheFlankWaveNothingBites() {
        GameState state = groveState(190, 8L);
        Enemy enemy = flankerAt(state, WorldLayout.groveTreeX(0), WorldLayout.groveTreeY(0));
        EnemyRoleSystem.update(state, 0.016f);
        assertFalse(enemy.flanker);
        assertEquals(1000f, state.getTreeHealth(1), 0.0001f);
    }
}
