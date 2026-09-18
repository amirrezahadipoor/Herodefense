package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.presentation.RunPresentationSystem;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The wave director is the flow that used to sit at the end of {@code HeroDefenseGame.updatePlaying}
 * (roadmap R2.2 slice 4, tested per R2.3). These cases drive it through a recording host, so they assert the
 * decisions — which screen opens, when a save happens, which cue plays — without a libGDX context.
 */
final class WaveDirectorTest {

    private final RecordingHost host = new RecordingHost();
    private final RecordingAudio audio = new RecordingAudio();
    private final ParticleSystem particles = new ParticleSystem();
    private final ScreenShakeSystem shake = new ScreenShakeSystem();
    private final CodexSystem codex = new CodexSystem();
    private final RunPresentationSystem presentation = new RunPresentationSystem(
        particles, shake, codex, new RecordingBeats());
    private final WaveDirector director = new WaveDirector(
        host,
        new WaveLifecycleSystem(new EnemyWaveSpawner(new EnemyFactory()), new ContinuousWaveRun()),
        audio,
        particles,
        shake,
        presentation,
        codex
    );

    @Test
    void heroDeathFellsEveryTreeChoosesTheEpilogueAndSaves() {
        GameState state = GameState.newRun(11L);
        state.waveNumber = 12;
        state.peakWaveReached = 11;
        state.plantedTreesCount = 2;
        state.hero.alive = false;
        host.state = state;

        director.afterCombat(true, false);

        assertEquals(GameScreenState.GAME_OVER, host.lastScreen, "a death ends the run on the epilogue screen");
        assertEquals(1, host.saves, "the ending has to be persisted");
        assertTrue(state.heroDiedThisRun);
        assertEquals(12, state.peakWaveReached, "the wave the hero died on counts as reached");
        assertFalse(state.epilogueId.isEmpty(), "an epilogue is chosen from the run");
        assertFalse(particles.particles().isEmpty(), "the world tree and both grove trees fall");
        assertTrue(shake.active(), "the tree fall shakes the screen");
        assertEquals(1, host.records, "a run that ends writes exactly one session record (roadmap R3.6)");
    }

    @Test
    void aLevelUpOpensTheOverlayBeforeTheWaveAdvances() {
        GameState state = GameState.newRun(11L);
        state.waveNumber = 8;
        state.waveActive = false;
        host.state = state;

        director.afterCombat(false, true);

        assertEquals(GameScreenState.LEVEL_UP, host.lastScreen);
        assertEquals(1, host.saves);
        assertEquals(0, host.reflections, "the wave reflection waits until the player spends the points");
    }

    @Test
    void anOrdinaryFrameOnlyShowsTheReflection() {
        GameState state = GameState.newRun(11L);
        state.waveNumber = 4;
        state.waveActive = false;
        host.state = state;

        director.afterCombat(false, false);

        assertNull(host.lastScreen, "nothing opens when the wave did not complete");
        assertEquals(0, host.saves, "an idle frame does not write the save file");
        assertEquals(0, host.records, "and it writes no session record either");
        assertEquals(1, host.reflections, "the reflection line is offered every frame, as before");
    }

    /**
     * The other ending of a run — the vigil completed — reaches the game-over screen through the reward card of the
     * final boss wave, because both modes end on a boss wave (wave 200 and wave 30 are both multiples of five). The
     * director's own {@code RUN_COMPLETED} branch is the guard for a completion that arrives through the wave loop,
     * so it cannot be exercised from here with the real lifecycle; the router branch is the one that runs, and
     * {@code SessionRecordIntegrityTest} is what keeps both of them writing the session record.
     */
    @Test
    void theDirectorWritesNoRecordForARunThatIsStillRunning() {
        GameState state = GameState.newRun(11L);
        state.waveNumber = 19;
        state.waveActive = true;
        host.state = state;

        director.afterCombat(false, false);

        assertFalse(state.runComplete, "clearing wave 19 is not the end of a run");
        assertEquals(0, host.records, "and a run that has not ended leaves no session record");
    }

    @Test
    void clearingTheWaveBeforeABossWavePlaysTheEntranceCue() {
        GameState state = GameState.newRun(11L);
        state.waveNumber = 19;
        state.waveActive = true;
        host.state = state;

        director.afterCombat(false, false);

        assertEquals(1, host.reflections);
        assertEquals(1, host.saves, "a completed wave moves the run forward and persists it");
        assertTrue(
            audio.cues.contains(AudioCue.BOSS_ENTRANCE)
                || audio.cues.contains(AudioCue.BOSS_ENTRANCE_DEEP)
                || audio.cues.contains(AudioCue.BOSS_ENTRANCE_SHRIEK)
                || audio.cues.contains(AudioCue.BOSS_ENTRANCE_VOID),
            "wave 20 announces its boss in that body's own entrance voice (roadmap F2)");
        assertEquals(20, state.waveNumber, "the run rolled into the boss wave");
        assertEquals(0, host.ceremonies, "the planting ceremony is reserved for waves 50, 100 and 150");
    }

    /**
     * Wave 200 is a boss wave, so clearing it offers the reward card first; the run only completes once that card
     * is taken (that hand-off lives in the reward flow, and the director keeps its own RUN_COMPLETED guard for a
     * completion that arrives through the wave loop instead).
     */
    @Test
    void theFinalBossWaveOffersItsCardBeforeTheRunCanEnd() {
        GameState state = GameState.newRun(11L);
        state.waveNumber = 200;
        state.waveActive = true;
        host.state = state;

        director.afterCombat(false, false);

        assertEquals(GameScreenState.CARD_CHOICE, host.lastScreen);
        assertEquals(200, state.waveNumber, "the run waits at the final boss");
        assertFalse(state.runComplete, "the card is taken before the run is declared complete");
        assertEquals(1, host.saves);
    }

    /** Records what the director decided instead of rendering it. */
    private static final class RecordingHost implements WaveDirector.Host {
        private GameState state;
        private GameScreenState lastScreen;
        private int saves;
        private int reflections;
        private int ceremonies;
        private int records;

        @Override
        public GameState gameState() {
            return state;
        }

        @Override
        public void transitionTo(GameScreenState screen) {
            lastScreen = screen;
        }

        @Override
        public void saveNow() {
            saves++;
        }

        @Override
        public void showWaveReflection() {
            reflections++;
        }

        @Override
        public void beginPlantingCeremony() {
            ceremonies++;
        }

        @Override
        public void recordRunEnd() {
            records++;
        }
    }

    private static final class RecordingAudio implements AudioPlayback {
        private final List<AudioCue> cues = new ArrayList<>();

        @Override
        public void play(AudioCue cue) {
            cues.add(cue);
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
