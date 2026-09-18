package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.gameplay.FocusFireSystem;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.function.Function;

/**
 * Corner brackets around the enemy the player tapped (roadmap R3.1).
 *
 * <p>The bracket fades with the remaining mark time, so the six-second window is visible without a number, and
 * the pulse is driven by the run clock instead of a frame counter so a dropped frame never changes its phase.
 */
final class FocusMarkRenderer {

    /** One bracket arm as a fraction of the framed sprite, clamped so small enemies still get a readable mark. */
    private static final float ARM_RATIO = 0.22f;

    private static final float THICKNESS_RATIO = 0.035f;

    private Texture pixel;

    void drawMarks(SpriteBatch batch, GameState state, float runTimeSeconds, Function<Enemy, float[]> boxOf) {
        if (state == null || boxOf == null) return;
        for (Enemy enemy : state.aliveEnemies) {
            drawMark(batch, enemy, runTimeSeconds, boxOf);
        }
        for (Boss boss : state.aliveBosses) {
            drawMark(batch, boss, runTimeSeconds, boxOf);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawMark(SpriteBatch batch, Enemy enemy, float runTimeSeconds, Function<Enemy, float[]> boxOf) {
        if (!FocusFireSystem.isMarked(enemy)) return;
        float[] box = boxOf.apply(enemy);
        float pulse = 0.72f + 0.28f * MathUtils.sin(runTimeSeconds * 6f);
        float fade = MathUtils.clamp(enemy.focusMarkSeconds / FocusFireSystem.MARK_SECONDS, 0f, 1f);
        batch.setColor(0.98f, 0.86f, 0.44f, Math.max(0.25f, pulse * fade));
        float arm = Math.max(10f, box[2] * ARM_RATIO);
        float thickness = Math.max(3f, box[2] * THICKNESS_RATIO);
        brackets(batch, box[0], box[1], box[2], arm, thickness);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /** Four L shaped brackets around a box: the tap target the player picked. */
    private void brackets(SpriteBatch batch, float boxX, float boxY, float size, float arm, float thickness) {
        Texture texture = pixelTexture();
        batch.draw(texture, boxX, boxY + size - thickness, arm, thickness);
        batch.draw(texture, boxX, boxY + size - arm, thickness, arm);
        batch.draw(texture, boxX + size - arm, boxY + size - thickness, arm, thickness);
        batch.draw(texture, boxX + size - thickness, boxY + size - arm, thickness, arm);
        batch.draw(texture, boxX, boxY, arm, thickness);
        batch.draw(texture, boxX, boxY, thickness, arm);
        batch.draw(texture, boxX + size - arm, boxY, arm, thickness);
        batch.draw(texture, boxX + size - thickness, boxY, thickness, arm);
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
}
