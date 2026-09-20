package com.amirrezahadipoor.herodefense.model;

/** A melee attacker moving from a spawn edge toward the fixed Hero. */
public class Enemy extends ArenaEntity {
    public String enemyType = "ROOTLING";
    public float health;
    public float maxHealth;
    public float damage;
    public float movementSpeed;
    public float attackRange;
    public float attackIntervalSeconds;
    public float attackCooldownSeconds;
    public int spawnLane;
    public boolean itemDropRolled;
    public boolean potionDropRolled;
    public boolean killRewardsGranted;
    public boolean defeatParticlesEmitted;
    public boolean alive = true;
    /** Seconds this enemy is frozen by a stunning arrow; it neither moves nor swings. */
    public float stunRemainingSeconds;
    /** Seconds the Crown of the Hollow Eye's mark lasts; marked foes take +25%. */
    public float markRemainingSeconds;
    /** Tap-to-focus window left (roadmap R3.1); independent from the Crown mythic mark above. */
    public float focusMarkSeconds;
    /** Brief white hit-flash after taking damage for readability. */
    public float hitFlashSeconds;
    /**
     * Silent Rootling watcher (Codex entry 5, "The Quiet Ones"): stands at the tree line and
     * never moves, attacks, or pays out; targeting, wave-clear counts, and drops all skip it.
     */
    public boolean silentWatcher;
    /** Elite affix id (see {@link EliteAffix}); null for regulars. */
    public String eliteAffix;
    /** Affix clock driving the rootward shield cycle and weeping trail cadence. */
    public float affixTimerSeconds;
    /** Remaining rootward shield time; a shielded elite takes no damage. */
    public float affixShieldRemainingSeconds;
    /** The elite's death has been resolved (blight blast, if any, already fired). */
    public boolean affixResolved;
    /**
     * Roadmap A3: the late-wave brute's berserk latch. Nothing in the game heals a regular enemy, so the latch
     * only ever sets, and the multipliers are applied once at the transition rather than recomputed per tick --
     * a per-tick recompute would have to remember the spawn-time trial and omen multipliers it had already
     * folded into {@code movementSpeed}.
     */
    public boolean enraged;

    /**
     * Roadmap A3, second half: the thrall has split into fragments (once per body), and the hound's lunge cycle
     * timer. {@code lungeSeconds} is the whole state machine: negative counts the cooldown up to zero, then the
     * same float walks through windup, dash and the hand-off to {@code stunRemainingSeconds} for the recovery.
     * {@code lungeBaseSpeed} remembers the speed the wave gave the hound, because the phases overwrite it.
     */
    public boolean splitSpawned;
    public float lungeSeconds;
    public float lungeBaseSpeed;
    /** The elite kill has been claimed for counts, codex, and its lore fragment. */
    public boolean eliteKillClaimed;
    /** Greetings received from the player; three and a silent watcher departs (roadmap ST3). */
    public int spareTouches;
    /** A dying blightburst has thinned below its warn threshold; the telegraph is owed. */
    public boolean blastWarned;
    /** A shared copy of a rootward elite's ward: damage lands at a reduced share while it holds. */
    public float affixWardRemainingSeconds;
    /** The share of damage a ward-carrying body takes while its shared ward holds. */
    public static final float SHARED_WARD_MULTIPLIER = 0.96f;
    /** A late-wave flanker: runs past the lane, bites the nearest planted tree, rejoins (roadmap A3). */
    public boolean flanker;
    /** This flanker's live target; recomputed every tick, so it never persists. */
    public boolean flankTargetValid;
    public float flankTargetX;
    public float flankTargetY;

    public Enemy() {
        super();
    }

    public Enemy(long id, String enemyType, float x, float y) {
        super(id, x, y);
        this.enemyType = enemyType;
    }

    public EnemyType type() {
        try {
            if (enemyType == null) return EnemyType.ROOTLING;
            return EnemyType.valueOf(enemyType);
        } catch (IllegalArgumentException ignored) {
            return EnemyType.ROOTLING;
        }
    }

    public boolean stunned() {
        return alive && stunRemainingSeconds > 0f;
    }

    public void receiveDamage(float amount) {
        if (!alive || amount <= 0f) {
            return;
        }
        if (affixShieldRemainingSeconds > 0f) {
            return;
        }
        float taken = affixWardRemainingSeconds > 0f ? amount * SHARED_WARD_MULTIPLIER : amount;
        health = Math.max(0f, health - taken);
        if (health == 0f) {
            alive = false;
            active = false;
        }
    }
}
