package com.amirrezahadipoor.herodefense.settings;

import com.amirrezahadipoor.herodefense.model.ItemTier;

/** Device-local accessibility/audio/inventory preferences independent from a run save. */
public final class GameSettings {
    public boolean soundEnabled = true;
    public boolean musicEnabled = true;
    /** Inventory auto-sell: ticked tiers are sold the moment a drop enters the backpack. */
    public boolean autoSellCommon;
    public boolean autoSellUncommon;
    public boolean autoSellRare;
    /**
     * The first-run coaching sequence (roadmap R7.1) is shown once per device: a run that starts after it
     * has been finished or skipped does not show it again. It lives with the other device-local preferences
     * rather than in a run save, because it is about the player and not about the run.
     */
    public boolean tutorialSeen;

    /** Legendary and Mythic items are never auto-sold; the toggle simply does not exist for them. */
    public boolean autoSells(ItemTier tier) {
        if (tier == null) return false;
        return switch (tier) {
            case COMMON -> autoSellCommon;
            case UNCOMMON -> autoSellUncommon;
            case RARE -> autoSellRare;
            case LEGENDARY -> false;
            case MYTHIC -> false;
        };
    }

    /** Flips the toggle for a sellable tier; returns false for tiers without a toggle. */
    public boolean toggleAutoSell(ItemTier tier) {
        if (tier == null) return false;
        switch (tier) {
            case COMMON -> autoSellCommon = !autoSellCommon;
            case UNCOMMON -> autoSellUncommon = !autoSellUncommon;
            case RARE -> autoSellRare = !autoSellRare;
            default -> {
                return false;
            }
        }
        return true;
    }
}
