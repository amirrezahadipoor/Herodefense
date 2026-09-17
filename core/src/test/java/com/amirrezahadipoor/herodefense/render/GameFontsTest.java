package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameStrings;
import com.amirrezahadipoor.herodefense.i18n.Translated;
import org.junit.jupiter.api.Test;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The committed faces carry every glyph the string tables can ask them for (roadmap R7.3).
 *
 * <p>This is the gate that a shaping bug cannot hide behind. A shaper that produces a codepoint the font does not
 * have does not fail a test and does not throw: it draws a box, or nothing, and the only person who notices is the
 * player. So the check is made against the real bytes of the real files -- each face is parsed here with
 * {@link Font#createFont} and asked, codepoint by codepoint, whether it can draw it.
 *
 * <p>Control characters are excluded, and only they. {@code FreeTypeFontGenerator.DEFAULT_CHARS} -- which the
 * English atlas has always been built from -- carries U+007F through U+009F, a block of C1 controls no font has a
 * glyph for and no screen ever draws; FreeType skips them. Everything else in the set has to be drawable, which
 * includes the format characters U+200C, U+200D and U+200E that Persian shaping and CLDR's number formatting both
 * rely on.
 *
 * <p>Three things are asserted, from the strictest outward:
 * <ol>
 *   <li>every table entry, in the language it is written in, is drawable by the face that language uses;</li>
 *   <li>every table entry's <em>shaped</em> form -- the presentation-form codepoints
 *       {@link PersianShaper} will emit at draw time -- is drawable by Vazirmatn;</li>
 *   <li>{@link GameFonts#PERSIAN_CHARACTERS}, the set the atlas is actually rasterised with, contains all of it
 *       and is drawable too, so a glyph cannot be missing from the atlas while present in the file.</li>
 * </ol>
 */
class GameFontsTest {

    private static final Path REPOSITORY = Path.of("..").normalize();

    private static final Path FONTS = REPOSITORY.resolve("android/assets/fonts");

    @Test
    void everyShippedFaceIsPresentWithItsLicence() {
        for (String name : List.of("Nunito-Bold.ttf", "Nunito-ExtraBold.ttf", "NUNITO-OFL.txt",
            "Vazirmatn-Bold.ttf", "Vazirmatn-ExtraBold.ttf", "VAZIRMATN-OFL.txt")) {
            assertTrue(Files.exists(FONTS.resolve(name)), "missing " + FONTS.resolve(name));
        }
        assertTrue(read(FONTS.resolve("VAZIRMATN-OFL.txt")).contains("SIL OPEN FONT LICENSE"),
            "the Persian face ships under the OFL and the licence text has to ship with it");
        assertTrue(read(FONTS.resolve("VAZIRMATN-OFL.txt")).contains("Vazirmatn"),
            "and it has to name the family it covers");
    }

    @Test
    void thePersianFaceCarriesEveryGlyphItsTableCanAskFor() {
        Font bold = load("Vazirmatn-Bold.ttf");
        Font extraBold = load("Vazirmatn-ExtraBold.ttf");
        List<String> problems = new ArrayList<>();

        for (Translated entry : GameStrings.all()) {
            String persian = entry.persian();
            // As written: the Arabic block, which a screen that ever drew an unshaped string would need.
            collectMissing(bold, extraBold, persian, name(entry), problems);
            // As shaped: the presentation forms the shaper actually emits, which is what the font is asked for.
            collectMissing(bold, extraBold, PersianShaper.shape(persian), name(entry) + " (shaped)", problems);
        }

        assertTrue(problems.isEmpty(),
            () -> problems.size() + " glyphs the Persian tables need are missing from Vazirmatn:%n"
                + String.join(String.format("%n"), problems));
    }

    @Test
    void theEnglishFaceCarriesEveryGlyphItsTableCanAskFor() {
        Font bold = load("Nunito-Bold.ttf");
        Font extraBold = load("Nunito-ExtraBold.ttf");
        List<String> problems = new ArrayList<>();
        for (Translated entry : GameStrings.all()) {
            collectMissing(bold, extraBold, entry.english(), name(entry), problems);
        }
        assertTrue(problems.isEmpty(),
            () -> problems.size() + " glyphs the English tables need are missing from Nunito:%n"
                + String.join(String.format("%n"), problems));
    }

    @Test
    void theRasterisedCharacterSetCoversBothTables() {
        Font bold = load("Vazirmatn-Bold.ttf");
        Font extraBold = load("Vazirmatn-ExtraBold.ttf");
        List<String> problems = new ArrayList<>();
        collectMissing(bold, extraBold, GameFonts.PERSIAN_CHARACTERS, "PERSIAN_CHARACTERS", problems);
        assertTrue(problems.isEmpty(),
            () -> problems.size() + " codepoints of the atlas set are not in the face:%n"
                + String.join(String.format("%n"), problems));

        // The atlas set is derived from the tables, so the check that matters is that nothing a table can emit is
        // outside it -- a string added without its glyphs coming along is the failure this prevents.
        List<String> outside = new ArrayList<>();
        for (Translated entry : GameStrings.all()) {
            for (String text : List.of(entry.persian(), PersianShaper.shape(entry.persian()),
                entry.text(GameLanguage.ENGLISH))) {
                text.codePoints().forEach(codepoint -> {
                    if (GameFonts.PERSIAN_CHARACTERS.codePoints().noneMatch(known -> known == codepoint)) {
                        outside.add(name(entry) + " needs U+" + String.format("%04X", codepoint));
                    }
                });
            }
        }
        assertTrue(outside.isEmpty(),
            () -> outside.size() + " codepoints are not in PERSIAN_CHARACTERS:%n"
                + String.join(String.format("%n"), outside));
    }

    @Test
    void theCharacterSetIsUnduplicatedAndInsideTheBasicPlane() {
        String characters = GameFonts.PERSIAN_CHARACTERS;
        int distinct = (int) characters.codePoints().distinct().count();
        assertEquals(distinct, characters.codePointCount(0, characters.length()),
            "the derived set contains a codepoint twice, which wastes an atlas cell");
        assertEquals(distinct, characters.length(),
            "the derived set holds a supplementary-plane character. FreeTypeFontParameter.characters is a String "
                + "that the generator walks as chars, so a surrogate pair would rasterise as two meaningless "
                + "halves; an emoji in a string table has to be drawn some other way");

        // The atlas is derived from the tables, so it is a superset of the English set by construction and the
        // count is a measurement rather than a threshold: it was 361 codepoints against the English 230 when the
        // five screens were tabled, and it moves when a string is added.
        assertTrue(distinct > GameFonts.CHARACTERS.codePointCount(0, GameFonts.CHARACTERS.length()),
            "the Persian atlas is a superset of the English one");
        characters.codePoints().forEach(codepoint ->
            assertTrue(codepoint < 0x10000, "U+" + String.format("%04X", codepoint) + " is outside the BMP"));
    }

    @Test
    void numbersTheFormatterCanEmitAreInTheAtlas() {
        // GameNumbers does not go through the tables: it formats values at draw time, and three of its outputs are
        // characters no English string ever contains -- U+2212 MINUS SIGN, U+200E LEFT-TO-RIGHT MARK and U+066C
        // ARABIC THOUSANDS SEPARATOR, which CLDR's fa-IR data puts into a negative number.
        Font bold = load("Vazirmatn-Bold.ttf");
        List<String> problems = new ArrayList<>();
        for (long value : new long[] {0, 5, 50, -1234, 1234567, 140}) {
            collectMissing(bold, bold, com.amirrezahadipoor.herodefense.i18n.GameNumbers
                .integer(value, GameLanguage.PERSIAN), "integer(" + value + ")", problems);
            collectMissing(bold, bold, com.amirrezahadipoor.herodefense.i18n.GameNumbers
                .percent(value, GameLanguage.PERSIAN), "percent(" + value + ")", problems);
            collectMissing(bold, bold, com.amirrezahadipoor.herodefense.i18n.GameNumbers
                .signed(value, GameLanguage.PERSIAN), "signed(" + value + ")", problems);
        }
        assertTrue(problems.isEmpty(),
            () -> problems.size() + " glyphs a formatted Persian number needs are missing:%n"
                + String.join(String.format("%n"), problems));
    }

    private static void collectMissing(Font bold, Font heavy, String text, String where, List<String> problems) {
        text.codePoints().filter(codepoint -> !Character.isISOControl(codepoint)).forEach(codepoint -> {
            if (!bold.canDisplay(codepoint)) {
                problems.add(where + ": U+" + String.format("%04X", codepoint) + " is not in the regular face");
            }
            if (!heavy.canDisplay(codepoint)) {
                problems.add(where + ": U+" + String.format("%04X", codepoint) + " is not in the heavy face");
            }
        });
    }

    private static Font load(String fileName) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, FONTS.resolve(fileName).toFile());
        } catch (FontFormatException | IOException e) {
            throw new UncheckedIOException(new IOException("cannot parse " + FONTS.resolve(fileName), e));
        }
    }

    private static String name(Translated entry) {
        return entry.getClass().getSimpleName() + "." + entry.key();
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path, e);
        }
    }
}
