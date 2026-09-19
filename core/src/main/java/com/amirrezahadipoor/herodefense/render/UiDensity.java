package com.amirrezahadipoor.herodefense.render;

/**
 * E4: The interface is text-heavy for a phone at arm's length.
 * This class owns the compact, icon-first UI rules that make the game readable at distance.
 *
 * Principles:
 * - No label longer than 20 chars in HUD, 30 chars in overlays
 * - Stats use icons + numbers, not words
 * - Common actions use icons with short verbs
 * - Visual hierarchy: title > value > metadata
 */
public final class UiDensity {
    private static final String DODGE = "dodge";

    /** Max chars for a HUD label (arm's length readability). */
    public static final int HUD_MAX_CHARS = 20;
    /** Max chars for an overlay row label. */
    public static final int OVERLAY_MAX_CHARS = 30;
    /** Max chars for a detail body line (already 60 in STORY_VOICE, but UI chrome should be shorter). */
    public static final int DETAIL_MAX_CHARS = 40;

    /** Icon-first stat abbreviations: HP, ATK, SPD, etc. */
    public static String abbrevStat(String statName) {
        if (statName == null) return "";
        return switch (statName.toLowerCase(java.util.Locale.ROOT)) {
            case "max_health", "health", "maxhealth" -> "HP";
            case "damage", "attack", "damage_per_attack" -> "ATK";
            case "attack_speed", "attacks_per_second", "attackspeed" -> "SPD";
            case DODGE, "dodge_chance" -> "DODGE";
            case "critical", "crit_chance", "crit" -> "CRIT";
            case "lifesteal" -> "LIFESTEAL";
            case "luck", "drop_chance" -> "LUCK";
            case "focus_gain", "focus" -> "FOCUS";
            default -> statName.length() > 6 ? statName.substring(0, 6).toUpperCase(java.util.Locale.ROOT) : statName.toUpperCase(java.util.Locale.ROOT);
        };
    }

    /** Short action verbs for arm's length: EQUIP -> EQ, SELL -> $, etc. */
    public static String shortAction(String action) {
        if (action == null) return "";
        return switch (action.toUpperCase(java.util.Locale.ROOT)) {
            case "EQUIP", "EQUIP_ITEM" -> "EQ";
            case "SELL", "SELL_ITEM" -> "$";
            case "FORGE", "ANVIL" -> "ANVIL";
            case "UNEQUIP" -> "UNEQ";
            case "AUTO-SELL" -> "A-SELL";
            default -> action.length() > 8 ? action.substring(0, 8) : action;
        };
    }

    /** Checks if a label is too long for its context. */
    public static boolean isTooLong(String label, int maxChars) {
        return label != null && label.length() > maxChars;
    }

    /** Truncates with ellipsis if too long, preserving readability. */
    public static String truncate(String label, int maxChars) {
        if (label == null) return "";
        if (label.length() <= maxChars) return label;
        if (maxChars <= 3) return label.substring(0, maxChars);
        return label.substring(0, maxChars - 3) + "...";
    }

    /** Icon name for a given stat — maps to UiIconRenderer keys. */
    public static String iconForStat(String statName) {
        if (statName == null) return "general_power";
        return switch (statName.toLowerCase(java.util.Locale.ROOT)) {
            case "max_health", "health" -> "health";
            case "damage" -> "agility"; // closest: attack
            case "attack_speed" -> DODGE; // speed
            case DODGE -> DODGE;
            case "critical" -> "agility";
            case "lifesteal" -> "lifesteal";
            case "luck" -> "luck";
            case "focus_gain" -> "general_power";
            default -> "general_power";
        };
    }

    private UiDensity() {}
}
