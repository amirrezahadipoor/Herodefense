package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.WaveModifier;
import com.amirrezahadipoor.herodefense.trials.TrialId;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Wave omens (roadmap R3.4, "wave modifiers"): the twist is deterministic, it lands only where it says it lands,
 * and each omen changes exactly the number it advertises — measured through the real spawner, factory and reward
 * system rather than through the enum's own fields.
 */
final class WaveOmenTest {

    private static final long SEED = 0x4845524F444546L;

    @Test
    void omensLandOnRegularWavesAfterTheOpeningAndNeverOnABossWave() {
        assertFalse(WaveModifier.isOmenWave(1), "wave 1 teaches the plain game");
        assertFalse(WaveModifier.isOmenWave(2));
        assertTrue(WaveModifier.isOmenWave(3));
        for (int wave = 1; wave <= GameState.FINAL_WAVE; wave++) {
            if (wave % 5 == 0) {
                assertFalse(WaveModifier.isOmenWave(wave), "wave " + wave + " is a boss wave");
            }
            boolean periodSaysOmen = WaveModifier.isOmenWave(wave)
                && !EnemyWaveSpawner.isEliteWave(wave, 0);
            assertEquals(
                periodSaysOmen,
                WaveOmens.of(SEED, wave, 0, true).isOmen(),
                "wave " + wave + " must agree with the run-level rule"
            );
        }
        int omenWaves = 0;
        for (int wave = 1; wave <= GameState.FINAL_WAVE; wave++) {
            if (WaveOmens.of(SEED, wave, 0, true).isOmen()) omenWaves++;
        }
        // Twenty-two of the two hundred waves carry an omen: the period offers twenty-six, seven of those land on
        // a boss wave, and the rest that coincide with an elite wave are refused (never two twists at once).
        assertEquals(22, omenWaves, "roughly one wave in nine carries an omen");
    }

    @Test
    void theTwistComesFromTheSeedAndTheSeedAlone() {
        assertEquals(WaveModifier.forWave(SEED, 43), WaveModifier.forWave(SEED, 43));
        Set<WaveModifier> seen = EnumSet.noneOf(WaveModifier.class);
        for (long seed = 0; seed < 64; seed++) {
            WaveModifier omen = WaveOmens.of(seed, 9, 0, true);
            if (omen.isOmen()) seen.add(omen);
        }
        assertEquals(6, seen.size(), "all six omens must be reachable: " + seen);
        for (int wave = 3; wave <= GameState.FINAL_WAVE; wave += 4) {
            WaveModifier omen = WaveModifier.forWave(SEED, wave);
            if (!omen.isOmen()) continue;
            assertTrue(omen.label().equals(omen.label().toUpperCase(java.util.Locale.ROOT)));
            assertNotEquals("", omen.detail());
            assertTrue(omen.coinMultiplier() > 1f, "an omen wave pays for itself");
            assertTrue(omen.healthMultiplier() <= 1.5f);
            assertTrue(omen.damageMultiplier() <= 1.3f);
        }
    }

    @Test
    void aRunThatDidNotDraftTheTrialHasNoOmensAtAll() {
        GameState state = GameState.newRun(SEED);
        assertEquals(0, omenWavesIn(state), "the untrialled run is the run every gate measured");
        state.activeTrials.add(TrialId.HOLLOW_OMENS.name());
        assertTrue(omenWavesIn(state) > 0, "the drafted trial is what lets the wood answer");
        assertFalse(WaveModifier.forWave(SEED, 43, false).isOmen());
    }

    @Test
    void neverTwoTwistsAtOnce() {
        GameState state = omenRun();
        for (int wave = 1; wave <= GameState.FINAL_WAVE; wave++) {
            if (EnemyWaveSpawner.isEliteWave(wave, state.ascensionTier)) {
                assertFalse(WaveOmens.of(state, wave).isOmen(),
                    "wave " + wave + " is already an elite wave");
            }
            if (wave % 5 == 0) {
                assertFalse(WaveOmens.of(state, wave).isOmen(), "wave " + wave + " is a boss wave");
            }
        }
        for (int tier = 0; tier <= 10; tier++) {
            GameState tierState = omenRun();
            tierState.ascensionTier = tier;
            for (int wave = 1; wave <= GameState.FINAL_WAVE; wave++) {
                if (EnemyWaveSpawner.isEliteWave(wave, tier)) {
                    assertFalse(WaveOmens.of(tierState, wave).isOmen(),
                        "tier " + tier + " wave " + wave + " is an elite wave");
                }
            }
        }
    }

    @Test
    void aStatusOmenLeavesTheBodyCountAloneAndTheSwarmOmenRaisesIt() {
        GameState state = omenRun();
        int plain = 4;
        assertEquals(plain, EnemyWaveSpawner.omenAdjustedCount(state, 4, plain),
            "wave 4 carries no omen, so nothing is added");
        int wave = firstOmenWave(state, WaveModifier.SWARM);
        assertEquals(Math.round(plain * WaveModifier.SWARM.enemyCountMultiplier()),
            EnemyWaveSpawner.omenAdjustedCount(state, wave, plain));
        int countWave = firstOmenWave(state, WaveModifier.IRON_HIDE);
        assertEquals(plain, EnemyWaveSpawner.omenAdjustedCount(state, countWave, plain),
            "a durability omen does not change how many walk in");
    }

    @Test
    void everyOmenReachesTheEnemyItNames() {
        GameState state = omenRun();
        DifficultyCurve curve = new DifficultyCurve();
        EnemyFactory factory = new EnemyFactory(curve);
        EnemyType type = EnemyType.ROOTLING;

        for (WaveModifier omen : new WaveModifier[] {
            WaveModifier.SWARM, WaveModifier.IRON_HIDE, WaveModifier.BLOODRUSH, WaveModifier.QUICKSTEP,
            WaveModifier.GILDED, WaveModifier.WARBAND}) {
            int wave = firstOmenWave(state, omen);
            Enemy enemy = factory.createForWave(state, type, 0f, 0f, 0, wave);
            float health = curve.regularHealth(type, wave, state.ascensionTier);
            float damage = curve.regularDamage(type, wave, state.ascensionTier);
            assertEquals(health * omen.healthMultiplier(), enemy.maxHealth, 1e-2f,
                omen + " must multiply health by exactly " + omen.healthMultiplier());
            assertEquals(health * omen.healthMultiplier(), enemy.health, 1e-2f);
            assertEquals(damage * omen.damageMultiplier(), enemy.damage, 1e-3f,
                omen + " must multiply damage by exactly " + omen.damageMultiplier());
            assertEquals(type.movementSpeed() * omen.speedMultiplier(), enemy.movementSpeed, 1e-3f,
                omen + " must multiply speed by exactly " + omen.speedMultiplier());
        }
    }

    @Test
    void anOmenWavePaysBetterForTheSameKill() {
        GameState plainState = GameState.newRun(SEED);
        GameState omenState = GameState.newRun(SEED);
        omenState.activeTrials.add(TrialId.HOLLOW_OMENS.name());
        int omenWave = firstOmenWave(omenState, WaveModifier.SWARM);
        WaveModifier omen = WaveModifier.forWave(SEED, omenWave);

        plainState.activeTrials.clear();
        int plainCoins = coinsForOneKill(plainState, omenWave);
        int omenCoins = coinsForOneKill(omenState, omenWave);

        assertEquals(Math.round(plainCoins * omen.coinMultiplier()), omenCoins,
            "the very same wave and the very same kill, on and off");
        assertTrue(omenCoins > plainCoins);
    }

    @Test
    void theGildedOmenIsTheDecisionThePoolWasMissing() {
        GameState state = omenRun();
        int wave = firstOmenWave(state, WaveModifier.GILDED);
        GameState plain = GameState.newRun(SEED);
        plain.waveNumber = wave;
        int plainCoins = coinsForOneKill(plain, wave);
        int gildedCoins = coinsForOneKill(state, wave);
        assertEquals(Math.round(plainCoins * WaveModifier.GILDED.coinMultiplier()), gildedCoins,
            "the gilded wave pays two and a half times the coin for the same kill");
        assertTrue(gildedCoins > plainCoins * 2, "and that is the decision: worth the tougher hide or not");
    }

    @Test
    void theWarbandOmenIsTheSwarmOmenInvertedAndNeverSlower() {
        GameState state = omenRun();
        int wave = firstOmenWave(state, WaveModifier.WARBAND);
        int plain = 10;
        assertEquals(Math.round(plain * WaveModifier.WARBAND.enemyCountMultiplier()),
            EnemyWaveSpawner.omenAdjustedCount(state, wave, plain),
            "a third fewer bodies walk in");
        WaveModifier warband = WaveModifier.WARBAND;
        assertTrue(warband.enemyCountMultiplier() * warband.healthMultiplier() <= 1.1f,
            "the wave's total health stays within a hair of an ordinary wave: the twist is shape, not mass");
        assertTrue(warband.enemyCountMultiplier() * warband.damageMultiplier() < 1f,
            "and its total contact damage is lower -- the few are heavy, not more dangerous together");
        assertTrue(warband.coinMultiplier() * warband.enemyCountMultiplier() <= 1.1f,
            "the wave pays about what an ordinary wave pays: fewer kills at a richer rate");
    }

    private static GameState omenRun() {
        GameState state = GameState.newRun(SEED);
        state.activeTrials.add(TrialId.HOLLOW_OMENS.name());
        return state;
    }

    private static int omenWavesIn(GameState state) {
        int count = 0;
        for (int wave = 1; wave <= GameState.FINAL_WAVE; wave++) {
            if (WaveOmens.of(state, wave).isOmen()) count++;
        }
        return count;
    }

    private static int firstOmenWave(GameState state, WaveModifier wanted) {
        for (int wave = 1; wave <= GameState.FINAL_WAVE; wave++) {
            if (WaveOmens.of(state, wave) == wanted) return wave;
        }
        throw new AssertionError("seed never rolls " + wanted);
    }

    private static int coinsForOneKill(GameState state, int wave) {
        state.waveNumber = wave;
        state.coins = 0;
        Enemy enemy = new Enemy(state.allocateEntityId(), EnemyType.ROOTLING.name(), 0f, 0f);
        enemy.alive = false;
        enemy.health = 0f;
        state.aliveEnemies.add(enemy);
        KillRewardResult result = new KillRewardSystem(new HeroProgressionSystem()).processDefeatedEnemies(state);
        assertEquals(1, result.kills());
        return state.coins;
    }
}
