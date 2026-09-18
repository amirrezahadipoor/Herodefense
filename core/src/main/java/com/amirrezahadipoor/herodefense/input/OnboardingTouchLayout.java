package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.WorldLayout;

/**
 * Where the first-run coaching banner and its Skip button live (roadmap R7.1).
 *
 * <p>The banner is anchored above the utility row rather than in the middle of the arena: the coaching is
 * about the arena, so it must not cover the ground the player is being told to tap, and it must not cover
 * the row of buttons it talks about. Its Skip target follows the shared 120-by-100 phone-HUD minimum, so a
 * player who wants to stop being taught can hit it on the first try, and the banner slides with the utility
 * row on tall panels the way every other HUD row does.
 */
public final class OnboardingTouchLayout {
    public static final float BANNER_WIDTH = 640f;
    public static final float BANNER_HEIGHT = 136f;
    /** World units between the utility row's top edge and the banner's bottom edge. */
    public static final float BANNER_GAP = 26f;
    /** Wide enough for the word Skip at the HUD label size, and tall enough to be the HUD's target. */
    public static final float SKIP_WIDTH = 150f;
    public static final float SKIP_HEIGHT = 100f;
    public static final float SKIP_INSET = 16f;

    private OnboardingTouchLayout() {
    }

    /** Bottom edge of the banner: above whatever row the utility buttons are on right now. */
    public static float bannerY() {
        return HudTouchLayout.utilityButtonY() + HudTouchLayout.UTILITY_BUTTON_HEIGHT + BANNER_GAP;
    }

    public static float bannerX() {
        return (WorldLayout.REFERENCE_WIDTH - BANNER_WIDTH) * 0.5f;
    }

    public static float bannerTop() {
        return bannerY() + BANNER_HEIGHT;
    }

    public static float skipX() {
        return bannerX() + BANNER_WIDTH - SKIP_WIDTH - SKIP_INSET;
    }

    public static float skipY() {
        return bannerY() + (BANNER_HEIGHT - SKIP_HEIGHT) * 0.5f;
    }

    /** True when a point is anywhere on the banner. */
    public static boolean contains(float x, float y) {
        return x >= bannerX() && x <= bannerX() + BANNER_WIDTH
            && y >= bannerY() && y <= bannerTop();
    }

    /** True when a tap is on the Skip target. */
    public static boolean skipAt(float x, float y) {
        return x >= skipX() && x <= skipX() + SKIP_WIDTH
            && y >= skipY() && y <= skipY() + SKIP_HEIGHT;
    }
}
