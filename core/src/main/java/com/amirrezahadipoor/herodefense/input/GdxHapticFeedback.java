package com.amirrezahadipoor.herodefense.input;

import com.badlogic.gdx.Gdx;

/**
 * Android-backed short vibration impulses; no-op behavior is delegated to libGDX.
 *
 * <p>Roadmap F4's vocabulary, within what the platform actually offers: libGDX 1.13 carries no pattern
 * vibration, only duration, amplitude (API 26+, with a full-amplitude fallback) and three named weights.
 * One motor can still speak -- a hit is short and full-strength, a boss is the longest and heaviest thing
 * the hands ever say, a level is a medium lift, a wave is the softest tick, an ultimate is one long pulse
 * under the release.
 */
public final class GdxHapticFeedback implements HapticFeedback {
    private static final int FULL_AMPLITUDE = 255;
    private static final boolean FALLBACK_TO_FULL = true;

    @Override
    public void tap() {
        Gdx.input.vibrate(14);
    }

    @Override
    public void cardSelection() {
        Gdx.input.vibrate(34);
    }

    @Override
    public void heroDamaged() {
        Gdx.input.vibrate(55, FULL_AMPLITUDE, FALLBACK_TO_FULL);
    }

    @Override
    public void bossDefeated() {
        Gdx.input.vibrate(140, FULL_AMPLITUDE, FALLBACK_TO_FULL);
    }

    @Override
    public void levelUp() {
        Gdx.input.vibrate(45, 160, FALLBACK_TO_FULL);
    }

    @Override
    public void waveCleared() {
        Gdx.input.vibrate(18, 90, FALLBACK_TO_FULL);
    }

    @Override
    public void ultimateReleased() {
        Gdx.input.vibrate(95, 220, FALLBACK_TO_FULL);
    }
}
