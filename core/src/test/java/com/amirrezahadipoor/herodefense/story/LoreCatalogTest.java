package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.amirrezahadipoor.herodefense.gameplay.ArenaLayout;
import com.amirrezahadipoor.herodefense.model.EliteAffix;
import org.junit.jupiter.api.Test;

final class LoreCatalogTest {
    @Test
    void definesEveryEntrySequentiallyNumberedAndUnique() {
        assertEquals(48, LoreCatalog.all().size());
        Set<String> ids = new HashSet<>();
        int expected = 1;
        for (LoreEntry entry : LoreCatalog.all()) {
            assertEquals(expected, entry.number());
            assertEquals(String.format("codex_%02d", expected), entry.id());
            ids.add(entry.id());
            assertFalse(entry.title().isBlank());
            assertFalse(entry.body().isBlank());
            assertNotNull(entry.trigger());
            assertFalse(entry.triggerParam().isBlank());
            expected++;
        }
        assertEquals(48, ids.size());
        assertNotNull(LoreCatalog.byId("codex_01"));
        assertNotNull(LoreCatalog.byId("codex_47"));
    }

    @Test
    void triggerGroupsMatchTheRoadmapSplit() {
        Map<LoreTrigger, Integer> counts = new EnumMap<>(LoreTrigger.class);
        for (LoreEntry entry : LoreCatalog.all()) {
            counts.put(entry.trigger(), counts.getOrDefault(entry.trigger(), 0) + 1);
        }
        assertEquals(8, counts.get(LoreTrigger.WAVE_MILESTONE));
        assertEquals(8, counts.get(LoreTrigger.BOSS_FIRST_KILL));
        assertEquals(EliteAffix.values().length, counts.get(LoreTrigger.ELITE_KILL));
        assertEquals(5, counts.get(LoreTrigger.ASCENSION));
        assertEquals(10, counts.get(LoreTrigger.SECRET));
        assertEquals(ArenaLayout.values().length, counts.get(LoreTrigger.FIELD_FIRST_NIGHT));
        assertEquals(1, counts.get(LoreTrigger.SET_HELD), "one entry for the timed shield, and it is a secret of its own");
    }

    @Test
    void waveBossEliteAndAscensionParamsAreExact() {
        assertEquals("1", LoreCatalog.byId("codex_01").triggerParam());
        assertEquals("100", LoreCatalog.byId("codex_08").triggerParam());
        assertEquals("ANCIENT_GOLEM", LoreCatalog.byId("codex_09").triggerParam());
        assertEquals("VOID_KNIGHT", LoreCatalog.byId("codex_12").triggerParam());
        assertEquals("FROST_TITAN", LoreCatalog.byId("codex_13").triggerParam());
        assertEquals("BLOODROOT_AVATAR", LoreCatalog.byId("codex_16").triggerParam());
        assertEquals("blightburst", LoreCatalog.byId("codex_17").triggerParam());
        assertEquals("rootward_ward", LoreCatalog.byId("codex_18").triggerParam());
        assertEquals("weeping_rot", LoreCatalog.byId("codex_19").triggerParam());
        assertEquals("10", LoreCatalog.byId("codex_24").triggerParam());
        assertEquals("OPEN_HEARTH", LoreCatalog.byId("codex_44").triggerParam());
        assertEquals("STANDING_STONES", LoreCatalog.byId("codex_45").triggerParam());
        assertEquals("THORNHEDGE", LoreCatalog.byId("codex_46").triggerParam());
        assertEquals("RUINED_RING", LoreCatalog.byId("codex_47").triggerParam());
    }

    @Test
    void bodiesMatchTheStoryDocumentVerbatim() {
        assertEquals(
            "So. You are the new Chief. Pip picked you, and Pip is never wrong about hearts. Stay close to my light, dearie.",
            LoreCatalog.byId("codex_01").body());
        assertEquals(
            "No song is ever written about this part. Not the falling, not the standing. Just the holding. Hold anyway, dearie.",
            LoreCatalog.byId("codex_07").body());
        assertEquals(
            "Fire is supposed to go out. This one said no, learned to pose, and hired no one. The Night's hottest star. His words, not mine.",
            LoreCatalog.byId("codex_11").body());
        assertEquals(
            "The first time was survival. The second time? Say it to yourself, dearie. Then come have soup. You earned it twice.",
            LoreCatalog.byId("codex_34").body());
        for (LoreEntry entry : LoreCatalog.all()) {
            assertTrue(entry.body().length() >= 30, entry.id());
        }
    }
}
