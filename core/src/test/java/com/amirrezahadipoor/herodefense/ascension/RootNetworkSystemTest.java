package com.amirrezahadipoor.herodefense.ascension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

final class RootNetworkSystemTest {
    private final RootNetworkSystem system = new RootNetworkSystem();

    @Test
    void freshRunAppliesEveryOwnedNodeOnce() {
        GameState state = GameState.newRun(7L);
        state.rootNodesPurchased.put("root_all_1", true);
        state.rootNodesPurchased.put("root_heart_1", true);

        system.applyPermanentBonuses(state);

        assertEquals(1, state.hero.stats.strength);
        assertEquals(1, state.hero.stats.agility);
        assertEquals(1, state.hero.stats.health);
        assertEquals(1, state.unspentTalentPoints);
        assertEquals(1025f, state.worldTreeMaxHealth);
        assertEquals(state.hero.maxHealth, state.hero.health);
    }

    @Test
    void midRunPurchaseAppliesOnlyTheNewNodeAndNeverHeals() {
        GameState state = GameState.newRun(8L);
        state.rootNodesPurchased.put("root_all_1", true);
        system.applyPermanentBonuses(state);
        int strengthAfterRunStart = state.hero.stats.strength;
        state.hero.health = state.hero.maxHealth * 0.1f;
        state.worldTreeHealth = 300f;

        state.rootNodesPurchased.put("root_heart_1", true);
        system.applyNodeDuringRun(state, "root_heart_1");

        assertEquals(strengthAfterRunStart, state.hero.stats.strength,
            "earlier nodes stay applied exactly once");
        assertEquals(1, state.unspentTalentPoints, "the new node's own grant arrives once");
        assertTrue(state.hero.health < state.hero.maxHealth,
            "a mid-run purchase is a bonus, not a full heal");
        assertEquals(300f, state.worldTreeHealth, "the tree is never healed mid-run");
        assertEquals(1025f, state.worldTreeMaxHealth, "the tree's ceiling still grows");
    }

    @Test
    void midRunStatNodeKeepsEquippedGearBonuses() {
        GameState state = GameState.newRun(9L);
        com.amirrezahadipoor.herodefense.model.Item gear =
            new com.amirrezahadipoor.herodefense.model.Item("charm", "Charm", "WEAPON", "COMMON");
        gear.statBonuses.put(com.amirrezahadipoor.herodefense.model.HeroStat.HEALTH.name(), 30f);
        state.equippedItems.put(
            com.amirrezahadipoor.herodefense.model.EquipmentSlot.WEAPON.name(), gear);
        state.synchronizeEquipmentHealth();
        float maxWithGear = state.hero.maxHealth;
        state.heartwood = 5_000;
        state.rootNodesPurchased.put("root_heart_1", false);

        state.rootNodesPurchased.put("root_heart_1", true);
        system.applyNodeDuringRun(state, "root_heart_1");

        assertEquals(maxWithGear + 50f, state.hero.maxHealth, "gear and the node stack additively");
        assertTrue(state.hero.health <= state.hero.maxHealth);
    }
}
