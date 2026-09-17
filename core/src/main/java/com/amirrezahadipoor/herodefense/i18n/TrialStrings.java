package com.amirrezahadipoor.herodefense.i18n;

/**
 * The Convergence draft: the screen that offers four trials, and the thirteen trials themselves (roadmap R7.3).
 *
 * <p>Both halves are here because they are one sentence to a player. A draft card reads as a name, a green
 * reward line and a red risk line, and translating the chrome while the card's own three lines stayed English
 * would have left the one screen where the choice is made unreadable -- which is worse than leaving it alone,
 * because the Persian frame invites a player to read what is inside it.
 *
 * <p>Two conventions the rest of {@code i18n} already set, kept deliberately. Percentages are written with the
 * Arabic percent sign U+066A and Persian digits on the Persian side, because that is what
 * {@code GameNumbers.percent} produces for a computed value, and a screen that shows "۲۵٪" in one line and
 * "25%" in the next is a screen in two languages. And no Persian entry carries U+0654, the hamza above that
 * spells the ezafe: {@code arabic_reshaper}'s default configuration deletes it, the golden vectors prove it is
 * gone by the time the shaper is done, and a mark that cannot survive the pipeline is a mark that only misleads
 * a reader of the source.
 *
 * <p>The trials' modifiers stay in {@code trials/TrialId} beside the enum constants. Only the words moved.
 */
public enum TrialStrings implements Translated {

    /** The draft screen's own words. */
    OFFER_TITLE("THE CONVERGENCE OFFERS", "همگرایی پیشنهاد می‌دهد"),
    CHOOSE_TWO("CHOOSE TWO TRIALS", "دو آزمون برگزینید"),
    CARD_TRIAL("TRIAL", "آزمون"),
    CARD_CHOSEN("CHOSEN", "برگزیده"),
    BIND_NOTE("Two trials bind for this run only. Both bite and bless.",
        "دو آزمون تنها برای همین نبرد بسته می‌شوند. هر دو هم می‌گزند و هم برکت می‌دهند."),
    TAP_HINT("Tap two cards to begin the descent", "برای آغاز فرود روی دو کارت بزنید"),
    PICK_STATUS("%1$s of %2$s bound  |  rewards green, costs red",
        "%1$s از %2$s بسته شد  |  پاداش سبز، هزینه سرخ"),

    /** What a still-locked card says it is waiting for. The ten open trials have no entry and no line. */
    LOCK_HEAVY_CROWNS("Unlock 10 Codex entries", "۱۰ مدخل دانشنامه را بگشایید"),
    LOCK_BOSS_BOUNTY("Ascend for the first time", "نخستین صعود خود را انجام دهید"),

    /**
     * Two trials pay the same thing, so they share one entry rather than holding the same sentence under two
     * names: Swift Hollow and Miser's Pact both raise coin income by thirty percent, and one translation for
     * one effect is what keeps the two cards from drifting apart in a later edit.
     */
    REWARD_COIN_INCOME_UP("+30% coin income", "درآمد سکه ۳۰٪ بیشتر"),

    /** The thirteen trials: name, then the red line, then the green one -- the card's own reading order. */
    SWIFT_HOLLOW_TITLE("Swift Hollow", "دره شتاب"),
    SWIFT_HOLLOW_RISK("Enemies move 25% faster", "دشمنان ۲۵٪ تندتر حرکت می‌کنند"),

    DRY_VEINS_TITLE("Dry Veins", "رگ‌های خشک"),
    DRY_VEINS_RISK("Potions never drop", "هرگز معجون نمی‌افتد"),
    DRY_VEINS_REWARD("+1 talent point every 4 levels", "هر ۴ سطح یک امتیاز استعداد"),

    HEAVY_CROWNS_TITLE("Heavy Crowns", "تاج‌های سنگین"),
    HEAVY_CROWNS_RISK("Bosses deal 30% more damage", "آسیب غول‌ها ۳۰٪ بیشتر است"),
    HEAVY_CROWNS_REWARD("Every boss drops a Rare+ item", "هر غول یک آیتم کمیاب یا بهتر می‌اندازد"),

    THIN_BLOOD_TITLE("Thin Blood", "خون رقیق"),
    THIN_BLOOD_RISK("Hero has 20% less max health", "جان بیشینه قهرمان ۲۰٪ کمتر است"),
    THIN_BLOOD_REWARD("Hero deals 20% more damage", "آسیب قهرمان ۲۰٪ بیشتر است"),

    GLASS_ARROWS_TITLE("Glass Arrows", "تیرهای شیشه‌ای"),
    GLASS_ARROWS_RISK("Hero deals 20% less damage", "آسیب قهرمان ۲۰٪ کمتر است"),
    GLASS_ARROWS_REWARD("Hero attacks 25% faster", "حملات قهرمان ۲۵٪ تندتر است"),

    IRON_TIDE_TITLE("Iron Tide", "موج آهنین"),
    IRON_TIDE_RISK("+3 enemies every wave", "۳ دشمن بیشتر در هر موج"),
    IRON_TIDE_REWARD("+25% experience", "۲۵٪ تجربه بیشتر"),

    STONE_SKIN_TITLE("Stone Skin", "پوست سنگی"),
    STONE_SKIN_RISK("Enemies have 20% more health", "جان دشمنان ۲۰٪ بیشتر است"),
    STONE_SKIN_REWARD("Double item drops", "غنیمت آیتم دو برابر"),

    BOSS_BOUNTY_TITLE("Boss Bounty", "پاداش غول"),
    BOSS_BOUNTY_RISK("Bosses have 30% more health", "جان غول‌ها ۳۰٪ بیشتر است"),
    BOSS_BOUNTY_REWARD("+30% Heartwood at Ascension", "۳۰٪ چوب دل بیشتر در صعود"),

    MISERS_PACT_TITLE("Miser's Pact", "پیمان خساست"),
    MISERS_PACT_RISK("Shop prices up 30%", "بهای فروشگاه ۳۰٪ بیشتر است"),

    FAMISHED_EARTH_TITLE("Famished Earth", "خاک گرسنه"),
    FAMISHED_EARTH_RISK("-30% coin income", "درآمد سکه ۳۰٪ کمتر است"),
    FAMISHED_EARTH_REWARD("+10% dodge chance", "۱۰٪ شانس جاخالی بیشتر"),

    BLOOD_PRICE_TITLE("Blood Price", "بهای خون"),
    BLOOD_PRICE_RISK("Hero takes 15% more damage", "آسیب وارده به قهرمان ۱۵٪ بیشتر است"),
    BLOOD_PRICE_REWARD("+3% lifesteal", "۳٪ خون‌آشامی"),

    HOLLOW_CALLING_TITLE("Hollow Calling", "ندای دره"),
    HOLLOW_CALLING_RISK("Enemies deal 20% more damage", "آسیب دشمنان ۲۰٪ بیشتر است"),
    HOLLOW_CALLING_REWARD("Hero has 15% more max health", "جان بیشینه قهرمان ۱۵٪ بیشتر است"),

    /** The omen trial (R3.4), whose risk is the wave modifier the rest of the game measures itself without. */
    HOLLOW_OMENS_TITLE("Hollow Omens", "نشان‌های دره"),
    HOLLOW_OMENS_RISK("Every sixth wave carries an omen", "هر موج ششم یک نشان دارد"),
    HOLLOW_OMENS_REWARD("+25% coins on omen waves", "۲۵٪ سکه بیشتر در موج‌های نشان‌دار");

    private final String english;
    private final String persian;

    TrialStrings(String english, String persian) {
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
