package com.amirrezahadipoor.herodefense.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link GameLanguage}: the codes that are persisted, the direction each one implies, and what an unknown or
 * absent value resolves to.
 *
 * <p>The fallbacks matter more than the happy path. A save written by a build that shipped three languages, or a
 * preferences file a player edited, has to produce a readable game rather than a crash on the first frame.
 */
class GameLanguageTest {

    @Test
    void persistsTheCodeItReads() {
        assertEquals(GameLanguage.ENGLISH, GameLanguage.fromCode("en"));
        assertEquals(GameLanguage.PERSIAN, GameLanguage.fromCode("fa"));
        assertEquals("en", GameLanguage.ENGLISH.code());
        assertEquals("fa", GameLanguage.PERSIAN.code());
    }

    @Test
    void fallsBackToEnglishForAnythingItDoesNotKnow() {
        assertEquals(GameLanguage.ENGLISH, GameLanguage.fromCode(null));
        assertEquals(GameLanguage.ENGLISH, GameLanguage.fromCode(""));
        assertEquals(GameLanguage.ENGLISH, GameLanguage.fromCode("de"));
        assertEquals(GameLanguage.ENGLISH, GameLanguage.fromCode("FA"), "the code is matched exactly, not folded");
        assertEquals(GameLanguage.ENGLISH, GameLanguage.fromCode("fa-IR"),
            "a locale tag is not a language code; the settings layer stores the code, not the tag");
    }

    @Test
    void knowsWhichLanguageRunsRightToLeft() {
        assertFalse(GameLanguage.ENGLISH.rightToLeft());
        assertTrue(GameLanguage.PERSIAN.rightToLeft());
        assertEquals(1, countOfRightToLeftLanguages(), "one of the two shipped languages mirrors its layout");
    }

    @Test
    void startsInTheLanguageTheDeviceIsSetTo() {
        assertEquals(GameLanguage.PERSIAN, GameLanguage.forSystemLocale(Locale.forLanguageTag("fa-IR")));
        assertEquals(GameLanguage.PERSIAN, GameLanguage.forSystemLocale(new Locale("fa")));
        assertEquals(GameLanguage.PERSIAN, GameLanguage.forSystemLocale(Locale.forLanguageTag("prs-AF")),
            "Dari is written in the same script with the same digits");
        assertEquals(GameLanguage.ENGLISH, GameLanguage.forSystemLocale(Locale.forLanguageTag("ar-EG")),
            "an Arabic locale is not a Persian one, and a Persian translation would be the wrong language");
        assertEquals(GameLanguage.ENGLISH, GameLanguage.forSystemLocale(Locale.GERMANY));
        assertEquals(GameLanguage.ENGLISH, GameLanguage.forSystemLocale(null));
    }

    @Test
    void cyclesForTheSettingsRow() {
        assertEquals(GameLanguage.PERSIAN, GameLanguage.ENGLISH.next());
        assertEquals(GameLanguage.ENGLISH, GameLanguage.PERSIAN.next());
        assertEquals(GameLanguage.values().length, cycleLength(), "tapping the row returns to where it started");
    }

    @Test
    void carriesTheLocaleItsNumbersAreFormattedFrom() {
        assertEquals("fa-IR", GameLanguage.PERSIAN.locale().toLanguageTag(),
            "fa-IR rather than bare fa is what selects the Persian digits and the Arabic thousands separator");
        assertEquals(Locale.US, GameLanguage.ENGLISH.locale());
    }

    private static long countOfRightToLeftLanguages() {
        return java.util.Arrays.stream(GameLanguage.values()).filter(GameLanguage::rightToLeft).count();
    }

    private static int cycleLength() {
        GameLanguage start = GameLanguage.ENGLISH;
        GameLanguage current = start.next();
        int steps = 1;
        while (current != start) {
            current = current.next();
            steps++;
        }
        return steps;
    }
}
