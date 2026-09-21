package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.amirrezahadipoor.herodefense.gameplay.BossFightScript;
import com.amirrezahadipoor.herodefense.gameplay.BossSpecialAttackSystem;
import com.amirrezahadipoor.herodefense.gameplay.BossSpecialZone;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;

/**
 * Draws the ground a boss special is about to cover, in the boss's identity color (audit item 2, roadmap A5).
 *
 * <p>The warning used to be a ring around the <em>hero</em>: a promise that something was coming, with no shape a
 * player could read and no answer except the brace. It draws a hitbox now -- the same circle or cone that
 * {@link BossSpecialZone} resolves the damage against -- so "can I get out of this" is answered by looking at the
 * ground instead of by guessing. Two details carry the read:
 *
 * <ul>
 *   <li><b>The zone fills in as the telegraph runs out.</b> The outline is drawn for the whole window and the
 *   interior closes in from the edge, so the time left is legible without a timer.</li>
 *   <li><b>A miss leaves a flash.</b> When the special lands on empty ground the zone flares for
 *   {@link BossSpecialAttackSystem#MISS_FLASH_SECONDS}: the feedback that makes a step feel like an answer instead
 *   of a coincidence.</li>
 * </ul>
 *
 * <p>Every point is drawn on the same squashed ground plane the hand-authored warning rings use, so the shape on
 * the floor is the shape the hero is standing in.
 */
final class TelegraphZoneRenderer {
    private static final int ARC_SEGMENTS = 28;
    private static final int FILL_RINGS = 3;
    private static final float DOT = 6f;
    private static final float STACK_STEP = 12f;
    private static final float GROUND_Y_OFFSET = -20f;
    private static final float FILL_ALPHA = 0.35f;
    private static final float MISS_RING_GROWTH = 2.4f;

    private TelegraphZoneRenderer() {
    }

    static void draw(SpriteBatch batch, Texture pixel, Boss boss, int stack, float runTimeSeconds) {
        BossType type = boss.bossDefinition();
        if (boss.specialPending) {
            BossFightScript script = BossFightScript.of(boss);
            float window = Math.max(0.0001f, script.telegraphSeconds());
            float remaining = Math.max(0f, Math.min(1f, boss.specialAnimationSeconds / window));
            float scale = script.currentTellScale(boss);
            float alpha = CombatEntityRenderer.telegraphAlpha(runTimeSeconds, remaining);
            batch.setColor(type.telegraphRed(), type.telegraphGreen(), type.telegraphBlue(), alpha);
            if (boss.specialZoneCone) {
                drawCone(batch, pixel, boss, stack, scale, remaining, alpha);
            } else {
                drawCircle(batch, pixel, boss, stack, scale, remaining, alpha);
            }
        }
        if (boss.specialMissFlashSeconds > 0f) {
            float left = boss.specialMissFlashSeconds / BossSpecialAttackSystem.MISS_FLASH_SECONDS;
            batch.setColor(type.telegraphRed(), type.telegraphGreen(), type.telegraphBlue(), 0.9f * left);
            float radius = boss.specialZoneRadius * (1f + MISS_RING_GROWTH * (1f - left)) + stack * STACK_STEP;
            ring(batch, pixel, boss.specialZoneX, boss.specialZoneY, radius, ARC_SEGMENTS);
        }
    }

    /** The slam and the planted zones: one outline, plus fill rings that close in as the warning runs out. */
    private static void drawCircle(
        SpriteBatch batch, Texture pixel, Boss boss, int stack, float scale, float remaining, float alpha) {
        float radius = boss.specialZoneRadius * scale + stack * STACK_STEP;
        ring(batch, pixel, boss.specialZoneX, boss.specialZoneY, radius, ARC_SEGMENTS);
        int filled = Math.round(FILL_RINGS * (1f - remaining));
        for (int index = 1; index <= filled; index++) {
            batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b, FILL_ALPHA * alpha);
            ring(batch, pixel, boss.specialZoneX, boss.specialZoneY, radius * index / (FILL_RINGS + 1f),
                ARC_SEGMENTS / 2);
        }
    }

    /** The sweep: the arc it will cover, its two closing edges, and the point-blank circle nobody escapes by hugging. */
    private static void drawCone(
        SpriteBatch batch, Texture pixel, Boss boss, int stack, float scale, float remaining, float alpha) {
        float reach = boss.specialZoneReach * scale;
        float half = boss.specialZoneHalfAngle;
        float step = half * 2f / ARC_SEGMENTS;
        for (int index = 0; index <= ARC_SEGMENTS; index++) {
            emit(batch, pixel, boss, (float) Math.cos(boss.specialZoneAngleRadians - half + step * index) * reach,
                (float) Math.sin(boss.specialZoneAngleRadians - half + step * index) * reach);
        }
        int spokes = Math.max(2, Math.round(4f * (1f - remaining)) + 1);
        for (int index = 1; index <= spokes; index++) {
            batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b, FILL_ALPHA * alpha);
            float fraction = index / (float) (spokes + 1);
            for (float edge : new float[] {-half, half}) {
                float angle = boss.specialZoneAngleRadians + edge;
                emit(batch, pixel, boss, (float) Math.cos(angle) * reach * fraction,
                    (float) Math.sin(angle) * reach * fraction);
            }
        }
        ring(batch, pixel, boss.specialZoneX, boss.specialZoneY,
            BossSpecialZone.POINT_BLANK_RADIUS * scale + stack * STACK_STEP, ARC_SEGMENTS / 2);
    }

    private static void ring(SpriteBatch batch, Texture pixel, float centerX, float centerY, float radius,
        int segments) {
        for (int index = 0; index < segments; index++) {
            double angle = index * Math.PI * 2.0 / segments;
            emit(batch, pixel, centerX, centerY, (float) Math.cos(angle) * radius,
                (float) Math.sin(angle) * radius);
        }
    }

    private static void emit(SpriteBatch batch, Texture pixel, Boss boss, float offsetX, float offsetY) {
        emit(batch, pixel, boss.specialZoneX, boss.specialZoneY, offsetX, offsetY);
    }

    /** One dot of the zone, offset from its centre in world units and drawn on the squashed ground plane. */
    private static void emit(
        SpriteBatch batch, Texture pixel, float centerX, float centerY, float offsetX, float offsetY) {
        float x = centerX + offsetX;
        float y = centerY + offsetY * CombatEntityRenderer.TELEGRAPH_SQUASH + GROUND_Y_OFFSET;
        batch.draw(pixel, x - DOT / 2f, y - DOT / 2f, DOT, DOT);
    }
}
