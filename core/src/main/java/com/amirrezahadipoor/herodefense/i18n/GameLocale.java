package com.amirrezahadipoor.herodefense.i18n;

/**
 * The language the game is currently speaking.
 *
 * <p>Roadmap R7.3. A screen draws a dozen strings a frame and none of them is in a position to be handed a
 * language: the call chain from {@code HeroDefenseGame} down to a label that says "COINS" passes through
 * renderers, layout helpers and icon atlases that have no opinion about it. So the choice lives here, set once
 * when settings are loaded and again when the player changes it, and read at the point a string or a number
 * becomes pixels.
 *
 * <p>This is the same shape as {@code GameFonts.shared()} and for the same reason: the game runs entirely on the
 * libGDX render thread, the value is written from that thread, and the field is {@code volatile} so a test or a
 * future background loader sees a consistent one rather than a torn read. It is not a lock and does not pretend
 * to be; there is nothing to protect, because a language change takes effect on the next frame.
 *
 * <p>The convenience methods exist so a call site reads as one expression. {@code GameLocale.text(COINS)} is the
 * string in the current language; {@code GameLocale.number(coins)} is the number in the current language's digits.
 * Neither takes a language parameter, because a screen that had to pass one would eventually pass the wrong one.
 */
public final class GameLocale {

    private static volatile GameLanguage current = GameLanguage.ENGLISH;

    private GameLocale() {
    }

    /** The language in force. Never null; it starts as English and only ever becomes another shipped language. */
    public static GameLanguage current() {
        return current;
    }

    /** Whether the screen mirrors and the text runs right to left. */
    public static boolean rightToLeft() {
        return current.rightToLeft();
    }

    /**
     * Switches the language. A null is ignored rather than defaulted, because the only caller that could pass one
     * is reading a setting that has already been resolved by {@link GameLanguage#fromCode(String)}.
     */
    public static void use(GameLanguage language) {
        if (language != null) {
            current = language;
        }
    }

    /** The entry's text in the current language. */
    public static String text(Translated entry) {
        return entry.text(current);
    }

    /** The entry's text in the current language with its arguments filled in. */
    public static String text(Translated entry, String... args) {
        return entry.text(current, args);
    }

    /** {@code value} in the current language's digits and separators. */
    public static String number(long value) {
        return GameNumbers.integer(value, current);
    }

    /** {@code value} as a whole percentage, with the current language's percent sign. */
    public static String percent(long value) {
        return GameNumbers.percent(value, current);
    }

    /** {@code value} with an explicit sign, for the rows that show a gain or a cost. */
    public static String signed(long value) {
        return GameNumbers.signed(value, current);
    }
}
