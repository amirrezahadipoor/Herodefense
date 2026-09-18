package com.amirrezahadipoor.herodefense;

/** Fixed portrait world-space contract used by rendering, touch hit tests, and assets. */
public final class WorldLayout {
    public static final float REFERENCE_WIDTH = 720f;
    public static final float REFERENCE_HEIGHT = 1280f;
    public static final float HERO_CENTER_X = REFERENCE_WIDTH * 0.5f;
    public static final float HERO_CENTER_Y = 600f;
    public static final float WORLD_TREE_X = HERO_CENTER_X;
    public static final float WORLD_TREE_Y = 755f;
    /** The second Heartwood planted at Wave 100 stands to the right of the first. */
    public static final float SECOND_TREE_X = 578f;
    public static final float SECOND_TREE_Y = 738f;
    /** Grove site for the Wave-50 planting (left flank, reuses sapling atlas). */
    public static final float GROVE_TREE_50_X = 142f;
    public static final float GROVE_TREE_50_Y = 738f;
    /** Grove site for the Wave-150 planting (mid-right, reuses sapling atlas). */
    public static final float GROVE_TREE_150_X = 360f + 118f;
    public static final float GROVE_TREE_150_Y = 738f;

    /**
     * The band the Hero may step inside (roadmap A1).
     *
     * <p>Portrait world space is 720 by 1280 with y up: the utility HUD row (inventory, shop, ultimate) sits at
     * y 24 with a 104-unit height, the status row (speed, pause) at y 1065 with a 100-unit height, and the
     * Heartwood line at y 738 to 755. The band therefore keeps a margin clear of both HUD rows and stops below
     * the trees, so the defender can meet a wave on either flank but cannot walk behind the thing being
     * defended or under a button. Enemies spawn off-frame (x -40, x 760, y -40) and walk in, so nothing about
     * the spawn lanes depends on these four numbers.
     *
     * <p>There is no obstacle collision anywhere in this arena -- enemies walk through the trees and through
     * each other today -- so the Hero is clamped to a rectangle and to nothing else. Giving the Hero collision
     * the field does not have would make the defender the only body in the game that can be blocked by a trunk.
     */
    public static final float HERO_WALK_MIN_X = 72f;
    public static final float HERO_WALK_MAX_X = REFERENCE_WIDTH - 72f;
    public static final float HERO_WALK_MIN_Y = 168f;
    public static final float HERO_WALK_MAX_Y = 700f;

    /** Returns the world position for a planted grove tree by its planting order (0 = 50, 1 = 100, 2 = 150). */
    public static float groveTreeX(int index) {
        return switch (index) {
            case 0 -> GROVE_TREE_50_X;
            case 1 -> SECOND_TREE_X;
            case 2 -> GROVE_TREE_150_X;
            default -> SECOND_TREE_X;
        };
    }

    public static float groveTreeY(int index) {
        return switch (index) {
            case 0 -> GROVE_TREE_50_Y;
            case 1 -> SECOND_TREE_Y;
            case 2 -> GROVE_TREE_150_Y;
            default -> SECOND_TREE_Y;
        };
    }

    private WorldLayout() {
    }
}
