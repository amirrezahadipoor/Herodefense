package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;

/**
 * Immutable definition of one Grove Codex entry: both languages of the text plus its unlock condition.
 *
 * <p>The story voice document once froze the codex as English-only; the game ships two languages everywhere
 * else, and a Persian player opening the codex would have met the one wall of English left in the run, so each
 * entry now carries its Persian twin. The two voices stay what {@code docs/STORY_VOICE.md} defines for the
 * English: the Hero terse, the Tree calm and plain. The Persian follows {@code docs/PERSIAN_PROOFREAD.md}'s
 * standards — no Latin letters, no ASCII digits, the grove's own vocabulary («دره» for the Hollow,
 * «دانشنامهٔ بیشه» for the codex, «چوب دل» for heartwood).
 */
public final class LoreEntry {

    private final String id;
    private final int number;
    private final String title;
    private final String body;
    private final String titlePersian;
    private final String bodyPersian;
    private final LoreTrigger trigger;
    private final String triggerParam;

    public LoreEntry(
        String id,
        int number,
        String title,
        String body,
        String titlePersian,
        String bodyPersian,
        LoreTrigger trigger,
        String triggerParam
    ) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.body = body;
        this.titlePersian = titlePersian;
        this.bodyPersian = bodyPersian;
        this.trigger = trigger;
        this.triggerParam = triggerParam;
    }

    public String id() {
        return id;
    }

    public int number() {
        return number;
    }

    /** The title in the language the game is speaking now. */
    public String title() {
        return GameLocale.current() == GameLanguage.PERSIAN ? titlePersian : title;
    }

    public String title(GameLanguage language) {
        return language == GameLanguage.PERSIAN ? titlePersian : title;
    }

    /** The body in the language the game is speaking now. */
    public String body() {
        return GameLocale.current() == GameLanguage.PERSIAN ? bodyPersian : body;
    }

    public String body(GameLanguage language) {
        return language == GameLanguage.PERSIAN ? bodyPersian : body;
    }

    public LoreTrigger trigger() {
        return trigger;
    }

    public String triggerParam() {
        return triggerParam;
    }
}
