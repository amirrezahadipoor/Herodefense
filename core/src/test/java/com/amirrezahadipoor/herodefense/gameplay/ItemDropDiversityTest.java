package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.items.EquipmentDefinition;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.EquipmentSlot;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.ItemTier;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * The item pool and the drop pick (roadmap R3.4, item-pool diversity).
 *
 * <p>Two of these cases are accounting rather than behaviour, on purpose: the pool's shape is the honest limit on
 * what a drop can be, and it was invisible until it was written down. Measured: 46 pieces, fourteen common /
 * twelve uncommon / nine rare / five legendary / six mythic; every slot carries seven or more pieces and every slot
 * has its own mythic, but there are only five legendaries in the whole game, so the first ring slot has nothing above
 * rare to find. Widening those thin places is an art task, not a code task.
 *
 * <p>What is <em>not</em> here is the ownership-aware pick, because it is not shipped: it works, and it moves the
 * trajectory of every run that drops equipment, which pushed the ascension gate's LIFESTEAL tier-0 scenario to a
 * 40.24% spike and the trial gate's BOSS_BOUNTY + FAMISHED_EARTH pair to 43.33% at wave 196, both against a 40%
 * ceiling. The ceilings belong to the balance program (R4.1-R4.5), so the pick stays uniform and the recipe waits in
 * the roadmap next to those numbers.
 */
final class ItemDropDiversityTest {

    private final ItemDropSystem drops = new ItemDropSystem();

    @Test
    void everyTierOffersAPool() {
        Map<ItemTier, Integer> counts = new EnumMap<>(ItemTier.class);
        for (EquipmentDefinition definition : EquipmentCatalog.all()) {
            counts.merge(definition.tier(), 1, Integer::sum);
        }
        for (ItemTier tier : ItemTier.values()) {
            assertTrue(counts.getOrDefault(tier, 0) > 0,
                tier + " has no pieces, so a drop of that tier could not be built");
        }
        assertEquals(46, EquipmentCatalog.all().size(), "the shipped pool");
        assertEquals(14, counts.get(ItemTier.COMMON), "commons");
        assertEquals(12, counts.get(ItemTier.UNCOMMON), "uncommons");
        assertEquals(9, counts.get(ItemTier.RARE), "rares");
        assertEquals(5, counts.get(ItemTier.LEGENDARY), "legendaries");
        assertEquals(6, counts.get(ItemTier.MYTHIC), "mythics: one per slot");
    }

    @Test
    void thePoolCompositionIsWrittenDown() {
        Map<EquipmentSlot, Integer> perSlot = new EnumMap<>(EquipmentSlot.class);
        Map<EquipmentSlot, Integer> legendaryPerSlot = new EnumMap<>(EquipmentSlot.class);
        Map<EquipmentSlot, Integer> mythicPerSlot = new EnumMap<>(EquipmentSlot.class);
        int weapons = 0;
        for (EquipmentDefinition definition : EquipmentCatalog.all()) {
            perSlot.merge(definition.slot(), 1, Integer::sum);
            if (definition.tier() == ItemTier.LEGENDARY) {
                legendaryPerSlot.merge(definition.slot(), 1, Integer::sum);
            }
            if (definition.tier() == ItemTier.MYTHIC) {
                mythicPerSlot.merge(definition.slot(), 1, Integer::sum);
            }
            if (definition.slot() == EquipmentSlot.WEAPON) weapons++;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            assertTrue(perSlot.getOrDefault(slot, 0) >= 4,
                slot + " carries only " + perSlot.getOrDefault(slot, 0) + " pieces");
        }
        // Recorded shape, not an accident: every slot carries at least seven pieces and its own mythic, and the
        // whole legendary tier is five pieces -- one per slot except the first ring, which stops at rare.
        assertEquals(9, weapons, "nine weapons across the five tiers");
        assertEquals(5, EquipmentCatalog.all().stream()
            .filter(definition -> definition.tier() == ItemTier.LEGENDARY).count(), "five legendaries in all");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            assertEquals(1, mythicPerSlot.getOrDefault(slot, 0),
                slot + " carries its own mythic");
        }
        assertEquals(0, legendaryPerSlot.getOrDefault(EquipmentSlot.RING_1, 0),
            "the first ring slot stops at rare, so a legendary ring can only land in the second");
        assertEquals(1, legendaryPerSlot.getOrDefault(EquipmentSlot.RING_2, 0));
    }

    @Test
    void anEmptyTierFallsBackInsteadOfIndexingAnEmptyList() {
        Map<ItemTier, List<EquipmentDefinition>> catalog = new EnumMap<>(ItemTier.class);
        for (ItemTier tier : ItemTier.values()) catalog.put(tier, new ArrayList<>());
        for (EquipmentDefinition definition : EquipmentCatalog.all()) {
            if (definition.tier() == ItemTier.LEGENDARY) {
                catalog.get(ItemTier.LEGENDARY).add(definition);
            }
        }
        List<EquipmentDefinition> fellBack = ItemDropSystem.tierPool(catalog, ItemTier.MYTHIC);
        assertFalse(fellBack.isEmpty(), "an empty tier must borrow the nearest non-empty one");
        assertEquals(ItemTier.LEGENDARY, fellBack.get(0).tier());

        Map<ItemTier, List<EquipmentDefinition>> emptyCatalog = new EnumMap<>(ItemTier.class);
        for (ItemTier tier : ItemTier.values()) emptyCatalog.put(tier, new ArrayList<>());
        assertTrue(ItemDropSystem.tierPool(emptyCatalog, ItemTier.RARE).isEmpty(),
            "with nothing anywhere the pool is empty, not an exception");
    }

    @Test
    void thePickSpendsExactlyOneCombatFloat() {
        GameState picked = GameState.newRun(0x0DDBA15L);
        GameState stepped = GameState.newRun(0x0DDBA15L);
        new ItemDropSystem().chooseFor(picked, ItemTier.COMMON);
        stepped.nextCombatRandomFloat();
        assertEquals(stepped.nextCombatRandomFloat(), picked.nextCombatRandomFloat(),
            "one draw per pick: a drop sits where it always sat in the combat stream");
    }

    @Test
    void theDropIsStillOneEntityWithAKnownItem() {
        GameState state = GameState.newRun(0x0DDBA14L);
        Set<String> known = new HashSet<>();
        for (EquipmentDefinition definition : EquipmentCatalog.all()) known.add(definition.id());
        int dropped = 0;
        for (int kill = 0; kill < 4000; kill++) {
            Boss boss = new Boss(state.allocateEntityId(), "THORN_MATRIARCH", 0f, 0f, 1);
            boss.alive = false;
            state.aliveBosses.clear();
            state.aliveBosses.add(boss);
            dropped += drops.processDefeatedEnemies(state);
        }
        assertTrue(dropped > 0, "four thousand rare-floor kills must drop something");
        assertEquals(dropped, state.drops.size(), "one drop entity per drop");
        int distinct = new HashSet<>(state.drops.stream().map(drop -> drop.itemId).toList()).size();
        assertTrue(distinct >= 30,
            "rare-floor kills are restricted to the upper tiers, and 30 of the 46 pieces still showed up: "
                + distinct);
        for (var drop : state.drops) {
            assertTrue(known.contains(drop.itemId), "drop carried an unknown item: " + drop.itemId);
        }
    }
}
