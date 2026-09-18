package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class CodexTouchLayoutTest {
    @Test
    void fiveVisibleRowsCoverTheListBandWithoutOverlap() {
        // G5 geometry: rows are 96 tall on a 108 stride from 990, so row r spans 990-96-108r .. 990-108r.
        assertEquals(5, CodexTouchLayout.VISIBLE_ROWS);
        assertEquals(0, CodexTouchLayout.visibleRowAt(360f, 942f));
        assertEquals(4, CodexTouchLayout.visibleRowAt(360f, 510f));
        assertEquals(2, CodexTouchLayout.visibleRowAt(360f, 726f));
        assertEquals(-1, CodexTouchLayout.visibleRowAt(10f, 942f));
        assertEquals(-1, CodexTouchLayout.visibleRowAt(710f, 942f));
        assertEquals(-1, CodexTouchLayout.visibleRowAt(360f, 440f), "the details panel is not a row");
    }

    @Test
    void gapsBetweenRowsBelongToNoRow() {
        float rowZeroBottom = CodexTouchLayout.rowBottom(0);
        float rowOneTop = CodexTouchLayout.rowBottom(1) + CodexTouchLayout.LIST_ROW_HEIGHT;
        float gapMiddle = (rowZeroBottom + rowOneTop) * 0.5f;
        assertTrue(gapMiddle < rowZeroBottom);
        assertTrue(gapMiddle > rowOneTop);
        assertEquals(-1, CodexTouchLayout.visibleRowAt(360f, gapMiddle));
    }

    @Test
    void closeTargetSitsTopRight() {
        assertTrue(CodexTouchLayout.closeAt(620f, 1160f));
        assertFalse(CodexTouchLayout.closeAt(360f, 1160f));
        assertFalse(CodexTouchLayout.closeAt(620f, 1000f));
    }
}
