package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.i18n.Translated;

/**
 * Verbatim {@code docs/STORY_CONTENT.md} §4 "Whispering Wounds" fragments in both languages, one per Elite
 * kill.
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
            default:
                return null;
        }
    }
}
