package com.amirrezahadipoor.herodefense.model;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.RunStrings;

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
    STANDARD(RunStrings.MODE_STANDARD, GameState.FINAL_WAVE),
    /** The same run, ended after the sixth boss. */
    BRIEF(RunStrings.MODE_BRIEF, 30);

    private final RunStrings title;
    private final int waves;

    GameMode(RunStrings title, int waves) {
        this.title = title;
        this.waves = waves;
    }

    /** The mode's name in the language in force. The save stores {@link #name()}, never this. */
    public String title() {
        return GameLocale.text(title);
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
