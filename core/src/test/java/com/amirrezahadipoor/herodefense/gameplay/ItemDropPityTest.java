package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.model.DropEntity;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.ItemTier;
import com.amirrezahadipoor.herodefense.save.GameStateCodec;
import org.junit.jupiter.api.Test;

/**
 * The pity rule (roadmap B1, R4.3's blocked feature): a guaranteed common after thirty dry kills, armed only
 * past the brief vigil, counted inside the roll that was already happening.
 *
 * <p>The three versions R4.3 measured are recorded in {@code docs/BALANCE.md} with the ceilings they broke;
 * this rule ships because A3 bought the headroom, and the tests here pin the two promises that keep it inside
 * that headroom: the brief vigil's economy stays exactly what its evidence measured, and the rule adds no draw
 * to a miss -- the streak is counted by the existing roll, and only an answered streak spends what a natural
 * drop would have spent.
 */
final class ItemDropPityTest {

    private static final int SCAN_LIMIT = 500;

    /** Kills one fresh dead body per call; returns nothing, the state shows the outcome. */
    private static void killOne(GameState state, ItemDropSystem drops) {
        Enemy enemy = new Enemy(state.allocateEntityId(), EnemyType.ROOTLING.name(), 100f, 200f);
        enemy.alive = false;
        state.aliveEnemies.add(enemy);
        drops.processDefeatedEnemies(state);
    }

    @Test
    void theThirtiethDryKillIsAnsweredWithACommon() {
        ItemDropSystem drops = new ItemDropSystem();
        long seed = -1;
        GameState state = null;
        for (long candidate = 1; candidate <= SCAN_LIMIT; candidate++) {
            GameState trial = GameState.newRun(candidate);
            trial.waveNumber = ItemDropSystem.PITY_ARMING_WAVE;
            boolean dry = true;
            for (int kill = 1; kill < ItemDropSystem.PITY_DRY_KILL_THRESHOLD; kill++) {
                killOne(trial, drops);
                if (!trial.drops.isEmpty()) {
                    dry = false;
                    break;
                }
            }
            if (dry) {
                seed = candidate;
                state = trial;
                break;
            }
        }
        assertTrue(seed > 0, "a seed whose first twenty-nine rolls all miss must exist within " + SCAN_LIMIT);
        assertEquals(ItemDropSystem.PITY_DRY_KILL_THRESHOLD - 1, state.dryKillsSinceItemDrop,
            "twenty-nine dry kills are counted, one short of the answer");

        killOne(state, drops);
        assertEquals(1, state.drops.size(), "the thirtieth dry kill is answered");
        assertEquals(0, state.dryKillsSinceItemDrop, "and the streak starts over");
        DropEntity drop = state.drops.get(0);
        assertNotNull(drop.itemId);
        assertEquals(ItemTier.COMMON, EquipmentCatalog.byId(drop.itemId).tier(),
            "the answer is a common -- the lightest thing the rule may mint");

        killOne(state, drops);
        assertEquals(1, state.drops.size(), "the thirty-first kill is an ordinary miss again: no free runs");
        assertEquals(1, state.dryKillsSinceItemDrop);
    }

    @Test
    void theBriefVigilNeverCountsADrought() {
        // Natural drops are the ordinary game and happen inside the vigil like anywhere else; what the gate
        // forbids is the rule -- no counting, no answering. The first measured version broke the vigil's floor
        // (0.0346 against 0.035) by minting items there, so the contract is that the streak itself never moves
        // below the arming wave, which is what keeps the vigil's economy bit-identical to its evidence.
        ItemDropSystem drops = new ItemDropSystem();
        for (long candidate = 1; candidate <= 64; candidate++) {
            GameState state = GameState.newRun(candidate);
            state.waveNumber = ItemDropSystem.PITY_ARMING_WAVE - 1;
            for (int kill = 1; kill <= ItemDropSystem.PITY_DRY_KILL_THRESHOLD + 10; kill++) {
                killOne(state, drops);
                assertEquals(0, state.dryKillsSinceItemDrop,
                    "seed " + candidate + " kill " + kill + ": the streak is not even counted before the rule arms");
            }
        }
    }

    @Test
    void aNaturalDropResetsTheStreak() {
        ItemDropSystem drops = new ItemDropSystem();
        for (long candidate = 1; candidate <= SCAN_LIMIT; candidate++) {
            GameState state = GameState.newRun(candidate);
            state.waveNumber = ItemDropSystem.PITY_ARMING_WAVE;
            state.dryKillsSinceItemDrop = 5;
            killOne(state, drops);
            if (state.drops.isEmpty()) {
                assertEquals(6, state.dryKillsSinceItemDrop, "a miss counts");
                continue;
            }
            assertEquals(0, state.dryKillsSinceItemDrop,
                "seed " + candidate + ": a natural drop resets the streak -- pity answers droughts, not luck");
            return;
        }
        throw new AssertionError("no seed within " + SCAN_LIMIT + " dropped on its first kill");
    }

    @Test
    void theStreakSurvivesASave() {
        GameState state = GameState.newRun(0xB17E5L);
        state.waveNumber = ItemDropSystem.PITY_ARMING_WAVE;
        state.dryKillsSinceItemDrop = ItemDropSystem.PITY_DRY_KILL_THRESHOLD - 1;
        GameState resumed = new GameStateCodec().decode(new GameStateCodec().encode(state));
        assertEquals(ItemDropSystem.PITY_DRY_KILL_THRESHOLD - 1, resumed.dryKillsSinceItemDrop,
            "a resumed run resumes its drought: the counter is a run field like totalKills, not a session one");
    }
}
