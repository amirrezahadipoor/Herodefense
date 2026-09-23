package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.SpeechVoice;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.input.HudTouchLayout;
import com.amirrezahadipoor.herodefense.presentation.DialogueBox;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * The parts of the dialogue box a headless JVM can hold: the speaker the name and colour come from, and
 * when the closing marker blinks. The drawing itself runs on the device, where the smoke journey captures it.
 */
final class DialogueBoxRendererTest {

    @Test
    void eachVoiceCarriesItsOwnNameAndColour() {
        assertEquals(StoryStrings.SPEAKER_WARDEN, DialogueBoxRenderer.speakerName(SpeechVoice.HERO));
        assertEquals(StoryStrings.SPEAKER_TREE, DialogueBoxRenderer.speakerName(SpeechVoice.TREE));
        assertEquals(StoryStrings.SPEAKER_HOLLOW, DialogueBoxRenderer.speakerName(SpeechVoice.HOLLOW));

        assertEquals(1f, DialogueBoxRenderer.speakerColor(SpeechVoice.HERO).r, 1e-4f, "the Warden's name is white");
        assertEquals(OverlayText.POSITIVE.r, DialogueBoxRenderer.speakerColor(SpeechVoice.TREE).r, 1e-4f,
            "the Tree's name is the leaf green the whisper already used");
        assertEquals(OverlayText.NEGATIVE.r, DialogueBoxRenderer.speakerColor(SpeechVoice.HOLLOW).r, 1e-4f,
            "the Hollow's name is the red its taunts already use");
    }

    @Test
    void theClosingMarkerBlinksOnlyWhileTheLineHolds() {
        java.util.List<AudioCue> played = new java.util.ArrayList<>();
        DialogueBox typing = new DialogueBox(played::add);
        typing.speak("Still typing out.", SpeechVoice.HERO, DialogueBox.Source.BEAT);
        assertFalse(DialogueBoxRenderer.closingMarkerVisible(typing), "no marker while the line still types");

        typing.tick(1.0f, false);
        assertFalse(typing.typing());
        assertTrue(DialogueBoxRenderer.closingMarkerVisible(typing), "the marker blinks on as the reading starts");

        DialogueBox fading = new DialogueBox(played::add);
        fading.speak("New line.", SpeechVoice.HERO, DialogueBox.Source.BEAT);
        fading.advance();
        fading.advance();
        assertTrue(fading.fading());
        assertFalse(DialogueBoxRenderer.closingMarkerVisible(fading), "the marker goes with the box");
    }

    @AfterEach
    void restoreTheDesignGrid() {
        ScreenEdges.reset();
    }

    /** The reference emulator's row heights: the name is a LABEL, the line a BODY. */
    private static float nameLine() {
        return ReferenceTypeMeasure.lineHeight(
            GameFonts.Role.forLegacyScale(DialogueBoxRenderer.NAME_SCALE));
    }

    private static float textLine() {
        return ReferenceTypeMeasure.lineHeight(
            GameFonts.Role.forLegacyScale(DialogueBoxRenderer.TEXT_SCALE));
    }

    @Test
    void theBoxIsAsTallAsItsLineAndNoTaller() {
        float top = DialogueBoxRenderer.ARENA_BOX_TOP;
        DialogueBoxRenderer.Layout one = DialogueBoxRenderer.layout(top, nameLine(), textLine(), 1, 1f);
        DialogueBoxRenderer.Layout two = DialogueBoxRenderer.layout(top, nameLine(), textLine(), 2, 1f);
        DialogueBoxRenderer.Layout many = DialogueBoxRenderer.layout(top, nameLine(), textLine(), 9, 1f);
        assertEquals(textLine(), two.height() - one.height(), 1e-3f, "each row adds one line height");
        assertEquals(DialogueBoxRenderer.MAX_TEXT_LINES - 1, (many.height() - one.height()) / textLine(), 1e-3f,
            "the box holds four rows and no more, whatever the line needs");
        assertEquals(0, DialogueBoxRenderer.layout(top, nameLine(), textLine(), 0, 1f).height() - one.height(), 1e-3f,
            "an empty line still gets one row, so the box never collapses onto its name");
        assertTrue(one.height() < 100f, "a one-line beat is a strip, not a quarter of the arena: " + one.height());
        assertTrue(one.nameY() < top && one.firstTextY() < one.nameY() && one.bottom() < one.firstTextY(),
            "name, then text, then the bottom edge");
    }

    /**
     * On the arena the box hangs under the HUD's captions and over the canopy: the Hero at 600f, the enemies at
     * the trunk (738f..755f) and the ground under them are never behind it. It used to span 760f..1015f.
     */
    @Test
    void theArenaBoxHangsUnderTheHudAndAboveTheTrunk() {
        assertEquals(DialogueBoxRenderer.ARENA_BOX_TOP, DialogueBoxRenderer.boxTop(DialogueBox.Source.BEAT));
        assertEquals(DialogueBoxRenderer.ARENA_BOX_TOP, DialogueBoxRenderer.boxTop(DialogueBox.Source.WHISPER));
        assertEquals(DialogueBoxRenderer.ARENA_BOX_TOP, DialogueBoxRenderer.boxTop(DialogueBox.Source.LETTER));
        assertTrue(DialogueBoxRenderer.ARENA_BOX_TOP < 1051f, "under the field detail caption's top line");
        assertTrue(DialogueBoxRenderer.ARENA_BOX_TOP < HudTouchLayout.DESIGN_BUTTON_Y,
            "under the status row's buttons");
        DialogueBoxRenderer.Layout fullest = DialogueBoxRenderer.layout(
            DialogueBoxRenderer.ARENA_BOX_TOP, nameLine(), textLine(), DialogueBoxRenderer.MAX_TEXT_LINES, 1f);
        assertTrue(fullest.bottom() > WorldLayout.WORLD_TREE_Y + 60f,
            "four rows at the reference size end at " + fullest.bottom() + ", above the trunk at "
                + WorldLayout.WORLD_TREE_Y);
        assertTrue(fullest.bottom() > WorldLayout.HERO_WALK_MAX_Y,
            "and above the band the Hero can step in");
    }

    /**
     * On the game-over screen the box goes to the top of the frame. The gate emulator's frame is 1480 units
     * tall, so the HUD's shift puts the box above the title panel there; a 16:9 frame has no room above the
     * panel and the box sits over its title line -- the summary's rows and the buttons are never under it.
     */
    @Test
    void theGameOverBoxSitsAboveTheTitlePanelOnATallFrame() {
        ScreenEdges.update(ReferenceTypeMeasure.REFERENCE);
        float top = DialogueBoxRenderer.boxTop(DialogueBox.Source.VICTORY);
        assertEquals(top, DialogueBoxRenderer.boxTop(DialogueBox.Source.DEATH));
        assertEquals(DialogueBoxRenderer.GAME_OVER_BOX_TOP + HudTouchLayout.MAX_EDGE_SHIFT, top, 1e-3f,
            "the frame is tall enough for the HUD's full shift, and the box takes the same one");
        assertTrue(top + 8f <= ScreenEdges.top(), "inside the frame");
        DialogueBoxRenderer.Layout victory = DialogueBoxRenderer.layout(top, nameLine(), textLine(), 2, 1f);
        assertTrue(victory.bottom() >= GameOverOverlayRenderer.TITLE_PANEL_Y + GameOverOverlayRenderer.TITLE_PANEL_HEIGHT,
            "a two-row victory line ends at " + victory.bottom() + ", above the title panel's top at "
                + (GameOverOverlayRenderer.TITLE_PANEL_Y + GameOverOverlayRenderer.TITLE_PANEL_HEIGHT));

        ScreenEdges.reset();
        float flat = DialogueBoxRenderer.boxTop(DialogueBox.Source.VICTORY);
        assertEquals(DialogueBoxRenderer.GAME_OVER_BOX_TOP, flat, 1e-3f, "a 16:9 frame has no shift to take");
        DialogueBoxRenderer.Layout fullest = DialogueBoxRenderer.layout(
            flat, nameLine(), textLine(), DialogueBoxRenderer.MAX_TEXT_LINES, 1f);
        assertTrue(fullest.bottom() > GameOverOverlayRenderer.SUMMARY_PANEL_Y + GameOverOverlayRenderer.SUMMARY_PANEL_HEIGHT,
            "even four rows on a flat frame end at " + fullest.bottom() + ", above the summary panel");
    }

    @Test
    void theVictoryLineIsTwoRowsAtTheBodySize() {
        GameFonts.Role role = GameFonts.Role.forLegacyScale(DialogueBoxRenderer.TEXT_SCALE);
        List<String> rows = CodexOverlayRenderer.wrapLines(StoryStrings.TREE_VICTORY.text(),
            line -> ReferenceTypeMeasure.width(line, role),
            DialogueBoxRenderer.BOX_W - 2f * DialogueBoxRenderer.INSET);
        assertTrue(rows.size() <= 2, "the victory line takes " + rows.size() + " rows: " + rows);
    }
}
