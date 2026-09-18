package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.DropEntity;
import org.junit.jupiter.api.Test;

final class DropRarityVisualTest {
    @Test
    void onlyRareAndLegendaryEquipmentDropsReceiveGlowTreatment() {
        assertEquals(VisualRarity.COMMON, rarity("ashwood_bow"));
        assertEquals(VisualRarity.UNCOMMON, rarity("moonwood_longbow"));
        assertEquals(VisualRarity.RARE, rarity("starfall_bow"));
        assertEquals(VisualRarity.LEGENDARY, rarity("worldbranch"));
        assertEquals(VisualRarity.MYTHIC, rarity("sunfall_last_arrow"));
        assertTrue(rarity("starfall_bow").isGlowing());
        assertTrue(rarity("worldbranch").isGlowing());

        DropEntity potion = new DropEntity(2L, "POTION", 0f, 0f, 1);
        potion.itemId = "TIER_6";
        assertEquals(VisualRarity.COMMON, CombatEntityRenderer.dropRarity(potion));
    }

    @Test
    void dropsMapToValidColorsInBothPalettes() {
        String[] samples = {
            "ashwood_bow", "moonwood_longbow", "starfall_bow", "worldbranch", "sunfall_last_arrow"
        };
        for (String sample : samples) {
            VisualRarity r = rarity(sample);
            assertNotNull(r.color(false), "standard color must exist for " + sample);
            assertNotNull(r.color(true), "accessible color must exist for " + sample);
        }
    }

    private static VisualRarity rarity(String itemId) {
        DropEntity drop = new DropEntity(1L, "ITEM", 0f, 0f, 1);
        drop.itemId = itemId;
        return CombatEntityRenderer.dropRarity(drop);
    }
}
