package com.amirrezahadipoor.herodefense.i18n;

/**
 * The Root Network screen's words (roadmap R7.3).
 *
 * <p>The screen is the permanent half of the game -- what a player spends Heartwood on between runs -- and it
 * already had two of its words in other tables: {@code MenuStrings.ROOT_NETWORK} names the destination row that
 * opens it and {@code GameOverStrings.ROOT_NETWORK} reports the run's total beside it. This table holds the
 * screen itself, and keeps their terms: "Root Network" and "Heartwood" everywhere,
 * because a currency that changes name between screens is a currency a player cannot plan with.
 */
public enum RootNetworkStrings implements Translated {

    TITLE("ROOT NETWORK"),
    SUBTITLE("Permanent growth. Never resets."),
    HEARTWOOD("HEARTWOOD: %1$s"),
    CLOSE("CLOSE"),
    AWAKENED("OK"),
    HINT("Tap a green node to awaken it with Heartwood"),

    /** The dawn ledger at the hub's foot: the count, and the line for a band that is still empty. */
    DAWNS("DAWNS: %1$s"),
    DAWNS_EMPTY("the tree is still young"),

    /** The two verdicts the screen flashes under the tree when a node is bought or cannot be. */
    FEEDBACK_NEED("NEED %1$s MORE HEARTWOOD"),
    FEEDBACK_AWAKENED("ROOT AWAKENED | %1$s");

    private final String english;

    RootNetworkStrings(String english) {
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
