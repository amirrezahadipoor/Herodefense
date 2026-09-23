package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class BreatherCinematicTest {

    @Test
    void beginsIdleBreathesOnceThenRests() {
        BreatherCinematic cinematic = new BreatherCinematic();
        assertEquals(BreatherCinematic.Phase.IDLE, cinematic.phase());
        assertFalse(cinematic.isActive());
        assertNull(cinematic.line());

        cinematic.begin(25);
        assertTrue(cinematic.isActive());
        assertEquals(25, cinematic.waveNumber());
        assertEquals(BreatherCinematic.Phase.BEAT, cinematic.phase());
        assertEquals("Twenty-five nights! Pip counted!", cinematic.line());

        advance(cinematic, BreatherCinematic.BREATHER_SECONDS - 0.01f);
        assertTrue(cinematic.isActive());
        assertEquals(BreatherCinematic.Phase.BEAT, cinematic.phase());

        assertTrue(cinematic.update(0.05f));
        assertFalse(cinematic.isActive());
        assertEquals(BreatherCinematic.Phase.DONE, cinematic.phase());
        assertNull(cinematic.line(), "the box clears once the beat is over");

        assertFalse(cinematic.update(0.05f));
    }

    @Test
    void skipJumpsToTheEnd() {
        BreatherCinematic cinematic = new BreatherCinematic();
        cinematic.begin(75);

        cinematic.skip();

        assertTrue(cinematic.update(0.01f));
        assertEquals(BreatherCinematic.Phase.DONE, cinematic.phase());
    }

    @Test
    void skipWhileIdleStaysIdle() {
        BreatherCinematic cinematic = new BreatherCinematic();

        cinematic.skip();

        assertEquals(BreatherCinematic.Phase.IDLE, cinematic.phase());
        assertFalse(cinematic.update(0.05f));
    }

    @Test
    void aWaveWithoutABreathBreathesAnEmptyBeat() {
        BreatherCinematic cinematic = new BreatherCinematic();
        cinematic.begin(6);

        assertEquals(BreatherCinematic.Phase.BEAT, cinematic.phase());
        assertEquals("", cinematic.line());

        advance(cinematic, BreatherCinematic.BREATHER_SECONDS + 0.05f);
        assertEquals(BreatherCinematic.Phase.DONE, cinematic.phase());
    }

    /** Advances in frame-sized steps; a backgrounded frame must never fast-forward the show. */
    private static void advance(BreatherCinematic cinematic, float seconds) {
        float remaining = seconds;
        while (remaining > 0f) {
            float step = Math.min(0.05f, remaining);
            cinematic.update(step);
            remaining -= step;
        }
    }
}
