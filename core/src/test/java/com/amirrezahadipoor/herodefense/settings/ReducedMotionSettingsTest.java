package com.amirrezahadipoor.herodefense.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.input.SettingsTouchController;
import com.amirrezahadipoor.herodefense.input.SettingsTouchLayout;
import com.amirrezahadipoor.herodefense.polish.ReducedMotion;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.badlogic.gdx.Preferences;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Roadmap G3a: the reduced-motion preference, from the row that sets it to the frame that honours it.
 *
 * <p>Four links, each of which can break on its own. A row that toggles a field nobody reads is a placebo; a
 * field that is read but never saved comes back wrong after a relaunch; a saved field that the composer stops
 * consulting silently un-ships the feature. So the tap, the persistence, the gate's meaning and the gate's use
 * are all asserted here, and the last one is asserted against the composer's source, because the composer needs
 * a GL context to run and a preference that can only be verified on a device is a preference that will rot.
 */
final class ReducedMotionSettingsTest {

    private static final Path MAIN = Path.of("src", "main", "java", "com", "amirrezahadipoor", "herodefense");

    @Test
    void theRowTogglesThePreferenceAndTheRowIsTheOneTheRendererDraws() {
        GameSettings settings = new GameSettings();
        assertFalse(settings.reducedMotion, "a fresh install keeps the motion the game was built with");
        SettingsTouchController touch = new SettingsTouchController();
        float centreX = SettingsTouchLayout.ROW_X + SettingsTouchLayout.ROW_WIDTH * 0.5f;
        float rowY = SettingsTouchLayout.REDUCED_MOTION_ROW_Y + 40f;

        assertEquals(SettingsTouchLayout.Action.TOGGLE_REDUCED_MOTION, touch.tap(settings, centreX, rowY));
        assertTrue(settings.reducedMotion, "one tap, one change");
        touch.tap(settings, centreX, rowY);
        assertFalse(settings.reducedMotion, "and the same row turns it back off");

        assertTrue(SettingsTouchLayout.REDUCED_MOTION_ROW_Y > 260f,
            "the footer note panel occupies 140f..260f, so the row has to sit above it");
        assertTrue(SettingsTouchLayout.LANGUAGE_ROW_Y - SettingsTouchLayout.REDUCED_MOTION_ROW_Y
                >= SettingsTouchLayout.ROW_HEIGHT, "and it must not overlap the row above it");
    }

    @Test
    void thePreferenceSurvivesARelaunch() {
        MemoryPreferences preferences = new MemoryPreferences();
        LocalSettingsRepository repository = new LocalSettingsRepository(preferences);
        GameSettings settings = new GameSettings();
        settings.reducedMotion = true;
        repository.save(settings);

        assertTrue(repository.load().reducedMotion, "the choice is device-local and outlives the run");
        assertFalse(new LocalSettingsRepository(new MemoryPreferences()).load().reducedMotion,
            "and a device that never asked for it does not get it");
    }

    @Test
    void theGateMeansWhatTheSettingSays() {
        GameSettings settings = new GameSettings();
        assertFalse(ReducedMotion.suppresses(settings));
        settings.reducedMotion = true;
        assertTrue(ReducedMotion.suppresses(settings));
        assertFalse(ReducedMotion.suppresses(null), "a missing settings object means motion, not a crash");
    }

    @Test
    void theShakeThePreferenceRemovesIsShakeThatExists() {
        // The gate is only worth anything if the thing it removes is real, so this asserts the impulse itself:
        // the strongest kick in the game has to move the camera at some point inside its own duration. If this
        // fails, ReducedMotion is suppressing nothing and the settings row is a placebo.
        ScreenShakeSystem shake = new ScreenShakeSystem();
        shake.triggerUltimate();
        assertTrue(shake.active(), "the ultimate kick is running");
        boolean moved = false;
        for (int frame = 0; frame < 40 && shake.active(); frame++) {
            shake.update(0.016f);
            if (shake.offsetX() != 0f || shake.offsetY() != 0f) {
                moved = true;
            }
        }
        assertTrue(moved, "a shake that never offsets the camera is not the thing the preference removes");
        assertFalse(shake.active(), "and it ends inside its own duration rather than running forever");
    }

    @Test
    void theComposerConsultsTheGateForBothSourcesOfMotion() throws IOException {
        String composer = Files.readString(
            MAIN.resolve("presentation/ScreenStateComposer.java"), StandardCharsets.UTF_8);
        assertTrue(composer.contains("ReducedMotion.suppresses(host.settings())"),
            "the composer stopped asking the preference: the camera shake is back whether the player wants it"
                + " or not");
        assertTrue(composer.contains("if (!reducedMotion) {"),
            "the ambient spore drift is drawn unconditionally again");
        assertTrue(composer.contains("baseCameraX + shakeX") && composer.contains("baseCameraY + shakeY"),
            "the camera is reading the shake system directly instead of the gated offsets");
    }

    private static final class MemoryPreferences implements Preferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override public Preferences putBoolean(String key, boolean val) { values.put(key, val); return this; }
        @Override public Preferences putInteger(String key, int val) { values.put(key, val); return this; }
        @Override public Preferences putLong(String key, long val) { values.put(key, val); return this; }
        @Override public Preferences putFloat(String key, float val) { values.put(key, val); return this; }
        @Override public Preferences putString(String key, String val) { values.put(key, val); return this; }
        @Override public Preferences put(Map<String, ?> vals) { values.putAll(vals); return this; }
        @Override public boolean getBoolean(String key) { return (Boolean) values.getOrDefault(key, false); }
        @Override public int getInteger(String key) { return (Integer) values.getOrDefault(key, 0); }
        @Override public long getLong(String key) { return (Long) values.getOrDefault(key, 0L); }
        @Override public float getFloat(String key) { return (Float) values.getOrDefault(key, 0f); }
        @Override public String getString(String key) { return (String) values.get(key); }
        @Override public boolean getBoolean(String key, boolean def) { return (Boolean) values.getOrDefault(key, def); }
        @Override public int getInteger(String key, int def) { return (Integer) values.getOrDefault(key, def); }
        @Override public long getLong(String key, long def) { return (Long) values.getOrDefault(key, def); }
        @Override public float getFloat(String key, float def) { return (Float) values.getOrDefault(key, def); }
        @Override public String getString(String key, String def) { return (String) values.getOrDefault(key, def); }
        @Override public Map<String, ?> get() { return values; }
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public void clear() { values.clear(); }
        @Override public void remove(String key) { values.remove(key); }
        @Override public Preferences flush() { return this; }
    }
}
