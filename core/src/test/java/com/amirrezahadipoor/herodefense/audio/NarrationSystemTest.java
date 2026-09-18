package com.amirrezahadipoor.herodefense.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.story.BossTitleNarration;
import com.amirrezahadipoor.herodefense.story.LoreCatalog;
import com.amirrezahadipoor.herodefense.story.LoreEntry;
import com.amirrezahadipoor.herodefense.story.LoreNarration;
import org.junit.jupiter.api.Test;

/**
 * F3: No voice or narration for lore entries and boss title cards.
 */
final class NarrationSystemTest {

    @Test
    void narrationSystemQueuesAndHistories() {
        NarrationSystem system = new NarrationSystem();
        assertFalse(system.hasProvider(), "No provider by default");

        NarrationRequest lore = NarrationRequest.lore("lore_01", "The First Tree", "It stood before time.");
        system.narrate(lore);
        assertEquals(1, system.history().size());
        assertEquals("lore_01", system.history().get(0).id);

        NarrationRequest boss = NarrationRequest.bossTitle("ANCIENT_GOLEM", "Ancient Golem awakens");
        system.narrate(boss);
        assertEquals(2, system.history().size());
        assertEquals(NarrationRequest.Type.BOSS_TITLE, system.history().get(1).type);
    }

    @Test
    void loreNarrationCoversAllEntries() {
        int count = LoreNarration.narratableCount();
        assertTrue(count >= 31, "Should have at least 31 lore entries, got " + count);
        for (LoreEntry entry : LoreCatalog.all()) {
            NarrationRequest req = LoreNarration.forEntry(entry);
            assertNotNull(req, "Lore entry " + entry.id() + " should have narration");
            assertFalse(req.text.isEmpty(), "Narration text empty for " + entry.id());
            assertTrue(req.text.length() <= 400, "Narration too long for TTS: " + entry.id());
        }
    }

    @Test
    void bossTitleNarrationCoversEightBosses() {
        assertTrue(BossTitleNarration.titleCardCount() >= 8, "Need at least 8 boss title cards");
        for (String bossType : BossTitleNarration.allTitleCards().keySet()) {
            NarrationRequest req = BossTitleNarration.forBoss(bossType);
            assertNotNull(req);
            assertFalse(req.text.isEmpty());
            assertEquals(NarrationRequest.Type.BOSS_TITLE, req.type);
        }
        // Unknown boss still gets fallback
        NarrationRequest fallback = BossTitleNarration.forBoss("UNKNOWN_BOSS");
        assertNotNull(fallback);
        assertTrue(fallback.text.contains("UNKNOWN BOSS") || fallback.text.contains("approaches"));
    }

    @Test
    void narrationCanBeDisabled() {
        NarrationSystem system = new NarrationSystem();
        system.setEnabled(false);
        system.narrate(NarrationRequest.lore("id", "title", "body"));
        assertEquals(0, system.history().size(), "Disabled system should not queue");
        system.setEnabled(true);
        system.narrate(NarrationRequest.lore("id", "title", "body"));
        assertEquals(1, system.history().size());
    }

    @Test
    void gameSettingsHasNarrationToggle() throws Exception {
        String source = java.nio.file.Files.readString(java.nio.file.Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/settings/GameSettings.java"
        ));
        assertTrue(source.contains("narrationEnabled"), "GameSettings must have narrationEnabled");
        assertTrue(source.contains("narrationVolume"), "GameSettings must have narrationVolume");
        assertTrue(source.contains("cycleNarrationVolume"), "Must have cycle method");
    }
}
