package com.amirrezahadipoor.herodefense.i18n;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link GameNumbers}: US English digits, separators and signs, the shapes the HUD and the combat pop-ups have
 * always drawn.
 *
 * <p>Until 2026-09-23 this class formatted per language and these tests pinned both scripts to exact codepoints.
 * The owner deleted the Persian translation outright, so the locale is fixed and these pin the one output left.
 */
class GameNumbersTest {

    @Test
    void formatsEnglishNumbersTheWayTheHudAlwaysHas() {
        assertEquals("140", GameNumbers.integer(140));
        assertEquals("1,234,567", GameNumbers.integer(1234567));
        assertEquals("0", GameNumbers.integer(0));
        assertEquals("-1,234", GameNumbers.integer(-1234));
        assertEquals("50%", GameNumbers.percent(50));
        assertEquals("+5", GameNumbers.signed(5));
        assertEquals("-5", GameNumbers.signed(-5));
    }

    @Test
    void compactNumberKeepsTheShapeTheCombatPopupsHaveAlwaysDrawn() {
        assertEquals("999", GameNumbers.compact(999));
        assertEquals("1.2k", GameNumbers.compact(1_240));
        assertEquals("120k", GameNumbers.compact(120_400));
        assertEquals("1", GameNumbers.compact(0), "a hit is never worth zero");
    }
}
