package com.amirrezahadipoor.herodefense.settings;

import com.badlogic.gdx.Preferences;

/** Persists main-menu settings immediately in device-local preferences. */
public final class LocalSettingsRepository {
    public static final String PREFERENCES_NAME = "hero-defense-settings";
    private static final String SOUND_KEY = "audio.sound";
    private static final String MUSIC_KEY = "audio.music";
    private static final String AUTO_SELL_COMMON_KEY = "inventory.autoSell.common";
    private static final String AUTO_SELL_UNCOMMON_KEY = "inventory.autoSell.uncommon";
    private static final String AUTO_SELL_RARE_KEY = "inventory.autoSell.rare";
    private static final String TUTORIAL_SEEN_KEY = "onboarding.tutorialSeen";
    private static final String MUSIC_VOLUME_KEY = "audio.musicVolume";
    private static final String SOUND_VOLUME_KEY = "audio.soundVolume";
    private final Preferences preferences;

    public LocalSettingsRepository(Preferences preferences) {
        if (preferences == null) throw new IllegalArgumentException("preferences cannot be null");
        this.preferences = preferences;
    }

    public GameSettings load() {
        GameSettings settings = new GameSettings();
        settings.soundEnabled = preferences.getBoolean(SOUND_KEY, true);
        settings.musicEnabled = preferences.getBoolean(MUSIC_KEY, true);
        settings.autoSellCommon = preferences.getBoolean(AUTO_SELL_COMMON_KEY, false);
        settings.autoSellUncommon = preferences.getBoolean(AUTO_SELL_UNCOMMON_KEY, false);
        settings.autoSellRare = preferences.getBoolean(AUTO_SELL_RARE_KEY, false);
        settings.tutorialSeen = preferences.getBoolean(TUTORIAL_SEEN_KEY, false);
        settings.musicVolume = preferences.getFloat(MUSIC_VOLUME_KEY, settings.musicVolume);
        settings.soundVolume = preferences.getFloat(SOUND_VOLUME_KEY, settings.soundVolume);
        // A preference file can be edited by hand or written by an older build; the screen only knows the
        // three named steps, so anything else is snapped to the closest one here rather than at draw time.
        settings.normalizeVolumes();
        return settings;
    }

    public void save(GameSettings settings) {
        if (settings == null) return;
        preferences.putBoolean(SOUND_KEY, settings.soundEnabled);
        preferences.putBoolean(MUSIC_KEY, settings.musicEnabled);
        preferences.putBoolean(AUTO_SELL_COMMON_KEY, settings.autoSellCommon);
        preferences.putBoolean(AUTO_SELL_UNCOMMON_KEY, settings.autoSellUncommon);
        preferences.putBoolean(AUTO_SELL_RARE_KEY, settings.autoSellRare);
        preferences.putBoolean(TUTORIAL_SEEN_KEY, settings.tutorialSeen);
        preferences.putFloat(MUSIC_VOLUME_KEY, settings.musicVolume);
        preferences.putFloat(SOUND_VOLUME_KEY, settings.soundVolume);
        preferences.flush();
    }
}
