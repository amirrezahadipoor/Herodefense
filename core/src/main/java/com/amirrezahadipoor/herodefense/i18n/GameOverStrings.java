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
    WAVE_REACHED("Wave reached", "موج رسیده"),
    BOSSES_DEFEATED("Bosses defeated", "غول‌های شکست‌خورده"),
    ENEMIES_DEFEATED("Enemies defeated", "دشمنان شکست‌خورده"),
    COINS_EARNED("Kill coins earned", "سکه‌های کسب‌شده"),
    HERO_LEVEL("Hero level", "سطح قهرمان"),
    MYTHIC_EARNED("Mythic earned", "افسانه‌های کسب‌شده"),

    DEFEND_AGAIN("DEFEND AGAIN", "دفاع دوباره"),
    RESTART_AT_WAVE_ONE("RESTART AT WAVE 1", "شروع دوباره از موج ۱"),
    DEFEND_AGAIN_SUBTITLE("Begin fresh run same tier", "یک دور تازه در همان رده"),

    ASCEND("ASCEND  |  +%1$s", "صعود  |  +%1$s"),
    ASCEND_SUBTITLE("  |  Harder foes, permanent roots", "  |  دشمنان سخت‌تر، ریشه‌های دائمی"),
    ROOT_NETWORK("ROOT NETWORK  |  %1$s", "شبکه ریشه  |  %1$s"),
    HEARTWOOD_TOTAL("%1$s HEARTWOOD", "%1$s چوب دل"),
    TIER("Tier %1$s", "ردهٔ %1$s"),
    BOSSES_COUNT("%1$s / 20", "%1$s / ۲۰");

    private final String english;
    private final String persian;

    GameOverStrings(String english, String persian) {
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
