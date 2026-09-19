package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/** A dying blightburst warns before it bursts; healthy ones and other Elites stay quiet. */
class BlightBurstWarnTest {

    private GameState stateWithBlightburst(float healthFraction) {
        GameState state = new GameState();
        Enemy elite = new Enemy(1L, "ROOTLING", 100f, 100f);
        elite.eliteAffix = "blightburst";
        elite.maxHealth = 100f;
        elite.health = elite.maxHealth * healthFraction;
        state.aliveEnemies.add(elite);
        return state;
    }

    @Test
    void dyingBlightburstRaisesTheWarn() {
        GameState state = stateWithBlightburst(0.30f);
        new EliteAffixSystem(null).update(state, 0.016f);
        assertTrue(state.aliveEnemies.get(0).blastWarned);
    }

    @Test
    void healthyBlightburstDoesNotWarn() {
        GameState state = stateWithBlightburst(0.90f);
        new EliteAffixSystem(null).update(state, 0.016f);
        assertFalse(state.aliveEnemies.get(0).blastWarned);
    }

    @Test
    void warnRaisesExactlyAtTheThreshold() {
        GameState state = stateWithBlightburst(
            EliteAffixSystem.BLIGHT_WARN_HEALTH_FRACTION);
        new EliteAffixSystem(null).update(state, 0.016f);
        assertTrue(state.aliveEnemies.get(0).blastWarned);
    }

    @Test
    void otherElitesNeverWarn() {
        GameState state = stateWithBlightburst(0.10f);
        state.aliveEnemies.get(0).eliteAffix = "gravemoss";
        new EliteAffixSystem(null).update(state, 0.016f);
        assertFalse(state.aliveEnemies.get(0).blastWarned);
    }
}
