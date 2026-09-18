package com.amirrezahadipoor.herodefense.audio;

/**
 * F3: No voice or narration for lore entries and boss title cards.
 * A request to speak a piece of text with context.
 */
public final class NarrationRequest {
    public enum Type { LORE_ENTRY, BOSS_TITLE, TUTORIAL, VICTORY }

    public final Type type;
    public final String id;
    public final String text;
    public final float urgency; // 0..1, boss titles higher

    public NarrationRequest(Type type, String id, String text, float urgency) {
        this.type = type;
        this.id = id;
        this.text = text;
        this.urgency = urgency;
    }

    public static NarrationRequest lore(String loreId, String title, String body) {
        String spoken = title + ". " + body;
        // Keep under 400 chars for TTS queue — lore body already capped
        if (spoken.length() > 380) spoken = spoken.substring(0, 377) + "...";
        return new NarrationRequest(Type.LORE_ENTRY, loreId, spoken, 0.4f);
    }

    public static NarrationRequest bossTitle(String bossId, String titleCard) {
        return new NarrationRequest(Type.BOSS_TITLE, bossId, titleCard, 0.9f);
    }
}
