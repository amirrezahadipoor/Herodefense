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
    // P6c longer waves: the horn that answers the last pulse of a wave walking in.
    FINAL_PUSH("audio/sfx/final_push.ogg", 0.46f, 0.60f),
    // P6a longer waves: the soft drum under combat, quickening as the wave thins.
    HEARTBEAT("audio/sfx/heartbeat.ogg", 0.30f, 0.50f),
    // P6b longer waves: the darker drum while a boss lives or an elite stands.
    DREAD_DRUM("audio/sfx/dread_drum.ogg", 0.34f, 0.50f),
    TELEGRAPH_WARNING("audio/sfx/telegraph_warning.ogg", 0.52f, 0.60f),
    UI_TAP("audio/sfx/ui_tap.ogg", 0.26f, 0.06f),
    UI_CLOSE("audio/sfx/ui_close.ogg", 0.28f, 0.10f),
    // Roadmap F2: identity variants, all generated in-repo. Light and heavy bodies die differently, and
    // three of the four boss bodies announce themselves in their own voice (the Matriarch keeps the horn).
    DEATH_LIGHT("audio/sfx/death_light.ogg", 0.40f, 0.05f),
    DEATH_HEAVY("audio/sfx/death_heavy.ogg", 0.46f, 0.06f),
    BOSS_ENTRANCE_DEEP("audio/sfx/boss_entrance_deep.ogg", 0.44f, 0.50f),
    BOSS_ENTRANCE_SHRIEK("audio/sfx/boss_entrance_shriek.ogg", 0.42f, 0.50f),
    BOSS_ENTRANCE_VOID("audio/sfx/boss_entrance_void.ogg", 0.42f, 0.50f),
    // The conversation voices (roadmap ST-voice): an Undertale-style blip under a typed line, never a spoken
    // word. Five short tones so a line's speaker is recognisable by ear -- Hero, Tree, Hollow, Pip (chirps up),
    // and the Night Shift's low square.
    SPEECH_HERO("audio/sfx/speech_hero.ogg", 0.34f, 0.05f),
    SPEECH_TREE("audio/sfx/speech_tree.ogg", 0.32f, 0.06f),
    SPEECH_HOLLOW("audio/sfx/speech_hollow.ogg", 0.35f, 0.06f),
    SPEECH_PIP("audio/sfx/speech_pip.ogg", 0.34f, 0.05f),
    SPEECH_BOSS("audio/sfx/speech_boss.ogg", 0.36f, 0.06f);

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
