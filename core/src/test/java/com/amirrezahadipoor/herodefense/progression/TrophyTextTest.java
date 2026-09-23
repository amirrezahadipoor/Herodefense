package com.amirrezahadipoor.herodefense.progression;

import com.amirrezahadipoor.herodefense.i18n.TrophyStrings;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The trophy shelf: every row has a name and a line, none of them is blank, both halves are English, and the
 * twelve that shipped first still say exactly what they always said.
 */
class TrophyTextTest {

    private static final String ARABIC_SCRIPT = "[\\u0600-\\u06FF]";

    @Test
    void everyTrophyHasBothHalvesInEnglish() {
        for (Trophy trophy : Trophy.values()) {
            for (String text : new String[] {
                TrophyText.title(trophy), TrophyText.hint(trophy)
            }) {
                assertFalse(text.isBlank(), trophy.id() + " has a blank line on the shelf");
                assertFalse(text.matches(".*" + ARABIC_SCRIPT + ".*"),
                    trophy.id() + " carries non-English script: " + text);
            }
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
    void everyTitleIsDistinct() {
        Set<String> titles = new HashSet<>();
        for (Trophy trophy : Trophy.values()) {
            assertTrue(titles.add(TrophyText.title(trophy)), "duplicate English title: " + trophy.id());
        }
    }
}
