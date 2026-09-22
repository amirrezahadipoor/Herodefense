package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * The gameplay half of a boss's evolution crossing.
 *
 * <p>The presentation already existed: at the fight's evolution threshold a crimson-gold ring fires from the
 * body (roadmap C2). Nothing followed it. The ring said "something changed" and no number changed with it, which
 * is the same failure the audit found in the boss special before A5 -- the loudest moment in a fight being
 * decoration.
 *
 * <p>What changes here is deliberately small and legible, because a boss's second half must be readable rather
 * than merely heavier: its specials come back sooner, it walks faster, and it hits harder -- the three things a
 * player already tracks, moved by amounts a player can feel without a spreadsheet. The crossing itself stays
 * exactly once per fight and stays the fight script's own threshold, so the phase boundary is the same one the
 * ring has always drawn.
 */
public final class BossEvolution {

    /** Special cadence in the second half. */
    public static final float ENRAGED_CADENCE_MULTIPLIER = 0.85f;
    /** Movement speed in the second half. */
    public static final float ENRAGED_SPEED_MULTIPLIER = 1.12f;
    /** Contact damage in the second half. */
    public static final float ENRAGED_DAMAGE_MULTIPLIER = 1.15f;
    /** The evolution cadence a second-half boss is allowed to reach, in seconds. */
    public static final float MIN_SPECIAL_INTERVAL_SECONDS = 1.6f;

    private BossEvolution() {
    }

    /**
     * Latches every fight that has crossed its own evolution threshold. Returns the number of bosses that evolved
     * on this frame, which the caller uses to fire the presentation beat exactly once.
     */
    public static int update(GameState state) {
        if (state == null || state.aliveBosses == null) {
            return 0;
        }
        int evolved = 0;
        for (Boss boss : state.aliveBosses) {
            if (boss == null || !boss.alive || boss.evolutionApplied || boss.maxHealth <= 0f) {
                continue;
            }
            if (boss.health > boss.maxHealth * BossFightScript.of(boss).evolutionHealthRatio()) {
                continue;
            }
            boss.evolutionApplied = true;
            boss.enraged = true;
            boss.movementSpeed *= ENRAGED_SPEED_MULTIPLIER;
            boss.damage *= ENRAGED_DAMAGE_MULTIPLIER;
            evolved++;
        }
        return evolved;
    }

    /** The special cooldown a second-half boss waits, or {@code base} while it is still in its first half. */
    public static float specialInterval(Boss boss, float base) {
        if (boss == null || !boss.enraged) {
            return base;
        }
        return Math.max(MIN_SPECIAL_INTERVAL_SECONDS, base * ENRAGED_CADENCE_MULTIPLIER);
    }
}
