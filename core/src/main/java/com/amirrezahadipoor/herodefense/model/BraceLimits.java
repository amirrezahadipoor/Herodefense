package com.amirrezahadipoor.herodefense.model;

/**
 * The brace's clock, in the model, because the model is where the Hero's own timers are repaired after a load
 * (roadmap A2).
 *
 * <p>Three seconds up, twelve seconds from raise to raise. The numbers are a trade rather than a buff: while the
 * shield is up the bow is silent and the Hero is rooted, so a brace spends a quarter of the bow's uptime to buy
 * sixty percent off whatever lands inside the window -- including the boss specials that
 * {@code BossSpecialAttackSystem} lands regardless of position, which is exactly the hit no step can avoid and
 * the reason this verb exists. {@code docs/BALANCE.md} carries the arithmetic and the reason the published bands
 * did not move: no simulated policy ever raises the shield.
 */
public final class BraceLimits {
    /** How long one raise of the shield lasts. */
    public static final float BRACE_SECONDS = 3f;
    /** Raise to raise. Longer than the brace, so the shield is a window and not a stance. */
    public static final float COOLDOWN_SECONDS = 12f;
    /** The share of a hit that gets through the shield. */
    public static final float DAMAGE_TAKEN_MULTIPLIER = 0.4f;

    /**
     * How long after the raise the shield is still <em>set</em>.
     *
     * <p>This is the number the whole verb turns on. A brace raised early is a damage reduction; a brace raised
     * as the blow arrives is a negation. The window is short enough that it cannot be held -- four and a half
     * tenths of a second, about the length of a boss's telegraph flash -- and long enough to be hittable by a
     * player who is watching the enemy rather than the meter.
     */
    public static final float SET_WINDOW_SECONDS = 0.45f;
    /**
     * How much of the cooldown a set takes back.
     *
     * <p>Without the refund a set is still a twelve-second decision, and the mechanic would be a bonus for luck
     * rather than a rhythm: read the blow, answer the blow, be ready for the next one. With it, two sets in a
     * row are possible and three need the wave to keep coming -- which is the difference between a flourish and
     * a way to play.
     */
    public static final float SET_COOLDOWN_REFUND_SECONDS = 6f;
    /** Focus a set is worth, in landed hits' worth of charge: three normal arrows. */
    public static final int SET_FOCUS_HITS = 3;
    /** How long the set's ring stays visible after it lands. */
    public static final float SET_FLASH_SECONDS = 0.5f;

    private BraceLimits() {
    }
}
