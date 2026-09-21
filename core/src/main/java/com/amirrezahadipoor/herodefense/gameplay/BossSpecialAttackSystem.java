package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Executes the eight authored boss specials, all through Dodge-aware damage, and lets the encounter's
 * {@link BossFightScript} decide the tempo, the telegraph length and whether a special lands twice (roadmap R3.2).
 * D3 added 4 new bosses — their specials reuse existing damage patterns with identity colors.
 */
public final class BossSpecialAttackSystem {
    public static final float TELEGRAPH_SECONDS = 0.5f;
    private final HeroDamageSystem heroDamageSystem;
    private final DifficultyCurve curve = new DifficultyCurve();
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
                    if (type == BossType.EMBER_WYRM || type == BossType.STORM_COLOSSUS) {
                        boss.specialPendingRollB = state.nextCombatRandomFloat();
                    }
                    if (type == BossType.THORN_MATRIARCH || type == BossType.BLOODROOT_AVATAR) {
                        state.hero.attackCooldownSeconds = Math.max(
                            state.hero.attackCooldownSeconds, 2f);
                    }
                    if (type == BossType.VOID_KNIGHT || type == BossType.SHADOW_LICH) {
                        chargeToMeleeRange(state, boss);
                    }
                    boss.specialPending = true;
                    telegraphsStarted++;
                    boss.specialAnimationSeconds = script.currentTelegraphSeconds(boss);
                }
            }
        }
    }

    public int consumeTelegraphsStarted() {
        int count = telegraphsStarted;
        telegraphsStarted = 0;
        return count;
    }

    private void execute(GameState state, Boss boss, BossFightScript script) {
        executeOnce(state, boss, script);
        if (script.doubleStrike() && state.hero.alive) {
            executeOnce(state, boss, script);
        }
    }

    private void executeOnce(GameState state, Boss boss, BossFightScript script) {
        // The base is a share of the expected bar, not a multiple of the boss's melee swing (audit item 1): a
        // telegraphed special is a different kind of event from a contact hit, and pricing it off the contact
        // damage is what left the game's loudest warning attached to 0.9% of the hero's health. The per-boss
        // multipliers below are the encounter's identity and are unchanged; only their base moved.
        float specialBase = curve.bossSpecialDamage(state.waveNumber, state.ascensionTier);
        switch (boss.bossDefinition()) {
            case ANCIENT_GOLEM, FROST_TITAN -> heroDamageSystem.applyIncomingHitWithRoll(
                state, specialBase * 1.6f * script.specialDamageMultiplier(), boss.specialPendingRollA);
            case THORN_MATRIARCH, BLOODROOT_AVATAR -> heroDamageSystem.applyIncomingHitWithRoll(
                state, specialBase * 0.5f * script.specialDamageMultiplier(), boss.specialPendingRollA);
            case EMBER_WYRM, STORM_COLOSSUS -> {
                heroDamageSystem.applyIncomingHitWithRoll(
                    state, specialBase * 0.55f * script.specialDamageMultiplier(), boss.specialPendingRollA);
                heroDamageSystem.applyIncomingHitWithRoll(
                    state, specialBase * 0.55f * script.specialDamageMultiplier(), boss.specialPendingRollB);
            }
            case VOID_KNIGHT, SHADOW_LICH -> heroDamageSystem.applyIncomingHitWithRoll(
                state, specialBase * 1.25f * script.specialDamageMultiplier(), boss.specialPendingRollA);
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
            case ANCIENT_GOLEM, FROST_TITAN -> 145f;
            case THORN_MATRIARCH, BLOODROOT_AVATAR -> 210f;
            case EMBER_WYRM, STORM_COLOSSUS -> 250f;
            case VOID_KNIGHT, SHADOW_LICH -> 480f;
        };
    }

    private static float cooldown(BossType type) {
        return switch (type) {
            case ANCIENT_GOLEM, FROST_TITAN -> 5.5f;
            case THORN_MATRIARCH, BLOODROOT_AVATAR -> 5f;
            case EMBER_WYRM, STORM_COLOSSUS -> 4.5f;
            case VOID_KNIGHT, SHADOW_LICH -> 4f;
        };
    }
}
