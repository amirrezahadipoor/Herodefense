package com.amirrezahadipoor.herodefense.audio;

/**
 * The bed that plays under the music (roadmap R6.2). One entry, and it is one on purpose: the vigil has a
 * place, and the place has a sound -- shelter, wind, a distant canopy. It is a {@code Music} layer rather than
 * an effect because it loops for as long as the run does, and it sits far below the beds so it is felt rather
 * than heard.
 */
public enum Ambience {
    VIGIL("audio/sfx/ambience_vigil.ogg", 0.11f);

    private final String path;
    private final float volume;

    Ambience(String path, float volume) {
        this.path = path;
        this.volume = volume;
    }

    public String path() {
        return path;
    }

    public float volume() {
        return volume;
    }
}
