package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * E4: The interface is text-heavy for a phone at arm's length.
 * Guards that UI chrome labels are short and icon-first.
 */
final class TextDensityTest {

    @Test
    void uiDensityConstantsAreReasonable() {
        assertTrue(UiDensity.HUD_MAX_CHARS <= 20, "HUD max must be <=20 for arm's length");
        assertTrue(UiDensity.OVERLAY_MAX_CHARS <= 30, "Overlay max must be <=30");
        assertTrue(UiDensity.DETAIL_MAX_CHARS <= 40);
    }

    @Test
    void abbrevStatIsShort() {
        assertTrue(UiDensity.abbrevStat("max_health").length() <= 10);
        assertTrue(UiDensity.abbrevStat("attack_speed").length() <= 10);
        assertTrue("ATK".equals(UiDensity.abbrevStat("damage")));
        assertTrue("HP".equals(UiDensity.abbrevStat("max_health")));
    }

    @Test
    void truncateRespectsMax() {
        String longName = "This is a very long item name that should be truncated";
        String truncated = UiDensity.truncate(longName, UiDensity.OVERLAY_MAX_CHARS);
        assertTrue(truncated.length() <= UiDensity.OVERLAY_MAX_CHARS);
        assertFalse(UiDensity.isTooLong(truncated, UiDensity.OVERLAY_MAX_CHARS));
        assertTrue(UiDensity.isTooLong(longName, UiDensity.OVERLAY_MAX_CHARS));
    }

    @Test
    void inventoryRendererUsesDensityGuard() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/render/InventoryOverlayRenderer.java"
        ));
        assertTrue(source.contains("UiDensity"), "Inventory must use UiDensity for E4");
        assertTrue(source.contains("truncate") || source.contains("OVERLAY_MAX_CHARS"),
            "Inventory must truncate long names");
        assertTrue(source.contains("abbrevStat") || source.contains("abbrev"),
            "Inventory must use abbreviated stats");
    }

    @Test
    void hudAndCodexRemainIconFirst() throws IOException {
        String hud = Files.readString(Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/render/HudRenderer.java"
        ));
        // HUD already icon-first, but must still contain icons.draw
        assertTrue(hud.contains("icons.draw"), "HUD must draw icons");
        String codex = Files.readString(Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/render/CodexOverlayRenderer.java"
        ));
        assertTrue(codex.contains("icons.draw") || codex.contains("UiFrameRenderer"),
            "Codex must be icon-aware");
    }
}
