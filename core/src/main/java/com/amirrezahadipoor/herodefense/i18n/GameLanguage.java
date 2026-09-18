package com.amirrezahadipoor.herodefense.i18n;

import java.util.Locale;

/**
 * The languages the game ships in, and the two facts the rest of the pipeline needs from each one.
 *
 * <p>Roadmap R7.3. The game's market is Iran and until this existed it was English-only in a left-to-right
 * layout, which the Phase 97 re-audit scored as the single largest deduction in the whole rubric: not a polish
 * problem, but a question of who can read the game at all.
 *
 * <p>Two languages, not a framework. There is no resource bundle loading, no plural-rule engine and no
 * translation memory here, because there are two locales and one of them is the one the game is written in.
 * What each carries is the three things a caller actually asks:
 * <ul>
 *   <li>{@link #rightToLeft()} -- whether the layout mirrors and the text runs right to left;</li>
 *   <li>{@link #locale()} -- the locale whose number symbols {@link GameNumbers} formats with, so digits and
 *       separators come from Unicode's data for that market rather than from a table here;</li>
 *   <li>{@link #code()} -- the two-letter code that is persisted, so a save written by one build means the same
 *       thing to the next one.</li>
 * </ul>
 *
 * <p>An unknown or absent code resolves to English rather than throwing: a hand-edited preference, or a save
 * written by a build that had more languages than this one, must still produce a readable game.
 */
public enum GameLanguage {

    /** The language the game was written in. */
    ENGLISH("en", Locale.US, false),

    /**
     * Persian as written in Iran. The locale is {@code fa-IR} rather than bare {@code fa} on purpose: it is what
     * selects the Extended Arabic-Indic digits (۰۱۲۳۴۵۶۷۸۹) and the Arabic thousands separator (٬) that Iranian
     * software uses, where a bare {@code fa} leaves the choice to whichever data the platform carries.
     */
    PERSIAN("fa", Locale.forLanguageTag("fa-IR"), true);

    private final String code;
    private final Locale locale;
    private final boolean rightToLeft;

    GameLanguage(String code, Locale locale, boolean rightToLeft) {
        this.code = code;
        this.locale = locale;
        this.rightToLeft = rightToLeft;
    }

    /** The persisted code, {@code "en"} or {@code "fa"}. */
    public String code() {
        return code;
    }

    /** The locale whose symbols and digits this language's numbers are formatted with. */
    public Locale locale() {
        return locale;
    }

    /** Whether text runs right to left and the layout mirrors. */
    public boolean rightToLeft() {
        return rightToLeft;
    }

    /** The language a persisted code names, falling back to English for anything unrecognised or absent. */
    public static GameLanguage fromCode(String code) {
        for (GameLanguage language : values()) {
            if (language.code.equals(code)) {
                return language;
            }
        }
        return ENGLISH;
    }

    /**
     * The language a device is set to, which is what a first run starts in. Persian for {@code fa} and for the
     * two Dari and Afghan Persian tags that Android reports as its own languages; English for everything else,
     * including other Arabic-script locales, because a Persian translation is not an Arabic one and showing the
     * wrong language confidently is worse than showing the one the game was written in.
     */
    public static GameLanguage forSystemLocale(Locale locale) {
        String language = locale == null ? "" : locale.getLanguage();
        if ("fa".equals(language) || "prs".equals(language) || "ps".equals(language)) {
            return PERSIAN;
        }
        return ENGLISH;
    }

    /** The next language in the list, wrapping -- how the settings row cycles when it is tapped. */
    public GameLanguage next() {
        GameLanguage[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
