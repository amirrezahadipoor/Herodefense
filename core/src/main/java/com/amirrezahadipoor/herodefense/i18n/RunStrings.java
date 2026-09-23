package com.amirrezahadipoor.herodefense.i18n;

/**
 * The words a run is made of (roadmap R7.3): what an omen wave is called and what it does, and what the two run
 * lengths are called.
 *
 * <p>These lived as literals inside the enums that carry them -- {@code model/WaveModifier} held both its label
 * and its detail, {@code model/GameMode} its title -- which put a sentence in the same constructor as five
 * multipliers. The multipliers are balance and belong in the model; the words are a language and belong here,
 * where {@code TranslationTableTest} checks them.
 *
 * <p>The omen details are written as sentences rather than as fragments ("more of them") because the HUD draws
 * the label and the detail on one line at 0.44 scale, and a fragment reads as a typo rather than as brevity.
 */
public enum RunStrings implements Translated {

    /** Wave omens (R3.4): the name drawn in the HUD and the line under it saying what changes. */
    OMEN_SWARM("SWARM"),
    OMEN_SWARM_DETAIL("more of them"),
    OMEN_IRON_HIDE("IRON HIDE"),
    OMEN_IRON_HIDE_DETAIL("harder to fell"),
    OMEN_BLOODRUSH("BLOODRUSH"),
    OMEN_BLOODRUSH_DETAIL("heavier blows"),
    OMEN_QUICKSTEP("QUICKSTEP"),
    OMEN_QUICKSTEP_DETAIL("they close faster"),
    OMEN_GILDED("GILDED"),
    OMEN_GILDED_DETAIL("tougher, and worth more"),
    OMEN_WARBAND("WARBAND"),
    OMEN_WARBAND_DETAIL("fewer, and heavier"),

    /** Wave events: how a wave arrives, and what the night looks like while it does. */
    EVENT_WEDGE("WEDGE"),
    EVENT_WEDGE_DETAIL("one spear, down the middle road"),
    EVENT_ENCIRCLE("ENCIRCLE"),
    EVENT_ENCIRCLE_DETAIL("every road, at once"),
    EVENT_TRICKLE("TRICKLE"),
    EVENT_TRICKLE_DETAIL("the lightest walk in first"),
    EVENT_ASH_FALL("ASH FALL"),
    EVENT_ASH_FALL_DETAIL("cold ash settles on the grove"),
    EVENT_PINCER("PINCER"),
    EVENT_PINCER_DETAIL("they close from both sides"),
    EVENT_TIDAL("TIDAL"),
    EVENT_TIDAL_DETAIL("one wall, from the south"),
    EVENT_VANGUARD("VANGUARD"),
    EVENT_VANGUARD_DETAIL("the heaviest walk in first"),
    EVENT_SCATTER("SCATTER"),
    EVENT_SCATTER_DETAIL("they fan out wide"),
    EVENT_EMBER_FALL("EMBER FALL"),
    EVENT_EMBER_FALL_DETAIL("ash drifts over the grove"),
    EVENT_MOONFOG("MOONFOG"),
    EVENT_MOONFOG_DETAIL("the night thickens"),
    EVENT_ROOT_RAIN("ROOT RAIN"),
    EVENT_ROOT_RAIN_DETAIL("the grove weeps"),
    EVENT_SPORE_DRIFT("SPORE DRIFT"),
    EVENT_SPORE_DRIFT_DETAIL("the air is full of spores"),

    /** Fields (A6): the place a run is fought on, named in the HUD while it is still new. */
    FIELD_OPEN_HEARTH("OPEN HEARTH"),
    FIELD_OPEN_HEARTH_DETAIL("nothing stands between you and them"),
    FIELD_STANDING_STONES("STANDING STONES"),
    FIELD_STANDING_STONES_DETAIL("the stones stop arrows"),
    FIELD_THORNHEDGE("THORNHEDGE"),
    FIELD_THORNHEDGE_DETAIL("the hedges cover the flanks"),
    FIELD_RUINED_RING("RUINED RING"),
    FIELD_RUINED_RING_DETAIL("a ring, broken where the roads run"),

    /** Run lengths (R3.5): the same run, ended at wave two hundred or at wave thirty. */
    MODE_STANDARD("The Long Vigil"),
    MODE_BRIEF("A Brief Vigil"),
    MODE_DAWN_WATCH("The Dawn Watch");

    private final String english;

    RunStrings(String english) {
        this.english = english;
    }

    @Override
    public String key() {
        return name();
    }

    @Override
    public String english() {
        return english;
    }

}
