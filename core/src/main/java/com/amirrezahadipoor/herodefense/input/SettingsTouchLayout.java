package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.render.UiMirror;

/**
 * Tap and drag-scroll settings bounds shared with the renderer.
 * F3 adds narration rows, G3d adds screen-reader row.
 */
public final class SettingsTouchLayout {
    public enum Action {
        NONE, TOGGLE_SOUND, TOGGLE_MUSIC, CYCLE_SOUND_LEVEL, CYCLE_MUSIC_LEVEL, CYCLE_LANGUAGE,
        TOGGLE_REDUCED_MOTION, CYCLE_TEXT_SIZE, TOGGLE_COLOUR_BLIND_RARITY,
        TOGGLE_NARRATION, CYCLE_NARRATION_LEVEL,
        TOGGLE_SCREEN_READER,
        CLOSE
    }

    public static final float ROW_X = 100f;
    public static final float ROW_WIDTH = 520f;
    public static final float ROW_HEIGHT = 130f;

    public static final float SOUND_ROW_Y = 960f;
    public static final float MUSIC_ROW_Y = 820f;
    public static final float SOUND_LEVEL_ROW_Y = 680f;
    public static final float MUSIC_LEVEL_ROW_Y = 540f;
    public static final float LANGUAGE_ROW_Y = 400f;
    public static final float REDUCED_MOTION_ROW_Y = 270f;
    public static final float CLOSE_X = 570f;
    public static final float CLOSE_Y = 1120f;
    public static final float CLOSE_SIZE = 100f;
    static final float CLOSE_INSET = 50f;

    public static final int VISIBLE_ROWS = 6;
    public static final int TOTAL_ROWS = 11;

    private SettingsTouchLayout() {
    }

    public static float closeX() {
        return UiMirror.trailingOnScreen(CLOSE_INSET, CLOSE_SIZE);
    }

    public static float slotY(int slot) {
        return switch (slot) {
            case 0 -> SOUND_ROW_Y;
            case 1 -> MUSIC_ROW_Y;
            case 2 -> SOUND_LEVEL_ROW_Y;
            case 3 -> MUSIC_LEVEL_ROW_Y;
            case 4 -> LANGUAGE_ROW_Y;
            case 5 -> REDUCED_MOTION_ROW_Y;
            default -> -1000f;
        };
    }

    public static int visibleSlotAt(float y) {
        for (int slot = 0; slot < VISIBLE_ROWS; slot++) {
            float bottom = slotY(slot);
            if (y >= bottom && y <= bottom + ROW_HEIGHT) {
                return slot;
            }
        }
        return -1;
    }

    public static Action actionForRow(int rowIndex) {
        return switch (rowIndex) {
            case 0 -> Action.TOGGLE_SOUND;
            case 1 -> Action.TOGGLE_MUSIC;
            case 2 -> Action.CYCLE_SOUND_LEVEL;
            case 3 -> Action.CYCLE_MUSIC_LEVEL;
            case 4 -> Action.CYCLE_LANGUAGE;
            case 5 -> Action.TOGGLE_REDUCED_MOTION;
            case 6 -> Action.CYCLE_TEXT_SIZE;
            case 7 -> Action.TOGGLE_COLOUR_BLIND_RARITY;
            case 8 -> Action.TOGGLE_NARRATION;
            case 9 -> Action.CYCLE_NARRATION_LEVEL;
            case 10 -> Action.TOGGLE_SCREEN_READER;
            default -> Action.NONE;
        };
    }

    public static Action actionAt(float x, float y, int firstVisibleIndex) {
        float closeX = closeX();
        if (x >= closeX && x <= closeX + CLOSE_SIZE && y >= CLOSE_Y && y <= CLOSE_Y + CLOSE_SIZE) {
            return Action.CLOSE;
        }
        if (x < ROW_X || x > ROW_X + ROW_WIDTH) return Action.NONE;
        int slot = visibleSlotAt(y);
        if (slot < 0) return Action.NONE;
        return actionForRow(firstVisibleIndex + slot);
    }

    public static Action actionAt(float x, float y) {
        return actionAt(x, y, 0);
    }
}
