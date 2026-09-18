package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * The D4 shaders held to the contracts a unit test can check without a GL context (roadmap D4).
 *
 * <p>Compilation itself is the emulator smoke run's job -- the renderers throw at construction in
 * pedantic mode, and CI starts the game on a real (virtual) device. What this test catches is the
 * mistake that does NOT throw: a uniform the Java sets under one name and the shader declares under
 * another. A mistyped uniform is silently ignored at the driver level, the effect draws with a
 * default value, and nothing fails anywhere -- exactly the class of dead-asset mistake
 * {@link InternalAssetReferences} was born from. So both directions are pinned by reading the
 * sources: every uniform the fragment stages declare is set by the renderer, and every uniform the
 * renderer sets is declared by one of the fragment stages.
 */
final class ArenaAtmosphereShaderTest {

    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path SHADERS = REPOSITORY.resolve("android/assets/shaders");
    private static final Path RENDERER = REPOSITORY.resolve(
        "core/src/main/java/com/amirrezahadipoor/herodefense/render/ArenaAtmosphereRenderer.java");

    private static final Pattern FRAGMENT_UNIFORM = Pattern.compile("uniform\\s+\\w+\\s+(u_\\w+)\\s*;");
    private static final Pattern JAVA_UNIFORM = Pattern.compile("setUniformf\\(\\s*\"(u_\\w+)\"");
    private static final Pattern VERTEX_CONTRACT = Pattern.compile(
        "attribute vec4 a_position;|attribute vec4 a_color;|attribute vec2 a_texCoord0;"
            + "|uniform mat4 u_projTrans;|varying vec4 v_color;|varying vec2 v_texCoords;");

    @Test
    void everyFragmentUniformIsSetAndEverySetUniformIsDeclared() throws IOException {
        Set<String> declared = new TreeSet<>();
        declared.addAll(uniforms(SHADERS.resolve("arena-veil.frag"), FRAGMENT_UNIFORM));
        declared.addAll(uniforms(SHADERS.resolve("boss-aura.frag"), FRAGMENT_UNIFORM));
        Set<String> set = new TreeSet<>(uniforms(RENDERER, JAVA_UNIFORM));

        List<String> unset = new ArrayList<>(declared);
        unset.removeAll(set);
        assertTrue(unset.isEmpty(), "fragment uniforms no Java call ever sets: " + unset);
        List<String> undeclared = new ArrayList<>(set);
        undeclared.removeAll(declared);
        assertTrue(undeclared.isEmpty(), "uniforms set against no fragment declaration: " + undeclared);

        // The exact vocabulary, so a rename on either side trips here rather than in a driver.
        assertEquals(
            Set.of("u_time", "u_tint", "u_aspect", "u_strength", "u_color", "u_pulse"),
            declared);
    }

    @Test
    void bothVertexShadersSpeakTheSpriteBatchContract() throws IOException {
        for (String name : List.of("arena-veil.vert", "boss-aura.vert")) {
            String source = Files.readString(SHADERS.resolve(name));
            assertEquals(6, matches(source, VERTEX_CONTRACT),
                name + " must declare the four SpriteBatch inputs and both varyings verbatim");
            assertTrue(source.contains("gl_Position = u_projTrans * a_position;"), name);
        }
    }

    @Test
    void bothEffectsStayClampedToAWhisper() throws IOException {
        // Premium restraint, pinned where it lives: the veil never passes 0.14 alpha and fades toward
        // the bottom of the frame; the aura never passes 0.5.
        String veil = Files.readString(SHADERS.resolve("arena-veil.frag"));
        assertTrue(veil.contains("clamp(") && veil.contains("0.0, 0.14"), "the veil keeps its ceiling");
        assertTrue(veil.contains("smoothstep(0.0, 0.55, v_texCoords.y)"), "and its fight-stays-clear fade");
        String aura = Files.readString(SHADERS.resolve("boss-aura.frag"));
        assertTrue(aura.contains("clamp(") && aura.contains("0.0, 0.5"), "the aura keeps its ceiling");
        assertTrue(aura.contains("* u_pulse"), "the breath answers to the reduced-motion multiplier");
    }

    @Test
    void theAuraSitsUnderTheBossFeetNotItsHead() {
        // boss.y is the anchor CombatEntityRenderer draws from: the sprite's feet sit 240 * 30/256
        // below it, and the pool is quarter-sunk around that line.
        float[] bounds = ArenaAtmosphereRenderer.auraBounds(360f, 500f);
        float feetY = 500f - 240f * CombatEntityRenderer.BOSS_FEET_RATIO;
        assertEquals(210f, bounds[0], "centred on the boss");
        assertEquals(feetY - ArenaAtmosphereRenderer.AURA_HEIGHT * 0.25f, bounds[1]);
        assertEquals(ArenaAtmosphereRenderer.AURA_WIDTH, bounds[2]);
        assertEquals(ArenaAtmosphereRenderer.AURA_HEIGHT, bounds[3]);
        assertTrue(bounds[1] < feetY && feetY < bounds[1] + bounds[3],
            "the feet line falls inside the pool, in its lower half");
        assertTrue(feetY - bounds[1] < bounds[3] * 0.5f, "and below the pool's centre");
    }

    @Test
    void theVeilTintIsTheStageGradeLiftedByItsOwnChannels() {
        StageGrade grade = StageGrade.forWave(1);
        float[] tint = ArenaAtmosphereRenderer.veilTint(grade);
        assertEquals(3, tint.length);
        for (int channel = 0; channel < 3; channel++) {
            assertEquals(grade.channel(1f, channel), tint[channel], 0.0001f, "channel " + channel);
            assertTrue(tint[channel] > 0f && tint[channel] <= 1f, "a tint, not a darkness");
        }
    }

    private static Set<String> uniforms(Path file, Pattern pattern) throws IOException {
        assertTrue(Files.isRegularFile(file), "missing " + file);
        Set<String> names = new TreeSet<>();
        Matcher matcher = pattern.matcher(Files.readString(file));
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private static int matches(String source, Pattern pattern) {
        Matcher matcher = pattern.matcher(source);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
