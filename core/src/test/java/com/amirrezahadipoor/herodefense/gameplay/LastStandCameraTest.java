package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/** The last-stand lens: still at full strength, pushing in as the field thins. */
final class LastStandCameraTest {
    @Test
    void noWaveMeansNoZoom() {
        assertEquals(1f, LastStandCamera.zoomFor(GameState.newRun(21L)), 0.0001f);
        assertEquals(1f, LastStandCamera.zoomFor(null), 0.0001f);
    }

    @Test
    void aClearedFieldMeansNoZoom() {
        GameState state = fighting(22L, 10, 0);

        assertEquals(1f, LastStandCamera.zoomFor(state), 0.0001f);
    }

    @Test
    void aFullWaveMeansNoZoom() {
        GameState state = fighting(23L, 10, 10);

        assertEquals(1f, LastStandCamera.zoomFor(state), 0.0001f);
    }

    @Test
    void theLastStandPushesIn() {
        GameState state = fighting(24L, 10, 1);

        assertEquals(1f + 0.14f * 0.9f, LastStandCamera.zoomFor(state), 0.001f);
    }

    @Test
    void theLensNeverPushesPastItsClosest() {
        GameState state = fighting(25L, 10, 12);

        assertEquals(1f, LastStandCamera.zoomFor(state), 0.0001f);
    }

    /** A wave mid-fight: active, dealt {@code planned}, with {@code living} hostiles standing. */
    private static GameState fighting(long seed, int planned, int living) {
        GameState state = GameState.newRun(seed);
        state.waveActive = true;
        state.wavePlannedEnemies = planned;
        for (int index = 0; index < living; index++) {
            state.aliveEnemies.add(new Enemy());
        }
        return state;
    }
}
