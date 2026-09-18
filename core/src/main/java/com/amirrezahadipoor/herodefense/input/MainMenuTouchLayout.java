package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.model.GameMode;

/**
 * Portrait main-menu bounds for tap-only actions including Root Network and the two run lengths.
 *
 * <p>The stack carries six rows now (roadmap R3.5 added the brief vigil), so the buttons are 96 px tall with a
 * 22 px gap. Every row is 96 px — well past the 44 px minimum for a finger — and the rows are laid out from a
 * table, so a drawn button and a tappable button cannot drift apart: {@link #rowBottom} is the single source for
 * both the renderer and {@link #actionAt}.
 */
public final class MainMenuTouchLayout {
    public enum Action { NONE, NEW_GAME, BRIEF_RUN, CONTINUE, SETTINGS, ROOT_NETWORK, CODEX }

    public static final float BUTTON_X = 120f;
    public static final float BUTTON_WIDTH = 480f;
    public static final float BUTTON_HEIGHT = 96f;
    public static final float BUTTON_STRIDE = 118f;
    public static final float BUTTON_TOP_BOTTOM = 812f;

    private MainMenuTouchLayout() {
    }

    /** Bottom edge of a menu row; row 0 is the top row. */
    public static float rowBottom(int row) {
        return BUTTON_TOP_BOTTOM - row * BUTTON_STRIDE;
    }

    public static Action actionAt(float x, float y, boolean continueAvailable) {
        if (x < BUTTON_X || x > BUTTON_X + BUTTON_WIDTH) return Action.NONE;
        if (inRow(y, 0)) return Action.NEW_GAME;
        if (inRow(y, 1)) return Action.BRIEF_RUN;
        if (inRow(y, 2)) return continueAvailable ? Action.CONTINUE : Action.NONE;
        if (inRow(y, 3)) return Action.ROOT_NETWORK;
        if (inRow(y, 4)) return Action.CODEX;
        if (inRow(y, 5)) return Action.SETTINGS;
        return Action.NONE;
    }

    /** Row index for a point, or -1: exposed so tests can assert rows never overlap. */
    public static int rowAt(float y) {
        for (int row = 0; row < 6; row++) {
            if (inRow(y, row)) return row;
        }
        return -1;
    }

    private static boolean inRow(float y, int row) {
        float bottom = rowBottom(row);
        return y >= bottom && y <= bottom + BUTTON_HEIGHT;
    }
}
