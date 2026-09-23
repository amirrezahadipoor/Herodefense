package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.amirrezahadipoor.herodefense.gameplay.PlantingCeremony;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import org.junit.jupiter.api.Test;

final class CeremonyLinesTest {
    @Test
    void twigsFullCeremonyBeatsMatchTheScriptVerbatim() {
        assertEquals(
            "One more, dearie. For me.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.WALK_OUT, 1)
        );
        assertEquals(
            "Grow brave, Twig.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.PLANT, 1)
        );
        assertEquals(
            "Drink up! Big gulps!",
            CeremonyLines.lineFor(PlantingCeremony.Phase.WATER, 1)
        );
        assertEquals(
            "He writes poems already.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.GROW, 1)
        );
        assertEquals(
            "Pip's got TWO brothers now!",
            CeremonyLines.lineFor(PlantingCeremony.Phase.WALK_BACK, 1)
        );
        assertNull(CeremonyLines.lineFor(PlantingCeremony.Phase.IDLE, 1));
        assertNull(CeremonyLines.lineFor(PlantingCeremony.Phase.DONE, 1));
        assertNull(CeremonyLines.lineFor(null, 1));
    }

    @Test
    void theShortRitesMirrorEachOtherAroundTwigsFullOne() {
        assertEquals(
            "A new tree, Chief! Dig here!",
            CeremonyLines.lineFor(PlantingCeremony.Phase.WALK_OUT, 0)
        );
        assertEquals(
            "Grow strong, little one.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.PLANT, 0)
        );
        assertEquals(
            "Sprout! My loud little boy.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.WALK_BACK, 0)
        );
        assertEquals(
            "Last seed. Make it count.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.WALK_OUT, 2)
        );
        assertEquals(
            "Grow soft, Leaf.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.PLANT, 2)
        );
        assertEquals(
            "Shh. She naps already.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.WALK_BACK, 2)
        );
        assertNull(CeremonyLines.lineFor(PlantingCeremony.Phase.WATER, 0));
        assertNull(CeremonyLines.lineFor(PlantingCeremony.Phase.GROW, 2));
    }

    @Test
    void theEntryBehindEachBeatIsTheOneTheBoxVoices() {
        assertEquals(
            StoryStrings.CEREMONY_GROW,
            CeremonyLines.entryFor(PlantingCeremony.Phase.GROW, 1)
        );
        assertEquals(
            StoryStrings.CEREMONY_50_PLANT,
            CeremonyLines.entryFor(PlantingCeremony.Phase.PLANT, 0)
        );
        assertEquals(
            StoryStrings.CEREMONY_150_WALK_BACK,
            CeremonyLines.entryFor(PlantingCeremony.Phase.WALK_BACK, 2)
        );
        assertNull(CeremonyLines.entryFor(null, 1));
    }

    @Test
    void thePlainLineForStillReadsTwigsSet() {
        assertEquals(
            CeremonyLines.lineFor(PlantingCeremony.Phase.WATER, 1),
            CeremonyLines.lineFor(PlantingCeremony.Phase.WATER)
        );
        assertNull(CeremonyLines.lineFor(PlantingCeremony.Phase.DONE));
        assertNull(CeremonyLines.lineFor(null));
    }
}
