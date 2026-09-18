package com.amirrezahadipoor.herodefense.i18n;

/**
 * The words a run is made of (roadmap R7.3): what an omen wave is called and what it does, and what the two run
 * lengths are called.
 *
 * <p>These lived as literals inside the enums that carry them -- {@code model/WaveModifier} held both its label
 * and its detail, {@code model/GameMode} its title -- which put a sentence in the same constructor as five
 * multipliers. The multipliers are balance and belong in the model; the words are a language and belong here,
 * where {@code TranslationTableTest} checks that both languages exist and where {@code GameFonts} can see the
 * glyphs they need.
 *
 * <p>The omen details are written as sentences in both languages rather than as fragments ("more of them" /
 * "تعدادشان بیشتر است") because the HUD draws the label and the detail on one line at 0.44 scale, and a fragment
 * in Persian reads as a typo rather than as brevity.
 */
public enum RunStrings implements Translated {

    /** Wave omens (R3.4): the name drawn in the HUD and the line under it saying what changes. */
    OMEN_SWARM("SWARM", "انبوه"),
    OMEN_SWARM_DETAIL("more of them", "تعدادشان بیشتر است"),
    OMEN_IRON_HIDE("IRON HIDE", "پوست‌آهنین"),
    OMEN_IRON_HIDE_DETAIL("harder to fell", "زمین‌زدنشان سخت‌تر است"),
    OMEN_BLOODRUSH("BLOODRUSH", "خون‌شتاب"),
    OMEN_BLOODRUSH_DETAIL("heavier blows", "ضربه‌هایشان سنگین‌تر است"),
    OMEN_QUICKSTEP("QUICKSTEP", "گام‌تند"),
    OMEN_QUICKSTEP_DETAIL("they close faster", "تندتر نزدیک می‌شوند"),
    OMEN_GILDED("GILDED", "زراندود"),
    OMEN_GILDED_DETAIL("tougher, and worth more", "هم جان‌سخت‌ترند، هم پرسودتر"),
    OMEN_WARBAND("WARBAND", "برگزیدگان"),
    OMEN_WARBAND_DETAIL("fewer, and heavier", "کمترند، ولی هرکدام سنگین‌تر است"),

    /** Run lengths (R3.5): the same run, ended at wave two hundred or at wave thirty. */
    MODE_STANDARD("The Long Vigil", "پاسداری بلند"),
    MODE_BRIEF("A Brief Vigil", "پاسداری کوتاه");

    private final String english;
    private final String persian;

    RunStrings(String english, String persian) {
        this.english = english;
        this.persian = persian;
    }

    @Override
    public String key() {
        return name();
    }

    @Override
    public String english() {
        return english;
    }

    @Override
    public String persian() {
        return persian;
    }
}
