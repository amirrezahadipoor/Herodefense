package com.amirrezahadipoor.herodefense.audio;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;

/**
 * Which of the three conversation voices a line speaks in (roadmap ST-voice): the blips themselves are the
 * {@code SpeechTyper}'s, one per character, in the tone of the speaker that owns the line. Nothing is
 * spoken aloud — the reading voice stays in the player's head — but the ear still knows whether a line is
 * the Hero's, the Tree's or the Hollow's.
 */
public final class SpeechBlip {

    private SpeechBlip() {
    }

    /**
     * The tone a line types in, known from the {@code Translated} entry it was drawn from; unknown or
     * table-less text defaults to the Hero. Mirrors the speaker split the story voice document fixes:
     * the Hero's lines are white and terse, the Tree's green and calm, the Hollow's a taunt.
     */
    public static SpeechVoice voiceFor(StoryStrings entry) {
        if (entry == null) {
            return SpeechVoice.HERO;
        }
        String name = entry.name();
        // The Tree's own lines: idle whispers, the codex's voice, and the ceremony's growth beat.
        if (name.startsWith("WHISPER_") || name.startsWith("CODEX_") || name.startsWith("CEREMONY_GROW")) {
            return SpeechVoice.TREE;
        }
        // The Hollow's taunts and memories: deaths, mercies, the wave-100 mark, the half-health beats and the
        // Whispering Wound fragments every Elite carries of it.
        if (name.startsWith("HOLLOW_") || name.startsWith("ELITE_")) {
            return SpeechVoice.HOLLOW;
        }
        // Boss title cards, reflections, openings, epilogues, deeds: the Warden's plain white voice.
        return SpeechVoice.HERO;
    }

    /** The tone of a line in the game's current language, resolved by matching it against the story tables. */
    public static SpeechVoice voiceForRunningText(String text) {
        if (text == null) {
            return SpeechVoice.HERO;
        }
        for (StoryStrings entry : StoryStrings.values()) {
            if (text.equals(GameLocale.text(entry))) {
                return voiceFor(entry);
            }
        }
        return SpeechVoice.HERO;
    }
}
