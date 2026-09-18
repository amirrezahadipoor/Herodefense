package com.amirrezahadipoor.herodefense.i18n;

/**
 * The Root Network screen's words (roadmap R7.3).
 *
 * <p>The screen is the permanent half of the game -- what a player spends Heartwood on between runs -- and it
 * already had two of its words in other tables: {@code MenuStrings.ROOT_NETWORK} names the destination row that
 * opens it and {@code GameOverStrings.ROOT_NETWORK} reports the run's total beside it. This table holds the
 * screen itself, and keeps their terms: "Root Network" is "شبکه ریشه" and "Heartwood" is "چوب دل" everywhere,
 * because a currency that changes name between screens is a currency a player cannot plan with.
 *
 * <p>{@code AWAKENED} is the one entry whose two sides are not the same word. The English "OK" is a tick mark
 * under a bought node; in Persian that reads as agreement rather than as a state, so the entry says what
 * happened to the node instead -- "بیدار" -- which is also the verb the screen's own hint uses.
 */
public enum RootNetworkStrings implements Translated {

    TITLE("ROOT NETWORK", "شبکه ریشه"),
    SUBTITLE("Permanent growth. Never resets.", "رشد دائمی. هرگز بازنشانی نمی‌شود."),
    HEARTWOOD("HEARTWOOD: %1$s", "چوب دل: %1$s"),
    CLOSE("CLOSE", "بستن"),
    AWAKENED("OK", "بیدار"),
    HINT("Tap a green node to awaken it with Heartwood",
        "روی گره سبز بزنید تا با چوب دل بیدارش کنید"),

    /** The two verdicts the screen flashes under the tree when a node is bought or cannot be. */
    FEEDBACK_NEED("NEED %1$s MORE HEARTWOOD", "%1$s چوب دل دیگر لازم است"),
    FEEDBACK_AWAKENED("ROOT AWAKENED | %1$s", "ریشه بیدار شد | %1$s");

    private final String english;
    private final String persian;

    RootNetworkStrings(String english, String persian) {
        this.english = english;
        this.persian = persian;
    }

    @Override
    public String key() {
        return name();
    }

    @Override
    public String english() {
        return english;
    }

    @Override
    public String persian() {
        return persian;
    }
}
