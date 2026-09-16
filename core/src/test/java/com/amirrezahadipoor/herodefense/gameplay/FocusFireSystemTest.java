package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/**
 * The tap-to-focus contract of roadmap R3.1: one deliberate decision per wave, deterministic enough to test.
 */
final class FocusFireSystemTest {

    @Test
    void tappingAnEnemyMarksItAndTheBowSwitchesToIt() {
        GameState state = GameState.newRun(70L);
        Enemy near = enemy(state, 90f, 0f);
        Enemy far = enemy(state, 300f, 0f);
        state.aliveEnemies.add(near);
        state.aliveEnemies.add(far);

        Enemy marked = FocusFireSystem.markAt(state, far.x, far.y);

        assertEquals(far.id, marked.id, "the tapped enemy is the one that gets marked");
        assertFalse(FocusFireSystem.isMarked(near), "only one enemy carries the mark");
        assertEquals(far.id, FocusFireSystem.markedTargetInRange(state, state.hero.x, state.hero.y,
            HeroAutoAttackSystem.ATTACK_RANGE).id);

        new HeroAutoAttackSystem().update(state, 0f);
        assertEquals(far.id, state.hero.currentTargetId,
            "the bow ignores the nearer target while the player's mark stands");
    }

    @Test
    void tappingEmptyGroundReleasesTheMark() {
        GameState state = GameState.newRun(71L);
        Enemy foe = enemy(state, 120f, 0f);
        state.aliveEnemies.add(foe);
        FocusFireSystem.markAt(state, foe.x, foe.y);
        assertTrue(FocusFireSystem.isMarked(foe));

        Enemy marked = FocusFireSystem.markAt(state, foe.x + FocusFireSystem.TAP_RADIUS * 3f, foe.y);

        assertNull(marked, "a tap that hits nothing marks nothing");
        assertFalse(FocusFireSystem.isMarked(foe), "and it releases the previous mark");
        assertFalse(FocusFireSystem.hasMarkedTarget(state));
    }

    @Test
    void theMarkExpiresAfterItsWindowAndThenTheNearestTargetWinsAgain() {
        GameState state = GameState.newRun(72L);
        Enemy near = enemy(state, 90f, 0f);
        Enemy far = enemy(state, 300f, 0f);
        state.aliveEnemies.add(near);
        state.aliveEnemies.add(far);
        FocusFireSystem.markAt(state, far.x, far.y);

        FocusFireSystem.tick(state, FocusFireSystem.MARK_SECONDS - 0.1f);
        assertTrue(FocusFireSystem.isMarked(far), "the mark survives inside its window");

        FocusFireSystem.tick(state, 0.2f);
        assertFalse(FocusFireSystem.isMarked(far), "and it runs out on time");

        new HeroAutoAttackSystem().update(state, 0f);
        assertEquals(near.id, state.hero.currentTargetId, "without a mark the bow is back to the nearest foe");
    }

    @Test
    void deadSilentOrUntappableTargetsAreNeverMarked() {
        GameState state = GameState.newRun(73L);
        Enemy dead = enemy(state, 100f, 0f);
        dead.alive = false;
        Enemy watcher = enemy(state, 110f, 0f);
        watcher.silentWatcher = true;
        state.aliveEnemies.add(dead);
        state.aliveEnemies.add(watcher);

        assertNull(FocusFireSystem.markAt(state, dead.x, dead.y));
        assertNull(FocusFireSystem.markAt(state, watcher.x, watcher.y));
        assertFalse(FocusFireSystem.hasMarkedTarget(state));
    }

    @Test
    void aBossCanBeMarkedAndStopsCountingAsSoonAsItDies() {
        GameState state = GameState.newRun(74L);
        Boss boss = new Boss(state.allocateEntityId(), "ANCIENT_GOLEM", state.hero.x + 200f, state.hero.y, 1);
        boss.health = 900f;
        boss.maxHealth = 900f;
        state.aliveBosses.add(boss);

        assertEquals(boss.id, FocusFireSystem.markAt(state, boss.x, boss.y).id);
        assertEquals(boss.id, FocusFireSystem.markedTargetInRange(state, state.hero.x, state.hero.y, 420f).id);

        boss.alive = false;
        assertFalse(FocusFireSystem.isMarked(boss), "a defeated boss stops being a focus target");
        assertNull(FocusFireSystem.markedTargetInRange(state, state.hero.x, state.hero.y, 420f));
    }

    @Test
    void theClosestOfSeveralTappedCandidatesWinsAndTiesGoToTheLowerId() {
        GameState state = GameState.newRun(75L);
        Enemy first = enemy(state, 100f, 0f);
        Enemy closer = enemy(state, 100f, 20f);
        state.aliveEnemies.add(first);
        state.aliveEnemies.add(closer);

        assertEquals(closer.id, FocusFireSystem.markAt(state, state.hero.x + 100f, state.hero.y + 12f).id,
            "the nearest candidate under the finger is the one marked");

        Enemy twin = enemy(state, 100f, 0f);
        state.aliveEnemies.add(twin);
        Enemy tied = FocusFireSystem.markAt(state, first.x, first.y);
        assertEquals(Math.min(first.id, twin.id), tied.id, "a tie resolves on the lower id, never on order");
    }

    @Test
    void degenerateInputIsIgnoredInsteadOfThrowing() {
        assertNull(FocusFireSystem.markAt(null, 0f, 0f));
        assertNull(FocusFireSystem.markAt(GameState.newRun(76L), Float.NaN, 0f));
        FocusFireSystem.tick(null, 1f);
        GameState state = GameState.newRun(77L);
        FocusFireSystem.tick(state, -1f);
        FocusFireSystem.clearAll(null);
        assertFalse(FocusFireSystem.hasMarkedTarget(null));
        assertFalse(FocusFireSystem.isMarked(null));
        assertNull(FocusFireSystem.markedTargetInRange(state, 0f, 0f, 0f));
    }

    @Test
    void theTapRadiusIsAHardLimitAndDistanceIsMeasuredToThePoint() {
        GameState state = GameState.newRun(78L);
        Enemy justInside = enemy(state, FocusFireSystem.TAP_RADIUS - 1f, 0f);
        state.aliveEnemies.add(justInside);
        assertEquals(justInside.id, FocusFireSystem.markAt(state, justInside.x, justInside.y).id);

        GameState other = GameState.newRun(79L);
        Enemy justOutside = enemy(other, FocusFireSystem.TAP_RADIUS + 1f, 0f);
        other.aliveEnemies.add(justOutside);
        assertNull(FocusFireSystem.markAt(other, other.hero.x, other.hero.y),
            "a tap at the hero's feet does not reach an enemy outside the radius");
    }

    private static Enemy enemy(GameState state, float offsetX, float offsetY) {
        Enemy enemy = new Enemy(
            state.allocateEntityId(),
            "ROOTLING",
            state.hero.x + offsetX,
            state.hero.y + offsetY
        );
        enemy.health = 100f;
        enemy.maxHealth = 100f;
        return enemy;
    }
}
