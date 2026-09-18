package com.amirrezahadipoor.herodefense.i18n;

/**
 * The run-summary screen's words (roadmap R7.3), in both shipped languages.
 *
 * <p>This is the screen a player reads after losing a long run, so its five measurement rows are named rather
 * than abbreviated. Values were literals in {@code render/GameOverOverlayRenderer}.
 */
public enum GameOverStrings implements Translated {

    /** The headline: which of the two endings this was. */
    DEFEAT("DEFEAT", "شکست خوردید"),
    RUN_COMPLETE("RUN COMPLETE", "دور کامل شد"),

    SUMMARY_TITLE("RUN SUMMARY", "خلاصهٔ دور"),
    WAVE_REACHED("Wave reached", "بالاترین موج"),
    BOSSES_DEFEATED("Bosses defeated", "غول‌های شکست‌خورده"),
    ENEMIES_DEFEATED("Enemies defeated", "دشمنان شکست‌خورده"),
    COINS_EARNED("Kill coins earned", "سکه‌های کسب‌شده"),
    HERO_LEVEL("Hero level", "سطح قهرمان"),
    MYTHIC_EARNED("Mythic earned", "اسطوره‌های کسب‌شده"),

    /**
     * The wave row's value: this run's last wave over the length the run was set to. It is one entry with two
     * arguments rather than a number, a separator and a number, because where the slash sits and which side of
     * it each number goes on is a property of the language, not of the renderer.
     */
    WAVE_COUNT("%1$s / %2$s", "%1$s / %2$s"),

    /**
     * The boss row's value. The total is written into both languages rather than passed in: the screen has always
     * drawn a literal 20 here, and a table that holds the sentence is the place that number belongs now.
     */
    BOSSES_COUNT("%1$s / 20", "%1$s / ۲۰"),

    DEFEND_AGAIN("DEFEND AGAIN", "دفاع دوباره"),
    RESTART_AT_WAVE_ONE("RESTART AT WAVE 1", "شروع دوباره از موج ۱"),
    DEFEND_AGAIN_SUBTITLE("Begin fresh run same tier", "یک دور تازه در همان رده"),

    /**
     * The ascend row: what it pays, and what the tier does next. The two languages disagree about how to draw
     * "3 becomes 4" and neither can borrow the other's arrow: U+2190 is in neither shipped face, and an ASCII
     * {@code ->} inside a right-to-left sentence is mirrored by the bidi pass into {@code -<}, which points the
     * wrong way. So Persian says it in a word -- "ردهٔ ۳ به ۴", tier three to four -- and English keeps its arrow,
     * which its own left-to-right run leaves alone.
     */
    ASCEND("ASCEND  |  +%1$s HEARTWOOD", "صعود  |  +%1$s چوب دل"),
    TIER_PROGRESS("Tier %1$s -> %2$s  |  Harder foes, permanent roots",
        "ردهٔ %1$s به %2$s  |  دشمنان سخت‌تر، ریشه‌های دائمی"),
    ROOT_NETWORK("ROOT NETWORK  |  %1$s HW", "شبکه ریشه  |  %1$s چوب دل"),
    ROOT_NETWORK_SUBTITLE("Spend Heartwood on permanent growth", "چوب دل را صرف رشد دائمی کنید");

    private final String english;
    private final String persian;

    GameOverStrings(String english, String persian) {
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
