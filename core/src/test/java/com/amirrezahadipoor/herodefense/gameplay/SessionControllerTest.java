package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.items.StarterLoadoutSystem;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.FloatingCoinTextSystem;
import com.amirrezahadipoor.herodefense.polish.FloatingDamageTextSystem;
import com.amirrezahadipoor.herodefense.polish.HitStopSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.presentation.RunPresentationSystem;
import com.amirrezahadipoor.herodefense.save.RunSaveRepository;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import com.amirrezahadipoor.herodefense.trials.TrialDraftSystem;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The run session (roadmap R2.2 slice 6, tested per R2.3): starting, restarting at a tier, ascending and picking a
 * save back up. The save file is a fake here, so the cases assert what the session does with it — clear it, write
 * it, or leave it alone — without a libGDX preferences file.
 */
final class SessionControllerTest {

    private final RecordingHost host = new RecordingHost();
    private final FakeSaves saves = new FakeSaves();
    private final GameFlowController flow = new GameFlowController();
    private final ParticleSystem particles = new ParticleSystem();
    private final SessionController controller = new SessionController(
        host, saves, flow, new StarterLoadoutSystem(), null, new TrialDraftSystem(), new HitStopSystem(),
        particles, new FloatingCoinTextSystem(), new FloatingDamageTextSystem(),
        new WaveLifecycleSystem(new EnemyWaveSpawner(new EnemyFactory()), new ContinuousWaveRun()),
        new OpeningCinematic()
    );

    @Test
    void aFreshRunDiscardsTheSaveThenOpensTheTrialDraft() {
        host.state = GameState.newRun(5L);

        controller.startNewRun();

        assertTrue(saves.cleared, "a brand-new run must not be able to rewind into the old one");
        assertNotNull(host.state);
        assertEquals(1, host.clockResets, "the run clock and the game-over timer are zeroed together");
        assertEquals(GameScreenState.TRIAL_DRAFT, flow.state());
        assertEquals(1, host.saves, "the fresh run is persisted before the player sees it");
    }

    @Test
    void restartingAtTheSameTierKeepsTheTierAndTheHeartwood() {
        GameState state = GameState.newRun(5L);
        state.ascensionTier = 3;
        state.heartwood = 250;
        host.state = state;

        controller.startNewRunSameTier();

        assertEquals(3, host.state.ascensionTier, "the death screen's \"again\" stays on the player's tier");
        assertEquals(250, host.state.heartwood);
        assertFalse(saves.cleared, "a same-tier restart keeps the save it just wrote");
        assertEquals(GameScreenState.TRIAL_DRAFT, flow.state());
    }

    @Test
    void ascendingAwardsHeartwoodAndClimbsExactlyOneTier() {
        GameState state = GameState.newRun(5L);
        state.ascensionTier = 2;
        state.heartwood = 100;
        state.peakWaveReached = 140;
        host.state = state;
        int heartwoodBefore = state.heartwood;

        controller.ascendRun();

        assertEquals(3, host.state.ascensionTier, "one ascension is one tier");
        assertTrue(host.state.heartwood > heartwoodBefore, "the tier is paid for in heartwood");
        assertEquals(GameScreenState.TRIAL_DRAFT, flow.state());
    }

    @Test
    void theBriefVigilKeepsTierAndTrophiesAndChangesOnlyTheLength() {
        GameState state = GameState.newRun(5L);
        state.ascensionTier = 3;
        state.heartwood = 250;
        state.trophies.award(com.amirrezahadipoor.herodefense.progression.Trophy.FIRST_VIGIL);
        host.state = state;

        controller.startBriefRun();

        assertEquals(com.amirrezahadipoor.herodefense.model.GameMode.BRIEF, host.state.mode);
        assertEquals(com.amirrezahadipoor.herodefense.model.GameMode.BRIEF.waves(), host.state.runLengthWaves());
        assertEquals(3, host.state.ascensionTier, "a short run is the same ladder");
        assertEquals(250, host.state.heartwood);
        assertTrue(host.state.trophies.isEarned(com.amirrezahadipoor.herodefense.progression.Trophy.FIRST_VIGIL),
            "and the same trophy case");
        assertEquals(GameScreenState.TRIAL_DRAFT, flow.state(), "the run still opens on the trial draft");
    }

    @Test
    void continuingWithoutAnOfferDoesNothing() {
        host.state = GameState.newRun(5L);
        host.continueAvailable = false;

        controller.continueRun();

        assertEquals(GameScreenState.MENU, flow.state(), "no save, no resume");
    }

    @Test
    void continuingASavedDraftReplaysTheDraftScreen() {
        GameState state = GameState.newRun(5L);
        new TrialDraftSystem().prepareOffer(state);
        host.state = state;
        host.continueAvailable = true;

        controller.continueRun();

        assertEquals(GameScreenState.TRIAL_DRAFT, flow.state(), "a save closed mid-draft resumes at the draft");
    }

    @Test
    void aDeadHeroCannotBeContinuedAndAFinishedRunIsNotOffered() {
        GameState dead = GameState.newRun(5L);
        dead.hero.alive = false;
        assertFalse(SessionController.canContinue(dead), "a dead hero has no run to continue");

        GameState finished = GameState.newRun(5L);
        finished.runComplete = true;
        assertFalse(SessionController.canContinue(finished), "a completed run is not a save");

        GameState alive = GameState.newRun(5L);
        assertTrue(SessionController.canContinue(alive), "a living hero in an unfinished run is");
    }

    @Test
    void theOpeningTierComesFromTheSnapshotWhenThereIsOne() {
        GameState state = GameState.newRun(5L);
        state.ascensionTier = 4;
        state.openingTier = -1;
        assertEquals(4, SessionController.openingTierFor(state), "no snapshot: the live tier is replayed");

        state.openingTier = 1;
        assertEquals(1, SessionController.openingTierFor(state), "with a snapshot, the save replays its own lines");
    }

    /** Records what the session asked for instead of driving screens and saves for real. */
    private static final class RecordingHost implements SessionController.Host {
        private GameState state;
        private boolean continueAvailable;
        private int clockResets;
        private int saves;

        @Override
        public GameState gameState() {
            return state;
        }

        @Override
        public void setGameState(GameState newState) {
            this.state = newState;
        }

        @Override
        public void resetRunClock() {
            clockResets++;
        }

        @Override
        public void saveNow() {
            saves++;
        }

        @Override
        public boolean continueAvailable() {
            return continueAvailable;
        }

        @Override
        public void showWaveReflection() {
        }

        @Override
        public void beginPlantingCeremony() {
        }

        @Override
        public void beginBossIntro() {
        }
    }

    private static final class FakeSaves implements RunSaveRepository {
        private GameState stored;
        private boolean cleared;

        @Override
        public void save(GameState state) {
            stored = state;
        }

        @Override
        public Optional<GameState> load() {
            return Optional.ofNullable(stored);
        }

        @Override
        public boolean hasSave() {
            return stored != null;
        }

        @Override
        public void clear() {
            cleared = true;
            stored = null;
        }
    }

}
