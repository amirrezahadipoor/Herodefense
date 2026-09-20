package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.EliteAffix;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.RotTrailSegment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Runs Elite affix combat: blightburst death blasts and hollowmolt death splits, plus the live
 * rootward/weeping/gravemoss/cinderhalo clocks.
 */
public final class EliteAffixSystem {
    public static final float BLIGHT_BLAST_RADIUS = 150f;
    public static final float BLIGHT_BLAST_DAMAGE_MULT = 1f;
    public static final float ROOTWARD_SHIELD_PERIOD = 6f;
    public static final float ROOTWARD_SHIELD_DURATION = 2f;
    public static final float ROOTWARD_FIRST_SHIELD_DELAY = 2f;
    public static final float WEEPING_TRAIL_INTERVAL = 0.5f;
    public static final float WEEPING_SEGMENT_LIFETIME = 3f;
    public static final float WEEPING_SEGMENT_RADIUS = 55f;
    public static final float WEEPING_DAMAGE_SHARE = 0.35f;
    public static final int MOLT_CHILD_COUNT = 2;
    public static final float MOLT_CHILD_HEALTH_SHARE = 0.15f;
    public static final float MOLT_CHILD_DAMAGE_SHARE = 0.45f;
    public static final float MOLT_CHILD_OFFSET = 16f;
    public static final float GRAVEMOSS_REGEN_PER_SECOND = 0.006f;
    public static final float CINDERHALO_RADIUS = 90f;
    public static final float CINDERHALO_TICK_SECONDS = 0.5f;
    public static final float CINDERHALO_DAMAGE_SHARE = 0.08f;
    /** A blightburst at or below this health share counts as dying and warns its blast ring. */
    public static final float BLIGHT_WARN_HEALTH_FRACTION = 0.35f;
    /** The rootward share (roadmap A3): radius, shared ward duration, and how many allies hold one. */
    public static final float ROOTWARD_SHARE_RADIUS = 90f;
    public static final float ROOTWARD_SHARE_DURATION = 3f;
    public static final int ROOTWARD_SHARE_ALLIES = 2;
    /** Out of a living elite's reach the shared ward melts this many times faster: the grip dies with its root. */
    public static final float ROOTWARD_ORPHAN_DECAY = 10f;

    private final HeroDamageSystem heroDamageSystem;

    public EliteAffixSystem(HeroDamageSystem heroDamageSystem) {
        this.heroDamageSystem = heroDamageSystem;
    }

    public void update(GameState state, float deltaSeconds) {
        if (state == null || state.hero == null || state.aliveEnemies == null || deltaSeconds < 0f) {
            return;
        }
        // Hollowmolt children are collected, not added in-loop: state.aliveEnemies is being iterated.
        // Shared wards decay in their own pass: a ward granted later in the tick must keep its
        // full duration, not pay for the frames of the bodies ahead of it in the list. The share
        // is the elite's grip -- a warded body carried out of any living elite's reach loses it
        // tenfold fast, so no warded straggler softens the wave that follows.
        float radiusSquared = ROOTWARD_SHARE_RADIUS * ROOTWARD_SHARE_RADIUS;
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy == null || enemy.affixWardRemainingSeconds <= 0f) {
                continue;
            }
            boolean inEliteReach = false;
            for (Enemy other : state.aliveEnemies) {
                if (other == null || other.eliteAffix == null || !other.alive) {
                    continue;
                }
                if (enemy.distanceSquaredTo(other.x, other.y) <= radiusSquared) {
                    inEliteReach = true;
                    break;
                }
            }
            float decay = inEliteReach ? 1f : ROOTWARD_ORPHAN_DECAY;
            enemy.affixWardRemainingSeconds =
                Math.max(0f, enemy.affixWardRemainingSeconds - deltaSeconds * decay);
        }
        List<Enemy> spawned = null;
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy == null || enemy.eliteAffix == null) continue;
            if (!enemy.alive) {
                List<Enemy> children = resolveDeath(state, enemy);
                if (!children.isEmpty()) {
                    if (spawned == null) spawned = new ArrayList<>();
                    spawned.addAll(children);
                }
                continue;
            }
            // A dying blightburst owes the player a warning before its death blast (roadmap A2).
            if (!enemy.blastWarned && enemy.maxHealth > 0f
                && EliteAffix.BLIGHTBURST.id().equals(enemy.eliteAffix)
                && enemy.health <= enemy.maxHealth * BLIGHT_WARN_HEALTH_FRACTION) {
                enemy.blastWarned = true;
            }
            if (enemy.stunned()) continue;
            if (EliteAffix.ROOTWARD_WARD.id().equals(enemy.eliteAffix)) {
                updateRootward(state, enemy, deltaSeconds);
            } else if (EliteAffix.WEEPING_ROT.id().equals(enemy.eliteAffix)) {
                updateWeeping(state, enemy, deltaSeconds);
            } else if (EliteAffix.GRAVEMOSS.id().equals(enemy.eliteAffix)) {
                updateGravemoss(enemy, deltaSeconds);
            } else if (EliteAffix.CINDERHALO.id().equals(enemy.eliteAffix)) {
                updateCinderhalo(state, enemy, deltaSeconds);
            }
        }
        if (spawned != null) {
            state.aliveEnemies.addAll(spawned);
        }
        updateTrail(state, deltaSeconds);
    }

    /** Resolves a dead Elite's affix once; returns hollowmolt's children, empty when it leaves none. */
    private List<Enemy> resolveDeath(GameState state, Enemy enemy) {
        if (enemy.affixResolved) return List.of();
        enemy.affixResolved = true;
        if (EliteAffix.BLIGHTBURST.id().equals(enemy.eliteAffix)
            && state.hero.alive
            && enemy.distanceSquaredTo(state.hero.x, state.hero.y)
                <= BLIGHT_BLAST_RADIUS * BLIGHT_BLAST_RADIUS) {
            heroDamageSystem.applyIncomingHit(
                state, enemy.damage * BLIGHT_BLAST_DAMAGE_MULT);
        }
        if (EliteAffix.HOLLOWMOLT.id().equals(enemy.eliteAffix)) {
            List<Enemy> children = new ArrayList<>(MOLT_CHILD_COUNT);
            for (int index = 0; index < MOLT_CHILD_COUNT; index++) {
                float sign = index % 2 == 0 ? -1f : 1f;
                Enemy child = new Enemy(state.allocateEntityId(), enemy.enemyType,
                    enemy.x + sign * MOLT_CHILD_OFFSET, enemy.y + sign * MOLT_CHILD_OFFSET * 0.5f);
                child.maxHealth = enemy.maxHealth * MOLT_CHILD_HEALTH_SHARE;
                child.health = child.maxHealth;
                child.damage = enemy.damage * MOLT_CHILD_DAMAGE_SHARE;
                child.movementSpeed = enemy.movementSpeed;
                child.attackRange = enemy.attackRange;
                children.add(child);
            }
            return children;
        }
        return List.of();
    }

    /** Gravemoss regrows a sliver of max health per second; stun is the window that stops it. */
    private static void updateGravemoss(Enemy enemy, float deltaSeconds) {
        if (enemy.health >= enemy.maxHealth) return;
        enemy.health = Math.min(enemy.maxHealth,
            enemy.health + enemy.maxHealth * GRAVEMOSS_REGEN_PER_SECOND * deltaSeconds);
    }

    /** Cinderhalo burns the hero on a fixed rhythm while they stand inside the halo; stun pauses the clock. */
    private void updateCinderhalo(GameState state, Enemy enemy, float deltaSeconds) {
        enemy.affixTimerSeconds += deltaSeconds;
        if (enemy.affixTimerSeconds < CINDERHALO_TICK_SECONDS) return;
        enemy.affixTimerSeconds -= CINDERHALO_TICK_SECONDS;
        if (state.hero != null && state.hero.alive
            && enemy.distanceSquaredTo(state.hero.x, state.hero.y)
                <= CINDERHALO_RADIUS * CINDERHALO_RADIUS) {
            heroDamageSystem.applyIncomingHit(state, enemy.damage * CINDERHALO_DAMAGE_SHARE);
        }
    }

    private static void updateRootward(GameState state, Enemy enemy, float deltaSeconds) {
        if (enemy.affixShieldRemainingSeconds > 0f) {
            enemy.affixShieldRemainingSeconds =
                Math.max(0f, enemy.affixShieldRemainingSeconds - deltaSeconds);
            return;
        }
        enemy.affixTimerSeconds += deltaSeconds;
        if (enemy.affixTimerSeconds >= ROOTWARD_SHIELD_PERIOD) {
            enemy.affixTimerSeconds = 0f;
            enemy.affixShieldRemainingSeconds = ROOTWARD_SHIELD_DURATION;
            shareShield(state, enemy);
        }
    }

    /**
     * The rootward share (roadmap A3): when the elite's own shield raises, the two nearest
     * regulars in reach catch a shorter copy of it as a shared ward -- damage lands at a reduced
     * share while it holds, never at zero, so it bends a volley without ever stalling a fight.
     * The ward always decays, so no body is ever permanently harder to kill.
     */
    private static void shareShield(GameState state, Enemy elite) {
        if (state.aliveEnemies == null) {
            return;
        }
        float radiusSquared = ROOTWARD_SHARE_RADIUS * ROOTWARD_SHARE_RADIUS;
        int shared = 0;
        for (Enemy ally : state.aliveEnemies) {
            if (shared >= ROOTWARD_SHARE_ALLIES) {
                return;
            }
            if (ally == null || ally == elite || !ally.alive || !ally.active
                || ally.silentWatcher || ally.eliteAffix != null) {
                continue;
            }
            if (ally.distanceSquaredTo(elite.x, elite.y) <= radiusSquared) {
                ally.affixWardRemainingSeconds = ROOTWARD_SHARE_DURATION;
                shared++;
            }
        }
    }

    private static void updateWeeping(GameState state, Enemy enemy, float deltaSeconds) {
        enemy.affixTimerSeconds += deltaSeconds;
        if (enemy.affixTimerSeconds < WEEPING_TRAIL_INTERVAL || state.rotTrail == null) return;
        enemy.affixTimerSeconds -= WEEPING_TRAIL_INTERVAL;
        state.rotTrail.add(new RotTrailSegment(state.allocateEntityId(), enemy.x, enemy.y,
            WEEPING_SEGMENT_LIFETIME, enemy.damage * WEEPING_DAMAGE_SHARE, enemy.id));
    }

    /**
     * Ticks rot patches out and bills standing heroes once per frame: each elite's
     * rot wounds once no matter how its patches overlap; two elites wound twice.
     */
    private void updateTrail(GameState state, float deltaSeconds) {
        if (state.rotTrail == null) return;
        Iterator<RotTrailSegment> segments = state.rotTrail.iterator();
        while (segments.hasNext()) {
            RotTrailSegment segment = segments.next();
            if (segment == null) {
                segments.remove();
                continue;
            }
            segment.remainingSeconds -= deltaSeconds;
            if (segment.remainingSeconds <= 0f) segments.remove();
        }
        float totalDps = 0f;
        for (int index = 0; index < state.rotTrail.size(); index++) {
            RotTrailSegment segment = state.rotTrail.get(index);
            if (coversHero(state, segment) && strongestOfSource(state, index)) {
                totalDps += segment.damagePerSecond;
            }
        }
        if (totalDps > 0f && state.hero.alive) {
            heroDamageSystem.applyEnvironmentalHit(state, totalDps * deltaSeconds);
        }
    }

    private boolean coversHero(GameState state, RotTrailSegment segment) {
        return state.hero.alive && segment != null
            && segment.distanceSquaredTo(state.hero.x, state.hero.y)
                <= WEEPING_SEGMENT_RADIUS * WEEPING_SEGMENT_RADIUS;
    }

    /** Exactly one covering patch bills per source: strongest wins, ties go low-index. */
    private boolean strongestOfSource(GameState state, int index) {
        RotTrailSegment segment = state.rotTrail.get(index);
        for (int other = 0; other < state.rotTrail.size(); other++) {
            if (other == index) continue;
            RotTrailSegment rival = state.rotTrail.get(other);
            if (rival == null || rival.sourceId != segment.sourceId || !coversHero(state, rival)) {
                continue;
            }
            if (rival.damagePerSecond > segment.damagePerSecond
                || (rival.damagePerSecond == segment.damagePerSecond && other < index)) {
                return false;
            }
        }
        return true;
    }
}
