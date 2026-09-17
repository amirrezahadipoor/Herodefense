package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.items.EquipmentDefinition;
import com.amirrezahadipoor.herodefense.model.DropEntity;
import com.amirrezahadipoor.herodefense.potions.PotionTier;

import java.util.HashMap;
import java.util.Map;

/**
 * The textures a drop on the ground is drawn with, loaded once each (roadmap R2.2 extraction, R8.3 residency).
 *
 * <p>Extracted from {@code CombatEntityRenderer} so the release pass (R8.3) could land inside a class the
 * architecture ratchet freezes: the cache is a cache, it owns nothing else, and it closes itself.
 */
final class DropTextureCache implements AutoCloseable {
    private final Map<String, Texture> textures = new HashMap<>();

    /** The texture for one drop, loading it on first sight. */
    Texture textureFor(DropEntity drop) {
        String path = pathFor(drop);
        Texture cached = textures.get(path);
        if (cached != null) {
            return cached;
        }
        Texture texture = load(path);
        textures.put(path, texture);
        return texture;
    }

    private static String pathFor(DropEntity drop) {
        if ("POTION".equals(drop.dropType)) {
            try {
                if (drop.itemId == null) throw new IllegalArgumentException("missing potion tier");
                return PotionTier.valueOf(drop.itemId).iconPath();
            } catch (IllegalArgumentException ignored) {
                return PotionTier.TIER_1.iconPath();
            }
        }
        if ("ITEM".equals(drop.dropType)) {
            EquipmentDefinition item = EquipmentCatalog.byId(drop.itemId);
            if (item != null) return item.iconPath();
            return "generated/icons/ui_inventory.png";
        }
        return "generated/icons/ui_coin.png";
    }

    private static Texture load(String path) {
        Texture texture = SheetPayloads.texture(path);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    @Override
    public void close() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
    }
}
