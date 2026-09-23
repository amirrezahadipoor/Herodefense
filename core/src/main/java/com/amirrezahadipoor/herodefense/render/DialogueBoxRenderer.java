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
import com.amirrezahadipoor.herodefense.input.HudTouchLayout;
import com.amirrezahadipoor.herodefense.presentation.DialogueBox;
import java.util.List;

/**
 * The Undertale-style dialogue box: a black box with a white double border, the speaker's name in the voice's
 * own colour at the leading corner, and the line typing out under the speaker's blips. The box slides up when
 * the message arrives, holds for the reading with a blinking closing marker, and fades itself away.
 *
 * <p>It draws what the {@link DialogueBox} has typed: the same reveal count the blips counted, so the letter
 * and the tap land together. The box sits on the 1280 design grid like the HUD rows above it, which is why a
 * tall panel gives it the same room they do.
 *
 * <p>Where it sits and how big it is are both the line's: the box is as tall as the whole line needs, one to
 * four rows plus the name, so a one-line beat is a slim strip and not a quarter of the arena. On the arena it
 * hangs under the HUD's lowest caption and over the canopy, where nothing walks, so the Hero, the enemies at
 * the trunk and the ground the player taps stay in view; it used to span 760f..1015f over the trunk and the
 * front of the fight. On the game-over screen it goes to the top of the frame, above the title panel on a
 * phone taller than 16:9 and over the panel's title line on one that is not, so the run's numbers and its
 * buttons are never under it -- the victory line used to cover the summary's title and first row.
 */
public final class DialogueBoxRenderer implements AutoCloseable {

    static final float BOX_X = 50f;
    static final float BOX_W = 620f;
    /** The arena box's top edge: under the field detail caption (top 1051f) and the grove line (1075f). */
    static final float ARENA_BOX_TOP = 1036f;
    /** The game-over box's top edge on the design grid; it rises with the HUD on tall panels. */
    static final float GAME_OVER_BOX_TOP = 1272f;
    static final float INSET = 20f;
    /** From the box's top edge to the top of the name's capitals, clear of the inner border at 10.5f. */
    static final float NAME_TOP_INSET = 18f;
    /** Under the last text row's full line height, to the bottom edge. */
    static final float BOTTOM_INSET = 8f;
    static final int MAX_TEXT_LINES = 4;
    /** The box rises this far while it slides into place. */
    static final float SLIDE_RISE = 16f;
    /** The name is a LABEL, the line is BODY, or LABEL when BODY would need more rows than the box has. */
    static final float NAME_SCALE = 0.9f;
    static final float TEXT_SCALE = 1.0f;
    static final float LONG_TEXT_SCALE = 0.9f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverlayText text = new OverlayText();

    /** The rows a line takes and where they go, resolved once per frame from the whole line, not the typed part. */
    record Layout(float top, float bottom, float nameY, float firstTextY, float stride, float textScale) {
        float height() {
            return top - bottom;
        }
    }

    public void draw(SpriteBatch batch, Matrix4 projection, DialogueBox box) {
        if (box == null || !box.active()) {
            return;
        }
        float alpha = box.alpha();
        if (alpha <= 0.001f) {
            return;
        }
        float rise = (1f - box.slide()) * SLIDE_RISE;
        Layout layout = layout(box, boxTop(box.source()) - rise);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        fill(shapes, layout.top(), layout.bottom(), alpha);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(projection);
        batch.begin();
        drawSpeakerName(batch, box, layout, alpha);
        drawTypedText(batch, box, layout, alpha);
        batch.end();

        if (closingMarkerVisible(box)) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.setProjectionMatrix(projection);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1f, 1f, 1f, alpha);
            float cx = BOX_X + BOX_W * 0.5f;
            float bottom = layout.bottom();
            shapes.triangle(cx - 11f, bottom - 20f, cx + 11f, bottom - 20f, cx, bottom - 36f);
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    /**
     * The top edge for the line's source: the arena's for a beat, a whisper and the hub's letter (and the
     * opening and ceremony, which speak through the same renderer), the frame's top for the two game-over words.
     */
    static float boxTop(DialogueBox.Source source) {
        if (source == DialogueBox.Source.DEATH || source == DialogueBox.Source.VICTORY) {
            return GAME_OVER_BOX_TOP + HudTouchLayout.topShift();
        }
        return ARENA_BOX_TOP;
    }

    /** Wraps the whole line at the body size, or the label size when the body would overrun the box's rows. */
    private Layout layout(DialogueBox box, float top) {
        float maxWidth = BOX_W - INSET * 2f;
        float textScale = TEXT_SCALE;
        List<String> rows = CodexOverlayRenderer.wrapLines(box.text(), row -> text.width(row, TEXT_SCALE), maxWidth);
        if (rows.size() > MAX_TEXT_LINES) {
            textScale = LONG_TEXT_SCALE;
            rows = CodexOverlayRenderer.wrapLines(box.text(), row -> text.width(row, LONG_TEXT_SCALE), maxWidth);
        }
        float nameLine = text.lineHeight(GameFonts.Role.forLegacyScale(NAME_SCALE));
        float stride = text.lineHeight(GameFonts.Role.forLegacyScale(textScale));
        return layout(top, nameLine, stride, rows.size(), textScale);
    }

    /**
     * The arithmetic of {@link #layout(DialogueBox, float)}, kept apart so a headless test can hold it. Every
     * y is the top of a line's capitals, the way the fonts are drawn; the rows step down by the text's line
     * height and the box ends one line height and an inset under the last row's top.
     */
    static Layout layout(float top, float nameLineHeight, float textLineHeight, int rows, float textScale) {
        int shown = Math.max(1, Math.min(MAX_TEXT_LINES, rows));
        float nameY = top - NAME_TOP_INSET;
        float firstTextY = nameY - nameLineHeight;
        float bottom = firstTextY - shown * textLineHeight - BOTTOM_INSET;
        return new Layout(top, bottom, nameY, firstTextY, textLineHeight, textScale);
    }

    /** The black box and its white double border, the way the game's frames are drawn. */
    private static void fill(ShapeRenderer shapes, float top, float bottom, float alpha) {
        shapes.setColor(0.004f, 0.006f, 0.010f, 0.92f * alpha);
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
    private void drawSpeakerName(SpriteBatch batch, DialogueBox box, Layout layout, float alpha) {
        text.drawLeading(
            batch, GameLocale.text(speakerName(box.voice())), BOX_X, BOX_W, INSET,
            layout.nameY(), NAME_SCALE, speakerColor(box.voice()), alpha
        );
    }

    /** The part of the line typed so far, wrapped and leading-aligned like every other panel row. */
    private void drawTypedText(SpriteBatch batch, DialogueBox box, Layout layout, float alpha) {
        float maxWidth = BOX_W - INSET * 2f;
        float scale = layout.textScale();
        List<String> rows = CodexOverlayRenderer.wrapLines(box.revealedText(), row -> text.width(row, scale), maxWidth);
        float y = layout.firstTextY();
        for (int index = 0; index < MAX_TEXT_LINES && index < rows.size(); index++) {
            text.drawLeading(batch, rows.get(index), BOX_X, BOX_W, INSET, y, scale, Color.WHITE, alpha);
            y -= layout.stride();
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
            case PIP -> StoryStrings.SPEAKER_PIP;
            case BOSS -> StoryStrings.SPEAKER_NIGHT;
            default -> StoryStrings.SPEAKER_WARDEN;
        };
    }

    /** The colour a name carries: the Warden's white, Granny's leaf green, the Hollow's red, Pip's gold,
     * and the Night Shift's ember. */
    static Color speakerColor(SpeechVoice voice) {
        return switch (voice) {
            case TREE -> OverlayText.POSITIVE;
            case HOLLOW -> OverlayText.NEGATIVE;
            case PIP -> OverlayText.GOLD;
            case BOSS -> OverlayText.EMBER;
            default -> Color.WHITE;
        };
    }

    @Override
    public void close() {
        shapes.dispose();
        text.close();
    }
}
