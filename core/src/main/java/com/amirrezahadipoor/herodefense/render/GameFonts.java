package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameNumbers;
import com.amirrezahadipoor.herodefense.i18n.GameStrings;
import com.amirrezahadipoor.herodefense.i18n.Translated;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Rasterises the licensed faces at the panel's real pixel size once per resize -- Nunito for English, Vazirmatn
 * for Persian.
 *
 * <p>Every text site asks for a role (title, heading, body, label) instead of a raw scale, so
 * legibility is decided in one place and stays physically consistent across densities.
 *
 * <p>Two faces, not one, because a bitmap font draws the glyphs it was told to rasterise and nothing else.
 * Vazirmatn is the Persian typeface: its alef, its ye and its kaf are drawn for Persian reading rather than
 * borrowed from an Arabic font, and it carries the whole Presentation Forms range that
 * {@link PersianShaper} produces. Nunito has none of it. Which face a role resolves to is
 * {@link #font(Role, GameLanguage)}, and the glyph set each is rasterised with is derived from the string tables
 * rather than listed by hand -- see {@link #persianCharacters()}.
 */
public final class GameFonts implements AutoCloseable {
    public enum Role {
        DISPLAY(32f, true),
        TITLE(22f, true),
        HEADING(17f, true),
        BODY(14f, false),
        LABEL(12f, true),
        CAPTION(11f, false);

        /** Physical size in Android sp; converted to world units per device so text never shrinks. */
        public final float sp;
        public final boolean heavy;

        Role(float sp, boolean heavy) {
            this.sp = sp;
            this.heavy = heavy;
        }

        /** Maps the legacy free-form scale multiplier onto the nearest typographic role. */
        public static Role forLegacyScale(float scale) {
            if (scale <= 0.72f) return CAPTION;
            if (scale <= 0.92f) return LABEL;
            if (scale <= 1.12f) return BODY;
            if (scale <= 1.55f) return HEADING;
            if (scale <= 2.05f) return TITLE;
            return DISPLAY;
        }
    }

    /** Guard rails in world units so low-density tablets and giant-font phones both stay sane. */
    static final float MIN_WORLD_SIZE = 16f;
    static final float MAX_WORLD_SIZE_FACTOR = 2.6f;
    static final String BOLD_PATH = "fonts/Nunito-Bold.ttf";
    static final String EXTRA_BOLD_PATH = "fonts/Nunito-ExtraBold.ttf";
    static final String PERSIAN_BOLD_PATH = "fonts/Vazirmatn-Bold.ttf";
    static final String PERSIAN_EXTRA_BOLD_PATH = "fonts/Vazirmatn-ExtraBold.ttf";
    static final String CHARACTERS =
        FreeTypeFontGenerator.DEFAULT_CHARS + "\u2013\u2014\u2019\u2022\u2026\u00d7\u00b7";

    /**
     * Everything a Persian screen can put on this font, derived from the string tables at class-load time.
     *
     * <p>{@link GameFontsTest} is the other half of this: it loads both committed faces and asserts that every
     * codepoint here is a glyph they actually have, and that every table entry -- shaped the way
     * {@link PersianShaper} will shape it at draw time -- is drawable. A character missing from the set draws as
     * a space; a character missing from the font draws as a box, and neither is visible in a code review.
     */
    static final String PERSIAN_CHARACTERS = persianCharacters();

    private static GameFonts shared;

    /** The libGDX application whose GL context owns the glyph textures. */
    private final Application owner = Gdx.app;
    private final FreeTypeFontGenerator bold;
    private final FreeTypeFontGenerator extraBold;
    private final FreeTypeFontGenerator persianBold;
    private final FreeTypeFontGenerator persianExtraBold;
    private final BitmapFont[][] fonts =
        new BitmapFont[GameLanguage.values().length][Role.values().length];
    private DisplayMetrics metrics;

    private GameFonts() {
        FreeTypeFontGenerator.setMaxTextureSize(2048);
        bold = new FreeTypeFontGenerator(Gdx.files.internal(BOLD_PATH));
        extraBold = new FreeTypeFontGenerator(Gdx.files.internal(EXTRA_BOLD_PATH));
        persianBold = new FreeTypeFontGenerator(Gdx.files.internal(PERSIAN_BOLD_PATH));
        persianExtraBold = new FreeTypeFontGenerator(Gdx.files.internal(PERSIAN_EXTRA_BOLD_PATH));
        rebuild(new DisplayMetrics(
            Gdx.graphics.getBackBufferWidth(),
            Gdx.graphics.getBackBufferHeight(),
            Gdx.graphics.getDensity()
        ));
    }

    /**
     * One glyph atlas set per libGDX application; renderers share it and never own a font.
     *
     * <p>Glyph textures live in the GL context of the application that rasterised them. When a
     * new {@link Application} starts in the same process (activity recreation, instrumentation
     * launching several activities back to back) the old atlases are dead, so a fresh set is
     * rasterised instead of drawing invisible text from the stale context.
     */
    public static GameFonts shared() {
        if (shared != null && shared.owner != Gdx.app) {
            shared.close();
        }
        if (shared == null) shared = new GameFonts();
        return shared;
    }

    public static boolean hasShared() {
        return shared != null;
    }

    /** Re-rasterises when the back buffer changes so glyphs stay pixel-exact after rotation/resize. */
    public void rebuild(DisplayMetrics newMetrics) {
        if (metrics != null
            && metrics.screenWidth() == newMetrics.screenWidth()
            && metrics.screenHeight() == newMetrics.screenHeight()) {
            return;
        }
        metrics = newMetrics;
        for (GameLanguage language : GameLanguage.values()) {
            for (Role role : Role.values()) {
                BitmapFont previous = fonts[language.ordinal()][role.ordinal()];
                if (previous != null) previous.dispose();
                fonts[language.ordinal()][role.ordinal()] = generate(role, language);
            }
        }
    }

    /** The English face, which is what every call site asked for before the game had two languages. */
    public BitmapFont font(Role role) {
        return font(role, GameLanguage.ENGLISH);
    }

    /** The face for {@code role} in {@code language}: Vazirmatn for Persian, Nunito otherwise. */
    public BitmapFont font(Role role, GameLanguage language) {
        return fonts[language.ordinal()][role.ordinal()];
    }

    public DisplayMetrics metrics() {
        return metrics;
    }

    /** World-unit glyph size for a role on the current panel; pure so tests can lock the contract. */
    static float worldSizeFor(Role role, DisplayMetrics metrics) {
        float natural = metrics.worldUnitsForDp(role.sp);
        float floor = MIN_WORLD_SIZE * role.sp / Role.CAPTION.sp;
        float ceiling = floor * MAX_WORLD_SIZE_FACTOR;
        return Math.max(floor, Math.min(ceiling, natural));
    }

    /**
     * The glyph set the Persian face is rasterised with: the English set, the symbols
     * {@link GameNumbers} can emit for a Persian locale, and every codepoint the string tables can produce --
     * both as written and as {@link PersianShaper} will have shaped them by the time they are drawn.
     *
     * <p>Both, because the two are different sets. A table entry holds the Arabic block (به), the shaper emits
     * Presentation Forms (ﺑﻪ), and a screen that ever drew the unshaped form would need the first set to not be
     * blank boxes. Deriving this from the tables rather than writing down a range is what keeps it honest when a
     * string is added: the new glyphs come along by construction, and {@link GameFontsTest} fails if the face
     * does not have them.
     */
    static String persianCharacters() {
        // Insertion-ordered, so the atlas is built in a stable order and two runs rasterise the same cells.
        Set<Integer> codepoints = new LinkedHashSet<>();
        CHARACTERS.codePoints().forEach(codepoints::add);
        // Persian digits, the thousands separator, the percent sign, the minus sign CLDR puts in front of a
        // negative number, the left-to-right mark that keeps that sign on the correct side of its digits, and the
        // plus GameNumbers writes for a gain.
        "\u06f0\u06f1\u06f2\u06f3\u06f4\u06f5\u06f6\u06f7\u06f8\u06f9\u066c\u066a\u2212\u200e+"
            .codePoints().forEach(codepoints::add);
        for (Translated entry : GameStrings.all()) {
            entry.persian().codePoints().forEach(codepoints::add);
            PersianShaper.shape(entry.persian()).codePoints().forEach(codepoints::add);
        }
        StringBuilder characters = new StringBuilder(codepoints.size());
        codepoints.forEach(characters::appendCodePoint);
        return characters.toString();
    }

    private BitmapFont generate(Role role, GameLanguage language) {
        float worldSize = worldSizeFor(role, metrics);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = metrics.glyphPixelsForWorldUnits(worldSize);
        parameter.characters = language == GameLanguage.PERSIAN ? PERSIAN_CHARACTERS : CHARACTERS;
        parameter.color = Color.WHITE;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.hinting = FreeTypeFontGenerator.Hinting.AutoMedium;
        parameter.kerning = true;
        parameter.incremental = false;
        BitmapFont font = face(language, role.heavy).generateFont(parameter);
        // Glyphs are rasterised in pixels; scale back so one glyph unit equals one world unit.
        font.getData().setScale(1f / metrics.pixelsPerWorldUnit());
        font.setUseIntegerPositions(false);
        return font;
    }

    private FreeTypeFontGenerator face(GameLanguage language, boolean heavy) {
        if (language == GameLanguage.PERSIAN) {
            return heavy ? persianExtraBold : persianBold;
        }
        return heavy ? extraBold : bold;
    }

    @Override
    public void close() {
        for (BitmapFont[] perLanguage : fonts) {
            for (BitmapFont font : perLanguage) if (font != null) font.dispose();
        }
        bold.dispose();
        extraBold.dispose();
        persianBold.dispose();
        persianExtraBold.dispose();
        if (shared == this) shared = null;
    }

    /** Releases the shared set only if this application still owns it; a newer app keeps its own. */
    public static void closeSharedFor(Application application) {
        if (shared != null && shared.owner == application) shared.close();
    }
}
