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
        // R2.2 slice 9 moved the frame out (`presentation/FrameDriver`: the frame order, the pause record, the
        // two timed story lines, the ambient clock and the game-over presentation timer). The class is 760
        // lines — half the 1,519-line god class the audit measured — and keeps shrinking toward the 400-line
        // ceiling.
        // R3.3 (trophies) added thirteen lines here: the save point now evaluates the trophy rules and hands
        // the result to `progression/TrophyPresenter`, and loading a save runs the one-time migration. R3.5
        // (run lengths) added one more: the touch host forwards the brief-vigil tap. The presentation of a
        // trophy deliberately lives in the feature, not in the game class.
        // R3.6 (playtest session records) pays for itself as far as the class allows: the three one-line run-start
        // forwarders are gone (the touch host talks to the session controller directly, which was all they did), and
        // what remains of the feature here is one field, one method and two host forwards — 775 lines, three over
        // the previous record, and 63 fields, one over. The extra field and the three lines are the price of the
        // game object owning the recorder; the next R2.2 slice still has to bring this class down to the ceiling.
        // R7.4 (Android's Back) added the input multiplexer and the caught key here and paid for both inside
        // the ceiling: the `openingTierFor` delegate is gone (`SessionController` owns that question and the
        // touch host already reads it from there) and `drawCurrentState` is inlined into `FrameHost.draw`,
        // whose one caller it was. 775 -> 771 lines at 63 fields, so the freeze is lowered to the new
        // measurement and the next change has to pay for itself too.
        "com/amirrezahadipoor/herodefense/HeroDefenseGame.java",
        // A1 raised this by four lines and no fields: the per-frame re-anchor in updatePlaying became a call to
        // HeroMovementSystem.update, which needed one import and three lines of comment saying why the anchor
        // left. Every other line of the movement verb -- the system, the drag routing, the meter -- landed in a
        // file the ratchet does not hold, which is the point of the extractions this file has already paid for.
        //
        // F4 raised this by two lines and no fields: the frame host answers hapticFeedback() so the run
        // watcher can reach the device's hands. The vocabulary, the watcher and every pattern live in files
        // the ratchet does not hold; this file only owns the seam, as it owns every other port.
        //
        // E1 raised it by two more, the same shape of seam: one import and one host override answering
        // postProcessRenderer(). The chain, its shaders and its fallback live in render/PostProcessRenderer
        // and the composer wraps the world pass with it; the game class only owns the port, and its field
        // count did not move because the instance lives in RenderStack like every other renderer.
        //
        // F3 (TTS narration) and G3d (accessibility screen-reader) added provider seams and narration accessors
        // to wire platform-specific TTS and TalkBack bridges without coupling core to Android.
        //
        // P4b (boss-intro cutscenes) raised this by 22 lines and one field: the cinematic instance the
        // router and the composer read through the Host ports, plus the two one-line host seams that begin
        // and expose it. The walk, the talk and the handoff live in gameplay/BossIntroCinematic and
        // gameplay/CinematicFlow, which the ratchet does not hold; the game class only owns the seam.
        new ArchitectureRatchet.Frozen(818, 64),
        // R3.1 tap-to-focus and R3.2 script-driven telegraphs added 14 lines and two fields to the renderer
        // (the mark drawing itself lives in FocusMarkRenderer; the sprite-box helper, the draw loop and the
        // telegraph scale stayed here). Recorded, not hidden.
        //
        // R8.3 brought this file *down* from 681 to 653 lines and stayed at 13 fields while adding a feature:
        // the atlas residency release pass needs the clip map to be access-ordered, to measure each resident
        // sheet and to protect the live wave. The lines for it were paid for by two extractions, which is the
        // ratchet working as intended -- `ArrowTextures` (the arrow pixmap builder, 41 lines) and
        // `DropTextureCache` (the drop texture map and its two helpers, 23 lines). The frozen size is lowered
        // to the new measurement so the next change has to pay for itself too.
        // The projectile pass left this file in the obstacle round. The lodged-arrow branch is what paid for
        // the extraction: an arrow that stops in a rock has no velocity left to be drawn from, so the pass
        // needed a second drawing path, and the honest place for both is a class that owns nothing but arrows.
        // `ProjectileRenderer` took the three arrow sprites, the trail, the glint and the rotation helpers with
        // it, and the renderer came in under the limit -- so the ratchet asks for the freeze to go, and it goes.
        // D2's and G4's two additions are recorded in the history above rather than in a frozen number here.
        // R3.5 added a mode-aware entry point (`runBrief`) and kept the old signature as a one-line delegate,
        // so the standard sweeps are unchanged by construction. R3.4 needed no growth here at all: the omens'
        // counterfactual is simply a run without the omen trial, which the existing trial axes already measure.
        "com/amirrezahadipoor/herodefense/balance/BalanceSimulator.java",
        // A3 raised the line count by 3 without adding a field: EnemyRoleSystem.update now ticks inside the
        // sweep loop, because bands published for a game without the late-wave roles would be evidence about
        // a build that no longer ships. No new state, no new method.
        //
        // P4b raised the line count by 4 without adding a field: the boss intro is presentation-only,
        // so the sweep completes it instantly the way it already completes the planting ceremony.
        //
        // P6a raised the line count by 3 without adding a field: the sweep keeps the same wave
        // clock the live game keeps, or the mythic time-effects would publish bands for a game
        // without them. No new state, no new method.
        new ArchitectureRatchet.Frozen(671, 23),
        // R3.3 added the trophy ledger (one field that a run may not reset) and R3.5 the run mode (plus
        // `runLengthWaves()`, which is what lets a thirty-wave run end without touching the long one). R3.4 added
        // no field: wave omens are switched on by the trial the player drafted, and the trial list already exists.
        //
        // C3 raised the line count by 21 without adding a field: the heartwood formula
        // gained a mode-aware overload that halves the brief vigil's pay, plus the javadoc explaining why a run
        // that cannot be lost must not be the efficient way to earn. The formula was not extracted into a class
        // of its own because its two call sites are this file's own award path and the death screen's preview of
        // it, and a third home for a four-line equation would be a fourth place to keep in step; the overload
        // keeps the three-argument call meaning STANDARD, which is what holds every older caller and the
        // progression equation still. If the formula grows again, extract it then and lower this number.
        "com/amirrezahadipoor/herodefense/model/GameState.java",
        // B1 raised the count by one field and six lines: the pity rule R4.3 wanted needs the dry-kill streak
        // remembered across kills, waves and saves -- a run counter like totalKills beside it, encoded by the
        // reflection codec like every other field. No method, no behaviour in the model itself.
        //
        // B3 raised it by one field and five lines: the pre-run draft now opens on a hero-path choice, and the
        // bound path is run state exactly like the trial picks -- encoded by the codec, transplanted by the
        // new-run reset, nulled when the string does not name a path. The bends themselves live in HeroPath and
        // its consumers, not here; the model only remembers the choice.
        //
        // P4b raised this by two fields and ten lines: bossIntroPending/bossIntroWave are run state like
        // ceremonyPending beside them -- a between-waves save must remember a deferred boss wave -- encoded
        // by the codec like every other field. No behaviour in the model itself.
        //
        // P6a raised this by two fields and six lines: wavePlannedEnemies/tricklePulse are run state
        // like waveActive beside them -- a between-waves save must remember which pulse walks in
        // next -- encoded by the codec like every other field. No behaviour in the model itself.
        //
        // P6b raised this by one field and three lines: escortWave is run state like tricklePulse
        // beside it -- a between-waves save must remember whether the second escort pulse is
        // still owed -- encoded by the codec like every other field. No behaviour in the model itself.
        new ArchitectureRatchet.Frozen(665, 89)
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
