package com.amirrezahadipoor.herodefense.settings;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.render.GameFonts;
import com.badlogic.gdx.Preferences;

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
    private static final String NARRATION_ENABLED_KEY = "audio.narrationEnabled";
    private static final String NARRATION_VOLUME_KEY = "audio.narrationVolume";
    private static final String SCREEN_READER_KEY = "accessibility.screenReader";
    private static final String LANGUAGE_KEY = "display.language";
    private static final String REDUCED_MOTION_KEY = "display.reducedMotion";
    private static final String COLOUR_BLIND_RARITY_KEY = "display.colourBlindRarity";
    private static final String TEXT_SIZE_KEY = "display.textSize";
    private static final String LAST_GIFT_DAY_KEY = "tree.lastGiftEpochDay";
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
        settings.narrationEnabled = preferences.getBoolean(NARRATION_ENABLED_KEY, settings.narrationEnabled);
        settings.narrationVolume = preferences.getFloat(NARRATION_VOLUME_KEY, settings.narrationVolume);
        settings.screenReaderEnabled = preferences.getBoolean(SCREEN_READER_KEY, settings.screenReaderEnabled);
        settings.normalizeVolumes();
        settings.reducedMotion = preferences.getBoolean(REDUCED_MOTION_KEY, false);
        settings.colourBlindRarity = preferences.getBoolean(COLOUR_BLIND_RARITY_KEY, false);
        settings.textSizeIndex = preferences.getInteger(TEXT_SIZE_KEY, settings.textSizeIndex);
        settings.normalizeTextSize();
        settings.language = GameLanguage.fromCode(preferences.getString(LANGUAGE_KEY, settings.language.code()));
        GameLocale.use(settings.language);
        GameFonts.applyTextScale(settings.textSizeScale());
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
        preferences.putBoolean(NARRATION_ENABLED_KEY, settings.narrationEnabled);
        preferences.putFloat(NARRATION_VOLUME_KEY, settings.narrationVolume);
        preferences.putBoolean(SCREEN_READER_KEY, settings.screenReaderEnabled);
        preferences.putBoolean(REDUCED_MOTION_KEY, settings.reducedMotion);
        preferences.putBoolean(COLOUR_BLIND_RARITY_KEY, settings.colourBlindRarity);
        preferences.putInteger(TEXT_SIZE_KEY, settings.textSizeIndex);
        preferences.putString(LANGUAGE_KEY, settings.language.code());
        preferences.flush();
    }

    /** The epoch day the Tree last paid its daily gift, or -1 before the first visit. */
    public long lastGiftEpochDay() {
        return preferences.getLong(LAST_GIFT_DAY_KEY, -1L);
    }

    /** Records that today's gift has been paid; device-level, so a new run cannot re-earn it. */
    public void markGifted(long epochDay) {
        preferences.putLong(LAST_GIFT_DAY_KEY, epochDay);
        preferences.flush();
    }
}
