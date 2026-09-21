package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.GameMode;
import com.amirrezahadipoor.herodefense.model.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The win-rate band for a player who does not optimise (roadmap R4.1, second half).
 *
 * <p>Every other gate in this repository measures the optimiser policy of {@link BalanceSimulator.Policy}: a player
 * who scores the reward cards, keeps both shop tabs moving, funds a skill evolution and reforges their gear. That
 * player is a useful instrument and a misleading one, because nobody plays the game that way on the first evening.
 * The item asked for the other measurement — what happens to someone who takes the first card they are offered,
 * never opens the shop, spreads their talent points evenly and never reforges — and for a *published band*, so the
 * answer is a number a later change has to respect rather than a sentence in a design document.
 *
 * <p>The band is measured in the brief vigil: thirty waves are enough to answer "did they survive the opening of
 * the game", short enough to run on a dozen seeds, and they are the same thirty waves the long vigil opens with, so
 * nothing here is a special case built for the test.
 *
 * <p>Two claims, and the second is the one that keeps the first honest: the non-optimiser must win some runs and
 * lose some runs — a band, not a wall and not a walkover — and the non-optimiser must do measurably worse than the
 * optimiser on the same seeds, or this policy is not a weaker player and the band means nothing.
 */
/**
 * Tagged {@code balance}: this suite sweeps whole runs, so it is the {@code :core:balanceGate} task's work and not
 * part of the fast unit loop (roadmap R4.5). The gate is what CI runs on every push; the tag only decides which
 * task pays for it, never whether it runs.
 */
@Tag("balance")
final class NonOptimiserBandTest {

    /** A dozen seeds, none of them special: the first two are the fixed sweep's, the rest are the next ten. */
    private static final long[] SEEDS = {
        0x4845524F444546L, 0x4845524F444546L + 1, 0x4845524F444546L + 2, 0x747269616C7331L, 0x4341524453494DL,
        0x4E414956453031L, 0x4E414956453032L, 0x4E414956453033L, 0x4E414956453034L, 0x4E414956453035L,
        0x4E414956453036L, 0x4E414956453037L};

    /**
     * The published band, in two halves because the two modes answer two different questions.
     *
     * <p><b>Re-based by B1, and what the opening owes the player now.</b> Before B1 the non-optimiser won every
     * one of the twelve brief vigils and reached 69.7 waves in the long one: a player who ignored potions, cards,
     * reforging and steps could not lose, which is exactly what item 1 of the audit asked to change. Anchoring
     * damage to the bar changed it: measured on the same dozen seeds the non-optimiser now wins 3 of 12 briefs,
     * never dies before wave 15, and reaches 27.3 waves in the long vigil, dying in most cases on or immediately
     * after one of the six boss waves the brief contains.
     *
     * <p>So the two halves of the band moved with the measurement, and the second half is now the promise worth
     * keeping: the opening is survivable for a while (never a death before wave 15, and it is still won outright
     * on some seeds), and the long vigil catches the passive player somewhere in its first quarter. The stronger
     * version of the old promise -- a passive player should finish the opening every time -- is not something the
     * damage axis can buy back, because the brief vigil holds six boss waves whose contact damage is anchored
     * like everything else. That is a pacing question, and it belongs to items 4 and 5 of the audit (the wave
     * table's events and the eighty-wave run), not to a multiplier here. It is recorded in the balance doc.
     */
    private static final float MINIMUM_BRIEF_WIN_RATE = 0.25f;
    private static final int MINIMUM_BRIEF_REACH_WAVES = 15;
    private static final float MINIMUM_AVERAGE_REACH_WAVES = 20f;
    private static final float MAXIMUM_AVERAGE_REACH_WAVES = 50f;

    @Test
    void theBriefVigilIsWinnableWithoutOptimising() {
        int wins = 0;
        int shallowest = Integer.MAX_VALUE;
        List<String> readings = new ArrayList<>();
        for (long seed : SEEDS) {
            BalanceReport report = new BalanceSimulator()
                .runWithPolicy(seed, BalanceSimulator.Policy.NAIVE, 0, GameMode.BRIEF);
            if (report.reachedFinalWave()) {
                wins++;
            }
            shallowest = Math.min(shallowest, report.waves().size());
            readings.add(String.format(Locale.ROOT, "%s: %s at wave %d, average pressure %.4f",
                Long.toHexString(seed), report.reachedFinalWave() ? "survived" : "fell",
                report.waves().size(), report.averageDamageFraction()));
        }
        float winRate = wins / (float) SEEDS.length;
        assertTrue(winRate >= MINIMUM_BRIEF_WIN_RATE, "a player who does not optimise won only " + wins + "/"
            + SEEDS.length + " brief runs (" + winRate + "), under the " + MINIMUM_BRIEF_WIN_RATE
            + " floor: the opening of the game has become an exam on its systems. readings: " + readings);
        assertTrue(shallowest >= MINIMUM_BRIEF_REACH_WAVES,
            "a passive player died at wave " + shallowest + ", before the " + MINIMUM_BRIEF_REACH_WAVES
                + " the opening has to teach them in. readings: " + readings);
    }

    /**
     * Both claims about the long run come from one sweep: the band the non-optimiser's reach has to sit inside,
     * and the proof that this policy is the weaker player (the optimiser finishes every one of the same runs). The
     * two were written as separate tests and merged when the suite's runtime was measured — the sweeps are the
     * expensive part and running them twice bought nothing.
     */
    @Test
    void theLongVigilCatchesANonOptimiserInsideItsBand() {
        int naiveWaves = 0;
        int optimiserWaves = 0;
        int fell = 0;
        List<String> readings = new ArrayList<>();
        for (long seed : SEEDS) {
            BalanceReport naive = new BalanceSimulator()
                .runWithPolicy(seed, BalanceSimulator.Policy.NAIVE, 0, GameMode.STANDARD);
            BalanceReport optimiser = new BalanceSimulator()
                .runWithPolicy(seed, BalanceSimulator.Policy.OPTIMISER, 0, GameMode.STANDARD);
            naiveWaves += naive.waves().size();
            optimiserWaves += optimiser.waves().size();
            if (!naive.reachedFinalWave()) {
                fell++;
            }
            readings.add(String.format(Locale.ROOT, "%s: non-optimiser wave %d, optimiser wave %d",
                Long.toHexString(seed), naive.waves().size(), optimiser.waves().size()));
        }
        float reach = naiveWaves / (float) SEEDS.length;
        assertTrue(reach >= MINIMUM_AVERAGE_REACH_WAVES, "the non-optimiser's average reach is " + reach
            + " waves, under the " + MINIMUM_AVERAGE_REACH_WAVES + " floor: the curve is a wall for anyone "
            + "who does not optimise. readings: " + readings);
        assertTrue(reach <= MAXIMUM_AVERAGE_REACH_WAVES, "the non-optimiser's average reach is " + reach
            + " waves, over the " + MAXIMUM_AVERAGE_REACH_WAVES + " ceiling: the long run no longer ends badly "
            + "for a player who ignores the systems. readings: " + readings);
        assertTrue(fell >= SEEDS.length / 2, "the long vigil is meant to catch this policy on most seeds, and it "
            + "caught " + fell + " of " + SEEDS.length + ". readings: " + readings);
        assertTrue(optimiserWaves == SEEDS.length * GameState.FINAL_WAVE,
            "the optimiser policy still finishes every long run on these seeds: " + optimiserWaves + " waves");
        assertTrue(naiveWaves < optimiserWaves, "the non-optimiser has to be the weaker player on the same "
            + "seeds: it reached " + naiveWaves + " waves against " + optimiserWaves);
    }

    @Test
    void theNonOptimiserIsMeasurablyWeakerThanTheOptimiser() {
        int naiveWaves = 0;
        int optimiserWaves = 0;
        for (long seed : SEEDS) {
            naiveWaves += new BalanceSimulator()
                .runWithPolicy(seed, BalanceSimulator.Policy.NAIVE, 0, GameMode.STANDARD).waves().size();
            optimiserWaves += new BalanceSimulator()
                .runWithPolicy(seed, BalanceSimulator.Policy.OPTIMISER, 0, GameMode.STANDARD).waves().size();
        }
        assertTrue(optimiserWaves == SEEDS.length * GameState.FINAL_WAVE,
            "the optimiser policy still finishes every long run on these seeds: " + optimiserWaves + " waves");
        assertTrue(naiveWaves < optimiserWaves, "the non-optimiser has to be the weaker player on the same "
            + "seeds: it reached " + naiveWaves + " waves against " + optimiserWaves);
    }

    /**
     * The policy switch itself: the optimiser's entry point and {@code Policy.OPTIMISER} have to be the same player,
     * or every gate in the repository would be measuring something slightly different from what it says.
     */
    @Test
    void theDefaultPolicyIsTheOptimiserBitForBit() {
        long seed = SEEDS[0];
        BalanceReport byDefault = new BalanceSimulator().runBrief(seed);
        BalanceReport byName =
            new BalanceSimulator().runBriefWithPolicy(seed, BalanceSimulator.Policy.OPTIMISER);
        assertTrue(byDefault.waves().size() == byName.waves().size(),
            "same run length: " + byDefault.waves().size() + " against " + byName.waves().size());
        for (int wave = 0; wave < byDefault.waves().size(); wave++) {
            assertTrue(byDefault.waves().get(wave).damageFraction()
                == byName.waves().get(wave).damageFraction(),
                "wave " + byDefault.waves().get(wave).wave() + " must play identically either way");
        }
        BalanceReport naive =
            new BalanceSimulator().runBriefWithPolicy(seed, BalanceSimulator.Policy.NAIVE);
        assertTrue(naive.waves().size() <= byDefault.waves().size(),
            "and the naive policy must not outlast the optimiser on this seed");
    }
}
