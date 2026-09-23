package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;

/**
 * Pip's milestone beats (MEMORY §9.3): one breath after clearing waves 25, 75, 125 and 175 --
 * every twenty-five waves except the plantings at 50/100/150, which hold ceremonies instead.
 * The beats play between waves, never during combat, so the silence rule never hears them.
 */
public final class BreatherLines {
    private BreatherLines() {
    }

    /** True for the cleared waves that earn a breath: 25, 75, 125, 175. */
    public static boolean isBreatherWave(int waveNumber) {
        return waveNumber > 0 && waveNumber % 50 == 25;
    }

    /** The table entry a cleared milestone wave speaks, or null when it earns no breath. */
    public static StoryStrings entryFor(int waveNumber) {
        return switch (waveNumber) {
            case 25 -> StoryStrings.BREATHER_25;
            case 75 -> StoryStrings.BREATHER_75;
            case 125 -> StoryStrings.BREATHER_125;
            case 175 -> StoryStrings.BREATHER_175;
            default -> null;
        };
    }

    /** The spoken line for a cleared milestone wave, or null when it earns no breath. */
    public static String lineFor(int waveNumber) {
        StoryStrings entry = entryFor(waveNumber);
        return entry == null ? null : GameLocale.text(entry);
    }
}
