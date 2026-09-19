package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/** The Crimson Root caps at 26% and pays everything past it in coins, never in life (roadmap B1). */
class LifestealCapTest {
    private final HeroAutoAttackSystem system = new HeroAutoAttackSystem();

    @Test
    void stackedSourcesNeverHealPastTheCap() {
        GameState state = GameState.newRun(20L);
        state.permanentEffects.put("lifesteal", 1.53f);
        state.hero.health = 50f;
        state.aliveEnemies.add(enemy(state));

        system.update(state, 0f);
        float damage = state.projectiles.get(0).damage;
        system.update(state, 0.2f);

        assertEquals(
            50f + HeroAutoAttackSystem.LIFESTEAL_CAP * damage, state.hero.health, 0.0001f);
    }

    @Test
    void overflowSharePaysCoinsInsteadOfLife() {
        GameState state = GameState.newRun(21L);
        state.permanentEffects.put("lifesteal", 1.53f);
        state.coins = 0;
        state.aliveEnemies.add(enemy(state));

        system.update(state, 0f);
        system.update(state, 0.2f);

        assertTrue(state.coins >= 1, "the share past the cap must mint coins");
    }

    @Test
    void atCapNothingConvertsToCoins() {
        GameState state = GameState.newRun(22L);
        state.permanentEffects.put("lifesteal", HeroAutoAttackSystem.LIFESTEAL_CAP);
        state.coins = 40;
        state.hero.health = 50f;
        state.aliveEnemies.add(enemy(state));

        system.update(state, 0f);
        float damage = state.projectiles.get(0).damage;
        system.update(state, 0.2f);

        assertEquals(40, state.coins);
        assertEquals(50f + HeroAutoAttackSystem.LIFESTEAL_CAP * damage, state.hero.health, 0.0001f);
    }

    @Test
    void capConstantSitsAboveTheHonestLandingZone() {
        assertEquals(0.26f, HeroAutoAttackSystem.LIFESTEAL_CAP, 0.000001f);
    }

    private Enemy enemy(GameState state) {
        Enemy target = new Enemy(
            state.allocateEntityId(), "ROOTLING", state.hero.x + 90f, state.hero.y + 0f);
        target.health = 100f;
        target.maxHealth = 100f;
        return target;
    }
}
