package com.amirrezahadipoor.herodefense.trials;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.TrialStrings;

/**
 * The thirteen Convergence Trials: paired risk/reward modifiers drafted before a run (pick 2
 * of 4 offered) and active for that run only. Each trial names its reward first — the green
 * line on the draft card — and its risk second, the red cost line.
 *
 * <p>The three lines a card shows are {@link TrialStrings} entries rather than fields here (R7.3): the
 * modifiers and the icon key are gameplay data and stay, the words live where both languages exist. The enum
 * constant's own {@code name()} is what a save stores and what the balance gates measure, so no language can
 * orphan a drafted trial.
 */
public enum TrialId {
    SWIFT_HOLLOW(
        TrialStrings.SWIFT_HOLLOW_TITLE, TrialStrings.SWIFT_HOLLOW_RISK,
        TrialStrings.REWARD_COIN_INCOME_UP, "speed"
    ),
    DRY_VEINS(
        TrialStrings.DRY_VEINS_TITLE, TrialStrings.DRY_VEINS_RISK, TrialStrings.DRY_VEINS_REWARD, "close"
    ),
    HEAVY_CROWNS(
        TrialStrings.HEAVY_CROWNS_TITLE, TrialStrings.HEAVY_CROWNS_RISK, TrialStrings.HEAVY_CROWNS_REWARD, "general_power"
    ),
    THIN_BLOOD(
        TrialStrings.THIN_BLOOD_TITLE, TrialStrings.THIN_BLOOD_RISK, TrialStrings.THIN_BLOOD_REWARD, "strength"
    ),
    GLASS_ARROWS(
        TrialStrings.GLASS_ARROWS_TITLE, TrialStrings.GLASS_ARROWS_RISK, TrialStrings.GLASS_ARROWS_REWARD, "agility"
    ),
    IRON_TIDE(
        TrialStrings.IRON_TIDE_TITLE, TrialStrings.IRON_TIDE_RISK, TrialStrings.IRON_TIDE_REWARD, "wave"
    ),
    STONE_SKIN(
        TrialStrings.STONE_SKIN_TITLE, TrialStrings.STONE_SKIN_RISK, TrialStrings.STONE_SKIN_REWARD, "inventory"
    ),
    BOSS_BOUNTY(
        TrialStrings.BOSS_BOUNTY_TITLE, TrialStrings.BOSS_BOUNTY_RISK, TrialStrings.BOSS_BOUNTY_REWARD, "coin"
    ),
    MISERS_PACT(
        TrialStrings.MISERS_PACT_TITLE, TrialStrings.MISERS_PACT_RISK,
        TrialStrings.REWARD_COIN_INCOME_UP, "shop"
    ),
    FAMISHED_EARTH(
        TrialStrings.FAMISHED_EARTH_TITLE, TrialStrings.FAMISHED_EARTH_RISK, TrialStrings.FAMISHED_EARTH_REWARD, "dodge"
    ),
    BLOOD_PRICE(
        TrialStrings.BLOOD_PRICE_TITLE, TrialStrings.BLOOD_PRICE_RISK, TrialStrings.BLOOD_PRICE_REWARD, "lifesteal"
    ),
    HOLLOW_CALLING(
        TrialStrings.HOLLOW_CALLING_TITLE, TrialStrings.HOLLOW_CALLING_RISK, TrialStrings.HOLLOW_CALLING_REWARD, "health"
    ),
    /**
     * The omen trial (roadmap R3.4). Wave modifiers live here rather than in every run on purpose: the untrialled
     * run is the run every balance gate measures, and its numbers must not move because a feature was added.
     * A player who wants the wood to answer drafts it, and the trial's own band is measured like the other twelve.
     */
    HOLLOW_OMENS(
        TrialStrings.HOLLOW_OMENS_TITLE, TrialStrings.HOLLOW_OMENS_RISK, TrialStrings.HOLLOW_OMENS_REWARD, "luck"
    );

    private final TrialStrings title;
    private final TrialStrings risk;
    private final TrialStrings reward;
    private final String iconKey;

    TrialId(TrialStrings title, TrialStrings risk, TrialStrings reward, String iconKey) {
        this.title = title;
        this.risk = risk;
        this.reward = reward;
        this.iconKey = iconKey;
    }

    /** The trial's name in the language in force. */
    public String title() {
        return GameLocale.text(title);
    }

    /** The red line on the draft card, in the language in force. */
    public String risk() {
        return GameLocale.text(risk);
    }

    /** The green line on the draft card, in the language in force. */
    public String reward() {
        return GameLocale.text(reward);
    }

    public String iconKey() {
        return iconKey;
    }

    /** How this trial unlocks; blank for the ten trials open from the first run. */
    public String lockHint() {
        return switch (this) {
            case HEAVY_CROWNS -> GameLocale.text(TrialStrings.LOCK_HEAVY_CROWNS);
            case BOSS_BOUNTY -> GameLocale.text(TrialStrings.LOCK_BOSS_BOUNTY);
            default -> "";
        };
    }

    /** Null-safe lookup; unknown or corrupt names resolve to null instead of throwing. */
    public static TrialId forName(String name) {
        if (name == null) {
            return null;
        }
        for (TrialId trial : values()) {
            if (trial.name().equals(name)) {
                return trial;
            }
        }
        return null;
    }
}
