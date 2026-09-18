package com.amirrezahadipoor.herodefense.playtest;

import com.badlogic.gdx.files.FileHandle;
import com.amirrezahadipoor.herodefense.model.GameState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Writes run records next to the save and keeps the last few (roadmap R3.6).
 *
 * <p>Where a record goes matters: it has to survive the run it describes, it must not need a permission, and it has
 * to be readable from a laptop with `adb exec-out run-as`, which is how a tester's session reaches the ledger. The
 * app's local storage satisfies all three, and the file name carries the time and the outcome so a directory
 * listing already tells the story.
 *
 * <p>A run ending is written once. Both endings (the hero fell, the vigil was completed) go through the same
 * {@link #record} call, and a second call for the same run is ignored rather than writing a duplicate: the run id
 * is its seed plus the wave it ended on plus how it ended.
 */
public final class RunRecordStore {

    /** How many sessions are kept on the device; older ones are deleted, newest first. */
    public static final int DEFAULT_KEEP = 12;

    private final FileHandle directory;
    private final int keep;
    private String lastRunId = "";

    public RunRecordStore(FileHandle directory) {
        this(directory, DEFAULT_KEEP);
    }

    public RunRecordStore(FileHandle directory, int keep) {
        if (directory == null) {
            throw new IllegalArgumentException("a record store needs a directory");
        }
        if (keep < 1) {
            throw new IllegalArgumentException("keep must be at least one record, got " + keep);
        }
        this.directory = directory;
        this.keep = keep;
    }

    /**
     * Writes the record of a finished run and returns the file it wrote, or {@code null} when this run was already
     * recorded. A failed write is not allowed to take the game down with it: the run is over, and a playtester
     * losing their session record is a smaller problem than the game crashing on the game-over screen.
     */
    public FileHandle record(GameState state, String platform, int appVersion, long recordedAtMillis) {
        String runId = state.runSeed + "-" + state.peakWaveReached + "-" + state.runComplete + "-" + state.heroDiedThisRun;
        if (runId.equals(lastRunId)) {
            return null;
        }
        lastRunId = runId;
        FileHandle file = directory.child(fileName(state, recordedAtMillis));
        try {
            directory.mkdirs();
            file.writeString(RunRecord.fromRun(state, platform, appVersion, recordedAtMillis).toJson(), false, "UTF-8");
            prune();
            return file;
        } catch (RuntimeException failure) {
            return null;
        }
    }

    /** Session files on disk, newest first. */
    public List<FileHandle> sessions() {
        List<FileHandle> files = new ArrayList<>();
        if (!directory.exists()) {
            return files;
        }
        FileHandle[] listed = directory.list();
        if (listed != null) {
            for (FileHandle file : listed) {
                if (file.name().startsWith("run-") && file.name().endsWith(".json")) {
                    files.add(file);
                }
            }
        }
        files.sort(Comparator.comparing(FileHandle::name).reversed());
        return files;
    }

    private void prune() {
        List<FileHandle> files = sessions();
        for (int index = keep; index < files.size(); index++) {
            files.get(index).delete();
        }
    }

    private static String fileName(GameState state, long recordedAtMillis) {
        String ending = state.runComplete ? "completed" : state.heroDiedThisRun ? "died" : "ended";
        return "run-" + recordedAtMillis + "-w" + state.peakWaveReached + "-" + ending + ".json";
    }
}
