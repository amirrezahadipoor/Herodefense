package com.amirrezahadipoor.herodefense.settings;

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
     * Text size (roadmap G3b): three named steps that scale every typographic role. Device-local like the
     * volumes, because it is about the player and not the run. 0.85/1.0/1.18 are the shipped scales — small
     * enough that a larger face never clips a row laid out for a smaller one (checked in
     * SettingsTextFitTest), large enough that the difference is readable at arm's length.
     */
    private static final float[] TEXT_SCALES = {0.85f, 1.0f, 1.18f};

    private static final SettingsStrings[] TEXT_SIZE_LABELS = {
        SettingsStrings.TEXT_SIZE_SMALL, SettingsStrings.LEVEL_NORMAL, SettingsStrings.TEXT_SIZE_LARGE
    };

    public int textSizeIndex = 1;

    public static int textSizeCount() {
        return TEXT_SCALES.length;
    }

    public static float textScaleValue(int index) {
        if (index < 0) return TEXT_SCALES[0];
        return TEXT_SCALES[Math.min(index, TEXT_SCALES.length - 1)];
    }

    public float textSizeScale() {
        return textScaleValue(textSizeIndex);
    }

    public String textSizeLabel() {
        if (textSizeIndex < 0) return GameLocale.text(TEXT_SIZE_LABELS[0]);
        return GameLocale.text(TEXT_SIZE_LABELS[Math.min(textSizeIndex, TEXT_SIZE_LABELS.length - 1)]);
    }

    public String cycleTextSize() {
        textSizeIndex = (textSizeIndex + 1) % TEXT_SCALES.length;
        return textSizeLabel();
    }

    public void normalizeTextSize() {
        if (textSizeIndex < 0 || textSizeIndex >= TEXT_SCALES.length) textSizeIndex = 1;
    }

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
    /** F3: voice/narration for lore and boss titles — device-local, off by default until TTS ready. */
    public boolean narrationEnabled = true;
    public float narrationVolume = LEVELS[LEVELS.length - 1];
    /** G3d: screen-reader for TalkBack — device-local, on by default, uses same TTS. */
    public boolean screenReaderEnabled = true;

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

    /** Keeps persisted values inside the named steps after a load. */
    public void normalizeVolumes() {
        musicVolume = levelValue(levelIndex(musicVolume));
        soundVolume = levelValue(levelIndex(soundVolume));
        narrationVolume = levelValue(levelIndex(narrationVolume));
    }

    public String cycleNarrationVolume() {
        narrationVolume = LEVELS[(levelIndex(narrationVolume) + 1) % LEVELS.length];
        return levelLabel(levelIndex(narrationVolume));
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
