package com.amirrezahadipoor.herodefense.render;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PersianShaper} held to {@code tools/i18n/golden/shaping-vectors.txt}, plus the individual rules those
 * vectors are made of.
 *
 * <p>The golden file is the point of this class. Persian shaping has no small number of cases -- four forms per
 * letter, four ligatures, joining that stops at half the alphabet, a bidi pass with its own seven weak-type rules
 * -- and reasoning about any single one of them in isolation is how a shaper ends up right on the demo string and
 * wrong in the settings menu. So the whole corpus is checked at once, against answers this repository did not
 * invent: {@code arabic_reshaper} and {@code python-bidi} produced them, and
 * {@code tools/i18n/make_shaping_vectors.py --check} reproduces them from the same libraries in CI.
 *
 * <p>A vector failure prints both sides as codepoints, because the two strings are usually made of characters that
 * look identical or invisible, and a diff of glyphs tells you nothing. The named tests below exist so that a
 * failure says which rule broke rather than only which line of the corpus.
 */
class PersianShaperTest {

    private static final Path GOLDEN = Path.of("..", "tools", "i18n", "golden", "shaping-vectors.txt").normalize();

    /** Measured when the corpus was generated; a smaller number means the file was truncated, not that we pass. */
    private static final int EXPECTED_VECTORS = 245;

    @Test
    void shapesEveryGoldenVector() {
        List<Vector> vectors = vectors();
        assertEquals(EXPECTED_VECTORS, vectors.size(),
            () -> "expected " + EXPECTED_VECTORS + " vectors in " + GOLDEN + ", found " + vectors.size()
                + " -- a truncated golden file would pass the rest of this test while checking nothing");

        List<String> failures = new ArrayList<>();
        for (Vector vector : vectors) {
            String actual = PersianShaper.shape(vector.logical);
            if (!actual.equals(vector.visual)) {
                failures.add(String.format(
                    "  logical %s%n    expected %s%n    actual   %s",
                    vector.logical, codepoints(vector.visual), codepoints(actual)));
            }
        }
        assertTrue(failures.isEmpty(),
            () -> failures.size() + " of " + vectors.size() + " shaping vectors differ:%n"
                + String.join(String.format("%n"), failures));
    }

    @Test
    void picksTheContextualFormOfEachLetter() {
        // beh on its own is isolated; before a letter it is initial; between two it is medial; after one it is
        // final. These are four different codepoints in the Presentation Forms-B block, and the font draws
        // whichever it is given.
        assertEquals("FE8F", codepoints(PersianShaper.reshape("ب")));
        assertEquals("FE8F 0020 FE8F", codepoints(PersianShaper.reshape("ب ب")));
        assertEquals("FE91 FE8E", codepoints(PersianShaper.reshape("با")));
        assertEquals("FEE3 FE90", codepoints(PersianShaper.reshape("مب")));
        assertEquals("FEE3 FE92 FBFD", codepoints(PersianShaper.reshape("مبی")));
    }

    @Test
    void stopsJoiningAtLettersThatOnlyJoinBackwards() {
        // alef, dal, ra, waw and the rest have no initial or medial form at all: nothing can join *through* them,
        // so the letter after one starts a new word visually even in the middle of a word.
        assertEquals("FEE3 FE8E", codepoints(PersianShaper.reshape("ما")), "alef joins backwards, so it is final");
        assertEquals("FEE3 FE8E FEE1", codepoints(PersianShaper.reshape("مام")),
            "and nothing joins through it, so the mim after it is isolated again");
        assertEquals("FEAD FEE1", codepoints(PersianShaper.reshape("رم")));
        assertEquals("FEAD FEE3 FEEE", codepoints(PersianShaper.reshape("رمو")));
    }

    @Test
    void deletesHarakatBeforeDecidingAnyJoin() {
        // The vowel marks are combining characters with no presentation forms of their own, and the reference
        // configuration drops them. Dropping them *first* is the part that matters: a shadda left in place would
        // sit between two letters and break the join between them.
        assertEquals("FDF2", codepoints(PersianShaper.reshape("اللّه")));
        assertEquals(codepoints(PersianShaper.reshape("مب")), codepoints(PersianShaper.reshape("مَب")),
            "a fatha between two letters is deleted before the join is decided, so the join survives it");
        assertEquals("FEE1 0020 FE8F", codepoints(PersianShaper.reshape("مَ بِ")),
            "and it is the space, not the mark, that isolates the beh here");
        assertEquals(codepoints(PersianShaper.reshape("سلام")),
            codepoints(PersianShaper.reshape("س\u0651\u064Eلام")),
            "a word with its vowel marks on shapes to exactly the word without them");
    }

    @Test
    void honoursTheZeroWidthJoinerAndItsOpposite() {
        // ZWJ forces the letters around it to join and is then dropped. ZWNJ does the opposite and is kept, which
        // is what makes Persian "می‌شود" (with the joiner) and "میشود" (without) two different spellings.
        assertEquals("FEE3 FBFF FEB8 FEEE FEA9", codepoints(PersianShaper.reshape("می\u200Dشود")));
        assertEquals("FEE3 FBFD 200C FEB7 FEEE FEA9", codepoints(PersianShaper.reshape("می\u200Cشود")));
        assertFalse(PersianShaper.reshape("می\u200Dشود").contains("\u200D"), "the joiner is drawn by joining, not by itself");
        assertTrue(PersianShaper.reshape("می\u200Cشود").contains("\u200C"), "the non-joiner stays: it is the break");
    }

    @Test
    void keepsTatweelAsAJoiner() {
        // The kashida is a lengthening stroke, not a letter, but it joins on both sides and survives shaping.
        assertEquals("FEB3 0640 FEFC FEE1", codepoints(PersianShaper.reshape("سـلام")));
    }

    @Test
    void substitutesTheLigatures() {
        assertEquals("FEFB", codepoints(PersianShaper.reshape("لا")));
        // The ligature takes the final form here because the lam joined to the mim before it, and the mim is
        // left with its initial form -- three letters in, two glyphs out.
        assertEquals("FEE3 FEFC", codepoints(PersianShaper.reshape("ملا")));
        assertEquals("FEF5", codepoints(PersianShaper.reshape("لآ")));
        assertEquals("FEF7", codepoints(PersianShaper.reshape("لأ")));
        assertEquals("FEF9", codepoints(PersianShaper.reshape("لإ")));
        assertEquals("FDF2", codepoints(PersianShaper.reshape("الله")));
        // Allah wins over lam-alef because the reference tries it first: an alternation takes the first branch that
        // matches at a position, not the longest.
        assertEquals("FDF2", codepoints(PersianShaper.reshape("اللّه")));
    }

    @Test
    void reversesRunsButNeverTheInsideOfANumber() {
        // The wave counter is the case that would be noticed first: reversed, "موج 12" would read as the word
        // followed by twenty-one.
        assertEquals("0031 0033 0020 FE9D FEEE FEE3", codepoints(PersianShaper.shape("موج 13")),
            "the digits keep their order and the run order flips around them");
        // Persian digits are an Arabic number run rather than a European one, and behave the same way.
        assertEquals("0028 06F1 06F2 0029 0020 FE9D FEEE FEE3", codepoints(PersianShaper.shape("موج (۱۲)")));
        // A sentence that starts in Latin keeps a left-to-right paragraph and puts the Persian run at the end.
        assertEquals("0057 0061 0076 0065 0020 0031 0032 003A 0020 FE9D FEEE FEE3",
            codepoints(PersianShaper.shape("Wave 12: موج")));
    }

    @Test
    void mirrorsBracketsInsideARightToLeftRun() {
        // L4: a bracket whose resolved direction is right to left is drawn as its mirror image, because after
        // reordering it sits on the other side of what it encloses. An unpaired bracket shows it; a matched pair
        // cancels out, since reversing the run swaps the two brackets and mirroring swaps them back.
        assertEquals("0029 0020 FE9D FEEE FEE3", codepoints(PersianShaper.shape("موج (")),
            "the opening bracket ends up leftmost and is drawn closed");
        assertEquals("005B 0020 FE9D FEEE FEE3", codepoints(PersianShaper.shape("موج ]")),
            "and the closing one is drawn open");
        assertEquals("0028 FE9D FEEE FEE3 0029", codepoints(PersianShaper.shape("(موج)")),
            "a matched pair reads the same as it would in Latin, with the word reversed inside it");
    }

    @Test
    void mirrorsNothingInALeftToRightRun() {
        assertEquals("0057 0061 0076 0065 0020 0028 0031 0032 0029", codepoints(PersianShaper.shape("Wave (12)")),
            "mirroring is a property of the resolved direction, not of the character");
    }

    @Test
    void leavesTextWithNothingToShapeAlone() {
        assertEquals("", PersianShaper.shape(""));
        assertEquals("Wave 12", PersianShaper.shape("Wave 12"));
        assertEquals("12/34", PersianShaper.shape("12/34"));
        assertEquals("Wave 12", PersianShaper.shape("Wave 12"), "pure ASCII takes the fast path and is untouched");
    }

    @Test
    void recognisesArabicScriptInAnyOfItsBlocks() {
        assertTrue(PersianShaper.containsArabicScript("موج"));
        assertTrue(PersianShaper.containsArabicScript("Wave 12 موج"));
        assertTrue(PersianShaper.containsArabicScript("می\u200Cشود"), "the non-joiner is part of the script's usage");
        assertTrue(PersianShaper.containsArabicScript("\uFEFB"), "already-shaped presentation forms count too");
        assertFalse(PersianShaper.containsArabicScript("Wave 12"));
        assertFalse(PersianShaper.containsArabicScript(""));
        assertFalse(PersianShaper.containsArabicScript(null));
    }

    /** One line of the golden file: what the string table says, and what the eye has to be handed. */
    private record Vector(String logical, String visual) {
    }

    private static List<Vector> vectors() {
        try {
            List<Vector> vectors = new ArrayList<>();
            for (String line : Files.readAllLines(GOLDEN, StandardCharsets.UTF_8)) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int tab = line.indexOf('\t');
                if (tab < 0) {
                    throw new IllegalStateException("vector line has no tab separator: " + line);
                }
                vectors.add(new Vector(line.substring(0, tab), fromCodepoints(line.substring(tab + 1).trim())));
            }
            return vectors;
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + GOLDEN.toAbsolutePath(), e);
        }
    }

    /** "FE8F 0020 FEB3" -> the string those codepoints name. */
    private static String fromCodepoints(String hex) {
        StringBuilder out = new StringBuilder();
        if (hex.isEmpty()) {
            return "";
        }
        for (String part : hex.split(" ")) {
            out.appendCodePoint(Integer.parseInt(part, 16));
        }
        return out.toString();
    }

    /** The string as codepoints, which is the only way two shaping results can be compared by eye. */
    private static String codepoints(String text) {
        StringBuilder out = new StringBuilder();
        text.codePoints().forEach(codepoint -> {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(String.format("%04X", codepoint));
        });
        return out.toString();
    }
}
