package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.story.BreatherLines;

/**
 * A milestone wave's breather (MEMORY P6c): Pip's one-line beat in the dialogue box while the
 * arena holds its breath for 1.2 seconds, then the next wave starts. Like the boss intro this
 * is a pure timeline the flow drives with {@link #update(float)}; a reload replays it from
 * the first frame, and the simulator completes it instantly without spending its seconds.
 */
public final class BreatherCinematic {

    /** How long the arena holds its breath: one line's worth. */
    public static final float BREATHER_SECONDS = 1.2f;
    /** A backgrounded frame must never fast-forward the show. */
    public static final float MAX_STEP_SECONDS = 0.1f;

    /** Timeline phases: one beat, then done. */
    public enum Phase {
        IDLE,
        BEAT,
        DONE
    }

    private boolean active;
    private float elapsedSeconds;
    private int waveNumber;
    private String line = "";

    /** Starts the breather for a cleared wave, snapshotting Pip's line in the game's language. */
    public void begin(int waveNumber) {
        this.waveNumber = waveNumber;
        String spoken = BreatherLines.lineFor(waveNumber);
        line = spoken == null ? "" : spoken;
        elapsedSeconds = 0f;
        active = true;
    }

    /**
     * Advances the show; returns true on the frame it finishes. Tap and back-button skips call
     * {@link #skip()} instead, and get here on the next frame.
     */
    public boolean update(float deltaSeconds) {
        if (!active) {
            return false;
        }
        elapsedSeconds += Math.min(Math.max(deltaSeconds, 0f), MAX_STEP_SECONDS);
        if (elapsedSeconds >= BREATHER_SECONDS) {
            active = false;
            return true;
        }
        return false;
    }

    /** Jumps to the end; the next {@link #update(float)} reports the finish. */
    public void skip() {
        if (active) {
            elapsedSeconds = BREATHER_SECONDS;
        }
    }

    public boolean isActive() {
        return active;
    }

    public float elapsedSeconds() {
        return elapsedSeconds;
    }

    /** The cleared milestone wave this breather breathes for. */
    public int waveNumber() {
        return waveNumber;
    }

    /** What the box reads now: Pip's line during the beat, null outside it. */
    public String line() {
        return phase() == Phase.BEAT ? line : null;
    }

    public Phase phase() {
        if (!active && elapsedSeconds <= 0f) {
            return Phase.IDLE;
        }
        if (elapsedSeconds >= BREATHER_SECONDS) {
            return Phase.DONE;
        }
        return Phase.BEAT;
    }
}
