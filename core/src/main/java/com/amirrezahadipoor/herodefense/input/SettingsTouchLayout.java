package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.render.UiMirror;

/**
 * Tap-only settings bounds shared with the renderer (roadmap R6.4 added the two level rows, R7.3 the language
 * row, which is why the footer note moved down to make room for a fifth, and roadmap G3a added the sixth).
 *
 * <p>The sixth row did not fit in the space the five were using, so the rows are 130f tall on a 140f pitch
 * instead of 150f on a 150f pitch. Both numbers are constrained from opposite sides and neither was chosen for
 * appearance: {@code MainMenuAndSettingsTouchTest} requires a row to be at least 96f tall, to clear the footer
 * note panel at 260f, not to overlap the row above it, and to clear the close button at 1120f, and the renderer
 * draws a header band from 1096f up. Six rows between 270f and 1090f is what those constraints leave. A
 * seventh row does not fit and needs a scrolling list, which is roadmap item G3-layout.
 *
 * <p>R7.3 also mirrors the screen, and a mirrored drawing with an unmirrored tap target is a button the player
 * can see in one place and press in another. So the one element on this screen that is not symmetric -- the close
 * button in the top corner -- has its x come from {@link #closeX()}, the same {@link UiMirror} call the renderer
 * makes for the icon it draws. The six rows need no such call: they span 100f..620f of a 720f screen, so their
 * margins are equal and mirroring leaves them exactly where they were.
 */
public final class SettingsTouchLayout {
    public enum Action {
        NONE, TOGGLE_SOUND, TOGGLE_MUSIC, CYCLE_SOUND_LEVEL, CYCLE_MUSIC_LEVEL, CYCLE_LANGUAGE,
        TOGGLE_REDUCED_MOTION, CLOSE
    }

    public static final float ROW_X = 100f;
    public static final float ROW_WIDTH = 520f;
    public static final float ROW_HEIGHT = 130f;

    public static final float SOUND_ROW_Y = 960f;
    public static final float MUSIC_ROW_Y = 820f;
    public static final float SOUND_LEVEL_ROW_Y = 680f;
    public static final float MUSIC_LEVEL_ROW_Y = 540f;
    public static final float LANGUAGE_ROW_Y = 400f;
    /** The last row that fits above the footer note panel; a seventh needs a scrolling list (G3-layout). */
    public static final float REDUCED_MOTION_ROW_Y = 270f;
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
        if (inRow(y, REDUCED_MOTION_ROW_Y)) return Action.TOGGLE_REDUCED_MOTION;
        return Action.NONE;
    }

    private static boolean inRow(float y, float rowY) {
        return y >= rowY && y <= rowY + ROW_HEIGHT;
    }
}
