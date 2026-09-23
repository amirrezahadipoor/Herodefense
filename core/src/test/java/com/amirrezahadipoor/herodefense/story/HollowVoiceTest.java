package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/** The Hollow speaks once per line, to the player, and never repeats itself. */
class HollowVoiceTest {

    @Test
    void firstDeathAndLaterDeathGetDifferentLines() {
        GameState state = new GameState();
        String first = HollowVoice.lineForDeath(state);
        assertNotNull(first);
        String again = HollowVoice.lineForDeath(state);
        assertNotNull(again);
        assertTrue(Boolean.TRUE.equals(state.codexUnlocked.get(HollowVoice.KEY_DEATH_FIRST)));
        assertTrue(Boolean.TRUE.equals(state.codexUnlocked.get(HollowVoice.KEY_DEATH_AGAIN)));
        assertNull(HollowVoice.lineForDeath(state));
    }

    @Test
    void deathLinesDifferBetweenFirstAndLaterFalls() {
        GameState state = new GameState();
        String first = HollowVoice.lineForDeath(state);
        String again = HollowVoice.lineForDeath(state);
        assertNotNull(first);
        assertNotNull(again);
        assertFalse(first.equals(again));
    }

    @Test
    void spareLineIsClaimedExactlyOnce() {
        GameState state = new GameState();
        assertNotNull(HollowVoice.lineForSpare(state));
        assertNull(HollowVoice.lineForSpare(state));
    }

    @Test
    void wave100LineIsClaimedExactlyOnce() {
        GameState state = new GameState();
        assertNotNull(HollowVoice.lineForGroveCeremony(state));
        assertNull(HollowVoice.lineForGroveCeremony(state));
    }

    @Test
    void deathLineWaitsInTheQueueUntilShown() {
        GameState state = new GameState();
        assertNull(HollowVoice.pendingDeathLine(state));
        HollowVoice.lineForDeath(state);
        assertEquals(GameLocale.text(StoryStrings.HOLLOW_DEATH_FIRST),
            HollowVoice.pendingDeathLine(state));
        HollowVoice.markDeathLineShown(state);
        assertNull(HollowVoice.pendingDeathLine(state));
        HollowVoice.markDeathLineShown(state);
        assertNull(HollowVoice.pendingDeathLine(state));
    }

    @Test
    void aSecondDeathQueuesTheLaterLine() {
        GameState state = new GameState();
        HollowVoice.lineForDeath(state);
        HollowVoice.markDeathLineShown(state);
        HollowVoice.lineForDeath(state);
        assertEquals(GameLocale.text(StoryStrings.HOLLOW_DEATH_AGAIN),
            HollowVoice.pendingDeathLine(state));
    }

    @Test
    void hollowLinesAreNeverBlank() {
        StoryStrings[] lines = {
            StoryStrings.HOLLOW_DEATH_FIRST,
            StoryStrings.HOLLOW_DEATH_AGAIN,
            StoryStrings.HOLLOW_SPARE,
            StoryStrings.HOLLOW_WAVE100};
        for (StoryStrings line : lines) {
            assertFalse(GameLocale.text(line).isBlank());
        }
    }
}
