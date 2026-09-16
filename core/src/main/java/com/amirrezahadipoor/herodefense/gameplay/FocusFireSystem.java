package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Tap-to-focus: the player's deliberate decision inside a wave (roadmap R3.1).
 *
 * <p>Until this system the bow always picked the nearest target and the only in-run choice happened between
 * waves, so the audit's "hero is locked to the arena centre" finding held exactly. Tapping an enemy now marks
 * it for {@link #MARK_SECONDS}: the bow switches to the marked target when it is inside reach and every arrow
 * into it carries {@link #DAMAGE_MULTIPLIER}. Tapping empty ground clears the mark, so the player can undo a
 * bad pick at any time. Everything here is deterministic and platform independent, which is what lets the
 * balance sweep and the unit tests describe the mechanic instead of the developer describing it by hand.
 */
public final class FocusFireSystem {

    /** How long a mark keeps the bow pointed at it; a second tap refreshes the window. */
    public static final float MARK_SECONDS = 6f;

    /** How close a tap must land to an enemy to count as picking it. */
    public static final float TAP_RADIUS = 56f;

    /** Damage taken by a marked target compared with an unmarked one. */
    public static final float DAMAGE_MULTIPLIER = 1.2f;

    private FocusFireSystem() {
    }

    /** Ages every mark; call once per simulation step. */
    public static void tick(GameState state, float deltaSeconds) {
        if (state == null || !(deltaSeconds > 0f) || !Float.isFinite(deltaSeconds)) return;
        for (Enemy enemy : state.aliveEnemies) {
            age(enemy, deltaSeconds);
        }
        for (Boss boss : state.aliveBosses) {
            age(boss, deltaSeconds);
        }
    }

    /**
     * Marks the living enemy closest to the tapped point when the tap lands inside {@link #TAP_RADIUS}, and
     * returns it. A tap that hits nothing clears the current mark and returns {@code null}, so a miss is a
     * safe way to release the bow's target lock. Only one enemy is ever marked.
     */
    public static Enemy markAt(GameState state, float worldX, float worldY) {
        if (state == null || !Float.isFinite(worldX) || !Float.isFinite(worldY)) return null;
        Enemy picked = null;
        float bestDistanceSquared = TAP_RADIUS * TAP_RADIUS;
        for (Enemy enemy : state.aliveEnemies) {
            picked = better(enemy, worldX, worldY, picked, bestDistanceSquared);
            if (picked != null) bestDistanceSquared = Math.min(bestDistanceSquared, picked.distanceSquaredTo(worldX, worldY));
        }
        for (Boss boss : state.aliveBosses) {
            picked = better(boss, worldX, worldY, picked, bestDistanceSquared);
            if (picked != null) bestDistanceSquared = Math.min(bestDistanceSquared, picked.distanceSquaredTo(worldX, worldY));
        }
        clearAll(state);
        if (picked != null) picked.focusMarkSeconds = MARK_SECONDS;
        return picked;
    }

    /** Drops every mark, for example when a wave ends or the player taps empty ground. */
    public static void clearAll(GameState state) {
        if (state == null) return;
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy != null) enemy.focusMarkSeconds = 0f;
        }
        for (Boss boss : state.aliveBosses) {
            if (boss != null) boss.focusMarkSeconds = 0f;
        }
    }

    /** True while this enemy carries the player's mark. */
    public static boolean isMarked(Enemy enemy) {
        return enemy != null && enemy.alive && enemy.focusMarkSeconds > 0f;
    }

    /**
     * The marked enemy when it is still alive and inside the bow's reach, otherwise {@code null} so the caller
     * falls back to its own target selection.
     */
    public static Enemy markedTargetInRange(GameState state, float x, float y, float range) {
        if (state == null || !(range > 0f)) return null;
        float maximumDistanceSquared = range * range;
        for (Enemy enemy : state.aliveEnemies) {
            if (inReach(enemy, x, y, maximumDistanceSquared)) return enemy;
        }
        for (Boss boss : state.aliveBosses) {
            if (inReach(boss, x, y, maximumDistanceSquared)) return boss;
        }
        return null;
    }

    /** True when at least one living enemy still carries a mark, used by the HUD and the presentation layer. */
    public static boolean hasMarkedTarget(GameState state) {
        if (state == null) return false;
        for (Enemy enemy : state.aliveEnemies) {
            if (isMarked(enemy)) return true;
        }
        for (Boss boss : state.aliveBosses) {
            if (isMarked(boss)) return true;
        }
        return false;
    }

    private static void age(Enemy enemy, float deltaSeconds) {
        if (enemy != null && enemy.focusMarkSeconds > 0f) {
            enemy.focusMarkSeconds = Math.max(0f, enemy.focusMarkSeconds - deltaSeconds);
        }
    }

    private static boolean inReach(Enemy enemy, float x, float y, float maximumDistanceSquared) {
        return isMarked(enemy) && enemy.active && !enemy.silentWatcher
            && enemy.distanceSquaredTo(x, y) <= maximumDistanceSquared;
    }

    private static Enemy better(Enemy candidate, float x, float y, Enemy current, float bestDistanceSquared) {
        if (candidate == null || !candidate.alive || !candidate.active || candidate.silentWatcher) return current;
        float distance = candidate.distanceSquaredTo(x, y);
        if (distance > TAP_RADIUS * TAP_RADIUS) return current;
        if (current == null || distance < bestDistanceSquared
            || (distance == bestDistanceSquared && candidate.id < current.id)) {
            return candidate;
        }
        return current;
    }
}
