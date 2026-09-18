package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.settings.GameSettings;
import org.junit.jupiter.api.Test;

/**
 * Roadmap G3-layout and G3c: the settings viewport supports scroll offsets and drag gestures so that
 * additional accessibility options fit cleanly without screen crowding.
 */
final class SettingsScrollLayoutTest {

    @Test
    void slotYMatchesStandardSixRowPositions() {
        assertEquals(SettingsTouchLayout.SOUND_ROW_Y, SettingsTouchLayout.slotY(0));
        assertEquals(SettingsTouchLayout.MUSIC_ROW_Y, SettingsTouchLayout.slotY(1));
        assertEquals(SettingsTouchLayout.SOUND_LEVEL_ROW_Y, SettingsTouchLayout.slotY(2));
        assertEquals(SettingsTouchLayout.MUSIC_LEVEL_ROW_Y, SettingsTouchLayout.slotY(3));
        assertEquals(SettingsTouchLayout.LANGUAGE_ROW_Y, SettingsTouchLayout.slotY(4));
        assertEquals(SettingsTouchLayout.REDUCED_MOTION_ROW_Y, SettingsTouchLayout.slotY(5));
        assertEquals(-1000f, SettingsTouchLayout.slotY(-1));
        assertEquals(-1000f, SettingsTouchLayout.slotY(6));
    }

    @Test
    void visibleSlotAtMapsCoordinatesCorrectly() {
        for (int slot = 0; slot < SettingsTouchLayout.VISIBLE_ROWS; slot++) {
            float y = SettingsTouchLayout.slotY(slot) + 20f;
            assertEquals(slot, SettingsTouchLayout.visibleSlotAt(y), "slot " + slot + " must be detected at Y=" + y);
        }
        assertEquals(-1, SettingsTouchLayout.visibleSlotAt(50f), "coordinates below list return -1");
        assertEquals(-1, SettingsTouchLayout.visibleSlotAt(1200f), "coordinates above list return -1");
    }

    @Test
    void actionAtDispatchesBasedOnFirstVisibleIndex() {
        float cx = SettingsTouchLayout.ROW_X + 50f;
        float slot0Y = SettingsTouchLayout.slotY(0) + 20f;
        float slot1Y = SettingsTouchLayout.slotY(1) + 20f;

        // At scroll offset 0
        assertEquals(SettingsTouchLayout.Action.TOGGLE_SOUND, SettingsTouchLayout.actionAt(cx, slot0Y, 0));
        assertEquals(SettingsTouchLayout.Action.TOGGLE_MUSIC, SettingsTouchLayout.actionAt(cx, slot1Y, 0));

        // At scroll offset 1 (scrolled down by 1 row)
        assertEquals(SettingsTouchLayout.Action.TOGGLE_MUSIC, SettingsTouchLayout.actionAt(cx, slot0Y, 1));
        assertEquals(SettingsTouchLayout.Action.CYCLE_SOUND_LEVEL, SettingsTouchLayout.actionAt(cx, slot1Y, 1));

        // Close button is invariant to scroll offset
        float closeX = SettingsTouchLayout.closeX() + 20f;
        float closeY = SettingsTouchLayout.CLOSE_Y + 20f;
        assertEquals(SettingsTouchLayout.Action.CLOSE, SettingsTouchLayout.actionAt(closeX, closeY, 0));
        assertEquals(SettingsTouchLayout.Action.CLOSE, SettingsTouchLayout.actionAt(closeX, closeY, 3));
    }

    @Test
    void controllerDragScrollsAndClampsCleanly() {
        SettingsTouchController controller = new SettingsTouchController();
        assertEquals(0, controller.firstVisibleIndex());

        // With totalRows = 6 (ceiling of visible rows), drag does not scroll
        controller.drag(100f, 6);
        assertEquals(0, controller.firstVisibleIndex());

        // With totalRows = 8, maxFirstVisible = 2
        controller.drag(60f, 8);
        assertEquals(1, controller.firstVisibleIndex());

        controller.drag(60f, 8);
        assertEquals(2, controller.firstVisibleIndex());

        // Clamped at maxFirstVisible
        controller.drag(120f, 8);
        assertEquals(2, controller.firstVisibleIndex());

        // Dragging down scrolls back
        controller.drag(-60f, 8);
        assertEquals(1, controller.firstVisibleIndex());

        controller.drag(-60f, 8);
        assertEquals(0, controller.firstVisibleIndex());

        // Clamped at 0
        controller.drag(-100f, 8);
        assertEquals(0, controller.firstVisibleIndex());
    }

    @Test
    void controllerDispatchesScrolledActionOnTap() {
        GameSettings settings = new GameSettings();
        SettingsTouchController controller = new SettingsTouchController();

        float cx = SettingsTouchLayout.ROW_X + 50f;
        float slot0Y = SettingsTouchLayout.slotY(0) + 20f;

        // Scrolled to offset 1
        controller.drag(60f, 8);
        assertEquals(1, controller.firstVisibleIndex());

        // Slot 0 now corresponds to row 1 (TOGGLE_MUSIC)
        assertTrue(settings.musicEnabled);
        SettingsTouchLayout.Action action = controller.tap(settings, cx, slot0Y);
        assertEquals(SettingsTouchLayout.Action.TOGGLE_MUSIC, action);
        assertFalse(settings.musicEnabled);

        // Reset via open()
        controller.open();
        assertEquals(0, controller.firstVisibleIndex());
    }

    @Test
    void scrollingExposesAndTogglesColourBlindRarityRow() {
        GameSettings settings = new GameSettings();
        SettingsTouchController controller = new SettingsTouchController();
        assertEquals(8, SettingsTouchLayout.TOTAL_ROWS);

        assertFalse(settings.colourBlindRarity);

        // Drag up by 120f -> firstVisibleIndex becomes 2
        controller.drag(120f);
        assertEquals(2, controller.firstVisibleIndex());

        // Slot 5 is the bottom visible row: 2 + 5 = row 7 (TOGGLE_COLOUR_BLIND_RARITY)
        float cx = SettingsTouchLayout.ROW_X + 50f;
        float slot5Y = SettingsTouchLayout.slotY(5) + 20f;
        SettingsTouchLayout.Action action = controller.tap(settings, cx, slot5Y);
        assertEquals(SettingsTouchLayout.Action.TOGGLE_COLOUR_BLIND_RARITY, action);
        assertTrue(settings.colourBlindRarity, "tapping accessible rarity row toggles it on");

        controller.tap(settings, cx, slot5Y);
        assertFalse(settings.colourBlindRarity, "tapping it again toggles it off");
    }
}
