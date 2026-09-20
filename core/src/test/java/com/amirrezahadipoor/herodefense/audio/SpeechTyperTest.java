package com.amirrezahadipoor.herodefense.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class SpeechTyperTest {
    @Test
    void theFirstBlipLandsImmediatelyAndTheRestTypeOutOverTime() {
        List<AudioCue> played = new ArrayList<>();
        SpeechTyper typer = new SpeechTyper(played::add);

        // Four words: one blip now, three more one per tick interval.
        typer.type("The Tree asks once.", SpeechVoice.TREE);
        assertEquals(1, played.size());
        assertTrue(typer.typing(), "three more blips to come");

        typer.tick(SpeechTyper.BLIP_SECONDS - 0.01f);
        assertEquals(1, played.size(), "nothing before the interval passes");

        typer.tick(0.02f);
        assertEquals(2, played.size(), "the second blip on the first crossing");

        typer.tick(SpeechTyper.BLIP_SECONDS);
        assertEquals(3, played.size(), "one blip per interval, never two at once");

        typer.tick(SpeechTyper.BLIP_SECONDS * 2);
        assertEquals(4, played.size(), "the last blip lands and the line is typed");
        assertFalse(typer.typing());

        assertTrue(played.stream().allMatch(cue -> cue == AudioCue.SPEECH_TREE), "the Tree's tone every tap");
    }

    @Test
    void aBlankOrNullLineStaysSilent() {
        List<AudioCue> played = new ArrayList<>();
        SpeechTyper typer = new SpeechTyper(played::add);
        typer.type(null, SpeechVoice.HERO);
        typer.type("   ", SpeechVoice.HERO);
        typer.tick(1f);
        assertEquals(0, played.size());
        assertFalse(typer.typing());
    }
}
