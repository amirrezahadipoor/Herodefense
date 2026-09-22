package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.SpawnLane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
        // Twelve kinds take turns one night per event wave, so the window is a run's first half (four hundred
        // waves, three full cycles of the rotation) rather than one loop of it.
        for (int wave = 1; wave <= 400; wave++) {
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
    void everyKindTakesExactlyOneNightPerCycleAndTheOpeningKeepsItsTwo() {
        // The four nights inside the opening -- the run's first quarter, which is what the pressure gates measure
        // by what a player who does not optimise survives -- are the four the shipped curve was measured with, in
        // the order it was measured in. The new nights begin after them.
        WaveEvents.Kind[] measured = {
            WaveEvents.Kind.TIDAL, WaveEvents.Kind.ROOT_RAIN, WaveEvents.Kind.SPORE_DRIFT, WaveEvents.Kind.VANGUARD
        };
        int night = 0;
        for (int wave = WaveEvents.FIRST_EVENT_WAVE; wave <= 50; wave++) {
            if (!WaveEvents.isEventWave(wave)) {
                continue;
            }
            assertEquals(measured[night], WaveEvents.eventFor(wave),
                "the night the opening was measured with at wave " + wave + " moved");
            night++;
        }
        assertEquals(measured.length, night, "four event waves must land in the first fifty waves");
        Set<WaveEvents.Kind> after = EnumSet.noneOf(WaveEvents.Kind.class);
        for (int wave = 51; wave <= 120; wave++) {
            after.add(WaveEvents.eventFor(wave));
        }
        after.remove(WaveEvents.Kind.NONE);
        assertTrue(after.size() >= 6, "the nights after the opening must be new ones, saw " + after);

        Map<WaveEvents.Kind, Integer> nights = new EnumMap<>(WaveEvents.Kind.class);
        for (int wave = 1; wave <= 400; wave++) {
            if (!WaveEvents.isEventWave(wave)) {
                continue;
            }
            nights.merge(WaveEvents.eventFor(wave), 1, Integer::sum);
        }
        assertEquals(12, nights.size(), "a cycle of twelve kinds must see all twelve: " + nights);
        for (Map.Entry<WaveEvents.Kind, Integer> taken : nights.entrySet()) {
            assertTrue(taken.getValue() >= 3,
                taken.getKey() + " took only " + taken.getValue() + " nights in 400 waves");
        }
        int most = Collections.max(nights.values());
        int least = Collections.min(nights.values());
        assertTrue(most - least <= 1, "the rotation must be even: " + nights);
    }

    @Test
    void everyEventWaveHasANightAndEveryNightIsAnEventWave() {
        // The night the HUD names is the night the plan drew: a wave that is an event wave always has a kind, and
        // a wave that is not is never given one. A kind silently attached to a boss or an omen wave would announce
        // a night nobody is playing.
        for (int wave = 1; wave <= 400; wave++) {
            boolean event = WaveEvents.isEventWave(wave);
            WaveEvents.Kind kind = WaveEvents.eventFor(wave);
            if (event) {
                assertNotEquals(WaveEvents.Kind.NONE, kind, "an event wave with no night: " + wave);
            } else {
                assertEquals(WaveEvents.Kind.NONE, kind, "a night on a wave that is not an event wave: " + wave);
            }
        }
    }

    @Test
    void aMovingNightChangesOneArrivalPropertyAndWeatherChangesNone() {
        for (WaveEvents.Kind kind : WaveEvents.Kind.values()) {
            if (kind == WaveEvents.Kind.NONE) {
                continue;
            }
            boolean moved = WaveEvents.jitterScale(kind) != 1f
                || WaveEvents.heaviestFirst(kind)
                || WaveEvents.lightestFirst(kind);
            if (kind == WaveEvents.Kind.PINCER || kind == WaveEvents.Kind.ENCIRCLE) {
                moved = true;
            }
            if (kind.isWeather()) {
                assertFalse(moved, "a weather night moved the bodies: " + kind);
            }
        }
        assertEquals(WaveEvents.ENCIRCLE_JITTER_SCALE, WaveEvents.jitterScale(WaveEvents.Kind.ENCIRCLE), 0f);
        assertTrue(WaveEvents.lightestFirst(WaveEvents.Kind.TRICKLE));
        assertFalse(WaveEvents.heaviestFirst(WaveEvents.Kind.TRICKLE));
    }

    @Test
    void trickleIsTheMirrorOfAVanguardAndNeitherChangesWhatTheWaveIsMadeOf() {
        EnemyType[] trickle = {EnemyType.FUNGAL_BRUTE, EnemyType.ROOTLING, EnemyType.GLOOM_WOLF};
        EnemyType[] vanguard = trickle.clone();
        WaveEvents.sortLightestFirst(trickle);
        WaveEvents.sortHeaviestFirst(vanguard);
        assertEquals(EnemyType.FUNGAL_BRUTE, vanguard[0]);
        assertEquals(vanguard[0], trickle[trickle.length - 1], "the mirror of a vanguard walks in last");
        assertEquals(vanguard[vanguard.length - 1], trickle[0]);
        List<EnemyType> one = new ArrayList<>(List.of(trickle));
        List<EnemyType> other = new ArrayList<>(List.of(vanguard));
        one.sort(Comparator.comparing(EnemyType::name));
        other.sort(Comparator.comparing(EnemyType::name));
        assertEquals(other, one, "a trickle is the same wave in a different order, never a new draw");
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
