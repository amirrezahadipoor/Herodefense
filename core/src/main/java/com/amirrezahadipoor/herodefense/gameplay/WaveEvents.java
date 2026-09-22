package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;

import java.util.Arrays;
import java.util.Comparator;
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
 * is noise, not a decision). Twelve kinds take turns: one night per event wave, and after the twelfth the plan
 * starts again, so a long run sees every kind as often as every other.
 *
 * <p>Weather events are presentation only, by construction: {@link #isWeather()} waves are asserted to leave
 * every multiplier at identity, so a weather night can never quietly become a harder night.
 */
public final class WaveEvents {

    /** The kinds of night a wave can be. Seven move the bodies, five move only the air. */
    public enum Kind {
        NONE(null, null, false),
        PINCER(RunStrings.EVENT_PINCER, RunStrings.EVENT_PINCER_DETAIL, false),
        TIDAL(RunStrings.EVENT_TIDAL, RunStrings.EVENT_TIDAL_DETAIL, false),
        WEDGE(RunStrings.EVENT_WEDGE, RunStrings.EVENT_WEDGE_DETAIL, false),
        VANGUARD(RunStrings.EVENT_VANGUARD, RunStrings.EVENT_VANGUARD_DETAIL, false),
        TRICKLE(RunStrings.EVENT_TRICKLE, RunStrings.EVENT_TRICKLE_DETAIL, false),
        SCATTER(RunStrings.EVENT_SCATTER, RunStrings.EVENT_SCATTER_DETAIL, false),
        ENCIRCLE(RunStrings.EVENT_ENCIRCLE, RunStrings.EVENT_ENCIRCLE_DETAIL, false),
        EMBER_FALL(RunStrings.EVENT_EMBER_FALL, RunStrings.EVENT_EMBER_FALL_DETAIL, true),
        MOONFOG(RunStrings.EVENT_MOONFOG, RunStrings.EVENT_MOONFOG_DETAIL, true),
        ROOT_RAIN(RunStrings.EVENT_ROOT_RAIN, RunStrings.EVENT_ROOT_RAIN_DETAIL, true),
        SPORE_DRIFT(RunStrings.EVENT_SPORE_DRIFT, RunStrings.EVENT_SPORE_DRIFT_DETAIL, true),
        ASH_FALL(RunStrings.EVENT_ASH_FALL, RunStrings.EVENT_ASH_FALL_DETAIL, true);

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

    /**
     * The first wave that can carry an event. The first fifteen waves are the shipped opening, unchanged to the
     * byte -- two bosses, no events, no verbs -- because the pressure gates measure the opening by what a player
     * who does not optimise survives, and a teaching night is not allowed to become an exam on the new systems.
     * The night's plan and the roles' verbs both begin here, together, with the HUD naming the first one.
     */
    public static final int FIRST_EVENT_WAVE = 16;
    /** One event every four waves, before the boss and omen exclusions thin it out. */
    public static final int EVENT_PERIOD = 4;
    private static final int BOSS_PERIOD = 5;
    private static final int OMEN_PERIOD = 6;
    /** The width multiplier a scattered wave spawns over. */
    static final float SCATTER_JITTER_SCALE = 2.2f;
    /** A wedge is a column, not a wall: the spread of the south road tightened to a spearhead. */
    static final float WEDGE_JITTER_SCALE = 0.45f;
    /** An encircling wave arrives from every door, so it uses each one and spreads the width of it. */
    static final float ENCIRCLE_JITTER_SCALE = 1.8f;
    /** The three roads, in the order an encircling wave walks them: left, right, then the middle. */
    private static final SpawnLane[] ENCIRCLE_LANES = {SpawnLane.LEFT, SpawnLane.RIGHT, SpawnLane.SOUTH};

    private WaveEvents() {
    }

    public static boolean isEventWave(int waveNumber) {
        return waveNumber >= FIRST_EVENT_WAVE
            && waveNumber % EVENT_PERIOD == 0
            && waveNumber % BOSS_PERIOD != 0
            && waveNumber % OMEN_PERIOD != 0;
    }

    /**
     * The order the nights arrive in, one per event wave, cycling every twelve.
     *
     * <p>A counter over the run's event waves rather than a function of the wave number, and that is the fix to a
     * quiet flaw in the first rotation: indexing by {@code waveNumber} meant a kind whose slot always landed on a
     * boss or an omen wave was a night nobody would ever see. Counting the event waves instead gives every kind
     * exactly one night per cycle, so all twelve kinds are nights a player actually fights.
     *
     * <p>The order is a tide rather than a shuffle. The four nights inside the opening -- the fifteen waves the
     * pressure gates measure by what a player who does not optimise survives -- are the four the shipped curve was
     * measured with, in the order it was measured in: tidal at wave 16, root rain at 28, spore drift at 32 and
     * vanguard at 44 stay exactly where they were, so every number recorded for the opening still describes the
     * night the player meets. From there each cycle climbs: the soft mover (trickle) and the wedge open the middle,
     * the two nights that spin up the day's income and armour (ash fall, ember fall) sit inside it, and the cycle
     * closes on the four heaviest nights -- pincer, scatter, encircle and moonfog -- which is what keeps the last
     * quarter of a two-hundred-wave run heavier than the first. None of this touches a multiplier: the order moves
     * <em>when</em> a night happens, never what it costs.
     */
    private static final Kind[] ROTATION = {
        Kind.TIDAL, Kind.ROOT_RAIN, Kind.SPORE_DRIFT, Kind.VANGUARD, Kind.TRICKLE, Kind.WEDGE,
        Kind.ASH_FALL, Kind.PINCER, Kind.EMBER_FALL, Kind.SCATTER, Kind.ENCIRCLE, Kind.MOONFOG
    };
    /** How many of the first {@code J} four-wave beats are an event wave: the beats at the top of the curve. */
    private static final int[] BEAT_PREFIX = {0, 1, 2, 2, 3, 3, 3, 4, 5, 5, 5, 6, 6, 7, 8};
    private static final int BEAT_PERIOD = 15;
    private static final int BEATS_IN_PERIOD = 8;
    /** The beats before the first event wave: 1, 2 and 3 are beaten out by the opening. */
    private static final int OPENING_EVENT_BEATS = 2;

    public static Kind eventFor(int waveNumber) {
        if (!isEventWave(waveNumber)) {
            return Kind.NONE;
        }
        return ROTATION[Math.floorMod(eventsBefore(waveNumber) - 1, ROTATION.length)];
    }

    /**
     * How many event waves the run has fought once {@code waveNumber} starts, this one included.
     *
     * <p>An event wave is a beat of four waves that is neither a boss beat (every fifth) nor an omen beat (every
     * sixth), so the beat numbers that carry an event repeat with a period of fifteen and there are eight of them
     * in it. Counting through the pattern instead of looping the wave numbers keeps this cheap enough to call from
     * the HUD every frame.
     */
    static int eventsBefore(int waveNumber) {
        int beat = waveNumber / EVENT_PERIOD;
        int whole = beat / BEAT_PERIOD;
        return whole * BEATS_IN_PERIOD + BEAT_PREFIX[beat % BEAT_PERIOD] - OPENING_EVENT_BEATS;
    }

    /** The lane a body at {@code index} arrives from, under this night's arrival pattern. */
    public static SpawnLane laneFor(Kind kind, int index) {
        if (kind == Kind.PINCER) {
            return index % 2 == 0 ? SpawnLane.LEFT : SpawnLane.RIGHT;
        }
        if (kind == Kind.TIDAL || kind == Kind.WEDGE) {
            return SpawnLane.SOUTH;
        }
        if (kind == Kind.ENCIRCLE) {
            return ENCIRCLE_LANES[index % ENCIRCLE_LANES.length];
        }
        return SpawnLane.fromIndex(index);
    }

    public static float jitterScale(Kind kind) {
        if (kind == Kind.SCATTER) {
            return SCATTER_JITTER_SCALE;
        }
        if (kind == Kind.WEDGE) {
            return WEDGE_JITTER_SCALE;
        }
        if (kind == Kind.ENCIRCLE) {
            return ENCIRCLE_JITTER_SCALE;
        }
        return 1f;
    }

    /** True when this night sends the heaviest bodies first instead of interleaving them. */
    public static boolean heaviestFirst(Kind kind) {
        return kind == Kind.VANGUARD;
    }

    /** True when this night sends its lightest bodies first: the mirror of a vanguard, and the longer decision. */
    public static boolean lightestFirst(Kind kind) {
        return kind == Kind.TRICKLE;
    }

    /**
     * Reorders a planned wave so the heaviest archetypes walk in first.
     *
     * <p>The multiset is untouched -- the same bodies in the same numbers, in a different order -- which is what
     * makes this a readable night rather than a harder one: the player meets the wave's real weight in the first
     * ten seconds instead of the last.
     */
    public static void sortHeaviestFirst(EnemyType[] planned) {
        // A stable sort on purpose: two archetypes of the same weight keep the order the wave was built in, so a
        // vanguard night is the same wave in a different order rather than a new draw.
        Arrays.sort(planned, Comparator.comparingDouble(EnemyType::baseHealth).reversed());
    }

    /**
     * Reorders a planned wave so the lightest archetypes walk in first: the same multiset, the opposite decision.
     *
     * <p>A vanguard night asks the player to spend everything in the first ten seconds; a trickle night asks the
     * opposite, and offers the coins to spend on it. Both are the same wave -- no body count, health or damage is
     * touched -- which is what lets them sit on a curve that was measured before either existed.
     */
    public static void sortLightestFirst(EnemyType[] planned) {
        Arrays.sort(planned, Comparator.comparingDouble(EnemyType::baseHealth));
    }

    /** The wave's event as the HUD should draw it, or {@link Kind#NONE}. */
    public static Kind visibleFor(int waveNumber) {
        return eventFor(waveNumber);
    }
}
