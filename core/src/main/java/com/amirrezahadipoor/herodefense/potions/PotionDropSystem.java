package com.amirrezahadipoor.herodefense.potions;

import com.amirrezahadipoor.herodefense.items.AffixEffects;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.DropEntity;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.trials.TrialEffects;

/**
 * Rolls a wave-weighted potion reward per defeat at roughly eight percent, raised by the
 * Potion Find affix.
 */
public final class PotionDropSystem {
    public static final float DROP_RATE = 0.08f;

    public int processDefeatedEnemies(GameState state) {
        if (state == null) return 0;
        int drops = 0;
        for (Enemy enemy : state.aliveEnemies) drops += rollOnce(state, enemy);
        for (Boss boss : state.aliveBosses) drops += rollOnce(state, boss);
        return drops;
    }

    public boolean isDrop(float roll) {
        if (roll < 0f || roll >= 1f || Float.isNaN(roll)) {
            throw new IllegalArgumentException("Potion roll must be in [0, 1)");
        }
        return roll < DROP_RATE;
    }

    /** Base rate scaled by the Potion Find affix; exactly {@link #DROP_RATE} without it. */
    public float effectiveDropRate(GameState state) {
        return DROP_RATE * AffixEffects.potionFindMultiplier(state);
    }

    /** Unlocks stronger tiers over the run, with higher unlocked tiers weighted more. */
    public PotionTier tierForRoll(int waveNumber, float roll) {
        if (roll < 0f || roll >= 1f || Float.isNaN(roll)) {
            throw new IllegalArgumentException("Tier roll must be in [0, 1)");
        }
        int wave = Math.max(1, Math.min(GameState.FINAL_WAVE, waveNumber));
        int unlocked = Math.min(PotionTier.values().length, 1 + (wave - 1) / 17);
        int totalWeight = unlocked * (unlocked + 1) / 2;
        float cursor = roll * totalWeight;
        for (int index = 0; index < unlocked; index++) {
            cursor -= index + 1;
            if (cursor < 0f) return PotionTier.values()[index];
        }
        return PotionTier.values()[unlocked - 1];
    }

    private int rollOnce(GameState state, Enemy enemy) {
        if (enemy == null || enemy.alive || enemy.potionDropRolled) return 0;
        enemy.potionDropRolled = true;
        if (enemy.silentWatcher) return 0;
        if (!TrialEffects.potionsDrop(state.activeTrials)) return 0;
        // One draw, compared against the Potion Find-scaled rate; without the affix this is
        // bit-identical to the old isDrop check, so seeded runs do not shift.
        if (state.nextCombatRandomFloat() >= effectiveDropRate(state)) return 0;
        PotionTier tier = tierForRoll(state.waveNumber, state.nextCombatRandomFloat());
        DropEntity drop = new DropEntity(
            state.allocateEntityId(), "POTION", enemy.x, enemy.y, 1
        );
        drop.itemId = tier.name();
        drop.pickupDelaySeconds = 2.6f;
        state.drops.add(drop);
        return 1;
    }
}
