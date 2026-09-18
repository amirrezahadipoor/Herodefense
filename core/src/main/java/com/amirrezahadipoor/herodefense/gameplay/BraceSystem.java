package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.BraceLimits;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.Hero;

/**
 * The shield the player raises by tapping the Hero itself (roadmap A2).
 *
 * <p>The audit's complaint was that nothing in the game is actively cast: five passive upgrade cards, a mark and
 * an ultimate, and no decision to make inside a wave. The brace is one decision with a real price. For
 * {@code BraceLimits.BRACE_SECONDS} everything that hurts -- melee swings, boss specials, the rot nobody dodges
 * -- hurts {@code BraceLimits.DAMAGE_TAKEN_MULTIPLIER} as much, and in exchange the bow fires nothing and the
 * Hero takes no step order: a planted shield is not a walking shield. The cooldown runs raise to raise, so the
 * verb is a window chosen against a telegraph, not a stance to hold.
 *
 * <p>The gesture is a tap on the Hero's own body. It is the one tap in the arena that meant nothing: a tap marks
 * the enemy under the finger, and on empty ground it releases the mark, but on the Hero there was no enemy and
 * nothing to release. It needs no new button, no new art and no new sound -- the sfx roster is exactly what
 * roadmap F2 measures, and borrowing a cue for the shield would deepen the reuse that finding counts -- so the
 * feedback is the sprite's tint, the bar under the feet and the haptic tap the router already owns.
 *
 * <p>What the brace deliberately does not do is dodge. {@code BossSpecialAttackSystem} applies its hits through
 * the damage pipeline with no position test, and the brace answers that from the other side: the special still
 * lands, and it lands on a shield. Roadmap A5 is still the decision about whether specials should ever be
 * dodgeable; this is the verb a player has in the meantime, and the one the Hero's own javadoc names when it says
 * a special is braced for rather than avoided.
 */
public final class BraceSystem {

    /** Half the width of the tap box around the Hero's body, in world units. */
    static final float TAP_HALF_WIDTH = 48f;
    /** The tap box reaches this far below and above the feet, so a tap on the bow or the head both count. */
    static final float TAP_BELOW_FEET = 12f;
    static final float TAP_ABOVE_FEET = 132f;

    private BraceSystem() {
    }

    /** Whether the shield is up right now. */
    public static boolean isBracing(GameState state) {
        return state != null && state.hero != null && state.hero.braceRemainingSeconds > 0f;
    }

    /** Whether raising the shield would take: alive, and the cooldown spent. */
    public static boolean canBrace(GameState state) {
        return state != null && state.hero != null && state.hero.alive
            && state.hero.braceRemainingSeconds <= 0f && state.hero.braceCooldownSeconds <= 0f;
    }

    /** The share of a hit that gets through right now; one for an unbraced Hero. */
    public static float damageTakenMultiplier(GameState state) {
        return isBracing(state) ? BraceLimits.DAMAGE_TAKEN_MULTIPLIER : 1f;
    }

    /**
     * Raises the shield.
     *
     * @return true when the shield went up, which is what lets the router answer with a haptic; false when the
     *     Hero is dead, already bracing, or still cooling down
     */
    public static boolean tryBrace(GameState state) {
        if (!canBrace(state)) {
            return false;
        }
        state.hero.braceRemainingSeconds = BraceLimits.BRACE_SECONDS;
        state.hero.braceCooldownSeconds = BraceLimits.COOLDOWN_SECONDS;
        // A live step order under a planted shield would walk the Hero out of its own stance on the next tick.
        state.hero.moveOrderActive = false;
        return true;
    }

    /** Whether a tap landed on the Hero's body rather than on the arena around it. */
    public static boolean tapHitsHero(GameState state, float worldX, float worldY) {
        if (state == null || state.hero == null || !Float.isFinite(worldX) || !Float.isFinite(worldY)) {
            return false;
        }
        Hero hero = state.hero;
        return Math.abs(worldX - hero.x) <= TAP_HALF_WIDTH
            && worldY >= hero.y - TAP_BELOW_FEET && worldY <= hero.y + TAP_ABOVE_FEET;
    }

    /** Ages both clocks. Called first in {@code CombatSystem.update}, before anything deals or fires damage. */
    public static void update(GameState state, float deltaSeconds) {
        if (state == null || state.hero == null || deltaSeconds <= 0f || !Float.isFinite(deltaSeconds)) {
            return;
        }
        Hero hero = state.hero;
        if (hero.braceRemainingSeconds > 0f) {
            hero.braceRemainingSeconds = Math.max(0f, hero.braceRemainingSeconds - deltaSeconds);
        }
        if (hero.braceCooldownSeconds > 0f) {
            hero.braceCooldownSeconds = Math.max(0f, hero.braceCooldownSeconds - deltaSeconds);
        }
        if (!hero.alive) {
            hero.braceRemainingSeconds = 0f;
        }
    }
}
