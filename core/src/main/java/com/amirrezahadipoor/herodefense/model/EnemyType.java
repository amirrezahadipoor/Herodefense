package com.amirrezahadipoor.herodefense.model;

/**
 * Distinct regular-enemy roles. Every type closes to melee range before attacking.
 *
 * <p>The roster is eight since roadmap R3.4, and the four additions are authored so that <em>seven</em> per-type
 * numbers keep the exact totals the four-role roster had: health 234, damage 56, experience 138, coins 38, speed
 * 242, reach 172 and attack interval 5.10 -- over four roles that was 117 / 28 / 69 / 19 / 242 / 172 / 5.10.
 * {@code EnemyFactoryTest} asserts the totals, so a future tuning pass cannot move the wave's weight by accident.
 * Speed, reach and interval matter as much as health here: the simulators showed that changing only the speed
 * profile moved the late sweep by several percent, which is the kind of drift the bands exist to catch.
 *
 * <p>The four additions do not enter the cycle at wave one. {@code EnemyWaveSpawner.rosterFor} keeps the first ten
 * waves on the four field creatures and only then draws from all eight, because the accepted balance evidence (the
 * two-hundred-wave sweep and the thirty-wave brief sweep) was measured on those openings; deep waves additionally
 * interleave the roster with a coprime stride so no wave opens with a run of heavy bodies.
 *
 * <p>What the additions change is variance: a wave mixes a 17-health wolf with a 46-health brute across eight
 * roles instead of four. That is the point of the item.
 */
public enum EnemyType {
    ROOTLING(20f, 5f, 68f, 38f, 1.15f, 12, 3),
    STONEKIN(34f, 7f, 42f, 44f, 1.40f, 18, 5),
    GLOOM_WOLF(17f, 6f, 96f, 40f, 0.90f, 15, 4),
    FUNGAL_BRUTE(46f, 10f, 36f, 50f, 1.65f, 24, 7),
    /** R3.4: the wiry flanker -- the lightest role that still hits like the middle of the roster. */
    BARK_STALKER(26f, 6f, 74f, 42f, 1.05f, 16, 4),
    /** R3.4: the fastest enemy in the game, and the hardest single hit of the light roles. */
    SAP_HOUND(22f, 8f, 100f, 36f, 0.90f, 14, 5),
    /** R3.4: the shielded mid-weight -- slow, but it reaches further than anything except the brute. */
    HUSK_WARDEN(30f, 7f, 40f, 46f, 1.35f, 20, 6),
    /** R3.4: the slow thorned mass, second only to the brute in health. */
    BRAMBLE_THRALL(39f, 7f, 28f, 48f, 1.80f, 19, 4);

    private final float baseHealth;
    private final float baseDamage;
    private final float movementSpeed;
    private final float attackRange;
    private final float attackIntervalSeconds;
    private final int experienceReward;
    private final int coinReward;

    EnemyType(
        float baseHealth,
        float baseDamage,
        float movementSpeed,
        float attackRange,
        float attackIntervalSeconds,
        int experienceReward,
        int coinReward
    ) {
        this.baseHealth = baseHealth;
        this.baseDamage = baseDamage;
        this.movementSpeed = movementSpeed;
        this.attackRange = attackRange;
        this.attackIntervalSeconds = attackIntervalSeconds;
        this.experienceReward = experienceReward;
        this.coinReward = coinReward;
    }

    public float baseHealth() {
        return baseHealth;
    }

    public float baseDamage() {
        return baseDamage;
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

    public int experienceReward() {
        return experienceReward;
    }

    public int coinReward() {
        return coinReward;
    }

    public boolean isMelee() {
        return true;
    }

    public String assetKey() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
