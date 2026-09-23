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

    /** Spawns the current wave for real: regulars in trickle pulses, the boss once its intro hands off. */
    private boolean spawnCurrentWave(GameState state) {
        state.wavePlannedEnemies = 0;
        state.tricklePulse = 0;
        state.escortWave = 0;
        if (bossSpawner.isBossWave(state.waveNumber)) {
            bossSpawner.spawn(state, state.waveNumber);
            // P6 boss escorts: half the escort walks in with the boss; the rest answers the
            // boss's blood (or its death-cry) from reinforceEscort. The intro prop stays a lone
            // boss -- see spawnBossIntroProp -- so the trash-talk gets the stage alone.
            int escortTotal = bossSpawner.escortCountForWave(state.waveNumber);
            if (escortTotal > 0) {
                regularSpawner.spawnTrickle(
                    state, state.waveNumber, escortTotal, 0, (escortTotal + 1) / 2);
                state.escortWave = 1;
            }
        } else {
            // The omen is adjusted once on the whole wave, not once per pulse, and the first pulse
            // walks in now; the rest follows from updateAfterCombat as the wave falls.
            int total = EnemyWaveSpawner.omenAdjustedCount(
                state,
                state.waveNumber,
                Math.min(
                    EnemyWaveSpawner.maxRegularEnemiesForWave(state.waveNumber),
                    regularSpawner.regularCountForWave(state.waveNumber)
                        + TrialEffects.extraEnemiesPerWave(state.activeTrials)
                )
            );
            int[] pulses = EnemyWaveSpawner.planTrickles(total);
            state.wavePlannedEnemies = total;
            state.tricklePulse = 1;
            regularSpawner.spawnTrickle(state, state.waveNumber, total, 0, pulses[0]);
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

    /**
     * Walks in the next trickle pulse of a regular wave when the wave has earned it: the second
     * pulse at half strength, the third at quarter strength. A zero living count always earns
     * the next pulse, so a wave whose bodies are all dead but whose plan is not empty can never
     * get stuck. There is deliberately no stalled-seconds fallback: it fired exactly when the
     * hero was weakest, dumped the rest of the wave on them, and inverted the brief vigil's
     * ramp (measured opening 0.92 over closing 0.70). The gates alone keep every wave moving.
     */
    private boolean reinforceTrickle(GameState state) {
        if (state.tricklePulse <= 0) {
            return false;
        }
        int[] pulses = EnemyWaveSpawner.planTrickles(state.wavePlannedEnemies);
        if (state.tricklePulse >= pulses.length) {
            return false;
        }
        int living = state.livingEnemyCount();
        int total = state.wavePlannedEnemies;
        boolean gated = state.tricklePulse == 1 ? living * 2 < total : living * 4 < total;
        if (!gated) {
            return false;
        }
        int bodyOffset = 0;
        for (int pulse = 0; pulse < state.tricklePulse; pulse++) {
            bodyOffset += pulses[pulse];
        }
        regularSpawner.spawnTrickle(
            state, state.waveNumber, total, bodyOffset, pulses[state.tricklePulse]);
        state.tricklePulse++;
        return true;
    }

    /**
     * Walks in the rest of a boss wave's escort when the fight has earned it: the second pulse
     * answers at two-thirds of the boss's health, or as vengeance when the boss falls first.
     * Like the trickle gates it fires before the living check, so killing the boss into an
     * unfired second pulse earns the avengers instead of the reward card.
     */
    private boolean reinforceEscort(GameState state) {
        if (state.escortWave != 1) {
            return false;
        }
        int escortTotal = bossSpawner.escortCountForWave(state.waveNumber);
        int firstPulse = (escortTotal + 1) / 2;
        if (escortTotal <= firstPulse) {
            state.escortWave = 2;
            return false;
        }
        Boss fighter = null;
        for (Boss candidate : state.aliveBosses) {
            if (candidate != null && candidate.alive) {
                fighter = candidate;
                break;
            }
        }
        boolean fallen = fighter == null;
        boolean bloodied = !fallen && fighter.maxHealth > 0f
            && fighter.health * 3f <= fighter.maxHealth * 2f;
        if (!fallen && !bloodied) {
            return false;
        }
        regularSpawner.spawnTrickle(
            state, state.waveNumber, escortTotal, firstPulse, escortTotal - firstPulse);
        state.escortWave = 2;
        return true;
    }

    /** Call after combat resolution. A new wave is spawned in the same update on clear. */
    public WaveCompletion updateAfterCombat(GameState state) {
        if (state == null || !state.waveActive || state.runComplete
            || state.hero == null || !state.hero.alive) {
            return WaveCompletion.NO_CHANGE;
        }
        // P6 longer waves: a trickled wave reinforces before it may clear -- the rest walks in at
        // half and quarter strength, and only then is the wave over.
        // This read happens before the living check on purpose: a pulse that only fired on an empty
        // field would never arrive mid-fight, which is the whole point of the longer waves.
        if (reinforceTrickle(state)) {
            return WaveCompletion.NO_CHANGE;
        }
        // P6 boss escorts: the second pulse answers the boss's blood before the wave may clear.
        if (reinforceEscort(state)) {
            return WaveCompletion.NO_CHANGE;
        }
        if (state.livingEnemyCount() > 0) {
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
