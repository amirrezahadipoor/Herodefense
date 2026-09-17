package com.amirrezahadipoor.herodefense.audio;

/**
 * The game's music beds (roadmap R6.1).
 *
 * <p>The repository had one loop for the whole experience. These are four, and each one is authored in
 * {@code tools/audio/generate_music.py} from a seed, so the notes, the arrangement and the mix are reviewable
 * code and the files carry no licence question at all. Each is a whole number of bars with a wrapped reverb
 * tail and a wrapped head, so it loops without a seam.
 *
 * <p>{@code baseVolume} is the bed's own level, not a player setting: {@link MusicSelectionPolicy} decides
 * which bed belongs to which screen and whether it ducks, and the settings decide whether music plays at all.
 */
public enum MusicBed {
    /** The vigil itself: the endless run, a slow minor walk with a lead motif that returns every eight bars. */
    VIGIL("audio/music/vigil.ogg", 0.30f),
    /** A boss is on the field: the same key, faster, lower, heavier percussion. */
    HOLLOW_MARCH("audio/music/hollow_march.ogg", 0.26f),
    /** Menus, the codex, the shop and the root network: slower, higher, almost no percussion. */
    HEARTWOOD_DAWN("audio/music/heartwood_dawn.ogg", 0.24f),
    /** The end of a run, won or lost: sparse, unresolved, no percussion at all. */
    QUIET_AFTER("audio/music/quiet_after.ogg", 0.22f);

    private final String path;
    private final float baseVolume;

    MusicBed(String path, float baseVolume) {
        this.path = path;
        this.baseVolume = baseVolume;
    }

    public String path() {
        return path;
    }

    public float baseVolume() {
        return baseVolume;
    }
}
