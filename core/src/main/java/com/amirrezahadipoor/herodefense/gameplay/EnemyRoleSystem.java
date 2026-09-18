package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * The roles the second half of a run adds (roadmap A3).
 *
 * <p>The audit's complaint about the late waves was precise: eight enemy types carry two hundred waves and the
 * second half scales their numbers rather than their behaviour, which is what the open plateau finding was
 * describing before curve changes alone marked it fixed. A role is behaviour -- the same sprite, the same spawn
 * lane, a different answer to "what is this body doing here" -- and this system owns the first two:
 *
 * <ul>
 *   <li><b>The warden's ward, from wave {@link #WARD_FROM_WAVE}.</b> Every living enemy within
 *       {@link #WARD_RADIUS} of a living HUSK_WARDEN takes {@link #WARD_DAMAGE_MULTIPLIER} of the damage it would
 *       have taken, the warden itself included. The shielded mid-weight becomes a body the wave is organised
 *       around, and the counter is a verb the game already teaches: mark the warden, because the mark is what
 *       tells the bow which of eight bodies matters. Kill the warden and the ward dies with it, in the same
 *       tick.</li>
 *   <li><b>The brute's berserk, from wave {@link #ENRAGE_FROM_WAVE}.</b> A FUNGAL_BRUTE below
 *       {@link #ENRAGE_HEALTH_RATIO} of its health latches into a berserk: faster closing, faster swings, until
 *       it dies. The heaviest regular body in the roster stops being a damage sponge and becomes a clock, and
 *       the brace from roadmap A2 is one of the answers -- three seconds of shield is exactly how long a berserk
 *       brute takes to regret reaching the Hero.</li>
 * </ul>
 *
 * <p>Both gates sit past the brief vigil's thirtieth wave by a wide margin, so the first session of the game is
 * untouched by design: the mode that must not punish a new player keeps the roster the balance evidence for it
 * was measured on. The two-hundred-wave sweep, on the other hand, now fights roles from its middle onwards, and
 * the bands in {@code docs/BALANCE.md} are re-measured against them by the balance gate rather than assumed to
 * survive -- that is the point of the item.
 *
 * <p>The ward multiplies damage on its way into {@code Enemy.receiveDamage}, at the three call sites that deal
 * damage to a foe (arrow impact, chain arc, ultimate), through {@link #damageTo} -- one function, so a fourth
 * call site that forgets the ward is a scan failure in {@code EnemyRoleSystemTest} rather than a shield that
 * works only against arrows.
 */
public final class EnemyRoleSystem {

    /** The wave from which wardens project their ward (inclusive). */
    public static final int WARD_FROM_WAVE = 121;
    /** How close a body has to be to a living warden to be warded, in world units. */
    public static final float WARD_RADIUS = 100f;
    /** The share of damage a warded body takes. */
    public static final float WARD_DAMAGE_MULTIPLIER = 0.94f;

    /** The wave from which brutes can go berserk (inclusive). */
    public static final int ENRAGE_FROM_WAVE = 141;
    /** The health ratio below which a brute latches. */
    public static final float ENRAGE_HEALTH_RATIO = 0.30f;
    /** Berserk closing speed, as a multiple of the speed the wave gave the brute. */
    public static final float ENRAGE_SPEED_MULTIPLIER = 1.25f;
    /** Berserk swing interval, as a multiple of the interval the wave gave the brute. */
    public static final float ENRAGE_INTERVAL_MULTIPLIER = 0.78f;

    private EnemyRoleSystem() {
    }

    /** Whether a living warden is close enough to {@code foe} to ward it, this tick. */
    public static boolean isWarded(GameState state, Enemy foe) {
        if (state == null || foe == null || !foe.alive || state.waveNumber < WARD_FROM_WAVE) {
            return false;
        }
        float radiusSquared = WARD_RADIUS * WARD_RADIUS;
        for (Enemy warden : state.aliveEnemies) {
            if (warden == null || !warden.alive || !warden.active || warden.silentWatcher) {
                continue;
            }
            if (!EnemyType.HUSK_WARDEN.name().equals(warden.enemyType)) {
                continue;
            }
            if (warden.distanceSquaredTo(foe.x, foe.y) <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    /** The damage a foe actually takes, ward and all. Every call site that hurts an enemy goes through here. */
    public static float damageTo(GameState state, Enemy foe, float amount) {
        if (!isWarded(state, foe)) {
            return amount;
        }
        return amount * WARD_DAMAGE_MULTIPLIER;
    }

    /**
     * Latches berserk brutes. Called from {@code CombatSystem.update}, after the brace's clocks and before the
     * tick's movement and melee resolution read the numbers it changes.
     */
    public static void update(GameState state, float deltaSeconds) {
        if (state == null || deltaSeconds <= 0f || state.waveNumber < ENRAGE_FROM_WAVE) {
            return;
        }
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy == null || !enemy.alive || enemy.enraged || enemy.silentWatcher) {
                continue;
            }
            if (!EnemyType.FUNGAL_BRUTE.name().equals(enemy.enemyType)) {
                continue;
            }
            if (enemy.maxHealth <= 0f || enemy.health / enemy.maxHealth >= ENRAGE_HEALTH_RATIO) {
                continue;
            }
            enemy.enraged = true;
            enemy.movementSpeed *= ENRAGE_SPEED_MULTIPLIER;
            enemy.attackIntervalSeconds = Math.max(0.1f,
                enemy.attackIntervalSeconds * ENRAGE_INTERVAL_MULTIPLIER);
        }
    }
}
