package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.RunStrings;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.SpawnLane;

/**
 * Wave events: a scheduled change to how a wave arrives and what the night looks like.
 *
 * <p>A run's regular waves differed only in size and position on the curve -- wave 43 and wave 143 were the same
 * wave with bigger numbers. Omens (R3.4) answer that with multipliers and therefore with difficulty. Events
 * answer it with <em>geometry and weather</em>: an event never changes a body count, a health, a damage or a
 * coin, so a wave that arrives as a pincer is exactly as heavy as the same wave arriving in three lanes. That
 * is deliberate and it is the reason events could be added on top of an already-measured curve: the balance
 * gates measured arrival geometry as much as they measured totals, and the events that move geometry were
 * measured on the fixed seeds exactly like the curve restatements before them.
 *
 * <p>The schedule is a pure function of the wave number, not of the seed: four waves carry an event, then a
 * gap, and every player meets the same rhythm in the same order, which is what lets the HUD teach it. Events
 * never land on a boss wave (a boss already has a script) and never on an omen wave (two surprises in one wave
 * is noise, not a decision).
 *
 * <p>Weather events are presentation only, by construction: {@link #isWeather()} waves are asserted to leave
 * every multiplier at identity, so a weather night can never quietly become a harder night.
 */
public final class WaveEvents {

    /** The kinds of night a wave can be. Four move the bodies, four move only the air. */
    public enum Kind {
        NONE(null, null, false),
        PINCER(RunStrings.EVENT_PINCER, RunStrings.EVENT_PINCER_DETAIL, false),
        TIDAL(RunStrings.EVENT_TIDAL, RunStrings.EVENT_TIDAL_DETAIL, false),
        VANGUARD(RunStrings.EVENT_VANGUARD, RunStrings.EVENT_VANGUARD_DETAIL, false),
        SCATTER(RunStrings.EVENT_SCATTER, RunStrings.EVENT_SCATTER_DETAIL, false),
        EMBER_FALL(RunStrings.EVENT_EMBER_FALL, RunStrings.EVENT_EMBER_FALL_DETAIL, true),
        MOONFOG(RunStrings.EVENT_MOONFOG, RunStrings.EVENT_MOONFOG_DETAIL, true),
        ROOT_RAIN(RunStrings.EVENT_ROOT_RAIN, RunStrings.EVENT_ROOT_RAIN_DETAIL, true),
        SPORE_DRIFT(RunStrings.EVENT_SPORE_DRIFT, RunStrings.EVENT_SPORE_DRIFT_DETAIL, true);

        private final RunStrings label;
        private final RunStrings detail;
        private final boolean weather;

        Kind(RunStrings label, RunStrings detail, boolean weather) {
            this.label = label;
            this.detail = detail;
            this.weather = weather;
        }

        public boolean isWeather() {
            return weather;
        }

        public String label() {
            return label == null ? "" : GameLocale.text(label);
        }

        public String detail() {
            return detail == null ? "" : GameLocale.text(detail);
        }
    }

    /** The first wave that can carry an event; the opening stays plain. */
    public static final int FIRST_EVENT_WAVE = 4;
    /** One event every four waves, before the boss and omen exclusions thin it out. */
    public static final int EVENT_PERIOD = 4;
    private static final int BOSS_PERIOD = 5;
    private static final int OMEN_PERIOD = 6;
    /** The width multiplier a scattered wave spawns over. */
    static final float SCATTER_JITTER_SCALE = 2.2f;

    private WaveEvents() {
    }

    public static boolean isEventWave(int waveNumber) {
        return waveNumber >= FIRST_EVENT_WAVE
            && waveNumber % EVENT_PERIOD == 0
            && waveNumber % BOSS_PERIOD != 0
            && waveNumber % OMEN_PERIOD != 0;
    }

    public static Kind eventFor(int waveNumber) {
        if (!isEventWave(waveNumber)) {
            return Kind.NONE;
        }
        Kind[] rotation = {
            Kind.PINCER, Kind.EMBER_FALL, Kind.VANGUARD, Kind.TIDAL,
            Kind.MOONFOG, Kind.SCATTER, Kind.ROOT_RAIN, Kind.SPORE_DRIFT
        };
        return rotation[Math.floorMod(waveNumber / EVENT_PERIOD - 1, rotation.length)];
    }

    /** The lane a body at {@code index} arrives from, under this night's arrival pattern. */
    public static SpawnLane laneFor(Kind kind, int index) {
        if (kind == Kind.PINCER) {
            return index % 2 == 0 ? SpawnLane.LEFT : SpawnLane.RIGHT;
        }
        if (kind == Kind.TIDAL) {
            return SpawnLane.SOUTH;
        }
        return SpawnLane.fromIndex(index);
    }

    public static float jitterScale(Kind kind) {
        return kind == Kind.SCATTER ? SCATTER_JITTER_SCALE : 1f;
    }

    /** True when this night sends the heaviest bodies first instead of interleaving them. */
    public static boolean heaviestFirst(Kind kind) {
        return kind == Kind.VANGUARD;
    }

    /**
     * Reorders a planned wave so the heaviest archetypes walk in first.
     *
     * <p>The multiset is untouched -- the same bodies in the same numbers, in a different order -- which is what
     * makes this a readable night rather than a harder one: the player meets the wave's real weight in the first
     * ten seconds instead of the last.
     */
    public static void sortHeaviestFirst(EnemyType[] planned) {
        for (int i = 1; i < planned.length; i++) {
            EnemyType value = planned[i];
            int j = i - 1;
            while (j >= 0 && planned[j].baseHealth() < value.baseHealth()) {
                planned[j + 1] = planned[j];
                j--;
            }
            planned[j + 1] = value;
        }
    }

    /** The wave's event as the HUD should draw it, or {@link Kind#NONE}. */
    public static Kind visibleFor(int waveNumber) {
        return eventFor(waveNumber);
    }
}
