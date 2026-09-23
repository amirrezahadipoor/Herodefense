package com.amirrezahadipoor.herodefense.i18n;

/**
 * The trophy shelf's words: a name and a line saying what it counts.
 *
 * <p>The names used to live in {@code Trophy} itself, and the shelf drew them straight to the
 * screen. They live here now, one entry per half of a trophy row, because everything a
 * player reads has to come from a table: the font derives its glyphs from the tables, and
 * {@code TranslationTableTest} checks them together.
 *
 * <p>Twenty trophies, forty entries, in the order {@code Trophy} declares them -- which is the order the shelf
 * draws its rows in, and the order {@code TrophyText} maps by name.
 */
public enum TrophyStrings implements Translated {

    FIRST_VIGIL_TITLE("First Vigil"),
    FIRST_VIGIL_HINT("Finish a run, however it ends."),
    STEADY_HAND_TITLE("Steady Hand"),
    STEADY_HAND_HINT("Clear a hundred waves in total."),
    LONG_HOLD_TITLE("Long Hold"),
    LONG_HOLD_HINT("Clear a thousand waves in total."),
    ENDLESS_PATIENCE_TITLE("Endless Patience"),
    ENDLESS_PATIENCE_HINT("Clear five thousand waves in total."),
    BARE_HANDS_TITLE("Bare Hands"),
    BARE_HANDS_HINT("Finish a run without ever reaching for a potion."),
    GARDENER_TITLE("Gardener"),
    GARDENER_HINT("Plant all three grove trees in one run."),
    DEEP_ROOTED_TITLE("Deep-Rooted"),
    DEEP_ROOTED_HINT("Bank a thousand heartwood."),
    HOLLOW_ANSWERED_TITLE("The Hollow Answered"),
    HOLLOW_ANSWERED_HINT("Reach wave 200."),
    TWELFTH_DESCENT_TITLE("Twelfth Descent"),
    TWELFTH_DESCENT_HINT("Ascend twelve times."),
    THORN_COLLECTOR_TITLE("Thorn Collector"),
    THORN_COLLECTOR_HINT("Put down a hundred elites."),
    LOREKEEPER_TITLE("Lorekeeper"),
    LOREKEEPER_HINT("Have twenty codex entries written."),
    WITNESS_TITLE("Witness"),
    WITNESS_HINT("Meet all eight boss identities at least once."),
    TEN_NIGHTS_TITLE("Ten Nights"),
    TEN_NIGHTS_HINT("Finish ten runs."),
    BEST_WAVE_HUNDRED_TITLE("A Hundred Deep"),
    BEST_WAVE_HUNDRED_HINT("Reach wave 100 in a single run."),
    BEST_WAVE_ONE_FIFTY_TITLE("Half Again as Deep"),
    BEST_WAVE_ONE_FIFTY_HINT("Reach wave 150 in a single run."),
    ELITE_HUNTER_TITLE("Elite Hunter"),
    ELITE_HUNTER_HINT("Put down twenty-five elites."),
    ELITE_LEGION_TITLE("Thorn Legion"),
    ELITE_LEGION_HINT("Put down five hundred elites."),
    FORESTER_TITLE("Forester"),
    FORESTER_HINT("Plant twelve grove trees in total."),
    FIVE_CLEAN_RUNS_TITLE("Five Clean Runs"),
    FIVE_CLEAN_RUNS_HINT("Finish five runs without a potion."),
    LORE_MASTER_TITLE("Grove Historian"),
    LORE_MASTER_HINT("Have forty codex entries written.");

    private final String english;

    TrophyStrings(String english) {
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
