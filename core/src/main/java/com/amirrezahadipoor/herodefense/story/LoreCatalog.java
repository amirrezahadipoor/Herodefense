package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Tree's own memories (roadmap ST3): the thirty-plus entries the Grove Codex shows the player,
 * each unlocked by the moment that earns it. The words were rewritten for a young reader (2026-09-22):
 * common words, short sentences, and the same spine as the spoken lines -- the waves are the nights,
 * the Hollow is the night itself, and the Tree remembers everyone who stood.
 */
public final class LoreCatalog {

    private LoreCatalog() {
    }

    private static final List<LoreEntry> ALL = List.copyOf(Arrays.asList(
            entry(1, "Before You.", "پیش از تو.",
                "Others stood here before you. I do not remember most of their names. I remember all of their last nights.",
                "پیش از تو، دیگرانی اینجا ایستاده‌اند. اسمِ بیشترشان را یادم نیست. همهٔ آخرین شب‌هایشان را یادم است.",
                LoreTrigger.WAVE_MILESTONE, "1"),
            entry(2, "The Three Directions.", "سه جهت.",
                "They never come from everywhere. Only from three sides. I never learned what holds back the fourth.",
                "از همه‌سو نمی‌آیند. فقط از سه سو. هرگز نفهمیدم چه چیزی چهارم را نگه می‌دارد.",
                LoreTrigger.WAVE_MILESTONE, "10"),
            entry(3, "Counting.", "شمارش.",
                "I used to count the seasons. Now I count the nights. Smaller pieces of time, and they pass just as slowly.",
                "فصل‌ها را می‌شمردم. حالا شب‌ها را. تکه‌های کوچک‌تری از زمان، و همان‌قدر آهسته می‌گذرند.",
                LoreTrigger.WAVE_MILESTONE, "20"),
            entry(4, "What Luck Finds.", "آنچه بخت پیدا می‌کند.",
                "Some of what drops from them still remembers being useful. Take it. I would rather you had it than the ground.",
                "برخی از چیزهایی که از آن‌ها می‌افتد هنوز به‌کار آمدن را به‌یاد دارد. بردار. دوست دارم تو داشته باشی، نه زمین.",
                LoreTrigger.WAVE_MILESTONE, "30"),
            entry(5, "The Quiet Ones.", "خاموش‌ها.",
                "Not every rootling attacks. Some just stand at the treeline and watch. I do not know if that is worse.",
                "هر ریشه‌زاد حمله نمی‌کند. بعضی فقط کنار خطِ درخت‌ها می‌ایستند و نگاه می‌کنند. نمی‌دانم آیا این بدتر است.",
                LoreTrigger.WAVE_MILESTONE, "40"),
            entry(6, "Old Names.", "اسم‌های کهنه.",
                "Rootling. Stonekin. Gloom Wolf. I gave three of those names to living things, once, when I meant something kinder.",
                "ریشه‌زاد. سنگ‌تن. گرگِ تیره. سه‌تا از این اسم‌ها را یک‌وقت خودم به موجودات زنده گذاشتم — وقتی معناش چیز مهربان‌تری بود.",
                LoreTrigger.WAVE_MILESTONE, "60"),
            entry(7, "The Long Middle.", "میانهٔ دراز.",
                "No song is ever written about this part. Not the falling, not the standing. Just the holding. Hold anyway.",
                "هیچ آوازی برای این بخش نوشته نمی‌شود. نه افتادن، نه ایستادن. فقط نگه‌داشتن. باز هم نگه‌دار.",
                LoreTrigger.WAVE_MILESTONE, "80"),
            entry(8, "A Grove Takes Root.", "جنگلی ریشه می‌دواند.",
                "I never asked for a second trunk or a third. I am glad for both. Grief is lighter when three trees carry it. So is standing guard.",
                "نه تنهٔ دوم را خواستم و نه سوم را. از هر دو خوشحالم. سوگ وقتی سه درخت آن را به‌دوش بکشند سبک‌تر است. نگهبانی هم.",
                LoreTrigger.WAVE_MILESTONE, "100"),
            entry(9, "What the Golem Guarded.", "گولم از چه چیزی نگهبانی می‌کرد.",
                "Before the Tree, there was a border stone, and the Golem kept it. The Hollow did not turn it — it only made the Golem believe the fight never ended. Its blow lands where you were a moment ago, not where you are. Put your shield there, not your feet.",
                "پیش از درخت، یک سنگِ مرزی بود و گولم نگهبانش. حُفره او را نبرداند؛ فقط باورش کرد که نبرد تموم نشده. ضربه‌اش به جایی می‌افتد که یک لحظه پیش بودی، نه جایی که هستی. سپرت را آنجا بگذار، نه پاهات را.",
                LoreTrigger.BOSS_FIRST_KILL, "ANCIENT_GOLEM"),
            entry(10, "The Matriarch's Garden.", "باغِ مادرِ خار.",
                "She is not attacking you with monsters. She is attacking you with her children. You deserve to know what you are ending. And why it might still be a mercy.",
                "او با هیولا به تو حمله نمی‌کند. با بچه‌هایش حمله می‌کند. حق دانی چه چیزی را تموم می‌کنی. و چرا باز هم ممکن است مهربانی باشد.",
                LoreTrigger.BOSS_FIRST_KILL, "THORN_MATRIARCH"),
            entry(11, "An Ember That Refused.", "اخگری که نپذیرفت.",
                "Fire is supposed to go out. This one said no. A no, given enough years, grows a shape. The Wyrm is that no, wearing scales.",
                "آتش باید خاموش شود. این یکی گفت نه. یک «نه» اگر سال‌ها عمر کند، شکلی پیدا می‌کند. اژدر همان نه است که فلس پوشیده.",
                LoreTrigger.BOSS_FIRST_KILL, "EMBER_WYRM"),
            entry(12, "The Shape of Falling.", "شکلِ افتادن.",
                "I once asked the Void Knight what it wanted, in the only language I have: stillness and time. It did not answer. I do not think it remembers the question. I think it only remembers falling — and wanting company on the way down.",
                "یک‌بار از شوالیهٔ تهی پرسیدم چه می‌خواهد؛ با تنها زبانی که دارم: سکوت و زمان. جواب نداد. فکر می‌کنم آن سؤال را یادش رفته. فکر می‌کنم فقط افتادن را یادش مانده — و خواستنِ یک هم‌رفتی در راهِ پایین.",
                LoreTrigger.BOSS_FIRST_KILL, "VOID_KNIGHT"),
            entry(13, "Winter That Walks.", "زمستانی که راه می‌رود.",
                "The frost came after the Hollow, not before. It keeps whatever it touches. The Titan is what kept walking after everything else froze.",
                "یخ بعد از حُفره آمد، نه پیش از آن. هرچه را لمس می‌کند نگه می‌دارد. غولِ یخ همان چیزی است که بعد از یخ‌زدنِ همه‌چیز هنوز راه می‌رفت.",
                LoreTrigger.BOSS_FIRST_KILL, "FROST_TITAN"),
            entry(14, "The Second Fall.", "سقوطِ دوم.",
                "It was not a boss before. It was the record keeper. Now it keeps the record of how many times the grove has fallen, and how.",
                "قبلاً غول نبود. دفتردار بود. حالا دفتر را نگه می‌دارد: جنگل چند بار و به چه شکلی ریزیده.",
                LoreTrigger.BOSS_FIRST_KILL, "SHADOW_LICH"),
            entry(15, "Thunder in Stone.", "تندر در سنگ.",
                "Stone that learned to hold thunder instead of moss. It never hurries. Thunder never does.",
                "سنگی که به‌جای خزه، تندر را در خودش نگه می‌دارد. هرگز عجله ندارد. تندر هیچ‌وقت عجله ندارد.",
                LoreTrigger.BOSS_FIRST_KILL, "STORM_COLOSSUS"),
            entry(16, "The Grove's Wound.", "زخمِ جنگل.",
                "A root of the World Tree, taken and twisted. It bleeds sap that never dries. The Tree knows its shape.",
                "ریشه‌ای از درخت جهان، گرفته و پیچیده. شیره‌ای می‌ریزد که هرگز خشک نمی‌شود. درخت شکلش را می‌شناسد.",
                LoreTrigger.BOSS_FIRST_KILL, "BLOODROOT_AVATAR"),
            entry(17, "On Letting Go.", "دربارهٔ رها کردن.",
                "Some of them stop fighting you and start fighting what is inside them instead. Usually they lose both fights at once.",
                "بعضی از آن‌ها دست از جنگیدن با تو برمی‌دارند و با آن چیزی که توی خودشان است می‌جنگند. معمولاً هر دو را یک‌وقت می‌بازند.",
                LoreTrigger.ELITE_KILL, "blightburst"),
            entry(18, "A Root's Last Job.", "آخرین کارِ یک ریشه.",
                "I do not control what the Hollow does with what was once mine. But I notice it still flinches toward protecting, even now. That is either hope, or a very old habit. I have stopped trying to tell the difference.",
                "در اختیار من نیست حُفره با چیزهایی که مالِ من بودند چه کند. اما می‌بینم هنوز به‌سمتِ محافظت پریشان می‌شود، حتی حالا. یا امید است یا عادتِ خیلی قدیمی. دیگر سعی نمی‌کنم فرقشان را بفهمم.",
                LoreTrigger.ELITE_KILL, "rootward_ward"),
            entry(19, "The Trail Home.", "ردی به‌سمتِ خانه.",
                "Every rotting thing wants to get back to the soil eventually. I only wish this kind of soil grew something other than more of itself.",
                "هر چیز پوسیده سرانجام می‌خواهد برگردد به خاک. فقط کاش این جنس خاک چیز دیگری برویاند، نه بیشتر از خودش.",
                LoreTrigger.ELITE_KILL, "weeping_rot"),
            entry(20, "Again.", "دوباره.",
                "You came back. I did not expect that. I am not sure the Hollow did either. That might be the only advantage either of us has left.",
                "برگشتی. انتظارش را نداشتم. مطمئن نیستم حُفره هم انتظارش را داشته. شاید همین تنها برتریِ باقی‌ماندهٔ هر دوی ما باشد.",
                LoreTrigger.ASCENSION, "1"),
            entry(21, "The Shape of a Habit.", "شکلِ یک عادت.",
                "Twice now. I am starting to recognize your footsteps before I see you. After this many years of forgetting faces, that is not nothing.",
                "حالا دو بار. دارم پیش از دیدنت، صدای قدم‌هایت را می‌شناسم. بعد از این همه سال فراموش‌کردنِ صورت‌ها، این چیز کوچکی نیست.",
                LoreTrigger.ASCENSION, "2"),
            entry(22, "What Does Not Reset.", "آنچه از نو نمی‌شود.",
                "The waves start over. The dark starts over. You do not. Not all the way. I have watched enough wardens to know the difference between starting fresh and just starting again.",
                "موج‌ها از نو شروع می‌شوند. تاریکی هم. تو نه. نه تا ته. آن‌قدر نگهبان دیده‌ام که فرقِ «از نو شروع کردن» و «فقط دوباره شروع کردن» را می‌دانم.",
                LoreTrigger.ASCENSION, "3"),
            entry(23, "A Question I Do Not Ask Often.", "سؤالی که کم می‌پرسم.",
                "Sometimes I wonder if the Hollow gets tired the way you do. I have decided I do not want to know the answer badly enough to ask.",
                "گاهی فکر می‌کنم حُفره هم مثل تو خسته می‌شود. به این نتیجه رسیده‌ام که آن‌قدر نمی‌خواهم جواب را بدانم که بپرسمش.",
                LoreTrigger.ASCENSION, "5"),
            entry(24, "The Long Watch.", "پاسِ بلند.",
                "I have had guards who lasted a season and guards who lasted a lifetime. I do not sort them by that anymore. I sort them by whether they came back. You keep coming back.",
                "نگهبانانی داشته‌ام که یک فصل دوام آوردند و نگهبانانی که یک عمر. دیگر با این‌هاشان دسته‌بندی نمی‌کنم. با این دسته‌بندی می‌کنم که آیا برگشتند یا نه. تو مدام برمی‌گردی.",
                LoreTrigger.ASCENSION, "10"),
            entry(25, "Bare-Handed", "خالی‌دست",
                "You did that with what you were given, not what you bought. I do not know whether to call that discipline or stubbornness. Maybe they are the same root.",
                "این کار را با چیزی که بهت داده بودند کردی، نه چیزی که خریدی. نمی‌دانم انضباط است یا لجبازی. شاید یک ریشه داشته باشند.",
                LoreTrigger.SECRET, "bare_handed"),
            entry(26, "A Full Set", "یک دستِ کامل",
                "Things that match hold together better. I could have told you that before you spent the coin to learn it.",
                "چیزهای هماهنگ بهتر از همه باقی می‌مانند. می‌توانستم پیش از آن‌که سکه‌هایت را خرجِ فهمیدنش کنی بگویم.",
                LoreTrigger.SECRET, "full_set"),
            entry(27, "Mastery, Spent", "چیرگیِ خرج‌شده",
                "You have done that ten times so clean it no longer looks like effort. I remember when standing here felt like that too.",
                "آن کار را ده بار آن‌قدر تمیز انجام داده‌ای که دیگر شبیه زحمت نیست. یادم هست ایستادنِ اینجا هم یک‌وقت همین‌طور بود.",
                LoreTrigger.SECRET, "mastery"),
            entry(28, "Reforged", "بازآهنگری‌شده",
                "Nothing stays the way it was made. Least of all you. I mean that kindly.",
                "هیچ چیز همان‌طور که ساخته شده نمی‌ماند. تو که آخرِ کار. این را از سرِ مهر می‌گویم.",
                LoreTrigger.SECRET, "reforged"),
            entry(29, "Six Mythics", "شش اسطوره",
                "I did not think there were six things left in this whole grove worth calling one-of-a-kind. I am glad I was wrong.",
                "نمی‌فکر می‌کردم در همهٔ این جنگل هنوز شش چیزِ «فقط یکی‌دور» مانده باشد. خوشحالم که اشتباه کردم.",
                LoreTrigger.SECRET, "six_mythics"),
            entry(30, "No Potions Spent", "بدون معجون",
                "You never once needed the weakest thing I could give you. I hope that was strength, and not just luck standing next to you the whole way.",
                "هرگز به ضعیف‌ترین چیزی که می‌توانستم بدهم نیاز پیدا نکردی. امیدوارم نیرو بوده باشد، نه فقط شانس که همهٔ راه کنارت می‌ایستاد.",
                LoreTrigger.SECRET, "no_potions"),
            entry(31, "The Long Pause", "مکثِ بلند",
                "Go if you have to. Come back when you can. Waiting is my specialty by now — I have had a lot of practice.",
                "اگر باید بروی برو. هر وقت می‌توانی برگرد. انتظار حالا تخصصِ من شده — تمرینش را کرده‌ام.",
                LoreTrigger.SECRET, "long_pause"),
            entry(32, "Every Elite, Once", "هر نخبه، یک‌بار",
                "You have heard every fragment I can whisper through them now. There is more to tell. There is always more. It is just not theirs to carry.",
                "حالا هر تکه‌ای را که از دلشان نجوا می‌کنم شنیده‌ای. بیشترش هم هست برای گفتن. همیشه هست. فقط بارِ آن با آن‌ها نیست.",
                LoreTrigger.SECRET, "every_elite"),
            entry(33, "Fastest Fall", "تندترین سقوط",
                "That was over before the Hollow finished sending it. I do not think it noticed yet.",
                "آن نبرد پیش از آن‌که حُفره فرستادنش را تموم کند تموم شد. فکر می‌کنم هنوز متوجه نشده.",
                LoreTrigger.SECRET, "fastest_fall"),
            entry(34, "Two Hundred, Once More", "دویست، یک‌بار دیگر",
                "The first time was survival. I suspect you already know what the second time was. Say it to yourself, if not to me.",
                "بارِ اول برای زنده ماندن بود. شک دارم خودت می‌دانی بارِ دوم چه بود. اگر به من نمی‌گویی، به خودت بگو.",
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
        out.append(entry.title(GameLanguage.PERSIAN))
            .append('\n')
            .append(entry.body(GameLanguage.PERSIAN))
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
