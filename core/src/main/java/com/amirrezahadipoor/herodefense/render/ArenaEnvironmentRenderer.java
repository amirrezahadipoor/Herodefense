package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.gameplay.ArenaLayout;
import com.amirrezahadipoor.herodefense.gameplay.ArenaTerrain;
import com.amirrezahadipoor.herodefense.model.ArenaObstacle;
import com.amirrezahadipoor.herodefense.model.GameState;

/** Draws the reviewed Blender-rendered forest floor, props, and defended World Tree. */
public final class ArenaEnvironmentRenderer implements AutoCloseable {
    private static final float TREE_SIZE = 330f;
    private static final float TREE_FEET_OFFSET = 31f;

    // X, Y, width, height, variant, shade. Edge patches frame rather than stripe the lane.
    private static final float[][] GROUND_PLACEMENTS = {
        {-72f, 20f, 292f, 190f, 0f, 0.96f},
        {500f, 34f, 286f, 186f, 1f, 0.95f},
        {-58f, 318f, 266f, 176f, 2f, 0.90f},
        {516f, 368f, 258f, 171f, 0f, 0.88f},
        {-50f, 640f, 244f, 164f, 1f, 0.83f},
        {528f, 696f, 236f, 159f, 2f, 0.81f},
        {-42f, 954f, 224f, 153f, 0f, 0.76f},
        {540f, 1000f, 216f, 148f, 1f, 0.73f}
    };

    // X, Y, draw size, variant. Smaller upper props reinforce portrait depth.
    private static final float[][] CRYSTAL_PLACEMENTS = {
        {-8f, 120f, 172f, 0f},
        {556f, 205f, 164f, 1f},
        {4f, 820f, 148f, 2f},
        {568f, 884f, 136f, 0f}
    };

    private final Texture backdrop;
    private final Texture backdrop2;
    private final Texture[] ground = new Texture[6];
    /** The four cover families the render batch produces, in the order their textures are loaded. */
    static final String[] COVER_FAMILIES = {"standing_stone", "ruin_slab", "thorn_hedge", "mossy_boulder"};
    static final int COVER_VARIANTS = 3;
    static final int FAMILY_STANDING_STONE = 0;
    static final int FAMILY_RUIN_SLAB = 1;
    static final int FAMILY_THORN_HEDGE = 2;
    static final int FAMILY_MOSSY_BOULDER = 3;
    /** The cover sprite paints about two thirds of the width the landmark art painted, so its box is larger. */
    static final float COVER_FRAME_SCALE = 1.55f;
    /** The share of the box between its lower edge and the painted base, measured on the rendered sprites. */
    static final float COVER_BASE_SHARE = 0.23f;
    /** How far below the outcrop's centre its painted base stands, as a share of the collision radius. */
    static final float COVER_BASE_BELOW_RADIUS = 0.87f;
    private final Texture[] crystals = new Texture[6];
    /** The field's own cover: four families of three variants, loaded once and never per obstacle. */
    private final Texture[] cover = new Texture[COVER_FAMILIES.length * COVER_VARIANTS];
    private final TextureAtlas healthyTreeAtlas;
    private final TextureAtlas damagedTreeAtlas;
    private final Array<TextureAtlas.AtlasRegion> healthyTreeFrames;
    private final Array<TextureAtlas.AtlasRegion> damagedTreeFrames;
    private final Array<TextureAtlas.AtlasRegion> destroyedTreeFrames;
    private final WorldTreeAnimationController treeAnimation =
        new WorldTreeAnimationController();
    private final ArenaAtmosphereRenderer atmosphere = new ArenaAtmosphereRenderer();
    private final GroundShadowRenderer shadowRenderer = new GroundShadowRenderer();
    private final DawnReveal dawnReveal = new DawnReveal();
    private final DawnGlowRenderer dawnGlow = new DawnGlowRenderer();
    private final HollowGaze hollowGaze = new HollowGaze();
    private final HollowGazeRenderer hollowGazeRenderer = new HollowGazeRenderer();
    /** The state the dawn clock is ticking for; a new state is a new run, and a new run is night. */
    private GameState dawnState;

    public ArenaEnvironmentRenderer() {
        backdrop = texture("generated/environment/arena_backdrop.png");
        backdrop2 = texture("generated/environment/arena_backdrop_2.png");
        for (int index = 0; index < 6; index++) {
            ground[index] = texture("generated/environment/ground_tile_" + index + ".png");
            crystals[index] = texture("generated/environment/crystal_prop_" + index + ".png");
        for (int family = 0; family < COVER_FAMILIES.length; family++) {
            for (int variant = 0; variant < COVER_VARIANTS; variant++) {
                cover[family * COVER_VARIANTS + variant] = texture(
                    "generated/environment/obstacle_" + COVER_FAMILIES[family] + "_" + variant + ".png"
                );
            }
        }
        }
        healthyTreeAtlas = SheetPayloads.atlas(
            Gdx.files.internal("generated/sprites/world_tree_healthy.atlas")
        );
        damagedTreeAtlas = SheetPayloads.atlas(
            Gdx.files.internal("generated/sprites/world_tree_damaged.atlas")
        );
        healthyTreeFrames = requireFrames(
            healthyTreeAtlas,
            "world_tree_healthy_idle",
            WorldTreeAnimationController.IDLE_FRAME_COUNT
        );
        damagedTreeFrames = requireFrames(
            damagedTreeAtlas,
            "world_tree_damaged_idle",
            WorldTreeAnimationController.IDLE_FRAME_COUNT
        );
        destroyedTreeFrames = requireFrames(
            damagedTreeAtlas,
            "world_tree_damaged_destroy",
            WorldTreeAnimationController.DESTROY_FRAME_COUNT
        );
    }

    public void draw(
        SpriteBatch batch,
        GameState state,
        float runTimeSeconds,
        float presentationDeltaSeconds,
        boolean motionSuppressed,
        boolean gameOverActive
    ) {
        // D1: second arena — forest for waves 1-100, hollow for 101-200, matching StageGrade TEAL/HOLLOW.
        Texture activeBackdrop = state.waveNumber >= 101 ? backdrop2 : backdrop;
        ScreenEdges.drawCover(batch, activeBackdrop);
        drawHollowGaze(batch, state, runTimeSeconds, presentationDeltaSeconds, motionSuppressed, gameOverActive);
        drawGround(batch, state);
        drawCrystals(batch, state.waveNumber);
        // The field's outcrops come with their own contact shadows, drawn on the ground and under their sprites:
        // the shadow is what says the crystal is solid rather than painted on.
        shadowRenderer.drawField(batch, state);
        drawFieldObstacles(batch, state);
        shadowRenderer.draw(batch, state);
        drawWorldTree(batch, state, runTimeSeconds, presentationDeltaSeconds);
        // D4: the air and the bosses' ground auras, over the finished arena and under the actors.
        // A suppressed-motion frame gets a frozen clock, which holds both effects on a calm still.
        atmosphere.draw(batch, state, motionSuppressed ? 0f : runTimeSeconds, motionSuppressed);
        drawDawn(batch, state, runTimeSeconds, presentationDeltaSeconds, motionSuppressed, gameOverActive);
    }

    /**
     * The night in the story is watching: the Hollow's two lights live in the dark upper field of
     * the HOLLOW arena, deep in the backdrop under everything else. They sharpen while a boss
     * stands, blink shut when the tree falls, and drift away into the dawn when the run is won.
     * The forest arena (waves 1-100) never sees them.
     */
    private void drawHollowGaze(
        SpriteBatch batch, GameState state, float runTimeSeconds,
        float presentationDeltaSeconds, boolean motionSuppressed, boolean gameOverActive
    ) {
        boolean bossAlive = false;
        for (var boss : state.aliveBosses) {
            if (boss != null && boss.alive) {
                bossAlive = true;
                break;
            }
        }
        // The night stands watch during the fight; only on the game-over screen does it react --
        // blink shut for a defeat, drift away for a dawn.
        hollowGaze.update(
            state.waveNumber, bossAlive, state.hero.alive, state.runComplete,
            gameOverActive ? presentationDeltaSeconds : 0f, motionSuppressed
        );
        if (!hollowGaze.present()) {
            return;
        }
        float strength = hollowGaze.strength(bossAlive, motionSuppressed ? 0f : runTimeSeconds, motionSuppressed);
        hollowGazeRenderer.draw(batch, strength, hollowGaze.drift());
    }

    /**
     * The arc's last colour: when the run is complete, the HOLLOW's night sky breaks into dawn gold
     * over the premium summary. The clock ticks on the presentation delta -- the simulation is
     * stopped at this point -- and a new state object is a new run, which starts in the dark again.
     * A suppressed-motion frame still dawns: the sunrise advances, it just stops breathing.
     */
    private void drawDawn(
        SpriteBatch batch, GameState state, float runTimeSeconds,
        float presentationDeltaSeconds, boolean motionSuppressed, boolean gameOverActive
    ) {
        if (dawnState != state) {
            dawnState = state;
            dawnReveal.reset();
        }
        // The sunrise begins on the victory screen, not earlier: runComplete is already true
        // while the final reward cards are chosen, and the dawn belongs to the summary.
        if (!state.runComplete || !gameOverActive) {
            return;
        }
        dawnReveal.tick(presentationDeltaSeconds);
        dawnGlow.draw(batch, dawnReveal.eased(), motionSuppressed ? 0f : runTimeSeconds);
    }

    /** Returns true for the second arena variant (hollow), used for waves 101-200. */
    public static boolean isSecondArena(int wave) {
        return wave >= 101;
    }

    /** R5.4: the ground is drawn *under* the stage's grade, not filtered after the frame is finished. */
    private void drawGround(SpriteBatch batch, GameState state) {
        int wave = state.waveNumber;
        StageGrade grade = StageGrade.forWave(wave);
        float originalColor = batch.getPackedColor();
        boolean second = isSecondArena(wave);
        // The layout picks the mix of ground sheets: the same tiles the arena always grew, in another order, so a
        // field reads as another place before a single obstacle is looked at.
        int variantOffset = (second ? 3 : 0) + ArenaLayout.forSeed(state.runSeed).ordinal();
        for (float[] placement : GROUND_PLACEMENTS) {
            float shade = placement[5];
            batch.setColor(
                grade.channel(shade * 0.96f, 0),
                grade.channel(shade, 1),
                grade.channel(shade * 0.97f, 2),
                0.96f
            );
            int variant = Math.floorMod(Math.round(placement[4]) + variantOffset, ground.length);
            batch.draw(
                ground[variant], placement[0], placement[1], placement[2], placement[3]
            );
        }
        batch.setPackedColor(originalColor);
    }

    /**
     * The run's solid outcrops.
     *
     * <p>A layout's cover is drawn with the same reviewed crystal art the arena always grew, at the size its
     * collision circle demands, with the variant rotated by the layout so two fields do not read as one place
     * twice. What a player has to be able to tell apart is solid from painted, and the shadow under each of these
     * is the only difference this pass needs.
     */
    private void drawFieldObstacles(SpriteBatch batch, GameState state) {
        if (state == null) {
            return;
        }
        boolean second = isSecondArena(state.waveNumber);
        ArenaLayout layout = ArenaLayout.forSeed(state.runSeed);
        for (ArenaObstacle obstacle : ArenaTerrain.fieldFor(state)) {
            int family = coverFamily(layout, obstacle.shelters);
            int variant = Math.floorMod(obstacle.variant + (second ? 1 : 0), COVER_VARIANTS);
            float size = obstacle.drawn * COVER_FRAME_SCALE;
            // The painted base sits this far up from the box's lower edge, and the box is placed so that base
            // lands where the landmark art's own footprint did: the ground shadows were measured against that,
            // so the cover had to arrive on the same line rather than re-derive it.
            float baseOffset = size * COVER_BASE_SHARE;
            batch.draw(
                cover[family * COVER_VARIANTS + variant],
                obstacle.x - size * 0.5f,
                obstacle.y - obstacle.radius * COVER_BASE_BELOW_RADIUS - baseOffset,
                size,
                size
            );
        }
    }

    /**
     * Which family stands on this field, for this kind of outcrop: one row per layout, standing first.
     *
     * <p>Two answers per layout, and no layout shows a family it has no outcrop of. The point of the table is that
     * a field is recognisable before a single body walks in: the ring's shelter is a broken wall, the hedge is
     * thorned, the open hearth is boulders, and the standing stones are stones. It is a table rather than a switch
     * because two layouts legitimately share a pair, and a switch would say the same thing four times over.
     */
    private static final int[][] COVER_BY_LAYOUT = {
        // Open Hearth: two low boulders, nothing to hide a flank behind.
        {FAMILY_STANDING_STONE, FAMILY_MOSSY_BOULDER},
        // Standing Stones: monoliths, the field the tall cover names.
        {FAMILY_STANDING_STONE, FAMILY_MOSSY_BOULDER},
        // Thornhedge: a low thorn row, and a broken wall where a shelter is asked for.
        {FAMILY_RUIN_SLAB, FAMILY_THORN_HEDGE},
        // Ruined Ring: a broken wall to stand behind, thorned rubble to walk around.
        {FAMILY_RUIN_SLAB, FAMILY_THORN_HEDGE},
    };

    /** The family index for one layout and kind of cover. Standing cover is column 0, low cover column 1. */
    static int coverFamily(ArenaLayout layout, boolean shelters) {
        return COVER_BY_LAYOUT[layout.ordinal()][shelters ? 0 : 1];
    }

    private void drawCrystals(SpriteBatch batch, int wave) {
        boolean second = isSecondArena(wave);
        int variantOffset = second ? 3 : 0;
        for (float[] placement : CRYSTAL_PLACEMENTS) {
            float size = placement[2];
            int variant = Math.round(placement[3]) + variantOffset;
            batch.draw(crystals[variant], placement[0], placement[1], size, size);
        }
    }

    private void drawWorldTree(
        SpriteBatch batch,
        GameState state,
        float runTimeSeconds,
        float presentationDeltaSeconds
    ) {
        WorldTreeAnimationController.Selection selection = treeAnimation.select(
            state, runTimeSeconds, presentationDeltaSeconds
        );
        Array<TextureAtlas.AtlasRegion> frames = switch (selection.state()) {
            case HEALTHY -> healthyTreeFrames;
            case DAMAGED -> damagedTreeFrames;
            case DESTROYING, DESTROYED -> destroyedTreeFrames;
        };
        batch.draw(
            frames.get(selection.frameIndex()),
            WorldLayout.WORLD_TREE_X - TREE_SIZE * 0.5f,
            WorldLayout.WORLD_TREE_Y - TREE_FEET_OFFSET,
            TREE_SIZE,
            TREE_SIZE
        );
    }

    private static Texture texture(String path) {
        Texture texture = SheetPayloads.texture(path);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private static Array<TextureAtlas.AtlasRegion> requireFrames(
        TextureAtlas atlas,
        String region,
        int expected
    ) {
        Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions(region);
        if (frames.size != expected) {
            atlas.dispose();
            throw new IllegalStateException(
                "Expected " + expected + " frames for " + region + ", found " + frames.size
            );
        }
        return frames;
    }

    @Override
    public void close() {
        backdrop.dispose();
        backdrop2.dispose();
        for (Texture texture : ground) texture.dispose();
        for (Texture texture : crystals) texture.dispose();
        for (Texture texture : cover) texture.dispose();
        dawnGlow.close();
        hollowGazeRenderer.close();
        healthyTreeAtlas.dispose();
        damagedTreeAtlas.dispose();
        atmosphere.close();
        shadowRenderer.close();
    }
}
