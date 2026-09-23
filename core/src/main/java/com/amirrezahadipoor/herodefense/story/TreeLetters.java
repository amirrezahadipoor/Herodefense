package com.amirrezahadipoor.herodefense.story;

/**
 * Granny's letters (roadmap ST5, reworded with the story in MEMORY P3): after each dawn she
 * writes a short letter, kept per dawn number. The player hears the first unread one typed
 * out in the root network, the way Granny speaks everywhere else -- warm, plain, and counting
 * every dawn like a gift.
 *
 * <p>Reading a letter marks it as read under the key {@code letter_<n>} in the save's
 * {@code codexUnlocked} map. That map doubles as the "the save has seen this" ledger; the codex
 * counts only Lore entry ids, so letter keys never affect the written-count.
 */
public final class TreeLetters {

    /** Letters exist per dawn number; every dawn from the tenth on re-reads the last letter. */
    public static final int TIERS = 10;

    private TreeLetters() {
    }

    /** The save key that records the letter for the given dawn number as read. */
    public static String readKey(int tier) {
        return "letter_" + clamped(tier);
    }

    /** The letter text for the given dawn number. */
    public static String text(int tier) {
        switch (clamped(tier)) {
            case 1:
                return EN_1;
            case 2:
                return EN_2;
            case 3:
                return EN_3;
            case 4:
                return EN_4;
            case 5:
                return EN_5;
            case 6:
                return EN_6;
            case 7:
                return EN_7;
            case 8:
                return EN_8;
            case 9:
                return EN_9;
            default:
                return EN_10;
        }
    }

    private static int clamped(int tier) {
        return Math.min(Math.max(tier, 1), TIERS);
    }

    private static final String EN_1 =
        "So that was you. I felt every step, and I am keeping the light you left here. "
            + "Rest now, dearie. The night will learn your name soon. Let it shake.";
    private static final String EN_2 =
        "You came back before the light faded. Good. I am keeping two dawns now, and the "
            + "second burns stronger. Pip counted them twice. He loves counting.";
    private static final String EN_3 =
        "The night keeps testing you, and you keep passing. I am starting to understand: "
            + "the dark is not the strong one. The strong one stands up again. And soup helps.";
    private static final String EN_4 =
        "I have been keeping a small thing. Roots are not anchors. They are hands. Mine "
            + "have been holding you up from below this whole time. Feel them, dearie.";
    private static final String EN_5 =
        "Halfway, and the word feels heavy. Here is the truth anyway: you were never "
            + "halfway. You were always the whole tree, only growing. Keep growing.";
    private static final String EN_6 =
        "The Hollow asked me what you are. I did not answer. Some things are not for "
            + "naming. They are for walking, one night at a time. Walk on, Chief.";
    private static final String EN_7 =
        "I have stopped fearing the night. It keeps losing, and it does not seem to mind. "
            + "The bravest thing about you: you keep winning, and you keep being kind.";
    private static final String EN_8 =
        "Count the dawns you gave me. Eight. I counted too, and I have a secret: the "
            + "light does not come from above. It comes from you. I am only the window.";
    private static final String EN_9 =
        "One more, and the story takes a different shape. I do not know that shape either. "
            + "But a tree that leans toward a door already knows the way. Lean with me, dearie.";
    private static final String EN_10 =
        "This is my last letter in this shape. After you go through the door, I cannot "
            + "write in words anymore. I will write in light. Every dawn you left here will "
            + "come for you. Walk into it, dearie. Soup is on the other side.";
}
