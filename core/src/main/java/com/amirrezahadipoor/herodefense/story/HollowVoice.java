package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.model.GameState;

import java.util.Map;

/**
 * The Hollow's own voice (roadmap ST1): the one speaker that addresses the player holding the
 * phone rather than the Hero on the field. Where the Tree comforts and the Warden reflects, the
 * Hollow notices -- deaths, mercies, reading habits -- and comments once, then never again.
 *
 * <p>Every line is claimed through a key in {@code codexUnlocked}, the same once-ever ledger the
 * whispers and title cards use, so a save that has heard a line never hears it twice and nothing
 * new has to be persisted.
 */
public final class HollowVoice {

    /** Codex-ledger keys the Hollow claims its lines with. */
    public static final String KEY_DEATH_FIRST = "hollow_death_first";
    public static final String KEY_DEATH_AGAIN = "hollow_death_again";
    public static final String KEY_SPARE = "hollow_spare";
    public static final String KEY_WAVE100 = "hollow_wave100";
    /** Bookkeeping key: a claimed death line has been revealed on the game-over panel. */
    public static final String KEY_DEATH_SHOWN = "hollow_death_shown";
    /** One spare key per spared watcher: "hollow_spare_1", "hollow_spare_2", ... -- the count IS the ledger. */
    public static final String KEY_SPARE_MILESTONE = "hollow_spare_";
    /** The greeting of a first session, and the verdict that waits for the session after a finished run. */
    public static final String KEY_HELLO = "hollow_hello";
    public static final String KEY_RUN_COMPLETE = "hollow_run_complete";
    public static final String KEY_VERDICT = "hollow_verdict";
    /** Which spare milestone speaks: the first (mercy noticed) and the third (mercy as habit). */
    private static final int FIRST_SPARE = 1;
    private static final int MERCY_HABIT_SPARE = 3;

    private HollowVoice() {
    }

    /** Marks {@code key} claimed and returns true when this is the first time. */
    public static boolean claim(GameState state, String key) {
        if (state == null || state.codexUnlocked == null) {
            return false;
        }
        return !Boolean.TRUE.equals(state.codexUnlocked.put(key, Boolean.TRUE));
    }

    /** The line for a Hero's fall, or null when the Hollow has nothing new to say. */
    public static String lineForDeath(GameState state) {
        if (claim(state, KEY_DEATH_FIRST)) {
            clearShown(state);
            return GameLocale.text(StoryStrings.HOLLOW_DEATH_FIRST);
        }
        if (claim(state, KEY_DEATH_AGAIN)) {
            clearShown(state);
            return GameLocale.text(StoryStrings.HOLLOW_DEATH_AGAIN);
        }
        return null;
    }

    /** A freshly claimed death line re-queues: the panel owes the player this one too. */
    private static void clearShown(GameState state) {
        if (state != null && state.codexUnlocked != null) {
            state.codexUnlocked.remove(KEY_DEATH_SHOWN);
        }
    }

    /**
     * The line for sparing a watcher, or null on the spares the Hollow lets pass in silence.
     * The first spare is noticed; the third is named as what it has become. Each spare claims the
     * next numbered key, so the ledger of spared lives is the ledger of hollow_spare_N keys and
     * nothing new has to be persisted.
     */
    public static String lineForSpare(GameState state) {
        if (state == null || state.codexUnlocked == null) {
            return null;
        }
        int spared = 0;
        for (String key : state.codexUnlocked.keySet()) {
            if (key != null && key.startsWith(KEY_SPARE_MILESTONE)) {
                spared++;
            }
        }
        if (!claim(state, KEY_SPARE_MILESTONE + (spared + 1))) {
            return null;
        }
        if (spared + 1 == FIRST_SPARE) {
            return GameLocale.text(StoryStrings.HOLLOW_SPARE);
        }
        if (spared + 1 == MERCY_HABIT_SPARE) {
            return GameLocale.text(StoryStrings.HOLLOW_MERCY_HABIT);
        }
        return null;
    }

    /**
     * The wave-one line of a session: a first meeting, or -- for the session after a finished
     * run -- the verdict, which depends on how the run treated its watchers. Everything is
     * claimed once-ever, so the Hollow greets the player exactly one way, exactly once.
     */
    public static String lineForGreeting(GameState state) {
        if (state == null || state.codexUnlocked == null) {
            return null;
        }
        if (Boolean.TRUE.equals(state.codexUnlocked.get(KEY_RUN_COMPLETE))
            && claim(state, KEY_VERDICT)) {
            return GameLocale.text(sparedAnything(state)
                ? StoryStrings.HOLLOW_VERDICT_MERCIFUL : StoryStrings.HOLLOW_VERDICT_STERN);
        }
        if (claim(state, KEY_HELLO)) {
            return GameLocale.text(StoryStrings.HOLLOW_HELLO);
        }
        return null;
    }

    /** Whether this save has ever let a watcher walk away. */
    private static boolean sparedAnything(GameState state) {
        for (String key : state.codexUnlocked.keySet()) {
            if (key != null && key.startsWith(KEY_SPARE_MILESTONE)) {
                return true;
            }
        }
        return false;
    }

    /** The silent fact that a run reached its end through the reward card; the verdict reads it. */
    public static void markRunComplete(GameState state) {
        claim(state, KEY_RUN_COMPLETE);
    }

    /** The line for the grove's first centennial planting, or null once it has been said. */
    public static String lineForGroveCeremony(GameState state) {
        if (claim(state, KEY_WAVE100)) {
            return GameLocale.text(StoryStrings.HOLLOW_WAVE100);
        }
        return null;
    }

    /**
     * The death line claimed at hero fall but not yet shown on the game-over panel, or null.
     * The panel marks it shown once it has actually revealed, so a death that interrupts the
     * app still finds its line waiting on the next panel.
     */
    public static String pendingDeathLine(GameState state) {
        if (state == null || state.codexUnlocked == null
            || Boolean.TRUE.equals(state.codexUnlocked.get(KEY_DEATH_SHOWN))) {
            return null;
        }
        boolean heardAgain = Boolean.TRUE.equals(state.codexUnlocked.get(KEY_DEATH_AGAIN));
        if (!heardAgain && !Boolean.TRUE.equals(state.codexUnlocked.get(KEY_DEATH_FIRST))) {
            return null;
        }
        return GameLocale.text(heardAgain
            ? StoryStrings.HOLLOW_DEATH_AGAIN : StoryStrings.HOLLOW_DEATH_FIRST);
    }

    /** Takes the parked death line off the panel's queue; safe to call more than once. */
    public static void markDeathLineShown(GameState state) {
        claim(state, KEY_DEATH_SHOWN);
    }

    /** How many Hollow lines this save has already heard; the codex shelf can show it. */
    public static int heardCount(GameState state) {
        if (state == null || state.codexUnlocked == null) {
            return 0;
        }
        int count = 0;
        for (Map.Entry<String, Boolean> entry : state.codexUnlocked.entrySet()) {
            if (entry.getKey() != null && entry.getKey().startsWith("hollow_")
                && Boolean.TRUE.equals(entry.getValue())) {
                count++;
            }
        }
        return count;
    }
}
