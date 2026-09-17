package com.amirrezahadipoor.herodefense.onboarding;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.OnboardingStrings;

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
 * <p>The words are {@link OnboardingStrings} entries rather than fields of this enum, which is where the
 * locale work (R7.3) said they would end up: the action and the budget are gameplay data and stay here, while
 * the line and the hint are a language and live where both of them exist and {@code GameFonts} can see the
 * glyphs they need. The stable {@link #id()} is untouched, so nothing that logs or tests a step depends on
 * display text.
 */
public enum OnboardingStep {
    WALK(
        "walk",
        OnboardingStrings.WALK_LINE,
        OnboardingStrings.WALK_HINT,
        OnboardingAction.TAP_GROUND,
        12f
    ),
    FIRE(
        "fire",
        OnboardingStrings.FIRE_LINE,
        OnboardingStrings.FIRE_HINT,
        OnboardingAction.DRAG_FIRE,
        12f
    ),
    LOOT(
        "loot",
        OnboardingStrings.LOOT_LINE,
        OnboardingStrings.LOOT_HINT,
        OnboardingAction.PICKUP_COLLECTED,
        10f
    ),
    CARD(
        "card",
        OnboardingStrings.CARD_LINE,
        OnboardingStrings.CARD_HINT,
        OnboardingAction.CARD_TAKEN,
        14f
    ),
    SHOP(
        "shop",
        OnboardingStrings.SHOP_LINE,
        OnboardingStrings.SHOP_HINT,
        OnboardingAction.SHOP_OPENED,
        12f
    );

    private final String id;
    private final OnboardingStrings line;
    private final OnboardingStrings hint;
    private final OnboardingAction action;
    private final float budgetSeconds;

    OnboardingStep(
        String id,
        OnboardingStrings line,
        OnboardingStrings hint,
        OnboardingAction action,
        float budgetSeconds
    ) {
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

    /** The coached line in the language in force, shown on the banner. */
    public String line() {
        return GameLocale.text(line);
    }

    /** Where the player is expected to look, in the language in force, shown under the line. */
    public String hint() {
        return GameLocale.text(hint);
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
