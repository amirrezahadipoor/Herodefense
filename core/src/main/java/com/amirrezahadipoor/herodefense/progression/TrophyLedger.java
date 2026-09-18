package com.amirrezahadipoor.herodefense.progression;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What the trophies remember between runs, and the only part of the save that a run may not reset (roadmap R3.3).
 *
 * <p>It lives in {@code GameState} as one field so a save carries it, and {@code GameState.resetForNewRun} hands the
 * same ledger to the fresh run instead of copying numbers around. {@link #ledgerVersion} is what makes an old save
 * migratable: a save written before trophies existed has no ledger at all, and {@link TrophyBook#migrate} fills in
 * whatever that save had already earned instead of pretending the player never did it.
 */
public final class TrophyLedger {

    /** Bumped when a trophy's meaning changes, so {@link TrophyBook#migrate} knows a save needs re-reading. */
    public static final int CURRENT_LEDGER_VERSION = 1;

    /** Waves the player has cleared across every run. */
    public int wavesCleared;
    /** Longest run the player has finished, in waves. */
    public int bestWave;
    /** Runs finished without a single potion. */
    public int potionlessFinishes;
    /** Grove trees planted, ever. */
    public int treesPlanted;
    /** Most grove trees standing at once in a single run. */
    public int mostTreesInOneRun;
    /** Trophies already earned, by id. */
    public Map<String, Boolean> earned = new LinkedHashMap<>();
    /** Which reading of the trophy rules this ledger has already been through. */
    public int ledgerVersion;

    /** One more wave behind the player. */
    public void recordWaveCleared() {
        wavesCleared = Math.max(0, wavesCleared) + 1;
    }

    /** One more grove tree standing; also remembers the best single run. */
    public void recordTreePlanted(int treesStandingNow) {
        treesPlanted = Math.max(0, treesPlanted) + 1;
        mostTreesInOneRun = Math.max(mostTreesInOneRun, treesStandingNow);
    }

    /** The end of a run, whichever way it ended. */
    public void recordRunEnd(int wavesReached, boolean usedNoPotion) {
        bestWave = Math.max(bestWave, wavesReached);
        if (usedNoPotion) {
            potionlessFinishes = Math.max(0, potionlessFinishes) + 1;
        }
    }

    public boolean isEarned(Trophy trophy) {
        return trophy != null && Boolean.TRUE.equals(earned.get(trophy.id()));
    }

    /** Marks a trophy earned; returns false when it already was. */
    public boolean award(Trophy trophy) {
        if (trophy == null || isEarned(trophy)) {
            return false;
        }
        earned.put(trophy.id(), Boolean.TRUE);
        return true;
    }

    public int earnedCount() {
        int count = 0;
        for (Trophy trophy : Trophy.values()) {
            if (isEarned(trophy)) count++;
        }
        return count;
    }

    /** Keeps a decoded save usable: missing maps appear, negative counters go back to zero. */
    public void repair() {
        if (earned == null) earned = new LinkedHashMap<>();
        wavesCleared = Math.max(0, wavesCleared);
        bestWave = Math.max(0, bestWave);
        potionlessFinishes = Math.max(0, potionlessFinishes);
        treesPlanted = Math.max(0, treesPlanted);
        mostTreesInOneRun = Math.max(0, mostTreesInOneRun);
        ledgerVersion = Math.max(0, ledgerVersion);
    }
}
