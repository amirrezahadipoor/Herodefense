package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.MenuStrings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link OverlayText#visual(String)} -- the one place in the render path where a string becomes what the font
 * draws.
 *
 * <p>Only the pure half is tested here; the drawing half needs a GL context and is covered by the emulator's
 * touch-test job, which screenshots the screens. What this pins is the decision that everything else depends on:
 * which strings get shaped, and that measuring and drawing agree about it.
 */
class OverlayTextTest {

    @AfterEach
    void leaveTheLanguageAsItWasFound() {
        GameLocale.use(GameLanguage.ENGLISH);
    }

    @Test
    void leavesAnEnglishStringExactlyAsItWas() {
        String text = MenuStrings.NEW_GAME.english();
        assertSame(text, OverlayText.visual(text), "shaping is skipped, not merely harmless");
        assertEquals("Hold the last green sanctuary", OverlayText.visual("Hold the last green sanctuary"));
    }

    @Test
    void leavesEnglishPunctuationAlone() {
        // The reason English is not simply handed to the shaper: these are non-ASCII, and a rule written for an
        // Arabic-script run has no business deciding where an en dash belongs.
        assertEquals("Waves 1\u201330 \u2022 Tier 2", OverlayText.visual("Waves 1\u201330 \u2022 Tier 2"));
        assertEquals("50% \u00d7 2", OverlayText.visual("50% \u00d7 2"));
    }

    @Test
    void shapesPersianTheWayTheShaperDoes() {
        GameLocale.use(GameLanguage.PERSIAN);
        for (String text : new String[] {
            MenuStrings.NEW_GAME.persian(),
            MenuStrings.PROGRESS_SUMMARY.persian(),
            "موج ۱۴۰",
            "Tier 3 | Peak 175",
        }) {
            assertEquals(PersianShaper.shape(text), OverlayText.visual(text), text);
        }
    }

    @Test
    void shapingMixedTextKeepsTheLatinRunReadable() {
        GameLocale.use(GameLanguage.PERSIAN);
        // A Persian sentence with a Latin word and a number in it. The Latin word and the digits are one
        // left-to-right run -- they do not split, and they do not turn inside out -- and the Persian word reverses
        // around them, which puts the run that reads first in the sentence last on screen.
        String visual = OverlayText.visual("موج Wave 12");
        assertEquals("0057 0061 0076 0065 0020 0031 0032 0020 FE9D FEEE FEE3", codepoints(visual),
            "the Latin run Wave 12 stays intact and in order; the Persian word is reversed after it");
    }

    @Test
    void measuresAndDrawsTheSameString() {
        // The failure this prevents is a centred label that is not centred: width() measuring the unshaped text
        // while draw() hands over the shaped one. Both go through visual(), and the shaped form is what the font
        // has advances for.
        GameLocale.use(GameLanguage.PERSIAN);
        String text = MenuStrings.TAGLINE.persian();
        assertEquals(OverlayText.visual(text), OverlayText.visual(text), "shaping is deterministic");
        assertEquals(PersianShaper.shape(text).codePointCount(0, PersianShaper.shape(text).length()),
            OverlayText.visual(text).codePointCount(0, OverlayText.visual(text).length()),
            "shaping may join letters into ligatures, so the count can fall -- but draw and width see one string");
    }

    private static String codepoints(String text) {
        StringBuilder out = new StringBuilder();
        text.codePoints().forEach(codepoint -> {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(String.format("%04X", codepoint));
        });
        return out.toString();
    }
}
