package com.amirrezahadipoor.herodefense.playtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.amirrezahadipoor.herodefense.model.GameMode;
import com.amirrezahadipoor.herodefense.model.GameState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * The session record is the evidence a playtest leaves behind (roadmap R3.6), so these cases check the three things
 * that make it evidence rather than a log line: it parses, it carries the run it describes, and it cannot grow
 * without bound or take the game down when the storage says no.
 */
final class RunRecordStoreTest {

    private Path directory;

    @AfterEach
    void removeTemporaryDirectory() throws IOException {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        try (Stream<Path> entries = Files.walk(directory)) {
            for (Path path : entries.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private FileHandle temporaryDirectory() throws IOException {
        directory = Files.createTempDirectory("run-records");
        return new FileHandle(directory.toFile());
    }

    private static GameState finishedRun(long seed, int waveReached) {
        GameState state = GameState.newRun(seed);
        state.mode = GameMode.STANDARD;
        state.waveNumber = waveReached;
        state.peakWaveReached = waveReached;
        state.totalKills = 418;
        state.defeatedBosses = 5;
        state.coins = 930;
        state.heroLevel = 24;
        state.potionsUsedThisRun = 2;
        state.heroDiedThisRun = true;
        state.hero.alive = false;
        return state;
    }

    @Test
    void theRecordCarriesTheRunItDescribes() throws IOException {
        RunRecordStore store = new RunRecordStore(temporaryDirectory());
        GameState state = finishedRun(0x4845524F444546L, 37);

        FileHandle written = store.record(state, "Android", 1, 1_758_000_000_000L);

        assertNotNull(written, "a finished run is written");
        assertTrue(written.name().startsWith("run-1758000000000-w37-"), "the name says when and how far: "
            + written.name());
        JsonValue json = new JsonReader().parse(written.readString("UTF-8"));
        assertEquals(RunRecord.SCHEMA, json.getString("schema"));
        assertEquals(RunRecord.SOURCE_DEVICE, json.getString("source"));
        assertEquals("Android", json.getString("platform"));
        assertEquals("STANDARD", json.getString("mode"));
        assertEquals(0x4845524F444546L, json.getLong("runSeed"));
        assertEquals(37, json.getInt("wavesCleared"));
        assertTrue(json.getBoolean("heroDied"));
        assertFalse(json.getBoolean("runCompleted"));
        assertEquals(1_758_000_000_000L, json.getLong("recordedAtMillis"));
        assertEquals("418", json.get("measurements").getString("totalKills"));
        assertEquals("5", json.get("measurements").getString("defeatedBosses"));
        assertEquals("2", json.get("measurements").getString("potionsUsed"));
    }

    @Test
    void theSameRunIsOnlyRecordedOnce() throws IOException {
        RunRecordStore store = new RunRecordStore(temporaryDirectory());
        GameState state = finishedRun(7L, 12);

        assertNotNull(store.record(state, "Android", 1, 1_000L), "the first call writes the session");
        assertNull(store.record(state, "Android", 1, 1_001L),
            "the game-over presentation may run again; the session must not be written twice");
        assertEquals(1, store.sessions().size());
    }

    @Test
    void onlyTheNewestSessionsAreKept() throws IOException {
        RunRecordStore store = new RunRecordStore(temporaryDirectory(), 3);

        for (int index = 0; index < 6; index++) {
            GameState state = finishedRun(index, 10 + index);
            assertNotNull(store.record(state, "Android", 1, 5_000L + index));
        }

        assertEquals(3, store.sessions().size(), "the cap is what keeps a device from filling up");
        assertEquals("run-5005-w15-died.json", store.sessions().get(0).name(),
            "newest first, so the session a tester just played is the one they pull");
    }

    @Test
    void aRunThatEndsWithoutADeathIsStillRecorded() throws IOException {
        RunRecordStore store = new RunRecordStore(temporaryDirectory());
        GameState state = GameState.newRun(99L);
        state.mode = GameMode.BRIEF;
        state.peakWaveReached = GameMode.BRIEF.waves();
        state.runComplete = true;

        FileHandle written = store.record(state, "Desktop", -1, 2_000L);

        assertNotNull(written);
        assertTrue(written.name().endsWith("-completed.json"), written.name());
        JsonValue json = new JsonReader().parse(written.readString("UTF-8"));
        assertTrue(json.getBoolean("runCompleted"));
        assertFalse(json.getBoolean("heroDied"));
        assertEquals("BRIEF", json.getString("mode"));
    }

    @Test
    void measurementsThatNeedEscapingStayReadable() {
        Map<String, String> measurements = new LinkedHashMap<>();
        measurements.put("note", "quoted \"value\"\nwith a second line\tand a tab");
        measurements.put("unicode", "ویژگی");

        String json = RunRecord.of(RunRecord.SOURCE_SIMULATOR, "headless", "BRIEF", 12L, 30, true, false,
            measurements, 1L).toJson();

        JsonValue parsed = new JsonReader().parse(json);
        assertEquals("quoted \"value\"\nwith a second line\tand a tab",
            parsed.get("measurements").getString("note"));
        assertEquals("ویژگی", parsed.get("measurements").getString("unicode"));
        assertEquals(RunRecord.SOURCE_SIMULATOR, parsed.getString("source"));
    }

    @Test
    void storageThatRefusesTheWriteDoesNotThrow() throws IOException {
        // The handle points at a regular file, so every write below fails the way a full or read-only device does.
        Path file = Files.createTempFile("not-a-directory", ".txt");
        Files.writeString(file, "the game is still allowed to end here", StandardCharsets.UTF_8);
        RunRecordStore store = new RunRecordStore(new FileHandle(file.toFile()), 3);

        assertNull(store.record(finishedRun(1L, 3), "Android", 1, 1L),
            "a session record is not worth crashing the game-over screen for");
        assertTrue(store.sessions().isEmpty());
        Files.deleteIfExists(file);
    }

    @Test
    void theStoreRefusesToBeBuiltWithoutSomewhereToWrite() throws IOException {
        FileHandle directory = temporaryDirectory();
        IllegalArgumentException failure = org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () -> new RunRecordStore(directory, 0));
        assertTrue(failure.getMessage().contains("at least one record"), failure.getMessage());
    }
}
