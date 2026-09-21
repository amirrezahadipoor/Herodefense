package com.amirrezahadipoor.herodefense.i18n;

/**
 * The game's own voice (roadmap R7.3): what a boss's first title card says, what the Tree says while a
 * tree is planted, what the Warden says to themselves at a wave milestone, what the Hollow says to the
 * player, and the line the trophy chime puts on screen.
 *
 * <p>These are the places where the game speaks rather than reports, which is why they are one table
 * rather than four: they share three narrators, and a narrator whose voice differs between screens is a
 * narrator the player notices. The words were rewritten for a young reader (2026-09-22): common words,
 * short sentences, and one idea the whole run can follow -- the waves are the nights, the Hollow is the
 * night itself, and two hundred nights later the dawn comes back.
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
    BOSS_ANCIENT_GOLEM("ANCIENT GOLEM — the oldest guard, still on duty.",
        "گولم باستانی — کهن‌ترین نگهبان؛ هنوز سرِ پست."),
    BOSS_THORN_MATRIARCH("THORN MATRIARCH — she grew half your enemies.",
        "مادرِ خار — نصفِ دشمنانت از او روییده‌اند."),
    BOSS_EMBER_WYRM("EMBER WYRM — a fire that never went out.",
        "اژدرِ اخگر — آتشی که هرگز خاموش نشده."),
    BOSS_VOID_KNIGHT("VOID KNIGHT — it fell, and forgot the way back.",
        "شوالیهٔ تهی — سقوط کرد و راهِ بازگشت را فراموش."),
    BOSS_FROST_TITAN("FROST TITAN — winter that learned to walk.",
        "غولِ یخ — زمستانی که راه رفتن آموخت."),
    BOSS_SHADOW_LICH("SHADOW LICH — keeper of the second fall.",
        "لیچِ سایه — نگهبانِ سقوطِ دوم."),
    BOSS_STORM_COLOSSUS("STORM COLOSSUS — thunder stored in a stone chest.",
        "غولِ طوفان — تندری که در سینهٔ سنگی جا شد."),
    BOSS_BLOODROOT_AVATAR("BLOODROOT AVATAR — the forest's own wound, walking.",
        "آواتارِ خون‌ریشه — زخمِ خودِ جنگل، راه می‌رود."),

    /** The planting ceremony's five beats, in the order the ceremony walks through them. */
    CEREMONY_WALK_OUT("A tree should not carry this alone. Not anymore.",
        "یک درخت نباید همه‌ی این بار را تنهایی به دوش بکشد. دیگر نه."),
    CEREMONY_PLANT("So we plant another. Grow loud. Grow angry. Grow.",
        "پس یکی دیگر می‌کاریم. بلند رشد کن. عصبانی رشد کن. رشد کن."),
    CEREMONY_WATER("I will hold the line. That is what I am for.",
        "من خط دفاع را نگه می‌دارم. همین کار من است."),
    CEREMONY_GROW("Two of us now. I remember what mornings sound like.",
        "حالا ما دویم. یاد دارم صبح‌ها چه صدایی دارند."),
    CEREMONY_WALK_BACK("Now hold the line. Both of us need you.",
        "حالا خط دفاع را نگه دار. هر دویِ ما به تو نیاز داریم."),

    /** The Warden's reflections at wave milestones: 25, 50, 75, 125, 150, 175. */
    REFLECTION_WAVE_25("The wolves fear something bigger than me. That should scare me more.",
        "گرگ‌ها از چیزی بزرگ‌تر از من می‌ترسند. باید بیشتر بترسم."),
    REFLECTION_WAVE_50("Half of what I have killed, I once knew. I try not to think about it.",
        "نیمی از چیزهایی که شکست دادم، روزی می‌شناختم. سعی می‌کنم بهش فکر نکنم."),
    REFLECTION_WAVE_75("Past the treeline, the ground is wrong. Not ground at all.",
        "آن‌سوی خطِ درخت‌ها، زمین غلط است. اصلاً زمین نیست."),
    REFLECTION_WAVE_125("Three trees now. Three times to lose. I would still make the trade.",
        "حالا سه درخت. سه برابرِ شانسِ باخت. باز هم همین معامله را می‌بستم."),
    REFLECTION_WAVE_150("It stopped sending the weak ones first. It is out of patience.",
        "دیگر آن‌های ضعیف را اول نمی‌فرستد. صبرش تموم شده."),
    REFLECTION_WAVE_175("What is left may be the last. Or it wants me to believe that.",
        "شاید آنچه مانده، آخرین باشد. یا می‌خواهد من این‌طور باور کنم."),

    /** The trophy line: a count when there are too many to read, otherwise the names joined. */
    TROPHY_COUNT("Trophy - %1$s earned", "جام: %1$s کسب شد"),
    TROPHY_NAMES("Trophy - %1$s", "جام: %1$s"),
    TROPHY_AND(" and ", " و "),

    /** The Hollow (roadmap ST1): the one voice that talks to the player, not the Warden. */
    HOLLOW_DEATH_FIRST("You fell. Not the Warden — you. I felt it happen.",
        "تو افتادی؛ نه نگهبان — خودِ تو. من حسش کردم."),
    HOLLOW_DEATH_AGAIN("Again. You always get up. I never get tired of watching.",
        "باز. تو همیشه بلند می‌شوی. من هرگز از تماشا کردنش خسته نمی‌شوم."),
    HOLLOW_SPARE("You spared it? It was not even fighting. Mercy. I remember that word. No one uses it anymore.",
        "رهاش کردی؟ اصلاً نمی‌جنگید. مهربانی. این کلمه را یادم هست. دیگر کسی از آن استفاده نمی‌کند."),
    HOLLOW_WAVE100("Halfway there. The Tree thanks you. I do not have to hurry. You do.",
        "نصفِ راه. درخت از تشکر کردن درگیر است. من عجله ندارم. تو داری."),
    HOLLOW_HELLO("I am the night. They called me the Hollow. This is the last tree — and I have come for it.",
        "من شب هستم. مرا «حُفره» می‌نامیدند. این آخرین درخت است — و من برای همین آمده‌ام."),
    HOLLOW_MERCY_HABIT("Three spared. You call it kindness. So do I. Mercy grows roots here too — watch what sprouts.",
        "سه‌تا را رهایشان کردی. مهربانی می‌نامیش. من هم. رحمت اینجا هم ریشه می‌دهد — ببین چه چیزی جوانه می‌زند."),
    HOLLOW_VERDICT_MERCIFUL("Last time, you let some of them go. The Tree calls that mercy. I call it a debt — and I remember my debts.",
        "دفعهٔ قبل، بعضی از آن‌ها را رها کردی. درخت بهش می‌گوید مهربانی. من بهش می‌گویم بدهی — و بدهی‌ها را به‌یاد می‌آورم."),
    HOLLOW_VERDICT_STERN("Last time, nothing on that field lived because you loved it. The Tree calls that victory. I call it an inventory.",
        "دفعهٔ قبل هیچ‌کدام از آن میدان زنده نماند چون تو دوستش داشتی. درخت بهش می‌گوید پیروزی. من بهش می‌گویم سیاهه."),

    /** The Hollow's half-health beat, one per boss identity. */
    HOLLOW_BOSS_GOLEM("It is slipping. It was never guarding you — it just cannot stop.",
        "دارد لق می‌زند. هرگز نگهبانِ تو نبود — فقط نمی‌تواند بایستد."),
    HOLLOW_BOSS_MATRIARCH("She calls the garden home. You are the frost.",
        "باغ را خانه می‌داند. تو یخِ تازه‌ای."),
    HOLLOW_BOSS_WYRM("Its no grows quiet. Push.",
        "«نه»‌اش کم‌صدا می‌شود. فشار بیاور."),
    HOLLOW_BOSS_VOID("It falls and wants company. Give it none.",
        "می‌افتد و هم‌کف می‌خواهد. نده."),
    HOLLOW_BOSS_TITAN("Winter keeps what it touches. Touch it back.",
        "زمستان هرچه را لمس کند می‌گیرد. تو هم لمسش کن."),
    HOLLOW_BOSS_LICH("It is writing your name down. Do not give it a long one.",
        "دارد اسمت را می‌نویسد. بلندش نکن."),
    HOLLOW_BOSS_COLOSSUS("Thunder never hurries. You should.",
        "رعد عجله ندارد. تو داشته باش."),
    HOLLOW_BOSS_BLOODROOT("The wound fights the cure. It always has.",
        "زخم با درمانش می‌جنگد. همیشه همین بوده."),

    /** The Tree's thank-you, spoken in the box the moment a run is completed: the summary waits for it,
     *  then rises — the victory's own parting word, in the Tree's own blips. */
    TREE_VICTORY("You kept the light alive, night after night. The dawn remembers you.",
        "تو نور را زنده نگه داشتی، شب به شب. سحر تو را به‌یاد خواهد داشت."),

    /** The Tree's daily gift (roadmap ST5): the return hook, spoken once a day. */
    DAILY_GIFT("Two heartwood, saved from yesterday. The Tree keeps count of your days. So do I.",
        "دو چوب‌جان از دیروز پس‌انداز شده. درخت روزهایت را می‌شمارد. من هم."),

    /** The Vigil Deeds (roadmap ST2): the run's named goals, paid once each. */
    DEED_WAVE_10("Deed: held to wave 10  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۱۰  |  + %1$s سکه"),
    DEED_WAVE_25("Deed: held to wave 25  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۲۵  |  + %1$s سکه"),
    DEED_WAVE_50("Deed: held to wave 50  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۵۰  |  + %1$s سکه"),
    DEED_WAVE_100("Deed: held to wave 100  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۱۰۰  |  + %1$s سکه"),
    DEED_WAVE_150("Deed: held to wave 150  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۱۵۰  |  + %1$s سکه"),
    DEED_WAVE_200("Deed: held to wave 200  |  + %1$s coins", "کارنامه: ایستادگی تا موج ۲۰۰  |  + %1$s سکه"),
    DEED_FIRST_BOSS("Deed: first boss felled  |  + %1$s coins", "کارنامه: اولین غول سرنگون شد  |  + %1$s سکه"),
    DEED_BOSSES_5("Deed: five bosses in one run  |  + %1$s coins", "کارنامه: پنج غول در یک دور  |  + %1$s سکه"),
    DEED_CLEAN_25("Deed: wave 25 without a potion  |  + %1$s coins", "کارنامه: موج ۲۵ بدون معجون  |  + %1$s سکه"),
    DEED_FLAWLESS_50("Deed: wave 50, never down, no potion  |  + %1$s coins",
        "کارنامه: موج ۵۰ بدون افتادن و بدون معجون  |  + %1$s سکه"),
    DEED_CODEX_10("Deed: read ten codex pages  |  + %1$s coins", "کارنامه: خواندن ده صفحه از دانشنامه  |  + %1$s سکه"),

    /** The Grove Codex screen's labels (moved out of {@code render/CodexOverlayRenderer} so the
     *  screen speaks both languages instead of pinning its chrome to English while the entries
     *  beneath it are read in Persian). */
    CODEX_TAB_LORE("LORE", "دانش‌ها"),
    CODEX_TAB_TROPHIES("TROPHIES", "جام‌ها"),
    CODEX_SHOWING("showing", "نمایش"),
    CODEX_TAP_TO_SHOW("tap to show", "برای نمایش بزن"),
    CODEX_WRITTEN("%1$s / %2$s WRITTEN", "%1$s / %2$s نوشته شد"),
    CODEX_EARNED("%1$s / %2$s EARNED", "%1$s / %2$s یافت شد"),
    CODEX_LOCKED_HINT("The Tree has not written this yet.", "درخت هنوز این را ننوشته است."),
    TROPHY_HEADER("WARDEN'S TROPHIES", "جام‌های نگهبان"),
    TROPHY_TAP_HINT("Tap a trophy to read what earns it.", "روی یک جام بزن تا ببینی چه چیزی آن را می‌آورد."),

    /** The new-run opening, one line set per ascension tier (§1). Tier 0 shipped, 1 and 2 their own,
     *  3+ one shared set. Three lines each, typed in the box over the push-in. */
    OPENING_0_ONE("Can you protect the World Tree?!", "می‌توانی از درخت جهان نگهبانی کنی؟!"),
    OPENING_0_TWO("Can you?", "می‌توانی؟"),
    OPENING_0_THREE("Are you sure?!", "مطمئنی؟!"),
    OPENING_1_ONE("The dark comes back. It always does.", "تاریکی برمی‌گردد. همیشه برمی‌گردد."),
    OPENING_1_TWO("The Tree is tired. So am I.", "درخت خسته است. من هم."),
    OPENING_1_THREE("Tonight we go further.", "امشب بیشتر پیش می‌رویم."),
    OPENING_2_ONE("It knows my name by now.", "الان اسم من را می‌شناسد."),
    OPENING_2_TWO("Good. Let it remember.", "خوب است. بگذار به‌یاد بیاورد."),
    OPENING_2_THREE("Roots first. Then the dark. Not today.", "اول ریشه‌ها. بعد تاریکی. امروز نه."),
    OPENING_3_ONE("New dawn. Same fight.", "سحرِ تازه. همان نبرد."),
    OPENING_3_TWO("The Tree asks: one more watch?", "درخت می‌پرسد: یک پاسِ دیگر؟"),
    OPENING_3_THREE("Say yes.", "بله بگو."),

    /** Branching end-of-run epilogues (§6). A = flawless, B = hard-fought, C/D/E = falls. */
    EPILOGUE_A_ONE("Two hundred nights. Not one step lost.", "دویست شب. بی‌آنکه گامی از دست بدهم."),
    EPILOGUE_A_TWO("The night needs a new plan.", "شب به نقشهٔ تازه‌ای نیاز دارد."),
    EPILOGUE_A_THREE("Until then, the Tree and I stand.", "تا آن زمان، من و درخت می‌ایستیم."),
    EPILOGUE_B_ONE("Two hundred nights. Every one of them close.", "دویست شب. هرکدامش از تهٔ جان."),
    EPILOGUE_B_TWO("I do not remember all of it. I remember not letting go.", "همه‌اش را یادم نیست. یادم هست که رها نکردم."),
    EPILOGUE_B_THREE("That is enough. It has to be.", "همین بس است. باید بس باشد."),
    EPILOGUE_C_ONE("Not even the middle.", "نه حتی تا میانه."),
    EPILOGUE_C_TWO("The Tree fell quiet so early. It should not have.", "درخت این‌قدر زود خاموش شد. نباید این‌طور می‌شد."),
    EPILOGUE_C_THREE("Next time it is loud.", "دفعهٔ بعد بلند می‌شود."),
    EPILOGUE_D_ONE("So close to the second root.", "آن‌قدر نزدیکِ ریشهٔ دوم."),
    EPILOGUE_D_TWO("I went farther than before. Far is not far enough.", "از قبل دورتر رفتم. دور هنوز کافی نیست."),
    EPILOGUE_D_THREE("Again.", "دوباره."),
    EPILOGUE_E_ONE("One tree stood when I fell. That counts.", "وقتی افتادم یک درخت ایستاده بود. این به‌حساب می‌آید."),
    EPILOGUE_E_TWO("The night paid for this run. It just lasted a little longer than me.",
        "شب بابت این دور تاوان داد. فقط کمی بیشتر از من دوام آورد."),
    EPILOGUE_E_THREE("Next time it pays for everything.", "دفعهٔ بعد برای همه‌چیز تاوان می‌دهد."),
    EPILOGUE_TRANSITION_ONE("The night is not gone. It is quiet, learning how to fall again.",
        "شب نرفته. ساکت است. دارد دوباره یاد می‌گیرد چگونه بریزد."),
    EPILOGUE_TRANSITION_TWO("Stand up. The Tree is still standing.", "بلند شو. درخت هنوز ایستاده است."),

    /** Whispering Wounds, one fragment pair per Elite affix (§4). */
    ELITE_BLIGHTBURST_ONE("It does not die. It just lets go — everything at once.",
        "نمی‌میرد. فقط رها می‌کند — همه‌چیز را یک‌جور."),
    ELITE_BLIGHTBURST_TWO("That burst is not anger. It is relief.", "آن انفجار خشم نیست. رهایی است."),
    ELITE_ROOTWARD_ONE("That shield is not armor. It is a root, remembering its job.",
        "آن سپر زره نیست. ریشه‌ای است که کارش را به‌یاد آورده."),
    ELITE_ROOTWARD_TWO("Even like this, it still tries to protect. It just forgot what.",
        "حتی این‌طور، هنوز می‌خواهد نگهبانی کند. فقط فراموش کرده از چه."),
    ELITE_WEEPING_ONE("Where it walks, the ground never heals.", "جایی که رد می‌شود، زمین التیام نمی‌کند."),
    ELITE_WEEPING_TWO("Follow its trail long enough. It leads to the Tree.", "ردش را تا ته دنبال کن. به درخت می‌رسد."),
    ELITE_HOLLOWMOLT_ONE("It never leaves a place empty. Nothing here does.",
        "هیچ‌وقت جایی را خالی نمی‌گذارد. اینجا هیچ‌کس این کار را نمی‌کند."),
    ELITE_HOLLOWMOLT_TWO("Two small silences where one loud one stood.", "دو سکوتِ کوچک به‌جای یک صدای بلند."),
    ELITE_GRAVEMOSS_ONE("The moss covers the wound while the wound is still there.",
        "خزه روی زخم می‌روید، در حالی که زخم هنوز آنجاست."),
    ELITE_GRAVEMOSS_TWO("That is not healing. That is something patient taking it back.",
        "این التیام نیست. چیزی صبور داردش برمی‌گرداند."),
    ELITE_CINDERHALO_ONE("Stand too close and it loves you — the way an ember loves wind.",
        "خیلی نزدیک نرو. دوستت می‌دارد — به‌اندازه‌ای که اخگر دوست دارد باد را."),
    ELITE_CINDERHALO_TWO("That heat is not attack. It is grief, still warm.", "آن گرما حمله نیست. سوگ است که هنوز گرم است."),

    /** Idle-whisper pool (§8): six Tree-voice lines, one per long pause, each shown once ever. */
    WHISPER_ONE("The roots kept your seat warm while you were gone.", "ریشه‌ها در نبودت جای تو را گرم نگه داشتند."),
    WHISPER_TWO("The Tree dreams, little guard — and it always wakes up.", "درخت خواب می‌بیند، نگهبانِ کوچک — و همیشه بیدار می‌شود."),
    WHISPER_THREE("I counted your absence in falling leaves — you were out a while.",
        "نبودنت را با برگ‌های ریزش‌کرده شمردم. دیر بودی."),
    WHISPER_FOUR("Rest is a weapon too — you are getting good at it.", "استراحت هم سلاح است. داری حرفه‌ای‌اش می‌شوی."),
    WHISPER_FIVE("The roots grow deepest in the quiet between fights.", "ریشه‌ها در سکوتِ میانِ نبردها عمیق‌تر می‌روند."),
    WHISPER_SIX("You are back — the paths never stopped watching for you.", "برگشتی. راه‌ها هرگز از نگاه کردن به تو چشم نبردند."),

    /** Speaker labels over the dialogue box: the box types every message in the speaker's own voice, and
     *  the label tells who is speaking. Same three speakers the blips already tell apart by ear. */
    SPEAKER_WARDEN("WARDEN", "نگهبان"),
    SPEAKER_TREE("TREE", "درخت"),
    SPEAKER_HOLLOW("HOLLOW", "حُفره"),

    /** Mythic item flavor (§7), one per slot's Mythic. */
    MYTHIC_SUNFALL("Shot once, long ago, at a high fall. The arrow never came back whole.",
        "یک‌بار، خیلی قبل‌تر، به‌سوی پرتگاهی بلند رها شد. تیر هرگز سالم برنگشت."),
    MYTHIC_CROWN("Wear it and you see weak spots the way the Hollow sees strong ones.",
        "سر بگذار. نقطه‌های ضعیف را همان‌طور می‌بینی که حُفره نقطه‌های قوی را می‌بیند."),
    MYTHIC_BARK("Cut from the World Tree's bark when it could spare wood. It knows how to close a wound.",
        "از پوست درخت جهان جدا شد، وقتی‌ها که می‌توانست بدهد. راهِ بستنِ زخم را می‌داند."),
    MYTHIC_WINDRUNNER("Made for running. He never ran again after he put them on.",
        "برای دویدن ساخته شد. بعد از پوشیدنش دیگر ندوید."),
    MYTHIC_VERDANT("A promise in sap. What heals you lets you keep healing.",
        "پیمانی در شیره. آنچه تو را خوب می‌کند، تو را خوب نگه می‌دارد."),
    MYTHIC_EMBERLESS("The ember that never went out — cooled, and put to work.",
        "اخگری که هرگز خاموش نشد. سرد شد و به‌کار افتاد.");

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
