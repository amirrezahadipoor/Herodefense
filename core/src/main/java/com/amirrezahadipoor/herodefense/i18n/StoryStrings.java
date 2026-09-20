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

    /** First-encounter boss title cards: one line per identity, shown once ever. D3 adds 4 more. */
    BOSS_ANCIENT_GOLEM("ANCIENT GOLEM — old guard who still stands.",
        "گولم باستانی — نگهبان کهنه‌کاری که هنوز ایستاده است."),
    BOSS_THORN_MATRIARCH("THORN MATRIARCH — she grew half your foes.",
        "مادرتاج خار — نیمی از دشمنانت را او رویانده است."),
    BOSS_EMBER_WYRM("EMBER WYRM — a fire that never went out.",
        "اژدر اخگر — آتشی که هرگز خاموش نشد."),
    BOSS_VOID_KNIGHT("VOID KNIGHT — he fell and forgot the rest.",
        "شوالیهٔ تهی — سقوط کرد و همه چیز را از یاد برد."),
    BOSS_FROST_TITAN("FROST TITAN — winter that learned to walk.",
        "غول یخبندان — زمستانی که راه رفتن آموخت."),
    BOSS_SHADOW_LICH("SHADOW LICH — keeper of the second fall.",
        "لیچ سایه — نگهبان سقوط دوم."),
    BOSS_STORM_COLOSSUS("STORM COLOSSUS — thunder in a stone chest.",
        "غول توفان — تندر در سینه‌ای سنگی."),
    BOSS_BLOODROOT_AVATAR("BLOODROOT AVATAR — the grove's own wound.",
        "آواتار خون‌ریشه — زخمِ خودِ بیشه."),

    /** The planting ceremony's five beats, in the order the ceremony walks through them. */
    CEREMONY_WALK_OUT("One root should not hold this alone.",
        "یک ریشه نباید این بار را تنهایی به دوش بکشد."),
    CEREMONY_PLANT("Then another one. Grow angry if you must.",
        "حالا یکی دیگر. اگر لازم است، خشمگین رشد کن."),
    CEREMONY_WATER("I will hold the line. That is my job.",
        "من خط دفاع را نگه می‌دارم. این وظیفهٔ من است."),
    CEREMONY_GROW("The grove remembers your gift.",
        "بیشه فداکاری تو را به یاد خواهد سپرد."),
    CEREMONY_WALK_BACK("Now hold the grove.", "حالا از بیشه دفاع کن."),

    /** The Warden's reflections at wave milestones: 25, 50, 75, 125, 150, 175. */
    REFLECTION_WAVE_25("Wolves fear something deeper than me. That should scare me more.",
        "گرگ‌ها از چیزی ترسناک‌تر از من فرار می‌کنند؛ این باید بیشتر مرا نگران کند."),
    REFLECTION_WAVE_50("Half of what I killed, I once knew. I try not to think of it.",
        "نیمی از کسانی را که شکست دادم روزی می‌شناختم؛ سعی می‌کنم به آن فکر نکنم."),
    REFLECTION_WAVE_75("Ground past the tree line feels wrong. Not ground at all.",
        "زمین آن‌سوی درختان حس ناامنی دارد؛ اصلاً شبیه زمین واقعی نیست."),
    REFLECTION_WAVE_125("Three trees now. Thrice to lose. Bad trade. I would still make it.",
        "حالا سه درخت داریم؛ سه برابر خطر باخت، اما هنوز هم ارزشش را دارد."),
    REFLECTION_WAVE_150("It no longer sends weak first. It is done waiting.",
        "دیگر دشمنان ضعیف را اول نمی‌فرستد؛ دیگر صبری برایش نمانده است."),
    REFLECTION_WAVE_175("What is left may be the last. Or it wants me to think so.",
        "شاید این آخرین نبرد باشد؛ یا شاید می‌خواهد من این‌طور فکر کنم."),

    /** The trophy line: a count when there are too many to read, otherwise the names joined. */
    TROPHY_COUNT("Trophy - %1$s earned", "جام - %1$s کسب شد"),
    TROPHY_NAMES("Trophy - %1$s", "جام - %1$s"),
    TROPHY_AND(" and ", " و "),

    /** The Hollow (roadmap ST1): the one voice that talks to the player, not the Hero. */
    HOLLOW_DEATH_FIRST("You fell. Not the Hero -- you. I can tell the difference.",
        "تو افتادی؛ نه قهرمان. خودت. من فرق این دو را می‌فهمم."),
    HOLLOW_DEATH_AGAIN("Again. You always get up. It is the only interesting thing about you.",
        "باز هم بلند شدی. تنها چیز جالبِ تو همین است."),
    HOLLOW_SPARE("It was only watching, and you let it go? I remember mercy. I feed on it later.",
        "داشت تماشایت می‌کرد و رهایش کردی؟ رحمتت را به خاطر می‌سپارم؛ بعداً."),
    HOLLOW_WAVE100("Halfway. The Tree thanks you. I am not in a hurry. Are you?",
        "نیمه‌ی راه. درخت از تو ممنون است. من عجله ندارم. تو چِی؟"),
    HOLLOW_HELLO("There you are. Not the Hero -- the one holding the phone. I have been counting your heartbeats since the menu.",
        "بفرما، رسیدی. نه قهرمان — همان که گوشی را در دستش است. از همان صفحه‌ی منو، ضربان‌هایت را می‌شمردم."),
    HOLLOW_MERCY_HABIT("Three spared, and you still call it kindness. So do I. Everything in this grove grows roots; mercy does too.",
        "سه نفر را رها کردی و هنوز اسمش را مهربانی می‌گذاری. من هم می‌گذارم. در این بیشه هر چیزی ریشه می‌دهد؛ رحمت هم."),
    HOLLOW_VERDICT_MERCIFUL("Your last run ended the way it began: with you deciding who deserves the morning. The grove calls it mercy. I call it a debt.",
        "دفتر پیشرت همان‌طور تمام شد که شروع شد: با تو که تصمیم گرفتی کدام‌یک سحر را می‌ارزد. بیشه اسمش را رحمت می‌گذارد؛ من اسمش را بدهی."),
    HOLLOW_VERDICT_STERN("Your last run ended, and nothing on that field lives because you loved it. The grove calls it victory. I call it inventory.",
        "دفتر پیشرت تمام شد، و هیچ‌چیز از آن میدان به محبتِ تو زنده نمانده. بیشه اسمش را پیروزی می‌گذارد؛ من اسمش را سیاهه."),

    /** The Hollow's half-health beat, one per boss identity. */
    HOLLOW_BOSS_GOLEM("The old guard wavers. Its boundary was never you.",
        "نگهبان کهنه لرزید. مرزش هرگز تو نبودی."),
    HOLLOW_BOSS_MATRIARCH("She calls the garden home. You are the frost.",
        "او باغ را خانه می‌داند. تو یخ‌بندان هستی."),
    HOLLOW_BOSS_WYRM("Its no grows quiet. Push.",
        "«نه»یش کم‌صدا می‌شود. فشار بیاور."),
    HOLLOW_BOSS_VOID("It falls and wants company. Give it none.",
        "می‌افتد و همدم می‌خواهد. بهش نده."),
    HOLLOW_BOSS_TITAN("Winter keeps what it touches. Touch it back.",
        "زمستان آنچه را لمس کند نگه می‌دارد. تو هم لمسش کن."),
    HOLLOW_BOSS_LICH("The record keeper drafts your name. Keep it brief.",
        "دفتردار، نامت را می‌نویسد. کوتاهش کن."),
    HOLLOW_BOSS_COLOSSUS("Thunder never hurries. You should.",
        "رعد عجله نمی‌کند. تو باید."),
    HOLLOW_BOSS_BLOODROOT("The wound fights the cure. It always has.",
        "زخم با درمان می‌جنگد. از همیشه."),

    /** The Tree's daily gift (roadmap ST5): the return hook, spoken once a day. */
    DAILY_GIFT("Two heartwood, kept from yesterday. The Tree counts your days.",
        "دو چوب‌جان از دیروز مانده. درخت روزهایت را می‌شمارد."),

    /** The Vigil Deeds (roadmap ST2): the run's named goals, paid once each. */
    DEED_WAVE_10("Deed: held to wave 10  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۱۰  |  + %1$s سکه"),
    DEED_WAVE_25("Deed: held to wave 25  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۲۵  |  + %1$s سکه"),
    DEED_WAVE_50("Deed: held to wave 50  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۵۰  |  + %1$s سکه"),
    DEED_WAVE_100("Deed: held to wave 100  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۱۰۰  |  + %1$s سکه"),
    DEED_WAVE_150("Deed: held to wave 150  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۱۵۰  |  + %1$s سکه"),
    DEED_WAVE_200("Deed: held to wave 200  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۲۰۰  |  + %1$s سکه"),
    DEED_FIRST_BOSS("Deed: first giant felled  |  + %1$s coins", "کارنامه: نخستین غول سرنگون شد  |  + %1$s سکه"),
    DEED_BOSSES_5("Deed: five giants in one vigil  |  + %1$s coins", "کارنامه: پنج غول در یک بیداری  |  + %1$s سکه"),
    DEED_CLEAN_25("Deed: wave 25 without a potion  |  + %1$s coins", "کارنامه: موج ۲۵ بدون معجون  |  + %1$s سکه"),
    DEED_FLAWLESS_50("Deed: wave 50 unfallen and dry  |  + %1$s coins", "کارنامه: موج ۵۰ بی‌مرگ و بی‌معجون  |  + %1$s سکه"),
    DEED_CODEX_10("Deed: read ten codex pages  |  + %1$s coins", "کارنامه: خواندن ده صفحه از دانشنامه  |  + %1$s سکه");

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
