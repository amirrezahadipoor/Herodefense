package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A boss's second half: the crossing happens once, it is the fight's own threshold, and it means something. */
class BossEvolutionTest {

    private static Boss bossIn(GameState state, float healthFraction) {
        Boss boss = new BossFactory().create(state, BossType.ANCIENT_GOLEM, 360f, 600f, 40, 2);
        boss.maxHealth = 1000f;
        boss.health = 1000f * healthFraction;
        state.aliveBosses.add(boss);
        return boss;
    }

    @Test
    void aBossThatHasNotCrossedItsThresholdIsUntouched() {
        GameState state = new GameState();
        Boss boss = bossIn(state, 1f);
        float speed = boss.movementSpeed;
        float damage = boss.damage;
        assertEquals(0, BossEvolution.update(state));
        assertEquals(speed, boss.movementSpeed, 0.001f);
        assertEquals(damage, boss.damage, 0.001f);
        assertFalse(boss.evolutionApplied);
    }

    @Test
    void theCrossingFiresExactlyOnceAndOnlyPastTheScriptsOwnThreshold() {
        GameState state = new GameState();
        Boss boss = bossIn(state, 1f);
        float threshold = BossFightScript.of(boss).evolutionHealthRatio();
        boss.health = boss.maxHealth * (threshold + 0.05f);
        assertEquals(0, BossEvolution.update(state), "fired above the threshold");

        boss.health = boss.maxHealth * (threshold - 0.01f);
        assertEquals(1, BossEvolution.update(state));
        assertTrue(boss.evolutionApplied);
        assertEquals(0, BossEvolution.update(state), "the crossing fired twice");
    }

    @Test
    void theSecondHalfIsFasterHeavierAndQuickerOnItsSpecials() {
        GameState state = new GameState();
        Boss boss = bossIn(state, 0.2f);
        float speed = boss.movementSpeed;
        float damage = boss.damage;
        BossEvolution.update(state);
        assertEquals(speed * BossEvolution.ENRAGED_SPEED_MULTIPLIER, boss.movementSpeed, 0.01f);
        assertEquals(damage * BossEvolution.ENRAGED_DAMAGE_MULTIPLIER, boss.damage, 0.01f);
        assertEquals(
            2.4f * BossEvolution.ENRAGED_CADENCE_MULTIPLIER,
            BossEvolution.specialInterval(boss, 2.4f),
            0.0001f
        );
        assertTrue(BossEvolution.specialInterval(boss, 2.4f) < 2.4f);
        assertEquals(
            BossEvolution.MIN_SPECIAL_INTERVAL_SECONDS,
            BossEvolution.specialInterval(boss, 1f),
            0.0001f,
            "the second half must not be allowed to chain specials faster than the floor"
        );
    }

    @Test
    void aBossInItsFirstHalfKeepsTheCadenceTheGatesMeasured() {
        GameState state = new GameState();
        Boss boss = bossIn(state, 1f);
        assertEquals(2.4f, BossEvolution.specialInterval(boss, 2.4f), 0.0001f);
    }
}
