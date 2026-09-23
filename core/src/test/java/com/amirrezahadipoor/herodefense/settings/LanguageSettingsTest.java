package com.amirrezahadipoor.herodefense.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.input.SettingsTouchController;
import com.amirrezahadipoor.herodefense.input.SettingsTouchLayout;
import com.badlogic.gdx.Preferences;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The game speaks English, and the settings layer keeps it that way (roadmap R7.3, retired).
 *
 * <p>Until 2026-09-23 this was the settings screen's language row: a tap cycled the language, the choice was
 * written to the device's preferences, and a Persian device opened in Persian. The owner deleted the Persian
 * translation outright, so the row is gone -- and what these pin is the tombstone: nothing to tap, nothing
 * persisted, and a preference file from a bilingual build cannot break the game. The locale is global state, so
 * each test puts it back the way it found it.
 */
final class LanguageSettingsTest {
    /** The locale is process-wide, so a test that touches it owes the next test the one it started with. */
    private GameLanguage before;

    @BeforeEach
    void rememberTheLanguage() {
        before = GameLocale.current();
    }

    @AfterEach
    void restoreTheLanguage() {
        GameLocale.use(before);
    }

    @Test
    void theSettingWordsSpeakEnglish() {
        GameSettings settings = new GameSettings();
        assertEquals("FULL", GameSettings.levelLabel(2));
        assertEquals("NORMAL", GameSettings.levelLabel(1));
        assertEquals("QUIET", GameSettings.levelLabel(0));
        // The row that cycles a level returns the word it landed on, so the screen and the setting agree.
        assertEquals("QUIET", settings.cycleSoundVolume(), "the level row wraps to the quietest step");
    }

    @Test
    void aLegacyLanguagePreferenceIsIgnoredAndNeverWritten() {
        MemoryPreferences preferences = new MemoryPreferences();
        preferences.putString("display.language", "fa");
        GameLocale.use(GameLanguage.ENGLISH);
        new LocalSettingsRepository(preferences).load();
        assertEquals(GameLanguage.ENGLISH, GameLocale.current(),
            "a bilingual build's preference cannot change what the game speaks");

        // Saving never writes the dead key: a device that never had it stays without it, and one that still
        // carries it is read past, never re-armed.
        MemoryPreferences fresh = new MemoryPreferences();
        new LocalSettingsRepository(fresh).save(new GameSettings());
        assertFalse(fresh.contains("display.language"), "saving never writes the dead key back");
    }

    @Test
    void theRowsWhereTheLanguageRowWasBelongToOtherSettingsNow() {
        SettingsTouchController touch = new SettingsTouchController();
        GameSettings settings = new GameSettings();
        float centreX = SettingsTouchLayout.ROW_X + SettingsTouchLayout.ROW_WIDTH * 0.5f;
        assertEquals(10, SettingsTouchLayout.TOTAL_ROWS);
        assertEquals(SettingsTouchLayout.Action.TOGGLE_REDUCED_MOTION,
            touch.tap(settings, centreX, 400f + 40f),
            "reduced motion moved into the deleted row's slot");
        assertTrue(settings.reducedMotion, "and the tap flips it, not a language");
    }

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
            Object value = values.get(key); return value instanceof Boolean b ? b : defValue;
        }
        @Override public int getInteger(String key, int defValue) {
            Object value = values.get(key); return value instanceof Integer i ? i : defValue;
        }
        @Override public long getLong(String key, long defValue) {
            Object value = values.get(key); return value instanceof Long l ? l : defValue;
        }
        @Override public float getFloat(String key, float defValue) {
            Object value = values.get(key); return value instanceof Float f ? f : defValue;
        }
        @Override public String getString(String key, String defValue) {
            Object value = values.get(key); return value instanceof String s ? s : defValue;
        }
        @Override public Map<String, ?> get() { return values; }
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public void clear() { values.clear(); }
        @Override public void remove(String key) { values.remove(key); }
        @Override public void flush() { }
    }
}
