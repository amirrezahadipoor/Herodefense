package com.amirrezahadipoor.herodefense.audio;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;

/**
 * Which of the five conversation voices a line speaks in (roadmap ST-voice): the blips themselves are the
 * {@code SpeechTyper}'s, one per character, in the tone of the speaker that owns the line. Nothing is
 * spoken aloud — the reading voice stays in the player's head — but the ear still knows whether a line is
 * the Warden's, Granny's, the Hollow's, Pip's or the Night Shift's.
 */
public final class SpeechBlip {

    private SpeechBlip() {
    }

    /**
     * The tone a line types in, known from the {@code Translated} entry it was drawn from; unknown or
     * table-less text defaults to the Hero. Mirrors the speaker split the story voice document fixes:
     * the Warden's lines are white and terse, Granny's green and calm, the Hollow's a dry taunt, Pip's
     * a loud rising chirp, and the Night Shift's a low square that only speaks before a boss wave.
     */
    public static SpeechVoice voiceFor(StoryStrings entry) {
        if (entry == null) {
            return SpeechVoice.HERO;
        }
        String name = entry.name();
        // Pip's comebacks close every boss-intro cutscene, so they answer in his chirp even though they
        // sit in the boss-intro block of the table.
        if (name.startsWith("BOSS_INTRO_") && name.endsWith("_PIP")) {
            return SpeechVoice.PIP;
        }
        // The Night Shift's trash-talk: first-encounter title cards and pre-wave intro scenes. Bosses
        // never talk once the wave starts, so no combat entity routes here.
        if (name.startsWith("BOSS_")) {
            return SpeechVoice.BOSS;
        }
        // The Hollow's deadpan: deaths, mercies, verdicts, the greeting, the wave-100 mark, and the
        // chapter-card lines it reads under each card.
        if (name.startsWith("HOLLOW_") || name.startsWith("CHAPTER_")) {
            return SpeechVoice.HOLLOW;
        }
        // Granny's own lines: the codex's voice, the victory thank-you, the daily gift, and the
        // planting ceremonies' Granny beats — Twig's walk-out and growth, the short rites' walk-backs.
        if (name.startsWith("CODEX_") || "TREE_VICTORY".equals(name) || "DAILY_GIFT".equals(name)
            || "CEREMONY_WALK_OUT".equals(name) || "CEREMONY_GROW".equals(name)
            || "CEREMONY_50_WALK_BACK".equals(name) || "CEREMONY_150_WALK_BACK".equals(name)) {
            return SpeechVoice.TREE;
        }
        // Pip's loud heart: openings, milestone beats, field notes on Elite kills, idle whispers, the
        // third beat of every epilogue, and the ceremonies' Pip beats — Twig's watering and walk-back,
        // the short rites' walk-outs.
        if (name.startsWith("OPENING_") || name.startsWith("REFLECTION_WAVE_") || name.startsWith("ELITE_")
            || name.startsWith("WHISPER_") || name.endsWith("_THREE") || "CEREMONY_WATER".equals(name)
            || "CEREMONY_WALK_BACK".equals(name) || "CEREMONY_50_WALK_OUT".equals(name)
            || "CEREMONY_150_WALK_OUT".equals(name)) {
            return SpeechVoice.PIP;
        }
        // Deeds, epilogue thirds one and two, the transition, the Warden's victory line, mythic flavors,
        // trophy chrome and the speaking-name plates: the Warden's plain white voice.
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
