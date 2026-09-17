package com.amirrezahadipoor.herodefense.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.settings.GameSettings;
import com.amirrezahadipoor.herodefense.settings.LocalSettingsRepository;
import com.badlogic.gdx.Preferences;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Roadmap R6.4: the audio settings persist, and a hand-edited file cannot break the screen. */
final class AudioLevelsSettingsTest {

    @Test
    void theLevelsCycleThroughThreeNamedStepsAndWrapAround() {
        GameSettings settings = new GameSettings();
        assertEquals("FULL", GameSettings.levelLabel(GameSettings.levelIndex(settings.musicVolume)));
        assertEquals("QUIET", settings.cycleMusicVolume());
        assertEquals("NORMAL", settings.cycleMusicVolume());
        assertEquals("FULL", settings.cycleMusicVolume(), "the third tap returns to full");
        assertEquals("QUIET", settings.cycleSoundVolume());
        assertTrue(settings.soundVolume < 1f && settings.soundVolume > 0f);
    }

    @Test
    void levelsSurviveAReloadAndNonsenseValuesSnapToTheNearestStep() {
        MemoryPreferences preferences = new MemoryPreferences();
        LocalSettingsRepository repository = new LocalSettingsRepository(preferences);
        GameSettings settings = new GameSettings();
        settings.cycleMusicVolume();
        settings.cycleSoundVolume();
        settings.cycleSoundVolume();
        repository.save(settings);
        assertTrue(preferences.flushed);

        GameSettings loaded = repository.load();
        assertEquals(settings.musicVolume, loaded.musicVolume);
        assertEquals(settings.soundVolume, loaded.soundVolume);

        // A preference file written by hand (or by an older build) must not produce a level the screen cannot
        // label: the value is snapped to the closest named step on load.
        preferences.putFloat("audio.musicVolume", 0.62f);
        GameSettings snapped = repository.load();
        assertEquals(GameSettings.levelValue(GameSettings.levelIndex(0.62f)), snapped.musicVolume);
        assertEquals("NORMAL", GameSettings.levelLabel(GameSettings.levelIndex(snapped.musicVolume)));
    }

    private static final class MemoryPreferences implements Preferences {
        private final Map<String, Object> values = new HashMap<>();
        private boolean flushed;

        @Override public Preferences putBoolean(String key, boolean value) {
            values.put(key, value);
            return this;
        }

        @Override public Preferences putInteger(String key, int value) {
            values.put(key, value);
            return this;
        }

        @Override public Preferences putLong(String key, long value) {
            values.put(key, value);
            return this;
        }

        @Override public Preferences putFloat(String key, float value) {
            values.put(key, value);
            return this;
        }

        @Override public Preferences putString(String key, String value) {
            values.put(key, value);
            return this;
        }

        @Override public Preferences put(Map<String, ?> map) {
            values.putAll(map);
            return this;
        }

        @Override public boolean getBoolean(String key) {
            return getBoolean(key, false);
        }

        @Override public int getInteger(String key) {
            return getInteger(key, 0);
        }

        @Override public long getLong(String key) {
            return getLong(key, 0L);
        }

        @Override public float getFloat(String key) {
            return getFloat(key, 0f);
        }

        @Override public String getString(String key) {
            return getString(key, "");
        }

        @Override public boolean getBoolean(String key, boolean defaultValue) {
            Object value = values.get(key);
            return value == null ? defaultValue : (Boolean) value;
        }

        @Override public int getInteger(String key, int defaultValue) {
            Object value = values.get(key);
            return value == null ? defaultValue : (Integer) value;
        }

        @Override public long getLong(String key, long defaultValue) {
            Object value = values.get(key);
            return value == null ? defaultValue : (Long) value;
        }

        @Override public float getFloat(String key, float defaultValue) {
            Object value = values.get(key);
            return value == null ? defaultValue : (Float) value;
        }

        @Override public String getString(String key, String defaultValue) {
            Object value = values.get(key);
            return value == null ? defaultValue : (String) value;
        }

        @Override public boolean contains(String key) {
            return values.containsKey(key);
        }

        @Override public void clear() {
            values.clear();
        }

        @Override public void remove(String key) {
            values.remove(key);
        }

        @Override public void flush() {
            flushed = true;
        }

        @Override public Map<String, ?> get() {
            return Map.copyOf(values);
        }
    }
}
