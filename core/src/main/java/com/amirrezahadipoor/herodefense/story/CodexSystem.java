package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.gameplay.ArenaLayout;
import com.amirrezahadipoor.herodefense.items.EquipmentSetBonus;
import com.amirrezahadipoor.herodefense.items.MythicEffects;
import com.amirrezahadipoor.herodefense.model.EliteAffix;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.Item;

import java.util.ArrayList;
import java.util.List;

/** Unlocks Grove Codex entries from run events. Every method is idempotent and null-safe. */
public final class CodexSystem {
    public static final int MASTERY_LEVEL = 10;
    public static final float LONG_PAUSE_SECONDS = 300f;
    public static final float FASTEST_FALL_SECONDS = 10f;
    public boolean isUnlocked(GameState state, String id) {
        if (state == null || state.codexUnlocked == null || id == null) return false;
        return Boolean.TRUE.equals(state.codexUnlocked.get(id));
    }

    public int unlockedCount(GameState state) {
        if (state == null || state.codexUnlocked == null) return 0;
        int count = 0;
        for (LoreEntry entry : LoreCatalog.all()) {
            if (isUnlocked(state, entry.id())) count++;
        }
        return count;
    }

    /** Unlocks one entry by id; returns true only on the first unlock. */
    public boolean unlock(GameState state, String id) {
        if (state == null || id == null || LoreCatalog.byId(id) == null) return false;
        if (state.codexUnlocked == null) return false;
        if (Boolean.TRUE.equals(state.codexUnlocked.get(id))) return false;
        state.codexUnlocked.put(id, true);
        return true;
    }

    /** Wave milestones 1–8: every entry whose wave was reached. */
    public List<String> unlockForWaveReached(GameState state) {
        List<String> unlocked = new ArrayList<>();
        if (state == null) return unlocked;
        for (LoreEntry entry : LoreCatalog.all()) {
            if (entry.trigger() != LoreTrigger.WAVE_MILESTONE) continue;
            if (state.waveNumber >= parseInt(entry.triggerParam(), Integer.MAX_VALUE)
                && unlock(state, entry.id())) {
                unlocked.add(entry.id());
            }
        }
        return unlocked;
    }

    /**
     * Field entries 44-47: the night's ground, the first time it is fought on.
     *
     * <p>A run is fought on one of the arena's four fields, so this is one entry per run and four across four runs
     * -- the codex is where a player finds out that the place they are standing in has a name, and that the other
     * three places exist. It is unlocked on the run's first wave rather than the fortieth, because the ground is
     * the one part of the night that is true before a single body walks in.
     */
    public List<String> unlockForField(GameState state, ArenaLayout layout) {
        List<String> unlocked = new ArrayList<>();
        if (state == null || layout == null) return unlocked;
        for (LoreEntry entry : LoreCatalog.all()) {
            if (entry.trigger() == LoreTrigger.FIELD_FIRST_NIGHT
                && layout.name().equals(entry.triggerParam())
                && unlock(state, entry.id())) {
                unlocked.add(entry.id());
            }
        }
        return unlocked;
    }

    /** Boss-first-kill entries 9–16 for the defeated identity. */
    public List<String> unlockForBossKill(GameState state, String bossType) {
        List<String> unlocked = new ArrayList<>();
        if (state == null || bossType == null) return unlocked;
        for (LoreEntry entry : LoreCatalog.all()) {
            if (entry.trigger() == LoreTrigger.BOSS_FIRST_KILL
                && bossType.equals(entry.triggerParam())
                && unlock(state, entry.id())) {
                unlocked.add(entry.id());
            }
        }
        return unlocked;
    }

    /** Elite-kill entries 17–19 for the defeated affix. */
    public List<String> unlockForEliteKill(GameState state, String affixId) {
        List<String> unlocked = new ArrayList<>();
        if (state == null || affixId == null) return unlocked;
        for (LoreEntry entry : LoreCatalog.all()) {
            if (entry.trigger() == LoreTrigger.ELITE_KILL
                && affixId.equals(entry.triggerParam())
                && unlock(state, entry.id())) {
                unlocked.add(entry.id());
            }
        }
        if (everyAffixKilled(state) && unlock(state, "codex_32")) {
            unlocked.add("codex_32");
        }
        return unlocked;
    }

    /** Ascension entries 20–24: every entry whose completed count was reached. */
    public List<String> unlockForAscension(GameState state) {
        List<String> unlocked = new ArrayList<>();
        if (state == null) return unlocked;
        for (LoreEntry entry : LoreCatalog.all()) {
            if (entry.trigger() != LoreTrigger.ASCENSION) continue;
            if (state.ascensionTier >= parseInt(entry.triggerParam(), Integer.MAX_VALUE)
                && unlock(state, entry.id())) {
                unlocked.add(entry.id());
            }
        }
        return unlocked;
    }

    /** Progress secrets: 25 Bare-Handed, 30 No Potions, 33 Fastest Fall, 34 Wave 200 twice. */
    public List<String> unlockSecretsForProgress(GameState state) {
        List<String> unlocked = new ArrayList<>();
        if (state == null) return unlocked;
        if (state.waveNumber >= 50 && state.shopStatsBoughtThisRun == 0 && unlock(state, "codex_25")) {
            unlocked.add("codex_25");
        }
        if (state.waveNumber >= GameState.PLANTING_WAVE + 1 && state.noPotionRun
            && unlock(state, "codex_30")) {
            unlocked.add("codex_30");
        }
        if (state.fastestWaveClearSeconds <= FASTEST_FALL_SECONDS && unlock(state, "codex_33")) {
            unlocked.add("codex_33");
        }
        if (state.waveNumber >= GameState.FINAL_WAVE && state.wave200ReachedCount >= 2
            && unlock(state, "codex_34")) {
            unlocked.add("codex_34");
        }
        return unlocked;
    }

    /** Secret 26 A Full Set: all four pieces of any set equipped at once; 29 Six Mythics. */
    public List<String> unlockSecretsForEquipment(GameState state) {
        List<String> unlocked = new ArrayList<>();
        if (state == null) return unlocked;
        if (!EquipmentSetBonus.completedSets(state).isEmpty() && unlock(state, "codex_26")) {
            unlocked.add("codex_26");
        }
        if (MythicEffects.ownsAllSix(state) && unlock(state, "codex_29")) {
            unlocked.add("codex_29");
        }
        return unlocked;
    }

    /** Secret 27 Mastery: any skill first reaches level 10. */
    public List<String> unlockSecretsForSkillPurchase(GameState state) {
        List<String> unlocked = new ArrayList<>();
        if (state == null || state.skillLevels == null) return unlocked;
        for (Integer level : state.skillLevels.values()) {
            if (level != null && level >= MASTERY_LEVEL && unlock(state, "codex_27")) {
                unlocked.add("codex_27");
                break;
            }
        }
        return unlocked;
    }

    /** Secret 28 Reforged: any item first reaches +5, in inventory or equipped. */
    public List<String> unlockSecretsForForge(GameState state) {
        List<String> unlocked = new ArrayList<>();
        if (state == null) return unlocked;
        if (anyMaxForged(state) && unlock(state, "codex_28")) unlocked.add("codex_28");
        return unlocked;
    }

    /** Secret 31 The Long Pause: a 300+ second pause was resumed. */
    public List<String> unlockSecretsForPause(GameState state) {
        List<String> unlocked = new ArrayList<>();
        if (state == null) return unlocked;
        if (state.longestPauseSeconds >= LONG_PAUSE_SECONDS && unlock(state, "codex_31")) {
            unlocked.add("codex_31");
        }
        return unlocked;
    }

    /** Secret 32 Every Elite: all three affixes killed at least once. */
    /** Secret 32 \"Every Elite, Once\" needs one kill of every affix the pool can draw, not just the
     *  three that shipped first: D2 grew the family to six (hollowmolt, gravemoss, cinderhalo),
     *  and a secret that claims \"every\" must count them too. The set is read from {@link EliteAffix}
     *  rather than from a copy of the first three ids, so a future affix cannot leave this behind again. */
    private static boolean everyAffixKilled(GameState state) {
        if (state.eliteKillCounts == null) return false;
        for (EliteAffix affix : EliteAffix.values()) {
            if (state.eliteKillCounts.getOrDefault(affix.id(), 0) < 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean anyMaxForged(GameState state) {
        if (state.inventory != null) {
            for (Item item : state.inventory) {
                if (item != null && item.upgradeLevel >= GameState.MAX_ITEM_UPGRADE) return true;
            }
        }
        if (state.equippedItems != null) {
            for (Item item : state.equippedItems.values()) {
                if (item != null && item.upgradeLevel >= GameState.MAX_ITEM_UPGRADE) return true;
            }
        }
        return false;
    }

    private static int parseInt(String value, int fallback) {
        try {
            if (value == null) return fallback;
            return Integer.parseInt(value);
        } catch (NumberFormatException bad) {
            return fallback;
        }
    }
}
