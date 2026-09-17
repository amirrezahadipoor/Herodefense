#!/usr/bin/env python3
"""Generate the two lookup tables the Persian shaper needs, from the libraries that define them.

`render/PersianShaper.java` has to agree with `tools/i18n/golden/shaping-vectors.txt` on all 245 vectors, and
those vectors were produced by `arabic_reshaper` and `python-bidi`. Both libraries keep their knowledge in plain
data: a map from each Arabic letter to its four contextual forms, and a map from each mirrored character to the
glyph that replaces it in a right-to-left run. Copying that data by hand is how a shaper ends up subtly wrong on
one letter, so this script copies it instead -- the Java files it writes are generated artefacts and say so at the
top.

    python3 tools/i18n/make_shaping_tables.py            # write both tables
    python3 tools/i18n/make_shaping_tables.py --check    # fail if the committed tables differ

Requires: arabic-reshaper==3.0.1, python-bidi==0.6.11 (the versions named in the golden vectors' header).
"""

from __future__ import annotations

import argparse
import pathlib
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
RENDER = ROOT / "core/src/main/java/com/amirrezahadipoor/herodefense/render"
LETTER_FORMS = RENDER / "ArabicLetterForms.java"
LIGATURES = RENDER / "ArabicLigatures.java"
MIRRORING = RENDER / "BidiMirroring.java"

EXPECTED = {"arabic-reshaper": "3.0.1", "python-bidi": "0.6.11"}
BASE = 0x0621  # ARABIC LETTER HAMZA, the first key in LETTERS_ARABIC
LAST = 0x06D3  # ARABIC LETTER YEH BARREE WITH HAMZA ABOVE, the last
ZWJ = 0x200D


def versions() -> dict[str, str]:
    out = subprocess.run(
        [sys.executable, "-m", "pip", "list", "--format=freeze"],
        capture_output=True, text=True, check=True,
    ).stdout
    found = {}
    for line in out.splitlines():
        name, _, version = line.partition("==")
        if name in EXPECTED:
            found[name] = version
    for name, want in EXPECTED.items():
        have = found.get(name)
        if have != want:
            sys.exit(f"{name}: need {want} to generate these tables, found {have or 'nothing'}")
    return found


def letter_table() -> tuple[dict[int, tuple[int, int, int, int]], list[int]]:
    """codepoint -> (isolated, initial, medial, final); 0 where a form does not exist."""
    from arabic_reshaper.letters import LETTERS_ARABIC

    table: dict[int, tuple[int, int, int, int]] = {}
    for key, forms in LETTERS_ARABIC.items():
        cp = ord(key)
        if cp == ZWJ:
            continue  # handled on its own: it is not part of the contiguous Arabic block
        if not BASE <= cp <= LAST:
            sys.exit(f"letter {cp:#06x} falls outside the generated table's range")
        if len(forms) != 4:
            sys.exit(f"letter {cp:#06x} has {len(forms)} forms, expected 4")
        table[cp] = tuple(ord(form) if form else 0 for form in forms)  # type: ignore[assignment]
    missing = [cp for cp in range(BASE, LAST + 1) if cp not in table]
    return {cp: table.get(cp, (-1, -1, -1, -1)) for cp in range(BASE, LAST + 1)}, missing


def ligature_table() -> list[tuple[str, list[int], list[int]]]:
    """The ligatures the reshaper's default configuration actually enables, in alternation order.

    Order matters and is not cosmetic: the reshaper builds one regular expression out of these patterns joined by
    ``|``, and a regular expression alternation takes the first branch that matches at a position, not the longest
    one. ALLAH therefore has to come before LAM WITH ALEF, or "الله" would shape as a lam-alef ligature followed by
    a lam and a heh.
    """
    from arabic_reshaper import ArabicReshaper
    from arabic_reshaper.ligatures import LIGATURES

    reshaper = ArabicReshaper()
    enabled = [(name, match, forms) for name, (match, forms) in LIGATURES
               if reshaper.configuration.getboolean(name)]
    table = []
    for name, match, forms in enabled:
        codepoints = [ord(ch) for ch in match]
        shaped = [ord(forms[index]) if forms[index] else 0 for index in range(4)]
        table.append((name, codepoints, shaped))
    if not table:
        sys.exit("no ligatures enabled; the default configuration changed under us")
    return table


def mirror_table() -> list[tuple[int, int]]:
    from bidi.mirror import MIRRORED

    pairs = sorted((ord(src), ord(dst)) for src, dst in MIRRORED.items())
    for src, dst in pairs:
        if src > 0xFFFF or dst > 0xFFFF:
            sys.exit(f"mirroring pair {src:#x}->{dst:#x} is outside the BMP; the table stores chars")
    return pairs


def wrap(values: list[str], per_line: int, indent: str = "        ") -> str:
    lines = []
    for start in range(0, len(values), per_line):
        lines.append(indent + " ".join(values[start:start + per_line]))
    return "\n".join(lines)


def letter_forms_java(found: dict[str, str], missing: list[int]) -> str:
    table, _ = letter_table()
    flat: list[str] = []
    for cp in range(BASE, LAST + 1):
        flat.extend(f"{value}," for value in table[cp])
    return f"""package com.amirrezahadipoor.herodefense.render;

/**
 * The four contextual forms of every Arabic-script letter, as {{@code arabic_reshaper}} {found["arabic-reshaper"]}
 * defines them.
 *
 * <p><strong>Generated file</strong> -- written by {{@code tools/i18n/make_shaping_tables.py}}, which reads the table
 * out of the library. Do not edit it by hand; edit the generator. The same script's {{@code --check}} mode compares
 * the committed file against a freshly generated one, so a silent edit fails the build.
 *
 * <p>Arabic script is cursive: the same letter is drawn differently depending on whether it joins to the letter
 * before it and to the letter after it. Unicode stores those variants as separate codepoints in the Presentation
 * Forms blocks, and that is what a bitmap font renders -- libGDX's FreeType path has no Arabic shaper, so
 * {{@link PersianShaper}} picks the form and the font draws it.
 *
 * <p>The table is a flat array over the contiguous block {BASE:#06x}..{LAST:#06x}, four ints per codepoint in the
 * order isolated, initial, medial, final. A zero means the letter has no such form -- the right-joining-only
 * letters (alef, dal, ra, waw and the rest, {len(missing)} codepoints of the range in total have no entry at all)
 * simply cannot take an initial or a medial shape, and asking for one is a bug in the caller. A {{@code -1}} means
 * the codepoint is not a shaping letter: it is inside the block's range but the reshaper leaves it alone.
 *
 * <p>U+{ZWJ:04X} ZERO WIDTH JOINER is not in the block and is handled separately by {{@link PersianShaper}}: it has
 * all four forms and is itself the form, which is how it forces two letters to join before being dropped.
 */
final class ArabicLetterForms {{

    /** First codepoint covered by {{@link #FORMS}}. */
    static final int BASE = {BASE:#06x};

    /** Last codepoint covered by {{@link #FORMS}}. */
    static final int LAST = {LAST:#06x};

    /** Form that joins to the letter before it only. */
    static final int ISOLATED = 0;
    static final int INITIAL = 1;
    static final int MEDIAL = 2;
    static final int FINAL = 3;

    /** Not a shaping letter: inside the block's range, left as it is. */
    static final int NOT_A_LETTER = -1;

    private static final int[] FORMS = {{
{wrap(flat, 12)}
    }};

    private ArabicLetterForms() {{
    }}

    /** Whether this codepoint is one the reshaper knows a contextual form for. */
    static boolean isLetter(int codepoint) {{
        return inRange(codepoint) && FORMS[(codepoint - BASE) * 4] != NOT_A_LETTER;
    }}

    /**
     * The codepoint of {{@code form}} for {{@code codepoint}}, or 0 if the letter has no such form. Returns the
     * codepoint itself for anything outside the table, which is what "leave it alone" means here.
     */
    static int form(int codepoint, int form) {{
        if (!inRange(codepoint)) {{
            return codepoint;
        }}
        int value = FORMS[(codepoint - BASE) * 4 + form];
        return value == NOT_A_LETTER ? codepoint : value;
    }}

    /** Whether the letter can join to a letter before it, which is what a final or a medial form means. */
    static boolean joinsBackward(int codepoint) {{
        return isLetter(codepoint) && (has(codepoint, FINAL) || has(codepoint, MEDIAL));
    }}

    /** Whether the letter can join to a letter after it, which is what an initial or a medial form means. */
    static boolean joinsForward(int codepoint) {{
        return isLetter(codepoint) && (has(codepoint, INITIAL) || has(codepoint, MEDIAL));
    }}

    /** Whether the letter can join on both sides at once. */
    static boolean joinsBothWays(int codepoint) {{
        return isLetter(codepoint) && has(codepoint, MEDIAL);
    }}

    private static boolean has(int codepoint, int form) {{
        return FORMS[(codepoint - BASE) * 4 + form] > 0;
    }}

    private static boolean inRange(int codepoint) {{
        return codepoint >= BASE && codepoint <= LAST;
    }}
}}
"""


def ligatures_java(found: dict[str, str], table: list[tuple[str, list[int], list[int]]]) -> str:
    matches = ",\n".join(
        "        { " + ", ".join(f"0x{cp:04X}" for cp in cps) + " }"
        for _, cps, _ in table)
    forms = ",\n".join(
        "        { " + ", ".join(f"0x{cp:04X}" if cp else "0" for cp in shaped) + " }"
        for _, _, shaped in table)
    names = "\n".join(f" *   <li>{{@code {name}}}</li>" for name, _, _ in table)
    return f"""package com.amirrezahadipoor.herodefense.render;

/**
 * The ligatures {{@code arabic_reshaper}} {found["arabic-reshaper"]} substitutes by default, in the order its
 * regular expression tries them.
 *
 * <p><strong>Generated file</strong> -- written by {{@code tools/i18n/make_shaping_tables.py}} from the library's own
 * ligature list, filtered by its default configuration. Do not edit it by hand; edit the generator, and use its
 * {{@code --check}} mode in CI.
 *
 * <p>A ligature is several letters drawn as one glyph, and Arabic script has a few that are not optional: no Persian
 * font draws lam followed by alef as two separate letters. {len(table)} are enabled out of the
 * {{@code LIGATURES}} the library knows about:
 * <ol>
{names}
 * </ol>
 *
 * <p>The order is the alternation order of the reshaper's regular expression, so it is part of the data: at any
 * position the first pattern that matches wins, not the longest. The rial sign and the rest of the library's
 * ligatures are switched off in its default configuration and so are absent here -- matching the reference exactly
 * is the point, and {{@code PersianShaperTest}} holds both to the same 245 vectors.
 *
 * <p>Each entry carries four forms in the {{@link ArabicLetterForms}} order -- isolated, initial, medial, final --
 * and a zero means the ligature has no such form. That happens for every lam-alef pair in the initial and medial
 * positions, and it is not an omission: when a lam-alef would have to join on both sides, the reshaper leaves the
 * two letters separate instead of drawing a ligature.
 */
final class ArabicLigatures {{

    private static final int[][] MATCHES = {{
{matches}
    }};

    private static final int[][] FORMS = {{
{forms}
    }};

    private ArabicLigatures() {{
    }}

    /** How many ligatures are tried, in order, at each position. */
    static int count() {{
        return MATCHES.length;
    }}

    /** The codepoints of ligature {{@code index}}, in the order they appear in the text. */
    static int[] match(int index) {{
        return MATCHES[index];
    }}

    /**
     * The glyph for ligature {{@code index}} in the given {{@link ArabicLetterForms}} form, or 0 if it has no such
     * form -- in which case the letters are shaped separately.
     */
    static int form(int index, int form) {{
        return FORMS[index][form];
    }}
}}
"""


def mirroring_java(found: dict[str, str], pairs: list[tuple[int, int]]) -> str:
    packed = [f"{(src << 32) | dst}L," for src, dst in pairs]
    return f"""package com.amirrezahadipoor.herodefense.render;

/**
 * The characters that are drawn as their mirror image inside a right-to-left run, as {{@code python-bidi}}
 * {found["python-bidi"]} defines them.
 *
 * <p><strong>Generated file</strong> -- written by {{@code tools/i18n/make_shaping_tables.py}} from the library's own
 * map. Do not edit it by hand; edit the generator, and use its {{@code --check}} mode in CI.
 *
 * <p>Mirroring is Unicode's L4 rule and it is the reason "(موج 12)" reads with the parenthes on the correct side
 * once the run has been reversed: the opening parenthesis of a right-to-left run has to be drawn as a closing one.
 * {{@link BidiReordering}} applies this after it has resolved each character's direction, never before, and only to
 * characters whose resolved direction is right-to-left.
 *
 * <p>{len(pairs)} pairs, sorted by source codepoint and packed one per {{@code long}} -- source in the high half,
 * replacement in the low -- so the lookup is a binary search over a primitive array with no boxing and no map.
 */
final class BidiMirroring {{

    private static final long[] PAIRS = {{
{wrap(packed, 6)}
    }};

    private BidiMirroring() {{
    }}

    /** The mirrored form of {{@code codepoint}}, or the codepoint itself if it is not mirrored. */
    static int mirror(int codepoint) {{
        if (codepoint < 0 || codepoint > 0xFFFF) {{
            return codepoint;
        }}
        int low = 0;
        int high = PAIRS.length - 1;
        while (low <= high) {{
            int mid = (low + high) >>> 1;
            int source = (int) (PAIRS[mid] >>> 32);
            if (source == codepoint) {{
                return (int) PAIRS[mid];
            }}
            if (source < codepoint) {{
                low = mid + 1;
            }} else {{
                high = mid - 1;
            }}
        }}
        return codepoint;
    }}

    /** Whether this codepoint has a mirrored form at all, which Unicode calls the Bidi_Mirrored property. */
    static boolean isMirrored(int codepoint) {{
        return mirror(codepoint) != codepoint;
    }}
}}
"""


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="fail instead of writing, if the files differ")
    args = parser.parse_args()

    found = versions()
    table, missing = letter_table()
    filled = sum(1 for forms in table.values() if forms[0] != -1)
    ligatures = ligature_table()
    pairs = mirror_table()

    wanted = {
        LETTER_FORMS: letter_forms_java(found, missing),
        LIGATURES: ligatures_java(found, ligatures),
        MIRRORING: mirroring_java(found, pairs),
    }

    if args.check:
        stale = [path for path, text in wanted.items()
                 if not path.exists() or path.read_text(encoding="utf-8") != text]
        if stale:
            for path in stale:
                print(f"STALE {path.relative_to(ROOT)}")
            print("run: python3 tools/i18n/make_shaping_tables.py")
            return 1
        print(f"OK {len(wanted)} generated tables match "
              f"arabic-reshaper {found['arabic-reshaper']} + python-bidi {found['python-bidi']} "
              f"({filled} letters, {len(ligatures)} ligatures, {len(pairs)} mirrored pairs)")
        return 0

    for path, text in wanted.items():
        path.write_text(text, encoding="utf-8")
        print(f"wrote {path.relative_to(ROOT)}")
    print(f"{filled} letters over {LAST - BASE + 1} codepoints, "
          f"{len(ligatures)} ligatures, {len(pairs)} mirrored pairs")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
