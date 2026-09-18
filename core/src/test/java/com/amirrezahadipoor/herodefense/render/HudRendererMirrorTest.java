package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.input.HudTouchLayout;
import org.junit.jupiter.api.Test;

/**
 * The arithmetic roadmap G4 added to the HUD, held to both directions (roadmap G4). The drawing itself is
 * GL calls a unit test cannot make; what it can hold is that every mirrored position the renderer computes
 * is the position the touch layout computes, and that a bar fill starts from the leading edge either way.
 */
final class HudRendererMirrorTest {

    @Test
    void barFillsGrowFromTheLeadingEdgeInBothLanguages() {
        // English: the track sits at the design grid and the fill starts at its left inset.
        assertEquals(91f, UiMirror.leadingOnScreen(HudRenderer.HEALTH_BAR_X, HudRenderer.HEALTH_BAR_WIDTH));
        assertEquals(93f, HudRenderer.barFillX(91f, 580f, 2f, 100f));
        GameLocale.use(GameLanguage.PERSIAN);
        try {
            // Persian: the track mirrors to 49..629 and the same fill hugs its right, leading edge,
            // ending two units short of it exactly as the English fill ends two short of 671.
            float barX = UiMirror.leadingOnScreen(HudRenderer.HEALTH_BAR_X, HudRenderer.HEALTH_BAR_WIDTH);
            assertEquals(49f, barX);
            assertEquals(527f, HudRenderer.barFillX(barX, 580f, 2f, 100f));
            assertEquals(627f, HudRenderer.barFillX(barX, 580f, 2f, 100f) + 100f);
            // A full bar fills the track in both languages.
            assertEquals(barX + 2f, HudRenderer.barFillX(barX, 580f, 2f, 576f));
        } finally {
            GameLocale.use(GameLanguage.ENGLISH);
        }
        assertEquals(93f, HudRenderer.barFillX(91f, 580f, 2f, 100f), "and the locale switch left no residue");
    }

    @Test
    void theDropTargetFollowsTheMirroredInventoryButton() {
        assertEquals(270f, CombatEntityRenderer.dropTargetX(),
            "English drops fly at the design-grid inventory button");
        GameLocale.use(GameLanguage.PERSIAN);
        try {
            assertEquals(HudTouchLayout.inventoryX() + HudTouchLayout.UTILITY_BUTTON_WIDTH * 0.5f,
                CombatEntityRenderer.dropTargetX());
            assertEquals(450f, CombatEntityRenderer.dropTargetX(),
                "Persian drops fly at the mirrored button, not at where it is drawn in English");
        } finally {
            GameLocale.use(GameLanguage.ENGLISH);
        }
    }
}
