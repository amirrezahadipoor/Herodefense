package com.amirrezahadipoor.herodefense.progression;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.TrophyStrings;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The trophy shelf in both languages: every row has a name and a line, none of them is blank, the Persian side is
 * Persian, and the twelve that shipped first still say exactly what they always said.
 */
class TrophyTextTest {

    private static final String PERSIAN_SCRIPT = "[\\u0600-\\u06FF]";
    private static final String LATIN = "[A-Za-z]";

    @Test
    void everyTrophyHasBothHalvesInBothLanguages() {
        for (Trophy trophy : Trophy.values()) {
            for (String text : new String[] {
                TrophyText.title(trophy), TrophyText.hint(trophy)
            }) {
                assertFalse(text.isBlank(), trophy.id() + " has a blank line on the shelf");
            }
            GameLocale.use(GameLanguage.PERSIAN);
            String persianTitle = TrophyText.title(trophy);
            String persianHint = TrophyText.hint(trophy);
            assertTrue(persianTitle.matches(".*" + PERSIAN_SCRIPT + ".*"),
                trophy.id() + " has no Persian title: " + persianTitle);
            assertTrue(persianHint.matches(".*" + PERSIAN_SCRIPT + ".*"),
                trophy.id() + " has no Persian hint: " + persianHint);
            assertFalse(persianTitle.matches(".*" + LATIN + ".*"),
                trophy.id() + " shows Latin letters in a Persian title: " + persianTitle);
            assertFalse(persianHint.matches(".*" + LATIN + ".*"),
                trophy.id() + " shows Latin letters in a Persian hint: " + persianHint);
            GameLocale.use(GameLanguage.ENGLISH);
        }
    }

    @Test
    void theTwelveThatShippedFirstStillSayWhatTheySaid() {
        assertEquals("First Vigil", TrophyText.title(Trophy.FIRST_VIGIL));
        assertEquals("Finish a run, however it ends.", TrophyText.hint(Trophy.FIRST_VIGIL));
        assertEquals("Steady Hand", TrophyText.title(Trophy.STEADY_HAND));
        assertEquals("Clear a thousand waves in total.", TrophyText.hint(Trophy.LONG_HOLD));
        assertEquals("The Hollow Answered", TrophyText.title(Trophy.HOLLOW_ANSWERED));
        assertEquals("Meet all eight boss identities at least once.", TrophyText.hint(Trophy.WITNESS));
    }

    @Test
    void theTableHasExactlyTwoEntriesPerTrophyAndNoOrphans() {
        assertEquals(Trophy.values().length * 2, TrophyStrings.values().length);
        Set<String> mapped = new HashSet<>();
        for (Trophy trophy : Trophy.values()) {
            mapped.add(TrophyText.title(trophy));
            mapped.add(TrophyText.hint(trophy));
        }
        assertEquals(Trophy.values().length * 2, mapped.size(),
            "two trophies share a line, so the shelf would read the same twice");
        assertEquals("", TrophyText.title(null));
        assertEquals("", TrophyText.hint(null));
    }

    @Test
    void everyTitleIsDistinctInBothLanguages() {
        Set<String> english = new HashSet<>();
        Set<String> persianTitles = new HashSet<>();
        for (Trophy trophy : Trophy.values()) {
            assertTrue(english.add(TrophyText.title(trophy)), "duplicate English title: " + trophy.id());
            GameLocale.use(GameLanguage.PERSIAN);
            assertTrue(persianTitles.add(TrophyText.title(trophy)),
                "duplicate Persian title: " + trophy.id());
            GameLocale.use(GameLanguage.ENGLISH);
        }
    }
}
