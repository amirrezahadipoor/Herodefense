package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.OnboardingStrings;
import com.amirrezahadipoor.herodefense.input.OnboardingTouchLayout;
import com.amirrezahadipoor.herodefense.onboarding.OnboardingStep;
import com.amirrezahadipoor.herodefense.onboarding.OnboardingSystem;

/**
 * The first vigil's coaching banner (roadmap R7.1).
 *
 * <p>It is drawn over the running wave rather than as its own screen, because the lessons are about the
 * running wave: the player learns by doing the thing while the enemies are already coming. The banner is a
 * band above the utility row, with the line the coach is waiting for, the hint that says where to do it, a
 * row of pips, one per step and drawn from the step table rather than from a count written here (filled once
 * each is done, and the current one filling with the step's own clock so a player can see that ignoring it is
 * temporary), and the Skip button that ends the lesson.
 */
public final class OnboardingOverlayRenderer implements AutoCloseable {
    static final float LINE_X = 44f;
    static final float LINE_Y_INSET = 78f;
    static final float HINT_Y_INSET = 34f;
    static final float LINE_SCALE = 1.06f;
    static final float HINT_SCALE = 0.72f;
    static final float TITLE_SCALE = 0.68f;
    static final float PIP_SIZE = 16f;
    static final float PIP_GAP = 10f;
    static final float PIP_X = 44f;
    static final float PIP_Y_INSET = 112f;
    static final float FADE_SECONDS = 0.22f;
    /** Room left between the coaching line/hint and the Skip button: text never draws under the button. */
    static final float SKIP_TEXT_GAP = 24f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverlayText text = new OverlayText();

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        OnboardingSystem onboarding,
        UiFrameRenderer frames
    ) {
        if (onboarding == null || !onboarding.active()) return;
        OnboardingStep step = onboarding.currentStep();
        float x = OnboardingTouchLayout.bannerX();
        float y = OnboardingTouchLayout.bannerY();
        float width = OnboardingTouchLayout.BANNER_WIDTH;
        float height = OnboardingTouchLayout.BANNER_HEIGHT;
        float reveal = Math.min(1f, onboarding.stepProgress() * 8f + FADE_SECONDS);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.055f, 0.115f, 0.098f, 0.86f * reveal);
        shapes.rect(x, y, width, height);
        shapes.setColor(0.16f, 0.42f, 0.30f, 0.90f * reveal);
        shapes.rect(x, y + height - 6f, width, 6f);
        drawPips(shapes, onboarding, x, y, reveal);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(projection);
        batch.begin();
        batch.setColor(1f, 1f, 1f, reveal);
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            OnboardingTouchLayout.skipX(), OnboardingTouchLayout.skipY(),
            OnboardingTouchLayout.SKIP_WIDTH, OnboardingTouchLayout.SKIP_HEIGHT, true, false
        );
        text.drawLeading(batch, GameLocale.text(OnboardingStrings.TITLE), x, width, PIP_X,
            y + PIP_Y_INSET + 14f, TITLE_SCALE, new Color(
            OverlayText.GOLD.r, OverlayText.GOLD.g, OverlayText.GOLD.b, reveal));
        // The coaching line and hint must stop before the Skip button they share the banner with: both
        // run at the same height as the button, and the first vigil's line ("Tap an enemy and the bow
        // focuses it") drew straight under it on the captured frames. They are fitted to the lane that
        // remains, shrinking like the menu rows instead of borrowing the button's space.
        float lineLaneWidth = OnboardingTouchLayout.skipX() - x - LINE_X - SKIP_TEXT_GAP;
        text.drawLeadingFitted(batch, step.line(), x, width, LINE_X, lineLaneWidth, y + LINE_Y_INSET,
            LINE_SCALE, new Color(
            OverlayText.IVORY.r, OverlayText.IVORY.g, OverlayText.IVORY.b, reveal));
        text.drawLeadingFitted(batch, step.hint(), x, width, LINE_X, lineLaneWidth, y + HINT_Y_INSET,
            HINT_SCALE, new Color(
            OverlayText.SUBTLE.r, OverlayText.SUBTLE.g, OverlayText.SUBTLE.b, reveal));
        text.drawCentered(
            batch, GameLocale.text(OnboardingStrings.SKIP),
            OnboardingTouchLayout.skipX() + OnboardingTouchLayout.SKIP_WIDTH * 0.5f,
            OnboardingTouchLayout.skipY() + 58f,
            0.94f, new Color(OverlayText.IVORY.r, OverlayText.IVORY.g, OverlayText.IVORY.b, reveal)
        );
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    /** One pip per step: completed steps solid, the current one filling with its own budget. */
    private void drawPips(ShapeRenderer shapes, OnboardingSystem onboarding, float x, float y, float reveal) {
        int steps = OnboardingStep.values().length;
        int done = onboarding.completedCount();
        OnboardingStep current = onboarding.currentStep();
        for (int index = 0; index < steps; index++) {
            float pipX = x + PIP_X + index * (PIP_SIZE + PIP_GAP);
            float pipY = y + PIP_Y_INSET - PIP_SIZE;
            if (index < done) {
                shapes.setColor(0.36f, 0.78f, 0.52f, 0.95f * reveal);
                shapes.rect(pipX, pipY, PIP_SIZE, PIP_SIZE);
            } else if (index == done && onboarding.active()) {
                float progress = Math.min(1f, current == null ? 0f : onboarding.stepProgress());
                shapes.setColor(0.20f, 0.30f, 0.26f, 0.90f * reveal);
                shapes.rect(pipX, pipY, PIP_SIZE, PIP_SIZE);
                shapes.setColor(0.74f, 0.60f, 0.30f, 0.95f * reveal);
                shapes.rect(pipX, pipY, PIP_SIZE * progress, PIP_SIZE);
            } else {
                shapes.setColor(0.20f, 0.30f, 0.26f, 0.70f * reveal);
                shapes.rect(pipX, pipY, PIP_SIZE, PIP_SIZE);
            }
        }
    }

    @Override
    public void close() {
        shapes.dispose();
        text.close();
    }
}
