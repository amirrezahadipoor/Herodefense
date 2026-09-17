package com.amirrezahadipoor.herodefense.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * The Unicode bidirectional algorithm, in the amount a Persian user interface needs: reordering plus mirroring.
 *
 * <p>Arabic script is written right to left but stored left to right, and libGDX draws a string glyph by glyph in
 * storage order. So "موج 12" would come out as the word, then the digits, in the wrong order and with the digits
 * reversed if they were reversed at all. This class turns a shaped string into the order a left-to-right bitmap
 * renderer must be handed so that the eye reads it correctly.
 *
 * <p>It is a port of {@code bidi.algorithm.get_display} from {@code python-bidi} 0.6.11 -- the same function, with
 * the same rule numbering and the same order of passes, that produced {@code tools/i18n/golden/shaping-vectors.txt}.
 * Porting rather than reinterpreting is deliberate: UAX&nbsp;#9 has a great many edges (weak number types, neutral
 * runs, embedding levels) and the golden vectors only pin down the behaviour of the whole pipeline, not the
 * reasoning behind each pass. Where the two libraries' data could disagree, the reference wins; the one place this
 * class knowingly differs is documented at {@link #bidiType(int)}.
 *
 * <p>The passes, in order:
 * <ol>
 *   <li>P2/P3 -- the paragraph direction comes from the first strong character, which is why a Persian string needs
 *       no marker and a mixed one behaves;</li>
 *   <li>X1-X9 -- explicit embedding and override controls, then their removal;</li>
 *   <li>X10 -- split into level runs, each with a start-of-run and end-of-run direction;</li>
 *   <li>W1-W7 -- weak types resolved: nonspacing marks, European and Arabic numbers, separators and terminators.
 *       This is what keeps "12" reading as twelve inside a right-to-left sentence instead of twenty-one;</li>
 *   <li>N1/N2 -- neutrals take the direction of the strong text around them, or the embedding direction;</li>
 *   <li>I1/I2 -- resolved types turned back into levels;</li>
 *   <li>L1/L2 -- trailing whitespace and separators reset, then every contiguous run at or above each level, from
 *       the highest down to the lowest odd one, reversed;</li>
 *   <li>L4 -- mirroring, via {@link BidiMirroring}.</li>
 * </ol>
 *
 * <p>{@link PersianShaper} calls this after shaping, never before: the contextual forms have to be chosen while the
 * letters are still in logical order, because "the letter before it" means the one before it in the sentence.
 */
final class BidiReordering {

    /** UAX #9's cap on explicit embedding depth. */
    private static final int EXPLICIT_LEVEL_LIMIT = 62;

    private static final byte L = 0;
    private static final byte R = 1;
    private static final byte AL = 2;
    private static final byte EN = 3;
    private static final byte ES = 4;
    private static final byte ET = 5;
    private static final byte AN = 6;
    private static final byte CS = 7;
    private static final byte NSM = 8;
    private static final byte BN = 9;
    private static final byte B = 10;
    private static final byte S = 11;
    private static final byte WS = 12;
    private static final byte ON = 13;
    private static final byte LRE = 14;
    private static final byte LRO = 15;
    private static final byte RLE = 16;
    private static final byte RLO = 17;
    private static final byte PDF = 18;

    /** No override in effect; X6 leaves each character's resolved type alone. */
    private static final byte NO_OVERRIDE = -1;

    private BidiReordering() {
    }

    /**
     * The display order of {@code text}: what a left-to-right renderer must be handed for the eye to read it in
     * {@code text}'s own direction. An empty or already-visual string comes back unchanged in the trivial sense --
     * this is not idempotent, and callers must not feed it its own output.
     */
    static String reorder(String text) {
        if (text == null || text.isEmpty()) {
            return text == null ? "" : text;
        }
        int[] codepoints = text.codePoints().toArray();
        Storage storage = new Storage(codepoints, baseLevel(codepoints));
        explicitEmbeddingsAndOverrides(storage);
        levelRuns(storage);
        weakTypes(storage);
        neutralTypes(storage);
        implicitLevels(storage);
        reorderLevels(storage);
        mirror(storage);
        return storage.display();
    }

    /** P2/P3: the first strong character decides the paragraph, and a paragraph with none is left to right. */
    private static int baseLevel(int[] codepoints) {
        for (int codepoint : codepoints) {
            byte type = bidiType(codepoint);
            if (type == AL || type == R) {
                return 1;
            }
            if (type == L) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * The bidi class of a codepoint, from the JDK's Unicode data.
     *
     * <p>This is the one place the port departs from its reference. {@code python-bidi} asks
     * {@code unicodedata.bidirectional}, which answers {@code ""} for a codepoint that has no assigned class, and
     * then trips its own assertion two passes later; the JDK answers {@link Character#DIRECTIONALITY_UNDEFINED} for
     * the same codepoint. Both are wrong to crash, so an undefined class is treated as other-neutral here, which is
     * what UAX&nbsp;#9 says an unassigned codepoint resolves to. The JDK also carries an older Unicode version than
     * CPython, which only shows up for codepoints assigned after it -- none of the Arabic, Latin, digit or
     * punctuation ranges a game string uses.
     */
    private static byte bidiType(int codepoint) {
        switch (Character.getDirectionality(codepoint)) {
            case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
                return L;
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
                return R;
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
                return AL;
            case Character.DIRECTIONALITY_EUROPEAN_NUMBER:
                return EN;
            case Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
                return ES;
            case Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR:
                return ET;
            case Character.DIRECTIONALITY_ARABIC_NUMBER:
                return AN;
            case Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
                return CS;
            case Character.DIRECTIONALITY_NONSPACING_MARK:
                return NSM;
            case Character.DIRECTIONALITY_BOUNDARY_NEUTRAL:
                return BN;
            case Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR:
                return B;
            case Character.DIRECTIONALITY_SEGMENT_SEPARATOR:
                return S;
            case Character.DIRECTIONALITY_WHITESPACE:
                return WS;
            case Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING:
                return LRE;
            case Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE:
                return LRO;
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
                return RLE;
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
                return RLO;
            case Character.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT:
                return PDF;
            default:
                return ON;
        }
    }

    /** X1-X9: apply the explicit embedding and override controls, then take them out of the text. */
    private static void explicitEmbeddingsAndOverrides(Storage storage) {
        int overflow = 0;
        int almostOverflow = 0;
        byte override = NO_OVERRIDE;
        Deque<Long> saved = new ArrayDeque<>();
        int level = storage.baseLevel;

        for (Item item : storage.items) {
            byte type = item.type;
            if (type == RLE || type == LRE || type == RLO || type == LRO) {
                // X2-X5: push the current state and enter the embedding, unless that would overflow the limit.
                if (overflow != 0) {
                    overflow++;
                    continue;
                }
                boolean rightward = type == RLE || type == RLO;
                int newLevel = rightward ? leastGreaterOdd(level) : leastGreaterEven(level);
                if (newLevel < EXPLICIT_LEVEL_LIMIT) {
                    saved.push((((long) level) << 32) | (override & 0xFFFFFFFFL));
                    level = newLevel;
                    override = type == RLO ? R : type == LRO ? L : NO_OVERRIDE;
                } else if (level == EXPLICIT_LEVEL_LIMIT - 2) {
                    almostOverflow++;
                } else {
                    overflow++;
                }
            } else if (type == PDF) {
                // X7
                if (overflow > 0) {
                    overflow--;
                } else if (almostOverflow > 0 && level != EXPLICIT_LEVEL_LIMIT - 1) {
                    almostOverflow--;
                } else if (!saved.isEmpty()) {
                    long state = saved.pop();
                    level = (int) (state >> 32);
                    override = (byte) (int) state;
                }
            } else if (type == B) {
                // X8: a paragraph separator resets the embedding state entirely.
                saved.clear();
                overflow = 0;
                almostOverflow = 0;
                item.level = storage.baseLevel;
                level = storage.baseLevel;
                override = NO_OVERRIDE;
            } else if (type != BN) {
                // X6: everything else takes the current level and any override in force. A boundary neutral is
                // ignored here and removed by X9.
                item.level = level;
                if (override != NO_OVERRIDE) {
                    item.type = override;
                }
            }
        }

        // X9
        storage.items.removeIf(item -> item.type == RLE || item.type == LRE || item.type == RLO || item.type == LRO
                || item.type == BN || item.type == PDF);
    }

    private static int leastGreaterOdd(int level) {
        return (level + 1) | 1;
    }

    private static int leastGreaterEven(int level) {
        return (level + 2) & ~1;
    }

    /** X10: split into runs of one level, each bracketed by the direction its boundaries resolve to. */
    private static void levelRuns(Storage storage) {
        storage.runs.clear();
        List<Item> items = storage.items;
        if (items.isEmpty()) {
            return;
        }
        byte sor = runDirection(storage.baseLevel, items.get(0).level);
        int start = 0;
        int length = 0;
        int previousLevel = items.get(0).level;

        for (Item item : items) {
            if (item.level == previousLevel) {
                length++;
            } else {
                byte eor = runDirection(previousLevel, item.level);
                storage.runs.add(new Run(sor, eor, start, length));
                sor = eor;
                start += length;
                length = 1;
            }
            previousLevel = item.level;
        }
        storage.runs.add(new Run(sor, runDirection(previousLevel, storage.baseLevel), start, length));
    }

    private static byte runDirection(int leftLevel, int rightLevel) {
        return (Math.max(leftLevel, rightLevel) & 1) == 0 ? L : R;
    }

    /** W1-W7: turn the weak types into strong ones and numbers, so the passes after this see only L, R, EN and AN. */
    private static void weakTypes(Storage storage) {
        for (Run run : storage.runs) {
            List<Item> chars = run.slice(storage);
            byte previousStrong = run.sor;
            byte previousType = run.sor;

            for (Item item : chars) {
                byte type = item.type;
                if (type == NSM) {
                    // W1
                    item.type = type = previousType;
                }
                if (type == EN && previousStrong == AL) {
                    // W2: a European number after Arabic text is an Arabic number.
                    item.type = AN;
                }
                if (type == R || type == L || type == AL) {
                    previousStrong = type;
                }
                previousType = item.type;
            }

            for (Item item : chars) {
                // W3
                if (item.type == AL) {
                    item.type = R;
                }
            }

            for (int index = 1; index < chars.size() - 1; index++) {
                // W4
                Item item = chars.get(index);
                byte before = chars.get(index - 1).type;
                byte after = chars.get(index + 1).type;
                if (item.type == ES && before == EN && after == EN) {
                    item.type = EN;
                }
                if (item.type == CS && before == after && (before == AN || before == EN)) {
                    item.type = before;
                }
            }

            for (int index = 0; index < chars.size(); index++) {
                // W5: terminators next to a European number join it.
                if (chars.get(index).type != EN) {
                    continue;
                }
                for (int at = index - 1; at >= 0 && chars.get(at).type == ET; at--) {
                    chars.get(at).type = EN;
                }
                for (int at = index + 1; at < chars.size() && chars.get(at).type == ET; at++) {
                    chars.get(at).type = EN;
                }
            }

            for (Item item : chars) {
                // W6
                if (item.type == ET || item.type == ES || item.type == CS) {
                    item.type = ON;
                }
            }

            previousStrong = run.sor;
            for (Item item : chars) {
                // W7: a European number after Latin text is Latin.
                if (item.type == EN && previousStrong == L) {
                    item.type = L;
                }
                if (item.type == L || item.type == R) {
                    previousStrong = item.type;
                }
            }
        }
    }

    /** N1/N2: neutrals take the direction of the strong text around them, or of the run they sit in. */
    private static void neutralTypes(Storage storage) {
        byte previousBidiType = ON;
        for (Run run : storage.runs) {
            List<Item> chars = new ArrayList<>();
            chars.add(new Item(0, run.sor));
            chars.addAll(run.slice(storage));
            chars.add(new Item(0, run.eor));

            Integer sequenceStart = null;
            for (int index = 0; index < chars.size(); index++) {
                byte type = chars.get(index).type;
                if (type == B || type == S || type == WS || type == ON) {
                    if (sequenceStart == null) {
                        sequenceStart = index;
                        previousBidiType = chars.get(index - 1).type;
                    }
                } else if (sequenceStart != null) {
                    byte nextBidiType = type;
                    if (previousBidiType == AN || previousBidiType == EN) {
                        previousBidiType = R;
                    }
                    if (nextBidiType == AN || nextBidiType == EN) {
                        nextBidiType = R;
                    }
                    for (int at = sequenceStart; at < index; at++) {
                        Item item = chars.get(at);
                        if (previousBidiType == nextBidiType) {
                            // N1
                            item.type = previousBidiType;
                        } else {
                            // N2
                            item.type = embeddingDirection(item.level);
                        }
                    }
                    sequenceStart = null;
                }
            }
        }
    }

    /** I1/I2: resolved types back into levels. */
    private static void implicitLevels(Storage storage) {
        for (Run run : storage.runs) {
            for (Item item : run.slice(storage)) {
                if (embeddingDirection(item.level) == L) {
                    if (item.type == R) {
                        item.level += 1;
                    } else if (item.type != L) {
                        item.level += 2;
                    }
                } else if (item.type != R) {
                    item.level += 1;
                }
            }
        }
    }

    /** L1/L2: reset trailing separators and whitespace, then reverse every run at or above each level. */
    private static void reorderLevels(Storage storage) {
        List<Item> items = storage.items;
        boolean shouldReset = true;
        for (int index = items.size() - 1; index >= 0; index--) {
            Item item = items.get(index);
            if (item.orig == B || item.orig == S) {
                item.level = storage.baseLevel;
                shouldReset = true;
            } else if (shouldReset && (item.orig == BN || item.orig == WS)) {
                item.level = storage.baseLevel;
            } else {
                shouldReset = false;
            }
        }

        int lineStart = 0;
        int highestLevel = 0;
        int lowestOddLevel = EXPLICIT_LEVEL_LIMIT;
        for (int index = 0; index < items.size(); index++) {
            Item item = items.get(index);
            if (item.level > highestLevel) {
                highestLevel = item.level;
            }
            if ((item.level & 1) == 1 && item.level < lowestOddLevel) {
                lowestOddLevel = item.level;
            }
            if (item.orig != B && index != items.size() - 1) {
                continue;
            }
            int lineEnd = item.orig == B ? index - 1 : index;
            reverseContiguous(items, lineStart, lineEnd, highestLevel, lowestOddLevel);
            lineStart = index + 1;
            highestLevel = 0;
            lowestOddLevel = EXPLICIT_LEVEL_LIMIT;
        }
    }

    private static void reverseContiguous(List<Item> items, int lineStart, int lineEnd, int highestLevel,
            int lowestOddLevel) {
        for (int level = highestLevel; level >= lowestOddLevel; level--) {
            Integer start = null;
            int end = 0;
            for (int index = lineStart; index <= lineEnd; index++) {
                if (items.get(index).level >= level) {
                    if (start == null) {
                        start = index;
                    }
                    end = index;
                } else if (start != null) {
                    reverse(items, start, end);
                    start = null;
                }
            }
            if (start != null) {
                reverse(items, start, end);
            }
        }
    }

    private static void reverse(List<Item> items, int from, int to) {
        int low = from;
        int high = to;
        while (low < high) {
            Item swap = items.get(low);
            items.set(low, items.get(high));
            items.set(high, swap);
            low++;
            high--;
        }
    }

    /** L4: a mirrored character in a right-to-left run is drawn as its mirror image. */
    private static void mirror(Storage storage) {
        for (Item item : storage.items) {
            if (embeddingDirection(item.level) == R) {
                item.codepoint = BidiMirroring.mirror(item.codepoint);
            }
        }
    }

    /** The direction an embedding level implies: even is left to right, odd is right to left. */
    private static byte embeddingDirection(int level) {
        return (level & 1) == 0 ? L : R;
    }

    /** One character on its way through the passes: the codepoint, its level, and its type before and after. */
    private static final class Item {
        private int codepoint;
        private int level;
        private byte type;
        private final byte orig;

        private Item(int codepoint, byte type) {
            this(codepoint, 0, type);
        }

        private Item(int codepoint, int level, byte type) {
            this.codepoint = codepoint;
            this.level = level;
            this.type = type;
            this.orig = type;
        }
    }

    /** A maximal run of one embedding level, with the directions its two boundaries resolve to. */
    private static final class Run {
        private final byte sor;
        private final byte eor;
        private final int start;
        private final int length;

        private Run(byte sor, byte eor, int start, int length) {
            this.sor = sor;
            this.eor = eor;
            this.start = start;
            this.length = length;
        }

        private List<Item> slice(Storage storage) {
            return storage.items.subList(start, start + length);
        }
    }

    /** The paragraph being resolved. */
    private static final class Storage {
        private final int baseLevel;
        private final List<Item> items = new ArrayList<>();
        private final List<Run> runs = new ArrayList<>();

        private Storage(int[] codepoints, int baseLevel) {
            this.baseLevel = baseLevel;
            for (int codepoint : codepoints) {
                items.add(new Item(codepoint, baseLevel, bidiType(codepoint)));
            }
        }

        private String display() {
            StringBuilder out = new StringBuilder(items.size() * 2);
            for (Item item : items) {
                out.appendCodePoint(item.codepoint);
            }
            return out.toString();
        }
    }
}
