package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

final class BossSpecialAttackSystemTest {
    private final BossFactory factory = new BossFactory();
    private final BossSpecialAttackSystem specials = new BossSpecialAttackSystem(new HeroDamageSystem());

    @Test
    void golemGroundSlamDealsOneHeavyHit() {
        Scenario scenario = scenario(BossType.ANCIENT_GOLEM, 0f);
        specials.update(scenario.state, 0f);
        detonate(scenario);
        assertEquals(984f, scenario.state.hero.health);
        assertEquals(1, scenario.boss.specialUseCount);
    }

    @Test
    void matriarchThornCageDamagesAndDelaysHeroAttack() {
        Scenario scenario = scenario(BossType.THORN_MATRIARCH, 0f);
        specials.update(scenario.state, 0f);
        assertEquals(1_000f, scenario.state.hero.health);
        assertEquals(2f, scenario.state.hero.attackCooldownSeconds);
        detonate(scenario);
        assertEquals(995f, scenario.state.hero.health);
        assertEquals(2f, scenario.state.hero.attackCooldownSeconds);
    }

    @Test
    void wyrmFlameSweepMakesTwoSeparateDodgeAwareHits() {
        Scenario scenario = scenario(BossType.EMBER_WYRM, 0f);
        long before = scenario.state.combatRandomState;
        specials.update(scenario.state, 0f);
        detonate(scenario);
        assertEquals(989f, scenario.state.hero.health);
        assertTrue(before != scenario.state.combatRandomState);
    }

    @Test
    void knightVoidChargeClosesDistanceBeforeStriking() {
        Scenario scenario = scenario(BossType.VOID_KNIGHT, 300f);
        specials.update(scenario.state, 0f);
        detonate(scenario);
        float distance = (float) Math.sqrt(scenario.boss.distanceSquaredTo(
            scenario.state.hero.x, scenario.state.hero.y
        ));
        assertEquals(scenario.boss.attackRange, distance, 0.001f);
        assertEquals(987.5f, scenario.state.hero.health);
    }

    @Test
    void triggerStartsATelegraphAndDamageLandsOnlyWhenItEnds() {
        Scenario scenario = scenario(BossType.ANCIENT_GOLEM, 0f);
        specials.update(scenario.state, 0f);
        assertEquals(1_000f, scenario.state.hero.health);
        assertTrue(scenario.boss.specialPending);
        assertEquals(BossSpecialAttackSystem.TELEGRAPH_SECONDS,
            scenario.boss.specialAnimationSeconds, 1e-6f);
        assertEquals(0, scenario.boss.specialUseCount);
        assertTrue(scenario.boss.specialCooldownSeconds <= 0f);
        detonate(scenario);
        assertEquals(984f, scenario.state.hero.health);
        assertFalse(scenario.boss.specialPending);
        assertEquals(0f, scenario.boss.specialAnimationSeconds, 1e-6f);
        assertEquals(1, scenario.boss.specialUseCount);
        assertTrue(scenario.boss.specialCooldownSeconds > 0f);
    }

    @Test
    void stunFreezesTheTelegraphInsteadOfCancelingIt() {
        Scenario scenario = scenario(BossType.ANCIENT_GOLEM, 0f);
        specials.update(scenario.state, 0f);
        scenario.boss.stunRemainingSeconds = 10f;
        specials.update(scenario.state, BossSpecialAttackSystem.TELEGRAPH_SECONDS);
        assertEquals(1_000f, scenario.state.hero.health);
        assertTrue(scenario.boss.specialPending);
        assertEquals(BossSpecialAttackSystem.TELEGRAPH_SECONDS,
            scenario.boss.specialAnimationSeconds, 1e-6f);
        scenario.boss.stunRemainingSeconds = 0f;
        detonate(scenario);
        assertEquals(984f, scenario.state.hero.health);
    }

    @Test
    void dodgeDiceRollAtTriggerSoDetonationSpendsNoRandomness() {
        Scenario oneRoll = scenario(BossType.ANCIENT_GOLEM, 0f);
        long before = oneRoll.state.combatRandomState;
        specials.update(oneRoll.state, 0f);
        assertTrue(before != oneRoll.state.combatRandomState);
        long atDetonation = oneRoll.state.combatRandomState;
        detonate(oneRoll);
        assertEquals(atDetonation, oneRoll.state.combatRandomState);

        Scenario wyrm = scenario(BossType.EMBER_WYRM, 0f);
        long wyrmBefore = wyrm.state.combatRandomState;
        specials.update(wyrm.state, 0f);
        long wyrmTrigger = wyrm.state.combatRandomState;
        assertTrue(wyrmBefore != wyrmTrigger);
        detonate(wyrm);
        assertEquals(wyrmTrigger, wyrm.state.combatRandomState);
    }

    @Test
    void knightDashesAtTriggerWhileDamageWaitsForDetonation() {
        Scenario scenario = scenario(BossType.VOID_KNIGHT, 300f);
        specials.update(scenario.state, 0f);
        float distance = (float) Math.sqrt(scenario.boss.distanceSquaredTo(
            scenario.state.hero.x, scenario.state.hero.y
        ));
        assertEquals(scenario.boss.attackRange, distance, 0.001f);
        assertEquals(1_000f, scenario.state.hero.health);
    }

    private void detonate(Scenario scenario) {
        specials.update(scenario.state, BossSpecialAttackSystem.TELEGRAPH_SECONDS);
    }

    @Test
    void everyEncounterWarnsForTheReferenceWindow() {
        // The warning *length* is pinned at the reference for the whole roster (see BossFightScript and the
        // R3.2 roadmap entry): it is the one axis the balance sweep feels, so the scripts must not move it.
        for (int bossNumber : new int[] {1, 5, 8}) {
            Scenario scenario = encounter(BossType.ANCIENT_GOLEM, bossNumber);
            specials.update(scenario.state, 0f);
            assertEquals(BossSpecialAttackSystem.TELEGRAPH_SECONDS, scenario.boss.specialAnimationSeconds, 1e-6f,
                "encounter " + bossNumber + " must warn for the reference window");

            specials.update(scenario.state, BossSpecialAttackSystem.TELEGRAPH_SECONDS * 0.5f);
            assertEquals(1_000f, scenario.state.hero.health, "and it must not land early");

            specials.update(scenario.state, BossSpecialAttackSystem.TELEGRAPH_SECONDS * 0.5f);
            assertTrue(scenario.state.hero.health < 1_000f, "it lands exactly when the window ends");
        }
    }

    @Test
    void aTwinTelegraphScriptLandsItsSpecialTwice() {
        Scenario single = encounter(BossType.ANCIENT_GOLEM, 6);
        specials.update(single.state, 0f);
        specials.update(single.state, BossFightScript.TWIN_TELEGRAPH.telegraphSeconds());
        assertEquals(1, single.boss.specialUseCount, "one telegraph is still one use of the special");

        Scenario reference = scenario(BossType.ANCIENT_GOLEM, 0f);
        specials.update(reference.state, 0f);
        detonate(reference);

        float singleGolemHit = 1_000f - reference.state.hero.health;
        float twinHit = singleGolemHit * BossFightScript.TWIN_TELEGRAPH.specialDamageMultiplier();
        assertEquals(1_000f - twinHit * 2f, single.state.hero.health, 1e-4f,
            "one telegraph, two damage applications of the script's per-hit damage");
    }

    @Test
    void anEnragedScriptChangesNothingTheSweepCanFeel() {
        // The enrage reaction is a readability beat: the tell is drawn smaller. It must not move the warning
        // length, the cycle or the damage, because those are what TrialSimulationTest and AscensionGateTest read.
        Scenario healthy = encounter(BossType.ANCIENT_GOLEM, 4);
        specials.update(healthy.state, 0f);
        float healthyWarning = healthy.boss.specialAnimationSeconds;
        assertEquals(BossFightScript.ENRAGED_HEART.telegraphSeconds(), healthyWarning, 1e-6f);
        specials.update(healthy.state, healthyWarning);
        assertEquals(BossFightScript.ENRAGED_HEART.currentTellScale(healthy.boss),
            BossFightScript.ENRAGED_HEART.telegraphScale(), 1e-6f, "a healthy boss draws its full tell");

        Scenario wounded = encounter(BossType.ANCIENT_GOLEM, 4);
        wounded.boss.health = wounded.boss.maxHealth * 0.3f;
        specials.update(wounded.state, 0f);
        assertEquals(healthyWarning, wounded.boss.specialAnimationSeconds, 1e-6f,
            "a wounded boss warns for exactly as long");
        specials.update(wounded.state, healthyWarning);

        assertEquals(healthy.boss.specialCooldownSeconds, wounded.boss.specialCooldownSeconds, 1e-4f,
            "and arms exactly the same cycle");
        assertTrue(BossFightScript.ENRAGED_HEART.currentTellScale(wounded.boss)
                < BossFightScript.ENRAGED_HEART.currentTellScale(healthy.boss),
            "the reaction the player actually sees is the smaller tell");
    }

    private Scenario encounter(BossType type, int bossNumber) {
        GameState state = GameState.newRun(bossNumber + 200L);
        state.hero.maxHealth = 1_000f;
        state.hero.health = 1_000f;
        Boss boss = factory.create(state, type, state.hero.x, state.hero.y, bossNumber, 0);
        boss.damage = 10f;
        boss.specialCooldownSeconds = 0f;
        state.aliveBosses.add(boss);
        return new Scenario(state, boss);
    }

    private Scenario scenario(BossType type, float xOffset) {
        GameState state = GameState.newRun(type.ordinal() + 100L);
        state.hero.maxHealth = 1_000f;
        state.hero.health = 1_000f;
        // Encounter 1 carries the MEASURED script on purpose: these cases describe the identity's own
        // mechanics, and the script behaviour has its own tests below.
        Boss boss = factory.create(
            state, type, state.hero.x + xOffset, state.hero.y, 1, 0
        );
        boss.damage = 10f;
        state.aliveBosses.add(boss);
        return new Scenario(state, boss);
    }

    private record Scenario(GameState state, Boss boss) {
    }
}
