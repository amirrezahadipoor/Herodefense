package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.gameplay.PlantingCeremony;

/**
 * Planting-ceremony lines, verbatim: three plantings, three line sets. Sprout's short rite at wave 50 and
 * Leaf's at 150 walk Pip, the Warden, Granny; Twig's full five-beat rite at wave 100 walks Granny, the
 * Warden, Pip, Granny, Pip. The callers that type a beat ask the entry for its own voice, so the box
 * always speaks the planting's three speakers in turn.
 */
public final class CeremonyLines {
    private CeremonyLines() {
    }

    /** The table entry a ceremony phase speaks, or null outside the beats of grove {@code groveIndex}. */
    public static StoryStrings entryFor(PlantingCeremony.Phase phase, int groveIndex) {
        if (phase == null) {
            return null;
        }
        if (groveIndex == 0) {
            return switch (phase) {
                case WALK_OUT -> StoryStrings.CEREMONY_50_WALK_OUT;
                case PLANT -> StoryStrings.CEREMONY_50_PLANT;
                case WALK_BACK -> StoryStrings.CEREMONY_50_WALK_BACK;
                default -> null;
            };
        }
        if (groveIndex == 2) {
            return switch (phase) {
                case WALK_OUT -> StoryStrings.CEREMONY_150_WALK_OUT;
                case PLANT -> StoryStrings.CEREMONY_150_PLANT;
                case WALK_BACK -> StoryStrings.CEREMONY_150_WALK_BACK;
                default -> null;
            };
        }
        return switch (phase) {
            case WALK_OUT -> StoryStrings.CEREMONY_WALK_OUT;
            case PLANT -> StoryStrings.CEREMONY_PLANT;
            case WATER -> StoryStrings.CEREMONY_WATER;
            case GROW -> StoryStrings.CEREMONY_GROW;
            case WALK_BACK -> StoryStrings.CEREMONY_WALK_BACK;
            default -> null;
        };
    }

    /** The spoken line for a ceremony phase of grove {@code groveIndex}, or null outside its beats. */
    public static String lineFor(PlantingCeremony.Phase phase, int groveIndex) {
        StoryStrings entry = entryFor(phase, groveIndex);
        return entry == null ? null : GameLocale.text(entry);
    }

    /** The spoken line for a wave-100 full-ceremony phase, or null outside the five beats. */
    public static String lineFor(PlantingCeremony.Phase phase) {
        return lineFor(phase, 1);
    }
}
