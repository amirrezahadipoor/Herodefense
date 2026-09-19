package com.amirrezahadipoor.herodefense.progression;

/**
 * The twelve trophies (roadmap R3.3): the achievements a run can earn and that survive every run after it.
 *
 * <p>Each one is a counter with a target, so the codex can show how far along the player is ("7 / 100") instead of
 * a locked box. The counters come from the persistent side of the game — the ledger, the heartwood bank, the
 * ascension tier, the codex itself — never from a single run, which is what keeps a trophy earned.
 */
public enum Trophy {

    FIRST_VIGIL("First Vigil", "Finish a run, however it ends.", 1),
    STEADY_HAND("Steady Hand", "Clear a hundred waves in total.", 100),
    LONG_HOLD("Long Hold", "Clear a thousand waves in total.", 1000),
    ENDLESS_PATIENCE("Endless Patience", "Clear five thousand waves in total.", 5000),
    BARE_HANDS("Bare Hands", "Finish a run without ever reaching for a potion.", 1),
    GARDENER("Gardener", "Plant all three grove trees in one run.", 3),
    DEEP_ROOTED("Deep-Rooted", "Bank a thousand heartwood.", 1000),
    HOLLOW_ANSWERED("The Hollow Answered", "Reach wave 200.", 1),
    TWELFTH_DESCENT("Twelfth Descent", "Ascend twelve times.", 12),
    THORN_COLLECTOR("Thorn Collector", "Put down a hundred elites.", 100),
    LOREKEEPER("Lorekeeper", "Have twenty codex entries written.", 20),
    WITNESS("Witness", "Meet all eight boss identities at least once.", 8);

    private final String title;
    private final String hint;
    private final int target;

    Trophy(String title, String hint, int target) {
        this.title = title;
        this.hint = hint;
        this.target = target;
    }

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public String title() {
        return title;
    }

    public String hint() {
        return hint;
    }

    /** How many of whatever this trophy counts are needed. */
    public int target() {
        return target;
    }
}
