# Hero Defense — Story Content

> **Voice rules live in `docs/STORY_VOICE.md`:** short lines, common words, one idea per line,
> funny beats heartfelt beats cool. Every line below follows those rules, and every one ships
> in English only (P2, 2026-09-23: the Persian translation was deleted by the owner's order —
> this file's words are the shipped words).
>
> **P3 note:** this file was deleted and rebuilt with the new story on 2026-09-23 (new cast,
> new dialogue). The script source is `MEMORY.md` §9; the shipped words live in
> `i18n/StoryStrings` (dialogue), `story/LoreCatalog` (codex), `story/BossLore` (bios) and
> `story/TreeLetters` (letters). Every quoted line below is verbatim from those files.

Full narrative text to drop straight into the systems that speak it. Five voices, kept distinct
on purpose — the dialogue box prints each speaker's name in their own colour:

- **The Warden (white)** — the smallest Warden yet, a kid with a bow. Terse, present-tense,
  almost silent: deeds, the ceremony's plant beats, the epilogues' first two thirds, one final
  line. When he talks, the run stops to listen.
- **Granny (leaf-green)** — the World Tree herself. Warm, plain, counting every dawn like a
  gift: the whole Grove Codex, the boss bios, the ten letters, the victory line, the daily
  gift, the ceremony's Granny beats.
- **The Hollow (red)** — the night itself, speaking directly to the player holding the phone
  (roadmap ST1), never to the Warden. Deadpan: deaths, mercies, verdicts, chapter cards.
- **Pip (gold)** — a lantern-firefly with a big mouth and a bigger heart, riding the Warden's
  shoulder. Openings, milestones, field notes, whispers, comebacks, epilogue thirds.
- **The Night Shift (ember)** — the eight ridiculous bosses. They speak ONLY in the
  watch-only intro cutscene before their wave: title card, trash-talk, Pip's comeback.
  Mid-fight they are silent, always (§6 of MEMORY, the silence rule).

Nothing here needs new art. It is text plus the unlock/trigger conditions to wire it to.

## World Premise (one paragraph, for internal reference — not shown in-game)

Long ago the Night (the **Hollow**) swallowed every light but one: one grove, one great Tree.
You are the smallest Warden yet — a kid with a bow. On your shoulder rides **Pip**, a
lantern-firefly with a big mouth and a bigger heart. The Tree — **Granny** — has three seeds
left and a lot of soup. The Hollow, too lazy to fight fair, hired help: eight odd bosses
called **the Night Shift**. Every fifth night, one of them clocks in. They talk big. You bonk
them. All 200 nights. Then dawn.

## 1. Opening Cinematics by Ascension Tier

Three lines each, all Pip's, typed in the box over the push-in. Tier 0 is Pip meeting you;
1 and 2 their own; 3+ one shared set (`StoryStrings.OPENING_*`, `gameplay/OpeningCinematic`).

**Tier 0 (first run ever)**
1. "Hey! Hey you! With the bow!"
2. "I'm Pip. You're the new Chief."
3. "Stay close. The Night is coming."

**Tier 1**
1. "Back again, Chief?"
2. "Granny saved you some light."
3. "Tonight we go further."

**Tier 2**
1. "The Night knows your name now."
2. "Good. Let it shake."
3. "Pip's got a plan!"

**Tier 3 and every tier after (reused as-is — do not author one set per tier)**
1. "New night. Same Chief."
2. "Granny says hi."
3. "Let's bonk the dark."

## 2. Mid-Run Story Beats

Single lines, shown the same way the opening's beats are shown, but one at a time and briefly —
no new animation, just the existing text overlay triggered at a wave boundary. Silent,
skippable on tap, exactly like the opening. Nothing here ever speaks during combat.

### 2.1 First-encounter boss title cards

Shown once only, the very first time each identity is fought (not on every repeat appearance).
In P4 these cards move into the boss-intro cutscenes (`StoryStrings.BOSS_*`).

- **Ancient Golem**, first meeting: *"GRUM — Night Shift Security. Do not wake."*
- **Thorn Matriarch**, first meeting: *"MAMA BRAMBLE — She grew half the bad guys."*
- **Ember Wyrm**, first meeting: *"SIZZLE — The hottest star of the Night."*
- **Void Knight**, first meeting: *"SIR FALLS-A-LOT — Very polite. Very clumsy."*
- **Frost Titan**, first meeting: *"BIG CHILL — Evil? Or just on holiday?"*
- **Shadow Lich**, first meeting: *"OLD PAGE — Librarian. Fines are final."*
- **Storm Colossus**, first meeting: *"CAPTAIN THUNDER — Scared of puddles."*
- **Bloodroot Avatar**, first meeting: *"BLUSH — Sorry about this. Really sorry."*

### 2.2 Pip's milestone beats (one loud line at a wave start, never mid-fight)

- **Wave 25:** "Twenty-five nights! Pip counted!"
- **Wave 50:** "Fifty nights! A new tree today!"
- **Wave 75:** "The dark is thick. My butt glows."
- **Wave 125:** "Half the Night Shift owes me coins."
- **Wave 150:** "Last seed tonight, Chief! Hold on!"
- **Wave 175:** "Almost dawn, Chief. Almost."

### 2.3 The three plantings (spoken lines, synced to the existing timeline)

Sprout's short rite at wave 50 and Leaf's at 150 walk Pip, the Warden, Granny; Twig's full
five-beat rite at wave 100 walks Granny, the Warden, Pip, Granny, Pip
(`story/CeremonyLines`, `gameplay/PlantingCeremony`).

**Wave 50 — Sprout (short: walk, plant, return)**
1. *(Pip, walking out)* "A new tree, Chief! Dig here!"
2. *(the Warden, planting the seed)* "Grow strong, little one."
3. *(Granny, walking back)* "Sprout! My loud little boy."

**Wave 100 — Twig (full: walk, plant, water, growth, return)**
1. *(Granny, walking out)* "One more, dearie. For me."
2. *(the Warden, planting the seed)* "Grow brave, Twig."
3. *(Pip, watering)* "Drink up! Big gulps!"
4. *(the sapling grows — Granny's line)* "He writes poems already."
5. *(Pip, walking back)* "Pip's got TWO brothers now!"

**Wave 150 — Leaf (short: walk, plant, return)**
1. *(Pip, walking out)* "Last seed. Make it count."
2. *(the Warden, planting the seed)* "Grow soft, Leaf."
3. *(Granny, walking back)* "Shh. She naps already."

### 2.4 Chapter cards (waves 1 / 51 / 101 / 151 — tabled, rendered in P7)

The card's title plus the Hollow's deadpan line beneath it. Chapter 1's line is the Hollow's
first greeting (`HOLLOW_HELLO`), so it needs no new key (`StoryStrings.CHAPTER_*`).

- **Chapter 1** — card: "FIRST LIGHT" — Hollow: "I am the Night. I was here first."
- **Chapter 2** — card: "AMBER HOUR" — Hollow: "You are still here. Cute."
- **Chapter 3** — card: "THE LONG DARK" — Hollow: "I do not get tired. Ask anyone."
- **Chapter 4** — card: "HOLD THE DAWN" — Hollow: "Fine. My best team. All of them."

### 2.5 Wave 200 — Ascension transition (shown after the epilogue, before the Ascend prompt)

Tier-independent, always the same, in the Warden's white:

- "The Night rests. It never leaves."
- "Stand up. Granny stands with you."

## 3. Boss-Intro Cutscenes (tabled in P3, built in P4 — the heart of the run)

Every boss wave opens with a watch-only cutscene like the planting ceremony: the boss walks
in, does its funny trash-talk, Pip answers back, the boss leaves — THEN the wave starts.
First meetings get four boss lines plus Pip's comeback; later meetings get two plus Pip's
(`StoryStrings.BOSS_INTRO_*`, 136 lines). The five meetings arc from "who are you" to
farewell: M1 first meetings (waves 5–40), M2 "YOU AGAIN" (45–80), M3 running gags (85–120),
M4 respect cracks (125–160), M5 farewell tour (165–200).

### M1 — First meetings

**GRUM (wave 5)** — card: *"GRUM — Night Shift Security. Do not wake."*
1. "Hrrrm? …Who woke Grum?"
2. "Grum was napping. For a hundred years."
3. "Now Grum must sit on you."
4. "Sorry. Rules. …Yaaawn."
- Pip: "He's falling asleep! Get him, Chief!"

**MAMA BRAMBLE (wave 10)** — card: *"MAMA BRAMBLE — She grew half the bad guys."*
1. "YOU! You stepped on my babies!"
2. "My poor Rootlings! My sweet thorns!"
3. "No supper for you, young Warden!"
4. "Come here. Mama must scold you. HARD."
- Pip: "She packed snacks! Evil snacks! Run!"

**SIZZLE (wave 15)** — card: *"SIZZLE — The hottest star of the Night."*
1. "SIZZLE IS IN THE BUILDING!"
2. "Look at these scales! Look at them!"
3. "No photos of the left side. It is my bad side."
4. "Now burn, little extra! You are not the star!"
- Pip: "Somebody boil water! Oh wait. He hates that."

**SIR FALLS-A-LOT (wave 20)** — card: *"SIR FALLS-A-LOT — Very polite. Very clumsy."* (trips on ENTER)
1. "Whoa—! …I am fine. I meant that."
2. "Good evening. I shall crush you now."
3. "Nothing personal. The Night pays well."
4. "Pardon me. En garde. …Sorry. En garde?"
- Pip: "Did he just… bow? TO US?"

**BIG CHILL (wave 25)** — card: *"BIG CHILL — Evil? Or just on holiday?"*
1. "Whoa. Little dude. Cool bow."
2. "Name's Chill. Big Chill."
3. "No stress. I freeze you soft."
4. "After, we get ice. …Get it? Ice?"
- Pip: "I can't tell if he's bad or on a break."

**OLD PAGE (wave 30)** — card: *"OLD PAGE — Librarian. Fines are final."*
1. "SHHHH! This is a QUIET grove!"
2. "Your card is TWO HUNDRED years late!"
3. "Warden. Do you know what OVERDUE means?"
4. "It means BONK. *stamp* OVERDUE."
- Pip (whisper): "Why are we whispering? …Why am I whispering?"

**CAPTAIN THUNDER (wave 35)** — card: *"CAPTAIN THUNDER — Scared of puddles."*
1. "I! AM! THE STORM!"
2. "My ship is a rock. My crew is thunder."
3. "Surrender your… wait. Is that MUD?"
4. "MUD! ON MY BOOTS! NOW YOU PAY!"
- Pip: "Note to self: bring mud next time."

**BLUSH (wave 40)** — card: *"BLUSH — Sorry about this. Really sorry."*
1. "H-hi. I'm Blush. Sorry."
2. "The Night said… um… bonk you?"
3. "I don't want to. But rules are rules."
4. "Please dodge? …Okay. Here I come. Sorry."
- Pip: "Chief. I like her. …Bonk her gently?"

### M2 — "YOU AGAIN" (waves 45–80)

**GRUM**
1. "You again? Grum just fell asleep!"
2. "Fine. Quick bonk. Then nap."
- Pip: "He brought a pillow, Chief!"

**MAMA BRAMBLE**
1. "Back for more scolding? Good!"
2. "Mama baked blame-cookies. Eat blame!"
- Pip: "Do NOT eat the blame, Chief."

**SIZZLE**
1. "The extra returns! Fans first!"
2. "This time I burn you in HD!"
- Pip: "What's HD? …He doesn't know either."

**SIR FALLS-A-LOT**
1. "Ah! My favorite… whoa—! …foe."
2. "Shall we? Mind the rocks. I never do."
- Pip: "Somebody catch him! …Not it."

**BIG CHILL**
1. "Little dude! Back for more chill?"
2. "Same deal. Soft freeze. No stress."
- Pip: "He remembered us! …I think."

**OLD PAGE**
1. "YOU! Still loud! Still late!"
2. "Fine doubled. Bonk doubled."
- Pip (whisper): "He brings a bigger stamp. Run."

**CAPTAIN THUNDER**
1. "Back to my waters, tiny sailor?"
2. "This time NO mud. I checked. Twice."
- Pip (hiding mud): "No mud here, Captain!"

**BLUSH**
1. "Oh! Hi again! …Sorry!"
2. "I practiced bonking. I'm still bad."
- Pip: "She practiced! So sweet! …Bonk her."

### M3 — Running gags (waves 85–120)

**GRUM**
1. "Grum dreamed of you. You were loud."
2. "Sit. Bonk. Nap. In that order."
- Pip: "We're in his dreams now. Big honor."

**MAMA BRAMBLE**
1. "You look thin! Are you eating?"
2. "Eat this thorn pie. Then bonk."
- Pip: "The pie is moving. THE PIE IS MOVING."

**SIZZLE**
1. "My fans demand a rematch! *crickets*"
2. "…My ONE fan. Where is my fan?"
- Pip: "I'm right here! Worst show ever!"

**SIR FALLS-A-LOT**
1. "A hundred nights! …Whoa—! …I live here now."
2. "On the floor. It is nice here."
- Pip: "Should we help him up? …He seems happy."

**BIG CHILL**
1. "A hundred nights and still cool."
2. "Respect, little dude. Ice?"
- Pip: "He offered us ice! We're friends now, right?"

**OLD PAGE**
1. "A hundred nights of NOISE!"
2. "I wrote it all down. All of it."
- Pip: "He wrote a book about us! We're famous!"

**CAPTAIN THUNDER**
1. "Half the sea behind us, sailor!"
2. "My rock-ship sails at dawn. Be on it. As my prisoner."
- Pip: "Prisoner with snacks? Ask about snacks."

**BLUSH**
1. "H-hi! I made you a card!"
2. "It says sorry. …In advance."
- Pip: "SHE MADE US A CARD! Chief, keep it!"

### M4 — Respect cracks (waves 125–160)

**GRUM**
1. "Grum naps less now. Watches you."
2. "You fight… good. Do not tell the Night."
- Pip: "Did Grum just… praise us?"

**MAMA BRAMBLE**
1. "My babies fear you now. Good."
2. "A mother knows strength. Bonk Mama gently."
- Pip: "Gently? CHIEF. GENTLY."

**SIZZLE**
1. "You stole my crowd, extra!"
2. "Fine. Duet. You and me. After I burn you."
- Pip: "He wants a duet! We're STARS!"

**SIR FALLS-A-LOT**
1. "I polished my armor for you… whoa—!"
2. "…The floor and I are old friends."
- Pip: "He polished! For US! …Somebody help him."

**BIG CHILL**
1. "Almost dawn, little dude."
2. "Freeze you soft. Always soft."
- Pip: "Soft freezes only. Best bad guy ever."

**OLD PAGE**
1. "One hundred fifty nights. Shhh."
2. "…You read my book? …Thank you."
- Pip: "He smiled! …I think that was a smile."

**CAPTAIN THUNDER**
1. "The sea ends soon, sailor."
2. "First mate. My offer stands. Mud and all."
- Pip: "TAKE THE JOB, CHIEF! …After dawn."

**BLUSH**
1. "We're friends, right? …Say yes?"
2. "Okay. Bonk time. Friends bonk soft."
- Pip: "SOFT BONKS! Everybody heard that!"

### M5 — Farewell tour (waves 165–200)

**GRUM**
1. "Last nap before dawn, little loud one."
2. "Wake Grum… when it is morning."
- Pip: "We'll wake you, big guy. Promise."

**MAMA BRAMBLE**
1. "Mama knit you a scarf. Thorny."
2. "Wear it. Bonk Mama. Then breakfast."
- Pip: "Breakfast! She said breakfast!"

**SIZZLE**
1. "Final show! Sizzle! Sold out!"
2. "You were… a good rival. Do not cry."
- Pip: "I'm not crying! …Encore!"

**SIR FALLS-A-LOT**
1. "One last fall… see? No— whoa—!"
2. "…Worth it. For you, old friend."
- Pip: "He called us friend! …Help him up. For real."

**BIG CHILL**
1. "Last wave, little dude. Stay cool."
2. "Dawn comes. Chill stays. Always."
- Pip: "Best bad guy ever. Don't tell the others."

**OLD PAGE**
1. "Final stamp. READ— *stamp*"
2. "…READER OF THE YEAR. Still overdue."
- Pip: "We won! …Wait, what did we win?"

**CAPTAIN THUNDER**
1. "Last storm, first mate!"
2. "After dawn, we sail. For real. No mud."
- Pip: "No mud! …I'll pack mud anyway."

**BLUSH (wave 200, the last boss)**
1. "Last bonk. …Can we be friends after?"
2. "Okay. Here I come. Sorry. Love you. Sorry."
- Pip: "Everybody… that was the cutest threat ever."

## 4. Boss Lore (Granny's bios for the Codex, one paragraph each)

Each renders as the second paragraph of its Codex entry 9–16 detail view, unlocked together
with the entry (`story/BossLore`). Fond but firm — and each carries the hint that beats
its boss.

- **Ancient Golem.** Grum naps through meetings and sits on problems. His blow lands where you were, not where you are. Be somewhere else, dearie.
- **Thorn Matriarch.** Mama scolds first and bonks second. Her babies fear you now, and she respects that. Bonk her gently. She knit you a scarf.
- **Ember Wyrm.** Sizzle burns brightest from the left side. That is his bad side, and he will tell you so. Applause confuses him. Use it.
- **Void Knight.** Sir challenges you, bows, and falls over. Mind the rocks. He never does. Under all that armor beats the politest heart I know.
- **Frost Titan.** Chill offers ice before every fight. Take it. It is good ice. Then dodge left. He always starts left. He is chill like that.
- **Shadow Lich.** Old Page shushes the whole grove, then stamps you OVERDUE. His stamp is slow but certain. Read his book. It is about you. All of it.
- **Storm Colossus.** The Captain checks his boots twice before every battle. Mud ruins his day, and your aim fixes it. Sail with him after dawn. He already asked.
- **Bloodroot Avatar.** Blush apologizes before, during, and after every blow. Dodge kindly. She practiced bonking for you, and she is still bad at it. Bless her.

## 5. The Grove Codex — 48 Entries

Narrated in Granny's voice throughout (`story/LoreCatalog`). Grouped by unlock trigger; the
grouping is for implementation clarity only — display them unsorted-by-category once unlocked,
ordinary list order.

### 5.1 Wave-milestone entries (unlock on first reaching the wave)

1. **Wave 1 — "Night One."** *"So. You are the new Chief. Pip picked you, and Pip is never wrong about hearts. Stay close to my light, dearie."*
2. **Wave 10 — "Three Roads."** *"They only come from three sides. I never learned what holds back the fourth. Whatever it is, I thank it daily."*
3. **Wave 20 — "Counting Nights."** *"I used to count seasons. Now I count nights. Yours are the first ones I count with a smile."*
4. **Wave 30 — "Found Things."** *"What drops from them still remembers being useful. Take it, dearie. Better your pockets than my roots."*
5. **Wave 40 — "The Watchers."** *"Not every rootling attacks. Some just stand at the treeline and watch. Let them watch. We are worth watching."*
6. **Wave 60 — "Old Names."** *"Rootling. Stonekin. Gloom Wolf. I named three of those things once, when I meant something kinder. Names stick. Be kind with yours."*
7. **Wave 80 — "The Long Middle."** *"No song is ever written about this part. Not the falling, not the standing. Just the holding. Hold anyway, dearie."*
8. **Wave 100 — "Twig."** *"Three of us now. Twig writes poems already. One more seed to go, dearie. We grow anyway."*

### 5.2 Boss-first-kill entries (unlock on first defeat of each identity)

9. **"Grum."** *"He was our guard before he was their guard. The Night never turned him. It only told him the fight never ended, and he believed it. Poor heavy boy."*
10. **"Mama Bramble."** *"She grew half the bad guys herself, back when growing things was all she did. She still packs snacks for battle. Do not eat the snacks."*
11. **"Sizzle."** *"Fire is supposed to go out. This one said no, learned to pose, and hired no one. The Night's hottest star. His words, not mine."*
12. **"Sir Falls-A-Lot."** *"Very polite. Very clumsy. He has fallen down every stair in the Night and apologized to each one. Catch him if you can. He will thank you."*
13. **"Big Chill."** *"Evil? Or just on holiday? The frost came after the Hollow, not before. He freezes you soft. He insists on soft."*
14. **"Old Page."** *"He was the record keeper before he was a boss. Now he keeps the record of every fall, and fines you for each one. Fines are final, dearie."*
15. **"Captain Thunder."** *"His ship is a rock. His crew is thunder. He fears no storm and no sailor. He fears mud. Bring mud."*
16. **"Blush."** *"Sorry about this one. Really sorry. She does not want to bonk you, but rules are rules. She made you a card. It says sorry. In advance."*

### 5.3 Elite-kill entries (unlock on first kill of an Elite carrying that affix)

17. **"The Pop."** *(Blightburst)* *"That one pops. Do not hug it. The pop is relief, Pip says, and then he laughs for a minute. Weird boy. Lovely boy."*
18. **"The Rude Shield."** *(Rootward Ward)* *"It guards nothing and still guards. I notice it flinches toward protecting, even now. Old habits, dearie. Mine is soup."*
19. **"The Yuck."** *(Weeping Rot)* *"Do not step in the yuck. Everything rotting wants to get back to the soil. This soil grows more of itself. Rude soil."*

### 5.4 Ascension entries (unlock on completing that Ascension tier)

20. **After Ascension 1 — "Again."** *"You came back. I did not expect that so soon. I am starting to recognize your footsteps. After all these years, that is not nothing."*
21. **After Ascension 2 — "Twice Now."** *"Twice now. I saved you some light, like always. The night is learning your name. Let it shake, Pip says. Pip is right."*
22. **After Ascension 3 — "What Stays."** *"The waves start over. The dark starts over. You do not. Not all the way. I have watched enough Chiefs to know."*
23. **After Ascension 5 — "Five Dawns."** *"Five dawns you have given me. Sometimes I wonder if the Hollow gets tired like you do. I have decided not to ask. I am busy counting your light."*
24. **After Ascension 10 — "Ten Dawns."** *"I once sorted my guards by how long they lasted. Now I sort them by whether they came back. You keep coming back, dearie."*

### 5.5 Curiosity / secret entries (unusual conditions — listed with their trigger)

25. **"Bare-Handed."** *(reach Wave 50 having bought no Shop stats this run)* — *"You did that with what you were given, not what you bought. Discipline or stubbornness? Around here they are the same root."*
26. **"A Full Set."** *(equip a complete 4-piece set for the first time)* — *"Things that match hold together better. I could have told you before you spent the coin. But you look lovely, dearie."*
27. **"Ten Times Clean."** *(any skill reaches level 10 for the first time)* — *"Ten times so clean it no longer looks like effort. I remember when standing here felt like that. It was a Tuesday."*
28. **"Reforged."** *(forge any item to its maximum Anvil level for the first time)* — *"Nothing stays the way it was made. Least of all you. I mean that kindly. Mostly."*
29. **"Six Wonders."** *(own all six Mythic items at once, any slot combination)* — *"Six one-of-a-kind things in one grove. I did not think we had that many wonders left. I am glad I was wrong."*
30. **"No Potions."** *(clear an entire Wave 1–100 stretch without consuming a single potion)* — *"You never once needed the weakest thing I could give you. I hope that was strength. Either way, soup is still on."*
31. **"The Long Pause."** *(pause the game mid-wave for an unusually long time, then resume)* — *"Go if you must. Come back when you can. Waiting is my specialty. Pip naps while he waits. I count. We are good at this."*
32. **"Every Elite."** *(kill at least one Elite of every affix type)* — *"You have heard every fragment Pip can shout through them now. There is more to tell. There is always more. Pip will find it."*
33. **"Fastest Fall."** *(clear a single wave in record time, e.g. under a defined threshold)* — *"That was over before the Hollow finished sending it. I do not think it noticed yet. Do not tell it. Let it find out."*
34. **"Twice to Dawn."** *(reach Wave 200 for the second time, any Ascension tier)* — *"The first time was survival. The second time? Say it to yourself, dearie. Then come have soup. You earned it twice."*

### 5.6 Deep-pool elite entries (roadmap D2, waves 101+)

35. **"The Hill."** *(Stoneshell)* *"It wears a hill. Cheater. It simply decides to be stone for a while, and stone does not care how hard you try."*
36. **"Angry Ground."** *(Gravebloom)* *"Everything that dies here leaves something in the soil. Most of it is quiet. Walk around the parts that still remember."*
37. **"More Friends."** *(Swarmcall)* *"It called friends! Unfair! I used to think killing it was the end of it. It always had more. Count them with Pip. He loves counting."*
38. **"Long Arms."** *(Spitebarb)* *"Long arms! Longer fouls! Stand close and it keeps its word. Stand far and the word means nothing. Fairest thing in the grove."*
39. **"Arm Up."** *(Hammerfall)* *"Arm up! Move, Chief! The ground goes dark in a circle, and you have that long to be elsewhere. Step. That is the whole lesson."*
40. **"The Howler."** *(Bloodhowl)* *"Hear the howl and you found the one that sets the pace. Find the howler. Bonk it. The wave will remember it is tired."*
41. **"One Becomes Two."** *(Hollowmolt)* *"One becomes two! Bad magic! Kill the loud one and something quieter is already standing in its spot. Two small quiets. Still loud."*
42. **"Moss on a Wound."** *(Gravemoss)* *"Moss on a wound. Still a wound. It heals the way a root drinks: slowly, and in the wrong direction. Stop healing. I mean it. Stop."*
43. **"Hot Hug."** *(Cinderhalo)* *"Hot hug! No hugs! Standing close to that one is standing close to a fire nobody will put out. Warm grief. Stay back, dearie."*

### The four fields (codex 44-47, unlocked by fighting on the ground itself)

The arena round gave a run four possible fields; these four rows are how the player finds out that the
place they are standing in is one of four, and that the other three exist. A run unlocks exactly one of
them, on its first wave, so the set is collected across runs rather than inside one.

44. **"Open Hearth."** *(Open Hearth field)* *"Some nights I give you nothing to hide behind. Two stones, one each side, and the rest is you and the distance you keep. Breathe. The open ground believes in you."*
45. **"Standing Stones."** *(Standing Stones field)* *"Four stones stood here before either of us, and they have not moved. Walk behind one. Your arrows stop at it too. The stone does not take sides."*
46. **"The Thornhedge."** *(Thornhedge field)* *"Small stones in a line, low enough to shoot over, high enough to trip a charge. Something planted them in a shape. Pip claims it was him. It was not him."*
47. **"The Ruined Ring."** *(Ruined Ring field)* *"Someone built a circle and left four gaps, one for each road. They were expecting our three directions too. They left no note. Only the ring. And us."*

### The set (codex 48, unlocked by timing a brace into a blow)

The one mechanic the game does not spell out: `BraceSystem` holds a blow whole while the shield is still *set*
(`BraceLimits.SET_WINDOW_SECONDS`), and the entry is what a player who found it reads afterwards.

48. **"Held, Not Blocked."** *(hold any blow with a raised shield)* *"There is a difference between a shield raised early and a shield raised into the blow. The first takes a share. The second takes all of it. Raise it late, dearie."*

## 6. Elite Affixes — Pip's Field Notes (two fragments each, funny)

One fragment per Elite kill, alternating I/II per affix (`story/EliteFragments`,
`StoryStrings.ELITE_*`). Pip shouts them over the kill — short, loud, wrong about
almost everything except the warning.

**Blightburst** *(explodes on death)*
- Fragment I: "It pops! Do not hug it."
- Fragment II: "That pop is relief. Weird."

**Rootward Ward** *(periodically shields)*
- Fragment I: "A shield! Rude shield!"
- Fragment II: "It guards nothing. Still guards."

**Weeping Rot** *(leaves a damaging trail)*
- Fragment I: "Don't step in the yuck."
- Fragment II: "The yuck leads to Granny?!"

**Hollowmolt** *(splits into two husks on death — roadmap D2, deep-run pool, wave 101+)*
- Fragment I: "One becomes two! Bad magic!"
- Fragment II: "Two small quiets. Still loud."

**Gravemoss** *(regrows its own health; stun stops it — roadmap D2, deep-run pool)*
- Fragment I: "Moss on a wound. Still a wound."
- Fragment II: "It's healing! …Stop healing!"

**Cinderhalo** *(burns whoever stands inside its halo — roadmap D2, deep-run pool)*
- Fragment I: "Hot hug! No hugs!"
- Fragment II: "Warm grief. Stay back."

**Stoneshell** *(decides to be stone for a while)*
- Fragment I: "It wears a hill. Cheater."
- Fragment II: "Stone naps. Stone hates you."

**Gravebloom** *(the ground stays angry where it stood)*
- Fragment I: "Angry ground. Walk around."
- Fragment II: "It remembers. Rude."

**Swarmcall** *(calls for more)*
- Fragment I: "It called friends! Unfair!"
- Fragment II: "More friends! SO many friends!"

**Spitebarb** *(long reach, keeps its word up close)*
- Fragment I: "Long arms! Longer fouls!"
- Fragment II: "Close work costs. Pay up."

**Hammerfall** *(raises one arm; the ground goes dark in a circle)*
- Fragment I: "Arm up! Move, Chief!"
- Fragment II: "Step. That's the lesson."

**Bloodhowl** *(its howl sets the pace of the line)*
- Fragment I: "It howls! They run!"
- Fragment II: "Find the howler. Bonk it."

## 7. Branching Epilogues (shown at run end, before the Ascension transition on a win)

The first two beats of each are the Warden's, terse and white; the third beat is Pip's
(`story/Epilogue`). On a win, the Warden's final line — "…We held." (`VICTORY_WARDEN`,
tabled; the victory-box sequencing lands with the P4 cinematics) — comes before the epilogue.

**A — Flawless Victory** *(Wave 200 cleared, never down, under 3 potions, 30%+ HP)*
1. "Two hundred nights. Zero falls."
2. "The Night needs a new plan."
3. "Pip's plan worked! …Mostly."

**B — Hard-Fought Victory** *(every other Wave-200 clear)*
1. "Two hundred nights. All heart."
2. "I fell. I rose. I held."
3. "Best Chief ever. Don't argue."

**C — Early Fall** *(Game Over before Wave 50)*
1. "Too soon. Too dark."
2. "Granny, keep my seat warm."
3. "We go again. Now. Up, Chief!"

**D — Middle Fall** *(Game Over, Wave 50–149)*
1. "Past Twig. Not past dawn."
2. "Next time, Night. Next time."
3. "Pip counted! Further next run!"

**E — Late Fall** *(Game Over, Wave 150+)*
1. "So close the dawn waved."
2. "It can wait one more run."
3. "One more run! Pip's got a NEW plan!"

## 8. Mythic Item Flavor (one per equipment slot)

Name and passive from `items/EquipmentCatalog` and `items/MythicEffects`; flavor verbatim
from `StoryStrings.MYTHIC_*` for the inventory details panel.

- **Weapon — "Sunfall, the Last Arrow."** *Passive: Chain arcs also Stun (1.0s).*
  Flavor: "One arrow. One dawn. Never missed."
- **Helmet — "Crown of the Hollow Eye."** *Passive: Crits Mark 4s; marked take +25%.*
  Flavor: "See weak spots. Bonk them."
- **Armor — "Bark of the First Root."** *Passive: Every 10th hit taken heals 20%.*
  Flavor: "Granny's bark. Heals you back."
- **Boots — "Windrunner's Last Steps."** *Passive: +1% attack speed/s in wave (max +25%).*
  Flavor: "Fast boots. Never run. Stand."
- **Ring — "Verdant Oath."** *Passive: Auto-potions grant +5% lifesteal 4s.*
  Flavor: "A pinky promise in sap."
- **Ring — "Emberless Core."** *Passive: Crits refund 35% of shot cooldown.*
  Flavor: "Cold ember. Hot temper."

## 9. Granny's Other Lines

- **Idle whispers (roadmap ST4):** if the game sits for an unusually long real-world
  stretch, the next resume can silently show one unused line before combat continues — six
  lines, each shown once ever, all Pip's (`story/WhisperLines`):
  1. "Chief? You sleeping? …Pip naps too."
  2. "Granny says hi. Eat your sunlight."
  3. "Pip guarded the grove. All alone. Brave."
  4. "The dark blinked first. Pip saw it."
  5. "Rest is training. Pip trains hard."
  6. "You're back! Pip missed you. A little."
- **Daily gift (roadmap ST5):** *"Two heartwood, saved for you. Granny counts."*
- **Victory line (spoken in the dialogue box the moment a run is completed, in Granny's
  blips):** *"Dawn, dearie! You did it! Soup for all!"*
- **Vigil Deeds (roadmap ST2):** the run's named goals, paid once each, in the Warden's
  white. Pattern: `Deed: <goal>  |  + <N> coins` — eleven goals: held to wave 10 / 25 /
  50 / 100 / 150 / 200, first boss felled, five bosses in one run, wave 25 without a
  potion, wave 50 never down with no potion, read ten codex pages.

## 10. The Hollow's Lines (roadmap ST1 — the night speaking to the player)

Spoken in the Hollow's voice (its own blips, its own speaker label). Each line is delivered once
per the rule noted next to it. The Hollow never speaks mid-fight: the old half-health boss beats
are deleted (§6 of MEMORY, the silence rule).

- **First hello** *(first wave 1 of the save, ever; also the Chapter 1 card line)*: "I am the Night. I was here first."
- **Death, first time** *(the first wave the Warden dies, per save — spoken in the death box)*: "You fell. Get up. The show needs you."
- **Death, again** *(the next death in the same save)*: "Again? …The floor likes you."
- **First spare** *(first time the Warden lets an enemy go)*: "You let it go? …Bold. I watched."
- **Third spare** *(mercy named as a habit)*: "Three spared. Mercy. I remember."
- **Wave 100 beat** *(once, at the planting ceremony — allowed, it is not combat)*: "Halfway. Cute tree. I am still here."
- **Verdict, merciful** *(opens the run after a finished run that spared at least one)*: "You spared some. Cute. I count the debt."
- **Verdict, stern** *(opens the run after a finished run that spared no one)*: "You spared none. Cold. My inventory grows."

## 11. The Night's Face (visual, no new words): the gaze and the dawn

The night in this story is watching — the Hollow counts debts and inventories — and the
reviewed HOLLOW arena leaves its dark upper field empty. Two cold lights live there, deep in
the backdrop under everything else.

- **The gaze.** In the HOLLOW arena (waves 101-200) the Hollow's two eyes are visible in the
  dark, a whisper of cold light — felt more than found. They fade in between waves 101 and 121,
  sharpen by 1.6x while a boss stands, blink shut (0.45s) when the tree falls, and drift apart
  and away over six seconds when the run is won. The forest arena (waves 1-100) never sees
  them. Reduced motion removes the breath and slows the drift; it never removes the night.
- **The dawn ledger.** At the foot of the Root Network hub, a dark band carries one carved notch
  per dawn the player has brought back (one per completed ascension) — the newest dawn still
  warm gold, the rest set. An empty band says "the tree is still young". Every other record in
  the game is a number in a corner; this one is carved where a player can look at it. Granny
  remembers, and this is what remembering looks like.
- **The dawn.** The run's colour arc (see `render/StageGrade`) is named for the day: **DAWN**
  1-50, **AMBER** 51-100, **TEAL** 101-150, **HOLLOW** 151-200. The victory closes that arc.

When a run completes — on the victory screen, not earlier — the sky behind the premium summary breaks from the HOLLOW's night into dawn
gold over eight seconds, procedural light (one shader pass, no new art): a glow at the horizon and
two slow bands rising out of it, breathing once per minute. The light lives at the bottom half of
the frame and dies before the top, so the epilogue lines and the title panel keep their contrast
while the night behind them turns to morning.

The timing is the story: Granny's victory line takes the box about six seconds (type, hold,
close), and the sunrise is eight — so the player reads "Dawn, dearie! You did it! Soup for all!"
**while the dawn is still arriving**, and it is finished breaking before they decide whether to
ascend. The first tenth of the sunrise is barely visible, on purpose: the player reads before
they notice. Reduced motion keeps the sunrise advancing and stops it breathing. A defeat never
dawns; a new run is night again.

## 12. A Few Extra Touches (small, optional, not required by any roadmap item)

- **Silent Rootling detail:** codex entry 5 ("The Watchers.") is worth actually implementing as a
  rare, harmless spawn variant — a Rootling that stands still at the tree line and never attacks —
  so the lore line has something on screen to point at, not just text. Purely visual, zero balance
  impact, and the kind of detail a player notices once and then looks for every run after.

## 13. Granny's Letters (roadmap ST5 — the Tree that writes)

Granny already speaks: codex, victory line, gift, ceremony beats. A masterpiece-grade story
layer adds what a tree that has stood this long would actually do — **write**. After each
dawn (each completed ascension), Granny writes a short letter. The player hears the first unread
one typed out in the dialogue box when they next enter the Root Network hub, in Granny's voice,
letter by letter, the way every other line in the game arrives (`story/TreeLetters`).

- **One letter per dawn number** (ten letters; the tenth is re-read for every dawn from the tenth
  on). Reading a letter — letting the box close on the hub, or leaving the hub with it still up —
  marks it read under its own key in the save (`letter_<n>` in the codex map, which counts only
  lore ids, so the written-count is untouched). A letter is therefore spoken **once, ever**.
- **Pacing:** the box is sticky on the hub — it never closes on its own. A letter is read at the
  player's pace, never away from them. A tap finishes the typing; the next tap closes it.
- **The first letter** (first dawn): "So that was you. I felt every step, and I am keeping the light you left here. Rest now, dearie. The night will learn your name soon. Let it shake."
- **The last letter** (tenth dawn and beyond, foreshadowing the door): "This is my last letter in this shape. After you go through the door, I cannot write in words anymore. I will write in light. Every dawn you left here will come for you. Walk into it, dearie. Soup is on the other side."
- **The middle letters** walk the spine in Granny's voice:
  - Dawn 2: "You came back before the light faded. Good. I am keeping two dawns now, and the second burns stronger. Pip counted them twice. He loves counting."
  - Dawn 3: "The night keeps testing you, and you keep passing. I am starting to understand: the dark is not the strong one. The strong one stands up again. And soup helps."
  - Dawn 4: "I have been keeping a small thing. Roots are not anchors. They are hands. Mine have been holding you up from below this whole time. Feel them, dearie."
  - Dawn 5: "Halfway, and the word feels heavy. Here is the truth anyway: you were never halfway. You were always the whole tree, only growing. Keep growing."
  - Dawn 6: "The Hollow asked me what you are. I did not answer. Some things are not for naming. They are for walking, one night at a time. Walk on, Chief."
  - Dawn 7: "I have stopped fearing the night. It keeps losing, and it does not seem to mind. The bravest thing about you: you keep winning, and you keep being kind."
  - Dawn 8: "Count the dawns you gave me. Eight. I counted too, and I have a secret: the light does not come from above. It comes from you. I am only the window."
  - Dawn 9: "One more, and the story takes a different shape. I do not know that shape either. But a tree that leans toward a door already knows the way. Lean with me, dearie."
- **Why this is the layer:** every other record in the game is a number; every other Granny line is
  spoken and gone. A letter is kept, is short, and is addressed to one player. It is the difference
  between a game that talks and a story that remembers you writing back.
