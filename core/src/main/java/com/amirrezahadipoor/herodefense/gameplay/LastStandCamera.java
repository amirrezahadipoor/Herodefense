package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * The last-stand lens (P6a longer waves): while a wave stands, the camera pushes in gently as the
 * field thins, so the final hostiles fill the frame. A pure function of the run state -- no timer,
 * no save footprint -- so the composer can read it every frame and the simulator never needs to.
 */
public final class LastStandCamera {
    /** The closest the lens ever pushes: a breath in, never a punch. */
    public static final float CLOSEST_ZOOM = 1.14f;

    private LastStandCamera() {
    }

    /**
     * The camera zoom for this frame: 1 while no wave stands, easing toward {@link #CLOSEST_ZOOM}
     * as the field thins.
     */
    public static float zoomFor(GameState state) {
        if (state == null || !state.waveActive || state.livingEnemyCount() == 0) {
            return 1f;
        }
        return 1f + (CLOSEST_ZOOM - 1f) * (1f - HeartbeatSystem.standingFraction(state));
    }
}
