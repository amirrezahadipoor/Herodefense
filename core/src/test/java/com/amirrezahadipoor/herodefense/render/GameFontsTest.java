package com.amirrezahadipoor.herodefense.render;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The committed faces carry every glyph the string tables can ask them for (roadmap R7.3).
 *
 * <p>A missing glyph does not fail a test and does not throw: it draws a box, or nothing, and the only person
 * who notices is the player. So the check is made against the real bytes of the real files -- each face is parsed
 * here with {@link Font#createFont} and asked, codepoint by codepoint, whether it can draw it.
 *
 * <p>Control characters are excluded, and only they. {@code FreeTypeFontGenerator.DEFAULT_CHARS} -- which the
 * atlas has always been built from -- carries U+007F through U+009F, a block of C1 controls no font has a glyph
 * for and no screen ever draws; FreeType skips them. Everything else in the set has to be drawable.
 *
 * <p>Until 2026-09-23 this gate covered two faces per role (Nunito plus Vazirmatn) and the shaped forms the
 * Persian shaper emitted. The owner deleted the Persian translation outright, so one face per role remains and
 * {@link #vazirmatnIsGone} pins the deletion: the files must be absent, not merely unused.
 */
class GameFontsTest {

    private static final Path REPOSITORY = Path.of("..").normalize();

    private static final Path FONTS = REPOSITORY.resolve("android/assets/fonts");

    @Test
    void everyShippedFaceIsPresentWithItsLicence() {
        for (String name : List.of("Nunito-Bold.ttf", "Nunito-ExtraBold.ttf", "NUNITO-OFL.txt")) {
            assertTrue(Files.exists(FONTS.resolve(name)), "missing " + FONTS.resolve(name));
        }
        assertTrue(read(FONTS.resolve("NUNITO-OFL.txt")).contains("SIL OPEN FONT LICENSE"),
            "the face ships under the OFL and the licence text has to ship with it");
        assertTrue(read(FONTS.resolve("NUNITO-OFL.txt")).contains("Nunito"),
            "and it has to name the family it covers");
    }

    @Test
    void vazirmatnIsGone() {
        for (String name : List.of("Vazirmatn-Bold.ttf", "Vazirmatn-ExtraBold.ttf", "VAZIRMATN-OFL.txt")) {
            assertFalse(Files.exists(FONTS.resolve(name)),
                "the Persian face was deleted with the translation and must not come back quietly: "
                    + FONTS.resolve(name));
        }
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
    void theRasterisedCharacterSetCoversTheTables() {
        Font bold = load("Nunito-Bold.ttf");
        Font extraBold = load("Nunito-ExtraBold.ttf");
        List<String> problems = new ArrayList<>();
        collectMissing(bold, extraBold, GameFonts.CHARACTERS, "CHARACTERS", problems);
        assertTrue(problems.isEmpty(),
            () -> problems.size() + " codepoints of the atlas set are not in the face:%n"
                + String.join(String.format("%n"), problems));

        // The atlas set is fixed, so the check that matters is that nothing a table can emit is outside it --
        // a string added without its glyphs in the set is the failure this prevents.
        List<String> outside = new ArrayList<>();
        for (Translated entry : GameStrings.all()) {
            entry.english().codePoints().forEach(codepoint -> {
                if (GameFonts.CHARACTERS.codePoints().noneMatch(known -> known == codepoint)) {
                    outside.add(name(entry) + " needs U+" + String.format("%04X", codepoint));
                }
            });
        }
        assertTrue(outside.isEmpty(),
            () -> outside.size() + " codepoints are not in CHARACTERS:%n"
                + String.join(String.format("%n"), outside));
    }

    @Test
    void theCharacterSetIsUnduplicatedAndInsideTheBasicPlane() {
        String characters = GameFonts.CHARACTERS;
        int distinct = (int) characters.codePoints().distinct().count();
        assertEquals(distinct, characters.codePointCount(0, characters.length()),
            "the set contains a codepoint twice, which wastes an atlas cell");
        assertEquals(distinct, characters.length(),
            "the set holds a supplementary-plane character. FreeTypeFontParameter.characters is a String "
                + "that the generator walks as chars, so a surrogate pair would rasterise as two meaningless "
                + "halves; an emoji in a string table has to be drawn some other way");
        characters.codePoints().forEach(codepoint ->
            assertTrue(codepoint < 0x10000, "U+" + String.format("%04X", codepoint) + " is outside the BMP"));
    }

    @Test
    void numbersTheFormatterCanEmitAreInTheAtlas() {
        // GameNumbers does not go through the tables: it formats values at draw time, so its digits, separators
        // and compact suffix are checked here rather than swept with the entries.
        Font bold = load("Nunito-Bold.ttf");
        List<String> problems = new ArrayList<>();
        for (long value : new long[] {0, 5, 50, -1234, 1234567, 140}) {
            collectMissing(bold, bold, com.amirrezahadipoor.herodefense.i18n.GameNumbers
                .integer(value), "integer(" + value + ")", problems);
            collectMissing(bold, bold, com.amirrezahadipoor.herodefense.i18n.GameNumbers
                .percent(value), "percent(" + value + ")", problems);
            collectMissing(bold, bold, com.amirrezahadipoor.herodefense.i18n.GameNumbers
                .signed(value), "signed(" + value + ")", problems);
            collectMissing(bold, bold, com.amirrezahadipoor.herodefense.i18n.GameNumbers
                .compact(value), "compact(" + value + ")", problems);
        }
        assertTrue(problems.isEmpty(),
            () -> problems.size() + " glyphs a formatted number needs are missing:%n"
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
