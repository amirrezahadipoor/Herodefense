package com.amirrezahadipoor.herodefense.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.input.SettingsTouchController;
import com.amirrezahadipoor.herodefense.input.SettingsTouchLayout;
import com.amirrezahadipoor.herodefense.render.VisualRarity;
import com.badlogic.gdx.Preferences;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Roadmap G3c: colour-blind accessible rarity colours, from settings persistence to centralized palette.
 */
final class ColourBlindRaritySettingsTest {

    private static final Path MAIN = Path.of("src", "main", "java", "com", "amirrezahadipoor", "herodefense");

    @Test
    void defaultSettingsKeepStandardPalette() {
        GameSettings settings = new GameSettings();
        assertFalse(settings.colourBlindRarity, "default install starts with standard rarity palette");
    }

    @Test
    void scrollingToRowSixTogglesColourBlindRarity() {
        GameSettings settings = new GameSettings();
        SettingsTouchController controller = new SettingsTouchController();
        float cx = SettingsTouchLayout.ROW_X + SettingsTouchLayout.ROW_WIDTH * 0.5f;

        // Scrolled down by 1 row so row 6 appears in slot 5
        controller.drag(60f);
        assertEquals(1, controller.firstVisibleIndex());

        float slot5Y = SettingsTouchLayout.slotY(5) + 40f;
        assertEquals(SettingsTouchLayout.Action.TOGGLE_COLOUR_BLIND_RARITY, controller.tap(settings, cx, slot5Y));
        assertTrue(settings.colourBlindRarity, "one tap toggles accessible rarity on");

        assertEquals(SettingsTouchLayout.Action.TOGGLE_COLOUR_BLIND_RARITY, controller.tap(settings, cx, slot5Y));
        assertFalse(settings.colourBlindRarity, "second tap toggles accessible rarity off");
    }

    @Test
    void preferenceSurvivesRelaunch() {
        MemoryPreferences preferences = new MemoryPreferences();
        LocalSettingsRepository repository = new LocalSettingsRepository(preferences);
        GameSettings settings = new GameSettings();
        settings.colourBlindRarity = true;
        repository.save(settings);

        assertTrue(repository.load().colourBlindRarity, "choice survives device relaunch");
        assertFalse(new LocalSettingsRepository(new MemoryPreferences()).load().colourBlindRarity,
            "device with default preferences stays false");
    }

    @Test
    void centralizedPaletteSwitchesColorsWhenAccessibleModeActive() {
        assertNotEquals(VisualRarity.UNCOMMON.color(false), VisualRarity.UNCOMMON.color(true));
        assertNotEquals(VisualRarity.RARE.color(false), VisualRarity.RARE.color(true));
        assertNotEquals(VisualRarity.LEGENDARY.color(false), VisualRarity.LEGENDARY.color(true));
        assertNotEquals(VisualRarity.MYTHIC.color(false), VisualRarity.MYTHIC.color(true));

        assertEquals(VisualRarity.DEFAULT_COMMON, VisualRarity.colorForTier("COMMON", false));
        assertEquals(VisualRarity.ACCESSIBLE_COMMON, VisualRarity.colorForTier("COMMON", true));
        assertEquals(VisualRarity.DEFAULT_UNCOMMON, VisualRarity.colorForTier("UNCOMMON", false));
        assertEquals(VisualRarity.ACCESSIBLE_UNCOMMON, VisualRarity.colorForTier("UNCOMMON", true));
        assertEquals(VisualRarity.DEFAULT_RARE, VisualRarity.colorForTier("RARE", false));
        assertEquals(VisualRarity.ACCESSIBLE_RARE, VisualRarity.colorForTier("RARE", true));
        assertEquals(VisualRarity.DEFAULT_LEGENDARY, VisualRarity.colorForTier("LEGENDARY", false));
        assertEquals(VisualRarity.ACCESSIBLE_LEGENDARY, VisualRarity.colorForTier("LEGENDARY", true));
        assertEquals(VisualRarity.DEFAULT_MYTHIC, VisualRarity.colorForTier("MYTHIC", false));
        assertEquals(VisualRarity.ACCESSIBLE_MYTHIC, VisualRarity.colorForTier("MYTHIC", true));
    }

    @Test
    void inventoryOverlayConsultsCentralizedVisualRarity() throws IOException {
        String inventory = Files.readString(
            MAIN.resolve("render/InventoryOverlayRenderer.java"), StandardCharsets.UTF_8);
        assertTrue(inventory.contains("VisualRarity.colorForTier"),
            "InventoryOverlayRenderer must delegate rarity coloring to centralized VisualRarity");
        assertTrue(inventory.contains("settings.colourBlindRarity"),
            "InventoryOverlayRenderer must pass the colour-blind setting into its draw operations");
    }

    private static final class MemoryPreferences implements Preferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override public Preferences putBoolean(String key, boolean val) { values.put(key, val); return this; }
        @Override public Preferences putInteger(String key, int val) { values.put(key, val); return this; }
        @Override public Preferences putLong(String key, long val) { values.put(key, val); return this; }
        @Override public Preferences putFloat(String key, float val) { values.put(key, val); return this; }
        @Override public Preferences putString(String key, String val) { values.put(key, val); return this; }
        @Override public Preferences put(Map<String, ?> vals) { values.putAll(vals); return this; }
        @Override public boolean getBoolean(String key) { return (Boolean) values.getOrDefault(key, false); }
        @Override public int getInteger(String key) { return (Integer) values.getOrDefault(key, 0); }
        @Override public long getLong(String key) { return (Long) values.getOrDefault(key, 0L); }
        @Override public float getFloat(String key) { return (Float) values.getOrDefault(key, 0f); }
        @Override public String getString(String key) { return (String) values.get(key); }
        @Override public boolean getBoolean(String key, boolean def) { return (Boolean) values.getOrDefault(key, def); }
        @Override public int getInteger(String key, int def) { return (Integer) values.getOrDefault(key, def); }
        @Override public long getLong(String key, long def) { return (Long) values.getOrDefault(key, def); }
        @Override public float getFloat(String key, float def) { return (Float) values.getOrDefault(key, def); }
        @Override public String getString(String key, String def) { return (String) values.getOrDefault(key, def); }
        @Override public Map<String, ?> get() { return values; }
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public void clear() { values.clear(); }
        @Override public void remove(String key) { values.remove(key); }
        @Override public void flush() { }
    }
}
