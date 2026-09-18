package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.progression.Trophy;
import com.amirrezahadipoor.herodefense.story.LoreCatalog;
import org.junit.jupiter.api.Test;

/**
 * The Codex's second shelf (roadmap R3.3): the trophy board is reachable, scrolls to its own end, and selecting a
 * trophy cannot be mistaken for selecting the lore entry that happens to sit at the same row number.
 */
final class CodexTrophyShelfTest {

    private static float tabCenterX(CodexTouchLayout.Tab tab) {
        return tab == CodexTouchLayout.Tab.LORE
            ? CodexTouchLayout.TAB_LEFT_X + CodexTouchLayout.TAB_WIDTH / 2f
            : CodexTouchLayout.TAB_RIGHT_X + CodexTouchLayout.TAB_WIDTH / 2f;
    }

    @Test
    void theCodexOpensOnTheLoreShelfAndSwitchesToTrophies() {
        CodexTouchController controller = new CodexTouchController();
        controller.open();

        assertEquals(CodexTouchLayout.Tab.LORE, controller.tab(), "the grove's words come first");
        assertEquals(LoreCatalog.all().size(), controller.rowCount());

        assertEquals(
            CodexTouchController.Action.TAB_CHANGED,
            controller.tap(new GameState(), tabCenterX(CodexTouchLayout.Tab.TROPHIES), CodexTouchLayout.TAB_Y + 20f)
        );
        assertEquals(CodexTouchLayout.Tab.TROPHIES, controller.tab());
        assertEquals(Trophy.values().length, controller.rowCount());
    }

    @Test
    void tappingTheShelfAlreadyShowingChangesNothing() {
        CodexTouchController controller = new CodexTouchController();
        controller.open();

        assertEquals(
            CodexTouchController.Action.NONE,
            controller.tap(new GameState(), tabCenterX(CodexTouchLayout.Tab.LORE), CodexTouchLayout.TAB_Y + 20f)
        );
        assertEquals(CodexTouchLayout.Tab.LORE, controller.tab());
    }

    @Test
    void aTrophySelectionDoesNotBecomeALoreSelection() {
        CodexTouchController controller = new CodexTouchController();
        controller.open();
        controller.tap(new GameState(), tabCenterX(CodexTouchLayout.Tab.TROPHIES), CodexTouchLayout.TAB_Y + 20f);

        float x = CodexTouchLayout.LIST_X + 40f;
        float y = CodexTouchLayout.rowBottom(CodexTouchLayout.Tab.TROPHIES, 1) + 20f;
        assertEquals(CodexTouchController.Action.SELECTED, controller.tap(new GameState(), x, y));
        assertEquals(1, controller.selectedIndex(), "row 1 of the trophy shelf is trophy 1");

        controller.tap(new GameState(), tabCenterX(CodexTouchLayout.Tab.LORE), CodexTouchLayout.TAB_Y + 20f);
        assertEquals(-1, controller.selectedIndex(), "switching shelves clears the selection instead of carrying it");
        assertEquals(0, controller.firstVisibleIndex(), "and the shelf scrolls back to its top");
    }

    @Test
    void theTrophyShelfScrollsToItsOwnEndAndStops() {
        CodexTouchController controller = new CodexTouchController();
        controller.open();
        controller.tap(new GameState(), tabCenterX(CodexTouchLayout.Tab.TROPHIES), CodexTouchLayout.TAB_Y + 20f);

        // The controller's convention: a positive drag delta advances the list.
        for (int drag = 0; drag < 40; drag++) {
            controller.drag(new GameState(), 100f);
        }

        int expectedMax = Math.max(0, Trophy.values().length - CodexTouchLayout.VISIBLE_ROWS);
        assertEquals(expectedMax, controller.firstVisibleIndex(), "the list stops at its last full page");
        assertNotEquals(LoreCatalog.all().size() - CodexTouchLayout.VISIBLE_ROWS, controller.firstVisibleIndex(),
            "the lore list's length does not leak into the trophy shelf");
    }

    @Test
    void bothShelvesClearTheTabStripAndShareOneGeometry() {
        float firstTrophyRow = CodexTouchLayout.rowBottom(CodexTouchLayout.Tab.TROPHIES, 0)
            + CodexTouchLayout.LIST_ROW_HEIGHT;
        assertTrue(firstTrophyRow <= CodexTouchLayout.TAB_Y,
            "the first trophy row must clear the tabs, got " + firstTrophyRow);

        float firstLoreRow = CodexTouchLayout.rowBottom(CodexTouchLayout.Tab.LORE, 0)
            + CodexTouchLayout.LIST_ROW_HEIGHT;
        assertTrue(firstLoreRow <= CodexTouchLayout.TAB_Y, "the lore shelf is not covered by the tabs either");
        assertEquals(CodexTouchLayout.LIST_TOP_Y, firstLoreRow, "the lore top is the layout constant");
        // G5 gave the rows the 96-unit thumb floor and the two shelves the same geometry: the old 32-unit
        // stagger was a leftover of the lore shelf starting "under the header" and the trophies "under the
        // tabs", and both readings were of the same strip. One top, one stride, one row height.
        assertEquals(firstLoreRow, firstTrophyRow, "both shelves start under the strip that names them");
    }

    @Test
    void aTrophyRowOutsideTheShelfIsIgnored() {
        CodexTouchController controller = new CodexTouchController();
        controller.open();
        controller.tap(new GameState(), tabCenterX(CodexTouchLayout.Tab.TROPHIES), CodexTouchLayout.TAB_Y + 20f);

        assertEquals(CodexTouchController.Action.NONE,
            controller.tap(new GameState(), 10f, CodexTouchLayout.TAB_Y + 20f),
            "taps outside the list are not selections");
    }
}
