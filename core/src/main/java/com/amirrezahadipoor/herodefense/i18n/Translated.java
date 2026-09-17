package com.amirrezahadipoor.herodefense.i18n;

import java.util.Locale;

/**
 * A user-facing string that exists in every language the game ships in.
 *
 * <p>The contract is enforced by the shape of it rather than by a review: each screen's strings are an enum
 * implementing this interface, and the enum's constructor takes both languages, so a string cannot be added
 * with only one of them. That is the answer to roadmap R7.3's "a test that fails on hard-coded or untranslated
 * user-facing strings" for the untranslated half -- there is nothing to forget, because there is no
 * single-argument constructor to forget it with.
 *
 * <p>The other half, hard-coded strings, is enforced on the renderers: {@code TranslationTableTest} reads the
 * sources of every screen whose strings are tabled and fails if one still passes a literal to the text layer.
 */
public interface Translated {

    /**
     * The entry's own name, which is how a failure points at it. An enum constant knows this and a cast does not
     * have to guess it: the tests that sweep the tables report `MenuStrings.NEW_GAME`, not a hash.
     */
    String key();

    /** The English text. */
    String english();

    /** The Persian text. */
    String persian();

    /** The text in {@code language}. */
    default String text(GameLanguage language) {
        return language == GameLanguage.PERSIAN ? persian() : english();
    }

    /**
     * The text in {@code language} with its placeholders filled.
     *
     * <p>Arguments are strings, not objects, so that a number has to have been formatted before it arrives:
     * {@code String.format} under {@link Locale#ROOT} would happily turn an {@code int} into Latin digits inside
     * a Persian sentence. {@link GameNumbers} is how a caller produces the string this method wants.
     *
     * <p>Placeholders are positional ({@code %1$s}) because the two languages do not put a value in the same
     * place in a sentence: "Tier 3 | Peak 175" reads "ردهٔ ۳ | اوج ۱۷۵" and the order is the pattern's business,
     * not the caller's.
     */
    default String text(GameLanguage language, String... args) {
        String pattern = text(language);
        return args.length == 0 ? pattern : String.format(Locale.ROOT, pattern, (Object[]) args);
    }
}
