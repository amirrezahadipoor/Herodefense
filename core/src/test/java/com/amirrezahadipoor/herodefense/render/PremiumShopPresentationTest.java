package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.input.StatShopTouchLayout;
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
