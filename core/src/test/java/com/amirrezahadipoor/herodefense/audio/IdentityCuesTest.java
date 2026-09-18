package com.amirrezahadipoor.herodefense.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Roadmap F2: every body class and every boss body gets its own sound, and every fallback is a shipped cue. */
final class IdentityCuesTest {
    @Test
    void everyEnemyTypeDiesInItsOwnBodyClass() {
        Set<AudioCue> deaths = EnumSet.noneOf(AudioCue.class);
        for (EnemyType type : EnemyType.values()) {
            AudioCue cue = IdentityCues.deathFor(type);
            assertEquals(true, cue == AudioCue.DEATH_LIGHT || cue == AudioCue.DEATH_HEAVY,
                type + " must land in a body class, not the generic death");
            deaths.add(cue);
        }
        assertEquals(2, deaths.size(), "both body classes are used by the roster");
        assertEquals(AudioCue.DEATH, IdentityCues.deathFor(null), "an unreadable body falls back to the shipped death");
    }

    @Test
    void everyBossBodyAnnouncesItselfInItsOwnVoice() {
        Set<AudioCue> entrances = EnumSet.noneOf(AudioCue.class);
        for (String body : new String[] {"ANCIENT_GOLEM", "THORN_MATRIARCH", "EMBER_WYRM", "VOID_KNIGHT", "FROST_TITAN", "SHADOW_LICH", "STORM_COLOSSUS", "BLOODROOT_AVATAR"}) {
            GameState state = GameState.newRun(5L);
            state.aliveBosses.add(new Boss(state.allocateEntityId(), body, 10f, 10f, 1));
            entrances.add(IdentityCues.bossEntranceFor(state));
        }
        assertEquals(8, entrances.size(), "eight bodies, eight voices — D3");
        assertEquals(AudioCue.BOSS_ENTRANCE, IdentityCues.bossEntranceFor(GameState.newRun(5L)),
            "no boss on the field falls back to the shipped horn");
        assertEquals(AudioCue.BOSS_ENTRANCE, IdentityCues.bossEntranceFor(null));
    }

    @Test
    void theFreshestCorpseNamesTheDeathSound() {
        GameState state = GameState.newRun(5L);
        Enemy light = new Enemy(state.allocateEntityId(), "GLOOM_WOLF", 0f, 0f);
        light.alive = false;
        state.aliveEnemies.add(light);
        assertEquals(EnemyType.GLOOM_WOLF, IdentityCues.newestCorpseType(state));
        Enemy heavy = new Enemy(state.allocateEntityId(), "FUNGAL_BRUTE", 0f, 0f);
        heavy.alive = false;
        state.aliveEnemies.add(heavy);
        assertEquals(EnemyType.FUNGAL_BRUTE, IdentityCues.newestCorpseType(state),
            "the newest corpse wins, because it is the one this frame killed");
        assertEquals(AudioCue.DEATH_HEAVY,
            IdentityCues.deathFor(IdentityCues.newestCorpseType(state)));
        assertEquals(null, IdentityCues.newestCorpseType(GameState.newRun(5L)));
    }

    @Test
    void anUnreadableBossNameFallsBackToTheShippedHorn() {
        GameState state = GameState.newRun(5L);
        state.aliveBosses.add(new Boss(state.allocateEntityId(), "NOT_A_BOSS", 10f, 10f, 1));
        assertEquals(AudioCue.BOSS_ENTRANCE, IdentityCues.bossEntranceFor(state));
    }
}
