package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;

/**
 * Between-boss reflection lines, verbatim (§2.2/§2.4): one quiet Hero-voice line at the start
 * of waves 25/50/75/125/150/175, shown as a brief skippable white-text story beat.
 */
public final class ReflectionLines {
    private ReflectionLines() {
    }

    /** The reflection line for a wave start, or null on waves without one. */
    public static String lineForWave(int waveNumber) {
        return switch (waveNumber) {
            case 25 -> GameLocale.text(StoryStrings.REFLECTION_WAVE_25);
            case 50 -> GameLocale.text(StoryStrings.REFLECTION_WAVE_50);
            case 75 -> GameLocale.text(StoryStrings.REFLECTION_WAVE_75);
            case 125 -> GameLocale.text(StoryStrings.REFLECTION_WAVE_125);
            case 150 -> GameLocale.text(StoryStrings.REFLECTION_WAVE_150);
            case 175 -> GameLocale.text(StoryStrings.REFLECTION_WAVE_175);
            default -> null;
        };
    }
}
