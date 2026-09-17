package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The Back button's decision table, and the proof that every decision it makes is a move the game's own flow
 * controller allows (roadmap R7.4).
 *
 * <p>The second half matters more than the first. A policy test that only reads the table back would pass on
 * a table that crashes the game: {@code GameFlowController.transitionTo} throws on an illegal transition, and
 * Back is the one input a player presses without looking at the screen. So
 * {@link #everyDecisionIsALegalMoveOnTheRealFlowController} walks all twelve screens into a real flow
 * controller by legal transitions and applies what the table says, which is the same thing a phone does.
 */
final class BackNavigationTest {

    /** Every screen the game has, so a new one shows up here as a missing expectation rather than a guess. */
    private static final List<GameScreenState> SCREENS = List.of(GameScreenState.values());

    @Test
    void everyScreenHasADecidedAnswerWhetherAPanelIsOpenOrNot() {
        for (GameScreenState screen : SCREENS) {
            assertNotNull(BackNavigation.actionFor(screen, false), screen + " has no Back decision");
            assertNotNull(BackNavigation.actionFor(screen, true), screen + " has no Back decision");
        }
        assertEquals(13, SCREENS.size(),
            "a screen was added: give it a row in the table below and in BackNavigation's switch");
    }

    @Test
    void theTableIsTheOneTheAuditAskedFor() {
        Map<GameScreenState, BackNavigation.Action> expected = new EnumMap<>(GameScreenState.class);
        expected.put(GameScreenState.MENU, BackNavigation.Action.EXIT_APPLICATION);
        expected.put(GameScreenState.SETTINGS, BackNavigation.Action.CLOSE_SCREEN);
        expected.put(GameScreenState.PLAYING, BackNavigation.Action.PAUSE_RUN);
        expected.put(GameScreenState.PAUSED, BackNavigation.Action.RESUME_RUN);
        expected.put(GameScreenState.CINEMATIC, BackNavigation.Action.SKIP_CEREMONY);
        expected.put(GameScreenState.GAME_OVER, BackNavigation.Action.QUIT_TO_MENU);
        for (GameScreenState held : List.of(GameScreenState.LEVEL_UP, GameScreenState.CARD_CHOICE,
                GameScreenState.TRIAL_DRAFT)) {
            expected.put(held, BackNavigation.Action.HELD);
        }
        for (GameScreenState overlay : List.of(GameScreenState.INVENTORY, GameScreenState.SHOP,
                GameScreenState.ROOT_NETWORK, GameScreenState.CODEX)) {
            expected.put(overlay, BackNavigation.Action.CLOSE_SCREEN);
        }
        for (Map.Entry<GameScreenState, BackNavigation.Action> row : expected.entrySet()) {
            assertSame(row.getValue(), BackNavigation.actionFor(row.getKey(), false),
                row.getKey() + " decides something else");
        }
        assertEquals(SCREENS.size(), expected.size(), "the table and the enum have to cover the same screens");
    }

    @Test
    void aDetailsPanelClosesBeforeTheScreenItSitsInside() {
        // The two screens that show a details panel for a selected row close the row first: a player reading
        // a lore page who presses Back expects the page, not the Codex.
        for (GameScreenState screen : List.of(GameScreenState.CODEX, GameScreenState.INVENTORY)) {
            assertSame(BackNavigation.Action.CLOSE_PANEL, BackNavigation.actionFor(screen, true),
                screen + " should close its panel first");
            assertSame(BackNavigation.Action.CLOSE_SCREEN, BackNavigation.actionFor(screen, false),
                screen + " with nothing selected closes the screen");
        }
        // The other two overlays have no panel, so the flag cannot change their answer.
        for (GameScreenState screen : List.of(GameScreenState.SHOP, GameScreenState.ROOT_NETWORK)) {
            assertSame(BackNavigation.Action.CLOSE_SCREEN, BackNavigation.actionFor(screen, true),
                screen + " has no details panel to close");
        }
        // And no screen outside those four grows a panel because a caller answered true by mistake.
        for (GameScreenState screen : SCREENS) {
            if (screen == GameScreenState.CODEX || screen == GameScreenState.INVENTORY
                || screen == GameScreenState.SHOP || screen == GameScreenState.ROOT_NETWORK) {
                continue;
            }
            assertSame(BackNavigation.actionFor(screen, false), BackNavigation.actionFor(screen, true),
                screen + " answers the panel question it was never asked");
        }
    }

    @Test
    void onlyTheMainMenuHandsThePressBackToThePlatform() {
        for (BackNavigation.Action action : BackNavigation.Action.values()) {
            assertEquals(action != BackNavigation.Action.EXIT_APPLICATION, BackNavigation.consumes(action),
                action + " is consumed by the game, or it is the platform leaving");
        }
        // The case the audit scored zero: a press mid-choice used to close the application.
        assertTrue(BackNavigation.consumes(BackNavigation.Action.HELD),
            "a held press still has to be swallowed, or the phone closes the game on a reward card");
    }

    @Test
    void aPressAppliesExactlyThePrimitiveTheTableNames() {
        Map<GameScreenState, String> expectedCall = new EnumMap<>(GameScreenState.class);
        expectedCall.put(GameScreenState.MENU, "exitApplication");
        expectedCall.put(GameScreenState.SETTINGS, "closeScreen");
        expectedCall.put(GameScreenState.PLAYING, "pauseRun");
        expectedCall.put(GameScreenState.PAUSED, "resumeRun");
        expectedCall.put(GameScreenState.CINEMATIC, "skipCeremony");
        expectedCall.put(GameScreenState.GAME_OVER, "quitToMenu");
        expectedCall.put(GameScreenState.LEVEL_UP, "");
        expectedCall.put(GameScreenState.CARD_CHOICE, "");
        expectedCall.put(GameScreenState.TRIAL_DRAFT, "");
        expectedCall.put(GameScreenState.SHOP, "closeScreen");
        expectedCall.put(GameScreenState.ROOT_NETWORK, "closeScreen");
        expectedCall.put(GameScreenState.INVENTORY, "closeDetailsPanel");
        expectedCall.put(GameScreenState.CODEX, "closeDetailsPanel");

        for (Map.Entry<GameScreenState, String> row : expectedCall.entrySet()) {
            RecordingPort port = new RecordingPort(row.getKey(), row.getKey() == GameScreenState.CODEX
                || row.getKey() == GameScreenState.INVENTORY);
            boolean consumed = BackNavigation.press(port);
            assertEquals(row.getValue().isEmpty() ? List.of("state", "detailsPanelOpen")
                    : List.of("state", "detailsPanelOpen", row.getValue()),
                port.calls, row.getKey() + " applied the wrong primitive");
            assertEquals(consumed, row.getKey() != GameScreenState.MENU,
                row.getKey() + " reports the wrong consumption");
        }
        assertEquals(SCREENS.size(), expectedCall.size(), "every screen has to be walked by this test");
    }

    /**
     * The test the rest of this file exists for: whatever the table decides, applying it to a real
     * {@link GameFlowController} is a legal transition. `transitionTo` throws otherwise, and on a phone that
     * throw is a crash on the navigation key.
     */
    @Test
    void everyDecisionIsALegalMoveOnTheRealFlowController() {
        assertBackLeaves(MENU(), GameScreenState.MENU);
        assertBackLeaves(SETTINGS(), GameScreenState.MENU);
        assertBackLeaves(PLAYING(), GameScreenState.PAUSED);
        assertBackLeaves(PAUSED(), GameScreenState.PLAYING);
        assertBackLeaves(LEVEL_UP(), GameScreenState.LEVEL_UP);
        assertBackLeaves(CARD_CHOICE(), GameScreenState.CARD_CHOICE);
        assertBackLeaves(TRIAL_DRAFT(), GameScreenState.TRIAL_DRAFT);
        assertBackLeaves(CINEMATIC(), GameScreenState.CINEMATIC);
        assertBackLeaves(GAME_OVER(), GameScreenState.MENU);
        // The four overlays return to the screen they were opened from, which the flow controller records.
        assertBackLeaves(INVENTORY_FROM_RUN(), GameScreenState.PLAYING);
        assertBackLeaves(SHOP_FROM_RUN(), GameScreenState.PLAYING);
        assertBackLeaves(ROOT_FROM_RUN(), GameScreenState.PLAYING);
        assertBackLeaves(CODEX_FROM_PAUSE(), GameScreenState.PAUSED);
        // An overlay opened from the menu returns to the menu, not into a run that is not there.
        assertBackLeaves(menuOverlay(GameScreenState.CODEX), GameScreenState.MENU);
        assertBackLeaves(menuOverlay(GameScreenState.SHOP), GameScreenState.MENU);
    }

    /** Drives {@code flow} into a screen, applies the flow's half of the Back decision, names the result. */
    private static void assertBackLeaves(GameFlowController flow, GameScreenState expected) {
        GameScreenState before = flow.state();
        BackNavigation.Action action = BackNavigation.actionFor(before, false);
        switch (action) {
            case CLOSE_SCREEN -> {
                if (before == GameScreenState.SETTINGS) {
                    flow.transitionTo(GameScreenState.MENU);
                } else {
                    flow.returnFromOverlay();
                }
            }
            case PAUSE_RUN -> flow.transitionTo(GameScreenState.PAUSED);
            case RESUME_RUN -> flow.returnFromOverlay();
            case QUIT_TO_MENU -> flow.transitionTo(GameScreenState.MENU);
            case HELD, CLOSE_PANEL, SKIP_CEREMONY, EXIT_APPLICATION -> {
                // No flow move: the screen keeps its state, which is what the assertion below checks.
            }
        }
        assertEquals(expected, flow.state(),
            "Back from " + before + " (" + action + ") landed somewhere the design does not describe");
    }

    private static GameFlowController MENU() {
        return new GameFlowController();
    }

    private static GameFlowController SETTINGS() {
        GameFlowController flow = MENU();
        flow.transitionTo(GameScreenState.SETTINGS);
        return flow;
    }

    private static GameFlowController PLAYING() {
        GameFlowController flow = MENU();
        flow.transitionTo(GameScreenState.PLAYING);
        return flow;
    }

    private static GameFlowController PAUSED() {
        GameFlowController flow = PLAYING();
        flow.transitionTo(GameScreenState.PAUSED);
        return flow;
    }

    private static GameFlowController LEVEL_UP() {
        GameFlowController flow = PLAYING();
        flow.transitionTo(GameScreenState.LEVEL_UP);
        return flow;
    }

    private static GameFlowController CARD_CHOICE() {
        GameFlowController flow = PLAYING();
        flow.transitionTo(GameScreenState.CARD_CHOICE);
        return flow;
    }

    private static GameFlowController CINEMATIC() {
        GameFlowController flow = PLAYING();
        flow.transitionTo(GameScreenState.CINEMATIC);
        return flow;
    }

    private static GameFlowController GAME_OVER() {
        GameFlowController flow = PLAYING();
        flow.transitionTo(GameScreenState.GAME_OVER);
        return flow;
    }

    private static GameFlowController TRIAL_DRAFT() {
        GameFlowController flow = MENU();
        flow.transitionTo(GameScreenState.TRIAL_DRAFT);
        return flow;
    }

    private static GameFlowController INVENTORY_FROM_RUN() {
        GameFlowController flow = PLAYING();
        flow.transitionTo(GameScreenState.INVENTORY);
        return flow;
    }

    private static GameFlowController SHOP_FROM_RUN() {
        GameFlowController flow = PLAYING();
        flow.transitionTo(GameScreenState.SHOP);
        return flow;
    }

    private static GameFlowController ROOT_FROM_RUN() {
        GameFlowController flow = PLAYING();
        flow.transitionTo(GameScreenState.ROOT_NETWORK);
        return flow;
    }

    private static GameFlowController CODEX_FROM_PAUSE() {
        GameFlowController flow = PAUSED();
        flow.transitionTo(GameScreenState.CODEX);
        return flow;
    }

    private static GameFlowController menuOverlay(GameScreenState overlay) {
        GameFlowController flow = MENU();
        flow.transitionTo(overlay);
        return flow;
    }

    /** Records which primitive a press applied, and in what order the policy read the screen. */
    private static final class RecordingPort implements BackNavigation.Port {
        private final GameScreenState state;
        private final boolean panelOpen;
        private final List<String> calls = new ArrayList<>();

        RecordingPort(GameScreenState state, boolean panelOpen) {
            this.state = state;
            this.panelOpen = panelOpen;
        }

        @Override
        public GameScreenState state() {
            calls.add("state");
            return state;
        }

        @Override
        public boolean detailsPanelOpen(GameScreenState screen) {
            calls.add("detailsPanelOpen");
            return panelOpen;
        }

        @Override
        public void closeDetailsPanel(GameScreenState screen) {
            calls.add("closeDetailsPanel");
        }

        @Override
        public void skipCeremony() {
            calls.add("skipCeremony");
        }

        @Override
        public void resumeRun() {
            calls.add("resumeRun");
        }

        @Override
        public void closeScreen(GameScreenState screen) {
            calls.add("closeScreen");
        }

        @Override
        public void pauseRun() {
            calls.add("pauseRun");
        }

        @Override
        public void quitToMenu() {
            calls.add("quitToMenu");
        }

        @Override
        public void exitApplication() {
            calls.add("exitApplication");
        }
    }

    @Test
    void thePolicyReadsTheScreenOnceAndAppliesOnePrimitive() {
        RecordingPort port = new RecordingPort(GameScreenState.PLAYING, false);
        assertTrue(BackNavigation.press(port));
        assertEquals(List.of("state", "detailsPanelOpen", "pauseRun"), port.calls);
        assertFalse(port.calls.contains("exitApplication"), "a run in progress is not the end of the stack");
    }
}
