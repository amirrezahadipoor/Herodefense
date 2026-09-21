package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The dawn ledger's layout: a notch per dawn, two rows, the count readout keeping its end clear. */
final class DawnLedgerTest {

    @Test
    void anEmptyLedgerCarvesNoNotches() {
        assertEquals(0, DawnLedger.notchesFor(0));
        assertEquals(0, DawnLedger.notchesFor(-3), "garbage cannot carve notches");
    }

    @Test
    void oneDawnCarvesOneNotch() {
        assertEquals(1, DawnLedger.notchesFor(1));
        assertTrue(DawnLedger.notchX(0) > DawnLedger.BAND_X, "the notch lives inside the band");
        assertTrue(DawnLedger.notchY(0) > DawnLedger.BAND_Y
                && DawnLedger.notchY(0) + DawnLedger.NOTCH_HEIGHT
                    < DawnLedger.BAND_Y + DawnLedger.BAND_HEIGHT,
            "and so does its bottom");
    }

    @Test
    void theRowsFillLeftToRightTopFirstAndThenWrap() {
        int capacity = DawnLedger.capacity();
        int rowCapacity = DawnLedger.rowCapacity();
        assertTrue(rowCapacity > 10, "the band fits a reasonable run of dawns per row");
        assertEquals(rowCapacity * 2, capacity);

        // Left to right within a row...
        for (int i = 1; i < rowCapacity; i++) {
            assertTrue(DawnLedger.notchX(i) > DawnLedger.notchX(i - 1), "notches march right");
        }
        // ...and the next row starts below, at the left again.
        assertTrue(DawnLedger.notchY(rowCapacity) < DawnLedger.notchY(0), "the wrap goes down");
        assertTrue(DawnLedger.notchX(rowCapacity) < DawnLedger.notchX(rowCapacity - 1),
            "...and back to the left");
    }

    @Test
    void theNotchesCapAtTheCapacityButTheCountNeverLies() {
        int capacity = DawnLedger.capacity();
        assertEquals(capacity, DawnLedger.notchesFor(capacity));
        assertEquals(capacity, DawnLedger.notchesFor(capacity * 10),
            "the band is full; the true count is the readout's job");
    }

    @Test
    void theNotchesKeepTheCountReadoutClear() {
        int capacity = DawnLedger.capacity();
        float lastNotchRight = DawnLedger.notchX(capacity - 1) + DawnLedger.NOTCH_WIDTH;
        float countReserveStart =
            DawnLedger.BAND_X + DawnLedger.BAND_WIDTH - 20f - DawnLedger.COUNT_RESERVE;
        assertTrue(lastNotchRight < countReserveStart,
            "no notch reaches under the count: " + lastNotchRight + " < " + countReserveStart);
    }
}
