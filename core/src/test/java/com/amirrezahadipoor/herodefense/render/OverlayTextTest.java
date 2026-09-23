package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.MenuStrings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link OverlayText#visual(String)} -- the one place in the render path where a string becomes what the font
 * draws.
 *
 * <p>Only the pure half is tested here; the drawing half needs a GL context and is covered by the emulator's
 * touch-test job, which screenshots the screens. Until 2026-09-23 this step shaped and reordered the run for a
 * right-to-left language; the owner deleted that translation outright, so what this pins now is that the step is
 * the identity: measuring and drawing see the string the caller passed, byte for byte.
 */
class OverlayTextTest {

    @AfterEach
    void leaveTheLanguageAsItWasFound() {
        GameLocale.use(GameLanguage.ENGLISH);
    }

    @Test
    void leavesAStringExactlyAsItWas() {
        String text = MenuStrings.NEW_GAME.english();
        assertSame(text, OverlayText.visual(text), "the step is the identity, not merely harmless");
        assertEquals("Hold the last green sanctuary", OverlayText.visual("Hold the last green sanctuary"));
    }

    @Test
    void leavesPunctuationAlone() {
        assertEquals("Waves 1\u201330 \u2022 Tier 2", OverlayText.visual("Waves 1\u201330 \u2022 Tier 2"));
        assertEquals("50% \u00d7 2", OverlayText.visual("50% \u00d7 2"));
    }

    @Test
    void passesNullAndEmptyThrough() {
        assertNull(OverlayText.visual(null));
        assertEquals("", OverlayText.visual(""));
    }

    @Test
    void measuresAndDrawsTheSameString() {
        // Both width() and draw() go through visual(), so the string that is measured is the string that is
        // drawn -- a centred label cannot be centred on one width and drawn at another.
        String text = MenuStrings.TAGLINE.english();
        assertSame(OverlayText.visual(text), OverlayText.visual(text), "the step is deterministic");
    }
}
