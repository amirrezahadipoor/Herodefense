package com.amirrezahadipoor.herodefense.presentation;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.ascension.RootNetworkSystem;
import com.amirrezahadipoor.herodefense.audio.AudioFrame;
import com.amirrezahadipoor.herodefense.input.InventoryTouchController;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.HitStopSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.polish.TouchFeedbackSystem;
import com.amirrezahadipoor.herodefense.render.IdleWhisperRenderer;
import com.amirrezahadipoor.herodefense.settings.GameSettings;
import com.amirrezahadipoor.herodefense.shop.StatShopSystem;
import com.amirrezahadipoor.herodefense.skills.SkillShopSystem;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import com.amirrezahadipoor.herodefense.story.WhisperLines;

/**
 * One frame of the game, in order (roadmap R2.2, slice 9).
 *
 * <p>This is what {@code HeroDefenseGame.render()} used to be, plus the per-frame bookkeeping that lived beside
 * it: the wall-clock pause record behind the Long Pause secret, the two timed story lines (a wave reflection and
 * an idle whisper), the ambient clock the arena reads, and the game-over presentation timer. They are one object
 * now because they are one thing — the frame. The logic is moved, not re-decided: a whisper still holds the
 * simulation while it is up and a story line still does not, and the pause record still fires the Long Pause
 * secret once per absence.
 *
 * <p>The frame decides; the game still does. Simulation, cinematics and drawing come back through {@link Host},
 * and the systems the frame only ticks are handed in once at construction.
 */
public final class FrameDriver {

    /** What the frame needs from the game object it runs inside. */
    public interface Host {
        GameState gameState();

        void saveNow();

        void updatePlaying(float deltaSeconds);

        void updateCinematic(float deltaSeconds);

        void draw(float presentationDeltaSeconds);
    }

    /** How long a story line stays on screen; the HUD and the whisper renderer share it. */
    public static final float LINE_SECONDS = IdleWhisperRenderer.SHOW_SECONDS;

    /** The wall clock the pause record reads; injected so a test can hold a pause for five minutes at once. */
    public interface NanoClock {
        long nanos();
    }

    /** A pause at least this long earns the Long Pause secret's whisper. */
    static final float LONG_PAUSE_SECONDS = 300f;

    private static final float GAME_OVER_PRESENTATION_CAP_SECONDS = 10f;

    private final Host host;
    private final GameFlowController flow;
    private final AudioFrame audioManager;
    private final GameSettings settings;
    private final TouchFeedbackSystem touchFeedbackSystem;
    private final InventoryTouchController inventoryTouchController;
    private final StatShopSystem statShopSystem;
    private final SkillShopSystem skillShopSystem;
    private final RootNetworkSystem rootNetworkSystem;
    private final HitStopSystem hitStopSystem;
    private final ScreenShakeSystem screenShakeSystem;
    private final ParticleSystem particleSystem;
    private final CodexSystem codexSystem;
    private final NanoClock clock;

    private GameScreenState lastFrameState = GameScreenState.MENU;
    private long pauseStartNanos;
    private String whisperLine;
    private float whisperSeconds;
    private String storyBeatLine;
    private float storyBeatSeconds;
    private float ambientSeconds;
    private float gameOverPresentationSeconds;

    public FrameDriver(
        Host host,
        GameFlowController flow,
        AudioFrame audioManager,
        GameSettings settings,
        TouchFeedbackSystem touchFeedbackSystem,
        InventoryTouchController inventoryTouchController,
        StatShopSystem statShopSystem,
        SkillShopSystem skillShopSystem,
        RootNetworkSystem rootNetworkSystem,
        HitStopSystem hitStopSystem,
        ScreenShakeSystem screenShakeSystem,
        ParticleSystem particleSystem,
        CodexSystem codexSystem,
        NanoClock clock
    ) {
        this.host = host;
        this.flow = flow;
        this.audioManager = audioManager;
        this.settings = settings;
        this.touchFeedbackSystem = touchFeedbackSystem;
        this.inventoryTouchController = inventoryTouchController;
        this.statShopSystem = statShopSystem;
        this.skillShopSystem = skillShopSystem;
        this.rootNetworkSystem = rootNetworkSystem;
        this.hitStopSystem = hitStopSystem;
        this.screenShakeSystem = screenShakeSystem;
        this.particleSystem = particleSystem;
        this.codexSystem = codexSystem;
        this.clock = clock == null ? System::nanoTime : clock;
    }

    /** Advances and draws one frame; {@code deltaSeconds} is already clamped by the caller. */
    public void update(float deltaSeconds) {
        audioManager.update(settings);
        trackPauseDuration();
        audioManager.tick(deltaSeconds);
        touchFeedbackSystem.update(deltaSeconds);
        if (inventoryTouchController != null) inventoryTouchController.update(deltaSeconds);
        statShopSystem.update(deltaSeconds);
        skillShopSystem.update(deltaSeconds);
        if (rootNetworkSystem != null) rootNetworkSystem.update(deltaSeconds);
        if (flow.simulationRunning() && whisperLine == null) {
            float gameplayDelta = hitStopSystem.consume(deltaSeconds);
            if (gameplayDelta > 0f) host.updatePlaying(gameplayDelta);
        } else if (flow.state() == GameScreenState.CINEMATIC) {
            host.updateCinematic(deltaSeconds);
        }
        if (whisperLine != null && flow.state() == GameScreenState.PLAYING) {
            whisperSeconds += deltaSeconds;
            if (whisperSeconds >= LINE_SECONDS) whisperLine = null;
        }
        if (storyBeatLine != null && flow.state() == GameScreenState.PLAYING) {
            storyBeatSeconds += deltaSeconds;
            if (storyBeatSeconds >= LINE_SECONDS) storyBeatLine = null;
        }
        ambientSeconds += deltaSeconds;
        if (flow.state() == GameScreenState.GAME_OVER) {
            // Combat has stopped; let the final death, shockwave, and leaf motes settle.
            screenShakeSystem.update(deltaSeconds);
            particleSystem.update(deltaSeconds);
        }
        GameState state = host.gameState();
        if (flow.state() == GameScreenState.GAME_OVER && state != null && !state.runComplete) {
            gameOverPresentationSeconds = Math.min(
                GAME_OVER_PRESENTATION_CAP_SECONDS,
                gameOverPresentationSeconds + deltaSeconds
            );
        } else {
            gameOverPresentationSeconds = 0f;
        }
        host.draw(deltaSeconds);
    }

    /** Wall-clock pause lengths feed the Long Pause secret; a resume persists the record. */
    private void trackPauseDuration() {
        GameState state = host.gameState();
        if (state == null) {
            lastFrameState = flow.state();
            return;
        }
        GameScreenState now = flow.state();
        if (now == GameScreenState.PAUSED && lastFrameState != GameScreenState.PAUSED) {
            pauseStartNanos = clock.nanos();
        } else if (now != GameScreenState.PAUSED && lastFrameState == GameScreenState.PAUSED) {
            float seconds = (clock.nanos() - pauseStartNanos) / 1_000_000_000f;
            if (seconds > 0f) {
                state.longestPauseSeconds = Math.max(state.longestPauseSeconds, seconds);
                codexSystem.unlockSecretsForPause(state);
                if (whisperLine == null && seconds >= LONG_PAUSE_SECONDS && now == GameScreenState.PLAYING) {
                    String line = WhisperLines.firstUnused(state.usedWhisperIds);
                    if (line != null) {
                        whisperLine = line;
                        whisperSeconds = 0f;
                        WhisperLines.markUsed(state.usedWhisperIds, line);
                    }
                }
                host.saveNow();
            }
        }
        lastFrameState = now;
    }

    /** Shows a story line for {@link #LINE_SECONDS}, restarting its timer. */
    public void showStoryBeat(String line) {
        storyBeatLine = line;
        storyBeatSeconds = 0f;
    }

    public String storyBeatLine() {
        return storyBeatLine;
    }

    public float storyBeatSeconds() {
        return storyBeatSeconds;
    }

    public String whisperLine() {
        return whisperLine;
    }

    public float whisperSeconds() {
        return whisperSeconds;
    }

    public void setWhisperLine(String line) {
        whisperLine = line;
    }

    public void setStoryBeatLine(String line) {
        storyBeatLine = line;
    }

    public float ambientSeconds() {
        return ambientSeconds;
    }

    public float gameOverPresentationSeconds() {
        return gameOverPresentationSeconds;
    }

    /** Called when a run restarts: the game-over timer belongs to the frame, not to the run. */
    public void resetGameOverPresentation() {
        gameOverPresentationSeconds = 0f;
    }
}
