package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * The run's steady heartbeat (P6a longer waves): a soft drum under combat that quickens as the wave
 * thins, so a long grind ends in a last-stand pulse. It ticks only in the live game -- the balance
 * simulator never constructs it -- and its timer is transient: saves neither store nor restore it.
 */
public final class HeartbeatSystem {
    /** Seconds between beats while the wave stands at full strength. */
    public static final float SLOW_PERIOD_SECONDS = 2.2f;
    /** Seconds between beats when a single hostile still stands. */
    public static final float FAST_PERIOD_SECONDS = 0.9f;

    private final AudioPlayback audio;
    private float secondsSinceBeat;

    public HeartbeatSystem(AudioPlayback audio) {
        this.audio = audio;
    }

    /**
     * Ticks the drum forward. Silence outside combat -- the beat belongs to a wave with hostiles
     * still standing, never to menus, rewards, or a cleared field.
     */
    public void update(GameState state, float deltaSeconds) {
        if (state == null || !state.waveActive || state.livingEnemyCount() == 0) {
            secondsSinceBeat = 0f;
            return;
        }
        secondsSinceBeat += Math.max(0f, deltaSeconds);
        float period = periodFor(state);
        if (secondsSinceBeat >= period) {
            // One beat per frame at most: a hitch must never pay back a skipped drum as a roll.
            secondsSinceBeat = Math.min(secondsSinceBeat - period, period);
            // Dread waves drum darker: a living boss or a standing elite swaps the heartbeat out.
            audio.play(DreadWaves.dreadStands(state) ? AudioCue.DREAD_DRUM : AudioCue.HEARTBEAT);
        }
    }

    /**
     * Seconds between beats for this wave's remaining strength: slow at full strength, quick at the
     * last stand.
     */
    public static float periodFor(GameState state) {
        float fraction = standingFraction(state);
        return FAST_PERIOD_SECONDS + (SLOW_PERIOD_SECONDS - FAST_PERIOD_SECONDS) * fraction;
    }

    /**
     * The fraction of the wave still standing, from the plan the spawner dealt: 1 while nothing has
     * fallen, 0 at the last hostile. Clamped, because escorts can walk in beyond the plan -- and the
     * lens reads this too, so the beat and the camera always agree.
     */
    public static float standingFraction(GameState state) {
        int planned = Math.max(1, state.wavePlannedEnemies);
        return Math.min(1f, Math.max(0f, (float) state.livingEnemyCount() / planned));
    }
}
