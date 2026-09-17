package com.amirrezahadipoor.herodefense.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.settings.GameSettings;
import org.junit.jupiter.api.Test;

/** Roadmap R6.4: losing focus is not the same as borrowing it, and both are asserted here. */
final class AudioFocusStateTest {

    @Test
    void aRealLossSilencesEverythingUntilFocusComesBack() {
        AudioFocusState focus = AudioFocusState.gained().apply(AudioFocusState.Event.LOSS);
        assertTrue(focus.silenced());
        assertEquals(0f, focus.musicGain());
        assertFalse(focus.musicAudible());
        assertFalse(focus.effectsAudible());

        GameSettings settings = new GameSettings();
        assertFalse(AudioPlaybackPolicy.shouldPlayMusic(settings, false, focus));
        assertFalse(AudioPlaybackPolicy.shouldPlayEffects(settings, false, focus));
    }

    @Test
    void borrowedFocusDucksTheMusicAndKeepsTheFightAudible() {
        AudioFocusState focus = AudioFocusState.gained().apply(AudioFocusState.Event.TRANSIENT_LOSS);
        assertEquals(0.25f, focus.musicGain());
        assertTrue(focus.musicAudible(), "a notification must not stop the music, only lower it");
        assertTrue(focus.effectsAudible(), "a silent fight is a broken-looking fight");
        GameSettings settings = new GameSettings();
        assertTrue(AudioPlaybackPolicy.shouldPlayMusic(settings, false, focus));
        assertTrue(AudioPlaybackPolicy.shouldPlayEffects(settings, false, focus));
    }

    @Test
    void focusReturningRestoresFullGainAndThePlayerTogglesStillWin() {
        AudioFocusState focus = AudioFocusState.gained()
            .apply(AudioFocusState.Event.LOSS)
            .apply(AudioFocusState.Event.GAIN);
        assertEquals(1f, focus.musicGain());
        assertFalse(focus.silenced());

        GameSettings muted = new GameSettings();
        muted.musicEnabled = false;
        assertFalse(AudioPlaybackPolicy.shouldPlayMusic(muted, false, focus));
        assertTrue(AudioPlaybackPolicy.shouldPlayEffects(muted, false, focus));
        muted.soundEnabled = false;
        assertFalse(AudioPlaybackPolicy.shouldPlayEffects(muted, false, focus));
        assertFalse(AudioPlaybackPolicy.shouldPlayEffects(muted, true, focus));
        assertTrue(AudioPlaybackPolicy.shouldPlayMusic(new GameSettings(), false, null));
    }
}
