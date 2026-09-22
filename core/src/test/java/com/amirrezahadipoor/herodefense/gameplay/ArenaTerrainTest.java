package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.model.ArenaObstacle;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The field: where the outcrops stand, what they stop, and what they may never break.
 *
 * <p>Two of these are the reason the layouts could be added to a game whose curve was already measured. First,
 * every outcrop keeps clear of the spawn mouths, the trunks and the Hero's first ground, so a wave arriving is
 * never a wave arriving inside a rock. Second, the field is one connected place: {@link #fieldIsWalkable} floods
 * the band on a grid and asks whether every square the Hero could stand on can be reached from where the run
 * starts -- a layout that quietly seals a corner would be a bug no play-test would find.
 */
class ArenaTerrainTest {

    private static final float GRID = 12f;
    private static final float STEP_SECONDS = 1f / 60f;

    @Test
    void theSameSeedIsTheSameField() {
        for (ArenaLayout layout : ArenaLayout.values()) {
            for (long seed = 1; seed <= 40; seed++) {
                List<ArenaObstacle> first = ArenaTerrain.build(layout, seed);
                List<ArenaObstacle> second = ArenaTerrain.build(layout, seed);
                assertEquals(first.size(), second.size(), layout + " size at seed " + seed);
                for (int index = 0; index < first.size(); index++) {
                    assertEquals(first.get(index).x, second.get(index).x, 0.0001f, layout + " x at seed " + seed);
                    assertEquals(first.get(index).y, second.get(index).y, 0.0001f, layout + " y at seed " + seed);
                    assertEquals(first.get(index).radius, second.get(index).radius, 0.0001f);
                    assertEquals(first.get(index).variant, second.get(index).variant);
                }
            }
        }
    }

    @Test
    void everyOutcropKeepsOutOfTheRoadsTheTrunksAndTheHerosGround() {
        for (ArenaLayout layout : ArenaLayout.values()) {
            for (long seed = 1; seed <= 120; seed++) {
                List<ArenaObstacle> field = ArenaTerrain.build(layout, seed);
                for (ArenaObstacle obstacle : field) {
                    String at = layout + " seed " + seed + " at " + obstacle.x + "," + obstacle.y;
                    assertTrue(obstacle.x >= WorldLayout.HERO_WALK_MIN_X + ArenaTerrain.EDGE_MARGIN - 0.001f,
                        "inside the band's left margin: " + at);
                    assertTrue(obstacle.x <= WorldLayout.HERO_WALK_MAX_X - ArenaTerrain.EDGE_MARGIN + 0.001f,
                        "inside the band's right margin: " + at);
                    assertTrue(obstacle.y >= ArenaTerrain.FIELD_MIN_Y - 0.001f
                        && obstacle.y <= ArenaTerrain.FIELD_MAX_Y + 0.001f, "inside the field band: " + at);
                    assertFalse(obstacle.covers(WorldLayout.HERO_CENTER_X, WorldLayout.HERO_CENTER_Y,
                        ArenaTerrain.HERO_CLEARANCE), "crowds the Heroes first ground: " + at);
                    assertFalse(obstacle.covers(WorldLayout.WORLD_TREE_X, WorldLayout.WORLD_TREE_Y, 96f),
                        "stands on the tree: " + at);
                }
                for (int first = 0; first < field.size(); first++) {
                    for (int second = first + 1; second < field.size(); second++) {
                        ArenaObstacle a = field.get(first);
                        ArenaObstacle b = field.get(second);
                        float limit = a.radius + b.radius + ArenaTerrain.POCKET_GAP;
                        float dx = a.x - b.x;
                        float dy = a.y - b.y;
                        assertTrue(dx * dx + dy * dy >= limit * limit - 0.001f,
                            "two outcrops share a pocket: " + layout + " seed " + seed);
                    }
                }
            }
        }
    }

    @Test
    void eachLayoutIsItsOwnPlaceAndEveryFieldHasCover() {
        int[] counts = new int[ArenaLayout.values().length];
        for (long seed = 0; seed < 400; seed++) {
            ArenaLayout layout = ArenaLayout.forSeed(seed);
            counts[layout.ordinal()] += ArenaTerrain.build(layout, seed).size();
        }
        for (ArenaLayout layout : ArenaLayout.values()) {
            assertTrue(counts[layout.ordinal()] > 200,
                layout + " appeared in only " + counts[layout.ordinal()] + " fields of 400 seeds");
        }
        assertTrue(counts[ArenaLayout.OPEN_HEARTH.ordinal()] < counts[ArenaLayout.RUINED_RING.ordinal()],
            "the open field must carry less cover than the ring");
        for (ArenaLayout layout : ArenaLayout.values()) {
            int least = Integer.MAX_VALUE;
            for (long seed = 0; seed < 200; seed++) {
                if (ArenaLayout.forSeed(seed) != layout) {
                    continue;
                }
                least = Math.min(least, ArenaTerrain.build(layout, seed).size());
            }
            assertTrue(least >= layout.minimumOutcrops(),
                layout + " fell to " + least + " outcrops, under its floor of " + layout.minimumOutcrops());
        }
    }

    @Test
    void theFieldIsStillOnePlaceTheHeroCanWalk() {
        for (ArenaLayout layout : ArenaLayout.values()) {
            for (long seed = 1; seed <= 60; seed++) {
                assertTrue(fieldIsWalkable(ArenaTerrain.build(layout, seed)),
                    layout + " seed " + seed + " seals part of the band off from the Heroes ground");
            }
        }
    }

    @Test
    void aStepIntoAnOutcropStopsOnItsSurface() {
        GameState state = GameState.newRun(7L);
        List<ArenaObstacle> field = ArenaTerrain.fieldFor(state);
        assertFalse(field.isEmpty(), "this seed's field is empty, so the step has nothing to meet");
        ArenaObstacle nearest = field.get(0);
        float distance = (float) Math.hypot(nearest.x - state.hero.x, nearest.y - state.hero.y);
        for (ArenaObstacle obstacle : field) {
            float candidate = (float) Math.hypot(obstacle.x - state.hero.x, obstacle.y - state.hero.y);
            if (candidate < distance) {
                distance = candidate;
                nearest = obstacle;
            }
        }
        // Stand one wave-budget's reach short of the stone: the walk has to be affordable, or the Hero simply runs
        // out of budget and stops in the open, which would prove nothing about the field.
        state.hero.x = nearest.x;
        state.hero.y = nearest.y + nearest.radius + ArenaTerrain.HERO_BODY_RADIUS + 140f;
        assertTrue(HeroMovementSystem.isInsideWalkableArea(state.hero.x, state.hero.y),
            "the test placed the Hero outside the walkable band");
        assertFalse(ArenaTerrain.isBlocked(field, state.hero.x, state.hero.y, ArenaTerrain.HERO_BODY_RADIUS),
            "the test placed the Hero inside an outcrop");
        HeroMovementSystem.beginWave(state);
        assertTrue(HeroMovementSystem.orderStepTo(state, nearest.x, nearest.y), "the order was refused");
        for (int tick = 0; tick < 120; tick++) {
            HeroMovementSystem.update(state, STEP_SECONDS);
        }
        assertFalse(ArenaTerrain.isBlocked(field, state.hero.x, state.hero.y, ArenaTerrain.HERO_BODY_RADIUS),
            "the Hero ended up inside an outcrop");
        float reached = (float) Math.hypot(nearest.x - state.hero.x, nearest.y - state.hero.y);
        assertTrue(reached <= nearest.radius + ArenaTerrain.HERO_BODY_RADIUS + 4f,
            "the Hero stopped " + reached + " from a stone of radius " + nearest.radius);
        assertFalse(state.hero.moveOrderActive, "the order stayed live against the stone");
    }

    @Test
    void aFoeWalksAroundAnOutcropInsteadOfLeaningOnIt() {
        GameState state = GameState.newRun(11L);
        List<ArenaObstacle> field = ArenaTerrain.fieldFor(state);
        assertFalse(field.isEmpty(), "this seed's field is empty, so there is nothing to walk around");
        ArenaObstacle blocker = field.get(0);
        // The foe starts on the far side of an outcrop from the Hero, dead on the line through its centre: the one
        // geometry a body that only knows how to walk at the Hero can stall on forever.
        float towardHeroX = state.hero.x - blocker.x;
        float towardHeroY = state.hero.y - blocker.y;
        float length = (float) Math.hypot(towardHeroX, towardHeroY);
        Enemy enemy = new Enemy(state.allocateEntityId(), "ROOTLING",
            blocker.x - towardHeroX / length * 150f, blocker.y - towardHeroY / length * 150f);
        enemy.maxHealth = 100f;
        enemy.health = 100f;
        enemy.damage = 10f;
        enemy.movementSpeed = 60f;
        enemy.attackRange = 40f;
        state.aliveEnemies.add(enemy);
        float before = (float) Math.hypot(state.hero.x - enemy.x, state.hero.y - enemy.y);
        EnemyMovementSystem movement = new EnemyMovementSystem();
        for (int tick = 0; tick < 900; tick++) {
            movement.update(state, STEP_SECONDS);
        }
        float after = (float) Math.hypot(state.hero.x - enemy.x, state.hero.y - enemy.y);
        assertTrue(after < before - 100f,
            "the foe only closed from " + before + " to " + after + ", so the outcrop stopped it");
        assertFalse(ArenaTerrain.isBlocked(field, enemy.x, enemy.y, ArenaTerrain.ENEMY_BODY_RADIUS),
            "the foe ended up inside an outcrop");
    }

    @Test
    void standingGroundStopsAnArrowAndLowCoverDoesNot() {
        for (ArenaLayout layout : ArenaLayout.values()) {
            List<ArenaObstacle> field = ArenaTerrain.build(layout, 23L);
            assertFalse(field.isEmpty(), layout + " has no cover at all");
            int standing = 0;
            int low = 0;
            for (ArenaObstacle obstacle : field) {
                // A flight path straight through the outcrop, from two hundred units either side of it.
                ArenaObstacle blocker = ArenaTerrain.firstBlocker(field, obstacle.x - 200f, obstacle.y,
                    obstacle.x + 200f, obstacle.y, ArenaTerrain.ARROW_RADIUS);
                assertTrue(ArenaTerrain.isBlocked(field, obstacle.x, obstacle.y, 1f),
                    layout + " lets a body stand inside an outcrop, so the field is not solid");
                if (obstacle.shelters) {
                    assertNotNull(blocker, layout + " has a standing outcrop that does not stop an arrow");
                    assertTrue(obstacle.height > obstacle.radius * 2f,
                        layout + " calls an outcrop standing without drawing it tall");
                    standing++;
                } else {
                    assertNull(blocker, layout + " has low cover that stops an arrow, so the bow loses shots");
                    assertTrue(obstacle.height < obstacle.radius * 2f,
                        layout + " calls an outcrop low without drawing it low");
                    low++;
                }
            }
            // The four fields differ in what they are made of, and that difference is the layout: two of them
            // promise an open line in their own name and must keep it, and the two that are named for standing
            // ground must actually carry some.
            boolean promisesAnOpenLine = layout == ArenaLayout.OPEN_HEARTH || layout == ArenaLayout.THORNHEDGE;
            assertTrue(promisesAnOpenLine == (standing == 0),
                layout + " carries " + standing + " standing outcrops, so its line is not the one it advertises");
            assertTrue(layout != ArenaLayout.RUINED_RING || low > 0,
                layout + " is named for a broken ring and carries no fallen stone: " + low + " low outcrops");
        }
    }

    @Test
    void aFlightPathClearOfEveryOutcropIsNot() {
        GameState state = GameState.newRun(23L);
        List<ArenaObstacle> field = ArenaTerrain.fieldFor(state);
        assertFalse(field.isEmpty(), "this seed's field is empty");
        ArenaObstacle obstacle = field.get(0);
        assertNull(ArenaTerrain.firstBlocker(
            field, obstacle.x - 200f, obstacle.y - 400f, obstacle.x + 200f, obstacle.y - 400f,
            ArenaTerrain.ARROW_RADIUS),
            "an arrow fired four hundred units short of every outcrop must reach its target");
    }

    /** The grid cell a coordinate falls on, and the check that the Hero's ground really is on a grid line. */
    private static int gridCell(float value, float origin) {
        int index = Math.round((value - origin) / GRID);
        assertEquals(0f, origin + index * GRID - value, 0.001f,
            "the Hero's ground must fall on a grid line, or the flood starts from the wrong cell");
        return index;
    }

    /** Floods the walkable band from the Heroes ground: true when every standable square is reachable. */
    private static boolean fieldIsWalkable(List<ArenaObstacle> field) {
        int columns = (int) ((WorldLayout.HERO_WALK_MAX_X - WorldLayout.HERO_WALK_MIN_X) / GRID) + 1;
        int rows = (int) ((WorldLayout.HERO_WALK_MAX_Y - WorldLayout.HERO_WALK_MIN_Y) / GRID) + 1;
        boolean[][] free = new boolean[columns][rows];
        int freeCount = 0;
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                float x = WorldLayout.HERO_WALK_MIN_X + column * GRID;
                float y = WorldLayout.HERO_WALK_MIN_Y + row * GRID;
                boolean open = !ArenaTerrain.isBlocked(field, x, y, ArenaTerrain.HERO_BODY_RADIUS);
                free[column][row] = open;
                if (open) {
                    freeCount++;
                }
            }
        }
        int startColumn = gridCell(WorldLayout.HERO_CENTER_X, WorldLayout.HERO_WALK_MIN_X);
        int startRow = gridCell(WorldLayout.HERO_CENTER_Y, WorldLayout.HERO_WALK_MIN_Y);
        if (!free[startColumn][startRow]) {
            return false;
        }
        boolean[][] seen = new boolean[columns][rows];
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {startColumn, startRow});
        seen[startColumn][startRow] = true;
        int reached = 0;
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            reached++;
            int[][] neighbours = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] offset : neighbours) {
                int column = cell[0] + offset[0];
                int row = cell[1] + offset[1];
                if (column < 0 || row < 0 || column >= columns || row >= rows) {
                    continue;
                }
                if (!free[column][row] || seen[column][row]) {
                    continue;
                }
                seen[column][row] = true;
                queue.add(new int[] {column, row});
            }
        }
        return reached == freeCount;
    }
}
