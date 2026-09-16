package com.amirrezahadipoor.herodefense.playtest;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.amirrezahadipoor.herodefense.model.GameState;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One played session, written down (roadmap R3.6).
 *
 * <p>The protocol asks for sessions that are recorded rather than remembered, and a session is only a record if the
 * numbers in it came out of the build that was played. This class is the single writer of the record format: the
 * game calls {@link #fromRun} when a run ends, the simulator capture calls {@link #of} with its own measurements,
 * and both produce the same JSON, so one promotion tool can read either and the ledger cannot drift into a
 * hand-typed shape.
 *
 * <p>The format is deliberately small: what was played, how far it got, and the counters the game already keeps.
 * No personal data, no device identifiers — a session is a build, a mode, a seed and its outcome.
 */
public final class RunRecord {

    /** Bumped when the field set changes, so an old file can be recognised instead of misread. */
    public static final String SCHEMA = "herodefense.run-record/1";

    /** A session played on a real device through the app itself. */
    public static final String SOURCE_DEVICE = "device";

    /** A session driven by the balance simulator, which runs the real systems without rendering. */
    public static final String SOURCE_SIMULATOR = "simulator";

    private final String source;
    private final String platform;
    private final int appVersion;
    private final String mode;
    private final int ascensionTier;
    private final long runSeed;
    private final int wavesCleared;
    private final boolean runCompleted;
    private final boolean heroDied;
    private final String activeTrials;
    private final Map<String, String> measurements;
    private final long recordedAtMillis;

    private RunRecord(
        String source,
        String platform,
        int appVersion,
        String mode,
        int ascensionTier,
        long runSeed,
        int wavesCleared,
        boolean runCompleted,
        boolean heroDied,
        String activeTrials,
        Map<String, String> measurements,
        long recordedAtMillis
    ) {
        this.source = source;
        this.platform = platform;
        this.appVersion = appVersion;
        this.mode = mode;
        this.ascensionTier = ascensionTier;
        this.runSeed = runSeed;
        this.wavesCleared = wavesCleared;
        this.runCompleted = runCompleted;
        this.heroDied = heroDied;
        this.activeTrials = activeTrials;
        this.measurements = new LinkedHashMap<>(measurements);
        this.recordedAtMillis = recordedAtMillis;
    }

    /** The record of a run that just ended inside the real game. */
    public static RunRecord fromRun(GameState state, String platform, int appVersion, long recordedAtMillis) {
        if (state == null) {
            throw new IllegalArgumentException("a run record needs the state it describes");
        }
        Map<String, String> values = new LinkedHashMap<>();
        values.put("heroLevel", Integer.toString(state.heroLevel));
        values.put("totalKills", Integer.toString(state.totalKills));
        values.put("defeatedBosses", Integer.toString(state.defeatedBosses));
        values.put("coins", Integer.toString(state.coins));
        values.put("heartwood", Integer.toString(state.heartwood));
        values.put("potionsUsed", Integer.toString(state.potionsUsedThisRun));
        values.put("plantedTrees", Integer.toString(state.plantedTreesCount));
        values.put("shopStatsBought", Integer.toString(state.shopStatsBoughtThisRun));
        values.put("worldTreeHealth", Float.toString(state.worldTreeHealth));
        values.put("fastestWaveClearSeconds", Float.toString(state.fastestWaveClearSeconds));
        values.put("longestPauseSeconds", Float.toString(state.longestPauseSeconds));
        values.put("heroDied", Boolean.toString(state.heroDiedThisRun));
        values.put("noPotionRun", Boolean.toString(state.noPotionRun));
        values.put("epilogue", state.epilogueId == null ? "" : state.epilogueId);
        return new RunRecord(
            SOURCE_DEVICE,
            platform == null ? "unknown" : platform,
            appVersion,
            state.mode.name(),
            state.ascensionTier,
            state.runSeed,
            state.peakWaveReached,
            state.runComplete,
            state.heroDiedThisRun,
            join(state.activeTrials),
            values,
            recordedAtMillis
        );
    }

    /**
     * A record assembled from measurements taken outside the game object (the simulator capture). The measurements
     * are kept as text so a session written this year still reads correctly next year.
     */
    public static RunRecord of(
        String source,
        String platform,
        String mode,
        long runSeed,
        int wavesCleared,
        int ascensionTier,
        boolean runCompleted,
        boolean heroDied,
        Map<String, String> measurements,
        long recordedAtMillis
    ) {
        return new RunRecord(
            source, platform, 0, mode, Math.max(0, ascensionTier), runSeed, wavesCleared, runCompleted, heroDied, "",
            measurements, recordedAtMillis
        );
    }

    private static String join(Iterable<String> values) {
        StringBuilder joined = new StringBuilder();
        for (String value : values) {
            if (joined.length() > 0) {
                joined.append(',');
            }
            joined.append(value);
        }
        return joined.toString();
    }

    /**
     * The record as the JSON the promotion tool reads. Written with libGDX's own writer rather than by hand, so the
     * escaping of a stray quote or a non-Latin character is the library's problem and not a source of a broken
     * session file; the field order is fixed, which keeps a diff of two records readable.
     */
    public String toJson() {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        root.addChild("schema", new JsonValue(SCHEMA));
        root.addChild("source", new JsonValue(source));
        root.addChild("platform", new JsonValue(platform));
        root.addChild("appVersion", new JsonValue(appVersion));
        root.addChild("mode", new JsonValue(mode));
        root.addChild("ascensionTier", new JsonValue(ascensionTier));
        root.addChild("runSeed", new JsonValue(runSeed));
        root.addChild("wavesCleared", new JsonValue(wavesCleared));
        root.addChild("runCompleted", new JsonValue(runCompleted));
        root.addChild("heroDied", new JsonValue(heroDied));
        root.addChild("activeTrials", new JsonValue(activeTrials));
        root.addChild("recordedAtMillis", new JsonValue(recordedAtMillis));
        JsonValue measured = new JsonValue(JsonValue.ValueType.object);
        for (Map.Entry<String, String> entry : measurements.entrySet()) {
            measured.addChild(entry.getKey(), new JsonValue(entry.getValue()));
        }
        root.addChild("measurements", measured);
        return root.prettyPrint(JsonWriter.OutputType.json, 120) + "\n";
    }
}
