package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.GameAudioManager;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.presentation.RunPresentationSystem;

/**
 * The two ceremonies a run is built around: the prologue that opens it and the planting of the grove trees every
 * fifty waves (roadmap R2.2).
 *
 * <p>Extracted from {@code HeroDefenseGame}, which used to own the state machine, the water-drop accumulator and
 * the wave hand-off inline. The layer ticks that run in every state (shake, particles, floating text) stay in the
 * game; this class owns only what a ceremony decides: which tree is being planted, when the prologue ends, when
 * the ceremony hands the run back to the wave loop, and which boss walks in at that moment.
 */
public final class CinematicFlow {

    /** What the ceremony needs from the game: the run state and a save point at the hand-off. */
    public interface Host {
        GameState gameState();

        void saveNow();
    }

    /** How often a pouring ceremony drops water from the can. */
    private static final float WATER_DROP_INTERVAL_SECONDS = 0.07f;

    private final Host host;
    private final GameFlowController flow;
    private final OpeningCinematic openingCinematic;
    private final PlantingCeremony plantingCeremony;
    private final WaveLifecycleSystem waveLifecycleSystem;
    private final ParticleSystem particleSystem;
    private final HeroAnimationController heroAnimationController;
    private final RunPresentationSystem presentationSystem;
    private final GameAudioManager audioManager;

    private float waterDropAccumulator;

    public CinematicFlow(
        Host host,
        GameFlowController flow,
        OpeningCinematic openingCinematic,
        PlantingCeremony plantingCeremony,
        WaveLifecycleSystem waveLifecycleSystem,
        ParticleSystem particleSystem,
        HeroAnimationController heroAnimationController,
        RunPresentationSystem presentationSystem,
        GameAudioManager audioManager
    ) {
        this.host = host;
        this.flow = flow;
        this.openingCinematic = openingCinematic;
        this.plantingCeremony = plantingCeremony;
        this.waveLifecycleSystem = waveLifecycleSystem;
        this.particleSystem = particleSystem;
        this.heroAnimationController = heroAnimationController;
        this.presentationSystem = presentationSystem;
        this.audioManager = audioManager;
    }

    /** Snapshots the run's opening tier, then plays that tier's lines. */
    public void beginOpening() {
        GameState state = host.gameState();
        state.openingTier = state.ascensionTier;
        openingCinematic.begin(state.openingTier);
    }

    /**
     * Starts the ceremony for the wave that just ended. Waves 50, 100 and 150 plant a named grove tree; every
     * other ceremony replays the shortest one.
     */
    public void beginPlantingCeremony() {
        GameState state = host.gameState();
        flow.transitionTo(GameScreenState.CINEMATIC);
        int groveIndex = Math.max(0, Math.min(2, state.plantedTreesCount));
        if (state.ceremonyPending) {
            int pendingWave = state.waveNumber - 1;
            if (pendingWave == 50) groveIndex = 0;
            else if (pendingWave == 100) groveIndex = 1;
            else if (pendingWave == 150) groveIndex = 2;
        }
        boolean shortCeremony = groveIndex == 0 || groveIndex == 2;
        plantingCeremony.begin(shortCeremony, groveIndex);
        state.anchorHeroAtArenaCenter();
    }

    /** Presentation-only ceremony tick; the wave-101 hand-off happens once it completes. */
    public void update(float deltaSeconds) {
        GameState state = host.gameState();
        if (openingCinematic.isActive()) {
            state.anchorHeroAtArenaCenter();
            heroAnimationController.update(state.hero, deltaSeconds);
            if (openingCinematic.update(deltaSeconds)) {
                waveLifecycleSystem.startCurrentWave(state);
                flow.transitionTo(GameScreenState.PLAYING);
                host.saveNow();
            }
            return;
        }
        boolean finished = plantingCeremony.update(deltaSeconds);
        if (plantingCeremony.pouring()) {
            waterDropAccumulator += deltaSeconds;
            while (waterDropAccumulator >= WATER_DROP_INTERVAL_SECONDS) {
                waterDropAccumulator -= WATER_DROP_INTERVAL_SECONDS;
                particleSystem.emitWaterDrops(
                    plantingCeremony.heroX() + 52f, plantingCeremony.heroY() + 58f
                );
            }
        } else {
            waterDropAccumulator = 0f;
        }
        if (finished) {
            int bossesBefore = ArenaQueries.livingBossCount(state);
            waveLifecycleSystem.completePlantingCeremony(state);
            flow.transitionTo(GameScreenState.PLAYING);
            if (ArenaQueries.livingBossCount(state) > bossesBefore) {
                audioManager.play(AudioCue.BOSS_ENTRANCE);
                presentationSystem.presentBossEntrance(state);
            }
            host.saveNow();
        }
    }
}
