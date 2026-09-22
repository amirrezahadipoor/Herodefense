package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.Hero;

/**
 * Drag-to-step movement for the Hero, on a budget the wave grants (roadmap A1).
 *
 * <p>Everything else in the simulation was already written against a Hero position rather than a constant:
 * {@code EnemyMovementSystem} walks every living foe toward {@code state.hero.x} and {@code state.hero.y},
 * {@code BossSpecialAttackSystem} charges at the same two numbers, {@code HeroAutoAttackSystem} and
 * {@code FocusFireSystem} measure their ranges from them, and the particles, the sprite and the equipment all
 * draw at them. So this system is the only thing that had to exist for the spatial layer to work -- and the only
 * thing that had to be <em>bounded</em>, because an unbounded Hero that outruns the field turns 200 waves into a
 * chase the balance evidence never measured.
 *
 * <p>The bound is a per-wave distance, not a speed. {@code Hero#WAVE_STEP_BUDGET} units at {@link #STEP_SPEED} is
 * a burst of about a second and a half: enough to meet a wave on the flank it came from, or to be somewhere else
 * when a melee swing is measured, and not enough to be sustained. Against the slowest creature the game fields
 * (BRAMBLE_THRALL at 28 units a second) spending the whole budget buys under ten seconds of distance, and under
 * three against the fastest (SAP_HOUND at 100). {@code HeroMovementSystemTest} computes those numbers from the
 * shipped tables rather than from this comment, and measures the melee half for real: a swing that lands at the
 * arena centre lands nowhere once the Hero has stepped out of reach.
 *
 * <p>A boss's special is the exception, and it is an exception in the code rather than in the tuning:
 * {@code BossSpecialAttackSystem.executeOnce} hands the hit straight to the damage pipeline with no position
 * test, so a telegraph warns about damage that stepping does not avoid. That is roadmap A5, and this system's
 * documentation is deliberately the place that says so, because the gesture a player learns here is the one that
 * will make them try it.
 *
 * <p>The gesture is a drag and not a tap, because a tap is already spoken for: it marks the enemy under the
 * finger for the bow. A drag during a wave previously did nothing at all beyond moving a press marker, which is
 * what makes this the cheapest verb the game could have gained. Only the first finger is honoured -- one Hero,
 * one order -- and only while the finger is inside {@code WorldLayout}'s walkable band, so a drag that starts on
 * a HUD button cannot walk the defender under it.
 *
 * <p>The balance simulator does not use this system: {@code BalanceSimulator} calls
 * {@code GameState.anchorHeroAtArenaCenter} every tick, which is a rooted Hero, which is the floor of player
 * skill. Every published band therefore still describes a player who never steps, and stepping can only be
 * better than that floor by less than ten seconds of distance a wave.
 */
public final class HeroMovementSystem {

    /** Units per second while an order is live. Faster than any enemy, for the length of the budget. */
    public static final float STEP_SPEED = 165f;

    /** Closer than this and the order is fulfilled; without it the Hero jitters on the last frame. */
    static final float ARRIVAL_EPSILON = 6f;

    private HeroMovementSystem() {
    }

    /** Whether a world point is somewhere the Hero is allowed to stand (and so somewhere a drag may aim at). */
    public static boolean isInsideWalkableArea(float worldX, float worldY) {
        return Float.isFinite(worldX) && Float.isFinite(worldY)
            && worldX >= WorldLayout.HERO_WALK_MIN_X && worldX <= WorldLayout.HERO_WALK_MAX_X
            && worldY >= WorldLayout.HERO_WALK_MIN_Y && worldY <= WorldLayout.HERO_WALK_MAX_Y;
    }

    /** The nearest legal x to {@code worldX}, for keeping the Hero inside the band after any write. */
    public static float clampX(float worldX) {
        if (!Float.isFinite(worldX)) {
            return WorldLayout.HERO_CENTER_X;
        }
        return Math.max(WorldLayout.HERO_WALK_MIN_X, Math.min(WorldLayout.HERO_WALK_MAX_X, worldX));
    }

    /** The nearest legal y to {@code worldY}. */
    public static float clampY(float worldY) {
        if (!Float.isFinite(worldY)) {
            return WorldLayout.HERO_CENTER_Y;
        }
        return Math.max(WorldLayout.HERO_WALK_MIN_Y, Math.min(WorldLayout.HERO_WALK_MAX_Y, worldY));
    }

    /**
     * Aims the Hero at a dragged point.
     *
     * @return true when an order was taken, which is what the router reports to the first-run coach; false when
     *     the Hero is dead, rooted for the rest of this wave, or the finger is outside the walkable band
     */
    public static boolean orderStepTo(GameState state, float worldX, float worldY) {
        if (state == null || state.hero == null || !state.hero.alive) {
            return false;
        }
        if (BraceSystem.isBracing(state)) {
            return false;
        }
        if (state.hero.stepBudgetUnits <= 0f || !isInsideWalkableArea(worldX, worldY)) {
            return false;
        }
        Hero hero = state.hero;
        hero.moveTargetX = clampX(worldX);
        hero.moveTargetY = clampY(worldY);
        hero.moveOrderActive = true;
        // E2: start walk animation immediately so first frame shows movement
        hero.beginWalkAnimation();
        return true;
    }

    /** Releases the finger: the Hero stops where it is instead of finishing the walk. */
    public static void cancelOrder(GameState state) {
        if (state != null && state.hero != null) {
            state.hero.moveOrderActive = false;
        }
    }

    /** True while an order is live and the budget can still pay for it -- what the meter and tests read. */
    public static boolean isStepping(GameState state) {
        return state != null && state.hero != null
            && state.hero.alive && state.hero.moveOrderActive && state.hero.stepBudgetUnits > 0f;
    }

    /** Refills the wave's stepping budget. Called from {@code WaveLifecycleSystem.startCurrentWave}. */
    public static void beginWave(GameState state) {
        if (state == null || state.hero == null) {
            return;
        }
        state.hero.stepBudgetUnits = Hero.WAVE_STEP_BUDGET;
        state.hero.moveOrderActive = false;
    }

    /** One simulation tick of stepping, and the clamp that keeps the Hero legal whatever else wrote to it. */
    public static void update(GameState state, float deltaSeconds) {
        if (state == null || state.hero == null || deltaSeconds <= 0f || !Float.isFinite(deltaSeconds)) {
            return;
        }
        Hero hero = state.hero;
        hero.x = clampX(hero.x);
        hero.y = clampY(hero.y);
        if (!hero.alive || BraceSystem.isBracing(state)) {
            hero.moveOrderActive = false;
            return;
        }
        if (!hero.moveOrderActive) {
            return;
        }
        if (hero.stepBudgetUnits <= 0f) {
            hero.moveOrderActive = false;
            return;
        }
        float dx = hero.moveTargetX - hero.x;
        float dy = hero.moveTargetY - hero.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (!(distance > ARRIVAL_EPSILON)) {
            hero.moveOrderActive = false;
            return;
        }
        float travel = Math.min(distance, Math.min(STEP_SPEED * deltaSeconds, hero.stepBudgetUnits));
        float fromX = hero.x;
        float fromY = hero.y;
        ArenaTerrain.resolveHero(
            ArenaTerrain.fieldFor(state),
            hero.x + dx / distance * travel,
            hero.y + dy / distance * travel
        );
        hero.x = clampX(ArenaTerrain.resolvedX());
        hero.y = clampY(ArenaTerrain.resolvedY());
        float movedX = hero.x - fromX;
        float movedY = hero.y - fromY;
        float moved = (float) Math.sqrt(movedX * movedX + movedY * movedY);
        // The budget pays for ground actually covered: a step into an outcrop is not a step the player bought.
        hero.stepBudgetUnits = Math.max(0f, hero.stepBudgetUnits - moved);
        // A step the field refuses ends the order there, rather than leaving the Hero leaning on a stone with a
        // live order the player cannot cancel except by spending another drag.
        if (moved < travel * 0.5f || hero.stepBudgetUnits <= 0f) {
            hero.moveOrderActive = false;
        }
    }
}
