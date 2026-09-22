package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * The eight roles' verbs: what a creature does that another creature does not.
 *
 * <p>Before this, all eight regular roles closed to melee range and swung, so the roster differed in numbers and
 * not in behaviour -- a wave was a quantity, never a question. Four verbs give the three roles whose silhouettes
 * already implied them something only they do:
 *
 * <table>
 * <caption>Verb by role</caption>
 * <tr><th>Role</th><th>Verb</th><th>What it asks of the player</th></tr>
 * <tr><td>Bark Stalker</td><td>spits from beyond melee reach</td>
 *     <td>Close it down, or step out of the tell -- the one regular attack in the game that a step avoids.</td></tr>
 * <tr><td>Fungal Brute</td><td>mends its wounded neighbours</td>
 *     <td>Kill it before the wave it is standing in, not after.</td></tr>
 * <tr><td>Husk Warden</td><td>raises a shared ward over nearby allies</td>
 *     <td>Break the warden or spend your damage on a shielded line.</td></tr>
 * <tr><td>Gloom Wolf</td><td>moves faster the more of its pack is near</td>
 *     <td>Thin the pack instead of the front runner.</td></tr>
 * </table>
 *
 * <p>Every verb is self-limiting and readable on purpose. The spitter stops walking while it winds up, so it
 * trades tempo for reach; the mend is capped in both size and targets and only touches allies already below
 * three fifths of their bar; the ward is a short window on two allies; the pack bonus is capped below the Hero's
 * walking speed so nothing here can outrun the step budget the movement contract is written against. None of the
 * four touches a wave's body count, and none of them fires before {@link #FIRST_VERB_WAVE}, so the opening still
 * teaches the plain game.
 */
public final class EnemyVerbs {

    /** The first wave whose creatures use their verbs; the first five waves stay plain. */
    public static final int FIRST_VERB_WAVE = 6;

    public static final float SPIT_RANGE = 330f;
    public static final float SPIT_WINDUP_SECONDS = 0.75f;
    public static final float SPIT_DAMAGE_SHARE = 0.85f;
    /** How far the Hero must have moved off the line the spit was aimed down to be missed. */
    public static final float SPIT_ESCAPE_UNITS = 72f;

    public static final float MEND_PERIOD_SECONDS = 7f;
    public static final float MEND_SHARE = 0.05f;
    public static final float MEND_WOUNDED_BELOW = 0.6f;
    public static final float MEND_RADIUS = 140f;
    public static final int MEND_MAX_TARGETS = 3;

    public static final float WARD_PERIOD_SECONDS = 9f;
    public static final float WARD_DURATION_SECONDS = 3f;
    public static final float WARD_RADIUS = 150f;
    public static final int WARD_MAX_ALLIES = 2;

    public static final float PACK_RADIUS = 150f;
    public static final float PACK_SPEED_PER_WOLF = 0.06f;
    public static final float PACK_SPEED_CAP = 0.18f;

    /** Where damage from a verb goes once it lands; wired to the Hero's own damage pipeline by the caller. */
    public interface DamageSink {
        void apply(float damage);
    }

    private EnemyVerbs() {
    }

    public static boolean activeIn(GameState state) {
        return state != null && state.waveNumber >= FIRST_VERB_WAVE;
    }

    /** The role's verb, or null when the role has none. */
    public static String verbOf(EnemyType type) {
        return switch (type) {
            case BARK_STALKER -> "SPIT";
            case FUNGAL_BRUTE -> "MEND";
            case HUSK_WARDEN -> "WARD";
            case GLOOM_WOLF -> "PACK";
            default -> null;
        };
    }

    public static void update(GameState state, float deltaSeconds, DamageSink sink) {
        if (!activeIn(state) || deltaSeconds <= 0f || sink == null) {
            return;
        }
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy == null || !enemy.alive || enemy.silentWatcher || enemy.stunned()) {
                continue;
            }
            EnemyType type = typeOf(enemy);
            if (type == null) {
                continue;
            }
            switch (type) {
                case BARK_STALKER -> spit(state, enemy, deltaSeconds, sink);
                case FUNGAL_BRUTE -> mend(state, enemy, deltaSeconds);
                case HUSK_WARDEN -> ward(state, enemy, deltaSeconds);
                case GLOOM_WOLF -> pack(state, enemy);
                default -> {
                }
            }
        }
    }

    /**
     * The spit: a wind-up the player can see, then a hit that lands only if the Hero is still on the line.
     *
     * <p>The tell is the role's own {@code verbLatched} flag, so the renderer draws the warning from the same
     * state the damage resolves against and the two can never disagree. A step of
     * {@link #SPIT_ESCAPE_UNITS} during the wind-up misses outright -- no damage and no dodge roll spent, the
     * same contract a boss's zone honours.
     */
    private static void spit(GameState state, Enemy enemy, float deltaSeconds, DamageSink sink) {
        float distance = (float) Math.sqrt(enemy.distanceSquaredTo(state.hero.x, state.hero.y));
        if (enemy.verbLatched) {
            enemy.verbTimerSeconds += deltaSeconds;
            if (enemy.verbTimerSeconds < SPIT_WINDUP_SECONDS) {
                return;
            }
            // The dodge is read before the latch clears: the tell's own state is the dodge's own state, so
            // stepping off the line during the wind-up and stepping off it on the frame of the hit are the same
            // answer to the same question.
            boolean missed = spitMissed(enemy, state.hero.x, state.hero.y);
            enemy.verbLatched = false;
            enemy.verbTimerSeconds = 0f;
            float landing = (float) Math.sqrt(enemy.distanceSquaredTo(state.hero.x, state.hero.y));
            if (!missed && state.hero.alive && landing <= SPIT_RANGE * 1.1f) {
                sink.apply(enemy.damage * SPIT_DAMAGE_SHARE);
            }
            return;
        }
        if (distance > SPIT_RANGE || distance <= enemy.attackRange) {
            return;
        }
        enemy.verbLatched = true;
        enemy.verbTimerSeconds = 0f;
        enemy.verbMarkX = state.hero.x;
        enemy.verbMarkY = state.hero.y;
    }

    /** True when the Hero has stepped clear of the line a latched spit was aimed down. */
    public static boolean spitMissed(Enemy enemy, float heroX, float heroY) {
        if (enemy == null || !enemy.verbLatched) {
            return false;
        }
        float dx = heroX - enemy.verbMarkX;
        float dy = heroY - enemy.verbMarkY;
        return Math.sqrt(dx * dx + dy * dy) > SPIT_ESCAPE_UNITS;
    }

    private static void mend(GameState state, Enemy enemy, float deltaSeconds) {
        enemy.verbTimerSeconds -= deltaSeconds;
        if (enemy.verbTimerSeconds > 0f) {
            return;
        }
        enemy.verbTimerSeconds = MEND_PERIOD_SECONDS;
        int healed = 0;
        for (Enemy ally : state.aliveEnemies) {
            if (healed >= MEND_MAX_TARGETS) {
                return;
            }
            if (ally == null || !ally.alive || ally.silentWatcher || ally.maxHealth <= 0f) {
                continue;
            }
            if (ally.health >= ally.maxHealth * MEND_WOUNDED_BELOW) {
                continue;
            }
            if (enemy.distanceSquaredTo(ally.x, ally.y) > MEND_RADIUS * MEND_RADIUS) {
                continue;
            }
            ally.health = Math.min(ally.maxHealth, ally.health + ally.maxHealth * MEND_SHARE);
            healed++;
        }
    }

    private static void ward(GameState state, Enemy enemy, float deltaSeconds) {
        enemy.verbTimerSeconds -= deltaSeconds;
        if (enemy.verbTimerSeconds > 0f) {
            return;
        }
        enemy.verbTimerSeconds = WARD_PERIOD_SECONDS;
        int warded = 0;
        for (Enemy ally : state.aliveEnemies) {
            if (warded >= WARD_MAX_ALLIES) {
                return;
            }
            if (ally == null || !ally.alive || ally.silentWatcher) {
                continue;
            }
            if (enemy.distanceSquaredTo(ally.x, ally.y) > WARD_RADIUS * WARD_RADIUS) {
                continue;
            }
            ally.affixWardRemainingSeconds = WARD_DURATION_SECONDS;
            warded++;
        }
    }

    /** Recomputes the wolf's pack bonus every frame and writes the speed the movement system already reads. */
    private static void pack(GameState state, Enemy enemy) {
        int nearby = 0;
        for (Enemy other : state.aliveEnemies) {
            if (other == null || !other.alive || other == enemy || other.silentWatcher) {
                continue;
            }
            if (!EnemyType.GLOOM_WOLF.name().equals(other.enemyType)) {
                continue;
            }
            if (enemy.distanceSquaredTo(other.x, other.y) <= PACK_RADIUS * PACK_RADIUS) {
                nearby++;
            }
        }
        float bonus = nearby == 0 ? 0f : Math.min(PACK_SPEED_CAP, PACK_SPEED_PER_WOLF * nearby);
        enemy.packSpeedMultiplier = 1f + bonus;
    }

    private static EnemyType typeOf(Enemy enemy) {
        for (EnemyType type : EnemyType.values()) {
            if (type.name().equals(enemy.enemyType)) {
                return type;
            }
        }
        return null;
    }
}
