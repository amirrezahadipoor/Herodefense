package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
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
    void theWholeHudMirrorsInPersianAndTheHitTestsFollowTheDrawing() {
        assertEquals(HudTouchLayout.SPEED_X, HudTouchLayout.speedX(), "English draws the frozen design grid");
        assertEquals(HudTouchLayout.INVENTORY_X, HudTouchLayout.inventoryX());
        GameLocale.use(GameLanguage.PERSIAN);
        try {
            // 720 minus the design edge minus the box: the utility row reads ultimate, shop, inventory
            // from the leading (right) edge, and the status row puts pause where speed was.
            assertEquals(170f, HudTouchLayout.speedX());
            assertEquals(30f, HudTouchLayout.pauseX());
            assertEquals(375f, HudTouchLayout.inventoryX());
            assertEquals(195f, HudTouchLayout.shopX());
            assertEquals(15f, HudTouchLayout.ultimateX());
            assertTrue(HudTouchLayout.ultimateX() < HudTouchLayout.shopX());
            assertTrue(HudTouchLayout.shopX() < HudTouchLayout.inventoryX());
            // The finger lands where the mirrored box is, and the English box is no longer there.
            assertTrue(HudTouchLayout.speedAt(200f, 1115f));
            assertFalse(HudTouchLayout.speedAt(490f, 1115f));
            assertTrue(HudTouchLayout.ultimateAt(60f, 76f));
            assertFalse(HudTouchLayout.ultimateAt(630f, 76f));
            assertTrue(HudTouchLayout.inventoryAt(420f, 76f));
            assertFalse(HudTouchLayout.inventoryAt(270f, 76f));
        } finally {
            GameLocale.use(GameLanguage.ENGLISH);
        }
    }
}
