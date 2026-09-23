package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.ascension.RootNetworkSystem;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.gameplay.BossIntroCinematic;
import com.amirrezahadipoor.herodefense.gameplay.HeroProgressionSystem;
import com.amirrezahadipoor.herodefense.gameplay.InventoryEquipmentSystem;
import com.amirrezahadipoor.herodefense.gameplay.OpeningCinematic;
import com.amirrezahadipoor.herodefense.gameplay.PlantingCeremony;
import com.amirrezahadipoor.herodefense.gameplay.WaveLifecycleSystem;
import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.TouchFeedbackSystem;
import com.amirrezahadipoor.herodefense.render.UiFrameRenderer;
import com.amirrezahadipoor.herodefense.settings.GameSettings;
import com.amirrezahadipoor.herodefense.settings.LocalSettingsRepository;
import com.amirrezahadipoor.herodefense.shop.StatShopSystem;
import com.amirrezahadipoor.herodefense.skills.SkillShopSystem;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * What a Back press does to the real screen chain (roadmap R7.4).
 *
 * <p>{@link BackNavigationTest} holds the decision table; this holds its application. The press is driven
 * through {@link ScreenTouchRouter#systemBack()}, over the real controllers and a real flow, and what is
 * asserted is that a press makes *the calls its button makes*: the same cue, the same controller, the same
 * save. That is the drift R7.7 was written about, and the Back button is the newest reader of these screens.
 *
 * <p>The host below answers the ten things a press may reach and refuses the other thirty-six. The refusals
 * are the point, not padding: they say what the Back button is allowed to touch, and a press that ever
 * reached for the shop system or the wave director would fail here with the name of the thing it reached.
 */
final class BackButtonRoutingTest {

    /** A run in progress pauses; the press is consumed and the run is written, because Back is pressed on
     *  the way out and roadmap R13.5 asks that a session survive the process. */
    @Test
    void aLiveRunPausesAndSavesInsteadOfClosingTheGame() {
        FakeHost host = new FakeHost();
        ScreenTouchRouter router = new ScreenTouchRouter(host);
        host.flow.transitionTo(GameScreenState.PLAYING);

        assertTrue(router.systemBack(), "the game consumed the press");
        assertEquals(GameScreenState.PAUSED, host.flow.state());
        assertEquals(List.of("saveNow"), host.calls, "the run is written when Back pauses it");
    }

    /** The pause overlay's Resume button and Back are the same move. */
    @Test
    void aPausedRunResumes() {
        FakeHost host = new FakeHost();
        ScreenTouchRouter router = new ScreenTouchRouter(host);
        host.flow.transitionTo(GameScreenState.PLAYING);
        host.flow.transitionTo(GameScreenState.PAUSED);

        assertTrue(router.systemBack());
        assertEquals(GameScreenState.PLAYING, host.flow.state());
        assertEquals(List.of(), host.calls, "resuming does not write the save again");
    }

    /** The menu is the end of the stack: the press belongs to the platform, which is the only caller that can
     *  finish an activity. */
    @Test
    void theMainMenuHandsThePressToThePlatform() {
        FakeHost host = new FakeHost();
        ScreenTouchRouter router = new ScreenTouchRouter(host);

        assertFalse(router.systemBack(), "nothing is left to go back to");
        assertEquals(List.of("exitApplication"), host.calls);
        assertEquals(GameScreenState.MENU, host.flow.state(), "the game does not move the screen itself");
    }

    /** The Codex closes the entry being read before it closes the shelf, and only the second press plays the
     *  cue the Close button plays. */
    @Test
    void theCodexClosesItsEntryBeforeItsShelf() {
        FakeHost host = new FakeHost();
        ScreenTouchRouter router = new ScreenTouchRouter(host);
        host.flow.transitionTo(GameScreenState.PLAYING);
        host.flow.transitionTo(GameScreenState.PAUSED);
        host.flow.transitionTo(GameScreenState.CODEX);
        host.codex.open();
        assertEquals(CodexTouchController.Action.SELECTED, host.codex.tap(host.state, 360f, 973f));

        assertTrue(router.systemBack(), "the entry is the deepest thing on screen");
        assertEquals(-1, host.codex.selectedIndex(), "the entry closed");
        assertTrue(host.codex.isOpen(), "and the shelf stayed open");
        assertEquals(GameScreenState.CODEX, host.flow.state());
        assertEquals(List.of(), host.calls, "closing a panel is not closing a screen");
        assertEquals(List.of(), host.cues);

        assertTrue(router.systemBack());
        assertFalse(host.codex.isOpen(), "the second press closes the shelf, as the Close button does");
        assertEquals(GameScreenState.PAUSED, host.flow.state(), "back to the screen the Codex was opened from");
        assertEquals(List.of(AudioCue.UI_CLOSE), host.cues);
        assertEquals(List.of("saveNow"), host.calls);
    }

    /** The backpack behaves the same way, and a press on the way out cannot sell or equip by accident. */
    @Test
    void theBackpackClosesTheItemBeforeTheScreen() {
        FakeHost host = new FakeHost();
        ScreenTouchRouter router = new ScreenTouchRouter(host);
        host.state.inventory.add(EquipmentCatalog.byId("leather_cap").createItem());
        host.flow.transitionTo(GameScreenState.PLAYING);
        host.flow.transitionTo(GameScreenState.INVENTORY);
        host.inventory.open();
        assertEquals(InventoryTouchController.Action.SELECTED, host.inventory.tap(host.state, 200f, 600f));

        assertTrue(router.systemBack());
        assertEquals(-1, host.inventory.selectedIndex(), "the details panel closed");
        assertTrue(host.inventory.isOpen(), "the backpack stayed open");
        assertEquals(GameScreenState.INVENTORY, host.flow.state());

        assertTrue(router.systemBack());
        assertEquals(GameScreenState.PLAYING, host.flow.state());
        assertEquals(List.of("saveNow"), host.calls);
    }

    /** Settings close onto the menu with the cue the settings Close button plays. */
    @Test
    void settingsCloseOntoTheMenu() {
        FakeHost host = new FakeHost();
        ScreenTouchRouter router = new ScreenTouchRouter(host);
        host.flow.transitionTo(GameScreenState.SETTINGS);

        assertTrue(router.systemBack());
        assertEquals(GameScreenState.MENU, host.flow.state());
        assertEquals(List.of(AudioCue.UI_CLOSE), host.cues);
    }

    /** A ceremony advances the way a tap on it does, and the opening is the ceremony that is playing. */
    @Test
    void aCeremonySkipsTheWayATapDoes() {
        FakeHost host = new FakeHost();
        ScreenTouchRouter router = new ScreenTouchRouter(host);
        host.flow.transitionTo(GameScreenState.PLAYING);
        host.flow.transitionTo(GameScreenState.CINEMATIC);
        host.opening.begin(1);

        assertTrue(router.systemBack());
        // `skip()` parks the timeline at its end; the ceremony's own next update reports completion and hands
        // the wave over. The key does not finish that frame for it, exactly as a tap does not.
        assertEquals(OpeningCinematic.Phase.DONE, host.opening.phase(), "the opening skipped to its end");
        assertEquals(GameScreenState.CINEMATIC, host.flow.state(),
            "the hand-off to the wave belongs to the ceremony, not to the key");
        assertTrue(host.opening.update(0.016f), "and the update after a skip is the one that ends it");
        assertFalse(host.opening.isActive());

        host.planting.begin();
        assertTrue(router.systemBack());
        assertEquals(PlantingCeremony.Phase.DONE, host.planting.phase(),
            "with no opening running, the planting ceremony is the one that skips");
    }

    /** The boss intro sits between the opening and the planting in the skip order. */
    @Test
    void aBossIntroSkipsTheWayATapDoes() {
        FakeHost host = new FakeHost();
        ScreenTouchRouter router = new ScreenTouchRouter(host);
        host.flow.transitionTo(GameScreenState.PLAYING);
        host.flow.transitionTo(GameScreenState.CINEMATIC);
        host.bossIntro.begin("VOID_KNIGHT", 2);

        assertTrue(router.systemBack());
        assertEquals(BossIntroCinematic.Phase.DONE, host.bossIntro.phase(),
            "with no opening running, the boss intro is the one that skips");
        assertEquals(GameScreenState.CINEMATIC, host.flow.state(),
            "the hand-off to the wave belongs to the ceremony, not to the key");
        assertTrue(host.bossIntro.update(0.016f), "and the update after a skip is the one that ends it");
        assertFalse(host.bossIntro.isActive());
    }

    /** The end screen ignores input during its own presentation, and Back is no exception: the run's record
     *  was written when the run ended, not when the player is allowed to leave the screen. */
    @Test
    void theEndScreenHoldsBackUntilItsPresentationIsOver() {
        FakeHost host = new FakeHost();
        ScreenTouchRouter router = new ScreenTouchRouter(host);
        host.flow.transitionTo(GameScreenState.PLAYING);
        host.flow.transitionTo(GameScreenState.GAME_OVER);

        assertTrue(router.systemBack(), "the press is still swallowed");
        assertEquals(GameScreenState.GAME_OVER, host.flow.state(), "and it changes nothing yet");
        assertEquals(List.of(), host.calls);

        host.state.runComplete = true;
        assertTrue(router.systemBack());
        assertEquals(GameScreenState.MENU, host.flow.state());
        assertEquals(List.of("saveNow"), host.calls);
    }

    /** The three screens that are a decision the player owes the game hold the press: nothing is discarded,
     *  and the phone does not close the game mid-choice — the behaviour the audit measured at zero. */
    @Test
    void aChoiceThePlayerOwesTheGameIsHeld() {
        for (GameScreenState screen : List.of(GameScreenState.LEVEL_UP, GameScreenState.CARD_CHOICE,
                GameScreenState.TRIAL_DRAFT)) {
            FakeHost host = new FakeHost();
            ScreenTouchRouter router = new ScreenTouchRouter(host);
            host.flow.transitionTo(GameScreenState.PLAYING);
            if (screen == GameScreenState.TRIAL_DRAFT) {
                host.flow.transitionTo(GameScreenState.MENU);
            }
            host.flow.transitionTo(screen);

            assertTrue(router.systemBack(), screen + " swallows the press");
            assertEquals(screen, host.flow.state(), screen + " keeps the decision on screen");
            assertEquals(List.of(), host.calls, screen + " does nothing at all");
            assertEquals(List.of(), host.cues, screen + " makes no sound");
        }
    }

    /** The shop and the root network have no details panel, so one press closes the screen and saves. */
    @Test
    void theShopAndTheRootNetworkCloseInOnePress() {
        for (GameScreenState screen : List.of(GameScreenState.SHOP, GameScreenState.ROOT_NETWORK)) {
            FakeHost host = new FakeHost();
            ScreenTouchRouter router = new ScreenTouchRouter(host);
            host.flow.transitionTo(GameScreenState.PLAYING);
            host.flow.transitionTo(screen);

            assertTrue(router.systemBack());
            assertEquals(GameScreenState.PLAYING, host.flow.state(), screen + " returned to the run");
            assertEquals(List.of("saveNow"), host.calls, screen + " saved on the way out, as its button does");
        }
    }

    /**
     * The game as a Back press sees it: real objects for the ten calls the policy may make, and a refusal for
     * the other thirty-six so the button's reach stays a stated fact instead of an accident.
     */
    private static final class FakeHost implements ScreenTouchRouter.Host {
        private final List<AudioCue> cues = new ArrayList<>();
        private final List<String> calls = new ArrayList<>();
        private final AudioPlayback audio = cues::add;
        private final GameFlowController flow = new GameFlowController();
        private final GameState state = GameState.newRun(7L);
        private final CodexTouchController codex = new CodexTouchController();
        private final InventoryTouchController inventory =
            new InventoryTouchController(new InventoryEquipmentSystem());
        private final OpeningCinematic opening = new OpeningCinematic();
        private final PlantingCeremony planting = new PlantingCeremony();
        private final BossIntroCinematic bossIntro = new BossIntroCinematic();

        /**
         * The end screen's presentation clock, held at zero on purpose: with no seconds elapsed the only way
         * the screen becomes interactive is a completed run, which is what the end-screen case then flips.
         */
        private static final float GAME_OVER_PRESENTATION_SECONDS = 0f;

        private static AssertionError refused(String what) {
            return new AssertionError("a Back press has no business reaching " + what);
        }

        @Override public AudioPlayback audioManager() {
            return audio;
        }

        @Override public BossIntroCinematic bossIntroCinematic() {
            return bossIntro;
        }

        @Override public CodexSystem codexSystem() {
            throw refused("codexSystem");
        }

        @Override public CodexTouchController codexTouchController() {
            return codex;
        }

        @Override public boolean continueAvailable() {
            throw refused("continueAvailable");
        }

        @Override public GameFlowController flow() {
            return flow;
        }

        @Override public float gameOverPresentationSeconds() {
            return GAME_OVER_PRESENTATION_SECONDS;
        }

        @Override public GameState gameState() {
            return state;
        }

        @Override public HapticFeedback hapticFeedback() {
            throw refused("hapticFeedback");
        }

        @Override public HeroProgressionSystem heroProgressionSystem() {
            throw refused("heroProgressionSystem");
        }

        @Override public InventoryTouchController inventoryTouchController() {
            return inventory;
        }

        @Override public OpeningCinematic openingCinematic() {
            return opening;
        }

        @Override public PauseTouchController pauseTouchController() {
            throw refused("pauseTouchController");
        }

        @Override public PlantingCeremony plantingCeremony() {
            return planting;
        }

        @Override public RewardCardTouchController rewardCardTouchController() {
            throw refused("rewardCardTouchController");
        }

        @Override public RootNetworkSystem rootNetworkSystem() {
            throw refused("rootNetworkSystem");
        }

        @Override public RootNetworkTouchController rootNetworkTouchController() {
            throw refused("rootNetworkTouchController");
        }

        @Override public GameSettings settings() {
            throw refused("settings");
        }

        @Override public LocalSettingsRepository settingsRepository() {
            throw refused("settingsRepository");
        }

        @Override public SettingsTouchController settingsTouchController() {
            throw refused("settingsTouchController");
        }

        @Override public SimulationSpeedTouchController simulationSpeedTouchController() {
            throw refused("simulationSpeedTouchController");
        }

        @Override public SkillShopSystem skillShopSystem() {
            throw refused("skillShopSystem");
        }

        @Override public StatShopSystem statShopSystem() {
            throw refused("statShopSystem");
        }

        @Override public StatShopTouchLayout.Tab shopTab() {
            throw refused("shopTab");
        }

        @Override public boolean storyDialogueActive() {
            throw refused("storyDialogueActive");
        }

        @Override public TouchFeedbackSystem touchFeedbackSystem() {
            throw refused("touchFeedbackSystem");
        }

        @Override public TrialDraftTouchController trialDraftTouchController() {
            throw refused("trialDraftTouchController");
        }

        @Override public UiFrameRenderer uiFrameRenderer() {
            throw refused("uiFrameRenderer");
        }

        @Override public WaveLifecycleSystem waveLifecycleSystem() {
            throw refused("waveLifecycleSystem");
        }

        @Override public void setShopTab(StatShopTouchLayout.Tab tab) {
            throw refused("setShopTab");
        }

        @Override public void advanceStoryDialogue() {
            throw refused("advanceStoryDialogue");
        }

        @Override public void setLastTouchWorldX(float x) {
            throw refused("setLastTouchWorldX");
        }

        @Override public void setLastTouchWorldY(float y) {
            throw refused("setLastTouchWorldY");
        }

        @Override public void countHandledTouchUp() {
            throw refused("countHandledTouchUp");
        }

        @Override public void saveNow() {
            calls.add("saveNow");
        }

        @Override public void recordRunEnd() {
            throw refused("recordRunEnd");
        }

        @Override public void startNewRunSameTier() {
            throw refused("startNewRunSameTier");
        }

        @Override public void startBriefRun() {
            throw refused("startBriefRun");
        }

        @Override public void ascendRun() {
            throw refused("ascendRun");
        }

        @Override public void continueRun() {
            throw refused("continueRun");
        }

        @Override public void beginOpening() {
            throw refused("beginOpening");
        }

        @Override public void fireUltimate() {
            throw refused("fireUltimate");
        }

        @Override public void beginPlantingCeremony() {
            throw refused("beginPlantingCeremony");
        }

        @Override public void beginBossIntro() {
            throw refused("beginBossIntro");
        }

        @Override public void focusFireAt(float worldX, float worldY) {
            throw refused("focusFireAt");
        }

        @Override public void exitApplication() {
            calls.add("exitApplication");
        }
    }
}
