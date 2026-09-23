package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import org.junit.jupiter.api.Test;

final class BreatherLinesTest {
    @Test
    void theFourBreathsMatchTheScriptVerbatim() {
        assertEquals("Twenty-five nights! Pip counted!", BreatherLines.lineFor(25));
        assertEquals("The dark is thick. My butt glows.", BreatherLines.lineFor(75));
        assertEquals("Half the Night Shift owes me coins.", BreatherLines.lineFor(125));
        assertEquals("Almost dawn, Chief. Almost.", BreatherLines.lineFor(175));
        assertEquals(StoryStrings.BREATHER_25, BreatherLines.entryFor(25));
        assertEquals(StoryStrings.BREATHER_175, BreatherLines.entryFor(175));
    }

    @Test
    void breathsFallEveryTwentyFiveWavesExceptThePlantings() {
        assertTrue(BreatherLines.isBreatherWave(25));
        assertTrue(BreatherLines.isBreatherWave(75));
        assertTrue(BreatherLines.isBreatherWave(125));
        assertTrue(BreatherLines.isBreatherWave(175));
        assertFalse(BreatherLines.isBreatherWave(1));
        assertFalse(BreatherLines.isBreatherWave(5));
        assertFalse(BreatherLines.isBreatherWave(24));
        assertFalse(BreatherLines.isBreatherWave(26));
        assertFalse(BreatherLines.isBreatherWave(50));
        assertFalse(BreatherLines.isBreatherWave(100));
        assertFalse(BreatherLines.isBreatherWave(150));
        assertFalse(BreatherLines.isBreatherWave(200));
        assertFalse(BreatherLines.isBreatherWave(0));
        assertFalse(BreatherLines.isBreatherWave(-25));
    }

    @Test
    void wavesWithoutABreathSpeakNothing() {
        assertNull(BreatherLines.lineFor(6));
        assertNull(BreatherLines.entryFor(6));
        assertNull(BreatherLines.lineFor(50));
        assertNull(BreatherLines.lineFor(200));
    }
}
