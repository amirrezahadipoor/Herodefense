package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.SpawnLane;

/** Places one rotating boss on every fifth wave. */
public final class BossWaveSpawner {
    private final BossFactory factory;

    public BossWaveSpawner(BossFactory factory) {
        this.factory = factory;
    }

    public boolean isBossWave(int waveNumber) {
        return waveNumber >= EnemyWaveSpawner.BOSS_WAVE_INTERVAL
            && waveNumber <= GameState.FINAL_WAVE
            && waveNumber % EnemyWaveSpawner.BOSS_WAVE_INTERVAL == 0;
    }

    /**
     * The boss's escort on a boss wave (P6 boss escorts): four bodies on the early laps, one
     * more every twenty-five waves, so the last bosses walk in with twelve. Zero anywhere else --
     * regular waves have their own pulses and owe the boss nothing.
     */
    public int escortCountForWave(int waveNumber) {
        if (!isBossWave(waveNumber)) {
            return 0;
        }
        return 4 + waveNumber / 25;
    }

    /**
     * How many boss waves a run of {@code runLengthWaves} holds: one every {@link EnemyWaveSpawner#BOSS_WAVE_INTERVAL}
     * waves, so forty in the standard run of 200 and six in the thirty-wave vigil. The end-of-run summary and the
     * reward screen both print the count against this, not against a number of their own -- the summary said
     * "39 / 20" while it kept one.
     */
    public static int bossesInRun(int runLengthWaves) {
        return Math.max(0, runLengthWaves) / EnemyWaveSpawner.BOSS_WAVE_INTERVAL;
    }

    public BossType typeForWave(int waveNumber) {
        if (!isBossWave(waveNumber)) {
            throw new IllegalArgumentException("Not a boss wave: " + waveNumber);
        }
        int bossNumber = waveNumber / 5;
        BossType[] types = BossType.values();
        return types[(bossNumber - 1) % types.length];
    }

    public Boss spawn(GameState state, int waveNumber) {
        if (state == null || !isBossWave(waveNumber)) {
            throw new IllegalArgumentException("A valid state and fifth wave are required");
        }
        int bossNumber = waveNumber / 5;
        SpawnLane lane = SpawnLane.fromIndex(bossNumber - 1);
        float x;
        float y;
        switch (lane) {
            case LEFT -> {
                x = -EnemyWaveSpawner.EDGE_OFFSET;
                y = WorldLayout.HERO_CENTER_Y;
            }
            case RIGHT -> {
                x = WorldLayout.REFERENCE_WIDTH + EnemyWaveSpawner.EDGE_OFFSET;
                y = WorldLayout.HERO_CENTER_Y;
            }
            case SOUTH -> {
                x = WorldLayout.HERO_CENTER_X;
                y = -EnemyWaveSpawner.EDGE_OFFSET;
            }
            default -> throw new IllegalStateException("Unhandled boss lane: " + lane);
        }
        Boss boss = factory.create(
            state, typeForWave(waveNumber), x, y, bossNumber, lane.id()
        );
        state.aliveBosses.add(boss);
        return boss;
    }
}
