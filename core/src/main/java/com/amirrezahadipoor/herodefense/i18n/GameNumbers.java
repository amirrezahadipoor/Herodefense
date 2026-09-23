package com.amirrezahadipoor.herodefense.i18n;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Numbers as the game writes them: US English digits, separators and signs.
 *
 * <p>Until 2026-09-23 this class formatted per language (English plus Persian) under roadmap R7.3. The owner
 * deleted the Persian translation outright, so the locale is fixed and the language parameters went with it --
 * 1234567 is "1,234,567" everywhere, and there is no second script to keep in sync.
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
     * {@code value} grouped: 1234567 is "1,234,567".
     */
    public static String integer(long value) {
        return new DecimalFormat(INTEGER_PATTERN, symbols()).format(value);
    }

    /**
     * {@code value} as a whole percentage: 50 is "50%".
     */
    public static String percent(long value) {
        return integer(value) + symbols().getPercent();
    }

    /**
     * {@code value} with an explicit plus sign, for the rows that show a gain: "+5" and "-5".
     */
    public static String signed(long value) {
        return value < 0 ? integer(value) : "+" + integer(value);
    }

    /**
     * The short combat number: whole below a thousand, then "1.2k"-style so a late-run hit stays narrow on
     * screen. The shape the combat pop-ups have always drawn.
     */
    public static String compact(long value) {
        long rounded = Math.max(1L, value);
        if (rounded < 1_000L) {
            return Long.toString(rounded);
        }
        if (rounded < 100_000L) {
            return String.format(Locale.ROOT, "%.1f", rounded / 1_000f) + "k";
        }
        return (rounded / 1_000L) + "k";
    }

    private static DecimalFormatSymbols symbols() {
        return DecimalFormatSymbols.getInstance(Locale.US);
    }
}
