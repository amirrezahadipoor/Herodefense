package com.amirrezahadipoor.herodefense.i18n;

/**
 * The main menu's words (roadmap R7.3), in both shipped languages.
 *
 * <p>Every value here was a literal inside {@code render/MainMenuRenderer} before this table existed, and the
 * row it belonged to is named in the constant. The English side is unchanged character for character, including
 * the double spaces around the footer's separators, because those spaces are the layout.
 *
 * <p>The Persian side is written the way the screen reads rather than translated word by word: "HW" becomes
 * "چوب دل" because an abbreviation of an English name means nothing to a Persian reader, and the tier row uses
 * the Persian ordinal marker (ردهٔ) the way the rest of Iranian software does.
 */
public enum MenuStrings implements Translated {

    /** The game's name, drawn at 2.28x in gold. */
    TITLE("HERO DEFENSE", "دفاع قهرمان"),

    /** The line above the title. */
    TAGLINE("THE WORLD TREE AWAITS", "درخت جهان در انتظار است"),

    /** The two-line pitch under the title. */
    PITCH("Hold the last green sanctuary through 200 waves, or thirty",
        "آخرین پناهگاه سبز را در ۲۰۰ موج نگه دارید، یا سی موج"),

    /** Row 1. */
    NEW_GAME("NEW GAME", "بازی جدید"),
    NEW_GAME_SUBTITLE("Begin a fresh defense", "یک دفاع تازه را آغاز کنید"),

    /** Row 2, present only when the thirty-wave vigil is unlocked. */
    BRIEF_VIGIL("BRIEF VIGIL", "پاسداری کوتاه"),
    BRIEF_VIGIL_SUBTITLE("Thirty waves | same vigil, half the heartwood",
        "سی موج | همان پاسداری، نیم چوب دل"),

    /** Row 3, present only while a run is live. */
    CONTINUE("CONTINUE", "ادامه"),
    CONTINUE_SUBTITLE("Return to the active wave", "بازگشت به موج جاری"),

    /** Row 3's subtitle when there is progress to name: tier, best wave, and Heartwood. */
    PROGRESS_SUMMARY("Tier %1$s | Peak %2$s | %3$s HW", "ردهٔ %1$s | اوج %2$s | %3$s چوب دل"),

    /** Row 4. */
    ROOT_NETWORK("ROOT NETWORK", "شبکه ریشه"),
    ROOT_NETWORK_SUBTITLE("%1$s Heartwood | Permanent growth", "%1$s چوب دل | رشد دائمی"),

    /** Row 5. */
    GROVE_CODEX("GROVE CODEX", "کدکس بیشه"),
    GROVE_CODEX_SUBTITLE("Thirty entries the Tree remembers", "سی مدخلی که درخت به یاد دارد"),

    /** Row 6. */
    SETTINGS("SETTINGS", "تنظیمات"),
    SETTINGS_SUBTITLE("Comfort, music, and effects", "راحتی، موسیقی و جلوه‌ها"),

    /** The strip along the bottom of the screen. */
    FOOTER("200 WAVES  |  ONE LAST TREE  |  ASCEND FOREVER  |  T%1$s",
        "۲۰۰ موج  |  یک درخت آخر  |  صعود همیشگی  |  ردهٔ %1$s"),

    /** The coin count in the top corner. */
    COINS("$ %1$s", "%1$s سکه");

    private final String english;
    private final String persian;

    MenuStrings(String english, String persian) {
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
