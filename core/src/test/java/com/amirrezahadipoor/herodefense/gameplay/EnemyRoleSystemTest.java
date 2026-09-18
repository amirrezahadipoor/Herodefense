package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Roadmap A3: the second half of a run gains behaviour, not just bigger numbers.
 *
 * <p>The ward and the berserk are asserted here as the pipeline experiences them -- damage through
 * {@code EnemyRoleSystem.damageTo}, speed and interval as the latch leaves them -- and the wave gates are
 * asserted from both sides, because a role that leaks into the first thirty waves would change the one session
 * the game promises not to punish, and a role that never arrives is the plateau the audit named.
 */
final class EnemyRoleSystemTest {

    private static GameState atWave(int wave) {
        GameState state = GameState.newRun(0xA3E01E5L);
        state.waveNumber = wave;
        return state;
    }

    private static Enemy body(GameState state, EnemyType type, float x, float y) {
        Enemy enemy = new Enemy(state.allocateEntityId(), type.name(), x, y);
        enemy.health = type.baseHealth();
        enemy.maxHealth = type.baseHealth();
        enemy.movementSpeed = type.movementSpeed();
        enemy.attackIntervalSeconds = type.attackIntervalSeconds();
        state.aliveEnemies.add(enemy);
        return enemy;
    }

    @Test
    void aLivingWardenWardsTheBodiesAroundItIncludingItself() {
        GameState state = atWave(EnemyRoleSystem.WARD_FROM_WAVE);
        Enemy warden = body(state, EnemyType.HUSK_WARDEN, 300f, 500f);
        Enemy shielded = body(state, EnemyType.ROOTLING, 360f, 520f);
        Enemy far = body(state, EnemyType.ROOTLING, 300f + EnemyRoleSystem.WARD_RADIUS + 40f, 500f);

        assertTrue(EnemyRoleSystem.isWarded(state, shielded), "a body inside the radius is warded");
        assertTrue(EnemyRoleSystem.isWarded(state, warden), "and the warden stands inside its own ward");
        assertFalse(EnemyRoleSystem.isWarded(state, far), "a body outside it is not");
        assertEquals(100f * EnemyRoleSystem.WARD_DAMAGE_MULTIPLIER,
            EnemyRoleSystem.damageTo(state, shielded, 100f), 0.001f);
        assertEquals(100f, EnemyRoleSystem.damageTo(state, far, 100f), 0.001f);

        warden.alive = false;
        assertFalse(EnemyRoleSystem.isWarded(state, shielded),
            "the ward dies with the warden, in the same tick: that is the counter the mark exists for");
        assertEquals(100f, EnemyRoleSystem.damageTo(state, shielded, 100f), 0.001f);
    }

    @Test
    void theWardStartsAtItsWaveAndNotOneEarlier() {
        GameState late = atWave(EnemyRoleSystem.WARD_FROM_WAVE);
        body(late, EnemyType.HUSK_WARDEN, 300f, 500f);
        Enemy warded = body(late, EnemyType.ROOTLING, 320f, 500f);
        assertTrue(EnemyRoleSystem.isWarded(late, warded));

        GameState early = atWave(EnemyRoleSystem.WARD_FROM_WAVE - 1);
        body(early, EnemyType.HUSK_WARDEN, 300f, 500f);
        Enemy plain = body(early, EnemyType.ROOTLING, 320f, 500f);
        assertFalse(EnemyRoleSystem.isWarded(early, plain),
            "wave " + (EnemyRoleSystem.WARD_FROM_WAVE - 1) + " fights the roster the first sweep measured");
    }

    @Test
    void theBriefVigilNeverMeetsARole() {
        GameState brief = atWave(30);
        body(brief, EnemyType.HUSK_WARDEN, 300f, 500f);
        Enemy brute = body(brief, EnemyType.FUNGAL_BRUTE, 320f, 500f);
        brute.health = brute.maxHealth * 0.1f;
        EnemyRoleSystem.update(brief, 1f / 60f);
        assertFalse(EnemyRoleSystem.isWarded(brief, brute));
        assertFalse(brute.enraged,
            "the thirty-wave vigil is the session that must not punish a new player; the roles wait for wave 101");
    }

    @Test
    void aWoundedBruteLatchesOnceAndOnlyPastItsWave() {
        GameState state = atWave(EnemyRoleSystem.ENRAGE_FROM_WAVE);
        Enemy brute = body(state, EnemyType.FUNGAL_BRUTE, 300f, 500f);
        brute.health = brute.maxHealth * (EnemyRoleSystem.ENRAGE_HEALTH_RATIO - 0.01f);
        float speed = brute.movementSpeed;
        float interval = brute.attackIntervalSeconds;

        EnemyRoleSystem.update(state, 1f / 60f);
        assertTrue(brute.enraged);
        assertEquals(speed * EnemyRoleSystem.ENRAGE_SPEED_MULTIPLIER, brute.movementSpeed, 0.001f);
        assertEquals(interval * EnemyRoleSystem.ENRAGE_INTERVAL_MULTIPLIER,
            brute.attackIntervalSeconds, 0.001f);

        EnemyRoleSystem.update(state, 1f / 60f);
        assertEquals(speed * EnemyRoleSystem.ENRAGE_SPEED_MULTIPLIER, brute.movementSpeed, 0.001f,
            "the latch sets once; a second tick must not compound it");

        GameState early = atWave(EnemyRoleSystem.ENRAGE_FROM_WAVE - 1);
        Enemy calm = body(early, EnemyType.FUNGAL_BRUTE, 300f, 500f);
        calm.health = calm.maxHealth * 0.1f;
        EnemyRoleSystem.update(early, 1f / 60f);
        assertFalse(calm.enraged, "one wave earlier the brute is still only a sponge");

        GameState whole = atWave(EnemyRoleSystem.ENRAGE_FROM_WAVE);
        Enemy healthy = body(whole, EnemyType.FUNGAL_BRUTE, 300f, 500f);
        healthy.health = healthy.maxHealth * (EnemyRoleSystem.ENRAGE_HEALTH_RATIO + 0.06f);
        EnemyRoleSystem.update(whole, 1f / 60f);
        assertFalse(healthy.enraged, "and above the ratio the berserk waits");

        GameState wrongBody = atWave(EnemyRoleSystem.ENRAGE_FROM_WAVE);
        Enemy wolf = body(wrongBody, EnemyType.GLOOM_WOLF, 300f, 500f);
        wolf.health = 1f;
        EnemyRoleSystem.update(wrongBody, 1f / 60f);
        assertFalse(wolf.enraged, "the berserk is the brute's role, not the roster's");
    }

    @Test
    void everyCallSiteThatHurtsAnEnemyGoesThroughTheWard() {
        List<String> unwarded = new ArrayList<>();
        int callSites = 0;
        for (Path root : List.of(Path.of("..", "core", "src", "main", "java").normalize())) {
            try (Stream<Path> files = Files.walk(root)) {
                for (Path path : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                    String source = read(path);
                    int from = 0;
                    while (true) {
                        int at = source.indexOf(".receiveDamage(", from);
                        if (at < 0) {
                            break;
                        }
                        int end = source.indexOf(");", at);
                        String statement = source.substring(at, end);
                        callSites++;
                        if (!statement.contains("EnemyRoleSystem.damageTo")) {
                            unwarded.add(path.getFileName() + ": " + statement.split("\\n")[0]);
                        }
                        from = end;
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException("cannot walk the main sources", e);
            }
        }
        assertEquals(3, callSites,
            "arrow impact, chain arc and ultimate are the three ways an enemy takes damage; a fourth call site"
                + " means this scan has to look at it before the ward can be trusted");
        assertTrue(unwarded.isEmpty(),
            "damage that skips the ward is a shield that works only against arrows: " + unwarded);
    }

    @Test
    void theSweepsFightTheRolesBecauseTheyRunTheRealCombat() {
        String simulator = read(Path.of("..", "core", "src", "main", "java",
            "com", "amirrezahadipoor", "herodefense", "balance", "BalanceSimulator.java").normalize());
        assertTrue(simulator.contains("EnemyRoleSystem.update(state"),
            "the simulator wires its systems by hand instead of hosting CombatSystem, so the roles have to tick"
                + " in its loop explicitly -- otherwise the balance gate publishes bands for a game that does"
                + " not ship");
        assertTrue(simulator.contains("new HeroAutoAttackSystem(") && simulator.contains("new HeroUltimateSystem("),
            "and the ward reaches the sweeps through the same two systems the live game fires");
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path.toAbsolutePath(), e);
        }
    }
}
