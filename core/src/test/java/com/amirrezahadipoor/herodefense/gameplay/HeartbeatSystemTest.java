package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The drum under combat: slow at full strength, quick at the last stand, silent anywhere else. */
final class HeartbeatSystemTest {
    private final RecordingAudio audio = new RecordingAudio();
    private final HeartbeatSystem heartbeat = new HeartbeatSystem(audio);

    @Test
    void aFullWaveBeatsSlowly() {
        GameState state = fighting(11L, 10, 10);

        heartbeat.update(state, HeartbeatSystem.SLOW_PERIOD_SECONDS - 0.01f);
        assertTrue(audio.cues.isEmpty(), "the drum waits out its slow period");

        heartbeat.update(state, 0.01f);
        assertEquals(List.of(AudioCue.HEARTBEAT), audio.cues);
    }

    @Test
    void theLastStandBeatsFast() {
        GameState state = fighting(12L, 10, 1);
        float period = HeartbeatSystem.periodFor(state);

        assertTrue(period < HeartbeatSystem.SLOW_PERIOD_SECONDS, "one hostile left beats faster than ten");
        heartbeat.update(state, period);
        heartbeat.update(state, period);
        assertEquals(List.of(AudioCue.HEARTBEAT, AudioCue.HEARTBEAT), audio.cues);
    }

    @Test
    void escortsBeyondThePlanStillBeatSlowly() {
        GameState state = fighting(13L, 10, 12);

        assertEquals(HeartbeatSystem.SLOW_PERIOD_SECONDS, HeartbeatSystem.periodFor(state), 0.001f);
        heartbeat.update(state, HeartbeatSystem.SLOW_PERIOD_SECONDS);
        assertEquals(List.of(AudioCue.HEARTBEAT), audio.cues);
    }

    @Test
    void menusAndClearedFieldsStaySilent() {
        heartbeat.update(GameState.newRun(14L), 10f);
        assertTrue(audio.cues.isEmpty(), "no wave, no drum");

        GameState cleared = fighting(15L, 10, 0);
        heartbeat.update(cleared, 10f);
        assertTrue(audio.cues.isEmpty(), "a cleared field rests silently");

        heartbeat.update(null, 10f);
        assertTrue(audio.cues.isEmpty(), "no state, no drum");
    }

    @Test
    void aHitchNeverPaysBackARoll() {
        GameState state = fighting(16L, 10, 10);

        heartbeat.update(state, 10f);
        assertEquals(List.of(AudioCue.HEARTBEAT), audio.cues);
    }

    /** A wave mid-fight: active, dealt {@code planned}, with {@code living} hostiles standing. */
    private static GameState fighting(long seed, int planned, int living) {
        GameState state = GameState.newRun(seed);
        state.waveActive = true;
        state.wavePlannedEnemies = planned;
        for (int index = 0; index < living; index++) {
            state.aliveEnemies.add(new Enemy());
        }
        return state;
    }

    private static final class RecordingAudio implements AudioPlayback {
        private final List<AudioCue> cues = new ArrayList<>();

        @Override
        public void play(AudioCue cue) {
            cues.add(cue);
        }
    }
}
