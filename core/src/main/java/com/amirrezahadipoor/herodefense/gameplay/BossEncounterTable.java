package com.amirrezahadipoor.herodefense.gameplay;

/**
 * Which fight script each boss encounter runs (roadmap R3.2).
 *
 * <p>Bosses arrive every fifth wave up to wave 200, which is forty encounters. The table walks a fixed
 * permutation of the eight scripts and shifts it every lap, so an encounter never repeats the script of the one
 * before it and every script is seen inside any twenty consecutive encounters. The mapping is pure arithmetic on
 * the encounter number: no dice, no clock, so a save file and the balance sweep always agree on what the player
 * is about to fight.
 */
public final class BossEncounterTable {

    /** Bosses arrive on every fifth wave; wave 5 is encounter 1 and wave 200 is encounter 40. */
    public static final int ENCOUNTERS_PER_RUN = 40;

    /** Encounter 1 keeps the authored fight so a first-time player meets the reference behaviour. */
    private static final BossFightScript[] LAP_ZERO = {
        BossFightScript.MEASURED,
        BossFightScript.CRYPTIC_TELL,
        BossFightScript.SNAPPING_TELL,
        BossFightScript.ENRAGED_HEART,
        BossFightScript.PATIENT_WARDEN,
        BossFightScript.TWIN_TELEGRAPH,
        BossFightScript.BULWARK,
        BossFightScript.ASSASSIN,
    };

    private static final int LAP_SHIFT = 3;

    private BossEncounterTable() {
    }

    /** The script for one encounter number (1-based); anything below 1 gets the reference fight. */
    public static BossFightScript scriptFor(int bossNumber) {
        if (bossNumber < 1) return BossFightScript.MEASURED;
        int index = (bossNumber - 1) % LAP_ZERO.length;
        int lap = (bossNumber - 1) / LAP_ZERO.length;
        int shifted = (index + lap * LAP_SHIFT) % LAP_ZERO.length;
        return LAP_ZERO[shifted];
    }

    /** How many distinct scripts a run runs through, used by the tests and the balance report. */
    public static int distinctScripts(int encounters) {
        boolean[] seen = new boolean[BossFightScript.values().length];
        int distinct = 0;
        for (int encounter = 1; encounter <= encounters; encounter++) {
            BossFightScript script = scriptFor(encounter);
            if (!seen[script.ordinal()]) {
                seen[script.ordinal()] = true;
                distinct++;
            }
        }
        return distinct;
    }
}
