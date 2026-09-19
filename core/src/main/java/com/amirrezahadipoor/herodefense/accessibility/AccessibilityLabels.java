package com.amirrezahadipoor.herodefense.accessibility;

import java.util.HashMap;
import java.util.Map;

/**
 * G3d: Screen-reader support.
 * Provides TalkBack-friendly labels for every interactive element.
 * Each label is a short, actionable sentence, not a visual description.
 */
public final class AccessibilityLabels {
    private static final Map<String, String> LABELS = new HashMap<>();
    static {
        // HUD
        LABELS.put("pause", "Pause game. Double tap to open menu.");
        LABELS.put("inventory", "Open gear and backpack. Double tap.");
        LABELS.put("shop", "Open stat shop. Double tap.");
        LABELS.put("ultimate", "Use ultimate ability when charged. Double tap.");
        LABELS.put("speed", "Toggle simulation speed. Double tap.");
        // Inventory
        LABELS.put("equip", "Equip selected item. Double tap.");
        LABELS.put("sell", "Sell selected item for coins. Double tap.");
        LABELS.put("forge", "Reforge selected item. Costs coins. Double tap.");
        LABELS.put("auto_sell_common", "Auto sell common items on pickup. Double tap to toggle.");
        LABELS.put("auto_sell_uncommon", "Auto sell uncommon items. Double tap to toggle.");
        LABELS.put("auto_sell_rare", "Auto sell rare items. Double tap to toggle.");
        LABELS.put("close_inventory", "Close inventory. Double tap.");
        // Codex
        LABELS.put("close_codex", "Close Grove Codex. Double tap.");
        LABELS.put("lore_tab", "Lore entries tab. Double tap.");
        LABELS.put("trophies_tab", "Trophies tab. Double tap.");
        // Settings
        LABELS.put("sound_toggle", "Sound effects toggle. Double tap.");
        LABELS.put("music_toggle", "Music toggle. Double tap.");
        LABELS.put("narration_toggle", "Narration for lore and boss titles. Double tap to toggle.");
        LABELS.put("reduced_motion", "Reduced motion. Removes shake and spores. Double tap.");
        LABELS.put("colour_blind", "Accessible rarity colours. Double tap.");
        LABELS.put("text_size", "Text size. Double tap to cycle small, normal, large.");
        // Main menu
        LABELS.put("new_run", "Start new run. Double tap.");
        LABELS.put("continue_run", "Continue saved run. Double tap.");
        LABELS.put("settings", "Open settings. Double tap.");
        LABELS.put("codex_menu", "Open Grove Codex. Double tap.");
        // Boss
        LABELS.put("boss_title", "Boss approaching. %s");
        // Generic
        LABELS.put("close", "Close. Double tap to go back.");
    }

    private AccessibilityLabels() {}

    public static String labelFor(String key) {
        return LABELS.getOrDefault(key, key.replace('_', ' '));
    }

    public static String bossTitleLabel(String bossName) {
        String template = LABELS.get("boss_title");
        return template != null ? String.format(template, bossName) : bossName;
    }

    public static Map<String, String> allLabels() {
        return new HashMap<>(LABELS);
    }

    public static int labelCount() {
        return LABELS.size();
    }

    /** Checks if a UI element has an accessibility label. */
    public static boolean hasLabel(String key) {
        return LABELS.containsKey(key);
    }
}
