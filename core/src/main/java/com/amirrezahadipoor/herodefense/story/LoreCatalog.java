package com.amirrezahadipoor.herodefense.story;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Tree's own memories (roadmap ST3): the thirty-plus entries the Grove Codex shows the player,
 * each unlocked by the moment that earns it. The words were rewritten for a young reader (2026-09-22):
 * common words, short sentences, and the same spine as the spoken lines -- the waves are the nights,
 * the Hollow is the night itself, and the Tree remembers everyone who stood.
 */
public final class LoreCatalog {

    private LoreCatalog() {
    }

    private static final List<LoreEntry> ALL = List.copyOf(Arrays.asList(
            entry(1, "Before You.", "Others stood here before you. I do not remember most of their names. I remember all of their last nights.", LoreTrigger.WAVE_MILESTONE, "1"),
            entry(2, "The Three Directions.", "They never come from everywhere. Only from three sides. I never learned what holds back the fourth.", LoreTrigger.WAVE_MILESTONE, "10"),
            entry(3, "Counting.", "I used to count the seasons. Now I count the nights. Smaller pieces of time, and they pass just as slowly.", LoreTrigger.WAVE_MILESTONE, "20"),
            entry(4, "What Luck Finds.", "Some of what drops from them still remembers being useful. Take it. I would rather you had it than the ground.", LoreTrigger.WAVE_MILESTONE, "30"),
            entry(5, "The Quiet Ones.", "Not every rootling attacks. Some just stand at the treeline and watch. I do not know if that is worse.", LoreTrigger.WAVE_MILESTONE, "40"),
            entry(6, "Old Names.", "Rootling. Stonekin. Gloom Wolf. I gave three of those names to living things, once, when I meant something kinder.", LoreTrigger.WAVE_MILESTONE, "60"),
            entry(7, "The Long Middle.", "No song is ever written about this part. Not the falling, not the standing. Just the holding. Hold anyway.", LoreTrigger.WAVE_MILESTONE, "80"),
            entry(8, "A Grove Takes Root.", "I never asked for a second trunk or a third. I am glad for both. Grief is lighter when three trees carry it. So is standing guard.", LoreTrigger.WAVE_MILESTONE, "100"),
            entry(9, "What the Golem Guarded.", "Before the Tree, there was a border stone, and the Golem kept it. The Hollow did not turn it — it only made the Golem believe the fight never ended. Its blow lands where you were a moment ago, not where you are. Put your shield there, not your feet.", LoreTrigger.BOSS_FIRST_KILL, "ANCIENT_GOLEM"),
            entry(10, "The Matriarch's Garden.", "She is not attacking you with monsters. She is attacking you with her children. You deserve to know what you are ending. And why it might still be a mercy.", LoreTrigger.BOSS_FIRST_KILL, "THORN_MATRIARCH"),
            entry(11, "An Ember That Refused.", "Fire is supposed to go out. This one said no. A no, given enough years, grows a shape. The Wyrm is that no, wearing scales.", LoreTrigger.BOSS_FIRST_KILL, "EMBER_WYRM"),
            entry(12, "The Shape of Falling.", "I once asked the Void Knight what it wanted, in the only language I have: stillness and time. It did not answer. I do not think it remembers the question. I think it only remembers falling — and wanting company on the way down.", LoreTrigger.BOSS_FIRST_KILL, "VOID_KNIGHT"),
            entry(13, "Winter That Walks.", "The frost came after the Hollow, not before. It keeps whatever it touches. The Titan is what kept walking after everything else froze.", LoreTrigger.BOSS_FIRST_KILL, "FROST_TITAN"),
            entry(14, "The Second Fall.", "It was not a boss before. It was the record keeper. Now it keeps the record of how many times the grove has fallen, and how.", LoreTrigger.BOSS_FIRST_KILL, "SHADOW_LICH"),
            entry(15, "Thunder in Stone.", "Stone that learned to hold thunder instead of moss. It never hurries. Thunder never does.", LoreTrigger.BOSS_FIRST_KILL, "STORM_COLOSSUS"),
            entry(16, "The Grove's Wound.", "A root of the World Tree, taken and twisted. It bleeds sap that never dries. The Tree knows its shape.", LoreTrigger.BOSS_FIRST_KILL, "BLOODROOT_AVATAR"),
            entry(17, "On Letting Go.", "Some of them stop fighting you and start fighting what is inside them instead. Usually they lose both fights at once.", LoreTrigger.ELITE_KILL, "blightburst"),
            entry(18, "A Root's Last Job.", "I do not control what the Hollow does with what was once mine. But I notice it still flinches toward protecting, even now. That is either hope, or a very old habit. I have stopped trying to tell the difference.", LoreTrigger.ELITE_KILL, "rootward_ward"),
            entry(19, "The Trail Home.", "Every rotting thing wants to get back to the soil eventually. I only wish this kind of soil grew something other than more of itself.", LoreTrigger.ELITE_KILL, "weeping_rot"),
            entry(20, "Again.", "You came back. I did not expect that. I am not sure the Hollow did either. That might be the only advantage either of us has left.", LoreTrigger.ASCENSION, "1"),
            entry(21, "The Shape of a Habit.", "Twice now. I am starting to recognize your footsteps before I see you. After this many years of forgetting faces, that is not nothing.", LoreTrigger.ASCENSION, "2"),
            entry(22, "What Does Not Reset.", "The waves start over. The dark starts over. You do not. Not all the way. I have watched enough wardens to know the difference between starting fresh and just starting again.", LoreTrigger.ASCENSION, "3"),
            entry(23, "A Question I Do Not Ask Often.", "Sometimes I wonder if the Hollow gets tired the way you do. I have decided I do not want to know the answer badly enough to ask.", LoreTrigger.ASCENSION, "5"),
            entry(24, "The Long Watch.", "I have had guards who lasted a season and guards who lasted a lifetime. I do not sort them by that anymore. I sort them by whether they came back. You keep coming back.", LoreTrigger.ASCENSION, "10"),
            entry(25, "Bare-Handed", "You did that with what you were given, not what you bought. I do not know whether to call that discipline or stubbornness. Maybe they are the same root.", LoreTrigger.SECRET, "bare_handed"),
            entry(26, "A Full Set", "Things that match hold together better. I could have told you that before you spent the coin to learn it.", LoreTrigger.SECRET, "full_set"),
            entry(27, "Mastery, Spent", "You have done that ten times so clean it no longer looks like effort. I remember when standing here felt like that too.", LoreTrigger.SECRET, "mastery"),
            entry(28, "Reforged", "Nothing stays the way it was made. Least of all you. I mean that kindly.", LoreTrigger.SECRET, "reforged"),
            entry(29, "Six Mythics", "I did not think there were six things left in this whole grove worth calling one-of-a-kind. I am glad I was wrong.", LoreTrigger.SECRET, "six_mythics"),
            entry(30, "No Potions Spent", "You never once needed the weakest thing I could give you. I hope that was strength, and not just luck standing next to you the whole way.", LoreTrigger.SECRET, "no_potions"),
            entry(31, "The Long Pause", "Go if you have to. Come back when you can. Waiting is my specialty by now — I have had a lot of practice.", LoreTrigger.SECRET, "long_pause"),
            entry(32, "Every Elite, Once", "You have heard every fragment I can whisper through them now. There is more to tell. There is always more. It is just not theirs to carry.", LoreTrigger.SECRET, "every_elite"),
            entry(33, "Fastest Fall", "That was over before the Hollow finished sending it. I do not think it noticed yet.", LoreTrigger.SECRET, "fastest_fall"),
            entry(34, "Two Hundred, Once More", "The first time was survival. I suspect you already know what the second time was. Say it to yourself, if not to me.", LoreTrigger.SECRET, "wave200_twice"),
            entry(35, "The Hill's Armour", "It does not dodge and it does not flinch. It simply decides to be stone for a while, and stone does not care how hard you are trying.", LoreTrigger.ELITE_KILL, "stoneshell"),
            entry(36, "What the Ground Kept", "Everything that dies here leaves something in the soil. Most of it is quiet. Some of it is still angry where it stood, and it stays angry for a while.", LoreTrigger.ELITE_KILL, "gravebloom"),
            entry(37, "Replacements", "I used to think killing it was the end of it. It calls for more. It always had more. That is the part of this night I never learned how to count.", LoreTrigger.ELITE_KILL, "swarmcall"),
            entry(38, "A Promise at Arm's Length", "It wears its reach like a warning. Stand close and it keeps its word; stand far and the word means nothing. That is the fairest thing in the grove.", LoreTrigger.ELITE_KILL, "spitebarb"),
            entry(39, "The Ground Says Where", "It raises one arm and the ground goes dark in a circle. You have that long to be somewhere else. I have never seen anyone learn this faster than one night.", LoreTrigger.ELITE_KILL, "hammerfall"),
            entry(40, "The One Holding the Leash", "Hear the howl and you have found the one that decides how fast the line walks. Kill it and the whole wave remembers how tired it was.", LoreTrigger.ELITE_KILL, "bloodhowl"),
            entry(41, "Nothing Is Left Empty", "I said it once and I will say it a different way: the Hollow does not leave a place unattended. Kill the loud one and something quieter is already standing in its spot.", LoreTrigger.ELITE_KILL, "hollowmolt"),
            entry(42, "Patience With a Root", "It heals the way a root drinks: slowly, without hurry, and in the wrong direction. Wounds here do not close. They simply get covered.", LoreTrigger.ELITE_KILL, "gravemoss"),
            entry(43, "Grief, Still Warm", "Standing close to that one is like standing close to a fire someone refuses to put out. It is not trying to hurt you. It is just still burning.", LoreTrigger.ELITE_KILL, "cinderhalo"),
            entry(44, "An Open Hearth", "Some nights I give you nothing to hide behind. Two stones, one on each side, and the rest is you and the distance you keep. Breathe. The open ground is not a punishment. It is the plainest version of the question.", LoreTrigger.FIELD_FIRST_NIGHT, "OPEN_HEARTH"),
            entry(45, "The Ones Who Stand", "Four stones stood here before either of us, and they have not moved. Walk behind one and your arrows stop at it too -- the stone does not know which side of the night you are on.", LoreTrigger.FIELD_FIRST_NIGHT, "STANDING_STONES"),
            entry(46, "Low and Thorned", "Small stones walked in a line, low enough to shoot over and high enough to trip a charge. Something planted them in a shape. I have never found out what, and I have had a long time to look.", LoreTrigger.FIELD_FIRST_NIGHT, "THORNHEDGE"),
            entry(47, "A Ring, Broken", "Someone built a circle here and left four gaps in it, one for each road. Whoever they were, they were expecting the same three directions we get. They did not leave a note. Only the ring.", LoreTrigger.FIELD_FIRST_NIGHT, "RUINED_RING"),
            entry(48, "Held, Not Blocked", "There is a difference between a shield raised early and a shield raised into the blow. The first "
                    + "one takes a share of it. The second one takes all of it, and the wood knows you were "
                    + "watching. Raise it late and you will learn the difference the hard way.", LoreTrigger.SET_HELD, "set")
    ));

    public static List<LoreEntry> all() {
        return ALL;
    }

    public static LoreEntry byId(String id) {
        return BY_ID.get(id);
    }

    private static final Map<String, LoreEntry> BY_ID = index();

    private static Map<String, LoreEntry> index() {
        Map<String, LoreEntry> map = new LinkedHashMap<>();
        for (LoreEntry entry : ALL) {
            if (map.put(entry.id(), entry) != null) {
                throw new IllegalStateException("Duplicate codex id: " + entry.id());
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private static LoreEntry entry(
        int number,
        String title,
        String body,
        LoreTrigger trigger,
        String triggerParam
    ) {
        return new LoreEntry(
            String.format("codex_%02d", number), number, title, body,
            trigger, triggerParam
        );
    }
}
