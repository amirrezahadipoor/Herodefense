package com.amirrezahadipoor.herodefense.i18n;

/**
 * The game's own voice (roadmap R7.3): what a boss's first title card says, what Pip the firefly shouts
 * at a wave milestone, what the Hollow says to the player, what Granny says when the dawn comes, and the
 * line the trophy chime puts on screen.
 *
 * <p>These are the places where the game speaks rather than reports, which is why they are one table
 * rather than four: they share one cast, and a character whose voice differs between screens is a
 * character the player notices. The words were rewritten with the whole story (2026-09-23, MEMORY P3):
 * Pip the firefly, Granny the World Tree, the nearly-silent Warden, the deadpan Hollow, and the eight
 * ridiculous bosses of the Night Shift -- common words, short lines, one idea per line.
 *
 * <p>The em dash the title cards use is kept rather than swapped for the "  |  "
 * the button-and-panel tables use: a title card is a sentence rather than a row of readouts.
 *
 * <p>What this does not hold, and cannot yet: the trophy *names* that {@code TROPHY_NAMES} joins are
 * {@code progression/Trophy}'s own 24 titles, still on the provenance ratchet until that file is tabled.
 */
public enum StoryStrings implements Translated {

    /** First-encounter boss title cards: one line per identity, shown once ever. All eight are in. */
    BOSS_ANCIENT_GOLEM("GRUM — Night Shift Security. Do not wake."),
    BOSS_THORN_MATRIARCH("MAMA BRAMBLE — She grew half the bad guys."),
    BOSS_EMBER_WYRM("SIZZLE — The hottest star of the Night."),
    BOSS_VOID_KNIGHT("SIR FALLS-A-LOT — Very polite. Very clumsy."),
    BOSS_FROST_TITAN("BIG CHILL — Evil? Or just on holiday?"),
    BOSS_SHADOW_LICH("OLD PAGE — Librarian. Fines are final."),
    BOSS_STORM_COLOSSUS("CAPTAIN THUNDER — Scared of puddles."),
    BOSS_BLOODROOT_AVATAR("BLUSH — Sorry about this. Really sorry."),

    /** The wave-100 full planting ceremony's five beats (Twig), in the order the ceremony walks through
     *  them. Voices: Granny, the Warden, Pip, Granny, Pip. */
    CEREMONY_WALK_OUT("One more, dearie. For me."),
    CEREMONY_PLANT("Grow brave, Twig."),
    CEREMONY_WATER("Drink up! Big gulps!"),
    CEREMONY_GROW("He writes poems already."),
    CEREMONY_WALK_BACK("Pip's got TWO brothers now!"),

    /** Pip's milestone beats at wave starts: 25, 50, 75, 125, 150, 175. */
    REFLECTION_WAVE_25("Twenty-five nights! Pip counted!"),
    REFLECTION_WAVE_50("Fifty nights! A new tree today!"),
    REFLECTION_WAVE_75("The dark is thick. My butt glows."),
    REFLECTION_WAVE_125("Half the Night Shift owes me coins."),
    REFLECTION_WAVE_150("Last seed tonight, Chief! Hold on!"),
    REFLECTION_WAVE_175("Almost dawn, Chief. Almost."),

    /** The trophy line: a count when there are too many to read, otherwise the names joined. */
    TROPHY_COUNT("Trophy - %1$s earned"),
    TROPHY_NAMES("Trophy - %1$s"),
    TROPHY_AND(" and "),

    /** The Hollow (roadmap ST1): the one voice that talks to the player, not the Warden. */
    HOLLOW_DEATH_FIRST("You fell. Get up. The show needs you."),
    HOLLOW_DEATH_AGAIN("Again? …The floor likes you."),
    HOLLOW_SPARE("You let it go? …Bold. I watched."),
    HOLLOW_WAVE100("Halfway. Cute tree. I am still here."),
    HOLLOW_HELLO("I am the Night. I was here first."),
    HOLLOW_MERCY_HABIT("Three spared. Mercy. I remember."),
    HOLLOW_VERDICT_MERCIFUL("You spared some. Cute. I count the debt."),
    HOLLOW_VERDICT_STERN("You spared none. Cold. My inventory grows."),

    /** Granny's thank-you, spoken in the box the moment a run is completed: the summary waits for it,
     *  then rises — the victory's own parting word, in the Tree's own blips. */
    TREE_VICTORY("Dawn, dearie! You did it! Soup for all!"),

    /** Granny's daily gift (roadmap ST5): the return hook, spoken once a day. */
    DAILY_GIFT("Two heartwood, saved for you. Granny counts."),

    /** The Vigil Deeds (roadmap ST2): the run's named goals, paid once each. */
    DEED_WAVE_10("Deed: held to wave 10  |  + %1$s coins"),
    DEED_WAVE_25("Deed: held to wave 25  |  + %1$s coins"),
    DEED_WAVE_50("Deed: held to wave 50  |  + %1$s coins"),
    DEED_WAVE_100("Deed: held to wave 100  |  + %1$s coins"),
    DEED_WAVE_150("Deed: held to wave 150  |  + %1$s coins"),
    DEED_WAVE_200("Deed: held to wave 200  |  + %1$s coins"),
    DEED_FIRST_BOSS("Deed: first boss felled  |  + %1$s coins"),
    DEED_BOSSES_5("Deed: five bosses in one run  |  + %1$s coins"),
    DEED_CLEAN_25("Deed: wave 25 without a potion  |  + %1$s coins"),
    DEED_FLAWLESS_50("Deed: wave 50, never down, no potion  |  + %1$s coins"),
    DEED_CODEX_10("Deed: read ten codex pages  |  + %1$s coins"),

    /** The Grove Codex screen's labels (moved out of {@code render/CodexOverlayRenderer} so the
     *  screen's chrome lives in the table beside the entries beneath it). */
    CODEX_TAB_LORE("LORE"),
    CODEX_TAB_TROPHIES("TROPHIES"),
    CODEX_SHOWING("showing"),
    CODEX_TAP_TO_SHOW("tap to show"),
    CODEX_WRITTEN("%1$s / %2$s WRITTEN"),
    CODEX_EARNED("%1$s / %2$s EARNED"),
    CODEX_LOCKED_HINT("Granny has not written this yet."),
    TROPHY_HEADER("WARDEN'S TROPHIES"),
    TROPHY_TAP_HINT("Tap a trophy to read what earns it."),

    /** The new-run opening, one line set per ascension tier. Tier 0 is Pip meeting you, 1 and 2 their own,
     *  3+ one shared set. Three lines each, all Pip's, typed in the box over the push-in. */
    OPENING_0_ONE("Hey! Hey you! With the bow!"),
    OPENING_0_TWO("I'm Pip. You're the new Chief."),
    OPENING_0_THREE("Stay close. The Night is coming."),
    OPENING_1_ONE("Back again, Chief?"),
    OPENING_1_TWO("Granny saved you some light."),
    OPENING_1_THREE("Tonight we go further."),
    OPENING_2_ONE("The Night knows your name now."),
    OPENING_2_TWO("Good. Let it shake."),
    OPENING_2_THREE("Pip's got a plan!"),
    OPENING_3_ONE("New night. Same Chief."),
    OPENING_3_TWO("Granny says hi."),
    OPENING_3_THREE("Let's bonk the dark."),

    /** Branching end-of-run epilogues. A = flawless, B = hard-fought, C/D/E = falls. The third beat of
     *  each is Pip's; the first two are the Warden's, terse and white. */
    EPILOGUE_A_ONE("Two hundred nights. Zero falls."),
    EPILOGUE_A_TWO("The Night needs a new plan."),
    EPILOGUE_A_THREE("Pip's plan worked! …Mostly."),
    EPILOGUE_B_ONE("Two hundred nights. All heart."),
    EPILOGUE_B_TWO("I fell. I rose. I held."),
    EPILOGUE_B_THREE("Best Chief ever. Don't argue."),
    EPILOGUE_C_ONE("Too soon. Too dark."),
    EPILOGUE_C_TWO("Granny, keep my seat warm."),
    EPILOGUE_C_THREE("We go again. Now. Up, Chief!"),
    EPILOGUE_D_ONE("Past Twig. Not past dawn."),
    EPILOGUE_D_TWO("Next time, Night. Next time."),
    EPILOGUE_D_THREE("Pip counted! Further next run!"),
    EPILOGUE_E_ONE("So close the dawn waved."),
    EPILOGUE_E_TWO("It can wait one more run."),
    EPILOGUE_E_THREE("One more run! Pip's got a NEW plan!"),
    EPILOGUE_TRANSITION_ONE("The Night rests. It never leaves."),
    EPILOGUE_TRANSITION_TWO("Stand up. Granny stands with you."),

    /** Pip's field notes, one fragment pair per Elite affix. */
    ELITE_BLIGHTBURST_ONE("It pops! Do not hug it."),
    ELITE_BLIGHTBURST_TWO("That pop is relief. Weird."),
    ELITE_ROOTWARD_ONE("A shield! Rude shield!"),
    ELITE_ROOTWARD_TWO("It guards nothing. Still guards."),
    ELITE_WEEPING_ONE("Don't step in the yuck."),
    ELITE_WEEPING_TWO("The yuck leads to Granny?!"),
    ELITE_HOLLOWMOLT_ONE("One becomes two! Bad magic!"),
    ELITE_HOLLOWMOLT_TWO("Two small quiets. Still loud."),
    ELITE_GRAVEMOSS_ONE("Moss on a wound. Still a wound."),
    ELITE_GRAVEMOSS_TWO("It's healing! …Stop healing!"),
    ELITE_CINDERHALO_ONE("Hot hug! No hugs!"),
    ELITE_CINDERHALO_TWO("Warm grief. Stay back."),
    ELITE_STONESHELL_ONE("It wears a hill. Cheater."),
    ELITE_STONESHELL_TWO("Stone naps. Stone hates you."),
    ELITE_GRAVEBLOOM_ONE("Angry ground. Walk around."),
    ELITE_GRAVEBLOOM_TWO("It remembers. Rude."),
    ELITE_SWARMCALL_ONE("It called friends! Unfair!"),
    ELITE_SWARMCALL_TWO("More friends! SO many friends!"),
    ELITE_SPITEBARB_ONE("Long arms! Longer fouls!"),
    ELITE_SPITEBARB_TWO("Close work costs. Pay up."),
    ELITE_HAMMERFALL_ONE("Arm up! Move, Chief!"),
    ELITE_HAMMERFALL_TWO("Step. That's the lesson."),
    ELITE_BLOODHOWL_ONE("It howls! They run!"),
    ELITE_BLOODHOWL_TWO("Find the howler. Bonk it."),

    /** Idle-whisper pool: six Pip lines, one per long pause, each shown once ever. */
    WHISPER_ONE("Chief? You sleeping? …Pip naps too."),
    WHISPER_TWO("Granny says hi. Eat your sunlight."),
    WHISPER_THREE("Pip guarded the grove. All alone. Brave."),
    WHISPER_FOUR("The dark blinked first. Pip saw it."),
    WHISPER_FIVE("Rest is training. Pip trains hard."),
    WHISPER_SIX("You're back! Pip missed you. A little."),

    /** Speaker labels over the dialogue box: the box types every message in the speaker's own voice, and
     *  the label tells who is speaking. Same three speakers the blips already tell apart by ear. */
    SPEAKER_WARDEN("WARDEN"),
    SPEAKER_TREE("GRANNY"),
    SPEAKER_HOLLOW("HOLLOW"),
    SPEAKER_PIP("PIP"),
    SPEAKER_NIGHT("NIGHT SHIFT"),

    /** Mythic item flavor (§7), one per slot's Mythic. */
    MYTHIC_SUNFALL("One arrow. One dawn. Never missed."),
    MYTHIC_CROWN("See weak spots. Bonk them."),
    MYTHIC_BARK("Granny's bark. Heals you back."),
    MYTHIC_WINDRUNNER("Fast boots. Never run. Stand."),
    MYTHIC_VERDANT("A pinky promise in sap."),
    MYTHIC_EMBERLESS("Cold ember. Hot temper."),

    /** Chapter cards (w1 / w51 / w101 / w151): the card's title plus the Hollow's deadpan line beneath it.
     *  Chapter 1's line is the Hollow's first greeting, so it needs no new key. */
    CHAPTER_1_CARD("FIRST LIGHT"),
    CHAPTER_2_CARD("AMBER HOUR"),
    CHAPTER_2_LINE("You are still here. Cute."),
    CHAPTER_3_CARD("THE LONG DARK"),
    CHAPTER_3_LINE("I do not get tired. Ask anyone."),
    CHAPTER_4_CARD("HOLD THE DAWN"),
    CHAPTER_4_LINE("Fine. My best team. All of them."),

    /** The Warden's final line: one white beat in the victory box before the epilogue, win or fall. He says
     *  almost nothing all run, so his two words land like a bell. */
    VICTORY_WARDEN("…We held."),

    /** The short planting ceremonies' three beats each: Sprout at wave 50, Leaf at wave 150. Voices walk
     *  Pip, the Warden, Granny both times — the short rites mirror each other around Twig's full one. */
    CEREMONY_50_WALK_OUT("A new tree, Chief! Dig here!"),
    CEREMONY_50_PLANT("Grow strong, little one."),
    CEREMONY_50_WALK_BACK("Sprout! My loud little boy."),
    CEREMONY_150_WALK_OUT("Last seed. Make it count."),
    CEREMONY_150_PLANT("Grow soft, Leaf."),
    CEREMONY_150_WALK_BACK("Shh. She naps already."),

    /** Pip's milestone breaths (MEMORY §9.3): one line after clearing 25/75/125/175, between
     *  waves, never during combat. */
    BREATHER_25("Twenty-five nights! Pip counted!"),
    BREATHER_75("The dark is thick. My butt glows."),
    BREATHER_125("Half the Night Shift owes me coins."),
    BREATHER_175("Almost dawn, Chief. Almost."),

    /** Boss-intro cutscenes, first meetings (waves 5–40): four trash-talk lines per boss, then Pip's
     *  comeback. The bosses speak ONLY here — never once their wave starts. */
    BOSS_INTRO_ANCIENT_GOLEM_M1_1("Hrrrm? …Who woke Grum?"),
    BOSS_INTRO_ANCIENT_GOLEM_M1_2("Grum was napping. For a hundred years."),
    BOSS_INTRO_ANCIENT_GOLEM_M1_3("Now Grum must sit on you."),
    BOSS_INTRO_ANCIENT_GOLEM_M1_4("Sorry. Rules. …Yaaawn."),
    BOSS_INTRO_ANCIENT_GOLEM_M1_PIP("He's falling asleep! Get him, Chief!"),
    BOSS_INTRO_THORN_MATRIARCH_M1_1("YOU! You stepped on my babies!"),
    BOSS_INTRO_THORN_MATRIARCH_M1_2("My poor Rootlings! My sweet thorns!"),
    BOSS_INTRO_THORN_MATRIARCH_M1_3("No supper for you, young Warden!"),
    BOSS_INTRO_THORN_MATRIARCH_M1_4("Come here. Mama must scold you. HARD."),
    BOSS_INTRO_THORN_MATRIARCH_M1_PIP("She packed snacks! Evil snacks! Run!"),
    BOSS_INTRO_EMBER_WYRM_M1_1("SIZZLE IS IN THE BUILDING!"),
    BOSS_INTRO_EMBER_WYRM_M1_2("Look at these scales! Look at them!"),
    BOSS_INTRO_EMBER_WYRM_M1_3("No photos of the left side. It is my bad side."),
    BOSS_INTRO_EMBER_WYRM_M1_4("Now burn, little extra! You are not the star!"),
    BOSS_INTRO_EMBER_WYRM_M1_PIP("Somebody boil water! Oh wait. He hates that."),
    BOSS_INTRO_VOID_KNIGHT_M1_1("Whoa—! …I am fine. I meant that."),
    BOSS_INTRO_VOID_KNIGHT_M1_2("Good evening. I shall crush you now."),
    BOSS_INTRO_VOID_KNIGHT_M1_3("Nothing personal. The Night pays well."),
    BOSS_INTRO_VOID_KNIGHT_M1_4("Pardon me. En garde. …Sorry. En garde?"),
    BOSS_INTRO_VOID_KNIGHT_M1_PIP("Did he just… bow? TO US?"),
    BOSS_INTRO_FROST_TITAN_M1_1("Whoa. Little dude. Cool bow."),
    BOSS_INTRO_FROST_TITAN_M1_2("Name's Chill. Big Chill."),
    BOSS_INTRO_FROST_TITAN_M1_3("No stress. I freeze you soft."),
    BOSS_INTRO_FROST_TITAN_M1_4("After, we get ice. …Get it? Ice?"),
    BOSS_INTRO_FROST_TITAN_M1_PIP("I can't tell if he's bad or on a break."),
    BOSS_INTRO_SHADOW_LICH_M1_1("SHHHH! This is a QUIET grove!"),
    BOSS_INTRO_SHADOW_LICH_M1_2("Your card is TWO HUNDRED years late!"),
    BOSS_INTRO_SHADOW_LICH_M1_3("Warden. Do you know what OVERDUE means?"),
    BOSS_INTRO_SHADOW_LICH_M1_4("It means BONK. *stamp* OVERDUE."),
    BOSS_INTRO_SHADOW_LICH_M1_PIP("Why are we whispering? …Why am I whispering?"),
    BOSS_INTRO_STORM_COLOSSUS_M1_1("I! AM! THE STORM!"),
    BOSS_INTRO_STORM_COLOSSUS_M1_2("My ship is a rock. My crew is thunder."),
    BOSS_INTRO_STORM_COLOSSUS_M1_3("Surrender your… wait. Is that MUD?"),
    BOSS_INTRO_STORM_COLOSSUS_M1_4("MUD! ON MY BOOTS! NOW YOU PAY!"),
    BOSS_INTRO_STORM_COLOSSUS_M1_PIP("Note to self: bring mud next time."),
    BOSS_INTRO_BLOODROOT_AVATAR_M1_1("H-hi. I'm Blush. Sorry."),
    BOSS_INTRO_BLOODROOT_AVATAR_M1_2("The Night said… um… bonk you?"),
    BOSS_INTRO_BLOODROOT_AVATAR_M1_3("I don't want to. But rules are rules."),
    BOSS_INTRO_BLOODROOT_AVATAR_M1_4("Please dodge? …Okay. Here I come. Sorry."),
    BOSS_INTRO_BLOODROOT_AVATAR_M1_PIP("Chief. I like her. …Bonk her gently?"),

    /** Boss-intro cutscenes, second meetings (waves 45–80): "YOU AGAIN" — two boss lines, Pip's comeback. */
    BOSS_INTRO_ANCIENT_GOLEM_M2_1("You again? Grum just fell asleep!"),
    BOSS_INTRO_ANCIENT_GOLEM_M2_2("Fine. Quick bonk. Then nap."),
    BOSS_INTRO_ANCIENT_GOLEM_M2_PIP("He brought a pillow, Chief!"),
    BOSS_INTRO_THORN_MATRIARCH_M2_1("Back for more scolding? Good!"),
    BOSS_INTRO_THORN_MATRIARCH_M2_2("Mama baked blame-cookies. Eat blame!"),
    BOSS_INTRO_THORN_MATRIARCH_M2_PIP("Do NOT eat the blame, Chief."),
    BOSS_INTRO_EMBER_WYRM_M2_1("The extra returns! Fans first!"),
    BOSS_INTRO_EMBER_WYRM_M2_2("This time I burn you in HD!"),
    BOSS_INTRO_EMBER_WYRM_M2_PIP("What's HD? …He doesn't know either."),
    BOSS_INTRO_VOID_KNIGHT_M2_1("Ah! My favorite… whoa—! …foe."),
    BOSS_INTRO_VOID_KNIGHT_M2_2("Shall we? Mind the rocks. I never do."),
    BOSS_INTRO_VOID_KNIGHT_M2_PIP("Somebody catch him! …Not it."),
    BOSS_INTRO_FROST_TITAN_M2_1("Little dude! Back for more chill?"),
    BOSS_INTRO_FROST_TITAN_M2_2("Same deal. Soft freeze. No stress."),
    BOSS_INTRO_FROST_TITAN_M2_PIP("He remembered us! …I think."),
    BOSS_INTRO_SHADOW_LICH_M2_1("YOU! Still loud! Still late!"),
    BOSS_INTRO_SHADOW_LICH_M2_2("Fine doubled. Bonk doubled."),
    BOSS_INTRO_SHADOW_LICH_M2_PIP("He brings a bigger stamp. Run."),
    BOSS_INTRO_STORM_COLOSSUS_M2_1("Back to my waters, tiny sailor?"),
    BOSS_INTRO_STORM_COLOSSUS_M2_2("This time NO mud. I checked. Twice."),
    BOSS_INTRO_STORM_COLOSSUS_M2_PIP("No mud here, Captain!"),
    BOSS_INTRO_BLOODROOT_AVATAR_M2_1("Oh! Hi again! …Sorry!"),
    BOSS_INTRO_BLOODROOT_AVATAR_M2_2("I practiced bonking. I'm still bad."),
    BOSS_INTRO_BLOODROOT_AVATAR_M2_PIP("She practiced! So sweet! …Bonk her."),

    /** Boss-intro cutscenes, third meetings (waves 85–120): running gags — two boss lines, Pip's comeback. */
    BOSS_INTRO_ANCIENT_GOLEM_M3_1("Grum dreamed of you. You were loud."),
    BOSS_INTRO_ANCIENT_GOLEM_M3_2("Sit. Bonk. Nap. In that order."),
    BOSS_INTRO_ANCIENT_GOLEM_M3_PIP("We're in his dreams now. Big honor."),
    BOSS_INTRO_THORN_MATRIARCH_M3_1("You look thin! Are you eating?"),
    BOSS_INTRO_THORN_MATRIARCH_M3_2("Eat this thorn pie. Then bonk."),
    BOSS_INTRO_THORN_MATRIARCH_M3_PIP("The pie is moving. THE PIE IS MOVING."),
    BOSS_INTRO_EMBER_WYRM_M3_1("My fans demand a rematch! *crickets*"),
    BOSS_INTRO_EMBER_WYRM_M3_2("…My ONE fan. Where is my fan?"),
    BOSS_INTRO_EMBER_WYRM_M3_PIP("I'm right here! Worst show ever!"),
    BOSS_INTRO_VOID_KNIGHT_M3_1("A hundred nights! …Whoa—! …I live here now."),
    BOSS_INTRO_VOID_KNIGHT_M3_2("On the floor. It is nice here."),
    BOSS_INTRO_VOID_KNIGHT_M3_PIP("Should we help him up? …He seems happy."),
    BOSS_INTRO_FROST_TITAN_M3_1("A hundred nights and still cool."),
    BOSS_INTRO_FROST_TITAN_M3_2("Respect, little dude. Ice?"),
    BOSS_INTRO_FROST_TITAN_M3_PIP("He offered us ice! We're friends now, right?"),
    BOSS_INTRO_SHADOW_LICH_M3_1("A hundred nights of NOISE!"),
    BOSS_INTRO_SHADOW_LICH_M3_2("I wrote it all down. All of it."),
    BOSS_INTRO_SHADOW_LICH_M3_PIP("He wrote a book about us! We're famous!"),
    BOSS_INTRO_STORM_COLOSSUS_M3_1("Half the sea behind us, sailor!"),
    BOSS_INTRO_STORM_COLOSSUS_M3_2("My rock-ship sails at dawn. Be on it. As my prisoner."),
    BOSS_INTRO_STORM_COLOSSUS_M3_PIP("Prisoner with snacks? Ask about snacks."),
    BOSS_INTRO_BLOODROOT_AVATAR_M3_1("H-hi! I made you a card!"),
    BOSS_INTRO_BLOODROOT_AVATAR_M3_2("It says sorry. …In advance."),
    BOSS_INTRO_BLOODROOT_AVATAR_M3_PIP("SHE MADE US A CARD! Chief, keep it!"),

    /** Boss-intro cutscenes, fourth meetings (waves 125–160): respect cracks — two boss lines, Pip's
     *  comeback. The Night Shift starts to like the Chief, and hates that it does. */
    BOSS_INTRO_ANCIENT_GOLEM_M4_1("Grum naps less now. Watches you."),
    BOSS_INTRO_ANCIENT_GOLEM_M4_2("You fight… good. Do not tell the Night."),
    BOSS_INTRO_ANCIENT_GOLEM_M4_PIP("Did Grum just… praise us?"),
    BOSS_INTRO_THORN_MATRIARCH_M4_1("My babies fear you now. Good."),
    BOSS_INTRO_THORN_MATRIARCH_M4_2("A mother knows strength. Bonk Mama gently."),
    BOSS_INTRO_THORN_MATRIARCH_M4_PIP("Gently? CHIEF. GENTLY."),
    BOSS_INTRO_EMBER_WYRM_M4_1("You stole my crowd, extra!"),
    BOSS_INTRO_EMBER_WYRM_M4_2("Fine. Duet. You and me. After I burn you."),
    BOSS_INTRO_EMBER_WYRM_M4_PIP("He wants a duet! We're STARS!"),
    BOSS_INTRO_VOID_KNIGHT_M4_1("I polished my armor for you… whoa—!"),
    BOSS_INTRO_VOID_KNIGHT_M4_2("…The floor and I are old friends."),
    BOSS_INTRO_VOID_KNIGHT_M4_PIP("He polished! For US! …Somebody help him."),
    BOSS_INTRO_FROST_TITAN_M4_1("Almost dawn, little dude."),
    BOSS_INTRO_FROST_TITAN_M4_2("Freeze you soft. Always soft."),
    BOSS_INTRO_FROST_TITAN_M4_PIP("Soft freezes only. Best bad guy ever."),
    BOSS_INTRO_SHADOW_LICH_M4_1("One hundred fifty nights. Shhh."),
    BOSS_INTRO_SHADOW_LICH_M4_2("…You read my book? …Thank you."),
    BOSS_INTRO_SHADOW_LICH_M4_PIP("He smiled! …I think that was a smile."),
    BOSS_INTRO_STORM_COLOSSUS_M4_1("The sea ends soon, sailor."),
    BOSS_INTRO_STORM_COLOSSUS_M4_2("First mate. My offer stands. Mud and all."),
    BOSS_INTRO_STORM_COLOSSUS_M4_PIP("TAKE THE JOB, CHIEF! …After dawn."),
    BOSS_INTRO_BLOODROOT_AVATAR_M4_1("We're friends, right? …Say yes?"),
    BOSS_INTRO_BLOODROOT_AVATAR_M4_2("Okay. Bonk time. Friends bonk soft."),
    BOSS_INTRO_BLOODROOT_AVATAR_M4_PIP("SOFT BONKS! Everybody heard that!"),

    /** Boss-intro cutscenes, last meetings (waves 165–200): the farewell tour — two boss lines, Pip's
     *  comeback. Wave 200's Blush gets the last threat of the run, and it is the cutest one. */
    BOSS_INTRO_ANCIENT_GOLEM_M5_1("Last nap before dawn, little loud one."),
    BOSS_INTRO_ANCIENT_GOLEM_M5_2("Wake Grum… when it is morning."),
    BOSS_INTRO_ANCIENT_GOLEM_M5_PIP("We'll wake you, big guy. Promise."),
    BOSS_INTRO_THORN_MATRIARCH_M5_1("Mama knit you a scarf. Thorny."),
    BOSS_INTRO_THORN_MATRIARCH_M5_2("Wear it. Bonk Mama. Then breakfast."),
    BOSS_INTRO_THORN_MATRIARCH_M5_PIP("Breakfast! She said breakfast!"),
    BOSS_INTRO_EMBER_WYRM_M5_1("Final show! Sizzle! Sold out!"),
    BOSS_INTRO_EMBER_WYRM_M5_2("You were… a good rival. Do not cry."),
    BOSS_INTRO_EMBER_WYRM_M5_PIP("I'm not crying! …Encore!"),
    BOSS_INTRO_VOID_KNIGHT_M5_1("One last fall… see? No— whoa—!"),
    BOSS_INTRO_VOID_KNIGHT_M5_2("…Worth it. For you, old friend."),
    BOSS_INTRO_VOID_KNIGHT_M5_PIP("He called us friend! …Help him up. For real."),
    BOSS_INTRO_FROST_TITAN_M5_1("Last wave, little dude. Stay cool."),
    BOSS_INTRO_FROST_TITAN_M5_2("Dawn comes. Chill stays. Always."),
    BOSS_INTRO_FROST_TITAN_M5_PIP("Best bad guy ever. Don't tell the others."),
    BOSS_INTRO_SHADOW_LICH_M5_1("Final stamp. READ— *stamp*"),
    BOSS_INTRO_SHADOW_LICH_M5_2("…READER OF THE YEAR. Still overdue."),
    BOSS_INTRO_SHADOW_LICH_M5_PIP("We won! …Wait, what did we win?"),
    BOSS_INTRO_STORM_COLOSSUS_M5_1("Last storm, first mate!"),
    BOSS_INTRO_STORM_COLOSSUS_M5_2("After dawn, we sail. For real. No mud."),
    BOSS_INTRO_STORM_COLOSSUS_M5_PIP("No mud! …I'll pack mud anyway."),
    BOSS_INTRO_BLOODROOT_AVATAR_M5_1("Last bonk. …Can we be friends after?"),
    BOSS_INTRO_BLOODROOT_AVATAR_M5_2("Okay. Here I come. Sorry. Love you. Sorry."),
    BOSS_INTRO_BLOODROOT_AVATAR_M5_PIP("Everybody… that was the cutest threat ever.");

    private final String english;

    StoryStrings(String english) {
        this.english = english;
    }

    @Override
    public String key() {
        return name();
    }

    @Override
    public String english() {
        return english;
    }

}
