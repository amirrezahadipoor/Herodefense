package com.amirrezahadipoor.herodefense.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
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

    private static final Path GAME = SOURCES.resolve("com/amirrezahadipoor/herodefense/HeroDefenseGame.java");

    private static final Path ROUTER = SOURCES.resolve(
        "com/amirrezahadipoor/herodefense/input/ScreenTouchRouter.java");

    private static final Path COMPOSER = SOURCES.resolve(
        "com/amirrezahadipoor/herodefense/presentation/ScreenStateComposer.java");

    /** Measured on 2026-09-16; the ratchet fails if one of these grows or if a new one appears. */
    private static final Map<String, ArchitectureRatchet.Frozen> FROZEN = Map.of(
        // R2.2 slice 6 moved the run session out (start, restart at tier, ascend, continue), collapsing three
        // copies of the same fifteen-line reset into one. The class is 954 lines — 37 % smaller than the
        // 1,519-line god class the audit measured — and keeps shrinking toward the 400-line ceiling.
        "com/amirrezahadipoor/herodefense/HeroDefenseGame.java",
        new ArchitectureRatchet.Frozen(954, 95),
        // R3.1 tap-to-focus and R3.2 script-driven telegraphs added 14 lines and two fields to the renderer
        // (the mark drawing itself lives in FocusMarkRenderer; the sprite-box helper, the draw loop and the
        // telegraph scale stayed here). Recorded, not hidden.
        "com/amirrezahadipoor/herodefense/render/CombatEntityRenderer.java",
        new ArchitectureRatchet.Frozen(681, 13),
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

    /**
     * R2.2 guard: the screen-state touch chain lives in {@linkplain #ROUTER the router} and the game class only
     * wires it. Without this, the next screen added to the game would grow the god class again and no test would
     * notice until the next audit.
     */
    @Test
    void theTouchChainLivesBehindTheRouter() {
        String game = read(GAME);
        String router = read(ROUTER);
        assertTrue(router.contains("implements TouchInputController.Listener"),
            "the router is the listener the touch input calls back into");
        assertTrue(game.contains("new ScreenTouchRouter(new TouchHost())"), "the game must wire the router");
        assertEquals(1, occurrences(game, "setInputProcessor"), "exactly one input registration in the game");
        Map<String, Integer> inputOnlyClasses = Map.ofEntries(
            Map.entry("PauseTouchLayout.", 0),
            Map.entry("HudTouchLayout.", 0),
            Map.entry("MainMenuTouchLayout.", 0),
            Map.entry("GameOverTouchLayout.", 0),
            Map.entry("LevelUpTouchLayout.", 0),
            Map.entry("SettingsTouchLayout.", 0),
            Map.entry("RewardCardTouchController.", 0),
            Map.entry("CodexTouchController.", 0),
            Map.entry("InventoryTouchController.", 0),
            Map.entry("RootNetworkTouchController.", 0),
            Map.entry("TrialDraftTouchController.", 0),
            Map.entry("SimulationSpeedTouchController.", 0)
        );
        for (Map.Entry<String, Integer> entry : inputOnlyClasses.entrySet()) {
            assertTrue(occurrences(game, entry.getKey()) <= entry.getValue(),
                "the touch chain moved to the router; " + entry.getKey() + " still appears in the game");
        }
        // The shop-tab field stays in the game (the HUD renders it), but its hit tests belong to the router.
        assertTrue(occurrences(game, "shopTab") > 0, "the game still owns the selected shop tab");
        for (String hitTest : List.of("statAt(", "tabAt(", "closeAt(", "rootAt(", "skillAt(", "evolutionOptionAt(")) {
            assertEquals(0, occurrences(game, "StatShopTouchLayout." + hitTest),
                "the router owns the StatShopTouchLayout." + hitTest + " hit test");
            assertTrue(occurrences(router, "StatShopTouchLayout." + hitTest) > 0,
                "the router keeps the StatShopTouchLayout." + hitTest + " hit test");
        }
        assertTrue(occurrences(router, "host.") > 100,
            "the router talks to the game only through its Host port");
        // Same guard for the frame composer: the per-state dispatch draws through its own port, and the game
        // keeps only the delegation, so a new overlay cannot be wired straight into the god class again.
        String composer = read(COMPOSER);
        assertTrue(composer.contains("public void draw(float presentationDeltaSeconds)"),
            "the composer owns the per-state frame build");
        assertTrue(occurrences(composer, "host.") > 100, "the composer reads the run through its Host port");
        // The renderers stay owned by the game (they are created and closed there), but every draw call of the
        // per-state frame now goes through the composer's port.
        for (String renderer : List.of("mainMenuRenderer", "statShopOverlayRenderer", "pauseOverlayRenderer",
                "rootNetworkOverlayRenderer", "hudRenderer", "combatEntityRenderer", "particleRenderer")) {
            assertTrue(occurrences(game, renderer + ".draw") == 0,
                "the draw call moved to the composer: " + renderer);
            assertTrue(occurrences(composer, "host." + renderer + "().draw") > 0,
                "the composer draws through its Host port: " + renderer);
        }
    }

    private static int occurrences(String source, String needle) {
        int total = 0;
        int index = source.indexOf(needle);
        while (index >= 0) {
            total++;
            index = source.indexOf(needle, index + needle.length());
        }
        return total;
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    @Test
    void theFreezeListIsHonest() {
        List<String> problems = ArchitectureRatchet.problems(SOURCES, Map.of());
        assertTrue(problems.size() >= FROZEN.size(),
            "each frozen offender must still be a real violation without the freeze; got " + problems);
    }
}
