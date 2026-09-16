package com.amirrezahadipoor.herodefense.playtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.amirrezahadipoor.herodefense.balance.BalanceSimulator;
import com.amirrezahadipoor.herodefense.model.GameMode;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Records an automated session, so a session in the ledger can come from something other than a hand (roadmap R3.6).
 *
 * <p>The balance simulator drives the real combat, economy and progression systems without rendering, which makes it
 * the cheapest honest player this repository has: it is not a human, but its session is a real run of the shipped
 * build with numbers that can be re-derived. This test is the capture step. It writes the same record format the
 * device writes into {@code build/playtests/}, which is where
 * {@code tools/playtests/promote_run_record.py} picks it up:
 *
 * <pre>
 * ./gradlew :core:test --tests "*SimulatorSessionCaptureTest"
 * python3 tools/playtests/promote_run_record.py --record core/build/playtests/simulator-*.json \
 *     --player "balance simulator" --build &lt;commit&gt; --duration 1
 * </pre>
 *
 * <p>It also asserts the run it recorded actually happened — a capture that quietly writes an empty session would
 * be worse than no capture at all.
 */
final class SimulatorSessionCaptureTest {

    /** The short vigil: a full two-hundred-wave sweep takes minutes, and a session record does not need one. */
    private static final long SEED = 0x4845524F444546L;
    private static final Path OUTPUT = Path.of("build", "playtests");

    @Test
    void theSimulatorSessionsAreWrittenWhereTheLedgerToolLooksForThem() {
        BalanceSimulator.BalanceReport report = new BalanceSimulator().runBrief(SEED);

        assertTrue(report.waves().size() >= GameMode.BRIEF.waves() - 1,
            "the capture has to be a real run, saw " + report.waves().size() + " waves");
        float fastest = Float.MAX_VALUE;
        float slowest = 0f;
        float peak = 0f;
        for (BalanceSimulator.WaveSample wave : report.waves()) {
            fastest = Math.min(fastest, wave.clearTimeSeconds());
            slowest = Math.max(slowest, wave.clearTimeSeconds());
            peak = Math.max(peak, wave.damageFraction());
        }

        Map<String, String> measurements = new LinkedHashMap<>();
        measurements.put("averageDamageFraction", Float.toString(report.averageDamageFraction()));
        measurements.put("peakDamageFraction", Float.toString(peak));
        measurements.put("reachedFinalWave", Boolean.toString(report.reachedFinalWave()));
        measurements.put("fastestWaveSeconds", Float.toString(fastest));
        measurements.put("slowestWaveSeconds", Float.toString(slowest));
        measurements.put("waves", Integer.toString(report.waves().size()));

        RunRecord record = RunRecord.of(
            RunRecord.SOURCE_SIMULATOR,
            "headless",
            GameMode.BRIEF.name(),
            SEED,
            report.waves().size(),
            report.reachedFinalWave(),
            !report.reachedFinalWave(),
            measurements,
            System.currentTimeMillis()
        );

        FileHandle directory = new FileHandle(OUTPUT.toFile());
        directory.mkdirs();
        FileHandle file = directory.child("simulator-" + SEED + ".json");
        file.writeString(record.toJson(), false, "UTF-8");

        JsonValue parsed = new JsonReader().parse(file.readString("UTF-8"));
        assertEquals(RunRecord.SCHEMA, parsed.getString("schema"));
        assertEquals(RunRecord.SOURCE_SIMULATOR, parsed.getString("source"));
        assertEquals(SEED, parsed.getLong("runSeed"));
        assertEquals(report.waves().size(), parsed.getInt("wavesCleared"));
        assertEquals(Integer.toString(report.waves().size()),
            parsed.get("measurements").getString("waves"),
            "the record has to describe the run that was just simulated");
        assertTrue(file.file().length() > 200, "and it has to be a file a tool can read: " + file.path());
    }
}
