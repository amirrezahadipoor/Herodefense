package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Removes finished bodies from the run's two actor lists (roadmap B4).
 *
 * <p>Before this system existed, nothing inside a run ever took a body out of {@code state.aliveEnemies}: the only
 * clear on that list was {@code ContinuousWaveRun.clearTransientCombatEntities}, which runs once when a run
 * starts. A two-hundred-wave run therefore ended with every body it had ever spawned still in the list -- measured
 * by {@code Probe4} at 3,512 bodies -- and because {@code GameStateCodec} serializes the whole {@code GameState}
 * with {@code Json.prettyPrint}, that was also the size of the save file: 3.07 MB of JSON, re-encoded and written
 * twice (primary and backup) on the frame thread at the end of a run, and once per level-up during it.
 *
 * <p>Three things follow from the fix, and they are the reason this is a system and not a line inside the wave
 * lifecycle:
 *
 * <ul>
 *   <li><b>A corpse is presentation, so it is allowed to exist -- for exactly as long as its presentation
 *       lasts.</b> {@link #CORPSE_LINGER_SECONDS} is the window in which the death particles, the audio cue and
 *       the loot flush read the body. Everything that reads a corpse does so within a frame or two of the kill
 *       ({@code IdentityCues.newestCorpseType}, the three {@code processDefeatedEnemies} passes), so the window is
 *       generous rather than tight.</li>
 *   <li><b>The lists are bounded by construction.</b> {@link #MAX_BODIES_IN_STATE} caps what a burst can leave
 *       behind even if the linger has not elapsed, so the save size is a constant of the design rather than a
 *       function of how long the run has been going.</li>
 *   <li><b>The reaper runs where the other per-frame systems run.</b> {@code CombatSystem.update} calls it last,
 *       after the loot passes, so nothing that wants a corpse can be beaten to it by the reaper -- the ordering is
 *       the contract, and {@code ReaperSystemTest} pins it.</li>
 * </ul>
 *
 * <p>Bosses are reaped by the same rule. A run fights forty boss encounters and every one of them used to leave a
 * body in the list until the run started over.
 */
public final class ReaperSystem {

    /**
     * How long a body stays in the state after it dies. Long enough for the death particles and the loot passes,
     * short enough that the list holds one wave's worth of bodies and not one run's worth.
     */
    public static final float CORPSE_LINGER_SECONDS = 0.6f;

    /**
     * The hard ceiling on the bodies a run may carry, living and dead together. A wave spawns at most
     * {@code EnemyWaveSpawner.MAX_REGULAR_ENEMIES} plus a boss, so ninety-six is more than three waves' worth and
     * is only ever reached by corpses waiting out the linger.
     */
    public static final int MAX_BODIES_IN_STATE = 96;

    private ReaperSystem() {
    }

    /**
     * Ages every corpse by one tick and takes out the ones whose presentation has finished. Living bodies are
     * never touched: this system owns the dead, not the field.
     */
    public static void update(GameState state, float deltaSeconds) {
        if (state == null) {
            return;
        }
        float elapsed = Math.max(0f, deltaSeconds);
        if (state.aliveEnemies != null) {
            ageAndReap(state.aliveEnemies, elapsed);
        }
        if (state.aliveBosses != null) {
            ageAndReap(state.aliveBosses, elapsed);
        }
    }

    /**
     * Takes out every dead body now, whatever its age. Called when a wave is cleared, so a save written between
     * waves carries the wave's survivors and nothing else.
     */
    public static void clear(GameState state) {
        if (state == null) {
            return;
        }
        if (state.aliveEnemies != null) {
            state.aliveEnemies.removeIf(enemy -> enemy == null || !enemy.alive);
        }
        if (state.aliveBosses != null) {
            state.aliveBosses.removeIf(boss -> boss == null || !boss.alive);
        }
    }

    /** How many bodies in the state are dead; the number the footprint gate measures. */
    public static int corpseCount(GameState state) {
        if (state == null) {
            return 0;
        }
        int corpses = 0;
        if (state.aliveEnemies != null) {
            for (Enemy enemy : state.aliveEnemies) {
                if (enemy != null && !enemy.alive) {
                    corpses++;
                }
            }
        }
        if (state.aliveBosses != null) {
            for (Boss boss : state.aliveBosses) {
                if (boss != null && !boss.alive) {
                    corpses++;
                }
            }
        }
        return corpses;
    }

    /** Total bodies the run is carrying, living and dead: the list size a save pays for. */
    public static int bodyCount(GameState state) {
        if (state == null) {
            return 0;
        }
        int total = 0;
        if (state.aliveEnemies != null) {
            total += state.aliveEnemies.size();
        }
        if (state.aliveBosses != null) {
            total += state.aliveBosses.size();
        }
        return total;
    }

    private static void ageAndReap(java.util.List<? extends Enemy> bodies, float elapsed) {
        int index = 0;
        while (index < bodies.size()) {
            Enemy body = bodies.get(index);
            if (body == null) {
                bodies.remove(index);
                continue;
            }
            if (body.alive) {
                index++;
                continue;
            }
            body.deadSeconds += elapsed;
            if (body.deadSeconds >= CORPSE_LINGER_SECONDS) {
                bodies.remove(index);
                continue;
            }
            index++;
        }
        // The cap is the belt to the linger's braces: a burst of deaths in one tick cannot leave the state
        // larger than the budget, even if every one of those bodies is still inside its window.
        int overflow = bodies.size() - MAX_BODIES_IN_STATE;
        if (overflow > 0) {
            for (int taken = 0; taken < overflow; taken++) {
                int oldestDead = oldestDeadIndex(bodies);
                bodies.remove(oldestDead < 0 ? 0 : oldestDead);
            }
        }
    }

    private static int oldestDeadIndex(java.util.List<? extends Enemy> bodies) {
        int bestIndex = -1;
        float bestAge = Float.NEGATIVE_INFINITY;
        for (int index = 0; index < bodies.size(); index++) {
            Enemy body = bodies.get(index);
            if (body != null && !body.alive && body.deadSeconds > bestAge) {
                bestAge = body.deadSeconds;
                bestIndex = index;
            }
        }
        return bestIndex;
    }
}
