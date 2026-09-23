package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class BossLoreTest {
    @Test
    void everyBossIdentityHasAVerbatimBio() {
        assertEquals(
            "Grum naps through meetings and sits on problems. His blow lands where you were, not where you are. Be somewhere else, dearie.",
            BossLore.bioFor("ANCIENT_GOLEM"));
        assertTrue(BossLore.bioFor("THORN_MATRIARCH").startsWith("Mama scolds first"));
        assertTrue(BossLore.bioFor("EMBER_WYRM").contains("his bad side"));
        assertTrue(BossLore.bioFor("VOID_KNIGHT").endsWith("politest heart I know."));
        assertNull(BossLore.bioFor("NOPE"));
        assertNull(BossLore.bioFor(null));
    }

    @Test
    void bossEntriesComposeTreeVoiceFirstAndBioSecond() {
        for (String id : new String[] {"codex_09", "codex_10", "codex_11", "codex_12", "codex_13", "codex_14", "codex_15", "codex_16"}) {
            LoreEntry entry = LoreCatalog.byId(id);
            String bio = BossLore.bioFor(entry.triggerParam());
            assertTrue(bio != null, id);
            // All eight bosses shipped with their own bio, so the two paragraphs are never identical;
            // the strict check here is the composition itself: body, blank line, bio.
            assertEquals(entry.body() + "\n\n" + bio, BossLore.detailFor(entry), id);
        }
    }

    @Test
    void otherEntriesRenderTheirBodyAlone() {
        for (LoreEntry entry : LoreCatalog.all()) {
            if (entry.trigger() == LoreTrigger.BOSS_FIRST_KILL) continue;
            assertEquals(entry.body(), BossLore.detailFor(entry));
            assertFalse(BossLore.detailFor(entry).contains("\n\n"));
        }
        assertEquals("", BossLore.detailFor(null));
    }
}
