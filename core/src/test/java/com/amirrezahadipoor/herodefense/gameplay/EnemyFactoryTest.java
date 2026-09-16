package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

final class EnemyFactoryTest {
    private final EnemyFactory factory = new EnemyFactory();

    @Test
    void createsAllEightDistinctRegularTypesAsMeleeAttackers() {
        GameState state = GameState.newRun(4L);
        for (EnemyType type : EnemyType.values()) {
            Enemy enemy = factory.create(state, type, 10f, 20f, 2);
            assertEquals(type.name(), enemy.enemyType);
            assertEquals(type.baseHealth(), enemy.health);
            assertEquals(type.baseDamage(), enemy.damage);
            assertEquals(type.attackRange(), enemy.attackRange);
            assertTrue(type.isMelee());
            assertTrue(enemy.attackRange <= 50f);
        }
        assertEquals(8, EnemyType.values().length, "R3.4 doubled the roster");
        // The invariant the spawner depends on: types cycle uniformly, so the roster's mean is the wave's
        // weight. Doubling the roster must not move it.
        float healthSum = 0f;
        float damageSum = 0f;
        float speedSum = 0f;
        float reachSum = 0f;
        float intervalSum = 0f;
        int experienceSum = 0;
        int coinSum = 0;
        for (EnemyType type : EnemyType.values()) {
            healthSum += type.baseHealth();
            damageSum += type.baseDamage();
            speedSum += type.movementSpeed();
            reachSum += type.attackRange();
            intervalSum += type.attackIntervalSeconds();
            experienceSum += type.experienceReward();
            coinSum += type.coinReward();
        }
        int roster = EnemyType.values().length;
        assertEquals(29.25f, healthSum / roster, 0.001f,
                "mean health per type is unchanged since the four-role roster");
        assertEquals(7f, damageSum / roster, 0.001f, "and so is mean damage");
        assertEquals(17.25f, (float) experienceSum / roster, 0.001f, "and mean experience");
        assertEquals(4.75f, (float) coinSum / roster, 0.001f, "and mean coins");
        // Speed, reach and interval are part of the wave's weight too, and the simulators proved it: a tuning
        // pass that moved only the speed profile shifted the late sweep by several percent.
        assertEquals(60.5f, speedSum / roster, 0.001f, "mean movement speed per type");
        assertEquals(43f, reachSum / roster, 0.001f, "mean attack reach per type");
        assertEquals(1.275f, intervalSum / roster, 0.001f, "mean attack interval per type");
    }

    @Test
    void archetypesHaveDifferentCombatProfilesAndMatchingAssetKeys() {
        assertTrue(EnemyType.GLOOM_WOLF.movementSpeed() > EnemyType.STONEKIN.movementSpeed());
        assertTrue(EnemyType.FUNGAL_BRUTE.baseHealth() > EnemyType.ROOTLING.baseHealth());
        assertEquals("fungal_brute", EnemyType.FUNGAL_BRUTE.assetKey());
    }
}
