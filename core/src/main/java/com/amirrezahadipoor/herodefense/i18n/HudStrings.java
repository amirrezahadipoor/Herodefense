package com.amirrezahadipoor.herodefense.i18n;

/**
 * The heads-up display's words (roadmap R7.3), in both shipped languages.
 *
 * <p>The HUD is the tightest space in the game and the one read most often: six labels and four counters, drawn
 * every frame. Persian words are longer than English ones -- "کیسهٔ پشت" for INVENTORY -- so the labels here are
 * the short forms and the layout has to fit them, which is what {@code HudLayoutTest} measures. Values were
 * literals in {@code render/HudRenderer}.
 */
public enum HudStrings implements Translated {

    WAVE("WAVE", "موج"),
    HEALTH("HEALTH", "جان"),
    COINS("COINS", "سکه"),
    SHOP("SHOP", "فروشگاه"),
    INVENTORY("INVENTORY", "کیسهٔ پشت"),
    ULTIMATE("ULTIMATE", "ضربهٔ نهایی"),

    /** The ultimate's charge, when it is not full. */
    CHARGE("%1$s/%2$s", "%1$s/%2$s"),
    /** The ultimate's charge, when it is. */
    READY("MAX", "کامل"),

    LEVEL("LV %1$s", "سطح %1$s"),
    GROVE("GROVE %1$s", "بیشهٔ %1$s"),
    XP("%1$s XP", "%1$s تجربه"),
    CRIT_CHANCE("%1$s%%", "%1$s٪"),
    COIN_TOTAL("$ %1$s", "%1$s سکه");

    private final String english;
    private final String persian;

    HudStrings(String english, String persian) {
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
