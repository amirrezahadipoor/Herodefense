package com.amirrezahadipoor.herodefense.accessibility;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.audio.NarrationSystem;
import org.junit.jupiter.api.Test;

final class ScreenReaderSupportTest {

    @Test
    void labelsCoverAllInteractiveElements() {
        assertTrue(AccessibilityLabels.labelCount() >= 20, "Need at least 20 labels for G3d");
        String[] requiredKeys = {
            "pause", "inventory", "shop", "ultimate", "speed",
            "equip", "sell", "forge", "close_inventory",
            "close_codex", "lore_tab", "trophies_tab",
            "sound_toggle", "music_toggle", "narration_toggle",
            "reduced_motion", "colour_blind", "text_size",
            "new_run", "continue_run", "settings", "close"
        };
        for (String key : requiredKeys) {
            assertTrue(AccessibilityLabels.hasLabel(key), "Missing label: " + key);
            String label = AccessibilityLabels.labelFor(key);
            assertFalse(label.isEmpty(), "Empty label for " + key);
            if (!key.startsWith("boss")) {
                assertTrue(label.toLowerCase().contains("double tap") || label.length() > 5,
                    "Label should be actionable: " + key + " -> " + label);
            }
        }
    }

    @Test
    void screenReaderSystemUsesTts() {
        NarrationSystem tts = new NarrationSystem();
        ScreenReaderSystem sr = new ScreenReaderSystem();
        sr.setTts(tts);
        sr.setEnabled(true);
        sr.announce("inventory");
        assertTrue(tts.history().size() >= 1, "Screen reader should queue via TTS");
        String last = tts.history().get(tts.history().size() - 1).text;
        assertTrue(last.contains("gear") || last.contains("backpack") || last.toLowerCase().contains("inventory"),
            "Should announce inventory: " + last);
    }

    @Test
    void screenReaderRespectsEnabledToggle() {
        NarrationSystem tts = new NarrationSystem();
        ScreenReaderSystem sr = new ScreenReaderSystem();
        sr.setTts(tts);
        sr.setEnabled(false);
        sr.announce("pause");
        assertTrue(tts.history().isEmpty(), "Disabled screen reader should not queue");
        sr.setEnabled(true);
        sr.announce("pause");
        assertFalse(tts.history().isEmpty());
    }

    @Test
    void gameSettingsHasScreenReaderToggle() throws Exception {
        String source = java.nio.file.Files.readString(java.nio.file.Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/settings/GameSettings.java"
        ));
        assertTrue(source.contains("screenReaderEnabled"), "GameSettings must have screenReaderEnabled");
        String touchLayout = java.nio.file.Files.readString(java.nio.file.Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/input/SettingsTouchLayout.java"
        ));
        assertTrue(touchLayout.contains("TOGGLE_SCREEN_READER"), "Settings must have screen reader toggle");
    }

    @Test
    void gameAudioManagerOwnsScreenReader() throws Exception {
        String source = java.nio.file.Files.readString(java.nio.file.Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/audio/GameAudioManager.java"
        ));
        assertTrue(source.contains("ScreenReaderSystem"), "GameAudioManager must own ScreenReaderSystem");
        assertTrue(source.contains("screenReader"), "Must have screenReader field");
    }
}
