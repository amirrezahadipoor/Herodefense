package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

final class HollowNightTintTest {

    private static final float EPS = 1e-4f;

    /** Steps the tint until it has settled on whatever it is settling on (13s at 30fps, past the ease's tail). */
    private static void settle(HollowNightTint tint, GameState state) {
        for (int step = 0; step < 400; step++) {
            tint.tick(state, 1f / 30f);
        }
    }

    private static GameState hollowRunWithBoss(String body) {
        GameState state = GameState.newRun(5L);
        state.waveNumber = 150;
        Boss boss = new Boss(state.allocateEntityId(), body, 10f, 10f, 1);
        state.aliveBosses.add(boss);
        return state;
    }

    @Test
    void theNightWearsTheFaceOfTheBossItFaces() {
        GameState state = hollowRunWithBoss("EMBER_WYRM");
        HollowNightTint tint = new HollowNightTint();
        settle(tint, state);
        assertEquals(1f, tint.strength(), EPS, "a standing boss is fully the night's face");
        assertEquals(BossType.EMBER_WYRM.telegraphRed(), tint.red(), EPS,
            "the mist takes the wyrm's own ember colour");
        assertEquals(BossType.EMBER_WYRM.telegraphGreen(), tint.green(), EPS);
        assertEquals(BossType.EMBER_WYRM.telegraphBlue(), tint.blue(), EPS);
    }

    @Test
    void theFaceEasesRatherThanSnaps() {
        GameState state = hollowRunWithBoss("FROST_TITAN");
        HollowNightTint tint = new HollowNightTint();
        tint.tick(state, 1f / 30f);
        assertTrue(tint.strength() > 0f, "the night begins to accept the titan's face at once");
        assertTrue(tint.strength() < 1f, "and it takes time to wear it fully");
        assertTrue(tint.blue() > 0f && tint.blue() < BossType.FROST_TITAN.telegraphBlue(),
            "the frost colour creeps in over the frames");
    }

    @Test
    void theNightGivesTheFaceBackWhenTheBossFalls() {
        GameState state = hollowRunWithBoss("VOID_KNIGHT");
        HollowNightTint tint = new HollowNightTint();
        settle(tint, state);
        Boss boss = state.aliveBosses.get(0);
        boss.alive = false;
        settle(tint, state);
        assertEquals(0f, tint.strength(), EPS, "no boss stands, so the night is plain night again");
        assertEquals(BossType.VOID_KNIGHT.telegraphRed(), tint.red(), EPS,
            "the colour freezes at the last face; only the strength goes");
    }

    @Test
    void theForestArenaNeverWearsAFace() {
        GameState state = GameState.newRun(5L);
        state.waveNumber = 100; // the last wave of the forest, still the first arena
        Boss boss = new Boss(state.allocateEntityId(), "ANCIENT_GOLEM", 10f, 10f, 1);
        state.aliveBosses.add(boss);
        HollowNightTint tint = new HollowNightTint();
        settle(tint, state);
        assertEquals(0f, tint.strength(), EPS,
            "the Hollow's face lives in the second arena alone; the forest night keeps its own");
    }

    @Test
    void aBossHandingOverTheFightIsABlendNotACut() {
        GameState first = hollowRunWithBoss("THORN_MATRIARCH");
        HollowNightTint tint = new HollowNightTint();
        settle(tint, first);
        assertEquals(BossType.THORN_MATRIARCH.telegraphGreen(), tint.green(), EPS);

        GameState second = hollowRunWithBoss("EMBER_WYRM");
        for (int step = 0; step < 10; step++) {
            tint.tick(second, 1f / 30f);
        }
        assertTrue(tint.green() < BossType.THORN_MATRIARCH.telegraphGreen(),
            "the thorn green is already leaving the night");
        assertTrue(tint.red() > BossType.THORN_MATRIARCH.telegraphRed(),
            "and the wyrm's red is already in it");
        settle(tint, second);
        assertEquals(BossType.EMBER_WYRM.telegraphRed(), tint.red(), EPS,
            "and the blend lands on the new face, strength never having left the night");
        assertEquals(1f, tint.strength(), EPS);
    }

    @Test
    void aFrozenClockHoldsTheFaceStill() {
        GameState state = hollowRunWithBoss("SHADOW_LICH");
        HollowNightTint tint = new HollowNightTint();
        tint.tick(state, 1f / 30f);
        float heldStrength = tint.strength();
        float heldBlue = tint.blue();
        tint.tick(state, 0f); // reduced motion hands in a frozen clock
        assertEquals(heldStrength, tint.strength(), EPS, "a frozen clock changes nothing");
        assertEquals(heldBlue, tint.blue(), EPS);
    }

    @Test
    void aNewRunIsANewNight() {
        GameState state = hollowRunWithBoss("BLOODROOT_AVATAR");
        HollowNightTint tint = new HollowNightTint();
        settle(tint, state);
        tint.reset();
        assertEquals(0f, tint.strength(), EPS, "the previous run's boss goes with the previous run");
        assertEquals(0f, tint.red(), EPS);
        assertEquals(0f, tint.green(), EPS);
        assertEquals(0f, tint.blue(), EPS);
    }

    @Test
    void everyIdentityHasAFaceTheNightCanWear() {
        for (BossType body : BossType.values()) {
            GameState state = hollowRunWithBoss(body.name());
            HollowNightTint tint = new HollowNightTint();
            settle(tint, state);
            assertEquals(1f, tint.strength(), EPS, body.name() + " wears the night fully");
            assertEquals(body.telegraphRed(), tint.red(), EPS, body.name() + "'s red");
            assertEquals(body.telegraphGreen(), tint.green(), EPS, body.name() + "'s green");
            assertEquals(body.telegraphBlue(), tint.blue(), EPS, body.name() + "'s blue");
        }
    }
}
