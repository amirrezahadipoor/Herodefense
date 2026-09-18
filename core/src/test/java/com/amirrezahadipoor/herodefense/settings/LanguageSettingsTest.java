package com.amirrezahadipoor.herodefense.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * The settings screen's own language row (roadmap R7.3): a tap changes the language, the change is
 * written to the device's preferences, and every word the settings screen draws afterwards is the
 * word of that language. The locale is global state, so each test puts it back the way it found it.
 */
final class LanguageSettingsTest {
    /** The locale is process-wide, so a test that changes it owes the next test the one it started with. */
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
    void tappingTheLanguageRowCyclesItAndTheLocaleFollows() {
        GameSettings settings = new GameSettings();
        SettingsTouchController touch = new SettingsTouchController();
        float centreX = SettingsTouchLayout.ROW_X + SettingsTouchLayout.ROW_WIDTH * 0.5f;

        assertEquals(
            SettingsTouchLayout.Action.CYCLE_LANGUAGE,
            touch.tap(settings, centreX, SettingsTouchLayout.LANGUAGE_ROW_Y + 40f)
        );
        assertEquals(GameLanguage.PERSIAN, settings.language, "one tap, one step");
        assertEquals(GameLanguage.PERSIAN, GameLocale.current(), "and the game speaks it immediately");

        touch.tap(settings, centreX, SettingsTouchLayout.LANGUAGE_ROW_Y + 40f);
        assertEquals(GameLanguage.ENGLISH, settings.language);
        assertEquals(GameLanguage.ENGLISH, GameLocale.current());
    }

    @Test
    void theChoiceSurvivesTheNextLaunchAndAnUnknownCodeFallsBackToEnglish() {
        MemoryPreferences preferences = new MemoryPreferences();
        LocalSettingsRepository repository = new LocalSettingsRepository(preferences);
        GameSettings settings = new GameSettings();
        settings.language = GameLanguage.PERSIAN;
        repository.save(settings);
        GameLocale.use(GameLanguage.ENGLISH);

        assertEquals(GameLanguage.PERSIAN, repository.load().language, "the code round-trips");
        assertEquals(GameLanguage.PERSIAN, GameLocale.current(), "and loading is what makes the game speak it");

        preferences.putString("display.language", "klingon");
        assertEquals(GameLanguage.ENGLISH, repository.load().language, "an unknown code cannot break the game");
        assertEquals(GameLanguage.ENGLISH, GameLocale.current());
    }

    @Test
    void theSettingWordsSpeakTheChosenLanguage() {
        GameSettings settings = new GameSettings();
        assertEquals(GameLanguage.ENGLISH, settings.language, "a fresh install speaks English");
        assertEquals("FULL", GameSettings.levelLabel(2));
        assertEquals("NORMAL", GameSettings.levelLabel(1));

        GameLocale.use(GameLanguage.PERSIAN);
        assertEquals("کامل", GameSettings.levelLabel(2));
        assertEquals("عادی", GameSettings.levelLabel(1));
        assertEquals("آرام", GameSettings.levelLabel(0));
        // The row that cycles a level returns the word it landed on, so the screen and the setting agree
        // in the language the player just chose rather than the one the build was compiled in.
        assertEquals("آرام", settings.cycleSoundVolume(), "the level row wraps to the quietest step");
    }

    @Test
    void theLanguageRowClearsTheFooterPanelAndDoesNotOverlapTheLevelRows() {
        assertTrue(SettingsTouchLayout.LANGUAGE_ROW_Y > 260f,
            "the footer note panel occupies 140f..260f, so a row below it would be unreadable");
        assertTrue(SettingsTouchLayout.MUSIC_LEVEL_ROW_Y - SettingsTouchLayout.LANGUAGE_ROW_Y
                >= SettingsTouchLayout.ROW_HEIGHT, "and it must not overlap the row above it");
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
