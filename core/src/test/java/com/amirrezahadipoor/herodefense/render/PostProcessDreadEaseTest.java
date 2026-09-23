package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** The dread edge eases toward its target: breathing in and out, never popping, never overshooting. */
class PostProcessDreadEaseTest {
    @Test
    void easingStepsTowardTheTarget() {
        assertEquals(0.5f, PostProcessRenderer.easeToward(0f, 1f, 1f, 0.5f), 0.0001f);
        assertEquals(0.5f, PostProcessRenderer.easeToward(1f, 0f, 1f, 0.5f), 0.0001f);
    }

    @Test
    void easingStopsExactlyOnTheTarget() {
        assertEquals(1f, PostProcessRenderer.easeToward(0.9f, 1f, 1f, 0.5f), 0.0001f);
        assertEquals(0f, PostProcessRenderer.easeToward(0.1f, 0f, 1f, 0.5f), 0.0001f);
        assertEquals(0.4f, PostProcessRenderer.easeToward(0.4f, 0.4f, 1f, 0.5f), 0.0001f);
    }

    @Test
    void aFrozenFrameHoldsItsLevel() {
        assertEquals(0.3f, PostProcessRenderer.easeToward(0.3f, 1f, 0f, 0.5f), 0.0001f);
    }
}
