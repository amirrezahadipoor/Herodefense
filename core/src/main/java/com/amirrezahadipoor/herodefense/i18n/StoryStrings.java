package com.amirrezahadipoor.herodefense.i18n;

/**
 * The game's own voice (roadmap R7.3): what a boss's first title card says, what the Tree says while a
 * tree is planted, what the Warden says to themselves at a wave milestone, what the Hollow says to the
 * player, and the line the trophy chime puts on screen.
 *
 * <p>These are the places where the game speaks rather than reports, which is why they are one table
 * rather than four: they share three narrators, and a narrator whose voice differs between screens is a
 * narrator the player notices. The words were rewritten for a young reader (2026-09-22): common words,
 * short sentences, and one idea the whole run can follow -- the waves are the nights, the Hollow is the
 * night itself, and two hundred nights later the dawn comes back.
 *
 * <p>The em dash the title cards use is kept rather than swapped for the "  |  "
 * the button-and-panel tables use: a title card is a sentence rather than a row of readouts.
 *
 * <p>What this does not hold, and cannot yet: the trophy *names* that {@code TROPHY_NAMES} joins are
 * {@code progression/Trophy}'s own 24 titles, still on the provenance ratchet until that file is tabled.
 */
public enum StoryStrings implements Translated {

    /** First-encounter boss title cards: one line per identity, shown once ever. D3 adds 4 more. */
    BOSS_ANCIENT_GOLEM("ANCIENT GOLEM — the oldest guard, still on duty."),
    BOSS_THORN_MATRIARCH("THORN MATRIARCH — she grew half your enemies."),
    BOSS_EMBER_WYRM("EMBER WYRM — a fire that never went out."),
    BOSS_VOID_KNIGHT("VOID KNIGHT — it fell, and forgot the way back."),
    BOSS_FROST_TITAN("FROST TITAN — winter that learned to walk."),
    BOSS_SHADOW_LICH("SHADOW LICH — keeper of the second fall."),
    BOSS_STORM_COLOSSUS("STORM COLOSSUS — thunder stored in a stone chest."),
    BOSS_BLOODROOT_AVATAR("BLOODROOT AVATAR — the forest's own wound, walking."),

    /** The planting ceremony's five beats, in the order the ceremony walks through them. */
    CEREMONY_WALK_OUT("A tree should not carry this alone. Not anymore."),
    CEREMONY_PLANT("So we plant another. Grow loud. Grow angry. Grow."),
    CEREMONY_WATER("I will hold the line. That is what I am for."),
    CEREMONY_GROW("Two of us now. I remember what mornings sound like."),
    CEREMONY_WALK_BACK("Now hold the line. Both of us need you."),

    /** The Warden's reflections at wave milestones: 25, 50, 75, 125, 150, 175. */
    REFLECTION_WAVE_25("The wolves fear something bigger than me. That should scare me more."),
    REFLECTION_WAVE_50("Half of what I have killed, I once knew. I try not to think about it."),
    REFLECTION_WAVE_75("Past the treeline, the ground is wrong. Not ground at all."),
    REFLECTION_WAVE_125("Three trees now. Three times to lose. I would still make the trade."),
    REFLECTION_WAVE_150("It stopped sending the weak ones first. It is out of patience."),
    REFLECTION_WAVE_175("What is left may be the last. Or it wants me to believe that."),

    /** The trophy line: a count when there are too many to read, otherwise the names joined. */
    TROPHY_COUNT("Trophy - %1$s earned"),
    TROPHY_NAMES("Trophy - %1$s"),
    TROPHY_AND(" and "),

    /** The Hollow (roadmap ST1): the one voice that talks to the player, not the Warden. */
    HOLLOW_DEATH_FIRST("You fell. Not the Warden — you. I felt it happen."),
    HOLLOW_DEATH_AGAIN("Again. You always get up. I never get tired of watching."),
    HOLLOW_SPARE("You spared it? It was not even fighting. Mercy. I remember that word. No one uses it anymore."),
    HOLLOW_WAVE100("Halfway there. The Tree thanks you. I do not have to hurry. You do."),
    HOLLOW_HELLO("I am the night. They called me the Hollow. This is the last tree — and I have come for it."),
    HOLLOW_MERCY_HABIT("Three spared. You call it kindness. So do I. Mercy grows roots here too — watch what sprouts."),
    HOLLOW_VERDICT_MERCIFUL("Last time, you let some of them go. The Tree calls that mercy. I call it a debt — and I remember my debts."),
    HOLLOW_VERDICT_STERN("Last time, nothing on that field lived because you loved it. The Tree calls that victory. I call it an inventory."),

    /** The Hollow's half-health beat, one per boss identity. */
    HOLLOW_BOSS_GOLEM("It is slipping. It was never guarding you — it just cannot stop."),
    HOLLOW_BOSS_MATRIARCH("She calls the garden home. You are the frost."),
    HOLLOW_BOSS_WYRM("Its no grows quiet. Push."),
    HOLLOW_BOSS_VOID("It falls and wants company. Give it none."),
    HOLLOW_BOSS_TITAN("Winter keeps what it touches. Touch it back."),
    HOLLOW_BOSS_LICH("It is writing your name down. Do not give it a long one."),
    HOLLOW_BOSS_COLOSSUS("Thunder never hurries. You should."),
    HOLLOW_BOSS_BLOODROOT("The wound fights the cure. It always has."),

    /** The Tree's thank-you, spoken in the box the moment a run is completed: the summary waits for it,
     *  then rises — the victory's own parting word, in the Tree's own blips. */
    TREE_VICTORY("You kept the light alive, night after night. The dawn remembers you."),

    /** The Tree's daily gift (roadmap ST5): the return hook, spoken once a day. */
    DAILY_GIFT("Two heartwood, saved from yesterday. The Tree keeps count of your days. So do I."),

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
    CODEX_LOCKED_HINT("The Tree has not written this yet."),
    TROPHY_HEADER("WARDEN'S TROPHIES"),
    TROPHY_TAP_HINT("Tap a trophy to read what earns it."),

    /** The new-run opening, one line set per ascension tier (§1). Tier 0 shipped, 1 and 2 their own,
     *  3+ one shared set. Three lines each, typed in the box over the push-in. */
    OPENING_0_ONE("Can you protect the World Tree?!"),
    OPENING_0_TWO("Can you?"),
    OPENING_0_THREE("Are you sure?!"),
    OPENING_1_ONE("The dark comes back. It always does."),
    OPENING_1_TWO("The Tree is tired. So am I."),
    OPENING_1_THREE("Tonight we go further."),
    OPENING_2_ONE("It knows my name by now."),
    OPENING_2_TWO("Good. Let it remember."),
    OPENING_2_THREE("Roots first. Then the dark. Not today."),
    OPENING_3_ONE("New dawn. Same fight."),
    OPENING_3_TWO("The Tree asks: one more watch?"),
    OPENING_3_THREE("Say yes."),

    /** Branching end-of-run epilogues (§6). A = flawless, B = hard-fought, C/D/E = falls. */
    EPILOGUE_A_ONE("Two hundred nights. Not one step lost."),
    EPILOGUE_A_TWO("The night needs a new plan."),
    EPILOGUE_A_THREE("Until then, the Tree and I stand."),
    EPILOGUE_B_ONE("Two hundred nights. Every one of them close."),
    EPILOGUE_B_TWO("I do not remember all of it. I remember not letting go."),
    EPILOGUE_B_THREE("That is enough. It has to be."),
    EPILOGUE_C_ONE("Not even the middle."),
    EPILOGUE_C_TWO("The Tree fell quiet so early. It should not have."),
    EPILOGUE_C_THREE("Next time it is loud."),
    EPILOGUE_D_ONE("So close to the second root."),
    EPILOGUE_D_TWO("I went farther than before. Far is not far enough."),
    EPILOGUE_D_THREE("Again."),
    EPILOGUE_E_ONE("One tree stood when I fell. That counts."),
    EPILOGUE_E_TWO("The night paid for this run. It just lasted a little longer than me."),
    EPILOGUE_E_THREE("Next time it pays for everything."),
    EPILOGUE_TRANSITION_ONE("The night is not gone. It is quiet, learning how to fall again."),
    EPILOGUE_TRANSITION_TWO("Stand up. The Tree is still standing."),

    /** Whispering Wounds, one fragment pair per Elite affix (§4). */
    ELITE_BLIGHTBURST_ONE("It does not die. It just lets go — everything at once."),
    ELITE_BLIGHTBURST_TWO("That burst is not anger. It is relief."),
    ELITE_ROOTWARD_ONE("That shield is not armor. It is a root, remembering its job."),
    ELITE_ROOTWARD_TWO("Even like this, it still tries to protect. It just forgot what."),
    ELITE_WEEPING_ONE("Where it walks, the ground never heals."),
    ELITE_WEEPING_TWO("Follow its trail long enough. It leads to the Tree."),
    ELITE_HOLLOWMOLT_ONE("It never leaves a place empty. Nothing here does."),
    ELITE_HOLLOWMOLT_TWO("Two small silences where one loud one stood."),
    ELITE_GRAVEMOSS_ONE("The moss covers the wound while the wound is still there."),
    ELITE_GRAVEMOSS_TWO("That is not healing. That is something patient taking it back."),
    ELITE_CINDERHALO_ONE("Stand too close and it loves you — the way an ember loves wind."),
    ELITE_CINDERHALO_TWO("That heat is not attack. It is grief, still warm."),
    ELITE_STONESHELL_ONE("It pulled the hill over itself and called that armour."),
    ELITE_STONESHELL_TWO("Stone is patient. Stone is not on your side."),
    ELITE_GRAVEBLOOM_ONE("It died and the ground kept the grudge."),
    ELITE_GRAVEBLOOM_TWO("Do not stand where something was angry."),
    ELITE_SWARMCALL_ONE("Kill it and it calls for replacements. It has replacements."),
    ELITE_SWARMCALL_TWO("The grove keeps sending. The grove always keeps sending."),
    ELITE_SPITEBARB_ONE("It does not want you in reach. It made its reach a promise."),
    ELITE_SPITEBARB_TWO("Close work has a price here. It always did."),
    ELITE_HAMMERFALL_ONE("It raises its arm and the ground tells you where."),
    ELITE_HAMMERFALL_TWO("Step. That is the whole lesson."),
    ELITE_BLOODHOWL_ONE("It howls and the line walks faster. It is proud of them."),
    ELITE_BLOODHOWL_TWO("Follow the sound and you find the one holding the leash."),

    /** Idle-whisper pool (§8): six Tree-voice lines, one per long pause, each shown once ever. */
    WHISPER_ONE("The roots kept your seat warm while you were gone."),
    WHISPER_TWO("The Tree dreams, little guard — and it always wakes up."),
    WHISPER_THREE("I counted your absence in falling leaves — you were out a while."),
    WHISPER_FOUR("Rest is a weapon too — you are getting good at it."),
    WHISPER_FIVE("The roots grow deepest in the quiet between fights."),
    WHISPER_SIX("You are back — the paths never stopped watching for you."),

    /** Speaker labels over the dialogue box: the box types every message in the speaker's own voice, and
     *  the label tells who is speaking. Same three speakers the blips already tell apart by ear. */
    SPEAKER_WARDEN("WARDEN"),
    SPEAKER_TREE("TREE"),
    SPEAKER_HOLLOW("HOLLOW"),

    /** Mythic item flavor (§7), one per slot's Mythic. */
    MYTHIC_SUNFALL("Shot once, long ago, at a high fall. The arrow never came back whole."),
    MYTHIC_CROWN("Wear it and you see weak spots the way the Hollow sees strong ones."),
    MYTHIC_BARK("Cut from the World Tree's bark when it could spare wood. It knows how to close a wound."),
    MYTHIC_WINDRUNNER("Made for running. He never ran again after he put them on."),
    MYTHIC_VERDANT("A promise in sap. What heals you lets you keep healing."),
    MYTHIC_EMBERLESS("The ember that never went out — cooled, and put to work.");

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
