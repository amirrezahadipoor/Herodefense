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
     * <p>The name is older than the honesty pass of 2026-09-18, when the coach still claimed the tap sent the
     * Hero walking. Nothing in {@code core/src/main} writes {@code hero.x} or {@code hero.y}, so the gesture the
     * router reports is a target choice and that is all. Renamed from a claim to a description; the step id in
     * {@link OnboardingStep} changed with it, and neither is persisted, so no save file notices.
     */
    TAP_GROUND,
    /**
     * The player tapped the HUD's ULTIMATE button while focus was full, and {@code HeroUltimateSystem} fired.
     *
     * <p>This replaced a {@code DRAG_FIRE} action on 2026-09-18. That one claimed a drag aims and fires the bow;
     * a drag during a wave moves a press marker and changes nothing else, so the step that waited for it could
     * only be completed by accident or by its own budget expiring. {@code OnboardingCoachTest} forbids two steps
     * waiting for the same action and forbids an action no step teaches, which is why the vocabulary gained a
     * real verb rather than sharing the tap.
     */
    ULTIMATE_FIRED,
    /** A coin or a drop reached the Hero. */
    PICKUP_COLLECTED,
    /** The player took a card on the level-up screen. */
    CARD_TAKEN,
    /** The player opened the shop. */
    SHOP_OPENED
}
