package com.amirrezahadipoor.herodefense.i18n;

import java.util.Locale;

/**
 * The language the game ships in: English, and only English.
 *
 * <p>Until 2026-09-23 this was a two-language enum (English plus Persian) under roadmap R7.3. The
 * owner then deleted the Persian translation outright -- one language, one voice, zero duplication --
 * so what remains is the anchor the rest of the pipeline reads from: the locale English numbers are
 * formatted with, and the direction the layout runs in. It stays an enum rather than collapsing to a
 * constant so every call site that was written against a language keeps reading the same way.
 */
public enum GameLanguage {

    /** The language the game was written in, and the only one it speaks. */
    ENGLISH("en", Locale.US, false);

    private final String code;
    private final Locale locale;
    private final boolean rightToLeft;

    GameLanguage(String code, Locale locale, boolean rightToLeft) {
        this.code = code;
        this.locale = locale;
        this.rightToLeft = rightToLeft;
    }

    /** The persisted code, {@code "en"}. */
    public String code() {
        return code;
    }

    /** The locale whose symbols and digits this language's numbers are formatted with. */
    public Locale locale() {
        return locale;
    }

    /** Whether text runs right to left and the layout mirrors: always false. */
    public boolean rightToLeft() {
        return rightToLeft;
    }
}
