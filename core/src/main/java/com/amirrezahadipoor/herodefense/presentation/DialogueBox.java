package com.amirrezahadipoor.herodefense.presentation;

import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.audio.SpeechTyper;
import com.amirrezahadipoor.herodefense.audio.SpeechVoice;

/**
 * One message at a time, the way Undertale speaks: the line types out character by character under the
 * speaker's blips, holds for the reading, then the box closes. The arena is held for the whole window --
 * the game waits for its narrator, not the other way round.
 *
 * <p>The reveal count lives in the {@link SpeechTyper} under the box, so the letters on screen and the
 * taps in the ear are the same clock: what the ear has heard is what the eye has seen. A tap finishes the
 * typing; the next tap closes the box. A {@linkplain #setSticky sticky} box never closes on its own --
 * the scene moves it (the opening, the ceremony) -- and the flow can withhold the expiry while the arena
 * is not the screen the box sits on ({@code allowExpiry}).
 */
public final class DialogueBox {

    /** What a line in the box belongs to; the frame keeps the beat, the whisper and the parting word apart. */
    public enum Source { BEAT, WHISPER, DEATH, VICTORY }

    /** The box slides up from below before the line types. */
    public static final float SLIDE_IN_SECONDS = 0.14f;
    /** After the line is fully typed, the box holds before closing: the reading time. */
    public static final float HOLD_SECONDS = 1.7f;
    /** The box's own closing fade. */
    public static final float FADE_OUT_SECONDS = 0.16f;

    private final SpeechTyper typer;

    private String text = "";
    private SpeechVoice voice = SpeechVoice.HERO;
    private Source source = Source.BEAT;
    private boolean sticky;
    private boolean fading;
    private float clock;
    private float holdClock;
    private float fadeClock;

    public DialogueBox(AudioPlayback playback) {
        this.typer = new SpeechTyper(playback);
    }

    /** Puts a new line in the box in the speaker's voice; a line still up is replaced, not queued. */
    public void speak(String line, SpeechVoice voice, Source source) {
        if (line == null || line.isBlank()) {
            return;
        }
        this.text = line;
        this.voice = voice != null ? voice : SpeechVoice.HERO;
        this.source = source;
        this.clock = 0f;
        this.holdClock = 0f;
        this.fadeClock = 0f;
        this.fading = false;
        typer.type(line, this.voice);
    }

    /** Removes the line at once, closing the box. Stickiness is the box's own property, not the line's. */
    public void clear() {
        text = "";
        voice = SpeechVoice.HERO;
        fading = false;
        clock = 0f;
        holdClock = 0f;
        fadeClock = 0f;
        typer.type("", SpeechVoice.HERO);
    }

    /** A sticky box never closes on its own; the scene changes it. */
    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    /**
     * Advances the box: the line types, then the reading time runs. A box that may expire and is not
     * sticky closes itself once the reading is over; everything else waits for the scene or a tap.
     */
    public void tick(float deltaSeconds, boolean allowExpiry) {
        if (text.isEmpty()) {
            return;
        }
        typer.tick(deltaSeconds);
        clock += deltaSeconds;
        if (fading) {
            fadeClock += deltaSeconds;
            if (fadeClock >= FADE_OUT_SECONDS) {
                clear();
            }
            return;
        }
        // The reading time is read off the frame clock, not accumulated per frame: the moment the typing
        // ends is (n-1) intervals after the box opened, and nothing about a split frame can blur it.
        float typingDone = (text.length() - 1) * SpeechTyper.SECONDS_PER_CHAR;
        float holdBefore = holdClock;
        holdClock = Math.max(0f, clock - typingDone);
        if (holdBefore < HOLD_SECONDS && holdClock >= HOLD_SECONDS
            && allowExpiry && !sticky && !typer.typing()) {
            // The fade starts in this same frame, at the point past the reading the frame already ran.
            fading = true;
            fadeClock = holdClock - HOLD_SECONDS;
        }
        if (fading && fadeClock >= FADE_OUT_SECONDS) {
            clear();
        }
    }

    /** A tap on the box: the first finishes the typing, the next closes it. */
    public void advance() {
        if (text.isEmpty() || fading) {
            return;
        }
        if (typer.typing()) {
            typer.skipToEnd();
            return;
        }
        fading = true;
        fadeClock = 0f;
    }

    /** Whether a line is up, typing or holding. */
    public boolean active() {
        return !text.isEmpty();
    }

    /** The whole line, whatever has been typed of it. */
    public String text() {
        return text;
    }

    /** The part of the line typed so far; what the box draws. */
    public String revealedText() {
        return text.substring(0, Math.min(typer.revealed(), text.length()));
    }

    /** The speaker the line is up in. */
    public SpeechVoice voice() {
        return voice;
    }

    /** What the line belongs to. */
    public Source source() {
        return source;
    }

    /** Whether the line is still typing out. */
    public boolean typing() {
        return typer.typing();
    }

    /** Whether the box is in its closing fade. */
    public boolean fading() {
        return fading;
    }

    /** Seconds since the box opened; the old beat/whisper timers, one clock now. */
    public float seconds() {
        return clock;
    }

    /** How long the fully typed line has been on screen; the closing marker's blink clock. */
    public float heldSeconds() {
        return holdClock;
    }

    /** 0..1: the slide-in against the closing fade, the box's own visibility envelope. */
    public float alpha() {
        if (text.isEmpty()) {
            return 0f;
        }
        float in = Math.min(1f, clock / SLIDE_IN_SECONDS);
        float out = fading ? Math.max(0f, 1f - fadeClock / FADE_OUT_SECONDS) : 1f;
        return in * out;
    }

    /** 0..1: how far the box has slid into place. */
    public float slide() {
        return text.isEmpty() ? 0f : Math.min(1f, clock / SLIDE_IN_SECONDS);
    }
}
