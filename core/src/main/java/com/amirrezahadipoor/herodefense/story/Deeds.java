package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.model.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Vigil Deeds (roadmap ST2): named goals the run itself completes, each paid once in coins.
 * A deed is derived from state the run already keeps -- no new counters -- and claimed through the
 * codex ledger, so a resumed run neither re-pays nor forgets one. The reward lands at the moment
 * the deed completes, mid-wave, where the kill income already flows.
 */
public enum Deeds {
    WAVE_10(StoryStrings.DEED_WAVE_10, "deed_wave_10", 100),
    WAVE_25(StoryStrings.DEED_WAVE_25, "deed_wave_25", 150),
    WAVE_50(StoryStrings.DEED_WAVE_50, "deed_wave_50", 250),
    WAVE_100(StoryStrings.DEED_WAVE_100, "deed_wave_100", 400),
    WAVE_150(StoryStrings.DEED_WAVE_150, "deed_wave_150", 600),
    WAVE_200(StoryStrings.DEED_WAVE_200, "deed_wave_200", 1000),
    FIRST_BOSS(StoryStrings.DEED_FIRST_BOSS, "deed_first_boss", 150),
    BOSSES_5(StoryStrings.DEED_BOSSES_5, "deed_bosses_5", 300),
    CLEAN_25(StoryStrings.DEED_CLEAN_25, "deed_clean_25", 200),
    FLAWLESS_50(StoryStrings.DEED_FLAWLESS_50, "deed_flawless_50", 500),
    CODEX_10(StoryStrings.DEED_CODEX_10, "deed_codex_10", 300);

    /** Codex-ledger key prefix every deed is claimed with. */
    public static final String KEY_PREFIX = "deed_";

    private final StoryStrings label;
    private final String key;
    private final int reward;

    Deeds(StoryStrings label, String key, int reward) {
        this.label = label;
        this.key = key;
        this.reward = reward;
    }

    public String key() {
        return key;
    }

    public int reward() {
        return reward;
    }

    /** The completed line, reward included, in the running language. */
    public String announce() {
        return GameLocale.text(label, GameLocale.number(reward));
    }

    /** Whether the run's own state has earned this deed right now. */
    public boolean earnedBy(GameState state) {
        return switch (this) {
            case WAVE_10 -> state.waveNumber >= 10;
            case WAVE_25 -> state.waveNumber >= 25;
            case WAVE_50 -> state.waveNumber >= 50;
            case WAVE_100 -> state.waveNumber >= 100;
            case WAVE_150 -> state.waveNumber >= 150;
            case WAVE_200 -> state.waveNumber >= GameState.FINAL_WAVE;
            case FIRST_BOSS -> state.firstBossKills != null && !state.firstBossKills.isEmpty();
            case BOSSES_5 -> state.defeatedBosses >= 5;
            case CLEAN_25 -> state.waveNumber >= 25 && !state.heroDiedThisRun
                && state.potionsUsedThisRun == 0;
            case FLAWLESS_50 -> state.waveNumber >= 50 && !state.heroDiedThisRun
                && state.potionsUsedThisRun == 0;
            case CODEX_10 -> codexPagesRead(state) >= 10;
        };
    }

    /** The run's pages of the codex actually read -- the ledger's own entries never count. */
    static int codexPagesRead(GameState state) {
        if (state == null || state.codexUnlocked == null) {
            return 0;
        }
        int pages = 0;
        for (Map.Entry<String, Boolean> entry : state.codexUnlocked.entrySet()) {
            String key = entry.getKey();
            if (key != null && key.startsWith("codex_")
                && Boolean.TRUE.equals(entry.getValue())) {
                pages++;
            }
        }
        return pages;
    }

    /**
     * Pays and marks every deed the current state has just earned; returns them in announce order.
     * A deed already in the ledger is never paid twice, and a state too fresh for the ledger pays
     * nothing rather than crashing.
     */
    public static List<Deeds> completeNewlyEarned(GameState state) {
        List<Deeds> newlyCompleted = new ArrayList<>();
        if (state == null || state.codexUnlocked == null) {
            return newlyCompleted;
        }
        for (Deeds deed : values()) {
            if (Boolean.TRUE.equals(state.codexUnlocked.get(deed.key))) {
                continue;
            }
            if (deed.earnedBy(state)) {
                state.codexUnlocked.put(deed.key, Boolean.TRUE);
                state.coins = Math.min(Integer.MAX_VALUE, state.coins + deed.reward);
                newlyCompleted.add(deed);
            }
        }
        return newlyCompleted;
    }
}
