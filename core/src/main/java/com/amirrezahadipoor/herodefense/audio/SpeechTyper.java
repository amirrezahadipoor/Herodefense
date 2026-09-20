package com.amirrezahadipoor.herodefense.audio;

/**
 * The Undertale-style typing voice, as a ticking object (roadmap ST-voice): a story line speaks as a run of
 * short blips, one per word, the first immediately and the rest spaced out so a line taps across its first
 * moment instead of beeping at once. Nothing is spoken aloud; the ear only hears which speaker is typing.
 */
public final class SpeechTyper {

    /** Undertale's typing tap: one blip per word, never faster than this. */
    static final float BLIP_SECONDS = 0.11f;

    private final AudioPlayback playback;
    private SpeechVoice voice = SpeechVoice.HERO;
    private int blipsLeft;
    private float clock;

    public SpeechTyper(AudioPlayback playback) {
        this.playback = playback;
    }

    /** Starts typing {@code line} in {@code voice}: the first blip lands now, the rest follow on {@code tick}. */
    public void type(String line, SpeechVoice voice) {
        this.voice = voice != null ? voice : SpeechVoice.HERO;
        this.blipsLeft = SpeechBlip.blipsFor(line);
        this.clock = 0f;
        if (blipsLeft > 0 && playback != null) {
            playback.play(this.voice.cue());
            blipsLeft--;
        }
    }

    /** Advances the typing: blips land one per {@value #BLIP_SECONDS} seconds until the line is typed out. */
    public void tick(float deltaSeconds) {
        if (blipsLeft <= 0 || playback == null) {
            return;
        }
        clock += deltaSeconds;
        while (blipsLeft > 0 && clock >= BLIP_SECONDS) {
            clock -= BLIP_SECONDS;
            playback.play(voice.cue());
            blipsLeft--;
        }
    }

    /** Whether a line is still ticking out. */
    public boolean typing() {
        return blipsLeft > 0;
    }
}
