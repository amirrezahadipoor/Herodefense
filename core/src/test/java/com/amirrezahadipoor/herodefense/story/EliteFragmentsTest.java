package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Locks the verbatim §4 "Whispering Wounds" fragments and their I/II alternation. */
final class EliteFragmentsTest {
    @Test
    void oddKillsShowFragmentOneAndEvenKillsFragmentTwo() {
        assertEquals(
            "It does not die. It just lets go — everything at once.",
            EliteFragments.fragmentFor("blightburst", 1));
        assertEquals("That burst is not anger. It is relief.",
            EliteFragments.fragmentFor("blightburst", 2));
        assertEquals(
            "That shield is not armor. It is a root, remembering its job.",
            EliteFragments.fragmentFor("rootward_ward", 3));
        assertEquals(
            "Even like this, it still tries to protect. It just forgot what.",
            EliteFragments.fragmentFor("rootward_ward", 4));
        assertEquals("Where it walks, the ground never heals.",
            EliteFragments.fragmentFor("weeping_rot", 5));
        assertEquals(
            "Follow its trail long enough. It leads to the Tree.",
            EliteFragments.fragmentFor("weeping_rot", 6));
    }

    @Test
    void theDeepPoolAffixesCarryTheirOwnTwoPartThreads() {
        assertEquals("It never leaves a place empty. Nothing here does.",
            EliteFragments.fragmentFor("hollowmolt", 1));
        assertEquals("Two small silences where one loud one stood.",
            EliteFragments.fragmentFor("hollowmolt", 2));
        assertEquals("The moss covers the wound while the wound is still there.",
            EliteFragments.fragmentFor("gravemoss", 3));
        assertEquals("That is not healing. That is something patient taking it back.",
            EliteFragments.fragmentFor("gravemoss", 4));
        assertEquals("Stand too close and it loves you — the way an ember loves wind.",
            EliteFragments.fragmentFor("cinderhalo", 5));
        assertEquals("That heat is not attack. It is grief, still warm.",
            EliteFragments.fragmentFor("cinderhalo", 6));
    }

    @Test
    void unknownAffixesShowNothingAndBadCountsClampToOne() {
        assertNull(EliteFragments.fragmentFor("unknown_affix", 1));
        assertNull(EliteFragments.fragmentFor(null, 1));
        assertEquals(EliteFragments.fragmentFor("blightburst", 1),
            EliteFragments.fragmentFor("blightburst", 0));
    }
}
