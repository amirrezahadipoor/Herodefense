# Story Voice — Plain Language Rules

These rules govern every story line. Every new or rewritten line in `docs/STORY_CONTENT.md` must follow them.
The taste behind them is `MEMORY.md` §2: a small hero, a loud little firefly, a warm grandma tree, and eight
ridiculous villains you secretly love — holding one light against the Night.

## Core
- **Short lines.** Every line ≤ 60 characters. A 10-year-old reads it cold. Break a long thought into two
  lines. No line should need to wrap on phone.
- **Common words.** Use words a 10-year-old knows on first read. Prefer short, everyday words over rare or
  long ones.
- **One idea per line.** One sentence = one idea. No commas joining two thoughts when a period will do.
- **Funny beats heartfelt beats cool.** The player should laugh at wave 5 and feel something at wave 200.

## Voices
- **Warden — terse, white.** Almost silent. Short. Present tense. Few words. When he talks, the run stops
  to listen. ("Grow brave, Twig." / "…We held.")
- **Granny — leaf-green, calm.** Warm, plain, counting every dawn like a gift. Soup, naps, counting,
  "dearie". No hard words, no long asides. ("Dawn, dearie! You did it! Soup for all!")
- **Hollow — red, deadpan.** The night itself, talking to the PLAYER, never the Warden. Dry one-liners:
  debts, inventories, floors that like you. ("You spared none. Cold. My inventory grows.")
- **Pip — gold, loud.** A firefly with a big mouth and a bigger heart. Shouts, counts everything twice,
  always makes it worse, always means well. ("Pip's got a plan!" / "Prisoner with snacks? Ask about snacks.")
- **Night Shift — ember, ridiculous.** Eight villains, each somebody with a want: Grum wants a nap, Mama
  wants to feed you, Sizzle wants applause, Blush wants a friend. Big talk before the wave; total
  silence once it starts. ("MUD! ON MY BOOTS! NOW YOU PAY!")

## Staging
- **Watch, then fight.** Comedy happens in watch-only cutscenes BEFORE the wave. Combat is sacred: no
  talking mid-fight, ever. Bosses speak only in their intro cutscene.
- **Characters, not narrators.** Everyone on screen is somebody with a want. The bosses arc from first
  meeting to farewell across their five encounters (meet → rival → respect → farewell).
- **Progression you can hug.** Saplings with names. A scarf from Mama. A card from Blush. Letters from
  Granny. The grove fills up with people who know your name.

## One language
The game ships every story line in **English only**: simple, understandable English in the source voice above.
A second language is not a second column in a table — it is a second voice to keep, and this game keeps one.

## Checks
- Every rewritten line is checked at minimum density (smallest phone width) for overflow.
- `StoryLineLengthTest` fails the build on any `StoryStrings` entry over 60 characters.
- Code and tests store the exact same wording as `STORY_CONTENT.md` — no drift.
- Numbers, unlock triggers, and Codex ids stay exactly as before. Only the words change.
- No story line carries a non-English script: `TranslationTableTest` fails the build on one.
- Read every new line OUT LOUD. If it doesn't make you smile, rewrite it.

Revised 2026-09-20: the game is bilingual. Earlier drafts froze the story as English-only; that direction was
reversed by explicit requirement.

Revised 2026-09-22: the full story (code + `STORY_CONTENT.md` + the proofread ledger) was rewritten for a
young reader — common words, short sentences, one spine: the waves are the nights, the Hollow is the night
speaking directly to the player, and two hundred nights later the dawn comes back. Tier 0 opening
lines remain the shipped text — do not rewrite them.

Revised 2026-09-23 (P2): the Persian translation is deleted by the owner's order — the game is English-only
again. The bilingual direction above is kept as history; the rules that remain are the source voice, one
language, and no drift between this file and the code.

Revised 2026-09-23 (P3): the whole story is deleted and rebuilt — new cast (Warden, Granny, Pip, Hollow,
Night Shift), five voices, watch-only boss intros, the silence rule. The pillars above replace the old
two-voice section; the ≤60-char + simple-words checks stay exactly as strict.
