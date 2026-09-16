package com.amirrezahadipoor.herodefense.integrity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Fails the build when a path that ends a run forgets to write its session record (roadmap R3.6).
 *
 * <p>A run can end in more than one place, and a playtest protocol is only honest if every one of them leaves the
 * numbers behind. The first draft of this feature wired the death path and missed the completion path — both modes
 * end on a boss wave, so a finished run leaves through the reward card in the touch router, not through the wave
 * director. That is exactly the kind of gap that reappears the next time a screen is added, so it is a test now:
 * every {@code transitionTo(GameScreenState.GAME_OVER)} in the main sources has to be followed by a
 * {@code recordRunEnd()} call within the same block, and a deliberate exception has to say why on the line itself.
 *
 * <p>The test reads source text, because the thing it guards is the shape of the code that ends a run. It is
 * deliberately loose about spacing and strict about presence.
 */
final class SessionRecordIntegrityTest {

    /** Test working directory is the {@code core} module, so the main sources sit one directory down. */
    private static final Path MAIN_SOURCES = Path.of("src", "main", "java").normalize();
    private static final Pattern ENDS_A_RUN = Pattern.compile("transitionTo\\(GameScreenState\\.GAME_OVER\\)");
    private static final Pattern RECORDS_THE_SESSION = Pattern.compile("recordRunEnd\\(\\)");
    private static final int LOOKAHEAD_LINES = 4;

    @Test
    void everyGameOverTransitionWritesTheSessionRecord() throws IOException {
        List<Path> sources = new ArrayList<>();
        try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(sources::add);
        }
        assertFalse(sources.isEmpty(), "the main sources must be readable from the test working directory");

        List<String> missing = new ArrayList<>();
        int endings = 0;
        for (Path source : sources) {
            List<String> lines = Files.readAllLines(source);
            for (int index = 0; index < lines.size(); index++) {
                String line = lines.get(index);
                if (!ENDS_A_RUN.matcher(line).find() || line.contains("integrity-exempt:")) {
                    continue;
                }
                endings++;
                boolean recorded = false;
                for (int offset = 0; offset <= LOOKAHEAD_LINES && index + offset < lines.size(); offset++) {
                    if (RECORDS_THE_SESSION.matcher(lines.get(index + offset)).find()) {
                        recorded = true;
                        break;
                    }
                }
                if (!recorded) {
                    missing.add(source.getFileName() + ":" + (index + 1) + " -> " + line.trim());
                }
            }
        }

        assertTrue(endings >= 3, "expected the run-ending paths to be found, saw " + endings
            + " — if the call sites moved, this guard needs to move with them");
        assertTrue(missing.isEmpty(),
            "a run that ends must write its session record (roadmap R3.6); these do not:\n  "
                + String.join("\n  ", missing));
    }

    /** The guard is worthless if it cannot see the call it looks for, so it is checked against known-good text. */
    @Test
    void theGuardRecognisesBothTheCallAndItsAbsence() {
        assertTrue(ENDS_A_RUN.matcher("            host.transitionTo(GameScreenState.GAME_OVER);").find());
        assertTrue(RECORDS_THE_SESSION.matcher("            host.recordRunEnd();").find());
        assertFalse(RECORDS_THE_SESSION.matcher("            host.saveNow();").find());
    }
}
