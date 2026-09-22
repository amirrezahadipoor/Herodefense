package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.ArenaObstacle;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;

import com.amirrezahadipoor.herodefense.WorldLayout;

import java.util.List;

/**
 * Moves every living melee enemy directly toward the fixed Hero until attack range. Once the
 * Hero has fallen the survivors march on the World Tree instead (the second tree, once planted,
 * draws whichever attackers are closer to it) so the defeat reads as the sanctuary being overrun.
 */
public final class EnemyMovementSystem {
    /** Enemies stop this far from a tree trunk while the siege plays out. */
    static final float TREE_SIEGE_STOP_DISTANCE = 96f;

    public void update(GameState state, float deltaSeconds) {
        if (state == null || state.hero == null || deltaSeconds <= 0f) {
            return;
        }
        boolean siege = !state.hero.alive;
        for (Enemy enemy : state.aliveEnemies) {
            decayMark(enemy, deltaSeconds);
            moveToward(state, enemy, siege, deltaSeconds);
        }
        for (Boss boss : state.aliveBosses) {
            decayMark(boss, deltaSeconds);
            moveToward(state, boss, siege, deltaSeconds);
        }
    }

    /** Crown of the Hollow Eye marks fade whether or not the foe can move. */
    private static void decayMark(Enemy enemy, float deltaSeconds) {
        if (enemy != null && enemy.markRemainingSeconds > 0f) {
            enemy.markRemainingSeconds = Math.max(0f, enemy.markRemainingSeconds - deltaSeconds);
        }
    }

    private static void moveToward(GameState state, Enemy enemy, boolean siege, float deltaSeconds) {
        if (!siege) {
            moveTowardHero(state, enemy, state.hero.x, state.hero.y, deltaSeconds);
            return;
        }
        if (enemy == null || !enemy.alive || !enemy.active || enemy.silentWatcher) {
            return;
        }
        float targetX = WorldLayout.WORLD_TREE_X;
        float targetY = WorldLayout.WORLD_TREE_Y;
        float bestDist2 = enemy.distanceSquaredTo(targetX, targetY);
        int groveCount = state.plantedTreesCount;
        // Backwards compat: old saves/tests set secondTreePlanted without count -> treat as SECOND_TREE (100)
        if (groveCount == 0 && state.secondTreePlanted) {
            float d2 = enemy.distanceSquaredTo(WorldLayout.SECOND_TREE_X, WorldLayout.SECOND_TREE_Y);
            if (d2 < bestDist2) {
                // The legacy branch switches the target once; nothing reads bestDist2 again on this path.
                targetX = WorldLayout.SECOND_TREE_X;
                targetY = WorldLayout.SECOND_TREE_Y;
            }
        } else {
            for (int i = 0; i < groveCount; i++) {
                float gx = WorldLayout.groveTreeX(i);
                float gy = WorldLayout.groveTreeY(i);
                float d2 = enemy.distanceSquaredTo(gx, gy);
                if (d2 < bestDist2) {
                    bestDist2 = d2;
                    targetX = gx;
                    targetY = gy;
                }
            }
        }
        // The dead Hero no longer stuns anything; the charge is uninterrupted.
        enemy.stunRemainingSeconds = 0f;
        float dx = targetX - enemy.x;
        float dy = targetY - enemy.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float remaining = distance - TREE_SIEGE_STOP_DISTANCE;
        if (distance == 0f || remaining <= 0f) {
            return;
        }
        float travel = Math.min(remaining, Math.max(0f, enemy.movementSpeed) * 1.35f * deltaSeconds);
        if (ArenaTerrain.resolveEnemy(
            ArenaTerrain.fieldFor(state), enemy.x, enemy.y, targetX, targetY, travel)) {
            enemy.x = ArenaTerrain.resolvedX();
            enemy.y = ArenaTerrain.resolvedY();
        }
    }

    private static void moveTowardHero(GameState state, Enemy enemy, float heroX, float heroY, float deltaSeconds) {
        if (enemy == null || !enemy.alive || !enemy.active || enemy.silentWatcher) {
            return;
        }
        if (enemy.stunRemainingSeconds > 0f) {
            enemy.stunRemainingSeconds = Math.max(0f, enemy.stunRemainingSeconds - deltaSeconds);
            return;
        }
        float dx = heroX - enemy.x;
        float dy = heroY - enemy.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        List<ArenaObstacle> field = ArenaTerrain.fieldFor(state);
        // A foe that cannot see the Hero does not get to stand and fight it. It walks on until the outcrop is out
        // of the line, which is what keeps cover from becoming a standstill: a creature hiding behind a stone can
        // neither strike nor be struck, so a wave with one left in its shadow would never end.
        float stop = ArenaTerrain.hasLineOfFire(field, enemy.x, enemy.y, heroX, heroY)
            ? Math.max(0f, enemy.attackRange)
            : 0f;
        float remaining = distance - stop;
        if (distance == 0f || remaining <= 0f) {
            return;
        }
        float travel = Math.min(
            remaining,
            Math.max(0f, enemy.movementSpeed) * Math.max(0.2f, enemy.packSpeedMultiplier) * deltaSeconds
        );
        if (ArenaTerrain.resolveEnemy(field, enemy.x, enemy.y, heroX, heroY, travel)) {
            enemy.x = ArenaTerrain.resolvedX();
            enemy.y = ArenaTerrain.resolvedY();
        }
    }
}
