package com.amirrezahadipoor.herodefense.model;

/** Eight authored boss identities — D3 closes the four-boss gap across forty encounters. */
public enum BossType {
    ANCIENT_GOLEM("GROUND_SLAM", 34f, 62f, 1.8f, 0.62f, 0.66f, 0.78f),
    THORN_MATRIARCH("THORN_CAGE", 46f, 150f, 1.6f, 0.35f, 0.85f, 0.45f),
    EMBER_WYRM("FLAME_SWEEP", 58f, 190f, 1.4f, 1.00f, 0.45f, 0.15f),
    VOID_KNIGHT("VOID_CHARGE", 72f, 74f, 1.2f, 0.65f, 0.40f, 1.00f),
    FROST_TITAN("FROST_NOVA", 38f, 70f, 1.7f, 0.40f, 0.70f, 1.00f),
    SHADOW_LICH("SOUL_DRAIN", 50f, 160f, 1.5f, 0.50f, 0.30f, 0.80f),
    STORM_COLOSSUS("THUNDER_CRASH", 40f, 80f, 1.6f, 0.90f, 0.85f, 0.20f),
    BLOODROOT_AVATAR("ROOT_WRATH", 36f, 140f, 1.9f, 0.80f, 0.20f, 0.20f);

    private final String uniqueAttack;
    private final float movementSpeed;
    private final float attackRange;
    private final float attackIntervalSeconds;
    private final float telegraphRed;
    private final float telegraphGreen;
    private final float telegraphBlue;

    BossType(
        String uniqueAttack,
        float movementSpeed,
        float attackRange,
        float attackIntervalSeconds,
        float telegraphRed,
        float telegraphGreen,
        float telegraphBlue
    ) {
        this.uniqueAttack = uniqueAttack;
        this.movementSpeed = movementSpeed;
        this.attackRange = attackRange;
        this.attackIntervalSeconds = attackIntervalSeconds;
        this.telegraphRed = telegraphRed;
        this.telegraphGreen = telegraphGreen;
        this.telegraphBlue = telegraphBlue;
    }

    public String uniqueAttack() {
        return uniqueAttack;
    }

    public float movementSpeed() {
        return movementSpeed;
    }

    public float attackRange() {
        return attackRange;
    }

    public float attackIntervalSeconds() {
        return attackIntervalSeconds;
    }

    /** Identity color of the boss's ground telegraph warning. */
    public float telegraphRed() {
        return telegraphRed;
    }

    /** Identity color of the boss's ground telegraph warning. */
    public float telegraphGreen() {
        return telegraphGreen;
    }

    /** Identity color of the boss's ground telegraph warning. */
    public float telegraphBlue() {
        return telegraphBlue;
    }

    public String assetKey() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
