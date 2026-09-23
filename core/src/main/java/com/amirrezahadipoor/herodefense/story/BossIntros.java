package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.i18n.Translated;

/**
 * The Night Shift's pre-wave trash-talk (MEMORY P4): which table entries a boss-intro cutscene
 * speaks. First meetings get four boss lines plus Pip's comeback; later meetings get two plus
 * Pip's. The meeting needs no persistence: it is pure arithmetic on the wave (MEMORY §3.4), so
 * old saves just work.
 *
 * <p>Entries come back unresolved ({@link Translated}) so the cinematic snapshots them in the
 * game's language when it begins, the way the opening snapshots its line set.
 */
public final class BossIntros {

    private BossIntros() {
    }

    /**
     * Which meeting a boss wave is: 1 for waves 5–40, 2 for 45–80, 3 for 85–120,
     * 4 for 125–160,
     * 5 for 165–200. Callers pass boss waves; anything below wave 5 reads as the first meeting.
     */
    public static int meetingForWave(int waveNumber) {
        return meetingForBossNumber(waveNumber / 5);
    }

    /**
     * Which meeting an encounter number (1-based, wave/5) is, clamped to meetings 1–5: anything
     * below the first reads as the first meeting, anything past the farewell tour reads as tour 5.
     */
    public static int meetingForBossNumber(int bossNumber) {
        return Math.max(1, Math.min(5, (bossNumber - 1) / 8 + 1));
    }

    /** How many trash-talk lines a meeting speaks: four for the first, two for every later one. */
    public static int talkCount(int meeting) {
        return clampedMeeting(meeting) == 1 ? 4 : 2;
    }

    /**
     * One trash-talk line, 0-based; null for unknown bosses, out-of-range meetings slots, or
     * out-of-range indexes. Meeting numbers above 5 read as the farewell tour.
     */
    public static Translated talkLine(String bossType, int meeting, int index) {
        int clampedMeeting = clampedMeeting(meeting);
        if (index < 0 || index >= talkCount(clampedMeeting)) {
            return null;
        }
        return entryOrNull("BOSS_INTRO_" + bossType + "_M" + clampedMeeting + "_" + (index + 1));
    }

    /** Pip's comeback closing a boss's intro; null for unknown bosses. */
    public static Translated comebackLine(String bossType, int meeting) {
        return entryOrNull("BOSS_INTRO_" + bossType + "_M" + clampedMeeting(meeting) + "_PIP");
    }

    /**
     * The first-encounter title card, read from the one table that owns the cards. The card shows
     * inside the intro's TITLE phase on first meetings (and small on repeats), never over combat.
     */
    public static String titleFor(String bossType) {
        return BossTitleCards.titleFor(bossType);
    }

    private static int clampedMeeting(int meeting) {
        return Math.max(1, Math.min(5, meeting));
    }

    /** The table entry behind a derived intro key, or null when no such key exists. */
    private static Translated entryOrNull(String key) {
        try {
            return StoryStrings.valueOf(key);
        } catch (IllegalArgumentException unknown) {
            // Unknown boss, misbuilt key: the intro has no line here and the box stays empty.
            return null;
        }
    }
}
