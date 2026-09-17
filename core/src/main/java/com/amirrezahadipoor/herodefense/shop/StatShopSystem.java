package com.amirrezahadipoor.herodefense.shop;

import com.amirrezahadipoor.herodefense.gameplay.HeroStatCalculator;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.HeroStat;
import com.amirrezahadipoor.herodefense.trials.TrialEffects;

/** Coin-only permanent stat upgrades; no platform billing or real-money path exists. */
public final class StatShopSystem {
    public enum PurchaseResult { NONE, PURCHASED, INSUFFICIENT_COINS, MAXED }

    /** Levels priced linearly; beyond this every purchase multiplies the price (endless). */
    public static final int CORE_LEVELS = 20;
    public static final int PRICE_STEP_PER_LEVEL = 20;
    public static final float ENDLESS_PRICE_GROWTH = 1.25f;
    public static final int PRICE_CEILING = 9_999_995;
    private static final float FEEDBACK_DURATION_SECONDS = 1.25f;
    /** How long a touched row keeps its tooltip on the help panel: long enough to read, short enough to leave. */
    public static final float HELP_DURATION_SECONDS = 4.5f;
    private final HeroStatCalculator statCalculator = new HeroStatCalculator();
    private PurchaseResult feedbackResult = PurchaseResult.NONE;
    private HeroStat feedbackStat;
    private int feedbackCoins;
    private float feedbackRemainingSeconds;
    private HeroStat helpStat;
    private float helpRemainingSeconds;

    public int purchasedLevels(GameState state, HeroStat stat) {
        if (state == null || stat == null) return 0;
        Integer value = state.shopUpgradeLevels.get(stat.name());
        return value == null ? 0 : Math.max(0, value);
    }

    public int price(GameState state, HeroStat stat) {
        if (state == null || stat == null) return Integer.MAX_VALUE;
        int base = priceForLevel(stat, purchasedLevels(state, stat));
        float mult = TrialEffects.shopPriceMultiplier(state.activeTrials);
        return mult == 1f ? base : (int) Math.round(base * mult / 5.0) * 5;
    }

    /** Linear through the core levels, then geometric with no level cap. */
    public static int priceForLevel(HeroStat stat, int purchasedLevels) {
        int level = Math.max(0, purchasedLevels);
        int core = Math.min(CORE_LEVELS, level);
        double price = basePrice(stat) + PRICE_STEP_PER_LEVEL * core;
        if (level > CORE_LEVELS) {
            price *= Math.pow(ENDLESS_PRICE_GROWTH, level - CORE_LEVELS);
        }
        if (price >= PRICE_CEILING) return PRICE_CEILING;
        return (int) Math.round(price / 5.0) * 5;
    }

    public void update(float realDeltaSeconds) {
        if (realDeltaSeconds <= 0f) return;
        if (helpRemainingSeconds > 0f) {
            helpRemainingSeconds = Math.max(0f, helpRemainingSeconds - realDeltaSeconds);
        }
        if (feedbackRemainingSeconds > 0f) {
            feedbackRemainingSeconds = Math.max(0f, feedbackRemainingSeconds - realDeltaSeconds);
            if (feedbackRemainingSeconds == 0f) clearFeedback();
        }
    }

    /**
     * Records that the player touched a talent's row (roadmap R7.2). The tooltip is shown for the row the
     * player asked about, which is the whole point of it: a help line that talks about a different stat than
     * the one under the finger teaches nothing.
     */
    public void noteTouch(HeroStat stat) {
        if (stat == null) return;
        helpStat = stat;
        helpRemainingSeconds = HELP_DURATION_SECONDS;
    }

    /** The row whose tooltip belongs on the help panel, or null while nobody has asked. */
    public HeroStat helpStat() {
        return helpRemainingSeconds > 0f ? helpStat : null;
    }

    /** Fades the help panel out at the end of its life instead of blinking it off. */
    public float helpAlpha() {
        if (helpRemainingSeconds <= 0f) return 0f;
        return Math.min(1f, helpRemainingSeconds / 0.35f);
    }

    public PurchaseResult feedbackResult() {
        return feedbackRemainingSeconds > 0f ? feedbackResult : PurchaseResult.NONE;
    }

    public String feedbackMessage() {
        if (feedbackRemainingSeconds <= 0f || feedbackStat == null) return null;
        return switch (feedbackResult) {
            case PURCHASED -> "PURCHASED  |  " + pretty(feedbackStat) + " +1  |  -$ " + feedbackCoins;
            case INSUFFICIENT_COINS -> "NEED $ " + feedbackCoins + " MORE  |  " + pretty(feedbackStat);
            default -> null;
        };
    }

    public float feedbackAlpha() {
        if (feedbackRemainingSeconds <= 0f) return 0f;
        return Math.min(1f, feedbackRemainingSeconds / 0.20f);
    }

    public boolean purchase(GameState state, HeroStat stat) {
        if (state == null || state.hero == null || stat == null) return false;
        int oldLevel = purchasedLevels(state, stat);
        int price = price(state, stat);
        if (state.coins < price) {
            showFeedback(PurchaseResult.INSUFFICIENT_COINS, stat, price - state.coins);
            return false;
        }

        float previousMaxHealth = statCalculator.maxHealth(state);
        state.coins -= price;
        state.shopUpgradeLevels.put(stat.name(), oldLevel + 1);
        increment(state, stat);
        if (stat == HeroStat.HEALTH) {
            float maxHealth = statCalculator.maxHealth(state);
            state.hero.health = Math.min(maxHealth, state.hero.health + maxHealth - previousMaxHealth);
            state.hero.maxHealth = maxHealth;
        }
        showFeedback(PurchaseResult.PURCHASED, stat, price);
        return true;
    }

    private void showFeedback(PurchaseResult result, HeroStat stat, int coins) {
        feedbackResult = result;
        feedbackStat = stat;
        feedbackCoins = Math.max(0, coins);
        feedbackRemainingSeconds = FEEDBACK_DURATION_SECONDS;
    }

    private void clearFeedback() {
        feedbackResult = PurchaseResult.NONE;
        feedbackStat = null;
        feedbackCoins = 0;
        feedbackRemainingSeconds = 0f;
    }

    private static String pretty(HeroStat stat) {
        String name = stat.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static int basePrice(HeroStat stat) {
        return switch (stat) {
            case STRENGTH -> 55;
            case AGILITY -> 60;
            case LUCK -> 50;
            case DODGE -> 50;
            case HEALTH -> 65;
        };
    }

    private static void increment(GameState state, HeroStat stat) {
        switch (stat) {
            case STRENGTH -> state.hero.stats.strength++;
            case AGILITY -> state.hero.stats.agility++;
            case LUCK -> state.hero.stats.luck++;
            case DODGE -> state.hero.stats.dodge++;
            case HEALTH -> state.hero.stats.health++;
        }
    }
}
