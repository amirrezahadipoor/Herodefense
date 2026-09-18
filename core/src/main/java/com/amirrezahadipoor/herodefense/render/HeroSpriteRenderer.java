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
 * its feet (roadmap A1).
 *
 * <p>The atlas has four clips -- idle, attack, hit and death -- and no walk cycle, so a stepping Hero slides on
 * the idle pose. That reads as a glide at 165 units a second for a second and a half, which is the length of the
 * whole budget, and it is the honest option until an art pass adds a fifth clip: inventing a bob in the renderer
 * would be a second source of truth for where the Hero's feet are, and {@code CeremonyHeroRenderer} matches this
 * class's geometry on purpose so the hand-off from a ceremony to the combat idle never jumps.
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

    private final TextureAtlas atlas;
    private Texture pixel;
    private final Map<HeroAnimationState, Array<TextureAtlas.AtlasRegion>> frames =
        new EnumMap<>(HeroAnimationState.class);

    public HeroSpriteRenderer() {
        atlas = SheetPayloads.atlas(Gdx.files.internal(ATLAS_PATH));
        register(HeroAnimationState.IDLE, "hero_idle", HeroAnimationController.IDLE_FRAMES);
        register(HeroAnimationState.ATTACK, "hero_attack", HeroAnimationController.ATTACK_FRAMES);
        register(HeroAnimationState.HIT, "hero_hit", HeroAnimationController.HIT_FRAMES);
        register(HeroAnimationState.DEATH, "hero_death", HeroAnimationController.DEATH_FRAMES);
    }

    public void draw(SpriteBatch batch, Hero hero, int frameIndex) {
        Array<TextureAtlas.AtlasRegion> clip = frames.get(hero.animationState);
        TextureAtlas.AtlasRegion frame = clip.get(Math.min(clip.size - 1, Math.max(0, frameIndex)));
        boolean bracing = hero.braceRemainingSeconds > 0f;
        if (bracing) {
            // A cold blue wash over the idle clip: the shield has no art of its own, and a tint is the one
            // signal that cannot be missed in a frame full of damage numbers while costing no new sheet.
            batch.setColor(0.62f, 0.80f, 0.95f, 1f);
        }
        batch.draw(
            frame,
            frameX(hero),
            frameY(hero),
            FRAME_SIZE,
            FRAME_SIZE
        );
        if (bracing) {
            batch.setColor(1f, 1f, 1f, 1f);
        }
        drawStepMeter(batch, hero);
        drawBraceMeter(batch, hero);
    }

    /**
     * What is left of this wave's stepping, under the feet, where the player is already looking.
     *
     * <p>A budget that empties silently reads as a bug -- the finger drags and the Hero refuses -- so the meter is
     * the feedback, and it is the only feedback: no floating text (a line over the arena competes with the coach
     * and the whispers), no haptic (a pulse says "something happened", not "you have 40 units left"). It appears
     * the moment the first unit is spent and disappears at the next wave, when the budget is full again.
     */
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
        // Green while the wave can still be repositioned, amber once the budget is a third spent, and the same
        // amber at zero rather than a red that would read as damage in a frame full of damage numbers.
        if (ratio > 0.34f) {
            batch.setColor(0.56f, 0.87f, 0.53f, 0.92f);
        } else {
            batch.setColor(0.95f, 0.76f, 0.36f, 0.92f);
        }
        batch.draw(texture, x, y, METER_WIDTH * ratio, METER_HEIGHT);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * The shield's own clock under the feet, one row below the step meter: full and bright while the brace is
     * up, then a dim refill for the cooldown. Both bars are absent at rest -- a Hero with a full step budget and
     * a spent cooldown draws exactly the pixels it drew before roadmap A2 -- so no captured screen changes until
     * a player chooses the verb.
     */
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

    @Override
    public void close() {
        if (pixel != null) {
            pixel.dispose();
            pixel = null;
        }
        atlas.dispose();
    }
}
