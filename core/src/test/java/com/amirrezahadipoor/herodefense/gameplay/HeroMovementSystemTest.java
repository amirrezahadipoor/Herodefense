package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.input.HudTouchLayout;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.Hero;
import com.amirrezahadipoor.herodefense.model.WaveModifier;
import com.amirrezahadipoor.herodefense.trials.TrialEffects;
import com.amirrezahadipoor.herodefense.trials.TrialId;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Roadmap A1: the Hero can step, and the step has a price the wave sets.
 *
 * <p>The tests below are the two halves of that sentence. The first half is ordinary behaviour -- an order walks
 * the Hero to the point, no order leaves it exactly where it stands, the band holds it, the anchor voids a pending
 * order, a fallen Hero does not walk. The second half is the reason the budget exists at all: that stepping can
 * never become kiting. The numbers for that are read out of the shipped tables rather than restated, so a future
 * enemy that walks slower than 28 units a second tightens the assertion instead of quietly breaking the argument.
 */
final class HeroMovementSystemTest {

    private static final float TICK = 1f / 60f;

    /** Grid spacing for the band scan: fine enough that no 120-unit button fits between two samples. */
    private static final float GRID_STEP = 12f;

    /** A run at wave one with a full step budget and the Hero where every run starts. */
    private static GameState rooted() {
        GameState state = GameState.newRun(20260918L);
        state.anchorHeroAtArenaCenter();
        HeroMovementSystem.beginWave(state);
        return state;
    }

    private static void tick(GameState state, int ticks) {
        for (int index = 0; index < ticks; index++) {
            HeroMovementSystem.update(state, TICK);
        }
    }

    private static float distance(Hero hero, float x, float y) {
        return (float) Math.hypot(hero.x - x, hero.y - y);
    }

    @Test
    void withNoOrderTheHeroHoldsTheLineToThePixel() {
        GameState state = rooted();
        tick(state, 600);
        assertEquals(GameState.ARENA_CENTER_X, state.hero.x,
            "no order means no drift: the anchor the game used to re-apply every frame is now the default");
        assertEquals(GameState.ARENA_CENTER_Y, state.hero.y);
        assertEquals(Hero.WAVE_STEP_BUDGET, state.hero.stepBudgetUnits,
            "and standing still spends nothing");
    }

    @Test
    void anOrderWalksTheHeroToThePointAndThenStops() {
        GameState state = rooted();
        assertTrue(HeroMovementSystem.orderStepTo(state, 500f, 400f), "the point is inside the band");
        assertTrue(HeroMovementSystem.isStepping(state));
        tick(state, 300);
        assertFalse(state.hero.moveOrderActive, "arriving ends the order rather than hovering on it");
        assertTrue(distance(state.hero, 500f, 400f) <= 8f,
            "the Hero ends on the point it was sent to: " + distance(state.hero, 500f, 400f));
        assertTrue(state.hero.stepBudgetUnits < Hero.WAVE_STEP_BUDGET, "and the walk was paid for");
    }

    @Test
    void oneTickMovesExactlyTheStepSpeed() {
        GameState state = rooted();
        HeroMovementSystem.orderStepTo(state, 460f, 600f);
        HeroMovementSystem.update(state, 0.1f);
        assertEquals(360f + HeroMovementSystem.STEP_SPEED * 0.1f, state.hero.x, 0.001f,
            "165 units a second is the number the balance argument below is built on");
        assertEquals(600f, state.hero.y, 0.001f, "and it moves along the line to the point, not diagonally");
    }

    @Test
    void theHeroCannotBePutOutsideTheWalkableBand() {
        GameState state = rooted();
        state.hero.x = -1200f;
        state.hero.y = 5000f;
        HeroMovementSystem.update(state, TICK);
        assertEquals(WorldLayout.HERO_WALK_MIN_X, state.hero.x);
        assertEquals(WorldLayout.HERO_WALK_MAX_Y, state.hero.y);
        assertFalse(HeroMovementSystem.orderStepTo(state, 0f, 0f),
            "a drag outside the band is not an order, so a finger on the HUD walks nobody");
        assertFalse(HeroMovementSystem.orderStepTo(state, Float.NaN, 400f));
        assertFalse(state.hero.moveOrderActive);
    }

    @Test
    void theBudgetIsTheLimitAndNotTheSpeed() {
        GameState state = rooted();
        float startX = state.hero.x;
        float startY = state.hero.y;
        assertTrue(HeroMovementSystem.orderStepTo(state, WorldLayout.HERO_WALK_MAX_X, WorldLayout.HERO_WALK_MAX_Y),
            "the far corner of the band is further away than one wave's budget can pay for");
        assertTrue(distance(state.hero, WorldLayout.HERO_WALK_MAX_X, WorldLayout.HERO_WALK_MAX_Y)
            > Hero.WAVE_STEP_BUDGET);
        tick(state, 1200);
        float travelled = distance(state.hero, startX, startY);
        assertEquals(Hero.WAVE_STEP_BUDGET, travelled, 0.75f,
            "the Hero travels its budget and not one unit further: " + travelled);
        assertEquals(0f, state.hero.stepBudgetUnits, 0.001f);
        assertFalse(state.hero.moveOrderActive, "running out roots the Hero instead of leaving a dead order live");
        assertFalse(HeroMovementSystem.orderStepTo(state, startX, startY),
            "and a rooted Hero takes no new order this wave");
        float rootedX = state.hero.x;
        tick(state, 600);
        assertEquals(rootedX, state.hero.x, "rooted means rooted, for the rest of the wave");
    }

    @Test
    void aNewWaveHandsTheBudgetBack() {
        GameState state = rooted();
        HeroMovementSystem.orderStepTo(state, 500f, 400f);
        tick(state, 300);
        float spent = Hero.WAVE_STEP_BUDGET - state.hero.stepBudgetUnits;
        assertTrue(spent > 0f);
        state.anchorHeroAtArenaCenter();
        assertEquals(spent, Hero.WAVE_STEP_BUDGET - state.hero.stepBudgetUnits, 0.001f,
            "the anchor puts the Hero back but does not refund the wave's stepping");
        HeroMovementSystem.beginWave(state);
        assertEquals(Hero.WAVE_STEP_BUDGET, state.hero.stepBudgetUnits);
        assertFalse(state.hero.moveOrderActive);
    }

    @Test
    void aFallenHeroTakesNoOrderAndFinishesNoWalk() {
        GameState state = rooted();
        HeroMovementSystem.orderStepTo(state, 500f, 400f);
        state.hero.alive = false;
        float fallenX = state.hero.x;
        tick(state, 120);
        assertEquals(fallenX, state.hero.x, "the dead do not walk, or the siege on the Heartwood never starts");
        assertFalse(state.hero.moveOrderActive);
        assertFalse(HeroMovementSystem.orderStepTo(state, 300f, 300f));
    }

    @Test
    void releasingTheDragStopsTheWalkWhereTheFingerLeft() {
        GameState state = rooted();
        HeroMovementSystem.orderStepTo(state, 500f, 400f);
        tick(state, 10);
        HeroMovementSystem.cancelOrder(state);
        float stoppedX = state.hero.x;
        float stoppedY = state.hero.y;
        tick(state, 300);
        assertEquals(stoppedX, state.hero.x);
        assertEquals(stoppedY, state.hero.y, "cancel means stop, not continue to the last point");
    }

    @Test
    void steppingOutOfMeleeReachIsWhatTheBudgetBuys() {
        GameState state = rooted();
        // A foe that does not walk, so the only thing that changes the distance between them is the Hero's step.
        Enemy foe = new Enemy(state.allocateEntityId(), "BRAMBLE_THRALL", 360f, 540f);
        foe.health = 100f;
        foe.maxHealth = 100f;
        foe.damage = 1f;
        foe.attackRange = 60f;
        foe.attackIntervalSeconds = 0.5f;
        foe.attackCooldownSeconds = 0f;
        foe.movementSpeed = 0f;
        state.aliveEnemies.add(foe);
        EnemyMeleeAttackSystem melee = new EnemyMeleeAttackSystem(new HeroDamageSystem());

        float healthAtTheCentre = state.hero.health;
        for (int swing = 0; swing < 200 && state.hero.health >= healthAtTheCentre; swing++) {
            melee.update(state, TICK);
        }
        assertTrue(state.hero.health < healthAtTheCentre,
            "the baseline: a foe inside its reach hits the Hero where the Hero stands");

        assertTrue(HeroMovementSystem.orderStepTo(state, 360f, 700f),
            "stepping north, away from the foe, is inside the band and inside the budget");
        tick(state, 120);
        assertTrue(distance(state.hero, foe.x, foe.y) > foe.attackRange,
            "the step took the Hero out of reach: " + distance(state.hero, foe.x, foe.y));
        float healthAfterTheStep = state.hero.health;
        for (int swing = 0; swing < 300; swing++) {
            melee.update(state, TICK);
        }
        assertEquals(healthAfterTheStep, state.hero.health,
            "and five seconds of the same foe swinging lands nothing, which is the whole point of the verb");
    }

    @Test
    void noHudButtonSitsInsideTheWalkableBand() {
        // Integer indices, floated inside the body: both analysers reject a float loop counter (PMD
        // DontUseFloatTypeForLoopIndices, SpotBugs FL_FLOATS_AS_LOOP_COUNTERS), and they are right -- an
        // accumulated `x += 12f` decides its own last column by rounding, so the grid it scans is the grid the
        // accumulator happens to reach rather than the band.
        int columns = Math.round((WorldLayout.HERO_WALK_MAX_X - WorldLayout.HERO_WALK_MIN_X) / GRID_STEP) + 1;
        int rows = Math.round((WorldLayout.HERO_WALK_MAX_Y - WorldLayout.HERO_WALK_MIN_Y) / GRID_STEP) + 1;
        assertEquals(49, columns, "the grid has to cover the band's own width");
        assertEquals(45, rows, "and its height, or a button could hide in the columns nobody scanned");
        for (int column = 0; column < columns; column++) {
            float x = WorldLayout.HERO_WALK_MIN_X + column * GRID_STEP;
            for (int row = 0; row < rows; row++) {
                float y = WorldLayout.HERO_WALK_MIN_Y + row * GRID_STEP;
                assertTrue(HeroMovementSystem.isInsideWalkableArea(x, y));
                String where = "at " + x + "," + y;
                assertFalse(HudTouchLayout.speedAt(x, y), "the speed button is in the band " + where);
                assertFalse(HudTouchLayout.pauseAt(x, y), "the pause button is in the band " + where);
                assertFalse(HudTouchLayout.inventoryAt(x, y), "the inventory button is in the band " + where);
                assertFalse(HudTouchLayout.shopAt(x, y), "the shop button is in the band " + where);
                assertFalse(HudTouchLayout.ultimateAt(x, y), "the ultimate button is in the band " + where);
            }
        }
    }

    @Test
    void nothingInTheGameSlowsTheFieldBelowItsBaseSpeeds() {
        for (WaveModifier omen : WaveModifier.values()) {
            assertTrue(omen.speedMultiplier() >= 1f,
                omen + " slows the field, which would break the head-start bound below");
        }
        for (TrialId trial : TrialId.values()) {
            assertTrue(TrialEffects.enemySpeedMultiplier(List.of(trial.name())) >= 1f,
                trial + " slows the field, which would break the head-start bound below");
        }
        assertTrue(TrialEffects.enemySpeedMultiplier(List.of()) == 1f);
    }

    @Test
    void steppingCannotBeSustainedAgainstTheSlowestThingInTheGame() {
        float slowest = Float.MAX_VALUE;
        float fastest = 0f;
        for (EnemyType type : EnemyType.values()) {
            slowest = Math.min(slowest, type.movementSpeed());
            fastest = Math.max(fastest, type.movementSpeed());
        }
        for (BossType type : BossType.values()) {
            for (BossFightScript script : BossFightScript.values()) {
                float speed = type.movementSpeed() * script.movementMultiplier();
                slowest = Math.min(slowest, speed);
                fastest = Math.max(fastest, speed);
            }
        }
        assertEquals(28f, slowest, 0.001f,
            "BRAMBLE_THRALL is still the slowest body in the game; if that changed the bound below changed with it");

        float headStartSeconds = Hero.WAVE_STEP_BUDGET / slowest;
        assertTrue(headStartSeconds <= 10f,
            "a whole wave's stepping may buy at most ten seconds of distance on the slowest foe, or a wave could"
                + " be kited: budget/slowest = " + headStartSeconds);
        assertTrue(Hero.WAVE_STEP_BUDGET / fastest <= 4f,
            "and under four seconds on the fastest: " + Hero.WAVE_STEP_BUDGET / fastest);

        float burstSeconds = Hero.WAVE_STEP_BUDGET / HeroMovementSystem.STEP_SPEED;
        assertTrue(burstSeconds >= 1f && burstSeconds <= 2.5f,
            "the budget has to be long enough to leave a telegraph and short enough not to be a run: "
                + burstSeconds + "s");
        assertTrue(HeroMovementSystem.STEP_SPEED > fastest,
            "the step has to outrun the field for the length of the burst, or dodging is decoration");
    }

    @Test
    void thePublishedBalanceEvidenceStillMeasuresARootedHero() {
        String simulator = read(Path.of("..", "core", "src", "main", "java",
            "com", "amirrezahadipoor", "herodefense", "balance", "BalanceSimulator.java").normalize());
        assertTrue(simulator.contains("anchorHeroAtArenaCenter"),
            "the simulator anchors every tick, which is what keeps every published band a floor rather than a"
                + " measurement of skilled play");
        assertFalse(simulator.contains("HeroMovementSystem"),
            "and no simulated policy steps, so the bands were not quietly re-measured against a moving Hero");

        String game = read(Path.of("..", "core", "src", "main", "java",
            "com", "amirrezahadipoor", "herodefense", "HeroDefenseGame.java").normalize());
        assertFalse(game.contains("anchorHeroAtArenaCenter"),
            "the played frame no longer re-anchors the Hero; if it does, stepping is dead code");
        assertTrue(game.contains("HeroMovementSystem.update(gameState"),
            "the played frame runs the movement system in the slot the anchor used to occupy");
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path.toAbsolutePath(), e);
        }
    }
}
