package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.balance.BalanceReport;
import com.amirrezahadipoor.herodefense.balance.WaveSample;
import com.amirrezahadipoor.herodefense.gameplay.DifficultyCurve;
import com.amirrezahadipoor.herodefense.gameplay.EnemyFactory;
import com.amirrezahadipoor.herodefense.gameplay.EnemyWaveSpawner;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Phase 26.1b: Elite waves are included in the simulator's gross-damage books,
 * not exempted. The accounting path shipped with the Elites themselves (25.2 /
 * 25.2b): the spawner marks 1-2 elites on Elite waves, and elite melee,
 * blightburst blasts, and weeping rot all land inside the measured HP-delta
 * window that feeds every WaveSample. This test locks the inclusion at the
 * simulator level; the affix mechanics themselves are locked by
 * EliteAffixSystemTest.
 */
final class EliteDamageAccountingTest {
    private static final long BASELINE_SEED = 0x4845524F444546L;

    @Test
    void eliteWavesArePresentAndTheirDamageReachesTheBooks() {
        BalanceReport report = new BalanceSimulator().run(BASELINE_SEED);
        assertTrue(report.reachedFinalWave());

        List<WaveSample> elite = report.waves().stream()
            .filter(sample -> EnemyWaveSpawner.isEliteWave(sample.wave(), 0))
            .toList();
        assertEquals(23, elite.size(), "Elite waves must spawn inside simulated runs");

        float eliteDamage = 0f;
        long pressured = 0;
        for (WaveSample sample : elite) {
            eliteDamage += sample.damageFraction();
            if (sample.damageFraction() >= 0.01f) pressured++;
        }
        System.out.println("elite_waves,elite_damage,pressured: "
            + elite.size() + "," + eliteDamage + "," + pressured);
        assertTrue(eliteDamage > 0f, "Elite waves must threaten through the sim's accounting");
        assertTrue(pressured >= 1, "At least one Elite wave must land pressured damage");

        float total = 0f;
        for (WaveSample sample : report.waves()) total += sample.damageFraction();
        float averageIncludingElites = total / report.waves().size();
        float averageExcludingElites =
            (total - eliteDamage) / (report.waves().size() - elite.size());
        assertTrue(averageIncludingElites > averageExcludingElites,
            "Reported average must include Elite-wave damage");
    }

    /**
     * R4.6: the elite contact multiplier is a flat 1.5 everywhere, which is what carried the late spikes -- every
     * spike the gates caught in the second half of a run sits on an elite wave (126, 133, 154, 182 and 196 are
     * elite waves). The second half now pays a softer multiplier, chosen by measurement over the five trial
     * seeds: 1.2 bought 0.01-0.03 of spike headroom on the worst pairs while moving average pressure by less
     * than 0.001, because an elite's damage does not change how long its wave takes. The first half keeps the
     * shipped 1.5, so no brief-vigil or tier-0 measurement moves.
     */
    @Test
    void theSecondHalfPaysASofterEliteContactMultiplier() {
        assertTrue(EnemyWaveSpawner.ELITE_SECOND_HALF_DAMAGE_MULT < EnemyWaveSpawner.ELITE_DAMAGE_MULT,
            "the second half's elite contact multiplier has to be the softer of the two");
        int firstHalf = firstEliteWave(1, GameState.PLANTING_WAVE, 0);
        int secondHalf = firstEliteWave(GameState.PLANTING_WAVE + 1, GameState.FINAL_WAVE, 0);
        assertEquals(
            EnemyWaveSpawner.ELITE_DAMAGE_MULT,
            eliteDamageMultiplierAt(firstHalf, 0),
            0.000001f,
            "wave " + firstHalf + " is a first-half elite wave, so it keeps the shipped multiplier"
        );
        assertEquals(
            EnemyWaveSpawner.ELITE_SECOND_HALF_DAMAGE_MULT,
            eliteDamageMultiplierAt(secondHalf, 0),
            0.000001f,
            "wave " + secondHalf + " is a second-half elite wave, so it pays the softer multiplier"
        );
        int lastElite = lastEliteWave(GameState.PLANTING_WAVE + 1, GameState.FINAL_WAVE - 1);
        assertEquals(
            EnemyWaveSpawner.ELITE_SECOND_HALF_DAMAGE_MULT,
            eliteDamageMultiplierAt(lastElite, 0),
            0.000001f,
            "and the multiplier holds to wave " + lastElite + ", the run's last elite wave"
        );
    }

    /**
     * R4.7's interaction, measured rather than assumed: a second-half elite's contact is the position rule (1.2
     * times the regular it stands among) at *every* tier, which means the ladder's base damage charge has landed on
     * it exactly once, through the baseline, and not twice. That is also why the ladder's spike ceiling is
     * tier-indexed in `AscensionGateTest` rather than flat: the charge reaches the elites, and the gate prices it.
     */
    @Test
    void theLadderChargesAnEliteExactlyLikeTheRegularItStandsAmong() {
        for (int tier : new int[] {0, 3, 6, 10}) {
            int wave = firstEliteWave(GameState.PLANTING_WAVE + 1, GameState.FINAL_WAVE - 1, tier);
            assertEquals(
                EnemyWaveSpawner.eliteDamageMultiplier(wave),
                eliteDamageMultiplierAt(wave, tier),
                0.000001f,
                "tier " + tier + " wave " + wave + ": the elite's contact multiplier is the position rule alone"
            );
        }
        int firstHalfElite = firstEliteWave(1, GameState.PLANTING_WAVE, 10);
        assertEquals(
            EnemyWaveSpawner.ELITE_DAMAGE_MULT,
            eliteDamageMultiplierAt(firstHalfElite, 10),
            0.000001f,
            "wave " + firstHalfElite + " is the first half, which keeps the shipped multiplier at tier 10 too"
        );
    }

    /** The ship's own elite schedule decides which waves this contract is checked on, not a copied interval. */
    private static int firstEliteWave(int from, int to, int ascensionTier) {
        for (int wave = from; wave <= to; wave++) {
            if (EnemyWaveSpawner.isEliteWave(wave, ascensionTier)) return wave;
        }
        throw new AssertionError("no elite wave for tier " + ascensionTier + " between " + from + " and " + to);
    }

    private static int lastEliteWave(int from, int to) {
        for (int wave = to; wave >= from; wave--) {
            if (EnemyWaveSpawner.isEliteWave(wave, 0)) return wave;
        }
        throw new AssertionError("no elite wave between " + from + " and " + to);
    }

    /**
     * How much harder an elite of the first archetype hits than the same spawn would as a regular, at that tier:
     * the baseline is the tier's own, so this is the multiplier the ladder actually leaves in place.
     */
    private static float eliteDamageMultiplierAt(int wave, int ascensionTier) {
        GameState state = GameState.newRun(0x4845524F444546L);
        state.ascensionTier = ascensionTier;
        new EnemyWaveSpawner(new EnemyFactory()).spawnRegularEnemies(state, wave, 8);
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy.eliteAffix != null && !enemy.eliteAffix.isEmpty()) {
                float baseline = new DifficultyCurve().regularDamage(enemy.type(), wave, ascensionTier);
                return enemy.damage / baseline;
            }
        }
        throw new AssertionError("wave " + wave + " is an elite wave but no elite was marked");
    }
}
