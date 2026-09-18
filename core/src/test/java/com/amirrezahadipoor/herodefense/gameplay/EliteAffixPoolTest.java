package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Roadmap D2's pool gate, pinned from both sides: the shallow run may only ever draw the three affixes the
 * frozen balance baseline was measured on -- the pool size below LATE_AFFIX_WAVE is the same 3 it has always
 * been, so every early and mid roll is bit-identical (the B2a lesson) -- and the deep run must actually see
 * the doubled pool, or the new three are decoration.
 */
final class EliteAffixPoolTest {
    private static final Set<String> BASE_THREE = Set.of("blightburst", "rootward_ward", "weeping_rot");
    private final EnemyWaveSpawner spawner = new EnemyWaveSpawner(new EnemyFactory());

    private Set<String> affixesSeen(long seed, int fromWave, int toWave) {
        Set<String> seen = new HashSet<>();
        for (int wave = fromWave; wave <= toWave; wave++) {
            GameState state = GameState.newRun(seed);
            spawner.spawnRegularEnemies(state, wave, 8);
            for (Enemy enemy : state.aliveEnemies) {
                if (enemy.eliteAffix != null) {
                    seen.add(enemy.eliteAffix);
                }
            }
        }
        return seen;
    }

    @Test
    void theShallowRunOnlyEverDrawsTheShippedThree() {
        for (long seed = 1L; seed <= 3L; seed++) {
            Set<String> seen = affixesSeen(seed, 1, EnemyWaveSpawner.LATE_AFFIX_WAVE - 1);
            assertTrue(seen.size() >= 2, "seed " + seed + " met no elites at all: " + seen);
            assertTrue(BASE_THREE.containsAll(seen),
                "seed " + seed + " drew a late affix before wave " + EnemyWaveSpawner.LATE_AFFIX_WAVE
                    + ": " + seen);
        }
    }

    @Test
    void theDeepRunOpensTheDoubledPool() {
        Set<String> seen = new HashSet<>();
        for (long seed = 1L; seed <= 3L; seed++) {
            seen.addAll(affixesSeen(seed, EnemyWaveSpawner.LATE_AFFIX_WAVE, 200));
        }
        assertTrue(seen.contains("hollowmolt"), "the deep run never met hollowmolt: " + seen);
        assertTrue(seen.contains("gravemoss"), "the deep run never met gravemoss: " + seen);
        assertTrue(seen.contains("cinderhalo"), "the deep run never met cinderhalo: " + seen);
        assertTrue(BASE_THREE.containsAll(seen) || seen.size() > 3,
            "the deep pool should carry all six: " + seen);
    }
}
