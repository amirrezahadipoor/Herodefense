package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    void aWoundedThrallComesApartIntoFragmentsThatCostNothingExtra() {
        GameState state = atWave(EnemyRoleSystem.SPLIT_FROM_WAVE);
        Enemy thrall = body(state, EnemyType.BRAMBLE_THRALL, 300f, 500f);
        thrall.damage = 7f;
        thrall.health = thrall.maxHealth * EnemyRoleSystem.SPLIT_HEALTH_RATIO;
        float healthLeft = thrall.health;
        int before = state.aliveEnemies.size();

        EnemyRoleSystem.update(state, 1f / 60f);

        assertFalse(thrall.alive, "the thrall's body is the split");
        assertTrue(thrall.splitSpawned);
        assertTrue(thrall.killRewardsGranted, "the corpse pays nothing; the fragments carry the lineage's reward");
        assertEquals(before + EnemyRoleSystem.SPLIT_FRAGMENT_COUNT, state.aliveEnemies.size());

        float fragmentHealth = 0f;
        float fragmentDps = 0f;
        java.util.Set<Float> phasesSeen = new java.util.HashSet<>();
        for (int i = before; i < state.aliveEnemies.size(); i++) {
            Enemy fragment = state.aliveEnemies.get(i);
            assertEquals("ROOTLING", fragment.enemyType);
            assertTrue(fragment.alive);
            assertEquals(thrall.maxHealth * EnemyRoleSystem.SPLIT_FRAGMENT_HEALTH_SHARE,
                fragment.health, 0.001f);
            assertEquals(thrall.damage * 0.5f, fragment.damage, 0.001f);
            assertEquals(thrall.movementSpeed, fragment.movementSpeed, 0.001f);
            assertEquals(thrall.attackIntervalSeconds, fragment.attackIntervalSeconds, 0.001f);
            assertEquals(thrall.spawnLane, fragment.spawnLane);
            assertTrue(fragment.itemDropRolled && fragment.potionDropRolled,
                "the lineage rolls its drops once, on the corpse: the split mints no economy");
            assertFalse(fragment.killRewardsGranted, "and the fragments pay the standard rootling reward");
            assertNotEquals(thrall.x, fragment.x, "fragments land to either side of the body");
            phasesSeen.add(fragment.attackCooldownSeconds);
            fragmentHealth += fragment.health;
            fragmentDps += fragment.damage / fragment.attackIntervalSeconds;
        }
        assertEquals(thrall.maxHealth * EnemyRoleSystem.SPLIT_FRAGMENT_HEALTH_SHARE
                * EnemyRoleSystem.SPLIT_FRAGMENT_COUNT, fragmentHealth, 0.001f,
            "the fragments inherit a hair under the thrall's budget at the ratio -- the spike ceiling left no"
                + " room for the split to be exactly free, let alone dearer");
        assertTrue(fragmentHealth <= healthLeft + 0.001f, "and never more than the thrall had left");
        assertEquals(thrall.damage / thrall.attackIntervalSeconds, fragmentDps, 0.001f,
            "and exactly its contact damage per second: the split changes the shape of the fight, not its cost");
        assertEquals(EnemyRoleSystem.SPLIT_FRAGMENT_COUNT, phasesSeen.size(),
            "the fragments swing half an interval apart: two bodies sharing the thrall's rhythm would land on"
                + " the same frames, and the gates measure single-wave maxima, not averages");
    }

    @Test
    void theSplitWaitsForItsWaveItsRatioAndANonEliteBody() {
        GameState early = atWave(EnemyRoleSystem.SPLIT_FROM_WAVE - 1);
        Enemy thrall = body(early, EnemyType.BRAMBLE_THRALL, 300f, 500f);
        thrall.health = thrall.maxHealth * 0.1f;
        EnemyRoleSystem.update(early, 1f / 60f);
        assertTrue(thrall.alive, "one wave earlier the thrall is still one body");

        GameState whole = atWave(EnemyRoleSystem.SPLIT_FROM_WAVE);
        Enemy healthy = body(whole, EnemyType.BRAMBLE_THRALL, 300f, 500f);
        healthy.health = healthy.maxHealth * (EnemyRoleSystem.SPLIT_HEALTH_RATIO + 0.01f);
        EnemyRoleSystem.update(whole, 1f / 60f);
        assertTrue(healthy.alive, "above the ratio it holds together");

        GameState elite = atWave(EnemyRoleSystem.SPLIT_FROM_WAVE);
        Enemy marked = body(elite, EnemyType.BRAMBLE_THRALL, 300f, 500f);
        marked.eliteAffix = "BLIGHTBURST";
        marked.health = marked.maxHealth * 0.1f;
        EnemyRoleSystem.update(elite, 1f / 60f);
        assertTrue(marked.alive,
            "an elite keeps its contract: affix, fragment lore and trophy all assume one body to kill");
    }

    @Test
    void theHoundLungesOnAToldRhythmAndPunishesItself() {
        GameState state = atWave(EnemyRoleSystem.LUNGE_FROM_WAVE);
        Enemy hound = body(state, EnemyType.SAP_HOUND, 300f, 500f);
        float walk = hound.movementSpeed;

        hound.lungeBaseSpeed = walk;
        hound.lungeSeconds = -0.01f;
        EnemyRoleSystem.update(state, 0.02f);
        assertEquals(0f, hound.movementSpeed, 0.001f, "the windup is a standstill the player can read");

        EnemyRoleSystem.update(state, EnemyRoleSystem.LUNGE_WINDUP_SECONDS);
        assertEquals(walk * EnemyRoleSystem.LUNGE_DASH_SPEED_MULTIPLIER, hound.movementSpeed, 0.01f,
            "then the dash");

        EnemyRoleSystem.update(state, EnemyRoleSystem.LUNGE_DASH_SECONDS);
        assertTrue(hound.stunRemainingSeconds >= EnemyRoleSystem.LUNGE_RECOVERY_STUN_SECONDS - 0.001f,
            "the dash ends in a self-stun: the punishment window");
        assertEquals(walk, hound.movementSpeed, 0.001f);
        assertTrue(hound.lungeSeconds <= -EnemyRoleSystem.LUNGE_COOLDOWN_SECONDS + 0.001f,
            "and the cycle resets to its cooldown");

        hound.stunRemainingSeconds = 0f;
        EnemyRoleSystem.update(state, EnemyRoleSystem.LUNGE_COOLDOWN_SECONDS - 0.1f);
        assertEquals(walk, hound.movementSpeed, 0.001f, "the cooldown is ordinary walking");
        EnemyRoleSystem.update(state, 0.1f);
        assertEquals(0f, hound.movementSpeed, 0.001f,
            "and the moment the cooldown ends the next windup begins: the cycle has no seam");
    }

    @Test
    void theLungeCoversSlightlyLessGroundThanWalkingWould() {
        // One full cycle: windup stands still, dash covers dash-seconds at dash-speed, the recovery stun eats
        // the start of the cooldown, and the rest of the cooldown walks. The sum has to stay under the ground a
        // plain walking hound covers in the same time, or the lunge would be pressure -- and pressure is what
        // the trial-spike ceiling, medianing exactly at 0.40, has no room left for.
        float walk = 100f;
        float cycle = EnemyRoleSystem.LUNGE_WINDUP_SECONDS + EnemyRoleSystem.LUNGE_DASH_SECONDS
            + EnemyRoleSystem.LUNGE_COOLDOWN_SECONDS;
        float walkedCooldown = Math.max(0f, EnemyRoleSystem.LUNGE_COOLDOWN_SECONDS
            - EnemyRoleSystem.LUNGE_RECOVERY_STUN_SECONDS);
        float lunged = EnemyRoleSystem.LUNGE_DASH_SECONDS * EnemyRoleSystem.LUNGE_DASH_SPEED_MULTIPLIER * walk
            + walkedCooldown * walk;
        assertTrue(lunged < cycle * walk,
            "lunge ground " + lunged + " must stay under walking ground " + (cycle * walk));
    }

    @Test
    void theLungeStartsAtItsWaveAndNotOneEarlier() {
        GameState early = atWave(EnemyRoleSystem.LUNGE_FROM_WAVE - 1);
        Enemy hound = body(early, EnemyType.SAP_HOUND, 300f, 500f);
        float walk = hound.movementSpeed;
        EnemyRoleSystem.update(early, 1f);
        assertEquals(walk, hound.movementSpeed, 0.001f);
        assertEquals(0f, hound.lungeBaseSpeed, 0.001f, "one wave earlier the hound is still just fast");
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
        assertEquals(4, callSites,
            "arrow impact, chain arc, ultimate and the thorns reflection are the four ways an enemy takes"
                + " damage; a fifth call site means this scan has to look at it before the ward can be trusted");
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
