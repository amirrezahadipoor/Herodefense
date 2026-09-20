package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The crit pulse's ratio maths: armed at one, decaying to zero, never past its cap. */
class PostProcessCritPulseTest {

    @Test
    void aFreshPulseIsFullAndAnIdleChainIsDark() {
        assertEquals(1f, PostProcessRenderer.pulseRatio(PostProcessRenderer.CRIT_PULSE_SECONDS), 0.0001f);
        assertEquals(0f, PostProcessRenderer.pulseRatio(0f), 0.0001f);
    }

    @Test
    void decayIsMonotonicAndNegativeRemainderClampsToDark() {
        float full = PostProcessRenderer.CRIT_PULSE_SECONDS;
        assertTrue(PostProcessRenderer.pulseRatio(full * 0.75f)
            > PostProcessRenderer.pulseRatio(full * 0.25f));
        assertEquals(0f, PostProcessRenderer.pulseRatio(-3f), 0.0001f);
    }

    @Test
    void thePulseCapIsAJudgedBriefFlash() {
        assertTrue(PostProcessRenderer.CRIT_PULSE_SECONDS > 0.1f
            && PostProcessRenderer.CRIT_PULSE_SECONDS <= 0.5f,
            "a hit answers, it does not stain the screen");
    }
}
