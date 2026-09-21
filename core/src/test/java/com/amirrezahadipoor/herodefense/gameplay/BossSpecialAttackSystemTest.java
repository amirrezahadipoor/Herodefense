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
        assertEquals(1_000f - specialBase(scenario) * 1.6f, scenario.state.hero.health, 1e-3f);
        assertEquals(1, scenario.boss.specialUseCount);
    }

    @Test
    void matriarchThornCageDamagesAndDelaysHeroAttack() {
        Scenario scenario = scenario(BossType.THORN_MATRIARCH, 0f);
        specials.update(scenario.state, 0f);
        assertEquals(1_000f, scenario.state.hero.health);
        assertEquals(2f, scenario.state.hero.attackCooldownSeconds);
        detonate(scenario);
        assertEquals(1_000f - specialBase(scenario) * 0.5f, scenario.state.hero.health, 1e-3f);
        assertEquals(2f, scenario.state.hero.attackCooldownSeconds);
    }

    @Test
    void wyrmFlameSweepMakesTwoSeparateDodgeAwareHits() {
        Scenario scenario = scenario(BossType.EMBER_WYRM, 0f);
        long before = scenario.state.combatRandomState;
        specials.update(scenario.state, 0f);
        detonate(scenario);
        assertEquals(1_000f - specialBase(scenario) * 1.1f, scenario.state.hero.health, 1e-3f);
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
        assertEquals(1_000f - specialBase(scenario) * 1.25f, scenario.state.hero.health, 1e-3f);
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
        assertEquals(1_000f - specialBase(scenario) * 1.6f, scenario.state.hero.health, 1e-3f);
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
        assertEquals(1_000f - specialBase(scenario) * 1.6f, scenario.state.hero.health, 1e-3f);
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

    /**
     * The anchored special base of the scenario's wave (audit item 1). Damage used to be a multiple of the boss's
     * melee swing, so these assertions could subtract a literal; a special is a share of the expected bar now, and
     * the literals that follow are the encounter multipliers -- which is the part of the special each test is
     * about. The wave-one golem number is pinned, once, in DifficultyCurveTest.
     */
    private static float specialBase(Scenario scenario) {
        return new DifficultyCurve().bossSpecialDamage(scenario.state.waveNumber);
    }

    private void detonate(Scenario scenario) {
        specials.update(scenario.state, BossSpecialAttackSystem.TELEGRAPH_SECONDS);
    }

    /**
     * The telegraph draws a hitbox now, and this is the decision it creates (audit item 2): the same warning, two
     * outcomes, decided by where the hero is standing when it ends.
     */
    @Test
    void theSlamOnlyLandsOnTheGroundItWarnedAbout() {
        Scenario inside = scenario(BossType.ANCIENT_GOLEM, 0f);
        specials.update(inside.state, 0f);
        detonate(inside);
        assertTrue(inside.state.hero.health < 1_000f, "standing in the slam must cost the hero");

        Scenario outside = scenario(BossType.ANCIENT_GOLEM, 0f);
        specials.update(outside.state, 0f);
        // Leave the planted disc by a margin, from wherever the body that slammed happened to be standing.
        outside.state.hero.x = outside.boss.specialZoneX - BossSpecialZone.SLAM_RADIUS - 40f;
        outside.state.hero.y = outside.boss.specialZoneY;
        detonate(outside);
        assertEquals(1_000f, outside.state.hero.health, "leaving the zone must cost nothing");
        assertTrue(outside.boss.specialMissFlashSeconds > 0f,
            "and the miss has to leave a mark on the ground to be readable");
        assertEquals(1, outside.boss.specialUseCount, "a missed special is still spent");
    }

    /** The slam is centred on the body that slammed, so backing off the boss is the answer rather than stepping. */
    @Test
    void theSlamIsCentredOnTheBossAndTheCageUnderTheHero() {
        // Offsets are inside each identity's trigger range -- the golem's is the shortest at 145 units -- so the
        // telegraph really starts and there is a zone to read.
        Scenario slam = scenario(BossType.ANCIENT_GOLEM, 100f);
        specials.update(slam.state, 0f);
        assertEquals(slam.boss.x, slam.boss.specialZoneX, 0.001f);
        assertEquals(slam.boss.y, slam.boss.specialZoneY, 0.001f);
        assertEquals(BossSpecialZone.SLAM_RADIUS, slam.boss.specialZoneRadius, 0.001f);

        Scenario cage = scenario(BossType.THORN_MATRIARCH, 150f);
        specials.update(cage.state, 0f);
        assertEquals(cage.state.hero.x, cage.boss.specialZoneX, 0.001f);
        assertEquals(cage.state.hero.y, cage.boss.specialZoneY, 0.001f);
        assertEquals(BossSpecialZone.PLANTED_RADIUS, cage.boss.specialZoneRadius, 0.001f);
    }

    /**
     * The warning is a question, and a question with one answer is not a decision (audit item 2): every identity's
     * telegraph has to cover the spot the hero was standing on when it appeared -- the stand-off each encounter
     * takes its own special at included -- or the "outside is a full miss" rule is a way to lose to arithmetic
     * rather than to a choice.
     */
    @Test
    void everyWarningCoversTheSpotTheHeroWasStandingOnWhenItAppeared() {
        for (BossType type : BossType.values()) {
            Scenario scenario = scenario(type, type.attackRange() + 5f);
            specials.update(scenario.state, 0f);
            assertTrue(scenario.boss.specialPending, type + " must reach its special from its own stand-off");
            assertTrue(BossSpecialZone.contains(scenario.boss, scenario.state.hero.x, scenario.state.hero.y),
                type + " warned about ground the hero was not standing on");
        }
    }

    /** The sweep is an arc from the boss: sidestep it and it misses, back out along its own axis and it does not. */
    @Test
    void theSweepMissesWhatStandsBesideItAndCatchesWhatItPointsAt() {
        Scenario beside = scenario(BossType.EMBER_WYRM, -200f);
        specials.update(beside.state, 0f);
        assertTrue(beside.boss.specialZoneCone, "the wyrm's special is a cone");
        beside.state.hero.y += 400f;
        detonate(beside);
        assertEquals(1_000f, beside.state.hero.health, "stepping out of the arc must beat it");

        Scenario alongTheAxis = scenario(BossType.EMBER_WYRM, -200f);
        specials.update(alongTheAxis.state, 0f);
        alongTheAxis.state.hero.x -= 120f;
        detonate(alongTheAxis);
        assertTrue(alongTheAxis.state.hero.health < 1_000f,
            "walking backwards along the sweep is still inside it");
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
