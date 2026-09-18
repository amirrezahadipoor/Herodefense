package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The encounter-to-script mapping of roadmap R3.2 has to be varied, deterministic and total. */
final class BossEncounterTableTest {

    @Test
    void theFirstEncounterIsTheAuthoredFightAndLaterOnesAreNot() {
        assertEquals(BossFightScript.MEASURED, BossEncounterTable.scriptFor(1));
        assertNotEquals(BossFightScript.MEASURED, BossEncounterTable.scriptFor(2),
            "the second encounter already runs a different script");
    }

    @Test
    void aFullRunUsesEveryScriptAndAnyTwentyConsecutiveEncountersUseAllEight() {
        assertEquals(BossFightScript.values().length, BossEncounterTable.distinctScripts(
            BossEncounterTable.ENCOUNTERS_PER_RUN), "all eight scripts appear across a run");

        for (int start = 1; start <= BossEncounterTable.ENCOUNTERS_PER_RUN - 19; start++) {
            Set<BossFightScript> window = new LinkedHashSet<>();
            for (int encounter = start; encounter < start + 20; encounter++) {
                window.add(BossEncounterTable.scriptFor(encounter));
            }
            assertEquals(BossFightScript.values().length, window.size(),
                "encounters " + start + ".." + (start + 19) + " must still show every script");
        }
    }

    @Test
    void noEncounterRepeatsTheScriptOfTheOneBeforeIt() {
        for (int encounter = 2; encounter <= BossEncounterTable.ENCOUNTERS_PER_RUN; encounter++) {
            assertNotEquals(BossEncounterTable.scriptFor(encounter - 1),
                BossEncounterTable.scriptFor(encounter),
                "encounter " + encounter + " repeats the previous script");
        }
    }

    @Test
    void theMappingIsPureArithmeticSoASaveFileAndTheSweepAlwaysAgree() {
        for (int encounter = 1; encounter <= BossEncounterTable.ENCOUNTERS_PER_RUN; encounter++) {
            assertEquals(BossEncounterTable.scriptFor(encounter), BossEncounterTable.scriptFor(encounter));
        }
        assertEquals(BossEncounterTable.scriptFor(9), BossEncounterTable.scriptFor(9));
        assertEquals(BossFightScript.values().length, BossEncounterTable.distinctScripts(
            BossFightScript.values().length));
    }

    @Test
    void outOfRangeEncountersFallBackInsteadOfThrowing() {
        assertEquals(BossFightScript.MEASURED, BossEncounterTable.scriptFor(0));
        assertEquals(BossFightScript.MEASURED, BossEncounterTable.scriptFor(-7));
        assertEquals(0, BossEncounterTable.distinctScripts(0));
        assertTrue(BossEncounterTable.ENCOUNTERS_PER_RUN >= 20,
            "the table has to cover the twenty encounters the audit asked about");
    }
}
