package com.amirrezahadipoor.herodefense.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class SpeechTyperTest {
    @Test
    void theFirstCharacterLandsImmediatelyAndTheRestTypeOutOverTime() {
        List<AudioCue> played = new ArrayList<>();
        SpeechTyper typer = new SpeechTyper(played::add);

        // Five characters: one blip now, four more one per character interval.
        typer.type("Once.", SpeechVoice.TREE);
        assertEquals(1, played.size(), "the first character blips immediately");
        assertEquals(1, typer.revealed());
        assertTrue(typer.typing(), "four more characters to come");

        typer.tick(SpeechTyper.SECONDS_PER_CHAR - 0.01f);
        assertEquals(1, typer.revealed(), "nothing before the interval passes");

        typer.tick(0.02f);
        assertEquals(2, typer.revealed(), "the second character on the first crossing");
        assertEquals(2, played.size());

        typer.tick(SpeechTyper.SECONDS_PER_CHAR);
        assertEquals(3, typer.revealed(), "one character per interval, never two at once");
        assertEquals(3, played.size());

        typer.tick(SpeechTyper.SECONDS_PER_CHAR * 2);
        assertEquals(5, typer.revealed(), "the last character lands and the line is typed");
        assertEquals(5, played.size(), "one blip per character, the whole line");
        assertFalse(typer.typing());

        assertTrue(played.stream().allMatch(cue -> cue == AudioCue.SPEECH_TREE), "the Tree's tone every tap");
    }

    @Test
    void aSkipLandsTheRestOfTheLineAtOnceUnderOneClosingBlip() {
        String line = "The long line goes on and on.";
        List<AudioCue> played = new ArrayList<>();
        SpeechTyper typer = new SpeechTyper(played::add);

        typer.type(line, SpeechVoice.HOLLOW);
        int beforeSkip = played.size();
        assertTrue(typer.typing());

        typer.skipToEnd();
        assertEquals(beforeSkip + 1, played.size(), "one blip covers the jump");
        assertFalse(typer.typing());
        assertEquals(line.length(), typer.revealed(), "the whole line is on the clock");

        typer.tick(1f);
        assertEquals(beforeSkip + 1, played.size(), "nothing left to type");
    }

    @Test
    void aBlankOrNullLineStaysSilent() {
        List<AudioCue> played = new ArrayList<>();
        SpeechTyper typer = new SpeechTyper(played::add);
        typer.type(null, SpeechVoice.HERO);
        typer.type("   ", SpeechVoice.HERO);
        typer.tick(1f);
        assertEquals(0, played.size());
        assertEquals(0, typer.revealed());
        assertFalse(typer.typing());
    }
}
