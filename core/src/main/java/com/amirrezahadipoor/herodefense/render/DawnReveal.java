package com.amirrezahadipoor.herodefense.render;

/**
 * The dawn after the last night: the victory sky's clock.
 *
 * <p>The run's colour arc (see {@link StageGrade}) begins in DAWN light at wave 1, sinks to the
 * HOLLOW's darkest stop at wave 200, and the victory is the arc closing: when the run completes,
 * the sky behind the premium summary breaks from night into dawn gold over
 * {@link #DAWN_SECONDS}. This class is the pure part of that -- the clock and the easing -- which
 * keeps the narrative timing (how far the sky has come when the Tree's victory line has finished
 * typing) testable without a GL context. The shader pass that paints it is {@link DawnGlowRenderer}.
 */
public final class DawnReveal {

    /** How long the sky takes to break. Slower than the defeat reveal on purpose: a sunrise is not a button. */
    public static final float DAWN_SECONDS = 8f;

    /** The victory line types in roughly this many seconds of real time, box hold included. */
    public static final float VICTORY_WORD_SECONDS = 6f;

    private float seconds;

    public void tick(float deltaSeconds) {
        if (Float.isFinite(deltaSeconds) && deltaSeconds > 0f) {
            seconds += deltaSeconds;
        }
    }

    public void reset() {
        seconds = 0f;
    }

    /** Raw 0..1: how far along the sunrise, before easing. */
    public float progress() {
        if (!Float.isFinite(seconds) || seconds <= 0f) {
            return 0f;
        }
        return Math.min(1f, seconds / DAWN_SECONDS);
    }

    /**
     * The sky's own curve: smoothstep. It starts barely visible, which is the point -- the player
     * reads the Tree's word before they notice the light -- and it settles the same way, so a dawn
     * that is done still looks like it could breathe.
     */
    public static float ease(float raw) {
        float p = Math.max(0f, Math.min(1f, raw));
        return p * p * (3f - 2f * p);
    }

    /** The eased sky at a point in the sunrise, given its raw progress. */
    public float eased() {
        return ease(progress());
    }
}
