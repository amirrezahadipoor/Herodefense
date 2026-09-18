package com.amirrezahadipoor.herodefense.i18n;

/**
 * The first-run vigil's coaching, in both languages (roadmap R7.3).
 *
 * <p>Five steps, one minute total, each a coached line plus the place the player is meant to look at. Both
 * halves are here rather than in {@code onboarding/OnboardingStep}'s constructor arguments, which is where that
 * enum's own javadoc said the locale work should put them: the budgets and the actions are gameplay data and
 * stay in the enum, the words are a language and come from a table that {@code TranslationTableTest} checks on
 * both sides.
 *
 * <p>Three of these lines were rewritten on 2026-09-18 because they described mechanics the game does not have:
 * the Hero cannot move (nothing in {@code core/src/main} writes {@code hero.x} or {@code hero.y}), a drag during
 * a wave only moves a press marker and aims nothing, and drops are auto-collected by {@code DropPickupSystem}
 * rather than walked over. A translation table faithfully carrying a false sentence ships the same falsehood in
 * every language, so both sides changed together and {@code OnboardingClaimsTest} now fails if a coached line
 * claims movement or aiming again.
 *
 * <p>The shop hint names no side in either language. The English it replaces said "bottom left", which is only
 * roughly true of a button at x=375 of a 720-unit-wide screen, and a side would have to be reworded again once
 * the HUD mirrors for RTL. An inaccuracy that changes with the language is worse than one that does not.
 */
public enum OnboardingStrings implements Translated {

    /** The banner's own chrome: what the vigil is called, and how a player who knows the game leaves it. */
    TITLE("YOUR FIRST VIGIL", "نخستین پاسداری شما"),
    SKIP("SKIP", "رد کردن"),

    /** Step 1: choose a target. The Hero stays where it stands; the mark is what the player controls. */
    TARGET_LINE("Tap an enemy and the bow focuses it",
        "روی دشمن بزنید تا کمان روی او متمرکز شود"),
    TARGET_HINT("an enemy in the arena", "یک دشمن در میدان"),

    /**
     * Step 2: the one verb the player triggers by hand. The bow itself is automatic, so the lesson is the
     * button, and it is worded conditionally because focus may not be full inside this step's budget -- the
     * budget exists for exactly that, and a line promising a full meter would be a second false claim.
     */
    ULTIMATE_LINE("When the focus meter fills, tap ULTIMATE to spend it",
        "وقتی نوار تمرکز پر شد، ضربهٔ نهایی را بزنید"),
    ULTIMATE_HINT("the ULTIMATE button", "دکمهٔ ضربهٔ نهایی"),

    /** Step 3: collect. {@code DropPickupSystem} homes drops in after a short delay; nobody walks anywhere. */
    LOOT_LINE("Coins and drops reach the Hero on their own after a moment",
        "سکه‌ها و غنیمت‌ها بعد از یک لحظه خودشان به قهرمان می‌رسند"),
    LOOT_HINT("a drop on the ground", "یک غنیمت روی زمین"),

    /** Step 4: choose a card. */
    CARD_LINE("Every level-up offers cards: tap the one you want",
        "هر ارتقای سطح کارت می‌دهد: کارتی را که می‌خواهید بزنید"),
    CARD_HINT("the level-up screen", "صفحهٔ ارتقای سطح"),

    /** Step 5: spend. */
    SHOP_LINE("Between waves, spend coins in the shop",
        "میان موج‌ها سکه‌ها را در فروشگاه خرج کنید"),
    SHOP_HINT("the shop button", "دکمهٔ فروشگاه");

    private final String english;
    private final String persian;

    OnboardingStrings(String english, String persian) {
        this.english = english;
        this.persian = persian;
    }

    @Override
    public String key() {
        return name();
    }

    @Override
    public String english() {
        return english;
    }

    @Override
    public String persian() {
        return persian;
    }
}
