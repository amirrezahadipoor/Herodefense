package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Boss bios in both languages, verbatim from {@code docs/STORY_CONTENT.md} section 3. Each renders as the second
 * paragraph of its Codex entry 9–16 detail view, unlocked together with the entry. The Persian follows
 * {@code docs/PERSIAN_PROOFREAD.md}: no Latin letters, no ASCII digits, the grove's own vocabulary.
 */
public final class BossLore {

    private BossLore() {
    }

    /** One bilingual bio. */
    private static final class Bio {
        final String english;
        final String persian;

        Bio(String english, String persian) {
            this.english = english;
            this.persian = persian;
        }
    }

    private static final Map<String, Bio> BIOS = bios();

    private static Map<String, Bio> bios() {
        Map<String, Bio> map = new LinkedHashMap<>();
        map.put("ANCIENT_GOLEM",
            new Bio(
                "Before it was a weapon of the Hollow, it was the forest's oldest guard. A stone keeper that had not moved from its post in longer than the Tree could recall. The Hollow did not need to turn it. It only needed to make it think the fight had never ended.",
                "پیش از آن‌که سلاح دره باشد، پیرترین نگهبان جنگل بود. نگهبان سنگی که بیش از آنچه درخت به یاد دارد از جای خود تکان نخورده بود. دره نیازی نداشت او را برگرداند؛ فقط لازم بود باورش کند که نبرد هرگز تمام نشده است."));
        map.put("THORN_MATRIARCH",
            new Bio(
                "She grew half the arena's Rootlings herself, back when growing things was all she did. What she plants now still takes root. It simply does not ask, and it is not kind.",
                "نیمی از ریشه‌زادهای میدان را خودش رویانده بود، در روزگاری که تنها کارش رویاندن بود. آنچه اکنون می‌کارد هنوز ریشه می‌دهد؛ فقط نمی‌پرسد و مهربان نیست."));
        map.put("EMBER_WYRM",
            new Bio(
                "When the Hollow first touched this ground, a thing here caught fire and never fully went out. The Wyrm is what that ember became once it learned to want more fuel.",
                "وقتی دره نخستین بار این خاک را لمس کرد، چیزی اینجا آتش گرفت و هرگز به‌تمام خاموش نشد. اژدر همان اخگری است که آموخت سوخت بیشتری بخواهد."));
        map.put("VOID_KNIGHT",
            new Bio(
                "No one here recalls what it looked like before. It does not either. It only recalls falling, and it has spent every year since trying to make another thing fall with it.",
                "هیچ‌کس اینجا به یاد ندارد که او پیش‌تر چه شکلی بود. خودش هم ندارد. تنها سقوط را به یاد می‌آورد، و از آن سال تاکنون همهٔ سال‌ها را صرفِ این کرده که چیز دیگری را هم با خودش بیندازد."));
        map.put("FROST_TITAN",
            new Bio(
                "There was frost in this grove before the Hollow, and all of it stayed where the cold put it. The Titan is the first of that frost to move. The Hollow only had to teach it which way to walk.",
                "پیش از دره نیز در این بیشه یخبندان بود و همه همان‌جا می‌ماندند که سرما نشانده بود. غولِ یخبندان نخستینِ آن یخبندان است که به راه افتاد؛ دره فقط باید راهِ رفتن را نشانش می‌داد."));
        map.put("SHADOW_LICH",
            new Bio(
                "The records it keeps are older than the Hollow. It wrote down every fall the grove took and never wrote down the name of what pushed. It still cannot write the difference.",
                "دفترهایی که نگه می‌دارد از خودِ دره کهن‌ترند. هر سقوطِ بیشه را نوشت و هرگز نامِ آنچه هلش داد ننوشت. هنوز هم نمی‌تواند این فرق را بنویسد."));
        map.put("STORM_COLOSSUS",
            new Bio(
                "It learned thunder from the Hollow's storms, and stillness from the mountain it was cut from. It chose both. That is why it does not hurry.",
                "تندر را از توفان‌های دره آموخت و سکون را از کوهی که از آن بریده شد. هر دو را برگزید؛ برای همین عجله نمی‌کند."));
        map.put("BLOODROOT_AVATAR",
            new Bio(
                "It remembers being part of me the way a branch remembers the rain it no longer feels. That memory was all the Hollow needed to dress in bark and call a traitor.",
                "به یاد دارد که پاره‌ای از من بود، آن‌گونه که شاخه بارانی را به یاد می‌آورد که دیگر حسش نمی‌کند. همین یاد برای دره بس بود تا آن را در پوست درخت بپوشاند و خائنش بخواند."));
        return Collections.unmodifiableMap(map);
    }

    /** The bio for a boss identity in the current language, or null when unknown. */
    public static String bioFor(String bossType) {
        return bioFor(bossType, GameLocale.current());
    }

    /** The bio for a boss identity in a specific language, or null when unknown. */
    public static String bioFor(String bossType, GameLanguage language) {
        if (bossType == null) {
            return null;
        }
        Bio bio = BIOS.get(bossType);
        if (bio == null) {
            return null;
        }
        return language == GameLanguage.PERSIAN ? bio.persian : bio.english;
    }

    /** Detail text for an entry: Tree-voice body first, boss bio second when one applies. */
    public static String detailFor(LoreEntry entry) {
        if (entry == null) {
            return "";
        }
        if (entry.trigger() != LoreTrigger.BOSS_FIRST_KILL) {
            return entry.body();
        }
        String bio = bioFor(entry.triggerParam());
        if (bio == null) {
            return entry.body();
        }
        return entry.body() + "\n\n" + bio;
    }

    /** Every boss identity that has a bio, for glyph-building and coverage sweeps. */
    public static java.util.List<String> allBossTypes() {
        return new java.util.ArrayList<>(BIOS.keySet());
    }

    /** The Persian text of every bio, for the font's glyph derivation. */
    public static String persianText() {
        StringBuilder out = new StringBuilder();
        for (Bio bio : BIOS.values()) {
            out.append(bio.persian).append('\n');
        }
        return out.toString();
    }
}
