package com.amirrezahadipoor.herodefense.progression;

/**
 * The twenty trophies (roadmap R3.3): the achievements a run can earn and that survive every run after it.
 *
 * <p>Each one is a counter with a target, so the codex can show how far along the player is ("7 / 100") instead of
 * a locked box. The counters come from the persistent side of the game -- the ledger, the heartwood bank, the
 * ascension tier, the codex itself -- never from a single run, which is what keeps a trophy earned.
 *
 * <p>The words are not here. A trophy used to carry its own English name and hint, which the shelf drew straight to
 * the screen; both languages now live in {@code i18n/TrophyStrings}, and {@link TrophyText} is the one place the
 * two halves are joined. What is left in this file is the part a translator must never touch: what the trophy
 * counts, and how much of it.
 */
public enum Trophy {

    /** Finished the first run, whoever was left standing. */
    FIRST_VIGIL(1),
    STEADY_HAND(100),
    LONG_HOLD(1000),
    ENDLESS_PATIENCE(5000),
    BARE_HANDS(1),
    GARDENER(3),
    DEEP_ROOTED(1000),
    HOLLOW_ANSWERED(1),
    TWELFTH_DESCENT(12),
    THORN_COLLECTOR(100),
    LOREKEEPER(20),
    WITNESS(8),
    /** Eight more, so the shelf keeps filling after the first sitting: runs finished, how deep one night went, and
     * what the deep band's elites cost. */
    TEN_NIGHTS(10),
    BEST_WAVE_HUNDRED(100),
    BEST_WAVE_ONE_FIFTY(150),
    ELITE_HUNTER(25),
    ELITE_LEGION(500),
    FORESTER(12),
    FIVE_CLEAN_RUNS(5),
    LORE_MASTER(40);

    private final int target;

    Trophy(int target) {
        this.target = target;
    }

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    /** How many of whatever this trophy counts are needed. */
    public int target() {
        return target;
    }
}
