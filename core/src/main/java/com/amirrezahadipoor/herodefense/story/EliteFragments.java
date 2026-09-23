package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.i18n.Translated;

/**
 * Pip's field notes: one fragment per Elite kill, alternating I/II per affix.
 */
public final class EliteFragments {

    private EliteFragments() {
    }

    /**
     * Fragment for an Elite kill in the language the game speaks: odd kill counts show I, even counts II, so the
     * two-part thread alternates deterministically per affix.
     */
    public static String fragmentFor(String affixId, int killCount) {
        Translated fragment = entryFor(affixId, killCount);
        return fragment == null ? null : GameLocale.text(fragment);
    }

    /** The fragment entry itself, for a caller that wants its own language. */
    public static Translated entryFor(String affixId, int killCount) {
        boolean first = (Math.max(1, killCount) & 1) == 1;
        switch (affixId == null ? "" : affixId) {
            case "blightburst":
                return first ? StoryStrings.ELITE_BLIGHTBURST_ONE : StoryStrings.ELITE_BLIGHTBURST_TWO;
            case "rootward_ward":
                return first ? StoryStrings.ELITE_ROOTWARD_ONE : StoryStrings.ELITE_ROOTWARD_TWO;
            case "weeping_rot":
                return first ? StoryStrings.ELITE_WEEPING_ONE : StoryStrings.ELITE_WEEPING_TWO;
            case "hollowmolt":
                return first ? StoryStrings.ELITE_HOLLOWMOLT_ONE : StoryStrings.ELITE_HOLLOWMOLT_TWO;
            case "gravemoss":
                return first ? StoryStrings.ELITE_GRAVEMOSS_ONE : StoryStrings.ELITE_GRAVEMOSS_TWO;
            case "cinderhalo":
                return first ? StoryStrings.ELITE_CINDERHALO_ONE : StoryStrings.ELITE_CINDERHALO_TWO;
            case "stoneshell":
                return first ? StoryStrings.ELITE_STONESHELL_ONE : StoryStrings.ELITE_STONESHELL_TWO;
            case "gravebloom":
                return first ? StoryStrings.ELITE_GRAVEBLOOM_ONE : StoryStrings.ELITE_GRAVEBLOOM_TWO;
            case "swarmcall":
                return first ? StoryStrings.ELITE_SWARMCALL_ONE : StoryStrings.ELITE_SWARMCALL_TWO;
            case "spitebarb":
                return first ? StoryStrings.ELITE_SPITEBARB_ONE : StoryStrings.ELITE_SPITEBARB_TWO;
            case "hammerfall":
                return first ? StoryStrings.ELITE_HAMMERFALL_ONE : StoryStrings.ELITE_HAMMERFALL_TWO;
            case "bloodhowl":
                return first ? StoryStrings.ELITE_BLOODHOWL_ONE : StoryStrings.ELITE_BLOODHOWL_TWO;
            default:
                return null;
        }
    }
}
