package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.gameplay.InventoryEquipmentSystem;
import com.amirrezahadipoor.herodefense.gameplay.OpeningCinematic;
import com.amirrezahadipoor.herodefense.gameplay.PlantingCeremony;
import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * What a Back press does to the real screen chain (roadmap R7.4).
 *
 * <p>{@link BackNavigationTest} holds the decision table; this holds its application. The port under test is
 * the one the router builds, driven through {@link ScreenTouchRouter#systemBack()}, so a press and the Close
 * button it stands in for are asserted to make the same calls: the same cue, the same controller, the same
 * save. That is the drift R7.7 was written about, and the Back button is the newest reader of these screens.
 *
 * <p>The {@code Host} is forty-six methods wide and a press touches nine of them, so it is answered by a
 * recording proxy instead of a hand-written fake: the proxy cannot quietly stop being what the game passes
 * in, because it is built from the interface itself.
 */
final class BackButtonRoutingTest {

    /** A run in progress pauses; the press is consumed and the run is written, because Back is pressed on
     *  the way out and roadmap R13.5 asks that a session survive the process. */
    @Test
    void aLiveRunPausesAndSavesInsteadOfClosingTheGame() {
        Fixture fixture = new Fixture();
        fixture.flow.transitionTo(GameScreenState.PLAYING);

        assertTrue(fixture.router.systemBack(), "the game consumed the press");
        assertEquals(GameScreenState.PAUSED, fixture.flow.state());
        assertTrue(fixture.calls.contains("saveNow"), "the run is written when Back pauses it");
        assertFalse(fixture.calls.contains("exitApplication"), "a run in progress is not the end of the stack");
    }

    /** The pause overlay's Resume button and Back are the same move. */
    @Test
    void aPausedRunResumes() {
        Fixture fixture = new Fixture();
        fixture.flow.transitionTo(GameScreenState.PLAYING);
        fixture.flow.transitionTo(GameScreenState.PAUSED);

        assertTrue(fixture.router.systemBack());
        assertEquals(GameScreenState.PLAYING, fixture.flow.state());
    }

    /** The menu is the end of the stack: the press belongs to the platform, which is the only caller that can
     *  finish an activity. */
    @Test
    void theMainMenuHandsThePressToThePlatform() {
        Fixture fixture = new Fixture();

        assertFalse(fixture.router.systemBack(), "nothing is left to go back to");
        assertEquals(List.of("exitApplication"), fixture.platformCalls);
        assertEquals(GameScreenState.MENU, fixture.flow.state(), "the game does not move the screen itself");
    }

    /** The Codex closes the entry being read before it closes the shelf, and both presses play no cue until
     *  the screen itself closes — the same order the Close button uses. */
    @Test
    void theCodexClosesItsEntryBeforeItsShelf() {
        Fixture fixture = new Fixture();
        fixture.flow.transitionTo(GameScreenState.PLAYING);
        fixture.flow.transitionTo(GameScreenState.PAUSED);
        fixture.flow.transitionTo(GameScreenState.CODEX);
        fixture.codex.open();
        assertEquals(CodexTouchController.Action.SELECTED, fixture.codex.tap(fixture.state, 360f, 973f));

        assertTrue(fixture.router.systemBack(), "the entry is the deepest thing on screen");
        assertEquals(-1, fixture.codex.selectedIndex(), "the entry closed");
        assertTrue(fixture.codex.isOpen(), "and the shelf stayed open");
        assertEquals(GameScreenState.CODEX, fixture.flow.state());
        assertTrue(fixture.cues.isEmpty(), "closing a panel is not closing a screen");

        assertTrue(fixture.router.systemBack());
        assertFalse(fixture.codex.isOpen(), "the second press closes the shelf, as the Close button does");
        assertEquals(GameScreenState.PAUSED, fixture.flow.state(), "back to the screen the Codex was opened from");
        assertEquals(List.of(AudioCue.UI_CLOSE), fixture.cues);
        assertTrue(fixture.calls.contains("saveNow"));
    }

    /** The backpack behaves the same way, and a press on the way out cannot sell or equip by accident. */
    @Test
    void theBackpackClosesTheItemBeforeTheScreen() {
        Fixture fixture = new Fixture();
        fixture.state.inventory.add(EquipmentCatalog.byId("leather_cap").createItem());
        fixture.flow.transitionTo(GameScreenState.PLAYING);
        fixture.flow.transitionTo(GameScreenState.INVENTORY);
        fixture.inventory.open();
        assertEquals(InventoryTouchController.Action.SELECTED, fixture.inventory.tap(fixture.state, 200f, 600f));

        assertTrue(fixture.router.systemBack());
        assertEquals(-1, fixture.inventory.selectedIndex(), "the details panel closed");
        assertTrue(fixture.inventory.isOpen(), "the backpack stayed open");
        assertEquals(GameScreenState.INVENTORY, fixture.flow.state());

        assertTrue(fixture.router.systemBack());
        assertEquals(GameScreenState.PLAYING, fixture.flow.state());
        assertTrue(fixture.calls.contains("saveNow"));
    }

    /** Settings close onto the menu with the cue the settings Close button plays. */
    @Test
    void settingsCloseOntoTheMenu() {
        Fixture fixture = new Fixture();
        fixture.flow.transitionTo(GameScreenState.SETTINGS);

        assertTrue(fixture.router.systemBack());
        assertEquals(GameScreenState.MENU, fixture.flow.state());
        assertEquals(List.of(AudioCue.UI_CLOSE), fixture.cues);
    }

    /** A ceremony advances the way a tap on it does, and the opening is the ceremony that is playing. */
    @Test
    void aCeremonySkipsTheWayATapDoes() {
        Fixture fixture = new Fixture();
        fixture.flow.transitionTo(GameScreenState.PLAYING);
        fixture.flow.transitionTo(GameScreenState.CINEMATIC);
        fixture.opening.begin(1);
        assertTrue(fixture.opening.isActive());

        assertTrue(fixture.router.systemBack());
        // `skip()` parks the timeline at its end and the ceremony's own next update reports completion, which
        // is exactly what a tap does: the key advances the ceremony, it does not finish the frame for it.
        assertEquals(OpeningCinematic.Phase.DONE, fixture.opening.phase(), "the opening skipped to its end");
        assertEquals(GameScreenState.CINEMATIC, fixture.flow.state(),
            "the hand-off to the wave belongs to the ceremony, not to the key");

        fixture.opening.update(1f);
        assertFalse(fixture.opening.isActive(), "and the update after a skip is the one that ends it");
        fixture.planting.begin();
        assertTrue(fixture.router.systemBack());
        assertEquals(PlantingCeremony.Phase.DONE, fixture.planting.phase(),
            "with no opening running, the planting ceremony is the one that skips");
    }

    /** The end screen ignores input during its own presentation, and Back is no exception: the run's record
     *  was written when the run ended, not when the player is allowed to leave the screen. */
    @Test
    void theEndScreenHoldsBackUntilItsPresentationIsOver() {
        Fixture fixture = new Fixture();
        fixture.flow.transitionTo(GameScreenState.PLAYING);
        fixture.flow.transitionTo(GameScreenState.GAME_OVER);
        fixture.answer("gameOverPresentationSeconds", 0f);

        assertTrue(fixture.router.systemBack(), "the press is still swallowed");
        assertEquals(GameScreenState.GAME_OVER, fixture.flow.state(), "and it changes nothing yet");

        fixture.state.runComplete = true;
        assertTrue(fixture.router.systemBack());
        assertEquals(GameScreenState.MENU, fixture.flow.state());
        assertTrue(fixture.calls.contains("saveNow"));
    }

    /** The three screens that are a decision the player owes the game hold the press: nothing is discarded,
     *  and the phone does not close the game mid-choice — the behaviour the audit measured at zero. */
    @Test
    void aChoiceThePlayerOwesTheGameIsHeld() {
        for (GameScreenState screen : List.of(GameScreenState.LEVEL_UP, GameScreenState.CARD_CHOICE,
                GameScreenState.TRIAL_DRAFT)) {
            Fixture fixture = new Fixture();
            fixture.flow.transitionTo(GameScreenState.PLAYING);
            if (screen == GameScreenState.TRIAL_DRAFT) {
                fixture.flow.transitionTo(GameScreenState.MENU);
            }
            fixture.flow.transitionTo(screen);
            int callsBefore = fixture.calls.size();

            assertTrue(fixture.router.systemBack(), screen + " swallows the press");
            assertEquals(screen, fixture.flow.state(), screen + " keeps the decision on screen");
            assertEquals(callsBefore, fixture.calls.size(), screen + " does nothing at all");
        }
    }

    /** The shop and the root network have no details panel, so one press closes the screen. */
    @Test
    void theShopAndTheRootNetworkCloseInOnePress() {
        for (GameScreenState screen : List.of(GameScreenState.SHOP, GameScreenState.ROOT_NETWORK)) {
            Fixture fixture = new Fixture();
            fixture.flow.transitionTo(GameScreenState.PLAYING);
            fixture.flow.transitionTo(screen);

            assertTrue(fixture.router.systemBack());
            assertEquals(GameScreenState.PLAYING, fixture.flow.state(), screen + " returned to the run");
            assertTrue(fixture.calls.contains("saveNow"), screen + " saved on the way out, as its button does");
        }
    }

    /** One fixture per case: real controllers and a real flow, a recording proxy for the rest of the port. */
    private static final class Fixture {
        private final GameFlowController flow = new GameFlowController();
        private final GameState state = GameState.newRun(7L);
        private final CodexTouchController codex = new CodexTouchController();
        private final InventoryTouchController inventory =
            new InventoryTouchController(new InventoryEquipmentSystem());
        private final OpeningCinematic opening = new OpeningCinematic();
        private final PlantingCeremony planting = new PlantingCeremony();
        private final List<AudioCue> cues = new ArrayList<>();
        private final List<String> calls = new ArrayList<>();
        private final List<String> platformCalls = new ArrayList<>();
        private final Map<String, Object> answers = new HashMap<>();
        private final ScreenTouchRouter router;

        Fixture() {
            AudioPlayback audio = cues::add;
            answers.put("flow", flow);
            answers.put("gameState", state);
            answers.put("codexTouchController", codex);
            answers.put("inventoryTouchController", inventory);
            answers.put("openingCinematic", opening);
            answers.put("plantingCeremony", planting);
            answers.put("audioManager", audio);
            answers.put("gameOverPresentationSeconds", 0f);
            router = new ScreenTouchRouter((ScreenTouchRouter.Host) Proxy.newProxyInstance(
                ScreenTouchRouter.Host.class.getClassLoader(),
                new Class<?>[] {ScreenTouchRouter.Host.class},
                this::answer));
        }

        void answer(String method, Object value) {
            answers.put(method, value);
        }

        private Object answer(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("saveNow".equals(name) || "exitApplication".equals(name)) {
                calls.add(name);
                if ("exitApplication".equals(name)) {
                    platformCalls.add(name);
                }
                return null;
            }
            if (answers.containsKey(name)) {
                return answers.get(name);
            }
            Class<?> type = method.getReturnType();
            if (type == boolean.class) return false;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0f;
            if (type == double.class) return 0d;
            return null;
        }
    }
}
