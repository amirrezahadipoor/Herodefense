package com.amirrezahadipoor.herodefense.render;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every `Gdx.files.internal("literal")` in the shipped sources must point at a file that is actually
 * packaged under {@code android/assets}.
 *
 * <p>The audit found a dead class loading two shader files that were never in the APK; nothing detected it
 * because nothing ever called the class. This test makes that mistake a build failure (roadmap R2.1).
 */
class InternalAssetReferenceTest {

    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path ASSETS = REPOSITORY.resolve("android/assets").normalize();
    private static final List<Path> MAIN_SOURCES = List.of(
        REPOSITORY.resolve("core/src/main/java"),
        REPOSITORY.resolve("android/src/main/java")
    );

    @Test
    void everyLiteralInternalPathShipsInTheApkAssets() {
        List<String> literals = new ArrayList<>();
        int dynamic = 0;
        for (Path root : MAIN_SOURCES) {
            literals.addAll(InternalAssetReferences.literalPaths(root));
            dynamic += InternalAssetReferences.dynamicCallCount(root);
        }
        assertFalse(literals.isEmpty(), "the scan found no Gdx.files.internal literals, so it proves nothing");
        assertTrue(literals.contains("shaders/rarity-glow.frag"),
            "expected the rarity glow shaders to be part of the literal set: " + literals);
        List<String> problems = InternalAssetReferences.problems(
            literals, path -> Files.isRegularFile(ASSETS.resolve(path)));
        assertTrue(problems.isEmpty(), "asset references without a file: " + problems);
        System.out.println("internal asset references: " + literals.size()
            + " literal paths checked, " + dynamic + " computed path(s) not checkable statically");
    }

    @Test
    void aReferenceWithoutAFileIsRejected() {
        List<String> problems = InternalAssetReferences.problems(
            List.of("shaders/post-process.frag", "shaders/rarity-glow.frag"),
            path -> "shaders/rarity-glow.frag".equals(path));
        assertTrue(problems.stream().anyMatch(problem -> problem.contains("shaders/post-process.frag")),
            problems.toString());
        assertFalse(problems.stream().anyMatch(problem -> problem.contains("rarity-glow")), problems.toString());
    }

    @Test
    void theResurrectedPostProcessRendererIsWiredAndItsShadersShip() throws IOException {
        // The old guard here was a tombstone: a dead PostProcessRenderer once referenced two shaders
        // that never shipped, and the test pinned the class's absence. E1 resurrected the name for
        // real, so the tombstone becomes the property it was actually defending -- a post-process
        // renderer may exist only if something calls it and every asset it names ships. The literal
        // scan above proves the four shaders exist in android/assets; this proves the chain is not
        // dead code: the composer wraps the in-run frame with it and the game answers the port.
        Path renderer = REPOSITORY.resolve(
            "core/src/main/java/com/amirrezahadipoor/herodefense/render/PostProcessRenderer.java");
        assertTrue(Files.exists(renderer), "E1's chain lives here");
        Path composer = REPOSITORY.resolve(
            "core/src/main/java/com/amirrezahadipoor/herodefense/presentation/ScreenStateComposer.java");
        String composerSource = java.nio.file.Files.readString(composer);
        assertTrue(composerSource.contains("host.postProcessRenderer().beginScene("),
            "the composer sends the in-run world into the chain");
        assertTrue(composerSource.contains("host.postProcessRenderer().endSceneAndComposite()"),
            "and returns the processed frame to the screen");
    }
}
