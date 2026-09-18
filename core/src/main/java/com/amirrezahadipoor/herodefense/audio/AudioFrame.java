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
    void guideMusic(MusicBed bed, float screenGain, boolean ambience);

    /**
     * The run's current intensity, 0..1 (roadmap F1). The deck lays it under the bed that carries a layer
     * and ignores it otherwise; a default because every fake that implements this port predates the layer
     * and has no reason to know about it.
     */
    default void guideTension(float tension) {
    }

    /** The platform told us something about who owns the speakers (roadmap R6.4). */
    void onAudioFocus(AudioFocusState.Event event);

    void tick(float realDeltaSeconds);
}
