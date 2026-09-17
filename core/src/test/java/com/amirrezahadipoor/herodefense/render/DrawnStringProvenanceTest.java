package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Nothing reaches the screen except through a string table (roadmap R7.3's "a test that fails on hard-coded or
 * untranslated user-facing strings").
 *
 * <p>{@code TranslationTableTest} checks the tables as tables: both languages present, both taking the same
 * arguments, no duplicates. That is the untranslated half, and alone it is worth nothing, because a screen can
 * ignore a perfect table and draw NEW GAME itself. This is the other half.
 *
 * <p>The scope is every main source in the game, and it has to be: {@link OverlayText} is package-private, so
 * only {@code render} can turn a string into pixels, but the string it is handed is often written somewhere
 * else. The onboarding coach lines live in {@code onboarding/OnboardingStep}, the boss taglines in
 * {@code story/BossTitleCards}, the stat suffixes in {@code tooltips/StatTooltips}, and a gate that stopped at
 * the render package would have passed all of them while a Persian player read them in English. One package is
 * excluded and it is excluded for a reason that can be checked: {@code balance} writes {@code docs/BALANCE.md},
 * which is a document a developer reads and {@code BalanceDocumentTest} already gates, not a sentence the game
 * draws.
 *
 * <p>Which literals are user-facing is decided by where they sit, because a renderer holds four other kinds of
 * literal that must not be reported. Icon keys, asset paths and colour hexes are easy: none of them is a
 * sentence. Diagnostics are the reason the scan is positional rather than a search for English -- exception
 * messages, log tags and shader uniform names are English on purpose, because a diagnostic is read by a
 * developer and not by a player, and translating one would be a claim this game cannot keep. So a literal is
 * user-facing when it is handed to something that draws, built inside a method whose name says it produces
 * words, or passed to a formatter:
 *
 * <ul>
 *   <li>inside the arguments of any call to a method named {@code draw*}. That covers {@code text.draw} and its
 *       aligned and mirrored variants, and it covers the per-screen helpers ({@code drawRow},
 *       {@code drawMenuAction}, {@code drawToggle}) that forward their strings to the text layer -- which is the
 *       hole a check on {@code text.draw} alone would leave, since a literal passed to a helper never appears at
 *       a {@code text.draw} call site and is still on screen.</li>
 *   <li>inside the body of a method whose name holds one of {@code label}, {@code text}, {@code title},
 *       {@code caption}, {@code hint}, {@code summary}, {@code name} or {@code line}, in either case and
 *       anywhere in the name, because this codebase spells the same kind of method both ways:
 *       {@code experienceLabel} and {@code labelFor}. Those helpers return a sentence instead of drawing one, so
 *       their literals reach the screen one frame later.</li>
 *   <li>inside a {@code String.format} or {@code String.join} call, which is how a sentence gets assembled out
 *       of pieces when one table entry with positional arguments would have held it.</li>
 * </ul>
 *
 * <p>Inside those spans a literal still has to look like a sentence to be reported: it holds a space, a capital
 * letter, a non-ASCII character, a percent sign or a bar, once its format specifiers are taken out. That admits
 * every string this game draws, excludes the icon keys those same calls carry beside them, and excludes a
 * diagnostic's number format, which is a specifier and nothing else.
 *
 * <p>{@link #NOT_YET_MIGRATED} is a ratchet, not a permission: those renderers predate the tables, they are
 * listed by name so the list can only shrink, and {@link #nothingIsListedThatHasAlreadyMoved} fails the build
 * when one of them stops offending so its name comes off.
 */
final class DrawnStringProvenanceTest {

    private static final Path SOURCES = Path
        .of("..", "core", "src", "main", "java", "com", "amirrezahadipoor", "herodefense")
        .normalize();

    /**
     * Packages whose strings are written into a document rather than drawn. {@code balance} generates
     * {@code docs/BALANCE.md} out of a sweep, and its own test fails when the document and the code disagree.
     */
    private static final String DOCUMENT_PACKAGES = "/balance/";

    /** A string literal, quoted, with escape pairs kept whole so an escaped quote cannot end the match early. */
    private static final Pattern LITERAL = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");

    /** Any call whose method draws: the text layer's own methods and the helpers that forward to them. */
    private static final Pattern DRAW_CALL = Pattern.compile("\\bdraw\\w*\\s*\\(");

    /** Any method whose name says it produces words rather than drawing them. */
    private static final Pattern WORD_METHOD = Pattern.compile(
        "(?i)\\b\\w*(?:label|text|title|subtitle|caption|hint|summary|name|lines?)\\w*\\s*\\(");

    /**
     * An enum constant with arguments, at the start of its line: {@code WALK("walk", "Tap empty ground...")}.
     * This is where the game's content vocabulary lives -- onboarding steps, wave omens, boss cards, item
     * passives -- and none of it passes through a {@code draw} call in its own file, so a scan that only followed
     * calls would have missed every word a player reads about the thing they are looking at.
     */
    private static final Pattern ENUM_CONSTANT = Pattern.compile("(?m)^[ \\t]*[A-Z][A-Z0-9_]*[ \\t]*\\(");

    /** The tables' own package, where literals in enum constants are the point rather than the offence. */
    private static final String TABLE_PACKAGE = "/i18n/";

    /** A sentence assembled out of pieces instead of held in one table entry. */
    private static final Pattern FORMATTER = Pattern.compile("\\bString\\s*\\.\\s*(?:format|join)\\s*\\(");

    /** A format specifier on its own -- {@code "%.1f"} -- is syntax, not a sentence. */
    private static final Pattern FORMAT_SPECIFIER = Pattern.compile("%[-#+ 0,(]*\\d*(?:\\.\\d+)?[a-zA-Z]");

    /**
     * An all-capitals key with an underscore in it -- {@code ANCIENT_GOLEM}, {@code STRAIGHT_RGBA} -- which is an
     * identifier a lookup table is keyed by, not a sentence. A single capital word is still treated as a sentence,
     * because {@code ON}, {@code OFF}, {@code MAX} and {@code ITEM} are drawn and {@code ANCIENT_GOLEM} is not.
     */
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+");

    private static final Pattern ASSET_PATH = Pattern.compile("[^\"\\s]*/[^\"\\s]*");
    private static final Pattern COLOUR_HEX = Pattern.compile("[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?");

    /**
     * The files that still carry their own words, named by {@link #nothingDrawsAHardCodedString} itself: run it,
     * read the report, and every name in it belongs here until that file speaks through a table. It is a ratchet
     * in both directions -- a name that stays after its file is migrated fails the build too.
     */
    private static final Set<String> NOT_YET_MIGRATED = Set.of(
        "AffixId.java",
        "BossTitleCards.java",
        "CeremonyLines.java",
        "CodexOverlayRenderer.java",
        "Epilogue.java",
        "EquipmentSetBonus.java",
        "FloatingCoinTextRenderer.java",
        "InventoryOverlayRenderer.java",
        "ItemForgeSystem.java",
        "LevelUpOverlayRenderer.java",
        "MythicEffects.java",
        "OnboardingOverlayRenderer.java",
        "OnboardingStep.java",
        "ReflectionLines.java",
        "RewardCardId.java",
        "RewardCardOverlayRenderer.java",
        "RootNetworkOverlayRenderer.java",
        "SkillEvolution.java",
        "SkillId.java",
        "StatShopOverlayRenderer.java",
        "StatTooltips.java",
        "TrialDraftOverlayRenderer.java",
        "TrialId.java",
        "Trophy.java",
        "TrophyPresenter.java"
    );

    @Test
    void nothingDrawsAHardCodedString() {
        List<String> problems = new ArrayList<>();
        for (Path path : sources()) {
            String name = fileName(path);
            List<String> found = drawnLiterals(path, read(path));
            if (found.isEmpty() || NOT_YET_MIGRATED.contains(name)) {
                continue;
            }
            problems.add(name + " draws " + found.size() + " hard-coded string" + (found.size() == 1 ? "" : "s")
                + ": " + String.join(", ", found.subList(0, Math.min(6, found.size())))
                + (found.size() > 6 ? ", ..." : ""));
        }
        assertTrue(problems.isEmpty(), () -> problems.size() + " files bypass the string tables:%n"
            + String.join("%n", problems) + "%n%n"
            + "Each of these words is drawn in one language only. Move it into the screen's table in "
            + "core/src/main/java/com/amirrezahadipoor/herodefense/i18n, list that table in GameStrings so the "
            + "font derives its glyphs and TranslationTableTest checks both languages, and draw it through "
            + "GameLocale.text. A file that cannot be migrated yet goes in NOT_YET_MIGRATED above, which is a "
            + "ratchet: nothingIsListedThatHasAlreadyMoved fails once its name can come off.");
    }

    @Test
    void nothingIsListedThatHasAlreadyMoved() {
        List<String> stale = new ArrayList<>();
        for (Path path : sources()) {
            String name = fileName(path);
            if (NOT_YET_MIGRATED.contains(name) && drawnLiterals(path, read(path)).isEmpty()) {
                stale.add(name);
            }
        }
        assertTrue(stale.isEmpty(), () -> "NOT_YET_MIGRATED lists " + String.join(", ", stale)
            + ", which no longer draws a hard-coded string: take the name off the list, because a ratchet that is "
            + "not tightened is a ratchet that will let the string come back");
    }

    /**
     * Every user-facing literal in the source, in file order and without duplicates: one literal can sit in two
     * spans at once, as a {@code String.format} inside a {@code draw} call does, and it is one string either way.
     */
    private static List<String> drawnLiterals(Path path, String source) {
        String code = withoutComments(source);
        SortedMap<Integer, String> hits = new TreeMap<>();
        for (Pattern call : List.of(DRAW_CALL, FORMATTER)) {
            collectCallArguments(code, call, hits);
        }
        collectWordMethodBodies(code, hits);
        if (!isTable(path)) {
            collectCallArguments(code, ENUM_CONSTANT, hits);
        }
        return new ArrayList<>(hits.values());
    }

    private static void collectCallArguments(String code, Pattern call, SortedMap<Integer, String> hits) {
        Matcher matcher = call.matcher(code);
        while (matcher.find()) {
            int open = matcher.end() - 1;
            int close = matching(code, open, '(', ')');
            if (close > open) {
                collectLiterals(code, open, close, hits);
            }
        }
    }

    /** A declaration's body, found by the brace that follows its parameter list and nothing else. */
    private static void collectWordMethodBodies(String code, SortedMap<Integer, String> hits) {
        Matcher matcher = WORD_METHOD.matcher(code);
        while (matcher.find()) {
            int close = matching(code, matcher.end() - 1, '(', ')');
            if (close < 0) {
                continue;
            }
            int brace = code.indexOf('{', close);
            if (brace < 0 || !code.substring(close + 1, brace).isBlank()) {
                continue;
            }
            int end = matching(code, brace, '{', '}');
            if (end > brace) {
                collectLiterals(code, brace, end, hits);
            }
        }
    }

    private static void collectLiterals(String code, int from, int to, SortedMap<Integer, String> hits) {
        Matcher matcher = LITERAL.matcher(code);
        matcher.region(from, to);
        while (matcher.find()) {
            String value = matcher.group(1);
            if (isDrawnSentence(value)) {
                hits.put(matcher.start(), '"' + value + '"');
            }
        }
    }

    private static boolean isTable(Path path) {
        return path.toString().replace('\\', '/').contains(TABLE_PACKAGE);
    }

    private static boolean isDrawnSentence(String value) {
        if (value.isEmpty() || ASSET_PATH.matcher(value).matches() || COLOUR_HEX.matcher(value).matches()
            || IDENTIFIER.matcher(value).matches()) {
            return false;
        }
        // What is left once the specifiers are taken out is the part a player reads: "%.1f" leaves nothing and is
        // a diagnostic's number format, "%1$s XP" leaves " XP" and is a sentence with a hole in it.
        String words = FORMAT_SPECIFIER.matcher(value).replaceAll("");
        return words.codePoints().anyMatch(cp -> cp == ' ' || cp == '%' || cp == '|' || cp > 0x7F
            || Character.isUpperCase(cp));
    }

    /**
     * The index of the bracket closing the one at {@code open}, or -1 when it is not there or not balanced.
     * Literals are stepped over whole, so a parenthesis inside a string cannot close anything.
     */
    private static int matching(String code, int open, char openChar, char closeChar) {
        if (open < 0 || open >= code.length() || code.charAt(open) != openChar) {
            return -1;
        }
        int depth = 0;
        int index = open;
        while (index < code.length()) {
            int pastLiteral = skipLiteral(code, index);
            if (pastLiteral > index) {
                index = pastLiteral;
                continue;
            }
            char current = code.charAt(index);
            if (current == openChar) {
                depth++;
            } else if (current == closeChar) {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
            index++;
        }
        return -1;
    }

    /** The index just past the literal that starts at {@code index}, or {@code index} when none starts there. */
    private static int skipLiteral(String code, int index) {
        char quote = code.charAt(index);
        if (quote != '"' && quote != '\'') {
            return index;
        }
        int cursor = index + 1;
        while (cursor < code.length()) {
            char current = code.charAt(cursor);
            if (current == '\\') {
                cursor += 2;
            } else if (current == quote) {
                return cursor + 1;
            } else {
                cursor++;
            }
        }
        return index;
    }

    /**
     * The source with its comments blanked, because a javadoc that quotes a screen's words would otherwise be
     * indistinguishable from a call that draws them. Blanked rather than cut, so every offset still lines up with
     * the file and a report can name a line. Newlines are kept for the same reason, and literals are copied
     * whole so a pair of slashes inside one -- an asset path, a URL -- does not swallow the rest of the line.
     */
    private static String withoutComments(String source) {
        StringBuilder out = new StringBuilder(source.length());
        int index = 0;
        while (index < source.length()) {
            char current = source.charAt(index);
            char next = index + 1 < source.length() ? source.charAt(index + 1) : '\0';
            if (current == '/' && next == '/') {
                index = blankLineComment(source, index, out);
            } else if (current == '/' && next == '*') {
                index = blankBlockComment(source, index, out);
            } else if (current == '"' || current == '\'') {
                index = copyLiteral(source, index, out);
            } else {
                out.append(current);
                index++;
            }
        }
        return out.toString();
    }

    private static int blankLineComment(String source, int from, StringBuilder out) {
        int index = from;
        while (index < source.length() && source.charAt(index) != '\n') {
            out.append(' ');
            index++;
        }
        return index;
    }

    private static int blankBlockComment(String source, int from, StringBuilder out) {
        int index = from;
        while (index < source.length()) {
            boolean closing = source.charAt(index) == '*'
                && index + 1 < source.length() && source.charAt(index + 1) == '/';
            if (closing) {
                out.append("  ");
                return index + 2;
            }
            out.append(source.charAt(index) == '\n' ? '\n' : ' ');
            index++;
        }
        return index;
    }

    private static int copyLiteral(String source, int from, StringBuilder out) {
        char quote = source.charAt(from);
        out.append(quote);
        int index = from + 1;
        while (index < source.length()) {
            char current = source.charAt(index);
            out.append(current);
            index++;
            if (current == '\\' && index < source.length()) {
                out.append(source.charAt(index));
                index++;
            } else if (current == quote) {
                return index;
            }
        }
        return index;
    }

    /** {@code Path.getFileName} is null for a filesystem root, and the report needs a name either way. */
    private static String fileName(Path path) {
        Path name = path.getFileName();
        return name == null ? path.toString() : name.toString();
    }

    private static List<Path> sources() {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            return files
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> !path.toString().replace('\\', '/').contains(DOCUMENT_PACKAGES))
                .sorted()
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot list " + SOURCES.toAbsolutePath(), e);
        }
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path, e);
        }
    }
}
