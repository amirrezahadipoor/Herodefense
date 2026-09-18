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

    private BraceLimits() {
    }
}
