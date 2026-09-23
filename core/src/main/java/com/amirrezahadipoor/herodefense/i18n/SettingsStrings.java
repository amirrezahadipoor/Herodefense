package com.amirrezahadipoor.herodefense.i18n;

/**
 * The settings screen's words (roadmap R7.3).
 *
 * <p>Until 2026-09-23 this screen carried the row that switched languages. The owner deleted the Persian
 * translation outright, so the row is gone and what remains are the comfort, audio and accessibility rows.
 * Values were literals in {@code render/SettingsOverlayRenderer}, plus the three volume step labels that lived
 * in {@code settings/GameSettings}.
 */
public enum SettingsStrings implements Translated {

    TITLE("SETTINGS"),
    FOOTER("Comfort choices saved on this device"),
    HINT("Tap a row to switch it, tap again to step the level."),
    CLOSE_HINT("Tap Close to return to the main menu."),

    MUSIC("MUSIC"),
    MUSIC_SUBTITLE("World Tree vigil theme"),
    MUSIC_LEVEL("MUSIC LEVEL"),
    MUSIC_LEVEL_SUBTITLE("How loud the vigil sits"),

    SOUND_EFFECTS("SOUND EFFECTS"),
    SOUND_EFFECTS_SUBTITLE("Arrows, hits, loot"),
    EFFECT_LEVEL("EFFECT LEVEL"),
    EFFECT_LEVEL_SUBTITLE("Hits, drops, level-ups"),

    ON("ON"),
    OFF("OFF"),
    TAP_TO_ENABLE("tap to enable"),
    TAP_TO_MUTE("tap to mute"),
    TAP_TO_STEP("tap to step"),

    /** The three named volume steps, which lived beside the values they label in GameSettings. */
    LEVEL_QUIET("QUIET"),
    LEVEL_NORMAL("NORMAL"),
    LEVEL_FULL("FULL"),

    /** Roadmap G3a: the reduced-motion row. Its "on" hint is its own, because "tap to mute" is about sound. */
    REDUCED_MOTION("REDUCED MOTION"),
    REDUCED_MOTION_SUBTITLE("No shake, no drifting spores"),
    TAP_TO_RESTORE_MOTION("tap to restore motion"),

    TEXT_SIZE("TEXT SIZE"),
    TEXT_SIZE_SUBTITLE("How large the letters sit"),
    TEXT_SIZE_SMALL("SMALL"),
    TEXT_SIZE_LARGE("LARGE"),

    /** Roadmap G3c: accessible colour-blind safe rarity palette. */
    COLOUR_BLIND_RARITY("ACCESSIBLE RARITY"),
    COLOUR_BLIND_RARITY_SUBTITLE("High contrast loot hues"),
    TAP_TO_RESTORE_RARITY("tap for default colours"),

    NARRATION("NARRATION"),
    NARRATION_SUBTITLE("Lore and boss titles spoken"),
    NARRATION_LEVEL("NARRATION LEVEL"),
    NARRATION_LEVEL_SUBTITLE("How loud the voice sits"),

    SCREEN_READER("SCREEN READER"),
    SCREEN_READER_SUBTITLE("TalkBack labels for all buttons"),

    TOUCH_ONLY("TOUCH ONLY"),


    CLOSE("Close");

    private final String english;

    SettingsStrings(String english) {
        this.english = english;
    }

    @Override
    public String key() {
        return name();
    }

    @Override
    public String english() {
        return english;
    }

}
