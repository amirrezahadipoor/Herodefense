package com.amirrezahadipoor.herodefense.audio;

/**
 * The Undertale-style typing voice, as a ticking object (roadmap ST-voice): a story line speaks as a run
 * of short blips, one per character, the first immediately and the rest one per
 * {@link #SECONDS_PER_CHAR}, so a line taps across its whole reading instead of beeping at once.
 * Nothing is spoken aloud; the ear only hears which speaker is typing.
 *
 * <p>The reveal count this object owns is the clock the text layer reads: the dialogue box draws exactly
 * the characters {@link #revealed()} has counted out, so the letter on the screen and the tap in the ear
 * can never drift apart.
 */
public final class SpeechTyper {

    /**
     * Undertale's typing tap: seconds per character. Fixed at the slowest speaker's blip interval so
     * every tap the typer counts passes the audio throttle and stays audible, not just the odd one.
     */
    public static final float SECONDS_PER_CHAR = 0.06f;

    private final AudioPlayback playback;
    private SpeechVoice voice = SpeechVoice.HERO;
    private int totalChars;
    private int revealed;
    private float clock;

    public SpeechTyper(AudioPlayback playback) {
        this.playback = playback;
    }

    /** Starts typing {@code line} in {@code voice}: the first character lands now, the rest follow on time. */
    public void type(String line, SpeechVoice voice) {
        this.voice = voice != null ? voice : SpeechVoice.HERO;
        this.totalChars = line == null || line.isBlank() ? 0 : line.length();
        this.revealed = 0;
        this.clock = 0f;
        if (totalChars > 0 && playback != null) {
            revealed = 1;
            playback.play(this.voice.cue());
        }
    }

    /** The drift float leaves behind on an exact sum; a whole line is not held hostage to it. */
    private static final float EPSILON = 1e-4f;

    /** Advances the typing: each newly revealed character taps the voice's blip. */
    public void tick(float deltaSeconds) {
        if (revealed >= totalChars || playback == null) {
            return;
        }
        clock += deltaSeconds;
        while (revealed < totalChars && clock >= SECONDS_PER_CHAR - EPSILON) {
            clock -= SECONDS_PER_CHAR;
            revealed++;
            playback.play(voice.cue());
        }
    }

    /** Tap-to-skip: the rest of the line lands at once under one closing blip. */
    public void skipToEnd() {
        if (revealed < totalChars && playback != null) {
            playback.play(voice.cue());
        }
        revealed = totalChars;
        clock = 0f;
    }

    /** How many characters of the current line have been typed out; the text layer draws exactly this many. */
    public int revealed() {
        return revealed;
    }

    /** Whether a line is still ticking out. */
    public boolean typing() {
        return revealed < totalChars;
    }
}
