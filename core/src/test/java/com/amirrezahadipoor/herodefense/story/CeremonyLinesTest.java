package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.gameplay.PlantingCeremony;
import org.junit.jupiter.api.Test;

final class CeremonyLinesTest {
    @Test
    void beatsMatchStoryContentVerbatim() {
        assertEquals(
            "A tree should not carry this alone. Not anymore.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.WALK_OUT)
        );
        assertEquals(
            "So we plant another. Grow loud. Grow angry. Grow.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.PLANT)
        );
        assertEquals(
            "I will hold the line. That is what I am for.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.WATER)
        );
        assertEquals(
            "Two of us now. I remember what mornings sound like.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.GROW)
        );
        assertEquals(
            "Now hold the line. Both of us need you.",
            CeremonyLines.lineFor(PlantingCeremony.Phase.WALK_BACK)
        );
        assertNull(CeremonyLines.lineFor(PlantingCeremony.Phase.IDLE));
        assertNull(CeremonyLines.lineFor(PlantingCeremony.Phase.DONE));
        assertNull(CeremonyLines.lineFor(null));
    }

    @Test
    void onlyTheGrowthBeatSpeaksInTheTreeVoice() {
        assertTrue(CeremonyLines.isTreeVoice(PlantingCeremony.Phase.GROW));
        assertFalse(CeremonyLines.isTreeVoice(PlantingCeremony.Phase.WALK_OUT));
        assertFalse(CeremonyLines.isTreeVoice(PlantingCeremony.Phase.PLANT));
        assertFalse(CeremonyLines.isTreeVoice(PlantingCeremony.Phase.WATER));
        assertFalse(CeremonyLines.isTreeVoice(PlantingCeremony.Phase.WALK_BACK));
        assertFalse(CeremonyLines.isTreeVoice(null));
    }
}
