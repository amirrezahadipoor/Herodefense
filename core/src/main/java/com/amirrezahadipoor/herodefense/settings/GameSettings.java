package com.amirrezahadipoor.herodefense.settings;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.SettingsStrings;
import com.amirrezahadipoor.herodefense.model.ItemTier;

/** Device-local accessibility/audio/inventory preferences independent from a run save. */
public final class GameSettings {
    public boolean soundEnabled = true;
    public boolean musicEnabled = true;
    /** Inventory auto-sell: ticked tiers are sold the moment a drop enters the backpack. */
    public boolean autoSellCommon;
    public boolean autoSellUncommon;
    public boolean autoSellRare;
    /**
     * The first-run coaching sequence (roadmap R7.1) is shown once per device: a run that starts after it
     * has been finished or skipped does not show it again. It lives with the other device-local preferences
     * rather than in a run save, because it is about the player and not about the run.
     */
    public boolean tutorialSeen;

    /**
     * Reduced motion (roadmap G3a): switches off the two sources of movement in this game that a player cannot
     * predict and does not control -- the camera impulse after a hit, a critical, an ultimate or a boss arrival,
     * and the ambient spore drift that never stops. Both are decoration; neither carries information the rest of
     * the screen does not also carry, which is what makes them safe to remove and the reason the hit particles,
     * the floating damage numbers and the hit-stop pause are deliberately left alone: those tell the player
     * something happened, and a preference is not a licence to make the game harder to read.
     *
     * <p>Device-local like the volumes, and off by default because it is a preference about the player and not
     * a difficulty choice.
     */
    public boolean reducedMotion;

    /**
     * Colour-blind accessible rarity palette (roadmap G3c): replaces the default green/gold/violet palette
     * with an Okabe-Ito / Wong accessible palette distinguishable under protanopia, deuteranopia, and tritanopia.
     */
    public boolean colourBlindRarity;

    /**
     * The language the game speaks (roadmap R7.3). It is a device-local preference like the volumes rather than
     * part of a run save: a player who switches to Persian halfway through a wave keeps Persian in the next run,
     * and a save file copied to another device does not carry a language with it.
     */
    public GameLanguage language = GameLanguage.ENGLISH;

    /**
     * Effect and music level, as one of three named steps (roadmap R6.4). Three taps cycle them because the
     * settings surface is touch-only with no drag handles; the numbers are here so the audio layer has one
     * source for "how loud is loud" and the screen has one source for the label.
     */
    private static final float[] LEVELS = {0.45f, 0.75f, 1.0f};

    /**
     * The three steps' names, in the string table rather than beside the values they label: the settings screen
     * draws these words, so a second copy of them here would be a second thing to translate and a way for the
     * screen and the setting to disagree.
     */
    private static final SettingsStrings[] LEVEL_LABELS = {
        SettingsStrings.LEVEL_QUIET, SettingsStrings.LEVEL_NORMAL, SettingsStrings.LEVEL_FULL
    };

    public float musicVolume = LEVELS[LEVELS.length - 1];
    public float soundVolume = LEVELS[LEVELS.length - 1];

    public static int levelCount() {
        return LEVELS.length;
    }

    public static float levelValue(int index) {
        if (index < 0) return LEVELS[0];
        return LEVELS[Math.min(index, LEVELS.length - 1)];
    }

    public static String levelLabel(int index) {
        if (index < 0) return GameLocale.text(LEVEL_LABELS[0]);
        return GameLocale.text(LEVEL_LABELS[Math.min(index, LEVEL_LABELS.length - 1)]);
    }

    /** The stored value's step, or the closest one, so a hand-edited preference cannot break the screen. */
    public static int levelIndex(float volume) {
        int best = 0;
        float bestDistance = Float.MAX_VALUE;
        for (int index = 0; index < LEVELS.length; index++) {
            float distance = Math.abs(LEVELS[index] - volume);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = index;
            }
        }
        return best;
    }

    /** Steps the music level up and wraps back to the quietest; returns the new level's label. */
    public String cycleMusicVolume() {
        musicVolume = LEVELS[(levelIndex(musicVolume) + 1) % LEVELS.length];
        return levelLabel(levelIndex(musicVolume));
    }

    public String cycleSoundVolume() {
        soundVolume = LEVELS[(levelIndex(soundVolume) + 1) % LEVELS.length];
        return levelLabel(levelIndex(soundVolume));
    }

    /** Cycles the language and returns the new one, the way the volume rows return their new label. */
    public GameLanguage cycleLanguage() {
        language = language.next();
        return language;
    }

    /** Keeps persisted values inside the named steps after a load. */
    public void normalizeVolumes() {
        musicVolume = levelValue(levelIndex(musicVolume));
        soundVolume = levelValue(levelIndex(soundVolume));
    }

    /** Legendary and Mythic items are never auto-sold; the toggle simply does not exist for them. */
    public boolean autoSells(ItemTier tier) {
        if (tier == null) return false;
        return switch (tier) {
            case COMMON -> autoSellCommon;
            case UNCOMMON -> autoSellUncommon;
            case RARE -> autoSellRare;
            case LEGENDARY -> false;
            case MYTHIC -> false;
        };
    }

    /** Flips the toggle for a sellable tier; returns false for tiers without a toggle. */
    public boolean toggleAutoSell(ItemTier tier) {
        if (tier == null) return false;
        switch (tier) {
            case COMMON -> autoSellCommon = !autoSellCommon;
            case UNCOMMON -> autoSellUncommon = !autoSellUncommon;
            case RARE -> autoSellRare = !autoSellRare;
            default -> {
                return false;
            }
        }
        return true;
    }
}
