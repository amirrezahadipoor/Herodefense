package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class BossFactoryTest {
    @Test
    void catalogContainsEightDistinctDesignAndAttackIdentities() {
        GameState state = GameState.newRun(9L);
        BossFactory factory = new BossFactory();
        Set<String> assets = new HashSet<>();
        Set<String> attacks = new HashSet<>();

        int number = 1;
        for (BossType type : BossType.values()) {
            Boss boss = factory.create(state, type, 1f, 2f, number++, 0);
            assertEquals(type, boss.bossDefinition());
            assertEquals(type.uniqueAttack(), boss.uniqueAttack);
            assets.add(type.assetKey());
            attacks.add(type.uniqueAttack());
        }

        assertEquals(8, BossType.values().length);
        assertEquals(8, assets.size());
        assertEquals(8, attacks.size());
        assertNotEquals(BossType.ANCIENT_GOLEM.assetKey(), BossType.EMBER_WYRM.assetKey());
    }

    @Test
    void theEncounterScriptIsStampedOnTheBossAndScalesItsStats() {
        GameState state = GameState.newRun(10L);
        BossFactory factory = new BossFactory();

        Boss first = factory.create(state, BossType.ANCIENT_GOLEM, 0f, 0f, 1, 0);
        assertEquals(BossFightScript.MEASURED.name(), first.fightScript);
        assertEquals(BossType.ANCIENT_GOLEM.movementSpeed(), first.movementSpeed, 1e-5f);
        assertEquals(BossType.ANCIENT_GOLEM.attackRange(), first.attackRange, 1e-5f);

        Boss assassin = factory.create(state, BossType.ANCIENT_GOLEM, 0f, 0f, 8, 0);
        assertEquals(BossFightScript.ASSASSIN.name(), assassin.fightScript);
        assertEquals(BossType.ANCIENT_GOLEM.movementSpeed() * BossFightScript.ASSASSIN.movementMultiplier(),
            assassin.movementSpeed, 1e-5f);
        assertEquals(BossType.ANCIENT_GOLEM.attackRange() * BossFightScript.ASSASSIN.attackRangeMultiplier(),
            assassin.attackRange, 1e-5f);
        assertEquals(BossType.ANCIENT_GOLEM.attackIntervalSeconds(), assassin.attackIntervalSeconds, 1e-5f,
            "the basic attack keeps the identity's cadence; only the special changes tempo");

        Boss warden = factory.create(state, BossType.ANCIENT_GOLEM, 0f, 0f, 5, 0);
        assertEquals(BossFightScript.PATIENT_WARDEN.name(), warden.fightScript);
        // The physics of a boss is the identity's own on purpose (see BossFightScript): two encounters of the
        // same identity fight differently because of the *warning and the shape of the hit*, not because the
        // sweep would have to absorb script-specific legs and reach.
        assertEquals(BossType.ANCIENT_GOLEM.attackRange(), warden.attackRange, 1e-5f);
        assertEquals(BossType.ANCIENT_GOLEM.movementSpeed(), warden.movementSpeed, 1e-5f);
        assertNotEquals(first.fightScript, warden.fightScript,
            "two encounters of the same identity must not fight identically");
        assertNotEquals(BossFightScript.PATIENT_WARDEN.telegraphScale(),
            BossFightScript.MEASURED.telegraphScale(), "and the tell the player reads has to be what changes");
    }
}
