package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The mirroring arithmetic (roadmap R7.3).
 *
 * <p>Until 2026-09-23 these tests held both directions, because the game shipped a right-to-left language. The
 * owner deleted that translation outright, so the screen never mirrors now -- but the arithmetic stays, and what
 * these pin is the direction the game draws: leading is the left edge, trailing is the right, and the helpers
 * agree with the hit tests that read the same methods.
 */
final class UiMirrorTest {

    @Test
    void theScreenDoesNotMirror() {
        assertFalse(UiMirror.mirrored());
    }

    @Test
    void leadingIsTheLeftEdgeAndTrailingIsTheRight() {
        assertEquals(136f, UiMirror.leading(100f, 520f, 36f, 200f), "36f in from the left");
        assertEquals(520f, UiMirror.trailing(100f, 520f, 30f, 70f), "30f in from the right");
    }

    @Test
    void anElementStaysItsInsetFromItsEdge() {
        float inset = 36f;
        float width = 200f;
        assertEquals(inset, UiMirror.leading(100f, 520f, inset, width) - 100f, "the gap is on the left");
        assertEquals(inset, 100f + 520f - (UiMirror.trailing(100f, 520f, inset, width) + width),
            "and the trailing gap is on the right");
    }

    @Test
    void aLeadingBoxAndATrailingBoxDoNotOverlap() {
        float rowWidth = 520f;
        float labelWidth = 180f;
        float valueWidth = 90f;
        float label = UiMirror.leading(100f, rowWidth, 36f, labelWidth);
        float value = UiMirror.trailing(100f, rowWidth, 30f, valueWidth);
        assertTrue(label + labelWidth < value, "the label ends before the value starts");
    }

    @Test
    void theScreenWideHelpersUseTheLogicalWidthTheOverlaysAreLaidOutIn() {
        assertEquals(720f, UiMirror.SCREEN_WIDTH);
        assertEquals(60f, UiMirror.leadingOnScreen(60f, 76f));
        assertEquals(588f, UiMirror.trailingOnScreen(68f, 64f));
    }

    @Test
    void aCentredElementStaysWhereItIsPut() {
        assertEquals(611f, UiMirror.centre(500f, 176f, 611f));
        assertEquals(360f, UiMirror.centre(0f, UiMirror.SCREEN_WIDTH, 360f));
    }

    @Test
    void aBoxWiderThanItsContainerStillSitsOnItsInset() {
        assertEquals(10f, UiMirror.leading(0f, 100f, 10f, 120f),
            "an oversized box hangs off the leading edge by the same rule");
    }
}
