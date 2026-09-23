package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.input.StatShopTouchLayout;
import com.amirrezahadipoor.herodefense.input.StatShopTouchLayout.Tab;
import com.amirrezahadipoor.herodefense.model.HeroStat;
import com.amirrezahadipoor.herodefense.skills.SkillEvolution;
import com.amirrezahadipoor.herodefense.skills.SkillId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Locks explicit affordability, benefit, paused context, and touch-safe cards. */
final class PremiumShopPresentationTest {
    @Test
    void affordabilityLabelsNeverDependOnColorAlone() {
        assertEquals("MAXED", StatShopOverlayRenderer.affordabilityLabel(true, false, 0, 0));
        assertEquals("LEVEL 7 / 10", StatShopOverlayRenderer.levelLabel(7, 10));
        assertEquals("LEVEL 23  |  ENDLESS", StatShopOverlayRenderer.levelLabel(23, 20));
        assertEquals(1f, StatShopOverlayRenderer.tierProgress(31, 20));
        assertEquals(0.5f, StatShopOverlayRenderer.tierProgress(5, 10));
        assertEquals("AFFORDABLE",
            StatShopOverlayRenderer.affordabilityLabel(false, true, 55, 80));
        assertEquals("NEED $ 20",
            StatShopOverlayRenderer.affordabilityLabel(false, false, 100, 80));
    }

    @Test
    void everyStatExplainsItsPermanentBenefit() {
        // R7.2 changed what this asserts, and the change is the point. The old contract was that the line starts
        // with "+1 " and is at least ten characters long -- which every line passed while three of the five named
        // a unit the code does not have ("+1 attack-speed rating" for a stat that gives 0.03 attacks per second,
        // "+1 critical-chance rating" for a stat that gives 2% drop rate). A line that satisfies a length check
        // and describes the wrong number is exactly the opacity the 2026-09-13 review measured. The contract now
        // is that the row states the per-point gain as a number, and comes from the one catalog both stat screens
        // read (`StatTooltipsTest` checks the numbers themselves against `HeroStats`).
        for (HeroStat stat : HeroStat.values()) {
            String benefit = StatShopOverlayRenderer.statBenefit(stat);
            assertEquals(com.amirrezahadipoor.herodefense.tooltips.StatTooltips.shortLine(stat), benefit,
                stat.name() + ": the row line has one source, not one per screen");
            assertTrue(benefit.startsWith("+"), stat.name());
            assertTrue(benefit.chars().anyMatch(Character::isDigit),
                stat.name() + " has to state a number, not a rating: " + benefit);
            assertTrue(benefit.length() <= com.amirrezahadipoor.herodefense.tooltips.StatTooltips
                .SHORT_LINE_CHARACTER_BUDGET, stat.name() + ": " + benefit);
        }
    }

    @Test
    void evolutionForkLabelsNameAllThreeOptionsAndFitTheirSlots() {
        for (SkillId skill : SkillId.values()) {
            String left = StatShopOverlayRenderer.skillForkLeft(skill);
            String mid = StatShopOverlayRenderer.skillForkMid(skill);
            String right = StatShopOverlayRenderer.skillForkRight(skill);
            assertTrue(left.startsWith("LEFT: "), skill.name());
            assertTrue(mid.startsWith("MID: "), skill.name());
            assertTrue(right.startsWith("RIGHT: "), skill.name());
            assertTrue(left.contains(SkillEvolution.forSkill(skill).get(0).displayName()),
                skill.name());
            assertTrue(mid.contains(SkillEvolution.forSkill(skill).get(1).displayName()),
                skill.name());
            assertTrue(right.contains(SkillEvolution.forSkill(skill).get(2).displayName()),
                skill.name());
            assertTrue(left.length() <= 34, skill.name() + ": " + left);
            assertTrue(mid.length() <= 34, skill.name() + ": " + mid);
            assertTrue(right.length() <= 34, skill.name() + ": " + right);
        }
    }

    @Test
    void evolvedRowsNameTheChosenEvolution() {
        assertEquals("STORM CHAIN (+2 arcs+stun)",
            StatShopOverlayRenderer.evolvedBenefit(SkillEvolution.STORM_CHAIN));
        assertEquals("VAMPIRIC CHAIN (heal 30%)",
            StatShopOverlayRenderer.evolvedBenefit(SkillEvolution.VAMPIRIC_CHAIN));
    }

    @Test
    void purchaseCardsAndCloseRemainGenerousTouchTargets() {
        assertTrue(StatShopTouchLayout.ROW_WIDTH >= 610f);
        assertTrue(StatShopTouchLayout.ROW_HEIGHT >= 135f);
        assertTrue(StatShopTouchLayout.CLOSE_SIZE >= 100f);
    }

    /**
     * The header on the gate emulator's own capture: the tab's description ran across the top of the Roots
     * button and the two context lines sat inside it, over its icon and label. The header now holds the
     * title, the coin count and the state word on the title's line against the trailing edge; the description
     * and the Close hint went into the help panel. This measures that arrangement with the committed face at
     * the emulator's sizes, so a longer word cannot quietly bring the collision back.
     */
    @Test
    void headerWordsShareTheirLineAndTheHelpPanelLinesFitTheirBox() {
        float titleEnd = 40f + ReferenceTypeMeasure.width("WORLD TREE ARMORY",
            GameFonts.Role.forLegacyScale(1.36f));
        float pausedStart = 720f - StatShopOverlayRenderer.HEADER_TRAILING_INSET
            - ReferenceTypeMeasure.width("COMBAT PAUSED", GameFonts.Role.forLegacyScale(0.68f));
        assertTrue(titleEnd + 24f <= pausedStart,
            "the title ends at " + titleEnd + " and the state word starts at " + pausedStart);
        // The word sits over the Roots and Close buttons' columns, so it has to end above both of them.
        float wordBottom = StatShopOverlayRenderer.HEADER_STATE_Y
            - ReferenceTypeMeasure.capHeight(GameFonts.Role.forLegacyScale(0.68f));
        assertTrue(wordBottom >= StatShopTouchLayout.ROOT_Y + StatShopTouchLayout.ROOT_HEIGHT + 2f,
            "the state word reaches down to " + wordBottom + ", the Roots button's top is "
                + (StatShopTouchLayout.ROOT_Y + StatShopTouchLayout.ROOT_HEIGHT));
        assertTrue(wordBottom >= StatShopTouchLayout.CLOSE_Y + StatShopTouchLayout.CLOSE_SIZE + 2f,
            "the state word reaches down to " + wordBottom + ", the Close button's top is "
                + (StatShopTouchLayout.CLOSE_Y + StatShopTouchLayout.CLOSE_SIZE));
        float inner = StatShopOverlayRenderer.HELP_PANEL_WIDTH - 2f * 28f;
        for (Tab tab : Tab.values()) {
            String description = StatShopOverlayRenderer.tabDescription(tab);
            float width = ReferenceTypeMeasure.width(description,
                GameFonts.Role.forLegacyScale(0.66f));
            assertTrue(width <= inner, tab + " description is " + width + " wide in a " + inner + " panel: "
                + description);
        }
        for (boolean returnsToPause : new boolean[] {true, false}) {
            String hint = StatShopOverlayRenderer.closeHint(returnsToPause);
            float width = ReferenceTypeMeasure.width(hint, GameFonts.Role.forLegacyScale(0.62f));
            assertTrue(width <= inner, hint + " is " + width + " wide in a " + inner + " panel");
        }
    }

    @Test
    void rendererBindsGeneratedStatesAndExplicitContext() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/render/StatShopOverlayRenderer.java"
        ));
        for (String required : new String[] {
            "WORLD TREE ARMORY", "COMBAT PAUSED", "Close returns to Pause",
            "Close returns to battle", "AFFORDABLE", "NEED $ ", "MAXED",
            "LEVEL ", "feedbackMessage()", "UiFrameRenderer.Kind.BUTTON",
            "UiFrameRenderer.Kind.PANEL", "MainMenuRenderer.pressedOffset"
        }) {
            assertTrue(source.contains(required), required);
        }
    }
}
