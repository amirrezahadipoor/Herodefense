package com.amirrezahadipoor.herodefense.i18n;

/**
 * The run-summary screen's words (roadmap R7.3), in both shipped languages.
 *
 * <p>This is the screen a player reads after losing a long run, so its five measurement rows are named rather
 * than abbreviated. Values were literals in {@code render/GameOverOverlayRenderer}.
 */
public enum GameOverStrings implements Translated {

    /** The headline: which of the two endings this was. */
    DEFEAT("DEFEAT"),
    RUN_COMPLETE("RUN COMPLETE"),

    SUMMARY_TITLE("RUN SUMMARY"),
    WAVE_REACHED("Wave reached"),
    BOSSES_DEFEATED("Bosses defeated"),
    ENEMIES_DEFEATED("Enemies defeated"),
    COINS_EARNED("Kill coins earned"),
    HERO_LEVEL("Hero level"),
    MYTHIC_EARNED("Mythic earned"),

    /**
     * A row's value as a count over its total: this run's last wave over the length the run was set to, and the
     * bosses defeated over the bosses the run holds. It is one entry with two arguments rather than a number, a
     * separator and a number, because where the slash sits and which side of it each number goes on is a
     * property of the language, not of the renderer. The boss row used to carry its own entry with a literal
     * twenty written into it, which is how a run of forty bosses came to read "39 / 20".
     */
    COUNT_OF_TOTAL("%1$s / %2$s"),

    DEFEND_AGAIN("DEFEND AGAIN"),
    RESTART_AT_WAVE_ONE("RESTART AT WAVE 1"),
    DEFEND_AGAIN_SUBTITLE("Begin fresh run same tier"),

    /**
     * The ascend row: what it pays, and what the tier does next. "3 becomes 4" is drawn with an ASCII
     * arrow because U+2190 is in neither shipped face.
     */
    ASCEND("ASCEND  |  +%1$s HEARTWOOD"),
    TIER_PROGRESS("Tier %1$s -> %2$s  |  Harder foes, permanent roots"),
    ROOT_NETWORK("ROOT NETWORK  |  %1$s HW"),
    ROOT_NETWORK_SUBTITLE("Spend Heartwood on permanent growth");

    private final String english;

    GameOverStrings(String english) {
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
