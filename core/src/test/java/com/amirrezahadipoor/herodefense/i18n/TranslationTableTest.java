package com.amirrezahadipoor.herodefense.i18n;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every string table in this package, checked as a table rather than one entry at a time.
 *
 * <p>Roadmap R7.3 asks for "a test that fails on hard-coded or untranslated user-facing strings". The
 * untranslated half is mostly structural -- a table's constructor takes the text, so there is no text-less form
 * to forget -- but structure only holds if every screen's strings are *in* a table. So this class reads the
 * package's own sources, finds every type that implements {@link Translated}, and fails if one is not in the
 * list below. Adding a screen's table without adding it here is a failure, not a gap.
 *
 * <p>What is then checked of every entry: none is blank, none carries a non-English script, every placeholder is
 * positional and dense from 1, formatting with arguments never throws, and no entry is a duplicate of another in
 * the same table under a different name.
 */
class TranslationTableTest {

    private static final Path SOURCES =
        Path.of("..", "core", "src", "main", "java", "com", "amirrezahadipoor", "herodefense", "i18n").normalize();

    /**
     * The tables this test sweeps, which is {@link GameStrings#tables()} rather than a list of its own: one list
     * means a new screen's strings cannot be drawn but unswept. {@link #everyTableIsListed} is what keeps that
     * list complete.
     */
    private static final List<Translated[]> TABLES = GameStrings.tables();

    /** A format placeholder, {@code %1$s}: positional, so the order stays the pattern's business. */
    private static final Pattern PLACEHOLDER = Pattern.compile("%(\\d+)\\$[sd]");

    @Test
    void everyTableIsListed() {
        List<String> declared = new ArrayList<>();
        try (Stream<Path> files = Files.list(SOURCES)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                String source = read(path);
                if (source.contains("implements Translated")) {
                    Matcher matcher = Pattern.compile("enum\\s+(\\w+)\\s+implements Translated").matcher(source);
                    assertTrue(matcher.find(), path.getFileName() + " implements Translated but declares no enum");
                    declared.add(matcher.group(1));
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException("cannot list " + SOURCES.toAbsolutePath(), e);
        }
        List<String> swept = new ArrayList<>();
        for (Translated[] table : TABLES) {
            swept.add(table[0].getClass().getSimpleName());
        }
        assertEquals(declared.stream().sorted().toList(), swept.stream().sorted().toList(),
            "a string table exists in " + SOURCES + " that GameStrings.tables() does not list: add it there,"
                + " because a table nobody sweeps is a table that can ship a blank string");
    }

    @Test
    void noEntryIsBlank() {
        List<String> problems = new ArrayList<>();
        for (Translated entry : entries()) {
            if (isBlank(entry.english())) {
                problems.add(name(entry) + " has no text");
            }
        }
        assertTrue(problems.isEmpty(), () -> problems.size() + " blank entries:%n" + String.join("%n", problems));
    }

    @Test
    void noEntryCarriesANonEnglishScript() {
        // A Persian sentence pasted into the English column compiles, passes a blank check, and ships. So the
        // rule is about the characters: no value carries Arabic-script codepoints outside a placeholder, which
        // is stripped first because "%1$s" is syntax.
        List<String> problems = new ArrayList<>();
        for (Translated entry : entries()) {
            String text = stripPlaceholders(entry.english());
            if (text.codePoints().anyMatch(TranslationTableTest::isArabicScript)) {
                problems.add(name(entry) + " carries non-English script: " + entry.english());
            }
        }
        assertTrue(problems.isEmpty(),
            () -> problems.size() + " entries are not English:%n" + String.join("%n", problems));
    }

    @Test
    void everyPlaceholderIsPositionalAndDenseFromOne() {
        // A bare %s compiles and a %3$s with no %1$s formats, until the day the arguments shift and the render
        // thread throws. So every percent sign opens a positional placeholder, and the positions run 1..max.
        List<String> problems = new ArrayList<>();
        for (Translated entry : entries()) {
            String pattern = entry.english();
            if (stripPlaceholders(pattern).contains("%")) {
                problems.add(name(entry) + " has a non-positional percent sign: " + pattern);
                continue;
            }
            List<Integer> positions = placeholders(pattern).stream().map(Integer::parseInt).sorted().toList();
            for (int index = 0; index < positions.size(); index++) {
                if (positions.get(index) != index + 1) {
                    problems.add(name(entry) + " takes positions " + positions + ", which are not dense from 1");
                    break;
                }
            }
        }
        assertTrue(problems.isEmpty(),
            () -> problems.size() + " entries disagree about their arguments:%n" + String.join("%n", problems));
    }

    @Test
    void formattingAnEntryWithItsArgumentsNeverThrows() {
        // The failure this prevents is a MissingFormatArgumentException on the render thread, which is a black
        // screen rather than a wrong word: the pattern is only ever exercised at the moment it is drawn.
        for (Translated entry : entries()) {
            List<String> positions = placeholders(entry.english());
            String[] args = new String[positions.stream().mapToInt(Integer::parseInt).max().orElse(0)];
            Arrays.fill(args, "7");
            String formatted = entry.text(args);
            assertTrue(formatted.contains("7") || args.length == 0,
                name(entry) + " formatted to " + formatted + " and lost its argument");
        }
    }

    @Test
    void noTableHoldsTheSameTextTwiceUnderTwoNames() {
        // A duplicate is how a table ends up with one entry changed and the other not, and the reader cannot
        // tell which one the screen used.
        List<String> problems = new ArrayList<>();
        for (Translated[] table : TABLES) {
            List<String> seen = new ArrayList<>();
            for (Translated entry : table) {
                String key = entry.english();
                if (seen.contains(key)) {
                    problems.add(name(entry) + " repeats \"" + key + "\" in " + table[0].getClass().getSimpleName());
                }
                seen.add(key);
            }
        }
        assertTrue(problems.isEmpty(), () -> String.join("%n", problems));
    }

    private static List<Translated> entries() {
        List<Translated> all = new ArrayList<>();
        for (Translated[] table : TABLES) {
            all.addAll(Arrays.asList(table));
        }
        return all;
    }

    private static String name(Translated entry) {
        return entry.getClass().getSimpleName() + "." + entry.key();
    }

    private static List<String> placeholders(String pattern) {
        List<String> found = new ArrayList<>();
        Matcher matcher = PLACEHOLDER.matcher(pattern);
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found.stream().sorted().toList();
    }

    private static String stripPlaceholders(String pattern) {
        return PLACEHOLDER.matcher(pattern).replaceAll("");
    }

    private static boolean isArabicScript(int codepoint) {
        return (codepoint >= 0x0600 && codepoint <= 0x06FF)
            || (codepoint >= 0x0750 && codepoint <= 0x077F)
            || (codepoint >= 0xFB50 && codepoint <= 0xFDFF)
            || (codepoint >= 0xFE70 && codepoint <= 0xFEFF);
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path, e);
        }
    }
}
