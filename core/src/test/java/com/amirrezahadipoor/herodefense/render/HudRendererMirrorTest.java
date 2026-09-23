package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amirrezahadipoor.herodefense.input.HudTouchLayout;
import org.junit.jupiter.api.Test;

/**
 * The arithmetic roadmap G4 added to the HUD (roadmap G4). The drawing itself is GL calls a unit test cannot
 * make; what it can hold is that every mirrored position the renderer computes is the position the touch layout
 * computes, and that a bar fill starts from the leading edge. Until 2026-09-23 these were held to both
 * directions; the owner deleted the right-to-left translation outright, so the one direction left is pinned.
 */
final class HudRendererMirrorTest {

    @Test
    void barFillsGrowFromTheLeadingEdge() {
        // The track sits at the design grid and the fill starts at its left inset.
        assertEquals(91f, UiMirror.leadingOnScreen(HudRenderer.HEALTH_BAR_X, HudRenderer.HEALTH_BAR_WIDTH));
        assertEquals(93f, HudRenderer.barFillX(91f, 580f, 2f, 100f));
        // A full bar fills the track.
        assertEquals(93f, HudRenderer.barFillX(91f, 580f, 2f, 576f));
    }

    @Test
    void theDropTargetFollowsTheInventoryButton() {
        assertEquals(270f, CombatEntityRenderer.dropTargetX(),
            "drops fly at the design-grid inventory button");
        assertEquals(HudTouchLayout.inventoryX() + HudTouchLayout.UTILITY_BUTTON_WIDTH * 0.5f,
            CombatEntityRenderer.dropTargetX(),
            "and that is the button's centre, not a second copy of its position");
    }
}
