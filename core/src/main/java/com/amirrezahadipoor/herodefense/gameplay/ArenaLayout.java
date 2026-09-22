package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.RunStrings;

/**
 * The four fields a run can be fought on.
 *
 * <p>The arena used to be one field with four ground colours: wave 43 and wave 143 happened in the same place,
 * and the only thing that changed between them was how many bodies walked in. A layout is the other half of that
 * answer -- the same fight, in a place with cover in it -- and it is chosen from the run seed, so two runs are
 * two places rather than two skins.
 *
 * <p>Every layout keeps the three approach roads clear, which is the rule the balance gates depend on: a wave
 * still arrives from the west, the east and the south along the corridors it always used, so a night with
 * obstacles is not a night with fewer of them reaching the Hero. What a layout changes is where the *player* can
 * stand, where an arrow can fly, and what an enemy walking a flank has to walk around.
 */
public enum ArenaLayout {

    OPEN_HEARTH(RunStrings.FIELD_OPEN_HEARTH, RunStrings.FIELD_OPEN_HEARTH_DETAIL, 2),
    STANDING_STONES(RunStrings.FIELD_STANDING_STONES, RunStrings.FIELD_STANDING_STONES_DETAIL, 3),
    THORNHEDGE(RunStrings.FIELD_THORNHEDGE, RunStrings.FIELD_THORNHEDGE_DETAIL, 3),
    RUINED_RING(RunStrings.FIELD_RUINED_RING, RunStrings.FIELD_RUINED_RING_DETAIL, 4);

    private final RunStrings label;
    private final RunStrings detail;
    private final int minimumOutcrops;

    ArenaLayout(RunStrings label, RunStrings detail, int minimumOutcrops) {
        this.label = label;
        this.detail = detail;
        this.minimumOutcrops = minimumOutcrops;
    }

    /**
     * The floor the seed's jitter may not take a layout below.
     *
     * <p>An outcrop that would land inside another one's pocket, on the tree or on the Hero's first ground is
     * dropped rather than nudged, so a lucky seed keeps the whole shape and an unlucky one keeps the part of it
     * that fits. A field that fitted none of it would not be a layout, which is what {@code ArenaTerrainTest}
     * holds this number to.
     */
    public int minimumOutcrops() {
        return minimumOutcrops;
    }

    public String label() {
        return GameLocale.text(label);
    }

    public String detail() {
        return GameLocale.text(detail);
    }

    /** The field this run is fought on. A pure function of the seed: the same save is the same place. */
    public static ArenaLayout forSeed(long runSeed) {
        ArenaLayout[] values = values();
        long mixed = runSeed * 0x9E3779B97F4A7C15L;
        mixed ^= mixed >>> 29;
        return values[Math.floorMod((int) mixed, values.length)];
    }
}
