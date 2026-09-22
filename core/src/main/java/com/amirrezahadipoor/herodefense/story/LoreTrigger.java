package com.amirrezahadipoor.herodefense.story;

/** How a Grove Codex entry is unlocked. */
public enum LoreTrigger {
    WAVE_MILESTONE,
    /** The first night fought on one of the arena's four fields. */
    FIELD_FIRST_NIGHT,
    BOSS_FIRST_KILL,
    ELITE_KILL,
    ASCENSION,
    SECRET,
    /** The first blow a raised shield answered rather than absorbed: the set. */
    SET_HELD
}
