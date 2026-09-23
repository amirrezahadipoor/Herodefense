package com.amirrezahadipoor.herodefense.i18n;

/**
 * The first-run vigil's coaching, in both languages (roadmap R7.3).
 *
 * <p>Seven steps, one minute total, each a coached line plus the place the player is meant to look at. Both
 * halves are here rather than in {@code onboarding/OnboardingStep}'s constructor arguments, which is where that
 * enum's own javadoc said the locale work should put them: the budgets and the actions are gameplay data and
 * stay in the enum, the words are a language and come from a table that {@code TranslationTableTest} checks on
 * both sides.
 *
 * <p>Three of these lines were rewritten on 2026-09-18 because they described mechanics the game did not have:
 * the Hero could not move, a drag during a wave only moved a press marker, and drops are auto-collected by
 * {@code DropPickupSystem} rather than walked over. A translation table faithfully carrying a false sentence
 * ships the same falsehood in every language, so both sides changed together and {@code OnboardingClaimsTest}
 * was written to fail if a coached line claimed aiming again. Two of those three corrections were the game
 * catching up to the sentence rather than the other way round: roadmap A1 built the walk, and the drag that used
 * to do nothing is now the gesture for it, so {@code MOVE_LINE} says in both languages what
 * {@code HeroMovementSystem} does. What is still false -- aiming by hand -- stays unbanned-by-accident and
 * banned-by-test.
 *
 * <p>The shop hint names no side in either language. The English it replaces said "bottom left", which is only
 * roughly true of a button at x=375 of a 720-unit-wide screen, and a side would have to be reworded again once
 * the HUD mirrors for RTL. An inaccuracy that changes with the language is worse than one that does not.
 */
public enum OnboardingStrings implements Translated {

    /** The banner's own chrome: what the vigil is called, and how a player who knows the game leaves it. */
    TITLE("YOUR FIRST VIGIL"),
    SKIP("SKIP"),

    /** Step 1: choose a target. A tap only ever marks; walking is the drag in step 2. */
    TARGET_LINE("Tap an enemy and the bow focuses it"),
    TARGET_HINT("an enemy in the arena"),

    /**
     * Step 2: step. Roadmap A1 gave the Hero legs and the drag that had never done anything became the gesture
     * for them. The budget is left to the meter under the Hero's feet rather than to a sentence here: a line that
     * has to explain a resource is a line the player will not read twice, and the meter only appears once some
     * of the budget has been spent, so the arena looks untouched until stepping is real.
     */
    MOVE_LINE("Drag on the ground and the Hero steps there"),
    MOVE_HINT("the ground beside the Hero"),

    /**
     * Step 3: the one verb the player triggers by hand. The bow itself is automatic, so the lesson is the
     * button, and it is worded conditionally because focus may not be full inside this step's budget -- the
     * budget exists for exactly that, and a line promising a full meter would be a second false claim.
     */
    ULTIMATE_LINE("When the focus meter fills, tap ULTIMATE to spend it"),
    ULTIMATE_HINT("the ULTIMATE button"),

    /**
     * Step 4: brace, and with it the A5 contract -- a boss's special is not dodged. The line says so in the
     * same breath as the gesture, because a player who learned to step in lesson two will otherwise spend the
     * first boss telegraph discovering the exception the hard way.
     */
    BRACE_LINE("A blow finds you anywhere: tap the Hero as it falls and the raised shield holds all of it"),
    BRACE_HINT("the Hero's own body"),

    /** Step 4: collect. {@code DropPickupSystem} homes drops in after a short delay; nobody walks to them. */
    LOOT_LINE("Coins and drops reach the Hero on their own after a moment"),
    LOOT_HINT("a drop on the ground"),

    /** Step 5: choose a card. */
    CARD_LINE("Every level-up offers cards: tap the one you want"),
    CARD_HINT("the level-up screen"),

    /** Step 6: spend. */
    SHOP_LINE("Between waves, spend coins in the shop"),
    SHOP_HINT("the shop button");

    private final String english;

    OnboardingStrings(String english) {
        this.english = english;
    }

    @Override
    public String key() {
        return name();
    }

    @Override
    public String english() {
        return english;
    }

}
