package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.amirrezahadipoor.herodefense.gameplay.HeroAnimationController;
import com.amirrezahadipoor.herodefense.model.BraceLimits;
import com.amirrezahadipoor.herodefense.model.Hero;
import com.amirrezahadipoor.herodefense.model.HeroAnimationState;

import java.util.EnumMap;
import java.util.Map;

/**
 * Draws the procedural Blender Hero atlas wherever the Hero currently stands, plus the wave's step meter under
 * its feet (roadmap A1) — E2 adds WALK clip support and procedural bob.
 *
 * <p>The atlas now has five clips -- idle, walk, attack, hit and death. Walk reuses idle frames as fallback until
 * the Blender art pipeline delivers a true walk cycle, but the controller and renderer already distinguish walk from
 * idle so the first-run coach and the balance simulator can tell a stepping Hero from a rooted one. When walking,
 * a subtle vertical bob (2 units at walk FPS) breaks the glide that A1 documented as the honest option until a fifth
 * clip existed. That bob is the step towards something else that E2 asked for: not a second source of truth for feet
 * (feet remain at hero.y), but a visual cue that the Hero is spending its budget.
 *
 * <p>The meter is drawn only once some of the budget has been spent. An untouched wave therefore renders exactly
 * the pixels it rendered before the Hero could move, which is what keeps the emulator smoke journeys' reference
 * brightness measurements meaningful without a re-capture.
 */
public final class HeroSpriteRenderer implements AutoCloseable {
    private static final String ATLAS_PATH = "generated/sprites/hero.atlas";
    static final float FRAME_SIZE = 192f;
    static final float FEET_OFFSET_FROM_FRAME_BOTTOM = 23f;
    /** The meter's box, in world units, centred under the feet. */
    static final float METER_WIDTH = 84f;
    static final float METER_HEIGHT = 6f;
    static final float METER_GAP_BELOW_FEET = 10f;
    /** E2: walk bob amplitude in world units. */
    static final float WALK_BOB_AMPLITUDE = 2.5f;

    private final TextureAtlas atlas;
    private Texture pixel;
    private final Map<HeroAnimationState, Array<TextureAtlas.AtlasRegion>> frames =
        new EnumMap<>(HeroAnimationState.class);

    public HeroSpriteRenderer() {
        atlas = SheetPayloads.atlas(Gdx.files.internal(ATLAS_PATH));
        register(HeroAnimationState.IDLE, "hero_idle", HeroAnimationController.IDLE_FRAMES);
        // E2: walk clip — try hero_walk, fallback to hero_idle if not yet rendered
        registerWithFallback(HeroAnimationState.WALK, "hero_walk", HeroAnimationController.WALK_FRAMES, "hero_idle");
        register(HeroAnimationState.ATTACK, "hero_attack", HeroAnimationController.ATTACK_FRAMES);
        register(HeroAnimationState.HIT, "hero_hit", HeroAnimationController.HIT_FRAMES);
        register(HeroAnimationState.DEATH, "hero_death", HeroAnimationController.DEATH_FRAMES);
    }

    public void draw(SpriteBatch batch, Hero hero, int frameIndex) {
        Array<TextureAtlas.AtlasRegion> clip = frames.get(hero.animationState);
        if (clip == null) {
            clip = frames.get(HeroAnimationState.IDLE);
        }
        TextureAtlas.AtlasRegion frame = clip.get(Math.min(clip.size - 1, Math.max(0, frameIndex)));
        boolean bracing = hero.braceRemainingSeconds > 0f;
        if (bracing) {
            batch.setColor(0.62f, 0.80f, 0.95f, 1f);
        }
        float bob = 0f;
        if (hero.animationState == HeroAnimationState.WALK) {
            // E2: sine bob at walk FPS, 2.5 units amplitude, feet stay at hero.y (bob is visual only)
            bob = (float) Math.sin(hero.animationStateSeconds * HeroAnimationController.WALK_FRAMES_PER_SECOND * 1.1f) * WALK_BOB_AMPLITUDE;
        }
        batch.draw(
            frame,
            frameX(hero),
            frameY(hero) + bob,
            FRAME_SIZE,
            FRAME_SIZE
        );
        if (bracing) {
            batch.setColor(1f, 1f, 1f, 1f);
        }
        drawStepMeter(batch, hero);
        drawBraceMeter(batch, hero);
    }

    private void drawStepMeter(SpriteBatch batch, Hero hero) {
        if (hero == null || hero.stepBudgetUnits >= Hero.WAVE_STEP_BUDGET) {
            return;
        }
        float ratio = Math.max(0f, Math.min(1f, hero.stepBudgetUnits / Hero.WAVE_STEP_BUDGET));
        float x = hero.x - METER_WIDTH * 0.5f;
        float y = frameY(hero) - METER_GAP_BELOW_FEET;
        Texture texture = pixelTexture();
        batch.setColor(0.05f, 0.08f, 0.06f, 0.55f);
        batch.draw(texture, x - 1.5f, y - 1.5f, METER_WIDTH + 3f, METER_HEIGHT + 3f);
        if (ratio > 0.34f) {
            batch.setColor(0.56f, 0.87f, 0.53f, 0.92f);
        } else {
            batch.setColor(0.95f, 0.76f, 0.36f, 0.92f);
        }
        batch.draw(texture, x, y, METER_WIDTH * ratio, METER_HEIGHT);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawBraceMeter(SpriteBatch batch, Hero hero) {
        Texture texture = pixelTexture();
        float x = hero.x - METER_WIDTH * 0.5f;
        float y = frameY(hero) - METER_GAP_BELOW_FEET - METER_HEIGHT - 5f;
        if (hero.braceRemainingSeconds > 0f) {
            float ratio = Math.max(0f, Math.min(1f, hero.braceRemainingSeconds / BraceLimits.BRACE_SECONDS));
            batch.setColor(0.05f, 0.08f, 0.10f, 0.55f);
            batch.draw(texture, x - 1.5f, y - 1.5f, METER_WIDTH + 3f, METER_HEIGHT + 3f);
            batch.setColor(0.62f, 0.80f, 0.95f, 0.95f);
            batch.draw(texture, x, y, METER_WIDTH * ratio, METER_HEIGHT);
        } else if (hero.braceCooldownSeconds > 0f) {
            float ready = 1f - Math.max(0f, Math.min(1f, hero.braceCooldownSeconds / BraceLimits.COOLDOWN_SECONDS));
            batch.setColor(0.05f, 0.08f, 0.10f, 0.40f);
            batch.draw(texture, x - 1.5f, y - 1.5f, METER_WIDTH + 3f, METER_HEIGHT + 3f);
            batch.setColor(0.45f, 0.52f, 0.58f, 0.55f);
            batch.draw(texture, x, y, METER_WIDTH * ready, METER_HEIGHT);
        } else {
            return;
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private Texture pixelTexture() {
        if (pixel == null) {
            com.badlogic.gdx.graphics.Pixmap pixmap =
                new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fill();
            pixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return pixel;
    }

    static float frameX(Hero hero) {
        return hero.x - FRAME_SIZE * 0.5f;
    }

    static float frameY(Hero hero) {
        return hero.y - FEET_OFFSET_FROM_FRAME_BOTTOM;
    }

    private void register(HeroAnimationState state, String regionName, int expectedFrames) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(regionName);
        if (regions.size != expectedFrames) {
            atlas.dispose();
            throw new IllegalStateException(
                "Expected " + expectedFrames + " frames for " + regionName + ", found " + regions.size
            );
        }
        frames.put(state, regions);
    }

    private void registerWithFallback(HeroAnimationState state, String regionName, int expectedFrames, String fallbackRegion) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(regionName);
        if (regions.size == expectedFrames) {
            frames.put(state, regions);
            return;
        }
        // Fallback: reuse fallbackRegion (idle) for walk until art pipeline delivers walk cycle
        Array<TextureAtlas.AtlasRegion> fallback = atlas.findRegions(fallbackRegion);
        if (fallback.size == 0) {
            atlas.dispose();
            throw new IllegalStateException(
                "Expected " + expectedFrames + " frames for " + regionName + " or fallback " + fallbackRegion + ", found none"
            );
        }
        // If fallback has more frames than needed, trim; if fewer, loop (but idle has 6 which matches walk)
        if (fallback.size >= expectedFrames) {
            Array<TextureAtlas.AtlasRegion> trimmed = new Array<>();
            for (int i = 0; i < expectedFrames; i++) {
                trimmed.add(fallback.get(i % fallback.size));
            }
            frames.put(state, trimmed);
        } else {
            frames.put(state, fallback);
        }
    }

    @Override
    public void close() {
        if (pixel != null) {
            pixel.dispose();
            pixel = null;
        }
        atlas.dispose();
    }
}
