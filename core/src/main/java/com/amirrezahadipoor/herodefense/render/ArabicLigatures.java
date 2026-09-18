package com.amirrezahadipoor.herodefense.render;

/**
 * The ligatures {@code arabic_reshaper} 3.0.1 substitutes by default, in the order its
 * regular expression tries them.
 *
 * <p><strong>Generated file</strong> -- written by {@code tools/i18n/make_shaping_tables.py} from the library's own
 * ligature list, filtered by its default configuration. Do not edit it by hand; edit the generator, and use its
 * {@code --check} mode in CI.
 *
 * <p>A ligature is several letters drawn as one glyph, and Arabic script has a few that are not optional: no Persian
 * font draws lam followed by alef as two separate letters. 5 are enabled out of the
 * {@code LIGATURES} the library knows about:
 * <ol>
 *   <li>{@code ARABIC LIGATURE ALLAH}</li>
 *   <li>{@code ARABIC LIGATURE LAM WITH ALEF}</li>
 *   <li>{@code ARABIC LIGATURE LAM WITH ALEF WITH HAMZA ABOVE}</li>
 *   <li>{@code ARABIC LIGATURE LAM WITH ALEF WITH HAMZA BELOW}</li>
 *   <li>{@code ARABIC LIGATURE LAM WITH ALEF WITH MADDA ABOVE}</li>
 * </ol>
 *
 * <p>The order is the alternation order of the reshaper's regular expression, so it is part of the data: at any
 * position the first pattern that matches wins, not the longest. The rial sign and the rest of the library's
 * ligatures are switched off in its default configuration and so are absent here -- matching the reference exactly
 * is the point, and {@code PersianShaperTest} holds both to the same 245 vectors.
 *
 * <p>Each entry carries four forms in the {@link ArabicLetterForms} order -- isolated, initial, medial, final --
 * and a zero means the ligature has no such form. That happens for every lam-alef pair in the initial and medial
 * positions, and it is not an omission: when a lam-alef would have to join on both sides, the reshaper leaves the
 * two letters separate instead of drawing a ligature.
 */
final class ArabicLigatures {

    private static final int[][] MATCHES = {
        { 0x0627, 0x0644, 0x0644, 0x0647 },
        { 0x0644, 0x0627 },
        { 0x0644, 0x0623 },
        { 0x0644, 0x0625 },
        { 0x0644, 0x0622 }
    };

    private static final int[][] FORMS = {
        { 0xFDF2, 0, 0, 0 },
        { 0xFEFB, 0, 0, 0xFEFC },
        { 0xFEF7, 0, 0, 0xFEF8 },
        { 0xFEF9, 0, 0, 0xFEFA },
        { 0xFEF5, 0, 0, 0xFEF6 }
    };

    private ArabicLigatures() {
    }

    /** How many ligatures are tried, in order, at each position. */
    static int count() {
        return MATCHES.length;
    }

    /** The codepoints of ligature {@code index}, in the order they appear in the text. */
    static int[] match(int index) {
        return MATCHES[index];
    }

    /**
     * The glyph for ligature {@code index} in the given {@link ArabicLetterForms} form, or 0 if it has no such
     * form -- in which case the letters are shaped separately.
     */
    static int form(int index, int form) {
        return FORMS[index][form];
    }
}
