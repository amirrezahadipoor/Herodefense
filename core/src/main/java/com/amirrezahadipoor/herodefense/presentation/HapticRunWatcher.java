package com.amirrezahadipoor.herodefense.presentation;

import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.gameplay.ArenaQueries;
import com.amirrezahadipoor.herodefense.input.HapticFeedback;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * The run's transitions, translated into the haptic vocabulary (roadmap F4): a hit, a boss falling, a
 * level, a wave rolling over, the ultimate going out. It watches by diffing the state a frame leaves
 * against the state the frame before it left, which keeps every gameplay system free of the device --
 * the same reason the music follows the flow from the presentation side.
 *
 * <p>Two rules keep it honest. The first frame after the run screen is (re)entered only remembers: a
 * load, a pause or a ceremony must never fire a pulse for damage that happened while nobody was looking.
 * And the hero-hit pulse is rate-limited, because a swarm landing six hits in a second is one event to
 * the hands, not six.
 */
public final class HapticRunWatcher {

    /** The smallest gap between two "you were hit" pulses; a swarm is one event, not a jackhammer. */
    private static final float HERO_HIT_COOLDOWN_SECONDS = 0.6f;

    /** Focus only ever falls when the ultimate spends it, so a fall this big is the ultimate. */
    private static final float ULTIMATE_FOCUS_DROP = 10f;

    private final HapticFeedback haptics;
    private boolean watching;
    private float heroHitCooldown;
    private float lastHeroHealth;
    private int lastBossCount;
    private int lastHeroLevel;
    private int lastWaveNumber;
    private float lastFocus;

    public HapticRunWatcher(HapticFeedback haptics) {
        this.haptics = haptics;
    }

    /** Reads one frame of the run; null haptics (a host without hands) makes the whole watcher a no-op. */
    public void watch(GameScreenState screen, GameState state, float deltaSeconds) {
        if (haptics == null) {
            return;
        }
        heroHitCooldown = Math.max(0f, heroHitCooldown - Math.max(0f, deltaSeconds));
        if (screen != GameScreenState.PLAYING || state == null || state.hero == null) {
            watching = false;
            return;
        }
        if (!watching) {
            watching = true;
            remember(state);
            return;
        }
        if (state.hero.alive && state.hero.health < lastHeroHealth - 0.001f && heroHitCooldown <= 0f) {
            haptics.heroDamaged();
            heroHitCooldown = HERO_HIT_COOLDOWN_SECONDS;
        }
        if (ArenaQueries.livingBossCount(state) < lastBossCount) {
            haptics.bossDefeated();
        }
        if (state.heroLevel > lastHeroLevel) {
            haptics.levelUp();
        }
        if (state.waveNumber > lastWaveNumber) {
            haptics.waveCleared();
        }
        if (state.focus < lastFocus - ULTIMATE_FOCUS_DROP) {
            haptics.ultimateReleased();
        }
        remember(state);
    }

    private void remember(GameState state) {
        lastHeroHealth = state.hero.health;
        lastBossCount = ArenaQueries.livingBossCount(state);
        lastHeroLevel = state.heroLevel;
        lastWaveNumber = state.waveNumber;
        lastFocus = state.focus;
    }
}
