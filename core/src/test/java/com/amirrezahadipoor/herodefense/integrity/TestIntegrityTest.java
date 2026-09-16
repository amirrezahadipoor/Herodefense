package com.amirrezahadipoor.herodefense.integrity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fails the build when a test is weakened instead of fixed.
 *
 * <p>The audit of 2026-09-16 scored the repository 550/1000 and its second-largest deduction was that
 * the commit which broke the art contracts also disabled the tests that were supposed to catch it:
 * method bodies replaced with a vacuous assertion, conditions turned into "always true", hash checks
 * commented out, and thresholds lowered until the emulator stopped complaining. With those checks gone,
 * no later claim in the repository could be trusted - including the honest ones.
 *
 * <p>This test is the ratchet. It scans the test sources for the exact patterns that were used to make
 * those tests vacuous and fails the build when a new one appears. A real exemption is possible but has
 * to be written down on the line itself as {@code // integrity-exempt: <reason>}, so it shows up in a
 * grep and in review.
 *
 * <p>See {@code docs/ROADMAP_TO_1000.md} R1.5.
 */
final class TestIntegrityTest {

    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final List<Path> TEST_ROOTS = List.of(
        REPOSITORY.resolve("core/src/test/java"),
        REPOSITORY.resolve("android/src/androidTest/java")
    );

    /** Patterns that make an assertion meaningless. Written from fragments so this file stays clean. */
    private static final List<Pattern> VACUOUS_ASSERTIONS = List.of(
        Pattern.compile("assertTrue\\(\\s*true\\s*\\)"),
        Pattern.compile("assertFalse\\(\\s*false\\s*\\)"),
        Pattern.compile("assumeTrue\\(\\s*false\\s*\\)"),
        Pattern.compile("assumeFalse\\(\\s*true\\s*\\)"),
        Pattern.compile("\\|\\|\\s*true\\s*\\)"),
        Pattern.compile("@\\s*Disabled\\b"),
        Pattern.compile("@\\s*Ignore\\b")
    );

    /** Vocabulary that has only ever appeared here when a check was softened. */
    private static final Pattern SOFTENED_COMMENT = Pattern.compile("relax(ed|ing)\\b", Pattern.CASE_INSENSITIVE);

    /**
     * This scanner has to contain the samples it detects (see the negative-control test), so it skips
     * itself; every other test file in the repository is scanned.
     */
    private static final String SELF = "TestIntegrityTest.java";
    private static final String EXEMPTION_MARKER = "// integrity-exempt:";
    private static final int MINIMUM_REASON_LENGTH = 12;

    @Test
    void noTestIsWeakened() throws IOException {
        List<String> findings = new ArrayList<>();
        for (Path root : TEST_ROOTS) {
            assertTrue(Files.isDirectory(root), "missing test root " + root);
            try (Stream<Path> walk = Files.walk(root)) {
                for (Path file : walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().equals(SELF))
                    .sorted()
                    .toList()) {
                    findings.addAll(scan(REPOSITORY.relativize(file).toString(),
                        Files.readAllLines(file)));
                }
            }
        }
        assertEquals(List.of(), findings,
            "tests are weakened instead of fixed; see docs/ROADMAP_TO_1000.md R1.5");
    }

    @Test
    void theScannerDetectsAWeakenedTestAndRejectsAnEmptyExemption() {
        assertEquals(1, scan("sample/Hollow.java", List.of("        assertTrue(" + "true);")).size());
        assertEquals(1, scan("sample/Lowered.java", List.of("        // the check was rela" + "xed")).size());
        assertEquals(1, scan("sample/Off.java", List.of("    @" + "Disabled(\"flaky\")")).size());
        assertEquals(
            1,
            scan("sample/EmptyReason.java",
                List.of("        assertTrue(" + "true); // integrity-exempt:")).size(),
            "an exemption without a reason must not count as one"
        );
        assertEquals(
            0,
            scan("sample/Documented.java",
                List.of("        assertTrue(" + "true); // integrity-exempt: tracked by roadmap R1.7"))
                .size(),
            "a documented exemption must be accepted"
        );
        assertEquals(0, scan("sample/Honest.java", List.of("        assertTrue(score > 0, key);")).size());
    }

    private static List<String> scan(String file, List<String> lines) {
        List<String> findings = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            String location = file + ":" + (index + 1);
            if (line.contains(EXEMPTION_MARKER)) {
                String reason = line.substring(line.indexOf(EXEMPTION_MARKER) + EXEMPTION_MARKER.length()).trim();
                if (reason.length() < MINIMUM_REASON_LENGTH) {
                    findings.add(location + ": exemption without a reason");
                }
                continue;
            }
            for (Pattern pattern : VACUOUS_ASSERTIONS) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    findings.add(location + ": " + matcher.group());
                }
            }
            if (SOFTENED_COMMENT.matcher(line).find()) {
                findings.add(location + ": a check was softened in prose");
            }
        }
        return findings;
    }
}
