package com.amirrezahadipoor.herodefense.i18n;

/**
 * The Convergence draft: the screen that offers four trials, and the thirteen trials themselves (roadmap R7.3).
 *
 * <p>Both halves are here because they are one sentence to a player. A draft card reads as a name, a green
 * reward line and a red risk line, and the screen where the choice is made has to be readable as one piece.
 *
 * <p>Percentages are written the way {@code GameNumbers.percent} produces them for a computed value, so a
 * tabled line and a computed line never disagree on screen.
 *
 * <p>The trials' modifiers stay in {@code trials/TrialId} beside the enum constants. Only the words moved.
 */
public enum TrialStrings implements Translated {

    /** The draft screen's own words. */
    OFFER_TITLE("THE CONVERGENCE OFFERS"),
    CHOOSE_TWO("CHOOSE TWO TRIALS"),
    CARD_TRIAL("TRIAL"),
    CARD_CHOSEN("CHOSEN"),
    BIND_NOTE("Two trials bind for this run only. Both bite and bless."),
    TAP_HINT("Tap two cards to begin the descent"),
    PICK_STATUS("%1$s of %2$s bound  |  rewards green, costs red"),

    /** What a still-locked card says it is waiting for. The ten open trials have no entry and no line. */
    LOCK_HEAVY_CROWNS("Unlock 10 Codex entries"),
    LOCK_BOSS_BOUNTY("Ascend for the first time"),

    /**
     * Two trials pay the same thing, so they share one entry rather than holding the same sentence under two
     * names: Swift Hollow and Miser's Pact both raise coin income by thirty percent, and one translation for
     * one effect is what keeps the two cards from drifting apart in a later edit.
     */
    REWARD_COIN_INCOME_UP("+30% coin income"),

    /** The thirteen trials: name, then the red line, then the green one -- the card's own reading order. */
    SWIFT_HOLLOW_TITLE("Swift Hollow"),
    SWIFT_HOLLOW_RISK("Enemies move 25% faster"),

    DRY_VEINS_TITLE("Dry Veins"),
    DRY_VEINS_RISK("Potions never drop"),
    DRY_VEINS_REWARD("+1 talent point every 4 levels"),

    HEAVY_CROWNS_TITLE("Heavy Crowns"),
    HEAVY_CROWNS_RISK("Bosses deal 30% more damage"),
    HEAVY_CROWNS_REWARD("Every boss drops a Rare+ item"),

    THIN_BLOOD_TITLE("Thin Blood"),
    THIN_BLOOD_RISK("Hero has 20% less max health"),
    THIN_BLOOD_REWARD("Hero deals 20% more damage"),

    GLASS_ARROWS_TITLE("Glass Arrows"),
    GLASS_ARROWS_RISK("Hero deals 20% less damage"),
    GLASS_ARROWS_REWARD("Hero attacks 25% faster"),

    IRON_TIDE_TITLE("Iron Tide"),
    IRON_TIDE_RISK("+3 enemies every wave"),
    IRON_TIDE_REWARD("+25% experience"),

    STONE_SKIN_TITLE("Stone Skin"),
    STONE_SKIN_RISK("Enemies have 20% more health"),
    STONE_SKIN_REWARD("Double item drops"),

    BOSS_BOUNTY_TITLE("Boss Bounty"),
    BOSS_BOUNTY_RISK("Bosses have 30% more health"),
    BOSS_BOUNTY_REWARD("+30% Heartwood at Ascension"),

    MISERS_PACT_TITLE("Miser's Pact"),
    MISERS_PACT_RISK("Shop prices up 30%"),

    FAMISHED_EARTH_TITLE("Famished Earth"),
    FAMISHED_EARTH_RISK("-30% coin income"),
    FAMISHED_EARTH_REWARD("+10% dodge chance"),

    BLOOD_PRICE_TITLE("Blood Price"),
    BLOOD_PRICE_RISK("Hero takes 15% more damage"),
    BLOOD_PRICE_REWARD("+3% lifesteal"),

    HOLLOW_CALLING_TITLE("Hollow Calling"),
    HOLLOW_CALLING_RISK("Enemies deal 20% more damage"),
    HOLLOW_CALLING_REWARD("Hero has 15% more max health"),

    /** The omen trial (R3.4), whose risk is the wave modifier the rest of the game measures itself without. */
    HOLLOW_OMENS_TITLE("Hollow Omens"),
    HOLLOW_OMENS_RISK("Every sixth wave carries an omen"),
    HOLLOW_OMENS_REWARD("+25% coins on omen waves");

    private final String english;

    TrialStrings(String english) {
        this.english = english;
    }

    @Override
    public String key() {
        return name();
    }

    @Override
    public String english() {
        return english;
    }

}
