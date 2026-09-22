package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.BraceLimits;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.HeroAnimationState;
import com.amirrezahadipoor.herodefense.model.IncomingHitResult;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The set: a shield raised <em>into</em> a blow answers it instead of absorbing part of it.
 *
 * <p>{@code BraceSystemTest} proves the stance -- three seconds of sixty-percent reduction for twelve seconds
 * without a bow. This file proves what the stance is worth when the player times it, because that is the
 * difference between a button that halves a number and a verb that rewards watching the enemy. Every clause here
 * goes through the real funnel ({@code HeroDamageSystem}) or the real meter ({@code FocusSystem}) rather than
 * asserting the fields {@code BraceSystem} happens to write.
 */
final class BraceSetTest {

    private static GameState fresh() {
        GameState state = GameState.newRun(0x5E7B2A11L);
        state.anchorHeroAtArenaCenter();
        return state;
    }

    private static void age(GameState state, float seconds) {
        BraceSystem.update(state, seconds);
    }

    @Test
    void aBlowThatArrivesIntoARaisedShieldIsHeldWhole() {
        GameState state = fresh();
        float health = state.hero.health;
        assertTrue(BraceSystem.tryBrace(state));
        age(state, BraceLimits.SET_WINDOW_SECONDS * 0.5f);
        assertTrue(BraceSystem.isSet(state), "half a window in, the shield is still set");

        assertEquals(IncomingHitResult.HELD,
            new HeroDamageSystem().applyIncomingHit(state, 40f),
            "a timed shield reports the blow as held, not as a dodge and not as damage");
        assertEquals(health, state.hero.health, 0.0001f, "and nothing is taken");
        assertEquals(1, state.hero.setsHeldThisRun);
        assertFalse(state.hero.animationState == HeroAnimationState.HIT,
            "a held blow does not flinch: the Hero never entered receiveIncomingHit, so the clip stays on the stance");
    }

    @Test
    void theWindowClosesAndThenTheSameShieldOnlyReduces() {
        GameState state = fresh();
        assertTrue(BraceSystem.tryBrace(state));
        age(state, BraceLimits.SET_WINDOW_SECONDS + 0.05f);
        assertFalse(BraceSystem.isSet(state), "the set is a window, not the whole brace");
        assertTrue(BraceSystem.isBracing(state), "the shield itself is still up");

        float health = state.hero.health;
        assertEquals(IncomingHitResult.DAMAGED, new HeroDamageSystem().applyIncomingHit(state, 40f));
        assertEquals(health - 40f * BraceLimits.DAMAGE_TAKEN_MULTIPLIER, state.hero.health, 0.0001f,
            "late, the same blow is sixty percent off rather than all of it");
        assertEquals(0, state.hero.setsHeldThisRun, "and nothing was held");
    }

    @Test
    void oneRaiseAnswersExactlyOneBlowHoweverManyBodyHitsLand() {
        GameState state = fresh();
        assertTrue(BraceSystem.tryBrace(state));
        age(state, BraceLimits.SET_WINDOW_SECONDS * 0.2f);

        assertEquals(IncomingHitResult.HELD, new HeroDamageSystem().applyIncomingHit(state, 30f));
        assertEquals(1, state.hero.setsHeldThisRun);
        float afterFirst = state.hero.health;
        assertEquals(IncomingHitResult.DAMAGED, new HeroDamageSystem().applyIncomingHit(state, 30f),
            "a second body in the same window is not a second negation");
        assertEquals(afterFirst - 30f * BraceLimits.DAMAGE_TAKEN_MULTIPLIER, state.hero.health, 0.0001f);
        assertEquals(1, state.hero.setsHeldThisRun, "the run still counts one");
    }

    @Test
    void theRotIsNotSettable() {
        GameState state = fresh();
        assertTrue(BraceSystem.tryBrace(state));
        age(state, BraceLimits.SET_WINDOW_SECONDS * 0.1f);
        float health = state.hero.health;

        new HeroDamageSystem().applyEnvironmentalHit(state, 30f);
        assertEquals(health - 30f * BraceLimits.DAMAGE_TAKEN_MULTIPLIER, state.hero.health, 0.0001f,
            "ground damage is stood on, not parried");
        assertEquals(0, state.hero.setsHeldThisRun, "and it does not spend the window either");
        assertTrue(BraceSystem.isSet(state), "the window is still open for the blow that comes from a body");
    }

    @Test
    void aHeldBlowNeverConsumesTheDodgeDie() {
        GameState braced = fresh();
        assertTrue(BraceSystem.tryBrace(braced));
        long streamBefore = braced.combatRandomState;
        new HeroDamageSystem().applyIncomingHit(braced, 20f);
        assertEquals(streamBefore, braced.combatRandomState,
            "a blow the player already answered does not cost a stat roll");

        GameState unbraced = fresh();
        long unbracedBefore = unbraced.combatRandomState;
        new HeroDamageSystem().applyIncomingHit(unbraced, 20f);
        assertFalse(unbraced.combatRandomState == unbracedBefore,
            "an ordinary blow still spends exactly one die from the persisted stream");
    }

    @Test
    void aSetGivesBackHalfTheCooldownAndThreeArrowsOfFocus() {
        GameState state = fresh();
        state.focus = 0f;
        assertTrue(BraceSystem.tryBrace(state));
        assertEquals(BraceLimits.COOLDOWN_SECONDS, state.hero.braceCooldownSeconds);
        age(state, BraceLimits.SET_WINDOW_SECONDS * 0.5f);
        assertEquals(BraceLimits.COOLDOWN_SECONDS - BraceLimits.SET_WINDOW_SECONDS * 0.5f,
            state.hero.braceCooldownSeconds, 0.0001f);

        assertEquals(IncomingHitResult.HELD, new HeroDamageSystem().applyIncomingHit(state, 20f));
        assertEquals(
            BraceLimits.COOLDOWN_SECONDS - BraceLimits.SET_WINDOW_SECONDS * 0.5f
                - BraceLimits.SET_COOLDOWN_REFUND_SECONDS,
            state.hero.braceCooldownSeconds,
            0.0001f,
            "the set takes its own share of the cooldown back"
        );

        GameState oracle = fresh();
        oracle.focus = 0f;
        FocusSystem.addHits(oracle, BraceLimits.SET_FOCUS_HITS, 0, 0);
        assertEquals(oracle.focus, state.focus, 0.0001f,
            "and it is worth exactly the Focus of three ordinary landed hits, read off FocusSystem itself");
        assertTrue(state.focus > 0f, "so one hold is a visible step on the meter");
    }

    @Test
    void twoSetsFitInsideOneWaveBecauseTheRefundPaysForTheSecond() {
        GameState state = fresh();
        assertTrue(BraceSystem.tryBrace(state));
        age(state, 0.2f);
        new HeroDamageSystem().applyIncomingHit(state, 20f);
        assertTrue(state.hero.braceCooldownSeconds < BraceLimits.COOLDOWN_SECONDS - 5f,
            "the refund is what makes a second set possible at all");

        // Twelve seconds from raise to raise is the un-set clock; the refund is what shortens it, and it does so
        // whether or not the brace is still running.
        assertTrue(state.hero.braceCooldownSeconds <= BraceLimits.COOLDOWN_SECONDS
            - BraceLimits.SET_COOLDOWN_REFUND_SECONDS, "half the cooldown is gone");
        age(state, BraceLimits.COOLDOWN_SECONDS - BraceLimits.SET_COOLDOWN_REFUND_SECONDS);
        assertTrue(BraceSystem.canBrace(state), "the second raise is ready inside the same wave");
        assertTrue(BraceSystem.tryBrace(state));
        age(state, 0.2f);
        assertEquals(IncomingHitResult.HELD, new HeroDamageSystem().applyIncomingHit(state, 20f));
        assertEquals(2, state.hero.setsHeldThisRun);
    }

    @Test
    void theFlashIsPresentationOnlyAndCannotOutliveTheBrace() {
        GameState state = fresh();
        assertTrue(BraceSystem.tryBrace(state));
        age(state, 0.1f);
        new HeroDamageSystem().applyIncomingHit(state, 20f);
        assertEquals(BraceLimits.SET_FLASH_SECONDS, state.hero.setFlashSeconds);
        age(state, BraceLimits.SET_FLASH_SECONDS);
        assertEquals(0f, state.hero.setFlashSeconds, "the burst fades on its own clock");

        state.hero.setFlashSeconds = 9f;
        state.hero.braceElapsedSeconds = -3f;
        state.hero.setsHeldThisRun = -5;
        state.hero.validateAndRepair();
        assertEquals(BraceLimits.SET_FLASH_SECONDS, state.hero.setFlashSeconds,
            "a save cannot hold a burst longer than the game's own");
        assertEquals(0f, state.hero.braceElapsedSeconds);
        assertEquals(0, state.hero.setsHeldThisRun);
    }

    @Test
    void theFirstHoldUnlocksItsCodexEntryOnce() {
        CodexSystem codex = new CodexSystem();
        GameState state = fresh();
        assertFalse(codex.isUnlocked(state, "codex_48"));
        assertTrue(codex.unlockSecretsForSet(state).isEmpty(),
            "nothing to unlock while no blow has been held");
        assertTrue(BraceSystem.tryBrace(state));
        age(state, 0.1f);
        new HeroDamageSystem().applyIncomingHit(state, 20f);
        assertEquals(List.of("codex_48"), codex.unlockSecretsForSet(state));
        assertTrue(codex.isUnlocked(state, "codex_48"));
        assertTrue(codex.unlockSecretsForSet(state).isEmpty(), "and only once");
    }
}
