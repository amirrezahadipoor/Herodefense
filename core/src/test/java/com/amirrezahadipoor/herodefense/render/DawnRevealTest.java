package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The victory sky's clock (see {@link DawnReveal}): how the dawn advances, how it eases, and the
 * narrative timing that makes it land -- the sky must be breaking by the time the Tree's victory
 * word has finished typing, and it must not be visible before the run is won.
 */
final class DawnRevealTest {

    @Test
    void aNewSkyStartsInDarkness() {
        DawnReveal reveal = new DawnReveal();
        assertEquals(0f, reveal.progress(), 1e-6f);
        assertEquals(0f, reveal.eased(), 1e-6f);

        reveal.tick(1f);
        reveal.tick(1f);
        assertTrue(reveal.progress() > 0f, "the sunrise begins the moment the run is complete");
        assertTrue(reveal.progress() < 0.5f, "but it is still night at this point");
    }

    @Test
    void theSkyBreaksAtEightSecondsAndStaysBroken() {
        DawnReveal reveal = new DawnReveal();
        for (int second = 0; second < 8; second++) {
            reveal.tick(1f);
        }
        assertEquals(1f, reveal.progress(), 1e-6f);
        assertEquals(1f, reveal.eased(), 1e-6f);

        reveal.tick(30f);
        assertEquals(1f, reveal.progress(), 1e-6f, "a dawn that is done stays done");
    }

    @Test
    void garbageCannotBreakTheClock() {
        DawnReveal reveal = new DawnReveal();
        reveal.tick(Float.NaN);
        reveal.tick(-5f);
        reveal.tick(0f);
        assertEquals(0f, reveal.progress(), 1e-6f, "not-a-deltas do not dawn the sky");

        reveal.reset();
        reveal.tick(100f);
        assertEquals(1f, reveal.progress(), 1e-6f);
        reveal.reset();
        assertEquals(0f, reveal.progress(), 1e-6f, "a new run is night again");
    }

    @Test
    void theEasingStartsBarelyVisibleAndSettlesSlowly() {
        assertEquals(0f, DawnReveal.ease(0f), 1e-6f);
        assertEquals(1f, DawnReveal.ease(1f), 1e-6f);
        assertEquals(0.5f, DawnReveal.ease(0.5f), 1e-6f);

        // The player reads before they notice: at one tenth of the sunrise the sky has barely
        // woken, and a tenth of the way down the curve it is still mostly night.
        assertTrue(DawnReveal.ease(0.1f) < 0.05f, "the first light is a whisper");
        assertTrue(DawnReveal.ease(0.9f) > 0.95f, "and the morning settles like a morning");

        // Monotone across the whole curve.
        float previous = 0f;
        for (int step = 1; step <= 100; step++) {
            float eased = DawnReveal.ease(step / 100f);
            assertTrue(eased >= previous, "the sky never un-rises at " + eased);
            previous = eased;
        }
    }

    @Test
    void theWordFinishesTypingBeforeTheSkyHasStoodStill() {
        // The Tree's victory line takes the box roughly VICTORY_WORD_SECONDS of real time (type,
        // hold, close). By the time it has finished, the sunrise must already be well under way --
        // the dawn is the word made light -- but not yet done, so it keeps arriving while the
        // summary is on screen.
        float raw = DawnReveal.VICTORY_WORD_SECONDS / DawnReveal.DAWN_SECONDS;
        float eased = DawnReveal.ease(raw);
        assertTrue(eased >= 0.8f, "the sky is breaking when the word ends: " + eased);
        assertTrue(eased < 1f, "and it is still arriving, not a screenshot of a sunrise");
    }
}
