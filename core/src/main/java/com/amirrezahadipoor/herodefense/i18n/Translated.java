package com.amirrezahadipoor.herodefense.i18n;

import java.util.Locale;

/**
 * A user-facing string in the language the game ships in.
 *
 * <p>The contract is enforced by the shape of it rather than by a review: each screen's strings are an enum
 * implementing this interface, and the enum's constructor takes the text, so a string cannot be added without
 * one. That is the answer to roadmap R7.3's "a test that fails on hard-coded or untranslated user-facing
 * strings" for the untranslated half -- there is nothing to forget, because there is no text-less constructor
 * to forget it with.
 *
 * <p>The other half, hard-coded strings, is enforced on the renderers: {@code TranslationTableTest} reads the
 * sources of every screen whose strings are tabled and fails if one still passes a literal to the text layer.
 *
 * <p>Until 2026-09-23 this interface carried two languages per entry (English plus Persian). The owner deleted
 * the Persian translation outright, so each entry holds the one shipped text and nothing else.
 */
public interface Translated {

    /**
     * The entry's own name, which is how a failure points at it. An enum constant knows this and a cast does not
     * have to guess it: the tests that sweep the tables report `MenuStrings.NEW_GAME`, not a hash.
     */
    String key();

    /** The English text. */
    String english();

    /** The text. */
    default String text() {
        return english();
    }

    /**
     * The text with its placeholders filled.
     *
     * <p>Arguments are strings, not objects, so that a number has to have been formatted before it arrives.
     * {@link GameNumbers} is how a caller produces the string this method wants.
     *
     * <p>Placeholders are positional ({@code %1$s}) so the order stays the pattern's business, not the caller's.
     */
    default String text(String... args) {
        String pattern = text();
        return args.length == 0 ? pattern : String.format(Locale.ROOT, pattern, (Object[]) args);
    }
}
