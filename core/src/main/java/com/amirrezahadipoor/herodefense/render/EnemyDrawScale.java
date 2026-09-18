package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.model.EnemyType;

/**
 * Drawn size of each regular enemy, in world units.
 *
 * <p>Extracted from {@code CombatEntityRenderer} when R3.4 doubled the roster: the four additions pushed that
 * file past its architecture-ratchet limit, and a size table belongs next to the art decisions rather than
 * inside the frame loop. The switch stays exhaustive on purpose — a ninth role will not compile until someone
 * authors the size its silhouette was drawn for.
 *
 * <p>Drawn size follows silhouette, not health: the wiry Bark Stalker reads smaller than the Fungal Brute it
 * shares a wave with, and the Sap Hound stays low and wide.
 */
final class EnemyDrawScale {
    private EnemyDrawScale() {
    }

    static float of(EnemyType type) {
        return switch (type) {
            case ROOTLING -> 148f;
            case STONEKIN -> 166f;
            case GLOOM_WOLF -> 158f;
            case FUNGAL_BRUTE -> 178f;
            case BARK_STALKER -> 152f;
            case SAP_HOUND -> 150f;
            case HUSK_WARDEN -> 158f;
            case BRAMBLE_THRALL -> 172f;
        };
    }
}
