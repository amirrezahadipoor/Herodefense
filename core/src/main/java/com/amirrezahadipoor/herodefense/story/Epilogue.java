package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.i18n.Translated;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.List;

/**
 * Branching end-of-run epilogues, verbatim (§6). A = flawless victory, B = hard-fought
 * victory, C/D/E = falls before wave 50 / at 50–149 / at 150+. On wins only, the two
 * tier-independent §2.5 transition lines follow the epilogue before the Ascend prompt.
 */
public enum Epilogue {
    A(StoryStrings.EPILOGUE_A_ONE, StoryStrings.EPILOGUE_A_TWO, StoryStrings.EPILOGUE_A_THREE),
    B(StoryStrings.EPILOGUE_B_ONE, StoryStrings.EPILOGUE_B_TWO, StoryStrings.EPILOGUE_B_THREE),
    C(StoryStrings.EPILOGUE_C_ONE, StoryStrings.EPILOGUE_C_TWO, StoryStrings.EPILOGUE_C_THREE),
    D(StoryStrings.EPILOGUE_D_ONE, StoryStrings.EPILOGUE_D_TWO, StoryStrings.EPILOGUE_D_THREE),
    E(StoryStrings.EPILOGUE_E_ONE, StoryStrings.EPILOGUE_E_TWO, StoryStrings.EPILOGUE_E_THREE);

    /** Tier-independent Ascension transition, shown after the epilogue on wins only (§2.5). */
    public static final List<Translated> TRANSITION = List.of(
        StoryStrings.EPILOGUE_TRANSITION_ONE, StoryStrings.EPILOGUE_TRANSITION_TWO
    );

    private final Translated[] lines;

    Epilogue(Translated... lines) {
        this.lines = lines;
    }

    /** The three beats in the language the game is speaking now. */
    public List<String> lines() {
        String[] out = new String[lines.length];
        for (int index = 0; index < lines.length; index++) {
            out[index] = GameLocale.text(lines[index]);
        }
        return List.of(out);
    }

    /** The two §2.5 transition beats in the current language. */
    public static List<String> transitionLines() {
        String[] out = new String[TRANSITION.size()];
        for (int index = 0; index < TRANSITION.size(); index++) {
            out[index] = GameLocale.text(TRANSITION.get(index));
        }
        return List.of(out);
    }

    /**
     * Ending to show: the id recorded when the run ended wins, so a later detour (root
     * network, reload) can never reselect; saves without an id select live.
     */
    public static Epilogue endingFor(GameState state) {
        if (state.epilogueId != null) {
            for (Epilogue epilogue : values()) {
                if (epilogue.name().equals(state.epilogueId)) {
                    return epilogue;
                }
            }
        }
        return select(state);
    }

    /**
     * Selects the epilogue for a finished run. A needs Wave 200 cleared with no Hero death,
     * fewer than 3 potions used, and finishing HP at or above 30% of max — B is every other
     * Wave-200 clear (a Hero death always ends the run, so the doc's "died and revived" clause
     * can never occur). Losses split by Game Over wave: C below 50, D at 50–149, E at 150+
     * (a Wave-200 death is E).
     */
    public static Epilogue select(GameState state) {
        if (state.runComplete) {
            boolean flawless = !state.heroDiedThisRun
                && state.potionsUsedThisRun < 3
                && state.hero != null
                && state.hero.maxHealth > 0f
                && state.hero.health / state.hero.maxHealth >= 0.3f;
            return flawless ? A : B;
        }
        int wave = Math.max(1, state.waveNumber);
        if (wave < 50) {
            return C;
        }
        if (wave < 150) {
            return D;
        }
        return E;
    }
}
