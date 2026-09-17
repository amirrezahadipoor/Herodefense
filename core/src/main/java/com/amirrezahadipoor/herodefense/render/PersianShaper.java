package com.amirrezahadipoor.herodefense.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Turns a Persian sentence into the string libGDX has to be given for it to draw one.
 *
 * <p>Two things stand between a Persian string in the table and Persian on screen, and the platform does neither.
 * Arabic script is cursive, so each letter has up to four shapes depending on what it joins to, and Unicode keeps
 * those shapes as separate codepoints in the Presentation Forms blocks; FreeType will happily draw whichever
 * codepoint it is handed, but nothing in libGDX's text path picks them -- HarfBuzz's Arabic shaper has no
 * Persian-specific handling and gdx-freetype does not invoke it here. Separately, the script runs right to left
 * while a bitmap font renderer lays glyphs out left to right in storage order.
 *
 * <p>So {@link #shape(String)} does both halves, in the only order that works:
 * <ol>
 *   <li>{@link #reshape(String)} -- choose each letter's contextual form while the letters are still in logical
 *       order, because "the letter before it" means the one before it in the sentence, not the one to its left on
 *       screen;</li>
 *   <li>{@link BidiReordering#reorder(String)} -- put the shaped letters into visual order and mirror the
 *       brackets that now face the wrong way.</li>
 * </ol>
 *
 * <p>The result is a string of presentation-form codepoints in visual order, which is exactly what
 * {@link GameFonts} has to have generated glyphs for -- see its character set, which covers the Presentation Forms
 * blocks for this reason. Reordering is not idempotent and reshaping is not reversible: shape once, at the point
 * the string enters the render path, and never shape something already shaped.
 *
 * <p><strong>Correctness.</strong> The behaviour here is not an interpretation of the Unicode charts. Every rule is
 * pinned by {@code tools/i18n/golden/shaping-vectors.txt} -- 245 vectors produced by {@code arabic_reshaper} 3.0.1
 * and {@code python-bidi} 0.6.11, the two libraries that define what the reference implementations do -- and
 * {@code PersianShaperTest} fails on the first vector this class does not reproduce, printing both sides as
 * codepoints. Where this class and the reference could disagree, the reference wins, including where the reference
 * is arguably wrong; see {@link #applyLigatures}.
 */
public final class PersianShaper {

    /**
     * ZERO WIDTH JOINER. Not a letter, but the reshaper's table gives it all four forms and itself as each of them,
     * which is how it forces the letters on either side of it to join -- and it is then dropped, having done its
     * work. This is the difference between "می‌شود" written with a joiner and without one.
     */
    private static final int ZWJ = 0x200D;

    /** Form index meaning "this entry is not a shaping letter; emit its codepoint as it stands". */
    private static final int NOT_SUPPORTED = -1;

    private PersianShaper() {
    }

    /**
     * The visual form of {@code logical}: shaped, reordered and mirrored, ready for a left-to-right bitmap
     * renderer. Text with no Arabic-script content is returned unchanged, and pure ASCII is returned without any
     * work at all -- an ASCII paragraph is left to right by definition and mirrors nothing.
     */
    public static String shape(String logical) {
        if (logical == null || logical.isEmpty()) {
            return logical == null ? "" : logical;
        }
        int[] codepoints = logical.codePoints().toArray();
        if (isAscii(codepoints)) {
            return logical;
        }
        int[] shaped = reshape(codepoints);
        return BidiReordering.reorder(new String(shaped, 0, shaped.length));
    }

    /**
     * Whether {@code text} contains anything this shaper would change. Callers that draw a lot of Latin-only
     * strings can use this to skip shaping entirely rather than relying on {@link #shape(String)}'s fast path.
     */
    public static boolean containsArabicScript(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.codePoints().anyMatch(PersianShaper::isArabicScript);
    }

    /**
     * The contextual forms, still in logical order: step one of {@link #shape(String)}.
     *
     * <p>A port of {@code arabic_reshaper.reshape} under its default configuration, which means: harakat are
     * deleted, tatweel is kept, the zero width joiner is honoured, and ligatures are applied. Each of those is a
     * configuration switch in the reference; only this combination is implemented, and the golden vectors were
     * generated with it.
     */
    static String reshape(String logical) {
        int[] shaped = reshape(logical.codePoints().toArray());
        return new String(shaped, 0, shaped.length);
    }

    private static int[] reshape(int[] codepoints) {
        List<int[]> output = new ArrayList<>(codepoints.length);

        for (int codepoint : codepoints) {
            if (isHarakat(codepoint)) {
                // delete_harakat: the vowel marks go before any joining decision is made, so they cannot influence
                // which form a letter takes. They are combining marks with no presentation forms of their own.
                continue;
            }
            if (output.isEmpty()) {
                output.add(new int[] {codepoint, ArabicLetterForms.ISOLATED});
            } else {
                int[] previous = output.get(output.size() - 1);
                if (!isShapingLetter(codepoint)) {
                    output.add(new int[] {codepoint, NOT_SUPPORTED});
                } else if (previous[1] == NOT_SUPPORTED || !joinsBackward(codepoint)
                        || !joinsForward(previous[0])
                        || (previous[1] == ArabicLetterForms.FINAL && !joinsBothWays(previous[0]))) {
                    // Something stops the join: the previous character is not a shaping letter at all, this letter
                    // has no final or medial form (alef, dal, ra, waw and the rest only ever join backwards), the
                    // previous letter has no initial or medial form, or the previous letter already ended a word
                    // and cannot join on both sides.
                    output.add(new int[] {codepoint, ArabicLetterForms.ISOLATED});
                } else if (previous[1] == ArabicLetterForms.ISOLATED) {
                    previous[1] = ArabicLetterForms.INITIAL;
                    output.add(new int[] {codepoint, ArabicLetterForms.FINAL});
                } else {
                    previous[1] = ArabicLetterForms.MEDIAL;
                    output.add(new int[] {codepoint, ArabicLetterForms.FINAL});
                }
            }

            // A joiner between two letters has now done its work: it made the one before it join forward and the one
            // after it join backward, and it is not itself drawn.
            if (output.size() > 1 && output.get(output.size() - 2)[0] == ZWJ) {
                output.remove(output.size() - 2);
            }
        }

        if (!output.isEmpty() && output.get(output.size() - 1)[0] == ZWJ) {
            output.remove(output.size() - 1);
        }

        applyLigatures(codepoints, output);
        return emit(output);
    }

    /**
     * Substitutes the ligatures in {@link ArabicLigatures} over the harakat-free text.
     *
     * <p>The ligature's own form is decided by the forms the two ends of the match ended up with, exactly as the
     * reference decides it: if the first letter was isolated or initial, the ligature is isolated when the last one
     * was isolated or final and initial otherwise; if the first letter was medial or final, the ligature is final
     * when the last one was isolated or final and medial otherwise. A lam-alef has no initial or medial form, so in
     * those two cases the ligature is skipped and the lam and the alef stay separate -- the scan still advances
     * past both, because that is what the reference's regular expression does with a match it then declines to use.
     *
     * <p>One deliberate deviation. The reference indexes the output list with positions from the harakat-free text,
     * which stops being the same list the moment a zero width joiner has been dropped from it; given a string with
     * both, it raises {@code IndexError}. This method skips the substitution instead of crashing, because the
     * difference can only be reached by text that the reference cannot process at all. No golden vector contains
     * both a joiner and a ligature.
     */
    private static void applyLigatures(int[] codepoints, List<int[]> output) {
        int[] stripped = stripHarakat(codepoints);
        int index = 0;
        while (index < stripped.length) {
            int ligature = -1;
            for (int candidate = 0; candidate < ArabicLigatures.count(); candidate++) {
                if (matchesAt(stripped, index, ArabicLigatures.match(candidate))) {
                    ligature = candidate;
                    break;
                }
            }
            if (ligature < 0) {
                index++;
                continue;
            }
            int start = index;
            int end = index + ArabicLigatures.match(ligature).length;
            index = end;
            if (end > output.size()) {
                continue;
            }
            int firstForm = output.get(start)[1];
            int lastForm = output.get(end - 1)[1];
            boolean firstJoins = firstForm == ArabicLetterForms.MEDIAL || firstForm == ArabicLetterForms.FINAL;
            boolean lastJoins = lastForm == ArabicLetterForms.INITIAL || lastForm == ArabicLetterForms.MEDIAL;
            int form = firstJoins ? (lastJoins ? ArabicLetterForms.MEDIAL : ArabicLetterForms.FINAL)
                    : (lastJoins ? ArabicLetterForms.INITIAL : ArabicLetterForms.ISOLATED);
            int glyph = ArabicLigatures.form(ligature, form);
            if (glyph == 0) {
                continue;
            }
            output.get(start)[0] = glyph;
            output.get(start)[1] = NOT_SUPPORTED;
            for (int at = start + 1; at < end; at++) {
                output.get(at)[0] = 0;
                output.get(at)[1] = NOT_SUPPORTED;
            }
        }
    }

    /**
     * The presentation-form codepoints, in logical order. An entry whose codepoint is 0 was consumed by a ligature;
     * an entry marked {@link #NOT_SUPPORTED} is not a shaping letter and is emitted as it stands.
     */
    private static int[] emit(List<int[]> output) {
        int[] shaped = new int[output.size()];
        int size = 0;
        for (int[] entry : output) {
            if (entry[0] == 0) {
                continue;
            }
            int glyph = entry[1] == NOT_SUPPORTED ? entry[0] : formOf(entry[0], entry[1]);
            if (glyph == 0) {
                // The letter has no such form. The reference emits an empty string here, dropping the character;
                // the joining rules above make this unreachable, and dropping a letter silently would be worse than
                // drawing the wrong one, so the unshaped letter is kept.
                glyph = entry[0];
            }
            shaped[size++] = glyph;
        }
        return size == shaped.length ? shaped : Arrays.copyOf(shaped, size);
    }

    private static int formOf(int codepoint, int form) {
        return codepoint == ZWJ ? ZWJ : ArabicLetterForms.form(codepoint, form);
    }

    /**
     * Whether the letter can end a join. The zero width joiner answers yes to every one of these questions: its
     * whole purpose is to make the letters around it join, and the reference gives it all four forms to say so.
     */
    private static boolean joinsBackward(int codepoint) {
        return codepoint == ZWJ || ArabicLetterForms.joinsBackward(codepoint);
    }

    private static boolean joinsForward(int codepoint) {
        return codepoint == ZWJ || ArabicLetterForms.joinsForward(codepoint);
    }

    private static boolean joinsBothWays(int codepoint) {
        return codepoint == ZWJ || ArabicLetterForms.joinsBothWays(codepoint);
    }

    private static boolean isShapingLetter(int codepoint) {
        return codepoint == ZWJ || ArabicLetterForms.isLetter(codepoint);
    }

    private static boolean matchesAt(int[] text, int index, int[] pattern) {
        if (index + pattern.length > text.length) {
            return false;
        }
        for (int offset = 0; offset < pattern.length; offset++) {
            if (text[index + offset] != pattern[offset]) {
                return false;
            }
        }
        return true;
    }

    private static int[] stripHarakat(int[] codepoints) {
        int kept = 0;
        for (int codepoint : codepoints) {
            if (!isHarakat(codepoint)) {
                kept++;
            }
        }
        if (kept == codepoints.length) {
            return codepoints;
        }
        int[] stripped = new int[kept];
        int at = 0;
        for (int codepoint : codepoints) {
            if (!isHarakat(codepoint)) {
                stripped[at++] = codepoint;
            }
        }
        return stripped;
    }

    /**
     * Whether this is a vowel mark or other combining sign that the reshaper deletes: the Arabic harakat, the
     * extended marks of the Arabic Supplement, and the Quranic annotation signs.
     */
    static boolean isHarakat(int codepoint) {
        return (codepoint >= 0x0610 && codepoint <= 0x061A)
                || (codepoint >= 0x064B && codepoint <= 0x065F)
                || codepoint == 0x0670
                || (codepoint >= 0x06D6 && codepoint <= 0x06DC)
                || (codepoint >= 0x06DF && codepoint <= 0x06E8)
                || (codepoint >= 0x06EA && codepoint <= 0x06ED)
                || (codepoint >= 0x08D4 && codepoint <= 0x08ED)
                || (codepoint >= 0x08E3 && codepoint <= 0x08FF);
    }

    /**
     * Whether this codepoint is Arabic script in any of its blocks, including the presentation forms this class
     * produces -- already-shaped text has to be recognised as such by callers.
     */
    private static boolean isArabicScript(int codepoint) {
        return (codepoint >= 0x0600 && codepoint <= 0x06FF)
                || (codepoint >= 0x0750 && codepoint <= 0x077F)
                || (codepoint >= 0x08A0 && codepoint <= 0x08FF)
                || (codepoint >= 0xFB50 && codepoint <= 0xFDFF)
                || (codepoint >= 0xFE70 && codepoint <= 0xFEFF)
                || codepoint == ZWJ || codepoint == 0x200C;
    }

    private static boolean isAscii(int[] codepoints) {
        for (int codepoint : codepoints) {
            if (codepoint > 0x7F) {
                return false;
            }
        }
        return true;
    }
}
