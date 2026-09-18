package com.amirrezahadipoor.herodefense.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.input.HapticFeedback;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * How a trophy is announced (roadmap R3.3): the hand, the ear, then the words — and the words yield to the Tree's
 * own story line instead of overwriting it.
 */
final class TrophyPresenterTest {

    private final RecordingAudio audio = new RecordingAudio();
    private final RecordingHaptics haptics = new RecordingHaptics();
    private final List<String> lines = new ArrayList<>();
    private boolean busy;

    private TrophyPresenter presenter() {
        return new TrophyPresenter(audio, haptics, lines::add, () -> busy);
    }

    @Test
    void nothingHappensWithoutATrophy() {
        presenter().announce(List.of());
        presenter().announce(null);

        assertEquals(0, haptics.taps);
        assertTrue(audio.cues.isEmpty());
        assertTrue(lines.isEmpty());
    }

    @Test
    void aTrophyTapsChimesAndSaysItsName() {
        presenter().announce(List.of(Trophy.STEADY_HAND));

        assertEquals(1, haptics.taps, "the player feels it first");
        assertEquals(List.of(AudioCue.LEVEL_UP), audio.cues, "then hears it");
        assertEquals(List.of("Trophy - Steady Hand"), lines);
    }

    @Test
    void aStoryLineIsNeverOverwritten() {
        busy = true;
        presenter().announce(List.of(Trophy.GARDENER));

        assertEquals(1, haptics.taps, "the feedback still happens");
        assertEquals(1, audio.cues.size());
        assertTrue(lines.isEmpty(), "but the Tree keeps the screen while it is speaking");
    }

    @Test
    void threeTrophiesAtOnceAreCountedRatherThanListed() {
        presenter().announce(List.of(Trophy.FIRST_VIGIL, Trophy.BARE_HANDS, Trophy.GARDENER));

        assertEquals(List.of("Trophy - 3 earned"), lines, "a line the HUD can actually fit");
        assertEquals("Trophy - First Vigil and Bare Hands", TrophyPresenter.lineFor(
            List.of(Trophy.FIRST_VIGIL, Trophy.BARE_HANDS)), "two names still read as names");
    }

    private static final class RecordingAudio implements AudioPlayback {
        private final List<AudioCue> cues = new ArrayList<>();

        @Override
        public void play(AudioCue cue) {
            cues.add(cue);
        }
    }

    private static final class RecordingHaptics implements HapticFeedback {
        private int taps;

        @Override
        public void tap() {
            taps++;
        }

        @Override
        public void cardSelection() {
        }
    }
}
