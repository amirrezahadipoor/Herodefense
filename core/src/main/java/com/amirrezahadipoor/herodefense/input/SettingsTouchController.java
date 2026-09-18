package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.settings.GameSettings;

/** Applies device-setting taps, drag-scrolls the list, and reports close separately. */
public final class SettingsTouchController {

    private static final float ROW_DRAG_THRESHOLD = 55f;

    private int firstVisibleIndex;
    private float accumulatedDrag;

    public void open() {
        firstVisibleIndex = 0;
        accumulatedDrag = 0f;
    }

    public int firstVisibleIndex() {
        return firstVisibleIndex;
    }

    public void drag(float deltaY, int totalRows) {
        accumulatedDrag += deltaY;
        int maxFirst = Math.max(0, totalRows - SettingsTouchLayout.VISIBLE_ROWS);
        while (accumulatedDrag >= ROW_DRAG_THRESHOLD) {
            if (firstVisibleIndex >= maxFirst) {
                accumulatedDrag = 0f;
                break;
            }
            firstVisibleIndex++;
            accumulatedDrag -= ROW_DRAG_THRESHOLD;
        }
        while (accumulatedDrag <= -ROW_DRAG_THRESHOLD) {
            if (firstVisibleIndex <= 0) {
                accumulatedDrag = 0f;
                break;
            }
            firstVisibleIndex--;
            accumulatedDrag += ROW_DRAG_THRESHOLD;
        }
        if (firstVisibleIndex >= maxFirst && accumulatedDrag > 0f) {
            accumulatedDrag = 0f;
        } else if (firstVisibleIndex <= 0 && accumulatedDrag < 0f) {
            accumulatedDrag = 0f;
        }
    }

    public void drag(float deltaY) {
        drag(deltaY, SettingsTouchLayout.TOTAL_ROWS);
    }

    /** Applies the action to the settings and returns it, so the caller can play the right cue. */
    public SettingsTouchLayout.Action tap(GameSettings settings, float x, float y) {
        SettingsTouchLayout.Action action = SettingsTouchLayout.actionAt(x, y, firstVisibleIndex);
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
            // Read straight from the settings object by the composer on the frame after the tap, so the shake
            // stops while the player is still looking at the row they pressed. That is the row's only proof:
            // nothing else on the screen changes when motion is reduced.
            case TOGGLE_REDUCED_MOTION -> settings.reducedMotion = !settings.reducedMotion;
            case TOGGLE_COLOUR_BLIND_RARITY -> settings.colourBlindRarity = !settings.colourBlindRarity;
            default -> {
                return action;
            }
        }
        return action;
    }
}
