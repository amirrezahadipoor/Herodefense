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

    /** First-encounter boss title cards: one line per identity, shown once ever. D3 adds 4 more. */
    BOSS_ANCIENT_GOLEM("GRUM — Night Shift Security. Do not wake."),
    BOSS_THORN_MATRIARCH("MAMA BRAMBLE — She grew half the bad guys."),
    BOSS_EMBER_WYRM("SIZZLE — The hottest star of the Night."),
    BOSS_VOID_KNIGHT("SIR FALLS-A-LOT — Very polite. Very clumsy."),
    BOSS_FROST_TITAN("BIG CHILL — Evil? Or just on holiday?"),
    BOSS_SHADOW_LICH("OLD PAGE — Librarian. Fines are final."),
    BOSS_STORM_COLOSSUS("CAPTAIN THUNDER — Scared of puddles."),
    BOSS_BLOODROOT_AVATAR("BLUSH — Sorry about this. Really sorry."),

    /** The planting ceremony's five beats, in the order the ceremony walks through them. */
    CEREMONY_WALK_OUT("A tree should not carry this alone. Not anymore."),
    CEREMONY_PLANT("So we plant another. Grow loud. Grow angry. Grow."),
    CEREMONY_WATER("I will hold the line. That is what I am for."),
    CEREMONY_GROW("Two of us now. I remember what mornings sound like."),
    CEREMONY_WALK_BACK("Now hold the line. Both of us need you."),

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

    /** Mythic item flavor (§7), one per slot's Mythic. */
    MYTHIC_SUNFALL("One arrow. One dawn. Never missed."),
    MYTHIC_CROWN("See weak spots. Bonk them."),
    MYTHIC_BARK("Granny's bark. Heals you back."),
    MYTHIC_WINDRUNNER("Fast boots. Never run. Stand."),
    MYTHIC_VERDANT("A pinky promise in sap."),
    MYTHIC_EMBERLESS("Cold ember. Hot temper.");

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
