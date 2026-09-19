package com.amirrezahadipoor.herodefense.ascension;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.RootNetworkStrings;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.trials.TrialEffects;

import java.util.Locale;
import java.util.Map;

/** Purchase and application of permanent Root Network nodes. */
public final class RootNetworkSystem {
    private static final float FEEDBACK_SECONDS = 1.2f;
    private String feedbackMessage;
    private float feedbackRemaining;

    public void update(float delta) {
        if (feedbackRemaining > 0f) {
            feedbackRemaining = Math.max(0f, feedbackRemaining - delta);
            if (feedbackRemaining == 0f) {
                feedbackMessage = null;
            }
        }
    }

    public String feedbackMessage() {
        return feedbackRemaining > 0f ? feedbackMessage : null;
    }

    public float feedbackAlpha() {
        if (feedbackRemaining <= 0f) return 0f;
        return Math.min(1f, feedbackRemaining / 0.2f);
    }

    public boolean isPurchased(GameState state, String nodeId) {
        if (state == null || nodeId == null) return false;
        Boolean v = state.rootNodesPurchased.get(nodeId);
        return v != null && v;
    }

    public boolean canPurchase(GameState state, String nodeId) {
        if (state == null || nodeId == null) return false;
        RootNodeDefinition def = RootNetworkCatalog.byId(nodeId);
        if (def == null) return false;
        if (isPurchased(state, nodeId)) return false;
        if (state.heartwood < def.cost()) return false;
        for (String req : def.requires()) {
            if (!isPurchased(state, req)) return false;
        }
        return true;
    }

    public boolean purchase(GameState state, String nodeId) {
        if (!canPurchase(state, nodeId)) {
            RootNodeDefinition def = RootNetworkCatalog.byId(nodeId);
            if (def != null && state != null && state.heartwood < def.cost()) {
                showFeedback(GameLocale.text(
                    RootNetworkStrings.FEEDBACK_NEED,
                    GameLocale.number(def.cost() - state.heartwood)));
            }
            return false;
        }
        RootNodeDefinition def = RootNetworkCatalog.byId(nodeId);
        state.heartwood -= def.cost();
        state.rootNodesPurchased.put(nodeId, true);
        // Upper-casing is a no-op on Persian and keeps the node name in the caps sentence it sits in for
                // English, so the call stays: the name is content from the catalog, not an entry that could be
                // written in caps at the table.
                showFeedback(GameLocale.text(
                    RootNetworkStrings.FEEDBACK_AWAKENED, def.name().toUpperCase(Locale.ROOT)));
        return true;
    }

    /** Run start: every owned node applies once to the fresh hero, who wakes at full health. */
    public void applyPermanentBonuses(GameState state) {
        if (state == null || state.hero == null) return;
        float extraMax = 0f;
        for (Map.Entry<String, Boolean> e : state.rootNodesPurchased.entrySet()) {
            if (e.getValue() == null || !e.getValue()) continue;
            RootNodeDefinition def = RootNetworkCatalog.byId(e.getKey());
            if (def != null) extraMax += applyNode(state, e.getKey(), def);
        }
        float boosted = extraMax * TrialEffects.heroMaxHealthMultiplier(state.activeTrials);
        state.synchronizeEquipmentHealth();
        state.hero.maxHealth += boosted;
        state.hero.health = state.hero.maxHealth;
        state.worldTreeMaxHealth = 1000f + extraMax * 0.5f;
        state.worldTreeHealth = state.worldTreeMaxHealth;
    }

    /** One mid-run purchase: only this node's stats, health moves by the delta, the tree is never healed. */
    public void applyNodeDuringRun(GameState state, String nodeId) {
        if (state == null || state.hero == null) return;
        RootNodeDefinition def = RootNetworkCatalog.byId(nodeId);
        if (def == null || !isPurchased(state, nodeId)) return;
        float previousMax = state.hero.maxHealth;
        applyNode(state, nodeId, def);
        float boosted = ownedFlatMaxBonus(state) * TrialEffects.heroMaxHealthMultiplier(state.activeTrials);
        state.synchronizeEquipmentHealth();
        state.hero.maxHealth += boosted;
        state.hero.health = Math.min(state.hero.maxHealth,
            state.hero.health + Math.max(0f, state.hero.maxHealth - previousMax));
        state.worldTreeMaxHealth = 1000f + ownedFlatMaxBonus(state) * 0.5f;
    }

    /** Applies one node's stats; returns its flat max-health bonus. */
    private float applyNode(GameState state, String id, RootNodeDefinition def) {
        if ("root_all_1".equals(id)) {
            state.hero.stats.strength += 1;
            state.hero.stats.agility += 1;
            state.hero.stats.luck += 1;
            state.hero.stats.dodge += 1;
            state.hero.stats.health += 1;
        } else if ("root_all_2".equals(id)) {
            state.hero.stats.strength += 2;
            state.hero.stats.agility += 2;
            state.hero.stats.luck += 2;
            state.hero.stats.dodge += 2;
            state.hero.stats.health += 2;
            state.coins += 100;
        } else if ("root_heart_1".equals(id)) {
            state.unspentTalentPoints += 1;
        } else {
            applyBonus(state, def);
        }
        return flatMaxBonus(id, def);
    }

    private static float flatMaxBonus(String id, RootNodeDefinition def) {
        if ("root_heart_1".equals(id)) return def.bonusAmount();
        return def.bonusType() == RootNodeBonusType.MAX_HEALTH_BONUS ? def.bonusAmount() : 0f;
    }

    private static float ownedFlatMaxBonus(GameState state) {
        float sum = 0f;
        for (Map.Entry<String, Boolean> e : state.rootNodesPurchased.entrySet()) {
            if (e.getValue() == null || !e.getValue()) continue;
            RootNodeDefinition def = RootNetworkCatalog.byId(e.getKey());
            if (def != null) sum += flatMaxBonus(e.getKey(), def);
        }
        return sum;
    }

    private void applyBonus(GameState state, RootNodeDefinition def) {
        switch (def.bonusType()) {
            case STARTING_STRENGTH -> state.hero.stats.strength += def.bonusAmount();
            case STARTING_AGILITY -> state.hero.stats.agility += def.bonusAmount();
            case STARTING_LUCK -> state.hero.stats.luck += def.bonusAmount();
            case STARTING_DODGE -> state.hero.stats.dodge += def.bonusAmount();
            case STARTING_HEALTH -> state.hero.stats.health += def.bonusAmount();
            case STARTING_COIN -> state.coins += def.bonusAmount();
            case STARTING_TALENT_POINT -> state.unspentTalentPoints += def.bonusAmount();
            case FOCUS_FILL_BONUS -> state.focusMax += def.bonusAmount();
            case MAX_HEALTH_BONUS -> { /* handled in caller */ }
        }
    }

    /** One line of feedback for the shop overlay; the node id is already carried by the row that was tapped. */
    private void showFeedback(String msg) {
        feedbackMessage = msg;
        feedbackRemaining = FEEDBACK_SECONDS;
    }
}
