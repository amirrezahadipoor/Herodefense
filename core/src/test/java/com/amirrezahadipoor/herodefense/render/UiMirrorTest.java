package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The mirroring arithmetic (roadmap R7.3).
 *
 * <p>These are the only tests in the repository that can check where a Persian screen puts things, because
 * everything above this line needs a GL context to draw into. What they hold is the property that matters: the
 * same call site puts an element the same distance from the leading edge in both languages, and the two edges
 * swap when the language does.
 */
final class UiMirrorTest {

    private GameLanguage before;

    @BeforeEach
    void rememberTheLanguage() {
        before = GameLocale.current();
    }

    @AfterEach
    void restoreTheLanguage() {
        GameLocale.use(before);
    }

    @Test
    void englishKeepsTheLeftEdgeLeadingAndPersianMovesItToTheRight() {
        GameLocale.use(GameLanguage.ENGLISH);
        assertFalse(UiMirror.mirrored());
        assertEquals(136f, UiMirror.leading(100f, 520f, 36f, 200f), "36f in from the left");
        assertEquals(520f, UiMirror.trailing(100f, 520f, 30f, 70f), "30f in from the right");

        GameLocale.use(GameLanguage.PERSIAN);
        assertTrue(UiMirror.mirrored());
        assertEquals(384f, UiMirror.leading(100f, 520f, 36f, 200f), "36f in from the right");
        assertEquals(130f, UiMirror.trailing(100f, 520f, 30f, 70f), "30f in from the left");
    }

    @Test
    void anElementStaysTheSameDistanceFromItsEdgeInBothLanguages() {
        float inset = 36f;
        float width = 200f;
        GameLocale.use(GameLanguage.ENGLISH);
        float english = UiMirror.leading(100f, 520f, inset, width);
        GameLocale.use(GameLanguage.PERSIAN);
        float persian = UiMirror.leading(100f, 520f, inset, width);

        assertEquals(inset, english - 100f, "in English the gap is on the left");
        assertEquals(inset, 100f + 520f - (persian + width), "and in Persian the same gap is on the right");
    }

    @Test
    void aLeadingBoxAndATrailingBoxSwapPlacesRatherThanOverlapping() {
        float rowWidth = 520f;
        float labelWidth = 180f;
        float valueWidth = 90f;
        GameLocale.use(GameLanguage.ENGLISH);
        float label = UiMirror.leading(100f, rowWidth, 36f, labelWidth);
        float value = UiMirror.trailing(100f, rowWidth, 30f, valueWidth);
        assertTrue(label + labelWidth < value, "the label ends before the value starts");

        GameLocale.use(GameLanguage.PERSIAN);
        float mirroredLabel = UiMirror.leading(100f, rowWidth, 36f, labelWidth);
        float mirroredValue = UiMirror.trailing(100f, rowWidth, 30f, valueWidth);
        assertTrue(mirroredValue + valueWidth < mirroredLabel, "and the value ends before the label starts");
        assertTrue(mirroredLabel > label, "the label crossed to the right");
        assertTrue(mirroredValue < value, "and the value crossed to the left");
    }

    @Test
    void theScreenWideHelpersUseTheLogicalWidthTheOverlaysAreLaidOutIn() {
        assertEquals(720f, UiMirror.SCREEN_WIDTH);
        GameLocale.use(GameLanguage.ENGLISH);
        assertEquals(60f, UiMirror.leadingOnScreen(60f, 76f));
        assertEquals(588f, UiMirror.trailingOnScreen(68f, 64f));

        GameLocale.use(GameLanguage.PERSIAN);
        assertEquals(584f, UiMirror.leadingOnScreen(60f, 76f), "the header icon crosses to the right");
        assertEquals(68f, UiMirror.trailingOnScreen(68f, 64f), "and the close button to the left");
    }

    @Test
    void aCentredElementMirrorsAboutItsOwnContainerAndNotAboutTheScreen() {
        GameLocale.use(GameLanguage.ENGLISH);
        assertEquals(611f, UiMirror.centre(500f, 176f, 611f));

        GameLocale.use(GameLanguage.PERSIAN);
        assertEquals(565f, UiMirror.centre(500f, 176f, 611f),
            "mirrored about the panel's own centre at 588f, so the badge stays inside the panel it is drawn on");
        assertEquals(360f, UiMirror.centre(0f, UiMirror.SCREEN_WIDTH, 360f),
            "and an element centred on a centred container does not move at all");
    }

    @Test
    void aBoxWiderThanItsContainerStillMirrorsByTheSameRule() {
        GameLocale.use(GameLanguage.PERSIAN);
        assertEquals(-30f, UiMirror.leading(0f, 100f, 10f, 120f),
            "an oversized box hangs off the leading edge, which is the same mistake in both directions");
        GameLocale.use(GameLanguage.ENGLISH);
        assertEquals(10f, UiMirror.leading(0f, 100f, 10f, 120f));
    }
}
