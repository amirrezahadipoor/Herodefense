package com.amirrezahadipoor.herodefense.story;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Granny's own memories (roadmap ST3): the forty-eight entries the Grove Codex shows the player,
 * each unlocked by the moment that earns it. The words were rewritten with the whole story
 * (2026-09-23, MEMORY P3): Granny writes them herself, in common words and short sentences --
 * the waves are the nights, the Hollow is the night itself, Pip counts everything twice, and
 * the grove grows anyway.
 */
public final class LoreCatalog {

    private LoreCatalog() {
    }

    private static final List<LoreEntry> ALL = List.copyOf(Arrays.asList(
            entry(1, "Night One.", "So. You are the new Chief. Pip picked you, and Pip is never wrong about hearts. Stay close to my light, dearie.", LoreTrigger.WAVE_MILESTONE, "1"),
            entry(2, "Three Roads.", "They only come from three sides. I never learned what holds back the fourth. Whatever it is, I thank it daily.", LoreTrigger.WAVE_MILESTONE, "10"),
            entry(3, "Counting Nights.", "I used to count seasons. Now I count nights. Yours are the first ones I count with a smile.", LoreTrigger.WAVE_MILESTONE, "20"),
            entry(4, "Found Things.", "What drops from them still remembers being useful. Take it, dearie. Better your pockets than my roots.", LoreTrigger.WAVE_MILESTONE, "30"),
            entry(5, "The Watchers.", "Not every rootling attacks. Some just stand at the treeline and watch. Let them watch. We are worth watching.", LoreTrigger.WAVE_MILESTONE, "40"),
            entry(6, "Old Names.", "Rootling. Stonekin. Gloom Wolf. I named three of those things once, when I meant something kinder. Names stick. Be kind with yours.", LoreTrigger.WAVE_MILESTONE, "60"),
            entry(7, "The Long Middle.", "No song is ever written about this part. Not the falling, not the standing. Just the holding. Hold anyway, dearie.", LoreTrigger.WAVE_MILESTONE, "80"),
            entry(8, "Twig.", "Three of us now. Twig writes poems already. One more seed to go, dearie. We grow anyway.", LoreTrigger.WAVE_MILESTONE, "100"),
            entry(9, "Grum.", "He was our guard before he was their guard. The Night never turned him. It only told him the fight never ended, and he believed it. Poor heavy boy.", LoreTrigger.BOSS_FIRST_KILL, "ANCIENT_GOLEM"),
            entry(10, "Mama Bramble.", "She grew half the bad guys herself, back when growing things was all she did. She still packs snacks for battle. Do not eat the snacks.", LoreTrigger.BOSS_FIRST_KILL, "THORN_MATRIARCH"),
            entry(11, "Sizzle.", "Fire is supposed to go out. This one said no, learned to pose, and hired no one. The Night's hottest star. His words, not mine.", LoreTrigger.BOSS_FIRST_KILL, "EMBER_WYRM"),
            entry(12, "Sir Falls-A-Lot.", "Very polite. Very clumsy. He has fallen down every stair in the Night and apologized to each one. Catch him if you can. He will thank you.", LoreTrigger.BOSS_FIRST_KILL, "VOID_KNIGHT"),
            entry(13, "Big Chill.", "Evil? Or just on holiday? The frost came after the Hollow, not before. He freezes you soft. He insists on soft.", LoreTrigger.BOSS_FIRST_KILL, "FROST_TITAN"),
            entry(14, "Old Page.", "He was the record keeper before he was a boss. Now he keeps the record of every fall, and fines you for each one. Fines are final, dearie.", LoreTrigger.BOSS_FIRST_KILL, "SHADOW_LICH"),
            entry(15, "Captain Thunder.", "His ship is a rock. His crew is thunder. He fears no storm and no sailor. He fears mud. Bring mud.", LoreTrigger.BOSS_FIRST_KILL, "STORM_COLOSSUS"),
            entry(16, "Blush.", "Sorry about this one. Really sorry. She does not want to bonk you, but rules are rules. She made you a card. It says sorry. In advance.", LoreTrigger.BOSS_FIRST_KILL, "BLOODROOT_AVATAR"),
            entry(17, "The Pop.", "That one pops. Do not hug it. The pop is relief, Pip says, and then he laughs for a minute. Weird boy. Lovely boy.", LoreTrigger.ELITE_KILL, "blightburst"),
            entry(18, "The Rude Shield.", "It guards nothing and still guards. I notice it flinches toward protecting, even now. Old habits, dearie. Mine is soup.", LoreTrigger.ELITE_KILL, "rootward_ward"),
            entry(19, "The Yuck.", "Do not step in the yuck. Everything rotting wants to get back to the soil. This soil grows more of itself. Rude soil.", LoreTrigger.ELITE_KILL, "weeping_rot"),
            entry(20, "Again.", "You came back. I did not expect that so soon. I am starting to recognize your footsteps. After all these years, that is not nothing.", LoreTrigger.ASCENSION, "1"),
            entry(21, "Twice Now.", "Twice now. I saved you some light, like always. The night is learning your name. Let it shake, Pip says. Pip is right.", LoreTrigger.ASCENSION, "2"),
            entry(22, "What Stays.", "The waves start over. The dark starts over. You do not. Not all the way. I have watched enough Chiefs to know.", LoreTrigger.ASCENSION, "3"),
            entry(23, "Five Dawns.", "Five dawns you have given me. Sometimes I wonder if the Hollow gets tired like you do. I have decided not to ask. I am busy counting your light.", LoreTrigger.ASCENSION, "5"),
            entry(24, "Ten Dawns.", "I once sorted my guards by how long they lasted. Now I sort them by whether they came back. You keep coming back, dearie.", LoreTrigger.ASCENSION, "10"),
            entry(25, "Bare-Handed.", "You did that with what you were given, not what you bought. Discipline or stubbornness? Around here they are the same root.", LoreTrigger.SECRET, "bare_handed"),
            entry(26, "A Full Set.", "Things that match hold together better. I could have told you before you spent the coin. But you look lovely, dearie.", LoreTrigger.SECRET, "full_set"),
            entry(27, "Ten Times Clean.", "Ten times so clean it no longer looks like effort. I remember when standing here felt like that. It was a Tuesday.", LoreTrigger.SECRET, "mastery"),
            entry(28, "Reforged.", "Nothing stays the way it was made. Least of all you. I mean that kindly. Mostly.", LoreTrigger.SECRET, "reforged"),
            entry(29, "Six Wonders.", "Six one-of-a-kind things in one grove. I did not think we had that many wonders left. I am glad I was wrong.", LoreTrigger.SECRET, "six_mythics"),
            entry(30, "No Potions.", "You never once needed the weakest thing I could give you. I hope that was strength. Either way, soup is still on.", LoreTrigger.SECRET, "no_potions"),
            entry(31, "The Long Pause.", "Go if you must. Come back when you can. Waiting is my specialty. Pip naps while he waits. I count. We are good at this.", LoreTrigger.SECRET, "long_pause"),
            entry(32, "Every Elite.", "You have heard every fragment Pip can shout through them now. There is more to tell. There is always more. Pip will find it.", LoreTrigger.SECRET, "every_elite"),
            entry(33, "Fastest Fall.", "That was over before the Hollow finished sending it. I do not think it noticed yet. Do not tell it. Let it find out.", LoreTrigger.SECRET, "fastest_fall"),
            entry(34, "Twice to Dawn.", "The first time was survival. The second time? Say it to yourself, dearie. Then come have soup. You earned it twice.", LoreTrigger.SECRET, "wave200_twice"),
            entry(35, "The Hill.", "It wears a hill. Cheater. It simply decides to be stone for a while, and stone does not care how hard you try.", LoreTrigger.ELITE_KILL, "stoneshell"),
            entry(36, "Angry Ground.", "Everything that dies here leaves something in the soil. Most of it is quiet. Walk around the parts that still remember.", LoreTrigger.ELITE_KILL, "gravebloom"),
            entry(37, "More Friends.", "It called friends! Unfair! I used to think killing it was the end of it. It always had more. Count them with Pip. He loves counting.", LoreTrigger.ELITE_KILL, "swarmcall"),
            entry(38, "Long Arms.", "Long arms! Longer fouls! Stand close and it keeps its word. Stand far and the word means nothing. Fairest thing in the grove.", LoreTrigger.ELITE_KILL, "spitebarb"),
            entry(39, "Arm Up.", "Arm up! Move, Chief! The ground goes dark in a circle, and you have that long to be elsewhere. Step. That is the whole lesson.", LoreTrigger.ELITE_KILL, "hammerfall"),
            entry(40, "The Howler.", "Hear the howl and you found the one that sets the pace. Find the howler. Bonk it. The wave will remember it is tired.", LoreTrigger.ELITE_KILL, "bloodhowl"),
            entry(41, "One Becomes Two.", "One becomes two! Bad magic! Kill the loud one and something quieter is already standing in its spot. Two small quiets. Still loud.", LoreTrigger.ELITE_KILL, "hollowmolt"),
            entry(42, "Moss on a Wound.", "Moss on a wound. Still a wound. It heals the way a root drinks: slowly, and in the wrong direction. Stop healing. I mean it. Stop.", LoreTrigger.ELITE_KILL, "gravemoss"),
            entry(43, "Hot Hug.", "Hot hug! No hugs! Standing close to that one is standing close to a fire nobody will put out. Warm grief. Stay back, dearie.", LoreTrigger.ELITE_KILL, "cinderhalo"),
            entry(44, "Open Hearth.", "Some nights I give you nothing to hide behind. Two stones, one each side, and the rest is you and the distance you keep. Breathe. The open ground believes in you.", LoreTrigger.FIELD_FIRST_NIGHT, "OPEN_HEARTH"),
            entry(45, "Standing Stones.", "Four stones stood here before either of us, and they have not moved. Walk behind one. Your arrows stop at it too. The stone does not take sides.", LoreTrigger.FIELD_FIRST_NIGHT, "STANDING_STONES"),
            entry(46, "The Thornhedge.", "Small stones in a line, low enough to shoot over, high enough to trip a charge. Something planted them in a shape. Pip claims it was him. It was not him.", LoreTrigger.FIELD_FIRST_NIGHT, "THORNHEDGE"),
            entry(47, "The Ruined Ring.", "Someone built a circle and left four gaps, one for each road. They were expecting our three directions too. They left no note. Only the ring. And us.", LoreTrigger.FIELD_FIRST_NIGHT, "RUINED_RING"),
            entry(48, "Held, Not Blocked.", "There is a difference between a shield raised early and a shield raised into the blow. The first takes a share. The second takes all of it. Raise it late, dearie.", LoreTrigger.SET_HELD, "set")
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
