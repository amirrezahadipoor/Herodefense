package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.SpeechVoice;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.presentation.DialogueBox;
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
}
