# Story Voice — Plain Language Rules

These rules govern every story line. Every new or rewritten line in `docs/STORY_CONTENT.md` must follow them.

## Core
- **Short lines.** Aim for ≤ 60 characters. Break a long thought into two lines. No line should need to wrap on phone.
- **Common words.** Use words a 12-year-old knows on first read. Prefer short, everyday words over rare or long ones.
- **One idea per line.** One sentence = one idea. No commas joining two thoughts when a period will do.

## Voices
- **Hero — terse, white.** Short. Present tense. Few words. Like a battle cry.
- **Tree — leaf-green, calm.** Slower, kind, but still plain. No hard words, no long asides.

## One language
The game ships every story line in **English only**: simple, understandable English in the source voice above.
A second language is not a second column in a table — it is a second voice to keep, and this game keeps one.

## Checks
- Every rewritten line is checked at minimum density (smallest phone width) for overflow.
- Code and tests store the exact same wording as `STORY_CONTENT.md` — no drift.
- Numbers, unlock triggers, and Codex ids stay exactly as before. Only the words change.
- No story line carries a non-English script: `TranslationTableTest` fails the build on one.

Revised 2026-09-20: the game is bilingual. Earlier drafts froze the story as English-only; that direction was
reversed by explicit requirement.

Revised 2026-09-22: the full story (code + `STORY_CONTENT.md` + the proofread ledger) was rewritten for a
young reader — common words, short sentences, one spine: the waves are the nights, the Hollow is the night
speaking directly to the player, and two hundred nights later the dawn comes back. Tier 0 opening
lines remain the shipped text — do not rewrite them.

Revised 2026-09-23 (P2): the Persian translation is deleted by the owner's order — the game is English-only
again. The bilingual direction above is kept as history; the rules that remain are the source voice, one
language, and no drift between this file and the code.
