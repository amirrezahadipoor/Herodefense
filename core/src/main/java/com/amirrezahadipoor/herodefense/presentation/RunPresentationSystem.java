package com.amirrezahadipoor.herodefense.presentation;

import com.amirrezahadipoor.herodefense.gameplay.BossFightScript;
import com.amirrezahadipoor.herodefense.gameplay.DropPickupSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.story.BossTitleCards;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import com.amirrezahadipoor.herodefense.story.Deeds;
import com.amirrezahadipoor.herodefense.story.EliteFragments;
import com.amirrezahadipoor.herodefense.story.HollowVoice;
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
        // The title card shows inside the watch-only intro; the entrance only claims the
        // encounter for the codex ledger, silently.
        BossTitleCards.claimFirstUnencountered(state, state.aliveBosses);
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

    /** What a freshly started wave opens with: the Hollow's halfway mark once, then reflections. */
    /**
     * The line a freshly started wave opens with: wave 1 belongs to the Hollow first (a first
     * session's hello, or the verdict on the run before it), and only an unclaimed greeting
     * falls through to the Warden's reflection.
     */
    public String waveOpenLine(GameState state) {
        if (state == null) {
            return null;
        }
        if (state.waveNumber == 1) {
            String greeting = HollowVoice.lineForGreeting(state);
            if (greeting != null) {
                return greeting;
            }
        }
        return waveStartLine(state);
    }

    public String waveStartLine(GameState state) {
        if (state == null) {
            return null;
        }
        if (state.waveNumber >= 100) {
            String hollow = HollowVoice.lineForGroveCeremony(state);
            if (hollow != null) {
                return hollow;
            }
        }
        return waveReflection(state);
    }

    /**
     * The Hollow at hero fall (roadmap ST1): the first death is addressed differently from every
     * later one. The line parks in the codex ledger until the game-over panel has shown it.
     */
    public String presentHollowDeath(GameState state) {
        return HollowVoice.lineForDeath(state);
    }

    /** One soft sparkle where a spared watcher stood, plus the Hollow's first word on mercy. */
    public String presentMercySpare(GameState state, float x, float y) {
        collectionSparkle(x, y + 30f);
        return HollowVoice.lineForSpare(state);
    }

    /**
     * Per-play tick: a fresh deed's announcement, then the evolution watch. Combat itself stays
     * silent -- no boss, Hollow, Pip or Warden line ever speaks while enemies live. Bosses talk
     * in their watch-only intro before the wave; once the fight starts, only deeds announce.
     */
    public void presentPlaytime(GameState state) {
        String deedLine = presentDeeds(state);
        if (deedLine != null) {
            beats.showBeat(deedLine);
            return;
        }
        presentBossEvolutions(state);
    }

    /**
     * The evolution signature (roadmap C2): the frame a boss crosses its fight's evolution
     * threshold — the enrage window's edge, or half health for the fights that only change
     * gear at the midpoint — one crimson-gold ring fires from its body. Exactly once per fight.
     */
    private void presentBossEvolutions(GameState state) {
        for (Boss boss : state.aliveBosses) {
            if (boss == null || !boss.alive || boss.evolutionPresented || boss.maxHealth <= 0f) {
                continue;
            }
            if (boss.health <= boss.maxHealth * BossFightScript.of(boss).evolutionHealthRatio()) {
                boss.evolutionPresented = true;
                particleSystem.emitEvolution(boss.x, boss.y + 40f);
            }
        }
    }

    /**
     * The Vigil Deeds (roadmap ST2): pays what the run has just earned and announces it.
     * Returns null on most frames; a deed line when one completed.
     */
    private String presentDeeds(GameState state) {
        java.util.List<Deeds> newlyCompleted = Deeds.completeNewlyEarned(state);
        if (newlyCompleted.isEmpty()) {
            return null;
        }
        beats.save();
        return newlyCompleted.get(0).announce();
    }

    /** The one bind of the collection sparkle; drops homing in and spared watchers share it. */
    private void collectionSparkle(float x, float y) {
        particleSystem.emitCollectionSparkle(x, y);
    }

    public void emitCollectionSparkles(GameState state, float deltaSeconds) {
        for (DropEntity drop : state.drops) {
            if (drop == null || !drop.active
                || drop.collectionStage != DropCollectionStage.HOMING) {
                continue;
            }
            if (drop.homingElapsedSeconds + deltaSeconds >= DropPickupSystem.HOMING_DURATION_SECONDS
                && ("ITEM".equals(drop.dropType) || "POTION".equals(drop.dropType))) {
                collectionSparkle(CombatEntityRenderer.dropTargetX(), CombatEntityRenderer.DROP_TARGET_Y + 30f);
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
