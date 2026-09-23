package com.amirrezahadipoor.herodefense.story;

/**
 * Immutable definition of one Grove Codex entry: the text plus its unlock condition.
 *
 * <p>The two voices stay what {@code docs/STORY_VOICE.md} defines: the Hero terse, the Tree calm
 * and plain.
 */
public final class LoreEntry {

    private final String id;
    private final int number;
    private final String title;
    private final String body;
    private final LoreTrigger trigger;
    private final String triggerParam;

    public LoreEntry(
        String id,
        int number,
        String title,
        String body,
        LoreTrigger trigger,
        String triggerParam
    ) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.body = body;
        this.trigger = trigger;
        this.triggerParam = triggerParam;
    }

    public String id() {
        return id;
    }

    public int number() {
        return number;
    }

    /** The title. */
    public String title() {
        return title;
    }

    /** The body. */
    public String body() {
        return body;
    }

    public LoreTrigger trigger() {
        return trigger;
    }

    public String triggerParam() {
        return triggerParam;
    }
}
