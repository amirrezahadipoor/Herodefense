# Story Voice — Plain Language Rules

These rules govern every story line. Every new or rewritten line in `docs/STORY_CONTENT.md` must follow them.

## Core
- **Short lines.** Aim for ≤ 60 characters. Break a long thought into two lines. No line should need to wrap on phone.
- **Common words.** Use words a 12-year-old knows on first read. Prefer short, everyday words over rare or long ones.
- **One idea per line.** One sentence = one idea. No commas joining two thoughts when a period will do.

## Voices
- **Hero — terse, white.** Short. Present tense. Few words. Like a battle cry.
- **Tree — leaf-green, calm.** Slower, kind, but still plain. No hard words, no long asides.

## Two languages, one voice
The game ships every story line in **two languages**: simple, understandable English and Persian.
- The English is the source voice above; the Persian is a plain, native rendering of the same line, never a
  word-for-word calque.
- The Persian follows `docs/PERSIAN_PROOFREAD.md`: no Latin letters, no ASCII digits, the grove's own
  vocabulary «دره» (the Hollow), «دانشنامهٔ بیشه» (the codex), «چوب دل» (heartwood), «معجون» (potion).
- A player sees one language per run, chosen by the game's language setting. The story does not mix them.

## Checks
- Every rewritten line is checked at minimum density (smallest phone width) for overflow.
- Code and tests store the exact same wording as `STORY_CONTENT.md` — no drift.
- Numbers, unlock triggers, and Codex ids stay exactly as before. Only the words change.
- `docs/PERSIAN_PROOFREAD.md` records the Persian twin of every shipped string key, and the build fails if a
  shipped key is missing from it.

Revised 2026-09-20: the game is bilingual. Earlier drafts froze the story as English-only; that direction was
reversed by explicit requirement.
