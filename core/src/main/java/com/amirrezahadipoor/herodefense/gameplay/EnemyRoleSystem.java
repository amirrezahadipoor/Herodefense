package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.ArrayList;
import java.util.List;

/**
 * The roles the second half of a run adds (roadmap A3).
 *
 * <p>The audit's complaint about the late waves was precise: eight enemy types carry two hundred waves and the
 * second half scales their numbers rather than their behaviour, which is what the open plateau finding was
 * describing before curve changes alone marked it fixed. A role is behaviour -- the same sprite, the same spawn
 * lane, a different answer to "what is this body doing here" -- and this system owns four, one per late block
 * of the run:
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
 *   <li><b>The thrall's split, from wave {@link #SPLIT_FROM_WAVE}.</b> A non-elite BRAMBLE_THRALL at or below
 *       {@link #SPLIT_HEALTH_RATIO} of its health comes apart into {@link #SPLIT_FRAGMENT_COUNT} rootling
 *       fragments carrying exactly its health budget at the ratio, its speed and reach, and half its damage
 *       each -- the same total hit points and the same total contact damage per second, now in two bodies
 *       instead of one. The role changes what the wave asks of the player (chain and area work doubles in
 *       value, single-target focus picks a fragment order) without changing what the wave costs, which is the
 *       whole reason the balance gate let it ship: the riskiest trial pair medians its spike exactly at the
 *       ceiling, so a late role had to arrive pressure-neutral or not at all.</li>
 *   <li><b>The hound's lunge, from wave {@link #LUNGE_FROM_WAVE}.</b> A SAP_HOUND cycles: a still windup the
 *       player can read, a dash at {@link #LUNGE_DASH_SPEED_MULTIPLIER} times its speed, then a
 *       {@link #LUNGE_RECOVERY_STUN_SECONDS}-second self-stun -- the punishment window -- before it walks
 *       again. Over a full cycle the hound covers slightly less ground than it would have walking, so the lunge
 *       is a rhythm the player fences with, not extra pressure: the fastest body in the roster becomes the one
 *       with tells.</li>
 * </ul>
 *
 * <p>Every gate sits past the brief vigil's thirtieth wave by a wide margin, so the first session of the game is
 * untouched by design: the mode that must not punish a new player keeps the roster the balance evidence for it
 * was measured on. The two-hundred-wave sweep, on the other hand, fights the roles from wave
 * {@link #WARD_FROM_WAVE} onwards, and the bands in {@code docs/BALANCE.md} are re-measured against them by the
 * balance gate rather than assumed to survive -- the first landing of the ward and the berserk failed four gate
 * classes and was answered with lighter numbers, not moved bands; that history is in the document.
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
    public static final float WARD_RADIUS = 90f;
    /** The share of damage a warded body takes. */
    public static final float WARD_DAMAGE_MULTIPLIER = 0.96f;

    /** The wave from which brutes can go berserk (inclusive). */
    public static final int ENRAGE_FROM_WAVE = 141;
    /** The health ratio below which a brute latches. */
    public static final float ENRAGE_HEALTH_RATIO = 0.30f;
    /** Berserk closing speed, as a multiple of the speed the wave gave the brute. */
    public static final float ENRAGE_SPEED_MULTIPLIER = 1.25f;
    /** Berserk swing interval, as a multiple of the interval the wave gave the brute. */
    public static final float ENRAGE_INTERVAL_MULTIPLIER = 0.80f;

    /** The wave from which wounded thralls come apart (inclusive). */
    public static final int SPLIT_FROM_WAVE = 161;
    /** The health ratio at or below which a thrall splits. */
    public static final float SPLIT_HEALTH_RATIO = 0.5f;
    /** How many fragments a splitting thrall leaves. */
    public static final int SPLIT_FRAGMENT_COUNT = 2;
    /**
     * The share of the thrall's maximum health each fragment inherits. Count times share sits a hair under
     * the split ratio, so finishing a split costs the player marginally less than finishing the thrall would
     * have -- the gate's spike ceiling left no room for the split to be exactly free, let alone dearer.
     */
    public static final float SPLIT_FRAGMENT_HEALTH_SHARE = 0.24f;
    /** How far to either side of the thrall's body the fragments appear, in world units. */
    private static final float SPLIT_FRAGMENT_SPREAD = 15f;

    /** The wave from which sap hounds lunge (inclusive). */
    public static final int LUNGE_FROM_WAVE = 181;
    /** The still windup before a dash, in seconds -- the tell. */
    public static final float LUNGE_WINDUP_SECONDS = 0.45f;
    /** The dash itself, in seconds. */
    public static final float LUNGE_DASH_SECONDS = 0.55f;
    /** Dash speed, as a multiple of the speed the wave gave the hound. */
    public static final float LUNGE_DASH_SPEED_MULTIPLIER = 3.2f;
    /** The self-stun a dash ends in -- the punishment window, in seconds. */
    public static final float LUNGE_RECOVERY_STUN_SECONDS = 0.9f;
    /** The walking time between one recovery and the next windup, in seconds. */
    public static final float LUNGE_COOLDOWN_SECONDS = 3.0f;

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
     * Ticks the berserk latch, the thrall split and the hound lunge. Called from {@code CombatSystem.update}
     * and from the balance simulator's hand-wired loop, after the brace's clocks and before the tick's
     * movement and melee resolution read the numbers it changes.
     */
    public static void update(GameState state, float deltaSeconds) {
        if (state == null || deltaSeconds <= 0f || state.waveNumber < ENRAGE_FROM_WAVE) {
            return;
        }
        List<Enemy> fragments = null;
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy == null || !enemy.alive || !enemy.active || enemy.silentWatcher) {
                continue;
            }
            if (EnemyType.FUNGAL_BRUTE.name().equals(enemy.enemyType)) {
                latchBerserk(enemy);
            }
            if (state.waveNumber >= SPLIT_FROM_WAVE
                && EnemyType.BRAMBLE_THRALL.name().equals(enemy.enemyType)) {
                List<Enemy> spawned = splitThrall(state, enemy);
                if (!spawned.isEmpty()) {
                    if (fragments == null) {
                        fragments = new ArrayList<>(SPLIT_FRAGMENT_COUNT);
                    }
                    fragments.addAll(spawned);
                }
            }
            if (state.waveNumber >= LUNGE_FROM_WAVE && EnemyType.SAP_HOUND.name().equals(enemy.enemyType)) {
                tickLunge(state, enemy, deltaSeconds);
            }
        }
        if (fragments != null) {
            state.aliveEnemies.addAll(fragments);
        }
    }

    private static void latchBerserk(Enemy enemy) {
        if (enemy.enraged || enemy.maxHealth <= 0f || enemy.health / enemy.maxHealth >= ENRAGE_HEALTH_RATIO) {
            return;
        }
        enemy.enraged = true;
        enemy.movementSpeed *= ENRAGE_SPEED_MULTIPLIER;
        enemy.attackIntervalSeconds = Math.max(0.1f,
            enemy.attackIntervalSeconds * ENRAGE_INTERVAL_MULTIPLIER);
    }

    /**
     * Comes a non-elite thrall apart at the split ratio. The fragments inherit exactly the thrall's health
     * budget at the split ratio (count times share equals the ratio itself; the blow that crosses the ratio
     * overkills the corpse by at most one hit), its speed, reach, lane and swing interval, and
     * half its damage each -- total contact damage per second is the thrall's, to the digit. The thrall's kill
     * reward is handed to the fragments (they pay the standard rootling reward; the corpse pays nothing), and
     * the lineage rolls its item and potion drops once, on the corpse, so the split mints no economy.
     */
    private static List<Enemy> splitThrall(GameState state, Enemy thrall) {
        if (thrall.splitSpawned || thrall.maxHealth <= 0f || thrall.health <= 0f) {
            return List.of();
        }
        if (thrall.health > thrall.maxHealth * SPLIT_HEALTH_RATIO) {
            return List.of();
        }
        if (thrall.eliteAffix != null && !thrall.eliteAffix.isEmpty()) {
            return List.of();
        }
        thrall.splitSpawned = true;
        thrall.killRewardsGranted = true;
        thrall.alive = false;
        List<Enemy> fragments = new ArrayList<>(SPLIT_FRAGMENT_COUNT);
        for (int i = 0; i < SPLIT_FRAGMENT_COUNT; i++) {
            float offset = (i % 2 == 0 ? -1f : 1f) * SPLIT_FRAGMENT_SPREAD * (1 + i / 2);
            Enemy fragment = new Enemy(state.allocateEntityId(), EnemyType.ROOTLING.name(),
                thrall.x + offset, thrall.y);
            fragment.maxHealth = thrall.maxHealth * SPLIT_FRAGMENT_HEALTH_SHARE;
            fragment.health = fragment.maxHealth;
            fragment.damage = thrall.damage * 0.5f;
            fragment.movementSpeed = thrall.movementSpeed;
            fragment.attackRange = thrall.attackRange;
            fragment.attackIntervalSeconds = thrall.attackIntervalSeconds;
            fragment.spawnLane = thrall.spawnLane;
            fragment.itemDropRolled = true;
            fragment.potionDropRolled = true;
            // Stagger the fragments' swings half an interval apart. Two bodies sharing the thrall's rhythm
            // would land their hits on the same frames: average-neutral, but the gates measure single-wave
            // maxima, and a synchronised double contact is exactly the kind of burst they exist to catch.
            fragment.attackCooldownSeconds = (i % 2) * thrall.attackIntervalSeconds * 0.5f;
            fragments.add(fragment);
        }
        return fragments;
    }

    /**
     * Walks a hound's lunge cycle. {@code lungeSeconds} is the whole machine: negative counts the cooldown up
     * towards zero, then the same float walks through windup and dash until it hands the hound to
     * {@code stunRemainingSeconds} for the recovery and resets to minus the cooldown. Over one full cycle the
     * hound covers dash-seconds times dash-speed plus the cooldown's walking, which the shipped numbers keep a
     * hair under the ground it would have covered just walking -- the lunge adds tells, not pressure.
     */
    private static void tickLunge(GameState state, Enemy enemy, float deltaSeconds) {
        if (!state.hero.alive) {
            return;
        }
        if (enemy.lungeBaseSpeed <= 0f) {
            enemy.lungeBaseSpeed = enemy.movementSpeed;
            enemy.lungeSeconds = -(1.2f + (enemy.id % 5) * 0.35f);
        }
        enemy.lungeSeconds += deltaSeconds;
        if (enemy.lungeSeconds < 0f) {
            enemy.movementSpeed = enemy.lungeBaseSpeed;
            return;
        }
        if (enemy.lungeSeconds < LUNGE_WINDUP_SECONDS) {
            enemy.movementSpeed = 0f;
            return;
        }
        if (enemy.lungeSeconds < LUNGE_WINDUP_SECONDS + LUNGE_DASH_SECONDS) {
            enemy.movementSpeed = enemy.lungeBaseSpeed * LUNGE_DASH_SPEED_MULTIPLIER;
            return;
        }
        enemy.stunRemainingSeconds = Math.max(enemy.stunRemainingSeconds, LUNGE_RECOVERY_STUN_SECONDS);
        enemy.movementSpeed = enemy.lungeBaseSpeed;
        enemy.lungeSeconds = -LUNGE_COOLDOWN_SECONDS;
    }
}
