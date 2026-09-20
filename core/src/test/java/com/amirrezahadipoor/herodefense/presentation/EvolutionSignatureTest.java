package com.amirrezahadipoor.herodefense.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleType;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import org.junit.jupiter.api.Test;

/** A boss's evolution ring fires exactly once, the frame it first crosses the enrage threshold. */
class EvolutionSignatureTest {

    private final ParticleSystem particles = new ParticleSystem();
    private final RunPresentationSystem presentation = new RunPresentationSystem(
        particles, new ScreenShakeSystem(), new CodexSystem(), new RunPresentationSystem.BeatSink() {
            @Override public void showBeat(String line) { }
            @Override public void save() { }
        });

    private GameState stateWithWoundedBoss() {
        GameState state = GameState.newRun(40L);
        Boss boss = new Boss(state.allocateEntityId(), "ANCIENT_GOLEM", 300f, 400f, 1);
        boss.maxHealth = 1000f;
        boss.health = 1f;
        state.aliveBosses.add(boss);
        return state;
    }

    @Test
    void crossingTheThresholdFiresOneRingAndThenNeverAgain() {
        GameState state = stateWithWoundedBoss();
        presentation.presentPlaytime(state);
        assertTrue(state.aliveBosses.get(0).evolutionPresented);
        long rings = particles.particles().stream()
            .filter(particle -> particle.type == ParticleType.EVOLUTION_RING).count();
        assertEquals(1, rings);

        presentation.presentPlaytime(state);
        presentation.presentPlaytime(state);
        rings = particles.particles().stream()
            .filter(particle -> particle.type == ParticleType.EVOLUTION_RING).count();
        assertEquals(1, rings);
    }

    @Test
    void aHealthyBossHasNotEvolvedYet() {
        GameState state = GameState.newRun(41L);
        Boss boss = new Boss(state.allocateEntityId(), "ANCIENT_GOLEM", 300f, 400f, 1);
        boss.maxHealth = 1000f;
        boss.health = 1000f;
        state.aliveBosses.add(boss);
        presentation.presentPlaytime(state);
        assertFalse(boss.evolutionPresented);
    }
}
