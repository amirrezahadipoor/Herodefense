package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The Hollow's gaze (see {@link HollowGaze}): the night only watches in its own arena, a boss
 * sharpens it, a defeat closes it like a blink, and a victory lets it drift away into the dawn.
 */
final class HollowGazeTest {

    @Test
    void theForestArenaIsNeverWatched() {
        HollowGaze gaze = new HollowGaze();
        gaze.update(40, false, true, false, 1f, false);
        assertTrue(!gaze.present() || gaze.openness(false) == 0f, "no eyes in the forest arena");

        gaze.update(HollowGaze.HOLLOW_WAVE, false, true, false, 0f, false);
        assertTrue(gaze.present(), "the night arrives with its own arena");
    }

    @Test
    void theGazeArrivesSoftlyAndReachesRestingStrength() {
        HollowGaze gaze = new HollowGaze();
        for (int wave = HollowGaze.HOLLOW_WAVE; wave <= HollowGaze.FULL_WAVE; wave++) {
            gaze.update(wave, false, true, false, 1f, false);
        }
        assertEquals(
            HollowGaze.RESTING_STRENGTH,
            gaze.strength(false, 0f, true), 1e-6f,
            "a reduced-motion frame reads the resting strength exactly, without the breath");
        assertTrue(gaze.openness(false) > 0.99f, "fully awake by wave " + HollowGaze.FULL_WAVE);
    }

    @Test
    void aBossSharpensTheGazeAndItsLeavingReturnsIt() {
        HollowGaze gaze = new HollowGaze();
        for (int wave = HollowGaze.HOLLOW_WAVE; wave <= HollowGaze.FULL_WAVE; wave++) {
            gaze.update(wave, false, true, false, 1f, false);
        }
        float resting = gaze.openness(false);
        gaze.update(HollowGaze.FULL_WAVE, true, true, false, 0f, false);
        assertEquals(resting * HollowGaze.BOSS_SHARPEN, gaze.openness(true), 1e-6f,
            "while a boss stands, the night leans closer");
        gaze.update(HollowGaze.FULL_WAVE, false, true, false, 0f, false);
        assertEquals(resting, gaze.openness(false), 1e-6f, "and it leans back");
    }

    @Test
    void aDefeatClosesTheEyesQuickAsABlink() {
        HollowGaze gaze = new HollowGaze();
        for (int wave = HollowGaze.HOLLOW_WAVE; wave <= HollowGaze.FULL_WAVE; wave++) {
            gaze.update(wave, false, true, false, 1f, false);
        }
        gaze.update(HollowGaze.FULL_WAVE, false, false, false, HollowGaze.CLOSE_SECONDS, false);
        assertEquals(0f, gaze.openness(false), 1e-6f, "the tree has fallen: the eyes close");

        // A fresh run (a new wave-1 frame) opens them again...
        gaze.update(1, false, true, false, 1f, false);
        gaze.update(HollowGaze.FULL_WAVE, false, true, false, 2f * HollowGaze.CLOSE_SECONDS, false);
        assertTrue(gaze.openness(false) > 0.9f, "...in its own arena, the night is watching again");
    }

    @Test
    void aVictoryLetsTheNightDriftAwayIntoTheDawn() {
        HollowGaze gaze = new HollowGaze();
        for (int wave = HollowGaze.HOLLOW_WAVE; wave <= HollowGaze.FULL_WAVE; wave++) {
            gaze.update(wave, false, true, false, 1f, false);
        }
        for (int second = 0; second < 6; second++) {
            gaze.update(HollowGaze.FULL_WAVE, false, true, true, 1f, false);
        }
        assertEquals(0f, gaze.openness(true), 1e-6f, "the dawn is done: the night is gone");
        assertTrue(gaze.drift() >= 1f, "and it left by drifting, not by winking");

        // Reduced motion drifts twice as slowly, and still leaves.
        HollowGaze calm = new HollowGaze();
        for (int wave = HollowGaze.HOLLOW_WAVE; wave <= HollowGaze.FULL_WAVE; wave++) {
            calm.update(wave, false, true, false, 1f, true);
        }
        for (int second = 0; second < 12; second++) {
            calm.update(HollowGaze.FULL_WAVE, false, true, true, 1f, true);
        }
        assertEquals(0f, calm.openness(true), 1e-6f, "a calm frame dawns too, just slower");
    }

    @Test
    void theBreathHoldsStillForReducedMotion() {
        assertEquals(0f, HollowGaze.breath(3f, true), 1e-6f, "no shaking, no drifting breath");
        for (int step = 0; step <= 28; step++) {
            float b = HollowGaze.breath(step / 2f, false);
            assertTrue(b >= -1f && b <= 1f, "the breath stays a breath at step " + step);
        }
    }
}
