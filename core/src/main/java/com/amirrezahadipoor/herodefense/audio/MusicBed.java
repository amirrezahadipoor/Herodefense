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
    /**
     * The vigil itself: the endless run, a slow minor walk with a lead motif that returns every eight bars.
     * Roadmap F1 gave it an intensity layer -- a stem in the same key, tempo, bar count and progression,
     * started on the same playhead and faded in as the run deepens, so hour two does not sound like minute
     * two. The layer is the run's; no other bed carries one.
     */
    VIGIL("audio/music/vigil.ogg", 0.30f, "audio/music/vigil_tension.ogg", 0.22f),
    /** A boss is on the field: the same key, faster, lower, heavier percussion. */
    HOLLOW_MARCH("audio/music/hollow_march.ogg", 0.26f, null, 0f),
    /** Menus, the codex, the shop and the root network: slower, higher, almost no percussion. */
    HEARTWOOD_DAWN("audio/music/heartwood_dawn.ogg", 0.24f, null, 0f),
    /** The end of a run, won or lost: sparse, unresolved, no percussion at all. */
    QUIET_AFTER("audio/music/quiet_after.ogg", 0.22f, null, 0f);

    private final String path;
    private final float baseVolume;
    private final String layerPath;
    private final float layerVolume;

    MusicBed(String path, float baseVolume, String layerPath, float layerVolume) {
        this.path = path;
        this.baseVolume = baseVolume;
        this.layerPath = layerPath;
        this.layerVolume = layerVolume;
    }

    public String path() {
        return path;
    }

    public float baseVolume() {
        return baseVolume;
    }

    /** The intensity layer's file, or null for a bed that carries no layer. */
    public String layerPath() {
        return layerPath;
    }

    /** The layer's own level at full tension, under the same player level and screen duck as the bed. */
    public float layerVolume() {
        return layerVolume;
    }
}
