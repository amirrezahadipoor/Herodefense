package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.GameScreenState;

/**
 * What Android's Back button does on each screen (roadmap R7.4).
 *
 * <p>The re-audit of 2026-09-17 scored this at zero of ten for one measurable reason: nothing in the
 * repository handled the key, so a press on *any* screen finished the activity. On the menu that is what a
 * player wants; halfway into wave 140 it loses the run, and it teaches the player that the phone's own
 * navigation is a trap they have to avoid. This class is the whole policy.
 *
 * <p>It is a decision table over {@link GameScreenState} rather than a chain of conditions in the launcher,
 * for two reasons. A switch expression with no {@code default} does not compile when a screen is added and
 * left undecided, so the table cannot silently stop covering the game. And a table with no libGDX types in it
 * can be tested against a real {@code GameFlowController}, which is what
 * {@code BackNavigationTest} does: every action the table returns is applied to the flow and asserted to land
 * on a legal transition, so Back can never reach a player as a crash.
 *
 * <p>The rule, one line per case:
 * <ul>
 *   <li>a screen the player opened closes, and the screen it was opened from comes back;</li>
 *   <li>a details panel inside that screen closes first, because backing out of the Codex should close the
 *       entry being read before it closes the shelf;</li>
 *   <li>a run in progress pauses; Back is not a quit button, and quitting is a decision the pause screen
 *       already offers;</li>
 *   <li>a ceremony skips forward the way a tap on it does, since it is the one screen whose content is
 *       optional and the player has already seen a tap do this;</li>
 *   <li>a choice the player owes the game — a reward card, a talent point, a trial draft — is <em>held</em>:
 *       the press is swallowed and nothing changes, because the alternative is discarding a decision the
 *       rules of the screen say has to be made;</li>
 *   <li>the main menu is the end of the stack, so Back there belongs to Android and leaves the game.</li>
 * </ul>
 */
public final class BackNavigation {

    /** What one press of Back does. */
    public enum Action {
        /** The screen is a choice the player owes the game; the press is swallowed and nothing changes. */
        HELD,
        /** A details panel inside the open screen closes, and the screen itself stays open. */
        CLOSE_PANEL,
        /** The ceremony advances, exactly as a tap on it does. */
        SKIP_CEREMONY,
        /** The pause overlay closes and the run continues. */
        RESUME_RUN,
        /** The open screen closes and the screen underneath it returns. */
        CLOSE_SCREEN,
        /** The run pauses. */
        PAUSE_RUN,
        /** The run is over and its screen closes onto the menu. */
        QUIT_TO_MENU,
        /** Nothing is left to go back to: the press belongs to the platform, which leaves the application. */
        EXIT_APPLICATION
    }

    /**
     * The nine primitives the policy applies itself with. Every one of them is something a tap on these
     * screens already does, so a press and the matching button cannot drift apart: the port is implemented by
     * {@link ScreenTouchRouter} over the same {@code Host} its touch chain uses.
     */
    public interface Port {
        /** The screen the player is looking at. */
        GameScreenState state();

        /**
         * Whether this screen is showing a details panel for a selected row. Only the two screens that have
         * one are ever asked, and both answer from the selection their controller already owns.
         */
        boolean detailsPanelOpen(GameScreenState state);

        /** Clears that selection, which is what closes the panel. */
        void closeDetailsPanel(GameScreenState state);

        /** Advances the ceremony that is playing. */
        void skipCeremony();

        /** Closes the pause overlay. */
        void resumeRun();

        /** Closes the open screen onto the screen it was opened from. */
        void closeScreen(GameScreenState state);

        /** Pauses the run. */
        void pauseRun();

        /** Leaves the run's end screen for the menu. */
        void quitToMenu();

        /** Hands the press to the platform, which finishes the activity. */
        void exitApplication();
    }

    private BackNavigation() {
    }

    /**
     * The action one press of Back takes on {@code state}.
     *
     * @param detailsPanelOpen whether the screen is showing a details panel; only read by the two screens
     *                         that have one, so callers may answer false for the rest
     */
    public static Action actionFor(GameScreenState state, boolean detailsPanelOpen) {
        // No `default`: a screen added to GameScreenState without a decision here fails to compile, which is
        // the point. The alternative is a Back button that quietly does nothing on the new screen.
        return switch (state) {
            case MENU -> Action.EXIT_APPLICATION;
            case SETTINGS -> Action.CLOSE_SCREEN;
            case PLAYING -> Action.PAUSE_RUN;
            case PAUSED -> Action.RESUME_RUN;
            case LEVEL_UP, CARD_CHOICE, TRIAL_DRAFT -> Action.HELD;
            case CINEMATIC -> Action.SKIP_CEREMONY;
            // Only these two screens have a details panel to close. The shop and the root network answer the
            // panel question with "no" by construction, so the flag is not read for them: a caller that
            // answered true by mistake cannot invent a panel that is not on screen.
            case CODEX, INVENTORY -> detailsPanelOpen ? Action.CLOSE_PANEL : Action.CLOSE_SCREEN;
            case SHOP, ROOT_NETWORK -> Action.CLOSE_SCREEN;
            case GAME_OVER -> Action.QUIT_TO_MENU;
        };
    }

    /**
     * True when the game swallows the press; false when the platform should leave the application.
     *
     * <p>A held press counts as swallowed. That is the difference between "Back does nothing here" and "Back
     * closes the game": on a reward card the player has to choose, and closing the activity instead would be
     * the one behaviour nobody asked for.
     */
    public static boolean consumes(Action action) {
        return action != Action.EXIT_APPLICATION;
    }

    /**
     * Applies one press and answers whether the game consumed it.
     *
     * <p>The decision and the application are one call so that a screen cannot be read, change, and then be
     * acted on: the state is read once, here, and the action that state maps to is the one applied.
     */
    public static boolean press(Port port) {
        GameScreenState state = port.state();
        Action action = actionFor(state, port.detailsPanelOpen(state));
        switch (action) {
            case HELD -> {
                // Swallowed on purpose: the screen is a decision the player still owes the game.
            }
            case CLOSE_PANEL -> port.closeDetailsPanel(state);
            case SKIP_CEREMONY -> port.skipCeremony();
            case RESUME_RUN -> port.resumeRun();
            case CLOSE_SCREEN -> port.closeScreen(state);
            case PAUSE_RUN -> port.pauseRun();
            case QUIT_TO_MENU -> port.quitToMenu();
            case EXIT_APPLICATION -> port.exitApplication();
        }
        return consumes(action);
    }
}
