package com.amirrezahadipoor.herodefense.input;

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
            default -> {
                return action;
            }
        }
        return action;
    }
}
