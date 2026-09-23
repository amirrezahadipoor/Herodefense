package com.amirrezahadipoor.herodefense.render;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Measures a run of text the way the game will draw it on the gate emulator: the committed Nunito face for
 * the role, at the world size {@link GameFonts#worldSizeFor} gives that role on {@code api35-1080x2220} at
 * density 2.75, by java.awt's layout of the same TrueType file FreeType rasterises at run time. Hinting differs
 * from FreeType's by a fraction of a glyph, which is why the boxes the layout tests assert against keep a margin.
 */
final class ReferenceTypeMeasure {
    /** The gate emulator of the Android touch workflow: 1080 by 2220 at density 2.75. */
    static final DisplayMetrics REFERENCE = new DisplayMetrics(1080, 2220, 2.75f);

    private static final Path FONTS = Path.of("..").normalize().resolve("android/assets/fonts");
    private static final Map<String, Font> FACES = new HashMap<>();

    private ReferenceTypeMeasure() {
    }

    /** Width in world units of {@code text} at the role's reference size. */
    static float width(String text, GameFonts.Role role) {
        if (text == null || text.isEmpty()) return 0f;
        FontRenderContext context = new FontRenderContext(null, true, true);
        return new TextLayout(text, face(role), context).getAdvance();
    }

    /** The face's line height in world units at the role's reference size: what callers that stack rows step by. */
    static float lineHeight(GameFonts.Role role) {
        FontRenderContext context = new FontRenderContext(null, true, true);
        return face(role).getLineMetrics("Hg", context).getHeight();
    }

    /** Height in world units of a capital H at the role's reference size: how far a caps line reaches below its top. */
    static float capHeight(GameFonts.Role role) {
        FontRenderContext context = new FontRenderContext(null, true, true);
        return (float) face(role).createGlyphVector(context, "H").getVisualBounds().getHeight();
    }

    /** The face the role draws in, at the world size the reference emulator gives the role. */
    static Font face(GameFonts.Role role) {
        String file = role.heavy ? "Nunito-ExtraBold.ttf" : "Nunito-Bold.ttf";
        Font base = FACES.computeIfAbsent(file, name -> {
            try {
                return Font.createFont(Font.TRUETYPE_FONT, FONTS.resolve(name).toFile());
            } catch (IOException | FontFormatException exception) {
                throw new IllegalStateException("cannot load " + name, exception);
            }
        });
        return base.deriveFont(GameFonts.worldSizeFor(role, REFERENCE));
    }
}
