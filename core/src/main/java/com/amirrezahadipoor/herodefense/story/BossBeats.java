package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;

/** The Hollow's one line per boss identity, spoken when the fight first crosses half. */
public final class BossBeats {

    private BossBeats() {
    }

    /** The half-health beat for a boss identity, or null for unknown ones. */
    public static String lineFor(String bossType) {
        if (bossType == null) {
            return null;
        }
        return switch (bossType) {
            case "ANCIENT_GOLEM" -> GameLocale.text(StoryStrings.HOLLOW_BOSS_GOLEM);
            case "THORN_MATRIARCH" -> GameLocale.text(StoryStrings.HOLLOW_BOSS_MATRIARCH);
            case "EMBER_WYRM" -> GameLocale.text(StoryStrings.HOLLOW_BOSS_WYRM);
            case "VOID_KNIGHT" -> GameLocale.text(StoryStrings.HOLLOW_BOSS_VOID);
            case "FROST_TITAN" -> GameLocale.text(StoryStrings.HOLLOW_BOSS_TITAN);
            case "SHADOW_LICH" -> GameLocale.text(StoryStrings.HOLLOW_BOSS_LICH);
            case "STORM_COLOSSUS" -> GameLocale.text(StoryStrings.HOLLOW_BOSS_COLOSSUS);
            case "BLOODROOT_AVATAR" -> GameLocale.text(StoryStrings.HOLLOW_BOSS_BLOODROOT);
            default -> null;
        };
    }
}
