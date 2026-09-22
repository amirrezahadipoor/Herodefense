package com.amirrezahadipoor.herodefense.gameplay;

/**
 * The deep band's numbers: the six affixes that only a run past wave one hundred ever meets, tuned as one band
 * against the pressure gates.
 *
 * <p>They live in their own file for the same reason the band exists at all. Every affix here buys its question
 * with pressure -- armour costs the player time, a call costs bodies, a hammer costs a moment of attention -- and
 * time, bodies and attention are exactly what the per-wave pressure ceilings measure. Tuning one of them in
 * isolation moves the others' budgets, so they are read, changed and re-measured together, in one place, next to
 * the comment that says what each is spending.
 *
 * <p>Two of them carry a ceiling expressed in the hero's own bar rather than in enemy damage
 * ({@link #SPITEBARB_BAR_CAP}, {@link #HAMMERFALL_BAR_CAP}): a deep body's damage grows with the wave while the
 * bar grows with the shop, and an affix that spends raw enemy damage is the one shape that could turn a single
 * wave into an ambush.
 */
public final class DeepBandTuning {

    /** Stoneshell's armour: a long, slow, self-only window with no share and no ally to give it to. */
    public static final float STONESHELL_PERIOD = 9f;
    public static final float STONESHELL_DURATION = 2f;
    /** Gravebloom: the same rot ground weeping_rot walks, kept longer and hotter because it was paid for in death. */
    public static final float GRAVEBLOOM_LIFETIME = 4.5f;
    public static final float GRAVEBLOOM_DAMAGE_SHARE = 0.3f;
    /** Swarmcall: a wave that refills itself, in the same children hollowmolt leaves behind. */
    public static final float SWARMCALL_PERIOD = 18f;
    public static final int SWARMCALL_CHILDREN = 2;
    public static final float SWARMCALL_CHILD_HEALTH_SHARE = 0.08f;
    public static final float SWARMCALL_CHILD_DAMAGE_SHARE = 0.28f;
    /** Spitebarb: a tight radius, a slow clock, a heavy return -- the answer to hugging an elite. */
    public static final float SPITEBARB_RADIUS = 72f;
    public static final float SPITEBARB_TICK_SECONDS = 1.5f;
    public static final float SPITEBARB_DAMAGE_SHARE = 0.12f;
    /** The most a single spitebarb return may take out of a full bar. */
    public static final float SPITEBARB_BAR_CAP = 0.015f;
    /** Hammerfall: one clock, read from both sides -- the warning and the strike cannot disagree. */
    public static final float HAMMERFALL_PERIOD = 4.5f;
    public static final float HAMMERFALL_WINDUP_SECONDS = 0.8f;
    public static final float HAMMERFALL_RADIUS = 115f;
    public static final float HAMMERFALL_DAMAGE_SHARE = 0.95f;
    /** The ceiling on one hammer blow, as a share of the hero's full bar: a tell the player can always survive. */
    public static final float HAMMERFALL_BAR_CAP = 0.08f;
    /** Bloodhowl: an aura recomputed every frame, so no body can carry a haste its source no longer gives. */
    public static final float BLOODHOWL_RADIUS = 170f;
    public static final float BLOODHOWL_HASTE = 0.12f;

    private DeepBandTuning() {
    }
}
