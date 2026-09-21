package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Boss bios in both languages, verbatim from {@code docs/STORY_CONTENT.md} section 3. Each renders as the second
 * paragraph of its Codex entry 9–16 detail view, unlocked together with the entry. The Persian follows
 * {@code docs/PERSIAN_PROOFREAD.md}: no Latin letters, no ASCII digits, the grove's own vocabulary
 * (rewritten 2026-09-22 for a young reader: common words, the Hollow named «حُفره»).
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
                "Before it was a weapon of the Hollow, it was the forest's oldest guard. A stone keeper that had not moved from its post in longer than the Tree could remember. The Hollow did not need to turn it. It only needed to make it think the fight had never ended.",
                "پیش از آن‌که سلاحِ حُفره باشد، پیرترین نگهبانِ جنگل بود. نگهبانِ سنگی که از جای خود تکان نخورده بود — از وقتی که درخت به یاد دارد. حُفره نیازی نداشت او را برگرداند؛ فقط لازم بود باورش کند که نبرد تموم نشده است."));
        map.put("THORN_MATRIARCH",
            new Bio(
                "She grew half the arena's Rootlings herself, back when growing things was all she did. What she plants now still takes root. It simply does not ask, and it is not kind.",
                "نیمی از ریشه‌زادای میدان را خودش روییده بود، روزگاری که تنها کارش رویاندن بود. آنچه حالا می‌کارد هنوز ریشه می‌دهد؛ فقط نمی‌پرسد، و مهربان نیست."));
        map.put("EMBER_WYRM",
            new Bio(
                "When the Hollow first touched this ground, a thing here caught fire and never fully went out. The Wyrm is what that ember became once it learned to want more fuel.",
                "وقتی حُفره نخستین بار این خاک را لمس کرد، چیزی اینجا آتش گرفت و هرگز به‌کلی خاموش نشد. اژدر همان اخگری است که آموخت سوختِ بیشتری بخواهد."));
        map.put("VOID_KNIGHT",
            new Bio(
                "No one here remembers what it looked like before. It does not either. It only remembers falling, and it has spent every year since trying to make another thing fall with it.",
                "هیچ‌کس اینجا نمی‌داند پیش‌تر چه شکلی بود. خودش هم نه. فقط سقوط را به یاد دارد — و هر سالِ از آن روز را صرفِ این کرده که چیزی دیگر را هم با خودش پایین بکشد."));
        map.put("FROST_TITAN",
            new Bio(
                "There was frost in this grove before the Hollow, and all of it stayed where the cold put it. The Titan is the first of that frost to move. The Hollow only had to teach it which way to walk.",
                "پیش از حُفره هم در این بیشه یخ بود و همه‌اش همان‌جا می‌ماند که سرما گذاشته بود. غولِ یخ نخستینِ آن یخ است که به راه افتاد. حُفره فقط باید جهتِ راه را بهش یاد داد."));
        map.put("SHADOW_LICH",
            new Bio(
                "The records it keeps are older than the Hollow. It wrote down every fall the grove took and never wrote down the name of what pushed. It still cannot write the difference.",
                "دفترهایی که نگه می‌دارد از خودِ حُفره کهن‌ترند. هر سقوطی را نوشت، اما هرگز نامِ آن چیزی که هل داد ننوشت. هنوز نمی‌تواند این فرق را بنویسد."));
        map.put("STORM_COLOSSUS",
            new Bio(
                "It learned thunder from the Hollow's storms, and stillness from the mountain it was cut from. It chose both. That is why it does not hurry.",
                "تندر را از توفان‌های حُفره آموخت و سکوت را از کوهی که از آنجا بریده شد. هر دو را خودش انتخاب کرد؛ برای همین عجله ندارد."));
        map.put("BLOODROOT_AVATAR",
            new Bio(
                "It remembers being part of me the way a branch remembers the rain it no longer feels. That memory was all the Hollow needed to dress in bark and call a traitor.",
                "یادش مانده که پاره‌ای از من بوده، همان‌طور که شاخه بارانی را یاد دارد که دیگر حسش نمی‌کند. همین به حُفره بس بود تا در پوستِ درخت بپوشد و آن را خائن بخواند."));
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
