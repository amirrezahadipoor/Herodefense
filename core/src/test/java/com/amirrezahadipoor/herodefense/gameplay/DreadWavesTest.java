package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.EliteAffix;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/** Dread stands while a boss lives or an elite holds the field -- and only then. */
final class DreadWavesTest {
    @Test
    void aLivingBossIsDread() {
        GameState state = fighting(31L);
        state.aliveBosses.add(new Boss());

        assertTrue(DreadWaves.dreadStands(state));
        assertEquals(1f, DreadWaves.dreadLevel(state), 0.0001f);
    }

    @Test
    void aFallenBossIsNoDread() {
        GameState state = fighting(32L);
        Boss fallen = new Boss();
        fallen.alive = false;
        state.aliveBosses.add(fallen);

        assertFalse(DreadWaves.dreadStands(state));
        assertEquals(0f, DreadWaves.dreadLevel(state), 0.0001f);
    }

    @Test
    void aStandingEliteIsDread() {
        GameState state = fighting(33L);
        Enemy elite = new Enemy();
        elite.eliteAffix = EliteAffix.BLIGHTBURST.id();
        state.aliveEnemies.add(elite);

        assertTrue(DreadWaves.dreadStands(state));
        assertEquals(1f, DreadWaves.dreadLevel(state), 0.0001f);
    }

    @Test
    void regularsAloneAreNoDread() {
        GameState state = fighting(34L);
        state.aliveEnemies.add(new Enemy());

        assertFalse(DreadWaves.dreadStands(state));
        assertEquals(0f, DreadWaves.dreadLevel(state), 0.0001f);
    }

    @Test
    void noWaveMeansNoDread() {
        assertFalse(DreadWaves.dreadStands(GameState.newRun(35L)));
        assertFalse(DreadWaves.dreadStands(null));
        assertEquals(0f, DreadWaves.dreadLevel(GameState.newRun(36L)), 0.0001f);
        assertEquals(0f, DreadWaves.dreadLevel(null), 0.0001f);
    }

    /** A wave mid-fight with an empty field, ready for whatever stands in it. */
    private static GameState fighting(long seed) {
        GameState state = GameState.newRun(seed);
        state.waveActive = true;
        state.wavePlannedEnemies = 10;
        return state;
    }
}
