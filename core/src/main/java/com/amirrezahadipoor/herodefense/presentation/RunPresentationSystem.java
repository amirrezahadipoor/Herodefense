package com.amirrezahadipoor.herodefense.presentation;

import com.amirrezahadipoor.herodefense.audio.NarrationRequest;
import com.amirrezahadipoor.herodefense.audio.NarrationSystem;
import com.amirrezahadipoor.herodefense.gameplay.DropPickupSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.story.BossTitleCards;
import com.amirrezahadipoor.herodefense.story.BossTitleNarration;
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
 * looks and sounds like. F3 adds narration for boss title cards.
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
    private NarrationSystem narration;

    public RunPresentationSystem(ParticleSystem particleSystem, ScreenShakeSystem screenShakeSystem,
        CodexSystem codexSystem, BeatSink beats) {
        this.particleSystem = particleSystem;
        this.screenShakeSystem = screenShakeSystem;
        this.codexSystem = codexSystem;
        this.beats = beats;
    }

    public void setNarrationSystem(NarrationSystem narration) {
        this.narration = narration;
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
            try {
                particleSystem.emitDeath(enemy.x, enemy.y + 30f, enemy.enemyType);
            } catch (Exception ignored) {
                particleSystem.emitDeath(enemy.x, enemy.y + 30f);
            }
        }
        particleSystem.emitCoins(enemy.x, enemy.y + 50f);
    }

    public void presentBossEntrance(GameState state) {
        String claimedBossType = null;
        for (Boss boss : state.aliveBosses) {
            if (boss == null || !boss.alive || boss.entrancePresented) {
                continue;
            }
            boss.entrancePresented = true;
            particleSystem.emitBossEntrance(boss.x, boss.y + 10f);
            if (claimedBossType == null) claimedBossType = boss.bossType;
        }
        screenShakeSystem.triggerBossEntrance();
        String titleCard = BossTitleCards.claimFirstUnencountered(state, state.aliveBosses);
        if (titleCard != null) {
            beats.showBeat(titleCard);
            beats.save();
            // F3: narrate boss title card if TTS available
            if (narration != null && claimedBossType != null) {
                NarrationRequest req = BossTitleNarration.forBoss(claimedBossType);
                if (req != null) narration.narrate(req);
            } else if (narration != null) {
                // fallback: narrate the title card line itself
                narration.narrate(new NarrationRequest(NarrationRequest.Type.BOSS_TITLE, "boss", titleCard, 0.9f));
            }
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

    public String waveReflection(GameState state) {
        if (!state.waveActive) {
            return null;
        }
        return ReflectionLines.lineForWave(state.waveNumber);
    }

    public void emitCollectionSparkles(GameState state, float deltaSeconds) {
        for (DropEntity drop : state.drops) {
            if (drop == null || !drop.active
                || drop.collectionStage != DropCollectionStage.HOMING) {
                continue;
            }
            if (drop.homingElapsedSeconds + deltaSeconds >= DropPickupSystem.HOMING_DURATION_SECONDS
                && ("ITEM".equals(drop.dropType) || "POTION".equals(drop.dropType))) {
                particleSystem.emitCollectionSparkle(
                    CombatEntityRenderer.dropTargetX(), CombatEntityRenderer.DROP_TARGET_Y + 30f
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
