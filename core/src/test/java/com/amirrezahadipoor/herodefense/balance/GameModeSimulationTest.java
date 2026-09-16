package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.GameMode;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The second run length (roadmap R3.5), measured with the same simulator that judges the long run.
 *
 * <p>The brief vigil is not a different game: it is the same run with the mode's own ending, which is why the first
 * assertion here can be the strongest one available — the first thirty waves of a brief run are bit-identical to
 * the first thirty waves of the long run on the same seed. The remaining assertions are the short mode's own
 * contract, derived from the same tuned band the long run uses (spike ceiling, average pressure floor) with one
 * deliberate exception recorded below.
 */
final class GameModeSimulationTest {

    private static final long[] SEEDS = {0x4845524F444546L, 0x4845524F444546L + 1, 0x4845524F444546L + 2};
    private static final float SPIKE_CEILING = 0.40f;
    private static final float AVERAGE_FLOOR = 0.035f;

    @Test
    void theBriefVigilIsTheLongVigilWithAShorterEnding() {
        long seed = SEEDS[0];
        BalanceReport full = new BalanceSimulator().run(seed);
        BalanceReport brief = new BalanceSimulator().runBrief(seed);

        assertEquals(GameState.FINAL_WAVE, full.waves().size(), "the long vigil is still two hundred waves");
        assertEquals(GameMode.BRIEF.waves(), brief.waves().size(), "the brief vigil ends where its mode says");
        assertTrue(brief.reachedFinalWave(), "and it counts as finished, not as a death");
        for (int wave = 0; wave < brief.waves().size(); wave++) {
            WaveSample fullWave = full.waves().get(wave);
            WaveSample briefWave = brief.waves().get(wave);
            assertEquals(fullWave.wave(), briefWave.wave(), "same wave numbers");
            assertEquals(fullWave.damageFraction(), briefWave.damageFraction(), 0f,
                "wave " + fullWave.wave() + " must play identically in both modes");
            assertEquals(fullWave.clearTimeSeconds(), briefWave.clearTimeSeconds(), 0f,
                "including how long it took");
        }
    }

    @Test
    void theBriefVigilKeepsTheTunedPressureBand() {
        for (long seed : SEEDS) {
            BalanceReport report = new BalanceSimulator().runBrief(seed);
            float spike = 0f;
            float total = 0f;
            for (WaveSample wave : report.waves()) {
                spike = Math.max(spike, wave.damageFraction());
                total += wave.damageFraction();
            }
            float average = total / Math.max(1, report.waves().size());
            assertTrue(spike <= SPIKE_CEILING,
                "brief run seed " + seed + " spiked to " + spike + " over " + SPIKE_CEILING);
            assertTrue(average >= AVERAGE_FLOOR,
                "brief run seed " + seed + " averaged only " + average + "; a short run must not be a walkover");
        }
    }

    @Test
    void theBriefVigilStillRampsInsteadOfPeakingAtOnce() {
        BalanceReport report = new BalanceSimulator().runBrief(SEEDS[1]);
        List<WaveSample> waves = report.waves();
        float opening = 0f;
        float closing = 0f;
        for (int index = 0; index < 10; index++) {
            opening += waves.get(index).damageFraction();
            closing += waves.get(waves.size() - 10 + index).damageFraction();
        }
        assertTrue(closing > opening,
            "the last ten waves must press harder than the first ten: opening=" + opening + " closing=" + closing);
    }

    /**
     * The brief mode's own contract, and the one place it differs from the long run's: the long gate demands a
     * minimum clear time of 24 s because a two-hundred-wave run has no business opening with instant waves. A
     * thirty-wave run does — waves 1-10 are legitimately fast, and asserting otherwise would be inventing a rule
     * for the short mode that the game's own curve does not have. Recorded here so the difference is deliberate.
     */
    @Test
    void theBriefVigilsFastOpeningIsExpectedAndBounded() {
        BalanceReport report = new BalanceSimulator().runBrief(SEEDS[2]);
        float fastest = Float.MAX_VALUE;
        float slowest = 0f;
        for (WaveSample wave : report.waves()) {
            fastest = Math.min(fastest, wave.clearTimeSeconds());
            slowest = Math.max(slowest, wave.clearTimeSeconds());
        }
        assertTrue(fastest > 4f, "no wave of the brief vigil may resolve instantly, got " + fastest);
        assertTrue(slowest <= 120f, "and none may stall, got " + slowest);
        assertTrue(slowest > fastest * 2f,
            "the mode has to end harder than it starts: fastest=" + fastest + " slowest=" + slowest);
    }

    @Test
    void theLongVigilIsUntouchedByTheNewMode() {
        GameState state = GameState.newRun(SEEDS[0]);
        assertEquals(GameMode.STANDARD, state.mode, "a fresh run is the shipped run");
        assertEquals(GameState.FINAL_WAVE, state.runLengthWaves());

        state.mode = GameMode.BRIEF;
        assertEquals(GameMode.BRIEF.waves(), state.runLengthWaves());
        state.waveNumber = 45;
        state.validateAndRepair();
        assertEquals(GameMode.BRIEF.waves(), state.waveNumber,
            "a brief save cannot hold a wave the mode does not have");

        state.resetForNewRun(SEEDS[0]);
        assertEquals(GameMode.BRIEF, state.mode, "and restarting keeps the length the player chose");
        assertFalse(state.runComplete);
    }
}
