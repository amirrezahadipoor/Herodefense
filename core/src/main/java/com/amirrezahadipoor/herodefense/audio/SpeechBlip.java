package com.amirrezahadipoor.herodefense.audio;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;

/**
 * The Undertale-style typing voice (roadmap ST-voice): a story line speaks as a run of short blips, one per
 * syllable, in the tone of the speaker that owns it. Nothing is spoken aloud — the reading voice stays in the
 * player's head — but the ear still knows whether a line is the Hero's, the Tree's or the Hollow's.
 *
 * <p>The blip count is capped; a long line does not machine-gun. The silent pause between blips is the rate
 * limiter's own interval, so a line reads as a scattering of taps rather than a beep.
 */
public final class SpeechBlip {

    /** A line longer than this many characters caps its blips, so a wall of text never sounds busy. */
    private static final int MAX_BLIPS = 12;

    private SpeechBlip() {
    }

    /** How many blips the given text speaks, proportional to its length and never absurd. */
    public static int blipsFor(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        // Roughly one blip per word, minimum one, capped.
        int words = text.trim().split("\\s+").length;
        return Math.max(1, Math.min(MAX_BLIPS, words));
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
