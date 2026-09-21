package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.ascension.RootNetworkSystem;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.gameplay.BraceSystem;
import com.amirrezahadipoor.herodefense.gameplay.FocusSystem;
import com.amirrezahadipoor.herodefense.gameplay.HeroMovementSystem;
import com.amirrezahadipoor.herodefense.gameplay.HeroProgressionSystem;
import com.amirrezahadipoor.herodefense.gameplay.OpeningCinematic;
import com.amirrezahadipoor.herodefense.gameplay.PlantingCeremony;
import com.amirrezahadipoor.herodefense.gameplay.WaveCompletion;
import com.amirrezahadipoor.herodefense.gameplay.WaveLifecycleSystem;
import com.amirrezahadipoor.herodefense.input.CodexTouchController;
import com.amirrezahadipoor.herodefense.input.GameOverTouchLayout;
import com.amirrezahadipoor.herodefense.input.HapticFeedback;
import com.amirrezahadipoor.herodefense.input.HudTouchLayout;
import com.amirrezahadipoor.herodefense.input.InventoryTouchController;
import com.amirrezahadipoor.herodefense.input.LevelUpTouchLayout;
import com.amirrezahadipoor.herodefense.input.MainMenuTouchLayout;
import com.amirrezahadipoor.herodefense.input.PauseTouchController;
import com.amirrezahadipoor.herodefense.input.PauseTouchLayout;
import com.amirrezahadipoor.herodefense.input.RewardCardTouchController;
import com.amirrezahadipoor.herodefense.input.RootNetworkTouchController;
import com.amirrezahadipoor.herodefense.input.SettingsTouchController;
import com.amirrezahadipoor.herodefense.input.SettingsTouchLayout;
import com.amirrezahadipoor.herodefense.input.SimulationSpeedTouchController;
import com.amirrezahadipoor.herodefense.input.StatShopTouchLayout;
import com.amirrezahadipoor.herodefense.input.TouchInputController;
import com.amirrezahadipoor.herodefense.input.TrialDraftTouchController;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.onboarding.OnboardingAction;
import com.amirrezahadipoor.herodefense.model.HeroStat;
import com.amirrezahadipoor.herodefense.polish.TouchFeedbackSystem;
import com.amirrezahadipoor.herodefense.render.GameOverOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.UiFrameRenderer;
import com.amirrezahadipoor.herodefense.settings.GameSettings;
import com.amirrezahadipoor.herodefense.settings.LocalSettingsRepository;
import com.amirrezahadipoor.herodefense.shop.StatShopSystem;
import com.amirrezahadipoor.herodefense.skills.SkillEvolution;
import com.amirrezahadipoor.herodefense.skills.SkillId;
import com.amirrezahadipoor.herodefense.skills.SkillShopSystem;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import com.amirrezahadipoor.herodefense.story.LoreCatalog;
import com.amirrezahadipoor.herodefense.story.LoreEntry;
import com.amirrezahadipoor.herodefense.story.LoreNarration;
import com.amirrezahadipoor.herodefense.audio.GameAudioManager;
import com.amirrezahadipoor.herodefense.accessibility.ScreenReaderSystem;
import com.amirrezahadipoor.herodefense.audio.NarrationRequest;

/**
 * Routes touch events by screen state: menu, run, overlays, shops, codex, ceremonies.
 *
 * <p>Extracted from {@code HeroDefenseGame} (roadmap R2.2) because the state chain is the part of the game
 * class that grows with every new screen. The router owns no state of its own; it reads and writes the game
 * through the {@link Host} interface, which keeps the game class the single owner of the flow, the story line
 * and the save file. The chain was moved verbatim, so the behaviour is the one the emulator smoke journeys
 * already cover.
 */
public final class ScreenTouchRouter implements TouchInputController.Listener {

    private final Host host;

    /** The Back button's view of the game (roadmap R7.4); one instance, since the port holds no state. */
    private final BackNavigation.Port backPort;

    public ScreenTouchRouter(Host host) {
        this.host = host;
        this.backPort = new ScreenBackPort(host);
    }


    /** What the router needs from the game; implemented by an adapter inside the game class. */
    public interface Host {
        AudioPlayback audioManager();

        CodexSystem codexSystem();

        CodexTouchController codexTouchController();

        boolean continueAvailable();

        GameFlowController flow();

        float gameOverPresentationSeconds();

        GameState gameState();

        HapticFeedback hapticFeedback();

        HeroProgressionSystem heroProgressionSystem();

        InventoryTouchController inventoryTouchController();

        OpeningCinematic openingCinematic();

        PauseTouchController pauseTouchController();

        PlantingCeremony plantingCeremony();

        RewardCardTouchController rewardCardTouchController();

        RootNetworkSystem rootNetworkSystem();

        RootNetworkTouchController rootNetworkTouchController();

        GameSettings settings();

        LocalSettingsRepository settingsRepository();

        SettingsTouchController settingsTouchController();

        SimulationSpeedTouchController simulationSpeedTouchController();

        SkillShopSystem skillShopSystem();

        StatShopSystem statShopSystem();

        StatShopTouchLayout.Tab shopTab();

        /** Whether a message box is on the screen; a tap goes to the box, not the HUD underneath. */
        boolean storyDialogueActive();

        TouchFeedbackSystem touchFeedbackSystem();

        TrialDraftTouchController trialDraftTouchController();

        UiFrameRenderer uiFrameRenderer();

        WaveLifecycleSystem waveLifecycleSystem();

        void setShopTab(StatShopTouchLayout.Tab tab);

        /** A tap on the box: the first finishes the typing, the next closes it. */
        void advanceStoryDialogue();

        void setLastTouchWorldX(float x);

        void setLastTouchWorldY(float y);

        void countHandledTouchUp();

        void saveNow();

        /** Writes down the session that just ended; a completed run ends here, through the reward card. */
        void recordRunEnd();

        void startNewRunSameTier();

        void startBriefRun();

        void ascendRun();

        void continueRun();

        void beginOpening();

        void fireUltimate();

        void beginPlantingCeremony();

        /** Tap-to-focus: marks the enemy under the tap, or clears the mark when the tap hits nothing. */
        void focusFireAt(float worldX, float worldY);

        /**
         * Hands Android's Back press to the platform when the menu is the last screen left (roadmap R7.4).
         * The game owns every other screen's answer; only leaving the application is the platform's to do.
         */
        void exitApplication();
    }

        @Override
        public boolean onTouchDown(float worldX, float worldY, int pointer) {
            host.uiFrameRenderer().press(worldX, worldY);
            return true;
        }

        @Override
        public boolean onTouchDragged(
            float worldX,
            float worldY,
            float deltaX,
            float deltaY,
            int pointer
        ) {
            host.uiFrameRenderer().movePress(worldX, worldY);
            // A drag during a wave is the Hero stepping (roadmap A1). It still aims nothing and fires nothing:
            // the bow keeps its own schedule and a tap keeps its own meaning, which is marking a target. The
            // walkable band is what excludes the HUD, so a drag that begins on a button walks nobody -- and the
            // order is only reported to the first-run coach when the movement system actually took it, which is
            // what keeps the coach's second lesson from completing on a drag that moved nothing.
            if (host.flow().state() == GameScreenState.PLAYING
                && HeroMovementSystem.orderStepTo(host.gameState(), worldX, worldY)) {
                host.flow().onboarding().notify(OnboardingAction.HERO_MOVED);
            }
            if (host.flow().state() == GameScreenState.INVENTORY
                && host.inventoryTouchController().isOpen()) {
                host.inventoryTouchController().drag(host.gameState(), deltaY);
            }
            if (host.flow().state() == GameScreenState.CODEX
                && host.codexTouchController().isOpen()) {
                host.codexTouchController().drag(host.gameState(), deltaY);
            }
            if (host.flow().state() == GameScreenState.SETTINGS) {
                host.settingsTouchController().drag(deltaY);
            }
            return true;
        }

        @Override
        public boolean onTouchUp(float worldX, float worldY, int pointer, boolean isTap) {
            host.uiFrameRenderer().release();
            host.setLastTouchWorldX(worldX);
            host.setLastTouchWorldY(worldY);
            host.countHandledTouchUp();
            if (!isTap) {
                // Releasing a drag ends the step order: the Hero stops where the finger left it rather than
                // finishing a walk the player is no longer asking for (A1).
                HeroMovementSystem.cancelOrder(host.gameState());
                return true;
            }
            boolean cardChoiceTap = host.flow().state() == GameScreenState.CARD_CHOICE
                || host.flow().state() == GameScreenState.TRIAL_DRAFT;
            if (!cardChoiceTap) {
                host.touchFeedbackSystem().triggerTap(worldX, worldY);
                host.hapticFeedback().tap();
            }
            if (host.flow().state() == GameScreenState.SETTINGS) {
                SettingsTouchLayout.Action action = host.settingsTouchController().tap(
                    host.settings(), worldX, worldY
                );
                if (action == SettingsTouchLayout.Action.CLOSE) {
                    host.audioManager().play(AudioCue.UI_CLOSE);
                    host.flow().transitionTo(GameScreenState.MENU);
                } else if (action != SettingsTouchLayout.Action.NONE) {
                    host.audioManager().play(AudioCue.UI_TAP);
                    host.settingsRepository().save(host.settings());
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.GAME_OVER) {
                // The death line box: a tap finishes the word or closes it, never the buttons the
                // panel will sit on once the word is done.
                if (host.storyDialogueActive()) {
                    host.advanceStoryDialogue();
                    return true;
                }
                if (GameOverOverlayRenderer.isInteractive(
                    host.gameOverPresentationSeconds(),
                    host.gameState().runComplete
                )) {
                    GameOverTouchLayout.Action action = GameOverTouchLayout.actionAt(worldX, worldY);
                    if (action == GameOverTouchLayout.Action.RESTART) {
                        host.startNewRunSameTier();
                    } else if (action == GameOverTouchLayout.Action.ASCEND) {
                        host.ascendRun();
                    } else if (action == GameOverTouchLayout.Action.ROOT_NETWORK) {
                        host.flow().transitionTo(GameScreenState.ROOT_NETWORK);
                    }
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.CARD_CHOICE) {
                if (host.rewardCardTouchController().tap(host.gameState(), worldX, worldY)) {
                    host.flow().onboarding().notify(OnboardingAction.CARD_TAKEN);
                    host.touchFeedbackSystem().triggerCardSelection(worldX, worldY);
                    host.hapticFeedback().cardSelection();
                    WaveCompletion result = host.waveLifecycleSystem().continueAfterBossReward(host.gameState());
                    if (result == WaveCompletion.RUN_COMPLETED) {
                        host.codexSystem().unlockSecretsForEquipment(host.gameState());
                        host.flow().transitionTo(GameScreenState.GAME_OVER);
                        // Both modes end on a boss wave, so a finished run reaches this branch rather than the
                        // director's: the session record has to be written here too (roadmap R3.6).
                        host.recordRunEnd();
                    } else if (result == WaveCompletion.PLANTING_CEREMONY) {
                        host.beginPlantingCeremony();
                    } else {
                        host.flow().transitionTo(GameScreenState.PLAYING);
                    }
                    host.saveNow();
                } else {
                    host.touchFeedbackSystem().triggerTap(worldX, worldY);
                    host.hapticFeedback().tap();
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.TRIAL_DRAFT) {
                int picksBefore = host.gameState().trialDraftPicks.size();
                String pathBefore = host.gameState().heroPath;
                if (host.trialDraftTouchController().tap(host.gameState(), worldX, worldY)) {
                    host.touchFeedbackSystem().triggerCardSelection(worldX, worldY);
                    host.hapticFeedback().cardSelection();
                    host.flow().transitionTo(GameScreenState.CINEMATIC);
                    host.beginOpening();
                    host.saveNow();
                } else if (pathBefore == null && host.gameState().heroPath != null) {
                    // Binding a path (roadmap B3) carries the same weight as a draft pick: it is run
                    // state, so a reload must not replay the choice the player just made.
                    host.touchFeedbackSystem().triggerCardSelection(worldX, worldY);
                    host.hapticFeedback().cardSelection();
                    host.saveNow();
                } else {
                    host.touchFeedbackSystem().triggerTap(worldX, worldY);
                    host.hapticFeedback().tap();
                    if (host.gameState().trialDraftPicks.size() > picksBefore) {
                        host.saveNow();
                    }
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.CINEMATIC) {
                if (host.openingCinematic().isActive()) {
                    host.openingCinematic().skip();
                } else {
                    host.plantingCeremony().skip();
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.LEVEL_UP) {
                HeroStat selectedStat = LevelUpTouchLayout.statAt(worldX, worldY);
                if (host.heroProgressionSystem().allocateTalentPoint(host.gameState(), selectedStat)) {
                    host.saveNow();
                    if (host.gameState().unspentTalentPoints == 0) {
                        host.flow().transitionTo(GameScreenState.PLAYING);
                    }
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.SHOP) {
                StatShopTouchLayout.Tab tab = StatShopTouchLayout.tabAt(worldX, worldY);
                if (StatShopTouchLayout.closeAt(worldX, worldY)) {
                    host.audioManager().play(AudioCue.UI_CLOSE);
                    host.flow().returnFromOverlay();
                    host.saveNow();
                } else if (StatShopTouchLayout.rootAt(worldX, worldY)) {
                    host.flow().transitionTo(GameScreenState.ROOT_NETWORK);
                } else if (tab != null) {
                    host.setShopTab(tab);
                } else if (host.shopTab() == StatShopTouchLayout.Tab.SKILLS) {
                    SkillId skill = StatShopTouchLayout.skillAt(worldX, worldY);
                    if (skill != null && host.skillShopSystem().atEvolutionFork(host.gameState(), skill)) {
                        int option = StatShopTouchLayout.evolutionOptionAt(worldX, worldY);
                        if (option >= 0 && host.skillShopSystem().purchaseEvolution(
                            host.gameState(), skill, SkillEvolution.forSkill(skill).get(option)
                        )) {
                            host.codexSystem().unlockSecretsForSkillPurchase(host.gameState());
                            host.audioManager().play(AudioCue.PURCHASE);
                            host.saveNow();
                        }
                    } else if (host.skillShopSystem().purchase(host.gameState(), skill)) {
                        host.codexSystem().unlockSecretsForSkillPurchase(host.gameState());
                        host.audioManager().play(AudioCue.PURCHASE);
                        host.saveNow();
                    }
                } else {
                    HeroStat stat = StatShopTouchLayout.statAt(worldX, worldY);
                    host.statShopSystem().noteTouch(stat);
                    if (host.statShopSystem().purchase(host.gameState(), stat)) {
                        host.gameState().shopStatsBoughtThisRun++;
                        host.gameState().bareHandedEligible = false;
                        host.audioManager().play(AudioCue.PURCHASE);
                        host.saveNow();
                    }
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.MENU) {
                MainMenuTouchLayout.Action action = MainMenuTouchLayout.actionAt(
                    worldX, worldY, host.continueAvailable()
                );
                // The first vigil (roadmap R7.1) is offered to a new run and never to a Continue: the player
                // who is resuming a session has already met the game, and a lesson about tapping the ground is
                // exactly the wrong thing to open with on the way back into wave 40.
                if (action != MainMenuTouchLayout.Action.NONE) host.audioManager().play(AudioCue.UI_TAP);
                if (action == MainMenuTouchLayout.Action.NEW_GAME) {
                    host.flow().onboarding().attach(host.settings(), host.settingsRepository());
                    host.flow().onboarding().beginIfUnseen(false);
                    host.startNewRunSameTier();
                } else if (action == MainMenuTouchLayout.Action.BRIEF_RUN) {
                    host.flow().onboarding().attach(host.settings(), host.settingsRepository());
                    host.flow().onboarding().beginIfUnseen(false);
                    host.startBriefRun();
                } else if (action == MainMenuTouchLayout.Action.CONTINUE) {
                    host.continueRun();
                } else if (action == MainMenuTouchLayout.Action.SETTINGS) {
                    host.settingsTouchController().open();
                    host.flow().transitionTo(GameScreenState.SETTINGS);
                } else if (action == MainMenuTouchLayout.Action.ROOT_NETWORK) {
                    host.flow().transitionTo(GameScreenState.ROOT_NETWORK);
                } else if (action == MainMenuTouchLayout.Action.CODEX) {
                    host.flow().transitionTo(GameScreenState.CODEX);
                    host.codexTouchController().open();
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.ROOT_NETWORK) {
                com.amirrezahadipoor.herodefense.input.RootNetworkTouchController.Action rnAction =
                    host.rootNetworkTouchController().tap(host.gameState(), worldX, worldY);
                if (rnAction == com.amirrezahadipoor.herodefense.input.RootNetworkTouchController.Action.CLOSED) {
                    host.flow().returnFromOverlay();
                    host.saveNow();
                } else if (rnAction == com.amirrezahadipoor.herodefense.input.RootNetworkTouchController.Action.PURCHASED) {
                    // Apply only the node just bought: re-applying every owned node re-stacked its stats.
                    host.rootNetworkSystem().applyNodeDuringRun(
                        host.gameState(), host.rootNetworkTouchController().lastPurchasedNodeId()
                    );
                    host.audioManager().play(AudioCue.PURCHASE);
                    host.saveNow();
                }
                return true;
            }
            // A message box is up (beat or whisper): the first tap finishes the typing, the next closes
            // the box, so a single tap can never fall through to a HUD button the box is sitting on.
            if (host.flow().state() == GameScreenState.PLAYING && host.storyDialogueActive()) {
                host.advanceStoryDialogue();
                return true;
            }
            if (host.flow().state() == GameScreenState.PLAYING
                && HudTouchLayout.inventoryAt(worldX, worldY)) {
                host.audioManager().play(AudioCue.UI_TAP);
                host.flow().transitionTo(GameScreenState.INVENTORY);
                host.inventoryTouchController().open();
                host.saveNow();
                return true;
            }
            if (host.flow().state() == GameScreenState.PLAYING
                && HudTouchLayout.shopAt(worldX, worldY)) {
                host.flow().onboarding().notify(OnboardingAction.SHOP_OPENED);
                host.flow().transitionTo(GameScreenState.SHOP);
                host.saveNow();
                return true;
            }
            if (host.flow().state() == GameScreenState.PLAYING
                && HudTouchLayout.ultimateAt(worldX, worldY)) {
                if (FocusSystem.isFull(host.gameState())) {
                    host.flow().onboarding().notify(OnboardingAction.ULTIMATE_FIRED);
                    host.fireUltimate();
                    host.saveNow();
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.PLAYING
                && host.simulationSpeedTouchController().tap(host.gameState(), worldX, worldY)) {
                host.saveNow();
                return true;
            }
            if (host.flow().state() == GameScreenState.PLAYING
                && host.pauseTouchController().tap(host.flow(), worldX, worldY)) {
                host.saveNow();
                return true;
            }
            // The first-run coach gets first refusal on a tap (roadmap R7.1): its Skip button is the one
            // target that must never double as a play input, or skipping the lesson would also send the Hero
            // walking into the wave it was warning about.
            if (host.flow().state() == GameScreenState.PLAYING
                && host.flow().onboarding().handleTap(worldX, worldY)) {
                host.saveNow();
                return true;
            }
            // A2: a tap on the Hero's own body raises the shield. It is the one arena tap that meant nothing
            // before this -- no enemy under the finger, no mark to release -- and it sits after the coach's Skip
            // so skipping a lesson can never double as bracing for a hit. A tap that finds the Hero mid-cooldown
            // is still consumed rather than passed to the bow: a shield that refuses is still an answer, and
            // letting the tap fall through would mark nothing and clear the player's mark as a shrug.
            if (host.flow().state() == GameScreenState.PLAYING
                && BraceSystem.tapHitsHero(host.gameState(), worldX, worldY)) {
                if (BraceSystem.tryBrace(host.gameState())) {
                    host.flow().onboarding().notify(OnboardingAction.BRACE_RAISED);
                    host.hapticFeedback().tap();
                }
                return true;
            }
            // Anything still unclaimed inside a running wave is a tap on the arena: mark the enemy under the
            // finger so the bow focuses it, or release the mark when the tap lands on empty ground (R3.1).
            if (host.flow().state() == GameScreenState.PLAYING) {
                host.flow().onboarding().notify(OnboardingAction.TAP_GROUND);
                host.focusFireAt(worldX, worldY);
                return true;
            }
            if (host.flow().state() == GameScreenState.INVENTORY
                && host.inventoryTouchController().isOpen()) {
                InventoryTouchController.Action action = host.inventoryTouchController().tap(
                    host.gameState(), host.settings(), worldX, worldY
                );
                if (action == InventoryTouchController.Action.CLOSED) {
                    host.flow().returnFromOverlay();
                    host.saveNow();
                } else if (action == InventoryTouchController.Action.EQUIPPED
                    || action == InventoryTouchController.Action.UNEQUIPPED
                    || action == InventoryTouchController.Action.SOLD
                    || action == InventoryTouchController.Action.FORGED) {
                    if (action == InventoryTouchController.Action.FORGED) {
                        host.codexSystem().unlockSecretsForForge(host.gameState());
                        host.audioManager().play(AudioCue.PURCHASE);
                    }
                    if (action == InventoryTouchController.Action.EQUIPPED) {
                        host.codexSystem().unlockSecretsForEquipment(host.gameState());
                    }
                    host.saveNow();
                } else if (action == InventoryTouchController.Action.AUTO_SELL_TOGGLED) {
                    host.settingsRepository().save(host.settings());
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.CODEX
                && host.codexTouchController().isOpen()) {
                CodexTouchController.Action action = host.codexTouchController().tap(
                    host.gameState(), worldX, worldY
                );
                if (action == CodexTouchController.Action.CLOSED) {
                    host.audioManager().play(AudioCue.UI_CLOSE);
                    host.codexTouchController().close();
                    host.flow().returnFromOverlay();
                    host.saveNow();
                } else if (action == CodexTouchController.Action.SELECTED) {
                    host.audioManager().play(AudioCue.UI_TAP);
                    // F3: narrate selected lore entry
                    if (host.audioManager() instanceof GameAudioManager) {
                        int idx = host.codexTouchController().selectedIndex();
                        if (idx >= 0 && idx < LoreCatalog.all().size()) {
                            LoreEntry entry = LoreCatalog.all().get(idx);
                            if (entry != null && host.gameState() != null
                                && host.codexTouchController().tab() == CodexTouchLayout.Tab.LORE) {
                                // Only narrate if unlocked
                                boolean unlocked = host.codexSystem().isUnlocked(host.gameState(), entry.id());
                                if (unlocked) {
                                    NarrationRequest req = LoreNarration.forEntry(entry);
                                    if (req != null && ((GameAudioManager) host.audioManager()).narration() != null) {
                                        ((GameAudioManager) host.audioManager()).narration().narrate(req);
                                    }
                                }
                            }
                        }
                    }
                }
                return true;
            }
            if (host.flow().state() == GameScreenState.PAUSED
                && PauseTouchLayout.shopAt(worldX, worldY)) {
                host.flow().transitionTo(GameScreenState.SHOP);
                announce(host.audioManager(), "shop");
                return true;
            }
            if (host.flow().state() == GameScreenState.PAUSED
                && PauseTouchLayout.inventoryAt(worldX, worldY)) {
                host.flow().transitionTo(GameScreenState.INVENTORY);
                host.inventoryTouchController().open();
                announce(host.audioManager(), "inventory");
                return true;
            }
            if (host.flow().state() == GameScreenState.PAUSED
                && PauseTouchLayout.rootAt(worldX, worldY)) {
                host.flow().transitionTo(GameScreenState.ROOT_NETWORK);
                return true;
            }
            if (host.flow().state() == GameScreenState.PAUSED
                && PauseTouchLayout.codexAt(worldX, worldY)) {
                host.flow().transitionTo(GameScreenState.CODEX);
                host.codexTouchController().open();
                announce(host.audioManager(), "close_codex");
                return true;
            }
            if (host.flow().state() == GameScreenState.PAUSED) {
                host.pauseTouchController().tap(host.flow(), worldX, worldY);
            }
            return true;
        }

    private void announce(com.amirrezahadipoor.herodefense.audio.AudioPlayback playback, String key) {
        if (playback instanceof GameAudioManager) {
            ScreenReaderSystem sr = ((GameAudioManager) playback).screenReader();
            if (sr != null) {
                sr.announce(key);
            }
        }
    }

    /**
     * Android's Back key (roadmap R7.4).
     *
     * <p>The policy is {@link BackNavigation}'s table; this is only its application, over the same
     * {@code Host} every tap on these screens already goes through. Each of the nine primitives below is the
     * call sequence the matching button makes, so a press and the button it stands in for cannot drift apart
     * the way the menu's rows and the menu's renderer had before R7.7.
     *
     * @return true when the game consumed the press, false when the platform should leave the application
     */
    public boolean systemBack() {
        return BackNavigation.press(backPort);
    }

}

