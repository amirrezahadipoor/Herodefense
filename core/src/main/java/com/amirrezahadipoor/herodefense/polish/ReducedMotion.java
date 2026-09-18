package com.amirrezahadipoor.herodefense.polish;

import com.amirrezahadipoor.herodefense.settings.GameSettings;

/**
 * What the reduced-motion preference switches off (roadmap G3a).
 *
 * <p>One question, one place. The preference could have been read inline wherever a camera impulse or an ambient
 * drift is drawn, and for two call sites that would be fewer files; it would also mean that the third call site
 * somebody adds next year has to remember the preference exists, and an accessibility setting that only works
 * where somebody remembered it is not an accessibility setting. So the gate is a named thing, and
 * {@code ReducedMotionSettingsTest} fails if the two places that draw motion stop consulting it.
 *
 * <p>What it covers, and what it deliberately does not: the camera impulse and the ambient spore drift are
 * motion the player did not ask for and cannot time, which is what vestibular sensitivity reacts to. Hit
 * particles, floating damage numbers and the hit-stop pause stay on, because those are how the game says
 * "something happened" and removing them would cost readability that the setting is meant to protect.
 */
public final class ReducedMotion {

    private ReducedMotion() {
    }

    /** True when unpredictable motion should be left undrawn. A missing settings object means motion is on. */
    public static boolean suppresses(GameSettings settings) {
        return settings != null && settings.reducedMotion;
    }
}
