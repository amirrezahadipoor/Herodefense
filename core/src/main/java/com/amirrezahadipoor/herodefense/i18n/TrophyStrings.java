package com.amirrezahadipoor.herodefense.i18n;

/**
 * The trophy shelf's words: a name and a line saying what it counts, in both shipped languages.
 *
 * <p>The names used to live in {@code Trophy} itself, in English only, and the shelf drew them straight to the
 * screen: a Persian player read "Steady Hand" and "Clear a thousand waves in total" inside a codex that was
 * otherwise in their own language. They live here now, one entry per half of a trophy row, because everything a
 * player reads has to come from a table: the font derives its glyphs from the tables, and both languages are
 * checked together by {@code TranslationTableTest}.
 *
 * <p>Twenty trophies, forty entries, in the order {@code Trophy} declares them -- which is the order the shelf
 * draws its rows in, and the order {@code TrophyText} maps by name.
 */
public enum TrophyStrings implements Translated {

    FIRST_VIGIL_TITLE("First Vigil", "نگهبانی نخست"),
    FIRST_VIGIL_HINT("Finish a run, however it ends.", "یک بازی را تمام کن، هرجور که تمام شود."),
    STEADY_HAND_TITLE("Steady Hand", "دستِ استوار"),
    STEADY_HAND_HINT("Clear a hundred waves in total.", "در مجموع صد موج را رد کن."),
    LONG_HOLD_TITLE("Long Hold", "پایداریِ بلند"),
    LONG_HOLD_HINT("Clear a thousand waves in total.", "در مجموع هزار موج را رد کن."),
    ENDLESS_PATIENCE_TITLE("Endless Patience", "صبوریِ بی‌پایان"),
    ENDLESS_PATIENCE_HINT("Clear five thousand waves in total.", "در مجموع پنج هزار موج را رد کن."),
    BARE_HANDS_TITLE("Bare Hands", "دستِ خالی"),
    BARE_HANDS_HINT("Finish a run without ever reaching for a potion.", "یک بازی را بدون دست‌زدن به هیچ شربتی تمام کن."),
    GARDENER_TITLE("Gardener", "باغبان"),
    GARDENER_HINT("Plant all three grove trees in one run.", "در یک بازی هر سه درختِ بیشه را بکار."),
    DEEP_ROOTED_TITLE("Deep-Rooted", "ریشه‌دوانده"),
    DEEP_ROOTED_HINT("Bank a thousand heartwood.", "هزار چوب‌دل ذخیره کن."),
    HOLLOW_ANSWERED_TITLE("The Hollow Answered", "حُفره پاسخ گرفت"),
    HOLLOW_ANSWERED_HINT("Reach wave 200.", "به موج ۲۰۰ برس."),
    TWELFTH_DESCENT_TITLE("Twelfth Descent", "فرودِ دوازدهم"),
    TWELFTH_DESCENT_HINT("Ascend twelve times.", "دوازده بار صعود کن."),
    THORN_COLLECTOR_TITLE("Thorn Collector", "خارگردآور"),
    THORN_COLLECTOR_HINT("Put down a hundred elites.", "صد نخبه را از پا دربیاور."),
    LOREKEEPER_TITLE("Lorekeeper", "نگهدارِ روایت‌ها"),
    LOREKEEPER_HINT("Have twenty codex entries written.", "بیست مدخلِ دانش‌نامه نوشته شود."),
    WITNESS_TITLE("Witness", "شاهد"),
    WITNESS_HINT("Meet all eight boss identities at least once.", "هر هشت هویتِ باس را دست‌کم یک‌بار ببین."),
    TEN_NIGHTS_TITLE("Ten Nights", "ده شب"),
    TEN_NIGHTS_HINT("Finish ten runs.", "ده بازی را تمام کن."),
    BEST_WAVE_HUNDRED_TITLE("A Hundred Deep", "صد موج فراتر"),
    BEST_WAVE_HUNDRED_HINT("Reach wave 100 in a single run.", "در یک بازی به موج ۱۰۰ برس."),
    BEST_WAVE_ONE_FIFTY_TITLE("Half Again as Deep", "نیم‌بارِ دیگر عمیق‌تر"),
    BEST_WAVE_ONE_FIFTY_HINT("Reach wave 150 in a single run.", "در یک بازی به موج ۱۵۰ برس."),
    ELITE_HUNTER_TITLE("Elite Hunter", "شکارچیِ نخبه"),
    ELITE_HUNTER_HINT("Put down twenty-five elites.", "بیست‌وپنج نخبه را از پا دربیاور."),
    ELITE_LEGION_TITLE("Thorn Legion", "لژیونِ خار"),
    ELITE_LEGION_HINT("Put down five hundred elites.", "پانصد نخبه را از پا دربیاور."),
    FORESTER_TITLE("Forester", "جنگل‌بان"),
    FORESTER_HINT("Plant twelve grove trees in total.", "در مجموع دوازده درختِ بیشه بکار."),
    FIVE_CLEAN_RUNS_TITLE("Five Clean Runs", "پنج بازیِ پاک"),
    FIVE_CLEAN_RUNS_HINT("Finish five runs without a potion.", "پنج بازی را بدون شربت تمام کن."),
    LORE_MASTER_TITLE("Grove Historian", "تاریخ‌نگارِ بیشه"),
    LORE_MASTER_HINT("Have forty codex entries written.", "چهل مدخلِ دانش‌نامه نوشته شود.");

    private final String english;
    private final String persian;

    TrophyStrings(String english, String persian) {
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
