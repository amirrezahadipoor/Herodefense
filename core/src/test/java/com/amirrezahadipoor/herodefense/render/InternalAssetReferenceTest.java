package com.amirrezahadipoor.herodefense.render;

import org.junit.jupiter.api.Test;

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
            path -> path.equals("shaders/rarity-glow.frag"));
        assertTrue(problems.stream().anyMatch(problem -> problem.contains("shaders/post-process.frag")),
            problems.toString());
        assertFalse(problems.stream().anyMatch(problem -> problem.contains("rarity-glow")), problems.toString());
    }

    @Test
    void theDeadPostProcessRendererIsGone() {
        Path dead = REPOSITORY.resolve(
            "core/src/main/java/com/amirrezahadipoor/herodefense/render/PostProcessRenderer.java");
        assertFalse(Files.exists(dead),
            "the dead post-process stub is back; it referenced two shaders that never shipped");
    }
}
