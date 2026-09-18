package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.gameplay.PlantingCeremony;

/**
 * Wave 100 planting-ceremony lines, verbatim (§2.3), synced one-to-one to the ceremony
 * phases: walk → plant → water → growth → return. The growth line speaks in the Tree's
 * leaf-green tint; the rest are the Hero's white.
 */
public final class CeremonyLines {
    private CeremonyLines() {
    }

    /** The spoken line for a ceremony phase, or null outside the five beats. */
    public static String lineFor(PlantingCeremony.Phase phase) {
        if (phase == null) {
            return null;
        }
        return switch (phase) {
            case WALK_OUT -> GameLocale.text(StoryStrings.CEREMONY_WALK_OUT);
            case PLANT -> GameLocale.text(StoryStrings.CEREMONY_PLANT);
            case WATER -> GameLocale.text(StoryStrings.CEREMONY_WATER);
            case GROW -> GameLocale.text(StoryStrings.CEREMONY_GROW);
            case WALK_BACK -> GameLocale.text(StoryStrings.CEREMONY_WALK_BACK);
            default -> null;
        };
    }

    /** True only for the growth beat, which renders in the Tree's tint. */
    public static boolean isTreeVoice(PlantingCeremony.Phase phase) {
        return phase == PlantingCeremony.Phase.GROW;
    }
}
