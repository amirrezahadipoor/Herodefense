package com.amirrezahadipoor.herodefense.i18n;

/**
 * The heads-up display's words (roadmap R7.3).
 *
 * <p>The HUD is the tightest space in the game and the one read most often: six labels and four counters, drawn
 * every frame. The coin total is not here -- the HUD calls {@code MainMenuRenderer.coinTotalLabel}, the same call
 * the pause overlay and the run summary make, so "$ 417" has one format in the game. The labels here are
 * the short forms and the layout has to fit them, which is what {@code HudLayoutTest} measures. Values were
 * literals in {@code render/HudRenderer}.
 */
public enum HudStrings implements Translated {

    WAVE("WAVE"),
    HEALTH("HEALTH"),
    COINS("COINS"),
    SHOP("SHOP"),
    INVENTORY("INVENTORY"),
    ULTIMATE("ULTIMATE"),

    LEVEL("LV %1$s"),
    TIER_BADGE("T%1$s"),

    /**
     * "n of m", which the HUD draws twice: the hero's health over its maximum and the wave over the run's last
     * one. One entry rather than two, because they are the same sentence in the same shape and a table holding
     * it twice would have two things to translate and no way to keep them agreeing.
     */
    FRACTION("%1$s / %2$s"),

    /** The grove's planted trees over its four slots, then its health as a percentage. */
    GROVE_STATUS("GROVE %1$s/4 %2$s"),

    /** The simulation speed, with the "x" the HUD has always drawn. */
    SPEED("%1$sx"),

    /** The experience bar's caption: progress toward the next level, or {@link #READY} once the hero is capped. */
    XP_PROGRESS("%1$s / %2$s XP"),
    READY("MAX"),

    /** The lilac tag floating over a stunned enemy (roadmap H3). */
    STUN_TAG("STUN");

    private final String english;

    HudStrings(String english) {
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
