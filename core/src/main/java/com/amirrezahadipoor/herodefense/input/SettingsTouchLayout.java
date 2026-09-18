package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.render.UiMirror;

/**
 * Tap and drag-scroll settings bounds shared with the renderer (roadmap R6.4 added the two level rows, R7.3 the language
 * row, roadmap G3a added the sixth, and roadmap G3-layout turns the viewport into a scrolling list so additional
 * accessibility options fit without screen collisions).
 *
 * <p>Six rows fit comfortably between the footer panel at 260f and the header band at 1096f. With G3-layout,
 * additional options scroll into this viewport via touch drag, with {@link #VISIBLE_ROWS} visible at any time.
 * At scroll offset 0, the first six rows sit exactly at the historical coordinates, preserving visual stability.
 *
 * <p>R7.3 also mirrors the screen, and a mirrored drawing with an unmirrored tap target is a button the player
 * can see in one place and press in another. So the one element on this screen that is not symmetric -- the close
 * button in the top corner -- has its x come from {@link #closeX()}, the same {@link UiMirror} call the renderer
 * makes for the icon it draws. The rows need no such call: they span 100f..620f of a 720f screen, so their
 * margins are equal and mirroring leaves them exactly where they were.
 */
public final class SettingsTouchLayout {
    public enum Action {
        NONE, TOGGLE_SOUND, TOGGLE_MUSIC, CYCLE_SOUND_LEVEL, CYCLE_MUSIC_LEVEL, CYCLE_LANGUAGE,
        TOGGLE_REDUCED_MOTION, TOGGLE_COLOUR_BLIND_RARITY, CLOSE
    }

    public static final float ROW_X = 100f;
    public static final float ROW_WIDTH = 520f;
    public static final float ROW_HEIGHT = 130f;

    public static final float SOUND_ROW_Y = 960f;
    public static final float MUSIC_ROW_Y = 820f;
    public static final float SOUND_LEVEL_ROW_Y = 680f;
    public static final float MUSIC_LEVEL_ROW_Y = 540f;
    public static final float LANGUAGE_ROW_Y = 400f;
    /** The bottom visible slot that fits above the footer note panel. */
    public static final float REDUCED_MOTION_ROW_Y = 270f;
    public static final float CLOSE_X = 570f;
    public static final float CLOSE_Y = 1120f;
    public static final float CLOSE_SIZE = 100f;
    /** How far the close box's trailing edge sits from the screen's trailing edge: 720f - 570f - 100f. */
    static final float CLOSE_INSET = 50f;

    public static final int VISIBLE_ROWS = 6;
    public static final int TOTAL_ROWS = 7;

    private SettingsTouchLayout() {
    }

    /** The close box's left edge in the language in force; {@link #CLOSE_X} is the left-to-right one. */
    public static float closeX() {
        return UiMirror.trailingOnScreen(CLOSE_INSET, CLOSE_SIZE);
    }

    /** Returns the Y coordinate of a visible slot (0 is top at 960f, 5 is bottom at 270f). */
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

    /** Which visible slot contains the vertical coordinate {@code y}, or -1 if outside. */
    public static int visibleSlotAt(float y) {
        for (int slot = 0; slot < VISIBLE_ROWS; slot++) {
            float bottom = slotY(slot);
            if (y >= bottom && y <= bottom + ROW_HEIGHT) {
                return slot;
            }
        }
        return -1;
    }

    /** Returns the Action mapped to logical row index {@code rowIndex}. */
    public static Action actionForRow(int rowIndex) {
        return switch (rowIndex) {
            case 0 -> Action.TOGGLE_SOUND;
            case 1 -> Action.TOGGLE_MUSIC;
            case 2 -> Action.CYCLE_SOUND_LEVEL;
            case 3 -> Action.CYCLE_MUSIC_LEVEL;
            case 4 -> Action.CYCLE_LANGUAGE;
            case 5 -> Action.TOGGLE_REDUCED_MOTION;
            case 6 -> Action.TOGGLE_COLOUR_BLIND_RARITY;
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
