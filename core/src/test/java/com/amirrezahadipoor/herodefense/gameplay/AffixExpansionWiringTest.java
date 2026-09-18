package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.items.AffixId;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.EquipmentSlot;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.Item;
import com.amirrezahadipoor.herodefense.potions.PotionDropSystem;
import org.junit.jupiter.api.Test;

/**
 * Each of the five B2a affixes must have exactly one real consumer in the shipped systems:
 * Elite Damage in the arrow formula, Thorns in the melee pipeline, Potion Find in the potion
 * roll, Focus Gain in the meter fill, Fortitude in the incoming-damage pipeline. Query-only
 * affixes would be dead text in the tooltip, so every lane is fired end to end here.
 */
final class AffixExpansionWiringTest {
    private final EnemyFactory factory = new EnemyFactory();

    @Test
    void eliteDamageOnlyMultipliesArrowsAimedAtElites() {
        HeroAutoAttackSystem attacks = new HeroAutoAttackSystem();

        GameState plain = autoAttackState(10L, null);
        attacks.update(plain, 0f);
        assertEquals(1, plain.projectiles.size());
        assertEquals(16f, plain.projectiles.get(0).damage, 0.001f);

        GameState elite = autoAttackState(10L, AffixId.ELITE_DAMAGE);
        Enemy foe = elite.aliveEnemies.get(0);
        foe.eliteAffix = "BRUTAL";
        attacks.update(elite, 0f);
        assertEquals(1, elite.projectiles.size());
        assertEquals(16f * 1.10f, elite.projectiles.get(0).damage, 0.01f,
            "the elite branch of the arrow formula must carry the Elite Damage affix");

        GameState unaffixedElite = autoAttackState(10L, null);
        unaffixedElite.aliveEnemies.get(0).eliteAffix = "BRUTAL";
        attacks.update(unaffixedElite, 0f);
        assertEquals(16f, unaffixedElite.projectiles.get(0).damage, 0.001f,
            "without the affix an elite takes exactly what a normal foe takes");
    }

    @Test
    void thornsReflectsTheMeleeSwingThatLanded() {
        EnemyMeleeAttackSystem melee = new EnemyMeleeAttackSystem(new HeroDamageSystem());

        GameState bare = meleeState(30L, null);
        Enemy bareAttacker = bare.aliveEnemies.get(0);
        melee.update(bare, 0f);
        assertEquals(95f, bare.hero.health, 0.001f, "a ROOTLING swing costs five health");
        assertEquals(bareAttacker.maxHealth, bareAttacker.health, 0f,
            "without Thorns the attacker pays nothing");

        GameState thorned = meleeState(30L, AffixId.THORNS);
        Enemy attacker = thorned.aliveEnemies.get(0);
        melee.update(thorned, 0f);
        assertEquals(95f, thorned.hero.health, 0.001f);
        assertEquals(attacker.maxHealth - attacker.damage * 0.20f, attacker.health, 0.001f,
            "a landed swing must reflect a fifth of itself back through the ward path");
    }

    @Test
    void potionFindRaisesTheDropRateWithoutMovingTheRoll() {
        PotionDropSystem drops = new PotionDropSystem();
        GameState bare = GameState.newRun(7L);
        assertEquals(PotionDropSystem.DROP_RATE, drops.effectiveDropRate(bare), 0f);
        GameState finder = GameState.newRun(7L);
        equip(finder, AffixId.POTION_FIND);
        assertEquals(PotionDropSystem.DROP_RATE * 1.15f, drops.effectiveDropRate(finder), 0.0001f);
    }

    @Test
    void focusGainChargesTheUltimateMeterFaster() {
        GameState bare = GameState.newRun(11L);
        FocusSystem.addHits(bare, 5, 0, 0);
        assertEquals(5 * FocusSystem.FOCUS_PER_HIT, bare.focus, 0.0001f);

        GameState focused = GameState.newRun(11L);
        equip(focused, AffixId.FOCUS_GAIN);
        FocusSystem.addHits(focused, 5, 0, 0);
        assertEquals(5 * FocusSystem.FOCUS_PER_HIT * 1.12f, focused.focus, 0.0001f,
            "the fill-rate multiplier is the single lane every Focus gain passes through");
    }

    @Test
    void fortitudeCutsTheMeleeSwingBeforeItReachesTheHero() {
        EnemyMeleeAttackSystem melee = new EnemyMeleeAttackSystem(new HeroDamageSystem());

        GameState bare = meleeState(30L, null);
        melee.update(bare, 0f);
        assertEquals(95f, bare.hero.health, 0.001f, "a ROOTLING swing costs five health");

        GameState fortified = meleeState(30L, AffixId.FORTITUDE);
        melee.update(fortified, 0f);
        assertEquals(100f - 5f * 0.94f, fortified.hero.health, 0.001f,
            "Fortitude must scale the swing inside the incoming-damage pipeline");
    }

    private GameState autoAttackState(long seed, AffixId affix) {
        GameState state = GameState.newRun(seed);
        state.hero.stats.strength = 3;
        if (affix != null) {
            equip(state, affix);
        }
        Enemy enemy = new Enemy(
            state.allocateEntityId(), "ROOTLING", state.hero.x + 90f, state.hero.y
        );
        enemy.health = 100f;
        enemy.maxHealth = 100f;
        state.aliveEnemies.add(enemy);
        return state;
    }

    private GameState meleeState(long seed, AffixId affix) {
        GameState state = GameState.newRun(seed);
        if (affix != null) {
            equip(state, affix);
        }
        Enemy enemy = factory.create(
            state, EnemyType.ROOTLING, state.hero.x - 100f, state.hero.y, 0
        );
        enemy.x = state.hero.x - enemy.attackRange;
        state.aliveEnemies.add(enemy);
        return state;
    }

    private static void equip(GameState state, AffixId affix) {
        Item item = new Item("probe", "Probe", EquipmentSlot.WEAPON.name(), "RARE");
        item.affixId = affix.name();
        state.equippedItems.put(EquipmentSlot.WEAPON.name(), item);
    }
}
