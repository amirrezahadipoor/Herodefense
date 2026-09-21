package com.amirrezahadipoor.herodefense.save;

import com.badlogic.gdx.Preferences;
import com.amirrezahadipoor.herodefense.model.GameState;

import java.util.Optional;

/**
 * Atomic-ish local Preferences save with one known-good fallback copy.
 *
 * <p><b>The disk leaves the frame thread (roadmap B4).</b> {@link #save} still encodes on the calling thread -- the
 * state belongs to the game and handing a live object to a worker is a race, not a save -- but the two preference
 * writes and the flush go to {@link BackgroundSaveWriter}. The measured cost that made this necessary was 165+ ms
 * of encode-plus-write at the end of a two-hundred-wave run, paid twice per save because the backup key holds the
 * same string, and the same call ran on every level-up. The corpse leak that {@code ReaperSystem} closes took most
 * of that cost with it; this class removes the rest of it from the frame.
 *
 * <p>Every read is a flush. {@link #load}, {@link #hasSave} and {@link #clear} call {@link #flush()} first, so the
 * answer is always about the newest state the game saved, never about the last one the worker got around to. That
 * is what makes the class safe for the caller that saves and immediately asks, which includes every test.
 */
public final class LocalSaveRepository implements RunSaveRepository, AutoCloseable {
    public static final String PREFERENCES_NAME = "hero-defense-local-save";
    private static final String PRIMARY_KEY = "run.primary";
    private static final String BACKUP_KEY = "run.backup";

    private final Preferences preferences;
    private final GameStateCodec codec;
    /** The off-frame writer. One per repository, created lazily by its first save. */
    private final BackgroundSaveWriter writer;

    public LocalSaveRepository(Preferences preferences) {
        this(preferences, new GameStateCodec());
    }

    LocalSaveRepository(Preferences preferences, GameStateCodec codec) {
        if (preferences == null) {
            throw new IllegalArgumentException("preferences cannot be null");
        }
        this.preferences = preferences;
        this.codec = codec;
        this.writer = new BackgroundSaveWriter(this::writePayload);
    }

    /** Writes one payload as the new primary and demotes the previous primary to the backup. Runs on the worker. */
    private void writePayload(String nextJson) {
        String previousJson = preferences.getString(PRIMARY_KEY, "");
        if (!previousJson.isBlank()) {
            preferences.putString(BACKUP_KEY, previousJson);
        }
        preferences.putString(PRIMARY_KEY, nextJson);
        preferences.flush();
    }

    /**
     * Encodes on this thread and hands the bytes to the writer. The previous behaviour -- encode and write inline
     * -- is one {@link #flush()} away: a caller that needs durability now calls it, which is what {@code dispose}
     * does before the process goes away.
     */
    @Override
    public void save(GameState state) {
        writer.submit(codec.encode(state));
    }

    /** Waits for any queued write to reach the preferences. Called by every read; idempotent. */
    public void flush() {
        writer.flush();
    }

    /** Flushes and stops the worker. Idempotent; a save after this one is written inline. */
    @Override
    public void close() {
        writer.close();
    }

    @Override
    public Optional<GameState> load() {
        flush();
        Optional<GameState> primary = decodeSafely(preferences.getString(PRIMARY_KEY, ""));
        if (primary.isPresent()) {
            return primary;
        }
        return decodeSafely(preferences.getString(BACKUP_KEY, ""));
    }

    @Override
    public boolean hasSave() {
        return load().isPresent();
    }

    @Override
    public void clear() {
        flush();
        preferences.remove(PRIMARY_KEY);
        preferences.remove(BACKUP_KEY);
        preferences.flush();
    }

    private Optional<GameState> decodeSafely(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(codec.decode(value));
        } catch (RuntimeException malformedSave) {
            return Optional.empty();
        }
    }
}
