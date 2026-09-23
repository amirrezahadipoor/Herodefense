# MEMORY — Hero Defense: the new story, and how we finish it

> **What this file is:** the single source of truth for the rebuild ordered by the owner on
> 2026-09-23. New story bible, new cast, the boss-intro cutscene design, longer waves, the
> English-only decision, the full dialogue draft, the implementation plan push-by-push, and the
> progress log. **Every agent that works on this repo reads this file first and updates the
> status section at the top before pushing.** If you ever forget where we are — you are here.

---

## 0. WHERE WE ARE (agents: keep this fresh)

| Field | Value |
|---|---|
| **Status** | `P2 DONE` — game is English-only, pushed. Next: `P3` story rebuild (delete old story, write new). |
| **Last push** | `P2` — Persian translation, shaper, fonts, language row + settings persistence deleted; 14 tests rewritten, audit migrated. |
| **Branch** | `main` (fast pushes, one idea per commit, push immediately). |
| **Build** | CI `test-core` must stay green on every push. It is the verifier. |
| **Owner's orders (2026-09-23)** | ① Delete Persian + all Persian translation. ② Delete the whole story, rebuild from zero with brainstorming: a beautiful story in simple words, new characters. ③ Longer waves. ④ Bosses must NEVER talk mid-fight. ⑤ Every boss wave opens with a watch-only cutscene (like the planting ceremony): the boss walks in, does funny trash-talk, walks back out — THEN the wave starts. ⑥ This MEMORY file tracks everything to the end. ⑦ Fast pushes; each push reports what was done and what remains. ⑧ No machine-garbage-soulless stuff. Full creative freedom. |

### Push log (append-only)

- **P1 (2026-09-23)** — `MEMORY.md` created: story bible, cast, chapter spine 1–200, boss-intro
  cutscene spec, longer-waves spec, English-only file catalog, full dialogue draft, P1–P8 plan.
  Done: the whole creative + technical plan, reviewed against the real code (574 Java files).
  Remaining: P2–P8 (code). Build: docs-only, CI unaffected.
- **P2 (2026-09-23)** — English-only strip, pushed. Done: 13 string tables stripped to
  English (420 entries, byte-identical EN side); `GameLanguage`/`Translated`/`GameNumbers`/
  `GameLocale` single-language; `LoreCatalog`/`BossLore`/`TreeLetters`/`LoreEntry` prose
  stripped (48 codex entries kept); settings language row + persistence deleted (10 rows,
  reduced-motion moved up); `GameFonts` Nunito-only; `OverlayText.visual` identity;
  **deleted** `PersianShaper`/`Bidi*`/`Arabic*`, `tools/i18n`, golden vectors, Vazirmatn
  files, `docs/PERSIAN_PROOFREAD.md`, 2 tests; 14 tests rewritten single-language; audit
  `persian` flag honestly False (was: 12 evidence lines); CI R7.3 shaping steps removed;
  `docs/STORY_VOICE.md` rewritten English-only. Verified: i18n+story javac compile,
  runtime smoke (0 Arabic codepoints in shipped text), 14/14 audit tests.
  Remaining: P3–P8. Watch: `UiMirror`/`GameLocale.rightToLeft` dormant LTR branches kept
  (audit `rightToLeft` evidence); `DrawnStringProvenanceTest` ratchet untouched.

### Session log (append-only, one line per work session)

- **2026-09-23 / session 1** — explored repo, mapped i18n/story/wave/cinematic systems, wrote
  MEMORY.md, pushed P1. Next session: P2 (English-only strip per §8).
- **2026-09-23 / session 2** — P2 done + pushed: full English-only strip per §8 (with one
  plan change: §12.4 dead-infra is DELETED, not kept — the audit's case-insensitive
  `persian` flag forced an honest zero). Verified by compile + smoke + audit; CI is the
  final word. Next: P3 story rebuild (new cast + dialogue per §9).

---

## ۱. خلاصه برای مالک (Persian summary — dev doc only, the GAME is English-only)

- داستان قبلی کامل حذف می‌شود. داستان جدید: ساده، کوتاه، بامزه، پُر از احساس.
- شخصیت‌های جدید: **Pip** (کرم شب‌تاب کوچولو، بامزه، بهترین دوست قهرمان)، **Granny**
  (درخت جهان، مادربزرگ مهربان)، سه نهال **Sprout / Twig / Leaf**، و تیم هشت‌نفره‌ی باس‌ها
  به اسم **The Night Shift** (شیفت شب) — هر کدام شخصیت طنز خودشان را دارند.
- هر Wave طولانی‌تر می‌شود: دشمن‌ها در چند موج کوچک می‌آیند + Waveهای باس نگهبان هم دارند.
- **وسط مبارزه هیچ‌کس حرف نمی‌زند.** قبل از هر Wave باس، یک صحنه‌ی سینمایی فقط-تماشایی
  پخش می‌شود (مثل صحنه‌ی کاشت درخت): باس وارد می‌شود، کل‌کل خنده‌دار می‌کند، برمی‌گردد،
  بعد مبارزه شروع می‌شود. هر باس ۵ بار در هر Run دیده می‌شود و هر بار lines جدید دارد
  (از «تو کی هستی؟» تا خداحافظی دوستانه).
- فارسی و ترجمه‌ی فارسی به‌طور کامل از بازی حذف می‌شود (تک‌زبانه‌ی انگلیسی).
- هر پوش سریع انجام می‌شود و گزارش «انجام شد / مانده» داده می‌شود. این فایل حافظه‌ی کار است.

---

## 2. BRAINSTORM (why the old story died, what the new one feels like)

**What was wrong.** The old story was three gray voices (Warden / Tree / Hollow) reciting
cryptic fortune-cookie lines while the player was busy dodging. Bosses were furniture with
health bars. Persian + English doubled every string and halved the soul. Agents polished
sentences; nobody gave the game a *friend*.

**The new feeling (one line):** *a small hero, a loud little firefly, a warm grandma tree,
and eight ridiculous villains you secretly love — holding one light against the Night.*

**Pillars (taste rules, not prose rules):**

1. **Simple words, short lines.** Every line ≤ 60 chars. A 10-year-old reads it cold.
   One idea per line. Funny beats heartfelt beats cool.
2. **Characters, not narrators.** Everyone on screen is somebody with a want: Grum wants a
   nap, Mama wants to feed you, Sizzle wants applause, Blush wants a friend. Pip wants to
   help and always makes it worse. The Warden wants to hold the line and says almost nothing.
3. **The bosses are the story.** 40 boss encounters = 40 little comedy sketches with a slow
   friendship arc underneath (meet → rival → respect → farewell). The player should LAUGH
   at wave 5 and feel something at wave 200.
4. **Watch, then fight.** Comedy happens in watch-only cutscenes BEFORE the wave. Combat is
   sacred: no talking mid-fight, ever. Title cards move into the intros.
5. **Progression you can hug.** Saplings with names. A scarf from Mama. A card from Blush.
   Letters from Granny. The grove fills up with people who know your name.
6. **English-only.** One language, one voice, zero duplication. (Owner order; see §8.
   Flagged: this drops the Cafe-Bazaar/IR-market angle — deliberate, reversible via git.)

**Names we keep from the old code (assets/audio depend on them):** `BossType` ids
(ANCIENT_GOLEM … BLOODROOT_AVATAR), the Hollow, the Warden, World Tree, wave numbers,
color arc (DAWN 1–50, AMBER 51–100, TEAL 101–150, HOLLOW 151–200). Everything else —
every word — is new.

---

## 3. STORY BIBLE

### 3.1 Premise (internal, never shown verbatim)

Long ago the Night (the **Hollow**) swallowed every light but one: one grove, one great
Tree. You are the smallest Warden yet — a kid with a bow. On your shoulder rides **Pip**,
a lantern-firefly with a big mouth and a bigger heart. The Tree — **Granny** — has three
seeds left and a lot of soup. The Hollow, too lazy to fight fair, hired help: eight odd
bosses called **the Night Shift**. Every fifth night, one of them clocks in. They talk big.
You bonk them. All 200 nights. Then dawn.

### 3.2 Cast (the new characters)

| # | Character | Role | Voice | Color | Notes |
|---|---|---|---|---|---|
| 1 | **The Warden ("Chief")** | You. Nearly silent hero. | Terse. 1–4 words. | White | Kids project onto silence. Speaks at plantings + the final line. Pip calls you **Chief**. |
| 2 | **Pip** | Firefly sidekick, comedy + heart. | Loud, fast, jokes, catchphrase *"Pip's got a plan!"* (plans fail; you save the day). | Lantern-yellow | Shoulder-rider. Does all trash-talk comebacks. Whispers around Old Page. Cries (happy) at wave 200. |
| 3 | **Granny (World Tree)** | Warm grandma. Quest-giver of seeds. | Kind, simple, funny-grandma (*"Eat your sunlight."*). | Leaf-green | Writes letters (TreeLetters system, new words). Names the saplings. Victory line + daily gift. |
| 4 | **The Hollow (the Night)** | Big bad. Deadpan drama king. | Short, dry, secretly fond. Never shouts. | Cold violet | Chapter cards, verdicts, death lines, mercy lines. Never speaks mid-fight. Final truth: the Night was never mad — just Night. |
| 5 | **Grum (Ancient Golem)** | Night Shift Security. | Sleepy, slow, yawns mid-threat. | Stone gray | Hasn't moved in 100 years. Threatens to SIT on you. Falls asleep in intros. M5: asks you to wake him at dawn. |
| 6 | **Mama Bramble (Thorn Matriarch)** | Smother-mother of monsters. | Scolding + feeding. | Berry red | Grew half the enemies ("my babies"). Offers moving pie. Knits you a (thorny) scarf. |
| 7 | **Sizzle (Ember Wyrm)** | Washed-up rockstar lizard. | Vain, loud, showbiz. | Ember orange | "SIZZLE IS IN THE BUILDING!" Bad left side. Terrified of water jokes. Wants a duet by M4. |
| 8 | **Sir Falls-a-Lot (Void Knight)** | Polite, catastrophically clumsy. | Over-apologetic chivalry. | Void purple | Trips EVERY entrance (scripted pratfall, running gag). Polishes armor for you (M4). |
| 9 | **Big Chill (Frost Titan)** | Surfer-dude ice giant. | "Whoa", "cool", "no stress". | Ice blue | Evil or on holiday? Unclear. Freezes you SOFT. Offers ice. Best bad guy. |
| 10 | **Old Page (Shadow Lich)** | 900-year-old librarian. | SHUSHES. Fines. Stamps. | Dusty teal | "Your card is 200 years late!" Writes a book about you (M3). Final stamp: READER OF THE YEAR. |
| 11 | **Captain Thunder (Storm Colossus)** | Loud captain of a rock-ship. | Pirate-brag, huge volume. | Storm gold | "I AM THE STORM!" Secret: terrified of MUD on his boots. Offers first-mate job (M4–M5). |
| 12 | **Blush (Bloodroot Avatar)** | Shy forest wound. Final boss (w200). | Tiny, stammering, apologetic. | Soft rose | "S-sorry. ...Bonk time. Sorry." Makes you a card (M3). Asks to be friends AFTER (M5). Cutest threat ever. |
| 13–15 | **Sprout / Twig / Leaf** | The three saplings (w50/100/150). | Loud baby / dramatic poet / sleepy. | Fresh green | Progression mascots. Leaf naps on Grum (cross-gag in Grum M4+). |

**Why this cast works:** 3 allies + 1 big bad + 8 comedy villains + 3 mascots = every system
(opening, chapters, intros, plantings, letters, whispers, epilogues) has a *face*. No more
floating aphorisms.

### 3.3 The spine: 200 nights, 4 chapters (rides the shipped color arc)

- **Ch.1 FIRST LIGHT (1–50, DAWN).** Pip meets you (w1). Meet the whole Night Shift, M1 each
  (w5–w40) + Grum & Mama rematches (w45, w50). Plant **Sprout** after w50 (short ceremony).
  Feeling: funny, fast, "who ARE these guys?"
- **Ch.2 AMBER HOUR (51–100, AMBER).** Rematches M2 + M3 begin; running gags deepen (Sir's
  falls, Sizzle's one fan, Page's book). Plant **Twig** after w100 (FULL ceremony — the big
  one). Hollow: "Halfway. Cute tree. I am still here." Feeling: warm routine, first respect.
- **Ch.3 THE LONG DARK (101–150, TEAL).** M3–M4; cracks of friendship (Grum's praise, Mama's
  scarf, Page's smile). Plant **Leaf** after w150 (short). Feeling: the long night, but the
  grove is full now — 4 trees, full hearts.
- **Ch.4 HOLD THE DAWN (151–200, HOLLOW).** M4–M5 farewell tour; every boss says goodbye in
  their own way; **Blush at wave 200** asks to be friends after. Dawn breaks (shipped sunrise
  VFX). Granny: soup for all. Pip cries. You say: *"…We held."* Feeling: laugh + lump in
  throat. Then Ascend (next dawn, Tier+1 openings).

### 3.4 Boss schedule (code truth: `bossNumber = wave/5`, identity = `types[(bossNumber-1) % 8]`)

| Meeting | Grum | Mama | Sizzle | Sir | Chill | Page | Cap | Blush |
|---|---|---|---|---|---|---|---|---|
| **M1** full intro | w5 | w10 | w15 | w20 | w25 | w30 | w35 | w40 |
| **M2** "you again" | w45 | w50 | w55 | w60 | w65 | w70 | w75 | w80 |
| **M3** running gag | w85 | w90 | w95 | w100 | w105 | w110 | w115 | w120 |
| **M4** respect | w125 | w130 | w135 | w140 | w145 | w150 | w155 | w160 |
| **M5** farewell | w165 | w170 | w175 | w180 | w185 | w190 | w195 | w200 |

Meeting number needs NO new persistence: `meeting = (bossNumber - 1) / 8 + 1`. Old saves
just work. (Boss waves colliding with plantings — w50, w100, w150 — already flow correctly:
boss → reward card → ceremony → next wave. Verified in `ContinuousWaveRun`.)

### 3.5 What gets deleted from the old story (P3)

- All `StoryStrings` words (table shape stays, content replaced, plus new keys).
- Hollow half-health mid-fight beats (`BossBeats`, `HOLLOW_BOSS_*`) — the silence rule (§6).
- Old openings, reflections, ceremony lines, epilogues, codex entries, elite fragments,
  mythic flavors, whispers, Tree letters, Hollow lines, deeds words (functional shape stays).
- `docs/STORY_CONTENT.md` full rewrite; `docs/STORY_VOICE.md` rewrite (this file's §2 pillars
  replace it; keep the ≤60-char + simple-words checks).
- Old agents' "do not rewrite Tier 0" pin: **overridden by owner** — everything is new.

---

## 4. BOSS-INTRO CUTSCENE SPEC (the key feature — P4)

**Owner's words, engineered:** before every boss wave, the game enters a watch-only
cinematic (same contract as the planting ceremony: combat frozen, player can only watch,
tap skips). The boss walks in from its lane, camera pushes, boss does 2–4 funny trash-talk
lines in the dialogue box (boss voice blips), Pip fires back one line, the boss walks back
out — THEN the wave spawns and the fight starts. No talking once combat begins.

### 4.1 Timeline (`BossIntroCinematic`, deterministic like `PlantingCeremony`)

| Phase | Time | What happens |
|---|---|---|
| ENTER | ~1.2s | Boss prop walks lane-edge → mark. Dust particles. Camera pushes (like opening). Boss SFX. |
| TITLE | ~1.6s | Name plate slams in: "GRUM — Night Shift Security. Do not wake." (M1 only; repeats skip to TALK with a small plate.) |
| TALK_1..N | ~2.0s each | Lines type in the sticky dialogue box, boss voice. N=4 for M1, N=2 for M2–M5. |
| COMEBACK | ~1.8s | Pip's reply (yellow, Pip voice). M1 also: Warden's 1 terse line? No — Warden stays silent in intros (rule: Warden speaks only at plantings + finale). Pip only. |
| EXIT | ~0.9s | Boss walks back to lane edge (Sir Falls-a-Lot TRIPS here on M2+ — scripted). |
| DONE | — | Hand-off: spawn boss wave (+ escorts, §5), `PLAYING`, boss-entrance shake+particles (no beat text). |

Total ≈ 9–11s (M1) / ≈ 6–7s (repeats). Skippable by tap (consistent with all cinematics).
Reloading mid-intro replays it from the start (pure function of elapsed time, like planting).

### 4.2 Wiring (files)

- **NEW** `gameplay/BossIntroCinematic.java` — timeline + phases + skip + line schedule.
- **NEW** `story/BossIntros.java` — `linesFor(bossType, meeting)` + `comebackFor(...)` + `titleFor(...)`
  (reads the new `StoryStrings` keys; meeting derived from wave, §3.4).
- `gameplay/WaveLifecycleSystem.startCurrentWave` — on boss waves: set
  `state.bossIntroPending = true` (+ `bossIntroWave`), do NOT spawn yet, return.
- `gameplay/WaveCompletion` — new value `BOSS_INTRO`.
- `model/GameState` — `bossIntroPending`, `bossIntroWave` (+ save/load round-trip).
- `gameplay/CinematicFlow` — `beginBossIntro()` + update branch (mirror planting; spawns the
  boss as a *prop* at begin so the renderer draws it; on DONE calls the spawner for real +
  escorts, transitions to PLAYING). Boss prop = real entity, combat frozen because CINEMATIC
  never ticks combat (verify in `HeroDefenseGame` — planting already relies on this).
- `presentation/RunPresentationSystem.presentBossEntrance` — keep particles + shake, DROP the
  `showBeat(titleCard)` (cards live in intros now). Keep `claimFirstUnencountered` ledger for
  codex unlocks (unlock silently).
- `presentation/RunPresentationSystem.presentPlaytime` — **delete the half-beat call** (§6).
- Input: CINEMATIC tap = skip already exists — verify it routes to boss intro too.
- Render: reuse boss rendering (prop is in `aliveBosses`); add camera-push factor read from
  `BossIntroCinematic` (mirror `OpeningCinematic` zoom); name-plate draw (P7 styling).
- Audio: **NEW** `SpeechVoice.PIP` + `SpeechVoice.BOSS` (+ blips in `SpeechBlip`:
  Pip = high chirp, Boss = low square; per-boss pitch offset optional P7).
- Tests: `BossIntroCinematicTest` (phases/timing/skip/lines), `BossIntrosTest` (every
  identity × M1–M5 has lines; meeting math), `CinematicFlowBossIntroTest` (hand-off spawns),
  `WaveLifecycleBossIntroTest` (gate), update `RunPresentationSystem` tests (no beat on
  entrance), input test (tap skips intro).

### 4.3 Line budget (all drafted in §9 — this file is the script source)

Per boss: M1 = title + 4 boss lines + 1 Pip comeback; M2–M5 = 2 boss lines + 1 Pip each.
8 bosses × (5 + 12) ≈ **136 intro lines**. Plus openings/chapters/plantings/epilogues/Hollow/
whispers/deeds/mythics/elites ≈ **~230 new lines total**. All ≤ 60 chars, simple words.

---

## 5. LONGER WAVES SPEC (P6)

**Owner: every wave should feel longer.** Measured truth today: regular waves spawn
`min(24, max(3, 4 + wave/2))` bodies ALL AT ONCE (wave 40+ already caps at 24); boss waves
spawn ONE boss and nothing else (short!). So "longer" = pacing + escorts, not just bodies:

1. **Regular waves — 3 trickles.** Same body count curve (rebalanced, see 4), arriving in
   pulses: 60% at start → +25% when half cleared or +8s → +15% "final push" with horn SFX.
   New: `EnemyWaveSpawner.planTrickles(wave)` (deterministic) + `WaveLifecycleSystem` mid-wave
   reinforcement check. Waves feel 30–50% longer with zero new art.
2. **Boss waves — escorts.** Boss + `(4 + wave/25)` minions in 2 trickles (at spawn + at
   boss 66% HP — HP-gated, NOT dialogue-gated; VFX ring already exists via evolution system).
   Boss HP +0–15% by lap (measured, not guessed).
3. **Breather beats.** After clearing milestone waves (25/50/…), 1.2s pause with Pip's line
   (§9) before next wave — longer *and* warmer.
4. **Balance honesty (keep the RULES.md gate ethic).** Retune via `BalanceSimulator`, update
   `docs/BALANCE.md` + gate tests with MEASURED numbers. Never weaken a gate to fit the code;
   change the code until the gates pass, then record. The old roadmap's ≥900 rubric is dead
   (owner deleted the roadmap direction); the honest-gates habit is not.
5. **Cap safety.** Arena ceiling `MAX_REGULAR_ENEMIES` 24 → 28 for waves 120+ (perf check on
   the emulator journey in CI; rollback to 24 if `device-evidence` complains).

---

## 6. THE SILENCE RULE (mid-fight — P5)

- **No dialogue during combat. Ever.** Bosses, Hollow, Pip, Warden: silent while `PLAYING`
  with living enemies. (Deeds = payment announcements, not dialogue: they still PAY always,
  but their overlay is suppressed while any boss lives and shown on the next boss-free frame.)
- Concretely: remove `presentBossHalfBeat` from `presentPlaytime`; delete `story/BossBeats`
  + `HOLLOW_BOSS_*` keys in P3; keep the evolution VFX ring (visual, not words).
- Title cards: M1 card shows INSIDE the intro (TITLE phase), never over combat.
- Tests currently asserting half-beats get rewritten to assert SILENCE
  (`presentPlaytime` with a wounded boss shows nothing) — a stronger test, not a weaker one.

---

## 7. UI/UX PASS (P7 — feel, not features)

1. **Speaker colors in the dialogue box:** Warden white / Pip lantern-yellow / Granny
   leaf-green / Hollow cold violet / bosses per-identity tint (reuse telegraph RGB!).
   Speaker label = name ("PIP", "GRUM"…). (Renderer reads `SpeechVoice`; add PIP/BOSS.)
2. **Boss name plate:** slams in during TITLE phase (big name + funny subtitle from §9).
3. **"WATCH • tap to skip" chip** during all CINEMATICs (eye icon, existing atlas if possible,
   else text chip). Intros must READ as watch-only.
4. **Chapter cards:** full-screen 1.6s card on w1/51/101/151 (title + Hollow deadpan line).
5. **Menu subtitle:** "Pip & the Night Shift" flavor line under the title (new game, new joke).
6. **Codex:** entries keep unlock logic; P3 replaces words; tab order LORE first (already?).
7. **A11y:** new voices get distinct blips (§4.2); reduced-motion path keeps intros (they're
   dialogue, not motion) but disables camera push + shake.
8. **No new art required** for P1–P6; P7 allows atlas-only additions if the chip needs an icon.

---

## 8. ENGLISH-ONLY DECISION (P2 — file catalog)

**Owner order: Persian + all Persian translation deleted entirely. The game ships one
language.** Design: keep the *shapes* (`GameLanguage`, `GameLocale`, `Translated`,
`GameNumbers` signatures) so call sites keep compiling; delete the *content*.

### 8.1 Main-code changes

| File | Change |
|---|---|
| `i18n/GameLanguage.java` | **Rewrite.** Single value `ENGLISH`. `fromCode()` → always ENGLISH (old `"fa"` saves fall back, never crash). `forSystemLocale()` → ENGLISH. `next()` → self. Keep `code()/locale()/rightToLeft()`(=false). |
| `i18n/Translated.java` | **Rewrite.** Remove `persian()`. `text(lang)` → `english()` (param kept for call-site compat, documented). Keep `text(lang, args)` formatting. |
| `i18n/GameNumbers.java` | **Rewrite.** Ignore `language` param (always `Locale.US` symbols). Keep all signatures. |
| `i18n/GameLocale.java` | Javadoc touch only (still the current-language holder; current is always ENGLISH). |
| `i18n/*Strings.java` (13 enums) | **Script-strip** the 2nd constructor arg everywhere (`NAME("en", "fa")` → `NAME("en")`), single-arg ctor, delete `persian` field/assign/method. `SettingsStrings`: ALSO delete `LANGUAGE*` entries (§8.2). |
| `render/GameFonts.java` | Remove Vazirmatn paths/generators, `PERSIAN_CHARACTERS`, `persianCharacters()`. Face always Nunito. |
| `story/LoreEntry.java`, `LoreCatalog.java`, `BossLore.java`, `TreeLetters.java` | Remove Persian fields/methods (`persianText()` etc.). Words replaced in P3 anyway. |
| `settings/GameSettings.java` | Remove `language` field (always-English; loader maps old `"fa"` → ENGLISH). |
| `settings/LocalSettingsRepository.java` | Remove language persistence (keep class; unknown/legacy codes ignored → ENGLISH). |
| `input/SettingsTouchLayout.java` | Remove `LANGUAGE_ROW_Y` (+ shift rows below up; keep hit-test order). |
| `input/SettingsTouchController.java` | Remove `Action.CYCLE_LANGUAGE` handling. |
| `render/SettingsOverlayRenderer.java` | Remove language row + `drawLanguage`. |
| `progression/Trophy*.java` | Remove Persian title/hint halves (read files in P2; mirror the Strings strip). |
| Any other `.persian()` callers | `grep -rn "\.persian()" core/src/main` in P2 and fix all (known: onboarding? combat renderers? — verify). |
| `render/OverlayText.java`, `PersianShaper`, `BidiReordering` | **KEPT** (retained infra, zero user-visible Persian; RTL branch never triggers). Documented, not deleted — deleting risks the text pipeline for zero player gain. Revisit in P8 if time. |
| `tools/i18n/*` | KEPT (shaper goldens for the kept tests). |

### 8.2 Settings-screen consequence

The language row disappears from Settings (a 1-language row is silly UX). Rows below shift
up; `SettingsTextFitTest` + touch tests updated. Old saves carrying `"fa"` load as English.

### 8.3 Tests (rewrite/delete — adapt, never weaken)

| Test | Fate |
|---|---|
| `i18n/PersianProofreadRecordTest` | **DELETE** (ledger deleted). |
| `i18n/TranslationTableTest` | **Rewrite:** keep table-completeness sweep; English non-blank; placeholders well-formed (single-language); formatting never throws; no duplicates. Drop all Persian asserts. |
| `i18n/GameLanguageTest` | **Rewrite:** single language; `fromCode`/`next`/`forSystemLocale` DELETED (no legacy API at all); no RTL; locale US. Legacy `"fa"` prefs are ignored, never read (pinned by `LanguageSettingsTest`). |
| `i18n/GameLocaleTest` | **Rewrite:** English only; null-use keeps ENGLISH. |
| `i18n/GameNumbersTest` | **Rewrite:** English shapes only (`1,234`, `50%`, `+5`, `1.2k`). |
| `settings/LanguageSettingsTest` | **Rewrite** (keep filename): game always speaks English; legacy `"fa"` pref → ENGLISH; no CYCLE_LANGUAGE action exists. |
| `render/GameFontsTest` | Drop Persian-glyph + Vazirmatn-license asserts; keep English coverage gate. |
| `render/HudRendererMirrorTest`, `render/UiMirrorTest` | **Rewrite:** assert NO mirroring (leading edge always left). Stronger, not weaker. |
| `render/SettingsTextFitTest` | Drop Persian length checks + `LANGUAGE_SUBTITLE` (entry deleted). |
| `render/OverlayTextTest` | Drop Persian shaping cases; keep Latin/bidi-neutral cases. |
| `render/ReferenceTypeMeasure` | Keep working (face always Nunito; check callers). |
| `input/HudTouchLayoutTest`, `input/RootNetworkTouchControllerTest` | Drop Persian-mirror cases; keep English hit-tests. |
| `input/MainMenuAndSettingsTouchTest` | Remove LANGUAGE_ROW cases; keep the rest. |
| `items/EquipmentSetBonusTest` | Drop Persian-placeholder parity loop. |
| `onboarding/OnboardingClaimsTest` | Drop Persian claim lists; keep English aim-claim checks. |
| `polish/FloatingDamageTextSystemTest` | Drop Persian expectations; keep English. |
| `progression/TrophyTextTest` | Drop Persian halves; keep English non-blank + pinned first-12. |
| `story/EpilogueTest` | Drop Persian test; keep the rest. |
| `render/PersianShaperTest` | **KEEP** (infra kept, §8.1). |

### 8.4 Docs / assets / workflows

- **DELETE** `docs/PERSIAN_PROOFREAD.md` (551 lines of what we no longer ship).
- **DELETE** `android/assets/fonts/Vazirmatn-Bold.ttf`, `Vazirmatn-ExtraBold.ttf`,
  `VAZIRMATN-OFL.txt` (verified: NOT in `asset_manifest.json`, NOT in `asset_hashes.json` —
  safe; re-verify with grep in P2).
- **Rewrite** `docs/STORY_VOICE.md` (short: English-only + §2 pillars + ≤60-char check).
- `docs/STORY_CONTENT.md`: header-note in P2, **full rewrite in P3**.
- `docs/ROADMAP.md`: **DO NOT TOUCH** (audit tooling counts it; the old roadmap is frozen
  history, MEMORY.md is the new plan — noted here, not there).
- `.github/workflows`: grep for fa/Persian in P2 (Cafe-Bazaar workflow may mention Persian
  store text — flag to owner, don't break the release lane).
- `store-assets/`: check for Persian screenshots/text in P2; flag, don't nuke blindly.

### 8.5 PMD/SpotBugs warning (P2's #1 footgun)

`ciStaticAnalysis` runs PMD + SpotBugs: **every removed reference must take its import with
it.** The strip script must delete now-unused imports (`GameLanguage` where unused, etc.)
or CI goes red on style, not logic. Verify by grepping each touched file after transform.

---

## 9. FULL DIALOGUE DRAFT (the script — P3 moves this into `StoryStrings`)

Rules honored: ≤60 chars/line, simple words, one idea/line. Format: `SPEAKER: "line"`.
`[CARD: ...]` = title card text. All counts verified ≤60 when tabled (P3 test asserts).

### 9.1 Openings (3 lines per tier; opening box learns per-line voices in P3)

- **T0** (first run ever — Pip meets you):
  PIP: "Hey! Hey you! With the bow!" / PIP: "I'm Pip. You're the new Chief." /
  PIP: "Stay close. The Night is coming."
- **T1:** PIP: "Back again, Chief?" / PIP: "Granny saved you some light." /
  PIP: "Tonight we go further."
- **T2:** PIP: "The Night knows your name now." / PIP: "Good. Let it shake." /
  PIP: "Pip's got a plan!" (+ WARDEN: "…No." as 4th? NO — 3-line sets stay 3; Warden's "No" becomes the T2 card subline. Decide in P3.)
- **T3+:** PIP: "New night. Same Chief." / PIP: "Granny says hi." / PIP: "Let's bonk the dark."

### 9.2 Chapter cards (w1 / w51 / w101 / w151 — HOLLOW deadpan + title)

- Ch1 `[CARD: FIRST LIGHT]` HOLLOW: "I am the Night. I was here first."
- Ch2 `[CARD: AMBER HOUR]` HOLLOW: "You are still here. Cute."
- Ch3 `[CARD: THE LONG DARK]` HOLLOW: "I do not get tired. Ask anyone."
- Ch4 `[CARD: HOLD THE DAWN]` HOLLOW: "Fine. My best team. All of them."

### 9.3 Pip milestones (beats on cleared milestone waves — NOT during combat)

- w25 PIP: "Twenty-five nights! Pip counted!"
- w75 PIP: "The dark is thick. My butt glows."
- w125 PIP: "Half the Night Shift owes me coins."
- w175 PIP: "Almost dawn, Chief. Almost."

### 9.4 Plantings (Sprout w50 short / Twig w100 FULL / Leaf w150 short)

- w50 — walk_out PIP: "A new tree, Chief! Dig here!" / plant WARDEN: "Grow strong, little one." /
  walk_back GRANNY: "Sprout! My loud little boy."
- w100 — walk_out GRANNY: "One more, dearie. For me." / plant WARDEN: "Grow brave, Twig." /
  water PIP: "Drink up! Big gulps!" / grow GRANNY: "He writes poems already." /
  walk_back PIP: "Pip's got TWO brothers now!"
- w150 — walk_out PIP: "Last seed. Make it count." / plant WARDEN: "Grow soft, Leaf." /
  walk_back GRANNY: "Shh. She naps already."

### 9.5 Boss intros M1 (title card + 4 boss lines + Pip comeback)

1. **GRUM (w5)** `[CARD: GRUM — Night Shift Security. Do not wake.]`
   "Hrrrm? …Who woke Grum?" / "Grum was napping. For a hundred years." /
   "Now Grum must sit on you." / "Sorry. Rules. …Yaaawn." — PIP: "He's falling asleep! Get him, Chief!"
2. **MAMA (w10)** `[CARD: MAMA BRAMBLE — She grew half the bad guys.]`
   "YOU! You stepped on my babies!" / "My poor Rootlings! My sweet thorns!" /
   "No supper for you, young Warden!" / "Come here. Mama must scold you. HARD." —
   PIP: "She packed snacks! Evil snacks! Run!"
3. **SIZZLE (w15)** `[CARD: SIZZLE — The hottest star of the Night.]`
   "SIZZLE IS IN THE BUILDING!" / "Look at these scales! Look at them!" /
   "No photos of the left side. It is my bad side." / "Now burn, little extra! You are not the star!" —
   PIP: "Somebody boil water! Oh wait. He hates that."
4. **SIR (w20)** `[CARD: SIR FALLS-A-LOT — Very polite. Very clumsy.]` *(trips on ENTER)*
   "Whoa—! …I am fine. I meant that." / "Good evening. I shall crush you now." /
   "Nothing personal. The Night pays well." / "Pardon me. En garde. …Sorry. En garde?" —
   PIP: "Did he just… bow? TO US?"
5. **CHILL (w25)** `[CARD: BIG CHILL — Evil? Or just on holiday?]`
   "Whoa. Little dude. Cool bow." / "Name's Chill. Big Chill." /
   "No stress. I freeze you soft." / "After, we get ice. …Get it? Ice?" —
   PIP: "I can't tell if he's bad or on a break."
6. **PAGE (w30)** `[CARD: OLD PAGE — Librarian. Fines are final.]`
   "SHHHH! This is a QUIET grove!" / "Your card is TWO HUNDRED years late!" /
   "Warden. Do you know what OVERDUE means?" / "It means BONK. *stamp* OVERDUE." —
   PIP (whisper): "Why are we whispering? …Why am I whispering?"
7. **CAP (w35)** `[CARD: CAPTAIN THUNDER — Scared of puddles.]`
   "I! AM! THE STORM!" / "My ship is a rock. My crew is thunder." /
   "Surrender your… wait. Is that MUD?" / "MUD! ON MY BOOTS! NOW YOU PAY!" —
   PIP: "Note to self: bring mud next time."
8. **BLUSH (w40)** `[CARD: BLUSH — Sorry about this. Really sorry.]`
   "H-hi. I'm Blush. Sorry." / "The Night said… um… bonk you?" /
   "I don't want to. But rules are rules." / "Please dodge? …Okay. Here I come. Sorry." —
   PIP: "Chief. I like her. …Bonk her gently?"

### 9.6 Boss intros M2 — "YOU AGAIN" (w45–w80; 2 boss + Pip)

- GRUM: "You again? Grum just fell asleep!" / "Fine. Quick bonk. Then nap." — PIP: "He brought a pillow, Chief!"
- MAMA: "Back for more scolding? Good!" / "Mama baked blame-cookies. Eat blame!" — PIP: "Do NOT eat the blame, Chief."
- SIZZLE: "The extra returns! Fans first!" / "This time I burn you in HD!" — PIP: "What's HD? …He doesn't know either."
- SIR: "Ah! My favorite… whoa—! …foe." / "Shall we? Mind the rocks. I never do." — PIP: "Somebody catch him! …Not it."
- CHILL: "Little dude! Back for more chill?" / "Same deal. Soft freeze. No stress." — PIP: "He remembered us! …I think."
- PAGE: "YOU! Still loud! Still late!" / "Fine doubled. Bonk doubled." — PIP (whisper): "He brings a bigger stamp. Run."
- CAP: "Back to my waters, tiny sailor?" / "This time NO mud. I checked. Twice." — PIP (hiding mud): "No mud here, Captain!"
- BLUSH: "Oh! Hi again! …Sorry!" / "I practiced bonking. I'm still bad." — PIP: "She practiced! So sweet! …Bonk her."

### 9.7 Boss intros M3 — running gags (w85–w120; 2 boss + Pip)

- GRUM: "Grum dreamed of you. You were loud." / "Sit. Bonk. Nap. In that order." — PIP: "We're in his dreams now. Big honor."
- MAMA: "You look thin! Are you eating?" / "Eat this thorn pie. Then bonk." — PIP: "The pie is moving. THE PIE IS MOVING."
- SIZZLE: "My fans demand a rematch! *crickets*" / "…My ONE fan. Where is my fan?" — PIP: "I'm right here! Worst show ever!"
- SIR: "A hundred nights! …Whoa—! …I live here now." / "On the floor. It is nice here." — PIP: "Should we help him up? …He seems happy."
- CHILL: "A hundred nights and still cool." / "Respect, little dude. Ice?" — PIP: "He offered us ice! We're friends now, right?"
- PAGE: "A hundred nights of NOISE!" / "I wrote it all down. All of it." — PIP: "He wrote a book about us! We're famous!"
- CAP: "Half the sea behind us, sailor!" / "My rock-ship sails at dawn. Be on it. As my prisoner." — PIP: "Prisoner with snacks? Ask about snacks."
- BLUSH: "H-hi! I made you a card!" / "It says sorry. …In advance." — PIP: "SHE MADE US A CARD! Chief, keep it!"

### 9.8 Boss intros M4 — respect cracks (w125–w160; 2 boss + Pip)

- GRUM: "Grum naps less now. Watches you." / "You fight… good. Do not tell the Night." — PIP: "Did Grum just… praise us?"
- MAMA: "My babies fear you now. Good." / "A mother knows strength. Bonk Mama gently." — PIP: "Gently? CHIEF. GENTLY."
- SIZZLE: "You stole my crowd, extra!" / "Fine. Duet. You and me. After I burn you." — PIP: "He wants a duet! We're STARS!"
- SIR: "I polished my armor for you… whoa—!" / "…The floor and I are old friends." — PIP: "He polished! For US! …Somebody help him."
- CHILL: "Almost dawn, little dude." / "Freeze you soft. Always soft." — PIP: "Soft freezes only. Best bad guy ever."
- PAGE: "One hundred fifty nights. Shhh." / "…You read my book? …Thank you." — PIP: "He smiled! …I think that was a smile."
- CAP: "The sea ends soon, sailor." / "First mate. My offer stands. Mud and all." — PIP: "TAKE THE JOB, CHIEF! …After dawn."
- BLUSH: "We're friends, right? …Say yes?" / "Okay. Bonk time. Friends bonk soft." — PIP: "SOFT BONKS! Everybody heard that!"

### 9.9 Boss intros M5 — farewell tour (w165–w200; 2 boss + Pip)

- GRUM: "Last nap before dawn, little loud one." / "Wake Grum… when it is morning." — PIP: "We'll wake you, big guy. Promise."
- MAMA: "Mama knit you a scarf. Thorny." / "Wear it. Bonk Mama. Then breakfast." — PIP: "Breakfast! She said breakfast!"
- SIZZLE: "Final show! Sizzle! Sold out!" / "You were… a good rival. Do not cry." — PIP: "I'm not crying! …Encore!"
- SIR: "One last fall… see? No— whoa—!" / "…Worth it. For you, old friend." — PIP: "He called us friend! …Help him up. For real."
- CHILL: "Last wave, little dude. Stay cool." / "Dawn comes. Chill stays. Always." — PIP: "Best bad guy ever. Don't tell the others."
- PAGE: "Final stamp. READ— *stamp*" / "…READER OF THE YEAR. Still overdue." — PIP: "We won! …Wait, what did we win?"
- CAP: "Last storm, first mate!" / "After dawn, we sail. For real. No mud." — PIP: "No mud! …I'll pack mud anyway."
- BLUSH (w200, the last boss): "Last bonk. …Can we be friends after?" / "Okay. Here I come. Sorry. Love you. Sorry." — PIP: "Everybody… that was the cutest threat ever."

### 9.10 Hollow (deadpan Night — verdicts, deaths, mercy, milestones)

- HELLO (first w1 ever): "I am the Night. I was here first."
- VERDICT_MERCIFUL: "You spared some. Cute. I counted."
- VERDICT_STERN: "You spared none. Cold. I like it."
- DEATH_FIRST: "You fell. Get up. The show needs you."
- DEATH_AGAIN: "Again? …The floor likes you."
- SPARE_1: "You let it go? …Bold. I watched."
- SPARE_3: "Three free. Mercy. I remember that word."
- WAVE100 (planting ceremony, allowed — not combat): "Halfway. Cute tree. I am still here."

### 9.11 Granny (victory, gift, letters voice)

- VICTORY: "Dawn, dearie! You did it! Soup for all!"
- DAILY_GIFT: "Two seeds from yesterday. Granny counts."
- LETTERS (10, one per dawn — P3 drafts from this voice; M1: "So that was you…", M10: door/last-letter beat stays, reworded warm).

### 9.12 Pip whispers (idle, 6, each once ever)

1. "Chief? You sleeping? …Pip naps too." 2. "Granny says hi. Eat your sunlight."
3. "Pip guarded the grove. All alone. Brave." 4. "The dark blinked first. Pip saw it."
5. "Rest is training. Pip trains hard." 6. "You're back! Pip missed you. A little."

### 9.13 Epilogues (A/B wins, C/D/E falls, transition)

- A flawless: "Two hundred nights. Zero falls." / "The Night needs a new plan." / PIP: "Pip's plan worked! …Mostly."
- B hard-fought: "Two hundred nights. All heart." / "I fell. I rose. I held." / PIP: "Best Chief ever. Don't argue."
- C early: "Too soon. Too dark." / "Granny, keep my seat warm." / PIP: "We go again. Now. Up, Chief!"
- D middle: "Past Twig. Not past dawn." / "Next time, Night. Next time." / PIP: "Pip counted! Further next run!"
- E late: "So close the dawn waved." / "It can wait one more run." / PIP: "One more run! Pip's got a NEW plan!"
- TRANSITION: "The Night rests. It never leaves." / "Stand up. Granny stands with you."
- WARDEN's final line (victory box, before epilogue A/B): "…We held."

### 9.14 Mythic flavors (one line each, simple)

- SUNFALL: "One arrow. One dawn. Never missed." / CROWN: "See weak spots. Bonk them." /
  BARK: "Granny's bark. Heals you back." / WINDRUNNER: "Fast boots. Never run. Stand." /
  VERDANT: "A pinky promise in sap." / EMBERLESS: "Cold ember. Hot temper."

### 9.15 Elite fragments (Pip's field notes — 2 per affix, funny)

- BLIGHTBURST: "It pops! Do not hug it." / "That pop is relief. Weird."
- ROOTWARD: "A shield! Rude shield!" / "It guards nothing. Still guards."
- WEEPING: "Don't step in the yuck." / "The yuck leads to Granny?!"
- HOLLOWMOLT: "One becomes two! Bad magic!" / "Two small quiets. Still loud."
- GRAVEMOSS: "Moss on a wound. Still a wound." / "It's healing! …Stop healing!"
- CINDERHALO: "Hot hug! No hugs!" / "Warm grief. Stay back."
- STONESHELL: "It wears a hill. Cheater." / "Stone naps. Stone hates you."
- GRAVEBLOOM: "Angry ground. Walk around." / "It remembers. Rude."
- SWARMCALL: "It called friends! Unfair!" / "More friends! SO many friends!"
- SPITEBARB: "Long arms! Longer fouls!" / "Close work costs. Pay up."
- HAMMERFALL: "Arm up! Move, Chief!" / "Step. That's the lesson."
- BLOODHOWL: "It howls! They run!" / "Find the howler. Bonk it."

### 9.16 Deeds (functional shape stays, words warmed — P3)

- "Deed: held to wave 10 | + N coins" etc. (keep pattern, English-only already handled in P2).

---

## 10. IMPLEMENTATION PLAN (pushes P1–P8)

| Push | Title | Files (main) | Tests | Risk |
|---|---|---|---|---|
| **P1** ✅ | MEMORY.md | `MEMORY.md` | none (docs) | none |
| **P2** | English-only strip | §8.1 catalog (i18n ×17, GameFonts, story ×4, settings ×2, input ×2, SettingsRenderer, Trophy, + grep stragglers); delete proofread doc + Vazir fonts + Vazir OFL | §8.3 catalog (1 delete, ~20 rewrites/updates) | MEDIUM — script + import hygiene; CI is the net |
| **P3** | New story tables | `StoryStrings` (rewrite + ~230 keys), `LoreCatalog`, `BossLore`, `TreeLetters`, `Epilogue`, `EliteFragments`, `ReflectionLines`→Pip milestones, `CeremonyLines`, `HollowVoice` keys, `OpeningCinematic` line sets + per-line voices, `SpeechVoice` +PIP/BOSS + `SpeechBlip`, `STORY_CONTENT.md` + `STORY_VOICE.md` | new `StoryLineLengthTest` (≤60 chars), update Epilogue/Reflection/Ceremony/Elite/Lore tests | MEDIUM — big but mechanical; keys reviewed against §9 |
| **P4** | Boss-intro cutscenes | NEW `BossIntroCinematic`, NEW `story/BossIntros`; `WaveLifecycleSystem`, `WaveCompletion`, `GameState`, `CinematicFlow`, `RunPresentationSystem`, render zoom + plate (basic), input route check | NEW `BossIntroCinematicTest`, `BossIntrosTest`, `CinematicFlowBossIntroTest`, `WaveLifecycleBossIntroTest`; update presentation tests | HIGH — the heart; needs render-reading first |
| **P5** | Silence rule | `RunPresentationSystem.presentPlaytime` (drop half-beat), delete `BossBeats`, deed-suppression-while-boss | rewrite half-beat tests → silence asserts | LOW |
| **P6** | Longer waves | `EnemyWaveSpawner` (trickles), `BossWaveSpawner` (escorts), `WaveLifecycleSystem` (mid-wave check), `DifficultyCurve`/`BalanceSimulator` retune, `BALANCE.md` | spawner tests, balance gates (measured), perf watch | MEDIUM — gates must be re-measured honestly |
| **P7** | UI/UX pass | speaker colors, name plate styling, watch-chip, chapter cards, menu subtitle, a11y blips | renderer/layout tests for new bits | LOW–MEDIUM |
| **P8** | Final sweep | docs touch-ups, release notes, MEMORY status → DONE | FULL suite green twice | LOW |

**Order is load-bearing:** P2 before P3 (tables change shape once), P3 before P4 (intros read
new keys), P5 with/after P4 (same files), P6 after P4 (escorts spawn via intro hand-off),
P7 last (styles what exists).

---

## 11. RULES OF THE ROAD (for agents)

1. **Read this file first. Update §0 before every push.** The push log + session log are the
   owner's dashboard.
2. **Fast pushes, one idea per commit, push immediately.** The owner watches the repo move.
   Each push message + chat report says: DONE / REMAINING.
3. **CI `test-core` is the verifier.** Never claim "done" on prose. If CI is red, the next
   push fixes it — nothing else lands on red.
4. **Gates are honest.** Re-measure, don't weaken (P6). A test that pins an OLD word gets
   REWRITTEN for the new word (P2/P3) — that's migration, not weakening. A test that pins a
   NUMBER gets re-measured numbers + the reason recorded.
5. **PMD/SpotBugs hygiene:** no unused imports, no dead refs. The strip script greps after itself.
6. **No new art until P7,** and then atlas-only. No new dependencies. No new modules.
7. **Determinism stays:** intros/trickles are pure functions of (wave, seed, elapsed) like
   everything else. Reload-safe, replay-safe.
8. **Feel is a feature.** Read every new line OUT LOUD. If it doesn't make you smile, rewrite
   it. Machine-garbage is a build failure of the soul — the owner will notice.
9. **Don't resurrect the roadmap.** `docs/ROADMAP.md` is frozen history. This file plans now.
10. **Secrets:** the owner's PAT lives in this session only. Never commit it, never print it,
    never put it in a file. Clone/push URLs redact it in logs.

---

## 12. FLAGGED DECISIONS (owner explicitly ordered; recorded so nobody "fixes" them back)

1. **English-only kills the IR-market angle** (Cafe Bazaar Persian store text, fa-IR digits).
   Deliberate per owner. Reversible via git history. The release workflow still builds the APK.
2. **Tier-0 opening rewritten** (old pin said never). Owner: whole story deleted.
3. **Old rubric ≥900 / Gate 1 abandoned.** The roadmap's scoring died with its direction;
   honest gates (§10/P6) replace it.
4. **PersianShaper/Bidi/Arabic/tools-i18n/golden-vectors DELETED outright** (P2, decided
   mid-push). The plan said "keep as dead infra", but the audit's `persian` flag is
   case-insensitive over code, so any `PersianShaper` identifier keeps it True — an honest
   False needed the files gone. R7.3 CI shaping steps removed with them. `UiMirror` and the
   `rightToLeft()` holders stay (dormant LTR, audit `rightToLeft` evidence).

---

*End of MEMORY.md — the plan to the end. Now go build it: P2 is next.*
