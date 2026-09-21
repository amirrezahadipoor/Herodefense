package com.amirrezahadipoor.herodefense.render;

/**
 * The Hollow's gaze: its two lights in the dark of the HOLLOW arena (waves 101-200).
 *
 * <p>The story already says it is watching -- "I felt it happen", "I never get tired of watching" --
 * and the arena's dark upper field is the one place the reviewed art leaves empty. The gaze lives
 * there. This class is the pure part: how open the eyes are, and how the run's events move them
 * (a boss sharpens them, a defeat closes them, a victory lets them drift away into the dawn). The
 * paint is {@link HollowGazeRenderer}; the timing rules are what a test holds.
 */
public final class HollowGaze {

    /** The arena the gaze lives in: the HOLLOW arena begins here. */
    public static final int HOLLOW_WAVE = 101;
    /** By here the night has fully woken: the gaze is at its resting strength. */
    public static final int FULL_WAVE = 121;
    /** How long it takes the eyes to close (a blink, not a yawn). */
    public static final float CLOSE_SECONDS = 0.45f;
    /** How long the eyes take to drift away when the run is won. */
    public static final float DRIFT_SECONDS = 6f;
    /** While a boss is alive the gaze sharpens by this factor. */
    public static final float BOSS_SHARPEN = 1.6f;
    /** The resting strength: a whisper. The light is meant to be felt, not found. */
    public static final float RESTING_STRENGTH = 0.07f;
    /** The slow breath the resting gaze takes; a reduced-motion caller drops the swing to zero. */
    public static final float BREATH_SWING = 0.10f;
    public static final float BREATH_PERIOD_SECONDS = 7f;

    private int wave;
    private float blink = 1f;
    private float drift;
    private boolean inHollowArena;

    /**
     * Advances the gaze by one presentation frame.
     *
     * @param wave the run's wave (0 when there is no run)
     * @param bossAlive a boss standing in the arena
     * @param heroAlive the tree still holds
     * @param runComplete the run is won
     * @param deltaSeconds presentation time since the last frame
     * @param motionSuppressed a reduced-motion frame: the drift leaves twice as slowly, the
     *        breath holds still
     */
    public void update(
        int wave, boolean bossAlive, boolean heroAlive, boolean runComplete,
        float deltaSeconds, boolean motionSuppressed
    ) {
        this.wave = wave;
        boolean hollow = wave >= HOLLOW_WAVE;
        if (!hollow) {
            // The forest arena is the Tree's own: the gaze never comes down there.
            blink = 1f;
            drift = 0f;
        } else if (deltaSeconds > 0f && Float.isFinite(deltaSeconds)) {
            if (!heroAlive && !runComplete) {
                // The tree has fallen: the eyes close, quick as a blink.
                blink = Math.max(0f, blink - deltaSeconds / CLOSE_SECONDS);
            } else {
                blink = Math.min(1f, blink + deltaSeconds / (2f * CLOSE_SECONDS));
            }
            if (runComplete) {
                // The dawn is breaking: the night leaves the arena, slowly.
                float rate = motionSuppressed ? 2f : 1f;
                drift = Math.min(1f, drift + deltaSeconds / (rate * DRIFT_SECONDS));
            } else {
                drift = 0f;
            }
        }
        inHollowArena = hollow;
    }

    /** True when the gaze can be visible at all on this frame. */
    public boolean present() {
        return inHollowArena;
    }

    /** The wave the ramp last read. */
    public int wave() {
        return wave;
    }

    /**
     * How open the eyes are, 0..1: the wave ramp in, the blink out, the drift away, and the boss's
     * sharpening. A defeat reads as a blink; a victory as a slow leaving.
     */
    public float openness(boolean bossAlive) {
        if (!inHollowArena || drift >= 1f) {
            return 0f;
        }
        float waveRamp = Math.max(0f, Math.min(1f,
            (wave - HOLLOW_WAVE + 1f) / (FULL_WAVE - HOLLOW_WAVE + 1f)));
        float sharp = bossAlive ? BOSS_SHARPEN : 1f;
        return waveRamp * blink * (1f - drift) * sharp;
    }

    /** The slow breath, -1..1; 0 for a reduced-motion frame. */
    public static float breath(float timeSeconds, boolean motionSuppressed) {
        if (motionSuppressed) {
            return 0f;
        }
        return (float) Math.sin(2f * Math.PI * (timeSeconds / BREATH_PERIOD_SECONDS));
    }

    /** The strength the paint should use this frame. */
    public float strength(boolean bossAlive, float timeSeconds, boolean motionSuppressed) {
        float base = openness(bossAlive) * RESTING_STRENGTH;
        return base + base * BREATH_SWING * breath(timeSeconds, motionSuppressed);
    }

    /** How far the eyes have drifted apart on the way out, 0..1. */
    public float drift() {
        return drift;
    }
}
