package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.IdentityCues;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.presentation.RunPresentationSystem;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.story.Epilogue;

/**
 * Runs the wave lifecycle bookkeeping that follows a frame of combat: death and the tree falling, the level-up
 * screen, the wave advance, boss entrances, the wave reflection, the reward-card offer, the planting ceremony and
 * the completed run (roadmap R2.2).
 *
 * <p>Extracted verbatim from {@code HeroDefenseGame}: the class that used to own this block was 1,519 lines, and
 * the wave director is one of the five slices that bring it to the roadmap's 400-line ceiling. The director owns
 * the systems it drives and talks back to the game through {@link Host} for the four things only the game can do
 * (its state, the screen transition, saving, and the two ceremonies the game itself begins).
 */
public final class WaveDirector {

    /** What the director needs from the game object it runs inside. */
    public interface Host {
        GameState gameState();

        void transitionTo(GameScreenState screen);

        void saveNow();

        void showWaveReflection();

        void beginPlantingCeremony();

        /** Writes down the session that just ended, once per run (roadmap R3.6). */
        void recordRunEnd();
    }

    private final Host host;
    private final WaveLifecycleSystem waveLifecycleSystem;
    private final AudioPlayback audioManager;
    private final ParticleSystem particleSystem;
    private final ScreenShakeSystem screenShakeSystem;
    private final RunPresentationSystem presentationSystem;
    private final CodexSystem codexSystem;

    public WaveDirector(
        Host host,
        WaveLifecycleSystem waveLifecycleSystem,
        AudioPlayback audioManager,
        ParticleSystem particleSystem,
        ScreenShakeSystem screenShakeSystem,
        RunPresentationSystem presentationSystem,
        CodexSystem codexSystem
    ) {
        if (host == null) {
            throw new IllegalArgumentException("A host is required");
        }
        this.host = host;
        this.waveLifecycleSystem = waveLifecycleSystem;
        this.audioManager = audioManager;
        this.particleSystem = particleSystem;
        this.screenShakeSystem = screenShakeSystem;
        this.presentationSystem = presentationSystem;
        this.codexSystem = codexSystem;
    }

    /**
     * One frame's worth of post-combat flow. {@code gameOver} is the melee result of this frame and
     * {@code leveledUp} is whether the kill rewards opened a level-up.
     */
    public void afterCombat(boolean gameOver, boolean leveledUp) {
        if (gameOver) {
            // The two endings both write a session record before the save: a playtest that ends is exactly the
            // moment the numbers have to leave the process, and both paths funnel through the host so the record
            // cannot be forgotten by one of them (roadmap R3.6).
            GameState state = host.gameState();
            state.heroDiedThisRun = true;
            if (state.waveNumber > state.peakWaveReached) {
                state.peakWaveReached = state.waveNumber;
            }
            particleSystem.emitTreeDestruction(WorldLayout.WORLD_TREE_X, WorldLayout.WORLD_TREE_Y);
            for (int index = 0; index < state.plantedTreesCount; index++) {
                particleSystem.emitTreeDestruction(WorldLayout.groveTreeX(index), WorldLayout.groveTreeY(index));
            }
            screenShakeSystem.triggerTreeFall();
            // The Hollow speaks at hero fall, once per line, to the player (roadmap ST1).
            presentationSystem.presentHollowDeath(state);
            state.trophies.recordRunEnd(state.peakWaveReached, state.noPotionRun);
            state.epilogueId = Epilogue.select(state).name();
            host.transitionTo(GameScreenState.GAME_OVER);
            host.recordRunEnd();
            host.saveNow();
            return;
        }
        if (leveledUp && host.gameState().hero.alive) {
            host.transitionTo(GameScreenState.LEVEL_UP);
            host.saveNow();
            return;
        }

        GameState state = host.gameState();
        int bossesBeforeWaveAdvance = ArenaQueries.livingBossCount(state);
        WaveCompletion waveCompletion = waveLifecycleSystem.updateAfterCombat(state);
        if (ArenaQueries.livingBossCount(state) > bossesBeforeWaveAdvance) {
            audioManager.play(IdentityCues.bossEntranceFor(state));
            presentationSystem.presentBossEntrance(state);
        }
        host.showWaveReflection();
        if (waveCompletion == WaveCompletion.NO_CHANGE) {
            return;
        }
        // The wave is behind the player: the trophy ledger counts it even if the run ends here.
        audioManager.play(AudioCue.WAVE_CLEAR);
        state.trophies.recordWaveCleared();
        if (state.waveNumber > state.peakWaveReached) {
            state.peakWaveReached = state.waveNumber;
        }
        if (waveCompletion == WaveCompletion.BOSS_REWARD) {
            host.transitionTo(GameScreenState.CARD_CHOICE);
        } else if (waveCompletion == WaveCompletion.PLANTING_CEREMONY) {
            host.beginPlantingCeremony();
        } else if (waveCompletion == WaveCompletion.RUN_COMPLETED) {
            state.trophies.recordRunEnd(state.peakWaveReached, state.noPotionRun);
            state.runComplete = true;
            codexSystem.unlockSecretsForEquipment(state);
            if (state.waveNumber > state.peakWaveReached) {
                state.peakWaveReached = state.waveNumber;
            }
            state.epilogueId = Epilogue.select(state).name();
            host.transitionTo(GameScreenState.GAME_OVER);
            host.recordRunEnd();
        }
        host.saveNow();
    }
}
