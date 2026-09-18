package com.amirrezahadipoor.herodefense.onboarding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.input.OnboardingTouchLayout;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.settings.GameSettings;
import com.amirrezahadipoor.herodefense.settings.LocalSettingsRepository;
import com.badlogic.gdx.Preferences;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Roadmap R7.1: the first vigil appears exactly once — which is a claim about the save file, so it is tested
 * through the same settings repository the game writes, not against a boolean in memory.
 */
final class OnboardingSystemTest {
    private static final float STEP = 0.5f;

    private final GameSettings settings = new GameSettings();
    private final Preferences preferences = new MemoryPreferences();
    private final LocalSettingsRepository repository = new LocalSettingsRepository(preferences);

    @Test
    void aNewRunOnAFreshInstallGetsTheLesson() {
        OnboardingSystem system = new OnboardingSystem();
        system.attach(settings, repository);
        system.beginIfUnseen(false);
        assertTrue(system.active());
        assertEquals(OnboardingStep.TARGET, system.currentStep());
    }

    @Test
    void aContinuedRunNeverGetsIt() {
        OnboardingSystem system = new OnboardingSystem();
        system.attach(settings, repository);
        system.beginIfUnseen(true);
        assertFalse(system.active(), "the player who is resuming a session has already met the game");
    }

    @Test
    void finishingTheLessonWritesTheFlagAndTheSecondRunIsSilent() {
        OnboardingSystem first = new OnboardingSystem();
        first.attach(settings, repository);
        first.beginIfUnseen(false);
        runToTheEnd(first);
        assertTrue(settings.tutorialSeen, "finishing the lesson records it");
        assertTrue(preferences.getBoolean("onboarding.tutorialSeen", false),
            "and it reaches the device's settings store, not just this session");

        // A second run in the same session, and a fresh session on the reloaded settings: both silent.
        OnboardingSystem second = new OnboardingSystem();
        second.attach(settings, repository);
        second.beginIfUnseen(false);
        assertFalse(second.active(), "the second run of the session gets nothing");

        GameSettings reloaded = repository.load();
        assertTrue(reloaded.tutorialSeen, "the flag survives a reload");
        OnboardingSystem afterRestart = new OnboardingSystem();
        afterRestart.attach(reloaded, repository);
        afterRestart.beginIfUnseen(false);
        assertFalse(afterRestart.active(), "and so does the run after the app was closed");
    }

    @Test
    void skippingAlsoCountsAsHavingSeenIt() {
        OnboardingSystem system = new OnboardingSystem();
        system.attach(settings, repository);
        system.beginIfUnseen(false);
        assertTrue(system.handleTap(
            OnboardingTouchLayout.skipX() + 10f, OnboardingTouchLayout.skipY() + 10f
        ), "a tap on Skip belongs to the coach");
        assertFalse(system.active());
        assertTrue(system.skipped());
        assertTrue(preferences.getBoolean("onboarding.tutorialSeen", false),
            "a skipped lesson is a lesson the player chose to skip once, not every run");
    }

    @Test
    void theSkipTargetSwallowsTheTapAndEverythingElseFallsThroughToTheGame() {
        OnboardingSystem system = new OnboardingSystem();
        system.attach(settings, repository);
        system.beginIfUnseen(false);
        assertFalse(system.handleTap(GameState.ARENA_CENTER_X, GameState.ARENA_CENTER_Y),
            "a tap on the arena is play input: the caller must still walk the Hero");
        assertTrue(system.active());
        assertFalse(system.handleTap(
            OnboardingTouchLayout.skipX() - 40f, OnboardingTouchLayout.skipY() + 10f
        ), "just outside the Skip target is still play input");
        assertTrue(system.active());
    }

    @Test
    void lootArrivingIsWhatTheOneLessonTheTouchLayerCannotSeeWaitsFor() {
        OnboardingSystem system = new OnboardingSystem();
        system.attach(settings, repository);
        system.beginIfUnseen(false);
        GameState state = GameState.newRun(0x4845524F444546L);
        system.observe(state);
        // Target, step, ultimate and brace first: the coach is strict about order, A1 put the walk
        // second and A5 put the shield right behind the ultimate.
        system.notify(OnboardingStep.TARGET.action());
        system.notify(OnboardingStep.MOVE.action());
        system.notify(OnboardingStep.ULTIMATE.action());
        system.notify(OnboardingStep.BRACE.action());
        assertEquals(OnboardingStep.LOOT, system.currentStep());
        system.observe(state);
        assertEquals(OnboardingStep.LOOT, system.currentStep(), "nothing arrived, so nothing was learned");
        state.coins += 3;
        system.observe(state);
        assertEquals(OnboardingStep.CARD, system.currentStep(), "a coin reached the Hero and the lesson moved on");
    }

    @Test
    void aTapOnSkipDuringTheLastStepIsStillJustASkip() {
        OnboardingSystem system = new OnboardingSystem();
        system.attach(settings, repository);
        system.beginIfUnseen(false);
        for (int step = 0; step < OnboardingStep.values().length - 1; step++) {
            system.notify(system.currentStep().action());
        }
        assertTrue(system.active());
        assertTrue(system.handleTap(
            OnboardingTouchLayout.skipX() + 10f, OnboardingTouchLayout.skipY() + 10f
        ));
        assertTrue(system.skipped(), "the player stopped one line short: that is a skip, and it is recorded");
    }

    @Test
    void aCoachWithoutPersistenceStillTeachesButCannotRemember() {
        OnboardingSystem system = new OnboardingSystem();
        system.beginIfUnseen(false);
        assertFalse(system.active(), "with no settings attached there is nothing to teach a player about");
        system.attach(new GameSettings(), null);
        system.beginIfUnseen(false);
        assertTrue(system.active());
        runToTheEnd(system);
        assertTrue(system.seen(), "the flag is set on the object in hand even when there is no store");
    }

    private void runToTheEnd(OnboardingSystem system) {
        for (int guard = 0; guard < 500 && system.active(); guard++) {
            if (system.active()) {
                system.notify(system.currentStep().action());
            } else {
                system.update(STEP);
            }
        }
    }

    /** Minimal in-memory Preferences, so the repository round-trip runs without a device. */
    private static final class MemoryPreferences implements Preferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override public Preferences putBoolean(String key, boolean val) { values.put(key, val); return this; }
        @Override public Preferences putInteger(String key, int val) { values.put(key, val); return this; }
        @Override public Preferences putLong(String key, long val) { values.put(key, val); return this; }
        @Override public Preferences putFloat(String key, float val) { values.put(key, val); return this; }
        @Override public Preferences putString(String key, String val) { values.put(key, val); return this; }
        @Override public Preferences put(Map<String, ?> vals) { values.putAll(vals); return this; }
        @Override public boolean getBoolean(String key) { return getBoolean(key, false); }
        @Override public int getInteger(String key) { return getInteger(key, 0); }
        @Override public long getLong(String key) { return getLong(key, 0L); }
        @Override public float getFloat(String key) { return getFloat(key, 0f); }
        @Override public String getString(String key) { return getString(key, ""); }
        @Override public boolean getBoolean(String key, boolean defValue) {
            Object value = values.get(key);
            return value instanceof Boolean bool ? bool : defValue;
        }
        @Override public int getInteger(String key, int defValue) {
            Object value = values.get(key);
            return value instanceof Integer integer ? integer : defValue;
        }
        @Override public long getLong(String key, long defValue) {
            Object value = values.get(key);
            return value instanceof Long longValue ? longValue : defValue;
        }
        @Override public float getFloat(String key, float defValue) {
            Object value = values.get(key);
            return value instanceof Float floatValue ? floatValue : defValue;
        }
        @Override public String getString(String key, String defValue) {
            Object value = values.get(key);
            return value instanceof String string ? string : defValue;
        }
        @Override public Map<String, ?> get() { return values; }
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public void clear() { values.clear(); }
        @Override public void remove(String key) { values.remove(key); }
        @Override public void flush() { }
    }
}
