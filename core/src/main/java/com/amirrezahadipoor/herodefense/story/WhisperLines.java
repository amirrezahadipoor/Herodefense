package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.i18n.Translated;

import java.util.List;
import java.util.Map;

/**
 * Idle-whisper pool: short Pip bursts, each shown at most once ever, when
 * a Resume follows a single real-time pause of at least 300 seconds. Order is fixed: the first still-unused line
 * is always next, and an exhausted pool stays silent.
 */
public final class WhisperLines {

    private static final List<Translated> ENTRIES = List.of(
        StoryStrings.WHISPER_ONE,
        StoryStrings.WHISPER_TWO,
        StoryStrings.WHISPER_THREE,
        StoryStrings.WHISPER_FOUR,
        StoryStrings.WHISPER_FIVE,
        StoryStrings.WHISPER_SIX
    );

    private WhisperLines() {
    }

    /** The pool in the game's current language, in fixed order, for tests that walk the whole set. */
    public static List<String> lines() {
        return ENTRIES.stream().map(GameLocale::text).toList();
    }

    public static String idFor(int index) {
        return "whisper_" + (index + 1);
    }

    /** The first line whose id is absent from {@code usedIds}, or null once all are used. */
    public static String firstUnused(Map<String, Boolean> usedIds) {
        for (int index = 0; index < ENTRIES.size(); index++) {
            if (usedIds == null || !Boolean.TRUE.equals(usedIds.get(idFor(index)))) {
                return GameLocale.text(ENTRIES.get(index));
            }
        }
        return null;
    }

    /**
     * Records the given pool line as used. Marking matches on the current language's line, which is what the
     * pool's only caller can see; a language switch mid-run keeps the already-spoken id spent.
     */
    public static void markUsed(Map<String, Boolean> usedIds, String line) {
        if (usedIds == null || line == null) {
            return;
        }
        int index = lines().indexOf(line);
        if (index >= 0) {
            usedIds.put(idFor(index), Boolean.TRUE);
        }
    }
}
