package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/**
 * Roadmap R7.1: the coaching banner has to be reachable and unobtrusive at the same time. Reachable, because
 * Skip is the one control a player who does not want the lesson has to hit on the first try; unobtrusive,
 * because the lesson is about the arena — a banner over the middle of it would cover the ground the player is
 * being asked to tap, and one over the utility row would cover the shop button it talks about.
 */
final class OnboardingTouchLayoutTest {
    private static final float HUD_MINIMUM_TARGET = 100f;

    @Test
    void theSkipTargetIsAStandardsSizedPhoneTarget() {
        assertTrue(OnboardingTouchLayout.SKIP_HEIGHT >= HUD_MINIMUM_TARGET,
            "Skip is at least as big as every other HUD target: " + OnboardingTouchLayout.SKIP_HEIGHT);
        assertTrue(OnboardingTouchLayout.SKIP_WIDTH >= HudTouchLayout.BUTTON_WIDTH,
            "and no narrower than the status-row buttons");
        assertTrue(OnboardingTouchLayout.skipAt(
            OnboardingTouchLayout.skipX() + 1f, OnboardingTouchLayout.skipY() + 1f));
        assertTrue(OnboardingTouchLayout.skipAt(
            OnboardingTouchLayout.skipX() + OnboardingTouchLayout.SKIP_WIDTH - 1f,
            OnboardingTouchLayout.skipY() + OnboardingTouchLayout.SKIP_HEIGHT - 1f));
        assertFalse(OnboardingTouchLayout.skipAt(
            OnboardingTouchLayout.skipX() - 2f, OnboardingTouchLayout.skipY() + 10f));
        assertFalse(OnboardingTouchLayout.skipAt(
            OnboardingTouchLayout.skipX() + 10f, OnboardingTouchLayout.skipY() + OnboardingTouchLayout.SKIP_HEIGHT + 2f));
    }

    @Test
    void theBannerSitsAboveTheUtilityRowAndBelowTheStatusRow() {
        float utilityTop = HudTouchLayout.utilityButtonY() + HudTouchLayout.UTILITY_BUTTON_HEIGHT;
        assertTrue(OnboardingTouchLayout.bannerY() >= utilityTop,
            "the banner never covers the utility row it points at");
        assertTrue(OnboardingTouchLayout.bannerTop() < HudTouchLayout.buttonY(),
            "and never reaches the status row: " + OnboardingTouchLayout.bannerTop());
    }

    @Test
    void theBannerLeavesTheArenaFloorAndTheHeroAlone() {
        assertTrue(OnboardingTouchLayout.bannerTop() < GameState.ARENA_CENTER_Y - 200f,
            "the ground the player is told to tap, and the Hero standing on it, stay uncovered: "
                + OnboardingTouchLayout.bannerTop());
        assertTrue(OnboardingTouchLayout.bannerX() >= 0f);
        assertTrue(OnboardingTouchLayout.bannerX() + OnboardingTouchLayout.BANNER_WIDTH
            <= WorldLayout.REFERENCE_WIDTH,
            "the banner fits the design width");
    }

    @Test
    void theSkipTargetIsInsideTheBannerItBelongsTo() {
        assertTrue(OnboardingTouchLayout.skipX() >= OnboardingTouchLayout.bannerX());
        assertTrue(OnboardingTouchLayout.skipX() + OnboardingTouchLayout.SKIP_WIDTH
            <= OnboardingTouchLayout.bannerX() + OnboardingTouchLayout.BANNER_WIDTH);
        assertTrue(OnboardingTouchLayout.skipY() >= OnboardingTouchLayout.bannerY());
        assertTrue(OnboardingTouchLayout.skipY() + OnboardingTouchLayout.SKIP_HEIGHT
            <= OnboardingTouchLayout.bannerTop());
        assertTrue(OnboardingTouchLayout.contains(
            OnboardingTouchLayout.skipX() + 5f, OnboardingTouchLayout.skipY() + 5f));
    }

    @Test
    void everyPointOfTheSkipTargetIsPartOfTheBanner() {
        int step = 10;
        for (int offsetX = 0; offsetX <= (int) OnboardingTouchLayout.SKIP_WIDTH; offsetX += step) {
            for (int offsetY = 0; offsetY <= (int) OnboardingTouchLayout.SKIP_HEIGHT; offsetY += step) {
                float x = OnboardingTouchLayout.skipX() + offsetX;
                float y = OnboardingTouchLayout.skipY() + offsetY;
                if (OnboardingTouchLayout.skipAt(x, y)) {
                    assertTrue(OnboardingTouchLayout.contains(x, y),
                        "Skip at " + x + "," + y + " is drawn inside the banner, so its target must be too");
                }
            }
        }
    }
}
