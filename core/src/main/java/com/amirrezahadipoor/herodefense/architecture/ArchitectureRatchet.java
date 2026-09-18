package com.amirrezahadipoor.herodefense.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Mechanical architecture rules, so the shape of the codebase is enforced by a test instead of by review.
 *
 * <p>The audit of 2026-09-16 deducted architecture points for a 1,519-line / 90-field god class and for a
 * "no unit test on the riskiest file" problem that grows every time the class does. Rules that only live in
 * prose are broken again; these are checked by {@code ArchitectureRatchetTest} on every build.
 *
 * <p>Rules:
 * <ol>
 *   <li>Files under {@code model/} and {@code balance/} must not import rendering or graphics types, so the
 *       simulation stays testable without a GL context.</li>
 *   <li>No class may exceed {@link #MAX_LINES} lines or {@link #MAX_FIELDS} instance fields, except for
 *       offenders that are frozen at their measured size. A frozen offender may shrink; if it grows, the
 *       build fails, and when it drops below the limit its entry must be deleted.</li>
 * </ol>
 *
 * <p>See {@code docs/ROADMAP_TO_1000.md} R2.4.
 */
public final class ArchitectureRatchet {

    public static final int MAX_LINES = 600;
    public static final int MAX_FIELDS = 40;

    private static final Pattern FIELD = Pattern.compile(
        "^\\s{4}(?:private|protected|public)\\s+(?!static\\s+final\\s+[A-Z0-9_]+(?:\\s|;|=))"
            + "[\\w<>\\[\\],\\.\\?\\s]+\\s+(\\w+)\\s*(?:=[^;]*)?;\\s*$",
        Pattern.MULTILINE);
    private static final Pattern LAYER_IMPORT = Pattern.compile(
        "^import\\s+(?:com\\.amirrezahadipoor\\.herodefense\\.(?:render|graphics)\\.|com\\.badlogic\\.gdx\\.graphics)",
        Pattern.MULTILINE);

    private ArchitectureRatchet() {
    }

    /** A frozen exception: how large the offender was when it was allowed. */
    public record Frozen(int lines, int fields) {
    }

    public static List<String> problems(Path sourceRoot, Map<String, Frozen> frozen) {
        List<String> problems = new ArrayList<>();
        for (Path file : javaFiles(sourceRoot)) {
            String relative = sourceRoot.relativize(file).toString().replace('\\', '/');
            String source = read(file);
            problems.addAll(forbiddenImports(relative, source));
            problems.addAll(size(relative, source, frozen.get(relative)));
        }
        for (Map.Entry<String, Frozen> frozenEntry : frozen.entrySet()) {
            String entry = frozenEntry.getKey();
            Path file = sourceRoot.resolve(entry);
            if (!Files.isRegularFile(file)) {
                problems.add("frozen offender " + entry + " no longer exists; remove it from the freeze list");
                continue;
            }
            String source = read(file);
            Frozen allowed = frozenEntry.getValue();
            if (countLines(source) <= MAX_LINES && countFields(source) <= MAX_FIELDS) {
                problems.add("frozen offender " + entry + " is inside the limits now; remove it from the "
                    + "freeze list");
            } else if (allowed == null) {
                problems.add("frozen offender " + entry + " has no measured size recorded");
            }
        }
        return problems;
    }

    /** Import rule for the layers that must stay free of rendering types. */
    public static List<String> forbiddenImports(String relativePath, String source) {
        List<String> problems = new ArrayList<>();
        if (!relativePath.contains("/model/") && !relativePath.contains("/balance/")) {
            return problems;
        }
        Matcher matcher = LAYER_IMPORT.matcher(source);
        while (matcher.find()) {
            problems.add(relativePath + " imports a rendering type: " + matcher.group().trim());
        }
        return problems;
    }

    /** Size rule for one file, with its frozen exception when there is one. */
    public static List<String> size(String relativePath, String source, Frozen frozen) {
        List<String> problems = new ArrayList<>();
        int lines = countLines(source);
        int fields = countFields(source);
        int allowedLines = frozen == null ? MAX_LINES : Math.max(MAX_LINES, frozen.lines());
        int allowedFields = frozen == null ? MAX_FIELDS : Math.max(MAX_FIELDS, frozen.fields());
        if (lines > allowedLines) {
            problems.add(relativePath + " has " + lines + " lines, over the limit of " + allowedLines);
        }
        if (fields > allowedFields) {
            problems.add(relativePath + " has " + fields + " instance fields, over the limit of "
                + allowedFields);
        }
        return problems;
    }

    /** Physical lines of the file; a trailing newline does not add a line. */
    public static int countLines(String source) {
        if (source.isEmpty()) {
            return 0;
        }
        int segments = source.split("\n", -1).length;
        return source.endsWith("\n") ? segments - 1 : segments;
    }

    /** Counts instance-field declarations with a documented heuristic: one declaration per line, no parens. */
    public static int countFields(String source) {
        int total = 0;
        Matcher matcher = FIELD.matcher(source);
        while (matcher.find()) {
            total++;
        }
        return total;
    }

    private static List<Path> javaFiles(Path root) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(root)) {
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
}
