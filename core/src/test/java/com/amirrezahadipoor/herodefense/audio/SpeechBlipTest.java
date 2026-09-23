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
        assertEquals(AudioCue.SPEECH_PIP, SpeechVoice.PIP.cue());
        assertEquals(AudioCue.SPEECH_BOSS, SpeechVoice.BOSS.cue());
    }

    @Test
    void theTablesRouteEachSpeakerToTheirOwnVoice() {
        assertEquals(SpeechVoice.HERO, SpeechBlip.voiceFor(null));
        // Pip's loud heart: openings, milestones, field notes, whispers, epilogue thirds.
        assertEquals(SpeechVoice.PIP, SpeechBlip.voiceFor(StoryStrings.OPENING_0_ONE));
        assertEquals(SpeechVoice.PIP, SpeechBlip.voiceFor(StoryStrings.REFLECTION_WAVE_25));
        assertEquals(SpeechVoice.PIP, SpeechBlip.voiceFor(StoryStrings.ELITE_BLIGHTBURST_ONE));
        assertEquals(SpeechVoice.PIP, SpeechBlip.voiceFor(StoryStrings.WHISPER_ONE));
        assertEquals(SpeechVoice.PIP, SpeechBlip.voiceFor(StoryStrings.EPILOGUE_A_THREE));
        // The Night Shift only speaks before its waves: title cards and intro scenes.
        assertEquals(SpeechVoice.BOSS, SpeechBlip.voiceFor(StoryStrings.BOSS_ANCIENT_GOLEM));
        assertEquals(SpeechVoice.BOSS, SpeechBlip.voiceFor(StoryStrings.BOSS_INTRO_ANCIENT_GOLEM_M1_1));
        assertEquals(SpeechVoice.PIP, SpeechBlip.voiceFor(StoryStrings.BOSS_INTRO_ANCIENT_GOLEM_M1_PIP));
        // Granny's calm green: the codex, the victory, the daily gift.
        assertEquals(SpeechVoice.TREE, SpeechBlip.voiceFor(StoryStrings.CODEX_TAB_LORE));
        assertEquals(SpeechVoice.TREE, SpeechBlip.voiceFor(StoryStrings.TREE_VICTORY));
        assertEquals(SpeechVoice.TREE, SpeechBlip.voiceFor(StoryStrings.DAILY_GIFT));
        // The Hollow's deadpan: taunts, verdicts, chapter-card lines.
        assertEquals(SpeechVoice.HOLLOW, SpeechBlip.voiceFor(StoryStrings.HOLLOW_HELLO));
        assertEquals(SpeechVoice.HOLLOW, SpeechBlip.voiceFor(StoryStrings.CHAPTER_2_LINE));
        // The Warden's white: deeds, epilogue thirds one and two, his victory line.
        assertEquals(SpeechVoice.HERO, SpeechBlip.voiceFor(StoryStrings.DEED_WAVE_10));
        assertEquals(SpeechVoice.HERO, SpeechBlip.voiceFor(StoryStrings.EPILOGUE_A_ONE));
        assertEquals(SpeechVoice.HERO, SpeechBlip.voiceFor(StoryStrings.VICTORY_WARDEN));
        // The plantings speak in three voices across their beats.
        assertEquals(SpeechVoice.TREE, SpeechBlip.voiceFor(StoryStrings.CEREMONY_WALK_OUT));
        assertEquals(SpeechVoice.HERO, SpeechBlip.voiceFor(StoryStrings.CEREMONY_PLANT));
        assertEquals(SpeechVoice.PIP, SpeechBlip.voiceFor(StoryStrings.CEREMONY_WATER));
        assertEquals(SpeechVoice.TREE, SpeechBlip.voiceFor(StoryStrings.CEREMONY_GROW));
        assertEquals(SpeechVoice.PIP, SpeechBlip.voiceFor(StoryStrings.CEREMONY_WALK_BACK));
        assertEquals(SpeechVoice.PIP, SpeechBlip.voiceFor(StoryStrings.CEREMONY_50_WALK_OUT));
        assertEquals(SpeechVoice.HERO, SpeechBlip.voiceFor(StoryStrings.CEREMONY_150_PLANT));
        assertEquals(SpeechVoice.TREE, SpeechBlip.voiceFor(StoryStrings.CEREMONY_150_WALK_BACK));
    }
}
