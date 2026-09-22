package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * The contact shadows every body stands on.
 *
 * <p>Actors used to be pasted onto the arena floor: a sprite with no relationship to the ground it was standing
 * on, which is why a wave read as a pile of stickers rather than as a place with bodies in it. One soft ellipse
 * under each body is the cheapest fix in the whole visual language and the loudest: it says where a creature
 * actually is, it separates overlapping bodies during a swarm, and it keeps the tree, the Hero and a boss in the
 * same physical space as the enemies they are fighting.
 *
 * <p>DraWn in the environment pass, before any actor: a shadow belongs to the ground. Everything here is one
 * white pixel and a tint, so the pass costs a handful of quads and adds no texture to the catalog -- the texture
 * budget is the tightest number in this project and a shadow is not worth a sheet.
 */
public final class GroundShadowRenderer implements AutoCloseable {

    /** Shadow width as a share of the body's drawn width. */
    static final float WIDTH_SHARE = 0.68f;
    /** Shadow height as a share of its width; the floor is a tilted plane, so a shadow is a lens, not a disc. */
    static final float FLATTEN = 0.30f;
    /** Opacity of a body's shadow. Low on purpose: a swarm of 24 must not turn the floor black. */
    static final float ALPHA = 0.30f;
    /** A boss casts a heavier shadow than a regular body. */
    static final float BOSS_ALPHA = 0.42f;
    /** The Hero's own width in world units, matching {@code HeroSpriteRenderer}'s drawn size. */
    static final float HERO_WIDTH = 132f;
    /** How far below the body's feet the shadow sits, so it reads as ground contact rather than a body part. */
    static final float FOOT_OFFSET = 10f;

    private static final int LAYERS = 3;
    private static final float LAYER_GROWTH = 0.42f;
    private static final float LAYER_ALPHA_FALLOFF = 0.42f;

    private final Texture pixel;
    private final Color color = new Color();

    public GroundShadowRenderer() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
    }

    /** Draws every living body's shadow. Must be called with the batch between begin and end, under the actors. */
    public void draw(SpriteBatch batch, GameState state) {
        if (state == null) {
            return;
        }
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy == null || !enemy.alive) {
                continue;
            }
            drawShadow(batch, enemy.x, enemy.y, EnemyDrawScale.of(enemy.type()), ALPHA);
        }
        for (Boss boss : state.aliveBosses) {
            if (boss == null || !boss.alive) {
                continue;
            }
            drawShadow(batch, boss.x, boss.y, CombatEntityRenderer.bossDrawSize(), BOSS_ALPHA);
        }
        if (state.hero != null && state.hero.alive) {
            drawShadow(batch, state.hero.x, state.hero.y, HERO_WIDTH, ALPHA);
        }
        drawTreeShadow(batch, WorldLayout.WORLD_TREE_X, WorldLayout.WORLD_TREE_Y);
        for (int index = 0; index < state.plantedTreesCount; index++) {
            drawTreeShadow(batch, WorldLayout.groveTreeX(index + 1), WorldLayout.groveTreeY(index + 1));
        }
    }

    private void drawTreeShadow(SpriteBatch batch, float centerX, float centerY) {
        drawShadow(batch, centerX, centerY, 300f, 0.34f);
    }

    private void drawShadow(SpriteBatch batch, float centerX, float centerY, float bodyWidth, float alpha) {
        float width = bodyWidth * WIDTH_SHARE;
        for (int layer = 0; layer < LAYERS; layer++) {
            float grown = width * (1f + LAYER_GROWTH * layer);
            float height = grown * FLATTEN;
            float layerAlpha = alpha * (1f - LAYER_ALPHA_FALLOFF * layer) / LAYERS;
            color.set(0.02f, 0.05f, 0.04f, Math.max(0f, layerAlpha));
            batch.setColor(color);
            batch.draw(
                pixel,
                centerX - grown * 0.5f,
                centerY - height * 0.5f - FOOT_OFFSET,
                grown,
                height
            );
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public void close() {
        pixel.dispose();
    }
}
