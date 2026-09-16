package com.amirrezahadipoor.herodefense.trials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class TrialIdTest {
    @Test
    void everyTrialsCarriesTitleRiskRewardAndItsOwnIcon() {
        assertEquals(13, TrialId.values().length);
        Set<String> iconKeys = new HashSet<>();
        for (TrialId trial : TrialId.values()) {
            assertFalse(trial.title().isBlank());
            assertFalse(trial.risk().isBlank());
            assertFalse(trial.reward().isBlank());
            assertFalse(trial.iconKey().isBlank());
            assertTrue(iconKeys.add(trial.iconKey()));
            assertEquals(trial, TrialId.forName(trial.name()));
        }
    }

    @Test
    void theOmenTrialNamesItsRiskAndRewardInTheDraft() {
        TrialId omen = TrialId.HOLLOW_OMENS;
        assertEquals("Hollow Omens", omen.title());
        assertTrue(omen.risk().contains("omen"), omen.risk());
        assertTrue(omen.reward().contains("25%"), omen.reward());
        assertEquals("", omen.lockHint(), "the thirteenth trial is open from the first run");
    }

    @Test
    void forNameResolvesUnknownNamesToNull() {
        assertNull(TrialId.forName(null));
        assertNull(TrialId.forName(""));
        assertNull(TrialId.forName("NOT_A_TRIAL"));
    }
}
