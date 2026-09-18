package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameMode;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

/**
 * The telegraph contract (roadmap R4.2): everything the game promises a player about a boss special, asserted for
 * the whole roster at every rung of the ladder and in both vigils.
 *
 * <p>{@code BossSpecialAttackSystemTest} proves each rule once, in isolation, on one boss and one script. This file
 * is the contract: the same rules read as promises to the player, checked against all four identities, the forty
 * encounters of a run, ascension tiers 0/3/6/10 and both run lengths, because a promise that only holds for the
 * first golem is not a promise. Five rules:
 *
 * <ol>
 *   <li><b>Every boss warns for the reference window.</b> 0.50 s, whatever the identity, the encounter number, the
 *       tier or the run length — the length is the one axis the balance sweep can feel, so the roster pins it
 *       ({@link BossFightScript} documents the measurement).</li>
 *   <li><b>The special lands when the window closes, never before it.</b> Every step of the window but the last
 *       leaves the hero's health untouched.</li>
 *   <li><b>One warning carries exactly the hits its script authorises.</b> One full hit, or two halves for the two
 *       scripts that strike twice — never a third, never a silent extra.</li>
 *   <li><b>The ladder buys the boss power and never reaction time.</b> Ten tiers of ascension raise what the special
 *       costs, and leave the window the player has to read at the reference half second.</li>
 *   <li><b>Both vigils promise the same thing.</b> A brief run's boss warns for as long as a long run's.</li>
 * </ol>
 *
 * <p>The tests drive {@link BossSpecialAttackSystem} directly, which is the same instance and the same call the
 * combat loop makes each frame; the melee, movement and drop systems are not part of this path and are not claimed
 * here.
 */
final class BossTelegraphContractTest {
    private static final int[] TIERS = {0, 3, 6, 10};
    private static final GameMode[] MODES = {GameMode.STANDARD, GameMode.BRIEF};
    private static final int[] ENCOUNTERS = {1, 2, 3, 5, 8, 13, 20, 40};
    /**
     * The hero is held at a thousand health, which is also what {@code BossSpecialAttackSystemTest} uses. A bigger
     * pool would blur the damage: a float at 100000 has a spacing of 1/128, so a boss special that costs 1.31 health
     * lands as 1.3125 and the ten-tier ascension bonus at the first encounters — about one percent — disappears into
     * the rounding entirely. Measured with a probe over the whole roster before this number was chosen.
     */
    private static final float HERO_HEALTH = 1_000f;
    private static final float STEP_SECONDS = 0.05f;

    private final BossFactory factory = new BossFactory();
    private final BossSpecialAttackSystem specials = new BossSpecialAttackSystem(new HeroDamageSystem());

    @Test
    void everyEncounterWarnsForTheReferenceWindowInBothVigilsAndAtEveryTier() {
        for (GameMode mode : MODES) {
            for (int tier : TIERS) {
                for (int encounter : ENCOUNTERS) {
                    Scenario scenario = triggered(mode, tier, encounter);
                    String where = mode + " tier " + tier + " boss " + encounter;
                    BossFightScript script = BossEncounterTable.scriptFor(encounter);
                    assertEquals(BossFightScript.REFERENCE_TELEGRAPH_SECONDS, scenario.boss.specialAnimationSeconds,
                        1e-6f, where + " must warn for the reference window");
                    assertEquals(script.telegraphSeconds(), scenario.boss.specialAnimationSeconds, 1e-6f,
                        where + " must warn for its own script's window");

                    int steps = Math.round(scenario.boss.specialAnimationSeconds / STEP_SECONDS);
                    for (int step = 1; step < steps; step++) {
                        specials.update(scenario.state, STEP_SECONDS);
                        assertEquals(HERO_HEALTH, scenario.state.hero.health,
                            where + " took the special before the window closed");
                    }
                    specials.update(scenario.state, STEP_SECONDS);
                    assertTrue(scenario.state.hero.health < HERO_HEALTH,
                        where + " must take the special when the window closes, not later");
                    assertEquals(1, scenario.boss.specialUseCount, where + " spent exactly one special");
                }
            }
        }
    }

    @Test
    void theLadderBuysTheBossPowerAndNeverReactionTime() {
        for (BossType type : BossType.values()) {
            for (int encounter : ENCOUNTERS) {
                Scenario opening = triggered(GameMode.STANDARD, 0, encounter, type);
                Scenario ascended = triggered(GameMode.STANDARD, 10, encounter, type);
                String where = type + " boss " + encounter;
                assertEquals(opening.boss.specialAnimationSeconds, ascended.boss.specialAnimationSeconds, 1e-6f,
                    where + " must warn for exactly as long ten tiers up");
                assertTrue(ascended.boss.damage > opening.boss.damage,
                    where + " must be the harder boss ten tiers up");
                specials.update(opening.state, BossFightScript.REFERENCE_TELEGRAPH_SECONDS);
                specials.update(ascended.state, BossFightScript.REFERENCE_TELEGRAPH_SECONDS);
                float openingHit = HERO_HEALTH - opening.state.hero.health;
                float ascendedHit = HERO_HEALTH - ascended.state.hero.health;
                assertTrue(ascendedHit > openingHit,
                    where + " must cost the hero more at tier 10: " + ascendedHit + " against " + openingHit);
            }
        }
    }

    @Test
    void oneWarningLandsExactlyTheHitsItsScriptAuthorises() {
        // The authored per-strike damage of each identity, measured: a golem slams for 1.6x, the matriarch's cage
        // lands 0.5x, the wyrm's sweep is two halves of 0.55x inside one strike (1.10x), the knight's void strike
        // 1.25x. A script scales its own strike through specialDamageMultiplier() and TWIN_TELEGRAPH lands two of
        // them on one warning, which is where the hits() factor comes in.
        int singleHitEncounter = 1;
        int twinEncounter = 6;
        assertEquals(1, BossEncounterTable.scriptFor(singleHitEncounter).hits());
        assertEquals(2, BossEncounterTable.scriptFor(twinEncounter).hits());
        for (BossType type : BossType.values()) {
            float identityPerStrike = switch (type) {
                case ANCIENT_GOLEM -> 1.60f;
                case THORN_MATRIARCH -> 0.50f;
                case EMBER_WYRM -> 1.10f;
                case VOID_KNIGHT -> 1.25f;
            };
            for (int encounter : new int[] {singleHitEncounter, twinEncounter}) {
                BossFightScript script = BossEncounterTable.scriptFor(encounter);
                Scenario scenario = triggered(GameMode.STANDARD, 0, encounter, type);
                specials.update(scenario.state, BossFightScript.REFERENCE_TELEGRAPH_SECONDS);
                float expected = scenario.boss.damage * identityPerStrike
                    * script.specialDamageMultiplier() * script.hits();
                assertEquals(expected, HERO_HEALTH - scenario.state.hero.health, 1e-3f,
                    type + " boss " + encounter + " must land " + script.hits() + " strike(s) of its authored damage");
            }
        }
    }

    @Test
    void aWarningTheHeroNeverSeesTheEndOfStillCostsNothingEarly() {
        // The contract has to survive a slow frame: a hundred-millisecond step is what a stuttering phone hands the
        // simulation. Four of those are still one window, and the health may only move on the one that ends it.
        for (BossType type : BossType.values()) {
            Scenario scenario = triggered(GameMode.STANDARD, 10, 40, type);
            specials.update(scenario.state, 0.40f);
            assertEquals(HERO_HEALTH, scenario.state.hero.health, type + " landed early on a 0.40 s step");
            assertTrue(scenario.boss.specialPending, type + " is still winding up after 0.40 s");
            specials.update(scenario.state, 0.10f);
            assertTrue(scenario.state.hero.health < HERO_HEALTH, type + " landed on the step that closed its window");
        }
    }

    @Test
    void everyIdentityWarnsForTheReferenceWindowAndNoScriptMovesIt() {
        for (BossFightScript script : BossFightScript.values()) {
            assertEquals(BossFightScript.REFERENCE_TELEGRAPH_SECONDS, script.telegraphSeconds(), 1e-6f,
                script + " must warn for the reference window");
            assertEquals(script.hits() == 2 ? 0.5f : 1f, script.specialDamageMultiplier(),
                "a twin script splits one cycle's damage into two dodge chances instead of adding damage");
        }
    }

    @Test
    void theStunnedBossKeepsItsWarningAndTheDeadHeroTakesNoSecondHit() {
        Scenario stunned = triggered(GameMode.STANDARD, 0, 1);
        stunned.boss.stunRemainingSeconds = 5f;
        specials.update(stunned.state, BossFightScript.REFERENCE_TELEGRAPH_SECONDS * 2f);
        assertEquals(HERO_HEALTH, stunned.state.hero.health, "a stunned boss cannot land its special");
        assertTrue(stunned.boss.specialPending, "and the warning is held, not thrown away");
        stunned.boss.stunRemainingSeconds = 0f;
        specials.update(stunned.state, BossFightScript.REFERENCE_TELEGRAPH_SECONDS);
        assertTrue(stunned.state.hero.health < HERO_HEALTH, "the held warning still lands once the stun ends");

        // The wyrm's sweep is the double strike, and the deepest encounter is where a one-health hero can be
        // killed by its first half: the second half must be skipped, because damage on a corpse would report a
        // second death to every listener.
        Scenario wyrm = triggered(GameMode.STANDARD, 0, ENCOUNTERS[ENCOUNTERS.length - 1], BossType.EMBER_WYRM);
        wyrm.state.hero.health = 1f;
        specials.update(wyrm.state, BossFightScript.REFERENCE_TELEGRAPH_SECONDS);
        assertEquals(0f, wyrm.state.hero.health, "the first half of the sweep kills the one-health hero");
        assertFalse(wyrm.state.hero.alive, "and the second half must not report a second death");
    }

    private Scenario triggered(GameMode mode, int tier, int encounter) {
        return triggered(mode, tier, encounter, BossType.ANCIENT_GOLEM);
    }

    private Scenario triggered(GameMode mode, int tier, int encounter, BossType type) {
        GameState state = GameState.newRun(type.ordinal() * 100L + encounter);
        state.mode = mode;
        state.ascensionTier = tier;
        state.hero.maxHealth = HERO_HEALTH;
        state.hero.health = HERO_HEALTH;
        Boss boss = factory.create(state, type, state.hero.x, state.hero.y, encounter, 0);
        boss.specialCooldownSeconds = 0f;
        state.aliveBosses.add(boss);
        specials.update(state, 0f);
        assertTrue(boss.specialPending, type + " boss " + encounter + " at tier " + tier + " must telegraph");
        assertEquals(0, boss.specialUseCount, "and it must not have landed anything yet");
        return new Scenario(state, boss);
    }

    private record Scenario(GameState state, Boss boss) {
    }
}
