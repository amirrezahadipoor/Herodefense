package com.amirrezahadipoor.herodefense.render;

/**
 * The four contextual forms of every Arabic-script letter, as {@code arabic_reshaper} 3.0.1
 * defines them.
 *
 * <p><strong>Generated file</strong> -- written by {@code tools/i18n/make_shaping_tables.py}, which reads the table
 * out of the library. Do not edit it by hand; edit the generator. The same script's {@code --check} mode compares
 * the committed file against a freshly generated one, so a silent edit fails the build.
 *
 * <p>Arabic script is cursive: the same letter is drawn differently depending on whether it joins to the letter
 * before it and to the letter after it. Unicode stores those variants as separate codepoints in the Presentation
 * Forms blocks, and that is what a bitmap font renders -- libGDX's FreeType path has no Arabic shaper, so
 * {@link PersianShaper} picks the form and the font draws it.
 *
 * <p>The table is a flat array over the contiguous block 0x0621..0x06d3, four ints per codepoint in the
 * order isolated, initial, medial, final. A zero means the letter has no such form -- the right-joining-only
 * letters (alef, dal, ra, waw and the rest, 102 codepoints of the range in total have no entry at all)
 * simply cannot take an initial or a medial shape, and asking for one is a bug in the caller. A {@code -1} means
 * the codepoint is not a shaping letter: it is inside the block's range but the reshaper leaves it alone.
 *
 * <p>U+200D ZERO WIDTH JOINER is not in the block and is handled separately by {@link PersianShaper}: it has
 * all four forms and is itself the form, which is how it forces two letters to join before being dropped.
 */
final class ArabicLetterForms {

    /** First codepoint covered by {@link #FORMS}. */
    static final int BASE = 0x0621;

    /** Last codepoint covered by {@link #FORMS}. */
    static final int LAST = 0x06d3;

    /** Form that joins to the letter before it only. */
    static final int ISOLATED = 0;
    static final int INITIAL = 1;
    static final int MEDIAL = 2;
    static final int FINAL = 3;

    /** Not a shaping letter: inside the block's range, left as it is. */
    static final int NOT_A_LETTER = -1;

    private static final int[] FORMS = {
        65152, 0, 0, 0, 65153, 0, 0, 65154, 65155, 0, 0, 65156,
        65157, 0, 0, 65158, 65159, 0, 0, 65160, 65161, 65163, 65164, 65162,
        65165, 0, 0, 65166, 65167, 65169, 65170, 65168, 65171, 0, 0, 65172,
        65173, 65175, 65176, 65174, 65177, 65179, 65180, 65178, 65181, 65183, 65184, 65182,
        65185, 65187, 65188, 65186, 65189, 65191, 65192, 65190, 65193, 0, 0, 65194,
        65195, 0, 0, 65196, 65197, 0, 0, 65198, 65199, 0, 0, 65200,
        65201, 65203, 65204, 65202, 65205, 65207, 65208, 65206, 65209, 65211, 65212, 65210,
        65213, 65215, 65216, 65214, 65217, 65219, 65220, 65218, 65221, 65223, 65224, 65222,
        65225, 65227, 65228, 65226, 65229, 65231, 65232, 65230, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, 1600, 1600, 1600, 1600, 65233, 65235, 65236, 65234,
        65237, 65239, 65240, 65238, 65241, 65243, 65244, 65242, 65245, 65247, 65248, 65246,
        65249, 65251, 65252, 65250, 65253, 65255, 65256, 65254, 65257, 65259, 65260, 65258,
        65261, 0, 0, 65262, 65263, 64488, 64489, 65264, 65265, 65267, 65268, 65266,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, 64336, 0, 0, 64337,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, 64477, 0, 0, 0,
        -1, -1, -1, -1, 64358, 64360, 64361, 64359, 64350, 64352, 64353, 64351,
        64338, 64340, 64341, 64339, -1, -1, -1, -1, -1, -1, -1, -1,
        64342, 64344, 64345, 64343, 64354, 64356, 64357, 64355, 64346, 64348, 64349, 64347,
        -1, -1, -1, -1, -1, -1, -1, -1, 64374, 64376, 64377, 64375,
        64370, 64372, 64373, 64371, -1, -1, -1, -1, 64378, 64380, 64381, 64379,
        64382, 64384, 64385, 64383, 64392, 0, 0, 64393, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, 64388, 0, 0, 64389,
        64386, 0, 0, 64387, 64390, 0, 0, 64391, -1, -1, -1, -1,
        -1, -1, -1, -1, 64396, 0, 0, 64397, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, 64394, 0, 0, 64395,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, 64362, 64364, 64365, 64363,
        -1, -1, -1, -1, 64366, 64368, 64369, 64367, -1, -1, -1, -1,
        -1, -1, -1, -1, 64398, 64400, 64401, 64399, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, 64467, 64469, 64470, 64468,
        -1, -1, -1, -1, 64402, 64404, 64405, 64403, -1, -1, -1, -1,
        64410, 64412, 64413, 64411, -1, -1, -1, -1, 64406, 64408, 64409, 64407,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        64414, 0, 0, 64415, 64416, 64418, 64419, 64417, -1, -1, -1, -1,
        -1, -1, -1, -1, 64426, 64428, 64429, 64427, -1, -1, -1, -1,
        64420, 0, 0, 64421, 64422, 64424, 64425, 64423, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, 64480, 0, 0, 64481,
        64473, 0, 0, 64474, 64471, 0, 0, 64472, 64475, 0, 0, 64476,
        64482, 0, 0, 64483, -1, -1, -1, -1, 64478, 0, 0, 64479,
        64508, 64510, 64511, 64509, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, 64484, 64486, 64487, 64485, -1, -1, -1, -1,
        64430, 0, 0, 64431, 64432, 0, 0, 64433,
    };

    private ArabicLetterForms() {
    }

    /** Whether this codepoint is one the reshaper knows a contextual form for. */
    static boolean isLetter(int codepoint) {
        return inRange(codepoint) && FORMS[(codepoint - BASE) * 4] != NOT_A_LETTER;
    }

    /**
     * The codepoint of {@code form} for {@code codepoint}, or 0 if the letter has no such form. Returns the
     * codepoint itself for anything outside the table, which is what "leave it alone" means here.
     */
    static int form(int codepoint, int form) {
        if (!inRange(codepoint)) {
            return codepoint;
        }
        int value = FORMS[(codepoint - BASE) * 4 + form];
        return value == NOT_A_LETTER ? codepoint : value;
    }

    /** Whether the letter can join to a letter before it, which is what a final or a medial form means. */
    static boolean joinsBackward(int codepoint) {
        return isLetter(codepoint) && (has(codepoint, FINAL) || has(codepoint, MEDIAL));
    }

    /** Whether the letter can join to a letter after it, which is what an initial or a medial form means. */
    static boolean joinsForward(int codepoint) {
        return isLetter(codepoint) && (has(codepoint, INITIAL) || has(codepoint, MEDIAL));
    }

    /** Whether the letter can join on both sides at once. */
    static boolean joinsBothWays(int codepoint) {
        return isLetter(codepoint) && has(codepoint, MEDIAL);
    }

    private static boolean has(int codepoint, int form) {
        return FORMS[(codepoint - BASE) * 4 + form] > 0;
    }

    private static boolean inRange(int codepoint) {
        return codepoint >= BASE && codepoint <= LAST;
    }
}
