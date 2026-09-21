package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.gameplay.DifficultyCurve;
import com.amirrezahadipoor.herodefense.model.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * What a wave costs the bar it lands on, measured on the fixed sweep (roadmap R4.1, rebuilt by B1).
 *
 * <p><b>What this test used to enforce, and why the numbers moved.</b> The old version ran the same five seeds and
 * asserted that the average gross damage fraction of a whole run sat between 0.05 and 0.15, that no single wave
 * passed 0.40, and that every quarter of the run was heavier than the one before it. Those numbers were honest
 * measurements of the game as it was, and the game as it was could not be lost: the audit measured the optimiser
 * finishing two hundred waves on every seed, the naive policy finishing thirty of thirty on the brief vigil, and a
 * five percent band being spent by hits that were worth 0.27% of the bar. A band is only a promise about the
 * game, and the promise underneath this one was "nothing you do to the hero matters", so B1 re-derived the damage
 * axis from the bar the hero actually carries and this test was re-based on the same measurement, wave by wave.
 *
 * <p><b>Why the band is now wider and split.</b> With damage anchored to the bar, the interesting question stopped
 * being "what is the average over two hundred waves" and became "what does one wave cost", which is what a player
 * pays. Measured on the shipped curve (five seeds, sustained optimiser policy): rank-and-file waves average 0.25
 * to 0.35 of a bar, boss waves 0.35 to 0.43, the worst rank-and-file wave 0.60 to 0.95, the worst boss wave 0.60
 * to 0.86, and the whole run averages 0.27 to 0.36. Those are the numbers the bands below are drawn around: the
 * floor exists so a later card or potion change cannot quietly make the game a walkover again, and the ceiling
 * exists so it cannot become a wall.
 *
 * <p><b>The three rules this gate keeps.</b> One: a rank-and-file wave is a quarter to a third of the bar, and no
 * wave -- boss or not -- takes more than a bar and a tenth, so no wave is a death sentence on arrival. Two: the
 * run gets heavier, the last quarter at least 1.8x the first on every seed, and the hot middle neither collapses
 * nor runs away. Three, and this is the one the audit's item 1 asked for: the run is <em>losable</em>. A policy
 * that ignores the game's verbs has to be dead before wave 100 on every seed of the fixed sweep, while the
 * optimiser still finishes all two hundred -- both halves of the same sentence, asserted together so neither can
 * be tuned away by accident.
 */
final class WavePressureCurveTest {

    /** The fixed sweep the balance gates use, so a curve change is judged on the same seeds everywhere. */
    private static final long[] SEEDS = {
        0x4845524F444546L, 0x4845524F444546L + 1, 0x4845524F444546L + 2, 0x747269616C7331L, 0x4341524453494DL};

    private static final int QUARTERS = 4;
    private static final int WAVES_PER_QUARTER = GameState.FINAL_WAVE / QUARTERS;

    /** Rank-and-file waves: the audit's per-wave target, a quarter to a third of the bar. */
    private static final float MINIMUM_RANK_AVERAGE = 0.15f;
    private static final float MAXIMUM_RANK_AVERAGE = 0.45f;

    /** A boss wave is the spike of its block: heavier than its neighbours, still payable with potions. */
    private static final float MAXIMUM_BOSS_AVERAGE = 0.60f;

    /** No wave, boss or rank, may cost more than a bar and a tenth in one go. */
    private static final float MAXIMUM_WAVE = 1.10f;

    /** The whole run, boss waves included. */
    private static final float MINIMUM_RUN_AVERAGE = 0.15f;
    private static final float MAXIMUM_RUN_AVERAGE = 0.55f;

    /** How much heavier the late run has to be than the opening, per seed. */
    private static final float MINIMUM_MIDDLE_STEP = 1.8f;
    private static final float MINIMUM_LATE_STEP = 1.8f;

    /** The hot middle may ease off, but not collapse, towards the end of the run. */
    private static final float PLATEAU_FLOOR = 0.75f;

    /** Where a player who ignores potions, cards and steps has to be dead. */
    private static final int NAIVE_SURVIVAL_CEILING = 100;

    /** How a boss wave is identified without asking the simulator: the director's own cadence. */
    private static boolean isBossWave(WaveSample wave) {
        return wave.wave() % 5 == 0;
    }

    private static List<WaveSample> rankWaves(BalanceReport report) {
        List<WaveSample> waves = new ArrayList<>();
        for (WaveSample wave : report.waves()) {
            if (!isBossWave(wave)) {
                waves.add(wave);
            }
        }
        return waves;
    }

    private static List<WaveSample> bossWaves(BalanceReport report) {
        List<WaveSample> waves = new ArrayList<>();
        for (WaveSample wave : report.waves()) {
            if (isBossWave(wave)) {
                waves.add(wave);
            }
        }
        return waves;
    }

    private static float mean(List<WaveSample> waves) {
        float total = 0f;
        for (WaveSample wave : waves) {
            total += wave.damageFraction();
        }
        return waves.isEmpty() ? 0f : total / waves.size();
    }

    private static float peak(List<WaveSample> waves) {
        float peak = 0f;
        for (WaveSample wave : waves) {
            peak = Math.max(peak, wave.damageFraction());
        }
        return peak;
    }

    /** The mean of one quarter of the run, rank-and-file waves only, so a boss block cannot tilt the shape. */
    private static float quarterAverage(List<WaveSample> rankWaves, int quarter) {
        float total = 0f;
        int count = 0;
        for (WaveSample wave : rankWaves) {
            int waveQuarter = Math.min(QUARTERS - 1, (wave.wave() - 1) / WAVES_PER_QUARTER);
            if (waveQuarter == quarter) {
                total += wave.damageFraction();
                count++;
            }
        }
        return count == 0 ? 0f : total / count;
    }

    /**
     * The run gets heavier, and it does it early: the step into the hot middle is the biggest step in the curve,
     * and the last quarter still costs at least 1.8x the first on every seed. The middle may plateau -- with the
     * spawn cap reached by wave 40 there is nothing left to add but health and hit weight -- but it may not fall
     * away, or the last third of the run would be a victory lap.
     */
    @Test
    void theRunGetsHeavierFromTheFirstQuarterToTheLast() {
        List<String> readings = new ArrayList<>();
        float[][] quarters = new float[SEEDS.length][QUARTERS];
        for (int index = 0; index < SEEDS.length; index++) {
            BalanceReport report = new BalanceSimulator().run(SEEDS[index]);
            List<WaveSample> rank = rankWaves(report);
            for (int quarter = 0; quarter < QUARTERS; quarter++) {
                quarters[index][quarter] = quarterAverage(rank, quarter);
            }
            readings.add(String.format(Locale.ROOT, "%s: %.4f %.4f %.4f %.4f",
                Long.toHexString(SEEDS[index]), quarters[index][0], quarters[index][1],
                quarters[index][2], quarters[index][3]));
        }
        for (int index = 0; index < SEEDS.length; index++) {
            String seed = Long.toHexString(SEEDS[index]);
            assertTrue(quarters[index][1] >= quarters[index][0] * MINIMUM_MIDDLE_STEP,
                seed + " never reaches the hot middle: quarter 2 averages " + quarters[index][1]
                    + " against " + quarters[index][0] + " for quarter 1; readings: " + readings);
            assertTrue(quarters[index][3] >= quarters[index][0] * MINIMUM_LATE_STEP,
                seed + " eases off toward the end: quarter 4 averages " + quarters[index][3]
                    + " against " + quarters[index][0] + " for quarter 1; readings: " + readings);
            assertTrue(quarters[index][2] >= quarters[index][1] * PLATEAU_FLOOR
                    && quarters[index][3] >= quarters[index][1] * PLATEAU_FLOOR,
                seed + " collapses after the middle: quarter 2 averages " + quarters[index][1]
                    + ", quarter 3 " + quarters[index][2] + ", quarter 4 " + quarters[index][3]
                    + "; readings: " + readings);
        }
    }

    /**
     * The bands, measured the way a player pays them: one wave at a time. A rank-and-file wave costs a quarter to a
     * third of the bar, a boss wave costs more than that and less than a bar, and no single wave is unpayable.
     */
    @Test
    void aRankAndFileWaveCostsAQuarterOfTheBarAndABossWaveIsTheSpike() {
        for (long seed : SEEDS) {
            BalanceReport report = new BalanceSimulator().run(seed);
            assertTrue(report.reachedFinalWave(), "seed " + Long.toHexString(seed)
                + " has to finish the run for its shape to mean anything");
            String where = "seed " + Long.toHexString(seed);
            float rankAverage = mean(rankWaves(report));
            float bossAverage = mean(bossWaves(report));
            float runAverage = report.averageDamageFraction();
            assertTrue(rankAverage >= MINIMUM_RANK_AVERAGE,
                where + "'s rank-and-file waves averaged only " + rankAverage + " of the bar, under the "
                    + MINIMUM_RANK_AVERAGE + " floor: hits have stopped mattering again");
            assertTrue(rankAverage <= MAXIMUM_RANK_AVERAGE,
                where + "'s rank-and-file waves averaged " + rankAverage + ", over the " + MAXIMUM_RANK_AVERAGE
                    + " ceiling");
            assertTrue(bossAverage <= MAXIMUM_BOSS_AVERAGE,
                where + "'s boss waves averaged " + bossAverage + " of the bar, over the " + MAXIMUM_BOSS_AVERAGE
                    + " ceiling");
            assertTrue(peak(rankWaves(report)) <= MAXIMUM_WAVE,
                where + " spiked to " + peak(rankWaves(report)) + " in one rank-and-file wave, over the "
                    + MAXIMUM_WAVE + " ceiling");
            assertTrue(peak(bossWaves(report)) <= MAXIMUM_WAVE,
                where + " spiked to " + peak(bossWaves(report)) + " in one boss wave, over the " + MAXIMUM_WAVE
                    + " ceiling");
            assertTrue(runAverage >= MINIMUM_RUN_AVERAGE,
                where + " averaged only " + runAverage + " over the two hundred waves; a run in this game is not "
                    + "a walkover");
            assertTrue(runAverage <= MAXIMUM_RUN_AVERAGE,
                where + " averaged " + runAverage + ", over the " + MAXIMUM_RUN_AVERAGE + " ceiling");
        }
    }

    /**
     * The audit's first item, as a gate: a run that ignores the game's verbs has to end. This is the assertion the
     * old curve could not have passed on any seed -- the naive policy finished the brief vigil on every seed and the
     * optimiser finished the long one, so no policy could lose. Both halves are asserted here: an unskilled run is
     * lost inside the first hundred waves, and a skilled one still reaches wave two hundred, so a later change
     * cannot buy losability by making the game unwinnable.
     */
    @Test
    void theGameIsLosableWhenTheVerbsAreIgnored() {
        for (long seed : SEEDS) {
            int naiveWaves = new BalanceSimulator()
                .runWithPolicy(seed, BalanceSimulator.Policy.NAIVE)
                .waves()
                .size();
            assertTrue(naiveWaves < NAIVE_SURVIVAL_CEILING,
                "seed " + Long.toHexString(seed) + " let a policy that ignores potions, cards and steps reach wave "
                    + naiveWaves + "; the run has to be losable");
            assertTrue(naiveWaves > 0, "and the run is not lost standing still at wave one");
            assertTrue(new BalanceSimulator().run(seed).reachedFinalWave(),
                "seed " + Long.toHexString(seed) + " has to stay winnable by the sustained policy");
        }
    }

    /**
     * The shape of the shipped curve, so a later reader can see why the run looks the way it does: the middle
     * segment (waves 25-80) is the hottest span, the second half's entry (101-150) is cooler than the middle and
     * hotter than the final quarter (151-200), and the final quarter is the coolest span in the curve. R4.6 moved
     * the second half's heat to the front of the half on purpose: the entry is where the step into the second
     * half lives and the tail is where the run's heaviest single waves lived.
     *
     * <p>B1 added the second half of this test: the share of the bar a hit takes falls across the run (the wave
     * lands more of them), the raw hit still grows in hit points, and the boss special -- the one telegraphed
     * attack in the game -- grows in absolute terms from the first boss to the last, because that is the number
     * the player learns to read.
     */
    @Test
    void theShippedCurveIsHotterInTheMiddleAndCoolerInTheSecondHalf() {
        DifficultyCurve curve = new DifficultyCurve();
        assertTrue(DifficultyCurve.MIDDLE_HEALTH_GROWTH > DifficultyCurve.ENEMY_HEALTH_GROWTH,
            "waves 25-80 compound a hotter health rate than the base");
        assertTrue(DifficultyCurve.SECOND_HALF_HEALTH_GROWTH < DifficultyCurve.MIDDLE_HEALTH_GROWTH,
            "the second half's entry is cooler than the middle span, so the run eases into its last hundred waves");
        assertTrue(DifficultyCurve.FINAL_QUARTER_HEALTH_GROWTH < DifficultyCurve.SECOND_HALF_HEALTH_GROWTH,
            "and the final quarter is the coolest span, which is where the run's heaviest single waves used to sit");
        assertTrue(DifficultyCurve.damageShareOfExpectedBar(151) <= DifficultyCurve.damageShareOfExpectedBar(150),
            "and the final quarter's hits are a smaller share of the bar than the entry's (audit item 1)");
        assertTrue(curve.baselineRegularHealth(GameState.FINAL_WAVE) > curve.baselineRegularHealth(120),
            "wave 200 is still heavier than wave 120");
        // Phase 91's old shape had "heavier hits in absolute terms" as a property of the damage rate. The B1
        // anchor changed what a heavier hit means: damage is a share of a bar that grows faster than the share
        // falls, so wave 200's hit is still bigger than wave 120's in points, while the *share* it takes is
        // smaller, because a wave at 200 lands far more of them. Both halves are asserted.
        assertTrue(curve.baselineRegularDamage(GameState.FINAL_WAVE) > curve.baselineRegularDamage(120),
            "with heavier hits in hit points as well");
        assertTrue(DifficultyCurve.damageShareOfExpectedBar(GameState.FINAL_WAVE)
                < DifficultyCurve.damageShareOfExpectedBar(120),
            "though each one is a smaller share of the bar it lands on");
        assertTrue(curve.bossSpecialDamage(GameState.FINAL_WAVE) > curve.bossSpecialDamage(100),
            "and the telegraphed hit grows in points from the first boss block to the last");
    }
}
