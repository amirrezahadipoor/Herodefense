package com.amirrezahadipoor.herodefense.i18n;

/**
 * The words around gear: what the anvil says when you use it, what a set is called, and what floats above the
 * Hero when a kill pays (roadmap R7.3).
 *
 * <p>Every entry here carries a number or a name that the caller has already formatted, because the two
 * languages disagree about more than vocabulary: a gain reads "+30" in English and "۳۰+" on screen in Persian
 * once the shaper and the bidi pass are done with it ({@code GameNumbers.signed} exists for exactly that), and
 * a coin amount is "$ 30" on one side and "۳۰ سکه" on the other. So the patterns take strings, not longs, and
 * the caller goes through {@code GameLocale.number} — the same reason {@link Translated#text} refuses objects.
 *
 * <p>The forge messages keep the double-space separators of the English they replace. Those separators are a
 * layout device: the message is drawn as one centred line over the anvil, and "  |  " is what makes the three
 * parts of it read as three parts at the scale it is drawn at.
 */
public enum ItemStrings implements Translated {

    /** The golden number that rises off a kill. {@code GameNumbers.signed} supplies the argument. */
    FLOATING_COIN("$ +%1$s", "+%1$s سکه"),

    /** Auto-sell conversion pop-up. English keeps the "+$ n" shape the combat renderer has always drawn;
     *  the pickup coin above reads "$ +n" in its own table entry — two pop-ups, two frozen strings. */
    FLOATING_COIN_AUTOSELL("+$ %1$s", "+%1$s سکه"),

    /** Anvil feedback: what happened, to what, and what it cost. */
    FORGE_REFORGED("REFORGED  |  %1$s  |  -$ %2$s", "بازآهنگری شد  |  %1$s  |  -%2$s سکه"),
    FORGE_AFFIX_REROLLED("AFFIX REROLLED  |  %1$s  |  -$ %2$s", "ویژگی تازه  |  %1$s  |  -%2$s سکه"),
    FORGE_NEED_COINS("NEED $ %1$s MORE  |  ANVIL", "%1$s سکهٔ دیگر لازم است  |  سندان"),
    FORGE_NOT_FORGEABLE("ANVIL TAKES RARE & LEGENDARY ONLY", "سندان تنها آیتم کمیاب و افسانه‌ای می‌پذیرد"),
    FORGE_MAXED("FULLY REFORGED  |  %1$s", "کاملاً بازآهنگری شده  |  %1$s"),

    /** The two equipment sets, named as a player reads them in the Inventory status line. */
    SET_VERDANT_COVENANT("Verdant Covenant", "پیمان سبزه"),
    SET_BASTION_OATH("Bastion Oath", "سوگند دژ"),

    /** An item's name with its forge level on the end: "Starfall Bow +1". */
    ITEM_UPGRADE_LEVEL("%1$s +%2$s", "%1$s +%2$s"),

    /**
     * The Inventory status line and one set's progress inside it. The line is one pattern holding both sets
     * rather than a joined list, because the separator between them is part of the sentence: every other table
     * keeps its "  |  " inside a pattern, and a separator literal in the code that builds the line would be
     * layout written in one language only. A third set means a third placeholder here, which is what
     * {@code EquipmentSetBonusTest.theStatusLineHasOneSlotPerSet} checks against the list of sets.
     */
    SETS_STATUS("SETS: %1$s | %2$s", "مجموعه‌ها: %1$s | %2$s"),
    SET_PROGRESS("%1$s %2$s/%3$s", "%1$s %2$s/%3$s");

    private final String english;
    private final String persian;

    ItemStrings(String english, String persian) {
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
