package com.amirrezahadipoor.herodefense.i18n;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Numbers as the player's market writes them (roadmap R7.3).
 *
 * <p>A wave counter that reads "140" to an Iranian player is not a small thing: the digits are not the ones
 * every other number on their phone uses, and neither is the separator in a five-figure coin total. So the
 * symbols come from the platform's locale data for {@link GameLanguage#locale()} -- for {@code fa-IR} that is
 * the Extended Arabic-Indic digits ۰۱۲۳۴۵۶۷۸۹, the Arabic thousands separator ٬ (U+066C) and the percent sign
 * ٪ (U+066A) -- rather than from a table in this file that could disagree with it.
 *
 * <p>{@link GameNumbersTest} pins the exact codepoints, because "the platform's data" is not a number a reviewer
 * can check by reading this. One of them is worth knowing before it surprises anyone: a negative Persian number
 * comes back with U+200E LEFT-TO-RIGHT MARK in front of U+2212 MINUS SIGN. That is CLDR's doing, not ours, and it
 * is what keeps the sign on the correct side of its digits once {@code render/BidiReordering} has reversed the
 * run. It also means the font has to carry three glyphs no English string ever asks for -- U+200E, U+2212 and
 * U+066C -- which is why the coverage gate in {@code GameFontsTest} is derived from these tables rather than from
 * a hand-written list.
 *
 * <p>Nothing here is cached: a {@link DecimalFormat} is not thread-safe and the game formats a handful of
 * numbers per frame on one thread, where constructing the formatter costs less than the guard around sharing it.
 */
public final class GameNumbers {

    /** Grouped integer, the only shape the HUD, menus and summaries use. */
    private static final String INTEGER_PATTERN = "#,##0";

    private GameNumbers() {
    }

    /**
     * {@code value} with this language's digits and thousands separators: 1234567 is "1,234,567" in English and
     * "۱٬۲۳۴٬۵۶۷" in Persian.
     */
    public static String integer(long value, GameLanguage language) {
        return new DecimalFormat(INTEGER_PATTERN, symbols(language)).format(value);
    }

    /**
     * {@code value} as a whole percentage: 50 is "50%" in English and "۵۰٪" in Persian. The sign comes from the
     * locale rather than being appended as an ASCII percent, which is the difference between a Persian sentence
     * that reads correctly and one with a Latin glyph in the middle of it.
     */
    public static String percent(long value, GameLanguage language) {
        return integer(value, language) + symbols(language).getPercent();
    }

    /**
     * {@code value} with an explicit plus sign, for the rows that show a gain: "+5" and "۵+".
     *
     * <p>The plus is written first in both languages and the bidirectional pass puts it where it belongs; that
     * is what {@code render/PersianShaper} is for, and it is why this method does not try to be clever about
     * order itself.
     */
    public static String signed(long value, GameLanguage language) {
        return value < 0 ? integer(value, language) : "+" + integer(value, language);
    }

    /**
     * Every ASCII digit in {@code text} replaced with this language's, leaving everything else alone.
     *
     * <p>This is for the numbers this class did not produce: a version string, an id, a value that arrived
     * already formatted. English passes text through unchanged.
     */
    public static String digits(String text, GameLanguage language) {
        if (text == null || text.isEmpty() || language == GameLanguage.ENGLISH) {
            return text;
        }
        char zero = symbols(language).getZeroDigit();
        StringBuilder out = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            out.append(character >= '0' && character <= '9' ? (char) (zero + character - '0') : character);
        }
        return out.toString();
    }

    /**
     * The short combat number: whole below a thousand, then "1.2k"-style so a late-run hit stays narrow on
     * screen. English keeps the ASCII shape it has always drawn; Persian gets its own digits, its own decimal
     * separator (U+066B, what CLDR uses for fa-IR) and its own suffix — «ه» for هزار — because a compact
     * number whose tail is a Latin letter is exactly the mixed-script word this class exists to prevent.
     */
    public static String compact(long value, GameLanguage language) {
        long rounded = Math.max(1L, value);
        String ascii;
        if (rounded < 1_000L) {
            ascii = Long.toString(rounded);
        } else if (rounded < 100_000L) {
            ascii = String.format(Locale.ROOT, "%.1f", rounded / 1_000f) + suffix(language);
        } else {
            ascii = (rounded / 1_000L) + suffix(language);
        }
        if (language == GameLanguage.ENGLISH) {
            return ascii;
        }
        return digits(ascii, language).replace('.', symbols(language).getDecimalSeparator());
    }

    private static String suffix(GameLanguage language) {
        return language == GameLanguage.PERSIAN ? "ه" : "k";
    }

    private static DecimalFormatSymbols symbols(GameLanguage language) {
        return DecimalFormatSymbols.getInstance(language.locale());
    }
}
