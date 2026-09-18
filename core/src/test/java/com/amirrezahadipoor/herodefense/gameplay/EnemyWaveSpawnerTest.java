package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.SpawnLane;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class EnemyWaveSpawnerTest {
    private final EnemyWaveSpawner spawner = new EnemyWaveSpawner(new EnemyFactory());

    @Test
    void distributesEnemiesAcrossExactlyThreeOutsideEdges() {
        GameState state = GameState.newRun(123L);
        spawner.spawnRegularEnemies(state, 1, 6);

        Set<Integer> lanes = new HashSet<>();
        for (Enemy enemy : state.aliveEnemies) {
            lanes.add(enemy.spawnLane);
            if (enemy.spawnLane == SpawnLane.LEFT.id()) assertTrue(enemy.x < 0f);
            if (enemy.spawnLane == SpawnLane.RIGHT.id()) assertTrue(enemy.x > WorldLayout.REFERENCE_WIDTH);
            if (enemy.spawnLane == SpawnLane.SOUTH.id()) assertTrue(enemy.y < 0f);
        }
        assertEquals(Set.of(0, 1, 2), lanes);
    }

    @Test
    void eliteWavesFallOnEverySeventhNonBossWave() {
        assertTrue(EnemyWaveSpawner.isEliteWave(7, 0));
        assertTrue(EnemyWaveSpawner.isEliteWave(14, 0));
        assertTrue(EnemyWaveSpawner.isEliteWave(196, 0));
        assertFalse(EnemyWaveSpawner.isEliteWave(6, 0));
        assertFalse(EnemyWaveSpawner.isEliteWave(8, 0));
        assertFalse(EnemyWaveSpawner.isEliteWave(5, 0));
        assertFalse(EnemyWaveSpawner.isEliteWave(35, 0));
        assertFalse(EnemyWaveSpawner.isEliteWave(70, 0));
        assertFalse(EnemyWaveSpawner.isEliteWave(0, 0));
    }

    @Test
    void eliteIntervalTightensOneWaveEveryThreeTiersAndNeverLandsOnTheBossLap() {
        assertEquals(7, EnemyWaveSpawner.eliteWaveInterval(-3));
        assertEquals(7, EnemyWaveSpawner.eliteWaveInterval(0));
        assertEquals(7, EnemyWaveSpawner.eliteWaveInterval(1));
        assertEquals(7, EnemyWaveSpawner.eliteWaveInterval(2));
        assertEquals(6, EnemyWaveSpawner.eliteWaveInterval(3));
        assertEquals(6, EnemyWaveSpawner.eliteWaveInterval(5));
        // Roadmap R4.8: the arithmetic step here used to be five, and five waves is the boss lap, so tiers 6-8
        // spawned no elites at all. The cadence steps over it instead.
        assertEquals(4, EnemyWaveSpawner.eliteWaveInterval(6));
        assertEquals(4, EnemyWaveSpawner.eliteWaveInterval(8));
        assertEquals(4, EnemyWaveSpawner.eliteWaveInterval(9));
        assertEquals(4, EnemyWaveSpawner.eliteWaveInterval(10));
        assertEquals(4, EnemyWaveSpawner.eliteWaveInterval(99));
    }

    @Test
    void eliteWavesFollowTheTierIntervalAndStillSkipBosses() {
        assertTrue(EnemyWaveSpawner.isEliteWave(6, 3));
        assertTrue(EnemyWaveSpawner.isEliteWave(12, 3));
        assertFalse(EnemyWaveSpawner.isEliteWave(7, 3));
        assertFalse(EnemyWaveSpawner.isEliteWave(30, 3));
        assertTrue(EnemyWaveSpawner.isEliteWave(4, 10));
        assertTrue(EnemyWaveSpawner.isEliteWave(8, 10));
        assertFalse(EnemyWaveSpawner.isEliteWave(7, 10));
        assertFalse(EnemyWaveSpawner.isEliteWave(20, 10));
        // Roadmap R4.8: tiers 6-8 used to run at a cadence of five waves, which is the boss lap, so they spawned
        // no elites in a whole run while the ladder claimed to escalate them. They now run at four.
        assertTrue(EnemyWaveSpawner.isEliteWave(4, 6));
        assertTrue(EnemyWaveSpawner.isEliteWave(8, 6));
        assertTrue(EnemyWaveSpawner.isEliteWave(4, 8));
        assertTrue(EnemyWaveSpawner.isEliteWave(8, 8));
        assertTrue(EnemyWaveSpawner.isEliteWave(196, 6), "and the last elite wave of a run is an elite wave");
        for (int wave = 1; wave <= 200; wave++) {
            if (wave % 5 == 0) {
                assertFalse(EnemyWaveSpawner.isEliteWave(wave, 6), "wave " + wave + " is a boss wave");
                assertFalse(EnemyWaveSpawner.isEliteWave(wave, 8), "wave " + wave + " is a boss wave");
            }
        }
    }

    @Test
    void eliteWavesMarkOneOrTwoEmpoweredNonWatchers() {
        for (long seed = 1L; seed <= 10L; seed++) {
            GameState state = GameState.newRun(seed);
            spawner.spawnRegularEnemies(state, 7, EnemyWaveSpawner.MAX_REGULAR_ENEMIES);
            java.util.List<Enemy> elites = new java.util.ArrayList<>();
            for (Enemy enemy : state.aliveEnemies) {
                if (enemy.eliteAffix != null) elites.add(enemy);
            }
            assertTrue(!elites.isEmpty() && elites.size() <= 2, "seed " + seed);
            for (Enemy elite : elites) {
                assertFalse(elite.silentWatcher);
                assertTrue(com.amirrezahadipoor.herodefense.model.EliteAffix
                    .fromId(elite.eliteAffix) != null);
                Enemy regular = null;
                for (Enemy enemy : state.aliveEnemies) {
                    if (enemy.eliteAffix == null && !enemy.silentWatcher
                        && enemy.type() == elite.type()) {
                        regular = enemy;
                        break;
                    }
                }
                assertTrue(regular != null, "seed " + seed);
                assertEquals(regular.maxHealth * EnemyWaveSpawner.ELITE_HEALTH_MULT,
                    elite.maxHealth, regular.maxHealth * 1e-4f);
                assertEquals(regular.damage * EnemyWaveSpawner.ELITE_DAMAGE_MULT,
                    elite.damage, regular.damage * 1e-4f);
                assertEquals(elite.maxHealth, elite.health);
            }
        }
    }

    @Test
    void eliteMarkingIsDeterministicPerSeedAndSkipsBossWaves() {
        GameState first = GameState.newRun(4242L);
        GameState second = GameState.newRun(4242L);
        spawner.spawnRegularEnemies(first, 14, 10);
        spawner.spawnRegularEnemies(second, 14, 10);
        for (int index = 0; index < 10; index++) {
            assertEquals(first.aliveEnemies.get(index).eliteAffix,
                second.aliveEnemies.get(index).eliteAffix);
        }
        GameState bossWave = GameState.newRun(4242L);
        spawner.spawnRegularEnemies(bossWave, 35, 10);
        for (Enemy enemy : bossWave.aliveEnemies) {
            assertEquals(null, enemy.eliteAffix);
        }
    }

    @Test
    void rootwardElitesOpenNearTheirFirstShield() {
        boolean seen = false;
        for (long seed = 1L; seed <= 50L; seed++) {
            for (int wave : new int[] {7, 14, 21, 28}) {
                GameState state = GameState.newRun(seed);
                spawner.spawnRegularEnemies(state, wave, 10);
                for (Enemy enemy : state.aliveEnemies) {
                    if ("rootward_ward".equals(enemy.eliteAffix)) {
                        seen = true;
                        assertEquals(EliteAffixSystem.ROOTWARD_SHIELD_PERIOD
                            - EliteAffixSystem.ROOTWARD_FIRST_SHIELD_DELAY,
                            enemy.affixTimerSeconds, 1e-6f);
                    }
                }
            }
        }
        assertTrue(seen);
    }

    @Test
    void lateWavePopulationIsCappedToAvoidUnfairMeleeSwarms() {
        assertEquals(EnemyWaveSpawner.MAX_REGULAR_ENEMIES, spawner.regularCountForWave(100));
        assertTrue(spawner.regularCountForWave(25) < EnemyWaveSpawner.MAX_REGULAR_ENEMIES);
    }

    @Test
    void sameRunWaveAndCountProduceSameSpawnPattern() {
        GameState first = GameState.newRun(555L);
        GameState second = GameState.newRun(555L);
        spawner.spawnRegularEnemies(first, 9, 8);
        spawner.spawnRegularEnemies(second, 9, 8);
        for (int index = 0; index < 8; index++) {
            assertEquals(first.aliveEnemies.get(index).x, second.aliveEnemies.get(index).x);
            assertEquals(first.aliveEnemies.get(index).y, second.aliveEnemies.get(index).y);
        }
    }

    @Test
    void aboutTwoPercentOfRootlingsStandSilentAtTheTreeLine() {
        GameState state = GameState.newRun(77L);
        int rootlings = 0;
        int silent = 0;
        for (int wave = 1; wave <= GameState.FINAL_WAVE; wave++) {
            state.aliveEnemies.clear();
            spawner.spawnRegularEnemies(state, wave, spawner.regularCountForWave(wave));
            for (Enemy enemy : state.aliveEnemies) {
                if (enemy.type() != EnemyType.ROOTLING) {
                    assertFalse(enemy.silentWatcher);
                    continue;
                }
                rootlings++;
                if (!enemy.silentWatcher) continue;
                silent++;
                assertTrue(enemy.x >= 90f && enemy.x <= 630f);
                assertTrue(enemy.y >= 800f && enemy.y <= 860f);
                assertTrue(enemy.distanceSquaredTo(state.hero.x, state.hero.y) > 100f * 100f);
            }
        }
        float rate = (float) silent / rootlings;
        assertTrue(rate > 0.005f && rate < 0.04f, "silent rate " + rate + " over " + rootlings);
    }

    @Test
    void theDoubledRosterStaysOutOfTheOpeningAndNeverClumps() {
        // R3.4 doubled the roster, and the accepted balance evidence was measured on openings spawned from the
        // four field creatures. The stagger is what keeps that evidence valid, so it is asserted here rather
        // than left to the simulators: waves 1-10 draw from exactly four roles, deeper waves from all eight.
        for (int wave = 1; wave <= 10; wave++) {
            assertEquals(4, EnemyWaveSpawner.rosterFor(wave), "wave " + wave + " must stay on the field roster");
        }
        assertEquals(8, EnemyWaveSpawner.rosterFor(11));
        assertEquals(8, EnemyWaveSpawner.rosterFor(GameState.FINAL_WAVE));
        // Coverage: every role still appears on every wave band, and no wave is a run of one archetype -- the
        // deep blocks interleave the roster instead of listing it.
        for (int wave = 11; wave <= 40; wave++) {
            GameState state = GameState.newRun(9L + wave);
            state.aliveEnemies.clear();
            int count = Math.max(4, spawner.regularCountForWave(wave));
            spawner.spawnRegularEnemies(state, wave, count);
            java.util.Set<EnemyType> seen = java.util.EnumSet.noneOf(EnemyType.class);
            EnemyType previous = null;
            for (int index = 0; index < count; index++) {
                EnemyType type = state.aliveEnemies.get(index).type();
                seen.add(type);
                // Interleaved, not listed: two neighbours never share an archetype, so a deep wave cannot open
                // with a run of heavy bodies the way a plain eight-long cycle could.
                if (previous != null) {
                    assertNotEquals(type, previous, "wave " + wave + " opened with a clump of " + type);
                }
                previous = type;
            }
            assertTrue(seen.size() >= Math.min(4, count), "wave " + wave + " only used " + seen);
        }
    }

    @Test
    void silentWatchersAreDeterministicForTheSameSeed() {
        GameState first = GameState.newRun(4242L);
        GameState second = GameState.newRun(4242L);
        spawner.spawnRegularEnemies(first, 40, 20);
        spawner.spawnRegularEnemies(second, 40, 20);
        for (int index = 0; index < 20; index++) {
            assertEquals(
                first.aliveEnemies.get(index).silentWatcher,
                second.aliveEnemies.get(index).silentWatcher
            );
        }
    }

    /**
     * Roadmap R4.7: the ladder's elite cadence has to actually produce elites at every tier. It did not. The
     * cadence tightens one wave per three tiers, and the step from seven to five landed the cadence exactly on the
     * boss lap -- every fifth wave is a boss wave and a boss wave may not carry elites -- so tiers 6 through 8
     * spawned no elites at all, in an entire two-hundred-wave run, while the ladder claimed to escalate them. The
     * cadence now steps 7 -> 6 -> 4 and this test is what keeps it honest: every tier from 0 to 10 has to find an
     * elite in a run of this length, and no tier's cadence may be the boss interval.
     */
    @Test
    void everyTierFindsElitesOnItsOwnCadence() {
        for (int tier = 0; tier <= 10; tier++) {
            int interval = EnemyWaveSpawner.eliteWaveInterval(tier);
            assertTrue(
                interval < EnemyWaveSpawner.BOSS_WAVE_INTERVAL || interval % EnemyWaveSpawner.BOSS_WAVE_INTERVAL != 0,
                "tier " + tier + " would put every elite wave on the boss lap: " + interval
            );
            int elites = 0;
            for (int wave = 1; wave <= GameState.FINAL_WAVE; wave++) {
                if (EnemyWaveSpawner.isEliteWave(wave, tier)) {
                    elites++;
                }
            }
            int floor = GameState.FINAL_WAVE / interval - GameState.FINAL_WAVE / (interval * 5) - 1;
            assertTrue(elites >= floor, "tier " + tier + " found only " + elites + " elite waves in a run of "
                + GameState.FINAL_WAVE + ", against a cadence of one per " + interval + " waves");
        }
        assertEquals(7, EnemyWaveSpawner.eliteWaveInterval(0), "tier 0 keeps the shipped cadence");
        assertEquals(6, EnemyWaveSpawner.eliteWaveInterval(3));
        assertEquals(4, EnemyWaveSpawner.eliteWaveInterval(6));
        assertEquals(4, EnemyWaveSpawner.eliteWaveInterval(10));
    }
}
