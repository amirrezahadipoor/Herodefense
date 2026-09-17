package com.amirrezahadipoor.herodefense.audio;

/**
 * What the platform's audio focus means for us (roadmap R6.4).
 *
 * <p>Two events, two different answers, and the difference is the whole point. {@code LOSS} is somebody else's
 * turn: a call, another app's music -- we stop and stay stopped until they are done. {@code TRANSIENT_LOSS} is
 * borrowed focus: a notification or a short prompt -- the music steps back to a quarter and the effects keep
 * playing, because a game that goes silent for a notification is a game that looks broken.
 *
 * <p>Immutable, so the manager can hold one value rather than remember a sequence of events; the listener on
 * the platform side turns Android's codes into these two.
 */
public final class AudioFocusState {

    /** What the platform told us. */
    public enum Event {
        GAIN,
        TRANSIENT_LOSS,
        LOSS
    }

    private static final AudioFocusState GAINED = new AudioFocusState(false, false);
    private static final AudioFocusState TRANSIENT = new AudioFocusState(true, false);
    private static final AudioFocusState LOST = new AudioFocusState(true, true);

    private final boolean ducked;
    private final boolean silent;

    private AudioFocusState(boolean ducked, boolean silent) {
        this.ducked = ducked;
        this.silent = silent;
    }

    public static AudioFocusState gained() {
        return GAINED;
    }

    public AudioFocusState apply(Event event) {
        if (event == null) return this;
        return switch (event) {
            case GAIN -> GAINED;
            case TRANSIENT_LOSS -> TRANSIENT;
            case LOSS -> LOST;
        };
    }

    /** Gain multiplier for music: full when we have focus, a quarter when it is borrowed, zero when lost. */
    public float musicGain() {
        if (silent) return 0f;
        return ducked ? 0.25f : 1f;
    }

    /** Whether the music is allowed to run at all. */
    public boolean musicAudible() {
        return musicGain() > 0f;
    }

    /** Effects survive borrowed focus; they stop only when focus is gone for good. */
    public boolean effectsAudible() {
        return !silent;
    }

    /** Whether the system, not the player, is currently holding the audio. */
    public boolean silenced() {
        return silent;
    }
}
