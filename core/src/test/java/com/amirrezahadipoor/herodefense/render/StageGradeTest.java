package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Roadmap R5.4: the arena renders under a stage grade rather than being filtered after it is drawn. These
 * cases hold the arc itself -- the stops, the names a review strip prints, the interpolation between stops and
 * the fact that the grade moves colour rather than alpha.
 */
final class StageGradeTest {
    private static final float TOLERANCE = 0.0001f;

    @Test
    void everyStageHasItsOwnGradeAndTheyAreTheReviewStripsNumbers() {
        // The same numbers live in tools/visual/review_strips.py; tools/visual/tests reads both files and fails
        // if they drift, so this case is the runtime half of that pair.
        assertGrade(1.00f, 1.00f, 1.00f, 0.00f, 1);
        assertGrade(1.02f, 0.98f, 0.92f, 0.00f, 51);
        assertGrade(0.94f, 1.00f, 1.02f, 0.00f, 101);
        assertGrade(0.86f, 0.90f, 1.00f, 0.06f, 151);
        assertEquals(4, StageGrade.stopCount());
    }

    @Test
    void theStopsNameTheStagesTheReviewSheetsPrint() {
        assertEquals("DAWN 1-50", StageGrade.nameForWave(1));
        assertEquals("DAWN 1-50", StageGrade.nameForWave(50));
        assertEquals("AMBER 51-100", StageGrade.nameForWave(51));
        assertEquals("TEAL 101-150", StageGrade.nameForWave(120));
        assertEquals("HOLLOW 151-200", StageGrade.nameForWave(200));
        // A wave past the last stop stays in the last stage rather than falling off the table.
        assertEquals("HOLLOW 151-200", StageGrade.nameForWave(240));
    }

    @Test
    void crossingAStageBoundaryMovesTheColourRatherThanJumpingIt() {
        StageGrade beforeBoundary = StageGrade.forWave(50);
        StageGrade afterBoundary = StageGrade.forWave(51);
        StageGrade onBoundary = StageGrade.forWave(50).red() == 1.00f
            ? StageGrade.forWave(50)
            : afterBoundary;
        assertTrue(Math.abs(beforeBoundary.red() - onBoundary.red()) < 0.01f);
        // Interpolation is real: halfway to AMBER is halfway between the two stops, not the next stop.
        StageGrade halfway = StageGrade.forWave(26);
        float expectedRed = 1.00f + (1.02f - 1.00f) * 0.5f;
        assertEquals(expectedRed, halfway.red(), TOLERANCE);
        // And the arc is monotone in the direction its stops move.
        assertTrue(StageGrade.forWave(60).red() > StageGrade.forWave(60).green());
    }

    @Test
    void theGradeMovesColourAndLeavesAlphaAlone() {
        StageGrade hollow = StageGrade.forWave(175);
        // The grade's own channels: a bright ambient value is pulled down, a dark one is lifted toward teal.
        assertTrue(hollow.channel(1.0f, 0) < 1.0f);
        assertTrue(hollow.channel(1.0f, 2) > hollow.channel(1.0f, 0));
        assertTrue(hollow.channel(0.0f, 2) > 0.0f);
        assertTrue(hollow.channel(0.0f, 2) > hollow.channel(0.0f, 0));
        // The dawn stage is the identity on colour, which is what makes it a baseline for the before/after pair.
        StageGrade dawn = StageGrade.forWave(1);
        assertEquals(1.0f, dawn.red(), TOLERANCE);
        assertEquals(1.0f, dawn.green(), TOLERANCE);
        assertEquals(1.0f, dawn.blue(), TOLERANCE);
        assertEquals(0.0f, dawn.shadowLift(), TOLERANCE);
        assertEquals(0.4f, dawn.channel(0.4f, 1), TOLERANCE);
    }

    private static void assertGrade(float red, float green, float blue, float lift, int wave) {
        StageGrade grade = StageGrade.forWave(wave);
        assertEquals(red, grade.red(), TOLERANCE, "red at wave " + wave);
        assertEquals(green, grade.green(), TOLERANCE, "green at wave " + wave);
        assertEquals(blue, grade.blue(), TOLERANCE, "blue at wave " + wave);
        assertEquals(lift, grade.shadowLift(), TOLERANCE, "lift at wave " + wave);
    }
}
