package com.amirrezahadipoor.herodefense.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The architecture rules of {@link ArchitectureRatchet}, checked on the real sources and proven to fail on
 * synthetic ones.
 *
 * <p>The freeze list is the honest part: three classes and one state holder were already oversize when the
 * rule was introduced. They are recorded at their measured size, may only shrink, and an entry must be
 * deleted once the class is inside the limits, which the ratchet itself verifies. See roadmap R2.4/R2.2.
 */
class ArchitectureRatchetTest {

    private static final Path SOURCES = Path.of("..", "core", "src", "main", "java").normalize();

    /** Measured on 2026-09-16; the ratchet fails if one of these grows or if a new one appears. */
    private static final Map<String, ArchitectureRatchet.Frozen> FROZEN = Map.of(
        "com/amirrezahadipoor/herodefense/HeroDefenseGame.java",
        new ArchitectureRatchet.Frozen(1447, 91),
        "com/amirrezahadipoor/herodefense/render/CombatEntityRenderer.java",
        new ArchitectureRatchet.Frozen(667, 11),
        "com/amirrezahadipoor/herodefense/balance/BalanceSimulator.java",
        new ArchitectureRatchet.Frozen(639, 23),
        "com/amirrezahadipoor/herodefense/model/GameState.java",
        new ArchitectureRatchet.Frozen(592, 80)
    );

    @Test
    void theShippedSourcesRespectTheRatchet() {
        assertTrue(java.nio.file.Files.isDirectory(SOURCES), "missing sources at " + SOURCES.toAbsolutePath());
        List<String> problems = ArchitectureRatchet.problems(SOURCES, FROZEN);
        assertTrue(problems.isEmpty(), "architecture ratchet violations: " + problems);
    }

    @Test
    void aRenderingImportInsideTheSimulationLayersIsRejected() {
        List<String> problems = ArchitectureRatchet.forbiddenImports("com/x/model/GameState.java",
            "import com.amirrezahadipoor.herodefense.render.HudRenderer;");
        assertEquals(1, problems.size(), problems.toString());
        assertTrue(problems.get(0).contains("imports a rendering type"), problems.toString());
        assertTrue(ArchitectureRatchet.forbiddenImports("com/x/model/GameState.java", "import java.util.List;")
            .isEmpty());
        assertTrue(ArchitectureRatchet.forbiddenImports("com/x/render/HudRenderer.java",
            "import com.badlogic.gdx.graphics.Texture;").isEmpty(),
            "rendering layers are allowed to use graphics types");
    }

    @Test
    void anOversizeClassIsRejectedAndAFrozenOneMayNotGrow() {
        String tooManyLines = "class Big {\n" + "    void call() {}\n".repeat(700) + "}\n";
        List<String> lineProblem = ArchitectureRatchet.size("com/x/Big.java", tooManyLines, null);
        assertEquals(1, lineProblem.size(), lineProblem.toString());
        assertTrue(lineProblem.get(0).contains("lines, over the limit"), lineProblem.toString());

        String fields = "class Wide {\n" + "    private int value;\n".repeat(45) + "}\n";
        List<String> fieldProblem = ArchitectureRatchet.size("com/x/Wide.java", fields, null);
        assertEquals(1, fieldProblem.size(), fieldProblem.toString());
        assertTrue(fieldProblem.get(0).contains("instance fields, over the limit"), fieldProblem.toString());

        String frozenTooSmall = "class Wide {\n" + "    private int value;\n".repeat(41) + "}\n";
        assertTrue(ArchitectureRatchet
            .size("com/x/Wide.java", frozenTooSmall, new ArchitectureRatchet.Frozen(45, 41)).isEmpty(),
            "a frozen offender at its recorded size passes");
        assertTrue(ArchitectureRatchet
            .size("com/x/Wide.java", fields, new ArchitectureRatchet.Frozen(45, 41)).size() == 1,
            "a frozen offender that grows fails");
    }

    @Test
    void theFreezeListIsHonest() {
        List<String> problems = ArchitectureRatchet.problems(SOURCES, Map.of());
        assertTrue(problems.size() >= FROZEN.size(),
            "each frozen offender must still be a real violation without the freeze; got " + problems);
    }
}
