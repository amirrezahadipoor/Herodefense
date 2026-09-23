package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.rewards.BossRewardCardSystem;
import com.amirrezahadipoor.herodefense.trials.TrialEffects;

/** Starts a wave and immediately rolls a cleared wave into the next one. */
public final class WaveLifecycleSystem {
    private final EnemyWaveSpawner regularSpawner;
    private final BossWaveSpawner bossSpawner;
    private final BossRewardCardSystem rewardCards;
    private final ContinuousWaveRun continuousRun;

    public WaveLifecycleSystem(EnemyWaveSpawner regularSpawner, ContinuousWaveRun continuousRun) {
        this(
            regularSpawner,
            new BossWaveSpawner(new BossFactory()),
            new BossRewardCardSystem(),
            continuousRun
        );
    }

    public WaveLifecycleSystem(
        EnemyWaveSpawner regularSpawner,
        BossWaveSpawner bossSpawner,
        BossRewardCardSystem rewardCards,
        ContinuousWaveRun continuousRun
    ) {
        this.regularSpawner = regularSpawner;
        this.bossSpawner = bossSpawner;
        this.rewardCards = rewardCards;
        this.continuousRun = continuousRun;
    }

    public boolean startCurrentWave(GameState state) {
        if (state == null || state.runComplete || state.waveActive || state.awaitingBossReward
            || state.ceremonyPending || state.bossIntroPending) {
            return false;
        }
        if (bossSpawner.isBossWave(state.waveNumber)) {
            // The fight waits for its watch-only intro: the wave number has already advanced,
            // and the spawn happens in completeBossIntro once the boss has walked back out.
            state.bossIntroPending = true;
            state.bossIntroWave = state.waveNumber;
            return true;
        }
        return spawnCurrentWave(state);
    }

    /** Spawns the current wave for real: regulars at once, the boss once its intro hands off. */
    private boolean spawnCurrentWave(GameState state) {
        if (bossSpawner.isBossWave(state.waveNumber)) {
            bossSpawner.spawn(state, state.waveNumber);
        } else {
            regularSpawner.spawnRegularEnemies(
                state,
                state.waveNumber,
                Math.min(
                    EnemyWaveSpawner.MAX_REGULAR_ENEMIES,
                    regularSpawner.regularCountForWave(state.waveNumber)
                        + TrialEffects.extraEnemiesPerWave(state.activeTrials)
                )
            );
        }
        state.waveActive = true;
        // A1: each wave grants its own stepping budget, so the defender is never rooted for the run by a wave
        // that spent everything, and never carries a surplus into the next one either.
        HeroMovementSystem.beginWave(state);
        return true;
    }

    /**
     * Called when the boss intro ends (or instantly by the simulator): the pending boss wave
     * spawns for real and the fight starts.
     */
    public boolean completeBossIntro(GameState state) {
        if (state == null || !state.bossIntroPending) {
            return false;
        }
        state.bossIntroPending = false;
        return spawnCurrentWave(state);
    }

    /**
     * The intro's prop: the pending wave's boss at its lane's edge, cleared of any stale twin
     * a reload may have persisted, or null when no intro is pending. The cinematic walks it to
     * its mark and back out; the last frame removes it and {@link #completeBossIntro} spawns
     * the fight's own boss.
     */
    public Boss spawnBossIntroProp(GameState state) {
        if (state == null || !state.bossIntroPending) {
            return null;
        }
        if (!bossSpawner.isBossWave(state.bossIntroWave)) {
            return null;
        }
        state.aliveBosses.clear();
        return bossSpawner.spawn(state, state.bossIntroWave);
    }

    /** Call after combat resolution. A new wave is spawned in the same update on clear. */
    public WaveCompletion updateAfterCombat(GameState state) {
        if (state == null || !state.waveActive || state.runComplete
            || state.hero == null || !state.hero.alive || state.livingEnemyCount() > 0) {
            return WaveCompletion.NO_CHANGE;
        }
        // B4: the wave is over, so the corpses of it are not carried into the next one. This is what keeps a
        // between-waves save (the level-up write, the pause write) proportional to one wave and not to the run.
        ReaperSystem.clear(state);
        if (bossSpawner.isBossWave(state.waveNumber)) {
            int bossNumber = state.waveNumber / 5;
            state.defeatedBosses = Math.max(state.defeatedBosses, bossNumber);
            ContinuousWaveRun.recordWaveClear(state);
            state.waveActive = false;
            rewardCards.prepareChoices(state, bossNumber);
            return WaveCompletion.BOSS_REWARD;
        }

        WaveCompletion result = continuousRun.completeCurrentWave(state);
        state.waveActive = false;
        if (result == WaveCompletion.NEXT_WAVE) {
            startCurrentWave(state);
            if (state.bossIntroPending) {
                return WaveCompletion.BOSS_INTRO;
            }
        }
        return result;
    }

    /** Called when the planting ceremony ends: a new Heartwood stands and the next wave begins. */
    public boolean completePlantingCeremony(GameState state) {
        if (state == null || !state.ceremonyPending) return false;
        state.ceremonyPending = false;
        // 32.1: generalize boolean to count with per-tree HP
        if (state.plantedTreesCount < 3) {
            state.plantedTreesCount++;
            if (state.plantedTreeHealth == null) state.plantedTreeHealth = new java.util.ArrayList<>();
            if (state.plantedTreeMaxHealth == null) state.plantedTreeMaxHealth = new java.util.ArrayList<>();
            state.plantedTreeHealth.add(state.worldTreeMaxHealth);
            state.plantedTreeMaxHealth.add(state.worldTreeMaxHealth);
        }
        state.trophies.recordTreePlanted(state.plantedTreesCount);
        state.secondTreePlanted = state.plantedTreesCount > 0;
        state.anchorHeroAtArenaCenter();
        return startCurrentWave(state);
    }

    /** Continues the same run after another system has applied and cleared one card. */
    public WaveCompletion continueAfterBossReward(GameState state) {
        if (state == null || state.awaitingBossReward || !state.pendingRewardCards.isEmpty()) {
            return WaveCompletion.NO_CHANGE;
        }
        WaveCompletion result = continuousRun.completeCurrentWave(state);
        if (result == WaveCompletion.NEXT_WAVE) {
            startCurrentWave(state);
            if (state.bossIntroPending) {
                return WaveCompletion.BOSS_INTRO;
            }
        }
        return result;
    }
}
