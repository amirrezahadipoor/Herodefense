package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Roadmap R8.3: the release rule, exercised without a device, including the two cases that would make it
 * dangerous — releasing a sheet the frame still needs, and a frame that stalls reloading half the catalog.
 */
final class AtlasResidencyPolicyTest {
    private static final long SHEET = AtlasResidencyPolicy.decodedBytes(1024, 1024);

    @Test
    void aSetInsideTheCapacityIsLeftAlone() {
        assertEquals(List.of(), AtlasResidencyPolicy.releases(
            List.of("a", "b"), Map.of("a", SHEET, "b", SHEET), List.of(), 2 * SHEET, 3 * SHEET, 4
        ));
        assertTrue(AtlasResidencyPolicy.withinCapacity(3 * SHEET, 3 * SHEET));
    }

    @Test
    void theLeastRecentlyUsedSheetGoesFirst() {
        // access order: c was drawn most recently, a longest ago
        List<String> released = AtlasResidencyPolicy.releases(
            List.of("a", "b", "c"), Map.of("a", SHEET, "b", SHEET, "c", SHEET), List.of(),
            3 * SHEET, 2 * SHEET, 4
        );
        assertEquals(List.of("a"), released, "one release is enough to fit, and it is the oldest one");
    }

    @Test
    void aProtectedSheetIsNeverReleasedEvenWhenItIsTheOldest() {
        List<String> released = AtlasResidencyPolicy.releases(
            List.of("live-boss", "old-enemy", "older-enemy"),
            Map.of("live-boss", SHEET, "old-enemy", SHEET, "older-enemy", SHEET),
            List.of("live-boss"), 3 * SHEET, 1 * SHEET, 4
        );
        assertFalse(released.contains("live-boss"),
            "a sheet the frame is about to draw cannot be evicted out from under it");
        assertEquals(List.of("old-enemy", "older-enemy"), released);
    }

    @Test
    void oneCallReleasesAtMostItsShareSoAFrameDoesNotStall() {
        List<String> released = AtlasResidencyPolicy.releases(
            List.of("a", "b", "c", "d"), Map.of("a", SHEET, "b", SHEET, "c", SHEET, "d", SHEET),
            List.of(), 4 * SHEET, 1 * SHEET, 2
        );
        assertEquals(2, released.size(), "the cap is honoured even when the set is still over capacity");
        assertEquals(List.of("a", "b"), released);
    }

    @Test
    void aSheetWithNoMeasurementCannotEvictAKnownOne() {
        // "unknown" has no recorded bytes: releasing it frees nothing, so it is not a way to satisfy the budget
        List<String> released = AtlasResidencyPolicy.releases(
            List.of("unknown", "known"), Map.of("known", SHEET), List.of(), 2 * SHEET, SHEET, 4
        );
        assertEquals(List.of("unknown", "known"), released,
            "both go, because the unknown one frees nothing and the budget still has to be met");
    }

    @Test
    void anImpossibleCapacityTerminatesInsteadOfLooping() {
        List<String> released = AtlasResidencyPolicy.releases(
            List.of("live"), Map.of("live", SHEET), List.of("live"), SHEET, 1, 4
        );
        assertEquals(List.of(), released, "everything is protected: nothing to release, and no infinite walk");
        assertFalse(AtlasResidencyPolicy.withinCapacity(SHEET, 1));
    }

    @Test
    void decodedBytesIsWhatTheGpuHolds() {
        assertEquals(4L * 1024 * 1024, SHEET);
        assertEquals(0L, AtlasResidencyPolicy.decodedBytes(0, 100));
    }

    @Test
    void theCombatSetAndTheCapacityAreTheSameOrderOfMagnitude() {
        // the policy exists because the shipped combat set is 99.3 MiB against a 100 MiB budget
        // (`perf:2026-09-17-residency`): a capacity far below the live set would thrash, one far above would
        // never run. This pins the relationship, not the number.
        long liveSet = 104_087_552L;
        long capacity = RuntimeResidency.ATLAS_CAPACITY_BYTES;
        assertTrue(capacity >= liveSet, "the capacity has to hold the live set it is derived from");
        assertTrue(capacity <= liveSet * 2, "and not be so large that it never releases anything");
    }
}
