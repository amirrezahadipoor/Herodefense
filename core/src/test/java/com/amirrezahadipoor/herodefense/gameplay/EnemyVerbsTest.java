package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The eight roles' verbs: who has one, when it fires, and what it may not do. */
class EnemyVerbsTest {

    private static final float DELTA = 1f / 60f;

    private static Enemy spawn(GameState state, EnemyType type, float x, float y) {
        Enemy enemy = new EnemyFactory().create(state, type, x, y, 0);
        state.aliveEnemies.add(enemy);
        return enemy;
    }

    @Test
    void threeRolesCarryAVerbAndTheRestCarryNone() {
        assertNull(EnemyVerbs.verbOf(EnemyType.ROOTLING));
        assertNull(EnemyVerbs.verbOf(EnemyType.STONEKIN));
        assertNotNull(EnemyVerbs.verbOf(EnemyType.BARK_STALKER));
        assertNotNull(EnemyVerbs.verbOf(EnemyType.FUNGAL_BRUTE));
        assertNotNull(EnemyVerbs.verbOf(EnemyType.HUSK_WARDEN));
        assertNotNull(EnemyVerbs.verbOf(EnemyType.GLOOM_WOLF));
    }

    @Test
    void noVerbFiresInTheOpeningWaves() {
        GameState state = new GameState();
        state.waveNumber = EnemyVerbs.FIRST_VERB_WAVE - 1;
        state.hero.x = 360f;
        state.hero.y = 600f;
        Enemy brute = spawn(state, EnemyType.FUNGAL_BRUTE, 360f, 640f);
        brute.health = 10f;
        Enemy hurt = spawn(state, EnemyType.ROOTLING, 380f, 660f);
        hurt.health = 1f;
        List<Float> hits = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            EnemyVerbs.update(state, DELTA, hits::add);
        }
        assertTrue(hits.isEmpty());
        assertEquals(1f, hurt.health, 0.001f, "a verb healed before its first wave");
    }

    @Test
    void theSpitterWindsUpThenHitsOnlySomethingStillStandingOnTheLine() {
        GameState state = new GameState();
        state.waveNumber = EnemyVerbs.FIRST_VERB_WAVE;
        state.hero.x = 360f;
        state.hero.y = 600f;
        Enemy stalker = spawn(state, EnemyType.BARK_STALKER, 360f, 900f);
        stalker.attackRange = 74f;
        List<Float> hits = new ArrayList<>();
        int frames = (int) Math.ceil(EnemyVerbs.SPIT_WINDUP_SECONDS * 60f) + 4;
        for (int i = 0; i < frames; i++) {
            EnemyVerbs.update(state, DELTA, hits::add);
        }
        assertEquals(1, hits.size(), "a stationary hero should be hit once");
        assertEquals(stalker.damage * EnemyVerbs.SPIT_DAMAGE_SHARE, hits.get(0), 0.01f);

        stalker.verbLatched = false;
        hits.clear();
        for (int i = 0; i < frames; i++) {
            state.hero.x = 360f + (i > 2 ? 160f : 0f);
            EnemyVerbs.update(state, DELTA, hits::add);
        }
        assertTrue(hits.isEmpty(), "a hero who stepped off the line should not be hit");
    }

    @Test
    void theSporekeeperMendsWoundedNeighboursAndIgnoresHealthyOnes() {
        GameState state = new GameState();
        state.waveNumber = EnemyVerbs.FIRST_VERB_WAVE;
        state.hero.x = 0f;
        state.hero.y = 0f;
        Enemy keeper = spawn(state, EnemyType.FUNGAL_BRUTE, 300f, 300f);
        Enemy wounded = spawn(state, EnemyType.ROOTLING, 320f, 320f);
        wounded.health = wounded.maxHealth * 0.25f;
        Enemy healthy = spawn(state, EnemyType.ROOTLING, 330f, 310f);
        float healthyBefore = healthy.health;
        for (int i = 0; i < 60 * 8; i++) {
            EnemyVerbs.update(state, DELTA, damage -> {
            });
        }
        assertTrue(wounded.health > wounded.maxHealth * 0.25f, "the wounded were not mended");
        assertTrue(wounded.health <= wounded.maxHealth + 0.001f, "mending overshot the bar");
        assertEquals(healthyBefore, healthy.health, 0.001f, "a healthy ally was mended");
        assertTrue(keeper.verbTimerSeconds > 0f, "the mend clock did not rearm");
    }

    @Test
    void theWardenWardsItsAlliesForAWindow() {
        GameState state = new GameState();
        state.waveNumber = EnemyVerbs.FIRST_VERB_WAVE;
        state.hero.x = 0f;
        state.hero.y = 0f;
        spawn(state, EnemyType.HUSK_WARDEN, 400f, 400f);
        Enemy ally = spawn(state, EnemyType.ROOTLING, 420f, 420f);
        for (int i = 0; i < 10; i++) {
            EnemyVerbs.update(state, DELTA, damage -> {
            });
        }
        assertTrue(ally.affixWardRemainingSeconds > 0f, "the ward never came up");
        float full = ally.health;
        ally.receiveDamage(ally.maxHealth * 0.5f);
        assertTrue(ally.health > full - ally.maxHealth * 0.5f, "the ward did not soften the blow");
    }

    @Test
    void thePackSpeedsWolvesUpButNeverPastTheHerosWalk() {
        GameState state = new GameState();
        state.waveNumber = EnemyVerbs.FIRST_VERB_WAVE;
        state.hero.x = 0f;
        state.hero.y = 0f;
        Enemy lone = spawn(state, EnemyType.GLOOM_WOLF, 200f, 200f);
        EnemyVerbs.update(state, DELTA, damage -> {
        });
        assertEquals(1f, lone.packSpeedMultiplier, 0.0001f, "a lone wolf found a pack");

        for (int i = 0; i < 6; i++) {
            spawn(state, EnemyType.GLOOM_WOLF, 210f + i * 4f, 205f);
        }
        EnemyVerbs.update(state, DELTA, damage -> {
        });
        assertTrue(lone.packSpeedMultiplier > 1f, "a pack did not quicken a wolf");
        assertTrue(
            lone.packSpeedMultiplier <= 1f + EnemyVerbs.PACK_SPEED_CAP + 0.0001f,
            "the pack bonus passed its cap");
        float fastest = lone.movementSpeed * lone.packSpeedMultiplier;
        assertTrue(fastest < 165f, "a wolf outran the Hero's walk");
    }

    @Test
    void aStunnedOrSilentBodyDoesNothing() {
        GameState state = new GameState();
        state.waveNumber = EnemyVerbs.FIRST_VERB_WAVE;
        state.hero.x = 0f;
        state.hero.y = 0f;
        Enemy keeper = spawn(state, EnemyType.FUNGAL_BRUTE, 100f, 100f);
        Enemy wounded = spawn(state, EnemyType.ROOTLING, 120f, 120f);
        wounded.health = wounded.maxHealth * 0.2f;
        keeper.stunRemainingSeconds = 30f;
        float before = wounded.health;
        for (int i = 0; i < 600; i++) {
            EnemyVerbs.update(state, DELTA, damage -> {
            });
        }
        assertEquals(before, wounded.health, 0.001f, "a stunned keeper mended its line");
        assertFalse(keeper.verbLatched);
    }
}
