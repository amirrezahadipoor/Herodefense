package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The thumb floor every overlay answers to (roadmap G5). HudTouchLayoutTest has always held the in-run HUD
 * to 96-unit targets; this is the same standard for the screens a thumb lands on most often while browsing:
 * the codex rows and tabs, the inventory slots, backpack rows and auto-sell chips, the shop tabs and root
 * button. Heights at the floor and a real gap between rows, plus the clearances that keep the taller
 * geometry from sliding under a neighbouring surface -- the failure mode a screenshot used to be the only
 * way to catch.
 */
final class OverlayTouchDensityTest {

    private static final float THUMB_FLOOR = 96f;
    private static final float MINIMUM_GAP = 8f;

    @Test
    void everyOverlayTargetMeetsTheThumbFloor() {
        assertTrue(CodexTouchLayout.LIST_ROW_HEIGHT >= THUMB_FLOOR);
        assertTrue(CodexTouchLayout.TAB_HEIGHT >= THUMB_FLOOR);
        assertTrue(CodexTouchLayout.LIST_ROW_STRIDE - CodexTouchLayout.LIST_ROW_HEIGHT >= MINIMUM_GAP);
        assertTrue(InventoryTouchLayout.SLOT_HEIGHT >= THUMB_FLOOR);
        assertTrue(InventoryTouchLayout.LIST_ROW_HEIGHT >= THUMB_FLOOR);
        assertTrue(InventoryTouchLayout.AUTO_SELL_HEIGHT >= THUMB_FLOOR);
        assertTrue(InventoryTouchLayout.SLOT_ROW_STRIDE - InventoryTouchLayout.SLOT_HEIGHT >= MINIMUM_GAP);
        assertTrue(InventoryTouchLayout.LIST_ROW_STRIDE - InventoryTouchLayout.LIST_ROW_HEIGHT >= MINIMUM_GAP);
        assertTrue(StatShopTouchLayout.TAB_HEIGHT >= THUMB_FLOOR);
        assertTrue(StatShopTouchLayout.ROOT_HEIGHT >= THUMB_FLOOR);
    }

    @Test
    void theCodexShelvesStillClearTheTabsAndTheDetailsPanel() {
        assertTrue(CodexTouchLayout.LIST_TOP_Y <= CodexTouchLayout.TAB_Y,
            "the lore shelf starts under the tab strip");
        assertTrue(CodexTouchLayout.TROPHY_LIST_TOP_Y <= CodexTouchLayout.TAB_Y,
            "and so does the trophy shelf");
        float loreBottom = CodexTouchLayout.rowBottom(CodexTouchLayout.VISIBLE_ROWS - 1);
        float trophyBottom =
            CodexTouchLayout.rowBottom(CodexTouchLayout.Tab.TROPHIES, CodexTouchLayout.VISIBLE_ROWS - 1);
        // The details panel is 60 + 380 tall; the last row of either shelf stays clear of its top edge.
        assertTrue(loreBottom >= 456f, "lore rows never touch the details panel");
        assertTrue(trophyBottom >= 456f, "trophy rows never touch the details panel");
        assertEquals(CodexTouchLayout.LIST_TOP_Y, CodexTouchLayout.TROPHY_LIST_TOP_Y,
            "both shelves share one geometry now");
    }

    @Test
    void theInventoryColumnsStillClearTheirNeighbours() {
        float lastRowBottom = InventoryTouchLayout.LIST_TOP_Y
            - InventoryTouchLayout.LIST_ROW_HEIGHT
            - (InventoryTouchLayout.VISIBLE_ROWS - 1) * InventoryTouchLayout.LIST_ROW_STRIDE;
        // The set-bonus line sits at 194 and the transient feedback toast at 216; the action buttons top
        // out at 190. The fourth row has to end above all of it.
        assertTrue(lastRowBottom >= 240f, "the fourth backpack row clears the set-bonus strip");
        float lowestSlotBottom = InventoryTouchLayout.SLOT_TOP_Y
            - InventoryTouchLayout.SLOT_HEIGHT
            - 2 * InventoryTouchLayout.SLOT_ROW_STRIDE;
        assertTrue(lowestSlotBottom >= InventoryTouchLayout.LIST_TOP_Y,
            "the taller slots never sit inside the backpack list");
        assertTrue(InventoryTouchLayout.AUTO_SELL_Y + InventoryTouchLayout.AUTO_SELL_HEIGHT <= 1160f,
            "the taller chips stay under the header band");
    }

    @Test
    void theShopRowsStillClearTheTabsAndTheHelpPanel() {
        assertTrue(StatShopTouchLayout.ROW_TOP <= StatShopTouchLayout.TAB_Y,
            "the five rows start under the taller tabs");
        assertTrue(StatShopTouchLayout.TAB_Y + StatShopTouchLayout.TAB_HEIGHT <= StatShopTouchLayout.ROOT_Y,
            "and the tabs never reach the root button or the close button beside it");
        // The help panel is 112 + 98 tall; the fifth row stays clear of its top edge.
        assertTrue(StatShopTouchLayout.rowBottom(4) >= 226f, "rows never touch the help panel");
    }
}
