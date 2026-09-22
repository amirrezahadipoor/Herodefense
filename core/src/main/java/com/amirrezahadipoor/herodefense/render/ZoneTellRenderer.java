package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.amirrezahadipoor.herodefense.gameplay.EnemyVerbs;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Everything the game draws on the ground to say "this is coming".
 *
 * <p>Extracted from {@code CombatEntityRenderer} when the role verbs arrived: two warning passes (the boss
 * special's zones and the Bark Stalker's spit line) had grown inside a class the architecture ratchet holds
 * frozen, and a warning is a drawing concern with one owner -- the frame it appears on is the frame the damage
 * resolves against, so both the tell and the hit read the same state and can never disagree.
 */
public final class ZoneTellRenderer implements AutoCloseable {

    /** The spit line's thickness and the radius of the mark it lands on. */
    static final float SPIT_LINE_THICKNESS = 7f;
    static final float SPIT_MARK_RADIUS = 22f;

    private final Texture pixel;
    private final TextureRegion pixelRegion;

    public ZoneTellRenderer() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixelRegion = new TextureRegion(pixel);
        pixmap.dispose();
    }

    public void draw(SpriteBatch batch, GameState state, float runTimeSeconds) {
        drawBossZones(batch, state, runTimeSeconds);
        drawVerbTells(batch, state);
    }

    private void drawBossZones(SpriteBatch batch, GameState state, float runTimeSeconds) {
        if (state == null || state.hero == null || !state.hero.alive || state.aliveBosses == null) {
            return;
        }
        int stack = 0;
        for (Boss boss : state.aliveBosses) {
            if (boss == null || !boss.alive) continue;
            TelegraphZoneRenderer.draw(batch, pixel, boss, stack, runTimeSeconds);
            stack++;
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * The Bark Stalker's wind-up: a line from the creature to the spot it is aiming at, drawn from the same latch
     * the damage resolves against. A tell that can disagree with the hit is worse than no tell, so there is
     * exactly one flag behind both (see {@code EnemyVerbs.spitMissed}).
     */
    private void drawVerbTells(SpriteBatch batch, GameState state) {
        if (state == null || state.aliveEnemies == null) {
            return;
        }
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy == null || !enemy.verbLatched || !enemy.alive) {
                continue;
            }
            float dx = enemy.verbMarkX - enemy.x;
            float dy = enemy.verbMarkY - enemy.y;
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length <= 1f) {
                continue;
            }
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            float reach = Math.min(length, EnemyVerbs.SPIT_RANGE);
            batch.setColor(0.86f, 0.62f, 0.34f, 0.30f);
            batch.draw(
                pixelRegion,
                enemy.x, enemy.y - SPIT_LINE_THICKNESS * 0.5f,
                0f, SPIT_LINE_THICKNESS * 0.5f,
                reach, SPIT_LINE_THICKNESS,
                1f, 1f,
                angle
            );
            batch.setColor(0.94f, 0.76f, 0.48f, 0.55f);
            batch.draw(
                pixel,
                enemy.verbMarkX - SPIT_MARK_RADIUS, enemy.verbMarkY - SPIT_MARK_RADIUS,
                SPIT_MARK_RADIUS * 2f, SPIT_MARK_RADIUS * 2f
            );
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void close() {
        pixel.dispose();
    }
}
