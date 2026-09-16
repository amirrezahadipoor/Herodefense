package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.settings.GameSettings;
import org.junit.jupiter.api.Test;

final class MainMenuAndSettingsTouchTest {
    @Test
    void everyMenuRowIsTappableAndTheBriefVigilHasItsOwnRow() {
        float cx = MainMenuTouchLayout.BUTTON_X + 40f;
        assertEquals(MainMenuTouchLayout.Action.NEW_GAME,
            MainMenuTouchLayout.actionAt(cx, MainMenuTouchLayout.rowBottom(0) + 20f, false));
        assertEquals(MainMenuTouchLayout.Action.BRIEF_RUN,
            MainMenuTouchLayout.actionAt(cx, MainMenuTouchLayout.rowBottom(1) + 20f, false),
            "the brief vigil is reachable without a save");
        assertEquals(MainMenuTouchLayout.Action.NONE,
            MainMenuTouchLayout.actionAt(cx, MainMenuTouchLayout.rowBottom(2) + 20f, false),
            "continue stays disabled while there is no run to resume");
        assertEquals(MainMenuTouchLayout.Action.CONTINUE,
            MainMenuTouchLayout.actionAt(cx, MainMenuTouchLayout.rowBottom(2) + 20f, true));
        assertEquals(MainMenuTouchLayout.Action.ROOT_NETWORK,
            MainMenuTouchLayout.actionAt(cx, MainMenuTouchLayout.rowBottom(3) + 20f, false));
        assertEquals(MainMenuTouchLayout.Action.CODEX,
            MainMenuTouchLayout.actionAt(cx, MainMenuTouchLayout.rowBottom(4) + 20f, false));
        assertEquals(MainMenuTouchLayout.Action.SETTINGS,
            MainMenuTouchLayout.actionAt(cx, MainMenuTouchLayout.rowBottom(5) + 20f, false));
    }

    @Test
    void theRowsNeverOverlapAndEveryOneIsBigEnoughForAFinger() {
        assertEquals(MainMenuTouchLayout.BUTTON_HEIGHT, 96f);
        for (int row = 0; row < 5; row++) {
            float gap = MainMenuTouchLayout.rowBottom(row)
                - (MainMenuTouchLayout.rowBottom(row + 1) + MainMenuTouchLayout.BUTTON_HEIGHT);
            assertEquals(22f, gap, 1e-3f, "row " + row + " and " + (row + 1) + " must not overlap");
        }
        assertTrue(MainMenuTouchLayout.rowBottom(5) > 140f, "the last row clears the footer line");
        assertTrue(MainMenuTouchLayout.rowBottom(0) + MainMenuTouchLayout.BUTTON_HEIGHT < 912f,
            "and the first row clears the title panel");
        assertTrue(MainMenuTouchLayout.BUTTON_HEIGHT >= 44f, "a tap target smaller than a finger is a bug");
    }

    @Test
    void settingsUseOnlyLargeTapToggles() {
        GameSettings settings = new GameSettings();
        SettingsTouchController touch = new SettingsTouchController();
        assertEquals(
            SettingsTouchLayout.Action.TOGGLE_SOUND,
            touch.tap(settings, 360f, 775f)
        );
        assertFalse(settings.soundEnabled);
        assertEquals(
            SettingsTouchLayout.Action.TOGGLE_MUSIC,
            touch.tap(settings, 360f, 575f)
        );
        assertFalse(settings.musicEnabled);
        assertEquals(SettingsTouchLayout.Action.CLOSE, touch.tap(settings, 620f, 1170f));
        assertTrue(SettingsTouchLayout.ROW_HEIGHT >= 96f);
    }
}
