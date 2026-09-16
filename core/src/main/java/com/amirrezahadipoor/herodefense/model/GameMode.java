package com.amirrezahadipoor.herodefense.model;

/**
 * How long a run is (roadmap R3.5).
 *
 * <p>The full vigil is the game as balanced: two hundred waves, four boss identities over forty encounters, three
 * grove trees. The brief vigil is the same run cut to thirty waves — the same spawner, the same economy, the same
 * difficulty curve, just an ending that a player can reach in one sitting. Nothing about the short mode changes how
 * a wave plays; it changes when the run stops, which is why it can be measured with the same simulator.
 */
public enum GameMode {

    /** The shipped two-hundred-wave run. */
    STANDARD("The Long Vigil", GameState.FINAL_WAVE),
    /** The same run, ended after the sixth boss. */
    BRIEF("A Brief Vigil", 30);

    private final String title;
    private final int waves;

    GameMode(String title, int waves) {
        this.title = title;
        this.waves = waves;
    }

    public String title() {
        return title;
    }

    /** The wave the run ends on. */
    public int waves() {
        return waves;
    }

    /** Parses a saved mode name, falling back to the shipped run so an old save cannot break. */
    public static GameMode fromName(String name) {
        if (name == null) {
            return STANDARD;
        }
        for (GameMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name.trim())) {
                return mode;
            }
        }
        return STANDARD;
    }
}
