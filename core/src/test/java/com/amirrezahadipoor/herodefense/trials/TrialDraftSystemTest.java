package com.amirrezahadipoor.herodefense.trials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

final class TrialDraftSystemTest {
    private final TrialDraftSystem draft = new TrialDraftSystem();

    @Test
    void prepareOfferDealsFourUniqueTrialsDeterministicallyFromTheRunSeed() {
        GameState first = GameState.newRun(42L);
        GameState second = GameState.newRun(42L);
        draft.prepareOffer(first);
        draft.prepareOffer(second);
        assertEquals(TrialDraftSystem.OFFER_COUNT, first.pendingTrialOffer.size());
        assertEquals(first.pendingTrialOffer, second.pendingTrialOffer);
        assertEquals(
            TrialDraftSystem.OFFER_COUNT, new HashSet<>(first.pendingTrialOffer).size()
        );
        assertTrue(first.draftPending());
        assertTrue(first.trialDraftPicks.isEmpty());
        assertTrue(first.activeTrials.isEmpty());
    }

    @Test
    void differentSeedsDoNotAllDealTheSameOffer() {
        GameState baseline = GameState.newRun(1L);
        draft.prepareOffer(baseline);
        boolean anyDifferent = false;
        for (long seed = 2L; seed <= 6L; seed++) {
            GameState other = GameState.newRun(seed);
            draft.prepareOffer(other);
            if (!other.pendingTrialOffer.equals(baseline.pendingTrialOffer)) {
                anyDifferent = true;
            }
        }
        assertTrue(anyDifferent);
    }

    @Test
    void twoPicksBindThePairAndClearTheOffer() {
        GameState state = GameState.newRun(7L);
        draft.prepareOffer(state);
        assertTrue(draft.choosePath(state, 0));
        String firstPick = state.pendingTrialOffer.get(0);
        String secondPick = state.pendingTrialOffer.get(1);

        assertFalse(draft.chooseTrial(state, 0));
        assertEquals(1, state.trialDraftPicks.size());
        assertTrue(state.draftPending());

        assertTrue(draft.chooseTrial(state, 1));
        assertEquals(2, state.activeTrials.size());
        assertTrue(state.activeTrials.contains(firstPick));
        assertTrue(state.activeTrials.contains(secondPick));
        assertTrue(state.pendingTrialOffer.isEmpty());
        assertTrue(state.trialDraftPicks.isEmpty());
        assertFalse(state.draftPending());
    }

    @Test
    void duplicateAndOutOfRangePicksAreIgnored() {
        GameState state = GameState.newRun(8L);
        draft.prepareOffer(state);
        assertTrue(draft.choosePath(state, 3));
        assertFalse(draft.chooseTrial(state, -1));
        assertFalse(draft.chooseTrial(state, TrialDraftSystem.OFFER_COUNT));
        assertFalse(draft.chooseTrial(state, 0));
        assertFalse(draft.chooseTrial(state, 0));
        assertEquals(1, state.trialDraftPicks.size());
        assertTrue(state.draftPending());
    }

    @Test
    void prepareOfferDiscardsAnyPreviousDraftState() {
        GameState state = GameState.newRun(9L);
        draft.prepareOffer(state);
        draft.choosePath(state, 2);
        draft.chooseTrial(state, 0);
        draft.prepareOffer(state);
        assertEquals(TrialDraftSystem.OFFER_COUNT, state.pendingTrialOffer.size());
        assertTrue(state.trialDraftPicks.isEmpty());
        assertTrue(state.activeTrials.isEmpty());
        assertNull(state.heroPath, "a fresh offer reopens the path phase");
    }

    @Test
    void noTrialBindsBeforeThePathIsChosen() {
        GameState state = GameState.newRun(11L);
        draft.prepareOffer(state);
        assertFalse(draft.chooseTrial(state, 0));
        assertFalse(draft.chooseTrial(state, 1));
        assertTrue(state.trialDraftPicks.isEmpty(), "the path phase gates every trial pick");
        assertTrue(state.draftPending(), "the offer stays open across the path phase");

        assertTrue(draft.choosePath(state, 1));
        assertEquals("ROOT", state.heroPath);
        assertTrue(state.draftPending(), "choosing a path does not close the draft");

        assertFalse(draft.chooseTrial(state, 0));
        assertTrue(draft.chooseTrial(state, 1));
        assertEquals(2, state.activeTrials.size());
    }

    @Test
    void thePathBindsOnceAndIgnoresGarbage() {
        GameState state = GameState.newRun(12L);
        draft.prepareOffer(state);
        assertFalse(draft.choosePath(state, -1));
        assertFalse(draft.choosePath(state, 4));
        assertNull(state.heroPath);
        assertTrue(draft.choosePath(state, 2));
        assertEquals("WIND", state.heroPath);
        assertFalse(draft.choosePath(state, 0), "a bound path is not re-chosen inside the run");
        assertEquals("WIND", state.heroPath);
    }
}
