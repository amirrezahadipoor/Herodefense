package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Locks the verbatim §4 "Whispering Wounds" fragments and their I/II alternation. */
final class EliteFragmentsTest {
    @Test
    void oddKillsShowFragmentOneAndEvenKillsFragmentTwo() {
        assertEquals(
            "It does not die so much as let go. What was holding it together was never its own to keep.",
            EliteFragments.fragmentFor("blightburst", 1));
        assertEquals("The burst is not rage. It's relief.",
            EliteFragments.fragmentFor("blightburst", 2));
        assertEquals(
            "The shield is not armor. It's a root, briefly recalling what it was for.",
            EliteFragments.fragmentFor("rootward_ward", 3));
        assertEquals(
            "Even changed, a thing in it still tries to protect a thing. It's just no longer sure what.",
            EliteFragments.fragmentFor("rootward_ward", 4));
        assertEquals("The ground it crosses does not heal. Not yet. Maybe not ever.",
            EliteFragments.fragmentFor("weeping_rot", 5));
        assertEquals(
            "Every trail leads back the same direction, if you follow it far enough: toward the Tree.",
            EliteFragments.fragmentFor("weeping_rot", 6));
    }

    @Test
    void theDeepPoolAffixesCarryTheirOwnTwoPartThreads() {
        assertEquals("It does not leave empty. Nothing here does.",
            EliteFragments.fragmentFor("hollowmolt", 1));
        assertEquals("Two smaller silences where one loud one stood. The Tree counts them as the same wound.",
            EliteFragments.fragmentFor("hollowmolt", 2));
        assertEquals("The moss grows over the wound while the wound is still wearing it.",
            EliteFragments.fragmentFor("gravemoss", 3));
        assertEquals("It is not healing. It is being reclaimed, slowly, by something patient.",
            EliteFragments.fragmentFor("gravemoss", 4));
        assertEquals("Stand close and it will love you the way an ember loves a dry wind.",
            EliteFragments.fragmentFor("cinderhalo", 5));
        assertEquals("The heat is not attack. It is grief, still warm from the fire that made it.",
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
