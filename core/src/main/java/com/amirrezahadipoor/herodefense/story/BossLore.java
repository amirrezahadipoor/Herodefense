package com.amirrezahadipoor.herodefense.story;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Boss bios, verbatim from {@code docs/STORY_CONTENT.md} section 3. Each renders as the second
 * paragraph of its Codex entry 9–16 detail view, unlocked together with the entry.
 */
public final class BossLore {

    private BossLore() {
    }

    /** One bio. */
    private static final class Bio {
        final String english;

        Bio(String english) {
            this.english = english;
        }
    }

    private static final Map<String, Bio> BIOS = bios();

    private static Map<String, Bio> bios() {
        Map<String, Bio> map = new LinkedHashMap<>();
        map.put("ANCIENT_GOLEM",
            new Bio(
                "Before it was a weapon of the Hollow, it was the forest's oldest guard. A stone keeper that had not moved from its post in longer than the Tree could remember. The Hollow did not need to turn it. It only needed to make it think the fight had never ended."));
        map.put("THORN_MATRIARCH",
            new Bio(
                "She grew half the arena's Rootlings herself, back when growing things was all she did. What she plants now still takes root. It simply does not ask, and it is not kind."));
        map.put("EMBER_WYRM",
            new Bio(
                "When the Hollow first touched this ground, a thing here caught fire and never fully went out. The Wyrm is what that ember became once it learned to want more fuel."));
        map.put("VOID_KNIGHT",
            new Bio(
                "No one here remembers what it looked like before. It does not either. It only remembers falling, and it has spent every year since trying to make another thing fall with it."));
        map.put("FROST_TITAN",
            new Bio(
                "There was frost in this grove before the Hollow, and all of it stayed where the cold put it. The Titan is the first of that frost to move. The Hollow only had to teach it which way to walk."));
        map.put("SHADOW_LICH",
            new Bio(
                "The records it keeps are older than the Hollow. It wrote down every fall the grove took and never wrote down the name of what pushed. It still cannot write the difference."));
        map.put("STORM_COLOSSUS",
            new Bio(
                "It learned thunder from the Hollow's storms, and stillness from the mountain it was cut from. It chose both. That is why it does not hurry."));
        map.put("BLOODROOT_AVATAR",
            new Bio(
                "It remembers being part of me the way a branch remembers the rain it no longer feels. That memory was all the Hollow needed to dress in bark and call a traitor."));
        return Collections.unmodifiableMap(map);
    }

    /** The bio for a boss identity, or null when unknown. */
    public static String bioFor(String bossType) {
        if (bossType == null) {
            return null;
        }
        Bio bio = BIOS.get(bossType);
        if (bio == null) {
            return null;
        }
        return bio.english;
    }

    /** Detail text for an entry: Tree-voice body first, boss bio second when one applies. */
    public static String detailFor(LoreEntry entry) {
        if (entry == null) {
            return "";
        }
        if (entry.trigger() != LoreTrigger.BOSS_FIRST_KILL) {
            return entry.body();
        }
        String bio = bioFor(entry.triggerParam());
        if (bio == null) {
            return entry.body();
        }
        return entry.body() + "\n\n" + bio;
    }

    /** Every boss identity that has a bio, for glyph-building and coverage sweeps. */
    public static java.util.List<String> allBossTypes() {
        return new java.util.ArrayList<>(BIOS.keySet());
    }

}
