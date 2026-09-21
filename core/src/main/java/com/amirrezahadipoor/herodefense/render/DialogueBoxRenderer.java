package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.audio.SpeechVoice;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.presentation.DialogueBox;
import java.util.List;

/**
 * The Undertale-style dialogue box: a black box with a white double border over the lower arena, the
 * speaker's name in the voice's own colour at the leading corner, and the line typing out under the
 * speaker's blips. The box slides up when the message arrives, holds for the reading with a blinking
 * closing marker, and fades itself away.
 *
 * <p>It draws what the {@link DialogueBox} has typed: the same reveal count the blips counted, so the
 * letter and the tap land together. The box sits on the 1280 design grid like the HUD rows above it,
 * which is why a tall panel gives it the same room they do.
 */
public final class DialogueBoxRenderer implements AutoCloseable {

    static final float BOX_X = 50f;
    static final float BOX_W = 620f;
    static final float BOX_TOP = 1015f;
    static final float BOX_BOTTOM = 760f;
    static final float INSET = 20f;
    static final float NAME_Y = 44f;
    static final float FIRST_TEXT_Y = 88f;
    static final float TEXT_STRIDE = 38f;
    static final int MAX_TEXT_LINES = 4;
    /** The box rises this far while it slides into place. */
    static final float SLIDE_RISE = 16f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverlayText text = new OverlayText();

    public void draw(SpriteBatch batch, Matrix4 projection, DialogueBox box) {
        if (box == null || !box.active()) {
            return;
        }
        float alpha = box.alpha();
        if (alpha <= 0.001f) {
            return;
        }
        float rise = (1f - box.slide()) * SLIDE_RISE;
        float top = BOX_TOP - rise;
        float bottom = BOX_BOTTOM - rise;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        fill(shapes, top, bottom, alpha);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(projection);
        batch.begin();
        drawSpeakerName(batch, box, top, alpha);
        drawTypedText(batch, box, top, alpha);
        batch.end();

        if (closingMarkerVisible(box)) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.setProjectionMatrix(projection);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1f, 1f, 1f, alpha);
            float cx = BOX_X + BOX_W * 0.5f;
            shapes.triangle(cx - 11f, bottom - 20f, cx + 11f, bottom - 20f, cx, bottom - 36f);
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    /** The black box and its white double border, the way the game's frames are drawn. */
    private static void fill(ShapeRenderer shapes, float top, float bottom, float alpha) {
        shapes.setColor(0.004f, 0.006f, 0.010f, 0.95f * alpha);
        shapes.rect(BOX_X, bottom, BOX_W, top - bottom);
        shapes.setColor(1f, 1f, 1f, alpha);
        shapes.rect(BOX_X, bottom, BOX_W, 3f);
        shapes.rect(BOX_X, top - 3f, BOX_W, 3f);
        shapes.rect(BOX_X, bottom, 3f, top - bottom);
        shapes.rect(BOX_X + BOX_W - 3f, bottom, 3f, top - bottom);
        float inner = 9f;
        shapes.rect(BOX_X + inner, bottom + inner, BOX_W - inner * 2f, 1.5f);
        shapes.rect(BOX_X + inner, top - inner - 1.5f, BOX_W - inner * 2f, 1.5f);
        shapes.rect(BOX_X + inner, bottom + inner, 1.5f, top - bottom - inner * 2f);
        shapes.rect(BOX_X + BOX_W - inner - 1.5f, bottom + inner, 1.5f, top - bottom - inner * 2f);
    }

    /** The speaker's name at the box's leading corner, in the voice's own colour. */
    private void drawSpeakerName(SpriteBatch batch, DialogueBox box, float top, float alpha) {
        text.drawLeading(
            batch, GameLocale.text(speakerName(box.voice())), BOX_X, BOX_W, INSET,
            top - NAME_Y, 1.2f, speakerColor(box.voice()), alpha
        );
    }

    /** The part of the line typed so far, wrapped and leading-aligned like every other panel row. */
    private void drawTypedText(SpriteBatch batch, DialogueBox box, float top, float alpha) {
        float maxWidth = BOX_W - INSET * 2f;
        List<String> rows = CodexOverlayRenderer.wrapLines(box.revealedText(), row -> text.width(row, 1.5f), maxWidth);
        float y = top - FIRST_TEXT_Y;
        for (int index = 0; index < MAX_TEXT_LINES && index < rows.size(); index++) {
            text.drawLeading(batch, rows.get(index), BOX_X, BOX_W, INSET, y, 1.5f, Color.WHITE, alpha);
            y -= TEXT_STRIDE;
        }
    }

    /** The blinking marker sits under the box while the fully typed line waits to be dismissed. */
    static boolean closingMarkerVisible(DialogueBox box) {
        return !box.typing() && !box.fading() && Math.sin(box.heldSeconds() * 5f) > 0f;
    }

    /** The table's name for the voice; the renderer never holds a sentence of its own. */
    static StoryStrings speakerName(SpeechVoice voice) {
        return switch (voice) {
            case TREE -> StoryStrings.SPEAKER_TREE;
            case HOLLOW -> StoryStrings.SPEAKER_HOLLOW;
            default -> StoryStrings.SPEAKER_WARDEN;
        };
    }

    /** The colour a name carries: the Warden's white, the Tree's leaf green, the Hollow's red. */
    static Color speakerColor(SpeechVoice voice) {
        return switch (voice) {
            case TREE -> OverlayText.POSITIVE;
            case HOLLOW -> OverlayText.NEGATIVE;
            default -> Color.WHITE;
        };
    }

    @Override
    public void close() {
        shapes.dispose();
        text.close();
    }
}
