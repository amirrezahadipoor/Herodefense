package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.EliteAffix;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.render.VisualRarity;
import com.amirrezahadipoor.herodefense.story.EliteFragments;
import com.amirrezahadipoor.herodefense.story.LoreCatalog;
import com.amirrezahadipoor.herodefense.story.LoreEntry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The deep band's six affixes: each one is a question about the wave, each one is readable, and none of them
 * touches a shallow run. This is also the place where the three lists that must stay in step are checked -- the
 * pool, the outline palette, and the words the Tree uses for it.
 */
class EliteAffixDeepPoolTest {

    private static final String[] DEEP_BAND = {
        "stoneshell", "gravebloom", "swarmcall", "spitebarb", "hammerfall", "bloodhowl"
    };

    private final EliteAffixSystem affixes = new EliteAffixSystem(new HeroDamageSystem());

    private static Enemy body(GameState state, String type, float x, float y) {
        Enemy enemy = new Enemy(state.allocateEntityId(), type, x, y);
        enemy.maxHealth = 100f;
        enemy.health = 100f;
        enemy.damage = 20f;
        enemy.movementSpeed = 40f;
        enemy.attackRange = 60f;
        state.aliveEnemies.add(enemy);
        return enemy;
    }

    private static Enemy elite(GameState state, String affixId, float x, float y) {
        Enemy enemy = body(state, "ROOTLING", x, y);
        enemy.eliteAffix = affixId;
        return enemy;
    }

    private static GameState runWithHero(float x, float y) {
        GameState state = GameState.newRun(4242L);
        state.hero.maxHealth = 5_000f;
        state.hero.health = 5_000f;
        state.hero.x = x;
        state.hero.y = y;
        return state;
    }

    private static void tick(EliteAffixSystem system, GameState state, float seconds, float step) {
        int frames = Math.max(1, Math.round(seconds / step));
        for (int frame = 0; frame < frames; frame++) {
            system.update(state, step);
        }
    }

    @Test
    void thePoolIsTwelveAndEveryRowHasPaletteAndVoice() {
        assertEquals(12, EliteAffix.values().length);
        assertEquals(3, EliteAffix.BASE_POOL_SIZE, "the shallow band may never grow");
        List<String> missing = new ArrayList<>();
        for (EliteAffix affix : EliteAffix.values()) {
            if (VisualRarity.forEliteAffix(affix.id()) == VisualRarity.COMMON) {
                missing.add("palette:" + affix.id());
            }
            if (EliteFragments.fragmentFor(affix.id(), 1) == null
                || EliteFragments.fragmentFor(affix.id(), 2) == null) {
                missing.add("fragment:" + affix.id());
            }
            if (codexFor(affix.id()) == null) {
                missing.add("codex:" + affix.id());
            }
        }
        assertTrue(missing.isEmpty(), "content rows without a face or a voice: " + missing);
    }

    private static LoreEntry codexFor(String affixId) {
        for (LoreEntry entry : LoreCatalog.all()) {
            if (affixId.equals(entry.triggerParam())) {
                return entry;
            }
        }
        return null;
    }

    @Test
    void theFirstThreeKeepTheirShippedColours() {
        assertNotEquals(VisualRarity.COMMON, VisualRarity.forEliteAffix("blightburst"));
        assertNotEquals(VisualRarity.COMMON, VisualRarity.forEliteAffix("rootward_ward"));
        assertNotEquals(VisualRarity.COMMON, VisualRarity.forEliteAffix("weeping_rot"));
        assertEquals(VisualRarity.COMMON, VisualRarity.forEliteAffix("not_an_affix"));
        assertEquals(VisualRarity.COMMON, VisualRarity.forEliteAffix(null));
    }

    @Test
    void stoneshellArmoursItselfAndNobodyElse() {
        GameState state = runWithHero(0f, 0f);
        Enemy shell = elite(state, "stoneshell", 400f, 400f);
        Enemy neighbour = elite(state, "gravebloom", 420f, 400f);
        tick(affixes, state, DeepBandTuning.STONESHELL_PERIOD + 0.2f, 0.1f);
        assertTrue(shell.affixShieldRemainingSeconds > 0f, "the shell never hardened");
        assertEquals(0f, neighbour.affixShieldRemainingSeconds, "the armour leaked to a neighbour");

        shell.receiveDamage(50f);
        assertTrue(shell.health > 50f, "the hardened body took full damage");
        neighbour.receiveDamage(50f);
        assertEquals(50f, neighbour.health, 0.001f, "an unhardened body softened a blow it should not have");
    }

    @Test
    void gravebloomKeepsTheGroundAngryLongerThanItsOwnerLived() {
        GameState state = runWithHero(0f, 0f);
        Enemy bloom = elite(state, "gravebloom", state.hero.x + 20f, state.hero.y);
        bloom.alive = false;
        affixes.update(state, 0.1f);
        assertEquals(1, state.rotTrail.size(), "a grave bloom left no ground behind");
        assertTrue(state.rotTrail.get(0).remainingSeconds > DeepBandTuning.GRAVEBLOOM_LIFETIME - 0.2f,
            "the bloom faded faster than its own lifetime");
        assertTrue(state.rotTrail.get(0).damagePerSecond > 0f);
        float afterDeath = state.hero.health;
        assertTrue(afterDeath < 5_000f, "standing in the bloom cost nothing");
    }

    @Test
    void swarmcallOpensADoorInsteadOfEndingTheWave() {
        GameState state = runWithHero(0f, 0f);
        Enemy caller = elite(state, "swarmcall", 600f, 600f);
        int before = state.aliveEnemies.size();
        tick(affixes, state, DeepBandTuning.SWARMCALL_PERIOD + 0.2f, 0.1f);
        assertEquals(before + DeepBandTuning.SWARMCALL_CHILDREN, state.aliveEnemies.size());
        for (Enemy child : state.aliveEnemies) {
            assertNotNull(child);
            assertEquals(caller.enemyType, child.enemyType, "a call invented a new creature");
        }
        tick(affixes, state, DeepBandTuning.SWARMCALL_PERIOD, 0.1f);
        assertEquals(before + 2 * DeepBandTuning.SWARMCALL_CHILDREN, state.aliveEnemies.size(),
            "the call did not come again");
    }

    @Test
    void spitebarbOnlyPunishesWhatIsInsideItsReach() {
        GameState state = runWithHero(300f, 300f);
        Enemy barb = elite(state, "spitebarb", 300f + DeepBandTuning.SPITEBARB_RADIUS - 20f, 300f);
        tick(affixes, state, DeepBandTuning.SPITEBARB_TICK_SECONDS * 2f, 0.1f);
        assertTrue(state.hero.health < 5_000f, "hugging a spitebarb cost nothing");

        GameState far = runWithHero(300f, 300f);
        elite(far, "spitebarb", 300f + DeepBandTuning.SPITEBARB_RADIUS + 40f, 300f);
        tick(affixes, far, DeepBandTuning.SPITEBARB_TICK_SECONDS * 2f, 0.1f);
        assertEquals(5_000f, far.hero.health, 0.001f, "the barb reached past its own reach");
        assertTrue(barb.alive);
    }

    @Test
    void hammerfallWarnsBeforeItStrikesAndOnlyInsideTheRing() {
        GameState state = runWithHero(200f, 200f);
        Enemy hammer = elite(state, "hammerfall", 200f + DeepBandTuning.HAMMERFALL_RADIUS - 30f, 200f);
        assertEquals(-1f, EliteAffixSystem.hammerfallWindupProgress(hammer), "a resting hammer warned");
        affixes.update(state, 0.1f);
        float early = EliteAffixSystem.hammerfallWindupProgress(hammer);
        assertTrue(early >= 0f, "the warning never started");
        tick(affixes, state, DeepBandTuning.HAMMERFALL_WINDUP_SECONDS, 0.1f);
        assertTrue(state.hero.health < 5_000f, "standing in the ring cost nothing");

        GameState outside = runWithHero(200f, 200f);
        elite(outside, "hammerfall", 200f + DeepBandTuning.HAMMERFALL_RADIUS + 60f, 200f);
        tick(affixes, outside, DeepBandTuning.HAMMERFALL_WINDUP_SECONDS + 0.3f, 0.1f);
        assertEquals(5_000f, outside.hero.health, 0.001f, "the strike landed past the ring it drew");
    }

    @Test
    void bloodhowlHurriesTheLineAndForgetsNothingItShouldNot() {
        GameState state = runWithHero(0f, 0f);
        Enemy howler = elite(state, "bloodhowl", 1_000f, 1_000f);
        Enemy near = body(state, "ROOTLING", 1_000f + DeepBandTuning.BLOODHOWL_RADIUS - 30f, 1_000f);
        Enemy wolf = body(state, "GLOOM_WOLF", 1_000f + 40f, 1_000f);
        wolf.packSpeedMultiplier = 1.18f;
        Enemy otherElite = elite(state, "stoneshell", 1_000f + 50f, 1_000f);
        howler.affixTimerSeconds = 0f;

        affixes.update(state, 0.1f);
        assertEquals(1f + DeepBandTuning.BLOODHOWL_HASTE, near.packSpeedMultiplier, 0.0001f);
        assertEquals(1.18f, wolf.packSpeedMultiplier, 0.0001f, "the howl overwrote a better pack bonus");
        assertEquals(1f, otherElite.packSpeedMultiplier, 0.0001f, "an elite hurried another elite");

        near.x = 3_000f;
        affixes.update(state, 0.1f);
        assertEquals(1f, near.packSpeedMultiplier, 0.0001f,
            "a body kept a haste its howler no longer gave");

        assertFalse(howler.affixResolved);
    }

    @Test
    void theDeepBandNeverFiresBeforeItsWave() {
        for (String affixId : DEEP_BAND) {
            for (EliteAffix affix : EliteAffix.values()) {
                if (affixId.equals(affix.id())) {
                    assertTrue(affix.ordinal() >= EliteAffix.BASE_POOL_SIZE,
                        affixId + " would be drawn in a shallow run");
                }
            }
        }
    }
}
