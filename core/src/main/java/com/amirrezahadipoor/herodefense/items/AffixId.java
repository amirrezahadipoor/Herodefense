package com.amirrezahadipoor.herodefense.items;

/**
 * Random affixes rolled onto Rare and Legendary drops. Each affix is a single-lane bonus;
 * Common and Uncommon items stay affix-free so early loot stays simple. The first fifteen
 * lanes roll on any Rare; the five expansion lanes (Elite Damage onward) live on Legendaries
 * from wave {@value #EXPANSION_FROM_WAVE} onward -- drops and Anvil rerolls both -- and carry
 * heavier numbers to match. Stun and Chain affixes only
 * add chance while their skill is learned. Thorns reflects only melee swings that actually
 * land, and stacked Fortitude pieces never cut incoming damage by more than half.
 */
public enum AffixId {
    CRIT_CHANCE("+3% Critical Chance", 0.03f),
    CRIT_DAMAGE("+12% Critical Damage", 0.12f),
    LIFESTEAL("+2% Lifesteal", 0.02f),
    COINS_ON_KILL("+3 Coins per Kill", 3f),
    DAMAGE("+4% Damage", 0.04f),
    ATTACK_SPEED("+3% Attack Speed", 0.03f),
    MAX_HEALTH("+4% Max Health", 0.04f),
    DODGE("+2% Dodge Chance", 0.02f),
    EXPERIENCE("+6% Experience", 0.06f),
    ITEM_FIND("+12% Item Find", 0.12f),
    POTION_POWER("+20% Potion Healing", 0.20f),
    STUN_CHANCE("+3% Stun Chance", 0.03f),
    CHAIN_CHANCE("+4% Chain Chance", 0.04f),
    MULTISHOT("+0.2 Extra Arrows", 0.20f),
    BOSS_DAMAGE("+8% Boss Damage", 0.08f),
    ELITE_DAMAGE("+10% Elite Damage", 0.10f),
    THORNS("Reflect 20% of Melee Damage", 0.20f),
    POTION_FIND("+15% Potion Find", 0.15f),
    FOCUS_GAIN("+12% Focus Gain", 0.12f),
    FORTITUDE("-6% Damage Taken", 0.06f);

    /** Lanes every Rare drop can roll: the original fifteen, in enum order. */
    public static final int BASE_POOL_SIZE = 15;

    /** Wave the expansion lanes join the Legendary pool: the second half of the run. */
    public static final int EXPANSION_FROM_WAVE = 101;

    private static final AffixId[] BASE_POOL = java.util.Arrays.copyOf(values(), BASE_POOL_SIZE);

    /** The Rare pool; the B2a expansion lanes live on Legendaries alone. */
    public static AffixId[] basePool() {
        return BASE_POOL.clone();
    }

    private final String display;
    private final float value;

    AffixId(String display, float value) {
        this.display = display;
        this.value = value;
    }

    public String display() {
        return display;
    }

    public float value() {
        return value;
    }

    /** Null-safe lookup; unknown or corrupt ids resolve to null instead of throwing. */
    public static AffixId forName(String name) {
        if (name == null) {
            return null;
        }
        for (AffixId affix : values()) {
            if (affix.name().equals(name)) {
                return affix;
            }
        }
        return null;
    }
}
