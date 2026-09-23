package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class HudTouchLayoutTest {
    @Test
    void gameplayTargetsAreGenerousDistinctAndPortraitSafe() {
        assertTrue(HudTouchLayout.BUTTON_WIDTH >= 96f);
        assertTrue(HudTouchLayout.BUTTON_HEIGHT >= 96f);
        assertTrue(HudTouchLayout.UTILITY_BUTTON_WIDTH >= 96f);
        assertTrue(HudTouchLayout.UTILITY_BUTTON_HEIGHT >= 96f);
        assertTrue(HudTouchLayout.speedAt(490f, 1115f));
        assertTrue(HudTouchLayout.pauseAt(630f, 1115f));
        assertTrue(HudTouchLayout.inventoryAt(270f, 76f));
        assertTrue(HudTouchLayout.shopAt(450f, 76f));
        assertTrue(HudTouchLayout.ultimateAt(630f, 76f));
        assertFalse(HudTouchLayout.ultimateAt(450f, 76f));
        assertFalse(HudTouchLayout.shopAt(630f, 76f));
        assertTrue(HudTouchLayout.ULTIMATE_X + HudTouchLayout.UTILITY_BUTTON_WIDTH <= 720f);
        assertFalse(HudTouchLayout.pauseAt(490f, 1115f));
        assertFalse(HudTouchLayout.speedAt(630f, 1115f));
        assertFalse(HudTouchLayout.inventoryAt(450f, 76f));
        assertFalse(HudTouchLayout.shopAt(270f, 76f));
    }

    @Test
    void theHudDrawsTheFrozenDesignGridAndTheHitTestsMatchIt() {
        assertEquals(HudTouchLayout.SPEED_X, HudTouchLayout.speedX(), "the screen draws the frozen design grid");
        assertEquals(HudTouchLayout.PAUSE_X, HudTouchLayout.pauseX());
        assertEquals(HudTouchLayout.INVENTORY_X, HudTouchLayout.inventoryX());
        assertEquals(HudTouchLayout.SHOP_X, HudTouchLayout.shopX());
        assertEquals(HudTouchLayout.ULTIMATE_X, HudTouchLayout.ultimateX());
    }
}
