package com.amirrezahadipoor.herodefense.story;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The 34 Grove Codex entries in both languages, in the Tree's voice. The English is verbatim from
 * {@code docs/STORY_CONTENT.md} section 5; the Persian is a plain, native rendering of the same words, following
 * {@code docs/PERSIAN_PROOFREAD.md} (no Latin letters, no ASCII digits, the grove's vocabulary: «دره» for the
 * Hollow, «دانشنامهٔ بیشه» for the codex, «چوب دل» for heartwood, «معجون» for potion).
 */
public final class LoreCatalog {

    private LoreCatalog() {
    }

    private static final List<LoreEntry> ALL = List.copyOf(Arrays.asList(
            entry(1, "Before You.", "پیش از تو.",
                "Others stood here before you. I do not recall most of their names. I recall all of their last stands.",
                "پیش از تو دیگرانی اینجا ایستاده‌اند. بیشتر نام‌هایشان را به یاد ندارم. همهٔ آخرین ایستادگی‌هایشان را به یاد دارم.",
                LoreTrigger.WAVE_MILESTONE, "1"),
            entry(2, "The Three Directions.", "سه جهت.",
                "They do not come from everywhere. Only from three. I have never learned what holds the fourth.",
                "از همه‌جا نمی‌آیند. تنها از سه سو. هرگز نفهمیدم چه چیز چهارم را نگه داشته است.",
                LoreTrigger.WAVE_MILESTONE, "10"),
            entry(3, "Counting.", "شمارش.",
                "I used to count the seasons. Now I count waves. It is a smaller unit of time, and it passes no more kindly.",
                "زمانی فصل‌ها را می‌شمردم. حالا موج‌ها را می‌شمارم. یکای کوچک‌تری از زمان است و مهربان‌تر نمی‌گذرد.",
                LoreTrigger.WAVE_MILESTONE, "20"),
            entry(4, "What Luck Finds.", "آنچه بخت می‌یابد.",
                "Some of what falls from them still recalls being useful. Wear it. I would rather you have it than the ground.",
                "برخی از آنچه از آنان می‌افتد هنوز سودمندی خود را به یاد دارد. بپوشش. ترجیح می‌دهم تو داشته‌باشی‌اش تا زمین.",
                LoreTrigger.WAVE_MILESTONE, "30"),
            entry(5, "The Quiet Ones.", "خاموش‌ها.",
                "Not every Rootling attacks. Some simply stand at the tree line and watch. I do not know if that is worse.",
                "هر ریشه‌زادی حمله نمی‌کند. برخی تنها کنار صف درخت‌ها می‌ایستند و نگاه می‌کنند. نمی‌دانم این بدتر است یا نه.",
                LoreTrigger.WAVE_MILESTONE, "40"),
            entry(6, "Old Names.", "نام‌های کهنه.",
                "Rootling. Stonekin. Gloom Wolf. Fungal Brute. I gave three of those names myself, once, to living things, meaning a kinder thing.",
                "ریشه‌زاد. سنگ‌تن. گرگِ تیره. غولِ قارچی. سه‌تا از این نام‌ها را خودم داده بودم، زمانی، به موجوداتِ زنده — و معنای مهربان‌تری از آن‌ها در نظر داشتم.",
                LoreTrigger.WAVE_MILESTONE, "60"),
            entry(7, "The Long Middle.", "میانهٔ دراز.",
                "This is the part no song is written about. Not the falling, not the standing. Just the holding. Hold anyway.",
                "این بخشی است که هیچ آوازی برایش ساخته نمی‌شود. نه افتادن، نه ایستادن؛ فقط نگه داشتن. به هر حال نگه دار.",
                LoreTrigger.WAVE_MILESTONE, "80"),
            entry(8, "A Grove Takes Root.", "بیشه‌ای ریشه می‌دواند.",
                "I did not ask for a second trunk, nor a third. I am glad of both regardless. Grief is lighter, split three ways. And so, it turns out, is standing guard.",
                "تنهٔ دوم را نخواستم، نه سوم را. با این حال از هر دو خشنودم. سوگ، سه‌سان که شود، سبک‌تر است. و پاسداری هم، چنان که پیدا شد، سبک‌تر است.",
                LoreTrigger.WAVE_MILESTONE, "100"),
            entry(9, "What the Golem Guarded.", "آنچه گولم از آن نگهبانی می‌کرد.",
                "Before the Tree, there was a boundary stone, and the Golem was its keeper. Under the Hollow's grip, it still believes this is a boundary and it a keeper. Its great arm does not follow your feet: it falls where you stood when the wind rose. Set your shield there, not your boots.",
                "پیش از درخت، سنگی مرزی بود و گولم نگهبانش. در چنگ دره، هنوز باور دارد اینجا مرزی است و او نگهبانی است. بازوی بزرگش گام‌هایت را دنبال نمی‌کند: آنجا فرود می‌آید که تو هنگام برخاستنِ باد ایستاده بودی. سپرت را آنجا بگذار، نه چکمه‌هایت را.",
                LoreTrigger.BOSS_FIRST_KILL, "ANCIENT_GOLEM"),
            entry(10, "The Matriarch's Garden.", "باغِ مادرتاج.",
                "She is not attacking you with monsters. She is attacking you with her children. I do not say this to trouble you. I say it because you deserve to know what you are ending, and why it still might be a mercy.",
                "با هیولا به تو حمله نمی‌کند. با فرزندانش حمله می‌کند. این را نمی‌گویم که آزارت دهم؛ می‌گویم چون سزاواری بدانی چه چیز را پایان می‌دهی و چرا این کار شاید باز هم رحمتی باشد.",
                LoreTrigger.BOSS_FIRST_KILL, "THORN_MATRIARCH"),
            entry(11, "An Ember That Refused.", "اخگری که نپذیرفت.",
                "Fire is supposed to go out. This one said no, and a no, given enough years, becomes a shape. The Wyrm is that no, wearing scales.",
                "آتش باید خاموش شود. این یکی گفت نه؛ و هر نه‌ای، اگر سال‌ها به او فرصت دهند، شکلی می‌شود. اژدر همان «نه» است که فلس پوشیده است.",
                LoreTrigger.BOSS_FIRST_KILL, "EMBER_WYRM"),
            entry(12, "The Shape of Falling.", "شکلِ افتادن.",
                "I asked the Void Knight, once, in the only language I have. Stillness, and time. What it wanted. It did not answer. I do not think it recalls the question anymore. I do not think it recalls much of anything except falling, and wanting company on the way down.",
                "یک بار از شوالیهٔ تهی پرسیدم، به تنها زبانی که دارم: سکون و زمان. که چه می‌خواهد. پاسخ نداد. گمان نمی‌کنم دیگر آن پرسش را به یاد آورد. گمان نمی‌کنم چیزی به یادش مانده باشد جز افتادن و آرزوی همدمی در راهِ سقوط.",
                LoreTrigger.BOSS_FIRST_KILL, "VOID_KNIGHT"),
            entry(13, "Winter That Walks.", "زمستانی که راه می‌رود.",
                "The frost came after the Hollow fell, not before. It kept what it touched. The Titan is what kept walking after everything else froze.",
                "یخبندان پس از سقوطِ دره آمد، نه پیش از آن. هرچه را لمس می‌کرد نگه می‌داشت. غولِ یخبندان همان چیزی است که پس از یخ‌زدنِ همه چیز باز هم راه می‌رفت.",
                LoreTrigger.BOSS_FIRST_KILL, "FROST_TITAN"),
            entry(14, "The Second Fall.", "سقوطِ دوم.",
                "It was not a boss before. It was a record keeper. Now it keeps the record of how many times the grove has fallen and how.",
                "او پیش‌تر غولی نبود؛ دفتردار بود. حالا دفترِ آن را نگه می‌دارد که بیشه چند بار و چگونه فرو افتاده است.",
                LoreTrigger.BOSS_FIRST_KILL, "SHADOW_LICH"),
            entry(15, "Thunder in Stone.", "تندر در سنگ.",
                "Stone that learned to hold thunder instead of moss. It does not hurry. Thunder never does.",
                "سنگی که یاد گرفت به‌جای خزه تندر را در خود نگه دارد. عجله نمی‌کند. تندر هرگز عجله نمی‌کند.",
                LoreTrigger.BOSS_FIRST_KILL, "STORM_COLOSSUS"),
            entry(16, "The Grove's Wound.", "زخمِ بیشه.",
                "The World Tree's own root, taken and twisted. It bleeds sap that never dries. The Tree knows its shape.",
                "ریشهٔ خودِ درختِ جهان است، گرفته‌شده و پیچ‌خورده. شیره‌ای می‌جوشد که هرگز خشک نمی‌شود. درخت شکلش را می‌شناسد.",
                LoreTrigger.BOSS_FIRST_KILL, "BLOODROOT_AVATAR"),
            entry(17, "On Letting Go.", "دربارهٔ رها کردن.",
                "Some of them stop fighting you and start fighting the thing inside them instead. That one usually loses both battles at once.",
                "برخی از آنان دست از نبرد با تو برمی‌دارند و به نبرد با آنچه درونشان است می‌پردازند. آن یک معمولاً هر دو نبرد را هم‌زمان می‌بازد.",
                LoreTrigger.ELITE_KILL, "blightburst"),
            entry(18, "A Root's Last Job.", "آخرین کارِ یک ریشه.",
                "I do not control what the Hollow does with what used to be mine. But I notice it still flinches toward protecting, even now. That is either hope or a very old habit. I have stopped trying to tell the difference.",
                "در اختیار من نیست که دره با آنچه زمانی از آنِ من بود چه می‌کند. اما می‌بینم که حتی اکنون به‌سوی پاسداشتن واکنش نشان می‌دهد. یا امید است یا عادتی بس کهنه. دیگر برای تشخیص تفاوتشان نمی‌کوشم.",
                LoreTrigger.ELITE_KILL, "rootward_ward"),
            entry(19, "The Trail Home.", "رَدّی به‌سوی خانه.",
                "Every rotting thing wants to return to soil eventually. I only wish this kind of soil grew a thing other than more of itself.",
                "هر چیزِ پوسیده‌ای سرآخر می‌خواهد به خاک بازگردد. فقط ایکاش این جنس خاک چیز دیگری برویاند، نه بیشتر از خودش.",
                LoreTrigger.ELITE_KILL, "weeping_rot"),
            entry(20, "Again.", "دوباره.",
                "You came back. I did not expect that. I am not certain the Hollow expected it either. Which may be the only advantage either of us has left.",
                "برگشتی. انتظارش را نداشتم. مطمئن هم نیستم دره انتظارش را داشته باشد. شاید همین تنها برتریِ باقی‌ماندهٔ هر دوی ما باشد.",
                LoreTrigger.ASCENSION, "1"),
            entry(21, "The Shape of a Habit.", "شکلِ یک عادت.",
                "Twice now. I am beginning to recognize your footsteps before I see you. That is not nothing, after this many years of forgetting faces.",
                "حالا دو بار. کم‌کم پیش از آن‌که ببینمت صدای گام‌هایت را می‌شناسم. پس از این همه سالِ فراموش‌کردنِ چهره‌ها، این چیزِ کمی نیست.",
                LoreTrigger.ASCENSION, "2"),
            entry(22, "What Doesn't Reset.", "آنچه صفر نمی‌شود.",
                "The waves start over. The dark starts over. You do not. Not all the way. I have watched enough Wardens to know the difference between someone starting fresh and someone simply starting again.",
                "موج‌ها از نو آغاز می‌شوند. تاریکی از نو. تو اما نه؛ نه به تمامی. آن‌قدر نگهبان دیده‌ام که فرقِ میانِ آن را بدانم که از نو آغاز می‌کند و آن را که تنها دوباره شروع می‌کند.",
                LoreTrigger.ASCENSION, "3"),
            entry(23, "A Question I Don't Ask Often.", "پرسشی که کم می‌پرسم.",
                "I wonder, sometimes, if the Hollow gets tired the way you do. I have decided I do not want to know the answer badly enough to ask it.",
                "گاه می‌اندیشم که آیا دره هم مانند تو خسته می‌شود. به این نتیجه رسیده‌ام که آن‌قدر تشنهٔ دانستنِ پاسخ نیستم که بپرسمش.",
                LoreTrigger.ASCENSION, "5"),
            entry(24, "The Long Vigil.", "پاسداریِ بلند.",
                "I have had guardians who lasted a season and guardians who lasted a lifetime. I no longer sort them by which. I sort them by whether they came back. You keep coming back.",
                "نگهبانانی داشته‌ام که یک فصل دوام آوردند و نگهبانانی که یک عمر. دیگر آنان را با این سنجش نمی‌شناسم؛ با این می‌شناسم که آیا بازگشتند یا نه. تو پیوسته برمی‌گردی.",
                LoreTrigger.ASCENSION, "10"),
            entry(25, "Bare-Handed", "بی‌ابزار",
                "You did that with what you were given, not with what you bought. I do not know whether to call that discipline or stubbornness. Possibly they are the same root.",
                "آن کار را با آنچه به تو داده شد انجام دادی، نه با آنچه خریدی. نمی‌دانم نامش را انضباط بگذارم یا لجاجت. شاید هر دو یک ریشه باشند.",
                LoreTrigger.SECRET, "bare_handed"),
            entry(26, "A Full Set", "یک دستِ کامل",
                "Matched things hold together better than mismatched ones. I could have told you that before you spent the coin learning it.",
                "چیزهای هم‌جنس بهتر از ناهمجنس‌ها یکدیگر را نگه می‌دارند. می‌توانستم پیش از آن‌که سکه‌هایت را خرجِ یادگیری‌اش کنی، این را به تو بگویم.",
                LoreTrigger.SECRET, "full_set"),
            entry(27, "Mastery, Spent", "چیرگیِ خرج‌شده",
                "You have done that thing ten times so precisely that it no longer looks like effort. I recall when standing here felt like that too.",
                "آن کار را ده بار چنان دقیق انجام داده‌ای که دیگر به تلاش نمی‌ماند. به یاد دارم که ایستادنِ اینجا هم زمانی چنین بود.",
                LoreTrigger.SECRET, "mastery"),
            entry(28, "Reforged", "باز آهنگری‌شده",
                "Nothing stays as it was made. You, least of all. I mean that kindly.",
                "هیچ چیز همان‌طور که ساخته شد نمی‌ماند. تو که دیگر هیچ. این را از سر مهر می‌گویم.",
                LoreTrigger.SECRET, "reforged"),
            entry(29, "Six Mythics", "شش اسطوره‌ای",
                "I did not think there were six things left in this whole grove worth calling unique. I am glad to be wrong.",
                "گمان نمی‌کردم در این همه بیشه هنوز شش چیزِ شایستهٔ نامِ «یگانه» مانده باشد. خشنودم که اشتباه می‌کردم.",
                LoreTrigger.SECRET, "six_mythics"),
            entry(30, "No Potions Spent", "بی‌آن‌که معجونی خرج شود",
                "You never once needed the weakest thing I could offer you. I hope that was strength, and not simply luck standing beside you the whole way.",
                "هرگز به ضعیف‌ترین چیزی که می‌توانستم به تو بدهم نیازمند نشدی. امیدوارم آن نیرو بوده باشد، نه صرفاً بخت که همهٔ راه کنارت ایستاده است.",
                LoreTrigger.SECRET, "no_potions"),
            entry(31, "The Long Pause", "مکثِ دراز",
                "I do not mind if you leave and come back. I have had many years of practice at waiting. It is rather a specialty of mine, at this point.",
                "برایت فرقی نمی‌کند که بروی و بازگردی. سال‌هاست انتظار کشیدن را تمرین کرده‌ام. حالا دیگر کاری است ویژهٔ من.",
                LoreTrigger.SECRET, "long_pause"),
            entry(32, "Every Elite, Once", "هر نخبه، یک بار",
                "You have heard every fragment I have to whisper through them now. There is more to tell. There is always more. It simply is not theirs to carry.",
                "حالا هر پاره‌ای را که از زبانِ آنان نجوا می‌کنم شنیده‌ای. بیش از این هم برای گفتن هست. همیشه هست. فقط بارِ آن بر دوشِ آنان نیست.",
                LoreTrigger.SECRET, "every_elite"),
            entry(33, "Fastest Fall", "تندترین سقوط",
                "That was over before the Hollow finished sending it. I do not think it noticed yet.",
                "آن نبرد پیش از آن‌که دره فرستادنش را تمام کند به پایان رسید. گمان نمی‌کنم هنوز فهمیده باشد.",
                LoreTrigger.SECRET, "fastest_fall"),
            entry(34, "Two Hundred, Once More", "دویست، یک بار دیگر",
                "The first time was survival. I suspect you already know what the second time was. Say it to yourself, if not to me.",
                "بارِ نخست بقا بود. گمان می‌کنم خودت می‌دانی بارِ دوم چه بود. به خودت بگو، اگر نه به من.",
                LoreTrigger.SECRET, "wave200_twice")
    ));

    public static List<LoreEntry> all() {
        return ALL;
    }

    public static LoreEntry byId(String id) {
        return BY_ID.get(id);
    }

    /** The Persian side of every entry's title and body, joined, for the font's glyph derivation. */
    public static String persianText() {
        StringBuilder out = new StringBuilder();
        for (LoreEntry entry : ALL) {
            appendPersian(out, entry);
        }
        return out.toString();
    }

    private static void appendPersian(StringBuilder out, LoreEntry entry) {
        out.append(entry.title(com.amirrezahadipoor.herodefense.i18n.GameLanguage.PERSIAN))
            .append('\n')
            .append(entry.body(com.amirrezahadipoor.herodefense.i18n.GameLanguage.PERSIAN))
            .append('\n');
    }

    private static final Map<String, LoreEntry> BY_ID = index();

    private static Map<String, LoreEntry> index() {
        Map<String, LoreEntry> map = new LinkedHashMap<>();
        for (LoreEntry entry : ALL) {
            if (map.put(entry.id(), entry) != null) {
                throw new IllegalStateException("Duplicate codex id: " + entry.id());
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private static LoreEntry entry(
        int number,
        String title,
        String titlePersian,
        String body,
        String bodyPersian,
        LoreTrigger trigger,
        String triggerParam
    ) {
        return new LoreEntry(
            String.format("codex_%02d", number), number, title, body, titlePersian, bodyPersian,
            trigger, triggerParam
        );
    }
}
