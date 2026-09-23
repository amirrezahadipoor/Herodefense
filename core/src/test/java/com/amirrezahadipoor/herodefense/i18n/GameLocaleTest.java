package com.amirrezahadipoor.herodefense.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link GameLocale}: what a call site gets when it asks for a string or a number.
 *
 * <p>The value is process-wide and the tests that read it run in the same JVM as the ones that set it, so every
 * test here leaves it as it found it. Until 2026-09-23 this held a choice of two languages; the owner deleted
 * the Persian translation outright, so the holder stays but the choice is gone.
 */
class GameLocaleTest {

    @AfterEach
    void leaveTheLanguageAsItWasFound() {
        GameLocale.use(GameLanguage.ENGLISH);
    }

    @Test
    void startsInTheLanguageTheGameWasWrittenIn() {
        assertEquals(GameLanguage.ENGLISH, GameLocale.current());
        assertFalse(GameLocale.rightToLeft());
        assertEquals("NEW GAME", GameLocale.text(MenuStrings.NEW_GAME));
        assertEquals("1,234", GameLocale.number(1234));
        assertEquals("50%", GameLocale.percent(50));
        assertEquals("+5", GameLocale.signed(5));
    }

    @Test
    void fillsAnEntrysArguments() {
        assertEquals("Tier 3 | Peak 175 | 42 HW",
            GameLocale.text(MenuStrings.PROGRESS_SUMMARY, "3", "175", "42"));
    }

    @Test
    void ignoresAnAbsentLanguageRatherThanDefaultingBehindTheCallersBack() {
        GameLocale.use(GameLanguage.ENGLISH);
        GameLocale.use(null);
        assertEquals(GameLanguage.ENGLISH, GameLocale.current(),
            "a null is a caller bug, and silently re-resolving would hide it behind a screen nobody asked for");
    }
}
