package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Hero;
import com.amirrezahadipoor.herodefense.model.HeroAnimationState;

/** Advances real combat-selected clips using the rendered 12 FPS frame contract — E2 adds WALK and smoothing. */
public final class HeroAnimationController {
    public static final float RENDERED_FRAMES_PER_SECOND = 12f;
    public static final float WALK_FRAMES_PER_SECOND = 14f;
    public static final int IDLE_FRAMES = 6;
    public static final int WALK_FRAMES = 6;
    public static final int ATTACK_FRAMES = 8;
    public static final int HIT_FRAMES = 4;
    public static final int DEATH_FRAMES = 10;

    /** E2: smoothstep interpolation factor for frame blending (0..1). */
    public static float interpolationFactor(Hero hero) {
        if (hero == null) return 0f;
        float fps = fpsFor(hero.animationState);
        float frameDuration = 1f / fps;
        float intoFrame = hero.animationStateSeconds % frameDuration;
        float t = Math.max(0f, Math.min(1f, intoFrame / frameDuration));
        return t * t * (3f - 2f * t);
    }

    private static float fpsFor(HeroAnimationState state) {
        return state == HeroAnimationState.WALK ? WALK_FRAMES_PER_SECOND : RENDERED_FRAMES_PER_SECOND;
    }

    public void update(Hero hero, float deltaSeconds) {
        if (hero == null || deltaSeconds < 0f) {
            return;
        }
        if (!hero.alive && hero.animationState != HeroAnimationState.DEATH) {
            hero.beginDeathAnimation();
        }
        hero.animationStateSeconds += deltaSeconds;
        switch (hero.animationState) {
            case ATTACK -> {
                if (hero.animationStateSeconds >= attackDuration(hero)) {
                    // E2: return to walk if still stepping, else idle
                    if (hero.moveOrderActive && hero.stepBudgetUnits > 0f && hero.alive) {
                        hero.beginWalkAnimation();
                    } else {
                        hero.beginIdleAnimation();
                    }
                }
            }
            case HIT -> {
                if (hero.animationStateSeconds >= clipDuration(HIT_FRAMES, RENDERED_FRAMES_PER_SECOND)) {
                    if (hero.moveOrderActive && hero.stepBudgetUnits > 0f && hero.alive) {
                        hero.beginWalkAnimation();
                    } else {
                        hero.beginIdleAnimation();
                    }
                }
            }
            case DEATH -> hero.animationStateSeconds = Math.min(
                hero.animationStateSeconds, clipDuration(DEATH_FRAMES, RENDERED_FRAMES_PER_SECOND)
            );
            case WALK -> {
                // Stay in walk while order active, else idle
                if (!hero.moveOrderActive || hero.stepBudgetUnits <= 0f || !hero.alive) {
                    hero.beginIdleAnimation();
                }
                // Walk loops
            }
            case IDLE -> {
                // E2: if stepping, transition to walk
                if (hero.moveOrderActive && hero.stepBudgetUnits > 0f && hero.alive) {
                    hero.beginWalkAnimation();
                }
            }
        }
    }

    public int frameIndex(Hero hero) {
        return switch (hero.animationState) {
            case IDLE -> loopingFrame(hero.animationStateSeconds, IDLE_FRAMES, RENDERED_FRAMES_PER_SECOND);
            case WALK -> loopingFrame(hero.animationStateSeconds, WALK_FRAMES, WALK_FRAMES_PER_SECOND);
            case ATTACK -> oneShotFrame(hero.animationStateSeconds, ATTACK_FRAMES, attackDuration(hero));
            case HIT -> oneShotFrame(hero.animationStateSeconds, HIT_FRAMES, clipDuration(HIT_FRAMES, RENDERED_FRAMES_PER_SECOND));
            case DEATH -> oneShotFrame(hero.animationStateSeconds, DEATH_FRAMES, clipDuration(DEATH_FRAMES, RENDERED_FRAMES_PER_SECOND));
        };
    }

    private static float attackDuration(Hero hero) {
        return clipDuration(ATTACK_FRAMES, RENDERED_FRAMES_PER_SECOND) / hero.stats.attacksPerSecond();
    }

    private static float clipDuration(int frameCount, float fps) {
        return frameCount / fps;
    }

    private static int loopingFrame(float elapsed, int count, float framesPerSecond) {
        return Math.max(0, (int) (elapsed * framesPerSecond)) % count;
    }

    private static int oneShotFrame(float elapsed, int count, float duration) {
        float progress = duration <= 0f ? 1f : elapsed / duration;
        return Math.min(count - 1, Math.max(0, (int) (progress * count)));
    }
}
