package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

final class ReflectionLinesTest {
    @Test
    void reflectionsMatchStoryContentVerbatim() {
        assertEquals(
            "The wolves fear something bigger than me. That should scare me more.",
            ReflectionLines.lineForWave(25)
        );
        assertEquals(
            "Half of what I have killed, I once knew. I try not to think about it.",
            ReflectionLines.lineForWave(50)
        );
        assertEquals(
            "Past the treeline, the ground is wrong. Not ground at all.",
            ReflectionLines.lineForWave(75)
        );
        assertEquals(
            "Three trees now. Three times to lose. I would still make the trade.",
            ReflectionLines.lineForWave(125)
        );
        assertEquals(
            "It stopped sending the weak ones first. It is out of patience.",
            ReflectionLines.lineForWave(150)
        );
        assertEquals(
            "What is left may be the last. Or it wants me to believe that.",
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
