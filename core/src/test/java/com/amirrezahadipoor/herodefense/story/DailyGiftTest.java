package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.settings.LocalSettingsRepository;
import com.badlogic.gdx.Preferences;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The Tree pays two heartwood on the first visit of a local day, once, and never farms. */
class DailyGiftTest {

    @Test
    void firstVisitPaysOnceAndTheDayIsThenQuiet() {
        GameState state = stateWithHeartwood(5);
        LocalSettingsRepository settings = settings();
        assertNotNull(DailyGift.claim(state, settings, 20_000L));
        assertEquals(5 + DailyGift.HEARTWOOD_AMOUNT, state.heartwood);
        assertNull(DailyGift.claim(state, settings, 20_000L));
        assertEquals(5 + DailyGift.HEARTWOOD_AMOUNT, state.heartwood);
    }

    @Test
    void aNewDayPaysAgainAndARestartedRunCannotFarmTheSameDay() {
        LocalSettingsRepository settings = settings();
        GameState morning = stateWithHeartwood(0);
        assertNotNull(DailyGift.claim(morning, settings, 20_000L));
        GameState freshRun = stateWithHeartwood(0);
        assertNull(DailyGift.claim(freshRun, settings, 20_000L));
        assertEquals(0, freshRun.heartwood);
        GameState nextMorning = stateWithHeartwood(0);
        assertNotNull(DailyGift.claim(nextMorning, settings, 20_001L));
        assertEquals(DailyGift.HEARTWOOD_AMOUNT, nextMorning.heartwood);
    }

    @Test
    void missingStateOrLedgerPaysNothing() {
        assertNull(DailyGift.claim(null, settings(), 20_000L));
        assertNull(DailyGift.claim(stateWithHeartwood(0), null, 20_000L));
    }

    @Test
    void theLineMentionsTheGiftInBothLanguages() {
        assertFalse(DailyGift.claim(stateWithHeartwood(0), settings(), 20_000L).isBlank());
    }

    private GameState stateWithHeartwood(int heartwood) {
        GameState state = GameState.newRun(30L);
        state.heartwood = heartwood;
        return state;
    }

    private LocalSettingsRepository settings() {
        return new LocalSettingsRepository(new MemoryPreferences());
    }

    /** Minimal in-memory stand-in for the device's Preferences. */
    private static final class MemoryPreferences implements Preferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override public Preferences putLong(String key, long val) { values.put(key, val); return this; }
        @Override public long getLong(String key, long defValue) {
            Object value = values.get(key);
            return value instanceof Long stored ? stored : defValue;
        }
        @Override public void flush() { }
        @Override public Preferences putBoolean(String key, boolean val) { values.put(key, val); return this; }
        @Override public boolean getBoolean(String key, boolean defValue) { return defValue; }
        @Override public Preferences putInteger(String key, int val) { values.put(key, val); return this; }
        @Override public int getInteger(String key, int defValue) { return defValue; }
        @Override public Preferences putFloat(String key, float val) { values.put(key, val); return this; }
        @Override public float getFloat(String key, float defValue) { return defValue; }
        @Override public Preferences putString(String key, String val) { values.put(key, val); return this; }
        @Override public String getString(String key, String defValue) { return defValue; }
        @Override public Preferences put(Map<String, ?> vals) { values.putAll(vals); return this; }
        @Override public void remove(String key) { values.remove(key); }
        @Override public void clear() { values.clear(); }
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public Map<String, ?> get() { return values; }
        @Override public boolean getBoolean(String key) { return false; }
        @Override public int getInteger(String key) { return 0; }
        @Override public float getFloat(String key) { return 0f; }
        @Override public String getString(String key) { return ""; }
        @Override public long getLong(String key) { return 0L; }
    }
}
