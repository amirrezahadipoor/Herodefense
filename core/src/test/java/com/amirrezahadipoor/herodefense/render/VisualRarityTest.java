package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.items.EquipmentDefinition;
import com.badlogic.gdx.graphics.Color;
import java.util.List;
import org.junit.jupiter.api.Test;

final class VisualRarityTest {
    @Test
    void onlyRareAndLegendaryItemsGlow() {
        assertFalse(VisualRarity.COMMON.isGlowing());
        assertFalse(VisualRarity.UNCOMMON.isGlowing());
        assertTrue(VisualRarity.RARE.isGlowing());
        assertTrue(VisualRarity.LEGENDARY.isGlowing());
        assertTrue(VisualRarity.MYTHIC.isGlowing());
        assertTrue(VisualRarity.LEGENDARY.intensity() > VisualRarity.RARE.intensity());
        assertTrue(VisualRarity.MYTHIC.intensity() > VisualRarity.LEGENDARY.intensity());
    }

    @Test
    void allEquippedCatalogTiersMapToTheRuntimeGlowPolicy() {
        for (EquipmentDefinition definition : EquipmentCatalog.all()) {
            VisualRarity rarity = VisualRarity.fromTier(definition.tier().name());
            assertTrue(
                rarity.isGlowing()
                    == ("RARE".equals(definition.tier().name())
                        || "LEGENDARY".equals(definition.tier().name())
                        || "MYTHIC".equals(definition.tier().name()))
            );
        }
    }

    @Test
    void defaultPaletteMatchesHistoricalColorsAndDistinguishesFiveTiers() {
        assertEquals(Color.valueOf("E7D8B1"), VisualRarity.COMMON.color(false));
        assertEquals(Color.valueOf("74C365"), VisualRarity.UNCOMMON.color(false));
        assertEquals(Color.valueOf("6FADEB"), VisualRarity.RARE.color(false));
        assertEquals(Color.valueOf("F2B84B"), VisualRarity.LEGENDARY.color(false));
        assertEquals(Color.valueOf("C77DFF"), VisualRarity.MYTHIC.color(false));

        List<VisualRarity> tiers = List.of(
            VisualRarity.COMMON, VisualRarity.UNCOMMON, VisualRarity.RARE,
            VisualRarity.LEGENDARY, VisualRarity.MYTHIC
        );
        for (int i = 0; i < tiers.size(); i++) {
            for (int j = i + 1; j < tiers.size(); j++) {
                assertNotEquals(tiers.get(i).color(false), tiers.get(j).color(false),
                    "default tiers must be distinct: " + tiers.get(i) + " vs " + tiers.get(j));
            }
        }
    }

    @Test
    void accessiblePaletteMaintainsSeparationAcrossAllFiveTiers() {
        List<VisualRarity> tiers = List.of(
            VisualRarity.COMMON, VisualRarity.UNCOMMON, VisualRarity.RARE,
            VisualRarity.LEGENDARY, VisualRarity.MYTHIC
        );

        for (int i = 0; i < tiers.size(); i++) {
            Color c1 = tiers.get(i).color(true);
            for (int j = i + 1; j < tiers.size(); j++) {
                Color c2 = tiers.get(j).color(true);
                assertNotEquals(c1, c2, "accessible tiers must not collide: " + tiers.get(i) + " vs " + tiers.get(j));
                float dr = c1.r - c2.r;
                float dg = c1.g - c2.g;
                float db = c1.b - c2.b;
                float distance = (float) Math.sqrt(dr * dr + dg * dg + db * db);
                assertTrue(distance >= 0.20f,
                    "pairwise Euclidean distance must be >= 0.20f between "
                        + tiers.get(i) + " and " + tiers.get(j) + ", got " + distance);
            }
        }
    }

    @Test
    void colorForTierHandlesNullAndUnknownGracefully() {
        assertEquals(VisualRarity.ACCESSIBLE_COMMON, VisualRarity.colorForTier(null, true));
        assertEquals(VisualRarity.DEFAULT_COMMON, VisualRarity.colorForTier(null, false));
        assertEquals(VisualRarity.ACCESSIBLE_COMMON, VisualRarity.colorForTier("UNKNOWN", true));
        assertEquals(VisualRarity.DEFAULT_COMMON, VisualRarity.colorForTier("UNKNOWN", false));
        assertEquals(VisualRarity.DEFAULT_LEGENDARY, VisualRarity.colorForTier("LEGENDARY"));
    }
}
