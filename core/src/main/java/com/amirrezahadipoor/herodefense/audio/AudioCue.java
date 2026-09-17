package com.amirrezahadipoor.herodefense.audio;

/**
 * License-ledger-backed short effects bundled with the Android assets.
 *
 * <p>{@code minIntervalSeconds} is the per-cue rate limit: Multi Shot volleys and chain arcs
 * can raise many identical events per frame, and stacking the same sample only clips.
 */
public enum AudioCue {
    HIT("audio/sfx/hit.ogg", 0.34f, 0.05f),
    DEATH("audio/sfx/death.ogg", 0.42f, 0.05f),
    ITEM_DROP("audio/sfx/item_drop.ogg", 0.45f, 0.05f),
    LEVEL_UP("audio/sfx/level_up.ogg", 0.42f, 0.20f),
    BOSS_ENTRANCE("audio/sfx/boss_entrance.ogg", 0.40f, 0.50f),
    // Phase 18 combat feel.
    CRITICAL("audio/sfx/critical.ogg", 0.50f, 0.09f),
    KILL("audio/sfx/kill.ogg", 0.40f, 0.08f),
    CHAIN_LIGHTNING("audio/sfx/chain_lightning.ogg", 0.38f, 0.10f),
    STUN("audio/sfx/stun.ogg", 0.36f, 0.25f),
    MULTI_SHOT("audio/sfx/multi_shot.ogg", 0.30f, 0.12f),
    PURCHASE("audio/sfx/purchase.ogg", 0.45f, 0.15f),
    // Roadmap R6.2: the gaps the 2026-09-13 review named -- the bow itself, the loot, the UI, the
    // telegraph a boss gives before a special, and the moment a wave ends.
    BOW_RELEASE("audio/sfx/bow_release.ogg", 0.30f, 0.06f),
    BOW_RELEASE_LIGHT("audio/sfx/bow_release_light.ogg", 0.22f, 0.06f),
    ULTIMATE_RELEASE("audio/sfx/bow_release_heavy.ogg", 0.50f, 0.30f),
    COIN_PICKUP("audio/sfx/coin_pickup.ogg", 0.36f, 0.12f),
    WAVE_CLEAR("audio/sfx/wave_clear.ogg", 0.46f, 1.20f),
    TELEGRAPH_WARNING("audio/sfx/telegraph_warning.ogg", 0.52f, 0.60f),
    UI_TAP("audio/sfx/ui_tap.ogg", 0.26f, 0.06f),
    UI_CLOSE("audio/sfx/ui_close.ogg", 0.28f, 0.10f);

    private final String path;
    private final float volume;
    private final float minIntervalSeconds;

    AudioCue(String path, float volume, float minIntervalSeconds) {
        this.path = path;
        this.volume = volume;
        this.minIntervalSeconds = minIntervalSeconds;
    }

    public String path() {
        return path;
    }

    public float volume() {
        return volume;
    }

    public float minIntervalSeconds() {
        return minIntervalSeconds;
    }
}
