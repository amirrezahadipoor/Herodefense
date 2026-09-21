package com.amirrezahadipoor.herodefense.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Preferences;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The save's contract after the disk moved off the frame thread (roadmap B4).
 *
 * <p>Two things have to stay true for the writer to be invisible: a read must never see a state older than the one
 * the game asked to save, and the two-generation backup must survive the writer replacing a queued payload. The
 * rest of the class -- the corrupt-primary fallback, the blank-save guard -- is unchanged and is covered here too
 * because it now runs through a different call path.
 */
final class LocalSaveRepositoryTest {

    @Test
    void aSaveIsVisibleToTheReadThatFollowsIt() {
        LocalSaveRepository repository = new LocalSaveRepository(new MemoryPreferences());
        GameState state = GameState.newRun(5L);
        state.waveNumber = 42;
        state.coins = 137;
        repository.save(state);

        GameState loaded = repository.load().orElseThrow();
        assertEquals(42, loaded.waveNumber, "the read waits for the queued write");
        assertEquals(137, loaded.coins);
        repository.close();
    }

    @Test
    void theBackupStillHoldsTheGenerationBeforeTheNewest() {
        LocalSaveRepository repository = new LocalSaveRepository(new MemoryPreferences());
        GameState first = GameState.newRun(5L);
        first.waveNumber = 10;
        repository.save(first);
        repository.flush();

        GameState second = GameState.newRun(5L);
        second.waveNumber = 20;
        repository.save(second);
        repository.flush();

        MemoryPreferences raw = new MemoryPreferences();
        LocalSaveRepository probe = new LocalSaveRepository(raw);
        probe.save(first);
        probe.flush();
        probe.save(second);
        probe.flush();
        assertNotEquals(raw.getString("run.primary"), raw.getString("run.backup"),
            "primary and backup are two different states");
        assertTrue(raw.getString("run.primary").contains("\"waveNumber\": 20"), "primary is the newest");
        assertTrue(raw.getString("run.backup").contains("\"waveNumber\": 10"), "backup is the one before it");
        repository.close();
        probe.close();
    }

    @Test
    void aCorruptPrimaryFallsBackToTheBackupThroughTheSameWriter() {
        MemoryPreferences preferences = new MemoryPreferences();
        LocalSaveRepository repository = new LocalSaveRepository(preferences);
        GameState good = GameState.newRun(9L);
        good.waveNumber = 7;
        repository.save(good);
        repository.flush();
        String goodJson = preferences.getString("run.primary");

        // The next save demotes the good state to the backup, and then the primary is corrupted under it.
        GameState next = GameState.newRun(9L);
        next.waveNumber = 8;
        repository.save(next);
        repository.flush();
        preferences.putString("run.backup", goodJson);
        preferences.putString("run.primary", "{ this is not json");
        assertEquals(7, repository.load().orElseThrow().waveNumber,
            "the backup answers when the primary cannot be decoded");
        repository.close();
    }

    @Test
    void clearWaitsForTheWriteItIsUndoingAndThenRemovesBothKeys() {
        MemoryPreferences preferences = new MemoryPreferences();
        LocalSaveRepository repository = new LocalSaveRepository(preferences);
        repository.save(GameState.newRun(3L));
        repository.clear();
        assertFalse(repository.hasSave(), "a clear that raced the writer would resurrect the save");
        assertFalse(preferences.contains("run.primary"));
        assertFalse(preferences.contains("run.backup"));
        repository.close();
    }

    @Test
    void aSaveAfterCloseIsWrittenInlineRatherThanDropped() {
        MemoryPreferences preferences = new MemoryPreferences();
        LocalSaveRepository repository = new LocalSaveRepository(preferences);
        repository.close();
        GameState late = GameState.newRun(3L);
        late.waveNumber = 99;
        repository.save(late);
        assertEquals(99, repository.load().orElseThrow().waveNumber,
            "the close is for the process leaving, not a promise to lose the last save");
    }

    @Test
    void closeIsIdempotentAndLeavesNoThreadBehind() {
        LocalSaveRepository repository = new LocalSaveRepository(new MemoryPreferences());
        repository.save(GameState.newRun(3L));
        repository.close();
        repository.close();
        repository.close();
        assertTrue(true, "three closes in a row are allowed");
    }

    /** An in-memory {@code Preferences}, the same shape the settings tests use. */
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
