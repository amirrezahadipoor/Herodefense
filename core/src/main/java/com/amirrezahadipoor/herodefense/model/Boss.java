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
