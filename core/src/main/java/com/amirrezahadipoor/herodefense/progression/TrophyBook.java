package com.amirrezahadipoor.herodefense.progression;

import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads the trophies off the player's persistent progress (roadmap R3.3).
 *
 * <p>Two jobs, both pure functions of a {@link GameState}:
 * <ul>
 *   <li>{@link #progress} — how far along a trophy is, so the codex can show it honestly.</li>
 *   <li>{@link #evaluate} — awards everything that is due and reports what was new. It is idempotent, so the game
 *       can call it at every save point instead of threading an event through every system.</li>
 * </ul>
 *
 * <p>{@link #migrate} is the save migration the roadmap item asks for: a save written before the ledger existed
 * has no ledger at all, so a returning player's progress (heartwood, ascensions, best wave, codex entries, boss
 * first kills) is read once and converted into the trophies it would have earned. Without it, a veteran would
 * start the new build with an empty trophy case and no way to tell that the game forgot.
 */
public final class TrophyBook {

    private TrophyBook() {
    }

    /** How many of this trophy's units the player has, capped at the target so a bar cannot overshoot. */
    public static int progress(GameState state, Trophy trophy) {
        if (state == null || trophy == null) {
            return 0;
        }
        TrophyLedger ledger = ledgerOf(state);
        return Math.min(trophy.target(), rawProgress(state, ledger, trophy));
    }

    private static int rawProgress(GameState state, TrophyLedger ledger, Trophy trophy) {
        return switch (trophy) {
            case FIRST_VIGIL -> state.totalRunsCompleted + state.wave200ReachedCount;
            case STEADY_HAND, LONG_HOLD, ENDLESS_PATIENCE -> ledger.wavesCleared;
            case BARE_HANDS -> ledger.potionlessFinishes;
            case GARDENER -> ledger.mostTreesInOneRun;
            case DEEP_ROOTED -> state.heartwood;
            case HOLLOW_ANSWERED -> state.wave200ReachedCount;
            case TWELFTH_DESCENT -> state.ascensionTier;
            case THORN_COLLECTOR -> elitesKilled(state);
            case LOREKEEPER -> writtenEntries(state);
            case WITNESS -> state.firstBossKills == null ? 0 : state.firstBossKills.size();
            // The shelf's second half: runs finished, how deep one night has been, and what the deep band cost.
            case TEN_NIGHTS -> state.totalRunsCompleted;
            case BEST_WAVE_HUNDRED, BEST_WAVE_ONE_FIFTY -> ledger.bestWave;
            case ELITE_HUNTER, ELITE_LEGION -> elitesKilled(state);
            case FORESTER -> ledger.treesPlanted;
            case FIVE_CLEAN_RUNS -> ledger.potionlessFinishes;
            case LORE_MASTER -> writtenEntries(state);
        };
    }

    /** Awards every trophy that is due; returns only the ones that were new, in trophy order. */
    public static List<Trophy> evaluate(GameState state) {
        List<Trophy> awarded = new ArrayList<>();
        if (state == null) {
            return awarded;
        }
        TrophyLedger ledger = ledgerOf(state);
        for (Trophy trophy : Trophy.values()) {
            if (rawProgress(state, ledger, trophy) >= trophy.target() && ledger.award(trophy)) {
                awarded.add(trophy);
            }
        }
        return awarded;
    }

    /**
     * Brings an old save's ledger up to the current rules. Returns the trophies the save had already earned, so
     * the caller can decide whether to tell the player; the ledger is marked read either way, so this runs once per
     * save.
     */
    public static List<Trophy> migrate(GameState state) {
        if (state == null) {
            return new ArrayList<>();
        }
        TrophyLedger ledger = ledgerOf(state);
        ledger.repair();
        List<Trophy> backfilled = new ArrayList<>();
        if (ledger.ledgerVersion < TrophyLedger.CURRENT_LEDGER_VERSION) {
            for (Trophy trophy : Trophy.values()) {
                if (rawProgress(state, ledger, trophy) >= trophy.target() && ledger.award(trophy)) {
                    backfilled.add(trophy);
                }
            }
            ledger.ledgerVersion = TrophyLedger.CURRENT_LEDGER_VERSION;
        }
        return backfilled;
    }

    /** Elites put down, summed over the affix counters the run already keeps. */
    private static int elitesKilled(GameState state) {
        if (state.eliteKillCounts == null) {
            return 0;
        }
        int total = 0;
        for (Map.Entry<String, Integer> entry : state.eliteKillCounts.entrySet()) {
            if (entry.getValue() != null) total += Math.max(0, entry.getValue());
        }
        return total;
    }

    /** Codex entries the Tree has actually written, i.e. unlocked and not merely listed. */
    private static int writtenEntries(GameState state) {
        if (state.codexUnlocked == null) {
            return 0;
        }
        int written = 0;
        for (Boolean unlocked : state.codexUnlocked.values()) {
            if (Boolean.TRUE.equals(unlocked)) written++;
        }
        return written;
    }

    private static TrophyLedger ledgerOf(GameState state) {
        if (state.trophies == null) {
            state.trophies = new TrophyLedger();
        } else {
            state.trophies.repair();
        }
        return state.trophies;
    }
}
