package com.amirrezahadipoor.herodefense.audio;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * Identity-sensitive cue selection (roadmap F2): the same event on different bodies must not sound like the
 * same recording, because audible reuse is what makes twelve identities feel like one. Every mapping here
 * falls back to a shipped cue, so a state this class cannot read still plays the game it played before F2.
 */
public final class IdentityCues {
    private IdentityCues() {
    }

    /** The death cue for a body class: swift small bodies fall light, armoured masses collapse heavy. */
    public static AudioCue deathFor(EnemyType type) {
        if (type == null) {
            return AudioCue.DEATH;
        }
        if (type == EnemyType.ROOTLING || type == EnemyType.GLOOM_WOLF
            || type == EnemyType.BARK_STALKER || type == EnemyType.SAP_HOUND) {
            return AudioCue.DEATH_LIGHT;
        }
        return AudioCue.DEATH_HEAVY; // STONEKIN, FUNGAL_BRUTE, HUSK_WARDEN, BRAMBLE_THRALL
    }

    /**
     * The entrance voice of the newest boss on the field. The golem's stone falls, the wyrm shrieks, the
     * knight's hollow collapses inward; the Thorn Matriarch keeps the shipped horn.
     */
    public static AudioCue bossEntranceFor(GameState state) {
        BossType type = newestBossType(state);
        if (type == null) {
            return AudioCue.BOSS_ENTRANCE;
        }
        return switch (type) {
            case ANCIENT_GOLEM -> AudioCue.BOSS_ENTRANCE_DEEP;
            case EMBER_WYRM -> AudioCue.BOSS_ENTRANCE_SHRIEK;
            case VOID_KNIGHT -> AudioCue.BOSS_ENTRANCE_VOID;
            case THORN_MATRIARCH -> AudioCue.BOSS_ENTRANCE;
            case FROST_TITAN -> AudioCue.BOSS_ENTRANCE_DEEP;
            case SHADOW_LICH -> AudioCue.BOSS_ENTRANCE_VOID;
            case STORM_COLOSSUS -> AudioCue.BOSS_ENTRANCE_DEEP;
            case BLOODROOT_AVATAR -> AudioCue.BOSS_ENTRANCE;
        };
    }

    /** The type of the freshest corpse still in the enemy list, or null when nothing has fallen. */
    public static EnemyType newestCorpseType(GameState state) {
        if (state == null || state.aliveEnemies == null) {
            return null;
        }
        for (int index = state.aliveEnemies.size() - 1; index >= 0; index--) {
            Enemy enemy = state.aliveEnemies.get(index);
            if (enemy != null && !enemy.alive) {
                return enemy.type();
            }
        }
        return null;
    }

    private static BossType newestBossType(GameState state) {
        if (state == null || state.aliveBosses == null) {
            return null;
        }
        for (int index = state.aliveBosses.size() - 1; index >= 0; index--) {
            Boss boss = state.aliveBosses.get(index);
            if (boss == null || !boss.alive || boss.bossType == null) {
                continue;
            }
            try {
                return BossType.valueOf(boss.bossType);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }
}
