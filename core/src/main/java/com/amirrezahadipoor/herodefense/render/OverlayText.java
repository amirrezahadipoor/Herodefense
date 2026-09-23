package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Shared premium-v2 typography: density-true glyphs in the current language's face, warm parchment fills, and a
 * fixed forest shadow. Legacy call sites still pass a scale; it is mapped onto a typographic role so
 * every label is physically at least 11sp on the device instead of 5sp of blurred bitmap font.
 *
 * <p>Every string that reaches a batch passes through {@link #visual(String)} first, and this is the only place
 * in the render path where that happens. Until 2026-09-23 that step shaped and reordered the run for the
 * right-to-left language the game shipped beside English; the owner deleted that translation outright, so the
 * step is the identity now and measuring is measuring what is drawn.
 */
final class OverlayText implements AutoCloseable {
    static final Color GOLD = Color.valueOf("EAC66D");
    static final Color IVORY = Color.valueOf("F7EBCB");
    static final Color SUBTLE = Color.valueOf("C6D2C4");
    static final Color POSITIVE = Color.valueOf("7ED898");
    static final Color NEGATIVE = Color.valueOf("EA7F79");
    static final Color EMBER = Color.valueOf("E8964F");
    static final Color MUTED = Color.valueOf("97A096");

    private static final float SHADOW_OFFSET_X = 1.5f;
    private static final float SHADOW_OFFSET_Y = -2.5f;
    private static final float SHADOW_ALPHA = 0.85f;

    private final GlyphLayout layout = new GlyphLayout();

    OverlayText() {
    }

    void draw(SpriteBatch batch, String text, float x, float y, float scale, Color color) {
        draw(batch, text, x, y, scale, color, 1f);
    }

    void draw(
        SpriteBatch batch, String text, float x, float y, float scale, Color color, float alpha
    ) {
        draw(batch, text, x, y, GameFonts.Role.forLegacyScale(scale), color, alpha);
    }

    void draw(
        SpriteBatch batch,
        String text,
        float x,
        float y,
        GameFonts.Role role,
        Color color,
        float alpha
    ) {
        if (text == null || text.isEmpty()) return;
        float resolvedAlpha = Math.max(0f, Math.min(1f, color.a * alpha));
        BitmapFont font = GameFonts.shared().font(role, GameLocale.current());
        String visual = visual(text);
        font.setColor(0.003f, 0.010f, 0.009f, resolvedAlpha * SHADOW_ALPHA);
        font.draw(batch, visual, x + SHADOW_OFFSET_X, y + SHADOW_OFFSET_Y);
        font.setColor(color.r, color.g, color.b, resolvedAlpha);
        font.draw(batch, visual, x, y);
    }

    /**
     * Draws {@code text} on the container's leading edge, {@code inset} in from it. The run is measured as
     * drawn, because mirroring with the wrong width would leave the last word hanging off the edge it was
     * measured against.
     */
    void drawLeading(
        SpriteBatch batch,
        String text,
        float containerX,
        float containerWidth,
        float inset,
        float y,
        float scale,
        Color color
    ) {
        draw(batch, text,
            UiMirror.leading(containerX, containerWidth, inset, width(text, scale)), y, scale, color);
    }

    /** {@link #drawLeading} with the alpha a reveal or a disabled state asks for. */
    void drawLeading(
        SpriteBatch batch,
        String text,
        float containerX,
        float containerWidth,
        float inset,
        float y,
        float scale,
        Color color,
        float alpha
    ) {
        draw(batch, text,
            UiMirror.leading(containerX, containerWidth, inset, width(text, scale)), y, scale, color, alpha);
    }

    /**
     * {@link #drawLeading} for a run that has to end inside {@code maxWidth} of its leading edge. The run is
     * measured at its own role; if it is wider, each smaller role down to CAPTION is tried, and if the smallest
     * role is still too wide the glyphs are squeezed to the width for this one draw and the font's own scale is
     * put back. The menu rows are the callers: a row's button ends where every other row's does, and a longer
     * translation, a bigger number or a larger system font must shrink rather than run under the frame's edge --
     * which is how the menu's second lines read "...half the heartw" before this existed.
     */
    void drawLeadingFitted(
        SpriteBatch batch,
        String text,
        float containerX,
        float containerWidth,
        float inset,
        float maxWidth,
        float y,
        float scale,
        Color color
    ) {
        if (text == null || text.isEmpty()) return;
        GameFonts.Role role = GameFonts.Role.forLegacyScale(scale);
        float width = width(text, role);
        GameFonts.Role[] roles = GameFonts.Role.values();
        while (width > maxWidth && role.ordinal() < roles.length - 1) {
            role = roles[role.ordinal() + 1];
            width = width(text, role);
        }
        if (width <= maxWidth) {
            draw(batch, text, UiMirror.leading(containerX, containerWidth, inset, width), y, role, color, 1f);
            return;
        }
        BitmapFont font = GameFonts.shared().font(role, GameLocale.current());
        float baseScaleX = font.getData().scaleX;
        float baseScaleY = font.getData().scaleY;
        float fit = maxWidth / width;
        font.getData().setScale(baseScaleX * fit, baseScaleY * fit);
        try {
            draw(batch, text, UiMirror.leading(containerX, containerWidth, inset, maxWidth), y, role, color, 1f);
        } finally {
            font.getData().setScale(baseScaleX, baseScaleY);
        }
    }

    /** {@link #drawTrailing} with the alpha a reveal or a disabled state asks for. */
    void drawTrailing(
        SpriteBatch batch,
        String text,
        float containerX,
        float containerWidth,
        float inset,
        float y,
        float scale,
        Color color,
        float alpha
    ) {
        draw(batch, text,
            UiMirror.trailing(containerX, containerWidth, inset, width(text, scale)), y, scale, color, alpha);
    }

    /** The same on the container's trailing edge, where a row's value and its hint go. */
    void drawTrailing(
        SpriteBatch batch,
        String text,
        float containerX,
        float containerWidth,
        float inset,
        float y,
        float scale,
        Color color
    ) {
        draw(batch, text,
            UiMirror.trailing(containerX, containerWidth, inset, width(text, scale)), y, scale, color);
    }

    void drawCentered(
        SpriteBatch batch, String text, float centerX, float y, float scale, Color color
    ) {
        drawCentered(batch, text, centerX, y, scale, color, 1f);
    }

    void drawCentered(
        SpriteBatch batch,
        String text,
        float centerX,
        float y,
        float scale,
        Color color,
        float alpha
    ) {
        GameFonts.Role role = GameFonts.Role.forLegacyScale(scale);
        draw(batch, text, centerX - width(text, role) * 0.5f, y, role, color, alpha);
    }

    void drawRightAligned(
        SpriteBatch batch, String text, float rightX, float y, float scale, Color color
    ) {
        drawRightAligned(batch, text, rightX, y, scale, color, 1f);
    }

    void drawRightAligned(
        SpriteBatch batch,
        String text,
        float rightX,
        float y,
        float scale,
        Color color,
        float alpha
    ) {
        GameFonts.Role role = GameFonts.Role.forLegacyScale(scale);
        draw(batch, text, rightX - width(text, role), y, role, color, alpha);
    }

    float width(String text, float scale) {
        return width(text, GameFonts.Role.forLegacyScale(scale));
    }

    float width(String text, GameFonts.Role role) {
        if (text == null || text.isEmpty()) return 0f;
        layout.setText(GameFonts.shared().font(role, GameLocale.current()), visual(text));
        return layout.width;
    }

    /**
     * The text as the font draws it: untouched. The step stays because every draw and every measure goes
     * through it, and a single funnel is worth more than the call it saves.
     */
    static String visual(String text) {
        return text;
    }

    /** Cap-to-baseline height of the role, used by callers that stack lines. */
    float lineHeight(GameFonts.Role role) {
        return GameFonts.shared().font(role, GameLocale.current()).getLineHeight();
    }

    @Override
    public void close() {
        // Fonts are owned by GameFonts.shared(); nothing to release per renderer.
    }
}
