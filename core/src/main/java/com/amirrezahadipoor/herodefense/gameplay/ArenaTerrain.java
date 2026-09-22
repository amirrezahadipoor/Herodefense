package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.model.ArenaObstacle;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.Hero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The solid ground of a run: where the outcrops stand, and what they stop.
 *
 * <p>Before this, nothing in the arena could be touched. The Hero was clamped to a rectangle, an arrow flew to
 * whatever it was aimed at, and an enemy walked through a trunk, a crystal and another enemy. A layout is only
 * real if the field is solid for everyone, so the rule is the same for all three: {@link #move} pushes a body out
 * of an outcrop, and {@link #firstBlocker} says which outcrop a flight path crosses.
 *
 * <p>Three things are deliberately left clear. The spawn mouths, because a body that materialises inside a stone
 * is stuck off-frame where the player cannot even see it. The Hero's starting ground, because the first order a
 * new player gives should not fail. And the outcrops stay out of each other's way by a margin, so a pocket can
 * never become a wall with no gap in it -- {@code ArenaTerrainTest} walks the whole band on a grid and proves,
 * for every layout and a hundred seeds, that the Hero's ground is still one connected place.
 *
 * <p>The layout is a pure function of the run seed, so the arena is recomputed rather than stored: no save gains
 * a field, and the same save is always the same field.
 */
public final class ArenaTerrain {

    /** Outcrops stay this far inside the band the Hero may walk in. */
    static final float EDGE_MARGIN = 104f;
    /** The ground around the Hero's start that stays clear of new outcrops. */
    static final float HERO_CLEARANCE = 150f;
    /** Nothing may be planted nearer than this to a spawn mouth. */
    static final float SPAWN_CLEARANCE = 150f;
    /** The gap kept between two outcrops, on top of both radii. */
    static final float POCKET_GAP = 72f;
    /** The band of the field that carries the layouts: below the Hero's road and above the frame edge. */
    static final float FIELD_MIN_Y = 300f;
    static final float FIELD_MAX_Y = 492f;
    /** How far an anchor may wander from its anchor point, so two runs are two fields and not two copies. */
    static final float JITTER = 10f;

    private static final float TREE_CLEARANCE = 96f;
    /** The Hero's collision radius, a little tighter than the body the sprite paints. */
    static final float HERO_BODY_RADIUS = 26f;
    /** A foe's collision radius: smaller than the Hero's, because a swarm has to be able to file through a gap. */
    static final float ENEMY_BODY_RADIUS = 16f;
    /** How much ground a flight path needs: an arrow is stopped by the outcrop, not by its painted edge. */
    public static final float ARROW_RADIUS = 6f;
    /** One shared result pair, so a moving body costs no allocation. Private: nothing may alias it. */
    private static final float[] RESOLVED = new float[2];

    private static List<ArenaObstacle> cachedField = Collections.emptyList();
    private static long cachedSeed = Long.MIN_VALUE;
    private static ArenaLayout cachedLayout;

    private ArenaTerrain() {
    }

    /** The field this state is fought on. One slot of cache: the balance simulator walks many seeds in a run. */
    public static List<ArenaObstacle> fieldFor(GameState state) {
        if (state == null) {
            return Collections.emptyList();
        }
        ArenaLayout layout = ArenaLayout.forSeed(state.runSeed);
        if (layout != cachedLayout || state.runSeed != cachedSeed) {
            cachedField = build(layout, state.runSeed);
            cachedLayout = layout;
            cachedSeed = state.runSeed;
        }
        return cachedField;
    }

    /** Builds a layout for a seed, without touching the cache. Deterministic and side-effect free. */
    public static List<ArenaObstacle> build(ArenaLayout layout, long seed) {
        Random random = new Random(seed ^ (0x5DEECE66DL * (layout.ordinal() + 1)));
        List<ArenaObstacle> field = new ArrayList<>();
        switch (layout) {
            // Open ground: two stones, one a side, far enough out that a run on this field is a run about aim.
            case OPEN_HEARTH -> {
                add(field, 200f, 320f, 28f, false, random);
                add(field, 520f, 320f, 28f, false, random);
            }
            // A pair of standing stones each side, tall enough to hide a flank behind.
            case STANDING_STONES -> {
                add(field, 176f, 316f, 36f, true, random);
                add(field, 176f, 488f, 32f, true, random);
                add(field, 544f, 316f, 36f, true, random);
                add(field, 544f, 488f, 32f, true, random);
            }
            // Small stones walked in a line: cover you can shoot over at one angle and lose the arrow at the next.
            case THORNHEDGE -> {
                add(field, 176f, 322f, 22f, false, random);
                add(field, 272f, 402f, 22f, false, random);
                add(field, 448f, 402f, 22f, false, random);
                add(field, 544f, 322f, 22f, false, random);
            }
            // A ring around the hearth, broken exactly where the three roads run through it.
            case RUINED_RING -> {
                add(field, 176f, 492f, 30f, true, random);
                add(field, 288f, 403f, 30f, false, random);
                add(field, 432f, 403f, 30f, false, random);
                add(field, 544f, 492f, 30f, true, random);
                add(field, 176f, 306f, 26f, true, random);
                add(field, 544f, 306f, 26f, true, random);
            }
        }
        return Collections.unmodifiableList(field);
    }

    /**
     * Plants one outcrop at its anchor, letting the placement breathe where the seed's jitter crowds something.
     *
     * <p>Dropping a crowded outcrop instead would make the field's density a lottery: a layout would be a ring on
     * one seed and three stones on the next. So a crowded anchor is pushed away from whatever it crowds -- the
     * outcrop, the tree, the Hero's first ground or a spawn mouth -- until it is legal, and only an anchor that
     * cannot escape inside its own band is left out.
     */
    private static void add(
        List<ArenaObstacle> field, float anchorX, float anchorY, float radius, boolean shelters, Random random
    ) {
        float x = clampFieldX(anchorX + (random.nextFloat() * 2f - 1f) * JITTER);
        float y = clampFieldY(anchorY + (random.nextFloat() * 2f - 1f) * JITTER);
        int variant = random.nextInt(6);
        for (int attempt = 0; attempt < 8; attempt++) {
            ArenaObstacle candidate = new ArenaObstacle(x, y, radius, radius * 3.1f,
                radius * (shelters ? 2.2f : 1.15f), variant, shelters);
            float[] push = crowdPush(candidate, field);
            if (push == null) {
                field.add(candidate);
                return;
            }
            x = clampFieldX(x + push[0]);
            y = clampFieldY(y + push[1]);
        }
    }

    /** Keeps an outcrop inside the band the Hero may walk, with room left around it. */
    private static float clampFieldX(float x) {
        return Math.max(WorldLayout.HERO_WALK_MIN_X + EDGE_MARGIN,
            Math.min(WorldLayout.HERO_WALK_MAX_X - EDGE_MARGIN, x));
    }

    /** Keeps an outcrop inside the layout band, which is where a field's cover is allowed to stand. */
    private static float clampFieldY(float y) {
        return Math.max(FIELD_MIN_Y, Math.min(FIELD_MAX_Y, y));
    }

    /** How far a crowded outcrop has to move to be legal, or null when it already is. */
    private static float[] crowdPush(ArenaObstacle candidate, List<ArenaObstacle> field) {
        for (ArenaObstacle other : field) {
            float limit = candidate.radius + other.radius + POCKET_GAP;
            float dx = candidate.x - other.x;
            float dy = candidate.y - other.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < limit) {
                return away(candidate, other.x, other.y, limit - distance + 1f, distance);
            }
        }
        float[] keepOuts = {WorldLayout.HERO_CENTER_X, WorldLayout.HERO_CENTER_Y, HERO_CLEARANCE};
        if (candidate.covers(keepOuts[0], keepOuts[1], keepOuts[2])) {
            return away(candidate, keepOuts[0], keepOuts[1],
                candidate.radius + keepOuts[2] - distanceTo(candidate, keepOuts[0], keepOuts[1]) + 1f,
                distanceTo(candidate, keepOuts[0], keepOuts[1]));
        }
        float[][] mouths = {{-40f, WorldLayout.HERO_CENTER_Y}, {WorldLayout.REFERENCE_WIDTH + 40f,
            WorldLayout.HERO_CENTER_Y}, {WorldLayout.HERO_CENTER_X, -40f}};
        for (float[] mouth : mouths) {
            if (candidate.covers(mouth[0], mouth[1], SPAWN_CLEARANCE)) {
                float distance = distanceTo(candidate, mouth[0], mouth[1]);
                return away(candidate, mouth[0], mouth[1],
                    candidate.radius + SPAWN_CLEARANCE - distance + 1f, distance);
            }
        }
        if (candidate.covers(WorldLayout.WORLD_TREE_X, WorldLayout.WORLD_TREE_Y, TREE_CLEARANCE)) {
            float distance = distanceTo(candidate, WorldLayout.WORLD_TREE_X, WorldLayout.WORLD_TREE_Y);
            return away(candidate, WorldLayout.WORLD_TREE_X, WorldLayout.WORLD_TREE_Y,
                candidate.radius + TREE_CLEARANCE - distance + 1f, distance);
        }
        return null;
    }

    private static float distanceTo(ArenaObstacle obstacle, float x, float y) {
        return (float) Math.hypot(obstacle.x - x, obstacle.y - y);
    }

    /** A push of {@code distance} along the line out of the point that is being crowded. */
    private static float[] away(ArenaObstacle candidate, float fromX, float fromY, float distance, float current) {
        float dx = candidate.x - fromX;
        float dy = candidate.y - fromY;
        if (current < 0.001f) {
            return new float[] {0f, distance};
        }
        return new float[] {dx / current * distance, dy / current * distance};
    }

    /** True when a body of {@code bodyRadius} centred here would be inside solid ground. */
    public static boolean isBlocked(List<ArenaObstacle> field, float x, float y, float bodyRadius) {
        if (field == null || field.isEmpty()) {
            return false;
        }
        for (ArenaObstacle obstacle : field) {
            if (obstacle.covers(x, y, bodyRadius)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The first standing outcrop a flight path crosses, or null when the path is clear.
     *
     * <p>Low cover is flown over: an arrow crossing a hedge is the same answer the bow gives a hedge in the hand,
     * and it is what keeps the arena honest -- a field of nothing but shelters would tax every shot the player
     * takes at the road in front of it, and a bow that spends the fight on rubble is a bow nobody aims.
     */
    public static ArenaObstacle firstBlocker(
        List<ArenaObstacle> field, float fromX, float fromY, float toX, float toY, float bodyRadius
    ) {
        if (field == null || field.isEmpty()) {
            return null;
        }
        ArenaObstacle nearest = null;
        float nearestSquared = Float.MAX_VALUE;
        for (ArenaObstacle obstacle : field) {
            if (!obstacle.shelters || !obstacle.blocksSegment(fromX, fromY, toX, toY, bodyRadius)) {
                continue;
            }
            float squared = obstacle.squaredDistanceToSegment(fromX, fromY, toX, toY);
            if (squared < nearestSquared) {
                nearestSquared = squared;
                nearest = obstacle;
            }
        }
        return nearest;
    }

    /** The outcrop a body of this radius is touching, or null when it is standing in the open. */
    public static ArenaObstacle nearestContact(List<ArenaObstacle> field, float x, float y, float bodyRadius) {
        if (field == null || field.isEmpty()) {
            return null;
        }
        ArenaObstacle nearest = null;
        float nearestSquared = Float.MAX_VALUE;
        for (ArenaObstacle obstacle : field) {
            float limit = obstacle.radius + Math.max(0f, bodyRadius) + 6f;
            float dx = x - obstacle.x;
            float dy = y - obstacle.y;
            float squared = dx * dx + dy * dy;
            if (squared < limit * limit && squared < nearestSquared) {
                nearestSquared = squared;
                nearest = obstacle;
            }
        }
        return nearest;
    }

    /**
     * Resolves a Hero step through the field into the shared result pair: the ordered point becomes the nearest
     * standable point beside whatever is in the way.
     *
     * <p>Pushing a body out along the line through an outcrop's centre is the whole of it. Walking into one puts
     * the Hero on its surface with the rest of the step spent, which is the slide a player expects from a step
     * order; the tick that follows a step that the field refuses is the caller's business, because a Hero that
     * grinds against a stone with the order still live is a Hero the player cannot steer.
     *
     * <p>The point is read back with {@link #resolvedX()} and {@link #resolvedY()} and written by
     * {@code HeroMovementSystem}, which owns the Hero's position. The field answers where a body may stand; one
     * system writes it.
     */
    public static void resolveHero(List<ArenaObstacle> field, float toX, float toY) {
        resolve(field, toX, toY, HERO_BODY_RADIUS);
    }

    /**
     * Walks a foe's step around whatever is in its way, and reports whether the field let it make progress.
     *
     * <p>A body that only knows how to walk at the Hero can stall dead-centre against an outcrop: its step is
     * pushed straight back out along the line it came in on, every frame, forever. So a step that the field eats
     * is answered with a step sideways -- along the outcrop's surface, toward whichever side the target is on --
     * which is what turns a wall into a detour. The caller writes the point only when this says there was one.
     */
    public static boolean resolveEnemy(
        List<ArenaObstacle> field, float fromX, float fromY, float targetX, float targetY, float travel
    ) {
        if (travel <= 0f) {
            return false;
        }
        float dx = targetX - fromX;
        float dy = targetY - fromY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance <= 0.001f) {
            return false;
        }
        resolve(field, fromX + dx / distance * travel, fromY + dy / distance * travel, ENEMY_BODY_RADIUS);
        float moved = (float) Math.hypot(RESOLVED[0] - fromX, RESOLVED[1] - fromY);
        if (moved < travel * 0.6f) {
            ArenaObstacle contact = nearestContact(field, RESOLVED[0], RESOLVED[1], ENEMY_BODY_RADIUS);
            if (contact != null) {
                float normalX = RESOLVED[0] - contact.x;
                float normalY = RESOLVED[1] - contact.y;
                float normalLength = (float) Math.hypot(normalX, normalY);
                if (normalLength > 0.001f) {
                    // The tangent has to be a direction, not a vector: taken straight off the outward normal its
                    // length is the outcrop's whole surface distance, which would slide a body most of an arena in
                    // a single frame and leave it vibrating between two stones it never leaves.
                    float tangentX = -normalY / normalLength;
                    float tangentY = normalX / normalLength;
                    float side = tangentX * dx + tangentY * dy >= 0f ? 1f : -1f;
                    resolve(field,
                        fromX + tangentX * side * travel,
                        fromY + tangentY * side * travel,
                        ENEMY_BODY_RADIUS);
                    moved = (float) Math.hypot(RESOLVED[0] - fromX, RESOLVED[1] - fromY);
                }
            }
        }
        return moved > 0.0001f;
    }

    /** The x of the point the last resolve wrote. */
    public static float resolvedX() {
        return RESOLVED[0];
    }

    /** The y of the point the last resolve wrote. */
    public static float resolvedY() {
        return RESOLVED[1];
    }

    /**
     * True when the line between two bodies is clear of every outcrop: the same test, and the same radius, an
     * arrow flies on.
     *
     * <p>Both sides of a fight use this. The bow asks it through {@link #firstBlocker}, and a foe that shoots at
     * a range longer than its own reach asks it before it strikes, so cover can never become a place a creature
     * hits the Hero from and the Hero cannot answer. A stone that hides the spitter hides the Hero from the
     * spitter too, which is the bargain the layouts are built on.
     */
    public static boolean hasLineOfFire(
        List<ArenaObstacle> field, float fromX, float fromY, float toX, float toY
    ) {
        return firstBlocker(field, fromX, fromY, toX, toY, ARROW_RADIUS) == null;
    }

    private static void resolve(List<ArenaObstacle> field, float toX, float toY, float bodyRadius) {
        float x = toX;
        float y = toY;
        if (field != null && !field.isEmpty()) {
            for (int pass = 0; pass < 3; pass++) {
                boolean pushed = false;
                for (ArenaObstacle obstacle : field) {
                    float limit = obstacle.radius + bodyRadius;
                    float dx = x - obstacle.x;
                    float dy = y - obstacle.y;
                    float squared = dx * dx + dy * dy;
                    if (squared >= limit * limit) {
                        continue;
                    }
                    float distance = (float) Math.sqrt(squared);
                    if (distance < 0.001f) {
                        dx = 0f;
                        dy = -1f;
                        distance = 1f;
                    }
                    x = obstacle.x + dx / distance * limit;
                    y = obstacle.y + dy / distance * limit;
                    pushed = true;
                }
                if (!pushed) {
                    break;
                }
            }
        }
        RESOLVED[0] = x;
        RESOLVED[1] = y;
    }
}
