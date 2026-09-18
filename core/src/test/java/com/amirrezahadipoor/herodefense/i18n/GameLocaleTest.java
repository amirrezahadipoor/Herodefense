package com.amirrezahadipoor.herodefense.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link GameLocale}: what a call site gets when it asks for a string or a number without saying which language
 * it wants.
 *
 * <p>The value is process-wide and the tests that read it run in the same JVM as the ones that set it, so every
 * test here leaves it as it found it. That is also why the class has no "reset to the device language" method:
 * the one thing that sets it is the settings layer, and a second way to set it would be a second way to be wrong.
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
    void speaksPersianOnceItIsAskedTo() {
        GameLocale.use(GameLanguage.PERSIAN);
        assertTrue(GameLocale.rightToLeft());
        assertEquals("بازی جدید", GameLocale.text(MenuStrings.NEW_GAME));
        assertEquals("۱٬۲۳۴", GameLocale.number(1234));
        assertEquals("۵۰٪", GameLocale.percent(50));
        assertEquals("+۵", GameLocale.signed(5));
    }

    @Test
    void fillsAnEntrysArgumentsInTheLanguageThatIsSpeaking() {
        assertEquals("Tier 3 | Peak 175 | 42 HW",
            GameLocale.text(MenuStrings.PROGRESS_SUMMARY, "3", "175", "42"));
        GameLocale.use(GameLanguage.PERSIAN);
        assertEquals("ردهٔ ۳ | اوج ۱۷۵ | ۴۲ چوب دل",
            GameLocale.text(MenuStrings.PROGRESS_SUMMARY,
                GameLocale.number(3), GameLocale.number(175), GameLocale.number(42)),
            "the caller formats the numbers, so the digits in the sentence are the sentence's language");
    }

    @Test
    void ignoresAnAbsentLanguageRatherThanDefaultingBehindTheCallersBack() {
        GameLocale.use(GameLanguage.PERSIAN);
        GameLocale.use(null);
        assertEquals(GameLanguage.PERSIAN, GameLocale.current(),
            "a null is a caller bug, and silently becoming English would hide it behind a screen of the wrong"
                + " language; the settings layer resolves codes through GameLanguage.fromCode, which is where an"
                + " unknown value becomes a decided one");
    }
}
