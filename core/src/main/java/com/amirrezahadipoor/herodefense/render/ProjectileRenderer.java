package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.Projectile;

/**
 * Every arrow in the air, and the ones that stopped.
 *
 * <p>Extracted from {@code CombatEntityRenderer} (the same trade as {@link ArrowTextures}, roadmap R2.2 paying
 * for R8.3): that class is frozen by the architecture ratchet, and the obstacle round added the one branch the
 * projectile pass was missing. A flight is three parts -- a trail of fletching streaks laid along the velocity,
 * the arrow itself rotated onto it, and a glint at the head -- and the extraction keeps them one pass with one
 * owner, because the trail's alpha, the arrow's size and the glint's size are three answers to the same question
 * (how much has this shot been paid for).
 *
 * <p>The branch the stones needed: an arrow that lodges in solid ground has no velocity left -- the simulation
 * zeroes it, because that is what lodging means -- so drawing it from its velocity would leave it lying flat in
 * the air. {@link #drawLodgedArrow} draws it at the angle it arrived at, half sunk into the rock and fading,
 * which is the mark the arena owes the player for a shot the field ate.
 */
final class ProjectileRenderer implements AutoCloseable {

    /** Fletching streaks behind a flying arrow. Three is the restraint the VFX gate asks for. */
    static final int PROJECTILE_TRAIL_STEPS = 3;
    /** An arrow with nothing moving: it fades over the seconds the simulation leaves it drawn. */
    static final float LODGED_ARROW_FADE_SECONDS = 0.75f;
    /** The share of a lodged arrow that is inside the rock: a buried head reads as a hit, a floating one as a bug. */
    static final float LODGED_ARROW_BURIED_SHARE = 0.34f;

    /** Raw progression in 0..10, the range the arrow trail and the bow's glow escalate over. */
    static final int MAX_PROGRESSION_STEP = 10;
    private static final float TRAIL_BASE_ALPHA = 0.55f;
    private static final float TRAIL_ALPHA_STEP = 0.15f;
    private static final float TRAIL_POWER_BOOST = 0.03f;
    private static final float TRAIL_ALPHA_CEILING = 0.85f;
    private static final float TRAIL_SPACING = 9f;
    private static final float TRAIL_TAPER = 1.6f;
    private static final float TRAIL_HEIGHT = 3f;

    private final Texture pixel;
    private final Texture arrowNormal;
    private final Texture arrowCrit;
    private final Texture arrowSecondary;

    /**
     * Takes the host's 1x1 white pixel rather than owning one: it is the same quad every other pass in the combat
     * renderer draws, and the host disposes it. The three arrow sprites are this pass's own and are released here.
     */
    ProjectileRenderer(Texture sharedPixel) {
        this.pixel = sharedPixel;
        arrowNormal = ArrowTextures.arrow(26, 6, 0.545f, 0.353f, 0.169f, 0.78f, 0.78f, 0.82f, 0.85f, 0.78f, 0.57f);
        arrowCrit = ArrowTextures.arrow(30, 8, 0.545f, 0.353f, 0.169f, 1f, 0.84f, 0.31f, 0.35f, 0.92f, 0.96f);
        arrowSecondary = ArrowTextures.arrow(20, 5, 0.30f, 0.36f, 0.23f, 0.72f, 0.75f, 0.78f, 0.48f, 0.80f, 0.52f);
    }

    /** Arrow rotation in degrees for a velocity vector; 0 is +X. */
    static float projectileRotation(float vx, float vy) {
        return MathUtils.atan2(vy, vx) * MathUtils.radiansToDegrees;
    }

    static float projectileTrailAlpha(int step) {
        return projectileTrailAlpha(step, 0);
    }

    /** The trail's alpha per streak, fading with distance and brightening with the shot's power step. */
    static float projectileTrailAlpha(int step, int powerStep) {
        float boost = Math.max(0, Math.min(MAX_PROGRESSION_STEP, powerStep)) * TRAIL_POWER_BOOST;
        return Math.min(TRAIL_ALPHA_CEILING, Math.max(0f, TRAIL_BASE_ALPHA - step * TRAIL_ALPHA_STEP) + boost);
    }

    /** 0..1 heat of a normal arrow's trail gold as raw progression climbs. */
    static float trailHeat(int powerStep) {
        return Math.max(0, Math.min(MAX_PROGRESSION_STEP, powerStep)) / (float) MAX_PROGRESSION_STEP;
    }

    void draw(SpriteBatch batch, GameState state, int powerStep) {
        if (state == null) {
            return;
        }
        float heat = trailHeat(powerStep);
        float goldRed = 0.93f + (1f - 0.93f) * heat;
        float goldGreen = 0.71f + (0.95f - 0.71f) * heat;
        float goldBlue = 0.25f + (0.75f - 0.25f) * heat;
        for (Projectile projectile : state.projectiles) {
            if (projectile == null || !projectile.active) {
                continue;
            }
            if (projectile.lodged) {
                drawLodgedArrow(batch, projectile);
                continue;
            }
            float angle = projectileRotation(projectile.velocityX, projectile.velocityY);
            float speed = (float) Math.sqrt(
                projectile.velocityX * projectile.velocityX
                    + projectile.velocityY * projectile.velocityY
            );
            float nx = speed <= 0f ? 1f : projectile.velocityX / speed;
            float ny = speed <= 0f ? 0f : projectile.velocityY / speed;
            drawTrail(batch, projectile, powerStep, angle, nx, ny, goldRed, goldGreen, goldBlue);
            float[] size = arrowSize(projectile);
            Texture arrowTex = arrowTexture(projectile);
            float arrowW = size[0];
            float arrowH = size[1];
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(
                arrowTex,
                projectile.x - arrowW * 0.5f,
                projectile.y - arrowH * 0.5f,
                arrowW * 0.5f,
                arrowH * 0.5f,
                arrowW,
                arrowH,
                1f,
                1f,
                angle,
                0, 0,
                (int) arrowW, (int) arrowH,
                false, false
            );
            // Head glint at the tip.
            batch.setColor(1f, 1f, 1f, 0.92f);
            float glint = projectile.critical ? 5f : projectile.secondary ? 3f : 4f;
            batch.draw(pixel, projectile.x + nx * (arrowW * 0.5f + 1f) - glint * 0.5f,
                projectile.y + ny * (arrowW * 0.5f + 1f) - glint * 0.5f, glint, glint);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * An arrow that has buried itself in solid ground.
     *
     * <p>Drawn where it stopped, at the angle it arrived at, with its head inside the rock: the shaft is shortened
     * by a third, the fletching streak behind it is gone (nothing is moving), and it sinks out of sight over the
     * three quarters of a second it has left rather than blinking away.
     */
    private void drawLodgedArrow(SpriteBatch batch, Projectile projectile) {
        float fade = Math.max(0f, Math.min(1f, projectile.lodgedSeconds / LODGED_ARROW_FADE_SECONDS));
        float angle = projectile.lodgedAngleDegrees;
        Texture arrowTex = arrowTexture(projectile);
        float[] size = arrowSize(projectile);
        float arrowW = size[0];
        float arrowH = size[1];
        float buried = arrowW * LODGED_ARROW_BURIED_SHARE;
        float shaft = arrowW - buried;
        // Anchored at the buried head, so the shaft grows back along the angle the shot arrived at.
        float dx = MathUtils.cosDeg(angle);
        float dy = MathUtils.sinDeg(angle);
        float nearX = projectile.x + dx * buried * 0.5f;
        float nearY = projectile.y + dy * buried * 0.5f;
        batch.setColor(1f, 1f, 1f, 0.92f * fade);
        batch.draw(
            arrowTex,
            nearX - shaft * 0.5f,
            nearY - arrowH * 0.5f,
            shaft * 0.5f,
            arrowH * 0.5f,
            shaft,
            arrowH,
            1f,
            1f,
            angle,
            0, 0,
            (int) shaft, (int) arrowH,
            false, false
        );
    }

    private void drawTrail(
        SpriteBatch batch, Projectile projectile, int powerStep, float angle, float nx, float ny,
        float goldRed, float goldGreen, float goldBlue
    ) {
        for (int step = 1; step <= PROJECTILE_TRAIL_STEPS; step++) {
            float back = step * TRAIL_SPACING;
            float alpha = projectileTrailAlpha(step, powerStep);
            if (projectile.critical) {
                batch.setColor(0.35f, 0.92f, 0.96f, alpha * 0.72f);
            } else if (projectile.secondary) {
                batch.setColor(0.62f, 0.86f, 0.58f, alpha * 0.65f);
            } else {
                batch.setColor(goldRed * 0.92f, goldGreen * 0.92f, goldBlue, alpha * 0.78f);
            }
            float streakW = TRAIL_SPACING - step * TRAIL_TAPER;
            float sx = projectile.x - nx * back;
            float sy = projectile.y - ny * back;
            batch.draw(
                pixel,
                sx - streakW * 0.5f, sy - TRAIL_HEIGHT * 0.5f,
                streakW * 0.5f, TRAIL_HEIGHT * 0.5f,
                streakW, TRAIL_HEIGHT,
                1f, 1f,
                angle,
                0, 0, 1, 1, false, false
            );
        }
    }

    private Texture arrowTexture(Projectile projectile) {
        if (projectile.critical) {
            return arrowCrit;
        }
        return projectile.secondary ? arrowSecondary : arrowNormal;
    }

    /** Width first, height second: the three shots are three sizes, not three recolours of one sprite. */
    private static float[] arrowSize(Projectile projectile) {
        if (projectile.critical) {
            return ARROW_CRIT_SIZE;
        }
        return projectile.secondary ? ARROW_SECONDARY_SIZE : ARROW_NORMAL_SIZE;
    }

    private static final float[] ARROW_NORMAL_SIZE = {26f, 6f};
    private static final float[] ARROW_CRIT_SIZE = {30f, 8f};
    private static final float[] ARROW_SECONDARY_SIZE = {20f, 5f};

    @Override
    public void close() {
        arrowNormal.dispose();
        arrowCrit.dispose();
        arrowSecondary.dispose();
    }

}
