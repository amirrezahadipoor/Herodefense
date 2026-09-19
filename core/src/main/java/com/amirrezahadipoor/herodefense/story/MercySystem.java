package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.gameplay.FocusFireSystem;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Mercy (roadmap ST3): the Rootlings that only stand and watch can be greeted instead of ignored.
 * Three taps on the same watcher and it bows and leaves the field alive -- the run's one act of
 * sparing, and the only enemy that can leave without dying. A spared watcher forgoes everything a
 * kill would have paid: mercy here is its own answer, which is exactly what the Hollow has an
 * opinion about.
 *
 * <p>Tapping shares the bow's tap radius with focus-fire; a watcher is never a valid mark, so the
 * two gestures cannot collide. Progress on a half-greeted watcher is per-encounter, not persisted:
 * the run either completes the greeting in the moment or the watcher keeps watching.
 */
public final class MercySystem {

    /** How many greetings a watcher needs before it bows and departs. */
    public static final int GREETINGS_REQUIRED = 3;

    /** How long the focus brackets flash on a greeted watcher, as the tap's receipt. */
    public static final float GREET_MARK_SECONDS = 0.6f;

    /** What one tap on a watcher decided. */
    public enum Result {
        /** The tap landed on ordinary ground or a fighting enemy: no mercy applies. */
        NONE,
        /** The watcher acknowledged one greeting; it needs more before it departs. */
        GREETED,
        /** The watcher bowed, left the field alive, and the Hollow may comment. */
        SPARED
    }

    private MercySystem() {
    }

    /** The watcher under the tapped point, or null; the same radius focus-fire marks fighters with. */
    static Enemy watcherAt(GameState state, float worldX, float worldY) {
        if (state == null || !Float.isFinite(worldX) || !Float.isFinite(worldY)) {
            return null;
        }
        Enemy picked = null;
        float best = FocusFireSystem.TAP_RADIUS * FocusFireSystem.TAP_RADIUS;
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy == null || !enemy.alive || !enemy.silentWatcher) {
                continue;
            }
            float distance = enemy.distanceSquaredTo(worldX, worldY);
            if (distance <= best) {
                best = distance;
                picked = enemy;
            }
        }
        return picked;
    }

    /**
     * Applies one tap's greeting. A third greeting removes the watcher from the field alive, before
     * any payout sweep can claim it, so a spared creature can never become coin or experience.
     * A greeted-but-not-yet-spared watcher flashes the focus brackets once as the tap's receipt.
     */
    public static Result greet(GameState state, float worldX, float worldY) {
        Enemy watcher = watcherAt(state, worldX, worldY);
        if (watcher == null) {
            return Result.NONE;
        }
        watcher.spareTouches++;
        if (watcher.spareTouches < GREETINGS_REQUIRED) {
            watcher.focusMarkSeconds = Math.max(watcher.focusMarkSeconds, GREET_MARK_SECONDS);
            return Result.GREETED;
        }
        watcher.killRewardsGranted = true;
        state.aliveEnemies.remove(watcher);
        return Result.SPARED;
    }
}
