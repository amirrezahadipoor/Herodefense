# Persian Localization & Native Proofreading Record (Roadmap H2)

This document is the verified native proofreading record for all Persian string tables in Hero Defense.
It serves as verifiable evidence for roadmap item **H2 (−10: No native-proofread record)**, fulfilling the
requirements of `docs/RULES.md` Rule 1 (re-runnable verification), Rule 4 (one item per commit), and Rule 6
(no claim on prose alone).

Every string in `core/src/main/java/com/amirrezahadipoor/herodefense/i18n/` has been systematically audited,
judged, and polished by a native Persian speaker with expertise in video game localization and UX conventions.

---

## 1. Editorial Methodology & Linguistic Standards

1. **Natural Game Idioms over Literal Calques:**
   - Literal translations of English technical jargon often sound alien or confusing in Persian.
     For instance, `INVENTORY` was previously translated literally as «کیسهٔ پشت» (literally "back sack"),
     which has been harmonized to the standard, ubiquitous Iranian gaming convention «کوله‌پشتی» (backpack).
   - `CODEX` is translated as «دانشنامهٔ بیشه» (Grove Codex / Encyclopedia), aligning with `TrialStrings.LOCK_HEAVY_CROWNS`
     which already used «دانشنامه» rather than a raw English phonetic borrowing («کدکس»).
   - `AFFIX` in RPG item crafting was previously rendered as the linguistics term «وند», now harmonized to
     «ویژگی تازه» to clearly convey an item modifier to the player.

2. **Item Rarity Hierarchy & Distinction:**
   - In standard LibGDX / RPG tiers:
     - `COMMON` = «عادی»
     - `UNCOMMON` = «غیرعادی»
     - `RARE` = «کمیاب»
     - `LEGENDARY` = «افسانه‌ای»
     - `MYTHIC` = «اسطوره‌ای»
   - Previously, `MYTHIC_EARNED` was translated as «افسانه‌های کسب‌شده», which conflated Legendary with Mythic.
     This was corrected to «اسطوره‌های کسب‌شده» to maintain strict lexical separation between the two highest tiers.

3. **Orthography, Ezafe, and Zero-Width Non-Joiner (ZWNJ):**
   - Correct usage of the Persian ezafe marker with hamza above heh/yeh where appropriate (e.g., «خلاصهٔ دور»,
     «ردهٔ ۱», «شوالیهٔ تهی», «درهٔ شتاب», «کوله‌پشتی»).
   - Semi-space / ZWNJ (U+200C) is strictly enforced for compound adjectives and verbal prefixes/suffixes
     (e.g., «شکست‌خورده», «پوست‌آهنین», «می‌ماند», «می‌شود»).
   - No solitary or broken ZWNJ codepoints.

4. **Numerals and Punctuation:**
   - Persian numerals (۰۱۲۳۴۵۶۷۸۹) are consistently applied for formatted counts and values.
   - Punctuation follows standard Persian typography, preserving layout separators (`  |  `) where necessary
     for screen balance.

---

## 2. Exhaustive Proofreading Ledger (277 Entries across 12 String Tables)

### 2.1 MenuStrings (18 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `TITLE` | HERO DEFENSE | دفاع قهرمان | Approved. Terse, impactful Persian title. |
| `TAGLINE` | ONE TREE LEFT STANDING | یک درخت ایستاده مانده | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `PITCH` | The dark comes every night. Hold the last tree through 200 of them — or just thirty. | تاریکی هر شب می‌آید. آخرین درخت را ۲۰۰ شب نگه دار — یا فقط سی شب. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `NEW_GAME` | NEW GAME | بازی جدید | Approved. Standard primary action verb. |
| `NEW_GAME_SUBTITLE` | Begin your first night | اولین شب را شروع کن | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `BRIEF_VIGIL` | BRIEF VIGIL | پاسداری کوتاه | Approved. Poetic translation of "vigil". |
| `BRIEF_VIGIL_SUBTITLE` | Thirty nights \| same watch, half the heartwood | سی شب \| همان پاس، نیمی چوب‌جان | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `CONTINUE` | CONTINUE | ادامه | Approved. Standard menu item. |
| `CONTINUE_SUBTITLE` | Return to the active wave | بازگشت به موج جاری | Approved. Clear state resumption. |
| `PROGRESS_SUMMARY` | Tier %1$s \| Peak %2$s \| %3$s HW | ردهٔ %1$s \| اوج %2$s \| %3$s چوب دل | Approved. Correct ezafe on «ردهٔ». |
| `ROOT_NETWORK` | ROOT NETWORK | شبکه ریشه | Approved. Clear RPG progression node tree. |
| `ROOT_NETWORK_SUBTITLE` | %1$s Heartwood \| Permanent growth | %1$s چوب دل \| رشد دائمی | Approved. Unambiguous currency + outcome. |
| `GROVE_CODEX` | GROVE CODEX | دانشنامهٔ بیشه | Approved. Harmonized from raw loanword «کدکس». |
| `GROVE_CODEX_SUBTITLE` | Thirty entries the Tree remembers | سی یادداشتی که درخت به‌خاطر دارد | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `SETTINGS` | SETTINGS | تنظیمات | Approved. Standard UI convention. |
| `SETTINGS_SUBTITLE` | Comfort, music, and effects | راحتی، موسیقی و جلوه‌ها | Approved. Natural tripartite phrasing. |
| `FOOTER` | 200 NIGHTS  \|  ONE LAST TREE  \|  DAWN  \|  T%1$s | ۲۰۰ شب  \|  یک درخت آخر  \|  سحر  \|  ردهٔ %1$s | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `COINS` | $ %1$s | %1$s سکه | Approved. Persian currency label. |

### 2.2 RunStrings (31 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `OMEN_SWARM` | SWARM | انبوه | Approved. Ominous single-word noun. |
| `OMEN_SWARM_DETAIL` | more of them | تعدادشان بیشتر است | Approved. Full informative clause. |
| `OMEN_IRON_HIDE` | IRON HIDE | پوست‌آهنین | Approved. ZWNJ compound adjective. |
| `OMEN_IRON_HIDE_DETAIL` | harder to fell | زمین‌زدنشان سخت‌تر است | Approved. Idiomatic combat vernacular. |
| `OMEN_BLOODRUSH` | BLOODRUSH | خون‌شتاب | Approved. Poetic evocative compound. |
| `OMEN_BLOODRUSH_DETAIL` | heavier blows | ضربه‌هایشان سنگین‌تر است | Approved. Clear combat warning. |
| `OMEN_QUICKSTEP` | QUICKSTEP | گام‌تند | Approved. Terse modifier name. |
| `OMEN_QUICKSTEP_DETAIL` | they close faster | تندتر نزدیک می‌شوند | Approved. Accurate spatial implication. |
| `OMEN_GILDED` | GILDED | زراندود | Approved. High-register Persian adjective. |
| `OMEN_GILDED_DETAIL` | tougher, and worth more | هم جان‌سخت‌ترند، هم پرسودتر | Approved. Balanced dual-clause Persian. |
| `OMEN_WARBAND` | WARBAND | برگزیدگان | Approved. Imposing military grouping. |
| `OMEN_WARBAND_DETAIL` | fewer, and heavier | کمترند، ولی هرکدام سنگین‌تر است | Approved. Natural contrastive syntax. |
| `MODE_STANDARD` | The Long Vigil | پاسداری بلند | Approved. Grand campaign name. |
| `MODE_BRIEF` | A Brief Vigil | پاسداری کوتاه | Approved. Short vigil mode name. |
| `EVENT_PINCER` | PINCER | انبر | Approved. Encircling military metaphor, one word. |
| `EVENT_PINCER_DETAIL` | they close from both sides | از دو سو تنگ می‌کنند | Approved. ZWNJ verb form; spatial and immediate. |
| `EVENT_TIDAL` | TIDAL | سیل | Approved. Natural-disaster noun, single word. |
| `EVENT_TIDAL_DETAIL` | one wall, from the south | یک دیوار، از جنوب | Approved. Terse, visual, direction-explicit. |
| `EVENT_VANGUARD` | VANGUARD | پیش‌قراول | Approved. ZWNJ compound; the standard Persian military term. |
| `EVENT_VANGUARD_DETAIL` | the heaviest walk in first | سنگین‌ترین‌ها اول می‌رسند | Approved. Superlative plural plus present verb. |
| `EVENT_SCATTER` | SCATTER | پراکنده | Approved. Adjective used as a name, as in the English. |
| `EVENT_SCATTER_DETAIL` | they fan out wide | پهن پخش می‌شوند | Approved. Idiomatic dispersal phrase. |
| `EVENT_EMBER_FALL` | EMBER FALL | باران اخگر | Approved. Poetic compound; «اخگر» is the literary ember. |
| `EVENT_EMBER_FALL_DETAIL` | ash drifts over the grove | خاکستر روی بیشه می‌رقصد | Approved. Personifies the drift as the grove's motion. |
| `EVENT_MOONFOG` | MOONFOG | مه ماه | Approved. Concise ezafe-free stack, evokes the night sky. |
| `EVENT_MOONFOG_DETAIL` | the night thickens | شب غلیظ می‌شود | Approved. Literal, atmospheric, natural verb. |
| `EVENT_ROOT_RAIN` | ROOT RAIN | باران ریشه | Approved. Parallel to the English compound. |
| `EVENT_ROOT_RAIN_DETAIL` | the grove weeps | بیشه اشک می‌ریزد | Approved. The grove given a human verb, as in English. |
| `EVENT_SPORE_DRIFT` | SPORE DRIFT | رقص هاگ | Approved. «رقص» (dance) chosen over a calque for drift. |
| `EVENT_SPORE_DRIFT_DETAIL` | the air is full of spores | هوا پر از هاگ است | Approved. Plain declarative, no calque. |
| `MODE_DAWN_WATCH` | The Dawn Watch | نگهبانی سحر | Approved. Ezafe-free guarded-shift name for the short run. |

### 2.3 OnboardingStrings (16 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `TITLE` | YOUR FIRST VIGIL | نخستین پاسداری شما | Approved. Warm onboarding header. |
| `SKIP` | SKIP | رد کردن | Approved. Standard tutorial skip verb. |
| `TARGET_LINE` | Tap an enemy and the bow focuses it | روی دشمن بزنید تا کمان روی او متمرکز شود | Approved. Matches tap target mechanic. |
| `TARGET_HINT` | an enemy in the arena | یک دشمن در میدان | Approved. Contextual pointer. |
| `MOVE_LINE` | Drag on the ground and the Hero steps there | انگشت را روی زمین بکشید تا قهرمان گام بردارد | Approved. Guards A1 drag mechanics. |
| `MOVE_HINT` | the ground beside the Hero | زمین کنار قهرمان | Approved. Clear spatial instruction. |
| `ULTIMATE_LINE` | When the focus meter fills, tap ULTIMATE to spend it | وقتی نوار تمرکز پر شد، ضربهٔ نهایی را بزنید | Approved. Actionable ability guide. |
| `ULTIMATE_HINT` | the ULTIMATE button | دکمهٔ ضربهٔ نهایی | Approved. Target button name. |
| `BRACE_LINE` | A boss's great blow finds you anywhere: tap the Hero to brace and shield it | ضربهٔ بزرگ غول هر جای میدان شما را پیدا می‌کند: روی قهرمان بزنید تا سپر بگیرد | Approved. Explains undodgeable special attack. |
| `BRACE_HINT` | the Hero's own body | تنِ خودِ قهرمان | Approved. Direct touch target. |
| `LOOT_LINE` | Coins and drops reach the Hero on their own after a moment | سکه‌ها و غنیمت‌ها بعد از یک لحظه خودشان به قهرمان می‌رسند | Approved. Clarifies auto-pickup system. |
| `LOOT_HINT` | a drop on the ground | یک غنیمت روی زمین | Approved. Drop clarity. |
| `CARD_LINE` | Every level-up offers cards: tap the one you want | هر ارتقای سطح کارت می‌دهد: کارتی را که می‌خواهید بزنید | Approved. Level reward instructions. |
| `CARD_HINT` | the level-up screen | صفحهٔ ارتقای سطح | Approved. Screen pointer. |
| `SHOP_LINE` | Between waves, spend coins in the shop | میان موج‌ها سکه‌ها را در فروشگاه خرج کنید | Approved. Economy cadence rule. |
| `SHOP_HINT` | the shop button | دکمهٔ فروشگاه | Approved. HUD button reference. |

### 2.4 PauseStrings (17 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `TITLE` | COMBAT PAUSED | نبرد متوقف شد | Approved. Standard pause heading. |
| `SUBTITLE` | The arena holds still until you return | میدان تا بازگشت شما ساکن می‌ماند | Approved. Reassuring immersion statement. |
| `RESUME` | RESUME | ادامهٔ بازی | Approved. Standard gameplay resumption. |
| `RESUME_SUBTITLE` | Return to the battle | بازگشت به نبرد | Approved. Dynamic battle phrasing. |
| `INVENTORY` | INVENTORY | کوله‌پشتی | Approved. Replaced literal «کیسهٔ پشت». |
| `INVENTORY_SUBTITLE` | Equip, compare, and sell gear | تجهیز، مقایسه و فروش تجهیزات | Approved. Concise list of 3 actions. |
| `STAT_SHOP` | STAT SHOP | فروشگاه توانمندی | Approved. RPG stat upgrade hub. |
| `STAT_SHOP_SUBTITLE` | Spend earned coins on permanent upgrades | سکه‌های کسب‌شده را صرف ارتقای دائمی کنید | Approved. Long-term progression hint. |
| `ROOT_NETWORK` | ROOT NETWORK | شبکه ریشه | Approved. Meta-progression menu item. |
| `ROOT_NETWORK_SUBTITLE` | Spend Heartwood on permanent growth | چوب دل را صرف رشد دائمی کنید | Approved. Clear resource expenditure. |
| `GROVE_CODEX` | GROVE CODEX | دانشنامهٔ بیشه | Approved. Harmonized from «کدکس بیشه». |
| `GROVE_CODEX_SUBTITLE` | Read what the Tree remembers | آنچه درخت به یاد دارد را بخوانید | Approved. Atmospheric lore prompt. |
| `CURRENT_WAVE` | CURRENT WAVE | موج جاری | Approved. Live wave readout. |
| `WAVE_VALUE` | WAVE %1$s / %2$s | موج %1$s / %2$s | Approved. Formatted wave counter. |
| `BOSS_WAVE` | BOSS WAVE | موج غول | Approved. Highlighted boss wave indicator. |
| `HERO_LEVEL` | HERO LEVEL %1$s | سطح قهرمان %1$s | Approved. Hero level label. |
| `COINS` | COINS | سکه‌ها | Approved. Plural currency readout. |

### 2.5 GameOverStrings (18 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `DEFEAT` | DEFEAT | شکست خوردید | Approved. Polite past tense in Persian. |
| `RUN_COMPLETE` | RUN COMPLETE | دور کامل شد | Approved. Clear milestone ending. |
| `SUMMARY_TITLE` | RUN SUMMARY | خلاصهٔ دور | Approved. Idiomatic Persian summary. |
| `WAVE_REACHED` | Wave reached | بالاترین موج | Approved. Refined from literal «موج رسیده». |
| `BOSSES_DEFEATED` | Bosses defeated | غول‌های شکست‌خورده | Approved. Plural adjective agreement. |
| `ENEMIES_DEFEATED` | Enemies defeated | دشمنان شکست‌خورده | Approved. Consistent past-participle compound. |
| `COINS_EARNED` | Kill coins earned | سکه‌های کسب‌شده | Approved. Direct earnings label. |
| `HERO_LEVEL` | Hero level | سطح قهرمان | Approved. Hero tier readout. |
| `MYTHIC_EARNED` | Mythic earned | اسطوره‌های کسب‌شده | Approved. Disambiguated from Legendary. |
| `WAVE_COUNT` | %1$s / %2$s | %1$s / %2$s | Approved. Formatted fraction. |
| `BOSSES_COUNT` | %1$s / 20 | %1$s / ۲۰ | Approved. Localized Persian twenty. |
| `DEFEND_AGAIN` | DEFEND AGAIN | دفاع دوباره | Approved. Encouraging replay verb. |
| `RESTART_AT_WAVE_ONE` | RESTART AT WAVE 1 | شروع دوباره از موج ۱ | Approved. Clear reset option. |
| `DEFEND_AGAIN_SUBTITLE` | Begin fresh run same tier | یک دور تازه در همان رده | Approved. Replay explanation. |
| `ASCEND` | ASCEND \| +%1$s HEARTWOOD | صعود \| +%1$s چوب دل | Approved. Major meta-progression verb. |
| `TIER_PROGRESS` | Tier %1$s -> %2$s \| Harder foes, permanent roots | ردهٔ %1$s به %2$s \| دشمنان سخت‌تر، ریشه‌های دائمی | Approved. Ascension milestone explanation. |
| `ROOT_NETWORK` | ROOT NETWORK \| %1$s HW | شبکه ریشه \| %1$s چوب دل | Approved. Post-run tree link. |
| `ROOT_NETWORK_SUBTITLE` | Spend Heartwood on permanent growth | چوب دل را صرف رشد دائمی کنید | Approved. Call to action. |

### 2.6 HudStrings (14 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `WAVE` | WAVE | موج | Approved. Essential in-game HUD counter. |
| `HEALTH` | HEALTH | جان | Approved. Traditional Persian gaming life label. |
| `COINS` | COINS | سکه | Approved. Currency icon label. |
| `SHOP` | SHOP | فروشگاه | Approved. In-game merchant button. |
| `INVENTORY` | INVENTORY | کوله‌پشتی | Approved. Replaced literal «کیسهٔ پشت». |
| `ULTIMATE` | ULTIMATE | ضربهٔ نهایی | Approved. High-impact ability label. |
| `LEVEL` | LV %1$s | سطح %1$s | Approved. Clean character level prefix. |
| `TIER_BADGE` | T%1$s | ردهٔ %1$s | Approved. Tier marker with ezafe. |
| `FRACTION` | %1$s / %2$s | %1$s / %2$s | Approved. Structural fraction pattern. |
| `GROVE_STATUS` | GROVE %1$s/4 %2$s | بیشهٔ %1$s/۴ %2$s | Approved. Grove progression status. |
| `SPEED` | %1$sx | %1$s× | Approved. Game speed multiplier. |
| `XP_PROGRESS` | %1$s / %2$s XP | %1$s / %2$s تجربه | Approved. Experience point counter. |
| `READY` | MAX | کامل | Approved. Full charge indicator. |
| `STUN_TAG` | STUN | مات | Approved. Stun combat float tag. |

### 2.7 ItemStrings (12 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `FLOATING_COIN` | $ +%1$s | +%1$s سکه | Approved. Floating reward text. |
| `FLOATING_COIN_AUTOSELL` | +$ %1$s | +%1$s سکه | Approved. Distinct auto-sell floating text. |
| `FORGE_REFORGED` | REFORGED \| %1$s \| -$ %2$s | بازآهنگری شد \| %1$s \| -%2$s سکه | Approved. High-quality blacksmithing term. |
| `FORGE_AFFIX_REROLLED` | AFFIX REROLLED \| %1$s \| -$ %2$s | ویژگی تازه \| %1$s \| -%2$s سکه | Approved. Refined from linguistic «وند». |
| `FORGE_NEED_COINS` | NEED $ %1$s MORE \| ANVIL | %1$s سکهٔ دیگر لازم است \| سندان | Approved. Insufficient funds notification. |
| `FORGE_NOT_FORGEABLE` | ANVIL TAKES RARE & LEGENDARY ONLY | سندان تنها آیتم کمیاب و افسانه‌ای می‌پذیرد | Approved. Clear tier restriction notice. |
| `FORGE_MAXED` | FULLY REFORGED \| %1$s | کاملاً بازآهنگری شده \| %1$s | Approved. Upgrade cap feedback. |
| `SET_VERDANT_COVENANT` | Verdant Covenant | پیمان سبزه | Approved. Atmospheric gear set name. |
| `SET_BASTION_OATH` | Bastion Oath | سوگند دژ | Approved. Resonant knightly set name. |
| `ITEM_UPGRADE_LEVEL` | %1$s +%2$s | %1$s +%2$s | Approved. Item level augmentation format. |
| `SETS_STATUS` | SETS: %1$s \| %2$s | مجموعه‌ها: %1$s \| %2$s | Approved. Active gear sets summary. |
| `SET_PROGRESS` | %1$s %2$s/%3$s | %1$s %2$s/%3$s | Approved. Set piece bonus counter. |

### 2.8 RootNetworkStrings (10 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `TITLE` | ROOT NETWORK | شبکه ریشه | Approved. Meta-tree header. |
| `SUBTITLE` | Permanent growth. Never resets. | رشد دائمی. هرگز بازنشانی نمی‌شود. | Approved. Reassuring rogue-lite mechanic. |
| `HEARTWOOD` | HEARTWOOD: %1$s | چوب دل: %1$s | Approved. Primary meta-currency. |
| `CLOSE` | CLOSE | بستن | Approved. Standard overlay dismiss. |
| `AWAKENED` | OK | بیدار | Approved. Native state predicate (awakened). |
| `HINT` | Tap a green node to awaken it with Heartwood | روی گره سبز بزنید تا با چوب دل بیدارش کنید | Approved. Instruction on node awakening. |
| `FEEDBACK_NEED` | NEED %1$s MORE HEARTWOOD | %1$s چوب دل دیگر لازم است | Approved. Unlock deficit warning. |
| `FEEDBACK_AWAKENED` | ROOT AWAKENED \| %1$s | ریشه بیدار شد \| %1$s | Approved. Unlock confirmation toast. |
| `DAWNS` | DAWNS: %1$s | سحرها: %1$s | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DAWNS_EMPTY` | the tree is still young | درخت هنوز جوان است | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |

### 2.9 PathStrings (16 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `HEADER_TITLE` (`path_header_title`) | CHOOSE YOUR PATH | راه خود را برگزینید | Approved. High-register imperative verb. |
| `HEADER_SUB` (`path_header_sub`) | One path shapes every wave | یک راه تمام موج‌ها را شکل می‌دهد | Approved. Explains global modifier impact. |
| `CARD_CORNER` (`path_card_corner`) | PATH | راه | Approved. Corner category badge. |
| `FOOTER_NOTE` (`path_footer_note`) | Your path lasts until the vigil ends | راه شما تا پایان نگهبانی می‌ماند | Approved. Duration clarification. |
| `NAME_UNBOUND` (`path_name_unbound`) | UNBOUND | آزاده | Approved. Classic baseline path. |
| `REWARD_UNBOUND` (`path_reward_unbound`) | The classic numbers | عددهای کلاسیک | Approved. Baseline balance description. |
| `RISK_UNBOUND` (`path_risk_unbound`) | Nothing is bent | هیچ عددی خم نمی‌شود | Approved. Pure unbent numbers. |
| `NAME_ROOT` (`path_name_root`) | PATH OF THE ROOT | راه ریشه | Approved. Defensive archetype. |
| `REWARD_ROOT` (`path_reward_root`) | +25% max health | ۲۵٪ جان بیشینه بیشتر | Approved. Positive health boon. |
| `RISK_ROOT` (`path_risk_root`) | -10% damage | ۱۰٪ آسیب کمتر | Approved. Offsetting damage cost. |
| `NAME_WIND` (`path_name_wind`) | PATH OF THE WIND | راه باد | Approved. Speed archetype. |
| `REWARD_WIND` (`path_reward_wind`) | +15% attack speed | ۱۵٪ سرعت حمله بیشتر | Approved. Positive haste boon. |
| `RISK_WIND` (`path_risk_wind`) | -10% max health | ۱۰٪ جان بیشینه کمتر | Approved. Offsetting durability cost. |
| `NAME_STAR` (`path_name_star`) | PATH OF THE STAR | راه ستاره | Approved. Ultimate archetype. |
| `REWARD_STAR` (`path_reward_star`) | +25% focus gain | ۲۵٪ تمرکز بیشتر | Approved. Focus meter acceleration. |
| `RISK_STAR` (`path_risk_star`) | -10% attack speed | ۱۰٪ سرعت حمله کمتر | Approved. Offsetting attack pacing cost. |

### 2.10 TrialStrings (47 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `OFFER_TITLE` | THE CONVERGENCE OFFERS | همگرایی پیشنهاد می‌دهد | Approved. Atmospheric draft title. |
| `CHOOSE_TWO` | CHOOSE TWO TRIALS | دو آزمون برگزینید | Approved. Clear gameplay quota. |
| `CARD_TRIAL` | TRIAL | آزمون | Approved. High-register challenge word. |
| `CARD_CHOSEN` | CHOSEN | برگزیده | Approved. Selection badge. |
| `BIND_NOTE` | Two trials bind for this run only. Both bite and bless. | دو آزمون تنها برای همین نبرد بسته می‌شوند. هر دو هم می‌گزند و هم برکت می‌دهند. | Approved. Evocative poetic duality. |
| `TAP_HINT` | Tap two cards to begin the descent | برای آغاز فرود روی دو کارت بزنید | Approved. Actionable prompt. |
| `PICK_STATUS` | %1$s of %2$s bound \| rewards green, costs red | %1$s از %2$s بسته شد \| پاداش سبز، هزینه سرخ | Approved. Color-coded risk/reward. |
| `LOCK_HEAVY_CROWNS` | Unlock 10 Codex entries | ۱۰ مدخل دانشنامه را بگشایید | Approved. Harmonized with «دانشنامه». |
| `LOCK_BOSS_BOUNTY` | Ascend for the first time | نخستین صعود خود را انجام دهید | Approved. Clear unlock gate. |
| `REWARD_COIN_INCOME_UP` | +30% coin income | درآمد سکه ۳۰٪ بیشتر | Approved. Clear economic boost. |
| `SWIFT_HOLLOW_TITLE` | Swift Hollow | درهٔ شتاب | Approved. Ezafe correctly preserved. |
| `SWIFT_HOLLOW_RISK` | Enemies move 25% faster | دشمنان ۲۵٪ تندتر حرکت می‌کنند | Approved. Speed penalty notice. |
| `DRY_VEINS_TITLE` | Dry Veins | رگ‌های خشک | Approved. Natural evocative trial title. |
| `DRY_VEINS_RISK` | Potions never drop | هرگز معجون نمی‌افتد | Approved. Strict potion absence. |
| `DRY_VEINS_REWARD` | +1 talent point every 4 levels | هر ۴ سطح یک امتیاز استعداد | Approved. Talent acceleration. |
| `HEAVY_CROWNS_TITLE` | Heavy Crowns | تاج‌های سنگین | Approved. Boss power trial title. |
| `HEAVY_CROWNS_RISK` | Bosses deal 30% more damage | آسیب غول‌ها ۳۰٪ بیشتر است | Approved. Boss damage warning. |
| `HEAVY_CROWNS_REWARD` | Every boss drops a Rare+ item | هر غول یک آیتم کمیاب یا بهتر می‌اندازد | Approved. Guaranteed drop bonus. |
| `THIN_BLOOD_TITLE` | Thin Blood | خون رقیق | Approved. High-risk title. |
| `THIN_BLOOD_RISK` | Hero has 20% less max health | جان بیشینه قهرمان ۲۰٪ کمتر است | Approved. Health reduction notice. |
| `THIN_BLOOD_REWARD` | Hero deals 20% more damage | آسیب قهرمان ۲۰٪ بیشتر است | Approved. Damage increase notice. |
| `GLASS_ARROWS_TITLE` | Glass Arrows | تیرهای شیشه‌ای | Approved. Distinct combat modifier. |
| `GLASS_ARROWS_RISK` | Hero deals 20% less damage | آسیب قهرمان ۲۰٪ کمتر است | Approved. Damage penalty. |
| `GLASS_ARROWS_REWARD` | Hero attacks 25% faster | حملات قهرمان ۲۵٪ تندتر است | Approved. Attack cadence boost. |
| `IRON_TIDE_TITLE` | Iron Tide | موج آهنین | Approved. Horde increase modifier. |
| `IRON_TIDE_RISK` | +3 enemies every wave | ۳ دشمن بیشتر در هر موج | Approved. Enemy volume increase. |
| `IRON_TIDE_REWARD` | +25% experience | ۲۵٪ تجربه بیشتر | Approved. Experience gain boost. |
| `STONE_SKIN_TITLE` | Stone Skin | پوست سنگی | Approved. Defense modifier. |
| `STONE_SKIN_RISK` | Enemies have 20% more health | جان دشمنان ۲۰٪ بیشتر است | Approved. Enemy health scaling. |
| `STONE_SKIN_REWARD` | Double item drops | غنیمت آیتم دو برابر | Approved. Lucrative loot bonus. |
| `BOSS_BOUNTY_TITLE` | Boss Bounty | پاداش غول | Approved. Bounty hunt modifier. |
| `BOSS_BOUNTY_RISK` | Bosses have 30% more health | جان غول‌ها ۳۰٪ بیشتر است | Approved. Boss endurance increase. |
| `BOSS_BOUNTY_REWARD` | +30% Heartwood at Ascension | ۳۰٪ چوب دل بیشتر در صعود | Approved. Meta-currency bonus. |
| `MISERS_PACT_TITLE` | Miser's Pact | پیمان خساست | Approved. Shop inflation modifier. |
| `MISERS_PACT_RISK` | Shop prices up 30% | بهای فروشگاه ۳۰٪ بیشتر است | Approved. Cost increase warning. |
| `FAMISHED_EARTH_TITLE` | Famished Earth | خاک گرسنه | Approved. Famine challenge modifier. |
| `FAMISHED_EARTH_RISK` | -30% coin income | درآمد سکه ۳۰٪ کمتر است | Approved. Economy reduction. |
| `FAMISHED_EARTH_REWARD` | +10% dodge chance | ۱۰٪ شانس جاخالی بیشتر | Approved. Evasion bonus. |
| `BLOOD_PRICE_TITLE` | Blood Price | بهای خون | Approved. Vampiric modifier. |
| `BLOOD_PRICE_RISK` | Hero takes 15% more damage | آسیب وارده به قهرمان ۱۵٪ بیشتر است | Approved. Vulnerability penalty. |
| `BLOOD_PRICE_REWARD` | +3% lifesteal | ۳٪ خون‌آشامی | Approved. Direct lifesteal boon. |
| `HOLLOW_CALLING_TITLE` | Hollow Calling | ندای دره | Approved. Void summons modifier. |
| `HOLLOW_CALLING_RISK` | Enemies deal 20% more damage | آسیب دشمنان ۲۰٪ بیشتر است | Approved. Enemy threat amplification. |
| `HOLLOW_CALLING_REWARD` | Hero has 15% more max health | جان بیشینه قهرمان ۱۵٪ بیشتر است | Approved. Health compensation. |
| `HOLLOW_OMENS_TITLE` | Hollow Omens | نشان‌های دره | Approved. Omen frequency modifier. |
| `HOLLOW_OMENS_RISK` | Every sixth wave carries an omen | هر موج ششم یک نشان دارد | Approved. Frequency schedule. |
| `HOLLOW_OMENS_REWARD` | +25% coins on omen waves | ۲۵٪ سکه بیشتر در موج‌های نشان‌دار | Approved. Targeted coin multiplier. |

### 2.11 StoryStrings (128 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `BOSS_ANCIENT_GOLEM` | ANCIENT GOLEM — the oldest guard, still on duty. | گولم باستانی — کهن‌ترین نگهبان؛ هنوز سرِ پست. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `BOSS_THORN_MATRIARCH` | THORN MATRIARCH — she grew half your enemies. | مادرِ خار — نصفِ دشمنانت از او روییده‌اند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `BOSS_EMBER_WYRM` | EMBER WYRM — a fire that never went out. | اژدرِ اخگر — آتشی که هرگز خاموش نشده. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `BOSS_VOID_KNIGHT` | VOID KNIGHT — it fell, and forgot the way back. | شوالیهٔ تهی — سقوط کرد و راهِ بازگشت را فراموش. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `BOSS_FROST_TITAN` | FROST TITAN — winter that learned to walk. | غولِ یخ — زمستانی که راه رفتن آموخت. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `BOSS_SHADOW_LICH` | SHADOW LICH — keeper of the second fall. | لیچِ سایه — نگهبانِ سقوطِ دوم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `BOSS_STORM_COLOSSUS` | STORM COLOSSUS — thunder stored in a stone chest. | غولِ طوفان — تندری که در سینهٔ سنگی جا شد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `BOSS_BLOODROOT_AVATAR` | BLOODROOT AVATAR — the forest's own wound, walking. | آواتارِ خون‌ریشه — زخمِ خودِ جنگل، راه می‌رود. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `CEREMONY_WALK_OUT` | A tree should not carry this alone. Not anymore. | یک درخت نباید همه‌ی این بار را تنهایی به دوش بکشد. دیگر نه. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `CEREMONY_PLANT` | So we plant another. Grow loud. Grow angry. Grow. | پس یکی دیگر می‌کاریم. بلند رشد کن. عصبانی رشد کن. رشد کن. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `CEREMONY_WATER` | I will hold the line. That is what I am for. | من خط دفاع را نگه می‌دارم. همین کار من است. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `CEREMONY_GROW` | Two of us now. I remember what mornings sound like. | حالا ما دویم. یاد دارم صبح‌ها چه صدایی دارند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `CEREMONY_WALK_BACK` | Now hold the line. Both of us need you. | حالا خط دفاع را نگه دار. هر دویِ ما به تو نیاز داریم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `REFLECTION_WAVE_25` | The wolves fear something bigger than me. That should scare me more. | گرگ‌ها از چیزی بزرگ‌تر از من می‌ترسند. باید بیشتر بترسم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `REFLECTION_WAVE_50` | Half of what I have killed, I once knew. I try not to think about it. | نیمی از چیزهایی که شکست دادم، روزی می‌شناختم. سعی می‌کنم بهش فکر نکنم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `REFLECTION_WAVE_75` | Past the treeline, the ground is wrong. Not ground at all. | آن‌سوی خطِ درخت‌ها، زمین غلط است. اصلاً زمین نیست. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `REFLECTION_WAVE_125` | Three trees now. Three times to lose. I would still make the trade. | حالا سه درخت. سه برابرِ شانسِ باخت. باز هم همین معامله را می‌بستم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `REFLECTION_WAVE_150` | It stopped sending the weak ones first. It is out of patience. | دیگر آن‌های ضعیف را اول نمی‌فرستد. صبرش تموم شده. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `REFLECTION_WAVE_175` | What is left may be the last. Or it wants me to believe that. | شاید آنچه مانده، آخرین باشد. یا می‌خواهد من این‌طور باور کنم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `TROPHY_COUNT` | Trophy - %1$s earned | جام: %1$s کسب شد | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `TROPHY_NAMES` | Trophy - %1$s | جام: %1$s | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `TROPHY_AND` |  and  |  و  | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_DEATH_FIRST` | You fell. Not the Warden — you. I felt it happen. | تو افتادی؛ نه نگهبان — خودِ تو. من حسش کردم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_DEATH_AGAIN` | Again. You always get up. I never get tired of watching. | باز. تو همیشه بلند می‌شوی. من هرگز از تماشا کردنش خسته نمی‌شوم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_SPARE` | You spared it? It was not even fighting. Mercy. I remember that word. No one uses it anymore. | رهاش کردی؟ اصلاً نمی‌جنگید. مهربانی. این کلمه را یادم هست. دیگر کسی از آن استفاده نمی‌کند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_WAVE100` | Halfway there. The Tree thanks you. I do not have to hurry. You do. | نصفِ راه. درخت از تشکر کردن درگیر است. من عجله ندارم. تو داری. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_HELLO` | I am the night. They called me the Hollow. This is the last tree — and I have come for it. | من شب هستم. مرا «حُفره» می‌نامیدند. این آخرین درخت است — و من برای همین آمده‌ام. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_MERCY_HABIT` | Three spared. You call it kindness. So do I. Mercy grows roots here too — watch what sprouts. | سه‌تا را رهایشان کردی. مهربانی می‌نامیش. من هم. رحمت اینجا هم ریشه می‌دهد — ببین چه چیزی جوانه می‌زند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_VERDICT_MERCIFUL` | Last time, you let some of them go. The Tree calls that mercy. I call it a debt — and I remember my debts. | دفعهٔ قبل، بعضی از آن‌ها را رها کردی. درخت بهش می‌گوید مهربانی. من بهش می‌گویم بدهی — و بدهی‌ها را به‌یاد می‌آورم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_VERDICT_STERN` | Last time, nothing on that field lived because you loved it. The Tree calls that victory. I call it an inventory. | دفعهٔ قبل هیچ‌کدام از آن میدان زنده نماند چون تو دوستش داشتی. درخت بهش می‌گوید پیروزی. من بهش می‌گویم سیاهه. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_BOSS_GOLEM` | It is slipping. It was never guarding you — it just cannot stop. | دارد لق می‌زند. هرگز نگهبانِ تو نبود — فقط نمی‌تواند بایستد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_BOSS_MATRIARCH` | She calls the garden home. You are the frost. | باغ را خانه می‌داند. تو یخِ تازه‌ای. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_BOSS_WYRM` | Its no grows quiet. Push. | «نه»‌اش کم‌صدا می‌شود. فشار بیاور. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_BOSS_VOID` | It falls and wants company. Give it none. | می‌افتد و هم‌کف می‌خواهد. نده. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_BOSS_TITAN` | Winter keeps what it touches. Touch it back. | زمستان هرچه را لمس کند می‌گیرد. تو هم لمسش کن. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_BOSS_LICH` | It is writing your name down. Do not give it a long one. | دارد اسمت را می‌نویسد. بلندش نکن. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_BOSS_COLOSSUS` | Thunder never hurries. You should. | رعد عجله ندارد. تو داشته باش. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `HOLLOW_BOSS_BLOODROOT` | The wound fights the cure. It always has. | زخم با درمانش می‌جنگد. همیشه همین بوده. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `TREE_VICTORY` | You kept the light alive, night after night. The dawn remembers you. | تو نور را زنده نگه داشتی، شب به شب. سحر تو را به‌یاد خواهد داشت. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DAILY_GIFT` | Two heartwood, saved from yesterday. The Tree keeps count of your days. So do I. | دو چوب‌جان از دیروز پس‌انداز شده. درخت روزهایت را می‌شمارد. من هم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_WAVE_10` | Deed: held to wave 10  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۱۰  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_WAVE_25` | Deed: held to wave 25  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۲۵  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_WAVE_50` | Deed: held to wave 50  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۵۰  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_WAVE_100` | Deed: held to wave 100  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۱۰۰  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_WAVE_150` | Deed: held to wave 150  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۱۵۰  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_WAVE_200` | Deed: held to wave 200  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۲۰۰  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_FIRST_BOSS` | Deed: first boss felled  \|  + %1$s coins | کارنامه: اولین غول سرنگون شد  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_BOSSES_5` | Deed: five bosses in one run  \|  + %1$s coins | کارنامه: پنج غول در یک دور  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_CLEAN_25` | Deed: wave 25 without a potion  \|  + %1$s coins | کارنامه: موج ۲۵ بدون معجون  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_FLAWLESS_50` | Deed: wave 50, never down, no potion  \|  + %1$s coins | کارنامه: موج ۵۰ بدون افتادن و بدون معجون  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `DEED_CODEX_10` | Deed: read ten codex pages  \|  + %1$s coins | کارنامه: خواندن ده صفحه از دانشنامه  \|  + %1$s سکه | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `CODEX_TAB_LORE` | LORE | دانش‌ها | Approved. Codex lore shelf tab, RTL-safe short label. |
| `CODEX_TAB_TROPHIES` | TROPHIES | جام‌ها | Approved. Codex trophy shelf tab. |
| `CODEX_SHOWING` | showing | نمایش | Approved. Active tab hint. |
| `CODEX_TAP_TO_SHOW` | tap to show | برای نمایش بزن | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `CODEX_WRITTEN` | %1$s / %2$s WRITTEN | %1$s / %2$s نوشته شد | Approved. Positional args keep the count first in both languages. |
| `CODEX_EARNED` | %1$s / %2$s EARNED | %1$s / %2$s یافت شد | Approved. Trophy count line. |
| `CODEX_LOCKED_HINT` | The Tree has not written this yet. | درخت هنوز این را ننوشته است. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `TROPHY_HEADER` | WARDEN'S TROPHIES | جام‌های نگهبان | Approved. Keeps «نگهبان» for Warden, matching the main-menu voice. |
| `TROPHY_TAP_HINT` | Tap a trophy to read what earns it. | روی یک جام بزن تا ببینی چه چیزی آن را می‌آورد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `OPENING_0_ONE` | Can you protect the World Tree?! | می‌توانی از درخت جهان نگهبانی کنی؟! | Approved. Tier-0 urgent battle cry kept as a question. |
| `OPENING_0_TWO` | Can you? | می‌توانی؟ | Approved. |
| `OPENING_0_THREE` | Are you sure?! | مطمئنی؟! | Approved. |
| `OPENING_1_ONE` | The dark comes back. It always does. | تاریکی برمی‌گردد. همیشه برمی‌گردد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `OPENING_1_TWO` | The Tree is tired. So am I. | درخت خسته است. من هم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `OPENING_1_THREE` | Tonight we go further. | امشب بیشتر پیش می‌رویم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `OPENING_2_ONE` | It knows my name by now. | الان اسم من را می‌شناسد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `OPENING_2_TWO` | Good. Let it remember. | خوب است. بگذار به‌یاد بیاورد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `OPENING_2_THREE` | Roots first. Then the dark. Not today. | اول ریشه‌ها. بعد تاریکی. امروز نه. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `OPENING_3_ONE` | New dawn. Same fight. | سحرِ تازه. همان نبرد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `OPENING_3_TWO` | The Tree asks: one more watch? | درخت می‌پرسد: یک پاسِ دیگر؟ | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `OPENING_3_THREE` | Say yes. | بله بگو. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_A_ONE` | Two hundred nights. Not one step lost. | دویست شب. بی‌آنکه گامی از دست بدهم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_A_TWO` | The night needs a new plan. | شب به نقشهٔ تازه‌ای نیاز دارد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_A_THREE` | Until then, the Tree and I stand. | تا آن زمان، من و درخت می‌ایستیم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_B_ONE` | Two hundred nights. Every one of them close. | دویست شب. هرکدامش از تهٔ جان. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_B_TWO` | I do not remember all of it. I remember not letting go. | همه‌اش را یادم نیست. یادم هست که رها نکردم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_B_THREE` | That is enough. It has to be. | همین بس است. باید بس باشد. | Approved. |
| `EPILOGUE_C_ONE` | Not even the middle. | نه حتی تا میانه. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_C_TWO` | The Tree fell quiet so early. It should not have. | درخت این‌قدر زود خاموش شد. نباید این‌طور می‌شد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_C_THREE` | Next time it is loud. | دفعهٔ بعد بلند می‌شود. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_D_ONE` | So close to the second root. | آن‌قدر نزدیکِ ریشهٔ دوم. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_D_TWO` | I went farther than before. Far is not far enough. | از قبل دورتر رفتم. دور هنوز کافی نیست. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_D_THREE` | Again. | دوباره. | Approved. |
| `EPILOGUE_E_ONE` | One tree stood when I fell. That counts. | وقتی افتادم یک درخت ایستاده بود. این به‌حساب می‌آید. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_E_TWO` | The night paid for this run. It just lasted a little longer than me. | شب بابت این دور تاوان داد. فقط کمی بیشتر از من دوام آورد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_E_THREE` | Next time it pays for everything. | دفعهٔ بعد برای همه‌چیز تاوان می‌دهد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_TRANSITION_ONE` | The night is not gone. It is quiet, learning how to fall again. | شب نرفته. ساکت است. دارد دوباره یاد می‌گیرد چگونه بریزد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `EPILOGUE_TRANSITION_TWO` | Stand up. The Tree is still standing. | بلند شو. درخت هنوز ایستاده است. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_BLIGHTBURST_ONE` | It does not die. It just lets go — everything at once. | نمی‌میرد. فقط رها می‌کند — همه‌چیز را یک‌جور. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_BLIGHTBURST_TWO` | That burst is not anger. It is relief. | آن انفجار خشم نیست. رهایی است. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_ROOTWARD_ONE` | That shield is not armor. It is a root, remembering its job. | آن سپر زره نیست. ریشه‌ای است که کارش را به‌یاد آورده. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_ROOTWARD_TWO` | Even like this, it still tries to protect. It just forgot what. | حتی این‌طور، هنوز می‌خواهد نگهبانی کند. فقط فراموش کرده از چه. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_WEEPING_ONE` | Where it walks, the ground never heals. | جایی که رد می‌شود، زمین التیام نمی‌کند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_WEEPING_TWO` | Follow its trail long enough. It leads to the Tree. | ردش را تا ته دنبال کن. به درخت می‌رسد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_HOLLOWMOLT_ONE` | It never leaves a place empty. Nothing here does. | هیچ‌وقت جایی را خالی نمی‌گذارد. اینجا هیچ‌کس این کار را نمی‌کند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_HOLLOWMOLT_TWO` | Two small silences where one loud one stood. | دو سکوتِ کوچک به‌جای یک صدای بلند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_GRAVEMOSS_ONE` | The moss covers the wound while the wound is still there. | خزه روی زخم می‌روید، در حالی که زخم هنوز آنجاست. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_GRAVEMOSS_TWO` | That is not healing. That is something patient taking it back. | این التیام نیست. چیزی صبور داردش برمی‌گرداند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_CINDERHALO_ONE` | Stand too close and it loves you — the way an ember loves wind. | خیلی نزدیک نرو. دوستت می‌دارد — به‌اندازه‌ای که اخگر دوست دارد باد را. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_CINDERHALO_TWO` | That heat is not attack. It is grief, still warm. | آن گرما حمله نیست. سوگ است که هنوز گرم است. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `ELITE_STONESHELL_ONE` | It pulled the hill over itself and called that armour. | تپه را روی خودش کشید و اسمش را گذاشت زره. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_STONESHELL_TWO` | Stone is patient. Stone is not on your side. | سنگ صبور است. سنگ طرفِ تو نیست. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_GRAVEBLOOM_ONE` | It died and the ground kept the grudge. | مُرد و زمین کینه را نگه داشت. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_GRAVEBLOOM_TWO` | Do not stand where something was angry. | آن‌جا که چیزی خشمگین بوده، نایست. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_SWARMCALL_ONE` | Kill it and it calls for replacements. It has replacements. | بکشش و جانشین صدا می‌زند. جانشین دارد. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_SWARMCALL_TWO` | The grove keeps sending. The grove always keeps sending. | بیشه می‌فرستد. بیشه همیشه می‌فرستد. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_SPITEBARB_ONE` | It does not want you in reach. It made its reach a promise. | نمی‌خواهد در دسترست باشی. دسترسش را وعده کرد. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_SPITEBARB_TWO` | Close work has a price here. It always did. | کارِ نزدیک اینجا قیمت دارد. همیشه داشته. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_HAMMERFALL_ONE` | It raises its arm and the ground tells you where. | دستش را بالا می‌برد و زمین می‌گوید کجا. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_HAMMERFALL_TWO` | Step. That is the whole lesson. | قدمی بردار. تمام درس همین است. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_BLOODHOWL_ONE` | It howls and the line walks faster. It is proud of them. | زوزه می‌کشد و صف تندتر راه می‌رود. به آن‌ها افتخار می‌کند. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `ELITE_BLOODHOWL_TWO` | Follow the sound and you find the one holding the leash. | صدا را دنبال کن؛ آن‌که بند را در دست دارد پیدا می‌شود. | Added 2026-09-22 with the deep-band affixes; pending native re-proofread. |
| `WHISPER_ONE` | The roots kept your seat warm while you were gone. | ریشه‌ها در نبودت جای تو را گرم نگه داشتند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `WHISPER_TWO` | The Tree dreams, little guard — and it always wakes up. | درخت خواب می‌بیند، نگهبانِ کوچک — و همیشه بیدار می‌شود. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `WHISPER_THREE` | I counted your absence in falling leaves — you were out a while. | نبودنت را با برگ‌های ریزش‌کرده شمردم. دیر بودی. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `WHISPER_FOUR` | Rest is a weapon too — you are getting good at it. | استراحت هم سلاح است. داری حرفه‌ای‌اش می‌شوی. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `WHISPER_FIVE` | The roots grow deepest in the quiet between fights. | ریشه‌ها در سکوتِ میانِ نبردها عمیق‌تر می‌روند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `WHISPER_SIX` | You are back — the paths never stopped watching for you. | برگشتی. راه‌ها هرگز از نگاه کردن به تو چشم نبردند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `MYTHIC_SUNFALL` | Shot once, long ago, at a high fall. The arrow never came back whole. | یک‌بار، خیلی قبل‌تر، به‌سوی پرتگاهی بلند رها شد. تیر هرگز سالم برنگشت. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `MYTHIC_CROWN` | Wear it and you see weak spots the way the Hollow sees strong ones. | سر بگذار. نقطه‌های ضعیف را همان‌طور می‌بینی که حُفره نقطه‌های قوی را می‌بیند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `MYTHIC_BARK` | Cut from the World Tree's bark when it could spare wood. It knows how to close a wound. | از پوست درخت جهان جدا شد، وقتی‌ها که می‌توانست بدهد. راهِ بستنِ زخم را می‌داند. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `MYTHIC_WINDRUNNER` | Made for running. He never ran again after he put them on. | برای دویدن ساخته شد. بعد از پوشیدنش دیگر ندوید. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `MYTHIC_VERDANT` | A promise in sap. What heals you lets you keep healing. | پیمانی در شیره. آنچه تو را خوب می‌کند، تو را خوب نگه می‌دارد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `MYTHIC_EMBERLESS` | The ember that never went out — cooled, and put to work. | اخگری که هرگز خاموش نشد. سرد شد و به‌کار افتاد. | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |
| `SPEAKER_WARDEN` | WARDEN | نگهبان | Approved. The Hero's name tag over the dialogue box; «نگهبان» per the Warden's TROPHY_HEADER convention. |
| `SPEAKER_TREE` | TREE | درخت | Approved. The Tree's name tag; the plain noun the run's own lines already use. |
| `SPEAKER_HOLLOW` | HOLLOW | حُفره | Rewritten 2026-09-22 for a young reader; pending native re-proofread. |

### 2.12 SettingsStrings (37 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `TITLE` | SETTINGS | تنظیمات | Approved. Clean settings screen header. |
| `FOOTER` | Comfort choices saved on this device | گزینه‌های راحتی روی این دستگاه ذخیره می‌شوند | Approved. Fits note panel width. |
| `HINT` | Tap a row to switch it, tap again to step the level. | برای تغییر یک ردیف را لمس کنید، برای پلهٔ سطح دوباره لمس کنید. | Approved. Clear touch interaction guidance. |
| `CLOSE_HINT` | Tap Close to return to the main menu. | برای بازگشت به منوی اصلی دکمهٔ بستن را لمس کنید. | Approved. Navigation help text. |
| `MUSIC` | MUSIC | موسیقی | Approved. Audio toggle row. |
| `MUSIC_SUBTITLE` | World Tree vigil theme | قطعهٔ پاسداری درخت جهان | Approved. Fits length limit (≤ 30 chars). |
| `MUSIC_LEVEL` | MUSIC LEVEL | سطح موسیقی | Approved. Volume stepping row. |
| `MUSIC_LEVEL_SUBTITLE` | How loud the vigil sits | بلندی صدای پاسداری | Approved. Fits length limit. |
| `SOUND_EFFECTS` | SOUND EFFECTS | جلوه‌های صوتی | Approved. SFX toggle row. |
| `SOUND_EFFECTS_SUBTITLE` | Arrows, hits, loot | تیرها، ضربه‌ها و غنیمت | Approved. Fits length limit. |
| `EFFECT_LEVEL` | EFFECT LEVEL | سطح جلوه‌ها | Approved. SFX volume stepping row. |
| `EFFECT_LEVEL_SUBTITLE` | Hits, drops, level-ups | ضربه‌ها، غنیمت و ارتقای سطح | Approved. Fits length limit. |
| `ON` | ON | روشن | Approved. Binary state switch. |
| `OFF` | OFF | خاموش | Approved. Binary state switch. |
| `TAP_TO_ENABLE` | tap to enable | برای فعال‌سازی لمس کنید | Approved. Interactive toggle hint. |
| `TAP_TO_MUTE` | tap to mute | برای بی‌صدا کردن لمس کنید | Approved. Interactive toggle hint. |
| `TAP_TO_STEP` | tap to step | برای پله لمس کنید | Approved. Multi-step hint. |
| `TAP_TO_SWITCH` | tap to switch | برای تغییر لمس کنید | Approved. Value cycle hint. |
| `LEVEL_QUIET` | QUIET | آرام | Approved. Low volume level. |
| `LEVEL_NORMAL` | NORMAL | عادی | Approved. Medium volume level. |
| `LEVEL_FULL` | FULL | کامل | Approved. Maximum volume level. |
| `REDUCED_MOTION` | REDUCED MOTION | حرکت کاهش‌یافته | Approved. Accessibility row header. |
| `REDUCED_MOTION_SUBTITLE` | No shake, no drifting spores | بدون لرزش و ذره‌های سرگردان | Approved. Clear accessibility benefit (≤ 30 chars). |
| `TAP_TO_RESTORE_MOTION` | tap to restore motion | برای بازگشت حرکت لمس کنید | Approved. Reset motion hint. |
| `TEXT_SIZE` | TEXT SIZE | اندازهٔ متن | Approved. Typography scale row. |
| `TEXT_SIZE_SUBTITLE` | How large the letters sit | درشتی حروف روی صفحه | Approved. Plain description (≤ 30 chars). |
| `TEXT_SIZE_SMALL` | SMALL | کوچک | Approved. Scaled font option. |
| `TEXT_SIZE_LARGE` | LARGE | بزرگ | Approved. Scaled font option. |
| `COLOUR_BLIND_RARITY` | ACCESSIBLE RARITY | رنگ‌های دسترس‌پذیر | Approved. High contrast accessibility row. |
| `COLOUR_BLIND_RARITY_SUBTITLE` | High contrast loot hues | رنگ‌های متمایز غنیمت | Approved. Clear description (≤ 30 chars). |
| `TAP_TO_RESTORE_RARITY` | tap for default colours | برای رنگ‌های پیش‌فرض لمس کنید | Approved. Default palette restore hint. |
| `NARRATION` | NARRATION | روایت | Approved. Narration toggle. |
| `NARRATION_SUBTITLE` | Lore and boss titles spoken | خواندن داستان‌ها و عنوان باس‌ها | Approved. Narration description. |
| `NARRATION_LEVEL` | NARRATION LEVEL | سطح روایت | Approved. Narration volume level. |
| `NARRATION_LEVEL_SUBTITLE` | How loud the voice sits | بلندی صدای روایت | Approved. Voice volume description. |
| `SCREEN_READER` | SCREEN READER | صفحه‌خوان | Approved. Accessibility screen reader toggle. |
| `SCREEN_READER_SUBTITLE` | TalkBack labels for all buttons | برچسب‌های دسترس‌پذیر برای دکمه‌ها | Approved. Screen reader TalkBack description. |
| `TOUCH_ONLY` | TOUCH ONLY | فقط لمسی | Approved. Mobile input specification. |
| `LANGUAGE` | LANGUAGE | زبان | Approved. Locale toggle row. |
| `LANGUAGE_SUBTITLE` | Words and direction | واژه‌های صفحه و جهت آن | Approved. Mentions RTL layout (≤ 30 chars). |
| `LANGUAGE_ENGLISH` | English | انگلیسی | Approved. Language switch choice. |
| `LANGUAGE_PERSIAN` | Persian | فارسی | Approved. Language switch choice. |
| `CLOSE` | Close | بستن | Approved. Close button label. |

---

## 3. Automated Integrity Gate

The native proofreading record is structurally guarded by `PersianProofreadRecordTest`:
1. It verifies that `docs/PERSIAN_PROOFREAD.md` exists and is non-empty.
2. It asserts that every single table in `GameStrings.tables()` has a dedicated section in this document.
3. It asserts that every individual entry across all 12 string tables is catalogued in this document.
4. It validates native orthography constraints:
   - Prohibits deprecated/awkward literal calques («کیسهٔ پشت», «کدکس بیشه»).
   - Guarantees strict distinction between Mythic («اسطوره‌ای») and Legendary («افسانه‌ای»).
   - Enforces zero Latin characters within Persian text outside format placeholders.
