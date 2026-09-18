package com.amirrezahadipoor.herodefense.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.DropCollectionStage;
import com.amirrezahadipoor.herodefense.model.DropEntity;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.GameMode;
import com.amirrezahadipoor.herodefense.model.Item;
import com.amirrezahadipoor.herodefense.model.Projectile;
import org.junit.jupiter.api.Test;

final class GameStateCodecTest {
    @Test
    void roundTripsTheWholeGameState() {
        GameState source = GameState.newRun(90210L);
        source.waveNumber = 25;
        source.coins = 417;
        source.heroLevel = 18;
        source.unspentTalentPoints = 2;
        source.hero.health = 73f;

        Enemy enemy = new Enemy(source.allocateEntityId(), "STONEKIN", 20f, 30f);
        enemy.health = enemy.maxHealth = 120f;
        source.aliveEnemies.add(enemy);
        Boss boss = new Boss(source.allocateEntityId(), "THORN_MATRIARCH", 50f, 60f, 7);
        boss.health = boss.maxHealth = 900f;
        source.aliveBosses.add(boss);
        source.projectiles.add(new Projectile(source.allocateEntityId(), source.hero.id, enemy.id, 1f, 2f));
        DropEntity drop = new DropEntity(source.allocateEntityId(), "COIN", 3f, 4f, 12);
        drop.collectionStage = DropCollectionStage.HOMING;
        drop.homingElapsedSeconds = 0.2f;
        source.drops.add(drop);

        Item item = new Item("weapon_01", "Ashwood Bow", "WEAPON", "COMMON");
        item.statBonuses.put("STRENGTH", 2f);
        source.inventory.add(item);
        source.equippedItems.put("WEAPON", item);
        source.permanentEffects.put("LIFESTEAL", 0.04f);
        source.shopUpgradeLevels.put("STRENGTH", 4);
        source.chosenRewardCards.put("7", "CARD_LIFESTEAL");
        source.healthPotions.set(2, 3);
        source.mode = GameMode.BRIEF;
        source.activeTrials.add("HOLLOW_OMENS");
        source.trophies.recordWaveCleared();
        source.trophies.wavesCleared = 42;
        source.trophies.award(com.amirrezahadipoor.herodefense.progression.Trophy.BARE_HANDS);

        GameState restored = new GameStateCodec().decode(new GameStateCodec().encode(source));

        assertEquals(25, restored.waveNumber, "the saved wave survives, inside the brief mode's own length");
        assertEquals(417, restored.coins);
        assertEquals(73f, restored.hero.health);
        assertEquals("STONEKIN", restored.aliveEnemies.get(0).enemyType);
        assertEquals("THORN_MATRIARCH", restored.aliveBosses.get(0).bossType);
        assertEquals(DropCollectionStage.HOMING, restored.drops.get(0).collectionStage);
        assertEquals(0.2f, restored.drops.get(0).homingElapsedSeconds);
        assertEquals("Ashwood Bow", restored.inventory.get(0).name);
        assertEquals(3, restored.healthPotions.get(2));
        assertEquals(4, restored.shopUpgradeLevels.get("STRENGTH"));
        assertEquals("CARD_LIFESTEAL", restored.chosenRewardCards.get("7"));
        assertFalse(restored.runComplete);
        assertEquals(GameMode.BRIEF, restored.mode, "the run length is part of the save");
        assertTrue(restored.activeTrials.contains("HOLLOW_OMENS"),
            "and so are the trials the player drafted");
        assertEquals(42, restored.trophies.wavesCleared, "and so is what the trophies remember");
        assertTrue(restored.trophies.isEarned(com.amirrezahadipoor.herodefense.progression.Trophy.BARE_HANDS));
    }
}
