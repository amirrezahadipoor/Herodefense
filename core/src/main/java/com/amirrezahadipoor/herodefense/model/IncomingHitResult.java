package com.amirrezahadipoor.herodefense.model;

/** Outcome of one enemy hit attempt against the Hero. */
public enum IncomingHitResult {
    IGNORED,
    DODGED,
    /**
     * The blow landed on a shield that was raised into it (roadmap: the set). Nothing is taken, the dodge die is
     * not spent, and the run counts it -- a set is a decision the player made, so it is reported as one rather
     * than folded into {@link #DODGED}, which is a stat roll.
     */
    HELD,
    DAMAGED,
    KILLED
}
