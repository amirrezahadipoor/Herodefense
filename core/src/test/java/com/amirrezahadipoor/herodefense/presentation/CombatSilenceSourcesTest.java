package com.amirrezahadipoor.herodefense.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * The mid-fight silence ratchet at the source level (MEMORY P5): the boss-fight dialogue
 * machinery stays deleted, and the boss entrance stays wordless. Behaviour is pinned in
 * {@link CombatSilenceTest}; this fails the moment anyone resurrects the machinery or teaches
 * the entrance a line.
 */
final class CombatSilenceSourcesTest {

    private static final Path SOURCES = Path
        .of("..", "core", "src", "main", "java", "com", "amirrezahadipoor", "herodefense")
        .normalize();

    /** Dead since P3a: the mid-fight boss lines, their keys, and their half-beat presenter. */
    private static final List<String> DELETED_SYMBOLS =
        List.of("BossBeat", "HOLLOW_BOSS", "halfBeat", "HalfBeat", "HALF_BEAT", "half_beat");

    /** Word-reaching calls the boss entrance must never grow. */
    private static final List<String> ENTRANCE_WORDS =
        List.of("showBeat", "showStoryBeat", "dialogue", "speak", "narrat", "announce", "beats.");

    @Test
    void theBossFightDialogueMachineryStaysDeleted() {
        assertTrue(Files.isDirectory(SOURCES), "missing sources at " + SOURCES.toAbsolutePath());
        List<String> resurrected = new ArrayList<>();
        for (Path file : javaFiles()) {
            String source = read(file);
            for (String symbol : DELETED_SYMBOLS) {
                if (source.contains(symbol)) {
                    resurrected.add(SOURCES.relativize(file) + " mentions " + symbol);
                }
            }
        }
        assertTrue(resurrected.isEmpty(),
            "boss-fight dialogue machinery is back in the sources: " + resurrected);
    }

    @Test
    void theBossEntranceClaimsAndFlashesButNeverSpeaks() {
        String source = read(SOURCES.resolve("presentation/RunPresentationSystem.java"));
        String body = methodBody(source, "public void presentBossEntrance(GameState state)");
        for (String word : ENTRANCE_WORDS) {
            assertFalse(body.contains(word), "presentBossEntrance reaches for words: " + word);
        }
        assertTrue(body.contains("claimFirstUnencountered"),
            "the silent ledger claim is the entrance's whole point");
        assertTrue(body.contains("emitBossEntrance"), "the entrance flash stays");
    }

    private static List<Path> javaFiles() {
        try (Stream<Path> walk = Files.walk(SOURCES)) {
            return walk.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .sorted()
                .toList();
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    /** The brace-matched body of the method starting at {@code needle}; strings stepped over. */
    private static String methodBody(String source, String needle) {
        int start = source.indexOf(needle);
        assertTrue(start >= 0, "missing method: " + needle);
        int brace = source.indexOf('{', start + needle.length());
        assertTrue(brace >= 0, "missing body: " + needle);
        int depth = 0;
        int index = brace;
        while (index < source.length()) {
            char current = source.charAt(index);
            if (current == '"' || current == '\'') {
                char quote = current;
                index++;
                while (index < source.length() && source.charAt(index) != quote) {
                    index += source.charAt(index) == '\\' ? 2 : 1;
                }
            } else if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(brace, index + 1);
                }
            }
            index++;
        }
        throw new AssertionError("unbalanced body: " + needle);
    }
}
