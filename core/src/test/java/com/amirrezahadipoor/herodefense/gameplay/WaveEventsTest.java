package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.SpawnLane;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The night's plan: when an event lands, where a wave arrives, and what an event may never touch. */
class WaveEventsTest {

    @Test
    void theOpeningStaysPlainAndEventsNeverLandOnABossOrAnOmen() {
        for (int wave = 1; wave < WaveEvents.FIRST_EVENT_WAVE; wave++) {
            assertFalse(WaveEvents.isEventWave(wave), "an event in the opening: " + wave);
        }
        for (int wave = WaveEvents.FIRST_EVENT_WAVE; wave <= 400; wave++) {
            if (!WaveEvents.isEventWave(wave)) continue;
            assertNotEquals(0, wave % 5, "an event on a boss wave: " + wave);
            assertNotEquals(0, wave % 6, "an event on an omen wave: " + wave);
        }
    }

    @Test
    void everyEventKindIsReachableInAnOrdinaryRun() {
        Set<WaveEvents.Kind> seen = EnumSet.noneOf(WaveEvents.Kind.class);
        // Eight kinds on a four-wave beat with boss and omen exclusions: a kind can be a hundred waves away, so the
        // window is an ordinary run's first half rather than one loop of the rotation.
        for (int wave = 1; wave <= 120; wave++) {
            seen.add(WaveEvents.eventFor(wave));
        }
        for (WaveEvents.Kind kind : WaveEvents.Kind.values()) {
            if (kind == WaveEvents.Kind.NONE) {
                continue;
            }
            assertTrue(seen.contains(kind), "unreachable event kind: " + kind);
        }
    }

    @Test
    void weatherEventsArePresentationOnly() {
        for (WaveEvents.Kind kind : WaveEvents.Kind.values()) {
            if (!kind.isWeather()) continue;
            assertEquals(1f, WaveEvents.jitterScale(kind), 0f);
            assertFalse(WaveEvents.heaviestFirst(kind));
            for (int index = 0; index < 6; index++) {
                assertEquals(
                    SpawnLane.fromIndex(index), WaveEvents.laneFor(kind, index),
                    "a weather night moved a body: " + kind);
                assertEquals(1f, WaveEvents.jitterScale(kind), 0f);
            }
        }
    }

    @Test
    void pincerTakesBothSidesAndTidalTakesTheSouth() {
        for (int index = 0; index < 8; index++) {
            SpawnLane lane = WaveEvents.laneFor(WaveEvents.Kind.PINCER, index);
            assertTrue(lane == SpawnLane.LEFT || lane == SpawnLane.RIGHT);
            assertEquals(SpawnLane.SOUTH, WaveEvents.laneFor(WaveEvents.Kind.TIDAL, index));
        }
    }

    @Test
    void vanguardReordersAndNeverChangesWhatTheWaveIsMadeOf() {
        EnemyType[] planned = {EnemyType.ROOTLING, EnemyType.FUNGAL_BRUTE, EnemyType.GLOOM_WOLF};
        EnemyType[] before = planned.clone();
        WaveEvents.sortHeaviestFirst(planned);
        assertEquals(EnemyType.FUNGAL_BRUTE, planned[0]);
        java.util.List<EnemyType> sorted = new java.util.ArrayList<>(java.util.List.of(planned));
        java.util.List<EnemyType> original = new java.util.ArrayList<>(java.util.List.of(before));
        sorted.sort(java.util.Comparator.comparing(EnemyType::name));
        original.sort(java.util.Comparator.comparing(EnemyType::name));
        assertEquals(original, sorted, "the multiset of a wave must survive the reorder");
    }

    @Test
    void theSameWaveArrivesTheSameWayOnEverySeed() {
        // The schedule is a function of the wave, not of the run: two players on different seeds meet the same
        // rhythm, in the same order, from the same edges. Positions still carry each seed's own jitter.
        GameState first = new GameState();
        first.runSeed = 12345L;
        GameState second = new GameState();
        second.runSeed = 987654321L;
        EnemyWaveSpawner spawner = new EnemyWaveSpawner(new EnemyFactory());
        int wave = 16;
        assertTrue(WaveEvents.isEventWave(wave));
        spawner.spawnRegularEnemies(first, wave, 6);
        spawner.spawnRegularEnemies(second, wave, 6);
        assertEquals(first.aliveEnemies.size(), second.aliveEnemies.size());
        for (int index = 0; index < first.aliveEnemies.size(); index++) {
            assertEquals(
                first.aliveEnemies.get(index).spawnLane,
                second.aliveEnemies.get(index).spawnLane,
                "the edge a body walked in from, on body " + index);
            assertEquals(
                first.aliveEnemies.get(index).enemyType,
                second.aliveEnemies.get(index).enemyType,
                "the archetype of body " + index);
        }
    }
}
