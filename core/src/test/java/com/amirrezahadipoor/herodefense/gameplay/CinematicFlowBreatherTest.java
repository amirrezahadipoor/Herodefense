package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.presentation.RunPresentationSystem;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The milestone breather after a cleared milestone wave: Pip's one-line beat in the box while
 * the arena holds its breath, then the next wave spawns. Driven through a recording host.
 */
final class CinematicFlowBreatherTest {

    private final RecordingHost host = new RecordingHost();
    private final GameFlowController flow = new GameFlowController();
    private final OpeningCinematic opening = new OpeningCinematic();
    private final PlantingCeremony planting = new PlantingCeremony();
    private final BossIntroCinematic bossIntro = new BossIntroCinematic();
    private final ParticleSystem particles = new ParticleSystem();
    private final ScreenShakeSystem shake = new ScreenShakeSystem();
    private final List<AudioCue> played = new ArrayList<>();
    private final RunPresentationSystem presentation = new RunPresentationSystem(
        particles, shake, new CodexSystem(), new RecordingBeats()
    );
    private final WaveLifecycleSystem waves =
        new WaveLifecycleSystem(new EnemyWaveSpawner(new EnemyFactory()), new ContinuousWaveRun());
    private final CinematicFlow cinematic = new CinematicFlow(
        host, flow, opening, planting, waves, particles, new HeroAnimationController(),
        presentation, played::add, bossIntro, shake
    );

    @Test
    void beginOpensTheCinematicWithPipsLineInTheBox() {
        host.state = breatherState(41L);

        cinematic.beginBreather();

        assertEquals(GameScreenState.CINEMATIC, flow.state());
        assertTrue(cinematic.breatherCinematic().isActive());
        assertEquals(25, cinematic.breatherCinematic().waveNumber());

        cinematic.update(0.05f);
        assertEquals("Twenty-five nights! Pip counted!", cinematic.dialogue().text());
    }

    @Test
    void tickingPastThePauseSpawnsTheNextWave() {
        GameState state = breatherState(42L);
        host.state = state;
        cinematic.beginBreather();

        tickUntilDone();

        assertEquals(GameScreenState.PLAYING, flow.state());
        assertFalse(state.breatherPending);
        assertTrue(state.waveActive);
        assertEquals(26, state.waveNumber);
        assertTrue(state.livingEnemyCount() > 0);
        assertEquals(1, host.saves);
        assertEquals(1, host.reflections);
    }

    @Test
    void skipFinishesTheBeatEarly() {
        GameState state = breatherState(43L);
        host.state = state;
        cinematic.beginBreather();
        cinematic.breatherCinematic().skip();

        cinematic.update(0.05f);

        assertEquals(GameScreenState.PLAYING, flow.state());
        assertTrue(state.waveActive);
        assertEquals(26, state.waveNumber);
    }

    @Test
    void aWaveWithoutABreathBreathesSilently() {
        GameState state = breatherState(44L);
        state.breatherWave = 6;
        host.state = state;
        cinematic.beginBreather();

        cinematic.update(0.05f);
        assertFalse(cinematic.dialogue().active(), "no line, no box");
        tickUntilDone();

        assertEquals(GameScreenState.PLAYING, flow.state());
        assertTrue(state.waveActive);
    }

    /** A state that cleared wave 25 and waits on Pip's beat before wave 26 spawns. */
    private static GameState breatherState(long seed) {
        GameState state = GameState.newRun(seed);
        state.waveNumber = 26;
        state.breatherPending = true;
        state.breatherWave = 25;
        return state;
    }

    private void tickUntilDone() {
        for (int frame = 0; frame < 60 && cinematic.breatherCinematic().isActive(); frame++) {
            cinematic.update(0.05f);
        }
    }

    private static final class RecordingHost implements CinematicFlow.Host {
        private GameState state;
        private int reflections;
        private int saves;

        @Override
        public GameState gameState() {
            return state;
        }

        @Override
        public void showWaveReflection() {
            reflections++;
        }

        @Override
        public void saveNow() {
            saves++;
        }
    }

    private static final class RecordingBeats implements RunPresentationSystem.BeatSink {
        @Override
        public void showBeat(String line) {
        }

        @Override
        public void save() {
        }
    }
}
