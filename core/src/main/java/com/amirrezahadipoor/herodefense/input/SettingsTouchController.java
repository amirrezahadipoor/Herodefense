package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.settings.GameSettings;

/** Applies device-setting taps and reports close separately. */
public final class SettingsTouchController {

    /** Applies the action to the settings and returns it, so the caller can play the right cue. */
    public SettingsTouchLayout.Action tap(GameSettings settings, float x, float y) {
        SettingsTouchLayout.Action action = SettingsTouchLayout.actionAt(x, y);
        if (settings == null) return SettingsTouchLayout.Action.NONE;
        switch (action) {
            case TOGGLE_SOUND -> settings.soundEnabled = !settings.soundEnabled;
            case TOGGLE_MUSIC -> settings.musicEnabled = !settings.musicEnabled;
            case CYCLE_SOUND_LEVEL -> settings.cycleSoundVolume();
            case CYCLE_MUSIC_LEVEL -> settings.cycleMusicVolume();
            // The locale is switched here rather than when the settings are next saved, so the screen the
            // player is looking at changes under their finger on the frame after the tap. That is the whole
            // point of the row: a language you have to leave the screen to see is a language you cannot
            // confirm you chose.
            case CYCLE_LANGUAGE -> GameLocale.use(settings.cycleLanguage());
            default -> {
                return action;
            }
        }
        return action;
    }
}
