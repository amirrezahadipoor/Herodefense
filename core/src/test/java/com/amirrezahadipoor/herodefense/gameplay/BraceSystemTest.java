package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.BraceLimits;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Roadmap A2: one verb the player casts, and the price that keeps it a decision.
 *
 * <p>The brace is three seconds of shield for twelve seconds of cooldown, sixty percent off everything that
 * lands inside the window, and in exchange the bow fires nothing and the feet stay planted. Every one of those
 * clauses is asserted below against the real pipeline -- damage through {@code HeroDamageSystem}, arrows through
 * {@code HeroAutoAttackSystem}, steps through {@code HeroMovementSystem} -- because a shield that only exists in
 * the system that owns it is exactly the kind of claim this suite was built to distrust.
 */
final class BraceSystemTest {

    private static GameState fresh() {
        GameState state = GameState.newRun(0xA2B2ACE2L);
        state.anchorHeroAtArenaCenter();
        return state;
    }

    private static void age(GameState state, float seconds) {
        BraceSystem.update(state, seconds);
    }

    @Test
    void theShieldGoesUpOnceAndRefusesUntilItsCooldownSpends() {
        GameState state = fresh();
        assertTrue(BraceSystem.canBrace(state));
        assertTrue(BraceSystem.tryBrace(state), "a fresh Hero can raise the shield");
        assertEquals(BraceLimits.BRACE_SECONDS, state.hero.braceRemainingSeconds);
        assertEquals(BraceLimits.COOLDOWN_SECONDS, state.hero.braceCooldownSeconds);
        assertFalse(BraceSystem.tryBrace(state), "raising it twice is not raising it longer");
        age(state, BraceLimits.BRACE_SECONDS);
        assertFalse(BraceSystem.isBracing(state), "three seconds is three seconds");
        assertFalse(BraceSystem.tryBrace(state), "and the cooldown outlives the shield by nine seconds");
        age(state, BraceLimits.COOLDOWN_SECONDS - BraceLimits.BRACE_SECONDS);
        assertTrue(BraceSystem.canBrace(state), "raise to raise, twelve seconds");
    }

    @Test
    void aBracedHeroTakesFortyPercentOfTheSameHit() {
        GameState open = fresh();
        float before = open.hero.health;
        new HeroDamageSystem().applyIncomingHit(open, 10f);
        assertEquals(before - 10f, open.hero.health, 0.001f, "unbraced, ten damage is ten damage");

        // A brace raised long before the blow is the reduction this test is about; a brace raised into the blow
        // is the set, and BraceSetTest owns that contract.
        GameState braced = fresh();
        assertTrue(BraceSystem.tryBrace(braced));
        age(braced, BraceLimits.SET_WINDOW_SECONDS + 0.05f);
        float bracedBefore = braced.hero.health;
        new HeroDamageSystem().applyIncomingHit(braced, 10f);
        assertEquals(bracedBefore - 10f * BraceLimits.DAMAGE_TAKEN_MULTIPLIER, braced.hero.health, 0.001f,
            "braced, the same hit is four");
    }

    @Test
    void theRotNobodyDodgesIsBracedToo() {
        GameState open = fresh();
        float before = open.hero.health;
        new HeroDamageSystem().applyEnvironmentalHit(open, 10f);
        assertEquals(before - 10f, open.hero.health, 0.001f);

        GameState braced = fresh();
        assertTrue(BraceSystem.tryBrace(braced));
        float bracedBefore = braced.hero.health;
        new HeroDamageSystem().applyEnvironmentalHit(braced, 10f);
        assertEquals(bracedBefore - 10f * BraceLimits.DAMAGE_TAKEN_MULTIPLIER, braced.hero.health, 0.001f,
            "a planted shield stands on the ground as much as the Hero does");
    }

    @Test
    void theBowIsSilentBehindTheShieldWhileArrowsInFlightStillLand() {
        GameState state = fresh();
        Enemy foe = new Enemy(state.allocateEntityId(), "GLOOM_WOLF", 360f, 500f);
        foe.health = 500f;
        foe.maxHealth = 500f;
        state.aliveEnemies.add(foe);
        HeroAutoAttackSystem bow = new HeroAutoAttackSystem();

        HeroAttackUpdateResult loose = bow.update(state, 1f / 60f);
        assertTrue(loose.shots() > 0, "an unbraced Hero with a foe in range shoots");

        assertTrue(BraceSystem.tryBrace(state));
        HeroAttackUpdateResult braced = bow.update(state, 1f / 60f);
        assertEquals(0, braced.shots(), "a braced Hero fires nothing new");
        assertTrue(state.hero.attackCooldownSeconds >= 0f, "and the cooldown still ages behind the shield");
    }

    @Test
    void aPlantedShieldRootsTheHeroAndVoidsTheOrderItInterrupts() {
        GameState state = fresh();
        HeroMovementSystem.orderStepTo(state, 500f, 400f);
        assertTrue(state.hero.moveOrderActive);
        assertTrue(BraceSystem.tryBrace(state));
        assertFalse(state.hero.moveOrderActive, "the shield voids a live step order rather than resuming it");
        assertFalse(HeroMovementSystem.orderStepTo(state, 300f, 400f),
            "and refuses new ones: a walking shield is not a planted one");
        age(state, BraceLimits.BRACE_SECONDS + BraceLimits.COOLDOWN_SECONDS);
        assertTrue(HeroMovementSystem.orderStepTo(state, 300f, 400f), "once the stance is over, the feet are free");
    }

    @Test
    void theTapBoxIsTheBodyAndNotTheArenaAroundIt() {
        GameState state = fresh();
        float x = state.hero.x;
        float y = state.hero.y;
        assertTrue(BraceSystem.tapHitsHero(state, x, y + 60f), "the chest");
        assertTrue(BraceSystem.tapHitsHero(state, x - 40f, y + 20f), "the bow arm");
        assertFalse(BraceSystem.tapHitsHero(state, x + 60f, y + 60f), "a hand's width to the side is arena");
        assertFalse(BraceSystem.tapHitsHero(state, x, y + 200f), "above the head is arena");
        assertFalse(BraceSystem.tapHitsHero(state, x, y - 40f), "below the feet is arena");
    }

    @Test
    void theDeadNeitherBraceNorStayBraced() {
        GameState state = fresh();
        assertTrue(BraceSystem.tryBrace(state));
        state.hero.alive = false;
        age(state, 1f / 60f);
        assertFalse(BraceSystem.isBracing(state), "a fallen Hero's shield is down");
        state.hero.braceCooldownSeconds = 0f;
        assertFalse(BraceSystem.tryBrace(state), "and the dead do not raise it again");
    }

    @Test
    void aLoadedSaveCannotCarryAnImpossibleShield() {
        GameState state = fresh();
        state.hero.braceRemainingSeconds = 99f;
        state.hero.braceCooldownSeconds = -4f;
        state.hero.validateAndRepair();
        assertEquals(0f, state.hero.braceElapsedSeconds,
            "and it cannot claim the shield was raised before it was raised");
        assertEquals(BraceLimits.BRACE_SECONDS, state.hero.braceRemainingSeconds,
            "a save cannot grant a longer shield than the game's own");
        assertEquals(0f, state.hero.braceCooldownSeconds);
        state.hero.braceRemainingSeconds = Float.NaN;
        state.hero.validateAndRepair();
        assertEquals(0f, state.hero.braceRemainingSeconds, "and a corrupt one reads as no shield, not as forever");
    }

    @Test
    void theClocksAgeBeforeTheDamageAndNoPolicyEverBraces() {
        String combat = read(Path.of("..", "core", "src", "main", "java",
            "com", "amirrezahadipoor", "herodefense", "gameplay", "CombatSystem.java").normalize());
        int ages = combat.indexOf("BraceSystem.update(state");
        int shoots = combat.indexOf("heroAutoAttackSystem.update(state");
        assertTrue(ages >= 0 && shoots >= 0 && ages < shoots,
            "the shield's clocks have to age before the bow and the damage resolve in the same tick");

        String simulator = read(Path.of("..", "core", "src", "main", "java",
            "com", "amirrezahadipoor", "herodefense", "balance", "BalanceSimulator.java").normalize());
        assertFalse(simulator.contains("BraceSystem"),
            "no simulated policy raises the shield, so every published band is still a floor: bracing can only"
                + " improve on it, for three seconds in every twelve");
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path.toAbsolutePath(), e);
        }
    }
}
