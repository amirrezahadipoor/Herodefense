package com.amirrezahadipoor.herodefense.i18n;

/**
 * The language the game is currently speaking: English, always.
 *
 * <p>Until 2026-09-23 this held a choice between English and Persian (roadmap R7.3). The owner deleted the
 * Persian translation outright, so the choice is gone but the holder stays: a screen draws a dozen strings a
 * frame and none of them is in a position to be handed a language, and {@code GameLocale.text(COINS)} still
 * reads as one expression at the call site.
 *
 * <p>This is the same shape as {@code GameFonts.shared()} and for the same reason: the game runs entirely on the
 * libGDX render thread, the value is written from that thread, and the field is {@code volatile} so a test or a
 * future background loader sees a consistent one rather than a torn read.
 *
 * <p>The convenience methods exist so a call site reads as one expression. {@code GameLocale.text(COINS)} is the
 * string; {@code GameLocale.number(coins)} is the number in the game's digits.
 */
public final class GameLocale {

    private static volatile GameLanguage current = GameLanguage.ENGLISH;

    private GameLocale() {
    }

    /** The language in force: English. Never null. */
    public static GameLanguage current() {
        return current;
    }

    /** Whether the screen mirrors and the text runs right to left: always false. */
    public static boolean rightToLeft() {
        return current.rightToLeft();
    }

    /**
     * Switches the language. A null is ignored rather than defaulted, because the only caller that could pass one
     * has a bug, and silently re-resolving would hide it behind a screen nobody asked for.
     */
    public static void use(GameLanguage language) {
        if (language != null) {
            current = language;
        }
    }

    /** The entry's text. */
    public static String text(Translated entry) {
        return entry.text();
    }

    /** The entry's text with its arguments filled in. */
    public static String text(Translated entry, String... args) {
        return entry.text(args);
    }

    /** {@code value} grouped: 1234 is "1,234". */
    public static String number(long value) {
        return GameNumbers.integer(value);
    }

    /** {@code value} in the short combat shape: "1.2k" once it passes a thousand. */
    public static String compact(long value) {
        return GameNumbers.compact(value);
    }

    /** {@code value} as a whole percentage: 50 is "50%". */
    public static String percent(long value) {
        return GameNumbers.percent(value);
    }

    /** {@code value} with an explicit sign, for the rows that show a gain or a cost. */
    public static String signed(long value) {
        return GameNumbers.signed(value);
    }
}
