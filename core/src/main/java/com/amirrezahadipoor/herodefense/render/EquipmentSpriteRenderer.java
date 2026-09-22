package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.amirrezahadipoor.herodefense.model.EquipmentSlot;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.HeroAnimationState;
import com.amirrezahadipoor.herodefense.model.Item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** Lazily draws only the currently equipped, Hero-rig-aligned attachment sheets. */
public final class EquipmentSpriteRenderer implements AutoCloseable {
    // Phase 29.4: only boots + weapon render on the hero for silhouette readability.
    private static final EquipmentSlot[] LAYER_ORDER = {
        EquipmentSlot.BOOTS,
        EquipmentSlot.WEAPON
    };

    private final Map<String, TextureAtlas> loadedAtlases = new HashMap<>();
    private final RarityGlowRenderer rarityGlowRenderer = new RarityGlowRenderer();

    /** Bow aura multiplier from raw progression: 1.0 unworked, 2.2 at the cap. */
    static float progressionGlowMultiplier(GameState state) {
        return 1f + CombatEntityRenderer.progressionStep(state) * 0.12f;
    }

    public void draw(SpriteBatch batch, GameState state, int frameIndex, float runTimeSeconds) {
        Set<String> activeIds = new HashSet<>();
        float bowGlow = progressionGlowMultiplier(state);
        for (EquipmentSlot slot : LAYER_ORDER) {
            Item item = state.equippedItems.get(slot.name());
            if (item == null || item.id == null || item.id.isEmpty()) {
                continue;
            }
            activeIds.add(item.id);
            TextureAtlas atlas = loadedAtlases.get(item.id);
            if (atlas == null) {
                String atlasPath = item.visualKey == null || item.visualKey.isEmpty()
                    ? EquipmentVisualContract.atlasPath(item.id)
                    : item.visualKey;
                atlas = SheetPayloads.atlas(Gdx.files.internal(atlasPath));
                loadedAtlases.put(item.id, atlas);
            }
            Array<TextureAtlas.AtlasRegion> frames = clipFor(atlas, item, state.hero.animationState);
            // A clip's own frame index is always inside the clip; the walk fallback loops the shorter idle
            // clip under the eight-frame walk count, the way the Hero's own walk fallback loops it.
            TextureAtlas.AtlasRegion frame = frames.get(Math.floorMod(Math.max(0, frameIndex), frames.size));
            rarityGlowRenderer.draw(
                batch,
                frame,
                HeroSpriteRenderer.frameX(state.hero),
                HeroSpriteRenderer.frameY(state.hero) + HeroSpriteRenderer.walkBob(state.hero),
                HeroSpriteRenderer.FRAME_SIZE,
                HeroSpriteRenderer.FRAME_SIZE,
                VisualRarity.fromTier(item.tier),
                runTimeSeconds,
                slot == EquipmentSlot.WEAPON ? bowGlow : 1f
            );
        }
        disposeUnequipped(activeIds);
    }

    /**
     * The first clip the sheet has for the state, in the contract's order: the state's own clip, then --
     * for a walking Hero only -- the idle clip the sheets were all rendered with. Only a sheet that has
     * neither is broken, and that is the one case that still throws.
     */
    static Array<TextureAtlas.AtlasRegion> clipFor(TextureAtlas atlas, Item item, HeroAnimationState state) {
        for (String regionName : EquipmentVisualContract.regionCandidates(item, state)) {
            Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions(regionName);
            if (frames.size > 0) {
                return frames;
            }
        }
        throw new IllegalStateException(
            "Missing equipment clip: " + EquipmentVisualContract.regionName(item, state));
    }

    private void disposeUnequipped(Set<String> activeIds) {
        Iterator<Map.Entry<String, TextureAtlas>> iterator = loadedAtlases.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TextureAtlas> entry = iterator.next();
            if (!activeIds.contains(entry.getKey())) {
                entry.getValue().dispose();
                iterator.remove();
            }
        }
    }

    @Override
    public void close() {
        for (TextureAtlas atlas : loadedAtlases.values()) {
            atlas.dispose();
        }
        loadedAtlases.clear();
        rarityGlowRenderer.close();
    }
}
