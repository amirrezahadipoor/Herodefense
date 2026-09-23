package com.amirrezahadipoor.herodefense.i18n;

/**
 * The main menu's words (roadmap R7.3).
 *
 * <p>Every value here was a literal inside {@code render/MainMenuRenderer} before this table existed, and the
 * row it belonged to is named in the constant. The English is unchanged character for character, including
 * the double spaces around the footer's separators, because those spaces are the layout.
 */
public enum MenuStrings implements Translated {

    /** The game's name, drawn at 2.28x in gold. */
    TITLE("HERO DEFENSE"),

    /** The line above the title. */
    TAGLINE("ONE TREE LEFT STANDING"),

    /** The two-line pitch under the title. */
    PITCH("The dark comes every night. Hold the last tree through 200 of them — or just thirty."),

    /** Row 1. */
    NEW_GAME("NEW GAME"),
    NEW_GAME_SUBTITLE("Begin your first night"),

    /** Row 2, present only when the thirty-wave vigil is unlocked. */
    BRIEF_VIGIL("BRIEF VIGIL"),
    BRIEF_VIGIL_SUBTITLE("Thirty nights | half heartwood"),

    /** Row 3, present only while a run is live. */
    CONTINUE("CONTINUE"),
    CONTINUE_SUBTITLE("Return to the active wave"),

    /** Row 3's subtitle when there is progress to name: tier, best wave, and Heartwood. */
    PROGRESS_SUMMARY("Tier %1$s | Peak %2$s | %3$s HW"),

    /** Row 4. */
    ROOT_NETWORK("ROOT NETWORK"),
    ROOT_NETWORK_SUBTITLE("Lasting growth | %1$s HW"),

    /** Row 5. */
    GROVE_CODEX("GROVE CODEX"),
    GROVE_CODEX_SUBTITLE("What the Tree remembers"),

    /** Row 6. */
    SETTINGS("SETTINGS"),
    SETTINGS_SUBTITLE("Comfort, music, and effects"),

    /** The strip along the bottom of the screen. */
    FOOTER("200 NIGHTS  |  ONE LAST TREE  |  DAWN  |  T%1$s"),

    /** The coin count in the top corner. */
    COINS("$ %1$s");

    private final String english;

    MenuStrings(String english) {
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
