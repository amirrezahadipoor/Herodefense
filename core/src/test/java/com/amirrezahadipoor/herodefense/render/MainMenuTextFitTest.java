package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.GameNumbers;
import com.amirrezahadipoor.herodefense.i18n.MenuStrings;
import com.amirrezahadipoor.herodefense.i18n.Translated;
import com.amirrezahadipoor.herodefense.input.MainMenuTouchLayout;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The main menu's text fits the boxes it is drawn in, measured with the committed faces at the size the
 * reference emulator draws them ({@code api35-1080x2220}, density 2.75 -- the gate profile of the Android
 * touch workflow). On that profile's own screenshot the pitch ran off both sides of the screen ("...k comes
 * every night ... or jus...") and three rows' second lines ran under their button's edge ("half the
 * heartw", "Tree rememb"), because nothing measured them. The renderer now wraps the pitch and fits the row
 * lines; this test keeps the strings themselves short enough that, on the reference profile, no row needs
 * the fit to step down a size. Larger system fonts and 2.0-density phones still get the fitted draw.
 *
 * <p>Widths come from {@link ReferenceTypeMeasure}: java.awt's layout of the same TrueType files FreeType
 * rasterises at run time.
 */
final class MainMenuTextFitTest {
    private static final DisplayMetrics REFERENCE = ReferenceTypeMeasure.REFERENCE;

    @Test
    void theReferenceEmulatorsRowLinesFitInsideTheirButtons() {
        // The second lines with the widest numbers a real save can put in them.
        Map<String, Translated> subtitles = Map.of(
            "new game", MenuStrings.NEW_GAME_SUBTITLE,
            "brief vigil", MenuStrings.BRIEF_VIGIL_SUBTITLE,
            "continue", MenuStrings.CONTINUE_SUBTITLE,
            "grove codex", MenuStrings.GROVE_CODEX_SUBTITLE,
            "settings", MenuStrings.SETTINGS_SUBTITLE
        );
        for (Map.Entry<String, Translated> row : subtitles.entrySet()) {
            assertFits(row.getKey() + " second line", row.getValue().text(),
                GameFonts.Role.LABEL, MainMenuRenderer.ROW_TEXT_MAX_WIDTH);
        }
        assertFits("root network second line",
            MenuStrings.ROOT_NETWORK_SUBTITLE.text(GameNumbers.integer(9_999)),
            GameFonts.Role.LABEL, MainMenuRenderer.ROW_TEXT_MAX_WIDTH);
        assertFits("continue progress line",
            MenuStrings.PROGRESS_SUMMARY.text(
                GameNumbers.integer(10), GameNumbers.integer(200),
                GameNumbers.integer(9_999)),
            GameFonts.Role.LABEL, MainMenuRenderer.ROW_TEXT_MAX_WIDTH);
        for (Translated title : List.of(MenuStrings.NEW_GAME, MenuStrings.BRIEF_VIGIL, MenuStrings.CONTINUE,
            MenuStrings.ROOT_NETWORK, MenuStrings.GROVE_CODEX, MenuStrings.SETTINGS)) {
            assertFits(title.key() + " title", title.text(),
                GameFonts.Role.HEADING, MainMenuRenderer.ROW_TEXT_MAX_WIDTH);
        }
    }

    @Test
    void thePitchWrapsInsideTheTitlePanel() {
        GameFonts.Role role = GameFonts.Role.forLegacyScale(MainMenuRenderer.PITCH_SCALE);
        List<String> lines = CodexOverlayRenderer.wrapLines(
            MenuStrings.PITCH.text(),
            row -> ReferenceTypeMeasure.width(row, role), MainMenuRenderer.PITCH_MAX_WIDTH);
        assertTrue(lines.size() >= 2, "the pitch is two lines by design, it was " + lines);
        assertTrue(lines.size() <= MainMenuRenderer.PITCH_MAX_LINES,
            "the pitch needs " + lines.size() + " lines, the panel holds "
                + MainMenuRenderer.PITCH_MAX_LINES + ": " + lines);
        for (String line : lines) {
            assertTrue(ReferenceTypeMeasure.width(line, role) <= MainMenuRenderer.PITCH_MAX_WIDTH,
                "a single word of the pitch is wider than the panel: " + line);
        }
    }

    @Test
    void theRowBoxIsTheButtonLessItsMargin() {
        assertEquals(330f, MainMenuRenderer.ROW_TEXT_MAX_WIDTH, 1e-4f,
            "the row's lines start 258f in and the button ends at 600f; the box is what is between, less 12f");
        assertEquals(MainMenuTouchLayout.BUTTON_X + MainMenuTouchLayout.BUTTON_WIDTH,
            MainMenuRenderer.ROW_TEXT_INSET + MainMenuRenderer.ROW_TEXT_MAX_WIDTH + 12f, 1e-4f);
        // Nunito's line height is 1.364 em; a line's glyphs reach from its top to one em below at most.
        float size = GameFonts.worldSizeFor(GameFonts.Role.forLegacyScale(MainMenuRenderer.PITCH_SCALE), REFERENCE);
        float thirdLineBottom = MainMenuRenderer.PITCH_Y
            - (MainMenuRenderer.PITCH_MAX_LINES - 1) * size * 1.364f * MainMenuRenderer.PITCH_LINE_PITCH - size;
        assertTrue(thirdLineBottom >= MainMenuTouchLayout.rowBottom(0) + MainMenuTouchLayout.BUTTON_HEIGHT,
            "three pitch lines at the reference size stay above the first row's frame; the third ends at "
                + thirdLineBottom);
    }

    private static void assertFits(
        String what, String text, GameFonts.Role role, float maxWidth
    ) {
        float width = ReferenceTypeMeasure.width(text, role);
        assertTrue(width <= maxWidth,
            what + " is " + width + " world units wide at " + role + " on the reference"
                + " emulator; the box is " + maxWidth + ": \"" + text + "\"");
    }
}
