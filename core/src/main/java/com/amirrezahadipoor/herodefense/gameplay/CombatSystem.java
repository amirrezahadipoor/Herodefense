package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.IdentityCues;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.FloatingCoinTextSystem;
import com.amirrezahadipoor.herodefense.polish.FloatingDamageTextSystem;
import com.amirrezahadipoor.herodefense.polish.HitStopSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.potions.AutoPotionSystem;
import com.amirrezahadipoor.herodefense.potions.PotionDropSystem;
import com.amirrezahadipoor.herodefense.potions.PotionTier;
import com.amirrezahadipoor.herodefense.presentation.RunPresentationSystem;
import com.amirrezahadipoor.herodefense.settings.GameSettings;
import com.amirrezahadipoor.herodefense.story.CodexSystem;

/**
 * One frame of combat: the hero's arrows and every effect they cause, the boss specials, the melee the hero takes
 * back, potions, drops and the rewards a kill pays out (roadmap R2.2).
 *
 * <p>Extracted verbatim from {@code HeroDefenseGame.updatePlaying}: the class that used to own this block was
 * 1,519 lines, and the combat frame is one of the five slices that bring it to the roadmap's 400-line ceiling.
 * The systems are handed in at construction rather than pulled from the game, so this class reads as the order of
 * a fight — shoot, react, be hit, heal, collect, pay out — and can be driven by a test without a libGDX context.
 */
public final class CombatSystem {

    /** What one frame of combat decided: whether the run ended and whether a level-up opened. */
    public record Frame(boolean gameOver, boolean leveledUp, int criticalHits) {
    }

    private final HeroAutoAttackSystem heroAutoAttackSystem;
    private final BossSpecialAttackSystem bossSpecialAttackSystem;
    private final EnemyMeleeAttackSystem enemyMeleeAttackSystem;
    private final AutoPotionSystem autoPotionSystem;
    private final ItemDropSystem itemDropSystem;
    private final PotionDropSystem potionDropSystem;
    private final KillRewardSystem killRewardSystem;
    private final EliteAffixSystem eliteAffixSystem;
    private final DropPickupSystem dropPickupSystem;
    private final RunPresentationSystem presentationSystem;
    private final CodexSystem codexSystem;
    private final ParticleSystem particleSystem;
    private final FloatingCoinTextSystem floatingCoinTextSystem;
    private final FloatingDamageTextSystem floatingDamageTextSystem;
    private final HitStopSystem hitStopSystem;
    private final ScreenShakeSystem screenShakeSystem;
    private final AudioPlayback audioManager;

    public CombatSystem(
        HeroAutoAttackSystem heroAutoAttackSystem,
        BossSpecialAttackSystem bossSpecialAttackSystem,
        EnemyMeleeAttackSystem enemyMeleeAttackSystem,
        AutoPotionSystem autoPotionSystem,
        ItemDropSystem itemDropSystem,
        PotionDropSystem potionDropSystem,
        KillRewardSystem killRewardSystem,
        EliteAffixSystem eliteAffixSystem,
        DropPickupSystem dropPickupSystem,
        RunPresentationSystem presentationSystem,
        CodexSystem codexSystem,
        ParticleSystem particleSystem,
        FloatingCoinTextSystem floatingCoinTextSystem,
        FloatingDamageTextSystem floatingDamageTextSystem,
        HitStopSystem hitStopSystem,
        ScreenShakeSystem screenShakeSystem,
        AudioPlayback audioManager
    ) {
        this.heroAutoAttackSystem = heroAutoAttackSystem;
        this.bossSpecialAttackSystem = bossSpecialAttackSystem;
        this.enemyMeleeAttackSystem = enemyMeleeAttackSystem;
        this.autoPotionSystem = autoPotionSystem;
        this.itemDropSystem = itemDropSystem;
        this.potionDropSystem = potionDropSystem;
        this.killRewardSystem = killRewardSystem;
        this.eliteAffixSystem = eliteAffixSystem;
        this.dropPickupSystem = dropPickupSystem;
        this.presentationSystem = presentationSystem;
        this.codexSystem = codexSystem;
        this.particleSystem = particleSystem;
        this.floatingCoinTextSystem = floatingCoinTextSystem;
        this.floatingDamageTextSystem = floatingDamageTextSystem;
        this.hitStopSystem = hitStopSystem;
        this.screenShakeSystem = screenShakeSystem;
        this.audioManager = audioManager;
    }

    /**
     * Advances combat by one frame and returns what the wave director has to react to. {@code settings} is passed
     * per call because the player can change auto-sell rules while the run is on.
     */
    public Frame update(GameState state, float simulationDelta, GameSettings settings) {
        // The brace's clocks age before anything deals or fires damage this tick, so a shield that expires on
        // this frame has already expired when the specials and the swings are resolved, and one raised by a tap
        // between frames is up for all of them.
        BraceSystem.update(state, simulationDelta);
        // The late-wave roles adjust the bodies before this tick's arrows, movement and melee read them (A3).
        EnemyRoleSystem.update(state, simulationDelta);
        int livingBeforeAttack = state.livingEnemyCount();
        int bossesBeforeAttack = ArenaQueries.livingBossCount(state);
        float enemyHealthBeforeAttack = ArenaQueries.totalEnemyHealth(state);
        HeroAttackUpdateResult attackEvents = heroAutoAttackSystem.update(state, simulationDelta);
        floatingDamageTextSystem.emitAll(attackEvents.events());
        // Every arrow in a Multi Shot volley bursts where it lands (events carry y + 40 for text).
        for (CombatEvent event : attackEvents.events()) {
            switch (event.kind()) {
                case HIT -> particleSystem.emitHit(event.x(), event.y() - 40f, false);
                case CRITICAL_HIT -> particleSystem.emitHit(event.x(), event.y() - 40f, true);
                case CHAIN_ARC -> particleSystem.emitChainArc(
                    event.fromX(), event.fromY(), event.x(), event.y());
                case STUN -> particleSystem.emitStunSparks(event.x(), event.y() - 30f, event.amount());
                default -> { }
            }
        }
        if (attackEvents.criticalHits() > 0) {
            hitStopSystem.triggerCriticalHit();
            screenShakeSystem.triggerCriticalHit();
            audioManager.play(AudioCue.CRITICAL);
        }
        if (attackEvents.chainArcs() > 0) audioManager.play(AudioCue.CHAIN_LIGHTNING);
        if (attackEvents.stuns() > 0) audioManager.play(AudioCue.STUN);
        if (attackEvents.shots() > 1) {
            audioManager.play(AudioCue.MULTI_SHOT);
            audioManager.play(AudioCue.BOW_RELEASE_LIGHT);
            particleSystem.emitMuzzleFlash(state.hero.x, state.hero.y + 45f, attackEvents.shots());
        } else if (attackEvents.shots() == 1) {
            audioManager.play(AudioCue.BOW_RELEASE);
        }
        if (ArenaQueries.totalEnemyHealth(state) < enemyHealthBeforeAttack - 0.001f) {
            audioManager.play(AudioCue.HIT);
        }
        if (state.livingEnemyCount() < livingBeforeAttack) {
            // F2: the freshest corpse names the body class, so small and heavy deaths sound different.
            audioManager.play(IdentityCues.deathFor(IdentityCues.newestCorpseType(state)));
            audioManager.play(AudioCue.KILL);
        }
        if (ArenaQueries.livingBossCount(state) < bossesBeforeAttack) {
            screenShakeSystem.triggerBossKill();
        }
        float heroHealthBeforeAttack = state.hero.health;
        bossSpecialAttackSystem.update(state, simulationDelta);
        if (bossSpecialAttackSystem.consumeTelegraphsStarted() > 0) {
            audioManager.play(AudioCue.TELEGRAPH_WARNING);
        }
        BossEvolution.update(state);
        EnemyVerbs.update(
            state, simulationDelta, damage -> enemyMeleeAttackSystem.applyVerbDamage(state, damage));
        boolean gameOver = enemyMeleeAttackSystem.update(state, simulationDelta);
        if (state.hero.health < heroHealthBeforeAttack - 0.001f) {
            screenShakeSystem.triggerHeroHit();
            particleSystem.emitHit(state.hero.x, state.hero.y + 45f, false);
            audioManager.play(state.hero.alive ? AudioCue.HIT : AudioCue.DEATH);
        }
        presentationSystem.emitDefeatParticles(state);
        if (!gameOver && state.hero.alive) {
            PotionTier used = autoPotionSystem.update(state);
            if (used != null) {
                state.potionsUsedThisRun++;
                state.noPotionRun = false;
            }
        }
        int itemDrops = itemDropSystem.processDefeatedEnemies(state);
        if (itemDrops > 0) audioManager.play(AudioCue.ITEM_DROP);
        potionDropSystem.processDefeatedEnemies(state);
        KillRewardResult killRewards = killRewardSystem.processDefeatedEnemies(state);
        for (Boss boss : state.aliveBosses) {
            if (boss != null && !boss.alive && boss.killRewardsGranted) {
                codexSystem.unlockForBossKill(state, boss.bossType);
            }
        }
        eliteAffixSystem.update(state, simulationDelta);
        presentationSystem.presentEliteFragments(state);
        codexSystem.unlockForWaveReached(state);
        codexSystem.unlockForField(state, ArenaLayout.forSeed(state.runSeed));
        codexSystem.unlockSecretsForProgress(state);
        codexSystem.unlockSecretsForSet(state);
        if (killRewards.coins() > 0) {
            audioManager.play(AudioCue.COIN_PICKUP);
            floatingCoinTextSystem.emit(
                state.hero.x,
                state.hero.y + 145f,
                killRewards.coins()
            );
        }
        if (killRewards.levelsGained() > 0) audioManager.play(AudioCue.LEVEL_UP);
        presentationSystem.emitPendingPickupParticles(state, simulationDelta);
        presentationSystem.emitCollectionSparkles(state, simulationDelta);
        int collectedDrops = dropPickupSystem.update(state, simulationDelta, settings);
        if (collectedDrops > 0) codexSystem.unlockSecretsForEquipment(state);
        if (dropPickupSystem.lastAutoSoldItems() > 0) {
            floatingDamageTextSystem.emitCoins(
                dropPickupSystem.lastAutoSoldCoins(),
                state.hero.x,
                state.hero.y + 96f
            );
            audioManager.play(AudioCue.ITEM_DROP);
        }
        // B4: last in the frame, and only here. Everything that wants a corpse -- the death cue that names the
        // body class, the three loot passes above, the defeat particles -- has already read it this tick, so the
        // reaper cannot beat a reader to the body it is about to take away.
        ReaperSystem.update(state, simulationDelta);
        return new Frame(gameOver, killRewards.levelsGained() > 0, attackEvents.criticalHits());
    }
}
