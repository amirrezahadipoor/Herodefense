package com.amirrezahadipoor.herodefense.input;

/** Tap-only settings bounds shared with the renderer (roadmap R6.4 added the two level rows). */
public final class SettingsTouchLayout {
    public enum Action {
        NONE, TOGGLE_SOUND, TOGGLE_MUSIC, CYCLE_SOUND_LEVEL, CYCLE_MUSIC_LEVEL, CLOSE
    }

    public static final float ROW_X = 100f;
    public static final float ROW_WIDTH = 520f;
    public static final float ROW_HEIGHT = 150f;

    public static final float SOUND_ROW_Y = 900f;
    public static final float MUSIC_ROW_Y = 750f;
    public static final float SOUND_LEVEL_ROW_Y = 600f;
    public static final float MUSIC_LEVEL_ROW_Y = 450f;
    public static final float CLOSE_X = 570f;
    public static final float CLOSE_Y = 1120f;
    public static final float CLOSE_SIZE = 100f;

    private SettingsTouchLayout() {
    }

    public static Action actionAt(float x, float y) {
        if (x >= CLOSE_X && x <= CLOSE_X + CLOSE_SIZE && y >= CLOSE_Y && y <= CLOSE_Y + CLOSE_SIZE) {
            return Action.CLOSE;
        }
        if (x < ROW_X || x > ROW_X + ROW_WIDTH) return Action.NONE;
        if (inRow(y, SOUND_ROW_Y)) return Action.TOGGLE_SOUND;
        if (inRow(y, MUSIC_ROW_Y)) return Action.TOGGLE_MUSIC;
        if (inRow(y, SOUND_LEVEL_ROW_Y)) return Action.CYCLE_SOUND_LEVEL;
        if (inRow(y, MUSIC_LEVEL_ROW_Y)) return Action.CYCLE_MUSIC_LEVEL;
        return Action.NONE;
    }

    private static boolean inRow(float y, float rowY) {
        return y >= rowY && y <= rowY + ROW_HEIGHT;
    }
}
