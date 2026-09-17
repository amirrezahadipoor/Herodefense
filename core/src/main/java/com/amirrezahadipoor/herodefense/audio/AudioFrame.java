package com.amirrezahadipoor.herodefense.audio;

import com.amirrezahadipoor.herodefense.settings.GameSettings;

/**
 * The audio work a frame does: apply settings (and keep the music running) and advance the rate limiter
 * (roadmap R2.3).
 *
 * <p>The second narrow port next to {@link AudioPlayback}. The frame driver needs these two calls every frame,
 * and naming {@code GameAudioManager} directly would have kept the whole frame untestable, because that class
 * opens Music and Sound resources in its constructor.
 */
public interface AudioFrame {

    void update(GameSettings settings);

    /**
     * Points the music at the bed the current game state wants, with the screen's gain (roadmap R6.3). The
     * frame says what the game is doing; the audio layer decides whether that means a crossfade.
     */
    void guideMusic(MusicBed bed, float screenGain);

    void tick(float realDeltaSeconds);
}
