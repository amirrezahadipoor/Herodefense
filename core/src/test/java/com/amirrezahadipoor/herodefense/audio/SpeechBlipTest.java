package com.amirrezahadipoor.herodefense.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import org.junit.jupiter.api.Test;

final class SpeechBlipTest {
    @Test
    void everySpeakerTypesInTheirOwnCue() {
        assertEquals(AudioCue.SPEECH_HERO, SpeechVoice.HERO.cue());
        assertEquals(AudioCue.SPEECH_TREE, SpeechVoice.TREE.cue());
        assertEquals(AudioCue.SPEECH_HOLLOW, SpeechVoice.HOLLOW.cue());
    }

    @Test
    void theBlipCountFollowsTheLineAndCaps() {
        assertEquals(0, SpeechBlip.blipsFor(null));
        assertEquals(0, SpeechBlip.blipsFor("   "));
        assertEquals(1, SpeechBlip.blipsFor("Again."));
        assertEquals(4, SpeechBlip.blipsFor("The Tree asks once."));
        assertEquals(2, SpeechBlip.blipsFor("Can you?"));
        String longLine = "word ".repeat(60).trim();
        assertEquals(12, SpeechBlip.blipsFor(longLine), "a wall of text never machine-guns");
    }

    @Test
    void theTablesRouteEachSpeakerToTheirOwnVoice() {
        assertEquals(SpeechVoice.HERO, SpeechBlip.voiceFor(null));
        assertEquals(SpeechVoice.TREE, SpeechBlip.voiceFor(StoryStrings.WHISPER_ONE));
        assertEquals(SpeechVoice.TREE, SpeechBlip.voiceFor(StoryStrings.CODEX_TAB_LORE));
        assertEquals(SpeechVoice.TREE, SpeechBlip.voiceFor(StoryStrings.CEREMONY_GROW));
        assertEquals(SpeechVoice.HOLLOW, SpeechBlip.voiceFor(StoryStrings.HOLLOW_HELLO));
        assertEquals(SpeechVoice.HOLLOW, SpeechBlip.voiceFor(StoryStrings.ELITE_BLIGHTBURST_ONE));
        assertEquals(SpeechVoice.HERO, SpeechBlip.voiceFor(StoryStrings.OPENING_0_ONE));
        assertEquals(SpeechVoice.HERO, SpeechBlip.voiceFor(StoryStrings.BOSS_ANCIENT_GOLEM));
    }
}
