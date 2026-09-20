package com.amirrezahadipoor.herodefense.polish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The pollen layer obeys the same budget and determinism contract as the spore layer. */
class PollenLayerTest {

    @Test
    void pollenStaysInsideTheAmbientBudget() {
        for (int index = 0; index < AmbientMoteField.pollenCount(); index++) {
            for (int step = 0; step < 220; step++) {
                float time = step * 1.37f;
                float alpha = AmbientMoteField.pollenAlpha(index, time);
                assertTrue(alpha >= 0f && alpha <= VfxBudget.AMBIENT_MAX_ALPHA,
                    index + " at " + time + " -> " + alpha);
            }
        }
    }

    @Test
    void pollenIsPureTimeAndIndex() {
        assertEquals(
            AmbientMoteField.pollenX(3, 41.5f), AmbientMoteField.pollenX(3, 41.5f));
        assertEquals(
            AmbientMoteField.pollenY(5, 7.25f), AmbientMoteField.pollenY(5, 7.25f));
    }

    @Test
    void pollenStaysOnTheArena() {
        for (int index = 0; index < AmbientMoteField.pollenCount(); index++) {
            for (int step = 0; step < 220; step++) {
                float time = step * 2.71f;
                float x = AmbientMoteField.pollenX(index, time);
                float y = AmbientMoteField.pollenY(index, time);
                assertTrue(x > 0f && x < 720f, "x " + x);
                assertTrue(y >= 180f, "y " + y);
            }
        }
    }
}
