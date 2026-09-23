package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

final class ReflectionLinesTest {
    @Test
    void reflectionsMatchStoryContentVerbatim() {
        assertEquals(
            "Twenty-five nights! Pip counted!",
            ReflectionLines.lineForWave(25)
        );
        assertEquals(
            "Fifty nights! A new tree today!",
            ReflectionLines.lineForWave(50)
        );
        assertEquals(
            "The dark is thick. My butt glows.",
            ReflectionLines.lineForWave(75)
        );
        assertEquals(
            "Half the Night Shift owes me coins.",
            ReflectionLines.lineForWave(125)
        );
        assertEquals(
            "Last seed tonight, Chief! Hold on!",
            ReflectionLines.lineForWave(150)
        );
        assertEquals(
            "Almost dawn, Chief. Almost.",
            ReflectionLines.lineForWave(175)
        );
    }

    @Test
    void otherWavesStaySilent() {
        for (int wave : new int[] {1, 5, 10, 15, 20, 24, 26, 100, 101, 200}) {
            assertNull(ReflectionLines.lineForWave(wave), "wave " + wave);
        }
    }
}
