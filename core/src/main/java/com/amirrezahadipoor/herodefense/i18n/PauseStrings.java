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

    INVENTORY("INVENTORY", "کیسهٔ پشت"),
    INVENTORY_SUBTITLE("Equip, compare, and sell gear", "تجهیز، مقایسه و فروش تجهیزات"),

    STAT_SHOP("STAT SHOP", "فروشگاه توانمندی"),
    STAT_SHOP_SUBTITLE("Spend earned coins on permanent upgrades", "سکه‌های کسب‌شده را صرف ارتقای دائمی کنید"),

    ROOT_NETWORK("ROOT NETWORK", "شبکه ریشه"),
    ROOT_NETWORK_SUBTITLE("Spend Heartwood on permanent growth", "چوب دل را صرف رشد دائمی کنید"),

    GROVE_CODEX("GROVE CODEX", "کدکس بیشه"),
    GROVE_CODEX_SUBTITLE("Read what the Tree remembers", "آنچه درخت به یاد دارد را بخوانید"),

    /** The two readouts under the title. */
    CURRENT_WAVE("CURRENT WAVE", "موج جاری"),
    WAVE_VALUE("WAVE %1$s", "موج %1$s"),
    BOSS_WAVE("BOSS WAVE", "موج غول"),
    HERO_LEVEL("HERO LEVEL %1$s", "سطح قهرمان %1$s"),
    COINS("COINS", "سکه‌ها"),
    COIN_VALUE("%1$s / %2$s", "%1$s / %2$s");

    private final String english;
    private final String persian;

    PauseStrings(String english, String persian) {
        this.english = english;
        this.persian = persian;
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
