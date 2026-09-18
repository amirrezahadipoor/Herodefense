package com.amirrezahadipoor.herodefense.input;

/** Portrait hit-target geometry for the Grove Codex list. */
public final class CodexTouchLayout {
    public static final float CLOSE_X = 570f;
    public static final float CLOSE_Y = 1110f;
    public static final float CLOSE_SIZE = 100f;

    public static final float LIST_X = 40f;
    public static final float LIST_WIDTH = 640f;
    public static final float LIST_TOP_Y = 1010f;
    /** Top of the trophy shelf, which starts under the tab strip instead of under the header. */
    public static final float TROPHY_LIST_TOP_Y = 978f;
    public static final float LIST_ROW_HEIGHT = 74f;
    public static final float LIST_ROW_STRIDE = 84f;
    public static final int VISIBLE_ROWS = 6;

    /** The two shelves: what the grove wrote, and what the Warden earned (roadmap R3.3). */
    public enum Tab {
        LORE,
        TROPHIES
    }

    public static final float TAB_Y = 1020f;
    public static final float TAB_HEIGHT = 74f;
    public static final float TAB_WIDTH = 312f;
    public static final float TAB_GAP = 16f;
    public static final float TAB_LEFT_X = LIST_X;
    public static final float TAB_RIGHT_X = LIST_X + TAB_WIDTH + TAB_GAP;

    private CodexTouchLayout() {
    }

    public static float rowBottom(int visibleRow) {
        return LIST_TOP_Y - LIST_ROW_HEIGHT - visibleRow * LIST_ROW_STRIDE;
    }

    /** Row geometry per shelf; the lore shelf keeps the geometry it shipped with. */
    public static float rowBottom(Tab shelf, int visibleRow) {
        if (shelf == Tab.TROPHIES) {
            return TROPHY_LIST_TOP_Y - LIST_ROW_HEIGHT - visibleRow * LIST_ROW_STRIDE;
        }
        return rowBottom(visibleRow);
    }

    /** Visible list row containing the point, or -1. */
    public static int visibleRowAt(float x, float y) {
        return visibleRowAt(Tab.LORE, x, y);
    }

    public static int visibleRowAt(Tab shelf, float x, float y) {
        if (x < LIST_X || x > LIST_X + LIST_WIDTH) return -1;
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            if (inside(x, y, LIST_X, rowBottom(shelf, row), LIST_WIDTH, LIST_ROW_HEIGHT)) return row;
        }
        return -1;
    }

    /** Which tab the point hits, or null when it misses the strip. */
    public static Tab tabAt(float x, float y) {
        if (!inside(x, y, TAB_LEFT_X, TAB_Y, TAB_WIDTH, TAB_HEIGHT)
            && !inside(x, y, TAB_RIGHT_X, TAB_Y, TAB_WIDTH, TAB_HEIGHT)) {
            return null;
        }
        return x < TAB_RIGHT_X ? Tab.LORE : Tab.TROPHIES;
    }

    public static boolean closeAt(float x, float y) {
        return inside(x, y, CLOSE_X, CLOSE_Y, CLOSE_SIZE, CLOSE_SIZE);
    }

    private static boolean inside(
        float x, float y, float left, float bottom, float width, float height
    ) {
        return x >= left && x <= left + width && y >= bottom && y <= bottom + height;
    }
}
