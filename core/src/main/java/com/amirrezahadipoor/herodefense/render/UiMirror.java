package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;

/**
 * Where an element goes when the screen mirrors (roadmap R7.3).
 *
 * <p>Shaping and reordering make a Persian sentence read correctly inside the box it is drawn in. They do not
 * move the box: a row whose title sits 36 units in from the left edge still sits 36 units in from the left edge
 * when the title is Persian, which puts the first word a player reads on the wrong side of the screen and the
 * value it belongs to on the wrong side of it. Android solves this with a layout direction and mirrored
 * constraints; this game lays its overlays out in absolute numbers, so the mirroring has to be a function those
 * numbers go through.
 *
 * <p>It is one function rather than a per-screen decision, because the alternative is a screen that mirrors its
 * labels and not its icons, or one that mirrors what it draws and not what it hit-tests -- and that second one
 * is a button the player can see in one place and press in another. {@code input/SettingsTouchLayout} calls this
 * for the close button's box for exactly that reason: the drawing and the tap target are the same box, decided
 * once.
 *
 * <p>The vocabulary is leading and trailing rather than left and right, because that is the only pair of words
 * that means the same thing in both languages: leading is where reading starts, which is the left edge in
 * English and the right edge in Persian. A call site says which of the two edges an element belongs to and how
 * far in from it, and this class turns that into an x.
 *
 * <p>Everything here is arithmetic on four floats with no state and no allocation, so it is called per element
 * per frame without measurement; {@code UiMirrorTest} holds it to the two directions.
 */
public final class UiMirror {

    /**
     * The logical width every overlay is laid out in. The projection maps it onto the device, so a screen-wide
     * inset is a distance from this edge and not from the physical one.
     */
    public static final float SCREEN_WIDTH = 720f;

    private UiMirror() {
    }

    /** Whether the screen mirrors, which is whether the current language reads right to left. */
    public static boolean mirrored() {
        return GameLocale.rightToLeft();
    }

    /**
     * The left edge of a box {@code width} wide that sits {@code inset} from the container's leading edge.
     *
     * <p>The width is an argument rather than something this class measures, because the caller already has it:
     * an icon's size is a constant, and a run of text is measured by {@link OverlayText#width(String, float)}
     * shaped, which is the only width that is true of what gets drawn.
     */
    public static float leading(float containerX, float containerWidth, float inset, float width) {
        return GameLocale.rightToLeft()
            ? containerX + containerWidth - inset - width
            : containerX + inset;
    }

    /** The left edge of a box {@code width} wide that sits {@code inset} from the container's trailing edge. */
    public static float trailing(float containerX, float containerWidth, float inset, float width) {
        return GameLocale.rightToLeft()
            ? containerX + inset
            : containerX + containerWidth - inset - width;
    }

    /** {@link #leading(float, float, float, float)} with the screen as the container. */
    public static float leadingOnScreen(float inset, float width) {
        return leading(0f, SCREEN_WIDTH, inset, width);
    }

    /** {@link #trailing(float, float, float, float)} with the screen as the container. */
    public static float trailingOnScreen(float inset, float width) {
        return trailing(0f, SCREEN_WIDTH, inset, width);
    }
}
