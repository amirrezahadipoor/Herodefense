package com.amirrezahadipoor.herodefense.presentation;

import com.amirrezahadipoor.herodefense.input.HapticFeedback;
import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.ascension.RootNetworkSystem;
import com.amirrezahadipoor.herodefense.audio.AudioFrame;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.audio.SpeechVoice;
import com.amirrezahadipoor.herodefense.audio.MusicBed;
import com.amirrezahadipoor.herodefense.audio.MusicSelectionPolicy;
import com.amirrezahadipoor.herodefense.gameplay.ArenaQueries;
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
 * it: the wall-clock pause record behind the Long Pause secret, the message box (a wave reflection, a boss
 * beat, an idle whisper), the ambient clock the arena reads, and the game-over presentation timer. They are
 * one object now because they are one thing — the frame. The logic is moved, not re-decided: a whisper still
 * holds the simulation while it is up, and now a story line does too -- the page stays still for the whole
 * message, the way Undertale waits for its dialog -- and the pause record still fires the Long Pause secret
 * once per absence.
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

        /** The device's hands (roadmap F4); a default because a host without them is a silence, not a crash. */
        default HapticFeedback hapticFeedback() {
            return null;
        }
    }

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
    private HapticRunWatcher hapticWatcher;

    private GameScreenState lastFrameState = GameScreenState.MENU;
    private long pauseStartNanos;
    private long lastFrameNanos;
    /**
     * The message box (roadmap ST-voice): beats and whispers are both lines in one Undertale-style box,
     * typed out under the speaker's blips. The frame keeps the beat and the whisper apart by source.
     */
    private final DialogueBox dialogue;
    private float ambientSeconds;
    private float gameOverPresentationSeconds;

    public FrameDriver(
        Host host,
        GameFlowController flow,
        AudioFrame audioManager,
        AudioPlayback playback,
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
        this.dialogue = new DialogueBox(playback);
    }

    /** The frame's message box: what a beat or whisper is typing out, in whose voice. */
    public DialogueBox storyDialogue() {
        return dialogue;
    }

    /** Whether a message is on the screen; a tap goes to the box, not the HUD underneath. */
    public boolean storyDialogueActive() {
        return dialogue.active();
    }

    /** A tap on the box: the first finishes the typing, the next closes it. */
    public void advanceStoryDialogue() {
        dialogue.advance();
    }

    /**
     * The run's transitions become haptics (roadmap F4), gated by the effects toggle because a phone that
     * is silenced should also be stilled.
     */
    private void watchHaptics(float deltaSeconds) {
        if (settings == null || !settings.soundEnabled) return;
        if (hapticWatcher == null) {
            hapticWatcher = new HapticRunWatcher(host.hapticFeedback());
        }
        hapticWatcher.watch(flow.state(), host.gameState(), deltaSeconds);
    }

    /** The music follows the flow (roadmap R6.3): the bed comes from the policy, never from a call site. */
    private void guideMusic() {
        GameScreenState state = flow.state();
        boolean bossFight = ArenaQueries.livingBossCount(host.gameState()) > 0;
        audioManager.guideMusic(
            MusicSelectionPolicy.bedFor(state, bossFight),
            MusicSelectionPolicy.gainFor(state),
            MusicSelectionPolicy.ambienceFor(state)
        );
        // Roadmap F1: the vigil's intensity layer rides the run's own pressure, not the screen's.
        audioManager.guideTension(MusicSelectionPolicy.tensionFor(state, host.gameState()));
    }

    /** Advances and draws one frame; {@code deltaSeconds} is already clamped by the caller. */
    public void update(float deltaSeconds) {
        long now = clock.nanos();
        // The death reveal is a real-time animation: slow frames must not stretch it, so it reads the
        // wall clock and a long stall lands it at its end state like any wall-clock animation.
        float wallDelta = lastFrameNanos == 0L
            ? 0f : Math.max(0f, (now - lastFrameNanos) / 1_000_000_000f);
        lastFrameNanos = now;
        audioManager.update(settings);
        trackPauseDuration();
        guideMusic();
        watchHaptics(deltaSeconds);
        audioManager.tick(deltaSeconds);
        touchFeedbackSystem.update(deltaSeconds);
        if (inventoryTouchController != null) inventoryTouchController.update(deltaSeconds);
        statShopSystem.update(deltaSeconds);
        skillShopSystem.update(deltaSeconds);
        if (rootNetworkSystem != null) rootNetworkSystem.update(deltaSeconds);
        // The message holds the arena for its whole life -- typing and reading both -- the way Undertale
        // waits for its dialog. The box keeps running during a ceremony, but it may only close itself on
        // the arena, where a closed box is what lets the fight resume.
        GameScreenState frameState = flow.state();
        if (frameState == GameScreenState.PLAYING || frameState == GameScreenState.CINEMATIC) {
            dialogue.tick(deltaSeconds, frameState == GameScreenState.PLAYING);
        }
        if (flow.simulationRunning() && !dialogue.active()) {
            float gameplayDelta = hitStopSystem.consume(deltaSeconds);
            if (gameplayDelta > 0f) host.updatePlaying(gameplayDelta);
        } else if (frameState == GameScreenState.CINEMATIC) {
            host.updateCinematic(deltaSeconds);
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
                gameOverPresentationSeconds + wallDelta
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
                    if (!dialogue.active() && seconds >= LONG_PAUSE_SECONDS && now == GameScreenState.PLAYING) {
                    String line = WhisperLines.firstUnused(state.usedWhisperIds);
                    if (line != null) {
                        setWhisperLine(line);
                        WhisperLines.markUsed(state.usedWhisperIds, line);
                    }
                }
                host.saveNow();
            }
        }
        lastFrameState = now;
    }

    /** Shows a story line in the Warden's voice; the box types it out under his blips. */
    public void showStoryBeat(String line) {
        showStoryBeat(line, SpeechVoice.HERO);
    }

    /**
     * Shows a story line in a specific speaker's tone (roadmap ST-voice): the line types out in the box and
     * its blips tap in the speaker's voice, and the arena waits for the whole reading. Non-blank only,
     * exactly like the plain {@link #showStoryBeat(String)}.
     */
    public void showStoryBeat(String line, SpeechVoice voice) {
        dialogue.speak(line, voice, DialogueBox.Source.BEAT);
    }

    /** The beat's line while it is up, or null; a whisper up instead reads as null, as before. */
    public String storyBeatLine() {
        return dialogue.active() && dialogue.source() == DialogueBox.Source.BEAT ? dialogue.text() : null;
    }

    /** Seconds the beat has been up; the box's own clock, kept for the readers that measure it. */
    public float storyBeatSeconds() {
        return dialogue.active() && dialogue.source() == DialogueBox.Source.BEAT ? dialogue.seconds() : 0f;
    }

    public String whisperLine() {
        return dialogue.active() && dialogue.source() == DialogueBox.Source.WHISPER ? dialogue.text() : null;
    }

    public float whisperSeconds() {
        return dialogue.active() && dialogue.source() == DialogueBox.Source.WHISPER ? dialogue.seconds() : 0f;
    }

    public void setWhisperLine(String line) {
        if (line == null || line.isBlank()) {
            if (dialogue.active() && dialogue.source() == DialogueBox.Source.WHISPER) {
                dialogue.clear();
            }
            return;
        }
        dialogue.speak(line, SpeechVoice.TREE, DialogueBox.Source.WHISPER);
    }

    public void setStoryBeatLine(String line) {
        if (line == null || line.isBlank()) {
            if (dialogue.active() && dialogue.source() == DialogueBox.Source.BEAT) {
                dialogue.clear();
            }
            return;
        }
        dialogue.speak(line, SpeechVoice.HERO, DialogueBox.Source.BEAT);
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
