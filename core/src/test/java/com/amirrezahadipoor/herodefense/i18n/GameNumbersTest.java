package com.amirrezahadipoor.herodefense.i18n;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link GameNumbers}, pinned to exact codepoints.
 *
 * <p>The assertions compare codepoints rather than strings, because the two are indistinguishable on screen and
 * distinguishable nowhere else: {@code "50%"} and {@code "۵۰٪"} look like the same expectation to a reader, and a
 * platform that quietly changed a locale's digits would pass one and fail the other. The values are what Unicode's
 * CLDR data gives for {@code fa-IR}, which is why the class delegates to a {@link java.text.DecimalFormat} built
 * from that locale instead of carrying its own digit table that could disagree with it.
 */
class GameNumbersTest {

    @Test
    void formatsEnglishNumbersTheWayTheHudAlwaysHas() {
        assertEquals("140", GameNumbers.integer(140, GameLanguage.ENGLISH));
        assertEquals("1,234,567", GameNumbers.integer(1234567, GameLanguage.ENGLISH));
        assertEquals("0", GameNumbers.integer(0, GameLanguage.ENGLISH));
        assertEquals("-1,234", GameNumbers.integer(-1234, GameLanguage.ENGLISH));
        assertEquals("50%", GameNumbers.percent(50, GameLanguage.ENGLISH));
        assertEquals("+5", GameNumbers.signed(5, GameLanguage.ENGLISH));
        assertEquals("-5", GameNumbers.signed(-5, GameLanguage.ENGLISH));
    }

    @Test
    void formatsPersianNumbersWithTheDigitsAndSeparatorsIranUses() {
        // ۱۴۰ -- the Extended Arabic-Indic digits of U+06F0..U+06F9, not the Arabic-Indic ١٤٠ of U+0660..U+0669
        // that an Arabic locale would use. Getting this wrong is the difference between a Persian screen and an
        // Arabic one, and nothing about the shape of the digits tells a reviewer which was chosen.
        assertEquals("U+06F1 U+06F4 U+06F0", codepoints(GameNumbers.integer(140, GameLanguage.PERSIAN)));
        assertEquals("U+06F1 U+066C U+06F2 U+06F3 U+06F4 U+066C U+06F5 U+06F6 U+06F7",
            codepoints(GameNumbers.integer(1234567, GameLanguage.PERSIAN)),
            "the thousands separator is U+066C ARABIC THOUSANDS SEPARATOR, not an ASCII comma");
        assertEquals("U+06F0", codepoints(GameNumbers.integer(0, GameLanguage.PERSIAN)));
        assertEquals("U+06F5 U+06F0 U+066A", codepoints(GameNumbers.percent(50, GameLanguage.PERSIAN)),
            "and the percent sign is U+066A ARABIC PERCENT SIGN, not U+0025");
    }

    @Test
    void signsAPersianGainWithThePlusTheBidiPassWillPlace() {
        assertEquals("U+002B U+06F5", codepoints(GameNumbers.signed(5, GameLanguage.PERSIAN)),
            "the plus is written first and the bidirectional pass puts it where it belongs");
        // Two things here are CLDR's doing and neither is obvious from the screen. The sign is U+2212 MINUS SIGN
        // rather than a hyphen, so the font has to carry that glyph. And it is preceded by U+200E LEFT-TO-RIGHT
        // MARK, which is how a right-to-left locale keeps a negative sign on the correct side of its number once
        // the bidirectional pass has reordered the run -- the shaper resolves U+200E as a strong left-to-right
        // character, which is exactly what puts the minus outside the digits.
        assertEquals("U+200E U+2212 U+06F1 U+066C U+06F2 U+06F3 U+06F4",
            codepoints(GameNumbers.signed(-1234, GameLanguage.PERSIAN)));
        assertEquals("U+200E U+2212 U+06F1 U+066C U+06F2 U+06F3 U+06F4",
            codepoints(GameNumbers.integer(-1234, GameLanguage.PERSIAN)),
            "and the mark comes from the formatter, not from the sign helper");
    }

    @Test
    void convertsDigitsInTextItDidNotFormat() {
        assertEquals("1.13.1", GameNumbers.digits("1.13.1", GameLanguage.ENGLISH), "English passes through");
        assertEquals("U+06F1 U+002E U+06F1 U+06F3 U+002E U+06F1",
            codepoints(GameNumbers.digits("1.13.1", GameLanguage.PERSIAN)));
        assertEquals("v2", GameNumbers.digits("v2", GameLanguage.ENGLISH));
        assertEquals("", GameNumbers.digits("", GameLanguage.PERSIAN));
        assertNull(GameNumbers.digits(null, GameLanguage.PERSIAN));
    }

    @Test
    void compactNumberSpeaksEachLanguagesOwnScript() {
        // English keeps the exact shape the combat pop-ups have always drawn.
        assertEquals("999", GameNumbers.compact(999, GameLanguage.ENGLISH));
        assertEquals("1.2k", GameNumbers.compact(1_240, GameLanguage.ENGLISH));
        assertEquals("120k", GameNumbers.compact(120_400, GameLanguage.ENGLISH));
        assertEquals("1", GameNumbers.compact(0, GameLanguage.ENGLISH), "a hit is never worth zero");
        // Persian: Extended Arabic-Indic digits, the U+066B decimal separator CLDR uses for fa-IR, «ه» suffix.
        assertEquals("U+06F9 U+06F9 U+06F9", codepoints(GameNumbers.compact(999, GameLanguage.PERSIAN)));
        assertEquals("U+06F1 U+066B U+06F2 U+0647", codepoints(GameNumbers.compact(1_240, GameLanguage.PERSIAN)));
        assertEquals("U+06F1 U+06F2 U+06F0 U+0647", codepoints(GameNumbers.compact(120_400, GameLanguage.PERSIAN)));
    }

    /** The string as codepoints, which is the only honest way to assert what a Persian number is made of. */
    private static String codepoints(String text) {
        StringBuilder out = new StringBuilder();
        text.codePoints().forEach(codepoint -> {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(String.format("U+%04X", codepoint));
        });
        return out.toString();
    }
}
