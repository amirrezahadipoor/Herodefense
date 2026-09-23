package com.amirrezahadipoor.herodefense.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link GameLanguage}: the one shipped language, the code that is persisted for it, the direction it implies,
 * and the locale its numbers are formatted from.
 *
 * <p>Until 2026-09-23 this was two languages and these tests pinned the fallbacks -- unknown codes, the device
 * locale, the settings row's cycle. The owner deleted the Persian translation outright, so what these pin now is
 * the smaller contract that remains: there is exactly one language, and it is English.
 */
class GameLanguageTest {

    @Test
    void shipsOneLanguageAndOnlyOne() {
        assertEquals(1, GameLanguage.values().length);
        assertEquals(GameLanguage.ENGLISH, GameLanguage.values()[0]);
    }

    @Test
    void persistsTheCodeItReads() {
        assertEquals("en", GameLanguage.ENGLISH.code());
    }

    @Test
    void laysOutLeftToRight() {
        assertFalse(GameLanguage.ENGLISH.rightToLeft());
        assertEquals(0, countOfRightToLeftLanguages(), "no shipped language mirrors its layout");
    }

    @Test
    void carriesTheLocaleItsNumbersAreFormattedFrom() {
        assertEquals(Locale.US, GameLanguage.ENGLISH.locale());
    }

    private static long countOfRightToLeftLanguages() {
        return java.util.Arrays.stream(GameLanguage.values()).filter(GameLanguage::rightToLeft).count();
    }
}
