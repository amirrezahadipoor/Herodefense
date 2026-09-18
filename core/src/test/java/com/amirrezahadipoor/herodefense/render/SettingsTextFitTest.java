package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.SettingsStrings;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * A row's subtitle and its trailing hint share one line, and until 2026-09-18 nothing stopped them from meeting
 * in the middle.
 *
 * <p>The CI emulator caught it by accident: the touch test screenshots the settings screen and gates its mean
 * luma, so when roadmap G3a added a sixth row the run failed on brightness and -- after the capture was moved
 * before the assertion -- left a PNG in the artifact. That PNG shows "boss entrances" ending under "tap to
 * step", "direction" ending under "tap to switch", and the footer hint running out of its own panel. Three
 * collisions, all of them pre-existing except the new row's, none of them visible to any test that did not draw.
 *
 * <p>What this class can enforce without a font is length, and length is what collided: a subtitle drawn at
 * 0.68f from x=136 and a hint right-aligned at x=590 have about thirty characters of shared line between them,
 * and every subtitle that overflowed was longer than thirty. So the limit is thirty, per language, and the
 * test names the entries it guards rather than sweeping the table, because FOOTER and the level labels are
 * drawn elsewhere at other scales and a blanket limit would forbid text that fits where it lives.
 *
 * <p>Character count is a proxy for width, not a measurement -- Persian glyphs are wider than Latin ones and a
 * wide-capitals string of twenty can beat a lowercase string of thirty. The honest gate is the CI screenshot,
 * which now survives its own brightness failure; this is the cheap tripwire in front of it.
 */
final class SettingsTextFitTest {

    /** The row subtitles: left column of a line whose right column holds the tap hint. */
    private static final List<SettingsStrings> ROW_SUBTITLES = List.of(
        SettingsStrings.SOUND_EFFECTS_SUBTITLE,
        SettingsStrings.MUSIC_SUBTITLE,
        SettingsStrings.EFFECT_LEVEL_SUBTITLE,
        SettingsStrings.MUSIC_LEVEL_SUBTITLE,
        SettingsStrings.LANGUAGE_SUBTITLE,
        SettingsStrings.REDUCED_MOTION_SUBTITLE,
        SettingsStrings.COLOUR_BLIND_RARITY_SUBTITLE
    );

    private static final int SUBTITLE_LIMIT = 30;

    /** The two lines inside the footer panel, which the panel's own border ended mid-word. */
    private static final int NOTE_LINE_LIMIT = 52;

    @Test
    void noRowSubtitleRunsIntoItsTapHint() {
        for (SettingsStrings subtitle : ROW_SUBTITLES) {
            assertTrue(subtitle.english().length() <= SUBTITLE_LIMIT,
                subtitle.key() + " is " + subtitle.english().length() + " characters in English: at 0.68f it"
                    + " ends inside the column the tap hint is right-aligned in, which is how three settings"
                    + " rows shipped reading \"boss entrantaps to step\"");
            assertTrue(subtitle.persian().length() <= SUBTITLE_LIMIT,
                subtitle.key() + " is " + subtitle.persian().length() + " characters in Persian, and Persian"
                    + " glyphs are wider than Latin ones at the same scale");
        }
    }

    @Test
    void theFooterLinesStayInsideThePanelTheyAreFramedBy() {
        assertTrue(SettingsStrings.HINT.english().length() <= NOTE_LINE_LIMIT,
            "the hint line left its own panel at the footer's draw scale");
        assertTrue(SettingsStrings.CLOSE_HINT.english().length() <= NOTE_LINE_LIMIT);
        assertTrue(SettingsStrings.FOOTER.english().length() <= NOTE_LINE_LIMIT,
            "the footer sits in the header band, which is wider, but it is framed by nothing and read against"
                + " the same 720f screen");
    }
}
