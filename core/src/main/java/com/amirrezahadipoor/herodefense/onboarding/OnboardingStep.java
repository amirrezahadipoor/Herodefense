package com.amirrezahadipoor.herodefense.onboarding;

/**
 * The first-run vigil's coaching steps, in order (roadmap R7.1).
 *
 * <p>Five lines, one minute, and each line waits for the thing it describes rather than for a timer: the
 * player is taught by doing. Two rules keep that from becoming a lecture. Every step carries its own
 * budget, and the coach advances when the budget runs out even if the player never performed the action —
 * a player who ignores the coach entirely must never be trapped by it, which is also what makes the
 * sequence's total a hard sixty seconds. And a step only ever completes on <em>its</em> action: taking a
 * card early does not tick off the walk lesson.
 *
 * <p>The texts are constants rather than literals at the call site so the upcoming locale work (R7.3) has
 * one place to read them from.
 */
public enum OnboardingStep {
    WALK(
        "walk",
        "Tap empty ground and the Hero walks there",
        "the arena floor",
        OnboardingAction.TAP_GROUND,
        12f
    ),
    FIRE(
        "fire",
        "Hold and drag to aim - the bow fires while you hold",
        "anywhere on the arena",
        OnboardingAction.DRAG_FIRE,
        12f
    ),
    LOOT(
        "loot",
        "Coins and drops come to you when you walk near them",
        "a drop on the ground",
        OnboardingAction.PICKUP_COLLECTED,
        10f
    ),
    CARD(
        "card",
        "Every level-up offers cards: tap the one you want",
        "the level-up screen",
        OnboardingAction.CARD_TAKEN,
        14f
    ),
    SHOP(
        "shop",
        "Between waves, spend coins in the shop",
        "the shop button, bottom left",
        OnboardingAction.SHOP_OPENED,
        12f
    );

    private final String id;
    private final String line;
    private final String hint;
    private final OnboardingAction action;
    private final float budgetSeconds;

    OnboardingStep(String id, String line, String hint, OnboardingAction action, float budgetSeconds) {
        this.id = id;
        this.line = line;
        this.hint = hint;
        this.action = action;
        this.budgetSeconds = budgetSeconds;
    }

    /** Stable identifier, for logs and for a test that must not depend on the display text. */
    public String id() {
        return id;
    }

    /** The coached line, shown on the banner. */
    public String line() {
        return line;
    }

    /** Where the player is expected to look, shown under the line. */
    public String hint() {
        return hint;
    }

    /** The action that completes this step. */
    public OnboardingAction action() {
        return action;
    }

    /** How long the coach waits for the action before moving on by itself. */
    public float budgetSeconds() {
        return budgetSeconds;
    }

    /** True when {@code action} is the touch-driven action this step teaches. */
    public boolean completesOn(OnboardingAction action) {
        return this.action == action;
    }

    /** The whole sequence's worst-case duration: five budgets, no slack. */
    public static float totalSeconds() {
        float total = 0f;
        for (OnboardingStep step : values()) {
            total += step.budgetSeconds;
        }
        return total;
    }
}
