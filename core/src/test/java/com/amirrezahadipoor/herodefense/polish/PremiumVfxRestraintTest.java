package com.amirrezahadipoor.herodefense.polish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.render.ParticleRendererContract;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Locks the style-guide 0.6 VFX budget for every layered effect. */
final class PremiumVfxRestraintTest {
    @Test
    void normalHitStaysWithinOneCoreSixMotesAndAQuarterSecond() {
        ParticleSystem particles = new ParticleSystem();
        particles.emitHit(0f, 0f, false);
        long cores = count(particles, ParticleType.IMPACT_CORE);
        long motes = count(particles, ParticleType.HIT);
        assertEquals(VfxBudget.NORMAL_HIT_MAX_CORES, cores);
        assertTrue(motes <= VfxBudget.NORMAL_HIT_MAX_MOTES);
        assertEquals(0, count(particles, ParticleType.CRITICAL_RING));
        for (Particle particle : particles.particles()) {
            assertTrue(particle.lifetimeSeconds <= VfxBudget.NORMAL_HIT_MAX_LIFETIME_SECONDS);
        }
    }

    @Test
    void criticalAndBossEventsExceedOnlyThroughDocumentedMultipliers() {
        ParticleSystem particles = new ParticleSystem();
        particles.emitHit(0f, 0f, true);
        assertEquals(2, count(particles, ParticleType.CRITICAL_RING));
        assertTrue(count(particles, ParticleType.HIT)
            <= Math.round(VfxBudget.NORMAL_HIT_MAX_MOTES * VfxBudget.CRITICAL_MULTIPLIER));

        particles.clear();
        particles.emitBossEntrance(0f, 0f);
        assertEquals(2, count(particles, ParticleType.BOSS_SHOCKWAVE));
        assertTrue(count(particles, ParticleType.BOSS_DUST)
            <= Math.round(VfxBudget.NORMAL_HIT_MAX_MOTES * VfxBudget.BOSS_MULTIPLIER));

        particles.clear();
        particles.emitBossDeath(0f, 0f);
        assertEquals(1, count(particles, ParticleType.BOSS_SHOCKWAVE));
        assertEquals(1, count(particles, ParticleType.DEATH_RING));
    }

    @Test
    void ultimateBlastStaysWithinItsDocumentedBudget() {
        assertEquals(3.0f, VfxBudget.ULTIMATE_MULTIPLIER);
        assertEquals(8, VfxBudget.ULTIMATE_MAX_ARCS);
        ParticleSystem particles = new ParticleSystem();
        particles.emitUltimateBlast(0f, 0f);
        assertEquals(1, count(particles, ParticleType.BOSS_SHOCKWAVE));
        assertEquals(2, count(particles, ParticleType.CRITICAL_RING));
        assertEquals(
            Math.round(VfxBudget.NORMAL_HIT_MAX_MOTES * VfxBudget.ULTIMATE_MULTIPLIER),
            count(particles, ParticleType.HIT)
        );
        assertEquals(VfxBudget.ULTIMATE_SPARKS, count(particles, ParticleType.CRITICAL_SPARK));
    }

    @Test
    void skillEffectsStayCheaperThanANormalHitAndExpire() {
        ParticleSystem particles = new ParticleSystem();
        particles.emitChainArc(0f, 0f, 120f, 40f);
        assertEquals(1, count(particles, ParticleType.CHAIN_BEAM));
        assertEquals(1, count(particles, ParticleType.CHAIN_FLASH));
        assertTrue(count(particles, ParticleType.HIT) <= VfxBudget.CHAIN_ARC_MAX_MOTES);
        assertTrue(particles.particles().size() <= 1 + VfxBudget.NORMAL_HIT_MAX_MOTES);
        Particle beam = particles.particles().get(0);
        assertEquals(120f, beam.endX);
        assertEquals(40f, beam.endY);
        for (Particle particle : particles.particles()) {
            assertTrue(particle.lifetimeSeconds <= VfxBudget.NORMAL_HIT_MAX_LIFETIME_SECONDS);
        }

        particles.clear();
        particles.emitStunSparks(0f, 0f, 5f);
        assertEquals(VfxBudget.STUN_SPARKS, count(particles, ParticleType.STUN_SPARK));
        for (Particle particle : particles.particles()) {
            assertTrue(particle.lifetimeSeconds <= 1.6f, "stun sparks are capped");
        }
        particles.update(1.7f);
        assertTrue(particles.particles().isEmpty());

        particles.clear();
        particles.emitHit(0f, 0f, true);
        assertEquals(2, count(particles, ParticleType.CRITICAL_RING));
        assertEquals(VfxBudget.CRITICAL_SPARKS, count(particles, ParticleType.CRITICAL_SPARK));
        assertTrue(ParticleType.CHAIN_BEAM.isBeam());
        assertFalse(ParticleType.CHAIN_BEAM.isRing());
        assertTrue(Math.abs(ParticleRendererContract.beamJitter(1f, 3)) <= 1f);
    }

    @Test
    void deathCollectionAndTreeEffectsAreBoundedAndExpire() {
        ParticleSystem particles = new ParticleSystem();
        particles.emitDeath(0f, 0f);
        assertEquals(1, count(particles, ParticleType.DEATH_RING));
        particles.emitCollectionSparkle(0f, 0f);
        assertTrue(count(particles, ParticleType.COLLECTION_SPARKLE) <= 6);
        particles.emitTreeDestruction(360f, 755f);
        assertTrue(count(particles, ParticleType.TREE_LEAF) <= 18);
        assertTrue(particles.particles().size() <= ParticleSystem.MAX_PARTICLES);
        particles.update(3f);
        assertTrue(particles.particles().isEmpty());
    }

    @Test
    void ringsExpandWhileMotesShrinkAndCoresFlash() {
        assertTrue(ParticleRendererContract.ringRadius(100f, 1f)
            < ParticleRendererContract.ringRadius(100f, 0.5f));
        assertTrue(ParticleRendererContract.ringRadius(100f, 0.5f)
            < ParticleRendererContract.ringRadius(100f, 0f));
        assertEquals(100f, ParticleRendererContract.ringRadius(100f, 0f), 0.001f);
        assertEquals(1f, ParticleRendererContract.moteScale(ParticleType.HIT, 1f));
        assertEquals(0f, ParticleRendererContract.moteScale(ParticleType.HIT, 0f));
        assertTrue(ParticleRendererContract.moteScale(ParticleType.IMPACT_CORE, 0.25f)
            > ParticleRendererContract.moteScale(ParticleType.HIT, 0.25f));
        assertTrue(ParticleType.CRITICAL_RING.isRing());
        assertFalse(ParticleType.HIT.isRing());
    }

    @Test
    void ambientSporesStayBelowCharacterContrastAndInsideTheArena() {
        for (int index = 0; index < AmbientMoteField.COUNT; index++) {
            for (int step = 0; step < 172; step++) {
                float time = step * 0.7f;
                float alpha = AmbientMoteField.alpha(index, time);
                assertTrue(alpha >= 0f && alpha <= VfxBudget.AMBIENT_MAX_ALPHA);
                float x = AmbientMoteField.x(index, time);
                float y = AmbientMoteField.y(index, time);
                assertTrue(x >= 0f && x <= 720f);
                assertTrue(y >= 150f && y <= 1030f);
            }
        }
        assertEquals(AmbientMoteField.x(3, 4.5f), AmbientMoteField.x(3, 4.5f));
    }

    @Test
    void shakeImpulsesForBossEntranceAndTreeFallStayRestrained() {
        ScreenShakeSystem shake = new ScreenShakeSystem();
        shake.triggerBossEntrance();
        assertTrue(Math.abs(shake.offsetX()) <= 8f);
        shake.update(0.25f);
        assertFalse(shake.active());
        shake.triggerTreeFall();
        assertTrue(Math.abs(shake.offsetX()) <= 9f);
        shake.update(0.61f);
        assertFalse(shake.active());
    }

    @Test
    void gameBindsEveryLayeredEventExactlyOnce() throws IOException {
        // The layered effects are wired by the game loop and by the three classes extracted from it
        // (roadmap R2.2). All four files are scanned together and each binding must appear exactly once,
        // so moving a call cannot silently duplicate an effect or drop it.
        String game = Files.readString(Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/HeroDefenseGame.java"
        )) + "\n" + Files.readString(Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/presentation/RunPresentationSystem.java"
        )) + "\n" + Files.readString(Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/presentation/ScreenStateComposer.java"
        )) + "\n" + Files.readString(Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/gameplay/WaveDirector.java"
        ));
        // Every layered event is bound exactly once, except the tree destruction, which is bound twice on
        // purpose: once for the world tree and once for each grove tree.
        assertEquals(1, occurrences(game, "emitBossEntrance("), "emitBossEntrance(");
        assertEquals(1, occurrences(game, "emitBossDeath("), "emitBossDeath(");
        assertEquals(2, occurrences(game, "emitTreeDestruction("), "emitTreeDestruction(");
        assertEquals(1, occurrences(game, "emitCollectionSparkle("), "emitCollectionSparkle(");
        assertEquals(1, occurrences(game, "drawAmbient("), "drawAmbient(");
        assertEquals(1, occurrences(game, "triggerBossEntrance()"), "triggerBossEntrance()");
        assertEquals(1, occurrences(game, "triggerTreeFall()"), "triggerTreeFall()");
        assertTrue(game.contains("boss.entrancePresented"));
        String projectiles = Files.readString(Path.of(
            "src/main/java/com/amirrezahadipoor/herodefense/render/ProjectileRenderer.java"
        ));
        assertTrue(projectiles.contains("PROJECTILE_TRAIL_STEPS"));
    }

    private static int occurrences(String text, String needle) {
        int total = 0;
        int index = text.indexOf(needle);
        while (index >= 0) {
            total++;
            index = text.indexOf(needle, index + needle.length());
        }
        return total;
    }

    private static long count(ParticleSystem particles, ParticleType type) {
        return particles.particles().stream().filter(p -> p.type == type).count();
    }
}
