package com.amirrezahadipoor.herodefense.gameplay;

/** Result of completing one wave in the same continuous run state. */
public enum WaveCompletion {
    NEXT_WAVE,
    BOSS_REWARD,
    /** Wave 100 is behind us: the planting ceremony plays before wave 101 spawns. */
    PLANTING_CEREMONY,
    /** A boss wave advanced: the watch-only intro plays before the wave spawns. */
    BOSS_INTRO,
    /** A milestone wave cleared: Pip's breather plays before the next wave spawns. */
    BREATHER,
    RUN_COMPLETED,
    NO_CHANGE
}
