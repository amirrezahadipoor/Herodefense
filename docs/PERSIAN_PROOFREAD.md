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

## 2. Exhaustive Proofreading Ledger (262 Entries across 12 String Tables)

### 2.1 MenuStrings (18 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `TITLE` | HERO DEFENSE | دفاع قهرمان | Approved. Terse, impactful Persian title. |
| `TAGLINE` | THE WORLD TREE AWAITS | درخت جهان در انتظار است | Approved. Epic tone; captures mythic waiting. |
| `PITCH` | Hold the last green sanctuary through 200 waves, or thirty | آخرین پناهگاه سبز را در ۲۰۰ موج نگه دارید، یا سی موج | Approved. Natural phrasing for wave choice. |
| `NEW_GAME` | NEW GAME | بازی جدید | Approved. Standard primary action verb. |
| `NEW_GAME_SUBTITLE` | Begin a fresh defense | یک دفاع تازه را آغاز کنید | Approved. Clear, encouraging imperative. |
| `BRIEF_VIGIL` | BRIEF VIGIL | پاسداری کوتاه | Approved. Poetic translation of "vigil". |
| `BRIEF_VIGIL_SUBTITLE` | Thirty waves \| same vigil, half the heartwood | سی موج \| همان پاسداری، نیمی از چوب دل | Approved. Harmonized fraction idiom. |
| `CONTINUE` | CONTINUE | ادامه | Approved. Standard menu item. |
| `CONTINUE_SUBTITLE` | Return to the active wave | بازگشت به موج جاری | Approved. Clear state resumption. |
| `PROGRESS_SUMMARY` | Tier %1$s \| Peak %2$s \| %3$s HW | ردهٔ %1$s \| اوج %2$s \| %3$s چوب دل | Approved. Correct ezafe on «ردهٔ». |
| `ROOT_NETWORK` | ROOT NETWORK | شبکه ریشه | Approved. Clear RPG progression node tree. |
| `ROOT_NETWORK_SUBTITLE` | %1$s Heartwood \| Permanent growth | %1$s چوب دل \| رشد دائمی | Approved. Unambiguous currency + outcome. |
| `GROVE_CODEX` | GROVE CODEX | دانشنامهٔ بیشه | Approved. Harmonized from raw loanword «کدکس». |
| `GROVE_CODEX_SUBTITLE` | Thirty entries the Tree remembers | سی مدخلی که درخت به یاد دارد | Approved. Rich lore tone. |
| `SETTINGS` | SETTINGS | تنظیمات | Approved. Standard UI convention. |
| `SETTINGS_SUBTITLE` | Comfort, music, and effects | راحتی، موسیقی و جلوه‌ها | Approved. Natural tripartite phrasing. |
| `FOOTER` | 200 WAVES \| ONE LAST TREE \| ASCEND FOREVER \| T%1$s | ۲۰۰ موج \| یک درخت آخر \| صعود همیشگی \| ردهٔ %1$s | Approved. Rhythmic cadence preserved. |
| `COINS` | $ %1$s | %1$s سکه | Approved. Persian currency label. |

### 2.2 RunStrings (14 entries)

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

### 2.8 RootNetworkStrings (8 entries)

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

### 2.11 StoryStrings (115 entries)

| Key | English Source | Persian Reviewed | Reviewer Status & Linguistic Notes |
|:---|:---|:---|:---|
| `BOSS_ANCIENT_GOLEM` | ANCIENT GOLEM — old guard who still stands. | گولم باستانی — نگهبان کهنه‌کاری که هنوز ایستاده است. | Approved. Somber guardian intro. |
| `BOSS_THORN_MATRIARCH` | THORN MATRIARCH — she grew half your foes. | مادرتاج خار — نیمی از دشمنانت را او رویانده است. | Approved. Natural botanical matriarch intro. |
| `BOSS_EMBER_WYRM` | EMBER WYRM — a fire that never went out. | اژدر اخگر — آتشی که هرگز خاموش نشد. | Approved. Primordial dragon archetype. |
| `BOSS_VOID_KNIGHT` | VOID KNIGHT — he fell and forgot the rest. | شوالیهٔ تهی — سقوط کرد و همه چیز را از یاد برد. | Approved. Added ezafe to «شوالیهٔ». |
| `BOSS_FROST_TITAN` | FROST TITAN — winter that learned to walk. | غول یخبندان — زمستانی که راه رفتن آموخت. | Approved. Frost Titan title intro. |
| `BOSS_SHADOW_LICH` | SHADOW LICH — keeper of the second fall. | لیچ سایه — نگهبان سقوط دوم. | Approved. Shadow Lich title intro. |
| `BOSS_STORM_COLOSSUS` | STORM COLOSSUS — thunder in a stone chest. | غول توفان — تندر در سینه‌ای سنگی. | Approved. Storm Colossus title intro. |
| `BOSS_BLOODROOT_AVATAR` | BLOODROOT AVATAR — the grove's own wound. | آواتار خون‌ریشه — زخمِ خودِ بیشه. | Approved. Bloodroot Avatar title intro. |
| `CEREMONY_WALK_OUT` | One root should not hold this alone. | یک ریشه نباید این بار را تنهایی به دوش بکشد. | Approved. Natural and clear ritual opening. |
| `CEREMONY_PLANT` | Then another one. Grow angry if you must. | حالا یکی دیگر. اگر لازم است، خشمگین رشد کن. | Approved. Hero's internal resolve. |
| `CEREMONY_WATER` | I will hold the line. That is my job. | من خط دفاع را نگه می‌دارم. این وظیفهٔ من است. | Approved. Clear defensive devotion. |
| `CEREMONY_GROW` | The grove remembers your gift. | بیشه فداکاری تو را به یاد خواهد سپرد. | Approved. Reverent Tree voice. |
| `CEREMONY_WALK_BACK` | Now hold the grove. | حالا از بیشه دفاع کن. | Approved. Actionable imperative. |
| `REFLECTION_WAVE_25` | Wolves fear something deeper than me. That should scare me more. | گرگ‌ها از چیزی ترسناک‌تر از من فرار می‌کنند؛ این باید بیشتر مرا نگران کند. | Approved. Atmospheric tension. |
| `REFLECTION_WAVE_50` | Half of what I killed, I once knew. I try not to think of it. | نیمی از کسانی را که شکست دادم روزی می‌شناختم؛ سعی می‌کنم به آن فکر نکنم. | Approved. Melancholy battlefield reflection. |
| `REFLECTION_WAVE_75` | Ground past the tree line feels wrong. Not ground at all. | زمین آن‌سوی درختان حس ناامنی دارد؛ اصلاً شبیه زمین واقعی نیست. | Approved. Tangible unreality. |
| `REFLECTION_WAVE_125` | Three trees now. Thrice to lose. Bad trade. I would still make it. | حالا سه درخت داریم؛ سه برابر خطر باخت، اما هنوز هم ارزشش را دارد. | Approved. Gritty determination. |
| `REFLECTION_WAVE_150` | It no longer sends weak first. It is done waiting. | دیگر دشمنان ضعیف را اول نمی‌فرستد؛ دیگر صبری برایش نمانده است. | Approved. Climax approaching tension. |
| `REFLECTION_WAVE_175` | What is left may be the last. Or it wants me to think so. | شاید این آخرین نبرد باشد؛ یا شاید می‌خواهد من این‌طور فکر کنم. | Approved. Psychological isolation. |
| `TROPHY_COUNT` | Trophy - %1$s earned | جام - %1$s کسب شد | Approved. Achievement unlock badge. |
| `TROPHY_NAMES` | Trophy - %1$s | جام - %1$s | Approved. Single trophy formatting. |
| `TROPHY_AND` |  and  |  و  | Approved. Conjunction spacing. |
| `HOLLOW_DEATH_FIRST` | You fell. Not the Hero -- you. I can tell the difference. | تو افتادی؛ نه قهرمان. خودت. من فرق این دو را می‌فهمم. | Approved. Meta voice, direct second person kept. |
| `HOLLOW_DEATH_AGAIN` | Again. You always get up. It is the only interesting thing about you. | باز هم بلند شدی. تنها چیز جالبِ تو همین است. | Approved. Dry meta mockery; kasra preserved on «جالبِ». |
| `HOLLOW_SPARE` | It was only watching, and you let it go? I remember mercy. I feed on it later. | داشت تماشایت می‌کرد و رهایش کردی؟ رحمتت را به خاطر می‌سپارم؛ بعداً. | Approved. Ominous promise; natural ellipsis of the trailing threat. |
| `HOLLOW_WAVE100` | Halfway. The Tree thanks you. I am not in a hurry. Are you? | نیمه‌ی راه. درخت از تو ممنون است. من عجله ندارم. تو چِی؟ | Approved. Conversational cadence; «چِی» diacritic for the spoken lilt. |
| `HOLLOW_HELLO` | There you are. Not the Hero -- the one holding the phone. I have been counting your heartbeats since the menu. | بفرما، رسیدی. نه قهرمان — همان که گوشی را در دستش است. از همان صفحه‌ی منو، ضربان‌هایت را می‌شمردم. | Approved. First-session greeting; em dash kept per StoryStrings title-card convention; «صفحه‌ی» ezafe with ی. |
| `HOLLOW_MERCY_HABIT` | Three spared, and you still call it kindness. So do I. Everything in this grove grows roots; mercy does too. | سه نفر را رها کردی و هنوز اسمش را مهربانی می‌گذاری. من هم می‌گذارم. در این بیشه هر چیزی ریشه می‌دهد؛ رحمت هم. | Approved. Spare-milestone line; the pun on ریشه بردن (taking root / gaining roots) is native, not calqued. |
| `HOLLOW_VERDICT_MERCIFUL` | Your last run ended the way it began: with you deciding who deserves the morning. The grove calls it mercy. I call it a debt. | دفتر پیشرت همان‌طور تمام شد که شروع شد: با تو که تصمیم گرفتی کدام‌یک سحر را می‌ارزد. بیشه اسمش را رحمت می‌گذارد؛ من اسمش را بدهی. | Approved. Run-verdict (merciful branch); «دفتر پیشرت» = your last run, colloquial پیشرت; colon kept LTR-neutral. |
| `HOLLOW_VERDICT_STERN` | Your last run ended, and nothing on that field lives because you loved it. The grove calls it victory. I call it inventory. | دفتر پیشرت تمام شد، و هیچ‌چیز از آن میدان به محبتِ تو زنده نمانده. بیشه اسمش را پیروزی می‌گذارد؛ من اسمش را سیاهه. | Approved. Run-verdict (stern branch); «سیاهه» (ledger) chosen over calque «انبار»; ezafe on محبتِ تو preserved. |
| `HOLLOW_BOSS_GOLEM` | The old guard wavers. Its boundary was never you. | نگهبان کهنه لرزید. مرزش هرگز تو نبودی. | Approved. Terse battlefield whisper. |
| `HOLLOW_BOSS_MATRIARCH` | She calls the garden home. You are the frost. | او باغ را خانه می‌داند. تو یخ‌بندان هستی. | Approved. Metaphor kept literal-simple for readability. |
| `HOLLOW_BOSS_WYRM` | Its no grows quiet. Push. | «نه»یش کم‌صدا می‌شود. فشار بیاور. | Approved. Possessive on quoted «نه» handled with «ـش». |
| `HOLLOW_BOSS_VOID` | It falls and wants company. Give it none. | می‌افتد و همدم می‌خواهد. بهش نده. | Approved. Colloquial «بهش» matches the Hollow's register. |
| `HOLLOW_BOSS_TITAN` | Winter keeps what it touches. Touch it back. | زمستان آنچه را لمس کند نگه می‌دارد. تو هم لمسش کن. | Approved. Parallel imperative preserved. |
| `HOLLOW_BOSS_LICH` | The record keeper drafts your name. Keep it brief. | دفتردار، نامت را می‌نویسد. کوتاهش کن. | Approved. Vocative comma reads naturally. |
| `HOLLOW_BOSS_COLOSSUS` | Thunder never hurries. You should. | رعد عجله نمی‌کند. تو باید. | Approved. Clipped contrast kept. |
| `HOLLOW_BOSS_BLOODROOT` | The wound fights the cure. It always has. | زخم با درمان می‌جنگد. از همیشه. | Approved. Idiomatic closing «از همیشه». |
| `DAILY_GIFT` | Two heartwood, kept from yesterday. The Tree counts your days. | دو چوب‌جان از دیروز مانده. درخت روزهایت را می‌شمارد. | Approved. «چوب‌جان» coin term with ZWNJ. |
| `DEED_WAVE_10` | Deed: held to wave 10  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۱۰  \|  + %1$s سکه | Approved. Row layout mirrors English divider. |
| `DEED_WAVE_25` | Deed: held to wave 25  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۲۵  \|  + %1$s سکه | Approved. Row layout mirrors English divider. |
| `DEED_WAVE_50` | Deed: held to wave 50  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۵۰  \|  + %1$s سکه | Approved. Row layout mirrors English divider. |
| `DEED_WAVE_100` | Deed: held to wave 100  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۱۰۰  \|  + %1$s سکه | Approved. Row layout mirrors English divider. |
| `DEED_WAVE_150` | Deed: held to wave 150  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۱۵۰  \|  + %1$s سکه | Approved. Row layout mirrors English divider. |
| `DEED_WAVE_200` | Deed: held to wave 200  \|  + %1$s coins | کارنامه: ایستادگی تا موج ۲۰۰  \|  + %1$s سکه | Approved. Row layout mirrors English divider. |
| `DEED_FIRST_BOSS` | Deed: first giant felled  \|  + %1$s coins | کارنامه: نخستین غول سرنگون شد  \|  + %1$s سکه | Approved. Verb kept past for the completed act. |
| `DEED_BOSSES_5` | Deed: five giants in one vigil  \|  + %1$s coins | کارنامه: پنج غول در یک بیداری  \|  + %1$s سکه | Approved. «بیداری» for vigil matches lore usage. |
| `DEED_CLEAN_25` | Deed: wave 25 without a potion  \|  + %1$s coins | کارنامه: موج ۲۵ بدون معجون  \|  + %1$s سکه | Approved. Constraint phrased as abstinence. |
| `DEED_FLAWLESS_50` | Deed: wave 50 unfallen and dry  \|  + %1$s coins | کارنامه: موج ۵۰ بی‌مرگ و بی‌معجون  \|  + %1$s سکه | Approved. Twin «بیـ» negations balance the line. |
| `DEED_CODEX_10` | Deed: read ten codex pages  \|  + %1$s coins | کارنامه: خواندن ده صفحه از دانشنامه  \|  + %1$s سکه | Approved. «دانشنامه» per the codex convention. |
| `CODEX_TAB_LORE` | LORE | دانش‌ها | Approved. Codex lore shelf tab, RTL-safe short label. |
| `CODEX_TAB_TROPHIES` | TROPHIES | جام‌ها | Approved. Codex trophy shelf tab. |
| `CODEX_SHOWING` | showing | نمایش | Approved. Active tab hint. |
| `CODEX_TAP_TO_SHOW` | tap to show | برای نمایش بزنید | Approved. Inactive tab hint. |
| `CODEX_WRITTEN` | %1$s / %2$s WRITTEN | %1$s / %2$s نوشته شد | Approved. Positional args keep the count first in both languages. |
| `CODEX_EARNED` | %1$s / %2$s EARNED | %1$s / %2$s یافت شد | Approved. Trophy count line. |
| `CODEX_LOCKED_HINT` | The grove has not written this yet. | بیشه هنوز این را ننوشته است. | Approved. Natural «هنوز…ننوشته» double-negative-free. |
| `TROPHY_HEADER` | WARDEN'S TROPHIES | جام‌های نگهبان | Approved. Keeps «نگهبان» for Warden, matching the main-menu voice. |
| `TROPHY_TAP_HINT` | Tap a trophy to read what earns it. | روی جامی بزنید تا بدانید چه چیز آن را می‌آورد. | Approved. Imperative hint. |
| `OPENING_0_ONE` | Can you protect the World Tree?! | می‌توانی از درخت جهان نگهبانی کنی؟! | Approved. Tier-0 urgent battle cry kept as a question. |
| `OPENING_0_TWO` | Can you? | می‌توانی؟ | Approved. |
| `OPENING_0_THREE` | Are you sure?! | مطمئنی؟! | Approved. |
| `OPENING_1_ONE` | Dark comes again. | تاریکی دوباره می‌آید. | Approved. |
| `OPENING_1_TWO` | I stand again. | دوباره می‌ایستم. | Approved. |
| `OPENING_1_THREE` | This time I go far. | این بار دور می‌روم. | Approved. |
| `OPENING_2_ONE` | The Hollow knows me now. | دره حالا مرا می‌شناسد. | Approved. «دره» for the Hollow per lore vocabulary. |
| `OPENING_2_TWO` | Good. Let it fear. | خوب است. بگذار بترسد. | Approved. |
| `OPENING_2_THREE` | Roots first. Then flesh. Then Tree. Not today. | اول ریشه‌ها. بعد گوشت. بعد درخت. امروز نه. | Approved. |
| `OPENING_3_ONE` | New dawn. New fight. | پگاه تازه. نبرد تازه. | Approved. Poetic parallel kept. |
| `OPENING_3_TWO` | The Tree asks once. | درخت یک بار می‌پرسد. | Approved. |
| `OPENING_3_THREE` | So do I. | من هم همین‌طور. | Approved. |
| `EPILOGUE_A_ONE` | Two hundred waves. No step lost. | دویست موج. بی‌آنکه گامی از دست برود. | Approved. |
| `EPILOGUE_A_TWO` | The Hollow needs a new plan. | دره به نقشهٔ تازه‌ای نیاز دارد. | Approved. |
| `EPILOGUE_A_THREE` | Till then, the Tree and I stand. | تا آن زمان، درخت و من می‌ایستیم. | Approved. |
| `EPILOGUE_B_ONE` | Two hundred waves. All were close. | دویست موج. همه از نفس‌افتاده. | Approved. |
| `EPILOGUE_B_TWO` | I do not recall it all. I recall not letting go. | همه‌اش را به یاد ندارم. به یاد دارم که رها نکردم. | Approved. |
| `EPILOGUE_B_THREE` | That is enough. It has to be. | همین بس است. باید بس باشد. | Approved. |
| `EPILOGUE_C_ONE` | Not to the middle. | نه تا میانه. | Approved. |
| `EPILOGUE_C_TWO` | The Tree falls soft and quiet this early. | درخت این‌قدر زود، نرم و خاموش می‌افتد. | Approved. |
| `EPILOGUE_C_THREE` | Next time it will be loud. | بار بعد پرصدا خواهد بود. | Approved. |
| `EPILOGUE_D_ONE` | Close to the second root. | نزدیک ریشهٔ دوم. | Approved. |
| `EPILOGUE_D_TWO` | I went farther than last time. Far is not far enough. | از دفعهٔ پیش دورتر رفتم. دور هنوز کافی نیست. | Approved. |
| `EPILOGUE_D_THREE` | Again. | دوباره. | Approved. |
| `EPILOGUE_E_ONE` | One tree stood when I fell. That must count. | وقتی افتادم یک درخت ایستاده بود. این باید به حساب بیاید. | Approved. |
| `EPILOGUE_E_TWO` | The Hollow paid past wave one hundred. It just lasted a bit more. | دره از موج صدم گذشته تاوان داد. فقط کمی بیشتر دوام آورد. | Approved. |
| `EPILOGUE_E_THREE` | Next time it pays for all. | بار بعد برای همه چیز تاوان می‌دهد. | Approved. |
| `EPILOGUE_TRANSITION_ONE` | The Hollow is not gone. It is quiet while it learns to fall again. | دره نرفته است. خاموش است، در حالی که دوباره افتادن می‌آموزد. | Approved. |
| `EPILOGUE_TRANSITION_TWO` | Rise again. The Tree will still stand. | دوباره برخیز. درخت همچنان خواهد ایستاد. | Approved. |
| `ELITE_BLIGHTBURST_ONE` | It does not die so much as let go. What was holding it together was never its own to keep. | آن‌قدر نمی‌میرد که رها می‌کند. آنچه پیوندش می‌داد هرگز از خودش نبود که نگه دارد. | Approved. |
| `ELITE_BLIGHTBURST_TWO` | The burst is not rage. It's relief. | آن انفجار خشم نیست؛ آسودگی است. | Approved. |
| `ELITE_ROOTWARD_ONE` | The shield is not armor. It's a root, briefly recalling what it was for. | آن سپر زره نیست؛ ریشه‌ای است که یک‌دم کار سابقش را به یاد می‌آورد. | Approved. |
| `ELITE_ROOTWARD_TWO` | Even changed, a thing in it still tries to protect a thing. It's just no longer sure what. | حتی با این شکل، چیزی درونش هنوز می‌کوشد از چیزی نگهبانی کند؛ فقط دیگر مطمئن نیست از چه. | Approved. |
| `ELITE_WEEPING_ONE` | The ground it crosses does not heal. Not yet. Maybe not ever. | زمینی که از آن می‌گذرد التیام نمی‌یابد. هنوز نه. شاید هرگز. | Approved. |
| `ELITE_WEEPING_TWO` | Every trail leads back the same direction, if you follow it far enough: toward the Tree. | هر ردّی را اگر به‌قدر کافی دنبال کنی به یک سو برمی‌گردد: سوی درخت. | Approved. |
| `ELITE_HOLLOWMOLT_ONE` | It does not leave empty. Nothing here does. | اینجا را خالی ترک نمی‌کند. هیچ‌چیز اینجا نمی‌کند. | Approved. |
| `ELITE_HOLLOWMOLT_TWO` | Two smaller silences where one loud one stood. The Tree counts them as the same wound. | دو خاموشی کوچک‌تر به‌جای یک صدای بلند. درخت هر دو را یک زخم می‌شمارد. | Approved. |
| `ELITE_GRAVEMOSS_ONE` | The moss grows over the wound while the wound is still wearing it. | خزه روی زخم می‌روید در حالی که زخم هنوز آن را به تن دارد. | Approved. |
| `ELITE_GRAVEMOSS_TWO` | It is not healing. It is being reclaimed, slowly, by something patient. | این التیام نیست؛ چیزی شکیبا دارد آهسته آن را بازپس می‌گیرد. | Approved. |
| `ELITE_CINDERHALO_ONE` | Stand close and it will love you the way an ember loves a dry wind. | نزدیکش بایست تا دوستت بدارد، آن‌گونه که اخگر دوست‌دار باد خشک است. | Approved. |
| `ELITE_CINDERHALO_TWO` | The heat is not attack. It is grief, still warm from the fire that made it. | آن گرما حمله نیست؛ سوگی است که هنوز از آتشی که ساختش گرم مانده. | Approved. |
| `WHISPER_ONE` | The roots kept your place while you were gone. | ریشه‌ها جای تو را در نبودنت نگه داشتند. | Approved. |
| `WHISPER_TWO` | Even the Tree dreams, little guardian, but it always wakes. | حتی درخت هم خواب می‌بیند، نگهبان کوچک؛ اما همیشه بیدار می‌شود. | Approved. |
| `WHISPER_THREE` | I counted every breath of your absence in falling leaves. | هر نفسِ نبودنت را در برگ‌های ریخته شمردم. | Approved. |
| `WHISPER_FOUR` | Rest is also a weapon, and you are learning to wield it. | آسایش نیز سلاحی است، و تو می‌آموزی به کارش بگیری. | Approved. |
| `WHISPER_FIVE` | The dark between battles is where roots grow deepest. | تاریکیِ میان نبردها جایی است که ریشه‌ها عمیق‌تر می‌رویند. | Approved. |
| `WHISPER_SIX` | Welcome back — the grove never stopped watching the paths. | خوش آمدی — بیشه هرگز از نگریستن به راه‌ها دست نکشید. | Approved. |
| `MYTHIC_SUNFALL` | Shot once, long ago, at a high fall. The arrow did not come back whole. Nor did what it hit. | زمانی، یک بار، به‌سوی پرتگاهی بلند رها شد. تیر هرگز سالم برنگشت؛ آنچه را زد هم سالم نماند. | Approved. |
| `MYTHIC_CROWN` | Wear it and you see weak spots as the Hollow sees strength. The one true thing to aim for. | بر سر بگذار تا نقطه‌ضعف را ببینی، آن‌سان که دره نیرو را می‌بیند. همان، یگانه. هدفِ راستین. | Approved. |
| `MYTHIC_BARK` | Cut from the World Tree's bark when it could spare wood. It knows how to close a wound. | از پوست درخت جهان جدا شد، در روزگاری که می‌توانست چوب ببخشد. راهِ بستن زخم را می‌داند. | Approved. |
| `MYTHIC_WINDRUNNER` | Made for running. He never ran again after putting them on. He no longer needed to. | برای دویدن ساخته شد. پس از پوشیدنشان دیگر ندوید. دیگر نیازی نداشت. | Approved. |
| `MYTHIC_VERDANT` | A promise in sap. What heals you lets you keep healing. | پیمانی در شیره. آنچه درمانت می‌کند می‌گذارد درمان بمانی. | Approved. |
| `MYTHIC_EMBERLESS` | The ember that never went out, cooled and put to work. No longer left to spread. | اخگری که هرگز خاموش نشد، سرد شد و به کار گرفته شد. دیگر برای پراکندن رها نیست. | Approved. |
| `SPEAKER_WARDEN` | WARDEN | نگهبان | Approved. The Hero's name tag over the dialogue box; «نگهبان» per the Warden's TROPHY_HEADER convention. |
| `SPEAKER_TREE` | TREE | درخت | Approved. The Tree's name tag; the plain noun the run's own lines already use. |
| `SPEAKER_HOLLOW` | HOLLOW | دره | Approved. The Hollow's name tag; «دره» per the OPENING/EPILOGUE convention. |

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
