package com.amirrezahadipoor.herodefense.model;

import com.amirrezahadipoor.herodefense.WorldLayout;

/**
 * The Elf defender: born at the arena centre, and able to step inside {@code WorldLayout}'s walkable band on a
 * per-wave budget (roadmap A1).
 *
 * <p>Stepping is a budget rather than a speed limit because a speed limit cannot be both. The slowest creature in
 * the game walks at 28 units a second, so a Hero slow enough never to be kited with would also be too slow to be
 * worth a gesture; a Hero fast enough to matter outruns half the roster forever. What a budget gives instead is a
 * burst -- 260 units a wave at 165 units a second, about a second and a half -- which is a flank choice and a step
 * out of a melee swing, and nothing that can be sustained across a two-hour run.
 *
 * <p>What it is not, and this is the boundary the coaching and the docs have to keep: a boss's special lands
 * wherever the Hero is standing when the telegraph ends. {@code BossSpecialAttackSystem} applies those hits
 * through the damage pipeline without a position test, so stepping is not a dodge against the four bosses. It is
 * a dodge against everything that has to close to melee range first, which is every regular enemy in the game and
 * a boss's ordinary swing. Roadmap A5 is the item that would change that, and until it is decided nothing may
 * claim otherwise.
 */
public final class Hero extends ArenaEntity {
    /** Units of stepping one wave grants; refilled by {@code HeroMovementSystem.beginWave}. */
    public static final float WAVE_STEP_BUDGET = 260f;

    public float health = HeroStats.BASE_MAX_HEALTH;
    public float maxHealth = HeroStats.BASE_MAX_HEALTH;
    public HeroStats stats = new HeroStats();
    public float attackCooldownSeconds;
    /** Verdant Oath buff: seconds of bonus lifesteal left from the last auto-potion. */
    public float mythicLifestealRemainingSeconds;
    /** Landed hits taken this run; Bark of the First Root heals on every tenth. */
    public int mythicHitsTaken;
    public long currentTargetId = -1L;
    public HeroAnimationState animationState = HeroAnimationState.IDLE;
    public float animationStateSeconds;
    public boolean alive = true;
    /** Where the player's drag last asked the Hero to stand, in world units. */
    public float moveTargetX = WorldLayout.HERO_CENTER_X;
    public float moveTargetY = WorldLayout.HERO_CENTER_Y;
    /** True only while a drag order is live; with no order the Hero holds the line exactly where it stands. */
    public boolean moveOrderActive;
    /** Units of stepping left this wave. At zero the Hero is rooted until the next wave begins. */
    public float stepBudgetUnits = WAVE_STEP_BUDGET;
    /** Seconds of brace left (roadmap A2); zero means the shield is down. */
    public float braceRemainingSeconds;
    /** Seconds until the shield may be raised again; ticks whether or not the brace is up. */
    public float braceCooldownSeconds;

    public Hero() {
        super();
    }

    public Hero(long id, float centerX, float centerY) {
        super(id, centerX, centerY);
    }

    /**
     * Puts the Hero somewhere the game decided rather than somewhere the player asked for: a ceremony pose, a
     * repair after loading a save, or the balance simulator's rooted policy. A pending step order is about a
     * place the Hero no longer is, so the anchor voids it instead of resuming it. The budget is left alone --
     * {@code HeroMovementSystem.beginWave} owns that, and an anchor mid-wave must not hand stepping back.
     */
    public void keepAt(float centerX, float centerY) {
        x = centerX;
        y = centerY;
        moveOrderActive = false;
    }

    public float damagePerAttack() {
        return stats.damage();
    }

    public float attackIntervalSeconds() {
        return stats.attackIntervalSeconds();
    }

    public float dropChanceMultiplier() {
        return stats.dropChanceMultiplier();
    }

    public float dodgeChance() {
        return stats.dodgeChance();
    }

    public IncomingHitResult receiveIncomingHit(float amount, float dodgeRoll) {
        return receiveIncomingHit(amount, dodgeRoll, dodgeChance());
    }

    public IncomingHitResult receiveIncomingHit(
        float amount,
        float dodgeRoll,
        float effectiveDodgeChance
    ) {
        if (!alive || amount <= 0f) {
            return IncomingHitResult.IGNORED;
        }
        if (dodgeRoll < 0f || dodgeRoll >= 1f || Float.isNaN(dodgeRoll)) {
            throw new IllegalArgumentException("Dodge roll must be in [0, 1)");
        }
        if (dodgeRoll < Math.max(0f, Math.min(1f, effectiveDodgeChance))) {
            return IncomingHitResult.DODGED;
        }
        health = Math.max(0f, health - amount);
        if (health == 0f) {
            alive = false;
            active = false;
            beginDeathAnimation();
            return IncomingHitResult.KILLED;
        }
        beginHitAnimation();
        return IncomingHitResult.DAMAGED;
    }

    public void beginIdleAnimation() {
        if (alive) {
            animationState = HeroAnimationState.IDLE;
            animationStateSeconds = 0f;
        }
    }

    public void beginAttackAnimation() {
        if (alive && animationState != HeroAnimationState.HIT) {
            animationState = HeroAnimationState.ATTACK;
            animationStateSeconds = 0f;
        }
    }

    public void beginHitAnimation() {
        if (alive) {
            animationState = HeroAnimationState.HIT;
            animationStateSeconds = 0f;
        }
    }

    public void beginDeathAnimation() {
        animationState = HeroAnimationState.DEATH;
        animationStateSeconds = 0f;
    }

    /** Repairs loaded stats and synchronizes derived HP without granting a heal. */
    public void validateAndRepair() {
        if (stats == null) {
            stats = new HeroStats();
        }
        stats.validateAndRepair();
        maxHealth = stats.maxHealth();
        health = Math.max(0f, Math.min(maxHealth, health));
        alive = health > 0f;
        active = alive;
        if (animationState == null) {
            animationState = alive ? HeroAnimationState.IDLE : HeroAnimationState.DEATH;
        }
        if (!alive) {
            animationState = HeroAnimationState.DEATH;
        }
        animationStateSeconds = Math.max(0f, animationStateSeconds);
        if (!Float.isFinite(moveTargetX) || !Float.isFinite(moveTargetY)) {
            moveTargetX = x;
            moveTargetY = y;
        }
        moveOrderActive = moveOrderActive && alive;
        stepBudgetUnits = Float.isFinite(stepBudgetUnits)
            ? Math.max(0f, Math.min(WAVE_STEP_BUDGET, stepBudgetUnits))
            : WAVE_STEP_BUDGET;
        braceRemainingSeconds = Float.isFinite(braceRemainingSeconds)
            ? Math.max(0f, Math.min(BraceLimits.BRACE_SECONDS, braceRemainingSeconds))
            : 0f;
        braceCooldownSeconds = Float.isFinite(braceCooldownSeconds)
            ? Math.max(0f, Math.min(BraceLimits.COOLDOWN_SECONDS, braceCooldownSeconds))
            : 0f;
        if (!alive) {
            braceRemainingSeconds = 0f;
        }
    }
}
