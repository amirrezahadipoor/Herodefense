package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * The night takes the colour of the boss it is facing (roadmap ST6). While a boss stands in the
 * HOLLOW arena, the arena's whisper veil blends toward that boss's own identity colour, so the
 * whole night reads as wearing the face of what it is watching -- and the colour eases back to
 * plain night when the boss falls.
 *
 * <p>The tint is pure state: the renderer steps it with the clock it already has, and the veil is
 * the only thing that mixes it in. The forest arena (waves 1-100) is never tinted, because the
 * night that wears a boss's face is the Hollow's, and the Hollow's face lives in the second arena
 * alone. A new {@link GameState} is a new run and a new night: the caller resets the tint when
 * the state it is ticking for changes.
 */
public final class HollowNightTint {

    /** How quickly the night accepts or releases a boss's colour, per second. */
    private static final float RAMP_PER_SECOND = 1.25f;
    /** Close enough to the face that the night is wearing it: the ease snaps rather than trails. */
    private static final float SETTLED = 1e-4f;

    private float red;
    private float green;
    private float blue;
    private float strength;

    /**
     * Steps the tint one frame toward what the arena should look like now. The colour eases
     * toward the standing boss's identity so a boss handing the fight to the next one is a
     * blend, not a cut; when no boss stands, the colour freezes at the last one and only the
     * strength eases away. {@code deltaSeconds} of zero (a frozen clock, reduced motion, or the
     * stopped simulation behind a summary) holds the night exactly where it is.
     */
    public void tick(GameState state, float deltaSeconds) {
        if (deltaSeconds <= 0f) {
            return;
        }
        float step = Math.min(1f, deltaSeconds * RAMP_PER_SECOND);
        Boss boss = standingBoss(state);
        float faceStrength = boss == null ? 0f : 1f;
        strength = eased(strength, faceStrength, step);
        if (boss != null) {
            BossType face = boss.bossDefinition();
            red = eased(red, face.telegraphRed(), step);
            green = eased(green, face.telegraphGreen(), step);
            blue = eased(blue, face.telegraphBlue(), step);
        }
    }

    /** An exponential step that snaps to its target once the trail is under {@link #SETTLED}. */
    private static float eased(float current, float target, float step) {
        float next = current + (target - current) * step;
        return Math.abs(target - next) < SETTLED ? target : next;
    }

    /** A new run is a new night: the previous run's boss is no longer the night's concern. */
    public void reset() {
        red = 0f;
        green = 0f;
        blue = 0f;
        strength = 0f;
    }

    /** Whether the night is wearing a boss's face at all right now (0..1, eased). */
    public float strength() {
        return strength;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    /**
     * The boss the night is facing: the first standing boss in the HOLLOW arena, or none. The
     * forest arena's night is not the Hollow's, so it never wears a boss's face.
     */
    private static Boss standingBoss(GameState state) {
        if (state == null || !ArenaEnvironmentRenderer.isSecondArena(state.waveNumber)
            || state.aliveBosses == null) {
            return null;
        }
        for (Boss boss : state.aliveBosses) {
            if (boss != null && boss.alive) {
                return boss;
            }
        }
        return null;
    }
}
