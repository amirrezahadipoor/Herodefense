package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.model.HeroAnimationState;
import com.amirrezahadipoor.herodefense.model.Item;

import java.util.List;
import java.util.Locale;

/** Naming bridge between equipped item records and generated attachment atlases. */
public final class EquipmentVisualContract {
    private EquipmentVisualContract() {
    }

    public static String atlasPath(String itemId) {
        return "generated/equipment/" + itemId + ".atlas";
    }

    public static String regionName(Item item, HeroAnimationState state) {
        return artId(item) + "_" + state.name().toLowerCase(Locale.ROOT);
    }

    /**
     * The clips a renderer may draw for a state, the state's own first. The attachment sheets were rendered
     * with idle, attack, hit and death; the walk cycle (roadmap E2) arrived later and no sheet carries one,
     * so a walking Hero wears its idle attachment -- the rule {@code HeroSpriteRenderer} already applies to
     * its own missing walk clip. Before this list existed the renderer asked the sheet for
     * {@code <art>_walk}, found nothing and threw, which made the first step of every run a crash: every run
     * starts with the ashwood bow equipped. The other four states keep no fallback -- a sheet missing one of
     * the clips it was rendered with is a broken sheet, not a known gap.
     */
    public static List<String> regionCandidates(Item item, HeroAnimationState state) {
        String own = regionName(item, state);
        if (state == HeroAnimationState.WALK) {
            return List.of(own, regionName(item, HeroAnimationState.IDLE));
        }
        return List.of(own);
    }

    /** Atlas region prefix: derived from the item's visual atlas so borrowed art resolves. */
    static String artId(Item item) {
        String key = item.visualKey;
        if (key != null && key.startsWith("generated/equipment/") && key.endsWith(".atlas")) {
            return key.substring("generated/equipment/".length(), key.length() - ".atlas".length());
        }
        return item.id;
    }
}
