package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.model.ItemTier;
import org.junit.jupiter.api.Test;

/**
 * The drop economy's table, generated from the code that rolls it (roadmap R4.3, first half).
 *
 * <p>Rates, pool sizes and sell prices live in three places — {@code ItemDropSystem}, {@code EquipmentCatalog} and
 * {@code EquipmentDefinition} — and a document that copies them lies the first time one of them moves. These tests
 * hold the economy's own arithmetic (the budget, the bands, the pools); the table published in {@code docs/BALANCE.md}
 * is generated from the same constants and checked by {@code BalanceDocumentTest} (roadmap R4.4), so this file no
 * longer reads the document at all. What stays here as well is the record of the three pity rules measured against
 * the balance sweep and blocked: they are candidates with prices, not behaviour, and the tests that would hold a
 * shipped rule are written when one can pass.
 */
final class DropEconomyTableTest {
    private static final float TOTAL_RATE = 0.099515f;

    private final ItemDropSystem drops = new ItemDropSystem();

    @Test
    void theItemBudgetAndItsCoinValueAddUp() {
        float items = 0f;
        float coins = 0f;
        for (ItemTier tier : ItemTier.values()) {
            items += baseRate(tier);
            coins += baseRate(tier) * sellPrice(tier);
        }
        assertEquals(TOTAL_RATE, items, 1e-6f, "about one item per ten kills");
        assertEquals(2.49591f, coins, 1e-4f, "and about two and a half coins per kill");
    }

    @Test
    void theTierBandsAreCumulativeAndLuckStretchesThem() {
        assertNull(drops.tierForRoll(TOTAL_RATE, 1f), "the budget runs out at 9.9515 percent");
        assertEquals(ItemTier.COMMON, drops.tierForRoll(TOTAL_RATE, 2f), "Luck buys longer bands");
        assertEquals(ItemTier.MYTHIC, drops.tierForRoll(0f, 1f));
        assertEquals(ItemTier.LEGENDARY, drops.tierForRoll(0.0001f, 1f), "under 0.0015 percent");
        assertEquals(ItemTier.RARE, drops.tierForRoll(0.005f, 1f), "under 0.0095");
        assertEquals(ItemTier.UNCOMMON, drops.tierForRoll(0.02f, 1f), "under 0.0395");
        assertEquals(ItemTier.COMMON, drops.tierForRoll(0.09f, 1f), "under 0.0995");
    }

    @Test
    void everyTierStillHasAPoolToPickFrom() {
        for (ItemTier tier : ItemTier.values()) {
            int pool = poolSize(tier);
            assertTrue(pool > 0, tier + " has no equipment, so a roll in its band would have nothing to drop");
        }
        assertEquals(46, EquipmentCatalog.all().size(), "the shipped pool, so a content edit has to say so here");
    }

    private static int poolSize(ItemTier tier) {
        return (int) EquipmentCatalog.all().stream().filter(item -> item.tier() == tier).count();
    }

    private static float baseRate(ItemTier tier) {
        return switch (tier) {
            case COMMON -> ItemDropSystem.COMMON_RATE;
            case UNCOMMON -> ItemDropSystem.UNCOMMON_RATE;
            case RARE -> ItemDropSystem.RARE_RATE;
            case LEGENDARY -> ItemDropSystem.LEGENDARY_RATE;
            case MYTHIC -> ItemDropSystem.LEGENDARY_RATE * ItemDropSystem.MYTHIC_SHARE_OF_LEGENDARY;
        };
    }

    private static int sellPrice(ItemTier tier) {
        return EquipmentCatalog.all().stream()
            .filter(item -> item.tier() == tier)
            .findFirst()
            .orElseThrow()
            .createItem()
            .sellPrice;
    }

}
