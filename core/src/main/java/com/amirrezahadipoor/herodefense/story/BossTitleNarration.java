package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.audio.NarrationRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * F3: Boss title cards written as if being read aloud now have a voice.
 */
public final class BossTitleNarration {
    private static final Map<String, String> TITLE_CARDS = new HashMap<>();
    static {
        TITLE_CARDS.put("ANCIENT_GOLEM", "Ancient Golem awakens. Stone remembers what flesh forgets.");
        TITLE_CARDS.put("EMBER_WYRM", "Ember Wyrm descends. The grove burns from above.");
        TITLE_CARDS.put("VOID_KNIGHT", "Void Knight arrives. Light bends and breaks.");
        TITLE_CARDS.put("THORN_MATRIARCH", "Thorn Matriarch rises. Roots that bind, thorns that bleed.");
        TITLE_CARDS.put("FROST_TITAN", "Frost Titan marches. Winter that never ends.");
        TITLE_CARDS.put("STORM_SERPENT", "Storm Serpent coils. Sky itself turns against you.");
        TITLE_CARDS.put("PLAGUE_HERALD", "Plague Herald spreads. Decay given form.");
        TITLE_CARDS.put("OBLIVION_CORE", "Oblivion Core pulses. The end of all songs.");
    }

    private BossTitleNarration() {}

    public static NarrationRequest forBoss(String bossType) {
        if (bossType == null) return null;
        String card = TITLE_CARDS.getOrDefault(bossType.toUpperCase(), bossType.replace('_', ' ') + " approaches.");
        return NarrationRequest.bossTitle(bossType, card);
    }

    public static Map<String, String> allTitleCards() {
        return new HashMap<>(TITLE_CARDS);
    }

    public static int titleCardCount() {
        return TITLE_CARDS.size();
    }
}
