package com.amirrezahadipoor.herodefense.render;

/**
 * The dawn ledger: the notches the Root Network hub keeps at its foot, one per dawn the player has
 * brought back (one per completed ascension, {@code GameState.totalAscensionsCompleted}).
 *
 * <p>Every other record in this game is a number in a corner; the ledger is the record carved
 * where the player can look at it. The tree remembers, and this is what remembering looks like.
 * The layout is pure so the wrapping and the capacity are testable; the paint lives in
 * {@link RootNetworkOverlayRenderer}.
 */
public final class DawnLedger {

    /** The dark band the notches are carved in, at the foot of the hub. */
    public static final float BAND_X = 30f;
    public static final float BAND_Y = 64f;
    public static final float BAND_WIDTH = 660f;
    public static final float BAND_HEIGHT = 60f;

    /** A notch is a carved slot: narrow and quiet. */
    public static final float NOTCH_WIDTH = 6f;
    public static final float NOTCH_HEIGHT = 20f;
    public static final float NOTCH_SPACING = 24f;
    /** The count readout keeps the right end of the band clear. */
    public static final float COUNT_RESERVE = 96f;

    private DawnLedger() {
    }

    /** How many notches fit in one row. */
    public static int rowCapacity() {
        float usable = BAND_WIDTH - 2 * 20f - COUNT_RESERVE;
        return (int) (usable / NOTCH_SPACING);
    }

    /** The ledger holds two rows. */
    public static int capacity() {
        return rowCapacity() * 2;
    }

    /** The number of notches actually carved for a given dawn count. */
    public static int notchesFor(int dawns) {
        return Math.max(0, Math.min(dawns, capacity()));
    }

    /** The x of the i-th notch (0-based), filling left to right, top row first. */
    public static float notchX(int index) {
        int column = index % rowCapacity();
        return BAND_X + 20f + column * NOTCH_SPACING;
    }

    /** The notch's bottom y: the top row holds the earliest dawns, the lower row the newer ones. */
    public static float notchY(int index) {
        int row = index / rowCapacity();
        return row == 0 ? BAND_Y + BAND_HEIGHT - 12f - NOTCH_HEIGHT : BAND_Y + 12f;
    }
}
