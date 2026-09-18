package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.items.EquipmentDefinition;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.DropEntity;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.ItemTier;
import com.amirrezahadipoor.herodefense.trials.TrialEffects;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Rolls at most one low-chance equipment drop per defeated enemy, modified by Luck.
 *
 * <p>Roadmap R4.3 wanted a pity rule here, and the three versions of it that were measured are recorded with
 * their prices in {@code docs/BALANCE.md}: the shipped game has none, because the balance sweep's ceilings sit
 * within a percent of their limits and every version of the rule moved one of them. The rule returns when R4.6
 * has bought the headroom it needs.
 */
public final class ItemDropSystem {
    public static final float COMMON_RATE = 0.06f;
    public static final float UNCOMMON_RATE = 0.03f;
    public static final float RARE_RATE = 0.008f;
    public static final float LEGENDARY_RATE = 0.0015f;
    /** Share of the Legendary band that upgrades to a Mythic (~7.5e-5 per kill). */
    public static final float MYTHIC_SHARE_OF_LEGENDARY = 0.01f;

    private final HeroStatCalculator statCalculator;
    private final Map<ItemTier, List<EquipmentDefinition>> byTier = new EnumMap<>(ItemTier.class);

    public ItemDropSystem() {
        this(new HeroStatCalculator());
    }

    public ItemDropSystem(HeroStatCalculator statCalculator) {
        this.statCalculator = statCalculator;
        for (ItemTier tier : ItemTier.values()) byTier.put(tier, new ArrayList<>());
        for (EquipmentDefinition item : EquipmentCatalog.all()) byTier.get(item.tier()).add(item);
    }

    public int processDefeatedEnemies(GameState state) {
        if (state == null || state.hero == null) return 0;
        int drops = 0;
        for (Enemy enemy : state.aliveEnemies) drops += rollOnce(state, enemy);
        for (Boss boss : state.aliveBosses) drops += rollOnce(state, boss);
        return drops;
    }

    public ItemTier tierForRoll(float roll, float luckMultiplier) {
        if (roll < 0f || roll >= 1f || Float.isNaN(roll)) {
            throw new IllegalArgumentException("Drop roll must be in [0, 1)");
        }
        float multiplier = Math.max(0f, luckMultiplier);
        float threshold = LEGENDARY_RATE * multiplier * MYTHIC_SHARE_OF_LEGENDARY;
        if (roll < threshold) return ItemTier.MYTHIC;
        threshold = LEGENDARY_RATE * multiplier;
        if (roll < threshold) return ItemTier.LEGENDARY;
        threshold += RARE_RATE * multiplier;
        if (roll < threshold) return ItemTier.RARE;
        threshold += UNCOMMON_RATE * multiplier;
        if (roll < threshold) return ItemTier.UNCOMMON;
        threshold += COMMON_RATE * multiplier;
        return roll < threshold ? ItemTier.COMMON : null;
    }

    /**
     * Picks the item a drop of this tier becomes.
     *
     * <p>What is shipped is the uniform pick over the tier, plus the guard the old code lacked (see
     * {@link #tierPool}); the interesting part is what was <em>measured</em> and deliberately left out. The obvious
     * improvement -- prefer pieces the hero does not own, and prefer them in a slot nothing occupies yet -- was
     * written, tested and taken to the three balance gates. It does work as a pool feature, and it moved the
     * trajectory of every run that drops equipment, which is enough to push two separate gates over ceilings that
     * sit at 40%: an ownership-aware pick put the ascension gate's LIFESTEAL tier-0 scenario at a 40.24% spike and
     * the trial gate's BOSS_BOUNTY + FAMISHED_EARTH pair at 43.33% on wave 196. Those ceilings are not this
     * feature's to move, so the pick stays uniform until the balance program (R4.1-R4.5) has bought the band real
     * headroom; the recipe is recorded in the roadmap beside those numbers.
     *
     * <p>One draw is spent here and only one, exactly as before, so a drop sits at the same place in the combat
     * random stream as it always did.
     */
    public EquipmentDefinition chooseFor(GameState state, ItemTier tier) {
        List<EquipmentDefinition> choices = tierPool(byTier, tier);
        int index = Math.min(
            choices.size() - 1,
            (int) (state.nextCombatRandomFloat() * choices.size())
        );
        return choices.get(index);
    }

    /**
     * The pool for a tier, with the guard the old code lacked: a tier with no pieces of its own falls back to the
     * nearest non-empty tier instead of indexing into an empty list. Every tier is populated today (asserted in the
     * tests), so this exists to keep a future content edit from turning a rare drop into a crash.
     */
    static List<EquipmentDefinition> tierPool(
        Map<ItemTier, List<EquipmentDefinition>> catalog, ItemTier tier
    ) {
        ItemTier[] tiers = ItemTier.values();
        int start = Math.max(0, Math.min(tiers.length - 1, tier.ordinal()));
        for (int distance = 0; distance < tiers.length; distance++) {
            int lower = start - distance;
            if (lower >= 0) {
                List<EquipmentDefinition> below = catalog.get(tiers[lower]);
                if (below != null && !below.isEmpty()) return below;
            }
            int higher = start + distance;
            if (higher < tiers.length && higher != lower) {
                List<EquipmentDefinition> above = catalog.get(tiers[higher]);
                if (above != null && !above.isEmpty()) return above;
            }
        }
        return List.of();
    }

    private int rollOnce(GameState state, Enemy enemy) {
        if (enemy == null || enemy.alive || enemy.itemDropRolled) return 0;
        enemy.itemDropRolled = true;
        if (enemy.silentWatcher) return 0;
        ItemTier tier = tierForRoll(
            state.nextCombatRandomFloat(),
            statCalculator.dropChanceMultiplier(state)
                * TrialEffects.itemDropChanceMultiplier(state.activeTrials)
        );
        boolean crowned = enemy instanceof Boss
            && TrialEffects.bossAlwaysDropsRarePlus(state.activeTrials);
        boolean rareFloor = crowned || enemy.eliteAffix != null;
        if (tier == null) {
            if (!rareFloor) return 0;
            tier = ItemTier.RARE;
        } else if (rareFloor && tier.ordinal() < ItemTier.RARE.ordinal()) {
            tier = ItemTier.RARE;
        }

        EquipmentDefinition selected = chooseFor(state, tier);
        DropEntity drop = new DropEntity(
            state.allocateEntityId(), "ITEM", enemy.x, enemy.y, 1
        );
        drop.itemId = selected.id();
        drop.pickupDelaySeconds = 2.6f;
        state.drops.add(drop);
        return 1;
    }
}
