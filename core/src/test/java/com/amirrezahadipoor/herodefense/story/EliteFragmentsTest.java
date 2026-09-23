package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Locks Pip's field-note fragments and their I/II alternation. */
final class EliteFragmentsTest {
    @Test
    void oddKillsShowFragmentOneAndEvenKillsFragmentTwo() {
        assertEquals(
            "It pops! Do not hug it.",
            EliteFragments.fragmentFor("blightburst", 1));
        assertEquals("That pop is relief. Weird.",
            EliteFragments.fragmentFor("blightburst", 2));
        assertEquals(
            "A shield! Rude shield!",
            EliteFragments.fragmentFor("rootward_ward", 3));
        assertEquals(
            "It guards nothing. Still guards.",
            EliteFragments.fragmentFor("rootward_ward", 4));
        assertEquals("Don't step in the yuck.",
            EliteFragments.fragmentFor("weeping_rot", 5));
        assertEquals(
            "The yuck leads to Granny?!",
            EliteFragments.fragmentFor("weeping_rot", 6));
    }

    @Test
    void theDeepPoolAffixesCarryTheirOwnTwoPartThreads() {
        assertEquals("One becomes two! Bad magic!",
            EliteFragments.fragmentFor("hollowmolt", 1));
        assertEquals("Two small quiets. Still loud.",
            EliteFragments.fragmentFor("hollowmolt", 2));
        assertEquals("Moss on a wound. Still a wound.",
            EliteFragments.fragmentFor("gravemoss", 3));
        assertEquals("It's healing! …Stop healing!",
            EliteFragments.fragmentFor("gravemoss", 4));
        assertEquals("Hot hug! No hugs!",
            EliteFragments.fragmentFor("cinderhalo", 5));
        assertEquals("Warm grief. Stay back.",
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
