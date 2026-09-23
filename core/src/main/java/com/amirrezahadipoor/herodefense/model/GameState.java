package com.amirrezahadipoor.herodefense.model;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.gameplay.HeroStatCalculator;
import com.amirrezahadipoor.herodefense.progression.TrophyLedger;
import com.amirrezahadipoor.herodefense.trials.TrialDraftSystem;
import com.amirrezahadipoor.herodefense.trials.TrialEffects;
import com.amirrezahadipoor.herodefense.trials.TrialId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Complete serializable state for one continuous Hero Defense run. */
public final class GameState {
    public static final int CURRENT_SCHEMA_VERSION = 2;
    public static final int FINAL_WAVE = 200;
    /** Clearing this wave (and its boss reward) triggers the planting ceremony. */
    public static final int PLANTING_WAVE = 100;
    /** Mirrors {@code HeroProgressionSystem.LEVEL_CAP}; kept here so save repair has no gameplay dependency. */
    public static final int MAX_HERO_LEVEL = 200;
    /** Mirrors {@code ItemForgeSystem.MAX_UPGRADE}. */
    public static final int MAX_ITEM_UPGRADE = 5;
    /** Seconds the monsters spend tearing down the trees after the Hero falls. */
    public static final float TREE_SIEGE_SECONDS = 3.2f;
    public static final float ARENA_CENTER_X = WorldLayout.HERO_CENTER_X;
    public static final float ARENA_CENTER_Y = WorldLayout.HERO_CENTER_Y;

    public int schemaVersion = CURRENT_SCHEMA_VERSION;
    public long runSeed;
    /** Persisted xorshift state keeps combat rolls deterministic across save/load. */
    public long combatRandomState;
    /** Independent xorshift stream for affix rolls, so loot identity never perturbs combat. */
    public long affixRandomState;
    /** Which run the player started (roadmap R3.5); a brief run ends at 30 waves. */
    public GameMode mode = GameMode.STANDARD;

    /** The hero path the run walks (roadmap B3); {@code null} until the pre-run draft binds one. */
    public String heroPath;

    public int waveNumber = 1;
    public int coins;
    public int heroLevel = 1;
    public int heroExperience;
    public int unspentTalentPoints;
    public int defeatedBosses;
    public int totalKills;
    public int totalKillCoinsEarned;
    /**
     * Consecutive kills whose drop roll came up empty (roadmap B1 / R4.3's pity rule). ItemDropSystem counts it,
     * answers at the threshold with a guaranteed common, and any natural drop resets it. Encoded like every run
     * counter, so a resumed run resumes its streak.
     */
    public int dryKillsSinceItemDrop;
    public float worldTreeHealth = 1000f;
    public float worldTreeMaxHealth = 1000f;
    public float simulationSpeed = 1f;
    public boolean waveActive;
    public boolean starterLoadoutGranted;
    public boolean awaitingBossReward;
    public int pendingRewardBossNumber;
    public boolean runComplete;
    /** Set when Wave 100 is cleared; cleared once the ceremony has played (or been skipped). */
    public boolean ceremonyPending;
    /**
     * Set when a boss wave's advance defers for its watch-only intro; cleared when the intro
     * hands the run to the fight. A reload replays the intro from its first frame.
     */
    public boolean bossIntroPending;
    /** The boss wave the pending intro plays for; wave/5 is the encounter, meeting per §3.4. */
    public int bossIntroWave;
    /** A milestone breather waits to play before the next wave spawns. */
    public boolean breatherPending;
    /** The cleared milestone wave the pending breather breathes for (25/75/125/175). */
    public int breatherWave;
    /** Planned bodies of the current regular wave; the trickle gates count the living against it. */
    public int wavePlannedEnemies;
    /** Trickle pulse already walking in (1-based); 0 on boss waves and before the first spawn. */
    public int tricklePulse;
    /** Escort pulses of this boss wave already walking in (1-based); 0 before the first. */
    public int escortWave;
    /** True once the second Heartwood stands; it is a monument, never a second loss condition. @deprecated use plantedTreesCount */
    public boolean secondTreePlanted;
    /** Number of additional Heartwoods planted beyond the original (0..3 for waves 50/100/150). */
    public int plantedTreesCount;
    /** Health of each planted tree (size == plantedTreesCount, each entry 0..plantedTreeMaxHealth). */
    public List<Float> plantedTreeHealth = new ArrayList<>();
    /** Max health for each planted tree (mirrors worldTreeMaxHealth at planting time, per-tree). */
    public List<Float> plantedTreeMaxHealth = new ArrayList<>();
    /** Counts down after the Hero dies while monsters destroy the trees; 0 = trees are gone. */
    public float treeSiegeRemainingSeconds;
    public long nextEntityId = 2L;

    // --- Ascension (Phase 20) ---
    public int ascensionTier;
    public int heartwood;
    public int peakWaveReached = 1;
    public boolean heroDiedThisRun;
    public int potionsUsedThisRun;
    public float fastestWaveClearSeconds = Float.MAX_VALUE;
    public float longestPauseSeconds;
    public int shopStatsBoughtThisRun;
    public float focus;
    public float focusMax = 100f;

    // Meta-progression
    public Map<String, Boolean> rootNodesPurchased = new LinkedHashMap<>();
    public Map<String, Boolean> codexUnlocked = new LinkedHashMap<>();
    public Map<String, Integer> eliteKillCounts = new LinkedHashMap<>();
    public Map<String, Boolean> firstBossKills = new LinkedHashMap<>();
    /** Boss identities already title-carded; once ever, never reset (§2.1). */
    public Map<String, Boolean> firstBossEncounters = new LinkedHashMap<>();
    /** Idle-whisper ids already shown once; the pool never repeats (§8). */
    public Map<String, Boolean> usedWhisperIds = new LinkedHashMap<>();
    public Map<String, String> skillEvolutions = new LinkedHashMap<>();
    public List<String> activeTrials = new ArrayList<>();
    public Map<String, Boolean> trialUnlocked = new LinkedHashMap<>();
    /** The four drafted trial names currently offered; cleared once the draft completes. */
    public List<String> pendingTrialOffer = new ArrayList<>();
    /** Trial names picked so far in the open draft; cleared once the draft completes. */
    public List<String> trialDraftPicks = new ArrayList<>();

    // Run stats for secret codex entries
    public boolean bareHandedEligible = true;
    public boolean noPotionRun = true;
    /** What the trophies remember between runs (roadmap R3.3). A new run never clears it. */
    public TrophyLedger trophies = new TrophyLedger();

    public int totalRunsCompleted;
    public int totalAscensionsCompleted;
    /** Times any run has advanced into wave 200; never reset (feeds codex entry 30). */
    public int wave200ReachedCount;
    /** Ascension tier whose opening lines this run plays; -1 until the opening begins. */
    public int openingTier = -1;
    /** Epilogue id (A–E) recorded when the run ended; blank until then. */
    public String epilogueId = "";
    /** Simulated combat seconds in the current wave (feeds the Fastest Fall secret). */
    public float waveElapsedSeconds;

    public Hero hero = new Hero(1L, ARENA_CENTER_X, ARENA_CENTER_Y);
    public List<Enemy> aliveEnemies = new ArrayList<>();
    public List<Boss> aliveBosses = new ArrayList<>();
    public List<Projectile> projectiles = new ArrayList<>();
    public List<DropEntity> drops = new ArrayList<>();
    public List<RotTrailSegment> rotTrail = new ArrayList<>();
    public List<Item> inventory = new ArrayList<>();
    /** Ascension tiers whose guaranteed Wave-200 Mythic was already granted. */
    public List<Integer> mythicGrantTiers = new ArrayList<>();
    /** Catalog id of the Mythic granted by this run's Wave-200 clear, if any. */
    public String mythicGrantedItemId;
    public Map<String, Item> equippedItems = new LinkedHashMap<>();
    public Map<String, Float> permanentEffects = new LinkedHashMap<>();
    public Map<String, Integer> shopUpgradeLevels = new LinkedHashMap<>();
    /** Purchased skill levels keyed by {@code SkillId.saveKey()}; absent means level 0. */
    public Map<String, Integer> skillLevels = new LinkedHashMap<>();
    /** Boss number encoded as a string key for stable JSON object-key round trips. */
    public Map<String, String> chosenRewardCards = new LinkedHashMap<>();
    public List<String> pendingRewardCards = new ArrayList<>();
    public List<Integer> healthPotions = new ArrayList<>();

    public GameState() {
        ensurePotionSlots();
    }

    public static GameState newRun(long seed) {
        GameState state = new GameState();
        state.runSeed = seed;
        state.combatRandomState = initialRandomState(seed);
        state.affixRandomState = initialRandomState(seed ^ 0xAFF1CE2D192ED03L);
        state.validateAndRepair();
        return state;
    }

    public long allocateEntityId() {
        return nextEntityId++;
    }

    /** Returns a deterministic uniform combat roll in [0, 1) and advances saved state. */
    public float nextCombatRandomFloat() {
        long value = combatRandomState;
        if (value == 0L) {
            value = initialRandomState(runSeed);
        }
        value ^= value << 13;
        value ^= value >>> 7;
        value ^= value << 17;
        combatRandomState = value;
        return (value >>> 40) / 16_777_216f;
    }

    /** Returns a deterministic uniform affix roll in [0, 1) on the affix stream. */
    public float nextAffixRandomFloat() {
        long value = affixRandomState;
        if (value == 0L) {
            value = initialRandomState(runSeed ^ 0xAFF1CE2D192ED03L);
        }
        value ^= value << 13;
        value ^= value >>> 7;
        value ^= value << 17;
        affixRandomState = value;
        return (value >>> 40) / 16_777_216f;
    }

    public void destroyWorldTree() {
        worldTreeHealth = 0f;
        for (int i = 0; i < plantedTreeHealth.size(); i++) {
            plantedTreeHealth.set(i, 0f);
        }
    }

    /** Total trees standing (original + planted). */
    public int totalTrees() {
        return 1 + Math.max(0, plantedTreesCount);
    }

    /** Health of a tree by index: 0 = original World Tree, 1..n = planted. */
    public float getTreeHealth(int index) {
        if (index == 0) return worldTreeHealth;
        if (index > 0 && index <= plantedTreeHealth.size()) return plantedTreeHealth.get(index - 1);
        return 0f;
    }

    /** Max health of a tree by index. */
    public float getTreeMaxHealth(int index) {
        if (index == 0) return worldTreeMaxHealth;
        if (index > 0 && index <= plantedTreeMaxHealth.size()) return plantedTreeMaxHealth.get(index - 1);
        return worldTreeMaxHealth;
    }

    /** Sets health of a tree by index, clamped to its max. */
    public void setTreeHealth(int index, float health) {
        if (index == 0) {
            worldTreeHealth = Math.max(0f, Math.min(worldTreeMaxHealth, health));
        } else if (index > 0 && index <= plantedTreeHealth.size()) {
            float max = getTreeMaxHealth(index);
            plantedTreeHealth.set(index - 1, Math.max(0f, Math.min(max, health)));
        }
    }

    /** Wave numbers that trigger a planting (50/100/150). */
    public static boolean isGrovePlantingWave(int wave) {
        return wave == 50 || wave == 100 || wave == 150;
    }

    /** Grove index for a planting wave (0->50, 1->100, 2->150). */
    public static int groveIndexForPlantingWave(int wave) {
        return switch (wave) {
            case 50 -> 0;
            case 100 -> 1;
            case 150 -> 2;
            default -> -1;
        };
    }

    /** Whether this wave's ceremony is the short 3-beat (50/150) rather than full 5-beat (100). */
    public static boolean isShortPlantingWave(int wave) {
        return wave == 50 || wave == 150;
    }

    /** Puts the Hero back at the arena centre: ceremonies, save repair, and the simulator's rooted policy. */
    public void anchorHeroAtArenaCenter() {
        if (hero != null) {
            hero.keepAt(ARENA_CENTER_X, ARENA_CENTER_Y);
        }
    }

    public int livingEnemyCount() {
        int count = 0;
        for (Enemy enemy : aliveEnemies) {
            if (enemy != null && enemy.alive && !enemy.silentWatcher) {
                count++;
            }
        }
        for (Boss boss : aliveBosses) {
            if (boss != null && boss.alive) {
                count++;
            }
        }
        return count;
    }

    /** Repairs safe defaults after loading an older or partially written save. */
    /** The wave this run ends on, which is the mode's own length. */
    public int runLengthWaves() {
        return mode == null ? FINAL_WAVE : mode.waves();
    }

    public void validateAndRepair() {
        schemaVersion = CURRENT_SCHEMA_VERSION;
        if (mode == null) {
            mode = GameMode.STANDARD;
        }
        waveNumber = Math.max(1, Math.min(runLengthWaves(), waveNumber));
        heroLevel = Math.max(1, Math.min(MAX_HERO_LEVEL, heroLevel));
        coins = Math.max(0, coins);
        heroExperience = Math.max(0, heroExperience);
        unspentTalentPoints = Math.max(0, unspentTalentPoints);
        totalKills = Math.max(0, totalKills);
        totalKillCoinsEarned = Math.max(0, totalKillCoinsEarned);
        simulationSpeed = simulationSpeed == 2f || simulationSpeed == 3f ? simulationSpeed : 1f;
        if (combatRandomState == 0L) {
            combatRandomState = initialRandomState(runSeed);
        }
        if (affixRandomState == 0L) {
            affixRandomState = initialRandomState(runSeed ^ 0xAFF1CE2D192ED03L);
        }
        if (hero == null) {
            hero = new Hero(1L, ARENA_CENTER_X, ARENA_CENTER_Y);
        }
        hero.keepAt(ARENA_CENTER_X, ARENA_CENTER_Y);
        hero.validateAndRepair();
        worldTreeMaxHealth = Math.max(1f, worldTreeMaxHealth);
        worldTreeHealth = Math.max(0f, Math.min(worldTreeMaxHealth, worldTreeHealth));
        treeSiegeRemainingSeconds = Float.isFinite(treeSiegeRemainingSeconds)
            ? Math.max(0f, Math.min(TREE_SIEGE_SECONDS, treeSiegeRemainingSeconds)) : 0f;
        if (!hero.alive && treeSiegeRemainingSeconds <= 0f) {
            destroyWorldTree();
        }
        if (hero.alive) treeSiegeRemainingSeconds = 0f;
        // --- Grove 32.1: planted-trees count with per-tree HP ---
        if (plantedTreeHealth == null) plantedTreeHealth = new ArrayList<>();
        if (plantedTreeMaxHealth == null) plantedTreeMaxHealth = new ArrayList<>();
        // Migrate old boolean save
        if (plantedTreesCount == 0 && secondTreePlanted) {
            plantedTreesCount = 1;
            if (plantedTreeHealth.isEmpty()) plantedTreeHealth.add(worldTreeMaxHealth);
            if (plantedTreeMaxHealth.isEmpty()) plantedTreeMaxHealth.add(worldTreeMaxHealth);
        }
        // Clamp count to list sizes and to 0..3
        plantedTreesCount = Math.max(0, Math.min(3, plantedTreesCount));
        while (plantedTreeHealth.size() < plantedTreesCount) plantedTreeHealth.add(worldTreeMaxHealth);
        while (plantedTreeHealth.size() > plantedTreesCount) plantedTreeHealth.remove(plantedTreeHealth.size() - 1);
        while (plantedTreeMaxHealth.size() < plantedTreesCount) plantedTreeMaxHealth.add(worldTreeMaxHealth);
        while (plantedTreeMaxHealth.size() > plantedTreesCount) plantedTreeMaxHealth.remove(plantedTreeMaxHealth.size() - 1);
        for (int i = 0; i < plantedTreesCount; i++) {
            float max = Math.max(1f, plantedTreeMaxHealth.get(i));
            plantedTreeMaxHealth.set(i, max);
            float cur = plantedTreeHealth.get(i);
            plantedTreeHealth.set(i, Math.max(0f, Math.min(max, cur)));
        }
        // Grove ceremony bookkeeping for 50/100/150
        if (waveNumber <= 50) {
            ceremonyPending = false;
            plantedTreesCount = 0;
            plantedTreeHealth.clear();
            plantedTreeMaxHealth.clear();
        } else if (waveNumber <= 100) {
            if (ceremonyPending) {
                // pending for 50, count stays 0 until ceremony completes
                plantedTreesCount = 0;
                plantedTreeHealth.clear();
                plantedTreeMaxHealth.clear();
            } else {
                if (plantedTreesCount < 1) {
                    plantedTreesCount = 1;
                    while (plantedTreeHealth.size() < plantedTreesCount) plantedTreeHealth.add(worldTreeMaxHealth);
                    while (plantedTreeMaxHealth.size() < plantedTreesCount) plantedTreeMaxHealth.add(worldTreeMaxHealth);
                } else if (plantedTreesCount > 1) {
                    plantedTreesCount = 1;
                    while (plantedTreeHealth.size() > 1) plantedTreeHealth.remove(plantedTreeHealth.size() - 1);
                    while (plantedTreeMaxHealth.size() > 1) plantedTreeMaxHealth.remove(plantedTreeMaxHealth.size() - 1);
                }
            }
        } else if (waveNumber <= 150) {
            if (ceremonyPending) {
                plantedTreesCount = 1;
                while (plantedTreeHealth.size() > 1) plantedTreeHealth.remove(plantedTreeHealth.size() - 1);
                while (plantedTreeMaxHealth.size() > 1) plantedTreeMaxHealth.remove(plantedTreeMaxHealth.size() - 1);
                while (plantedTreeHealth.isEmpty()) plantedTreeHealth.add(worldTreeMaxHealth);
                while (plantedTreeMaxHealth.isEmpty()) plantedTreeMaxHealth.add(worldTreeMaxHealth);
            } else {
                if (plantedTreesCount < 2) {
                    plantedTreesCount = 2;
                    while (plantedTreeHealth.size() < plantedTreesCount) plantedTreeHealth.add(worldTreeMaxHealth);
                    while (plantedTreeMaxHealth.size() < plantedTreesCount) plantedTreeMaxHealth.add(worldTreeMaxHealth);
                } else if (plantedTreesCount > 2) {
                    plantedTreesCount = 2;
                    while (plantedTreeHealth.size() > 2) plantedTreeHealth.remove(plantedTreeHealth.size() - 1);
                    while (plantedTreeMaxHealth.size() > 2) plantedTreeMaxHealth.remove(plantedTreeMaxHealth.size() - 1);
                }
            }
        } else {
            if (ceremonyPending) {
                plantedTreesCount = 2;
                while (plantedTreeHealth.size() > 2) plantedTreeHealth.remove(plantedTreeHealth.size() - 1);
                while (plantedTreeMaxHealth.size() > 2) plantedTreeMaxHealth.remove(plantedTreeMaxHealth.size() - 1);
                while (plantedTreeHealth.size() < 2) plantedTreeHealth.add(worldTreeMaxHealth);
                while (plantedTreeMaxHealth.size() < 2) plantedTreeMaxHealth.add(worldTreeMaxHealth);
            } else {
                if (plantedTreesCount < 3) {
                    plantedTreesCount = 3;
                    while (plantedTreeHealth.size() < plantedTreesCount) plantedTreeHealth.add(worldTreeMaxHealth);
                    while (plantedTreeMaxHealth.size() < plantedTreesCount) plantedTreeMaxHealth.add(worldTreeMaxHealth);
                } else if (plantedTreesCount > 3) {
                    plantedTreesCount = 3;
                    while (plantedTreeHealth.size() > 3) plantedTreeHealth.remove(plantedTreeHealth.size() - 1);
                    while (plantedTreeMaxHealth.size() > 3) plantedTreeMaxHealth.remove(plantedTreeMaxHealth.size() - 1);
                }
            }
        }
        secondTreePlanted = plantedTreesCount > 0;
        if (ceremonyPending) waveActive = false;
        if (aliveEnemies == null) aliveEnemies = new ArrayList<>();
        if (aliveBosses == null) aliveBosses = new ArrayList<>();
        if (livingEnemyCount() > 0) waveActive = true;
        // The intro's prop is a living body in the save, but the wave has not started: the intro
        // replays and the prop respawns fresh, so the flag wins over the body count.
        if (bossIntroPending) waveActive = false;
        if (breatherPending) waveActive = false;
        if (runComplete) waveActive = false;
        if (projectiles == null) projectiles = new ArrayList<>();
        if (drops == null) drops = new ArrayList<>();
        if (inventory == null) inventory = new ArrayList<>();
        if (mythicGrantTiers == null) mythicGrantTiers = new ArrayList<>();
        if (equippedItems == null) equippedItems = new LinkedHashMap<>();
        inventory.removeIf(item -> item == null);
        equippedItems.values().removeIf(item -> item == null);
        for (Item item : inventory) item.upgradeLevel = Math.max(0, Math.min(MAX_ITEM_UPGRADE, item.upgradeLevel));
        for (Item item : equippedItems.values()) item.upgradeLevel = Math.max(0, Math.min(MAX_ITEM_UPGRADE, item.upgradeLevel));
        synchronizeEquipmentHealth();
        if (permanentEffects == null) permanentEffects = new LinkedHashMap<>();
        if (shopUpgradeLevels == null) shopUpgradeLevels = new LinkedHashMap<>();
        shopUpgradeLevels.replaceAll((key, value) -> value == null ? 0 : Math.max(0, value));
        if (skillLevels == null) skillLevels = new LinkedHashMap<>();
        skillLevels.replaceAll((key, value) -> value == null ? 0 : Math.max(0, value));
        if (chosenRewardCards == null) chosenRewardCards = new LinkedHashMap<>();
        if (pendingRewardCards == null) pendingRewardCards = new ArrayList<>();
        if (pendingRewardCards.size() != 3) {
            pendingRewardCards.clear();
            awaitingBossReward = false;
            pendingRewardBossNumber = 0;
        }
        if (awaitingBossReward) waveActive = false;
        ensurePotionSlots();
        nextEntityId = Math.max(2L, nextEntityId);

        // --- Ascension fields (Phase 20) ---
        ascensionTier = Math.max(0, ascensionTier);
        heartwood = Math.max(0, heartwood);
        peakWaveReached = Math.max(1, Math.min(FINAL_WAVE, peakWaveReached));
        if (waveNumber > peakWaveReached) peakWaveReached = waveNumber;
        potionsUsedThisRun = Math.max(0, potionsUsedThisRun);
        fastestWaveClearSeconds = Float.isFinite(fastestWaveClearSeconds) && fastestWaveClearSeconds > 0f
            ? fastestWaveClearSeconds : Float.MAX_VALUE;
        longestPauseSeconds = Float.isFinite(longestPauseSeconds) ? Math.max(0f, longestPauseSeconds) : 0f;
        shopStatsBoughtThisRun = Math.max(0, shopStatsBoughtThisRun);
        focus = Float.isFinite(focus) ? Math.max(0f, Math.min(focusMax, focus)) : 0f;
        focusMax = Float.isFinite(focusMax) && focusMax > 0f ? focusMax : 100f;
        totalRunsCompleted = Math.max(0, totalRunsCompleted);
        if (trophies == null) {
            trophies = new TrophyLedger();
        }
        trophies.repair();
        totalAscensionsCompleted = Math.max(0, totalAscensionsCompleted);
        wave200ReachedCount = Math.max(0, wave200ReachedCount);
        openingTier = Math.max(-1, openingTier);
        if (epilogueId == null) epilogueId = "";
        waveElapsedSeconds = Float.isFinite(waveElapsedSeconds) ? Math.max(0f, waveElapsedSeconds) : 0f;
        if (rootNodesPurchased == null) rootNodesPurchased = new LinkedHashMap<>();
        if (codexUnlocked == null) codexUnlocked = new LinkedHashMap<>();
        if (eliteKillCounts == null) eliteKillCounts = new LinkedHashMap<>();
        if (firstBossKills == null) firstBossKills = new LinkedHashMap<>();
        if (firstBossEncounters == null) firstBossEncounters = new LinkedHashMap<>();
        if (usedWhisperIds == null) usedWhisperIds = new LinkedHashMap<>();
        if (skillEvolutions == null) skillEvolutions = new LinkedHashMap<>();
        if (activeTrials == null) activeTrials = new ArrayList<>();
        if (trialUnlocked == null) trialUnlocked = new LinkedHashMap<>();
        if (pendingTrialOffer == null) pendingTrialOffer = new ArrayList<>();
        if (trialDraftPicks == null) trialDraftPicks = new ArrayList<>();
        if (rotTrail == null) rotTrail = new ArrayList<>();
        rootNodesPurchased.values().removeIf(v -> v == null);
        codexUnlocked.values().removeIf(v -> v == null);
        firstBossKills.values().removeIf(v -> v == null);
        firstBossEncounters.values().removeIf(v -> v == null);
        usedWhisperIds.values().removeIf(v -> v == null);
        skillEvolutions.values().removeIf(v -> v == null);
        trialUnlocked.values().removeIf(v -> v == null);
        activeTrials.removeIf(t -> TrialId.forName(t) == null);
        pendingTrialOffer.removeIf(t -> TrialId.forName(t) == null);
        trialDraftPicks.removeIf(t -> TrialId.forName(t) == null);
        rotTrail.removeIf(segment -> segment == null);
        if (HeroPath.forName(heroPath) == null) heroPath = null;
        eliteKillCounts.replaceAll((k, v) -> v == null ? 0 : Math.max(0, v));
    }

    public static int calculateHeartwoodReward(int peakWave, int ascensionTier, boolean flawless) {
        return calculateHeartwoodReward(peakWave, ascensionTier, flawless, GameMode.STANDARD);
    }

    /**
     * Heartwood for finishing a run, by mode (roadmap C3).
     *
     * <p>The formula is the one the progression equation is built from -- {@code peakWave / 5}, plus fifty for
     * reaching wave 200, plus ten a tier, plus twenty for a flawless run -- and the brief vigil takes half of
     * whatever it produces. That halving is the whole of C3's fix, and it is not a punishment for playing short:
     * the thirty-wave vigil is a floor by design, measured and accepted as one in
     * {@code finding-brief-vigil-has-no-teeth}, because the first session of the game must not punish a player
     * for not knowing a talent tree exists. A floor is fine. A floor that pays more heartwood per wave than the
     * run which can actually kill you is a farm, and a veteran farming an unloseable mode is the one player the
     * floor was never meant to serve. Half puts the long vigil back on top of the economy -- 0.55 heartwood a
     * wave flawless at tier zero against the brief run's 0.43 -- without touching a number the long run pays.
     */
    public static int calculateHeartwoodReward(
        int peakWave,
        int ascensionTier,
        boolean flawless,
        GameMode mode
    ) {
        int base = peakWave / 5;
        if (peakWave >= FINAL_WAVE) base += 50;
        base += ascensionTier * 10;
        if (flawless) base += 20;
        if (mode == GameMode.BRIEF) base /= 2;
        return Math.max(0, base);
    }

    public void recordWaveReached(int wave) {
        if (wave > peakWaveReached) peakWaveReached = wave;
        if (wave > waveNumber) waveNumber = wave;
    }

    public void resetForNewRun(long newSeed) {
        // Keep meta-progression
        int keptTier = ascensionTier;
        int keptHeartwood = heartwood;
        Map<String, Boolean> keptRoots = new LinkedHashMap<>(rootNodesPurchased);
        Map<String, Boolean> keptCodex = new LinkedHashMap<>(codexUnlocked);
        Map<String, Integer> keptElite = new LinkedHashMap<>(eliteKillCounts);
        Map<String, Boolean> keptFirstBoss = new LinkedHashMap<>(firstBossKills);
        Map<String, Boolean> keptEncounters = new LinkedHashMap<>(firstBossEncounters);
        Map<String, Boolean> keptWhispers = new LinkedHashMap<>(usedWhisperIds);
        Map<String, Boolean> keptTrialsUnlocked = new LinkedHashMap<>(trialUnlocked);
        List<Integer> keptMythicGrants =
            mythicGrantTiers == null ? new ArrayList<>() : new ArrayList<>(mythicGrantTiers);
        int keptRuns = totalRunsCompleted;
        int keptAscensions = totalAscensionsCompleted;

        // Full reset to fresh run
        GameMode keptMode = mode == null ? GameMode.STANDARD : mode;
        GameState fresh = newRun(newSeed);
        fresh.mode = keptMode;
        fresh.ascensionTier = keptTier;
        fresh.heartwood = keptHeartwood;
        fresh.rootNodesPurchased = keptRoots;
        fresh.codexUnlocked = keptCodex;
        fresh.eliteKillCounts = keptElite;
        fresh.firstBossKills = keptFirstBoss;
        fresh.firstBossEncounters = keptEncounters;
        fresh.usedWhisperIds = keptWhispers;
        fresh.trialUnlocked = keptTrialsUnlocked;
        fresh.mythicGrantTiers = keptMythicGrants;
        fresh.totalRunsCompleted = keptRuns;
        fresh.totalAscensionsCompleted = keptAscensions;
        fresh.peakWaveReached = 1;

        // Copy fresh into this
        this.mode = fresh.mode;
        this.runSeed = fresh.runSeed;
        this.combatRandomState = fresh.combatRandomState;
        this.affixRandomState = fresh.affixRandomState;
        this.waveNumber = 1;
        this.coins = 0;
        this.heroLevel = 1;
        this.heroExperience = 0;
        this.unspentTalentPoints = 0;
        this.defeatedBosses = 0;
        this.totalKills = 0;
        this.totalKillCoinsEarned = 0;
        this.worldTreeHealth = worldTreeMaxHealth;
        this.waveActive = false;
        this.starterLoadoutGranted = false;
        this.awaitingBossReward = false;
        this.pendingRewardBossNumber = 0;
        this.runComplete = false;
        this.ceremonyPending = false;
        this.bossIntroPending = false;
        this.bossIntroWave = 0;
        this.breatherPending = false;
        this.breatherWave = 0;
        this.wavePlannedEnemies = 0;
        this.tricklePulse = 0;
        this.escortWave = 0;
        this.secondTreePlanted = false;
        this.plantedTreesCount = 0;
        this.plantedTreeHealth = new ArrayList<>();
        this.plantedTreeMaxHealth = new ArrayList<>();
        this.treeSiegeRemainingSeconds = 0f;
        this.heroDiedThisRun = false;
        this.potionsUsedThisRun = 0;
        this.fastestWaveClearSeconds = Float.MAX_VALUE;
        this.longestPauseSeconds = 0f;
        this.openingTier = -1;
        this.epilogueId = "";
        this.shopStatsBoughtThisRun = 0;
        this.bareHandedEligible = true;
        this.noPotionRun = true;
        this.dryKillsSinceItemDrop = 0;
        this.rotTrail = fresh.rotTrail;
        this.simulationSpeed = 1f;
        this.waveElapsedSeconds = 0f;
        this.focus = 0f;
        this.hero = fresh.hero;
        this.aliveEnemies = fresh.aliveEnemies;
        this.aliveBosses = fresh.aliveBosses;
        this.projectiles = fresh.projectiles;
        this.drops = fresh.drops;
        this.inventory = fresh.inventory;
        this.equippedItems = fresh.equippedItems;
        this.permanentEffects = fresh.permanentEffects;
        this.shopUpgradeLevels = fresh.shopUpgradeLevels;
        this.skillLevels = fresh.skillLevels;
        this.chosenRewardCards = fresh.chosenRewardCards;
        this.pendingRewardCards = fresh.pendingRewardCards;
        this.healthPotions = fresh.healthPotions;
        this.activeTrials = fresh.activeTrials;
        this.pendingTrialOffer = fresh.pendingTrialOffer;
        this.trialDraftPicks = fresh.trialDraftPicks;
        this.heroPath = fresh.heroPath;
        this.skillEvolutions = fresh.skillEvolutions;
        this.nextEntityId = 2L;
        this.mythicGrantTiers = fresh.mythicGrantTiers;
        this.mythicGrantedItemId = null;
        validateAndRepair();
    }

    /** A trial offer is open and the run's pair is not yet bound. */
    public boolean draftPending() {
        return pendingTrialOffer != null && !pendingTrialOffer.isEmpty()
            && (activeTrials == null || activeTrials.size() < TrialDraftSystem.PICK_COUNT);
    }

    public int ascendAndAwardHeartwood() {
        boolean flawless = !heroDiedThisRun;
        int earned = Math.round(calculateHeartwoodReward(peakWaveReached, ascensionTier, flawless, mode)
            * TrialEffects.heartwoodMultiplier(activeTrials));
        heartwood += earned;
        ascensionTier++;
        totalAscensionsCompleted++;
        totalRunsCompleted++;
        return earned;
    }

    private static long initialRandomState(long seed) {
        long mixed = seed + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
        mixed ^= mixed >>> 31;
        return mixed == 0L ? 0xD1B54A32D192ED03L : mixed;
    }

    /** Canonical max-health recompute: hero points plus equipment, through every multiplier. */
    public void synchronizeEquipmentHealth() {
        hero.maxHealth = new HeroStatCalculator().maxHealth(this);
        hero.health = Math.max(0f, Math.min(hero.maxHealth, hero.health));
    }

    private void ensurePotionSlots() {
        if (healthPotions == null) {
            healthPotions = new ArrayList<>();
        }
        while (healthPotions.size() < 6) {
            healthPotions.add(0);
        }
        while (healthPotions.size() > 6) {
            healthPotions.remove(healthPotions.size() - 1);
        }
        for (int i = 0; i < healthPotions.size(); i++) {
            healthPotions.set(i, Math.max(0, healthPotions.get(i)));
        }
    }
}
