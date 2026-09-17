package com.amirrezahadipoor.herodefense.audio;

/**
 * An equal-cover gain ramp between two music beds (roadmap R6.3).
 *
 * <p>A cut between tracks is audible as a click and a gap; a crossfade costs two decoders for less than a
 * second. The invariant the rest of the audio code relies on is in {@link #incoming()} and
 * {@link #outgoing()}: the two gains always sum to one, so two beds can never add up to a louder track than
 * either of them.
 */
public final class Crossfade {

    /** How long a change of bed takes. Long enough to hide the seam, short enough to follow the game. */
    public static final float SECONDS = 0.8f;

    private float progress = 1f;

    /** Begins a fade: the incoming bed starts silent and reaches full gain in {@link #SECONDS}. */
    public void start() {
        progress = 0f;
    }

    /** Advances the ramp by real (not simulation) time, clamped so a stall cannot overshoot the end. */
    public void advance(float realDeltaSeconds) {
        if (realDeltaSeconds <= 0f) return;
        progress = Math.min(1f, progress + realDeltaSeconds / SECONDS);
    }

    /** Gain of the bed being faded in, 0..1. */
    public float incoming() {
        return progress;
    }

    /** Gain of the bed being faded out, 0..1; the two always sum to one. */
    public float outgoing() {
        return 1f - progress;
    }

    /** True while both beds are audible, i.e. while a fade is in progress. */
    public boolean fading() {
        return progress < 1f;
    }
}
