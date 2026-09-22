package com.amirrezahadipoor.herodefense.progression;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.TrophyStrings;
import com.amirrezahadipoor.herodefense.i18n.Translated;

/** Which half of {@code TrophyStrings} belongs to which trophy, in the language the game is speaking. */
public final class TrophyText {

    private TrophyText() {
    }

    /** The trophy's name in the current language, or an empty string for a null trophy. */
    public static String title(Trophy trophy) {
        Translated entry = titleEntry(trophy);
        return entry == null ? "" : GameLocale.text(entry);
    }

    /** What the trophy counts, in the current language, or an empty string for a null trophy. */
    public static String hint(Trophy trophy) {
        Translated entry = hintEntry(trophy);
        return entry == null ? "" : GameLocale.text(entry);
    }

    private static Translated titleEntry(Trophy trophy) {
        return trophy == null ? null : switch (trophy) {
            case FIRST_VIGIL -> TrophyStrings.FIRST_VIGIL_TITLE;
            case STEADY_HAND -> TrophyStrings.STEADY_HAND_TITLE;
            case LONG_HOLD -> TrophyStrings.LONG_HOLD_TITLE;
            case ENDLESS_PATIENCE -> TrophyStrings.ENDLESS_PATIENCE_TITLE;
            case BARE_HANDS -> TrophyStrings.BARE_HANDS_TITLE;
            case GARDENER -> TrophyStrings.GARDENER_TITLE;
            case DEEP_ROOTED -> TrophyStrings.DEEP_ROOTED_TITLE;
            case HOLLOW_ANSWERED -> TrophyStrings.HOLLOW_ANSWERED_TITLE;
            case TWELFTH_DESCENT -> TrophyStrings.TWELFTH_DESCENT_TITLE;
            case THORN_COLLECTOR -> TrophyStrings.THORN_COLLECTOR_TITLE;
            case LOREKEEPER -> TrophyStrings.LOREKEEPER_TITLE;
            case WITNESS -> TrophyStrings.WITNESS_TITLE;
            case TEN_NIGHTS -> TrophyStrings.TEN_NIGHTS_TITLE;
            case BEST_WAVE_HUNDRED -> TrophyStrings.BEST_WAVE_HUNDRED_TITLE;
            case BEST_WAVE_ONE_FIFTY -> TrophyStrings.BEST_WAVE_ONE_FIFTY_TITLE;
            case ELITE_HUNTER -> TrophyStrings.ELITE_HUNTER_TITLE;
            case ELITE_LEGION -> TrophyStrings.ELITE_LEGION_TITLE;
            case FORESTER -> TrophyStrings.FORESTER_TITLE;
            case FIVE_CLEAN_RUNS -> TrophyStrings.FIVE_CLEAN_RUNS_TITLE;
            case LORE_MASTER -> TrophyStrings.LORE_MASTER_TITLE;
        };
    }

    private static Translated hintEntry(Trophy trophy) {
        return trophy == null ? null : switch (trophy) {
            case FIRST_VIGIL -> TrophyStrings.FIRST_VIGIL_HINT;
            case STEADY_HAND -> TrophyStrings.STEADY_HAND_HINT;
            case LONG_HOLD -> TrophyStrings.LONG_HOLD_HINT;
            case ENDLESS_PATIENCE -> TrophyStrings.ENDLESS_PATIENCE_HINT;
            case BARE_HANDS -> TrophyStrings.BARE_HANDS_HINT;
            case GARDENER -> TrophyStrings.GARDENER_HINT;
            case DEEP_ROOTED -> TrophyStrings.DEEP_ROOTED_HINT;
            case HOLLOW_ANSWERED -> TrophyStrings.HOLLOW_ANSWERED_HINT;
            case TWELFTH_DESCENT -> TrophyStrings.TWELFTH_DESCENT_HINT;
            case THORN_COLLECTOR -> TrophyStrings.THORN_COLLECTOR_HINT;
            case LOREKEEPER -> TrophyStrings.LOREKEEPER_HINT;
            case WITNESS -> TrophyStrings.WITNESS_HINT;
            case TEN_NIGHTS -> TrophyStrings.TEN_NIGHTS_HINT;
            case BEST_WAVE_HUNDRED -> TrophyStrings.BEST_WAVE_HUNDRED_HINT;
            case BEST_WAVE_ONE_FIFTY -> TrophyStrings.BEST_WAVE_ONE_FIFTY_HINT;
            case ELITE_HUNTER -> TrophyStrings.ELITE_HUNTER_HINT;
            case ELITE_LEGION -> TrophyStrings.ELITE_LEGION_HINT;
            case FORESTER -> TrophyStrings.FORESTER_HINT;
            case FIVE_CLEAN_RUNS -> TrophyStrings.FIVE_CLEAN_RUNS_HINT;
            case LORE_MASTER -> TrophyStrings.LORE_MASTER_HINT;
        };
    }
}
