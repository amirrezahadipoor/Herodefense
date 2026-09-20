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
    DEED_CODEX_10("Deed: read ten codex pages  |  + %1$s coins", "کارنامه: خواندن ده صفحه از دانشنامه  |  + %1$s سکه"),

    /** The Grove Codex screen's labels (moved out of {@code render/CodexOverlayRenderer} so the
     *  screen speaks both languages instead of pinning its chrome to English while the entries
     *  beneath it are read in Persian). */
    CODEX_TAB_LORE("LORE", "دانش‌ها"),
    CODEX_TAB_TROPHIES("TROPHIES", "جام‌ها"),
    CODEX_SHOWING("showing", "نمایش"),
    CODEX_TAP_TO_SHOW("tap to show", "برای نمایش بزنید"),
    CODEX_WRITTEN("%1$s / %2$s WRITTEN", "%1$s / %2$s نوشته شد"),
    CODEX_EARNED("%1$s / %2$s EARNED", "%1$s / %2$s یافت شد"),
    CODEX_LOCKED_HINT("The grove has not written this yet.", "بیشه هنوز این را ننوشته است."),
    TROPHY_HEADER("WARDEN'S TROPHIES", "جام‌های نگهبان"),
    TROPHY_TAP_HINT("Tap a trophy to read what earns it.", "روی جامی بزنید تا بدانید چه چیز آن را می‌آورد."),

    /** The new-run opening, one line set per ascension tier (§1). Tier 0 shipped, 1 and 2 their own,
     *  3+ one shared set. Three lines each, spoken in white over the push-in. */
    OPENING_0_ONE("Can you protect the World Tree?!", "می‌توانی از درخت جهان نگهبانی کنی؟!"),
    OPENING_0_TWO("Can you?", "می‌توانی؟"),
    OPENING_0_THREE("Are you sure?!", "مطمئنی؟!"),
    OPENING_1_ONE("Dark comes again.", "تاریکی دوباره می‌آید."),
    OPENING_1_TWO("I stand again.", "دوباره می‌ایستم."),
    OPENING_1_THREE("This time I go far.", "این بار دور می‌روم."),
    OPENING_2_ONE("The Hollow knows me now.", "دره حالا مرا می‌شناسد."),
    OPENING_2_TWO("Good. Let it fear.", "خوب است. بگذار بترسد."),
    OPENING_2_THREE("Roots first. Then flesh. Then Tree. Not today.", "اول ریشه‌ها. بعد گوشت. بعد درخت. امروز نه."),
    OPENING_3_ONE("New dawn. New fight.", "پگاه تازه. نبرد تازه."),
    OPENING_3_TWO("The Tree asks once.", "درخت یک بار می‌پرسد."),
    OPENING_3_THREE("So do I.", "من هم همین‌طور."),

    /** Branching end-of-run epilogues (§6). A = flawless, B = hard-fought, C/D/E = falls. */
    EPILOGUE_A_ONE("Two hundred waves. No step lost.", "دویست موج. بی‌آنکه گامی از دست برود."),
    EPILOGUE_A_TWO("The Hollow needs a new plan.", "دره به نقشهٔ تازه‌ای نیاز دارد."),
    EPILOGUE_A_THREE("Till then, the Tree and I stand.", "تا آن زمان، درخت و من می‌ایستیم."),
    EPILOGUE_B_ONE("Two hundred waves. All were close.", "دویست موج. همه از نفس‌افتاده."),
    EPILOGUE_B_TWO("I do not recall it all. I recall not letting go.", "همه‌اش را به یاد ندارم. به یاد دارم که رها نکردم."),
    EPILOGUE_B_THREE("That is enough. It has to be.", "همین بس است. باید بس باشد."),
    EPILOGUE_C_ONE("Not to the middle.", "نه تا میانه."),
    EPILOGUE_C_TWO("The Tree falls soft and quiet this early.", "درخت این‌قدر زود، نرم و خاموش می‌افتد."),
    EPILOGUE_C_THREE("Next time it will be loud.", "بار بعد پرصدا خواهد بود."),
    EPILOGUE_D_ONE("Close to the second root.", "نزدیک ریشهٔ دوم."),
    EPILOGUE_D_TWO("I went farther than last time. Far is not far enough.", "از دفعهٔ پیش دورتر رفتم. دور هنوز کافی نیست."),
    EPILOGUE_D_THREE("Again.", "دوباره."),
    EPILOGUE_E_ONE("One tree stood when I fell. That must count.", "وقتی افتادم یک درخت ایستاده بود. این باید به حساب بیاید."),
    EPILOGUE_E_TWO("The Hollow paid past wave one hundred. It just lasted a bit more.", "دره از موج صدم گذشته تاوان داد. فقط کمی بیشتر دوام آورد."),
    EPILOGUE_E_THREE("Next time it pays for all.", "بار بعد برای همه چیز تاوان می‌دهد."),
    EPILOGUE_TRANSITION_ONE("The Hollow is not gone. It is quiet while it learns to fall again.", "دره نرفته است. خاموش است، در حالی که دوباره افتادن می‌آموزد."),
    EPILOGUE_TRANSITION_TWO("Rise again. The Tree will still stand.", "دوباره برخیز. درخت همچنان خواهد ایستاد."),

    /** Whispering Wounds, one fragment pair per Elite affix (§4). */
    ELITE_BLIGHTBURST_ONE("It does not die so much as let go. What was holding it together was never its own to keep.",
        "آن‌قدر نمی‌میرد که رها می‌کند. آنچه پیوندش می‌داد هرگز از خودش نبود که نگه دارد."),
    ELITE_BLIGHTBURST_TWO("The burst is not rage. It's relief.", "آن انفجار خشم نیست؛ آسودگی است."),
    ELITE_ROOTWARD_ONE("The shield is not armor. It's a root, briefly recalling what it was for.",
        "آن سپر زره نیست؛ ریشه‌ای است که یک‌دم کار سابقش را به یاد می‌آورد."),
    ELITE_ROOTWARD_TWO("Even changed, a thing in it still tries to protect a thing. It's just no longer sure what.",
        "حتی با این شکل، چیزی درونش هنوز می‌کوشد از چیزی نگهبانی کند؛ فقط دیگر مطمئن نیست از چه."),
    ELITE_WEEPING_ONE("The ground it crosses does not heal. Not yet. Maybe not ever.",
        "زمینی که از آن می‌گذرد التیام نمی‌یابد. هنوز نه. شاید هرگز."),
    ELITE_WEEPING_TWO("Every trail leads back the same direction, if you follow it far enough: toward the Tree.",
        "هر ردّی را اگر به‌قدر کافی دنبال کنی به یک سو برمی‌گردد: سوی درخت."),
    ELITE_HOLLOWMOLT_ONE("It does not leave empty. Nothing here does.", "اینجا را خالی ترک نمی‌کند. هیچ‌چیز اینجا نمی‌کند."),
    ELITE_HOLLOWMOLT_TWO("Two smaller silences where one loud one stood. The Tree counts them as the same wound.",
        "دو خاموشی کوچک‌تر به‌جای یک صدای بلند. درخت هر دو را یک زخم می‌شمارد."),
    ELITE_GRAVEMOSS_ONE("The moss grows over the wound while the wound is still wearing it.",
        "خزه روی زخم می‌روید در حالی که زخم هنوز آن را به تن دارد."),
    ELITE_GRAVEMOSS_TWO("It is not healing. It is being reclaimed, slowly, by something patient.",
        "این التیام نیست؛ چیزی شکیبا دارد آهسته آن را بازپس می‌گیرد."),
    ELITE_CINDERHALO_ONE("Stand close and it will love you the way an ember loves a dry wind.",
        "نزدیکش بایست تا دوستت بدارد، آن‌گونه که اخگر دوست‌دار باد خشک است."),
    ELITE_CINDERHALO_TWO("The heat is not attack. It is grief, still warm from the fire that made it.",
        "آن گرما حمله نیست؛ سوگی است که هنوز از آتشی که ساختش گرم مانده."),

    /** Idle-whisper pool (§8): six Tree-voice lines, one per long pause, each shown once ever. */
    WHISPER_ONE("The roots kept your place while you were gone.", "ریشه‌ها جای تو را در نبودنت نگه داشتند."),
    WHISPER_TWO("Even the Tree dreams, little guardian, but it always wakes.", "حتی درخت هم خواب می‌بیند، نگهبان کوچک؛ اما همیشه بیدار می‌شود."),
    WHISPER_THREE("I counted every breath of your absence in falling leaves.", "هر نفسِ نبودنت را در برگ‌های ریخته شمردم."),
    WHISPER_FOUR("Rest is also a weapon, and you are learning to wield it.", "آسایش نیز سلاحی است، و تو می‌آموزی به کارش بگیری."),
    WHISPER_FIVE("The dark between battles is where roots grow deepest.", "تاریکیِ میان نبردها جایی است که ریشه‌ها عمیق‌تر می‌رویند."),
    WHISPER_SIX("Welcome back — the grove never stopped watching the paths.", "خوش آمدی — بیشه هرگز از نگریستن به راه‌ها دست نکشید."),

    /** Mythic item flavor (§7), one per slot's Mythic. */
    MYTHIC_SUNFALL("Shot once, long ago, at a high fall. The arrow did not come back whole. Nor did what it hit.",
        "زمانی، یک بار، به‌سوی پرتگاهی بلند رها شد. تیر هرگز سالم برنگشت؛ آنچه را زد هم سالم نماند."),
    MYTHIC_CROWN("Wear it and you see weak spots as the Hollow sees strength. The one true thing to aim for.",
        "بر سر بگذار تا نقطه‌ضعف را ببینی، آن‌سان که دره نیرو را می‌بیند. همان، یگانه. هدفِ راستین."),
    MYTHIC_BARK("Cut from the World Tree's bark when it could spare wood. It knows how to close a wound.",
        "از پوست درخت جهان جدا شد، در روزگاری که می‌توانست چوب ببخشد. راهِ بستن زخم را می‌داند."),
    MYTHIC_WINDRUNNER("Made for running. He never ran again after putting them on. He no longer needed to.",
        "برای دویدن ساخته شد. پس از پوشیدنشان دیگر ندوید. دیگر نیازی نداشت."),
    MYTHIC_VERDANT("A promise in sap. What heals you lets you keep healing.", "پیمانی در شیره. آنچه درمانت می‌کند می‌گذارد درمان بمانی."),
    MYTHIC_EMBERLESS("The ember that never went out, cooled and put to work. No longer left to spread.",
        "اخگری که هرگز خاموش نشد، سرد شد و به کار گرفته شد. دیگر برای پراکندن رها نیست.");

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
