package com.amirrezahadipoor.herodefense.i18n;

/**
 * The game's own voice (roadmap R7.3): what a boss's first title card says, what the Warden says while a tree
 * is planted, what the Warden says to itself at a wave milestone, and the line the trophy chime puts on screen.
 *
 * <p>These are the four places where the game speaks rather than reports, which is why they are one table
 * rather than four: they share a narrator, and a narrator whose voice differs between screens is a narrator
 * the player notices. They lived in {@code story/} and {@code progression/} as switch arms over a boss type, a
 * ceremony phase and a wave number; the switches stay where they are, because which line plays when is
 * presentation logic, and only the words moved.
 *
 * <p>The em dash the English title cards use is kept on the Persian side rather than swapped for the "  |  "
 * the button-and-panel tables use. It is in Vazirmatn-Bold (checked with {@code fontTools} against the shipped
 * face, along with U+066A and U+200C), it is the same mark a Persian reader meets in print, and a title card is
 * a sentence rather than a row of readouts.
 *
 * <p>What this does not translate, and cannot yet: the trophy *names* that {@code TROPHY_NAMES} joins are
 * {@code progression/Trophy}'s own 24 titles, still English and still on the provenance ratchet, so a Persian
 * player gets a Persian sentence around English names until that file is tabled.
 */
public enum StoryStrings implements Translated {

    /** First-encounter boss title cards: one line per identity, shown once ever. */
    BOSS_ANCIENT_GOLEM("ANCIENT GOLEM — old guard who still stands.",
        "گولم باستانی — نگهبان کهنه‌کاری که هنوز ایستاده است."),
    BOSS_THORN_MATRIARCH("THORN MATRIARCH — she grew half your foes.",
        "مادرتاج خار — نیمی از دشمنانت را او رویاند."),
    BOSS_EMBER_WYRM("EMBER WYRM — a fire that never went out.",
        "اژدر اخگر — آتشی که هرگز خاموش نشد."),
    BOSS_VOID_KNIGHT("VOID KNIGHT — he fell and forgot the rest.",
        "شوالیه تهی — افتاد و بقیه را فراموش کرد."),

    /** The planting ceremony's five beats, in the order the ceremony walks through them. */
    CEREMONY_WALK_OUT("One root should not hold this alone.",
        "یک ریشه نباید این را به تنهایی نگه دارد."),
    CEREMONY_PLANT("Then another one. Grow angry if you must.",
        "پس یکی دیگر. اگر باید، خشمگین شو."),
    CEREMONY_WATER("I will hold the line. That is my job.",
        "من خط را نگه می‌دارم. کارم همین است."),
    CEREMONY_GROW("The grove remembers your gift.",
        "بیشه هدیه تو را به یاد دارد."),
    CEREMONY_WALK_BACK("Now hold the grove.", "حالا بیشه را نگه دار."),

    /** The Warden's reflections at wave milestones: 25, 50, 75, 125, 150, 175. */
    REFLECTION_WAVE_25("Wolves fear something deeper than me. That should scare me more.",
        "گرگ‌ها از چیزی ژرف‌تر از من می‌ترسند. این باید بیشتر بترساندم."),
    REFLECTION_WAVE_50("Half of what I killed, I once knew. I try not to think of it.",
        "نیمی از آنچه کشته‌ام روزی می‌شناختم. سعی می‌کنم به آن فکر نکنم."),
    REFLECTION_WAVE_75("Ground past the tree line feels wrong. Not ground at all.",
        "زمین آن‌سوی خط درختان درست نیست. اصلاً زمین نیست."),
    REFLECTION_WAVE_125("Three trees now. Thrice to lose. Bad trade. I would still make it.",
        "حالا سه درخت. سه بار باختن. دادوستد بدی است. باز هم می‌کنم."),
    REFLECTION_WAVE_150("It no longer sends weak first. It is done waiting.",
        "دیگر نخست ضعیف‌ها را نمی‌فرستد. انتظارش تمام شده است."),
    REFLECTION_WAVE_175("What is left may be the last. Or it wants me to think so.",
        "آنچه مانده شاید آخرین باشد. یا می‌خواهد چنین فکر کنم."),

    /** The trophy line: a count when there are too many to read, otherwise the names joined. */
    TROPHY_COUNT("Trophy - %1$s earned", "جام - %1$s کسب شد"),
    TROPHY_NAMES("Trophy - %1$s", "جام - %1$s"),
    TROPHY_AND(" and ", " و ");

    private final String english;
    private final String persian;

    StoryStrings(String english, String persian) {
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
