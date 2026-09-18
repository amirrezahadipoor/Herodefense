package com.amirrezahadipoor.herodefense.render;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Finds every `Gdx.files.internal("literal")` path in the main sources, so a reference to a file that
 * does not ship can be detected before it crashes a device.
 *
 * <p>Why: {@code PostProcessRenderer} shipped for months loading {@code shaders/post-process.vert} and
 * {@code shaders/post-process.frag}, neither of which existed in {@code android/assets}; the class was
 * dead, so nothing ever noticed. A packaging check ("every internal path exists") is cheap and turns that
 * class of mistake into a build failure.
 *
 * <p>Only literal arguments are checked, and comments and string literals are stripped first so that an
 * example in a javadoc block is not reported as a reference. Concatenated paths cannot be resolved
 * statically, so they are counted and reported instead of silently ignored (roadmap R2.1/R2.5).
 */
public final class InternalAssetReferences {

    private static final Pattern LITERAL =
        Pattern.compile("Gdx\\.files\\.internal\\(\\s*\"([^\"]+)\"\\s*\\)");
    private static final Pattern ANY_CALL = Pattern.compile("Gdx\\.files\\.internal\\(");

    private InternalAssetReferences() {
    }

    /** Distinct literal paths referenced by the Java sources under {@code root}. */
    public static List<String> literalPaths(Path root) {
        Set<String> paths = new TreeSet<>();
        for (Path file : javaFiles(root)) {
            String text = stripCommentsAndLiterals(read(file));
            Matcher matcher = LITERAL.matcher(text);
            while (matcher.find()) {
                paths.add(matcher.group(1));
            }
        }
        return new ArrayList<>(paths);
    }

    /** How many internal-loading calls take a computed path and therefore cannot be checked here. */
    public static int dynamicCallCount(Path root) {
        int calls = 0;
        int literals = 0;
        for (Path file : javaFiles(root)) {
            String text = stripCommentsAndLiterals(read(file));
            calls += count(ANY_CALL, text);
            literals += count(LITERAL, text);
        }
        return calls - literals;
    }

    /** One message per referenced path that {@code exists} rejects. */
    public static List<String> problems(List<String> literals, Predicate<String> exists) {
        List<String> problems = new ArrayList<>();
        for (String literal : literals) {
            if (!exists.test(literal)) {
                problems.add("no file at android/assets/" + literal + " for Gdx.files.internal(\""
                    + literal + "\")");
            }
        }
        return problems;
    }

    /**
     * Removes block comments, line comments and javadoc examples, keeping string literal contents that
     * belong to real code. Without this, the pattern would match its own documentation.
     */
    static String stripCommentsAndLiterals(String source) {
        StringBuilder out = new StringBuilder(source.length());
        int index = 0;
        while (index < source.length()) {
            char current = source.charAt(index);
            if (current == '/' && index + 1 < source.length() && source.charAt(index + 1) == '*') {
                int end = source.indexOf("*/", index + 2);
                index = end < 0 ? source.length() : end + 2;
                continue;
            }
            if (current == '/' && index + 1 < source.length() && source.charAt(index + 1) == '/') {
                int end = source.indexOf('\n', index);
                index = end < 0 ? source.length() : end;
                continue;
            }
            out.append(current);
            index++;
        }
        return out.toString();
    }

    private static int count(Pattern pattern, String text) {
        int found = 0;
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            found++;
        }
        return found;
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
