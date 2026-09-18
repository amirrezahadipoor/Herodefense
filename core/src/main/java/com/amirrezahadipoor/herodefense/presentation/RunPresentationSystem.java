package com.amirrezahadipoor.herodefense.presentation;

import com.amirrezahadipoor.herodefense.gameplay.DropPickupSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.story.BossTitleCards;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import com.amirrezahadipoor.herodefense.story.EliteFragments;
import com.amirrezahadipoor.herodefense.story.ReflectionLines;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.DropCollectionStage;
import com.amirrezahadipoor.herodefense.model.DropEntity;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.render.CombatEntityRenderer;

/**
 * The presentation side of a run: what a defeat, a boss entrance, an elite kill or a freshly started wave
 * looks and sounds like.
 *
 * <p>Extracted from {@code HeroDefenseGame} (roadmap R2.2) because these are the methods that grow every
 * time a new effect is added, and they need nothing from the game class except the particle system, the
 * shake system, the codex and somewhere to put a story line. The game keeps the story line itself so the
 * HUD stays the only writer of that text.
 *
 * <p>Every method here is idempotent per entity through the existing claim flags
 * (`defeatParticlesEmitted`, `entrancePresented`, `eliteKillClaimed`, `collectionEffectEmitted`), which is
 * why they can be called from several places in the update loop without duplicating an effect.
 */
public final class RunPresentationSystem {

    /** Where a story line and a save request go; implemented by the game. */
    public interface BeatSink {
        void showBeat(String line);

        void save();
    }

    private final ParticleSystem particleSystem;
    private final ScreenShakeSystem screenShakeSystem;
    private final CodexSystem codexSystem;
    private final BeatSink beats;

    public RunPresentationSystem(ParticleSystem particleSystem, ScreenShakeSystem screenShakeSystem,
        CodexSystem codexSystem, BeatSink beats) {
        this.particleSystem = particleSystem;
        this.screenShakeSystem = screenShakeSystem;
        this.codexSystem = codexSystem;
        this.beats = beats;
    }

    public void emitDefeatParticles(GameState state) {
        for (Enemy enemy : state.aliveEnemies) {
            emitDefeatParticles(enemy, false);
        }
        for (Boss boss : state.aliveBosses) {
            emitDefeatParticles(boss, true);
        }
    }

    public void emitDefeatParticles(Enemy enemy, boolean boss) {
        if (enemy == null || enemy.alive || enemy.defeatParticlesEmitted) {
            return;
        }
        enemy.defeatParticlesEmitted = true;
        if (boss) {
            particleSystem.emitBossDeath(enemy.x, enemy.y + 40f);
        } else {
            // Per-type death burst for readability
            try {
                particleSystem.emitDeath(enemy.x, enemy.y + 30f, enemy.enemyType);
            } catch (Exception ignored) {
                particleSystem.emitDeath(enemy.x, enemy.y + 30f);
            }
        }
        particleSystem.emitCoins(enemy.x, enemy.y + 50f);
    }

    public void presentBossEntrance(GameState state) {
        for (Boss boss : state.aliveBosses) {
            if (boss == null || !boss.alive || boss.entrancePresented) {
                continue;
            }
            boss.entrancePresented = true;
            particleSystem.emitBossEntrance(boss.x, boss.y + 10f);
        }
        screenShakeSystem.triggerBossEntrance();
        String titleCard = BossTitleCards.claimFirstUnencountered(state, state.aliveBosses);
        if (titleCard != null) {
            beats.showBeat(titleCard);
            beats.save();
        }
    }

    /** Claims Elite kills for counts, codex, secret 28, and their §4 fragment overlay. */
    public void presentEliteFragments(GameState state) {
        boolean claimed = false;
        for (Enemy enemy : state.aliveEnemies) {
            if (enemy == null || enemy.alive || enemy.eliteAffix == null || enemy.eliteKillClaimed) {
                continue;
            }
            enemy.eliteKillClaimed = true;
            claimed = true;
            int count = 1;
            if (state.eliteKillCounts != null) {
                count = state.eliteKillCounts.getOrDefault(enemy.eliteAffix, 0) + 1;
                state.eliteKillCounts.put(enemy.eliteAffix, count);
            }
            codexSystem.unlockForEliteKill(state, enemy.eliteAffix);
            String fragment = EliteFragments.fragmentFor(enemy.eliteAffix, count);
            if (fragment != null) {
                beats.showBeat(fragment);
            }
        }
        if (claimed) {
            beats.save();
        }
    }

    /**
     * The reflection line for a freshly started wave, or {@code null} when a beat is already showing.
     *
     * @return the line to display, or {@code null}
     */
    public String waveReflection(GameState state) {
        if (!state.waveActive) {
            return null;
        }
        return ReflectionLines.lineForWave(state.waveNumber);
    }

    /** Sparkles where a homing drop lands on the Inventory control, before the drop is removed. */
    public void emitCollectionSparkles(GameState state, float deltaSeconds) {
        for (DropEntity drop : state.drops) {
            if (drop == null || !drop.active
                || drop.collectionStage != DropCollectionStage.HOMING) {
                continue;
            }
            if (drop.homingElapsedSeconds + deltaSeconds >= DropPickupSystem.HOMING_DURATION_SECONDS
                && ("ITEM".equals(drop.dropType) || "POTION".equals(drop.dropType))) {
                particleSystem.emitCollectionSparkle(
                    CombatEntityRenderer.DROP_TARGET_X, CombatEntityRenderer.DROP_TARGET_Y + 30f
                );
            }
        }
    }

    public void emitPendingPickupParticles(GameState state, float deltaSeconds) {
        for (DropEntity drop : state.drops) {
            if (drop == null || !drop.active || drop.collectionEffectEmitted) {
                continue;
            }
            boolean enteringHoming = drop.collectionStage == DropCollectionStage.HOMING
                || drop.pickupDelaySeconds <= deltaSeconds;
            if (enteringHoming
                && ("ITEM".equals(drop.dropType) || "POTION".equals(drop.dropType))) {
                drop.collectionEffectEmitted = true;
                particleSystem.emitItemPickup(drop.x, drop.y + 25f);
            }
        }
    }
}
