package com.amirrezahadipoor.herodefense.i18n;

/**
 * The pause overlay's words (roadmap R7.3), in both shipped languages.
 *
 * <p>The pause screen is the one place a player reads four destinations at once while a run is on the line, so
 * its rows carry a title and a subtitle each and both are here. Values were literals in
 * {@code render/PauseOverlayRenderer}.
 */
public enum PauseStrings implements Translated {

    TITLE("COMBAT PAUSED", "نبرد متوقف شد"),
    SUBTITLE("The arena holds still until you return", "میدان تا بازگشت شما ساکن می‌ماند"),

    RESUME("RESUME", "ادامهٔ بازی"),
    RESUME_SUBTITLE("Return to the battle", "بازگشت به نبرد"),

    INVENTORY("INVENTORY", "کوله‌پشتی"),
    INVENTORY_SUBTITLE("Equip, compare, and sell gear", "تجهیز، مقایسه و فروش تجهیزات"),

    STAT_SHOP("STAT SHOP", "فروشگاه توانمندی"),
    STAT_SHOP_SUBTITLE("Spend earned coins on permanent upgrades", "سکه‌های کسب‌شده را صرف ارتقای دائمی کنید"),

    ROOT_NETWORK("ROOT NETWORK", "شبکه ریشه"),
    ROOT_NETWORK_SUBTITLE("Spend Heartwood on permanent growth", "چوب دل را صرف رشد دائمی کنید"),

    GROVE_CODEX("GROVE CODEX", "دانشنامهٔ بیشه"),
    GROVE_CODEX_SUBTITLE("Read what the Tree remembers", "آنچه درخت به یاد دارد را بخوانید"),

    /**
     * The two readouts under the title. The wave line is one entry with two arguments rather than a wave entry
     * and a separator, because "WAVE 12 / 200" is one sentence the screen draws in one call: splitting it would
     * put the decision about where the slash goes in the renderer instead of in the language.
     */
    CURRENT_WAVE("CURRENT WAVE", "موج جاری"),
    WAVE_VALUE("WAVE %1$s / %2$s", "موج %1$s / %2$s"),
    BOSS_WAVE("BOSS WAVE", "موج غول"),
    HERO_LEVEL("HERO LEVEL %1$s", "سطح قهرمان %1$s"),
    COINS("COINS", "سکه‌ها");

    private final String english;
    private final String persian;

    PauseStrings(String english, String persian) {
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
