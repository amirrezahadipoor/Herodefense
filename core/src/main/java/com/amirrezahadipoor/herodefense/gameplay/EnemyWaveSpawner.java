package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.model.EliteAffix;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.SpawnLane;
import com.amirrezahadipoor.herodefense.model.WaveModifier;
import com.amirrezahadipoor.herodefense.render.ScreenEdges;

import java.util.ArrayList;
import java.util.List;

/** Deterministically distributes each regular wave over three arena edges. */
public final class EnemyWaveSpawner {
    public static final float EDGE_OFFSET = 40f;
    public static final int MAX_REGULAR_ENEMIES = 24;
    /** First wave admitted past the shipped ceiling (P6 longer waves). */
    public static final int RAISED_CAP_FIRST_WAVE = 120;
    /** The body's ceiling at the very end of a run. */
    public static final int RAISED_CAP_MAX_ENEMIES = 28;
    /** Waves per step of the climb from the shipped ceiling to the raised one. */
    public static final int RAISED_CAP_STEP_WAVES = 20;
    /** Smallest wave that arrives in pulses rather than whole. */
    static final int FIRST_TRICKLED_WAVE_SIZE = 8;
    /**
     * Surge waves (roadmap A4): scheduled payday waves in the run's second half -- every tenth
     * wave, offset to +8 so the schedule never lands on a boss lap, pays 1.25x coins. The bodies
     * half of the idea was vetoed by the shipped pressure contract: any body reinforcement in
     * 101-150 lifted that quarter's mean more than the five-percent quarter-shape rule allows
     * (measured: +4 bodies on four waves broke it on two of five gate seeds). The coins stand at
     * 1.25x: 1.5x crossed purchase thresholds on a gate seed and cascaded its whole curve, 1.25x
     * measured as exactly zero shape damage on all five.
     */
    public static final int SURGE_FIRST_WAVE = 101;
    public static final int SURGE_LAST_WAVE = 200;
    public static final int SURGE_EVERY = 10;
    public static final int SURGE_OFFSET = 3;
    public static final float SURGE_COIN_MULTIPLIER = 1.25f;
    /** The wave the doubled Elite affix pool opens at (roadmap D2); below it only the base three draw. */
    public static final int LATE_AFFIX_WAVE = 101;
    private static final float SIDE_JITTER = 180f;
    private static final float SOUTH_JITTER = 250f;
    /** One in fifty Rootling spawns stands silent at the tree line ("The Quiet Ones"). */
    static final int SILENT_WATCHER_ONE_IN = 50;
    /** Elites carry roughly triple health and half-again damage. */
    public static final float ELITE_HEALTH_MULT = 3f;
    public static final float ELITE_DAMAGE_MULT = 1.5f;
    /**
     * The second half carries a softer elite contact multiplier (roadmap R4.6). Every spike the balance
     * gates have caught late in a run sits on an elite wave -- 126, 133, 154, 182, 196 are all elite waves
     * on the shipped spawn schedule -- and the flat 1.5 was multiplying a damage baseline that already
     * climbed for a hundred more waves. Measured over the five trial seeds: 1.2 buys 0.01-0.03 of spike
     * headroom on the worst pairs and moves the average pressure by less than 0.001, because an elite's
     * damage does not change how long its wave takes. The first half keeps the shipped 1.5, so every brief
     * vigil and every tier-0 gate stays bit-identical.
     */
    public static final float ELITE_SECOND_HALF_DAMAGE_MULT = 1.2f;

    /**
     * The elite contact multiplier a wave pays (roadmap R4.7 measured this interaction). Position decides it, and
     * only position: the first half keeps the shipped 1.5 and the second half pays the softer 1.2. The ascension
     * ladder's base damage charge multiplies that too, deliberately, because the charge multiplies every enemy's
     * baseline and an elite is an enemy -- which is why the ladder's spike ceiling is tier-indexed in
     * `AscensionGateTest` (0.40 + 0.02 per tier) rather than flat: an elite's contact is the sharpest the charge
     * gets, it is the one hit of theirs a player cannot walk away from, and the gate prices it instead of pretending
     * it is not there.
     */
    public static float eliteDamageMultiplier(int waveNumber) {
        return waveNumber > GameState.PLANTING_WAVE ? ELITE_SECOND_HALF_DAMAGE_MULT : ELITE_DAMAGE_MULT;
    }
    private static final long ELITE_SALT = 0xE11E7AFF1E57A1E5L;
    /** Tree-line box where Silent Rootling watchers stand and never leave. */
    static final float TREE_LINE_MIN_X = 90f;
    static final float TREE_LINE_MAX_X = 630f;
    static final float TREE_LINE_MIN_Y = 800f;
    static final float TREE_LINE_MAX_Y = 860f;

    private final EnemyFactory factory;

    public EnemyWaveSpawner(EnemyFactory factory) {
        this.factory = factory;
    }

    public int regularCountForWave(int waveNumber) {
        return Math.min(
            maxRegularEnemiesForWave(waveNumber),
            Math.max(3, 4 + Math.max(1, waveNumber) / 2)
        );
    }

    /**
     * The arena's own ceiling for a wave (P6 longer waves). The shipped 24 holds until wave 120,
     * then the ceiling climbs one body per twenty waves to 28 at wave 180, so the last stretch
     * of a run fields deeper waves without a sudden four-body ambush at the boundary.
     */
    public static int maxRegularEnemiesForWave(int waveNumber) {
        if (waveNumber < RAISED_CAP_FIRST_WAVE) {
            return MAX_REGULAR_ENEMIES;
        }
        return Math.min(RAISED_CAP_MAX_ENEMIES,
            MAX_REGULAR_ENEMIES + (waveNumber - RAISED_CAP_FIRST_WAVE) / RAISED_CAP_STEP_WAVES + 1);
    }

    /**
     * The night's plan for a wave in pulses (P6 longer waves). Skirmishes walk in whole -- a
     * four-body wave split in two would stutter instead of pressing, and splitting the opening
     * inverted the brief vigil's ramp (measured opening 1.04 over closing 0.92). Waves of 8+
     * come in three pulses: roughly sixty percent up front, a quarter behind, the rest last,
     * with the last pulse never a lone straggler.
     */
    static int[] planTrickles(int total) {
        if (total < FIRST_TRICKLED_WAVE_SIZE) {
            return new int[] {Math.max(1, total)};
        }
        int first = Math.round(total * 0.6f);
        int second = Math.round(total * 0.25f);
        int third = total - first - second;
        if (third < 2) {
            first -= 2 - third;
            third = 2;
        }
        return new int[] {first, second, third};
    }

    /**
     * How many roster entries a wave draws from (R3.4).
     *
     * <p>The roster doubles from four to eight, and the four additions enter the cycle at
     * {@link #DEEP_ROSTER_FIRST_WAVE} rather than at wave one. Reason: the accepted balance evidence (the
     * two-hundred-wave sweep and the thirty-wave brief sweep) was measured against waves whose first ten were
     * spawned from the four field creatures, and the deep roster's faster rotation pulls heavy archetypes into
     * the opening. Staggering the entry keeps the opening bit-identical to that evidence and makes the new
     * creatures a depth beat instead of a difficulty rewrite. `EnemyWaveSpawnerTest` asserts both halves.
     */
    static int rosterFor(int waveNumber) {
        return waveNumber >= DEEP_ROSTER_FIRST_WAVE ? EnemyType.values().length : FIELD_ROSTER;
    }

    /** The first wave whose spawns may draw from the doubled roster. */
    public static final int DEEP_ROSTER_FIRST_WAVE = 11;

    /** The four field creatures, and the head of {@link EnemyType#values()}. */
    static final int FIELD_ROSTER = 4;

    /** Coprime with the eight-role roster, so one block still holds every archetype exactly once. */
    static final int DEEP_ROSTER_STRIDE = 3;

    /**
     * Elite cadence tightens one wave every three tiers, floored at every 4th wave -- and never five waves, because
     * every fifth wave of this game is a boss wave and {@link #isEliteWave} excludes those. A cadence of five is
     * therefore not a cadence at all: it lands on the boss lap every time, and tiers 6 through 8 spawned **zero**
     * elites in an entire run (measured while pinning R4.7's elite interaction: `theLadderChargesAnElite...` asked
     * for a tier-6 elite wave and there was none to find between waves 101 and 199, or anywhere else). The ladder
     * therefore steps 7 -> 6 -> 4, and `EnemyWaveSpawnerTest` asserts that every tier actually gets elites.
     */
    public static int eliteWaveInterval(int ascensionTier) {
        int interval = Math.max(4, 7 - Math.max(0, ascensionTier) / 3);
        return interval == BOSS_WAVE_INTERVAL ? interval - 1 : interval;
    }

    /** Boss waves: every fifth one, which is why an elite cadence may never be five. */
    public static final int BOSS_WAVE_INTERVAL = 5;

    public static boolean isEliteWave(int waveNumber, int ascensionTier) {
        return waveNumber > 0
            && waveNumber % eliteWaveInterval(ascensionTier) == 0
            && waveNumber % BOSS_WAVE_INTERVAL != 0;
    }

    /**
     * True when this wave is on the surge schedule (roadmap A4). The schedule never lands on a
     * boss wave by construction; an elite-wave collision is excluded at the tier-aware call sites.
     */
    public static boolean isSurgeWave(int waveNumber) {
        return waveNumber >= SURGE_FIRST_WAVE
            && waveNumber <= SURGE_LAST_WAVE
            && waveNumber % SURGE_EVERY == SURGE_OFFSET;
    }

    /** A regular wave's body count including the SWARM omen, still capped by the arena's own ceiling. */
    public static int omenAdjustedCount(GameState state, int waveNumber, int count) {
        WaveModifier omen = WaveOmens.of(state, waveNumber);
        if (!omen.isOmen()) return count;
        return Math.min(maxRegularEnemiesForWave(waveNumber),
            Math.max(1, Math.round(count * omen.enemyCountMultiplier())));
    }

    public void spawnRegularEnemies(GameState state, int waveNumber, int count) {
        if (state == null || count <= 0) {
            return;
        }
        int spawnCount = omenAdjustedCount(state, waveNumber, count);
        int firstIndex = state.aliveEnemies.size();
        spawnBodies(state, waveNumber, planBodies(waveNumber, spawnCount), 0, spawnCount);
        if (isEliteWave(waveNumber, state.ascensionTier)) {
            // The omen never reaches this branch (elite waves draw NONE), so the adjusted count is
            // the requested count here; passing what was actually added keeps the elite marking
            // inside the bodies this call spawned.
            markElites(state, waveNumber, firstIndex, spawnCount);
        }
    }

    /**
     * Walks one trickle pulse of a regular wave into the arena: {@code count} bodies of the
     * wave's whole plan, starting at {@code bodyOffset}. Elites are marked from the first pulse
     * only, so an elite wave still carries exactly the one or two empowered bodies the shipped
     * schedule promised.
     */
    public void spawnTrickle(
        GameState state, int waveNumber, int waveTotal, int bodyOffset, int count
    ) {
        if (state == null || count <= 0 || waveTotal <= 0 || bodyOffset < 0) {
            return;
        }
        int bodies = Math.min(count, waveTotal - bodyOffset);
        if (bodies <= 0) {
            return;
        }
        int firstIndex = state.aliveEnemies.size();
        spawnBodies(state, waveNumber, planBodies(waveNumber, waveTotal), bodyOffset, bodies);
        if (bodyOffset == 0 && isEliteWave(waveNumber, state.ascensionTier)) {
            markElites(state, waveNumber, firstIndex, bodies);
        }
    }

    /**
     * The night's plan for this wave: the same bodies in the same numbers, decided before
     * anything walks in, because a vanguard wave reorders the arrivals and a scatter wave widens
     * them. Computed whole even for a trickled wave, so every pulse is a slice of one ordering
     * and the event still means what it meant.
     */
    private static EnemyType[] planBodies(int waveNumber, int total) {
        EnemyType[] types = EnemyType.values();
        WaveEvents.Kind event = WaveEvents.eventFor(waveNumber);
        EnemyType[] planned = new EnemyType[Math.max(0, total)];
        int rosterForPlanned = rosterFor(waveNumber);
        int strideForPlanned = rosterForPlanned > FIELD_ROSTER ? DEEP_ROSTER_STRIDE : 1;
        for (int index = 0; index < planned.length; index++) {
            planned[index] = types[Math.floorMod(
                waveNumber - 1 + index * strideForPlanned, rosterForPlanned)];
        }
        if (WaveEvents.heaviestFirst(event)) {
            WaveEvents.sortHeaviestFirst(planned);
        } else if (WaveEvents.lightestFirst(event)) {
            WaveEvents.sortLightestFirst(planned);
        }
        return planned;
    }

    /**
     * Walks a slice of the night's plan into the arena. Every hash reads the body's own index in
     * the whole plan, so a trickled wave spawns exactly the bodies the untrickled wave would have.
     */
    private void spawnBodies(
        GameState state, int waveNumber, EnemyType[] planned, int bodyOffset, int count
    ) {
        WaveEvents.Kind event = WaveEvents.eventFor(waveNumber);
        for (int index = 0; index < count; index++) {
            int body = bodyOffset + index;
            SpawnLane lane = WaveEvents.laneFor(event, body);
            float jitter = signedUnit(state.runSeed, waveNumber, body)
                * WaveEvents.jitterScale(event);
            float x;
            float y;
            switch (lane) {
                case LEFT -> {
                    x = -EDGE_OFFSET;
                    y = WorldLayout.HERO_CENTER_Y + jitter * SIDE_JITTER;
                }
                case RIGHT -> {
                    x = WorldLayout.REFERENCE_WIDTH + EDGE_OFFSET;
                    y = WorldLayout.HERO_CENTER_Y + jitter * SIDE_JITTER;
                }
                case SOUTH -> {
                    x = WorldLayout.HERO_CENTER_X + jitter * SOUTH_JITTER;
                    // Tall panels reveal world below y 0; hide the spawn under the true edge.
                    y = ScreenEdges.bottom() - EDGE_OFFSET;
                }
                default -> throw new IllegalStateException("Unhandled spawn lane: " + lane);
            }
            EnemyType type = planned[body];
            Enemy enemy = factory.createForWave(
                state, type, x, y, lane.id(), waveNumber
            );
            if (type == EnemyType.ROOTLING && isSilentWatcher(state.runSeed, waveNumber, body)) {
                enemy.silentWatcher = true;
                enemy.x = TREE_LINE_MIN_X + watcherUnit(state.runSeed, waveNumber, body, 1L)
                    * (TREE_LINE_MAX_X - TREE_LINE_MIN_X);
                enemy.y = TREE_LINE_MIN_Y + watcherUnit(state.runSeed, waveNumber, body, 2L)
                    * (TREE_LINE_MAX_Y - TREE_LINE_MIN_Y);
            }
            state.aliveEnemies.add(enemy);
        }
    }

    /** Marks 1-2 non-watcher spawns as Elites with deterministic hash-picked affixes. */
    private static void markElites(GameState state, int waveNumber, int firstIndex, int count) {
        List<Integer> candidates = new ArrayList<>();
        for (int offset = 0; offset < count; offset++) {
            Enemy enemy = state.aliveEnemies.get(firstIndex + offset);
            if (enemy != null && !enemy.silentWatcher) candidates.add(firstIndex + offset);
        }
        if (candidates.isEmpty()) return;
        int elites = 1 + Math.floorMod(watcherMix(state.runSeed, waveNumber, 0, ELITE_SALT), 2);
        for (int pick = 0; pick < elites && !candidates.isEmpty(); pick++) {
            int slot = Math.floorMod(
                watcherMix(state.runSeed, waveNumber, 11 + pick, ELITE_SALT), candidates.size());
            Enemy elite = state.aliveEnemies.get(candidates.remove(slot));
            // D2's three new affixes enter the draw at LATE_AFFIX_WAVE; below it the pool size stays 3
            // and every roll is bit-identical to the shipped curve (the B2a lesson: never remap a draw
            // the frozen baseline was measured on).
            int poolSize = waveNumber >= LATE_AFFIX_WAVE
                ? EliteAffix.values().length : EliteAffix.BASE_POOL_SIZE;
            EliteAffix affix = EliteAffix.values()[Math.floorMod(
                watcherMix(state.runSeed, waveNumber, 101 + pick, ELITE_SALT),
                poolSize)];
            elite.eliteAffix = affix.id();
            if (affix == EliteAffix.ROOTWARD_WARD) {
                elite.affixTimerSeconds = EliteAffixSystem.ROOTWARD_SHIELD_PERIOD
                    - EliteAffixSystem.ROOTWARD_FIRST_SHIELD_DELAY;
            }
            elite.health *= ELITE_HEALTH_MULT;
            elite.maxHealth *= ELITE_HEALTH_MULT;
            elite.damage *= eliteDamageMultiplier(waveNumber);
        }
    }

    private static float signedUnit(long seed, int wave, int index) {
        long value = seed + 0x9E3779B97F4A7C15L * (wave * 31L + index + 1L);
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return ((value >>> 40) / 8_388_607.5f) - 1f;
    }

    private static boolean isSilentWatcher(long seed, int wave, int index) {
        return Math.floorMod(
            watcherMix(seed, wave, index, 0xC2B280737A5763D5L), SILENT_WATCHER_ONE_IN
        ) == 0;
    }

    private static float watcherUnit(long seed, int wave, int index, long salt) {
        return (watcherMix(seed, wave, index, 0x165667B19E3779F9L + salt) >>> 40) / 16_777_216f;
    }

    private static long watcherMix(long seed, int wave, int index, long salt) {
        long value = seed + salt * (wave * 131L + index * 17L + 1L);
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return value;
    }
}
