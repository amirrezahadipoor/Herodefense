package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.render.GameFonts;
import com.amirrezahadipoor.herodefense.settings.GameSettings;

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

    public SettingsTouchLayout.Action tap(GameSettings settings, float x, float y) {
        SettingsTouchLayout.Action action = SettingsTouchLayout.actionAt(x, y, firstVisibleIndex);
        if (settings == null) return SettingsTouchLayout.Action.NONE;
        switch (action) {
            case TOGGLE_SOUND -> settings.soundEnabled = !settings.soundEnabled;
            case TOGGLE_MUSIC -> settings.musicEnabled = !settings.musicEnabled;
            case CYCLE_SOUND_LEVEL -> settings.cycleSoundVolume();
            case CYCLE_MUSIC_LEVEL -> settings.cycleMusicVolume();
            case TOGGLE_REDUCED_MOTION -> settings.reducedMotion = !settings.reducedMotion;
            case CYCLE_TEXT_SIZE -> {
                settings.cycleTextSize();
                GameFonts.applyTextScale(settings.textSizeScale());
            }
            case TOGGLE_COLOUR_BLIND_RARITY -> settings.colourBlindRarity = !settings.colourBlindRarity;
            case TOGGLE_NARRATION -> settings.narrationEnabled = !settings.narrationEnabled;
            case CYCLE_NARRATION_LEVEL -> settings.cycleNarrationVolume();
            case TOGGLE_SCREEN_READER -> settings.screenReaderEnabled = !settings.screenReaderEnabled;
            default -> {
                return action;
            }
        }
        return action;
    }
}
