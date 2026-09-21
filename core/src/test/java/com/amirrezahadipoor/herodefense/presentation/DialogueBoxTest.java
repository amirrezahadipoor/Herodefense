package com.amirrezahadipoor.herodefense.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.SpeechTyper;
import com.amirrezahadipoor.herodefense.audio.SpeechVoice;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The box one message at a time: what types out, when the arena gets back, what a tap does, and what a
 * sticky box (the opening, the ceremony) refuses to do on its own.
 */
final class DialogueBoxTest {

    /** "Once more." is ten characters, so its whole life is countable. */
    private static final String LINE = "Once more.";

    private final List<AudioCue> played = new ArrayList<>();
    private final DialogueBox box = new DialogueBox(played::add);

    private static float typingSeconds(String line) {
        return (line.length() - 1) * SpeechTyper.SECONDS_PER_CHAR;
    }

    @Test
    void aLineTypesOutOneCharacterAtATimeThenHoldsThenCloses() {
        box.speak(LINE, SpeechVoice.HERO, DialogueBox.Source.BEAT);
        assertEquals(1, box.revealedText().length(), "the first character lands with the box");
        assertEquals(1, played.size(), "and it blips in the speaker's tone");

        box.tick(typingSeconds(LINE), true);
        assertEquals(LINE, box.revealedText(), "the line is fully typed");
        assertFalse(box.typing());
        assertEquals(LINE.length(), played.size(), "one blip per character, the whole line");

        float life = typingSeconds(LINE) + DialogueBox.HOLD_SECONDS + DialogueBox.FADE_OUT_SECONDS;
        box.tick(life, true);
        assertFalse(box.active(), "the box closes itself once the reading is over");
        assertEquals(0f, box.alpha(), "and nothing of it is left to draw");
    }

    @Test
    void aTapFinishesTheTypingAndTheNextTapClosesTheBox() {
        box.speak("A long line that the eye follows word by word.", SpeechVoice.HOLLOW, DialogueBox.Source.BEAT);
        assertTrue(box.typing());

        box.advance();
        assertFalse(box.typing(), "the first tap lands the rest at once");
        assertTrue(box.active(), "but the line is still up for the reading");

        box.advance();
        assertTrue(box.fading(), "the second tap closes the box");
        box.tick(DialogueBox.FADE_OUT_SECONDS, true);
        assertFalse(box.active());

        box.advance();
        assertFalse(box.active(), "a tap on a closed box does nothing");
    }

    @Test
    void aStickyBoxNeverClosesOnItsOwn() {
        box.setSticky(true);
        box.speak(LINE, SpeechVoice.TREE, DialogueBox.Source.BEAT);

        float life = typingSeconds(LINE) + DialogueBox.HOLD_SECONDS * 5 + DialogueBox.FADE_OUT_SECONDS;
        box.tick(life, true);
        assertTrue(box.active(), "a sticky box outlives any reading time");
        assertFalse(box.fading());

        box.advance();
        assertTrue(box.fading(), "a tap still closes it");
        box.tick(DialogueBox.FADE_OUT_SECONDS, true);
        assertFalse(box.active());

        box.setSticky(true);
        box.speak("Again.", SpeechVoice.TREE, DialogueBox.Source.BEAT);
        assertTrue(box.active(), "stickiness is the box's own, not the line's");
    }

    @Test
    void withoutExpiryTheBoxHoldsNoMatterHowLongTheSceneRuns() {
        box.speak(LINE, SpeechVoice.HERO, DialogueBox.Source.BEAT);
        box.tick(typingSeconds(LINE) + DialogueBox.HOLD_SECONDS * 3, false);
        assertTrue(box.active(), "the box waits for the flow to say the arena is the screen");
        box.tick(0.01f, true);
        assertTrue(box.active(), "and starts its reading once the flow allows it");
    }

    @Test
    void aNewLineReplacesTheOldOneInsteadOfQueueing() {
        box.speak(LINE, SpeechVoice.HERO, DialogueBox.Source.BEAT);
        box.tick(0.2f, true);
        assertTrue(box.typing());

        box.speak("New line.", SpeechVoice.HOLLOW, DialogueBox.Source.BEAT);
        assertEquals("New line.", box.text());
        assertEquals(SpeechVoice.HOLLOW, box.voice());
        assertEquals(1, box.revealedText().length(), "the new line types from its own first character");
        assertFalse(box.fading());
    }

    @Test
    void theSourceTellsTheBeatFromTheWhisperAndSecondsRunOnOneClock() {
        box.speak("A whisper.", SpeechVoice.TREE, DialogueBox.Source.WHISPER);
        assertEquals(DialogueBox.Source.WHISPER, box.source());
        assertEquals("A whisper.", box.text());
        box.tick(0.5f, true);
        assertEquals(0.5f, box.seconds(), 1e-4f);

        box.speak("A beat.", SpeechVoice.HERO, DialogueBox.Source.BEAT);
        assertEquals(DialogueBox.Source.BEAT, box.source());
        assertEquals(0f, box.seconds(), 1e-4f, "a new line restarts the clock");
    }

    @Test
    void aBlankOrNullLineNeverOpensTheBox() {
        box.speak(null, SpeechVoice.HERO, DialogueBox.Source.BEAT);
        box.speak("   ", SpeechVoice.HERO, DialogueBox.Source.BEAT);
        assertFalse(box.active());
        assertEquals(0, played.size());
    }

    @Test
    void theEnvelopeSlidesInHoldsAndFadesOut() {
        box.speak(LINE, SpeechVoice.HERO, DialogueBox.Source.BEAT);
        assertTrue(box.alpha() < 0.5f, "the box is still sliding in");
        assertTrue(box.slide() < 1f);

        box.tick(DialogueBox.SLIDE_IN_SECONDS, true);
        assertEquals(1f, box.alpha(), 1e-4f);
        assertEquals(1f, box.slide(), 1e-4f);

        box.tick(typingSeconds(LINE) + DialogueBox.HOLD_SECONDS, true);
        assertTrue(box.fading());
        assertTrue(box.alpha() < 1f, "the closing fade is already under it");
        box.tick(DialogueBox.FADE_OUT_SECONDS, true);
        assertEquals(0f, box.alpha());
    }

    @Test
    void theHeldClockDrivesTheClosingMarkerAndResetWithEveryLine() {
        box.speak(LINE, SpeechVoice.HERO, DialogueBox.Source.BEAT);
        assertEquals(0f, box.heldSeconds(), 1e-4f, "nothing is held while the line still types");
        box.tick(typingSeconds(LINE), true);
        assertEquals(0f, box.heldSeconds(), 1e-4f);
        box.tick(0.3f, true);
        assertEquals(0.3f, box.heldSeconds(), 1e-4f);

        box.speak("Again.", SpeechVoice.HERO, DialogueBox.Source.BEAT);
        assertEquals(0f, box.heldSeconds(), 1e-4f, "a new line starts an unheld reading");
    }
}
