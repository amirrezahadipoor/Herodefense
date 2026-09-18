package com.amirrezahadipoor.herodefense.settings;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
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
    private static final String LANGUAGE_KEY = "display.language";
    private static final String REDUCED_MOTION_KEY = "display.reducedMotion";
    private static final String COLOUR_BLIND_RARITY_KEY = "display.colourBlindRarity";
    private static final String TEXT_SIZE_KEY = "display.textSize";
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
        // The same argument, for the same reason, in the same place: a code this build does not know becomes
        // English here rather than at draw time. Loading the device's preferences is what makes the game speak
        // them, and this is the only place it happens other than the settings row that changes it.
        settings.reducedMotion = preferences.getBoolean(REDUCED_MOTION_KEY, false);
        settings.colourBlindRarity = preferences.getBoolean(COLOUR_BLIND_RARITY_KEY, false);
        settings.textSizeIndex = preferences.getInteger(TEXT_SIZE_KEY, settings.textSizeIndex);
        settings.normalizeTextSize();
        settings.language = GameLanguage.fromCode(preferences.getString(LANGUAGE_KEY, settings.language.code()));
        GameLocale.use(settings.language);
        try {
            Class<?> fontsClass = Class.forName(
                "com.amirrezahadipoor.herodefense.render.GameFonts");
            java.lang.reflect.Method apply = fontsClass.getMethod("applyTextScale", float.class);
            apply.invoke(null, settings.textSizeScale());
        } catch (Exception ignored) {
            // Headless tests and early startup have no GL context; the scale is still remembered and will
            // be applied when GameFonts.shared() is first created via its DisplayMetrics path.
        }
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
        preferences.putBoolean(REDUCED_MOTION_KEY, settings.reducedMotion);
        preferences.putBoolean(COLOUR_BLIND_RARITY_KEY, settings.colourBlindRarity);
        preferences.putInteger(TEXT_SIZE_KEY, settings.textSizeIndex);
        preferences.putString(LANGUAGE_KEY, settings.language.code());
        preferences.flush();
    }
}
