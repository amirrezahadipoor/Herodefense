package com.amirrezahadipoor.herodefense.story;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Granny's bios of the Night Shift (MEMORY P3): each renders as the second paragraph of its
 * Codex entry 9–16 detail view, unlocked together with the entry. Fond but firm, the way she
 * writes about anyone who has tried to bonk her Chief -- and each carries the hint that beats
 * its boss. {@code docs/STORY_CONTENT.md} section 3 is reworded to match in P3d.
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
                "Grum naps through meetings and sits on problems. His blow lands where you were, not where you are. Be somewhere else, dearie."));
        map.put("THORN_MATRIARCH",
            new Bio(
                "Mama scolds first and bonks second. Her babies fear you now, and she respects that. Bonk her gently. She knit you a scarf."));
        map.put("EMBER_WYRM",
            new Bio(
                "Sizzle burns brightest from the left side. That is his bad side, and he will tell you so. Applause confuses him. Use it."));
        map.put("VOID_KNIGHT",
            new Bio(
                "Sir challenges you, bows, and falls over. Mind the rocks. He never does. Under all that armor beats the politest heart I know."));
        map.put("FROST_TITAN",
            new Bio(
                "Chill offers ice before every fight. Take it. It is good ice. Then dodge left. He always starts left. He is chill like that."));
        map.put("SHADOW_LICH",
            new Bio(
                "Old Page shushes the whole grove, then stamps you OVERDUE. His stamp is slow but certain. Read his book. It is about you. All of it."));
        map.put("STORM_COLOSSUS",
            new Bio(
                "The Captain checks his boots twice before every battle. Mud ruins his day, and your aim fixes it. Sail with him after dawn. He already asked."));
        map.put("BLOODROOT_AVATAR",
            new Bio(
                "Blush apologizes before, during, and after every blow. Dodge kindly. She practiced bonking for you, and she is still bad at it. Bless her."));
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

    /** Detail text for an entry: Granny's body first, boss bio second when one applies. */
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
