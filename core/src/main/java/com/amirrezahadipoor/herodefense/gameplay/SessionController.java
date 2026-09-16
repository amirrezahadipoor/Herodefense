package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.ascension.RootNetworkSystem;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.GameAudioManager;
import com.amirrezahadipoor.herodefense.items.StarterLoadoutSystem;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.FloatingCoinTextSystem;
import com.amirrezahadipoor.herodefense.polish.FloatingDamageTextSystem;
import com.amirrezahadipoor.herodefense.polish.HitStopSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.presentation.RunPresentationSystem;
import com.amirrezahadipoor.herodefense.save.LocalSaveRepository;
import com.amirrezahadipoor.herodefense.trials.TrialDraftSystem;

/**
 * The run session: starting a run, restarting at the same tier, ascending, and picking a saved run back up
 * (roadmap R2.2).
 *
 * <p>Extracted from {@code HeroDefenseGame}. The three ways to begin a run used to carry three copies of the same
 * fifteen-line reset, which is exactly how a god class drifts: change the reset once and two paths keep the old
 * behaviour. They now share one {@code prepareFreshRun()}, and a test can start a run without a libGDX context.
 */
public final class SessionController {

    /** What the session needs from the game: its state object, its clock, saving and two screen hand-offs. */
    public interface Host {
        GameState gameState();

        void setGameState(GameState state);

        /** Zeroes the simulation clock and the game-over presentation timer together. */
        void resetRunClock();

        void saveNow();

        boolean continueAvailable();

        void showWaveReflection();

        void beginPlantingCeremony();
    }

    private final Host host;
    private final LocalSaveRepository saves;
    private final GameFlowController flow;
    private final StarterLoadoutSystem starterLoadoutSystem;
    private final RootNetworkSystem rootNetworkSystem;
    private final TrialDraftSystem trialDraftSystem;
    private final HitStopSystem hitStopSystem;
    private final ParticleSystem particleSystem;
    private final FloatingCoinTextSystem floatingCoinTextSystem;
    private final FloatingDamageTextSystem floatingDamageTextSystem;
    private final WaveLifecycleSystem waveLifecycleSystem;
    private final RunPresentationSystem presentationSystem;
    private final OpeningCinematic openingCinematic;
    private final GameAudioManager audioManager;

    public SessionController(
        Host host,
        LocalSaveRepository saves,
        GameFlowController flow,
        StarterLoadoutSystem starterLoadoutSystem,
        RootNetworkSystem rootNetworkSystem,
        TrialDraftSystem trialDraftSystem,
        HitStopSystem hitStopSystem,
        ParticleSystem particleSystem,
        FloatingCoinTextSystem floatingCoinTextSystem,
        FloatingDamageTextSystem floatingDamageTextSystem,
        WaveLifecycleSystem waveLifecycleSystem,
        RunPresentationSystem presentationSystem,
        OpeningCinematic openingCinematic,
        GameAudioManager audioManager
    ) {
        this.host = host;
        this.saves = saves;
        this.flow = flow;
        this.starterLoadoutSystem = starterLoadoutSystem;
        this.rootNetworkSystem = rootNetworkSystem;
        this.trialDraftSystem = trialDraftSystem;
        this.hitStopSystem = hitStopSystem;
        this.particleSystem = particleSystem;
        this.floatingCoinTextSystem = floatingCoinTextSystem;
        this.floatingDamageTextSystem = floatingDamageTextSystem;
        this.waveLifecycleSystem = waveLifecycleSystem;
        this.presentationSystem = presentationSystem;
        this.openingCinematic = openingCinematic;
        this.audioManager = audioManager;
    }

    /** A brand-new run: the saved run is discarded, so the tier ladder starts over. */
    public void startNewRun() {
        saves.clear();
        host.setGameState(GameState.newRun(System.currentTimeMillis()));
        prepareFreshRun();
    }

    /** The death screen's "again": a new seed on the tier the player already reached. */
    public void startNewRunSameTier() {
        if (host.gameState() == null) {
            startNewRun();
            return;
        }
        host.gameState().resetForNewRun(System.currentTimeMillis());
        prepareFreshRun();
    }

    /** Awards heartwood, climbs one tier, and starts the new run from it. */
    public void ascendRun() {
        if (host.gameState() == null) {
            startNewRun();
            return;
        }
        host.gameState().ascendAndAwardHeartwood();
        host.gameState().resetForNewRun(System.currentTimeMillis());
        prepareFreshRun();
    }

    /** Picks a saved run back up on the screen its save was interrupted at. */
    public void continueRun() {
        GameState state = host.gameState();
        if (!host.continueAvailable() || !canContinue(state)) {
            return;
        }
        flow.transitionTo(GameScreenState.PLAYING);
        if (state.draftPending()) {
            // A save closed mid-draft replays the draft from its persisted offer and picks.
            flow.transitionTo(GameScreenState.TRIAL_DRAFT);
        } else if (state.awaitingBossReward) {
            flow.transitionTo(GameScreenState.CARD_CHOICE);
        } else if (state.ceremonyPending) {
            // A save closed mid-ceremony replays it from the start; it is deterministic.
            host.beginPlantingCeremony();
        } else if (state.unspentTalentPoints > 0) {
            flow.transitionTo(GameScreenState.LEVEL_UP);
        } else if (!state.waveActive && ArenaQueries.untouchedFirstWave(state)) {
            // Killed during the opening: replay it so every new run still starts with the prologue.
            flow.transitionTo(GameScreenState.CINEMATIC);
            openingCinematic.begin(openingTierFor(state));
        } else if (!state.waveActive) {
            int bossesBefore = ArenaQueries.livingBossCount(state);
            waveLifecycleSystem.startCurrentWave(state);
            if (ArenaQueries.livingBossCount(state) > bossesBefore) {
                audioManager.play(AudioCue.BOSS_ENTRANCE);
                presentationSystem.presentBossEntrance(state);
            }
            host.showWaveReflection();
        }
    }

    /** True while the run is worth offering on the menu: alive, not finished. */
    public static boolean canContinue(GameState state) {
        return state != null && state.hero != null && state.hero.alive && !state.runComplete;
    }

    /** Tier whose opening lines a save replays: the snapshot, else the live tier. */
    public static int openingTierFor(GameState state) {
        return state.openingTier >= 0 ? state.openingTier : state.ascensionTier;
    }

    /**
     * The reset all three starts share, in the order the run expects it: a fresh loadout, the permanent root
     * network bonuses, a zeroed clock, cleared effect layers, then the trial draft the run opens on.
     */
    private void prepareFreshRun() {
        GameState state = host.gameState();
        starterLoadoutSystem.provisionOnce(state);
        if (rootNetworkSystem != null) {
            rootNetworkSystem.applyPermanentBonuses(state);
        }
        host.resetRunClock();
        hitStopSystem.clear();
        particleSystem.clear();
        floatingCoinTextSystem.clear();
        floatingDamageTextSystem.clear();
        trialDraftSystem.prepareOffer(state);
        flow.transitionTo(GameScreenState.TRIAL_DRAFT);
        host.saveNow();
    }
}
