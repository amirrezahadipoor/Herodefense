package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.render.UiMirror;

/**
 * Tap-only settings bounds shared with the renderer (roadmap R6.4 added the two level rows, R7.3 the language
 * row, which is why the footer note moved down to make room for a fifth).
 *
 * <p>R7.3 also mirrors the screen, and a mirrored drawing with an unmirrored tap target is a button the player
 * can see in one place and press in another. So the one element on this screen that is not symmetric -- the close
 * button in the top corner -- has its x come from {@link #closeX()}, the same {@link UiMirror} call the renderer
 * makes for the icon it draws. The five rows need no such call: they span 100f..620f of a 720f screen, so their
 * margins are equal and mirroring leaves them exactly where they were.
 */
public final class SettingsTouchLayout {
    public enum Action {
        NONE, TOGGLE_SOUND, TOGGLE_MUSIC, CYCLE_SOUND_LEVEL, CYCLE_MUSIC_LEVEL, CYCLE_LANGUAGE, CLOSE
    }

    public static final float ROW_X = 100f;
    public static final float ROW_WIDTH = 520f;
    public static final float ROW_HEIGHT = 150f;

    public static final float SOUND_ROW_Y = 900f;
    public static final float MUSIC_ROW_Y = 750f;
    public static final float SOUND_LEVEL_ROW_Y = 600f;
    public static final float MUSIC_LEVEL_ROW_Y = 450f;
    public static final float LANGUAGE_ROW_Y = 300f;
    public static final float CLOSE_X = 570f;
    public static final float CLOSE_Y = 1120f;
    public static final float CLOSE_SIZE = 100f;
    /** How far the close box's trailing edge sits from the screen's trailing edge: 720f - 570f - 100f. */
    static final float CLOSE_INSET = 50f;

    private SettingsTouchLayout() {
    }

    /** The close box's left edge in the language in force; {@link #CLOSE_X} is the left-to-right one. */
    public static float closeX() {
        return UiMirror.trailingOnScreen(CLOSE_INSET, CLOSE_SIZE);
    }

    public static Action actionAt(float x, float y) {
        float closeX = closeX();
        if (x >= closeX && x <= closeX + CLOSE_SIZE && y >= CLOSE_Y && y <= CLOSE_Y + CLOSE_SIZE) {
            return Action.CLOSE;
        }
        if (x < ROW_X || x > ROW_X + ROW_WIDTH) return Action.NONE;
        if (inRow(y, SOUND_ROW_Y)) return Action.TOGGLE_SOUND;
        if (inRow(y, MUSIC_ROW_Y)) return Action.TOGGLE_MUSIC;
        if (inRow(y, SOUND_LEVEL_ROW_Y)) return Action.CYCLE_SOUND_LEVEL;
        if (inRow(y, MUSIC_LEVEL_ROW_Y)) return Action.CYCLE_MUSIC_LEVEL;
        if (inRow(y, LANGUAGE_ROW_Y)) return Action.CYCLE_LANGUAGE;
        return Action.NONE;
    }

    private static boolean inRow(float y, float rowY) {
        return y >= rowY && y <= rowY + ROW_HEIGHT;
    }
}
