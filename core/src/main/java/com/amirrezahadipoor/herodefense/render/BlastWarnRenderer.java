package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.amirrezahadipoor.herodefense.gameplay.EliteAffixSystem;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Pulsing danger ring under a dying blightburst Elite: the exact ground its death blast will
 * cover, pulsing on a 0.6 s beat so it reads as a fuse rather than decor (roadmap A2).
 */
public final class BlastWarnRenderer {

    /** Dashes on the warn ring, and the 0.6 s beat they pulse on. */
    static final int SEGMENTS = 20;
    static final float PERIOD_SECONDS = 0.6f;

    private final Texture pixel;

    public BlastWarnRenderer(Texture pixel) {
        this.pixel = pixel;
    }

    /** One squashed ground ring per warned, still-living blightburst. */
    public void draw(SpriteBatch batch, GameState state, float runTimeSeconds) {
        if (state == null || state.aliveEnemies == null) {
            return;
        }
        boolean drew = false;
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy == null || !enemy.alive || !enemy.blastWarned) {
                continue;
            }
            float pulse = (float) Math.sin(runTimeSeconds * Math.PI * 2.0 / PERIOD_SECONDS);
            batch.setColor(0.55f, 1.0f, 0.25f,
                MathUtils.clamp(0.30f + 0.22f * pulse, 0.08f, 0.52f));
            for (int index = 0; index < SEGMENTS; index++) {
                double angle = index * Math.PI * 2.0 / SEGMENTS;
                float px = enemy.x + (float) Math.cos(angle) * EliteAffixSystem.BLIGHT_BLAST_RADIUS;
                float py = enemy.y
                    + (float) Math.sin(angle) * EliteAffixSystem.BLIGHT_BLAST_RADIUS * CombatEntityRenderer.TELEGRAPH_SQUASH;
                batch.draw(pixel, px - 3f, py - 3f, 6f, 6f);
            }
            drew = true;
        }
        if (drew) {
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }
}
