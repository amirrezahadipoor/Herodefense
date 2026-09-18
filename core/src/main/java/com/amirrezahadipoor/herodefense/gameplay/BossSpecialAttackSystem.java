package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Executes the four authored boss specials, all through Dodge-aware damage, and lets the encounter's
 * {@link BossFightScript} decide the tempo, the telegraph length and whether a special lands twice (roadmap R3.2).
 */
public final class BossSpecialAttackSystem {
    /** Warning window between a special's trigger and its damage landing. */
    public static final float TELEGRAPH_SECONDS = 0.5f;
    private final HeroDamageSystem heroDamageSystem;
    /**
     * Telegraphs started since the last read (roadmap R6.2). The warning a boss gives before a special was
     * visible and silent; this is the same signal the fight renderer already draws from, published for audio
     * rather than for the screen, and read-once so a frame cannot play it twice.
     */
    private int telegraphsStarted;

    public BossSpecialAttackSystem(HeroDamageSystem heroDamageSystem) {
        this.heroDamageSystem = heroDamageSystem;
    }

    public void update(GameState state, float deltaSeconds) {
        if (state == null || state.hero == null || !state.hero.alive || deltaSeconds < 0f) {
            return;
        }
        for (Boss boss : state.aliveBosses) {
            if (boss == null || !boss.alive || !boss.active || !state.hero.alive) {
                continue;
            }
            BossFightScript script = BossFightScript.of(boss);
            boss.specialCooldownSeconds -= deltaSeconds;
            if (boss.specialPending) {
                if (!boss.stunned()) {
                    boss.specialAnimationSeconds -= deltaSeconds;
                    if (boss.specialAnimationSeconds <= 0f) {
                        boss.specialAnimationSeconds = 0f;
                        boss.specialPending = false;
                        execute(state, boss, script);
                        boss.specialCooldownSeconds +=
                            script.armedCooldownSeconds(cooldown(boss.bossDefinition()));
                        boss.specialUseCount++;
                    }
                }
            } else {
                boss.specialAnimationSeconds = Math.max(0f, boss.specialAnimationSeconds - deltaSeconds);
                float triggerRange = triggerRange(boss.bossDefinition());
                if (boss.specialCooldownSeconds <= 0f
                    && !boss.stunned()
                    && boss.distanceSquaredTo(state.hero.x, state.hero.y)
                        <= triggerRange * triggerRange) {
                    BossType type = boss.bossDefinition();
                    boss.specialPendingRollA = state.nextCombatRandomFloat();
                    if (type == BossType.EMBER_WYRM) {
                        boss.specialPendingRollB = state.nextCombatRandomFloat();
                    }
                    if (type == BossType.THORN_MATRIARCH) {
                        state.hero.attackCooldownSeconds = Math.max(
                            state.hero.attackCooldownSeconds, 2f);
                    }
                    if (type == BossType.VOID_KNIGHT) {
                        chargeToMeleeRange(state, boss);
                    }
                    boss.specialPending = true;
                    telegraphsStarted++;
                    boss.specialAnimationSeconds = script.currentTelegraphSeconds(boss);
                }
            }
        }
    }

    /** Telegraphs begun since this was last called, then cleared. */
    public int consumeTelegraphsStarted() {
        int count = telegraphsStarted;
        telegraphsStarted = 0;
        return count;
    }

    /** One special of the boss's identity, repeated when the encounter's script strikes twice. */
    private void execute(GameState state, Boss boss, BossFightScript script) {
        executeOnce(state, boss, script);
        if (script.doubleStrike() && state.hero.alive) {
            executeOnce(state, boss, script);
        }
    }

    private void executeOnce(GameState state, Boss boss, BossFightScript script) {
        switch (boss.bossDefinition()) {
            case ANCIENT_GOLEM -> heroDamageSystem.applyIncomingHitWithRoll(
                state, boss.damage * 1.6f * script.specialDamageMultiplier(), boss.specialPendingRollA);
            case THORN_MATRIARCH -> heroDamageSystem.applyIncomingHitWithRoll(
                state, boss.damage * 0.5f * script.specialDamageMultiplier(), boss.specialPendingRollA);
            case EMBER_WYRM -> {
                heroDamageSystem.applyIncomingHitWithRoll(
                    state, boss.damage * 0.55f * script.specialDamageMultiplier(), boss.specialPendingRollA);
                heroDamageSystem.applyIncomingHitWithRoll(
                    state, boss.damage * 0.55f * script.specialDamageMultiplier(), boss.specialPendingRollB);
            }
            case VOID_KNIGHT -> heroDamageSystem.applyIncomingHitWithRoll(
                state, boss.damage * 1.25f * script.specialDamageMultiplier(), boss.specialPendingRollA);
        }
    }

    private static void chargeToMeleeRange(GameState state, Boss boss) {
        float dx = boss.x - state.hero.x;
        float dy = boss.y - state.hero.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0f) {
            dx = 1f;
            dy = 0f;
            length = 1f;
        }
        boss.x = state.hero.x + dx / length * boss.attackRange;
        boss.y = state.hero.y + dy / length * boss.attackRange;
    }

    private static float triggerRange(BossType type) {
        return switch (type) {
            case ANCIENT_GOLEM -> 145f;
            case THORN_MATRIARCH -> 210f;
            case EMBER_WYRM -> 250f;
            case VOID_KNIGHT -> 480f;
        };
    }

    private static float cooldown(BossType type) {
        return switch (type) {
            case ANCIENT_GOLEM -> 5.5f;
            case THORN_MATRIARCH -> 5f;
            case EMBER_WYRM -> 4.5f;
            case VOID_KNIGHT -> 4f;
        };
    }
}
