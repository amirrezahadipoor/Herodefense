package com.amirrezahadipoor.herodefense.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.HeroDefenseGame;
import com.amirrezahadipoor.herodefense.input.MainMenuTouchLayout;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.BooleanSupplier;

/**
 * Android's Back button, pressed through the platform (roadmap R7.4).
 *
 * <p>Every press here is a real {@link KeyEvent#KEYCODE_BACK} injected by the instrumentation, so it travels
 * the path a thumb does: window, view, libGDX's input queue, the screen chain. Nothing calls the game
 * directly, which is the point — before this existed the key finished the activity from every screen, and the
 * re-audit of 2026-09-17 scored it at zero of ten because nothing in the repository handled it at all.
 *
 * <p>What is asserted is the shape of the stack, not the pixels: a press closes the deepest thing on screen,
 * a run pauses instead of ending, a choice the player owes the game is held, and only the main menu leaves.
 */
@RunWith(AndroidJUnit4.class)
public final class BackButtonNavigationTest {
    private static final float WORLD_WIDTH = 720f;
    private static final float WORLD_HEIGHT = 1280f;
    private static final String SAVE_NAME = "hero-defense-local-save";

    /** The centre of a menu row's width: every menu action spans the same column. */
    private static final float MENU_X =
        MainMenuTouchLayout.BUTTON_X + MainMenuTouchLayout.BUTTON_WIDTH / 2f;

    /** How far down the menu to look for a row, so a grown menu still resolves. */
    private static final int MAXIMUM_MENU_ROWS = 12;

    @Test
    public void backClosesTheDeepestThingOnScreenAndOnlyTheMainMenuLeaves() {
        clearRunSave();
        try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)) {
            HeroDefenseGame game = gameFrom(scenario);
            await("libGDX touch input", game::readyForTouch);
            await("main menu", () -> game.screenState() == GameScreenState.MENU);
            View surface = gameSurfaceFrom(scenario);
            SystemClock.sleep(1_000L);

            // --- the Codex, with an entry open: two presses, because the entry is deeper than the shelf
            long touches = game.handledTouchUpCount();
            float codexY = menuActionY(MainMenuTouchLayout.Action.CODEX, false);
            tapWorld(surface, MENU_X, codexY);
            await("codex touch dispatch", () -> game.handledTouchUpCount() > touches);
            float[] correction = touchCorrection(game, MENU_X, codexY);
            await("codex opens", () -> game.screenState() == GameScreenState.CODEX);
            tapWorld(surface, 360f + correction[0], 973f + correction[1]); // first lore row
            await("codex entry opens", 3_000L, () -> !game.screenState().equals(GameScreenState.MENU));
            SystemClock.sleep(400L);

            pressBack();
            await("the entry closes and the shelf stays", () -> game.screenState() == GameScreenState.CODEX);
            SystemClock.sleep(400L);
            assertEquals(GameScreenState.CODEX, game.screenState());

            pressBack();
            await("the shelf closes onto the menu", () -> game.screenState() == GameScreenState.MENU);

            // --- the Codex with nothing selected: one press, which is what makes the two above meaningful
            touches = game.handledTouchUpCount();
            tapWorld(surface, MENU_X, codexY);
            await("codex touch dispatch again", () -> game.handledTouchUpCount() > touches);
            await("codex opens again", () -> game.screenState() == GameScreenState.CODEX);
            pressBack();
            await("an unopened entry costs no press", () -> game.screenState() == GameScreenState.MENU);

            // --- settings close onto the menu, and the application survives the press
            touches = game.handledTouchUpCount();
            float settingsY = menuActionY(MainMenuTouchLayout.Action.SETTINGS, false);
            tapWorld(surface, MENU_X, settingsY);
            await("settings touch dispatch", () -> game.handledTouchUpCount() > touches);
            await("settings open", () -> game.screenState() == GameScreenState.SETTINGS);
            pressBack();
            await("settings close onto the menu", () -> game.screenState() == GameScreenState.MENU);
            assertEquals(ActivityScenario.State.RESUMED, scenario.getState());

            // --- the menu is the end of the stack, so this press is the platform's
            pressBack();
            await("the application leaves", 10_000L,
                () -> scenario.getState() == ActivityScenario.State.DESTROYED);
        }
    }

    @Test
    public void backDuringARunPausesItAndAHeldChoiceSurvivesThePress() {
        clearRunSave();
        try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)) {
            HeroDefenseGame game = gameFrom(scenario);
            await("libGDX touch input", game::readyForTouch);
            await("main menu", () -> game.screenState() == GameScreenState.MENU);
            View surface = gameSurfaceFrom(scenario);
            SystemClock.sleep(1_000L);

            long touches = game.handledTouchUpCount();
            float newGameY = menuActionY(MainMenuTouchLayout.Action.NEW_GAME, false);
            tapWorld(surface, MENU_X, newGameY);
            await("new-game touch dispatch", () -> game.handledTouchUpCount() > touches);
            float[] correction = touchCorrection(game, MENU_X, newGameY);

            // The draft is a decision the player owes the game: Back is swallowed and the picks stay.
            await("trial draft", () -> game.screenState() == GameScreenState.TRIAL_DRAFT);
            pressBack();
            SystemClock.sleep(600L);
            assertEquals(GameScreenState.TRIAL_DRAFT, game.screenState());
            assertTrue(game.gameState().trialDraftPicks.isEmpty());
            assertEquals(ActivityScenario.State.RESUMED, scenario.getState());

            tapWorld(surface, 360f + correction[0], 887.5f + correction[1]); // first trial card
            await("first trial pick", () -> game.gameState().trialDraftPicks.size() == 1);
            tapWorld(surface, 360f + correction[0], 702.5f + correction[1]); // second trial card
            await("trial pair bound", () -> game.gameState().activeTrials.size() == 2);
            await("opening cinematic", () -> game.screenState() == GameScreenState.CINEMATIC);

            // A ceremony is the one screen whose content is optional, so Back advances it as a tap does.
            pressBack();
            await("the opening skips and wave 1 starts", 15_000L,
                () -> game.screenState() == GameScreenState.PLAYING);
            assertEquals(1, game.gameState().waveNumber);

            // The point of the whole item: a run in progress pauses. It does not end, and it is written down.
            pressBack();
            await("the run pauses", () -> game.screenState() == GameScreenState.PAUSED);
            assertEquals(ActivityScenario.State.RESUMED, scenario.getState());
            pressBack();
            await("the run resumes", () -> game.screenState() == GameScreenState.PLAYING);
            assertTrue(game.gameState().waveActive || game.gameState().waveNumber >= 1);

            // From the pause screen an overlay opens and closes by Back, returning to the run and not to the
            // menu: the screen underneath an overlay is the one the player left.
            pressBack();
            await("paused again", () -> game.screenState() == GameScreenState.PAUSED);
            tapWorld(surface, 360f + correction[0], 780f + correction[1]); // Inventory row on the pause screen
            await("inventory opens over the pause", () ->
                game.screenState() == GameScreenState.INVENTORY && game.inventoryOpen());
            pressBack();
            await("the backpack closes onto the pause", () -> game.screenState() == GameScreenState.PAUSED);
            pressBack();
            await("and the pause resumes the run", () -> game.screenState() == GameScreenState.PLAYING);
            assertFalse(scenario.getState() == ActivityScenario.State.DESTROYED);
        }
    }

    @After
    public void leaveNoRunBehind() {
        clearRunSave();
    }

    /** A real system Back press, through the platform rather than a call into the game. */
    private static void pressBack() {
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
    }

    /**
     * Where the menu puts one of its actions: the layout owns the drawn row and the tappable row, so the
     * journey asks it instead of copying a number (the reason R7.7 exists).
     */
    private static float menuActionY(MainMenuTouchLayout.Action action, boolean continueAvailable) {
        for (int row = 0; row <= MAXIMUM_MENU_ROWS; row++) {
            float y = MainMenuTouchLayout.rowBottom(row) + MainMenuTouchLayout.BUTTON_HEIGHT / 2f;
            if (MainMenuTouchLayout.actionAt(MENU_X, y, continueAvailable) == action) return y;
        }
        throw new AssertionError("the main menu has no tappable row for " + action);
    }

    private static HeroDefenseGame gameFrom(ActivityScenario<AndroidLauncher> scenario) {
        java.util.concurrent.atomic.AtomicReference<HeroDefenseGame> reference =
            new java.util.concurrent.atomic.AtomicReference<>();
        scenario.onActivity(activity -> reference.set(activity.gameForTests()));
        assertNotNull(reference.get());
        return reference.get();
    }

    private static View gameSurfaceFrom(ActivityScenario<AndroidLauncher> scenario) {
        java.util.concurrent.atomic.AtomicReference<View> reference =
            new java.util.concurrent.atomic.AtomicReference<>();
        scenario.onActivity(activity -> reference.set(((AndroidGraphics) activity.getGraphics()).getView()));
        assertNotNull(reference.get());
        return reference.get();
    }

    private static void tapWorld(View surface, float worldX, float worldY) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            float[] point = worldPoint(surface, worldX, worldY);
            long downTime = SystemClock.uptimeMillis();
            dispatchTouch(surface, downTime, downTime, MotionEvent.ACTION_DOWN, point[0], point[1]);
            dispatchTouch(surface, downTime, downTime + 32L, MotionEvent.ACTION_UP, point[0], point[1]);
        });
    }

    private static void dispatchTouch(View surface, long downTime, long eventTime, int action, float x, float y) {
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, 0);
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try {
            assertTrue(surface.dispatchTouchEvent(event));
        } finally {
            event.recycle();
        }
    }

    /** Mirrors the ExtendViewport: width pinned to 720, extra height split above and below. */
    private static float[] worldPoint(View surface, float worldX, float worldY) {
        float scale = surface.getWidth() / WORLD_WIDTH;
        float visibleWorldHeight = surface.getHeight() / scale;
        float bottomWorld = (WORLD_HEIGHT - visibleWorldHeight) * 0.5f;
        return new float[] {worldX * scale, (visibleWorldHeight - (worldY - bottomWorld)) * scale};
    }

    private static float[] touchCorrection(HeroDefenseGame game, float expectedWorldX, float expectedWorldY) {
        float actualX = game.lastTouchWorldX();
        float actualY = game.lastTouchWorldY();
        assertFalse(Float.isNaN(actualX));
        assertFalse(Float.isNaN(actualY));
        return new float[] {expectedWorldX - actualX, expectedWorldY - actualY};
    }

    private static void await(String label, BooleanSupplier condition) {
        await(label, 5_000L, condition);
    }

    private static void await(String label, long timeoutMillis, BooleanSupplier condition) {
        long deadline = SystemClock.uptimeMillis() + timeoutMillis;
        while (SystemClock.uptimeMillis() < deadline) {
            if (condition.getAsBoolean()) return;
            SystemClock.sleep(50L);
        }
        throw new AssertionError("Timed out waiting for " + label);
    }

    private static void clearRunSave() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertTrue(context.getSharedPreferences(SAVE_NAME, Context.MODE_PRIVATE).edit().clear().commit());
    }
}
