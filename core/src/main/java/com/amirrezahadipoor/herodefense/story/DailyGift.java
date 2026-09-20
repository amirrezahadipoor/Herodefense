package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.settings.LocalSettingsRepository;

/**
 * The Tree's daily gift (roadmap ST5): the return hook. The first session of a local day pays two
 * heartwood and the Tree says so; the rest of the day it keeps quiet. The marker lives on the
 * device, not the run, so starting a fresh vigil cannot farm it.
 */
public final class DailyGift {

    /** Heartwood kept from yesterday, paid on the day's first session. */
    public static final int HEARTWOOD_AMOUNT = 2;

    private DailyGift() {
    }

    /**
     * Pays today's gift exactly once per local day and returns the Tree's line, or null when the
     * day has already been paid. Idempotent per frame: the game may ask on every tick.
     */
    public static String claim(GameState state, LocalSettingsRepository settings, long epochDay) {
        if (state == null || settings == null || settings.lastGiftEpochDay() == epochDay) {
            return null;
        }
        settings.markGifted(epochDay);
        state.heartwood = Math.min(Integer.MAX_VALUE, state.heartwood + HEARTWOOD_AMOUNT);
        return GameLocale.text(StoryStrings.DAILY_GIFT);
    }
}
