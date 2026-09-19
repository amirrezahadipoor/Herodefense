package com.amirrezahadipoor.herodefense.rewards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.Item;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

final class BossRewardCardSystemTest {
    private final BossRewardCardSystem rewards = new BossRewardCardSystem();

    @Test
    void preparesExactlyThreeUniquePersistedChoices() {
        GameState state = GameState.newRun(51L);
        rewards.prepareChoices(state, 1);

        assertTrue(state.awaitingBossReward);
        assertEquals(1, state.pendingRewardBossNumber);
        assertEquals(3, state.pendingRewardCards.size());
        assertEquals(3, new HashSet<>(state.pendingRewardCards).size());

        var original = new java.util.ArrayList<>(state.pendingRewardCards);
        rewards.prepareChoices(state, 1);
        assertEquals(original, state.pendingRewardCards);
    }

    @Test
    void tappedChoiceAppliesImmediatelyPersistsAndClearsPauseState() {
        GameState state = GameState.newRun(52L);
        state.awaitingBossReward = true;
        state.pendingRewardBossNumber = 3;
        state.pendingRewardCards.add("HEALTH");
        state.pendingRewardCards.add("GENERAL_POWER");
        state.pendingRewardCards.add("LIFESTEAL");
        state.hero.health = 50f;

        assertTrue(rewards.chooseCard(state, 0));

        assertEquals(1, state.hero.stats.health);
        assertEquals(110f, state.hero.maxHealth);
        assertEquals(60f, state.hero.health);
        assertEquals("HEALTH", state.chosenRewardCards.get("3"));
        assertTrue(state.pendingRewardCards.isEmpty());
        assertTrue(!state.awaitingBossReward);
    }

    @Test
    void healthCardKeepsEquippedGearBonuses() {
        GameState state = GameState.newRun(54L);
        Item gear = new Item("test_charm", "Test Charm", "WEAPON", "COMMON");
        gear.statBonuses.put(com.amirrezahadipoor.herodefense.model.HeroStat.HEALTH.name(), 30f);
        state.equippedItems.put(
            com.amirrezahadipoor.herodefense.model.EquipmentSlot.WEAPON.name(), gear);
        state.synchronizeEquipmentHealth();
        state.hero.health = 50f;
        state.awaitingBossReward = true;
        state.pendingRewardBossNumber = 3;
        state.pendingRewardCards.add("HEALTH");
        state.pendingRewardCards.add("GENERAL_POWER");
        state.pendingRewardCards.add("LIFESTEAL");

        assertTrue(rewards.chooseCard(state, 0));

        assertEquals(1, state.hero.stats.health);
        assertEquals(410f, state.hero.maxHealth, "the gear bonus survives the card");
        assertEquals(60f, state.hero.health);
    }

    @Test
    void additiveEffectsPersistInPermanentEffectMap() {
        GameState state = GameState.newRun(53L);
        state.awaitingBossReward = true;
        state.pendingRewardBossNumber = 4;
        state.pendingRewardCards.add("GENERAL_POWER");
        state.pendingRewardCards.add("COIN_INCOME");
        state.pendingRewardCards.add("LIFESTEAL");

        assertTrue(rewards.chooseCard(state, 1));
        assertEquals(
            new RewardPowerBudget().magnitude(RewardCardId.COIN_INCOME, 4),
            state.permanentEffects.get(BossRewardCardSystem.COIN_INCOME_KEY).floatValue(),
            0.0001f
        );
        assertEquals("COIN_INCOME", state.chosenRewardCards.get("4"));
    }
}
