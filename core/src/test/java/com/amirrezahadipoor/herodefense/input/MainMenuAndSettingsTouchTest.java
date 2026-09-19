package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.render.UiMirror;
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
        float centreX = SettingsTouchLayout.ROW_X + SettingsTouchLayout.ROW_WIDTH * 0.5f;

        assertEquals(
            SettingsTouchLayout.Action.TOGGLE_SOUND,
            touch.tap(settings, centreX, SettingsTouchLayout.SOUND_ROW_Y + 40f)
        );
        assertFalse(settings.soundEnabled);
        assertEquals(
            SettingsTouchLayout.Action.TOGGLE_MUSIC,
            touch.tap(settings, centreX, SettingsTouchLayout.MUSIC_ROW_Y + 40f)
        );
        assertFalse(settings.musicEnabled);
        assertEquals(SettingsTouchLayout.Action.CLOSE,
            touch.tap(settings, SettingsTouchLayout.CLOSE_X + 40f, SettingsTouchLayout.CLOSE_Y + 40f));
        assertTrue(SettingsTouchLayout.ROW_HEIGHT >= 96f);

        // G3b: text-size row cycles through 3 steps, scrolls into view
        touch.open();
        touch.drag(60f, SettingsTouchLayout.TOTAL_ROWS);
        // After scrolling 1 step, slot 5 is the new text-size row (index 6)
        float textSizeSlotY = SettingsTouchLayout.slotY(5) + 40f;
        assertEquals(SettingsTouchLayout.Action.CYCLE_TEXT_SIZE,
            SettingsTouchLayout.actionAt(centreX, textSizeSlotY, 1));
        int before = settings.textSizeIndex;
        touch.tap(settings, centreX, textSizeSlotY);
        assertEquals((before + 1) % GameSettings.textSizeCount(), settings.textSizeIndex,
            "tapping text-size row cycles the text scale step");
    }

    @Test
    void theTwoLevelRowsStepTheSettingAndCannotOverlapEachOtherOrTheCloseButton() {
        GameSettings settings = new GameSettings();
        SettingsTouchController touch = new SettingsTouchController();
        float centreX = SettingsTouchLayout.ROW_X + SettingsTouchLayout.ROW_WIDTH * 0.5f;

        assertEquals(
            SettingsTouchLayout.Action.CYCLE_SOUND_LEVEL,
            touch.tap(settings, centreX, SettingsTouchLayout.SOUND_LEVEL_ROW_Y + 40f)
        );
        assertEquals(GameSettings.levelValue(0), settings.soundVolume);
        assertEquals(
            SettingsTouchLayout.Action.CYCLE_MUSIC_LEVEL,
            touch.tap(settings, centreX, SettingsTouchLayout.MUSIC_LEVEL_ROW_Y + 40f)
        );
        assertEquals(GameSettings.levelValue(0), settings.musicVolume);
        touch.tap(settings, centreX, SettingsTouchLayout.MUSIC_LEVEL_ROW_Y + 40f);
        assertEquals(GameSettings.levelValue(1), settings.musicVolume, "the row steps one notch per tap");

        float[] rows = {
            SettingsTouchLayout.SOUND_ROW_Y,
            SettingsTouchLayout.MUSIC_ROW_Y,
            SettingsTouchLayout.SOUND_LEVEL_ROW_Y,
            SettingsTouchLayout.MUSIC_LEVEL_ROW_Y,
            SettingsTouchLayout.LANGUAGE_ROW_Y,
            SettingsTouchLayout.REDUCED_MOTION_ROW_Y
        };
        // TOTAL_ROWS includes the text-size row (G3b) and accessible rarity row (G3c), scrolling into the viewport
        assertEquals(11, SettingsTouchLayout.TOTAL_ROWS, "G3b, G3c, F3, and G3d add text size, colour-blind, narration, and screen-reader rows");
        for (int index = 0; index < rows.length; index++) {
            assertTrue(rows[index] > 260f, "a row must clear the footer note panel");
            if (index > 0) {
                assertTrue(rows[index - 1] - rows[index] >= SettingsTouchLayout.ROW_HEIGHT,
                    "rows must not overlap");
            }
        }
        assertTrue(rows[0] + SettingsTouchLayout.ROW_HEIGHT < SettingsTouchLayout.CLOSE_Y,
            "the first row must clear the close button");
    }

    @Test
    void theCloseButtonMovesWithTheLanguageAndItsTapTargetMovesWithIt() {
        GameLanguage before = GameLocale.current();
        try {
            GameLocale.use(GameLanguage.ENGLISH);
            assertEquals(SettingsTouchLayout.CLOSE_X, SettingsTouchLayout.closeX());
            assertEquals(SettingsTouchLayout.Action.CLOSE,
                SettingsTouchLayout.actionAt(SettingsTouchLayout.CLOSE_X + 40f,
                    SettingsTouchLayout.CLOSE_Y + 40f));

            GameLocale.use(GameLanguage.PERSIAN);
            assertEquals(50f, SettingsTouchLayout.closeX(),
                "the box sits 50f in from the screen's trailing edge, whichever edge that is");
            assertEquals(SettingsTouchLayout.Action.CLOSE,
                SettingsTouchLayout.actionAt(90f, SettingsTouchLayout.CLOSE_Y + 40f));
            assertEquals(SettingsTouchLayout.Action.NONE,
                SettingsTouchLayout.actionAt(SettingsTouchLayout.CLOSE_X + 40f,
                    SettingsTouchLayout.CLOSE_Y + 40f),
                "the tap target left with the drawing: a button seen on one side and pressed on the other is a bug");
            assertTrue(UiMirror.trailingOnScreen(68f, 64f) < UiMirror.SCREEN_WIDTH * 0.5f,
                "and the icon the renderer draws at a 68f inset is on that same side");

            assertEquals(SettingsTouchLayout.Action.CYCLE_LANGUAGE,
                SettingsTouchLayout.actionAt(SettingsTouchLayout.ROW_X + 260f,
                    SettingsTouchLayout.LANGUAGE_ROW_Y + 40f),
                "the five rows span 100f..620f of a 720f screen, so equal margins leave their taps where they were");
        } finally {
            GameLocale.use(before);
        }
    }
}
