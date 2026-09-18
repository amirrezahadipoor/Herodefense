package com.amirrezahadipoor.herodefense.onboarding;

/**
 * The player actions the onboarding coach can be advanced by (roadmap R7.1).
 *
 * <p>Every one of them is produced by a touch: this game has no keyboard and no mouse, which
 * {@code TouchOnlyInputPolicyTest} enforces for the whole repository. The coaching itself therefore only
 * has to be reachable by touch, and the coach's contract test asserts that no step waits for anything
 * else.
 */
public enum OnboardingAction {
    /**
     * The player tapped the arena: on an enemy the bow marks it, on empty ground the mark is released.
     *
     * <p>The name is older than the honesty pass of 2026-09-18, when the coach claimed a tap sent the Hero
     * walking. A tap still only chooses a target -- walking is a drag, and it has its own action below -- so the
     * rename from a claim to a description stayed correct when roadmap A1 gave the Hero its legs. Neither the
     * name nor the step id is persisted, so no save file notices either change.
     */
    TAP_GROUND,
    /**
     * The player dragged inside the arena and the Hero took a step order (roadmap A1).
     *
     * <p>Reported only when {@code HeroMovementSystem.orderStepTo} accepted the order, which is the difference
     * between a lesson that teaches a gesture and a lesson that fires on any finger movement: a drag over the
     * HUD, a drag with the wave's step budget spent, and a drag after the Hero has fallen all report nothing, so
     * the step's budget still has to expire in those cases and the player is not told they learned a walk that
     * did not happen.
     */
    HERO_MOVED,
    /**
     * The player tapped the HUD's ULTIMATE button while focus was full, and {@code HeroUltimateSystem} fired.
     *
     * <p>This replaced a {@code DRAG_FIRE} action on 2026-09-18. That one claimed a drag aims and fires the bow,
     * and a drag aimed nothing, so the step that waited for it could only be completed by accident or by its own
     * budget expiring. Roadmap A1 later gave the drag a real job -- walking -- but the claim this action replaced
     * was about <em>aiming</em>, and aiming by hand is still not a thing the game does: the bow fires on its own
     * schedule at whatever the tap marked. {@code OnboardingCoachTest} forbids two steps waiting for the same
     * action and forbids an action no step teaches, which is why the vocabulary gained a real verb rather than
     * sharing the tap, and why {@code HERO_MOVED} is a second entry rather than a reuse of this one.
     */
    ULTIMATE_FIRED,
    /** A coin or a drop reached the Hero. */
    PICKUP_COLLECTED,
    /** The player took a card on the level-up screen. */
    CARD_TAKEN,
    /** The player opened the shop. */
    SHOP_OPENED
}
