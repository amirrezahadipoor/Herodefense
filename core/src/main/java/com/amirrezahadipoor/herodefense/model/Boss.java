package com.amirrezahadipoor.herodefense.model;

/** Milestone enemy with a distinct behavior and animation identity. */
public final class Boss extends Enemy {
    public String bossType = "ANCIENT_GOLEM";
    public String uniqueAttack = "GROUND_SLAM";
    /** Fight script name from {@code BossFightScript}; unknown names fall back to the measured fight. */
    public String fightScript = "MEASURED";
    public float specialCooldownSeconds;
    public float specialAnimationSeconds;
    /** A telegraph is counting down; the special lands when it reaches zero. */
    public boolean specialPending;
    /** Trigger-time dodge dice for the pending special; the wyrm spends both. */
    public float specialPendingRollA;
    public float specialPendingRollB;
    /** Presentation-only flag so a loaded save never replays the arrival shockwave. */
    public boolean entrancePresented;
    public int specialUseCount;
    public int bossNumber;
    /** The Hollow's half-health line has been spoken for this fight (roadmap ST4). */
    public boolean halfBeatSpoken;
    /** The enrage-crossing evolution burst has fired for this fight (roadmap C2). */
    public boolean evolutionPresented;
    /**
     * The ground the pending special will cover, planted when the telegraph starts and read when it ends
     * (audit item 2). A special is a zone now, not a guaranteed hit: where the hero stands at detonation decides
     * whether it lands at all, which is what turns "step or brace" into one decision with two answers.
     */
    public float specialZoneX;
    public float specialZoneY;
    /** Circle radius; ignored by a cone. */
    public float specialZoneRadius;
    /** True for the sweep identities, whose zone is an arc from the boss rather than a circle. */
    public boolean specialZoneCone;
    public float specialZoneAngleRadians;
    public float specialZoneHalfAngle;
    public float specialZoneReach;
    /** Presentation-only: how long the "the zone missed" flash has left to draw. */
    public float specialMissFlashSeconds;

    public Boss() {
        super();
    }

    public Boss(long id, String bossType, float x, float y, int bossNumber) {
        super(id, bossType, x, y);
        this.bossType = bossType;
        this.bossNumber = bossNumber;
    }

    public BossType bossDefinition() {
        try {
            if (bossType == null) return BossType.ANCIENT_GOLEM;
            return BossType.valueOf(bossType);
        } catch (IllegalArgumentException ignored) {
            return BossType.ANCIENT_GOLEM;
        }
    }
}
