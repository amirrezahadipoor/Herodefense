package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;

/**
 * The eight fight scripts a boss encounter can run (roadmap R3.2).
 *
 * <p>Before this the four boss identities always fought the same way: the same cadence, the same telegraph and
 * no reaction to being wounded, so the fortieth boss felt like the first one with a bigger health bar. A script
 * layers tempo, reach, readability, reaction and shape onto the identity: the visual and damage identity still
 * comes from {@code BossType}, while the script decides how the fight breathes. Every parameter is a constant
 * here, so the balance sweep and the unit tests can describe a fight instead of a spreadsheet.
 *
 * <p><b>What a script may change is measured, not stylistic.</b> Every axis that moves the combat simulation was
 * tested against the shipped balance sweep before it was dropped: scaling the per-hit damage (up to 1.30×), a
 * variable special cycle (0.70–1.30×), script-specific legs and reach, and finally the warning *length* itself
 * (0.28–0.85 s). Each of those moved a single-wave spike cell by 15–45 % — with the warning length pinned at the
 * reference 0.50 s, four of the six cells the earlier drafts broke reproduce the phase-87 numbers bit for bit,
 * including the two that used to sit over their ceiling. The sweep reads one deterministic 200-wave stream, so
 * changing *when* a boss warns reshuffles every draw after it; changing how the special is packaged does not.
 *
 * <p>So the shipped roster pins the physics and the timing — one cycle, one warning length, the identity's own
 * legs and reach — and varies only what the simulation cannot feel: how large the tell is drawn (0.85×–1.35×),
 * how many hits the tell carries (one full hit, or two half hits, i.e. two dodge chances), and whether being
 * wounded shrinks the tell. One cycle still lands exactly the reference damage
 * ({@code specialDamageMultiplier() = cycleMultiplier / hits()}), and {@code BossFightScriptTest} asserts the
 * pins so a later phase that wants warning-length variety has to change them on purpose and re-run the sweep. */
public enum BossFightScript {

    /** The authored identity fight, unchanged: this is what a run met before scripts existed. */
    MEASURED(1.00f, 1.00f, 1.00f, 0.50f, 1.00f, 0f, 1.00f, false),

    /** The hardest tell to read in the roster: same warning, drawn smaller than any other. */
    CRYPTIC_TELL(1.00f, 1.00f, 1.00f, 0.50f, 0.85f, 0f, 1.00f, false),

    /** A small, quick-looking tell: shorter on screen than the reference one. */
    SNAPPING_TELL(1.00f, 1.00f, 1.00f, 0.50f, 0.95f, 0f, 1.00f, false),

    /** Fights the reference fight until it is wounded, then its tell shrinks below 40 % health. */
    ENRAGED_HEART(1.00f, 1.00f, 1.00f, 0.50f, 1.00f, 0.40f, 0.70f, false),

    /** A large tell, held on screen at 1.25× the reference size. */
    PATIENT_WARDEN(1.00f, 1.00f, 1.00f, 0.50f, 1.25f, 0f, 1.00f, false),

    /** Every special lands twice for half the damage each, on one warning: two dodge chances. */
    TWIN_TELEGRAPH(1.00f, 1.00f, 1.00f, 0.50f, 1.10f, 0f, 1.00f, true),

    /** The biggest tell in the roster, drawn at 1.35× so nothing about it can be missed. */
    BULWARK(1.00f, 1.00f, 1.00f, 0.50f, 1.35f, 0f, 1.00f, false),

    /** A tell drawn small with two half hits behind it, and it shrinks further when wounded. */
    ASSASSIN(1.00f, 1.00f, 1.00f, 0.50f, 0.85f, 0.30f, 0.70f, true);

    /** Warning window of the reference fight; a cycle is one of these plus the identity's own cooldown. */
    public static final float REFERENCE_TELEGRAPH_SECONDS = 0.50f;

    private final float cycleMultiplier;
    private final float movementMultiplier;
    private final float attackRangeMultiplier;
    private final float telegraphSeconds;
    private final float telegraphScale;
    private final float enrageHealthRatio;
    private final float enrageTellScale;
    private final boolean doubleStrike;

    BossFightScript(
        float cycleMultiplier,
        float movementMultiplier,
        float attackRangeMultiplier,
        float telegraphSeconds,
        float telegraphScale,
        float enrageHealthRatio,
        float enrageTellScale,
        boolean doubleStrike
    ) {
        this.cycleMultiplier = cycleMultiplier;
        this.movementMultiplier = movementMultiplier;
        this.attackRangeMultiplier = attackRangeMultiplier;
        this.telegraphSeconds = telegraphSeconds;
        this.telegraphScale = telegraphScale;
        this.enrageHealthRatio = enrageHealthRatio;
        this.enrageTellScale = enrageTellScale;
        this.doubleStrike = doubleStrike;
    }

    /** The script a boss was created with; an unknown or missing name falls back to the measured fight. */
    public static BossFightScript of(Boss boss) {
        return boss == null ? MEASURED : ofName(boss.fightScript);
    }

    /** Same fallback rule for a stored name, so an old save file never breaks a fight. */
    public static BossFightScript ofName(String name) {
        if (name == null) return MEASURED;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException unknownScript) {
            return MEASURED;
        }
    }

    /** Length of one full special cycle, as a multiple of the reference fight's own cycle. */
    public float attackIntervalMultiplier() {
        return cycleMultiplier;
    }

    public float movementMultiplier() {
        return movementMultiplier;
    }

    public float attackRangeMultiplier() {
        return attackRangeMultiplier;
    }

    /** Warning length of an unwounded boss; the cycle length does not depend on it. */
    public float telegraphSeconds() {
        return telegraphSeconds;
    }

    public float telegraphScale() {
        return telegraphScale;
    }

    public float enrageHealthRatio() {
        return enrageHealthRatio;
    }

    /** How much of the tell is left while the boss is wounded. */
    public float enrageTellScale() {
        return enrageTellScale;
    }

    public boolean doubleStrike() {
        return doubleStrike;
    }

    /** Hits one cycle lands. */
    public int hits() {
        return doubleStrike ? 2 : 1;
    }

    /**
     * Damage per hit. The cycle multiplier is shared out over the hits, so
     * {@code specialDamageMultiplier() x hits() == attackIntervalMultiplier()} for every script and no script can
     * ever raise the damage of one special above the reference fight.
     */
    public float specialDamageMultiplier() {
        return cycleMultiplier / hits();
    }

    /** True while this script's enrage window is open. */
    public boolean enraged(Boss boss) {
        return enrageHealthRatio > 0f && boss != null && boss.maxHealth > 0f
            && boss.health / boss.maxHealth <= enrageHealthRatio;
    }

    /** The warning a boss gives right now. Pinned at the reference length for the whole roster. */
    public float currentTelegraphSeconds(Boss boss) {
        return telegraphSeconds;
    }

    /** How large the warning is drawn right now: an enraged boss draws a smaller tell, never a shorter one. */
    public float currentTellScale(Boss boss) {
        return enraged(boss) ? telegraphScale * enrageTellScale : telegraphScale;
    }

    /**
     * Cooldown armed when a special lands. The warning rides *inside* this cycle (the pending telegraph already
     * drains the cooldown while it hangs), so the cycle is the identity's cooldown times the script's tempo and
     * the warning never adds or removes damage — it only moves the moment the damage lands.
     */
    public float armedCooldownSeconds(float typeCooldownSeconds) {
        return typeCooldownSeconds * cycleMultiplier;
    }
}
