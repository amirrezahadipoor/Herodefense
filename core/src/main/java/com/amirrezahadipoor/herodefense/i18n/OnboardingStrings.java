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
 * <p>The shop hint names no side in either language. The English it replaces said "bottom left", which is only
 * roughly true of a button at x=375 of a 720-unit-wide screen, and a side would have to be reworded again once
 * the HUD mirrors for RTL. An inaccuracy that changes with the language is worse than one that does not.
 */
public enum OnboardingStrings implements Translated {

    /** The banner's own chrome: what the vigil is called, and how a player who knows the game leaves it. */
    TITLE("YOUR FIRST VIGIL", "نخستین پاسداری شما"),
    SKIP("SKIP", "رد کردن"),

    /** Step 1: move. */
    WALK_LINE("Tap empty ground and the Hero walks there",
        "روی زمین خالی بزنید تا قهرمان به آنجا برود"),
    WALK_HINT("the arena floor", "کف میدان"),

    /** Step 2: shoot. */
    FIRE_LINE("Hold and drag to aim - the bow fires while you hold",
        "نگه دارید و بکشید تا نشانه بروید - کمان تا وقتی نگه داشته‌اید شلیک می‌کند"),
    FIRE_HINT("anywhere on the arena", "هر نقطهٔ میدان"),

    /** Step 3: collect. */
    LOOT_LINE("Coins and drops come to you when you walk near them",
        "سکه‌ها و غنیمت‌ها وقتی نزدیکشان شوید به سمت شما می‌آیند"),
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
