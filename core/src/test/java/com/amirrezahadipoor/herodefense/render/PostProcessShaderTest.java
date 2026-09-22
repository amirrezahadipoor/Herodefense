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
 * The E1 chain held to everything a unit test can hold without a GL context (roadmap E1).
 *
 * <p>Compilation on real GL is the CI emulator's job: the renderer throws at construction in
 * pedantic mode and the smoke run builds the stack. What must never be silent is the other failure
 * class -- a uniform set under one name in Java and declared under another in GLSL is ignored by
 * the driver, the pass draws with a default, and no gate anywhere notices. That is the exact
 * silence {@link InternalAssetReferences} was born from, so the vocabulary is pinned in both
 * directions, the restraint constants are pinned in the class that owns them, and the fallback
 * arithmetic is pinned as the pure function it is.
 */
final class PostProcessShaderTest {

    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path SHADERS = REPOSITORY.resolve("android/assets/shaders");
    private static final Path RENDERER = REPOSITORY.resolve(
        "core/src/main/java/com/amirrezahadipoor/herodefense/render/PostProcessRenderer.java");

    private static final Pattern FRAGMENT_UNIFORM = Pattern.compile("uniform\\s+\\w+\\s+(u_\\w+)\\s*;");
    private static final Pattern JAVA_UNIFORM = Pattern.compile("setUniform[if]\\(\\s*\"(u_\\w+)\"");
    private static final Pattern VERTEX_CONTRACT = Pattern.compile(
        "attribute vec4 a_position;|attribute vec4 a_color;|attribute vec2 a_texCoord0;"
            + "|uniform mat4 u_projTrans;|varying vec4 v_color;|varying vec2 v_texCoords;");

    private static final List<String> FRAGMENTS =
        List.of("post-bright.frag", "post-blur.frag", "post-composite.frag");

    @Test
    void everyFragmentUniformIsSetAndEverySetUniformIsDeclared() throws IOException {
        Set<String> declared = new TreeSet<>();
        for (String fragment : FRAGMENTS) {
            declared.addAll(uniforms(SHADERS.resolve(fragment), FRAGMENT_UNIFORM));
        }
        // u_texture is the SpriteBatch's own sampler: the batch binds and sets it per draw, and no
        // application code may touch it -- which is itself worth pinning.
        assertTrue(declared.remove("u_texture"), "every fragment stage samples the batch texture");
        String rendererSource = Files.readString(RENDERER);
        assertTrue(!JAVA_UNIFORM.matcher(rendererSource).results()
                .anyMatch(match -> "u_texture".equals(match.group(1))),
            "the renderer never sets the batch's own sampler by hand");

        Set<String> set = new TreeSet<>(uniforms(RENDERER, JAVA_UNIFORM));
        List<String> unset = new ArrayList<>(declared);
        unset.removeAll(set);
        assertTrue(unset.isEmpty(), "fragment uniforms no Java call ever sets: " + unset);
        List<String> undeclared = new ArrayList<>(set);
        undeclared.removeAll(declared);
        assertTrue(undeclared.isEmpty(), "uniforms set against no fragment declaration: " + undeclared);

        assertEquals(
            Set.of("u_threshold", "u_texelStep", "u_bloom", "u_bloomIntensity", "u_vignette", "u_pulse",
                "u_exposure"),
            declared);
    }

    /**
     * The night lift is a lift, not a wash: black stays black, white stays white, nothing between them
     * falls or clips, and a dark ground pixel comes back brighter by about the exposure. The shader runs
     * the same expression per channel, and the source is held to it so the two cannot drift apart.
     */
    @Test
    void theNightLiftBrightensTheDarkAndLeavesBlackAndWhiteAlone() throws IOException {
        float exposure = PostProcessRenderer.EXPOSURE;
        assertTrue(exposure > 1f && exposure <= 2f, "a lift the arithmetic stays monotonic under: " + exposure);
        assertEquals(0f, PostProcessRenderer.lift(0f, exposure), "black is still black");
        assertEquals(1f, PostProcessRenderer.lift(1f, exposure), 1e-6f, "white is still white");
        float dark = 37f / 255f;
        float lifted = PostProcessRenderer.lift(dark, exposure);
        assertTrue(lifted > dark * 1.25f, "the night ground gains a quarter or more: " + lifted / dark);
        float previous = 0f;
        for (int step = 1; step <= 100; step++) {
            float value = PostProcessRenderer.lift(step / 100f, exposure);
            assertTrue(value > previous, "monotonic at " + step);
            assertTrue(value <= 1f, "never clips at " + step);
            previous = value;
        }
        String fragment = Files.readString(SHADERS.resolve("post-composite.frag"));
        assertTrue(fragment.contains("lit * (u_exposure - (u_exposure - 1.0) * lit)"),
            "the shader runs the same lift the test holds");
        String bright = Files.readString(SHADERS.resolve("post-bright.frag"));
        assertTrue(!bright.contains("u_exposure"), "the bright pass reads the scene before the lift");
    }

    @Test
    void theSharedVertexShaderSpeaksTheSpriteBatchContract() throws IOException {
        String source = Files.readString(SHADERS.resolve("post-process.vert"));
        assertEquals(6, matches(source, VERTEX_CONTRACT),
            "the four SpriteBatch inputs and both varyings, verbatim");
        assertTrue(source.contains("gl_Position = u_projTrans * a_position;"));
    }

    @Test
    void theMultiTexturePassRestoresTheActiveUnitBeforeTheBatchDraws() throws IOException {
        // SpriteBatch binds the drawn texture to the ACTIVE unit at flush. A second-unit bind that
        // is not followed by a restore silently swaps the composite's inputs -- scene becomes bloom,
        // bloom becomes scene -- and every in-run frame goes near-black. This is the pin for the
        // failure the first E1 emulator run measured (luma 0.2 against a floor of 30).
        String source = Files.readString(RENDERER);
        int secondUnit = source.indexOf(".bind(1);");
        int restore = source.indexOf("glActiveTexture(GL20.GL_TEXTURE0);");
        assertTrue(secondUnit >= 0, "the composite binds the bloom texture to unit 1");
        assertTrue(restore > secondUnit,
            "and restores unit 0 as the active texture before the batch draw");
        int draw = source.indexOf("batch.draw(", restore);
        assertTrue(draw > restore, "the restore happens before the composite's draw call");
    }

    @Test
    void theChainStaysRestrainedAndItsBuffersStaySane() {
        assertTrue(PostProcessRenderer.BLOOM_THRESHOLD > 0.5f
                && PostProcessRenderer.BLOOM_THRESHOLD < 1f,
            "only light bleeds: the threshold sits above the dark arena art");
        assertTrue(PostProcessRenderer.BLOOM_INTENSITY <= 0.5f, "a lens effect, not a fog");
        assertTrue(PostProcessRenderer.VIGNETTE_STRENGTH <= 0.5f, "corners darken, never blacken");
        assertEquals(540, PostProcessRenderer.halfExtent(1080));
        assertEquals(1, PostProcessRenderer.halfExtent(1), "never a zero-extent buffer");
        assertEquals(1, PostProcessRenderer.halfExtent(0), "and never a negative one");
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
