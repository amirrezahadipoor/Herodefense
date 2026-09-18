package com.amirrezahadipoor.herodefense.tooltips;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.HeroStat;
import com.amirrezahadipoor.herodefense.model.HeroStats;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Roadmap R7.2: every stat the game displays has a tooltip, every tooltip is a sentence rather than a slogan,
 * and the numbers inside the sentences are the game's own constants rather than copies of them.
 */
final class StatTooltipsTest {
    /**
     * The coverage mechanism. A new talent fails this test twice over — once because the table has no row for
     * it, and once because {@link StatTooltips#shortLine} would not compile without a branch — and the keyword
     * is what the tooltip has to mention for a player to learn the mechanic rather than the name of the number.
     */
    private static final Map<HeroStat, String> REQUIRED_MECHANIC = Map.of(
        HeroStat.STRENGTH, "damage",
        HeroStat.AGILITY, "speed",
        HeroStat.LUCK, "drop",
        HeroStat.DODGE, "chance",
        HeroStat.HEALTH, "HP"
    );

    @Test
    void everyTalentHasATooltipAndTheCoverageTableKnowsAllOfThem() {
        assertEquals(EnumSet.allOf(HeroStat.class), REQUIRED_MECHANIC.keySet(),
            "a talent without a row here is a talent nobody is taught: add it and write its tooltip");
        for (HeroStat stat : HeroStat.values()) {
            String tooltip = StatTooltips.tooltip(stat);
            assertFalse(tooltip.isBlank(), stat + " has no tooltip");
            assertTrue(tooltip.toLowerCase(java.util.Locale.ROOT)
                    .contains(REQUIRED_MECHANIC.get(stat).toLowerCase(java.util.Locale.ROOT)),
                stat + "'s tooltip has to name the mechanic it moves; got: " + tooltip);
            assertFalse(StatTooltips.shortLine(stat).isBlank(), stat + " has no row line");
        }
    }

    @Test
    void everyTooltipFitsTheLineItIsDrawnOn() {
        for (HeroStat stat : HeroStat.values()) {
            assertTrue(StatTooltips.tooltip(stat).length() <= StatTooltips.TOOLTIP_CHARACTER_BUDGET,
                stat + "'s tooltip is " + StatTooltips.tooltip(stat).length() + " characters, past the "
                    + StatTooltips.TOOLTIP_CHARACTER_BUDGET + "-character help line");
            assertTrue(StatTooltips.shortLine(stat).length() <= StatTooltips.SHORT_LINE_CHARACTER_BUDGET,
                stat + "'s row line does not fit its button: " + StatTooltips.shortLine(stat));
        }
        for (StatTooltips.Readout readout : StatTooltips.Readout.values()) {
            assertTrue(StatTooltips.explain(readout).length() <= StatTooltips.TOOLTIP_CHARACTER_BUDGET,
                readout + "'s tooltip is past the help line's budget");
        }
    }

    @Test
    void theNumbersInTheTextAreTheGamesOwnConstants() {
        assertTrue(StatTooltips.shortLine(HeroStat.STRENGTH).contains("2"),
            "strength is " + HeroStats.DAMAGE_PER_STRENGTH + " damage per point, and the row says so");
        assertTrue(StatTooltips.tooltip(HeroStat.DODGE)
                .contains(String.valueOf(HeroStats.MAX_DODGE_PERCENT)),
            "the dodge tooltip has to state the cap the code enforces (" + HeroStats.MAX_DODGE_PERCENT + "%)");
        assertTrue(StatTooltips.tooltip(HeroStat.LUCK)
                .contains(String.valueOf(HeroStats.DROP_MULTIPLIER_PERCENT)),
            "and the luck tooltip the multiplier the code applies (" + HeroStats.DROP_MULTIPLIER_PERCENT + "%)");
        assertTrue(StatTooltips.tooltip(HeroStat.AGILITY).contains("0.03"),
            "attack speed per point is a constant, so the text quotes it");
        assertTrue(StatTooltips.tooltip(HeroStat.HEALTH)
                .contains(String.valueOf(HeroStats.MAX_HEALTH_PER_POINT_ROUNDED)),
            "so does the health tooltip");
    }

    @Test
    void everyReadoutTheGamePutsOnScreenIsExplainedWithANumberOrAConsequence() {
        Set<String> labels = new java.util.HashSet<>();
        for (StatTooltips.Readout readout : StatTooltips.Readout.values()) {
            String explanation = StatTooltips.explain(readout);
            assertFalse(explanation.isBlank(), readout + " has no explanation");
            assertFalse(StatTooltips.label(readout).isBlank(), readout + " has no label");
            assertTrue(labels.add(StatTooltips.label(readout)), "two readouts share the label "
                + StatTooltips.label(readout));
            assertTrue(explanation.length() > 40,
                readout + "'s explanation is a slogan, not an explanation: " + explanation);
            assertFalse(explanation.endsWith(" "), "trailing space in " + readout + "'s explanation");
        }
    }

    @Test
    void nullsAreSurvivableRatherThanExceptions() {
        assertEquals("", StatTooltips.shortLine(null));
        assertEquals("", StatTooltips.tooltip(null));
        assertEquals("", StatTooltips.label(null));
        assertEquals("", StatTooltips.explain(null));
    }

    @Test
    void theHelpPanelNeverNeedsMoreLinesThanItHas() {
        for (HeroStat stat : HeroStat.values()) {
            java.util.List<String> lines = StatTooltips.noteLines(StatTooltips.tooltip(stat));
            assertTrue(lines.size() <= StatTooltips.NOTE_LINE_COUNT,
                stat + "'s tooltip wraps to " + lines.size() + " lines and the panel has "
                    + StatTooltips.NOTE_LINE_COUNT);
            for (String line : lines) {
                assertTrue(line.length() <= StatTooltips.NOTE_LINE_CHARACTER_BUDGET,
                    "line too wide for the panel: " + line);
            }
            assertEquals(StatTooltips.tooltip(stat).replaceAll("\\s+", " "), String.join(" ", lines),
                "wrapping may not lose or reorder a word of " + stat);
        }
    }

    @Test
    void wrappingHandlesTheEdgesWithoutInventingText() {
        assertEquals(java.util.List.of(), StatTooltips.noteLines(null));
        assertEquals(java.util.List.of(), StatTooltips.noteLines("   "));
        assertEquals(java.util.List.of("one"), StatTooltips.noteLines("one"));
        String longWord = "x".repeat(StatTooltips.NOTE_LINE_CHARACTER_BUDGET + 5);
        assertEquals(java.util.List.of(longWord), StatTooltips.noteLines(longWord),
            "a single word longer than the panel is left alone rather than cut in half");
        java.util.List<String> two = StatTooltips.noteLines("alpha ".repeat(20).trim());
        assertEquals(2, two.size());
        assertTrue(two.get(0).endsWith("alpha"), "lines break between words, not inside them");
    }
}
