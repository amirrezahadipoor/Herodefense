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
    /** The player tapped empty arena ground and the Hero started walking there. */
    TAP_GROUND,
    /** The player held and dragged, which is how the bow aims and fires. */
    DRAG_FIRE,
    /** A coin or a drop reached the Hero. */
    PICKUP_COLLECTED,
    /** The player took a card on the level-up screen. */
    CARD_TAKEN,
    /** The player opened the shop. */
    SHOP_OPENED
}
