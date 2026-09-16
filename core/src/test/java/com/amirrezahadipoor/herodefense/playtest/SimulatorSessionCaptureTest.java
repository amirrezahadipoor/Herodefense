package com.amirrezahadipoor.herodefense.playtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.amirrezahadipoor.herodefense.balance.BalanceSimulator;
import com.amirrezahadipoor.herodefense.model.GameMode;
import com.amirrezahadipoor.herodefense.model.GameState;

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
            0,
            report.reachedFinalWave(),
            !report.reachedFinalWave(),
            measurements,
            System.currentTimeMillis()
        );

        FileHandle directory = new FileHandle(OUTPUT.toFile());
        directory.mkdirs();
        FileHandle file = directory.child("simulator-brief-" + SEED + ".json");
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

    /**
     * The long vigil, recorded as its own session. The brief capture proves the build can be played to an ending;
     * this one carries the numbers the curve work needs — the pressure of each fifty-wave quarter, which is how the
     * shallow step at waves 101-150 was found and how a later change is judged.
     */
    @Test
    void theLongVigilIsCapturedWithItsQuarterPressures() {
        BalanceSimulator.BalanceReport report = new BalanceSimulator().run(SEED);
        assertEquals(GameState.FINAL_WAVE, report.waves().size(), "the long vigil is still two hundred waves");
        assertTrue(report.reachedFinalWave(), "and this seed still finishes it");

        float peak = 0f;
        float firstTwenty = 0f;
        float lastTwenty = 0f;
        for (int index = 0; index < report.waves().size(); index++) {
            peak = Math.max(peak, report.waves().get(index).damageFraction());
        }
        for (int index = 0; index < 20; index++) {
            firstTwenty += report.waves().get(index).damageFraction();
            lastTwenty += report.waves().get(report.waves().size() - 20 + index).damageFraction();
        }

        Map<String, String> measurements = new LinkedHashMap<>();
        measurements.put("averageDamageFraction", Float.toString(report.averageDamageFraction()));
        measurements.put("peakDamageFraction", Float.toString(peak));
        measurements.put("firstTwentyWaves", Float.toString(firstTwenty / 20f));
        measurements.put("lastTwentyWaves", Float.toString(lastTwenty / 20f));
        for (int quarter = 0; quarter < 4; quarter++) {
            float total = 0f;
            int count = 0;
            for (int wave = quarter * 50; wave < (quarter + 1) * 50; wave++) {
                total += report.waves().get(wave).damageFraction();
                count++;
            }
            measurements.put("quarter" + (quarter + 1) + "Waves", Float.toString(total / count));
        }

        RunRecord record = RunRecord.of(
            RunRecord.SOURCE_SIMULATOR, "headless", GameMode.STANDARD.name(), SEED, report.waves().size(), 0,
            report.reachedFinalWave(), !report.reachedFinalWave(), measurements, System.currentTimeMillis());

        FileHandle directory = new FileHandle(OUTPUT.toFile());
        directory.mkdirs();
        FileHandle file = directory.child("simulator-standard-" + SEED + ".json");
        file.writeString(record.toJson(), false, "UTF-8");

        JsonValue parsed = new JsonReader().parse(file.readString("UTF-8"));
        assertEquals(GameMode.STANDARD.name(), parsed.getString("mode"));
        assertEquals(GameState.FINAL_WAVE, parsed.getInt("wavesCleared"));
        assertTrue(Float.parseFloat(parsed.get("measurements").getString("quarter4Waves"))
            > Float.parseFloat(parsed.get("measurements").getString("quarter1Waves")),
            "the fourth quarter has to be heavier than the first, or the curve is not rising");
    }

    /**
     * The non-optimiser at the top of the ladder, recorded as its own session. It exists because the measurement
     * surprised everyone who read it first: a player who ignores every system does *better* in the brief vigil at
     * tier 10 than at tier 0, which is the opposite of what an ascension ladder is for. The record carries the
     * numbers so the finding that came out of it can point at a session rather than at a memory.
     */
    @Test
    void theNonOptimiserAtTierTenIsRecorded() {
        BalanceSimulator.BalanceReport report = new BalanceSimulator()
            .runWithPolicy(SEED, BalanceSimulator.Policy.NAIVE, 10, GameMode.BRIEF);

        Map<String, String> measurements = new LinkedHashMap<>();
        measurements.put("averageDamageFraction", Float.toString(report.averageDamageFraction()));
        measurements.put("policy", BalanceSimulator.Policy.NAIVE.name());
        measurements.put("tier", "10");
        measurements.put("waves", Integer.toString(report.waves().size()));
        measurements.put("survived", Boolean.toString(report.reachedFinalWave()));

        RunRecord record = RunRecord.of(
            RunRecord.SOURCE_SIMULATOR, "headless", GameMode.BRIEF.name(), SEED, report.waves().size(), 10,
            report.reachedFinalWave(), !report.reachedFinalWave(), measurements, System.currentTimeMillis());

        FileHandle directory = new FileHandle(OUTPUT.toFile());
        directory.mkdirs();
        FileHandle file = directory.child("simulator-naive-tier10-" + SEED + ".json");
        file.writeString(record.toJson(), false, "UTF-8");

        JsonValue parsed = new JsonReader().parse(file.readString("UTF-8"));
        assertEquals("NAIVE", parsed.get("measurements").getString("policy"));
        assertEquals(10, parsed.getInt("ascensionTier"),
            "a record of a tier run says which tier, or the ledger cannot tell the two apart");
    }
}
