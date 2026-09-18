package com.amirrezahadipoor.herodefense.input;

/** Portrait hit-target geometry for the Grove Codex list. */
public final class CodexTouchLayout {
    public static final float CLOSE_X = 570f;
    public static final float CLOSE_Y = 1110f;
    public static final float CLOSE_SIZE = 100f;

    public static final float LIST_X = 40f;
    public static final float LIST_WIDTH = 640f;
    public static final float LIST_TOP_Y = 990f;
    /** Top of the trophy shelf. Both shelves start under the tab strip with the same geometry (roadmap G5). */
    public static final float TROPHY_LIST_TOP_Y = 990f;
    /**
     * Rows are 96 tall on a 108 stride: the codebase's own generous-target floor, the one HudTouchLayoutTest
     * has always held the HUD to, and twelve units of gap a thumb can land between two rows without choosing
     * the wrong one (roadmap G5). Five such rows fit the band between the tabs and the details panel; six of
     * the old 74s did not leave room for either the height or the gap.
     */
    public static final float LIST_ROW_HEIGHT = 96f;
    public static final float LIST_ROW_STRIDE = 108f;
    public static final int VISIBLE_ROWS = 5;

    /** The two shelves: what the grove wrote, and what the Warden earned (roadmap R3.3). */
    public enum Tab {
        LORE,
        TROPHIES
    }

    public static final float TAB_Y = 998f;
    public static final float TAB_HEIGHT = 96f;
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
