package com.amirrezahadipoor.herodefense.audio;

import com.amirrezahadipoor.herodefense.settings.GameSettings;

/** Pure lifecycle/settings/focus policy used by the platform audio owner. */
public final class AudioPlaybackPolicy {
    private AudioPlaybackPolicy() {
    }

    public static boolean shouldPlayMusic(GameSettings settings, boolean appBackgrounded) {
        return shouldPlayMusic(settings, appBackgrounded, AudioFocusState.gained());
    }

    /** Music needs the player's toggle, a foreground app and audio focus (roadmap R6.4). */
    public static boolean shouldPlayMusic(
        GameSettings settings,
        boolean appBackgrounded,
        AudioFocusState focus
    ) {
        return settings != null && settings.musicEnabled && !appBackgrounded
            && (focus == null || focus.musicAudible());
    }

    public static boolean shouldPlayEffects(GameSettings settings, boolean appBackgrounded) {
        return shouldPlayEffects(settings, appBackgrounded, AudioFocusState.gained());
    }

    /** Effects survive borrowed focus: only a real loss stops them. */
    public static boolean shouldPlayEffects(
        GameSettings settings,
        boolean appBackgrounded,
        AudioFocusState focus
    ) {
        return settings != null && settings.soundEnabled && !appBackgrounded
            && (focus == null || focus.effectsAudible());
    }
}
