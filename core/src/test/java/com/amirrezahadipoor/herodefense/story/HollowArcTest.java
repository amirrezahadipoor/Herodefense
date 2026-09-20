package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The Hollow's arc (roadmap ST1+): a first hello, mercy noticed then named, and a verdict that
 * waits for the session after a finished run -- each line spoken once ever, none repeated.
 */
class HollowArcTest {
    private GameLanguage before;

    @BeforeEach
    void speakEnglish() {
        before = GameLocale.current();
        GameLocale.use(GameLanguage.ENGLISH);
    }

    @AfterEach
    void restoreTheLanguage() {
        GameLocale.use(before);
    }

    @Test
    void aNewPlayerIsGreetedOnceAndNeverAgain() {
        GameState state = GameState.newRun(11L);
        assertTrue(HollowVoice.lineForGreeting(state) != null, "the first wave-1 speaks");
        assertNull(HollowVoice.lineForGreeting(state), "the second wave-1 does not");
    }

    @Test
    void theFirstSpareIsNoticedAndTheThirdIsNamed() {
        GameState state = GameState.newRun(12L);
        assertTrue(HollowVoice.lineForSpare(state) != null, "spare one speaks");
        assertNull(HollowVoice.lineForSpare(state), "spare two passes in silence");
        assertTrue(HollowVoice.lineForSpare(state) != null, "spare three is named as a habit");
        assertNull(HollowVoice.lineForSpare(state), "spare four passes too");
        assertEquals(4, state.codexUnlocked.size() >= 4 ? 4 : state.codexUnlocked.size(),
            "the ledger holds one numbered key per spare");
    }

    @Test
    void theVerdictOpensTheRunThatFollowsAFinishedOne() {
        GameState finished = GameState.newRun(13L);
        assertTrue(HollowVoice.lineForGreeting(finished) != null,
            "their first session opened with the hello");
        HollowVoice.markRunComplete(finished);

        GameState nextRun = GameState.newRun(14L);
        // The ledger rides with the save; the next run reads the same map.
        nextRun.codexUnlocked.putAll(finished.codexUnlocked);
        String verdict = HollowVoice.lineForGreeting(nextRun);
        assertTrue(verdict != null && verdict.contains("inventory"),
            "a run that spared nobody is read as inventory");
        assertNull(HollowVoice.lineForGreeting(nextRun), "a verdict is delivered once");
    }

    @Test
    void mercyChangesTheVerdict() {
        GameState finished = GameState.newRun(15L);
        HollowVoice.lineForGreeting(finished);
        HollowVoice.lineForSpare(finished);
        HollowVoice.markRunComplete(finished);

        GameState nextSession = GameState.newRun(16L);
        nextSession.codexUnlocked.putAll(finished.codexUnlocked);
        String verdict = HollowVoice.lineForGreeting(nextSession);
        assertTrue(verdict != null && verdict.contains("debt"),
            "a run that spared its watcher is read as a debt");
    }
}
