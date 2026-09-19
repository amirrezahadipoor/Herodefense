package com.amirrezahadipoor.herodefense.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.ascension.RootNetworkSystem;
import com.amirrezahadipoor.herodefense.audio.AudioFrame;
import com.amirrezahadipoor.herodefense.audio.MusicBed;
import com.amirrezahadipoor.herodefense.input.InventoryTouchController;
import com.amirrezahadipoor.herodefense.gameplay.InventoryEquipmentSystem;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.HitStopSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.polish.TouchFeedbackSystem;
import com.amirrezahadipoor.herodefense.settings.GameSettings;
import com.amirrezahadipoor.herodefense.shop.StatShopSystem;
import com.amirrezahadipoor.herodefense.skills.SkillShopSystem;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import com.amirrezahadipoor.herodefense.story.WhisperLines;
import org.junit.jupiter.api.Test;

/**
 * The frame (roadmap R2.2 slice 9, tested per R2.3): which update path runs, how long a story line stays up, what
 * a long pause records, and how the game-over presentation timer behaves. Everything the driver ticks is a real
 * system here (they are all libGDX-free); the audio is a recorder and the clock is a counter, which is what makes
 * a five-minute pause testable in microseconds.
 */
final class FrameDriverTest {

    private final RecordingHost host = new RecordingHost();
    private final GameFlowController flow = new GameFlowController();
    private final RecordingAudio audio = new RecordingAudio();
    private final GameSettings settings = new GameSettings();
    private final HitStopSystem hitStop = new HitStopSystem();
    private final CodexSystem codex = new CodexSystem();
    private long nanos;

    private FrameDriver driver(boolean withInventory) {
        return new FrameDriver(
            host, flow, audio, settings, new TouchFeedbackSystem(),
            withInventory ? new InventoryTouchController(new InventoryEquipmentSystem()) : null,
            new StatShopSystem(), new SkillShopSystem(), new RootNetworkSystem(), hitStop,
            new ScreenShakeSystem(), new ParticleSystem(), codex, () -> nanos
        );
    }

    @Test
    void aPlayingFrameRunsTheSimulationOnceAndDrawsOnce() {
        FrameDriver driver = driver(true);
        host.state = GameState.newRun(21L);
        flow.transitionTo(GameScreenState.PLAYING);

        driver.update(0.05f);

        assertEquals(1, host.played, "the simulation advances once per frame");
        assertEquals(0, host.cinematics, "no prologue runs while the arena is live");
        assertEquals(1, host.draws, "and the frame still draws");
    }

    @Test
    void theFrameAsksForTheBedTheGameStateWantsAndDucksWhileThePlayerDecides() {
        FrameDriver driver = driver(false);
        host.state = GameState.newRun(21L);
        flow.transitionTo(GameScreenState.MENU);

        driver.update(0.05f);
        assertEquals(MusicBed.HEARTWOOD_DAWN, audio.bed, "the menu has its own bed");
        assertFalse(audio.ambience, "the vigil's own sound belongs to the arena, not to a menu");

        flow.transitionTo(GameScreenState.PLAYING);
        driver.update(0.05f);
        assertEquals(MusicBed.VIGIL, audio.bed, "the run has its own bed");
        assertEquals(1f, audio.gain);
        assertTrue(audio.ambience, "wind under a run is what makes the place a place");

        flow.transitionTo(GameScreenState.LEVEL_UP);
        driver.update(0.05f);
        assertEquals(MusicBed.VIGIL, audio.bed, "a level-up wall does not change the track, it lowers it");
        assertTrue(audio.gain < 1f, "the music steps back while the player chooses");
        assertEquals(3, audio.ticks, "the fade advances once per frame, with real time");
    }

    @Test
    void aCinematicFrameRunsThePrologueInsteadOfTheArena() {
        FrameDriver driver = driver(false);
        host.state = GameState.newRun(21L);
        // A ceremony is entered from the arena, which is the only way the shipped game reaches one.
        flow.transitionTo(GameScreenState.PLAYING);
        flow.transitionTo(GameScreenState.CINEMATIC);

        driver.update(0.05f);

        assertEquals(0, host.played, "the arena does not tick during a ceremony");
        assertEquals(1, host.cinematics);
        assertEquals(1, host.draws);
    }

    @Test
    void aWhisperHoldsTheSimulationUntilItsWindowIsOver() {
        FrameDriver driver = driver(false);
        host.state = GameState.newRun(21L);
        flow.transitionTo(GameScreenState.PLAYING);
        driver.setWhisperLine("the tree remembers");

        driver.update(0.5f);
        assertEquals(0, host.played, "the simulation waits while a whisper is on screen");

        driver.update(FrameDriver.LINE_SECONDS);
        assertEquals(0, host.played, "and stays held for the whole window");

        driver.update(0.01f);
        assertEquals(1, host.played, "and releases the arena the moment the line expires");
        assertNull(driver.whisperLine());
    }

    @Test
    void aStoryLineAlsoExpiresAndDoesNotHoldTheArena() {
        FrameDriver driver = driver(false);
        host.state = GameState.newRun(21L);
        flow.transitionTo(GameScreenState.PLAYING);

        driver.showStoryBeat("wave 7");
        assertEquals("wave 7", driver.storyBeatLine());
        driver.update(0.4f);

        assertEquals(1, host.played, "a story line is read while the fight continues");
        assertEquals("wave 7", driver.storyBeatLine(), "and it is still up inside its window");

        driver.update(FrameDriver.LINE_SECONDS);
        assertNull(driver.storyBeatLine(), "then it clears itself");
    }

    @Test
    void theGameOverTimerClimbsToItsCapAndResetsForTheNextRun() {
        FrameDriver driver = driver(false);
        GameState state = GameState.newRun(21L);
        host.state = state;
        flow.transitionTo(GameScreenState.PLAYING);
        flow.transitionTo(GameScreenState.GAME_OVER);

        for (int second = 0; second < 12; second++) {
            nanos += 1_000_000_000L;
            driver.update(1f);
        }
        assertEquals(10f, driver.gameOverPresentationSeconds(), 1e-4f, "the death screen settles, then holds");

        state.runComplete = true;
        nanos += 1_000_000_000L;
        driver.update(1f);
        assertEquals(0f, driver.gameOverPresentationSeconds(), 1e-4f,
            "a completed run slides away instead of holding the death beat");

        driver.resetGameOverPresentation();
        assertEquals(0f, driver.gameOverPresentationSeconds(), 1e-4f);
    }

    @Test
    void theDeathRevealRunsOnWallClockSoSlowFramesCannotStallIt() {
        FrameDriver driver = driver(false);
        host.state = GameState.newRun(21L);
        flow.transitionTo(GameScreenState.PLAYING);
        flow.transitionTo(GameScreenState.GAME_OVER);

        // Four frames at roughly four per second: clamped deltas would crawl (~0.27s here),
        // while the wall clock delivers the ~1.1s the defeat reveal needs to turn tappable.
        for (int frame = 0; frame < 5; frame++) {
            nanos += 250_000_000L;
            driver.update(1f / 15f);
        }

        assertTrue(driver.gameOverPresentationSeconds() >= 1f,
            "the restart gate opens in real time, not frame-clamped time");
    }

    @Test
    void aLongPauseIsRecordedAndEarnsItsWhisperOnlyOnce() {
        FrameDriver driver = driver(true);
        GameState state = GameState.newRun(21L);
        host.state = state;
        flow.transitionTo(GameScreenState.PLAYING);

        // Leave for six minutes and come back. The frame keeps running while paused, which is how the driver
        // sees the pause start at all.
        flow.transitionTo(GameScreenState.PAUSED);
        driver.update(0.02f);
        nanos += 6L * 60L * 1_000_000_000L;
        flow.transitionTo(GameScreenState.PLAYING);
        driver.update(0.02f);

        assertTrue(state.longestPauseSeconds >= 360f, "the wall-clock pause is recorded");
        assertNotNull(driver.whisperLine(), "a long absence earns a whisper");
        String first = driver.whisperLine();

        // A second long absence: the same line must not come back.
        driver.setWhisperLine(null);
        flow.transitionTo(GameScreenState.PAUSED);
        driver.update(0.02f);
        nanos += 6L * 60L * 1_000_000_000L;
        flow.transitionTo(GameScreenState.PLAYING);
        driver.update(0.02f);

        assertNotNull(driver.whisperLine());
        assertFalse(first.equals(driver.whisperLine()), "each long pause tells the player something new");
        assertTrue(Boolean.TRUE.equals(state.usedWhisperIds.get(WhisperLines.idFor(WhisperLines.LINES.indexOf(first)))),
            "and the line is marked as told");
    }

    /** Records what the frame decided to do instead of simulating and drawing it. */
    private static final class RecordingHost implements FrameDriver.Host {
        private GameState state;
        private int played;
        private int cinematics;
        private int draws;

        @Override
        public GameState gameState() {
            return state;
        }

        @Override
        public void saveNow() {
        }

        @Override
        public void updatePlaying(float deltaSeconds) {
            played++;
        }

        @Override
        public void updateCinematic(float deltaSeconds) {
            cinematics++;
        }

        @Override
        public void draw(float presentationDeltaSeconds) {
            draws++;
        }
    }

    private static final class RecordingAudio implements AudioFrame {
        private MusicBed bed;
        private float gain = -1f;
        private boolean ambience;
        private int ticks;

        @Override
        public void update(GameSettings settings) {
        }

        @Override
        public void guideMusic(MusicBed requested, float screenGain, boolean withAmbience) {
            bed = requested;
            gain = screenGain;
            ambience = withAmbience;
        }

        @Override
        public void onAudioFocus(com.amirrezahadipoor.herodefense.audio.AudioFocusState.Event event) {
        }

        @Override
        public void tick(float realDeltaSeconds) {
            ticks++;
        }
    }
}
