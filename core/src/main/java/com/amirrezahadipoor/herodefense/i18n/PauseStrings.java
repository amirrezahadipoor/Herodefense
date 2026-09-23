package com.amirrezahadipoor.herodefense.i18n;

/**
 * The pause overlay's words (roadmap R7.3), in both shipped languages.
 *
 * <p>The pause screen is the one place a player reads four destinations at once while a run is on the line, so
 * its rows carry a title and a subtitle each and both are here. Values were literals in
 * {@code render/PauseOverlayRenderer}.
 */
public enum PauseStrings implements Translated {

    TITLE("COMBAT PAUSED"),
    SUBTITLE("The arena holds still until you return"),

    RESUME("RESUME"),
    RESUME_SUBTITLE("Return to the battle"),

    INVENTORY("INVENTORY"),
    INVENTORY_SUBTITLE("Equip, compare, and sell gear"),

    STAT_SHOP("STAT SHOP"),
    STAT_SHOP_SUBTITLE("Spend earned coins on permanent upgrades"),

    ROOT_NETWORK("ROOT NETWORK"),
    ROOT_NETWORK_SUBTITLE("Spend Heartwood on permanent growth"),

    GROVE_CODEX("GROVE CODEX"),
    GROVE_CODEX_SUBTITLE("Read what the Tree remembers"),

    /**
     * The two readouts under the title. The wave line is one entry with two arguments rather than a wave entry
     * and a separator, because "WAVE 12 / 200" is one sentence the screen draws in one call: splitting it would
     * put the decision about where the slash goes in the renderer instead of in the language.
     */
    CURRENT_WAVE("CURRENT WAVE"),
    WAVE_VALUE("WAVE %1$s / %2$s"),
    BOSS_WAVE("BOSS WAVE"),
    HERO_LEVEL("HERO LEVEL %1$s"),
    COINS("COINS");

    private final String english;

    PauseStrings(String english) {
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
